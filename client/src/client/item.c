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
 * Object management. */

#include <include.h>

/** The list of free (unused) objects */
static object *free_objects = NULL;

/**
 * Allocates a new object.
 * @return The object. */
static object *object_new()
{
	object *op = calloc(1, sizeof(object));

	if (!op)
	{
		LOG(llevError, "object_new(): Out of memory.\n");
	}

	return op;
}

/**
 * Allocates a list of objects.
 * @param nrof How many objects to allocate.
 * @return The allocated objects in a list. */
static object *objects_alloc(int nrof)
{
	object *op, *list;
	int i;

	list = op = object_new();

	for (i = 1; i < nrof; i++)
	{
		op->next = object_new();
		op->next->prev = op;
		op = op->next;
	}

	return list;
}

/**
 * Frees all objects in a list.
 * @param op Start of the list. */
void objects_free(object *op)
{
	object *next;

	while (op)
	{
		if (op->inv)
		{
			objects_free(op->inv);
		}

		next = op->next;
		free(op);
		op = next;
	}
}
/**
 * Find an object inside another object, but not inside inventories.
 * @param op Object to search in.
 * @param tag ID of the object we're looking for.
 * @return Matching object if found, NULL otherwise. */
object *object_find_object_inv(object *op, sint32 tag)
{
	object *tmp;

	for (tmp = op->inv; tmp; tmp = tmp->next)
	{
		if (tmp->tag == tag)
		{
			return op;
		}
	}

	return NULL;
}

/**
 * Find an object inside another object by its tag.
 * @param op Object to search in.
 * @param tag ID of the object we're looking for.
 * @return Matching object if found, NULL otherwise. */
object *object_find_object(object *op, sint32 tag)
{
	for (; op; op = op->next)
	{
		if (op->tag == tag)
		{
			return op;
		}
		else if (op->inv)
		{
			object *tmp = object_find_object(op->inv, tag);

			if (tmp)
			{
				return tmp;
			}
		}
	}

	return NULL;
}

/**
 * Attempts to find an object by its tag, wherever it may be.
 * @param tag Tag to look for.
 * @return Matching object if found, NULL otherwise. */
object *object_find(sint32 tag)
{
	object *op;

	if (tag == 0)
	{
		return cpl.below;
	}

	if (tag == -1)
	{
		return cpl.sack;
	}

	if (tag == -2)
	{
		return cpl.shop;
	}

	/* Below the player. */
	if (cpl.below)
	{
		op = object_find_object(cpl.below->inv, tag);

		if (op)
		{
			return op;
		}
	}

	/* In shop. */
	if (cpl.shop)
	{
		op = object_find_object(cpl.shop->inv, tag);

		if (op)
		{
			return op;
		}
	}

	/* Open container. */
	if (cpl.sack)
	{
		op = object_find_object(cpl.sack->inv, tag);

		if (op)
		{
			return op;
		}
	}

	/* Last attempt, inside the player. */
	return object_find_object(cpl.ob, tag);
}

/**
 * Remove an object.
 * @param op What to remove. */
void object_remove(object *op)
{
	if (!op || op == cpl.ob || op == cpl.below || op == cpl.sack)
	{
		return;
	}

	if (op->inv)
	{
		object_remove_inventory(op);
	}

	if (op->prev)
	{
		op->prev->next = op->next;
	}
	else
	{
		op->env->inv = op->next;
	}

	if (op->next)
	{
		op->next->prev = op->prev;
	}

	/* Add object to the list of free objects. */
	op->next = free_objects;

	if (op->next)
	{
		op->next->prev = op;
	}

	free_objects = op;

	/* Clear the object so it can be reused. */
	memset(op, 0, sizeof(object));
}

/**
 * Remove all items in object's inventory.
 * @param op The object to remove inventory of. */
void object_remove_inventory(object *op)
{
	if (!op)
	{
		return;
	}

	while (op->inv)
	{
		object_remove(op->inv);
	}
}

/**
 * Adds an object to inventory of 'env'.
 * @param env Which object to add to.
 * @param op Object to add.
 * @param bflag If 1, the object will be added to the end of the
 * inventory instead of the start. */
static void object_add(object *env, object *op, int bflag)
{
	object *tmp;

	if (!op)
	{
		return;
	}

	if (!bflag)
	{
		op->next = env->inv;

		if (op->next)
		{
			op->next->prev = op;
		}

		op->prev = NULL;
		env->inv = op;
		op->env = env;
	}
	else
	{
		for (tmp = env->inv; tmp && tmp->next; tmp = tmp->next)
		{
		}

		op->next = NULL;
		op->prev = tmp;
		op->env = env;

		if (!tmp)
		{
			env->inv = op;
		}
		else
		{
			if (tmp->next)
			{
				tmp->next->prev = op;
			}

			tmp->next = op;
		}
	}
}

/**
 * Creates a new object and inserts it into 'env'.
 * @param env Which object to insert the created object into. Can be NULL
 * not to insert the created object anywhere.
 * @param tag The object's ID.
 * @param bflag If 1, the object will be added to the end of the
 * inventory instead of the start.
 * @return The created object. */
object *object_create(object *env, sint32 tag, int bflag)
{
	object *op;

	/* Allocate more objects if needed. */
	if (!free_objects)
	{
		free_objects = objects_alloc(NROF_ITEMS);
	}

	op = free_objects;
	free_objects = free_objects->next;

	if (free_objects)
	{
		free_objects->prev = NULL;
	}

	op->tag = tag;

	if (env)
	{
		object_add(env, op, bflag);
	}

	return op;
}

/**
 * Set object's values.
 * @param op Object. */
void object_set_values(object *op, const char *name, sint32 weight, uint16 face, int flags, uint16 anim, uint16 animspeed, sint32 nrof, uint8 itype, uint8 stype, uint8 qual, uint8 cond, uint8 skill, uint8 level, uint8 dir)
{
	if (!op)
	{
		LOG (-1, "Error in object_set_values(): object pointer is NULL.\n");
		return;
	}

	if (nrof < 0)
	{
		op->nrof = 1;
		strncpy(op->s_name, "Warning: Old name cmd! This is a bug.", sizeof(op->s_name) - 1);
		op->s_name[sizeof(op->s_name) - 1] = '\0';
	}
	/* we have a nrof - object1 command */
	else
	{
		/* Program always expect at least 1 object internal */
		if (nrof == 0)
		{
			nrof = 1;
		}

		op->nrof = nrof;

		if (*name != '\0')
		{
			strncpy(op->s_name, name, sizeof(op->s_name) - 1);
			op->s_name[sizeof(op->s_name) - 1] = '\0';
		}
	}

	op->weight = (float) weight / 1000;

	if (itype != 254)
	{
		op->itype = itype;
	}

	if (stype != 254)
	{
		op->stype = stype;
	}

	if (qual != 254)
	{
		op->item_qua = qual;
	}

	if (cond != 254)
	{
		op->item_con = cond;
	}

	if (skill != 254)
	{
		op->item_skill = skill;
	}

	if (level != 254)
	{
		op->item_level = level;
	}

	op->face = face;
	op->animation_id = anim;
	op->anim_speed = animspeed;
	op->direction = dir;
	op->flags = flags;
}

/**
 * Toggle the locked status of an object.
 * @param op Object. */
void toggle_locked(object *op)
{
	SockList sl;
	char buf[MAX_BUF];

	/* If object is on the ground, don't lock it. */
	if (!op || !op->env || op->env->tag == 0)
	{
		return;
	}

	sl.buf = (unsigned char *) buf;
	strcpy((char *) sl.buf, "lock ");
	sl.len = 5;
	sl.buf[sl.len++] = !(op->flags & F_LOCKED);
	SockList_AddInt(&sl, op->tag);
	send_socklist(sl);
}

/**
 * Update the marked object.
 * @param op The object. */
void object_send_mark(object *op)
{
	SockList sl;
	char buf[MAX_BUF];

	/* If object is on the ground, don't mark it. */
	if (!op || !op->env || op->env->tag == 0)
	{
		return;
	}

	if (cpl.mark_count == op->tag)
	{
		cpl.mark_count = -1;
	}
	else
	{
		cpl.mark_count = op->tag;
	}

	sl.buf = (unsigned char *) buf;
	strcpy((char *) sl.buf, "mark ");
	sl.len = 5;
	SockList_AddInt(&sl, op->tag);
	send_socklist(sl);
}

/**
 * Initializes the various objects of ::cpl structure, freeing them first
 * if necessary. */
void objects_init()
{
	objects_free(cpl.sack);
	objects_free(cpl.below);
	objects_free(cpl.ob);
	objects_free(cpl.shop);

	cpl.ob = object_new();
	cpl.below = object_new();
	cpl.sack = object_new();
	cpl.shop = object_new();

	cpl.below->weight = -111;
	cpl.sack->weight = -111;
	cpl.shop->weight = -111;
}

/**
 * Update an object with new attributes.
 * @param tag The object ID to update.
 * @param loc Location of the object. */
void update_object(int tag, int loc, const char *name, int weight, int face, int flags, int anim, int animspeed, int nrof, uint8 itype, uint8 stype, uint8 qual, uint8 cond, uint8 skill, uint8 level, uint8 direction, int bflag)
{
	object *ip, *env;

	ip = object_find(tag);
	env = object_find(loc);

	/* Need to do some special handling if this is the player that is
	 * being updated. */
	if (cpl.ob->tag == tag)
	{
		cpl.ob->nrof = 1;
		cpl.ob->weight = (float) weight / 1000;
		cpl.ob->face = face;
		cpl.ob->flags = flags;

		cpl.ob->animation_id = anim;
		cpl.ob->anim_speed = animspeed;
		cpl.ob->nrof = nrof;
		cpl.ob->direction = direction;
	}
	else
	{
		if (ip && ip->env != env)
		{
			object_remove(ip);
			ip = NULL;
		}

		object_set_values(ip ? ip : object_create(env, tag, bflag), name, weight, (uint16) face, flags, (uint16) anim, (uint16) animspeed, nrof, itype, stype, qual, cond, skill, level, direction);
	}
}

/**
 * Animate one object.
 * @param ob The object to animate. */
static void animate_object(object *ob)
{
	if (ob->animation_id > 0)
	{
		check_animation_status(ob->animation_id);
	}

	if (ob->animation_id > 0 && ob->anim_speed)
	{
		ob->last_anim++;

		if (ob->last_anim >= ob->anim_speed)
		{
			if (++ob->anim_state >= animations[ob->animation_id].frame)
			{
				ob->anim_state = 0;
			}

			if (ob->direction > animations[ob->animation_id].facings)
			{
				ob->face = animations[ob->animation_id].faces[ob->anim_state];
			}
			else
			{
				ob->face = animations[ob->animation_id].faces[animations[ob->animation_id].frame * ob->direction + ob->anim_state];
			}

			ob->last_anim = 0;
		}
	}
}

/**
 * Animate all possible objects. */
void animate_objects()
{
	object *ob;

	if (cpl.ob)
	{
		/* For now, only the players inventory needs to be animated */
		for (ob = cpl.ob->inv; ob; ob = ob->next)
		{
			animate_object(ob);
		}
	}

	if (cpl.below)
	{
		for (ob = cpl.below->inv; ob; ob = ob->next)
		{
			animate_object(ob);
		}
	}

	if (cpl.container)
	{
		for (ob = cpl.sack->inv; ob; ob = ob->next)
		{
			animate_object(ob);
		}
	}

	if (cpl.shop)
	{
		for (ob = cpl.shop->inv; ob; ob = ob->next)
		{
			animate_object(ob);
		}
	}
}
