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
 * Atrinik Python plugin object related code. */

#include <plugin_python.h>

/** Object fields structure */
typedef struct
{
	/** The field type */
	char *name;

	/** Type of the field */
	field_type type;

	/** Offset in object structure */
	uint32 offset;

	/** Flags for special handling */
	uint32 flags;

	/** Extra data for some special fields */
	uint32 extra_data;
} obj_fields_struct;

/**
 * All the possible fields of an object.
 *
 * @todo Message field needs special handling (check for endmsg, limit to
 * 4096 characters.
 * @todo Limit weight to >= 0
 * @todo Maximum 100000 quantity
 * @todo Make enemy and owner settable (requires HOOK to set_npc_enemy()
 * and set_owner().
 * @todo Limit last_sp and last_grace to max 16000?
 * @todo -10.0 \< speed \< 10.0, also might want to call
 * update_object_speed()
 * @todo Limit food to max 999 (at least to players)?
 * @todo Damage: limit to 0 <= dam <= 120?
 * @todo Limit hitpoints, spellpoints and grace to +/- 16000?
 * @todo Limit weapon_class and armour_class to +/- 120.
 * @todo Limit all player stats to +/- 30. */
obj_fields_struct obj_fields[] =
{
	{"below",                  FIELDTYPE_OBJECT,     offsetof(object, below),                  FIELDFLAG_READONLY,          0},
	{"above",                  FIELDTYPE_OBJECT,     offsetof(object, above),                  FIELDFLAG_READONLY,          0},
	{"inventory",              FIELDTYPE_OBJECT,     offsetof(object, inv),                    FIELDFLAG_READONLY,          0},
	{"environment",            FIELDTYPE_OBJECT,     offsetof(object, env),                    FIELDFLAG_READONLY,          0},
	{"map",                    FIELDTYPE_MAP,        offsetof(object, map),                    FIELDFLAG_READONLY,          0},
	{"name",                   FIELDTYPE_SHSTR,      offsetof(object, name),                   FIELDFLAG_PLAYER_READONLY,   0},
	{"title",                  FIELDTYPE_SHSTR,      offsetof(object, title),                  0,                           0},
	{"race",                   FIELDTYPE_SHSTR,      offsetof(object, race),                   0,                           0},
	{"slaying",                FIELDTYPE_SHSTR,      offsetof(object, slaying),                0,                           0},
	{"message",                FIELDTYPE_SHSTR,      offsetof(object, msg),                    0,                           0},
	{"artifact",               FIELDTYPE_SHSTR,      offsetof(object, artifact),               0,                           0},
	{"weight",                 FIELDTYPE_SINT32,     offsetof(object, weight),                 0,                           0},

	{"weight_limit",           FIELDTYPE_UINT32,     offsetof(object, weight_limit),           0,                           0},
	{"carrying",               FIELDTYPE_SINT32,     offsetof(object, carrying),               0,                           0},
	{"path_attuned",           FIELDTYPE_UINT32,     offsetof(object, path_attuned),           0,                           0},
	{"path_repelled",          FIELDTYPE_UINT32,     offsetof(object, path_repelled),          0,                           0},
	{"path_denied",            FIELDTYPE_UINT32,     offsetof(object, path_denied),            0,                           0},
	{"value",                  FIELDTYPE_SINT64,     offsetof(object, value),                  0,                           0},
	{"quantity",               FIELDTYPE_UINT32,     offsetof(object, nrof),                   0,                           0},
	{"enemy",                  FIELDTYPE_OBJECTREF,  offsetof(object, enemy),                  FIELDFLAG_READONLY,          offsetof(object, enemy_count)},
	{"attacked_by",            FIELDTYPE_OBJECTREF,  offsetof(object, attacked_by),            FIELDFLAG_READONLY,          offsetof(object, attacked_by_count)},
	{"owner",                  FIELDTYPE_OBJECTREF,  offsetof(object, owner),                  FIELDFLAG_READONLY,          offsetof(object, ownercount)},

	{"x",                      FIELDTYPE_SINT16,     offsetof(object, x),                      FIELDFLAG_READONLY,          0},
	{"y",                      FIELDTYPE_SINT16,     offsetof(object, y),                      FIELDFLAG_READONLY,          0},
	{"attacked_by_distance",   FIELDTYPE_SINT16,     offsetof(object, attacked_by_distance),   0,                           0},
	{"last_damage",            FIELDTYPE_UINT16,     offsetof(object, last_damage),            0,                           0},
	{"terrain_type",           FIELDTYPE_UINT16,     offsetof(object, terrain_type),           0,                           0},
	{"terrain_flag",           FIELDTYPE_UINT16,     offsetof(object, terrain_flag),           0,                           0},
	{"material",               FIELDTYPE_UINT16,     offsetof(object, material),               0,                           0},
	{"material_real",          FIELDTYPE_SINT16,     offsetof(object, material_real),          0,                           0},
	{"last_heal",              FIELDTYPE_SINT16,     offsetof(object, last_heal),              0,                           0},
	{"last_sp",                FIELDTYPE_SINT16,     offsetof(object, last_sp),                0,                           0},

	{"last_grace",             FIELDTYPE_SINT16,     offsetof(object, last_grace),             0,                           0},
	{"last_eat",               FIELDTYPE_SINT16,     offsetof(object, last_eat),               0,                           0},
	{"animation_id",           FIELDTYPE_UINT16,     offsetof(object, animation_id),           0,                           0},
	{"inv_animation_id",       FIELDTYPE_UINT16,     offsetof(object, inv_animation_id),       0,                           0},
	{"magic",                  FIELDTYPE_SINT8,      offsetof(object, magic),                  0,                           0},
	{"state",                  FIELDTYPE_UINT8,      offsetof(object, state),                  0,                           0},
	{"level",                  FIELDTYPE_SINT8,      offsetof(object, level),                  FIELDFLAG_PLAYER_READONLY,   0},
	{"direction",              FIELDTYPE_SINT8,      offsetof(object, direction),              0,                           0},
	{"facing",                 FIELDTYPE_SINT8,      offsetof(object, facing),                 0,                           0},
	{"quick_pos",              FIELDTYPE_UINT8,      offsetof(object, quick_pos),              0,                           0},
	{"quickslot",              FIELDTYPE_UINT8,      offsetof(object, quickslot),              FIELDFLAG_READONLY,          0},

	{"type",                   FIELDTYPE_UINT8,      offsetof(object, type),                   FIELDFLAG_READONLY,          0},
	{"sub_type_1",             FIELDTYPE_UINT8,      offsetof(object, sub_type1),              0,                           0},
	{"item_quality",           FIELDTYPE_UINT8,      offsetof(object, item_quality),           0,                           0},
	{"item_condition",         FIELDTYPE_UINT8,      offsetof(object, item_condition),         0,                           0},
	{"item_race",              FIELDTYPE_UINT8,      offsetof(object, item_race),              0,                           0},
	{"item_level",             FIELDTYPE_UINT8,      offsetof(object, item_level),             0,                           0},
	{"item_skill",             FIELDTYPE_UINT8,      offsetof(object, item_skill),             0,                           0},
	{"glow_radius",            FIELDTYPE_SINT8,      offsetof(object, glow_radius),            0,                           0},
	{"move_status",            FIELDTYPE_SINT8,      offsetof(object, move_status),            0,                           0},
	{"move_type",              FIELDTYPE_UINT8,      offsetof(object, move_type),              0,                           0},

	{"anim_enemy_dir",         FIELDTYPE_SINT8,      offsetof(object, anim_enemy_dir),         0,                           0},
	{"anim_moving_dir",        FIELDTYPE_SINT8,      offsetof(object, anim_moving_dir),        0,                           0},
	{"anim_enemy_dir_last",    FIELDTYPE_SINT8,      offsetof(object, anim_enemy_dir_last),    0,                           0},
	{"anim_moving_dir_last",   FIELDTYPE_SINT8,      offsetof(object, anim_moving_dir_last),   0,                           0},
	{"anim_last_facing",       FIELDTYPE_SINT8,      offsetof(object, anim_last_facing),       0,                           0},
	{"anim_last_facing_last",  FIELDTYPE_SINT8,      offsetof(object, anim_last_facing_last),  0,                           0},
	{"anim_speed",             FIELDTYPE_UINT8,      offsetof(object, anim_speed),             0,                           0},
	{"last_anim",              FIELDTYPE_UINT8,      offsetof(object, last_anim),              0,                           0},
	{"behavior",               FIELDTYPE_UINT8,      offsetof(object, behavior),               0,                           0},
	{"run_away",               FIELDTYPE_UINT8,      offsetof(object, run_away),               0,                           0},

	{"layer",                  FIELDTYPE_UINT8,      offsetof(object, layer),                  0,                           0},
	{"speed",                  FIELDTYPE_FLOAT,      offsetof(object, speed),                  FIELDFLAG_PLAYER_READONLY,   0},
	{"speed_left",             FIELDTYPE_FLOAT,      offsetof(object, speed_left),             0,                           0},
	{"weapon_speed",           FIELDTYPE_FLOAT,      offsetof(object, weapon_speed),           0,                           0},
	{"weapon_speed_left",      FIELDTYPE_FLOAT,      offsetof(object, weapon_speed_left),      0,                           0},
	{"weapon_speed_add",       FIELDTYPE_FLOAT,      offsetof(object, weapon_speed_add),       0,                           0},
	{"experience",             FIELDTYPE_SINT32,     offsetof(object, stats.exp),              0,                           0},
	{"hitpoints",              FIELDTYPE_SINT32,     offsetof(object, stats.hp),               0,                           0},

	{"max_hitpoints",          FIELDTYPE_SINT32,     offsetof(object, stats.maxhp),            FIELDFLAG_PLAYER_READONLY,   0},
	{"spellpoints",            FIELDTYPE_SINT16,     offsetof(object, stats.sp),               0,                           0},
	{"max_spellpoints",        FIELDTYPE_SINT16,     offsetof(object, stats.maxsp),            FIELDFLAG_PLAYER_READONLY,   0},
	{"grace",                  FIELDTYPE_SINT16,     offsetof(object, stats.grace),            0,                           0},
	{"max_grace",              FIELDTYPE_SINT16,     offsetof(object, stats.maxgrace),         FIELDFLAG_PLAYER_READONLY,   0},
	{"food",                   FIELDTYPE_SINT16,     offsetof(object, stats.food),             0,                           0},
	{"damage",                 FIELDTYPE_SINT16,     offsetof(object, stats.dam),              FIELDFLAG_PLAYER_READONLY,   0},
	{"weapon_class",           FIELDTYPE_SINT16,     offsetof(object, stats.wc),               FIELDFLAG_PLAYER_READONLY,   0},
	{"armour_class",           FIELDTYPE_SINT16,     offsetof(object, stats.ac),               FIELDFLAG_PLAYER_READONLY,   0},
	{"weapon_class_range",     FIELDTYPE_UINT8,      offsetof(object, stats.wc_range),         0,                           0},

	{"strength",               FIELDTYPE_SINT8,      offsetof(object, stats.Str),              FIELDFLAG_PLAYER_FIX,        0},
	{"dexterity",              FIELDTYPE_SINT8,      offsetof(object, stats.Dex),              FIELDFLAG_PLAYER_FIX,        0},
	{"constitution",           FIELDTYPE_SINT8,      offsetof(object, stats.Con),              FIELDFLAG_PLAYER_FIX,        0},
	{"wisdom",                 FIELDTYPE_SINT8,      offsetof(object, stats.Wis),              FIELDFLAG_PLAYER_FIX,        0},
	{"charisma",               FIELDTYPE_SINT8,      offsetof(object, stats.Cha),              FIELDFLAG_PLAYER_FIX,        0},
	{"intelligence",           FIELDTYPE_SINT8,      offsetof(object, stats.Int),              FIELDFLAG_PLAYER_FIX,        0},
	{"power",                  FIELDTYPE_SINT8,      offsetof(object, stats.Pow),              FIELDFLAG_PLAYER_FIX,        0},
	{"luck",                   FIELDTYPE_SINT8,      offsetof(object, stats.luck),             FIELDFLAG_PLAYER_READONLY,   0}
};

/** Number of object fields. */
#define NUM_OBJFIELDS (sizeof(obj_fields) / sizeof(obj_fields[0]))

/**
 * This is a list of strings that correspond to the FLAG_... values.
 *
 * This is a simple 1:1 mapping - if FLAG_FRIENDLY is 15, then the 15th
 * element of this array should match that name.
 *
 * If an entry is NULL, that flag cannot be set/read from scripts
 *
 * Yes, this is almost exactly a repeat from loader.l.
 *
 * List of the flags and their meaning: */
static char *flag_names[NUM_FLAGS + 1] =
{
	"f_sleep",               "f_confused",          "f_paralyzed",           "f_scared",          "f_is_blind",
	"f_is_invisible",        "f_is_ethereal",       "f_is_good",             "f_no_pick",         "f_walk_on",
	"f_no_pass",             "f_is_animated",       "f_slow_move",           "f_flying",          "f_monster",
	"f_friendly",            NULL,                  "f_been_applied",        "f_auto_apply",      "f_treasure",
	"f_is_neutral",          "f_see_invisible",     "f_can_roll",            "f_generator",       "f_is_turnable",
	"f_walk_off",            "f_fly_on",            "f_fly_off",             "f_is_used_up",      "f_identified",
	"f_reflecting",          "f_changing",          "f_splitting",           "f_hitback",         "f_startequip",
	"f_blocksview",          "f_undead",            "f_can_stack",           "f_unaggressive",    "f_reflect_missile",
	"f_reflect_spell",       "f_no_magic",          "f_no_fix_player",       "f_is_evil",         "f_tear_down",
	"f_run_away",            "f_pass_thru",         "f_can_pass_thru",       "f_pick_up",         "f_unique",
	"f_no_drop",             "f_is_indestructible", "f_can_cast_spell",      "f_can_use_scroll",  "f_can_use_range",
	"f_can_use_bow",         "f_can_use_armour",    "f_can_use_weapon",      "f_can_use_ring",    "f_has_ready_range",
	"f_has_ready_bow",       "f_xrays",             "f_no_apply",            "f_is_floor",        "f_lifesave",
	"f_is_magical",          "f_alive",             "f_stand_still",         "f_random_move",     "f_only_attack",
	"f_wiz",                 "f_stealth",           NULL,                    NULL,                "f_cursed",
	"f_damned",              "f_see_anywhere",      NULL,                    NULL,                "f_can_use_skill",
	"f_is_thrown",           "f_is_vul_sphere",     "f_is_proof_sphere",     "f_is_male",         "f_is_female",
	"f_applied",             "f_inv_locked",        "f_is_wooded",           "f_is_hilly",        "f_has_ready_skill",
	"f_has_ready_weapon",    "f_no_skill_ident",    NULL,                    "f_can_see_in_dark", "f_is_cauldron",
	"f_is_dust",             "f_no_steal",          "f_one_hit",             NULL,                "f_berserk",
	"f_no_attack",           "f_invulnerable",      "f_quest_item",          NULL,                "f_is_vul_elemental",
	"f_is_proof_elemental",  "f_is_vul_magic",      "f_is_proof_magic",      "f_is_vul_physical", "f_is_proof_physical",
	"f_sys_object",          "f_use_fix_pos",       "f_unpaid",              NULL,                "f_make_invisible",
	"f_make_ethereal",       NULL,                  "f_is_named",            NULL,                "f_no_teleport",
	"f_corpse",              "f_corpse_forced",     "f_player_only",         "f_no_cleric",       "f_one_drop",
	"f_cursed_perm",         "f_damned_perm",       "f_door_closed",         NULL,                "f_is_missile",
	"f_can_reflect_missile", "f_can_reflect_spell", "flag_is_assassination", NULL,                NULL,
	NULL
};

/**
 * Object constants. */
static Atrinik_Constant object_constants[] =
{
	{"MAP_INFO_NORMAL",              MAP_INFO_NORMAL},
	{"MAP_INFO_ALL",                 MAP_INFO_ALL},

	{"COST_TRUE",                    F_TRUE},
	{"COST_BUY",                     F_BUY},
	{"COST_SELL",                    F_SELL},

	{"APPLY_TOGGLE",                 0},
	{"APPLY_ALWAYS",                 AP_APPLY},
	{"UNAPPLY_ALWAYS",               AP_UNAPPLY},
	{"UNAPPLY_NO_MERGE",             AP_NO_MERGE},
	{"UNAPPLY_IGNORE_CURSE",         AP_IGNORE_CURSE},
	{"APPLY_NO_EVENT",               AP_NO_EVENT},

	{"NEUTER",                       0},
	{"MALE",                         1},
	{"FEMALE",                       2},
	{"HERMAPHRODITE",                3},

	{"CAST_NORMAL",                  0},
	{"CAST_POTION",                  1},

	{"LEARN",                        0},
	{"UNLEARN",                      1},

	{"UNIDENTIFIED",                 0},
	{"IDENTIFIED",                   1},

	{"IDENTIFY_NORMAL",              IDENTIFY_MODE_NORMAL},
	{"IDENTIFY_ALL",                 IDENTIFY_MODE_ALL},
	{"IDENTIFY_MARKED",              IDENTIFY_MODE_MARKED},

	{"CLONE_WITH_INVENTORY",         0},
	{"CLONE_WITHOUT_INVENTORY",      1},

	{"EXP_AGILITY", EXP_AGILITY},
	{"EXP_MENTAL", EXP_MENTAL},
	{"EXP_MAGICAL", EXP_MAGICAL},
	{"EXP_PERSONAL", EXP_PERSONAL},
	{"EXP_PHYSICAL", EXP_PHYSICAL},
	{"EXP_WISDOM", EXP_WISDOM},

	{"COLOR_WHITE", NDI_WHITE},
	{"COLOR_ORANGE", NDI_ORANGE},
	{"COLOR_NAVY", NDI_NAVY},
	{"COLOR_RED", NDI_RED},
	{"COLOR_GREEN", NDI_GREEN},
	{"COLOR_BLUE", NDI_BLUE},
	{"COLOR_GREY", NDI_GREY},
	{"COLOR_BROWN", NDI_BROWN},
	{"COLOR_PURPLE", NDI_PURPLE},
	{"COLOR_PINK", NDI_PINK},
	{"COLOR_YELLOW", NDI_YELLOW},
	{"COLOR_DK_NAVY", NDI_DK_NAVY},
	{"COLOR_DK_GREEN", NDI_DK_GREEN},
	{"COLOR_DK_ORANGE", NDI_DK_ORANGE},

	{"NDI_SAY",                      NDI_SAY},
	{"NDI_SHOUT",                    NDI_SHOUT},
	{"NDI_TELL",                     NDI_TELL},
	{"NDI_PLAYER",                   NDI_PLAYER},
	{"NDI_ANIM",                     NDI_ANIM},
	{"NDI_EMOTE",                    NDI_EMOTE},
	{"NDI_ALL",                      NDI_ALL},

	{"PLAYER_EQUIP_MAIL", PLAYER_EQUIP_MAIL},
	{"PLAYER_EQUIP_GAUNTLET", PLAYER_EQUIP_GAUNTLET},
	{"PLAYER_EQUIP_BRACER", PLAYER_EQUIP_BRACER},
	{"PLAYER_EQUIP_HELM", PLAYER_EQUIP_HELM},
	{"PLAYER_EQUIP_BOOTS", PLAYER_EQUIP_BOOTS},
	{"PLAYER_EQUIP_CLOAK", PLAYER_EQUIP_CLOAK},
	{"PLAYER_EQUIP_GIRDLE", PLAYER_EQUIP_GIRDLE},
	{"PLAYER_EQUIP_SHIELD", PLAYER_EQUIP_SHIELD},
	{"PLAYER_EQUIP_RRING", PLAYER_EQUIP_RRING},
	{"PLAYER_EQUIP_LRING", PLAYER_EQUIP_LRING},
	{"PLAYER_EQUIP_AMULET", PLAYER_EQUIP_AMULET},
	{"PLAYER_EQUIP_WEAPON", PLAYER_EQUIP_WEAPON},
	{"PLAYER_EQUIP_BOW", PLAYER_EQUIP_BOW},

	{"QUEST_TYPE_SPECIAL", QUEST_TYPE_SPECIAL},
	{"QUEST_TYPE_KILL", QUEST_TYPE_KILL},
	{"QUEST_TYPE_KILL_ITEM", QUEST_TYPE_KILL_ITEM},
	{"QUEST_STATUS_COMPLETED", QUEST_STATUS_COMPLETED},

	{"TYPE_PLAYER",                  PLAYER},
	{"TYPE_BULLET",                  BULLET},
	{"TYPE_ROD",                     ROD},
	{"TYPE_TREASURE",                TREASURE},
	{"TYPE_POTION",                  POTION},
	{"TYPE_FOOD",                    FOOD},
	{"TYPE_POISON",                  POISON},
	{"TYPE_BOOK",                    BOOK},
	{"TYPE_CLOCK",                   CLOCK},
	{"TYPE_FBULLET",                 FBULLET},
	{"TYPE_FBALL",                   FBALL},
	{"TYPE_LIGHTNING",               LIGHTNING},
	{"TYPE_ARROW",                   ARROW},
	{"TYPE_BOW",                     BOW},
	{"TYPE_WEAPON",                  WEAPON},
	{"TYPE_ARMOUR",                  ARMOUR},
	{"TYPE_PEDESTAL",                PEDESTAL},
	{"TYPE_ALTAR",                   ALTAR},
	{"TYPE_CONFUSION",               CONFUSION},
	{"TYPE_LOCKED_DOOR",             LOCKED_DOOR},
	{"TYPE_SPECIAL_KEY",             SPECIAL_KEY},
	{"TYPE_MAP",                     MAP},
	{"TYPE_DOOR",                    DOOR},
	{"TYPE_KEY",                     KEY},
	{"TYPE_MMISSILE",                MMISSILE},
	{"TYPE_TIMED_GATE",              TIMED_GATE},
	{"TYPE_TRIGGER",                 TRIGGER},
	{"TYPE_GRIMREAPER",              GRIMREAPER},
	{"TYPE_MAGIC_EAR",               MAGIC_EAR},
	{"TYPE_TRIGGER_BUTTON",          TRIGGER_BUTTON},
	{"TYPE_TRIGGER_ALTAR",           TRIGGER_ALTAR},
	{"TYPE_TRIGGER_PEDESTAL",        TRIGGER_PEDESTAL},
	{"TYPE_SHIELD",                  SHIELD},
	{"TYPE_HELMET",                  HELMET},
	{"TYPE_HORN",                    HORN},
	{"TYPE_MONEY",                   MONEY},
	{"TYPE_CLASS",                   CLASS},
	{"TYPE_GRAVESTONE",              GRAVESTONE},
	{"TYPE_AMULET",                  AMULET},
	{"TYPE_PLAYERMOVER",             PLAYERMOVER},
	{"TYPE_TELEPORTER",              TELEPORTER},
	{"TYPE_CREATOR",                 CREATOR},
	{"TYPE_SKILL",                   SKILL},
	{"TYPE_EXPERIENCE",              EXPERIENCE},
	{"TYPE_EARTHWALL",               EARTHWALL},
	{"TYPE_GOLEM",                   GOLEM},
	{"TYPE_BOMB",                    BOMB},
	{"TYPE_THROWN_OBJ",              THROWN_OBJ},
	{"TYPE_BLINDNESS",               BLINDNESS},
	{"TYPE_GOD",                     GOD},
	{"TYPE_DETECTOR",                DETECTOR},
	{"TYPE_DEAD_OBJECT",             DEAD_OBJECT},
	{"TYPE_DRINK",                   DRINK},
	{"TYPE_MARKER",                  MARKER},
	{"TYPE_HOLY_ALTAR",              HOLY_ALTAR},
	{"TYPE_PLAYER_CHANGER",          PLAYER_CHANGER},
	{"TYPE_PEARL",                   PEARL},
	{"TYPE_GEM",                     GEM},
	{"TYPE_FIRECHEST",               FIRECHEST},
	{"TYPE_FIREWALL",                FIREWALL},
	{"TYPE_CHECK_INV",               CHECK_INV},
	{"TYPE_MOOD_FLOOR",              MOOD_FLOOR},
	{"TYPE_EXIT",                    EXIT},
	{"TYPE_SHOP_FLOOR",              SHOP_FLOOR},
	{"TYPE_SHOP_MAT",                SHOP_MAT},
	{"TYPE_RING",                    RING},
	{"TYPE_FLOOR",                   FLOOR},
	{"TYPE_FLESH",                   FLESH},
	{"TYPE_INORGANIC",               INORGANIC},
	{"TYPE_LIGHT_APPLY",             LIGHT_APPLY},
	{"TYPE_LIGHTER",                 LIGHTER},
	{"TYPE_WALL",                    WALL},
	{"TYPE_LIGHT_SOURCE",            LIGHT_SOURCE},
	{"TYPE_MISC_OBJECT",             MISC_OBJECT},
	{"TYPE_MONSTER",                 MONSTER},
	{"TYPE_SPAWN_POINT",             SPAWN_POINT},
	{"TYPE_LIGHT_REFILL",            LIGHT_REFILL},
	{"TYPE_SPAWN_POINT_MOB",         SPAWN_POINT_MOB},
	{"TYPE_SPAWN_POINT_INFO",        SPAWN_POINT_INFO},
	{"TYPE_SPELLBOOK",               SPELLBOOK},
	{"TYPE_ORGANIC",                 ORGANIC},
	{"TYPE_CLOAK",                   CLOAK},
	{"TYPE_CONE",                    CONE},
	{"TYPE_AURA",                    AURA},
	{"TYPE_SPINNER",                 SPINNER},
	{"TYPE_GATE",                    GATE},
	{"TYPE_BUTTON",                  BUTTON},
	{"TYPE_HANDLE",                  HANDLE},
	{"TYPE_PIT",                     PIT},
	{"TYPE_TRAPDOOR",                TRAPDOOR},
	{"TYPE_WORD_OF_RECALL",          WORD_OF_RECALL},
	{"TYPE_SIGN",                    SIGN},
	{"TYPE_BOOTS",                   BOOTS},
	{"TYPE_GLOVES",                  GLOVES},
	{"TYPE_BASE_INFO",               BASE_INFO},
	{"TYPE_RANDOM_DROP",             RANDOM_DROP},
	{"TYPE_CONVERTER",               CONVERTER},
	{"TYPE_BRACERS",                 BRACERS},
	{"TYPE_POISONING",               POISONING},
	{"TYPE_SAVEBED",                 SAVEBED},
	{"TYPE_POISONCLOUD",             POISONCLOUD},
	{"TYPE_WAND",                    WAND},
	{"TYPE_ABILITY",                 ABILITY},
	{"TYPE_SCROLL",                  SCROLL},
	{"TYPE_DIRECTOR",                DIRECTOR},
	{"TYPE_GIRDLE",                  GIRDLE},
	{"TYPE_FORCE",                   FORCE},
	{"TYPE_POTION_EFFECT",           POTION_EFFECT},
	{"TYPE_JEWEL",                   JEWEL},
	{"TYPE_NUGGET",                  NUGGET},
	{"TYPE_EVENT_OBJECT",            EVENT_OBJECT},
	{"TYPE_WAYPOINT_OBJECT",         WAYPOINT_OBJECT},
	{"TYPE_QUEST_CONTAINER",         QUEST_CONTAINER},
	{"TYPE_CLOSE_CON",               CLOSE_CON},
	{"TYPE_CONTAINER",               CONTAINER},
	{"TYPE_ARMOUR_IMPROVER",         ARMOUR_IMPROVER},
	{"TYPE_WEAPON_IMPROVER",         WEAPON_IMPROVER},
	{"TYPE_WEALTH",                  WEALTH},
	{"TYPE_SKILLSCROLL",             SKILLSCROLL},
	{"TYPE_DEEP_SWAMP",              DEEP_SWAMP},
	{"TYPE_IDENTIFY_ALTAR",          IDENTIFY_ALTAR},
	{"TYPE_CANCELLATION",            CANCELLATION},
	{"TYPE_BALL_LIGHTNING",          BALL_LIGHTNING},
	{"TYPE_SWARM_SPELL",             SWARM_SPELL},
	{"TYPE_RUNE",                    RUNE},
	{"TYPE_POWER_CRYSTAL",           POWER_CRYSTAL},
	{"TYPE_CORPSE",                  CORPSE},
	{"TYPE_DISEASE",                 DISEASE},
	{"TYPE_SYMPTOM",                 SYMPTOM},

	{"SOUNDTYPE_NORMAL", SOUND_NORMAL},
	{"SOUNDTYPE_SPELL", SOUND_SPELL},

	{"SOUND_LEVEL_UP", SOUND_LEVEL_UP},
	{"SOUND_FIRE_ARROW", SOUND_FIRE_ARROW},
	{"SOUND_LEARN_SPELL", SOUND_LEARN_SPELL},
	{"SOUND_FUMBLE_SPELL", SOUND_FUMBLE_SPELL},
	{"SOUND_WAND_POOF", SOUND_WAND_POOF},
	{"SOUND_OPEN_DOOR", SOUND_OPEN_DOOR},
	{"SOUND_PUSH_PLAYER", SOUND_PUSH_PLAYER},
	{"SOUND_HIT_IMPACT", SOUND_HIT_IMPACT},
	{"SOUND_HIT_CLEAVE", SOUND_HIT_CLEAVE},
	{"SOUND_HIT_SLASH", SOUND_HIT_SLASH},
	{"SOUND_HIT_PIERCE", SOUND_HIT_PIERCE},
	{"SOUND_MISS_BLOCK", SOUND_MISS_BLOCK},
	{"SOUND_MISS_HAND", SOUND_MISS_HAND},
	{"SOUND_MISS_MOB", SOUND_MISS_MOB},
	{"SOUND_MISS_PLAYER", SOUND_MISS_PLAYER},
	{"SOUND_PET_IS_KILLED", SOUND_PET_IS_KILLED},
	{"SOUND_PLAYER_DIES", SOUND_PLAYER_DIES},
	{"SOUND_OB_EVAPORATE", SOUND_OB_EVAPORATE},
	{"SOUND_OB_EXPLODE", SOUND_OB_EXPLODE},
	{"SOUND_PLAYER_KILLS", SOUND_PLAYER_KILLS},
	{"SOUND_TURN_HANDLE", SOUND_TURN_HANDLE},
	{"SOUND_FALL_HOLE", SOUND_FALL_HOLE},
	{"SOUND_DRINK_POISON", SOUND_DRINK_POISON},
	{"SOUND_DROP_THROW", SOUND_DROP_THROW},
	{"SOUND_LOSE_SOME", SOUND_LOSE_SOME},
	{"SOUND_THROW", SOUND_THROW},
	{"SOUND_GATE_OPEN", SOUND_GATE_OPEN},
	{"SOUND_GATE_CLOSE", SOUND_GATE_CLOSE},
	{"SOUND_OPEN_CONTAINER", SOUND_OPEN_CONTAINER},
	{"SOUND_GROWL", SOUND_GROWL},
	{"SOUND_ARROW_HIT", SOUND_ARROW_HIT},
	{"SOUND_DOOR_CLOSE", SOUND_DOOR_CLOSE},
	{"SOUND_TELEPORT", SOUND_TELEPORT},
	{"SOUND_CLICK", SOUND_CLICK},

	{"SOUND_MAGIC_DEFAULT", SOUND_MAGIC_DEFAULT},
	{"SOUND_MAGIC_ACID", SOUND_MAGIC_ACID},
	{"SOUND_MAGIC_ANIMATE", SOUND_MAGIC_ANIMATE},
	{"SOUND_MAGIC_AVATAR", SOUND_MAGIC_AVATAR},
	{"SOUND_MAGIC_BOMB", SOUND_MAGIC_BOMB},
	{"SOUND_MAGIC_BULLET1", SOUND_MAGIC_BULLET1},
	{"SOUND_MAGIC_BULLET2", SOUND_MAGIC_BULLET2},
	{"SOUND_MAGIC_CANCEL", SOUND_MAGIC_CANCEL},
	{"SOUND_MAGIC_COMET", SOUND_MAGIC_COMET},
	{"SOUND_MAGIC_CONFUSION", SOUND_MAGIC_CONFUSION},
	{"SOUND_MAGIC_CREATE", SOUND_MAGIC_CREATE},
	{"SOUND_MAGIC_DARK", SOUND_MAGIC_DARK},
	{"SOUND_MAGIC_DEATH", SOUND_MAGIC_DEATH},
	{"SOUND_MAGIC_DESTRUCTION", SOUND_MAGIC_DESTRUCTION},
	{"SOUND_MAGIC_ELEC", SOUND_MAGIC_ELEC},
	{"SOUND_MAGIC_FEAR", SOUND_MAGIC_FEAR},
	{"SOUND_MAGIC_FIRE", SOUND_MAGIC_FIRE},
	{"SOUND_MAGIC_FIREBALL1", SOUND_MAGIC_FIREBALL1},
	{"SOUND_MAGIC_FIREBALL2", SOUND_MAGIC_FIREBALL2},
	{"SOUND_MAGIC_HWORD", SOUND_MAGIC_HWORD},
	{"SOUND_MAGIC_ICE", SOUND_MAGIC_ICE},
	{"SOUND_MAGIC_INVISIBLE", SOUND_MAGIC_INVISIBLE},
	{"SOUND_MAGIC_INVOKE", SOUND_MAGIC_INVOKE},
	{"SOUND_MAGIC_INVOKE2", SOUND_MAGIC_INVOKE2},
	{"SOUND_MAGIC_MAGIC", SOUND_MAGIC_MAGIC},
	{"SOUND_MAGIC_MANABALL", SOUND_MAGIC_MANABALL},
	{"SOUND_MAGIC_MISSILE", SOUND_MAGIC_MISSILE},
	{"SOUND_MAGIC_MMAP", SOUND_MAGIC_MMAP},
	{"SOUND_MAGIC_ORB", SOUND_MAGIC_ORB},
	{"SOUND_MAGIC_PARALYZE", SOUND_MAGIC_PARALYZE},
	{"SOUND_MAGIC_POISON", SOUND_MAGIC_POISON},
	{"SOUND_MAGIC_PROTECTION", SOUND_MAGIC_PROTECTION},
	{"SOUND_MAGIC_RSTRIKE", SOUND_MAGIC_RSTRIKE},
	{"SOUND_MAGIC_RUNE", SOUND_MAGIC_RUNE},
	{"SOUND_MAGIC_SBALL", SOUND_MAGIC_SBALL},
	{"SOUND_MAGIC_SLOW", SOUND_MAGIC_SLOW},
	{"SOUND_MAGIC_SNOWSTORM", SOUND_MAGIC_SNOWSTORM},
	{"SOUND_MAGIC_STAT", SOUND_MAGIC_STAT},
	{"SOUND_MAGIC_STEAMBOLT", SOUND_MAGIC_STEAMBOLT},
	{"SOUND_MAGIC_SUMMON1", SOUND_MAGIC_SUMMON1},
	{"SOUND_MAGIC_SUMMON2", SOUND_MAGIC_SUMMON2},
	{"SOUND_MAGIC_SUMMON3", SOUND_MAGIC_SUMMON3},
	{"SOUND_MAGIC_TELEPORT", SOUND_MAGIC_TELEPORT},
	{"SOUND_MAGIC_TURN", SOUND_MAGIC_TURN},
	{"SOUND_MAGIC_WALL", SOUND_MAGIC_WALL},
	{"SOUND_MAGIC_WALL2", SOUND_MAGIC_WALL2},
	{"SOUND_MAGIC_WOUND", SOUND_MAGIC_WOUND},

	{NULL, 0}
};

/**
 * @defgroup plugin_python_object_functions Python plugin object functions
 * Object related functions used in Atrinik Python plugin.
 *@{*/

/**
 * <h1>object.GetSkill(<i>\<int\></i> type, <i>\<int\></i> id)</h1>
 *
 * Fetch a skill or exp_skill object from the specified object.
 * @param type Type of the object to look for. Unused.
 * @param id ID of the skill or experience
 * @return The object if found.
 * @todo Remove the type parameter?
 * @todo Could speed this up for players by using their skill pointer and
 * experience arrays. That would make the type parameter used again as well. */
static PyObject *Atrinik_Object_GetSkill(Atrinik_Object *whoptr, PyObject *args)
{
	object *tmp;
	int type, id;

	if (!PyArg_ParseTuple(args, "ii", &type, &id))
	{
		return NULL;
	}

	/* Browse the inventory of object to find a matching skill or exp_obj. */
	for (tmp = WHO->inv; tmp; tmp = tmp->below)
	{
		if (tmp->type == SKILL && tmp->stats.sp == id)
		{
			return wrap_object(tmp);
		}

		if (tmp->type == EXPERIENCE && tmp->sub_type1 == id)
		{
			return wrap_object(tmp);
		}
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SetSkill(<i>\<int\></i> type, <i>\<int\></i> skillid,
 * <i>\<long\></i> level, <i>\<long\></i> value)</h1>
 *
 * Set object's experience in the skill to a new value.
 * Also can change the level of a skill.
 * @param type Type of the skill, should be TYPE_SKILL
 * @param skillid ID of the skill
 * @param level Level to set for the skill. Must be non zero to set,
 * otherwise experience is set instead.
 * @param value Experience to set for the skill. Only set if level is
 * lower than 1.
 * @todo Overall experience is not changed (should it be?)
 * @todo Could speed this up for players by using their skill pointer array.
 * That way we wouldn't need the type parameter either.
 * @todo Only allow to be ran on players? Monsters can't get exp. */
static PyObject *Atrinik_Object_SetSkill(Atrinik_Object *whoptr, PyObject *args)
{
	object *tmp;
	int type, skill, currentxp;
	long level, value;

	if (!PyArg_ParseTuple(args, "iill", &type, &skill, &level, &value))
	{
		return NULL;
	}

	/* We don't set anything in exp_obj types */
	if (type != SKILL)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	/* Browse the inventory of object to find a matching skill. */
	for (tmp = WHO->inv; tmp; tmp = tmp->below)
	{
		if (tmp->type == type && tmp->stats.sp == skill)
		{
			/*LOG(-1,"LEVEL1 %d (->%d) :: %s (exp %d)\n",tmp->level,level,hooks->query_name(tmp), tmp->stats.exp);*/

			/* This is a bit tricky: some skills are marked with exp -1
			 * or -2 as special used skills (level but no exp):
			 * if we have here a level > 0, we set level but NEVER exp.
			 * if we have level == 0, we only set exp - the
			 * addexp */
			if (level > 0)
			{
				tmp->level = level;
			}
			else
			{
				currentxp = tmp->stats.exp;
				value = value - currentxp;

				hooks->add_exp(WHO, value, skill);
			}

			/*LOG(-1,"LEVEL2 %d (->%d) :: %s (exp %d)\n",tmp->level,level,hooks->query_name(tmp), tmp->stats.exp);*/

			/* We will sure change skill exp, mark for update */
			if (WHO->type == PLAYER && CONTR(WHO))
			{
				CONTR(WHO)->update_skills = 1;
			}

			Py_INCREF(Py_None);
			return Py_None;
		}
	}

	RAISE("Unknown skill");
}

/**
 * <h1>object.ActivateRune(<i>\<object\></i> who)</h1>
 *
 * Activate a rune.
 * @param who Who should be affected by the effects of the rune.
 * @warning Untested. */
static PyObject *Atrinik_Object_ActivateRune(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &whatptr))
	{
		return NULL;
	}

	hooks->spring_trap(WHAT, WHO);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.CheckTrigger(<i>\<object\></i> who)</h1>
 *
 * Check trigger of an object.
 * @param who Who is triggering the trigger object.
 * @warning Unfinished, do not use.
 * @todo Create a hook for check_trigger() to make this work. */
static PyObject *Atrinik_Object_CheckTrigger(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;

	(void) whoptr;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &whatptr))
	{
		return NULL;
	}

	/* check_trigger(WHAT,WHO); should be hook too! */

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetGod()</h1>
 *
 * Determine who is the object follower of (who the god is).
 * @return Returns a string of the god's name. */
static PyObject *Atrinik_Object_GetGod(Atrinik_Object *whoptr, PyObject *args)
{
	(void) args;

	return Py_BuildValue("s", hooks->determine_god(WHO));
}

/**
 * <h1>object.SetGod(<i>\<string\></i> godname)</h1>
 *
 * Make an object become follower of a different god.
 * @param godname Name of the god. */
static PyObject *Atrinik_Object_SetGod(Atrinik_Object *whoptr, PyObject *args)
{
	char *txt;

	if (!PyArg_ParseTuple(args, "s", &txt))
	{
		return NULL;
	}

	if (hooks->command_rskill(WHO, "divine prayers"))
	{
		object *god = hooks->find_god(txt);

		hooks->become_follower(WHO, god);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.TeleportTo(<i>\<string\></i> map, <i>\<int\></i> x,
 * <i>\<int\></i> y, <i>\<int\></i> unique)</h1>
 *
 * Teleport object to the given position of map.
 * @param map Map name to teleport the object to
 * @param x X position on the map
 * @param y Y position on the map
 * @param unique If non-zero, the destination will be unique map for the
 * player. Optional, defaults to 0. */
static PyObject *Atrinik_Object_TeleportTo(Atrinik_Object *whoptr, PyObject *args)
{
	char *mapname;
	object *current = hooks->get_object();
	int x, y, u = 0;

	if (!PyArg_ParseTuple(args, "sii|i", &mapname, &x, &y, &u))
	{
		return NULL;
	}

	FREE_AND_COPY_HASH(EXIT_PATH(current), mapname);
	EXIT_X(current) = x;
	EXIT_Y(current) = y;

	if (u)
	{
		current->last_eat = MAP_PLAYER_MAP;
	}

	hooks->enter_exit(WHO, current);

	if (WHO->map)
	{
		hooks->play_sound_map(WHO->map, WHO->x, WHO->y, SOUND_TELEPORT, SOUND_NORMAL);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.InsertInside(<i>\<object\></i> where)</h1>
 *
 * Insert object into where.
 * @param where Where to insert the object. */
static PyObject *Atrinik_Object_InsertInside(Atrinik_Object *whatptr, PyObject *args)
{
	Atrinik_Object *whereptr;
	object *myob, *obenv, *tmp;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &whereptr))
	{
		return NULL;
	}

	myob = WHAT;
	obenv = myob->env;

	if (!QUERY_FLAG(myob, FLAG_REMOVED))
	{
		hooks->remove_ob(myob);
	}

	myob = hooks->insert_ob_in_ob(myob, WHERE);

	/* Make sure the inventory image/text is updated */
	for (tmp = WHERE; tmp != NULL; tmp = tmp->env)
	{
		if (tmp->type == PLAYER)
		{
			hooks->esrv_send_item(tmp, myob);
			break;
		}
	}

	/* If we're taking from player. */
	for (tmp = obenv; tmp != NULL; tmp = tmp->env)
	{
		if (tmp->type == PLAYER)
		{
			hooks->esrv_send_inventory(tmp, tmp);
			break;
		}
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Apply(<i>\<object\></i> what, <i>\<int\></i> flags)</h1>
 *
 * Forces object to apply what.
 * @param what What object to apply.
 * @param flags Reasonable combination of the following:
 * - <b>Atrinik.APPLY_TOGGLE</b>: Normal apply (toggle)
 * - <b>Atrinik.APPLY_ALWAYS</b>: Always apply (never unapply)
 * - <b>Atrinik.UNAPPLY_ALWAYS</b>: Always unapply (never apply)
 * - <b>Atrinik.UNAPPLY_NOMERGE</b>: Don't merge unapplied items
 * - <b>Atrinik.UNAPPLY_IGNORE_CURSE</b>: Unapply cursed items
 * @return Return values:
 * - <b>0</b>: Object cannot apply objects of that type.
 * - <b>1</b>: Object was applied, or not...
 * - <b>2</b>: Object must be in inventory to be applied */
static PyObject *Atrinik_Object_Apply(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;
	int flags;

	if (!PyArg_ParseTuple(args, "O!i", &Atrinik_ObjectType, &whatptr, &flags))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->manual_apply(WHO, WHAT, flags));
}

/**
 * <h1>object.PickUp(<i>\<object\></i> what)</h1>
 *
 * Force the object to pick up what.
 * @param what The object to pick up */
static PyObject *Atrinik_Object_PickUp(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &whatptr))
	{
		return NULL;
	}

	hooks->pick_up(WHO, WHAT);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Drop(<i>\<object\></i> what, <i>[string]</i> name)</h1>
 *
 * Drop an object.
 * @param what Object to drop.
 * @param name If what is None, this is used for the equivalent of /drop
 * command. */
static PyObject *Atrinik_Object_Drop(Atrinik_Object *whoptr, PyObject *args)
{
	char *name;
	Atrinik_Object *ob;

	if (!PyArg_ParseTuple(args, "O!|s", &Atrinik_ObjectType, &ob, &name))
	{
		return NULL;
	}

	if (ob)
	{
		hooks->drop(WHO, ob->obj);
	}
	else
	{
		hooks->command_drop(WHO, name);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Deposit(<i>\<object\></i> deposit_object, <i>\<string\></i>
 * string)</h1>
 *
 * Deposit value or string money from object in deposit_object.
 *
 * Control first object has that amount of money, then remove it
 * from object and add it in ->value of deposit_object.
 *
 * @param deposit_object The deposit object
 * @param string How much money to deposit, in string representation.
 * @return The value deposited. */
static PyObject *Atrinik_Object_Deposit(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *obptr;
	char *text;

	if (!PyArg_ParseTuple(args, "O!s", &Atrinik_ObjectType, &obptr, &text))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->bank_deposit(WHO, obptr->obj, text));
}

/**
 * <h1>object.Withdraw(<i>\<object\></i> deposit_object, <i>\<string\>
 * </i> string)</h1>
 *
 * Withdraw value or string money from object in deposit_object.
 *
 * @param deposit_object The withdraw object
 * @param string How much money to withdraw, in string representation.
 * @return The value withdrawn. */
static PyObject *Atrinik_Object_Withdraw(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *obptr;
	char *text;

	if (!PyArg_ParseTuple(args, "O!s", &Atrinik_ObjectType, &obptr, &text))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->bank_withdraw(WHO, obptr->obj, text));
}

/**
 * <h1>object.Communicate(<i>\<string\></i> message)</h1>
 *
 * Object says message to everybody on its map.
 *
 * @param message The message to say. */
static PyObject *Atrinik_Object_Communicate(Atrinik_Object *whoptr, PyObject *args)
{
	char *message;

	if (!PyArg_ParseTuple(args, "s", &message))
	{
		return NULL;
	}

	hooks->communicate(WHO, message);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Say(<i>\<string\></i> message, <i>\<int\></i> mode)</h1>
 *
 * Object says message to everybody on its map.
 *
 * @param message The message to say.
 * @param mode If set to non-zero, message is not prefixed with
 * "object.name says: ". Optional, defaults to 0. */
static PyObject *Atrinik_Object_Say(Atrinik_Object *whoptr, PyObject *args)
{
	char *message, buf[HUGE_BUF];
	int mode = 0;

	if (!PyArg_ParseTuple(args, "s|i", &message, &mode))
	{
		return NULL;
	}

	if (mode)
	{
		hooks->new_info_map(NDI_NAVY | NDI_UNIQUE, WHO->map, WHO->x, WHO->y, MAP_INFO_NORMAL, message);
	}
	else
	{
		snprintf(buf, sizeof(buf), "%s says: %s", hooks->query_name(WHO, NULL), message);
		hooks->new_info_map(NDI_NAVY | NDI_UNIQUE, WHO->map, WHO->x, WHO->y, MAP_INFO_NORMAL, buf);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SayTo(<i>\<object\></i> target, <i>\<string\></i> message,
 * <i>\<int\></i> mode)</h1>
 *
 * NPC talks only to player but map gets "xxx talks to yyy" msg too.
 *
 * @param target Target object the NPC is talking to.
 * @param message The message to say.
 * @param mode If set to non-zero, there is no "xxx talks to yyy" map
 * message. The message is not prefixed with "xxx says: " either.
 * Optional, defaults to 0. */
static PyObject *Atrinik_Object_SayTo(Atrinik_Object *whoptr, PyObject *args)
{
	object *target;
	Atrinik_Object *obptr2;
	char *message;
	int mode = 0;

	if (!PyArg_ParseTuple(args, "O!s|i", &Atrinik_ObjectType, &obptr2, &message, &mode))
	{
		return NULL;
	}

	target = obptr2->obj;

	if (mode)
	{
		hooks->new_draw_info(NDI_NAVY | NDI_UNIQUE, target, message);
	}
	else
	{
		char buf[HUGE_BUF];

		snprintf(buf, sizeof(buf), "%s talks to %s.", hooks->query_name(WHO, NULL), hooks->query_name(target, NULL));
		hooks->new_info_map_except(NDI_UNIQUE, WHO->map, WHO->x, WHO->y, MAP_INFO_NORMAL, WHO, target, buf);

		snprintf(buf, sizeof(buf), "\n%s says: %s", hooks->query_name(WHO, NULL), message);
		hooks->new_draw_info(NDI_NAVY | NDI_UNIQUE, target, buf);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Write(<i>\<string\></i> message, <i>\<int\></i> color)</h1>
 *
 * Writes a message to a specific player.
 *
 * @param message The message to write
 * @param color Color to write the message in. Defaults to orange. */
static PyObject *Atrinik_Object_Write(Atrinik_Object *whoptr, PyObject *args)
{
	int color = NDI_UNIQUE | NDI_ORANGE;
	char *message;

	if (!PyArg_ParseTuple(args, "s|i", &message, &color))
	{
		return NULL;
	}

	hooks->new_draw_info(color, WHO, message);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetGender()</h1>
 *
 * Get an object's gender.
 * @retval NEUTER No gender.
 * @retval MALE Male.
 * @retval FEMALE Female.
 * @retval HERMAPHRODITE Both male and female. */
static PyObject *Atrinik_Object_GetGender(Atrinik_Object *whoptr, PyObject *args)
{
	int gender;

	(void) args;

	if (!QUERY_FLAG(WHO, FLAG_IS_MALE) && !QUERY_FLAG(WHO, FLAG_IS_FEMALE))
	{
		gender = 0;
	}
	else if (QUERY_FLAG(WHO, FLAG_IS_MALE) && !QUERY_FLAG(WHO, FLAG_IS_FEMALE))
	{
		gender = 1;
	}
	else if (!QUERY_FLAG(WHO, FLAG_IS_MALE) && QUERY_FLAG(WHO, FLAG_IS_FEMALE))
	{
		gender = 2;
	}
	else
	{
		gender = 3;
	}

	return Py_BuildValue("i", gender);
}

/**
 * <h1>object.SetGender(<i>\<int\></i> gender)</h1>
 *
 * Changes the gender of object.
 * @param gender The new gender to set. One of:
 * - <b>Atrinik.NEUTER</b>: No gender.
 * - <b>Atrinik.MALE</b>: Male.
 * - <b>Atrinik.FEMALE</b>: Female.
 * - <b>Atrinik.HERMAPHRODITE</b>: Both male and female. */
static PyObject *Atrinik_Object_SetGender(Atrinik_Object *whoptr, PyObject *args)
{
	int gender;

	if (!PyArg_ParseTuple(args, "i", &gender))
	{
		return NULL;
	}

	/* Set object to neuter */
	CLEAR_FLAG(WHO, FLAG_IS_MALE);
	CLEAR_FLAG(WHO, FLAG_IS_FEMALE);

	/* Reset to male or female */
	if (gender & 1)
	{
		SET_FLAG(WHO, FLAG_IS_MALE);
	}

	if (gender & 2)
	{
		SET_FLAG(WHO, FLAG_IS_FEMALE);
	}

	/* Update the players client if object was a player */
	if (WHO->type == PLAYER)
	{
		CONTR(WHO)->socket.ext_title_flag = 1;
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SetRank(<i>\<string\></i> rank_string)</h1>
 *
 * Set the rank of an object to rank_string.
 *
 * @param rank_string The new rank string to set. If "Mr", then clear the
 * rank.
 * @return If successfully set the rank, return the rank force object. */
static PyObject *Atrinik_Object_SetRank(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;
	char *rank;

	if (!PyArg_ParseTuple(args, "s", &rank))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->RANK_FORCE && walk->arch->name == hooks->shstr_cons->rank_force)
		{
			/* We find the rank of the player, now change it to new one */
			if (walk->title)
			{
				FREE_AND_CLEAR_HASH(walk->title);
			}

			/* "Mr" is keyword to clear title and not add it as rank */
			if (strcmp(rank, "Mr"))
			{
				FREE_AND_COPY_HASH(walk->title, rank);
			}

			/* Demand update to client */
			CONTR(WHO)->socket.ext_title_flag = 1;
			return wrap_object(walk);
		}
	}

	LOG(llevDebug, "Python Warning:: SetRank: Object %s has no rank_force!\n", hooks->query_name(WHO, NULL));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetRank()</h1>
 *
 * Get the rank of a player.
 * @return Player's rank. */
static PyObject *Atrinik_Object_GetRank(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;

	(void) args;

	if (WHO->type != PLAYER)
	{
		RAISE("GetRank() can only be used on players.");
	}

	for (walk = WHO->inv; walk; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->RANK_FORCE && walk->arch->name == hooks->shstr_cons->rank_force)
		{
			return Py_BuildValue("s", walk->title);
		}
	}

	return NULL;
}

/**
 * <h1>object.SetAlignment(<i>\<string\></i> alignment_string)</h1>
 *
 * Set the alignment of an object to alignment_string.
 *
 * @param alignment_string The new alignment string to set.
 * @return If successfully set the alignment, return the alignment force
 * object. */
static PyObject *Atrinik_Object_SetAlignment(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;
	char *align;

	if (!PyArg_ParseTuple(args, "s", &align))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->ALIGNMENT_FORCE && walk->arch->name == hooks->shstr_cons->alignment_force)
		{
			/* We find the alignment of the player, now change it to new one */
			if (walk->title)
			{
				FREE_AND_CLEAR_HASH(walk->title);
			}

			FREE_AND_COPY_HASH(walk->title, align);

			/* Demand update to client */
			CONTR(WHO)->socket.ext_title_flag = 1;

			return wrap_object(walk);
		}
	}

	LOG(llevDebug, "Python Warning:: SetAlignment: Object %s has no alignment_force!\n", hooks->query_name(WHO, NULL));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetAlignmentForce()</h1>
 *
 * Get the alignment_force from object's inventory.
 *
 * @return The alignment force if found. */
static PyObject *Atrinik_Object_GetAlignmentForce(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;

	if (!PyArg_ParseTuple(args, ""))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->ALIGNMENT_FORCE && walk->arch->name == hooks->shstr_cons->alignment_force)
		{
			return wrap_object(walk);
		}
	}

	LOG(llevDebug, "Python Warning:: GetAlignmentForce: Object %s has no aligment_force!\n", hooks->query_name(WHO, NULL));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SetGuildForce(<i>\<string\></i> rank_string)</h1>
 *
 * Set the current rank of object to rank_string.
 *
 * This only sets the title. The guild tag is in slaying field of the
 * force object.
 *
 * To test for guild force, use
 * @ref Atrinik_Object_GetGuildForce "Atrinik.GetGuildForce()".
 *
 * To set the guild tag you can use this function, because it returns the
 * guild force object after setting the title.
 *
 * @param rank_string The rank string in the guild to set.
 * @return The guild force object if found. */
static PyObject *Atrinik_Object_SetGuildForce(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;
	char *guild;

	if (!PyArg_ParseTuple(args, "s", &guild))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->GUILD_FORCE && walk->arch->name == hooks->shstr_cons->guild_force)
		{
			/* We find the rank of the player, now change it to new one */
			if (walk->title)
			{
				FREE_AND_CLEAR_HASH(walk->title);
			}

			if (guild && strcmp(guild, ""))
			{
				FREE_AND_COPY_HASH(walk->title, guild);
			}

			/* Demand update to client */
			CONTR(WHO)->socket.ext_title_flag = 1;

			return wrap_object(walk);
		}
	}

	LOG(llevDebug, "Python Warning:: SetGuildForce: Object %s has no guild_force!\n", hooks->query_name(WHO, NULL));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetGuildForce()</h1>
 *
 * Get the guild force from player's inventory.
 *
 * @return The guild force object if found. */
static PyObject *Atrinik_Object_GetGuildForce(Atrinik_Object *whoptr, PyObject *args)
{
	object *walk;

	if (!PyArg_ParseTuple(args, ""))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name == hooks->shstr_cons->GUILD_FORCE && walk->arch->name == hooks->shstr_cons->guild_force)
		{
			return wrap_object(walk);
		}
	}

	LOG(llevDebug, "Python Warning:: GetGuildForce: Object %s has no guild_force!\n", hooks->query_name(WHO, NULL));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Fix()</h1>
 *
 * Recalculate player's or monster's stats depending on equipment, forces
 * skills, etc.
 *
 * @warning Untested. */
static PyObject *Atrinik_Object_Fix(Atrinik_Object *whoptr, PyObject *args)
{
	if (!PyArg_ParseTuple(args, ""))
	{
		return NULL;
	}

	hooks->fix_player(WHO);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Kill(<i>\<object\></i> what, <i>\<int\></i> how)</h1>
 *
 * Kill an object.
 *
 * @param what The object to kill
 * @param how How to kill the object.
 *
 * @warning Untested. */
static PyObject *Atrinik_Object_Kill(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;
	int ktype;

	if (!PyArg_ParseTuple(args, "O!i", &Atrinik_ObjectType, &whatptr, &ktype))
	{
		return NULL;
	}

	WHAT->speed = 0;
	WHAT->speed_left = 0.0;
	hooks->update_ob_speed(WHAT);

	if (QUERY_FLAG(WHAT, FLAG_REMOVED))
	{
		LOG(llevDebug, "Warning (from KillObject): Trying to remove removed object\n");
		RAISE("Trying to remove removed object");
	}
	else
	{
		WHAT->stats.hp = -1;
		hooks->kill_object(WHAT, 1, WHO, ktype);
	}

	/* This is to avoid the attack routine to continue after we called
	 * killObject, since the attacked object no longer exists.
	 * By fixing guile_current_other to NULL, guile_use_weapon_script will
	 * return -1, meaning the attack function must be immediately terminated. */
	if (WHAT == current_context->other)
	{
		current_context->other = NULL;
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.CastAbility(<i>\<object\></i> target, <i>\<int\></i>
 * spellno, <i>\<int\></i> mode, <i>\<int\></i> direction, <i>\<string\>
 * </i> option)</h1>
 *
 * Object casts the ability numbered spellno on target.
 * @param target The target object
 * @param spellno ID of the spell to cast
 * @param mode Atrinik.CAST_NORMAL or Atrinik.CAST_POTION
 * @param direction The direction to cast the ability in
 * @param option Additional string option(s) */
static PyObject *Atrinik_Object_CastAbility(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *target;
	int spell, dir, mode;
	SpellTypeFrom item;
	char *stringarg;

	if (!PyArg_ParseTuple(args, "O!iiis", &Atrinik_ObjectType, &target, &spell, &mode, &dir, &stringarg))
	{
		return NULL;
	}

	if (WHO->type != PLAYER)
	{
		item = spellNPC;
	}
	else
	{
		if (!mode)
		{
			item = spellNormal;
		}
		else
		{
			item = spellPotion;
		}
	}

	hooks->cast_spell(target->obj, WHO, dir, spell, 1, item, stringarg);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.DoKnowSpell(<i>\<int\></i> spell)</h1>
 *
 * Check if object knowns a given spell.
 * @param spell ID of the spell to check for
 * @return 1 if the object knows the spell, 0 otherwise */
static PyObject *Atrinik_Object_DoKnowSpell(Atrinik_Object *whoptr, PyObject *args)
{
	int spell;

	if (!PyArg_ParseTuple(args, "i", &spell))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->check_spell_known(WHO, spell));
}

/**
 * <h1>object.AcquireSpell(<i>\<int\></i> spell, <i>\<int\></i> mode)
 * </h1>
 *
 * Object will learn or unlearn spell.
 * @param spell ID of the spell to learn/unlearn for
 * @param mode Possible modes:
 * - <b>Atrinik.LEARN</b>: Learn the spell
 * - <b>Atrinik.UNLEARN</b>: Unlearn the spell
 * @return 1 if the object knows the spell, 0 otherwise */
static PyObject *Atrinik_Object_AcquireSpell(Atrinik_Object *whoptr, PyObject *args)
{
	int spell, mode;

	if (!PyArg_ParseTuple(args, "ii", &spell, &mode))
	{
		return NULL;
	}

	if (mode)
	{
		hooks->do_forget_spell(WHO, spell);
	}
	else
	{
		hooks->do_learn_spell(WHO, spell, 0);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.DoKnowSkill(<i>\<int\></i> skill)</h1>
 *
 * Check if object knowns a given skill.
 * @param skill ID of the skill to check for
 * @return 1 if the object knows the skill, 0 otherwise */
static PyObject *Atrinik_Object_DoKnowSkill(Atrinik_Object *whoptr, PyObject *args)
{
	int skill;

	if (!PyArg_ParseTuple(args, "i", &skill))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->check_skill_known(WHO, skill));
}

/**
 * <h1>object.AcquireSkill(<i>\<int\></i> skillno)</h1>
 *
 * Object will learn or unlearn skill.
 * @param skillno ID of the skill to learn/unlearn for
 * @return 1 if the object knows the skill, 0 otherwise */
static PyObject *Atrinik_Object_AcquireSkill(Atrinik_Object *whoptr, PyObject *args)
{
	int skill;

	if (!PyArg_ParseTuple(args, "i", &skill))
	{
		return NULL;
	}

	hooks->learn_skill(WHO, NULL, NULL, skill, 0);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.FindMarkedObject()</h1>
 *
 * Find marked object in object's inventory.
 * @return The marked object, or None if no object is marked. */
static PyObject *Atrinik_Object_FindMarkedObject(Atrinik_Object *whoptr, PyObject *args)
{
	(void) args;

	return wrap_object(hooks->find_marked_object(WHO));
}

/**
 * <h1>object.CheckInvisibleInside(<i>\<string\></i> id)</h1>
 *
 * Find a force inside object's inventory.
 *
 * @param id Slaying field of the force must match this ID.
 * @return The force object is found with matching ID.
 * @warning Untested. */
static PyObject *Atrinik_Object_CheckInvisibleInside(Atrinik_Object *whoptr, PyObject *args)
{
	char *id;
	object *tmp2;

	if (!PyArg_ParseTuple(args, "s", &id))
	{
		return NULL;
	}

	for (tmp2 = WHO->inv; tmp2 !=NULL; tmp2 = tmp2->below)
	{
		if (tmp2->type == FORCE && tmp2->slaying && !strcmp(tmp2->slaying, id))
		{
			break;
		}
	}

	return wrap_object(tmp2);
}

/**
 * <h1>object.CreatePlayerForce(<i>\<string\></i> force_name, <i>\<int\>
 * </i> time)</h1>
 *
 * Creates and inserts an invisible player force in object.
 *
 * The values of the force will affect the object it is in, which should
 * usually be player.
 *
 * @param force_name Name of the player force
 * @param time If non-zero, the force will be removed again after
 * time / 0.02 ticks. Optional, defaults to 0.
 * @return The new player force object. */
static PyObject *Atrinik_Object_CreatePlayerForce(Atrinik_Object *whereptr, PyObject *args)
{
	char *txt;
	object *myob;
	int time = 0;

	if (!PyArg_ParseTuple(args, "s|i", &txt, &time))
	{
		return NULL;
	}

	myob = hooks->get_archetype("player_force");

	if (!myob)
	{
		LOG(llevDebug, "Python WARNING:: CreatePlayerForce: Can't find archetype 'player_force'\n");
		RAISE("Can't find archetype 'player_force'");
	}

	/* For temporary forces */
	if (time > 0)
	{
		SET_FLAG(myob, FLAG_IS_USED_UP);
		myob->stats.food = time;
		myob->speed = 0.02f;
		hooks->update_ob_speed(myob);
	}

	/* Setup the force and put it in activator */
	if (myob->name)
	{
		FREE_AND_CLEAR_HASH(myob->name);
	}

	FREE_AND_COPY_HASH(myob->name, txt);
	myob = hooks->insert_ob_in_ob(myob, WHERE);

	hooks->esrv_send_item(WHERE, myob);

	return wrap_object(myob);
}

/**
 * <h1>object.GetQuestObject(<i>\<string\></i> quest_name)</h1>
 *
 * Get a quest object for specified quest.
 * @param quest_name Name of the quest to look for.
 * @return The quest object if found. */
static PyObject *Atrinik_Object_GetQuestObject(Atrinik_Object *whoptr, PyObject *args)
{
	char *quest_name;
	object *walk;

	if (!PyArg_ParseTuple(args, "s", &quest_name))
	{
		return NULL;
	}

	/* Let's first check the inventory for the quest_container object */
	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->type == QUEST_CONTAINER)
		{
			for (walk = walk->inv; walk != NULL; walk = walk->below)
			{
				if (!strcmp(walk->name, quest_name))
				{
					return wrap_object(walk);
				}
			}

			break;
		}
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.StartQuest(<i>\<string\></i> quest_name)</h1>
 *
 * Create a quest object inside the specified object, starting a new
 * quest.
 * @param quest_name Name of the quest.
 * @return The newly created quest object. */
static PyObject *Atrinik_Object_StartQuest(Atrinik_Object *whoptr, PyObject *args)
{
	object *quest_container, *quest_object, *myob;
	char *quest_name;

	if (!PyArg_ParseTuple(args, "s", &quest_name))
	{
		return NULL;
	}

	myob = hooks->get_archetype(QUEST_CONTAINER_ARCHETYPE);

	if (!(quest_container = hooks->present_in_ob(QUEST_CONTAINER, WHO)))
	{
		quest_container = hooks->get_object();
		hooks->copy_object(myob, quest_container);
		hooks->insert_ob_in_ob(quest_container, WHO);
	}

	quest_object = hooks->get_object();
	hooks->copy_object(myob, quest_object);

	quest_object->magic = 0;
	FREE_AND_COPY_HASH(quest_object->name, quest_name);
	hooks->insert_ob_in_ob(quest_object, quest_container);

	return wrap_object(quest_object);
}

/**
 * <h1>object.CreatePlayerInfo(<i>\<string\></i> name)</h1>
 *
 * Creates a player info object of specified name in object's inventory.
 *
 * The values of a player info  object will NOT affect the object it is
 * in.
 *
 * @param name Name of the player info
 * @return The new player info object */
static PyObject *Atrinik_Object_CreatePlayerInfo(Atrinik_Object *whereptr, PyObject *args)
{
	char *txt;
	object *myob;

	if (!PyArg_ParseTuple(args, "s", &txt))
	{
		return NULL;
	}

	myob = hooks->get_archetype("player_info");

	if (!myob)
	{
		LOG(llevDebug, "Python WARNING:: CreatePlayerInfo: Cant't find archtype 'player_info'\n");
		RAISE("Cant't find archtype 'player_info'");
	}

	/* Setup the info and put it in activator */
	if (myob->name)
	{
		FREE_AND_CLEAR_HASH(myob->name);
	}

	FREE_AND_COPY_HASH(myob->name, txt);
	myob = hooks->insert_ob_in_ob(myob, WHERE);

	hooks->esrv_send_item(WHERE, myob);

	return wrap_object(myob);
}

/**
 * <h1>object.GetPlayerInfo(<i>\<string\></i> name)</h1>
 *
 * Get the first player info object with the specified name in object's
 * inventory.
 *
 * @param name Name of the player info
 * @return The player info object if found, None otherwise. */
static PyObject *Atrinik_Object_GetPlayerInfo(Atrinik_Object *whoptr, PyObject *args)
{
	char *name;
	object *walk;

	if (!PyArg_ParseTuple(args, "s", &name))
	{
		return NULL;
	}

	/* Get the first linked player info arch in this inventory */
	for (walk = WHO->inv; walk != NULL; walk = walk->below)
	{
		if (walk->name && walk->arch->name == hooks->shstr_cons->player_info && !strcmp(walk->name, name))
		{
			return wrap_object(walk);
		}
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.GetNextPlayerInfo(<i>\<object\></i> player_info)</h1>
 *
 * Get next player info object in object's inventory with same name as
 * player_info.
 *
 * @param player_info Previously found player info object.
 * @return The next player info object if found, None otherwise. */
static PyObject *Atrinik_Object_GetNextPlayerInfo(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *myob;
	char name[128];
	object *walk;

	(void) whoptr;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &myob))
	{
		return NULL;
	}

	/* Our check paramters: arch "force_info", name of this arch */
	strncpy(name, STRING_OBJ_NAME(myob->obj), 127);
	name[63] = '\0';

	/* Get the next linked player_info arch in this inventory */
	for (walk = myob->obj->below; walk != NULL; walk = walk->below)
	{
		if (walk->name && walk->arch->name == hooks->shstr_cons->player_info && !strcmp(walk->name, name))
		{
			return wrap_object(walk);
		}
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.CreateInvisibleInside(<i>\<string\></i> id)</h1>
 *
 * Create an invisible force object in object's inventory.
 *
 * @param id String ID of the force object.
 * @return The created invisible force object. */
static PyObject *Atrinik_Object_CreateInvisibleInside(Atrinik_Object *whereptr, PyObject *args)
{
	char *txt;
	object *myob;

	if (!PyArg_ParseTuple(args, "s", &txt))
	{
		return NULL;
	}

	myob = hooks->get_archetype("force");

	if (!myob)
	{
		LOG(llevDebug, "Python WARNING:: CFCreateInvisibleInside: Can't find archtype 'force'\n");
		RAISE("Cant't find archtype 'force'");
	}

	myob->speed = 0.0;
	hooks->update_ob_speed(myob);

	if (myob->slaying)
	{
		FREE_AND_CLEAR_HASH(myob->slaying);
	}

	FREE_AND_COPY_HASH(myob->slaying, txt);
	myob = hooks->insert_ob_in_ob(myob, WHERE);

	hooks->esrv_send_item(WHERE, myob);

	return wrap_object(myob);
}

/**
 * <h1>object.CreateObjectInside(<i>\<string\></i> archname, <i>\<long\>
 * </i> identified, <i>\<long\></i> number, <i>\<long\></i> value)</h1>
 *
 * Creates an object from archname and inserts into object.
 *
 * @param archname Name of the arch
 * @param identifed Either Atrinik.IDENTIFIED or Atrinik.UNIDENTIFIED
 * @param number Number of objects to create
 * @param value If higher than -1, will be used as the new object's value
 * instead of the arch's default. Optional, defaults to -1.
 * @return The created invisible force object. */
static PyObject *Atrinik_Object_CreateObjectInside(Atrinik_Object *whereptr, PyObject *args)
{
	object *myob, *tmp;
	long value = -1, id, nrof = 1;
	char *txt;

	if (!PyArg_ParseTuple(args, "sll|l", &txt, &id, &nrof, &value))
	{
		return NULL;
	}

	myob = hooks->get_archetype(txt);

	if (!myob)
	{
		LOG(llevDebug, "BUG python_CFCreateObjectInside(): ob:>%s< = NULL!\n", hooks->query_name(myob, NULL));
		RAISE("Failed to create the object. Did you use an existing arch?");
	}

	/* -1 means, we use original value */
	if (value != -1)
	{
		myob->value = (sint32) value;
	}

	if (id)
	{
		SET_FLAG(myob, FLAG_IDENTIFIED);
	}

	if (nrof > 1)
	{
		myob->nrof = nrof;
	}

	myob = hooks->insert_ob_in_ob(myob, WHERE);

	/* Make sure inventory image/text is updated */
	for (tmp = WHERE; tmp != NULL; tmp = tmp->env)
	{
		if (tmp->type == PLAYER)
		{
			hooks->esrv_send_item(tmp, myob);
		}
	}

	return wrap_object(myob);
}


/**
 * Helper function for Atrinik_Object_CheckInventory() to recursively
 * check inventories. */
static object *object_check_inventory_rec(object *tmp, int mode, char *arch_name, char *name, char *title, int type)
{
	object *tmp2;

	while (tmp)
	{
		if ((!name || (tmp->name && !strcmp(tmp->name, name))) && (!title || (tmp->title && !strcmp(tmp->title, title))) && (!arch_name || (tmp->arch && tmp->arch->name && !strcmp(tmp->arch->name, arch_name))) && (type == -1 || tmp->type == type))
		{
			return tmp;
		}

		if (mode == 2 || (mode && tmp->type == CONTAINER))
		{
			if ((tmp2 = object_check_inventory_rec(tmp->inv, mode, arch_name, name, title, type)))
			{
				return tmp2;
			}
		}

		tmp = tmp->below;
	}

	return NULL;
}

/**
 * <h1>object.CheckInventory(<i>\<int\></i> mode, <i>\<string|None\></i>
 * arch, <i>\<string|None\></i> name, <i>\<string|None\></i> title, <i>
 * \<int\></i> type)</h1>
 *
 * Looks for a certain arch object in object's inventory.
 *
 * @param mode How to search the inventory. Possible modes:
 * - <b>0</b>: Only inventory
 * - <b>1</b>: Inventory and containers
 * - <b>2</b>: All inventory
 * @param arch Arch name of the object to search for
 * @param name Name of the object. Optional, defaults to ignore name.
 * @param title Title of the object. Optional, defaults to ignore title.
 * @param type Type of the object. Optional, defaults to ignore type.
 * @return The object we wanted if found, None otherwise. */
static PyObject *Atrinik_Object_CheckInventory(Atrinik_Object *whoptr, PyObject *args)
{
	int type = -1, mode = 0;
	char *name = NULL, *title = NULL, *arch_name = NULL;
	object *tmp, *tmp2;

	if (!PyArg_ParseTuple(args, "iz|zzi", &mode, &arch_name, &name, &title, &type))
	{
		return NULL;
	}

	tmp = WHO->inv;

	while (tmp)
	{
		if ( (!name || (tmp->name && !strcmp(tmp->name, name))) && (!title || (tmp->title && !strcmp(tmp->title, title))) && (!arch_name || (tmp->arch && tmp->arch->name && !strcmp(tmp->arch->name, arch_name))) && (type == -1 || tmp->type == type))
		{
			return wrap_object(tmp);
		}

		if (mode == 2 || (mode == 1 && tmp->type == CONTAINER))
		{
			if ((tmp2 = object_check_inventory_rec(tmp->inv, mode, arch_name, name, title, type)))
			{
				return wrap_object(tmp2);
			}
		}

		tmp = tmp->below;
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SetSaveBed(<i>\<map\></i> map, <i>\<int\></i> x, <i>\<int\>
 * </i> y)</h1>
 *
 * Sets new save bed position for object.
 *
 * @param map Map of the new save bed
 * @param x X position of the new save bed
 * @param y Y position of the new save bed */
static PyObject *Atrinik_Object_SetSaveBed(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Map *map;
	int x, y;

	if (!PyArg_ParseTuple(args, "O!ii", &Atrinik_MapType, &map, &x, &y))
	{
		return NULL;
	}

	if (WHO->type == PLAYER)
	{
		strcpy(CONTR(WHO)->savebed_map, map->map->path);
		CONTR(WHO)->bed_x = x;
		CONTR(WHO)->bed_y = y;
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Remove()</h1>
 *
 * Takes the object out of whatever map or inventory it is in. The object
 * can then be inserted or teleported somewhere else, or just left alone
 * for the garbage collection to take care of. */
static PyObject *Atrinik_Object_Remove(Atrinik_Object *whoptr, PyObject *args)
{
	object *myob;
	object *obenv;

	if (!PyArg_ParseTuple(args, ""))
	{
		return NULL;
	}

	myob = WHO;
	obenv = myob->env;

	/* Don't allow removing any of the involved objects. Messes things up... */
	if (current_context->activator == myob || current_context->who == myob || current_context->other == myob)
	{
		RAISE("You are not allowed to remove one of the active objects. Workaround using CFTeleport or some other solution.");
	}

	hooks->remove_ob(myob);

	/* Player inventory can be removed even if the activator is not a player */
	if (obenv != NULL && obenv->type == PLAYER)
	{
		hooks->esrv_send_inventory(obenv, obenv);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.SetPosition(<i>\<int\></i> x, <i>\<int\></i> y)</h1>
 *
 * Sets new position coordinates for the object.
 *
 * Cannot be used to move objects out of containers, use Drop() or
 * TeleportTo() for that.
 * @param x New X position on the same map
 * @param y New Y position on the same map */
static PyObject *Atrinik_Object_SetPosition(Atrinik_Object *whoptr, PyObject *args)
{
	int x, y;

	if (!PyArg_ParseTuple(args, "ii", &x, &y))
	{
		return NULL;
	}

	hooks->transfer_ob(WHO, x, y, 0, NULL, NULL);

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.IdentifyItem(<i>\<object\></i> target, <i>\<object\></i>
 * marked, <i>\<long\></i> mode)</h1>
 *
 * Object identifies object(s) in target's inventory.
 *
 * @param target The target object
 * @param marked Marked object. Only use if mode is
 * Atrinik.IDENTIFY_MARKED, otherwise use None.
 * @param mode Possible modes:
 * - <b>Atrinik.IDENTIFY_NORMAL</b>: Normal identify
 * - <b>Atrinik.IDENTIFY_ALL</b>: Identify all items
 * - <b>Atrinik.IDENTIFY_MARKED</b>: Identify only marked item */
static PyObject *Atrinik_Object_IdentifyItem(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *target;
	PyObject *ob;
	object *marked = NULL;
	long mode;

	if (!PyArg_ParseTuple(args, "O!Ol", &Atrinik_ObjectType, &target, &ob, &mode))
	{
		return NULL;
	}

	if (mode == 2)
	{
		if (!PyObject_TypeCheck(ob, &Atrinik_ObjectType))
		{
			RAISE("Parameter 2 must be a Atrinik.Object for mode IDENTIFY_MARKED");
		}

		marked = ((Atrinik_Object *) ob)->obj;
	}
	else if (mode == 0 || mode == 1)
	{
		if (ob != Py_None)
		{
			RAISE("Parameter 2 must be None for modes IDENTIFY_NORMAL and IDENTIFY_ALL");
		}
	}
	else
	{
		RAISE("Mode must be IDENTIFY_NORMAL, IDENTIFY_ALL or IDENTIFY_MARKED");
	}

	hooks->cast_identify(target->obj, WHO->level, marked, mode);

	if (WHO)
	{
		hooks->play_sound_map(WHO->map, WHO->x, WHO->y, hooks->spells[SP_IDENTIFY].sound, SOUND_SPELL);
	}
	else if (target->obj)
	{
		hooks->play_sound_map(target->obj->map, target->obj->x, target->obj->y, hooks->spells[SP_IDENTIFY].sound, SOUND_SPELL);
	}

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.IsOfType(<i>\<int\></i> type)</h1>
 *
 * Check if specified object is of certain type.
 *
 * @param type The type to check for
 * @return 1 if the object is of specifie type, 0 otherwise */
static PyObject *Atrinik_Object_IsOfType(Atrinik_Object *whoptr, PyObject *args)
{
	int type;

	if (!PyArg_ParseTuple(args, "i", &type))
	{
		return NULL;
	}

	return Py_BuildValue("i", WHO->type == type ? 1 : 0);
}

/**
 * <h1>object.Save()</h1>
 *
 * Dump an object, as if it was being saved to map or player file. Useful
 * for saving the object somewhere for loading later with
 * @ref Atrinik_LoadObject "LoadObject()".
 * @return String containing the saved object. */
static PyObject *Atrinik_Object_Save(Atrinik_Object *whoptr, PyObject *args)
{
	PyObject *ret;
	StringBuffer *sb;
	char *result;

	(void) args;

	sb = hooks->stringbuffer_new();
	hooks->dump_object(WHO, sb);
	result = hooks->stringbuffer_finish(sb);

	ret = Py_BuildValue("s", result);
	free(result);

	return ret;
}

/**
 * <h1>object.GetIP()</h1>
 *
 * Get IP of a specified player object.
 *
 * @param type The type to check for
 * @return The IP address of player */
static PyObject *Atrinik_Object_GetIP(Atrinik_Object *whoptr, PyObject *args)
{
	static char *result;

	(void) args;

	if (WHO->type != PLAYER)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	if (CONTR(WHO))
	{
		result = CONTR(WHO)->socket.host;
		return Py_BuildValue("s", result);
	}
	else
	{
		LOG(llevDebug, "PYTHON:: Error - This object has no controller\n");
		return Py_BuildValue("s", "");
	}
}

/**
 * <h1>object.GetArchName()</h1>
 *
 * Get arch name of an object.
 *
 * @return The arch name of the object. */
static PyObject *Atrinik_Object_GetArchName(Atrinik_Object *whoptr, PyObject *args)
{
	if (!PyArg_ParseTuple(args, ""))
	{
		return NULL;
	}

	return Py_BuildValue("s", WHO->arch->name);
}

/**
 * <h1>object.ShowCost(<i>\<sint64\></i> value)</h1>
 *
 * Show cost of value.
 * @param value Value to show cost from
 * @return string describing value as X gold, X silver, X copper. */
static PyObject *Atrinik_Object_ShowCost(Atrinik_Object *whoptr, PyObject *args)
{
	sint64 value;

	(void) whoptr;

	if (!PyArg_ParseTuple(args, "L", &value))
	{
		return NULL;
	}

	return Py_BuildValue("s", hooks->cost_string_from_value(value));
}

/**
 * <h1>object.GetItemCost(<i>\<object\></i> object, <i>\<int\></i> type)
 * </h1>
 *
 * Get cost of an object in integer value.
 *
 * @param object The object to query cost for
 * @param type Possible types:
 * - <b>Atrinik.COST_TRUE</b>
 * - <b>Atrinik.COST_BUY</b>
 * - <b>Atrinik.COST_SELL</b>
 * @return The cost of the item as integer.
 * @warning Untested. */
static PyObject *Atrinik_Object_GetItemCost(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;
	int flag;

	if (!PyArg_ParseTuple(args, "O!i", &Atrinik_ObjectType, &whatptr, &flag))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->query_cost(WHAT, WHO, flag));
}

/**
 * <h1>object.GetMoney()</h1>
 *
 * Get all the money the object is carrying as integer.
 *
 * @return The amount of money the object is carrying in copper (as
 * integer). */
static PyObject *Atrinik_Object_GetMoney(Atrinik_Object *whoptr, PyObject *args)
{
	(void) args;

	return Py_BuildValue("i", hooks->query_money(WHO));
}

/**
 * <h1>object.PayForItem(<i>\<object\></i> object)</h1>
 *
 * @warning Untested. */
static PyObject *Atrinik_Object_PayForItem(Atrinik_Object *whoptr, PyObject *args)
{
	Atrinik_Object *whatptr;

	if (!PyArg_ParseTuple(args, "O!", &Atrinik_ObjectType, &whatptr))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->pay_for_item(WHAT, WHO));
}

/**
 * <h1>object.PayAmount(<i>\<int\></i> value)</h1>
 *
 * Get the object to pay a specified amount of money in copper.
 *
 * @param value The amount of money in copper to pay for
 * @return 1 if the object paid the money (the object had enough money in
 * inventory), 0 otherwise. */
static PyObject *Atrinik_Object_PayAmount(Atrinik_Object *whoptr, PyObject *args)
{
	int to_pay;

	if (!PyArg_ParseTuple(args, "i", &to_pay))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->pay_for_amount(to_pay, WHO));
}

/**
 * <h1>player.SendCustomCommand(<i>\<int\></i> command_id,
 * <i>\<string\></i> command_data)</h1>
 *
 * Send a fake command to player's client. This can be used to fake a
 * book GUI, for example. */
static PyObject *Atrinik_Object_SendCustomCommand(Atrinik_Object *whoptr, PyObject *args)
{
	char *customcmd;
	char cmd;

	if (!PyArg_ParseTuple(args, "bs", &cmd, &customcmd))
	{
		return NULL;
	}

	if (WHO->type != PLAYER || !CONTR(WHO))
	{
		RAISE("SendCustomCommand(): Can only be used on players.");
	}

	hooks->Write_String_To_Socket(&CONTR(WHO)->socket, cmd, customcmd, strlen(customcmd));

	Py_INCREF(Py_None);
	return Py_None;
}

/**
 * <h1>object.Clone(<i>\<int\></i> mode)</h1>
 *
 * Clone an object.
 *
 * You should do something with the clone.
 * @ref Atrinik_Object_TeleportTo "TeleportTo()" or
 * @ref Atrinik_Object_InsertInside "InsertInside()" are useful functions
 * for that.
 *
 * @param mode Optional mode, one of:
 * - <b>Atrinik.CLONE_WITH_INVENTORY</b> (default)
 * - <b>Atrinik.CLONE_WITHOUT_INVENTORY</b>
 * @return The cloned object. */
static PyObject *Atrinik_Object_Clone(Atrinik_Object *whoptr, PyObject *args)
{
	int mode = 0;
	object *clone;

	if (!PyArg_ParseTuple(args, "|i", &mode))
	{
		return NULL;
	}

	if (!mode)
	{
		clone = hooks->object_create_clone(WHO);
	}
	else
	{
		clone = hooks->get_object();
		hooks->copy_object(WHO, clone);
	}

	if (clone->type == PLAYER || QUERY_FLAG(clone, FLAG_IS_PLAYER))
	{
		clone->type = MONSTER;
		CLEAR_FLAG(clone, FLAG_IS_PLAYER);
	}

	return wrap_object(clone);
}

/**
 * <h1>object.SwapApartments(<i>\<string\></i> oldmap, <i>\<string\></i>
 * newmap, <i>\<int\></i> x, <i>\<int\></i> y)</h1>
 *
 * Swaps oldmap apartment with newmap one.
 * Copies old items from oldmap to newmap at x, y and saves the map.
 *
 * @param oldmap The old apartment map
 * @param oldmap The new apartment map
 * @param x X position to copy the items to
 * @param y Y position to copy the items to */
static PyObject *Atrinik_Object_SwapApartments(Atrinik_Object *whoptr, PyObject *args)
{
	char *mapold, *mapnew;
	int x, y;

	if (!PyArg_ParseTuple(args, "ssii", &mapold, &mapnew, &x, &y))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->swap_apartments(mapold, mapnew, x, y, WHO));
}

/**
 * <h1>object.GetUnmodifiedAttribute(<i>\<int\></i> attribute_id)</h1>
 *
 * @warning Unfinished.
 * @todo Finish. */
static PyObject *Atrinik_Object_GetUnmodifiedAttribute(Atrinik_Object *whoptr, PyObject *args)
{
	int fieldno;

	if (!PyArg_ParseTuple(args, "i", &fieldno))
	{
		return NULL;
	}

	if (fieldno < 0 || fieldno >= (int) NUM_OBJFIELDS)
	{
		RAISE("Illegal field ID");
	}

	if (WHO->type != PLAYER)
	{
		RAISE("Can only be used on players");
	}

	RAISE("Not implemented");

#if 0
	switch (fieldno)
	{
		case OBJFIELD_STAT_INT:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Int);
		case OBJFIELD_STAT_STR:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Str);
		case OBJFIELD_STAT_CHA:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Cha);
		case OBJFIELD_STAT_WIS:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Wis);
		case OBJFIELD_STAT_DEX:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Dex);
		case OBJFIELD_STAT_CON:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Con);
		case OBJFIELD_STAT_POW:
			return Py_BuildValue("i", CONTR(WHO)->orig_stats.Pow);

		default:
			RAISE("No unmodified version of attribute available");
	}
#endif
}

/**
 * <h1>object.GetSaveBed()</h1>
 * Get a player's save bed location.
 * @note Can only be used on player objects.
 * @return A dictionary containing the information about the player's
 * save bed:
 * - <b>map</b>: Map path of the save bed.
 * - <b>x</b>: X location of the save bed.
 * - <b>y</b>: Y location of the save bed. */
static PyObject *Atrinik_Object_GetSaveBed(Atrinik_Object *whoptr, PyObject *args)
{
	PyObject *dict;

	(void) args;

	if (WHO->type != PLAYER)
	{
		RAISE("Can only be used on player objects.");
	}

	dict = PyDict_New();

	PyDict_SetItemString(dict, "map", Py_BuildValue("s", CONTR(WHO)->savebed_map));
	PyDict_SetItemString(dict, "x", Py_BuildValue("i", CONTR(WHO)->bed_x));
	PyDict_SetItemString(dict, "y", Py_BuildValue("i", CONTR(WHO)->bed_y));

	return dict;
}

/**
 * <h1>object.ReadKey(\<string\> key)</h1>
 * Get key value of an object.
 * @param key Key to look for.
 * @return Value for the key if found, None otherwise. */
static PyObject *Atrinik_Object_ReadKey(Atrinik_Object *whoptr, PyObject *args)
{
	char *key;

	if (!PyArg_ParseTuple(args, "s", &key))
	{
		return NULL;
	}

	return Py_BuildValue("s", hooks->object_get_value(WHO, key));
}

/**
 * <h1>object.WriteKey(\<string\> key, [string] value, [int] add_key)</h1>
 * Set the key value of an object.
 * @param key Key to look for.
 * @param value Value to set for the key. If not passed, will clear the
 * key's value if the key is found.
 * @param add_key Whether to add the key if it's not found in the object.
 * Defaults to 1.
 * @return 1 on success, 0 on failure. */
static PyObject *Atrinik_Object_WriteKey(Atrinik_Object *whoptr, PyObject *args)
{
	char *key, *value = NULL;
	int add_key = 1;

	if (!PyArg_ParseTuple(args, "s|si", &key, &value, &add_key))
	{
		return NULL;
	}

	return Py_BuildValue("i", hooks->object_set_value(WHO, key, value, add_key));
}

/**
 * <h1>object.GetEquipment(\<int\> slot)</h1>
 * Get a player's current equipment for a given slot.
 * @param slot One of @ref PLAYER_EQUIP_xxx constants.
 * @return The equipment for the given slot, can be None. */
static PyObject *Atrinik_Object_GetEquipment(Atrinik_Object *whoptr, PyObject *args)
{
	int slot;

	if (!PyArg_ParseTuple(args, "i", &slot))
	{
		return NULL;
	}

	if (WHO->type != PLAYER || !CONTR(WHO))
	{
		RAISE("Can only be used on players.");
	}

	if (slot < 0 || slot >= PLAYER_EQUIP_MAX)
	{
		RAISE("Illegal slot number.");
	}

	return wrap_object(CONTR(WHO)->equipment[slot]);
}

/**
 * <h1>object.GetName(<i>[object]</i> caller)</h1>
 * An equivalent of query_base_name().
 * @param caller Object calling this.
 * @return Full name of the object, including material, name, title,
 * etc. */
static PyObject *Atrinik_Object_GetName(Atrinik_Object *whatptr, PyObject *args)
{
	Atrinik_Object *ob;

	if (!PyArg_ParseTuple(args, "|O!", &Atrinik_ObjectType, &ob))
	{
		return NULL;
	}

	return Py_BuildValue("s", hooks->query_short_name(WHAT, ob ? ob->obj : WHAT));
}

/**
 * <h1>object.GetParty()</h1>
 * Get player's party.
 * @warning Only use on player objects.
 * @return Party if player is member of a party, None otherwise. */
static PyObject *Atrinik_Object_GetParty(Atrinik_Object *whatptr, PyObject *args)
{
	(void) args;

	if (WHAT->type != PLAYER || !CONTR(WHAT))
	{
		RAISE("Can only be used on players.");
	}

	return wrap_party(CONTR(WHAT)->party);
}

/**
 * <h1>object.CreateTimer(<i>\<long\></i> delay, <i>\<int\></i> mode)</h1>
 * Create a timer for an object.
 * @param delay How long until the timer event gets executed for the object
 * @param mode If 1, delay is in seconds, if 2, delay is in ticks (8 ticks = 1 second)
 * @return 0 and higher means success (and represents ID of the created timer),
 * anything lower means a failure of some sort. */
static PyObject *Atrinik_Object_CreateTimer(Atrinik_Object *whatptr, PyObject *args)
{
	int mode, timer;
	long delay;

	if (!PyArg_ParseTuple(args, "li", &delay, &mode))
	{
		return NULL;
	}

	timer = hooks->cftimer_find_free_id();

	if (timer != TIMER_ERR_ID)
	{
		int res = hooks->cftimer_create(timer, delay, WHAT, mode);

		if (res != TIMER_ERR_NONE)
		{
			timer = res;
		}
	}

	return Py_BuildValue("i", timer);
}

/**
 * <h1>player.Sound(<i>\<int\></i> x, <i>\<int\></i> y, <i>\<int\></i> soundnumber, <i>[int]</i> soundtype)</h1>
 *
 * Play a sound to player.
 * @param x X position on the map where the sound is coming from, can be 0.
 * @param y Y position on the map where the sound is coming from, can be 0.
 * @param soundnumber ID of the sound to play.
 * @param soundtype Type of the sound, one of:
 * - SOUNDTYPE_NORMAL (default): Sound number should be one of @ref sound_numbers_normal "normal sound numbers".
 * - SOUNDTYPE_SPELL: Sound number should be one of @ref sound_numbers_spell "spell sound numbers". */
static PyObject *Atrinik_Object_Sound(Atrinik_Object *whoptr, PyObject *args)
{
	int x, y, sound_number, sound_type = SOUND_NORMAL;

	if (!PyArg_ParseTuple(args, "iii|i", &x, &y, &sound_number, &sound_type))
	{
		return NULL;
	}

	if (WHO->type != PLAYER || !CONTR(WHO))
	{
		RAISE("Sound(): Can only be used on players.");
	}

	hooks->play_sound_player_only(CONTR(WHO), sound_number, sound_type, x, y);

	Py_INCREF(Py_None);
	return Py_None;
}

/*@}*/

/** Available Python methods for the AtrinikObject object */
static PyMethodDef ObjectMethods[] =
{
	{"SetSaveBed",                   (PyCFunction) Atrinik_Object_SetSaveBed,             METH_VARARGS, 0},
	{"SwapApartments",               (PyCFunction) Atrinik_Object_SwapApartments,         METH_VARARGS, 0},
	{"GetSkill",                     (PyCFunction) Atrinik_Object_GetSkill,               METH_VARARGS, 0},
	{"SetSkill",                     (PyCFunction) Atrinik_Object_SetSkill,               METH_VARARGS, 0},
	{"ActivateRune",                 (PyCFunction) Atrinik_Object_ActivateRune,           METH_VARARGS, 0},
	{"CastAbility",                  (PyCFunction) Atrinik_Object_CastAbility,            METH_VARARGS, 0},
	{"InsertInside",                 (PyCFunction) Atrinik_Object_InsertInside,           METH_VARARGS, 0},
	{"GetGod",                       (PyCFunction) Atrinik_Object_GetGod,                 METH_VARARGS, 0},
	{"SetGod",                       (PyCFunction) Atrinik_Object_SetGod,                 METH_VARARGS, 0},
	{"TeleportTo",                   (PyCFunction) Atrinik_Object_TeleportTo,             METH_VARARGS, 0},
	{"Apply",                        (PyCFunction) Atrinik_Object_Apply,                  METH_VARARGS, 0},
	{"PickUp",                       (PyCFunction) Atrinik_Object_PickUp,                 METH_VARARGS, 0},
	{"Drop",                         (PyCFunction) Atrinik_Object_Drop,                   METH_VARARGS, 0},
	{"Fix",                          (PyCFunction) Atrinik_Object_Fix,                    METH_VARARGS, 0},
	{"Kill",                         (PyCFunction) Atrinik_Object_Kill,                   METH_VARARGS, 0},
	{"DoKnowSpell",                  (PyCFunction) Atrinik_Object_DoKnowSpell,            METH_VARARGS, 0},
	{"AcquireSpell",                 (PyCFunction) Atrinik_Object_AcquireSpell,           METH_VARARGS, 0},
	{"DoKnowSkill",                  (PyCFunction) Atrinik_Object_DoKnowSkill,            METH_VARARGS, 0},
	{"AcquireSkill",                 (PyCFunction) Atrinik_Object_AcquireSkill,           METH_VARARGS, 0},
	{"FindMarkedObject",             (PyCFunction) Atrinik_Object_FindMarkedObject,       METH_VARARGS, 0},
	{"GetQuestObject",               (PyCFunction) Atrinik_Object_GetQuestObject,         METH_VARARGS, 0},
	{"StartQuest",                   (PyCFunction) Atrinik_Object_StartQuest,             METH_VARARGS, 0},
	{"CreatePlayerForce",            (PyCFunction) Atrinik_Object_CreatePlayerForce,      METH_VARARGS, 0},
	{"CreatePlayerInfo",             (PyCFunction) Atrinik_Object_CreatePlayerInfo,       METH_VARARGS, 0},
	{"GetPlayerInfo",                (PyCFunction) Atrinik_Object_GetPlayerInfo,          METH_VARARGS, 0},
	{"GetNextPlayerInfo",            (PyCFunction) Atrinik_Object_GetNextPlayerInfo,      METH_VARARGS, 0},
	{"CheckInvisibleObjectInside",   (PyCFunction) Atrinik_Object_CheckInvisibleInside,   METH_VARARGS, 0},
	{"CreateInvisibleObjectInside",  (PyCFunction) Atrinik_Object_CreateInvisibleInside,  METH_VARARGS, 0},
	{"CreateObjectInside",           (PyCFunction) Atrinik_Object_CreateObjectInside,     METH_VARARGS, 0},
	{"CheckInventory",               (PyCFunction) Atrinik_Object_CheckInventory,         METH_VARARGS, 0},
	{"Remove",                       (PyCFunction) Atrinik_Object_Remove,                 METH_VARARGS, 0},
	{"SetPosition",                  (PyCFunction) Atrinik_Object_SetPosition,            METH_VARARGS, 0},
	{"IdentifyItem",                 (PyCFunction) Atrinik_Object_IdentifyItem,           METH_VARARGS, 0},
	{"Deposit",                      (PyCFunction) Atrinik_Object_Deposit,                METH_VARARGS, 0},
	{"Withdraw",                     (PyCFunction) Atrinik_Object_Withdraw,               METH_VARARGS, 0},
	{"Communicate",                  (PyCFunction) Atrinik_Object_Communicate,            METH_VARARGS, 0},
	{"Say",                          (PyCFunction) Atrinik_Object_Say,                    METH_VARARGS, 0},
	{"SayTo",                        (PyCFunction) Atrinik_Object_SayTo,                  METH_VARARGS, 0},
	{"Write",                        (PyCFunction) Atrinik_Object_Write,                  METH_VARARGS, 0},
	{"SetGender",                    (PyCFunction) Atrinik_Object_SetGender,              METH_VARARGS, 0},
	{"GetGender",                    (PyCFunction) Atrinik_Object_GetGender,              METH_VARARGS, 0},
	{"SetRank",                      (PyCFunction) Atrinik_Object_SetRank,                METH_VARARGS, 0},
	{"GetRank",                      (PyCFunction) Atrinik_Object_GetRank,                METH_VARARGS, 0},
	{"SetAlignment",                 (PyCFunction) Atrinik_Object_SetAlignment,           METH_VARARGS, 0},
	{"GetAlignmentForce",            (PyCFunction) Atrinik_Object_GetAlignmentForce,      METH_VARARGS, 0},
	{"SetGuildForce",                (PyCFunction) Atrinik_Object_SetGuildForce,          METH_VARARGS, 0},
	{"GetGuildForce",                (PyCFunction) Atrinik_Object_GetGuildForce,          METH_VARARGS, 0},
	{"IsOfType",                     (PyCFunction) Atrinik_Object_IsOfType,               METH_VARARGS, 0},
	{"Save",                         (PyCFunction) Atrinik_Object_Save,                   METH_VARARGS, 0},
	{"GetIP",                        (PyCFunction) Atrinik_Object_GetIP,                  METH_VARARGS, 0},
	{"GetArchName",                  (PyCFunction) Atrinik_Object_GetArchName,            METH_VARARGS, 0},
	{"ShowCost",                     (PyCFunction) Atrinik_Object_ShowCost,               METH_VARARGS, 0},
	{"GetItemCost",                  (PyCFunction) Atrinik_Object_GetItemCost,            METH_VARARGS, 0},
	{"GetMoney",                     (PyCFunction) Atrinik_Object_GetMoney,               METH_VARARGS, 0},
	{"PayForItem",                   (PyCFunction) Atrinik_Object_PayForItem,             METH_VARARGS, 0},
	{"PayAmount",                    (PyCFunction) Atrinik_Object_PayAmount,              METH_VARARGS, 0},
	{"GetUnmodifiedAttribute",       (PyCFunction) Atrinik_Object_GetUnmodifiedAttribute, METH_VARARGS, 0},
	{"SendCustomCommand",            (PyCFunction) Atrinik_Object_SendCustomCommand,      METH_VARARGS, 0},
	{"CheckTrigger",                 (PyCFunction) Atrinik_Object_CheckTrigger,           METH_VARARGS, 0},
	{"Clone",                        (PyCFunction) Atrinik_Object_Clone,                  METH_VARARGS, 0},
	{"GetSaveBed",                   (PyCFunction) Atrinik_Object_GetSaveBed,             METH_VARARGS, 0},
	{"ReadKey",                      (PyCFunction) Atrinik_Object_ReadKey,                METH_VARARGS, 0},
	{"WriteKey",                     (PyCFunction) Atrinik_Object_WriteKey,               METH_VARARGS, 0},
	{"GetEquipment",                 (PyCFunction) Atrinik_Object_GetEquipment,           METH_VARARGS, 0},
	{"GetName",                      (PyCFunction) Atrinik_Object_GetName,                METH_VARARGS, 0},
	{"GetParty",                     (PyCFunction) Atrinik_Object_GetParty,               METH_VARARGS, 0},
	{"CreateTimer",                  (PyCFunction) Atrinik_Object_CreateTimer,            METH_VARARGS, 0},
	{"Sound",                        (PyCFunction) Atrinik_Object_Sound,                  METH_VARARGS, 0},
	{NULL, NULL, 0, 0}
};

/**
 * Get object's attribute.
 * @param whoptr Python object wrapper.
 * @param context Void pointer to the field.
 * @return Python object with the attribute value, NULL on failure. */
static PyObject *Object_GetAttribute(Atrinik_Object *whoptr, void *context)
{
	void *field_ptr, *field_ptr2 = NULL;
	obj_fields_struct *field = (obj_fields_struct *) context;

	field_ptr = (void *) ((char *) WHO + field->offset);

	if (field->type == FIELDTYPE_OBJECTREF)
	{
		field_ptr2 = (void *) ((char *) WHO + field->extra_data);
	}

	return generic_field_getter(field->type, field_ptr, field_ptr2);
}

/**
 * Set attribute of an object.
 * @param whoptr Python object wrapper.
 * @param value Value to set.
 * @param context Void pointer to the field.
 * @return 0 on success, -1 on failure.
 * @todo Better handling of types, signs, and overflows. */
static int Object_SetAttribute(Atrinik_Object *whoptr, PyObject *value, void *context)
{
	void *field_ptr;
	object *tmp;
	obj_fields_struct *field = (obj_fields_struct *) context;

	if ((field->flags & FIELDFLAG_READONLY) || ((field->flags & FIELDFLAG_PLAYER_READONLY) && WHO->type == PLAYER))
	{
		INTRAISE("Trying to modify readonly field.");
	}

	field_ptr = (void *) ((char *) WHO + field->offset);

	if (generic_field_setter(field->type, field_ptr, value) == -1)
	{
		return -1;
	}

	/* Make sure the inventory image/text is updated. */
	for (tmp = WHO->env; tmp; tmp = tmp->env)
	{
		if (tmp->type == PLAYER)
		{
			hooks->esrv_send_item(tmp, WHO);
		}
	}

	/* Special handling for some player stuff. */
	if (WHO->type == PLAYER)
	{
		if (field->offset == offsetof(object, stats.Int))
		{
			CONTR(WHO)->orig_stats.Int = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Str))
		{
			CONTR(WHO)->orig_stats.Str = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Cha))
		{
			CONTR(WHO)->orig_stats.Cha = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Wis))
		{
			CONTR(WHO)->orig_stats.Wis = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Dex))
		{
			CONTR(WHO)->orig_stats.Dex = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Con))
		{
			CONTR(WHO)->orig_stats.Con = (sint8) PyInt_AsLong(value);
		}
		else if (field->offset == offsetof(object, stats.Pow))
		{
			CONTR(WHO)->orig_stats.Pow = (sint8) PyInt_AsLong(value);
		}

		if (field->flags & FIELDFLAG_PLAYER_FIX)
		{
			hooks->fix_player(WHO);
		}
	}

	return 0;
}

/**
 * Get object's flag.
 * @param whoptr Python object wrapper.
 * @param context Void pointer to the flag ID.
 * @return 1 if the object has the flag set, 0 otherwise. */
static PyObject *Object_GetFlag(Atrinik_Object *whoptr, void *context)
{
	size_t flagno = (size_t) context;

	if (flagno >= NUM_FLAGS)
	{
		RAISE("Unknown flag");
	}

	return Py_BuildValue("i", QUERY_FLAG(WHO, flagno) ? 1 : 0);
}

/**
 * Set flag for an object.
 * @param whoptr Python object wrapper.
 * @param val Value to set.
 * @param context Void pointer to the flag ID.
 * @return 0 on success, -1 on failure.
 * @todo If gender of player has changed, demand update to client. */
static int Object_SetFlag(Atrinik_Object *whoptr, PyObject *val, void *context)
{
	int value;
	object *tmp;
	size_t flagno = (size_t) context;

	if (flagno >= NUM_FLAGS)
	{
		INTRAISE("Unknown flag.");
	}

	if (!PyInt_Check(val))
	{
		INTRAISE("Value must be 0 or 1.");
	}

	value = PyInt_AsLong(val);

	if (value < 0 || value > 1)
	{
		INTRAISE("Value must be 0 or 1.");
	}

	if (value)
	{
		SET_FLAG(WHO, flagno);
	}
	else
	{
		CLEAR_FLAG(WHO, flagno);
	}

	/* Make sure the inventory image/text is updated */
	for (tmp = WHO->env; tmp; tmp = tmp->env)
	{
		if (tmp->type == PLAYER)
		{
			hooks->esrv_send_item(tmp, WHO);
		}
	}

	/* If gender has changed, demand update to client. */
#if 0
	if ()
	{
		CONTR(WHO)->socket.ext_title_flag = 1;
	}
#endif

	return 0;
}

/**
 * Create a new object wrapper.
 * @param type Type object.
 * @param args Unused.
 * @param kwds Unused.
 * @return The new wrapper. */
static PyObject *Atrinik_Object_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	Atrinik_Object *self = (Atrinik_Object *) type->tp_alloc(type, 0);

	(void) args;
	(void) kwds;

	if (self)
	{
		self->obj = NULL;
	}

	return (PyObject *) self;
}

/**
 * Free an object wrapper.
 * @param self The wrapper to free. */
static void Atrinik_Object_dealloc(Atrinik_Object *self)
{
	self->obj = NULL;
#ifndef IS_PY_LEGACY
	Py_TYPE(self)->tp_free((PyObject *) self);
#else
	self->ob_type->tp_free((PyObject *) self);
#endif
}

/**
 * Return a string representation of an object.
 * @param self The object type.
 * @return Python object containing the arch name and name of the object. */
static PyObject *Atrinik_Object_str(Atrinik_Object *self)
{
	char buf[HUGE_BUF];

	snprintf(buf, sizeof(buf), "[%s \"%s\"]", STRING_OBJ_ARCH_NAME(self->obj), STRING_OBJ_NAME(self->obj));
	return Py_BuildValue("s", buf);
}

static int Atrinik_Object_InternalCompare(Atrinik_Object *left, Atrinik_Object *right)
{
	OBJEXISTCHECK_INT(left);
	OBJEXISTCHECK_INT(right);
	return (left->obj < right->obj ? -1 : (left->obj == right->obj ? 0 : 1));
}

static PyObject *Atrinik_Object_RichCompare(Atrinik_Object *left, Atrinik_Object *right, int op)
{
	int result;

	if (!left || !right || !PyObject_TypeCheck((PyObject *) left, &Atrinik_ObjectType) || !PyObject_TypeCheck((PyObject *) right, &Atrinik_ObjectType))
	{
		Py_INCREF(Py_NotImplemented);
		return Py_NotImplemented;
	}

	result = Atrinik_Object_InternalCompare(left, right);

	/* Handle removed objects. */
	if (result == -1 && PyErr_Occurred())
	{
		return NULL;
	}

	/* Based on how Python 3.0 (GPL compatible) implements it for internal types: */
	switch (op)
	{
		case Py_EQ:
			result = (result == 0);
			break;
		case Py_NE:
			result = (result != 0);
			break;
		case Py_LE:
			result = (result <= 0);
			break;
		case Py_GE:
			result = (result >= 0);
			break;
		case Py_LT:
			result = (result == -1);
			break;
		case Py_GT:
			result = (result == 1);
			break;
	}

	return PyBool_FromLong(result);
}

/**
 * Start iterating.
 * @param seq Object to start iterating from.
 * @return A new instance of PyObject, containing Atrinik_Object, with a reference
 * to 'seq'. */
static PyObject *object_iter(PyObject *seq)
{
	Atrinik_Object *obj, *orig_obj = (Atrinik_Object *) seq;

	if (!orig_obj->obj)
	{
		return NULL;
	}

	obj = PyObject_NEW(Atrinik_Object, &Atrinik_ObjectType);
	Py_INCREF(seq);
	obj->iter = (Atrinik_Object *) seq;
	obj->iter_type = OBJ_ITER_TYPE_NONE;

	/* Select which iteration type we're doing. It's possible that
	 * an object has both below and above set (it's not the first and
	 * not the last object), in which case we will prefer below. */
	if (orig_obj->obj->below)
	{
		obj->iter_type = OBJ_ITER_TYPE_BELOW;
	}
	else if (orig_obj->obj->above)
	{
		obj->iter_type = OBJ_ITER_TYPE_ABOVE;
	}

	return (PyObject *) obj;
}

/**
 * Get next object for iteration.
 * @param obj Previous object.
 * @return Next object, NULL if there is nothing left. */
static PyObject *object_iternext(Atrinik_Object *obj)
{
	/* Do we need to iterate? */
	if (obj->iter_type != OBJ_ITER_TYPE_NONE)
	{
		object *tmp = obj->iter->obj;

		/* Check which way we're iterating. */
		if (obj->iter_type == OBJ_ITER_TYPE_BELOW)
		{
			obj->iter->obj = tmp->below;
		}
		else if (obj->iter_type == OBJ_ITER_TYPE_ABOVE)
		{
			obj->iter->obj = tmp->above;
		}

		/* Nothing left, so mark iter_type to show that. */
		if (!obj->iter->obj)
		{
			obj->iter_type = OBJ_ITER_TYPE_NONE;
		}

		return wrap_object(tmp);
	}

	return NULL;
}

/** This is filled in when we initialize our object type. */
static PyGetSetDef Object_getseters[NUM_OBJFIELDS + NUM_FLAGS + 1];

/** Our actual Python ObjectType. */
PyTypeObject Atrinik_ObjectType =
{
#ifdef IS_PY3K
	PyVarObject_HEAD_INIT(NULL, 0)
#else
	PyObject_HEAD_INIT(NULL)
	0,
#endif
	"Atrinik.Object",
	sizeof(Atrinik_Object),
	0,
	(destructor) Atrinik_Object_dealloc,
	NULL, NULL, NULL,
#ifdef IS_PY3K
	NULL,
#else
	(cmpfunc) Atrinik_Object_InternalCompare,
#endif
	0, 0, 0, 0, 0, 0,
	(reprfunc) Atrinik_Object_str,
	0, 0, 0,
	Py_TPFLAGS_DEFAULT,
	"Atrinik objects",
	NULL, NULL,
	(richcmpfunc) Atrinik_Object_RichCompare,
	0,
	(getiterfunc) object_iter,
	(iternextfunc) object_iternext,
	ObjectMethods,
	0,
	Object_getseters,
	0, 0, 0, 0, 0, 0, 0,
	Atrinik_Object_new,
	0, 0, 0, 0, 0, 0, 0, 0
#ifndef IS_PY_LEGACY
	, 0
#endif
};

/**
 * Initialize the object wrapper.
 * @param module The Atrinik Python module.
 * @return 1 on success, 0 on failure. */
int Atrinik_Object_init(PyObject *module)
{
	size_t i, flagno;

	/* Field getseters */
	for (i = 0; i < NUM_OBJFIELDS; i++)
	{
		PyGetSetDef *def = &Object_getseters[i];

		def->name = obj_fields[i].name;
		def->get = (getter) Object_GetAttribute;
		def->set = (setter) Object_SetAttribute;
		def->doc = NULL;
		def->closure = (void *) &obj_fields[i];
	}

	/* Flag getseters */
	for (flagno = 0; flagno < NUM_FLAGS; flagno++)
	{
		if (flag_names[flagno])
		{
			PyGetSetDef *def = &Object_getseters[i++];

			def->name = flag_names[flagno];
			def->get = (getter) Object_GetFlag;
			def->set = (setter) Object_SetFlag;
			def->doc = NULL;
			def->closure = (void *) flagno;
		}
	}

	Object_getseters[i].name = NULL;

	/* Add constants */
	for (i = 0; object_constants[i].name; i++)
	{
		if (PyModule_AddIntConstant(module, object_constants[i].name, object_constants[i].value))
		{
			return 0;
		}
	}

	Atrinik_ObjectType.tp_new = PyType_GenericNew;

	if (PyType_Ready(&Atrinik_ObjectType) < 0)
	{
		return 0;
	}

	Py_INCREF_TYPE(&Atrinik_ObjectType);
	PyModule_AddObject(module, "Object", (PyObject *) &Atrinik_ObjectType);

	return 1;
}

/**
 * Utility method to wrap an object.
 * @param what Object to wrap.
 * @return Python object wrapping the real object. */
PyObject *wrap_object(object *what)
{
	Atrinik_Object *wrapper;

	/* Return None if no object was to be wrapped. */
	if (!what)
	{
		Py_INCREF(Py_None);
		return Py_None;
	}

	wrapper = PyObject_NEW(Atrinik_Object, &Atrinik_ObjectType);

	if (wrapper)
	{
		wrapper->obj = what;
	}

	return (PyObject *) wrapper;
}
