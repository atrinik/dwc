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
 * Atrinik plugin support header file.
 *
 * @author Yann Chachkoff */

#ifndef PLUGIN_H
#define PLUGIN_H

#ifndef WIN32
#	include <dlfcn.h>
#endif

#undef MODULEAPI

#ifdef WIN32
#	ifdef PYTHON_PLUGIN_EXPORTS
#		define MODULEAPI __declspec(dllexport)
#	else
#		define MODULEAPI __declspec(dllimport)
#	endif
#else
#	define MODULEAPI
#endif

#include <global.h>

#ifdef HAVE_TIME_H
#	include <time.h>
#endif

#ifndef WIN32
#include <dirent.h>
#endif

/**
 * @defgroup event_numbers Event number codes
 * Event ID codes.
 *@{*/

/** No event. This exists only to reserve the "0". */
#define EVENT_NONE     0
/** Object applied-unapplied. */
#define EVENT_APPLY    1
/** Monster attacked or Scripted Weapon used. */
#define EVENT_ATTACK   2
/** Player or monster dead. */
#define EVENT_DEATH    3
/** Object dropped on the floor. */
#define EVENT_DROP     4
/** Object picked up. */
#define EVENT_PICKUP   5
/** Someone speaks. */
#define EVENT_SAY      6
/** Thrown object stopped. */
#define EVENT_STOP     7
/** Triggered each time the object can react/move. */
#define EVENT_TIME     8
/** Object is thrown. */
#define EVENT_THROW    9
/** Button pushed, lever pulled, etc. */
#define EVENT_TRIGGER  10
/** Container closed. */
#define EVENT_CLOSE	   11
/** Timer connected triggered it. */
#define EVENT_TIMER    12

/** A new character has been created. */
#define EVENT_BORN     13
/** Global time event. */
#define EVENT_CLOCK    14
/** Triggered when the server crashes. Not recursive */
#define EVENT_CRASH    15
/** Global Death event */
#define EVENT_GDEATH   16
/** Triggered when anything got killed by anyone. */
#define EVENT_GKILL    17
/** Player login. */
#define EVENT_LOGIN    18
/** Player logout. */
#define EVENT_LOGOUT   19
/** A player entered a map. */
#define EVENT_MAPENTER 20
/** A player left a map. */
#define EVENT_MAPLEAVE 21
/** A map is resetting. */
#define EVENT_MAPRESET 22
/** A player character has been removed. */
#define EVENT_REMOVE   23
/** A player shouts something. */
#define EVENT_SHOUT    24
/** A player tells something. */
#define EVENT_TELL     25
/*@}*/

/** Number of local events */
#define NR_LOCAL_EVENTS 13

/** Number of events. */
#define NR_EVENTS 26

/**
 * Get an event flag from event number code.
 * @see event_numbers */
#define EVENT_FLAG(x) (1 << (x - 1))

/**
 * Check to see if object has an event in its object::event_flags.
 * @param ob Object.
 * @param event Event to check. */
#define HAS_EVENT(ob, event) (ob->event_flags & EVENT_FLAG(event))

/**
 * The plugin hook list.
 *
 * If you need a function or variable from server accessed by a plugin,
 * add it here and to ::hooklist in plugins.c. */
struct plugin_hooklist
{
	char *(*query_name)(object *, object *);
	const char *(*re_cmp)(const char *, const char *);
	object *(*present_in_ob)(unsigned char, object *);
	int (*players_on_map)(mapstruct *);
	char *(*create_pathname)(const char *);
	char *(*normalize_path)(const char *, const char *, char *);
	void (*LOG)(LogLevel, const char *, ...);
	void (*free_string_shared)(const char *);
	const char *(*add_string)(const char *);
	void (*remove_ob)(object *);
	void (*fix_player)(object *);
	object *(*insert_ob_in_ob)(object *, object *);
	void (*new_info_map)(int, mapstruct *, int, int, int, const char *);
	void (*new_info_map_except)(int , mapstruct *, int, int, int, object *, object *, const char *);
	void (*spring_trap)(object *, object *);
	int (*cast_spell)(object *, object *, int, int, int, SpellTypeFrom, char *);
	void (*update_ob_speed)(object *);
	int (*command_rskill)(object *, char *);
	void (*become_follower)(object *, object *);
	void (*pick_up)(object *, object *);
	mapstruct *(*get_map_from_coord)(mapstruct *, int *, int *);
	void (*esrv_send_item)(object *, object *);
	player *(*find_player)(char *);
	int (*manual_apply)(object *, object *, int);
	int (*command_drop)(object *, char *);
	int (*transfer_ob)(object *, int, int, int, object *, object *);
	int (*kill_object)(object *, int, object *, int);
	void (*do_learn_spell)(object *, int, int);
	void (*do_forget_spell)(object *, int);
	int (*look_up_spell_name)(const char *);
	int (*check_spell_known)(object *, int);
	void (*esrv_send_inventory)(object *, object *);
	object *(*get_archetype)(const char *);
	mapstruct *(*ready_map_name)(const char *, int);
	sint64 (*add_exp)(object *, sint64, int);
	const char *(*determine_god)(object *);
	object *(*find_god)(const char *);
	void (*register_global_event)(const char *, int);
	void (*unregister_global_event)(const char *, int);
	object *(*load_object_str)(char *);
	sint64 (*query_cost)(object *, object *, int);
	sint64 (*query_money)(object *);
	int (*pay_for_item)(object *, object *);
	int (*pay_for_amount)(sint64, object *);
	void (*new_draw_info)(int, object *, const char *);
	void (*communicate)(object *, char *);
	object *(*object_create_clone)(object *);
	object *(*get_object)();
	void (*copy_object)(object *, object *, int);
	void (*enter_exit)(object *, object *);
	void (*play_sound_map)(mapstruct *, int, int, int, int);
	int (*learn_skill)(object *, object *, char *, int, int);
	object *(*find_marked_object)(object *);
	int (*cast_identify)(object *, int, object *, int);
	int (*lookup_skill_by_name)(char *);
	int (*check_skill_known)(object *, int);
	archetype *(*find_archetype)(const char *);
	object *(*arch_to_object)(archetype *);
	object *(*insert_ob_in_map)(object *, mapstruct *, object *, int);
	char *(*cost_string_from_value)(sint64);
	int (*bank_deposit)(object *, object *, char *);
	int (*bank_withdraw)(object *, object *, char *);
	int (*swap_apartments)(char *, char *, int, int, object *);
	int (*player_exists)(char *);
	void (*get_tod)(timeofday_t *);
	const char *(*object_get_value)(const object *, const char *const);
	int (*object_set_value)(object *, const char *, const char *, int);
	void (*drop)(object *, object *);
	char *(*query_short_name)(object *, object *);
	object *(*beacon_locate)(const char *);
	char *(*strdup_local)(const char *);
	void (*adjust_player_name)(char *);
	party_struct *(*find_party)(const char *);
	void (*add_party_member)(party_struct *, object *);
	void (*remove_party_member)(party_struct *, object *);
	void (*send_party_message)(party_struct *, char *, int, object *);
	void (*Write_String_To_Socket)(socket_struct *, char, char *, int);
	void (*dump_object)(object *, StringBuffer *);
	StringBuffer *(*stringbuffer_new)();
	char *(*stringbuffer_finish)(StringBuffer *);
	char *(*cleanup_chat_string)(char *);
	int (*cftimer_find_free_id)();
	int (*cftimer_create)(int, long, object *, int);
	int (*cftimer_destroy)(int);
	int (*find_face)(char *, int);
	int (*find_animation)(char *);
	void (*play_sound_player_only)(player *, int, int, int, int);
	void (*new_draw_info_format)(int, object *, char *, ...);
	int (*was_destroyed)(object *, tag_t);
	int (*object_get_gender)(object *);
	int (*change_abil)(object *, object *);
	object *(*decrease_ob_nr)(object *, uint32);
	int (*check_walk_off)(object *, object *, int);
	int (*wall)(mapstruct *, int, int);
	int (*blocked)(object *, mapstruct *, int, int, int);

	const char **season_name;
	const char **weekdays;
	const char **month_name;
	const char **periodsofday;
	spell *spells;
	struct shstr_constants *shstr_cons;
	const char **gender_noun;
	const char **gender_subjective;
	const char **gender_subjective_upper;
	const char **gender_objective;
	const char **gender_possessive;
	const char **gender_reflexive;
	const char **object_flag_names;
};

/** General API function. */
typedef void *(*f_plug_api) (int *type, ...);
/** First function called in a plugin. */
typedef void *(*f_plug_init) (struct plugin_hooklist *hooklist);
/** Function called after the plugin was initialized. */
typedef void *(*f_plug_pinit) ();

#ifndef WIN32
	/** Library handle. */
#	define LIBPTRTYPE void *
	/** Load a shared library. */
#	define plugins_dlopen(fname) dlopen(fname, RTLD_NOW | RTLD_GLOBAL)
	/** Unload a shared library. */
#	define plugins_dlclose(lib) dlclose(lib)
	/** Get a function from a shared library. */
#	define plugins_dlsym(lib, name) dlsym(lib, name)
	/** Library error. */
#	define plugins_dlerror() dlerror()
#else
#	define LIBPTRTYPE HMODULE
#	define plugins_dlopen(fname) LoadLibrary(fname)
#	define plugins_dlclose(lib) FreeLibrary(lib)
#	define plugins_dlsym(lib, name) GetProcAddress(lib, name)
#endif

/** One loaded plugin. */
typedef struct _atrinik_plugin
{
	/** Event handler function. */
	f_plug_api eventfunc;

	/** Plugin getProperty function. */
	f_plug_api propfunc;

	/** Pointer to the plugin library. */
	LIBPTRTYPE libptr;

	/** Plugin identification string. */
	char id[MAX_BUF];

	/** Plugin's full name. */
	char fullname[MAX_BUF];

	/** Global events registered. */
	sint8 gevent[NR_EVENTS];

	/** Next plugin in list. */
	struct _atrinik_plugin *next;
} atrinik_plugin;

/**
 * @defgroup exportable_plugin_functions Exportable plugin functions
 * Exportable functions. Any plugin should define all these.
 *@{*/
/**
 * Called when the plugin initialization process starts.
 * @param hooklist Plugin hooklist to register. */
extern MODULEAPI void initPlugin(struct plugin_hooklist *hooklist);

/**
 * Called to ask various informations about the plugin.
 * @param type Integer pointer for va_start().
 * @return Return value depends on the type of information requested.
 * Can be NULL. */
extern MODULEAPI void *getPluginProperty(int *type, ...);

/**
 * Called whenever an event occurs.
 * @param type Integer pointer for va_start().
 * @return Integer containing the event's return value. */
extern MODULEAPI void *triggerEvent(int *type, ...);

/**
 * Called by the server when the plugin loading is completed. */
extern MODULEAPI void postinitPlugin();
/*@}*/

#endif
