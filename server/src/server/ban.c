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
 * Banning related functions.
 *
 * It is possible to use the /ban DM command or interactive server mode
 * ban command to ban specified player or IP from the game.
 *
 * Syntax for banning:
 * Player:IP
 *
 * Where Player is the player name and IP is the IP address. It is
 * possible to use * for both IP and player, which means any match. */

#include <global.h>

/** The list of the bans. */
static objectlink *ban_list = NULL;

/**
 * Add a ban entry to ::ban_list.
 * @param name Name to ban.
 * @param ip IP to ban. */
static void add_ban_entry(char *name, char *ip)
{
	objectlink *ol = get_objectlink();
	_ban_struct *gptr = (_ban_struct *) get_poolchunk(pool_bans);

	memset(gptr, 0, sizeof(_ban_struct));
	ol->objlink.ban = gptr;

	ol->objlink.ban->ip = strdup_local(ip);
	FREE_AND_COPY_HASH(ol->objlink.ban->name, name);
	objectlink_link(&ban_list, NULL, NULL, ban_list, ol);
}

/**
 * Remove a ban entry from ::ban_list.
 * @param ol Pointer to the objectlink to remove. */
static void remove_ban_entry(objectlink *ol)
{
	free(ol->objlink.ban->ip);
	FREE_AND_CLEAR_HASH(ol->objlink.ban->name);
	objectlink_unlink(&ban_list, NULL, ol);
	return_poolchunk(ol->objlink.ban, pool_bans);
	return_poolchunk(ol, pool_objectlink);
}

/**
 * Load the ban file. */
void load_bans_file()
{
	char filename[MAX_BUF], buf[MAX_BUF], name[64], ip[64];
	FILE *fp;

	snprintf(filename, sizeof(filename), "%s/%s", settings.localdir, BANFILE);

	if (!(fp = fopen(filename, "r")))
	{
		return;
	}

	while (fgets(buf, sizeof(buf), fp))
	{
		/* Skip comments and blank lines. */
		if (buf[0] == '#' || buf[0] == '\n')
		{
			continue;
		}

		if (sscanf(buf, "%s %s", name, ip) == 2)
		{
			add_ban_entry(name, ip);
		}
		else
		{
			LOG(llevBug, "BUG: Malformed line in bans file: %s\n", buf);
		}
	}

	fclose(fp);
}

/**
 * Save the bans file. */
void save_bans_file()
{
	char filename[MAX_BUF];
	FILE *fp;
	objectlink *ol;

	snprintf(filename, sizeof(filename), "%s/%s", settings.localdir, BANFILE);

	if (!(fp = fopen(filename, "w")))
	{
		LOG(llevBug, "BUG: Cannot open %s for writing.\n", filename);
		return;
	}

	for (ol = ban_list; ol; ol = ol->next)
	{
		fprintf(fp, "%s %s\n", ol->objlink.ban->name, ol->objlink.ban->ip);
	}

	fclose(fp);
}

/**
 * Check if this player or host is banned.
 * @param name Login name to check.
 * @param ip Host name to check.
 * @return 1 if banned, 0 if not. */
int checkbanned(const char *name, char *ip)
{
	objectlink *ol;

	for (ol = ban_list; ol; ol = ol->next)
	{
		int name_matches = name && (ol->objlink.ban->name[0] == '*' || ol->objlink.ban->name == name);

		if ((name_matches || ol->objlink.ban->name[0] == '*') && (ol->objlink.ban->ip[0] == '*' || strstr(ip, ol->objlink.ban->ip) || !strcmp(ip, ol->objlink.ban->ip)))
		{
			return 1;
		}
	}

	return 0;
}

/**
 * Add a ban. Will take care of getting the right values from input
 * string.
 * @param input The input string with both name and IP.
 * @return 1 on success, 0 on failure. */
int add_ban(char *input)
{
	char *tmp[2];

	if (split_string(input, tmp, sizeof(tmp) / sizeof(*tmp), ':') != 2 || *tmp[1] == '\0')
	{
		return 0;
	}

	/* IPs with ':' in them will be impossible to remove once added. */
	if (strstr(tmp[1], ":"))
	{
		return 0;
	}

	add_ban_entry(tmp[0], tmp[1]);
	save_bans_file();
	return 1;
}

/**
 * Remove a ban. Will take care of getting the right values from input
 * string.
 * @param input The input string with both name and IP.
 * @return 1 on success, 0 on failure. */
int remove_ban(char *input)
{
	char *tmp[2];
	objectlink *ol;

	if (split_string(input, tmp, sizeof(tmp) / sizeof(*tmp), ':') != 2 || *tmp[1] == '\0')
	{
		return 0;
	}

	for (ol = ban_list; ol; ol = ol->next)
	{
		if (!strcmp(ol->objlink.ban->name, tmp[0]) && !strcmp(ol->objlink.ban->ip, tmp[1]))
		{
			remove_ban_entry(ol);
			save_bans_file();
			return 1;
		}
	}

	return 0;
}

/**
 * List all bans.
 * @param op Player object to print this information to, NULL to output
 * it to the log. */
void list_bans(object *op)
{
	objectlink *ol;

	if (op)
	{
		new_draw_info(NDI_UNIQUE, op, "List of bans:");
	}
	else
	{
		LOG(llevInfo, "\nList of bans:\n");
	}

	for (ol = ban_list; ol; ol = ol->next)
	{
		if (op)
		{
			new_draw_info_format(NDI_UNIQUE, op, "%s:%s", ol->objlink.ban->name, ol->objlink.ban->ip);
		}
		else
		{
			LOG(llevInfo, "%s:%s\n", ol->objlink.ban->name, ol->objlink.ban->ip);
		}
	}
}
