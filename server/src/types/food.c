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
 * Handles code used for @ref FOOD "food", @ref DRINK "drinks" and
 * @ref FLESH "flesh". */

#include <global.h>
#include <math.h>

/** Maximum allowed food value. */
#define FOOD_MAX 999

/**
 * Apply a food/drink/flesh object.
 * @param op The object applying this.
 * @param tmp The object to apply. */
void apply_food(object *op, object *tmp)
{
	if (op->type != PLAYER)
	{
		op->stats.hp = op->stats.maxhp;
	}
	else
	{
		char buf[MAX_BUF];

		if (op->stats.food + tmp->stats.food > FOOD_MAX)
		{
			if ((op->stats.food + tmp->stats.food) - FOOD_MAX > tmp->stats.food / 5)
			{
				new_draw_info(NDI_UNIQUE, op, "You are too full to eat this right now!");
				return;
			}

			if (tmp->type == FOOD || tmp->type == FLESH)
			{
				new_draw_info(NDI_UNIQUE, op, "You feel full, but what a waste of food!");
			}
			else
			{
				new_draw_info(NDI_UNIQUE, op, "Most of the drink goes down your face not your throat!");
			}
		}

		if (!QUERY_FLAG(tmp, FLAG_CURSED) && !QUERY_FLAG(tmp, FLAG_DAMNED))
		{
			int capacity_remaining = FOOD_MAX - op->stats.food;

			if (tmp->type == DRINK)
			{
				snprintf(buf, sizeof(buf), "Ahhh...that %s tasted good.", tmp->name);
			}
			else
			{
				snprintf(buf, sizeof(buf), "The %s tasted %s", tmp->name, tmp->type == FLESH ? "terrible!" : "good.");
			}

			op->stats.food += tmp->stats.food;

			if (capacity_remaining < tmp->stats.food)
			{
				op->stats.hp += capacity_remaining / 50;
			}
			else
			{
				op->stats.hp += tmp->stats.food / 50;
			}

			if (op->stats.hp > op->stats.maxhp)
			{
				op->stats.hp = op->stats.maxhp;
			}
		}
		/* cursed/damned = food is decreased instead of increased */
		else
		{
			int ft = tmp->stats.food;

			snprintf(buf, sizeof(buf), "The %s tasted terrible!", tmp->name);

			if (ft > 0)
			{
				ft = -ft;
			}

			op->stats.food += ft;
		}

		new_draw_info(NDI_UNIQUE, op, buf);

		/* adjust food to borders when needed */
		if (op->stats.food > FOOD_MAX)
		{
			op->stats.food = FOOD_MAX;
		}
		else if (op->stats.food < 0)
		{
			op->stats.food = 0;
		}

		/* special food hack -b.t. */
		if (tmp->title || QUERY_FLAG(tmp, FLAG_CURSED)|| QUERY_FLAG(tmp, FLAG_DAMNED))
		{
			eat_special_food(op, tmp);
		}
	}

	decrease_ob(tmp);
}

/**
 * Create a food force to include buff/debuff effects of stats and
 * protections to the player.
 * @param who The player object.
 * @param food The food.
 * @param force The force object. */
void create_food_force(object* who, object *food, object *force)
{
	int i;

	force->stats.Str = food->stats.Str;
	force->stats.Pow = food->stats.Pow;
	force->stats.Dex = food->stats.Dex;
	force->stats.Con = food->stats.Con;
	force->stats.Int = food->stats.Int;
	force->stats.Wis = food->stats.Wis;
	force->stats.Cha = food->stats.Cha;

	for (i = 0; i < NROFATTACKS; i++)
	{
		force->protection[i] = food->protection[i];
	}

	/* if damned, set all negative if not and double or triple them */
	if (QUERY_FLAG(food, FLAG_CURSED) || QUERY_FLAG(food, FLAG_DAMNED))
	{
		int stat_multiplier = QUERY_FLAG(food, FLAG_CURSED) ? 2 : 3;

		if (force->stats.Str > 0)
		{
			force->stats.Str =- force->stats.Str;
		}

		force->stats.Str *= stat_multiplier;

		if (force->stats.Dex > 0)
		{
			force->stats.Dex =- force->stats.Dex;
		}

		force->stats.Dex *= stat_multiplier;

		if (force->stats.Con > 0)
		{
			force->stats.Con =- force->stats.Con;
		}

		force->stats.Con *= stat_multiplier;

		if (force->stats.Int > 0)
		{
			force->stats.Int =- force->stats.Int;
		}

		force->stats.Int *= stat_multiplier;

		if (force->stats.Wis > 0)
		{
			force->stats.Wis =- force->stats.Wis;
		}

		force->stats.Wis *= stat_multiplier;

		if (force->stats.Pow > 0)
		{
			force->stats.Pow =- force->stats.Pow;
		}

		force->stats.Pow *= stat_multiplier;

		if (force->stats.Cha > 0)
		{
			force->stats.Cha =- force->stats.Cha;
		}

		force->stats.Cha *= stat_multiplier;

		for (i = 0; i < NROFATTACKS; i++)
		{
			if (force->protection[i] > 0)
			{
				force->protection[i] =- force->protection[i];
			}

			force->protection[i] *= stat_multiplier;
		}
	}

	if (food->speed_left)
	{
		force->speed = food->speed_left;
	}

	SET_FLAG(force, FLAG_APPLIED);

	force = insert_ob_in_ob(force, who);
	/* Mostly to display any messages */
	change_abil(who, force);
	/* This takes care of some stuff that change_abil() */
	fix_player(who);
}

/**
 * The food gives specials, like +/- hp or sp, protections and stats.
 *
 * Food can be good or bad (good effect or bad effect), and cursed or
 * not. If food is "good" (for example, Str +1 and Dex +1), then it puts
 * those effects as force in the player for some time.
 *
 * If good food is cursed, all positive values are turned to negative
 * values.
 *
 * If bad food (Str -1, Dex -1) is uncursed, it gives just those values.
 *
 * If bad food is cursed, all negative values are doubled.
 *
 * Food effects can stack. For really powerful food, a high food value
 * should be set, so the player can't eat a lot of such food, as his
 * stomach will be full.
 * @param who Object eating the food.
 * @param food The food object. */
void eat_special_food(object *who, object *food)
{
	/* if there is any stat or protection value - create force for the object! */
	if (food->stats.Pow || food->stats.Str || food->stats.Dex || food->stats.Con || food->stats.Int || food->stats.Wis || food->stats.Cha)
	{
		create_food_force(who, food, get_archetype("force"));
	}
	else
	{
		int i;

		for (i = 0; i < NROFATTACKS; i++)
		{
			if (food->protection[i] > 0)
			{
				create_food_force(who, food, get_archetype("force"));
				break;
			}
		}
	}

	/* Check for hp, sp and grace change */
	if (food->stats.hp)
	{
		if (QUERY_FLAG(food, FLAG_CURSED) || QUERY_FLAG(food, FLAG_DAMNED))
		{
			int tmp = food->stats.hp;

			if (tmp > 0)
			{
				tmp = -tmp;
			}

			strcpy(CONTR(who)->killer, food->name);

			if (QUERY_FLAG(food, FLAG_CURSED))
			{
				who->stats.hp += tmp * 2;
			}
			else
			{
				who->stats.hp += tmp * 3;
			}

			new_draw_info(NDI_UNIQUE, who, "Eck!... that was rotten food!");
		}
		else
		{
			new_draw_info(NDI_UNIQUE, who, "You begin to feel better.");
			who->stats.hp += food->stats.hp;

			if (who->stats.hp > who->stats.maxhp)
			{
				who->stats.hp = who->stats.maxhp;
			}
		}
	}

	if (food->stats.sp)
	{
		if (QUERY_FLAG(food, FLAG_CURSED) || QUERY_FLAG(food, FLAG_DAMNED))
		{
			int tmp = food->stats.sp;

			if (tmp > 0)
			{
				tmp = -tmp;
			}

			new_draw_info(NDI_UNIQUE, who, "Your mana is drained!");

			if (QUERY_FLAG(food, FLAG_CURSED))
			{
				who->stats.sp += tmp * 2;
			}
			else
			{
				who->stats.sp += tmp * 3;
			}

			if (who->stats.sp < 0)
			{
				who->stats.sp = 0;
			}
		}
		else
		{
			new_draw_info(NDI_UNIQUE, who, "You feel a rush of magical energy!");
			who->stats.sp += food->stats.sp;

			if (who->stats.sp > who->stats.maxsp)
			{
				who->stats.sp = who->stats.maxsp;
			}
		}
	}

	if (food->stats.grace && determine_god(who) != shstr_cons.none)
	{
		if (QUERY_FLAG(food, FLAG_CURSED) || QUERY_FLAG(food, FLAG_DAMNED))
		{
			int tmp = food->stats.grace;

			if (tmp > 0)
			{
				tmp = -tmp;
			}

			new_draw_info(NDI_UNIQUE, who, "Your grace is drained!");

			if (QUERY_FLAG(food, FLAG_CURSED))
			{
				who->stats.grace += tmp * 2;
			}
			else
			{
				who->stats.grace += tmp * 3;
			}

			if (who->stats.grace < 0)
			{
				who->stats.grace = 0;
			}
		}
		else
		{
			new_draw_info(NDI_UNIQUE, who, "You are returned to a state of grace.");
			who->stats.grace += food->stats.grace;

			if (who->stats.grace > who->stats.maxgrace)
			{
				who->stats.grace = who->stats.maxgrace;
			}
		}
	}
}
