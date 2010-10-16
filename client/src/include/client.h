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

#include "include.h"

#define MAX_BUF 256

/* How many skill types server supports/client will get sent to it.
 * If more skills are added to server, this needs to get increased. */
#define MAX_SKILL   6

/* Defines for the ext map command */

/* Object is sleeping */
#define FFLAG_SLEEP		0x01
/* Object is confused */
#define FFLAG_CONFUSED	0x02
/* Object is paralyzed */
#define FFLAG_PARALYZED	0x04
/* Object is scared */
#define FFLAG_SCARED	0x08
/* Object is blinded */
#define FFLAG_BLINDED	0x10
/* Object is invisible (but when sent, player can see it) */
#define FFLAG_INVISIBLE	0x20
/* Object is ethereal - but when sent, object can be seen */
#define FFLAG_ETHEREAL	0x40
/* Object is target of player */
#define FFLAG_PROBE		0x80

#define INPUT_MODE_NO		0
#define INPUT_MODE_CONSOLE	1
#define INPUT_MODE_NUMBER	4
#define INPUT_MODE_GETKEY	8

#define NUM_MODE_GET  1
#define NUM_MODE_DROP 2

/* Values for send_command option */
#define SC_NORMAL 0

/** Screensize structure */
typedef struct screensize
{
	/** Screen X */
	int x;

	/** Screen Y */
	int y;
} _screensize;

extern struct screensize *Screensize;

typedef struct Animations
{
	/* 0 = all fields are invalid, 1 = anim is loaded */
	int loaded;

	/* Length of one a animation frame (num_anim / facings) */
	int frame;
	uint16 *faces;

	/* Number of frames */
	uint8 facings;

	/* Number of animations. Value of 2 means
	 * only faces[0], [1] have meaningful values. */
	uint8 num_animations;
	uint8 flags;
} Animations;

typedef struct _anim_table
{
	/* Length of anim_cmd data */
	int len;

	/* Faked animation command */
	char *anim_cmd;
}_anim_table;

extern _anim_table *anim_table;
extern Animations *animations;
extern size_t animations_num;

/**
 * Contains the base information we use to make up a packet we want to send. */
typedef struct SockList
{
	/**
	 * How much data in buf. */
	int len;
	/**
	 * Start of data in buf. */
	int pos;
	/**
	 * Daa. */
	unsigned char *buf;
} SockList;

/* ClientSocket could probably hold more of the global values - it could
 * probably hold most all socket/communication related values instead
 * of globals. */
typedef struct ClientSocket
{
	/* Typedef your socket type to SOCKET */
	SOCKET fd;
} ClientSocket;

extern ClientSocket csocket;

typedef struct Stat_struct
{
	sint8 Str, Dex, Con, Wis, Cha, Int, Pow;

	/* Weapon Class and Armour Class */
	sint16 wc, ac;
	sint8 level;

	/* Hit Points. */
	sint32 hp;

	/* Max hit points*/
	sint32 maxhp;

	/* Spell points.  Used to cast spells. */
	sint32 sp;

	/* Max spell points. */
	sint32 maxsp;

	/* Grace.  Used to cast prayers. */
	sint32 grace;

	/* Max grace */
	sint32 maxgrace;

	/* Experience.  Killers gain 1/10. */
	sint64 exp;

	/* How much food in stomach.  0 = starved. */
	sint16 food;

	/* How much damage this object does when hitting */
	sint16 dam;

	/* Gets converted to a float for display */
	sint32 speed;

	/* Gets converted to a float for display */
	int weapon_sp;

	/* Contains fire on/run on flags */
	uint16 flags;

	/* Resistant values */
	sint16 protection[20];

	/* Level and experience totals for */
	sint16 skill_level[MAX_SKILL];

	/* Skills */
	sint64 skill_exp[MAX_SKILL];
} Stats;

typedef enum _inventory_win
{
	IWIN_BELOW,
	IWIN_INV
}_inventory_win;

/** The player structure. */
typedef struct Player_Struct
{
	/* Player object */
	item *ob;

	/* Items below the player (pl.below->inv) */
	item *below;

	/* inventory of a open container */
	item *sack;

	/* Pointer to open container */
	item *container;

	/** Inventory of items in shop */
	item *shop;

	/* Tag of the container */
	sint32 container_tag;

	uint32 weight_limit;

	/* Repeat count on command */
	uint32 count;

	/** Are we a DM? */
	int dm;

	/* Target mode */
	int	target_mode;

	/* Target */
	int	target_code;

	/* Target's color */
	int	target_color;

	/* Inventory windows */
	int inventory_win;

	/* Menu that is opened */
	int menustatus;

	int loc;
	int tag;
	int nrof;

	/* Skill group and entry of ready skill */
	int skill_g;
	int skill_e;

	int warn_hp;

	int win_inv_slot;
	int win_inv_tag;
	int win_quick_tag;
	int win_pdoll_tag;
	int win_inv_start;
	int win_inv_count;
	int win_inv_ctag;

	int win_below_slot;
	int win_below_tag;
	int win_below_start;
	int win_below_count;
	int win_below_ctag;

	/* Input mode: no, console (textstring), numinput */
	int input_mode;
	int	nummode;

	/** Currently marked item. */
	int mark_count;

	/* HP, mana and grace regeneration */
	float gen_hp;
	float gen_sp;
	float gen_grace;

	/* Skill cooldown time */
	float action_timer;

	/* True if fire key is pressed = action key (ALT;CTRL) */
	uint32 fire_on:1;

	/* True if run key is on = action key (ALT;CTRL) */
	uint32 run_on:1;

	/* True if fire key is pressed = permanent mode */
	uint32 firekey_on:1;

	/* True if run key is pressed = permanent mode */
	uint32 runkey_on:1;

	float window_weight;
	float real_weight;

	int warn_statdown;
	int warn_statup;
	int warn_drain;

	/* Player stats */
	Stats stats;

	/* HP of our target in % */
	char target_hp;

	/* Last command entered */
	char last_command[MAX_BUF];

	/* Name and password.  Only used while logging in. */
	char name[40];
	char password[40];

	/* Target name */
	char target_name[MAX_BUF];
	char num_text[300];
	char skill_name[128];

	/* Rank and name of char */
	char rankandname[MAX_BUF];

	/* Name of char */
	char pname[MAX_BUF];

	/* Race and profession of character */
	char race[MAX_BUF];
	char title[MAX_BUF];

	/* Rank */
	char rank[MAX_BUF];

	/* God */
	char godname[MAX_BUF];

	/* Alignment */
	char alignment[MAX_BUF];

	/* Gender */
	char gender[MAX_BUF];

	/* Range attack chosen */
	char range[MAX_BUF];

	/* Party name this player is member of */
	char partyname[MAX_BUF];

	/** Whom to reply to. */
	char player_reply[64];
} Client_Player;

/* Player object. */
extern Client_Player cpl;

/* These are multiplication values that should be used when changing
 * floats to ints, and vice version.  MULTI is integer representatin
 * (float to int), MULTF is float, for going from int to float. */
#define FLOAT_MULTI	100000
#define FLOAT_MULTF	100000.0

/* ID's for the various stats that get sent across. */
#define CS_STAT_HP	 		1
#define CS_STAT_MAXHP		2
#define CS_STAT_SP	 		3
#define CS_STAT_MAXSP		4
#define CS_STAT_STR	 		5
#define CS_STAT_INT	 		6
#define CS_STAT_WIS	 		7
#define CS_STAT_DEX	 		8
#define CS_STAT_CON	 		9
#define CS_STAT_CHA			10
#define CS_STAT_EXP			11
#define CS_STAT_LEVEL		12
#define CS_STAT_WC			13
#define CS_STAT_AC			14
#define CS_STAT_DAM			15
#define CS_STAT_ARMOUR		16
#define CS_STAT_SPEED		17
#define CS_STAT_FOOD		18
#define CS_STAT_WEAP_SP 	19
#define CS_STAT_RANGE		20
#define CS_STAT_TITLE		21
#define CS_STAT_POW			22
#define CS_STAT_GRACE		23
#define CS_STAT_MAXGRACE	24
#define CS_STAT_FLAGS		25
#define CS_STAT_WEIGHT_LIM	26
#define CS_STAT_EXT_TITLE 	27
#define CS_STAT_REG_HP 		28
#define CS_STAT_REG_MANA 	29
#define CS_STAT_REG_GRACE 	30
#define CS_STAT_TARGET_HP 	31
#define CS_STAT_ACTION_TIME	36

/* Start and end of resistances, inclusive. */
#define CS_STAT_RESIST_START	100
#define CS_STAT_RESIST_END		117
#define CS_STAT_RES_PHYS		100
#define CS_STAT_RES_MAG			101
#define CS_STAT_RES_FIRE		102
#define CS_STAT_RES_ELEC		103
#define CS_STAT_RES_COLD		104
#define CS_STAT_RES_CONF		105
#define CS_STAT_RES_ACID		106
#define CS_STAT_RES_DRAIN		107
#define CS_STAT_RES_GHOSTHIT	108
#define CS_STAT_RES_POISON		109
#define CS_STAT_RES_SLOW		110
#define CS_STAT_RES_PARA		111
#define CS_STAT_TURN_UNDEAD		112
#define CS_STAT_RES_FEAR		113
#define CS_STAT_RES_DEPLETE		114
#define CS_STAT_RES_DEATH		115
#define CS_STAT_RES_HOLYWORD	116
#define CS_STAT_RES_BLIND		117

/* Start and end of skill experience + skill level, inclusive. */
#define CS_STAT_SKILLEXP_START 		118
#define CS_STAT_SKILLEXP_END 		129
#define CS_STAT_SKILLEXP_AGILITY 	118
#define CS_STAT_SKILLEXP_AGLEVEL 	119
#define CS_STAT_SKILLEXP_PERSONAL 	120
#define CS_STAT_SKILLEXP_PELEVEL 	121
#define CS_STAT_SKILLEXP_MENTAL 	122
#define CS_STAT_SKILLEXP_MELEVEL 	123
#define CS_STAT_SKILLEXP_PHYSIQUE 	124
#define CS_STAT_SKILLEXP_PHLEVEL 	125
#define CS_STAT_SKILLEXP_MAGIC 		126
#define CS_STAT_SKILLEXP_MALEVEL 	127
#define CS_STAT_SKILLEXP_WISDOM 	128
#define CS_STAT_SKILLEXP_WILEVEL 	129

#define CS_STAT_PROT_START		130
#define CS_STAT_PROT_END		149

#define CS_STAT_PROT_HIT		130
#define CS_STAT_PROT_SLASH		131
#define CS_STAT_PROT_CLEAVE		132
#define CS_STAT_PROT_PIERCE		133
#define CS_STAT_PROT_WMAGIC		134

#define CS_STAT_PROT_FIRE		135
#define CS_STAT_PROT_COLD		136
#define CS_STAT_PROT_ELEC		137
#define CS_STAT_PROT_POISON		138
#define CS_STAT_PROT_ACID		139

#define CS_STAT_PROT_MAGIC		140
#define CS_STAT_PROT_MIND		141
#define CS_STAT_PROT_BODY		142
#define CS_STAT_PROT_PSIONIC	143
#define CS_STAT_PROT_ENERGY		144

#define CS_STAT_PROT_NETHER		145
#define CS_STAT_PROT_CHAOS		146
#define CS_STAT_PROT_DEATH		147
#define CS_STAT_PROT_HOLY		148
#define CS_STAT_PROT_CORRUPT	149

/* These are used with CS_STAT_FLAGS above to communicate S->C what the
 * server thinks the fireon & runon states are. */
#define SF_FIREON			1
#define SF_RUNON			2
#define SF_BLIND			4
#define SF_XRAYS			8
#define SF_INFRAVISION		16

/** Say command */
#define NDI_SAY     0x0100
/** The message is a shout */
#define NDI_SHOUT   0x0200
/** The message is a tell */
#define NDI_TELL    0x0400
/** This message comes from a player */
#define NDI_PLAYER  0x0800
/** Message is an emote command. */
#define NDI_EMOTE   0x01000
/**
 * Message will be played as animation in the middle of the client
 * screen. */
#define NDI_ANIM    0x02000

/* Flags for the item command */
#define F_APPLIED		0x000F
#define F_LOCATION		0x00F0
#define F_UNPAID		0x0200
#define F_MAGIC			0x0400
#define F_CURSED		0x0800
#define F_DAMNED		0x1000
#define F_OPEN			0x2000
#define F_NOPICK		0x4000
#define F_LOCKED		0x8000
#define F_TRAPPED       0x10000

#define UPD_LOCATION	0x01
#define UPD_FLAGS		0x02
#define UPD_WEIGHT		0x04
#define UPD_FACE		0x08
#define UPD_NAME		0x10
#define UPD_ANIM		0x20
#define UPD_ANIMSPEED	0x40
#define UPD_NROF		0x80
#define UPD_DIRECTION	0x100

/* White */
#define COLOR_DEFAULT 	0
#define COLOR_WHITE  	0
#define COLOR_ORANGE 	1
/* Navy */
#define COLOR_LBLUE  	2
#define COLOR_RED		3
#define COLOR_GREEN 	4
#define COLOR_BLUE  	5
#define COLOR_GREY  	6
#define COLOR_YELLOW  	7
#define COLOR_DK_NAVY  	8

/* Client only colors */
#define COLOR_HGOLD 	64
#define COLOR_DGOLD		65
#define COLOR_DBROWN  	44

#define COLOR_BLACK 	255

#define COLOR_FLAG_CLIPPED 0x0100

#define MAP_UPDATE_CMD_SAME 0
#define MAP_UPDATE_CMD_NEW 1
#define MAP_UPDATE_CMD_CONNECTED 2

/**
 * @defgroup MAP2_FLAG_xxx Map2 layer flags
 * Flags used to mark what kind of data there is on layers
 * in map2 protocol.
 *@{*/
/** Multi-arch object. */
#define MAP2_FLAG_MULTI      1
/** Player name. */
#define MAP2_FLAG_NAME       2
/** Target's HP bar. */
#define MAP2_FLAG_PROBE      4
/** Tile's Z position. */
#define MAP2_FLAG_HEIGHT     8
/** Zoom. */
#define MAP2_FLAG_ZOOM 16
/** X align. */
#define MAP2_FLAG_ALIGN 32
/** Draw the object twice. */
#define MAP2_FLAG_DOUBLE 64
/*@}*/

/**
 * @defgroup MAP2_FLAG_EXT_xxx Map2 tile flags
 * Flags used to mark what kind of data there is on different
 * tiles in map2 protocol.
 *@{*/
/** An animation. */
#define MAP2_FLAG_EXT_ANIM   1
/*@}*/

/**
 * @defgroup MAP2_MASK_xxx Map2 mask flags
 * Flags used for masks in map2 protocol.
 *@{*/
/** Clear cell, with all layers. */
#define MAP2_MASK_CLEAR      0x2
/** Add darkness. */
#define MAP2_MASK_DARKNESS   0x4
/*@}*/

/**
 * @defgroup MAP2_LAYER_xxx Map2 layer types
 *@{*/
/** Clear this layer. */
#define MAP2_LAYER_CLEAR 255
/*@}*/

/**
 * @defgroup CMD_QUICKSLOT_xxx Quickslot commands
 * The various quickslot commands.
 *@{*/
/** Set an item as a quickslot. Uses object::quickslot. */
#define CMD_QUICKSLOT_SET 1
/** Set a spell as a quickslot. Uses player::spell_quickslots. */
#define CMD_QUICKSLOT_SETSPELL 2
/** Unset a quickslot, be it spell or item. */
#define CMD_QUICKSLOT_UNSET 3
/*@}*/

/**
 * @defgroup QUICKSLOT_TYPE_xxx Quickslot data types
 * Quickslot data types.
 *@{*/
/** Item quickslot. */
#define QUICKSLOT_TYPE_ITEM 1
/** Spell quickslot. */
#define QUICKSLOT_TYPE_SPELL 2
/*@}*/

/**
 * @defgroup CMD_SOUND_xxx Sound command types
 * The sound command types.
 *@{*/
/** A sound effect, like poison, melee/range hit, spell sound, etc. */
#define CMD_SOUND_EFFECT 1
/** Background music. */
#define CMD_SOUND_BACKGROUND 2
/** Path to sound effect with an absolute filename. MIDI is not supported. */
#define CMD_SOUND_ABSOLUTE 3
/*@}*/
