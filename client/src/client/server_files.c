/************************************************************************
*            Atrinik, a Multiplayer Online Role Playing Game            *
*                                                                       *
*    Copyright (C) 2009-2010 Alex Tokar and Atrinik Development Team    *
*                                                                       *
* Fork from Daimonin (Massive Multiplayer Online Role Playing Game)     *
* and Crossfire (Multiplayer game for X-windows).                       *
*                                                                       *
* This program is free software; you can redistribute it and/or modify  *
* it under the terms of the GNU General Public License as published by  *
* the Free Software Foundation; either version 2 of the License, or     *
* (at your option) any later version.                                   *
*                                                                       *
* This program is distributed in the hope that it will be useful,       *
* but WITHOUT ANY WARRANTY; without even the implied warranty of        *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
* GNU General Public License for more details.                          *
*                                                                       *
* You should have received a copy of the GNU General Public License     *
* along with this program; if not, write to the Free Software           *
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.             *
*                                                                       *
* The author can be reached at admin@atrinik.org                        *
************************************************************************/

/**
 * @file
 * Manages server file updates. */

#include <include.h>

/** File names of the server files inside srv_files directory. */
static const char *const server_file_names[SERVER_FILES_MAX] =
{
	"skills", NULL, NULL, "anims", "bmaps",
	"hfiles", "updates", "spells", "settings"
};

/** Identifiers of the server files used in the setup command. */
static const char *const server_file_setup_names[SERVER_FILES_MAX] =
{
	"skf", NULL, NULL, "amf", "bpf",
	"hpf", "upf", "spfv2", "ssf"
};

/** Post-loading functions to call. */
static void (*server_file_funcs[SERVER_FILES_MAX])() =
{
	read_skills, NULL, NULL, NULL, read_bmaps,
	read_help_files, file_updates_parse, read_spells, server_settings_init
};

/** The server files. */
static server_files_struct server_files[SERVER_FILES_MAX];

/**
 * Initialize the necessary structures. */
void server_files_init()
{
	memset(&server_files, 0, sizeof(server_files));
}

/**
 * Load the server files. If they haven't changed since last load, no
 * loading will be done. */
void server_files_load()
{
	size_t i;
	FILE *fp;
	struct stat sb;
	size_t st_size, numread;
	char *contents;

	for (i = 0; i < SERVER_FILES_MAX; i++)
	{
		/* Invalid server file or it was previously loaded. */
		if (!server_file_names[i] || server_files[i].loaded)
		{
			continue;
		}

		/* Open the file. */
		fp = server_file_open(i);

		if (!fp)
		{
			return;
		}

		/* Get and store the size. */
		fstat(fileno(fp), &sb);
		st_size = sb.st_size;
		server_files[i].size = st_size;

		/* Allocate temporary buffer and read into it the file. */
		contents = malloc(st_size);
		numread = fread(contents, 1, st_size, fp);

		/* Calculate and store the checksum, free the temporary buffer
		 * and close the file pointer. */
		server_files[i].crc32 = crc32(1L, (const unsigned char FAR *) contents, numread);
		free(contents);
		fclose(fp);

		/* Mark that we have loaded this file. */
		server_files[i].loaded = 1;

		if (server_file_funcs[i])
		{
			server_file_funcs[i]();
		}
	}
}

/**
 * Construct a path to the specified @ref SERVER_FILE_xxx "file".
 * @param id ID of the server file, one of @ref SERVER_FILE_xxx.
 * @param[out] buf Will contain the constructed path.
 * @param buf_size Size of 'buf'.
 * @return 'buf'. */
static char *server_file_path(size_t id, char *buf, size_t buf_size)
{
	snprintf(buf, buf_size, "srv_files/%s", server_file_names[id]);
	return buf;
}

/**
 * Open a server file for reading.
 * @param id ID of the server file, one of @ref SERVER_FILE_xxx.
 * @return The file pointer, or NULL on failure of opening the file. */
FILE *server_file_open(size_t id)
{
	char buf[MAX_BUF];

	/* Doesn't exist. */
	if (!server_file_names[id])
	{
		return NULL;
	}

	server_file_path(id, buf, sizeof(buf));
	return fopen_wrapper(buf, "rb");
}

/**
 * Mark a server file for update.
 * @param id ID of the server file, one of @ref SERVER_FILE_xxx. */
void server_file_mark_update(size_t id)
{
	server_files[id].update = 1;
}

/**
 * We have received the server file we asked for, so save it to disk.
 * @param id ID of the server file, one of @ref SERVER_FILE_xxx.
 * @param data The data to save.
 * @param len Length of 'data'. */
void server_file_save(size_t id, unsigned char *data, size_t len)
{
	char path[MAX_BUF];
	FILE *fp;

	/* Finished updating. */
	server_files[id].update = 0;

	server_file_path(id, path, sizeof(path));
	fp = fopen_wrapper(path, "wb");

	if (!fp)
	{
		LOG(llevBug, "server_file_save(): Can't open %s for writing.\n", path);
		return;
	}

	if (fwrite(data, 1, len, fp) != len)
	{
		LOG(llevBug, "server_file_save(): Failed to write to %s.\n", path);
	}
	else
	{
		/* Mark the server file for reload. */
		server_files[id].loaded = 0;
	}

	fclose(fp);
}

/**
 * Figure out whether there are server files being updated.
 * @return 1 if there are any server files being updated, 0 otherwise. */
int server_files_updating()
{
	size_t i;

	/* Check all files. */
	for (i = 0; i < SERVER_FILES_MAX; i++)
	{
		/* The server file was marked for update previously, so start
		 * updating. */
		if (server_files[i].update == 1)
		{
			char buf[MAX_BUF];

			snprintf(buf, sizeof(buf), "rf %"FMT64U, (uint64) i);
			cs_write_string(buf, strlen(buf));
			/* Mark the file as 'being updated'. */
			server_files[i].update = -1;
			return 1;
		}
		/* The file is being updated. */
		else if (server_files[i].update == -1)
		{
			return 1;
		}
	}

	return 0;
}

/**
 * Add data about server files we have to the setup string sent to the
 * server.
 * @param[out] buf Where to write.
 * @param buf_size Size of 'buf'. */
void server_files_setup_add(char *buf, size_t buf_size)
{
	size_t i;
	char tmp[MAX_BUF];

	/* Load up the files. */
	server_files_load();

	for (i = 0; i < SERVER_FILES_MAX; i++)
	{
		/* Invalid file. */
		if (!server_file_setup_names[i])
		{
			continue;
		}

		/* Add the server file identifier, its size and the checksum. */
		snprintf(tmp, sizeof(tmp), " %s %"FMT64U"|%lx", server_file_setup_names[i], (uint64) server_files[i].size, server_files[i].crc32);
		strncat(buf, tmp, buf_size - strlen(buf) - 1);
	}
}
