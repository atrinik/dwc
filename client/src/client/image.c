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
 * Handles image related code. */

#include <include.h>

/** Default bmaps loaded from atrinik.p0. */
static bmap_struct *bmaps_default[BMAPS_MAX];
/** Bmaps loaded from the server bmaps file. */
static bmap_struct *bmaps = NULL;
/** Number of entries in ::bmaps. */
static size_t bmaps_size = 0;

/**
 * Hash string to find it in the ::bmaps array.
 * @param str The string to hash.
 * @return Hashed string. */
static unsigned long bmap_hash(const char *str)
{
	unsigned long hash = 0;
	int i = 0;
	unsigned int rot = 0;
	const char *p;

	for (p = str; i < MAXSTRING && *p; p++, i++)
	{
		hash ^= (unsigned long) *p << rot;
		rot += 2;

		if (rot >= (sizeof(unsigned long) - sizeof(char)) * CHAR_BIT)
		{
			rot = 0;
		}
	}

	return hash % BMAPS_MAX;
}

/**
 * Find a bmap by name.
 * @param name The bmap name to find.
 * @return NULL if not found, pointer to the bmap otherwise. */
bmap_struct *bmap_find(const char *name)
{
	bmap_struct *bmap;
	unsigned long index;

	if (name == NULL)
	{
		return NULL;
	}

	index = bmap_hash(name);

	for (; ;)
	{
		bmap = bmaps_default[index];

		/* Not in the array. */
		if (!bmap)
		{
			return NULL;
		}

		if (!strcmp(bmap->name, name))
		{
			return bmap;
		}

		if (++index >= BMAPS_MAX)
		{
			index = 0;
		}
	}
}

/**
 * Add a bmap to the ::bmaps array.
 * @param at The bitmap to add. */
void bmap_add(bmap_struct *bmap)
{
	unsigned long index = bmap_hash(bmap->name), orig_index = index;

	for (; ;)
	{
		if (bmaps_default[index] && !strcmp(bmaps_default[index]->name, bmap->name))
		{
			LOG(llevBug, "bmap_add(): Double use of bmap name %s.\n", bmap->name);
		}

		if (!bmaps_default[index])
		{
			bmaps_default[index] = bmap;
			return;
		}

		if (++index == BMAPS_MAX)
		{
			index = 0;
		}

		if (index == orig_index)
		{
			LOG(llevBug, "bmap_add(): bmaps array is too small for %s.\n", bmap->name);
			return;
		}
	}
}

/**
 * Read bmaps from atrinik.p0, calculate checksums, etc. */
void read_bmaps_p0()
{
	FILE *fp;
	size_t tmp_buf_size, pos;
	char *tmp_buf, buf[MAX_BUF], *cp, *end;
	size_t len;
	bmap_struct *bmap;

	fp = fopen_wrapper(FILE_ATRINIK_P0, "rb");

	if (!fp)
	{
		LOG(llevError, "%s doesn't exist.\n", FILE_ATRINIK_P0);
	}

	memset((void *) bmaps_default, 0, BMAPS_MAX * sizeof(bmap_struct *));

	tmp_buf_size = 24 * 1024;
	tmp_buf = malloc(tmp_buf_size);

	while (fgets(buf, sizeof(buf) - 1, fp) != NULL)
	{
		if (strncmp(buf, "IMAGE ", 6))
		{
			LOG(llevError, "The file %s is corrupted.\n", FILE_ATRINIK_P0);
		}

		/* Skip accross the image ID data. */
		for (cp = buf + 6; *cp != ' '; cp++)
		{
		}

		len = atoi(cp);

		/* Skip accross the length data. */
		for (cp = cp + 1; *cp != ' '; cp++)
		{
		}

		/* Adjust the buffer if necessary. */
		if (len > tmp_buf_size)
		{
			tmp_buf_size = len;
			tmp_buf = realloc(tmp_buf, tmp_buf_size);
		}

		pos = ftell(fp);

		if (!fread(tmp_buf, 1, len, fp))
		{
			break;
		}

		/* Eliminate newline. */
		end = strchr(cp, '\n');

		if (end)
		{
			*end = '\0';
		}

		/* Trim left whitespace. */
		while (*cp == ' ')
		{
			cp++;
		}

		bmap = malloc(sizeof(bmap_struct));
		bmap->name = strdup(cp);
		bmap->crc32 = crc32(1L, (const unsigned char FAR *) tmp_buf, len);
		bmap->len = len;
		bmap->pos = pos;
		bmap_add(bmap);
	}

	free(tmp_buf);
	fclose(fp);
}

/**
 * Read bmaps server file. */
void read_bmaps()
{
	FILE *fp;
	char buf[HUGE_BUF], name[MAX_BUF];
	uint32 len, crc32;
	bmap_struct *bmap;
	size_t i;

	fp = server_file_open(SERVER_FILE_BMAPS);

	if (!fp)
	{
		return;
	}

	/* Free previously allocated bmaps. */
	if (bmaps)
	{
		size_t i;

		for (i = 0; i < bmaps_size; i++)
		{
			free(bmaps[i].name);
		}

		free(bmaps);
		bmaps_size = 0;
		bmaps = NULL;
	}

	for (i = 0; i < MAX_FACE_TILES; i++)
	{
		if (FaceList[i].name)
		{
			free(FaceList[i].name);
			FaceList[i].name = NULL;
			sprite_free_sprite(FaceList[i].sprite);
			FaceList[i].sprite = NULL;
			FaceList[i].checksum = 0;
			FaceList[i].flags = 0;
		}
	}

	while (fgets(buf, sizeof(buf) - 1, fp))
	{
		if (sscanf(buf, "%x %x %s", &len, &crc32, name) != 3)
		{
			LOG(llevBug, "Syntax error in server bmaps file: %s\n", buf);
			break;
		}

		/* Find the bmap. */
		bmap = bmap_find(name);
		/* Expand the array. */
		bmaps = realloc(bmaps, sizeof(*bmaps) * (bmaps_size + 1));

		/* Does it exist, and the lengths and checksums match? */
		if (bmap && bmap->len == len && bmap->crc32 == crc32)
		{
			bmaps[bmaps_size].pos = bmap->pos;
		}
		/* It doesn't exist in the atrinik.p0 file. */
		else
		{
			bmaps[bmaps_size].pos = -1;
		}

		bmaps[bmaps_size].len = len;
		bmaps[bmaps_size].crc32 = crc32;
		bmaps[bmaps_size].name = strdup(name);

		bmaps_size++;
	}

	fclose(fp);
}

/**
 * Finish face command.
 * @param pnum ID of the face.
 * @param checksum Face checksum.
 * @param face Face name. */
void finish_face_cmd(int pnum, uint32 checksum, char *face)
{
	char buf[2048];
	FILE *stream;
	struct stat statbuf;
	size_t len;
	static uint32 newsum = 0;
	unsigned char *data;

	/* Loaded or requested. */
	if (FaceList[pnum].name)
	{
		/* Let's check the name, checksum and sprite. Only if all is ok,
		 * we stay with it */
		if (!strcmp(face, FaceList[pnum].name) && checksum == FaceList[pnum].checksum && FaceList[pnum].sprite)
		{
			return;
		}

		/* Something is different. */
		free(FaceList[pnum].name);
		FaceList[pnum].name = NULL;
		sprite_free_sprite(FaceList[pnum].sprite);
	}

	snprintf(buf, sizeof(buf), "%s.png", face);
	FaceList[pnum].name = (char *) malloc(strlen(buf) + 1);
	strcpy(FaceList[pnum].name, buf);

	FaceList[pnum].checksum = checksum;

	/* Check private cache first */
	snprintf(buf, sizeof(buf), DIRECTORY_CACHE"/%s", FaceList[pnum].name);

	if ((stream = fopen_wrapper(buf, "rb")) != NULL)
	{
		fstat(fileno (stream), &statbuf);
		len = statbuf.st_size;
		data = malloc(len);
		len = fread(data, 1, len, stream);
		fclose(stream);
		newsum = 0;

		/* Something is wrong... Unlink the file and let it reload. */
		if (len <= 0)
		{
			unlink(buf);
			checksum = 1;
		}
		/* Checksum check */
		else
		{
			newsum = crc32(1L, data, len);
		}

		free(data);

		if (newsum == checksum)
		{
			FaceList[pnum].sprite = sprite_tryload_file(buf, 0, NULL);

			if (FaceList[pnum].sprite)
			{
				return;
			}
		}
	}

	snprintf(buf, sizeof(buf), "askface %d", pnum);
	cs_write_string(buf, strlen(buf));
}

/**
 * Load picture from atrinik.p0 file.
 * @param num ID of the picture to load.
 * @return 1 if the file does not exist, 0 otherwise. */
static int load_picture_from_pack(int num)
{
	FILE *stream;
	char *pbuf;
	SDL_RWops *rwop;

	if ((stream = fopen_wrapper(FILE_ATRINIK_P0, "rb")) == NULL)
	{
		return 1;
	}

	lseek(fileno(stream), bmaps[num].pos, SEEK_SET);

	pbuf = malloc(bmaps[num].len);

	if (!fread(pbuf, bmaps[num].len, 1, stream))
	{
		fclose(stream);
		return 0;
	}

	fclose(stream);

	rwop = SDL_RWFromMem(pbuf, bmaps[num].len);

	FaceList[num].sprite = sprite_tryload_file(NULL, 0, rwop);

	SDL_FreeRW(rwop);
	free(pbuf);

	return 0;
}

/**
 * Load face from user's graphics directory.
 * @param num ID of the face to load.
 * @return 1 on success, 0 on failure. */
static int load_gfx_user_face(uint16 num)
{
	char buf[MAX_BUF];
	FILE *stream;
	struct stat statbuf;
	size_t len;
	unsigned char *data;

	/* First check for this image in gfx_user directory. */
	snprintf(buf, sizeof(buf), DIRECTORY_GFX_USER"/%s.png", bmaps[num].name);

	if ((stream = fopen_wrapper(buf, "rb")) != NULL)
	{
		fstat(fileno(stream), &statbuf);
		len = statbuf.st_size;
		data = malloc(len);
		len = fread(data, 1, len, stream);
		fclose(stream);

		if (len > 0)
		{
			/* Try to load it. */
			FaceList[num].sprite = sprite_tryload_file(buf, 0, NULL);

			if (FaceList[num].sprite)
			{
				snprintf(buf, sizeof(buf), DIRECTORY_GFX_USER"/%s.png", bmaps[num].name);
				FaceList[num].name = (char *) malloc(strlen(buf) + 1);
				strcpy(FaceList[num].name, buf);
				FaceList[num].checksum = crc32(1L, data, len);
				free(data);
				return 1;
			}
		}

		/* If we are here something was wrong with the file. */
		free(data);
	}

	return 0;
}

/**
 * We got a face - test if we have it loaded. If not, ask the server to
 * send us face command.
 * @param pnum Face ID.
 * @return 0 if face is not there, 1 if face was requested or loaded. */
int request_face(int pnum)
{
	char buf[MAX_BUF];
	uint16 num = (uint16) (pnum &~ 0x8000);

	if (options.reload_gfx_user && load_gfx_user_face(num))
	{
		return 1;
	}

	/* Loaded or requested */
	if (FaceList[num].name || FaceList[num].flags & FACE_REQUESTED)
	{
		return 1;
	}

	if (num >= bmaps_size)
	{
		LOG(llevBug, "request_face(): Server sent picture ID too big (%d, max: %"FMT64U")\n", num, (uint64) bmaps_size);
		return 0;
	}

	if (load_gfx_user_face(num))
	{
		return 1;
	}

	/* Best case - we have it in atrinik.p0 */
	if (bmaps[num].pos != -1)
	{
		snprintf(buf, sizeof(buf), "%s.png", bmaps[num].name);
		FaceList[num].name = (char *) malloc(strlen(buf) + 1);
		strcpy(FaceList[num].name, buf);
		FaceList[num].checksum = bmaps[num].crc32;
		load_picture_from_pack(num);
	}
	/* Second best case - check the cache for it, or request it. */
	else
	{
		FaceList[num].flags |= FACE_REQUESTED;
		finish_face_cmd(num, bmaps[num].crc32, bmaps[num].name);
	}

	return 1;
}

/**
 * Find a face ID by name. Request the face by finding it, loading it or requesting it.
 * @param name Face name to find
 * @return Face ID if found, -1 otherwise */
int get_bmap_id(char *name)
{
	int l = 0, r = bmaps_size - 1, x;

	while (r >= l)
	{
		x = (l + r) / 2;

		if (strcmp(name, bmaps[x].name) < 0)
		{
			r = x - 1;
		}
		else
		{
			l = x + 1;
		}

		if (!strcmp(name, bmaps[x].name))
		{
			request_face(x);
			return x;
		}
	}

	return -1;
}

/**
 * Blit a face.
 * @param id ID of the face.
 * @param x X position.
 * @param y Y position. */
void blit_face(int id, int x, int y)
{
	if (id == -1 || !FaceList[id].sprite)
	{
		return;
	}

	if (FaceList[id].sprite->status != SPRITE_STATUS_LOADED)
	{
		return;
	}

	sprite_blt(FaceList[id].sprite, x, y, NULL, NULL);
}
