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
 * Header file for generic lists implementation. */

#ifndef LIST_H
#define LIST_H

/** One list. */
typedef struct list_struct
{
	/** Next list in a linked list. */
	struct list_struct *next;

	/** Previous list in a linked list. */
	struct list_struct *prev;

	/** ID of the list, one of @ref LIST_xxx. */
	uint32 id;

	/** X position of the list. */
	int x;

	/** Y position of the list. */
	int y;

	/** List's maximum width. */
	int width;

	/** Maximum height of the list's rows. */
	int height;

	/** Number of rows. */
	uint32 rows;

	/** Number of columns in a row. */
	uint32 cols;

	/** Spacing between column names and the actual rows start. */
	int spacing;

	/** An array of the column widths. */
	uint32 *col_widths;

	/** An array of the column spacings. */
	int *col_spacings;

	/** An array of pointers to the column names. */
	char **col_names;

	/** An array of which columns are centered. */
	uint8 *col_centered;

	/**
	 * Array of arrays of pointers to the text. In other words:
	 *
	 * row -> col -> text. */
	char ***text;

	/** Height of one row. */
	uint16 row_height;

	/**
	 * Frame offset (used when drawing the frame around the rows and when
	 * coloring the row entries). */
	sint16 frame_offset;

	/** Height of the header with column names. */
	uint16 header_height;

	/**
	 * Currently highlighted row ID + 1, therefore, 0 means no
	 * highlighted row. */
	uint32 row_highlighted;

	/**
	 * Currently selected row ID + 1, therefore, 0 means no selected
	 * row. */
	uint32 row_selected;

	/**
	 * Row offset used for scrolling.
	 *
	 * - 0 = Row #0 is shown first in the list.
	 * - 10 = Row #10 is shown first in the list. */
	uint32 row_offset;

	/**
	 * Used for figuring out whether a double click occurred (keeps last
	 * ticks value). */
	uint32 click_tick;

	/** Which key to repeat. If -1, no key. */
	sint32 repeat_key;

	/**
	 * Used for figuring out how many key repeats to simulate (keeps the
	 * ticks value). */
	uint32 repeat_key_ticks;

	/** If 1, this list has the active focus. */
	uint8 focus;

	/**
	 * Function that will draw frame (and/or other effects) right before
	 * the column names and the actual rows.
	 * @param list List. */
	void (*draw_frame_func)(struct list_struct *list);

	/**
	 * Function that will color the specified row.
	 * @param list List.
	 * @param row Row number, 0-[max visible rows].
     * @param box Contains base x/y/width/height information to use. */
	void (*row_color_func)(struct list_struct *list, int row, SDL_Rect box);

	/**
 	 * Function to highlight a row (due to mouse being over it).
	 * @param list List.
	 * @param box Contains base x/y/width/height information to use. */
	void (*row_highlight_func)(struct list_struct *list, SDL_Rect box);

	/**
	 * Function to color a selected row.
	 * @param list List.
	 * @param box Contains base x/y/width/height information to use. */
	void (*row_selected_func)(struct list_struct *list, SDL_Rect box);

	/**
	 * Function to handle ESC key being pressed while the list had focus.
	 * @param list List. */
	void (*handle_esc_func)(struct list_struct *list);

	/**
	 * Function to handle enter key being pressed on a selected row, or
	 * a row being double clicked.
	 * @param list List. */
	void (*handle_enter_func)(struct list_struct *list);
} list_struct;

/**
 * @defgroup LIST_xxx List IDs
 * IDs of lists in use. Each list used should have a unique ID.
 *@{*/
/** The list showing servers. */
#define LIST_SERVERS 1
/*@}*/

/** Figure out Y position where rows should actually start. */
#define LIST_ROWS_START(list) ((list)->y + (list)->header_height + (list)->spacing)
/** Figure out maximum visible rows. */
#define LIST_ROWS_MAX(list) ((uint32) ((list)->height + (list)->spacing) / (list)->row_height)
/**
 * Adjust row ID by the row offset, thus transforming row ID to
 * 0-[max visible rows]. */
#define LIST_ROW_OFFSET(row, list) ((row) - (list)->row_offset)
/** Figure out full height of the list, including its header. */
#define LIST_HEIGHT_FULL(list) ((list)->height + (list)->spacing + (list)->header_height)

/** Double click delay in ticks. */
#define DOUBLE_CLICK_DELAY 300
/** Key repeat delay in ticks. */
#define KEY_REPEAT_DELAY 25
/** Initial delay value. */
#define KEY_REPEAT_DELAY_INIT 175

#endif
