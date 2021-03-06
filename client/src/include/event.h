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
 * Event related header file. */

#ifndef EVENT_H
#define EVENT_H

#define MAX_KEYS 512

/** All the key functions. */
typedef enum key_funcs
{
	KEYFUNC_NO,
	KEYFUNC_RUN,
	KEYFUNC_MOVE,
	KEYFUNC_CONSOLE,
	KEYFUNC_CURSOR,
	KEYFUNC_RANGE,
	KEYFUNC_APPLY,
	KEYFUNC_DROP,
	KEYFUNC_GET,
	KEYFUNC_LOCK,
	KEYFUNC_MARK,
	KEYFUNC_EXAMINE,
	KEYFUNC_PAGEUP,
	KEYFUNC_PAGEDOWN,
	KEYFUNC_HELP,
	KEYFUNC_PAGEUP_TOP,
	KEYFUNC_PAGEDOWN_TOP,
	KEYFUNC_OPTION,
	KEYFUNC_SPELL,
	KEYFUNC_KEYBIND,
	KEYFUNC_SKILL,
	KEYFUNC_TARGET_ENEMY,
	KEYFUNC_TARGET_FRIEND,
	KEYFUNC_TARGET_SELF,
	KEYFUNC_FIREREADY,
	KEYFUNC_COMBAT,
	KEYFUNC_QLIST,
} key_funcs;

/** One key macro. */
typedef struct key_macro
{
	/** The macro's name. */
	char macro[64];

	/** Command to run. */
	char cmd[64];

	/** One of ::key_funcs. */
	key_funcs key;

	/** A default value for commands. */
	int value;

	/** The default send mode. */
	int mode;
} key_macro;

/** In what status we are in the key binding dialog. */
enum
{
	KEYBIND_STATUS_NO,
	KEYBIND_STATUS_EDIT,
	KEYBIND_STATUS_EDITKEY
};

extern int old_mouse_y;

enum
{
	DRAG_GET_STATUS = -1,
	DRAG_NONE,
	DRAG_IWIN_BELOW,
	DRAG_IWIN_INV,
	DRAG_QUICKSLOT,
	DRAG_QUICKSLOT_SPELL,
	DRAG_PDOLL
};

extern int cursor_type;
extern int itemExamined;

SDLKey get_action_keycode, drop_action_keycode;

typedef struct _keys
{
	int pressed; /*true: key is pressed*/
	uint32 time; /*tick time last repeat is initiated*/
} _keys;

_keys keys[MAX_KEYS];

#endif
