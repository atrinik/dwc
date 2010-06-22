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
 * Handles object moving and pushing. */

#include <global.h>

/**
 * Try to move object in specified direction.
 * @param op What to move.
 * @param dir Direction to move the object to.
 * @param originator Typically the same as op, but can be dfferent if
 * originator is causing op to move (originator is pushing op).
 * @return 0 if the object is not able to move to the desired space, 1
 * otherwise (in which case we also move the object accordingly). -1 if
 * the object was destroyed in the move process (most likely when hit a
 * deadly trap or something). */
int move_ob(object *op, int dir, object *originator)
{
	object *tmp;
	mapstruct *m;
	int xt, yt, flags;

	if (op == NULL)
	{
		LOG(llevBug, "BUG: move_ob(): Trying to move NULL.\n");
		return 0;
	}

	if (QUERY_FLAG(op, FLAG_REMOVED))
	{
		LOG(llevBug, "BUG: move_ob: monster has been removed - will not process further\n");
		return 0;
	}

	/* this function should now only be used on the head - it won't call itself
	 * recursively, and functions calling us should pass the right part. */
	if (op->head)
	{
		LOG(llevDebug, "DEBUG: move_ob() called with non head object: %s %s (%d,%d)\n", query_name(op->head, NULL), op->map->path ? op->map->path : "<no map>", op->x, op->y);
		op = op->head;
	}

	/* animation stuff */
	if (op->head)
	{
		op->head->anim_moving_dir = dir;
	}
	else
	{
		op->anim_moving_dir = dir;
	}

	op->direction = dir;

	xt = op->x + freearr_x[dir];
	yt = op->y + freearr_y[dir];

	/* we have here a get_map_from_coord - we can skip all */
	if (!(m = get_map_from_coord(op->map, &xt, &yt)))
	{
		return 0;
	}

	/* Don't allow non-players to move onto player-only tiles. */
	if (op->type != PLAYER && GET_MAP_FLAGS(m, xt, yt) & P_PLAYER_ONLY)
	{
		return 0;
	}

	/* multi arch objects... */
	if (op->more)
	{
		/* Look in single tile move to see how we handle doors.
		 * This needs to be done before we allow multi tile mobs to do
		 * more fancy things. */
		if (blocked_link(op, freearr_x[dir], freearr_y[dir]))
		{
			return 0;
		}

		remove_ob(op);

		if (check_walk_off(op, originator, MOVE_APPLY_MOVE) & (CHECK_WALK_DESTROYED | CHECK_WALK_MOVED))
		{
			return 1;
		}

		for (tmp = op; tmp != NULL; tmp = tmp->more)
		{
			tmp->x += freearr_x[dir], tmp->y += freearr_y[dir];
		}

		insert_ob_in_map(op, op->map, op, 0);

		return 1;
	}

	/* Single arch */
	if (!QUERY_FLAG(op, FLAG_WIZPASS))
	{
		/* Is the spot blocked from something? */
		if ((flags = blocked(op, m, xt, yt, op->terrain_flag)))
		{
			/* A closed door which we can open? */
			if ((flags & P_DOOR_CLOSED) && (op->behavior & BEHAVIOR_OPEN_DOORS) && open_door(op, m, xt, yt, 1))
			{
				if (op->type == PLAYER)
				{
					return 1;
				}
			}
			else
			{
				return 0;
			}
		}
	}

	remove_ob(op);

	if (check_walk_off(op, originator, MOVE_APPLY_MOVE) & (CHECK_WALK_DESTROYED | CHECK_WALK_MOVED))
	{
		return 1;
	}

	op->x += freearr_x[dir];
	op->y += freearr_y[dir];

	insert_ob_in_map(op, op->map, originator, 0);

	return 1;
}

/**
 * Move an object (even linked objects) to another spot on the same map.
 *
 * Does nothing if there is no free spot.
 * @param op What to move.
 * @param x New X coordinate.
 * @param y New Y coordinate.
 * @param randomly If 1, use find_free_spot() to find the destination,
 * otherwise use find_first_free_spot().
 * @param originator What is causing op to move.
 * @param trap Trap.
 * @return 1 if the object was destroyed, 0 otherwise. */
int transfer_ob(object *op, int x, int y, int randomly, object *originator, object *trap)
{
	int i, ret;
	object *tmp;

	/* this is not 100% tested for mobs - enter_exit will still fail to return for mobs */
	/* but some testing should make it for mobs too */
	if (trap != NULL && EXIT_PATH(trap))
	{
		if (op->type == PLAYER && trap->msg && strncmp(EXIT_PATH(trap), "/!", 2) && strncmp(EXIT_PATH(trap), "/random/", 8))
		{
			new_draw_info(NDI_NAVY, op, trap->msg);
		}

		enter_exit(op, trap);
		return 1;
	}
	else if (randomly)
	{
		i = find_free_spot(op->arch, NULL, op->map, x, y, 0, SIZEOFFREE);
	}
	else
	{
		i = find_first_free_spot(op->arch, op, op->map, x, y);
	}

	/* No free spot */
	if (i == -1)
	{
		return 0;
	}

	if (op->head != NULL)
	{
		op = op->head;
	}

	remove_ob(op);

	if (check_walk_off(op, NULL, MOVE_APPLY_DEFAULT) != CHECK_WALK_OK)
	{
		return 1;
	}

	for (tmp = op; tmp != NULL; tmp = tmp->more)
	{
		tmp->x = x + freearr_x[i] + (tmp->arch == NULL ? 0 : tmp->arch->clone.x);
		tmp->y = y + freearr_y[i] + (tmp->arch == NULL ? 0 : tmp->arch->clone.y);
	}

	ret = (insert_ob_in_map(op, op->map, originator, 0) == NULL);

	return ret;
}

/**
 * Teleport an item around a nearby random teleporter of specified type.
 * @param teleporter The teleporter.
 * @param tele_type Teleporter type.
 * @param user Object using the teleporter.
 * @return  1 if the object was destroyed, 0 otherwise. */
int teleport(object *teleporter, uint8 tele_type, object *user)
{
	/* Better use c/malloc here in the future */
	object *altern[120];
	int i, j, k, nrofalt = 0, xt, yt;
	object *other_teleporter, *tmp;
	mapstruct *m;

	if (user == NULL)
	{
		return 0;
	}

	if (user->head != NULL)
	{
		user = user->head;
	}

	/* Find all other teleporters within range. This range should really
	 * be setable by some object attribute instead of using hard coded
	 * values. */
	for (i = -5; i < 6; i++)
	{
		for (j = -5; j < 6; j++)
		{
			if (i == 0 && j == 0)
			{
				continue;
			}

			xt = teleporter->x + i;
			yt = teleporter->y + j;

			if (!(m = get_map_from_coord(teleporter->map, &xt, &yt)))
			{
				continue;
			}

			other_teleporter = get_map_ob(m, xt, yt);

			while (other_teleporter)
			{
				if (other_teleporter->type == tele_type)
				{
					break;
				}

				other_teleporter = other_teleporter->above;
			}

			if (other_teleporter)
			{
				altern[nrofalt++] = other_teleporter;
			}
		}
	}

	if (!nrofalt)
	{
		LOG(llevBug, "BUG: teleport(): No alternative teleporters around!\n");
		return 0;
	}

	other_teleporter = altern[RANDOM() % nrofalt];
	k = find_free_spot(user->arch, user, other_teleporter->map, other_teleporter->x, other_teleporter->y, 1, 9);

	if (k == -1)
	{
		return 0;
	}

	remove_ob(user);

	if (check_walk_off(user, NULL, MOVE_APPLY_VANISHED) != CHECK_WALK_OK)
	{
		return 1;
	}

	/* Update location for the object */
	for (tmp = user; tmp != NULL; tmp = tmp->more)
	{
		tmp->x = other_teleporter->x + freearr_x[k] + (tmp->arch == NULL ? 0 : tmp->arch->clone.x);
		tmp->y = other_teleporter->y + freearr_y[k] + (tmp->arch == NULL ? 0 : tmp->arch->clone.y);
	}

	tmp = insert_ob_in_map(user, other_teleporter->map, NULL, 0);

	return (tmp == NULL);
}

/**
 * An object is being pushed.
 * @param op What is being pushed.
 * @param dir Pushing direction.
 * @param pusher What is pushing op.
 * @return 0 if the object couldn't be pushed, 1 otherwise. */
int push_ob(object *op, int dir, object *pusher)
{
	object *tmp, *floor;
	mapstruct *m;
	int x, y, flags;

	/* Don't allow pushing multi-arch objects. */
	if (op->head)
	{
		return 0;
	}

	/* Check whether we are strong enough to push this object. */
	if (op->weight && (op->weight / 50000 - 1 > 0 ? rndm(0, op->weight / 50000 - 1) : 0) > pusher->stats.Str)
	{
		return 0;
	}

	x = op->x + freearr_x[dir];
	y = op->y + freearr_y[dir];

	if (!(m = get_map_from_coord(op->map, &x, &y)))
	{
		return 0;
	}

	floor = GET_MAP_OB_LAYER(m, x, y, 0);

	/* Floor has no-push flag set? */
	if (floor && QUERY_FLAG(floor, FLAG_XRAYS))
	{
		return 0;
	}

	flags = blocked(op, m, x, y, op->terrain_flag);

	if (flags)
	{
		if (flags & (P_NO_PASS | P_CHECK_INV) || ((flags & P_DOOR_CLOSED) && !open_door(op, m, x, y, 1)))
		{
			return 0;
		}
		else
		{
			return 0;
		}
	}

	/* Try to find something that would block the push. */
	for (tmp = GET_MAP_OB(m, x, y); tmp; tmp = tmp->above)
	{
		if (tmp->head || IS_LIVE(tmp) || tmp->type == TELEPORTER || tmp->type == SHOP_MAT)
		{
			return 0;
		}
	}

	remove_ob(op);

	if (check_walk_off(op, NULL, MOVE_APPLY_VANISHED) != CHECK_WALK_OK)
	{
		return 0;
	}

	op->x = op->x + freearr_x[dir];
	op->y = op->y + freearr_y[dir];
	insert_ob_in_map(op, op->map, pusher, 0);
	return 1;
}

int missile_reflection_adjust(object *op, int flag)
{
	/* no more direction/reflection! */
	if (!op->stats.maxgrace)
	{
		return 0;
	}

	op->stats.maxgrace--;

	/* restore the "how long we can fly" counter */
	if (!flag)
	{
		op->last_sp = op->stats.grace;
	}

	/* go on with reflection/direction */
	return 1;
}
