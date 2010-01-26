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
 * Event related header file. */

#ifndef EVENT_H
#define EVENT_H

#define MAX_KEYS    512
#define MAX_KEYMAP  512

typedef struct _key_macro
{
	/* The macro */
	char macro[64];

	/* Our command string */
	char cmd[64];

	/* Internal: Use this function to generate the command */
	int internal;

	/* A default value for commands */
	int value;

	/* The default send mode */
	int mode;

	int menu_mode;
} _key_macro;

/** In what status we are in the key binding dialog. */
enum
{
	KEYBIND_STATUS_NO,
	KEYBIND_STATUS_EDIT,
	KEYBIND_STATUS_EDITKEY
};

/** All the key functions. */
enum
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
	KEYFUNC_LAYER0,
	KEYFUNC_LAYER1,
	KEYFUNC_LAYER2,
	KEYFUNC_LAYER3,
	KEYFUNC_TARGET_ENEMY,
	KEYFUNC_TARGET_FRIEND,
	KEYFUNC_TARGET_SELF,
	KEYFUNC_FIREREADY,
	KEYFUNC_COMBAT,
	KEYFUNC_QLIST
};

/** Keybind structure */
typedef struct _keybind_key
{
	/** The macro */
	char macro[256];

	/** The text */
	char keyname[256];

	/** -1: new macro - 0-xx edit entry */
	int entry;

	/** Key ID. */
	int key;

	/** Repeat? */
	int repeat_flag;
}_keybind_key;

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

/* Dor debug/alpha, remove later */
extern int KeyScanFlag;
extern int cursor_type;

/* Whether there is an event (removed at end of main loop) */
extern int MouseEvent;
extern int itemExamined;

/* State of the buttons */
extern uint32 MouseState;
extern _key_macro defkey_macro[];
extern const int DEFAULT_KEYMAP_MACROS;

/* Use these constants to determine the state of mouse and its events */
enum
{
	/* Don't change this to 0 (enum default is 0, so leave this as is) */
	IDLE = 1,
	LB_DN,
	LB_UP,
	RB_DN,
	RB_UP,
	MB_UP,
	MB_DN
};

#endif
