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
 * Structures and defines related to living objects, including stats
 * of objects. */

#ifndef LIVING_H
#define LIVING_H

/**
 * @defgroup STATS Object statistics */
/*@{*/
/** Strength. */
#define STR             0
/** Dexterity. */
#define DEX             1
/** Constitution. */
#define CON             2
/** Wisdom. */
#define WIS             3
/** Charisma. */
#define CHA             4
/** Intelligence. */
#define INTELLIGENCE    5
/** Power. */
#define POW             6
/** Number of stats. */
#define NUM_STATS       7
/*@}*/

/* Changed from NO_STAT to NO_STAT_VAL to fix conlfict on
 * AIX systems */

/* needed by skills code -b.t. */
#define NO_STAT_VAL 99

extern int dam_bonus[MAX_STAT + 1];
extern int thaco_bonus[MAX_STAT + 1];
extern float cha_bonus[MAX_STAT + 1];
extern float speed_bonus[MAX_STAT + 1];
extern uint32 weight_limit[MAX_STAT + 1];
extern int learn_spell[MAX_STAT + 1];
extern int savethrow[MAXLEVEL + 1];
extern const char *const restore_msg[NUM_STATS];
extern const char *const lose_msg[NUM_STATS];
extern const char *const statname[NUM_STATS];
extern const char *const short_stat_name[NUM_STATS];

extern char *spellpathnames[NRSPELLPATHS];

/**
 * Calculates damage based on level. */
#define LEVEL_DAMAGE(level) (float) ((level) ? 0.75f + (level) * 0.25f : 1.0f)

#ifdef WIN32
#pragma pack(push,1)
#endif

/**
 * Mostly used by "alive" objects, but also by other objects like gates,
 * buttons, waypoints and a number of other objects. */
typedef struct liv
{
	/** Experience. */
	sint64 exp;

	/** Hit points. */
	sint32 hp;

	/** Max hit points. */
	sint32 maxhp;

	/** Spell points. Used to cast mage spells. */
	sint16 sp;

	/** Max spell points. */
	sint16 maxsp;

	/** Grace. Used to invoke clerical prayers. */
	sint16 grace;

	/** Max grace. */
	sint16 maxgrace;

	/** How much food in stomach. 0 = starved. */
	sint16 food;

	/** How much damage this object does when hitting. */
	sint16 dam;

	/** Weapon class. */
	sint16 wc;

	/** Armour class. */
	sint16 ac;

	/**
	 * Random value range we add to wc value of attacker:
	 * wc + (random() % wc_range). If it's higher than
	 * defender's AC then we can hit our enemy. */
	uint8 wc_range;

	/** Strength. */
	sint8 Str;

	/** Dexterity. */
	sint8 Dex;

	/** Constitution. */
	sint8 Con;

	/** Wisdom. */
	sint8 Wis;

	/** Charisma. */
	sint8 Cha;

	/** Intelligence. */
	sint8 Int;

	/** Power. */
	sint8 Pow;
} living;

#ifdef WIN32
#pragma pack(pop)
#endif

#endif
