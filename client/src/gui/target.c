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
 *  */

#include <include.h>

/**
 * Handle mouse events for target widget.
 * @param x Mouse X position
 * @param y Mouse Y position */
void widget_event_target(widgetdata *widget, int x, int y)
{
	/* Combat modes */
	if (y > widget->y1 + 3 && y < widget->y1 + 38 && x > widget->x1 + 3 && x < widget->x1 + 30)
		check_keys(SDLK_c);

	/* Talk button */
	if (y > widget->y1 + 7 && y < widget->y1 + 25 && x > widget->x1 + 223 && x < widget->x1 + 259)
	{
		if (cpl.target_code)
			send_command("/t_tell hello");
	}
}

/**
 * Show target widget.
 * @param x X position of the target
 * @param y Y position of the target */
void widget_show_target(widgetdata *widget)
{
	char *ptr = NULL;
	SDL_Rect box;
	double temp;
	int hp_tmp;

	sprite_blt(Bitmaps[BITMAP_TARGET_BG], widget->x1, widget->y1, NULL, NULL);

	sprite_blt(Bitmaps[cpl.target_mode ? BITMAP_TARGET_ATTACK : BITMAP_TARGET_NORMAL], widget->x1 + 5, widget->y1 + 4, NULL, NULL);

	sprite_blt(Bitmaps[BITMAP_TARGET_HP_B], widget->x1 + 4, widget->y1 + 24, NULL, NULL);

	hp_tmp = (int) cpl.target_hp;

	/* Redirect target_hp to our hp - server doesn't send it
	 * because we should know our hp exactly */
	if (cpl.target_code == 0)
		hp_tmp = (int)(((float) cpl.stats.hp / (float) cpl.stats.maxhp) * 100.0f);

	if (cpl.target_code == 0)
	{
		if (cpl.target_mode)
			ptr = "target self (hold attack)";
		else
			ptr = "target self";
	}
	else if (cpl.target_code == 1)
	{
		if (cpl.target_mode)
			ptr = "target and attack enemy";
		else
			ptr = "target enemy";
	}
	else if (cpl.target_code == 2)
	{
		if (cpl.target_mode)
			ptr = "target friend (hold attack)";
		else
			ptr = "target friend";
	}

	if (cpl.target_code)
	{
		int mx, my, mb;

		mb = SDL_GetMouseState(&mx, &my) & SDL_BUTTON(SDL_BUTTON_LEFT);

		sprite_blt(Bitmaps[BITMAP_TARGET_TALK], widget->x1 + 223, widget->y1 + 7, NULL, NULL);

		if (mx > widget->x1 + 200 && mx < widget->x1 + 200 + 20 && my > widget->y1 + 3 && my < widget->y1 + 13)
		{
			static int delta = 0;

			if (!(SDL_GetMouseState(&mx, &my) & SDL_BUTTON(SDL_BUTTON_LEFT)))
			{
				delta = 0;
			}
			else if (mb && mb_clicked && !(delta++ & 7))
			{
				char tmp_buf[MAX_BUF];

				snprintf(tmp_buf, sizeof(tmp_buf), "shop load %s", cpl.target_name);
				cs_write_string(tmp_buf, strlen(tmp_buf));
			}

			StringBlt(ScreenSurface, &SystemFont, "Shop", widget->x1 + 200, widget->y1 + 3, COLOR_HGOLD, NULL, NULL);
		}
		else
		{
			StringBlt(ScreenSurface, &SystemFont, "Shop", widget->x1 + 200, widget->y1 + 3, COLOR_WHITE, NULL, NULL);
		}
	}

	if (options.show_target_self || cpl.target_code != 0)
	{
		if (hp_tmp)
		{
			temp = (double) hp_tmp * 0.01;
			box.x = 0;
			box.y = 0;
			box.h = Bitmaps[BITMAP_TARGET_HP]->bitmap->h;
			box.w = (int) (Bitmaps[BITMAP_TARGET_HP]->bitmap->w * temp);

			if (!box.w)
			{
				box.w = 1;
			}

			if (box.w > Bitmaps[BITMAP_TARGET_HP]->bitmap->w)
			{
				box.w = Bitmaps[BITMAP_TARGET_HP]->bitmap->w;
			}

			sprite_blt(Bitmaps[BITMAP_TARGET_HP], widget->x1 + 5, widget->y1 + 25, &box, NULL);
		}

		if (ptr)
		{
			/* Draw the name of the target */
			StringBlt(ScreenSurface, &SystemFont, cpl.target_name, widget->x1 + 35, widget->y1 + 3, cpl.target_color, NULL, NULL);

			/* Either draw HP remaining percent and description... */
			if (hp_tmp > 0)
			{
				char hp_text[MAX_BUF];
				int hp_color;
				int xhpoffset = 0;

				snprintf(hp_text, sizeof(hp_text), "HP: %d%%", hp_tmp);

				if (hp_tmp > 90)
				{
					hp_color = COLOR_GREEN;
				}
				else if (hp_tmp > 75)
				{
					hp_color = COLOR_DGOLD;
				}
				else if (hp_tmp > 50)
				{
					hp_color = COLOR_HGOLD;
				}
				else if (hp_tmp > 25)
				{
					hp_color = COLOR_ORANGE;
				}
				else if (hp_tmp > 10)
				{
					hp_color = COLOR_YELLOW;
				}
				else
				{
					hp_color = COLOR_RED;
				}

				StringBlt(ScreenSurface, &SystemFont, hp_text, widget->x1 + 35, widget->y1 + 14, hp_color, NULL, NULL);
				xhpoffset = 50;

				StringBlt(ScreenSurface, &SystemFont, ptr, widget->x1 + 35 + xhpoffset, widget->y1 + 14, cpl.target_color, NULL, NULL);
			}
			/* Or draw just the description */
			else
			{
				StringBlt(ScreenSurface, &SystemFont, ptr, widget->x1 + 35, widget->y1 + 14, cpl.target_color, NULL, NULL);
			}
		}
	}
}
