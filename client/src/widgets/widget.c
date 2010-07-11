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
 * This file controls all the widget related functions, movement of the
 * widgets, initialization, etc.
 *
 * To add a new widget:
 * -# Add an entry (same index in both cases) to ::con_widget and ::WidgetID.
 * -# If applicable, add extended attributes in its own struct, and add handler code for its initialisation in create_widget_object().
 * -# If applicable, add handler code for widget movement in widget_event_mousedn().
 * -# If applicable, add handler code to get_widget_owner().
 * -# Add handler function to process_widget(). */

#include <include.h>

/* File-scope routines */

/* Init and kill */
void            init_widgets();
void            kill_widget_tree(widgetdata *widget);
/* Widget management */
void            remove_widget_object_intern(widgetdata *widget);
void            remove_widget_inv(widgetdata *widget);
widgetdata     *create_widget(int widget_id);
void            remove_widget(widgetdata *widget);
/* Widget component handling */
widgetdata     *add_label(char *text, _Font *font, int color);
widgetdata     *create_menu(int x, int y, widgetdata *owner);
void            add_menuitem(widgetdata *menu, char *text, void (*menu_func_ptr)(widgetdata *, int, int), int has_submenu);
void            add_separator(widgetdata *widget);
/* Events */
widgetdata     *get_widget_owner_rec(int x, int y, widgetdata *widget, widgetdata *end);
int             widget_event_start_move(widgetdata *widget, int x, int y);
int             widget_event_respond(int x, int y);
int             widget_event_override();
void            move_widget_rec(widgetdata *widget, int x, int y);
void            resize_widget_rec(widgetdata *widget, int x, int width, int y, int height);
/* File Handling */
static int      load_interface_file(char *filename);
void            save_interface_file_rec(widgetdata *widget, FILE *stream);
/* Misc */
void            process_widgets_rec(widgetdata *widget);
static void     process_widget(widgetdata *widget);
widgetdata     *get_outermost_container(widgetdata *widget);
#ifdef DEBUG_WIDGET
/* debug */
int             debug_count_nodes_rec(widgetdata *widget, int i, int j, int output);
void            debug_count_nodes(int output);
#endif

/** Current (default) data list of all widgets. */
static widgetdata def_widget[TOTAL_SUBWIDGETS];

/** Default data list of all widgets. */
/* {name, x1, y1, wd, ht, moveable?, show?, redraw?, unique?, no_kill?, visible?, delete_inv?, save?, save_width_height?
 * * the next members are used internally *
 * next(NULL), prev(NULL), inv(NULL), inv_rev(NULL), env(NULL), type_next(NULL), type_prev(NULL),
 * subwidget(NULL), widgetSF(NULL), WidgetTypeID(0), WidgetSubtypeID(0), WidgetObjID(0)} */
static const widgetdata con_widget[TOTAL_SUBWIDGETS] =
{
    /* base widgets */
	{"STATS",           227,   0, 172, 102, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"RESIST",          497,   0, 198,  79, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MAIN_LVL",        399,  39,  98,  62, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"SKILL_EXP",       497,  79, 198,  22, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"REGEN",           399,   0,  98,  39, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"SKILL_LVL",       695,   0,  52, 101, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MENUBUTTONS",     747,   0,  47, 101, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"QUICKSLOTS",      509, 107, 282,  34, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"CHATWIN",           0, 426, 261, 233, 1, 1, 1, 0, 1, 1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MSGWIN",          539, 426, 261, 233, 1, 1, 1, 0, 1, 1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MIXWIN",          539, 420, 261, 233, 1, 0, 1, 0, 1, 1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"PLAYERDOLL",        0,  41, 221, 224, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"BELOWINV",        262, 545, 274,  55, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"PLAYERINFO",        0,   0, 219,  41, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"RANGEBOX",          6,  51,  94,  60, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"TARGET",          267, 514, 264,  31, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MAININV",         539, 147, 239,  32, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MAPNAME",         228, 106,  36,  16, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"CONSOLE",         271, 489, 256,  25, 1, 0, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"NUMBER",          270, 471, 256,  43, 1, 0, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"SHOP",            300, 147, 200, 320, 1, 0, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"FPS",             123,  47,  70,  12, 1, 1, 1, 1, 1, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"CONTAINER",         0,   0, 128, 128, 1, 1, 1, 0, 1, 1, 0, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"LABEL",             0,   0,   5,   5, 1, 1, 1, 0, 0, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"BITMAP",            0,   0,   5,   5, 1, 1, 1, 0, 0, 1, 1, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	/* subwidgets */
	{"CONTAINER_STRIP",   0,   0, 128, 128, 1, 1, 1, 0, 1, 1, 0, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MENU",              0,   0,   5,   5, 0, 1, 1, 0, 0, 1, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
	{"MENUITEM",          0,   0,   5,   5, 0, 1, 1, 0, 0, 0, 1, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0},
};


/* Default overall priority tree. Will change during runtime.
 * Widget at the top (head) of the tree has highest priority.
 * Events go to the top (head) of the tree first.
 * Displaying goes to the right (foot) of the tree first. */
/** The root node at the top of the tree. */
static widgetdata *widget_list_head;
/** The last sibling on the top level of the tree (the far right). */
static widgetdata *widget_list_foot;

/**
 * The head and foot node for each widget type.
 * The nodes in this linked list do not change until a node is deleted. */
/* TODO: change cur_widget to type_list_head */
widgetdata *cur_widget[TOTAL_SUBWIDGETS];
static widgetdata *type_list_foot[TOTAL_SUBWIDGETS];

/**
 * Determines which widget has mouse focus
 * This value is determined in the mouse routines for the widgets */
widgetevent widget_mouse_event =
{
	NULL, 0, 0
};

/** This is used when moving a widget with the mouse. */
static widgetmove widget_event_move =
{
	0, NULL, 0, 0
};

/**
 * A way to steal the mouse, and to prevent widgets from using mouse events
 * Example: Prevents widgets from using mouse events during dragging procedure */
static int IsMouseExclusive = 0;

/**
 * The alpha setting in the last frame. If it differs from the current frame,
 * certain widgets need to be redrawn. */
int old_alpha_option = 0;

/**
 * Load the defaults and initialize the priority list.
 * Create the interface file, if it doesn't exist */
static void init_widgets_fromDefault()
{
	int lp;

	/* Exit, if there are no widget IDs */
	if (!TOTAL_SUBWIDGETS)
	{
		return;
	}

	/* Store the constant default widget lookup in the current lookup(s) */
	for (lp = 0; lp < TOTAL_SUBWIDGETS; ++lp)
	{
		def_widget[lp] = con_widget[lp];
	}

	/* Initiate the linked lists for the widgets. */
	init_widgets();
}

/**
 * Try to load the main interface file and initialize the priority list
 * On failure, initialize the widgets with init_widgets_fromDefault() */
void init_widgets_fromCurrent()
{
	/* Exit, if there are no widgets */
	if (!TOTAL_SUBWIDGETS)
	{
		return;
	}

	/* If can't open/load the interface file load defaults and create file */
	if (!load_interface_file(INTERFACE_FILE))
	{
		/* Inform user */
		LOG(llevInfo, "Can't open/load the interface file - %s. Resetting\n", INTERFACE_FILE);

		/* Load the defaults - this also allocates priority list */
		init_widgets_fromDefault();

		/* Create the interface file */
		save_interface_file();
	}
}

/** Wrapper function to handle the creation of a widget. */
widgetdata *create_widget_object(int widget_subtype_id)
{
	widgetdata *widget;
	_textwin *textwin;
	_widget_container *container;
    _widget_container_strip *container_strip;
	_menu *menu;
	_menuitem *menuitem;
    _widget_label *label;
	_widget_bitmap *bitmap;
    int widget_type_id = widget_subtype_id, i;

	/* map the widget subtype to widget type */
	if (widget_subtype_id >= TOTAL_WIDGETS)
	{
		switch (widget_subtype_id)
		{
			case CONTAINER_STRIP_ID:
			case MENU_ID:
			case MENUITEM_ID:
				widget_type_id = CONTAINER_ID;
				break;
			/* no subtype was found, so get out of here */
			default:
				return NULL;
		}
	}

	/* sanity check */
	if (widget_subtype_id < 0 || widget_subtype_id >= TOTAL_SUBWIDGETS)
	{
		return NULL;
	}

	/* don't create more than one widget if it is a unique widget */
	if (con_widget[widget_subtype_id].unique && cur_widget[widget_subtype_id])
	{
		return NULL;
	}

	/* allocate the widget node, this should always be the first function called in here */
	widget = create_widget(widget_subtype_id);
    widget->WidgetTypeID = widget_type_id;
	/* allocate the custom attributes for the widget if applicable */
	switch (widget->WidgetTypeID)
	{
		case CHATWIN_ID:
		case MSGWIN_ID:
		case MIXWIN_ID:
			textwin = malloc(sizeof (_textwin));
			if (!textwin)
			{
				exit(0);
			}
			/* begin initialising the members that need it, I basically here just used copypasta from the old textwin_init() function */
			textwin->size = 22;
			textwin->scroll = 0;
			textwin->bot_drawLine = 0;
			textwin->act_bufsize = 0;
			for (i = 0; i < TEXT_WIN_MAX; ++i)
			{
				textwin->text[i].channel = 0;
				textwin->text[i].flags = 0;
				textwin->text[i].color = 0;
				/* you won't believe how much quality time I've spent preventing this variable from playing chase with my memory */
				textwin->text[i].key_clipped = 0;
			}
			/* that's right, a void * cast to _textwin *.
			 * usually it's not a nice thing to do, but in this case it's an excellent way of extending a struct */
			widget->subwidget = (_textwin *) textwin;
			/* this should take care of the initialisation problem */
			widget->ht = textwin->size * 10 + 13;
			break;
		case MAPNAME_ID:
			/* set the bounding box to another one that exists, otherwise it can be wrong initially */
			if (cur_widget[MAPNAME_ID])
			{
				widget->wd = cur_widget[MAPNAME_ID]->wd;
				widget->ht = cur_widget[MAPNAME_ID]->ht;
			}
			break;
		case CONTAINER_ID:
			container = malloc(sizeof (_widget_container));
			if (!container)
			{
				exit(0);
			}
			/* begin initialising the members */
			container->widget_type = -1;
			container->outer_padding_top = 10;
			container->outer_padding_bottom = 10;
			container->outer_padding_left = 10;
			container->outer_padding_right = 10;
			container->x_left_buf1 = 0;
			container->x_left_buf2 = 0;
			container->x_right_buf1 = 0;
			container->x_right_buf2 = 0;
			container->y_top_buf1 = 0;
			container->y_top_buf2 = 0;
			container->y_bottom_buf1 = 0;
			container->y_bottom_buf2 = 0;
			/* have the subwidget point to it */
			widget->subwidget = (_widget_container *) container;
			/* allocate the custom attributes for the container if applicable */
			switch (widget->WidgetSubtypeID)
			{
				case CONTAINER_STRIP_ID:
				case MENU_ID:
				case MENUITEM_ID:
					container_strip = malloc(sizeof (_widget_container_strip));
					if (!container_strip)
					{
						exit(0);
					}
					/* Begin initialsing the members. */
					container_strip->inner_padding = 10;
					container_strip->horizontal = 0;
					container_strip->size = 0;
					/* Have the subcontainer point to it. */
					container->subcontainer = (_widget_container_strip *) container_strip;
					/* Allocate the custom attributes for the strip container if applicable. */
					switch (widget->WidgetSubtypeID)
					{
						case MENU_ID:
							menu = malloc(sizeof (_menu));
							if (!menu)
							{
								exit(0);
							}
							/* Begin initialising the members. */
							menu->submenu = NULL;
							menu->owner = NULL;
							/* Have the sub strip container point to it. */
							container_strip->subcontainer_strip = (_menu *) menu;
							break;
						case MENUITEM_ID:
							menuitem = malloc(sizeof (_menuitem));
							if (!menuitem)
							{
								exit(0);
							}
							/* Begin initialsing the members. */
							menuitem->menu_func_ptr = NULL;
							menuitem->menu_type = MENU_NORMAL;
							/* Have the sub strip container point to it. */
							container_strip->subcontainer_strip = (_menuitem *) menuitem;
							break;
					}
					break;
			}
			break;
		case LABEL_ID:
			label = malloc(sizeof (_widget_label));
			if (!label)
			{
				exit(0);
			}
			/* begin initialising the members */
			label->text = "";
			label->font = &SystemFont;
			label->color = COLOR_DEFAULT;
			/* have the subwidget point to it */
			widget->subwidget = (_widget_label *) label;
			break;
		case BITMAP_ID:
			bitmap = malloc(sizeof (_widget_bitmap));
			if (!bitmap)
			{
				exit(0);
			}
			/* begin initialising the members */
			bitmap->bitmap_id = 0;
			/* have the subwidget point to it */
			widget->subwidget = (_widget_bitmap *) bitmap;
			break;
	}

	return widget;
}

/** Wrapper function to handle the obliteration of a widget. */
void remove_widget_object(widgetdata *widget)
{
    /* don't delete the last widget if there needs to be at least one of this widget type */
	if (widget->no_kill && cur_widget[widget->WidgetSubtypeID] == type_list_foot[widget->WidgetSubtypeID])
	{
        return;
	}

    remove_widget_object_intern(widget);
}

/**
 * Wrapper function to handle the annihilation of a widget, including possibly killing the linked list altogether.
 * Please do not use, this should only be explicitly called by kill_widget_tree() and remove_widget_object().
 * Use remove_widget_object() for everything else. */
void remove_widget_object_intern(widgetdata *widget)
{
	widgetdata *tmp;
	_widget_container *container;
	_widget_container_strip *container_strip;
	int widget_subtype_id = widget->WidgetSubtypeID;

	/* If this flag is enabled, we need to delete all contents of the widget too, which calls for some recursion. */
	if (widget->delete_inv)
	{
		remove_widget_inv(widget);
	}

	/* If this widget happens to be the owner of an event, keeping them pointed to it is a bad idea. */
	if (widget_mouse_event.owner == widget)
	{
		widget_mouse_event.owner = NULL;
	}
	if (widget_event_move.owner == widget)
	{
		widget_event_move.owner = NULL;
	}
	/* If any menu is open and this widget is the owner, bad things could happen here too. Clear the pointers. */
	if (cur_widget[MENU_ID] && (MENU(cur_widget[MENU_ID]))->owner == widget)
	{
		for (tmp = cur_widget[MENU_ID]; tmp; tmp = tmp->type_next)
		{
			(MENU(cur_widget[MENU_ID]))->owner = NULL;
		}
	}

	/* Get the environment if it exists, this is used to make containers auto-resize when the widget is deleted. */
	tmp = widget->env;

	/* remove the custom attribute nodes if they exist */
	if (widget->subwidget)
	{
		switch (widget_subtype_id)
		{
			case CONTAINER_STRIP_ID:
			case MENU_ID:
			case MENUITEM_ID:
				if (widget_subtype_id == MENUITEM_ID)
				{
					container_strip = CONTAINER_STRIP(widget);
					if (container_strip->subcontainer_strip)
					{
						free(container_strip->subcontainer_strip);
						container_strip->subcontainer_strip = NULL;
					}
				}
				container = CONTAINER(widget);
				if (container->subcontainer)
				{
					free(container->subcontainer);
					container->subcontainer = NULL;
				}
				break;
		}
		free(widget->subwidget);
		widget->subwidget = NULL;
	}
	/* finally de-allocate the widget node, this should always be the last node removed in here */
	remove_widget(widget);

	/* resize the container that used to contain this widget, if it exists */
	if (tmp)
	{
		/* if something else exists in its inventory, make it auto-resize to fit the widgets inside */
		if (tmp->inv)
		{
			resize_widget(tmp->inv, RESIZE_RIGHT, tmp->inv->wd);
			resize_widget(tmp->inv, RESIZE_BOTTOM, tmp->inv->ht);
		}
		/* otherwise if its inventory is empty, resize it to its default size */
		else
		{
			resize_widget(tmp, RESIZE_RIGHT, con_widget[tmp->WidgetSubtypeID].wd);
			resize_widget(tmp, RESIZE_BOTTOM, con_widget[tmp->WidgetSubtypeID].ht);
		}
	}
}

/**
 * Deletes the entire inventory of a widget, child nodes first. This should be the fastest way.
 * Any widgets that can't be deleted should end up on the top level.
 * This function is already automatically handled with the delete_inv flag,
 * so it shouldn't be called explicitly apart from in remove_widget_object_intern(). */
void remove_widget_inv(widgetdata *widget)
{
	widgetdata *tmp;

	for (widget = widget->inv; widget; widget = tmp)
	{
		/* call this function recursively to get to the first child node deep down inside the widget */
		remove_widget_inv(widget);
		/* we need a temp pointer for the next node, as the current node is about to be no more */
		tmp = widget->next;
		/* then remove the widget, and slowly work our way up the tree deleting widgets until we get to the original widget again */
		remove_widget_object(widget);
	}
}

/** Wrapper function to initiate one of each widget. */
/* TODO: this is looking more and more like a function to simply initiate all the widgets with their default attributes,
 * as loading from a file now creates nodes dynamically instead, so this function is now doomed to that role */
void init_widgets()
{
    int i;

    /* exit, if there're no widgets */
	if (!TOTAL_SUBWIDGETS)
	{
	    return;
	}

    /* in all cases should reset */
	kill_widgets();

    /* initiate the widget tree and everything else that links to it. */
    for (i = 0; i < TOTAL_SUBWIDGETS; ++i)
	{
	    create_widget_object(i);
	}

    LOG(llevDebug, "..Allocated %d nodes!\n", i);
}

/**
 * Deinitialize all widgets, and free their SDL surfaces. */
void kill_widgets()
{
	/* get rid of the pointer to the widgets first */
	widget_mouse_event.owner = NULL;
	widget_event_move.owner = NULL;

	/* kick off the chain reaction, there's no turning back now :) */
	if (widget_list_head)
	{
		kill_widget_tree(widget_list_head);
	}
}

/** Recursive function to nuke the entire widget tree. */
void kill_widget_tree(widgetdata *widget)
{
	widgetdata *tmp;

	do
	{
		/* we want to process the widgets starting from the left hand side of the tree first */
		if (widget->inv)
		{
			kill_widget_tree(widget->inv);
		}

		/* store the next sibling in a tmp variable, as widget is about to be zapped from existance */
		tmp = widget->next;

		/* here we call our widget kill function, and force removal by using the internal one */
		remove_widget_object_intern(widget);

		/* get the next sibling for our next loop */
		widget = tmp;
	}
	while (widget);
}

/**
 * Creates a new widget object with a unique ID and inserts it at the root of the widget tree.
 * This should always be the first function called by create_widget_object() in order to get the pointer
 * to the new node so we can link it to other new nodes that depend on it. */
widgetdata *create_widget(int widget_id)
{
	widgetdata *node;
	static int widget_uid = 0; /* our unique widget count variable */

#ifdef DEBUG_WIDGET
	LOG(llevInfo, "Entering create_widget()..\n");
#endif

	/* allocate it */
	node = malloc (sizeof(widgetdata));
	if (!node)
	{
		exit(0);
	}

	/* set the members */
	*node = con_widget[widget_id]; /* this also sets all pointers in the struct to NULL */
	node->WidgetSubtypeID = widget_id;
	node->WidgetObjID = widget_uid; /* give it a unique ID */

	/* link it up to the tree if the root exists */
	if (widget_list_head)
	{
		node->next = widget_list_head;
		widget_list_head->prev = node;
	}

	/* set the foot if it doesn't exist */
	if (!widget_list_foot)
	{
		widget_list_foot = node;
	}

	/* the new node becomes the new root node, which also automatically brings it to the front */
	widget_list_head = node;

	/* if head of widget type linked list doesn't exist, set the head and foot */
	if (!cur_widget[widget_id])
	{
		cur_widget[widget_id] = type_list_foot[widget_id] = node;
	}
	/* otherwise, link the node in to the existing type list */
	else
	{
		type_list_foot[widget_id]->type_next = node;
		node->type_prev = type_list_foot[widget_id];
		type_list_foot[widget_id] = node;
	}

	/* increment the unique ID counter */
	++widget_uid;

	LOG(llevDebug, "..ALLOCATED: %s, WidgetObjID: %d\n", node->name, node->WidgetObjID);
#ifdef DEBUG_WIDGET
	debug_count_nodes(1);

	LOG(llevInfo, "..create_widget(): Done.\n");
#endif

	return node;
}

/** Removes the pointer passed to it from anywhere in the linked list and reconnects the adjacent nodes to each other. */
void remove_widget(widgetdata *widget)
{
	widgetdata *tmp = NULL;

#ifdef DEBUG_WIDGET
	LOG(llevInfo, "Entering remove_widget()..\n");
#endif

	/* node to delete is the only node in the tree, bye-bye binary tree :) */
	if (!widget_list_head->next && !widget_list_head->inv)
	{
		widget_list_head = NULL;
		widget_list_foot = NULL;
		cur_widget[widget->WidgetSubtypeID] = NULL;
		type_list_foot[widget->WidgetSubtypeID] = NULL;
	}
	else
	{
		/* node to delete is the head, move the pointer to next node */
		if (widget == widget_list_head)
		{
			widget_list_head = widget_list_head->next;
			widget_list_head->prev = NULL;
		}
		/* node to delete is the foot, move the pointer to the previous node */
		else if (widget == widget_list_foot)
		{
			widget_list_foot = widget_list_foot->prev;
			widget_list_foot->next = NULL;
		}
		/* node is first sibling, and should have a parent since it is not the root node */
		else if (!widget->prev)
		{
			/* node is also the last sibling, so NULL the parent's inventory */
			if (!widget->next)
			{
				widget->env->inv = NULL;
				widget->env->inv_rev = NULL;
			}
			/* or else make it the parent's first child */
			else
			{
				widget->env->inv = widget->next;
				widget->next->prev = NULL;
			}
		}
		/* node is last sibling and should have a parent, move the inv_rev pointer to the previous sibling */
		else if (!widget->next)
		{
			widget->env->inv_rev = widget->prev;
			widget->prev->next = NULL;
		}
		/* node to delete is in the middle of the tree somewhere */
		else
		{
			widget->next->prev = widget->prev;
			widget->prev->next = widget->next;
		}

		/* move the children to the top level of the list, starting from the end child */
		for (tmp = widget->inv_rev; tmp; tmp = tmp->prev)
		{
			/* tmp is no longer in a container */
			tmp->env = NULL;
			widget_list_head->prev = tmp;
			tmp->next = widget_list_head;
			widget_list_head = tmp;
		}

		/* if widget type list has only one node, kill it */
		if (cur_widget[widget->WidgetSubtypeID] == type_list_foot[widget->WidgetSubtypeID])
		{
			cur_widget[widget->WidgetSubtypeID] = type_list_foot[widget->WidgetSubtypeID] = NULL;
		}
		/* widget is head node */
		else if (widget == cur_widget[widget->WidgetSubtypeID])
		{
			cur_widget[widget->WidgetSubtypeID] = cur_widget[widget->WidgetSubtypeID]->type_next;
			cur_widget[widget->WidgetSubtypeID]->type_prev = NULL;
		}
		/* widget is foot node */
		else if (widget == type_list_foot[widget->WidgetSubtypeID])
		{
			type_list_foot[widget->WidgetSubtypeID] = type_list_foot[widget->WidgetSubtypeID]->type_prev;
			type_list_foot[widget->WidgetSubtypeID]->type_next = NULL;
		}
		/* widget is in middle of type list */
		else
		{
			widget->type_prev->type_next = widget->type_next;
			widget->type_next->type_prev = widget->type_prev;
		}
	}

	LOG(llevDebug, "..REMOVED: %s, WidgetObjID: %d\n", widget->name, widget->WidgetObjID);

	/* free the surface */
	if (widget->widgetSF)
	{
		SDL_FreeSurface(widget->widgetSF);
		widget->widgetSF = NULL;
	}
	free(widget);

#ifdef DEBUG_WIDGET
	debug_count_nodes(1);
	LOG(llevInfo, "..remove_widget(): Done.\n");
#endif
}

/** Removes the widget from the container it is inside and moves it to the top of the priority tree. */
void detach_widget(widgetdata *widget)
{
	/* sanity check */
	if (!widget->env)
	{
		return;
	}

	/* first unlink the widget from the container and siblings */

	/* if widget is only one in the container's inventory, clear both pointers */
	if (!widget->prev && !widget->next)
	{
		widget->env->inv = NULL;
		widget->env->inv_rev = NULL;
	}
	/* widget is first sibling */
	else if (!widget->prev)
	{
		widget->env->inv = widget->next;
		widget->next->prev = NULL;
	}
	/* widget is last sibling */
	else if (!widget->next)
	{
		widget->env->inv_rev = widget->prev;
		widget->prev->next = NULL;
	}
	/* widget is a middle sibling */
	else
	{
		widget->prev->next = widget->next;
		widget->next->prev = widget->prev;
	}

	/* if something else exists in the container's inventory, make it auto-resize to fit the widgets inside */
	if (widget->env->inv)
	{
		resize_widget(widget->env->inv, RESIZE_RIGHT, widget->env->inv->wd);
		resize_widget(widget->env->inv, RESIZE_BOTTOM, widget->env->inv->ht);
	}
	/* otherwise if its inventory is empty, resize it to its default size */
	else
	{
		resize_widget(widget->env, RESIZE_RIGHT, con_widget[widget->env->WidgetSubtypeID].wd);
		resize_widget(widget->env, RESIZE_BOTTOM, con_widget[widget->env->WidgetSubtypeID].ht);
	}

	/* widget is no longer in a container */
	widget->env = NULL;
	/* move the widget to the top of the priority tree */
	widget->prev = NULL;
	widget_list_head->prev = widget;
	widget->next = widget_list_head;
	widget_list_head = widget;
}

#ifdef DEBUG_WIDGET
/** A debug function to count the number of widget nodes that exist. */
int debug_count_nodes_rec(widgetdata *widget, int i, int j, int output)
{
    int tmp = 0;

    do
    {
        /* we print out the top node, and then go down a level, rather than go down first */
        if (output)
        {
            /* a way of representing graphically how many levels down we are */
            for (tmp = 0; tmp < j; ++ tmp)
			{
                printf("..");
			}

            LOG(llevInfo, "..%s, WidgetObjID: %d\n", widget->name, widget->WidgetObjID);
        }

	    ++i;

        /* we want to process the widgets starting from the left hand side of the tree first */
        if (widget->inv)
		{
            i = debug_count_nodes_rec(widget->inv, i, j + 1, output);
		}

        /* get the next sibling for our next loop */
        widget = widget->next;
    }
    while (widget);

    return i;
}

void debug_count_nodes(int output)
{
    int i = 0;

    LOG(llevInfo, "Output of widget nodes:\n");
    LOG(llevInfo, "========================================\n");
    if (widget_list_head)
	{
        i = debug_count_nodes_rec(widget_list_head, 0, 0, output);
	}
    LOG(llevInfo, "========================================\n");
    LOG(llevInfo, "..Total widget nodes: %d\n", i);
}
#endif

/**
 * Load the widgets interface from a file.
 * @param filename The interface filename.
 * @return 1 on success, 0 on failure. */
static int load_interface_file(char *filename)
{
	int i = -1, pos;
	FILE *stream;
	widgetdata *widget = NULL;
	_textwin *textwin = NULL;
	char line[256], keyword[256], parameter[256];
	int found_widget[TOTAL_SUBWIDGETS] = {0};

	LOG(llevDebug, "Entering load_interface_file()..\n");

	/* Sanity check - if the file doesn't exist, exit with error */
	if (!(stream = fopen_wrapper(filename, "r")))
	{
		/* Inform user */
		LOG(llevInfo, "load_interface_file(): Can't find file %s.\n", filename);
		return 0;
	}

	/* Read the settings from the file */
	while (fgets(line, 255, stream))
	{
		if (line[0] == '#' || line[0] == '\n')
		{
			continue;
		}

		i = 0;

		while (line[i] && line[i] != ':')
		{
			i++;
		}

		line[++i] = '\0';

		strncpy(keyword, line, sizeof(keyword));
		strncpy(parameter, line + i + 1, sizeof(parameter));

		/* Remove the newline character */
		parameter[strcspn(line + i + 1, "\n")] = 0;

		/* Beginning */
		if (strncmp(keyword, "Widget:", 7) == 0)
		{
			LOG(llevDebug, "..Trying to find \"Widget: %s\"\n", parameter);

			pos = 0;

			/* Find the index of the widget for reference */
			while (pos < TOTAL_SUBWIDGETS && (strcmp(con_widget[pos].name, parameter) != 0))
			{
				++pos;
			}

			/* The widget name couldn't be found? */
			if (pos >= TOTAL_SUBWIDGETS)
			{
				continue;
			}
			/* Get the block */
			else
			{
				/* If we haven't found this widget, mark it */
				if (!found_widget[pos])
				{
#ifdef DEBUG_WIDGET
					LOG(llevInfo, "Found! (Index = %d) (%d widgets total)\n", pos, TOTAL_SUBWIDGETS);
#endif
					found_widget[pos] = 1;
				}

				/* create the widget with that ID, it is already fully initialised to the defaults */
				widget = create_widget_object(pos);

				/* in case something went wrong */
				if (!widget)
				{
					LOG(llevDebug, ".. Failed to create widget!\n");
					continue;
				}

				textwin = TEXTWIN(widget);

				while (fgets(line, 255, stream))
				{
					if (line[0] == '#' || line[0] == '\n')
					{
						continue;
					}

					/* End marker */
					if (strncmp(line, "end", 3) == 0)
						break;

					i = 0;

					while (line[i] && line[i] != ':')
					{
						i++;
					}

					line[++i] = '\0';
					strcpy(keyword, line);
					strcpy(parameter, line + i + 1);

					if (strncmp(keyword, "x:", 2) == 0)
					{
						widget->x1 = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->x1);
					}
					else if (strncmp(keyword, "y:", 2) == 0)
					{
						widget->y1 = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->y1);
					}
					else if (strncmp(keyword, "moveable:", 9) == 0)
					{
						widget->moveable = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->moveable);
					}
					else if (strncmp(keyword, "active:", 7) == 0)
					{
						widget->show = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->show);
					}
					else if (strncmp(keyword, "width:", 6) == 0)
					{
						widget->wd = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->wd);
					}
					else if (strncmp(keyword, "height:", 7) == 0)
					{
						widget->ht = atoi(parameter);
						LOG(llevDebug, "..Loading: (%s %d)\n", keyword, widget->ht);
					}

                    /* handle loading of extended attributes here */
                    switch (widget->WidgetTypeID)
                    {
                        case CHATWIN_ID:
                        case MSGWIN_ID:
                        case MIXWIN_ID:
                            if (strncmp(keyword, "....TextwinSize:", 16) == 0)
                            {
                                textwin->size = atoi(parameter);
                                /* update the widget to the loaded textwin size */
                                widget->ht = textwin->size * 10 + 13;
						        LOG(llevDebug, "..Loading: (%s %d)\n", keyword, textwin->size);
						    }
						    break;
				    }
				}
			}
		}
	}

	fclose(stream);

	/* Go through the widgets */
	for (pos = 0; pos < TOTAL_SUBWIDGETS; ++pos)
	{
		/* If a required widget was not found, load the default data for it. */
		if (!found_widget[pos] && con_widget[pos].no_kill)
		{
			/* A newly created widget is loaded with the default values. */
			create_widget_object(pos);
			LOG(llevDebug, "load_interface_file(): Critical widget is missing! Recreating with default values.\n");
		}
	}

	LOG(llevDebug, "..load_interface_file(): Done.\n");

	return 1;
}

/**
 * Save the widgets interface to a file. */
void save_interface_file()
{
	FILE *stream;

	/* Leave, if there's an error opening or creating */
	if (!(stream = fopen_wrapper(INTERFACE_FILE, "w")))
	{
		return;
	}

	fputs("#############################################\n", stream);
	fputs("# This is the Atrinik client interface file #\n", stream);
	fputs("#############################################\n", stream);

	/* start walking through the widgets */
	save_interface_file_rec(widget_list_head, stream);

	fclose(stream);
}

/**
 * The recursive part of save_interface_file().
 * NEVER call this explicitly, use save_interface_file() in order to use this safely. */
void save_interface_file_rec(widgetdata *widget, FILE *stream)
{
	_textwin *textwin;

    do
    {
		/* skip the widget if it shouldn't be saved */
		if (!widget->save)
		{
			widget = widget->next;
			continue;
		}

		/* we want to process the widgets starting from the left hand side of the tree first */
		if (widget->inv)
		{
			save_interface_file_rec(widget->inv, stream);
		}

		fprintf(stream, "\nWidget: %s\n", widget->name);
		fprintf(stream, "moveable: %d\n", widget->moveable);
		fprintf(stream, "active: %d\n", widget->show);
		fprintf(stream, "x: %d\n", widget->x1);
		fprintf(stream, "y: %d\n", widget->y1);

		if (widget->save_width_height)
		{
			fprintf(stream, "width: %d\n", widget->wd);
			fprintf(stream, "height: %d\n", widget->ht);
		}

		/* Handle saving of extended attributes here */
		switch (widget->WidgetTypeID)
		{
			case CHATWIN_ID:
			case MSGWIN_ID:
			case MIXWIN_ID:
				textwin = TEXTWIN(widget);
				fprintf(stream, "....TextwinSize: %d\n", textwin->size);
				break;
		}

		/* End of block */
		fputs("end\n", stream);

		/* get the next sibling for our next loop */
		widget = widget->next;
	}
	while (widget);
}

/**
 * Mouse is down. Check for owner of the mouse focus.
 * Setup widget dragging, if enabled
 * @param x Mouse X position.
 * @param y Mouse Y position.
 * @param event SDL event type.
 * @return 1 if this is a widget and we're handling the mouse, 0 otherwise. */
int widget_event_mousedn(int x, int y, SDL_Event *event)
{
	widgetdata *widget, *menu, *tmp;

	/* update the widget event struct if the mouse is in a widget, or else get out of here for sanity reasons */
	if (!widget_event_respond(x, y))
	{
		return 0;
	}

	widget = widget_mouse_event.owner;
	/* sanity check */
	if (!widget)
	{
		return 0;
	}

	/* Set the priority to this widget */
	SetPriorityWidget(widget);

	/* Right mouse button was clicked */
	if (MouseEvent == RB_DN)
	{
		/* For some reason, checking for the ctrl key won't work here. */
		if (cpl.fire_on)
		{
			/* Move the widget. */
			if (!widget_event_start_move(widget, x, y))
			{
				return 0;
			}
		}
		/* Normal right click. */
		else if (!cur_widget[MENU_ID])
		{
			/* Create a context menu for the widget clicked on. */
			menu = create_menu(x, y, widget);
			/* This bit probably shouldn't be hard coded in future. */
			add_menuitem(menu, "Move Widget", &menu_move_widget, MENU_NORMAL);
			add_menuitem(menu, "Create Widget", &menu_create_widget, MENU_NORMAL);
			add_menuitem(menu, "Remove Widget", &menu_remove_widget, MENU_NORMAL);
			add_menuitem(menu, "Detach Widget", &menu_detach_widget, MENU_NORMAL);
            add_separator(menu);
            add_menuitem(menu, "Chat Window Filters", &menu_detach_widget, MENU_SUBMENU);

			/* Bit hack-ish, but this is to fix the menu from disappearing. */
			widget = menu;
		}
	}
	/* create a new widget of the same type, quick hack until we have something better such as buttons */
	else if (MouseEvent == MB_DN)
	{
		if (cpl.fire_on) /* for some reason, checking for the ctrl key won't work here */
		{
			remove_widget_object(widget);
		}
		else
		{
			create_widget_object(widget->WidgetSubtypeID);
		}
	}
	/* Normal condition - respond to mouse down event */
	else
	{
        /* Handler(s) for miscellanous mouse movement(s) go here. */

        /* Special case for menuitems, if menuitem or a widget inside is clicked on, calls the function tied to the menuitem. */
        widget_menu_event(widget, x, y);

        /* Place here all the mousedown handlers. */
		switch (widget->WidgetTypeID)
		{
			case SKILL_EXP_ID:
				widget_skill_exp_event(widget);
				break;

			case MENU_B_ID:
				widget_menubuttons_event(widget, x, y);
				break;

			case QUICKSLOT_ID:
				widget_quickslots_mouse_event(widget, x, y, MOUSE_DOWN);
				break;

			case CHATWIN_ID:
			case MSGWIN_ID:
			case MIXWIN_ID:
				textwin_event(TW_CHECK_BUT_DOWN, event, widget);
				break;

			case RANGE_ID:
				widget_range_event(widget, x, y, *event, MOUSE_DOWN);
				break;

			case BELOW_INV_ID:
				widget_below_window_event(widget, x, y, MOUSE_DOWN);
				break;

			case TARGET_ID:
				widget_event_target(widget, x, y);
				break;

			case MAIN_INV_ID:
				widget_inventory_event(widget, x, y, *event);
				break;

			case PLAYER_INFO_ID:
				widget_player_data_event(widget, x, y);
				break;

			case IN_NUMBER_ID:
				widget_number_event(widget, x, y);
				break;
		}
	}

	/* User didn't click on a menu, so remove any menus that exist. */
	if (widget->WidgetSubtypeID != MENU_ID)
	{
		for (menu = cur_widget[MENU_ID]; menu; menu = tmp)
		{
			tmp = menu->type_next;
			remove_widget_object(menu);
		}
	}

	return 1;
}

/**
 * Mouse is up. Check for owner of mouse focus.
 * Stop dragging the widget, if active.
 * @param x Mouse X position.
 * @param y Mouse Y position.
 * @param event SDL event type.
 * @return 1 if this is a widget and we're handling the mouse, 0 otherwise. */
int widget_event_mouseup(int x, int y, SDL_Event *event)
{
	widgetdata *widget;
	widgetdata *widget_container = NULL;

	/* Widget moving condition */
	if (widget_event_move.active)
	{
		widget = widget_mouse_event.owner;

		widget_event_move.active = 0;
		widget_mouse_event.x = x;
		widget_mouse_event.y = y;
		/* no widgets are being moved now */
		widget_event_move.owner = NULL;

		/* Disable the custom cursor */
		f_custom_cursor = 0;

		/* Show the system cursor */
		SDL_ShowCursor(1);

		/* Due to a bug in SDL 1.2.x, the mouse X/Y position is not updated
		 * while in fullscreen with the cursor hidden, so we must take care
		 * of it ourselves. Apparently SDL 1.3 should fix it.
		 * See http://old.nabble.com/Mouse-movement-problems-in-fullscreen-mode-td20890669.html
		 * for details. */
		SDL_WarpMouse(x, y);

		/* Somehow the owner before the widget dragging is gone now. Not a good idea to carry on... */
		if (!widget)
		{
			return 0;
		}

		/* check to see if it's on top of a widget container */
		widget_container = get_widget_owner(x, y, widget->next, NULL);

        /* attempt to insert it into the widget container if it exists */
        insert_widget_in_container(widget_container, widget);

		return 1;
	}
	/* Normal condition - respond to mouse up event */
	else
	{
		/* update the widget event struct if the mouse is in a widget, or else get out of here for sanity reasons */
		if (!widget_event_respond(x, y))
		{
			return 0;
		}

		widget = widget_mouse_event.owner;
		/* sanity check */
		if (!widget)
			return 0;

		/* Handler for the widgets go here */
		switch (widget->WidgetTypeID)
		{
			case QUICKSLOT_ID:
				widget_quickslots_mouse_event(widget, x, y, MOUSE_UP);
				break;

			case CHATWIN_ID:
			case MSGWIN_ID:
			case MIXWIN_ID:
				textwin_event(TW_CHECK_BUT_UP, event, widget);
				break;

			case PDOLL_ID:
				widget_show_player_doll_event();
				break;

			case RANGE_ID:
				widget_range_event(widget, x, y, *event, MOUSE_UP);
				break;

			case MAIN_INV_ID:
				widget_inventory_event(widget, x, y, *event);
				break;
		}

		return 1;
	}
}

/**
 * Mouse was moved. Check for owner of mouse focus.
 * Drag the widget, if active.
 * @param x Mouse X position.
 * @param y Mouse Y position.
 * @param event SDL event type.
 * @return 1 if this is a widget and we're handling the mouse, 0 otherwise. */
int widget_event_mousemv(int x, int y, SDL_Event *event)
{
	widgetdata *widget;
	int dx = 0, dy = 0;

	/* With widgets we have to clear every loop the txtwin cursor */
	cursor_type = 0;

	/* Widget moving condition */
	if (widget_event_move.active)
	{
		widget = widget_event_move.owner;

		/* The widget being moved doesn't exist. Sanity check in case something mad like this happens. */
		if (!widget)
		{
			return 0;
		}

#ifdef WIDGET_SNAP
		if (options.widget_snap > 0)
		{
			if (event->motion.xrel != 0 && event->motion.yrel != 0)
			{
				int mID = widget_event_move.owner->WidgetTypeID;
				widget_node *node;

				for (node = priority_list_head; node; node = node->next)
				{
					int nID = node->WidgetID;
					int done = 0;

					if (nID == mID || !cur_widget[nID].show)
					{
						continue;
					}

					if ((TOP(mID) >= TOP(nID) && TOP(mID) <= BOTTOM (nID)) || (BOTTOM(mID) >= TOP(nID) && BOTTOM(mID) <= BOTTOM(nID)))
					{
						if (event->motion.xrel < 0 && LEFT(mID) <= RIGHT(nID) + options.widget_snap && LEFT(mID) > RIGHT(nID))
						{
#if 0
							adjx = RIGHT(nID);
#endif
							event->motion.x = RIGHT(nID) + widget_event_move.xOffset;
							done = 1;
						}
						else if (event->motion.xrel > 0 && RIGHT(mID) >= LEFT(nID) - options.widget_snap && RIGHT(mID) < LEFT(nID))
						{
#if 0
							adjx = LEFT(nID) - cur_widget[mID].wd;
#endif
							event->motion.x = LEFT(nID) - cur_widget[mID].wd + widget_event_move.xOffset;
							done = 1;
						}
					}

					if ((LEFT(mID) >= LEFT(nID) && LEFT(mID) <= RIGHT(nID)) || (RIGHT(mID) >= LEFT(nID) && RIGHT(mID) <= RIGHT(nID)))
					{
						if (event->motion.yrel < 0 && TOP(mID) <= BOTTOM(nID) + options.widget_snap && TOP(mID) > BOTTOM(nID))
						{
#if 0
							adjy = BOTTOM(nID);
#endif
							event->motion.y = BOTTOM(nID) + widget_event_move.yOffset;
							done = 1;
						}
						else if (event->motion.yrel > 0 && BOTTOM(mID) >= TOP(nID) - options.widget_snap && BOTTOM(mID) < TOP(nID))
						{
#if 0
							adjy = TOP(nID) - cur_widget[mID].ht;
#endif
							event->motion.y = TOP(nID) - cur_widget[mID].ht + widget_event_move.yOffset;
							done = 1;
						}
					}

					if (done)
					{
#if 0
						draw_info_format(COLOR_RED, "%s l=%d r=%d t=%d b=%d", cur_widget[nID].name, LEFT(nID), RIGHT(nID), TOP(nID), BOTTOM(nID));
#endif
						sound_play_effect("scroll.ogg", 10);

						/* Acts as a brake, preventing mID from 'skipping' through a stack of nodes */
						event->motion.xrel = event->motion.yrel = 0;
						SDL_PushEvent(event);
						break;
					}
				}
			}
		}
#endif
		/* get the offset */
		dx = x - widget_event_move.xOffset - widget->x1;
		dy = y - widget_event_move.yOffset - widget->y1;

		/* we move the widget here, as well as all the widgets inside it if they exist */
		/* we use the recursive version since we already have the outermost container */
		move_widget_rec(widget, dx, dy);

		map_udate_flag = 2;

		return 1;
	}
	/* Normal condition - respond to mouse move event */
	else
	{
		_textwin *textwin = NULL;

		/* update the widget event struct if the mouse is in a widget, or else get out of here for sanity reasons */
		if (!widget_event_respond(x, y))
		{
			return 0;
		}

		widget = widget_mouse_event.owner;
		/* sanity check */
		if (!widget)
		{
			return 0;
		}

		/* Handlers for miscellanous mouse movements go here */

		/* Handlers for the widgets mouse move */
		switch (widget->WidgetTypeID)
		{
			case CHATWIN_ID:
			case MSGWIN_ID:
			case MIXWIN_ID:
				textwin = TEXTWIN(widget);
				/* textwin special handling */
				if (textwin)
				{
					if (textwin->highlight != TW_HL_NONE)
					{
						textwin->highlight = TW_HL_NONE;
						WIDGET_REDRAW(widget);
					}
				}
				textwin_event(TW_CHECK_MOVE, event, widget);
				break;

			case MAIN_INV_ID:
				widget_inventory_event(widget, x, y, *event);
				break;
		}

		return 1;
	}
}

/** Handles the initiation of widget dragging. */
int widget_event_start_move(widgetdata *widget, int x, int y)
{
	/* get the outermost container so we can move the container with everything in it */
	widget = get_outermost_container(widget);

	/* if its moveable, start moving it when the conditions warrant it, or else run away */
	if (!widget->moveable)
	{
		return 0;
	}

	/* we know this widget owns the mouse.. */
	widget_event_move.active = 1;

	/* start the movement procedures */
	widget_event_move.owner = widget;
	widget_event_move.xOffset = x - widget->x1;
	widget_event_move.yOffset = y - widget->y1;

	/* enable the custom cursor */
	f_custom_cursor = MSCURSOR_MOVE;
	/* hide the system cursor */
	SDL_ShowCursor(0);

#ifdef WIN32
	/* Workaround another bug with SDL 1.2.x on Windows. Make sure the cursor
	 * is in the centre of the screen if we are in fullscreen mode. */
	if (ScreenSurface->flags & SDL_FULLSCREEN)
	{
		SDL_WarpMouse(Screensize->x / 2, Screensize->y / 2);
	}
#endif

	return 1;
}

/** Updates the widget mouse event struct in order to respond to an event. */
int widget_event_respond(int x, int y)
{
    /* only update the owner if there is no event override taking place */
    if (!widget_event_override())
	{
        widget_mouse_event.owner = get_widget_owner(x, y, NULL, NULL);
	}

    /* sanity check.. return if mouse is not in a widget */
    if (!widget_mouse_event.owner)
	{
        return 0;
	}

    /* setup the event structure in response */
	widget_mouse_event.x = x;
    widget_mouse_event.y = y;

    return 1;
}

/** Priority overide function, we have to have that here for resizing... */
int widget_event_override()
{
    if (textwin_flags & TW_RESIZE)
	{
        return 1;
	}

    return 0;
}

/** Find the widget with mouse focus on a mouse-hit-test basis. */
widgetdata *get_widget_owner(int x, int y, widgetdata *start, widgetdata *end)
{
	widgetdata *success;

	/* mouse cannot be used by widgets */
	if (IsMouseExclusive)
	{
		return NULL;
	}

	/* no widgets exist */
	if (!widget_list_head)
	{
		return NULL;
	}

	/* sometimes we want a fast way to get the widget behind the widget at the front.
	 * this is what start is for, and we will only start walking the list beginning with start.
	 * if start is NULL, we just do a regular search */
	if (!start)
	{
		start = widget_list_head;
	}

	/* ok, let's kick off the recursion. if we find our widget, we get a widget back. if not, we get a big fat NULL */
	success = get_widget_owner_rec(x, y, start, end);

	/*LOG(llevDebug, "WIDGET OWNER: %s, WidgetObjID: %d\n", success? success->name: "NULL", success? success->WidgetObjID: -1);*/

	return success;
}

/* traverse through the tree & perform custom or default hit-test */
widgetdata *get_widget_owner_rec(int x, int y, widgetdata *widget, widgetdata *end)
{
	widgetdata *success = NULL;

	do
	{
		/* we want to get the first widget starting from the left hand side of the tree first */
		if (widget->inv)
		{
			success = get_widget_owner_rec(x, y, widget->inv, end);
			/* we found a widget in the hit test? if so, get out of this recursive mess with our prize */
			if (success)
			{
				return success;
			}
		}

		/* skip if widget is hidden */
		if (!widget->show)
		{
			widget = widget->next;
			continue;
		}

		switch (widget->WidgetTypeID)
		{
			case PDOLL_ID: /* Playerdoll widget is NOT a rectangle, handle special... */
				if (x > widget->x1 + 111)
				{
					if (x <= widget->x1 + widget->wd && y >= widget->y1 && y <= ((x - (widget->x1 + 111)) / -2) + 215 + widget->y1)
					{
						return widget;
					}
				}
				else
				{
					if (x >= widget->x1 && y >= widget->y1 && y <= ((x - widget->x1) / 2) + 160 + widget->y1)
					{
						return widget;
					}
				}
				break;

			default:
				if (x >= widget->x1 && x <= (widget->x1 + widget->wd) && y >= widget->y1 && y <= (widget->y1 + widget->ht))
				{
					return widget;
				}
		}

		/* get the next sibling for our next loop */
		widget = widget->next;
	}
	while (widget || widget != end);

	return NULL;
}

/**
 * Function list for each widget. Calls the widget with the process type.
 * @param nID The widget ID. */
static void process_widget(widgetdata *widget)
{
	switch (widget->WidgetTypeID)
	{
		case STATS_ID:
			widget_player_stats(widget);
			break;

		case RESIST_ID:
			widget_show_resist(widget);
			break;

		case MAIN_LVL_ID:
			widget_show_main_lvl(widget);
			break;

		case SKILL_EXP_ID:
			widget_show_skill_exp(widget);
			break;

		case REGEN_ID:
			widget_show_regeneration(widget);
			break;

		case SKILL_LVL_ID:
			widget_skillgroups(widget);
			break;

		case MENU_B_ID:
			widget_menubuttons(widget);
			break;

		case QUICKSLOT_ID:
			widget_quickslots(widget);
			break;

		case CHATWIN_ID:
		case MSGWIN_ID:
		case MIXWIN_ID:
			widget_textwin_show(widget);
			break;

		case PDOLL_ID:
			widget_show_player_doll(widget);
			break;

		case BELOW_INV_ID:
			widget_show_below_window(widget);
			break;

		case PLAYER_INFO_ID:
			widget_show_player_data(widget);
			break;

		case RANGE_ID:
			widget_show_range(widget);
			break;

		case TARGET_ID:
			widget_show_target(widget);
			break;

		case MAIN_INV_ID:
			widget_show_inventory_window(widget);
			break;

		case MAPNAME_ID:
			widget_show_mapname(widget);
			break;

		case IN_CONSOLE_ID:
			widget_show_console(widget);
			break;

		case IN_NUMBER_ID:
			widget_show_number(widget);
			break;

		case SHOP_ID:
			widget_show_shop(widget);
			break;

		case FPS_ID:
			widget_show_fps(widget);
			break;

		case CONTAINER_ID:
			widget_show_container(widget);
			break;

		case LABEL_ID:
			widget_show_label(widget);
			break;

		case BITMAP_ID:
			widget_show_bitmap(widget);
			break;
	}
}

/**
 * Traverse through all the widgets and call the corresponding handlers.
 * This is now a wrapper function just to make the sanity checks before continuing with the actual handling. */
void process_widgets()
{
	/* sanity check */
	if (!widget_list_foot)
	{
		return;
	}

	process_widgets_rec(widget_list_foot);

	/* update the alpha option for use in the next frame */
    old_alpha_option = options.use_TextwinAlpha;
}

/**
 * The priority list is a binary tree, so we walk the tree by using loops and recursions.
 * We actually only need to recurse for every child node. When we traverse the siblings, we can just do a simple loop.
 * This makes it as fast as a linear linked list if there are no child nodes. */
void process_widgets_rec(widgetdata *widget)
{
	do
	{
		/* if widget isn't hidden, process it. this is mostly to do with rendering them */
		if (widget->show && widget->visible)
		{
			process_widget(widget);
		}

		/* we want to process the widgets starting from the right hand side of the tree first */
		if (widget->inv_rev)
		{
			process_widgets_rec(widget->inv_rev);
		}

		/* get the previous sibling for our next loop */
		widget = widget->prev;
	}
	while (widget);
}

/**
 * A recursive function to bring a widget to the front of the priority list.
 * This makes the widget get displayed last so that they appear on top, and handle events first.
 * In order to do this, we need to recurse backwards up the tree to the top node,
 * and then work our way back down again, bringing each node in front of its siblings. */
void SetPriorityWidget(widgetdata *node)
{
	LOG(llevDebug, "Entering SetPriorityWidget(WidgetObjID=%d)..\n", node->WidgetObjID);

	/* widget doesn't exist, means parent node has no children, so nothing to do here */
	if (!node)
	{
		LOG(llevDebug, "..SetPriorityWidget(): Done (Node does not exist).\n");
		return;
	}

	LOG(llevDebug, "..BEFORE:\n");
	LOG(llevDebug, "....node: %p - %s\n", node, node->name);
	LOG(llevDebug, "....node->env: %p - %s\n", node->env, node->env? node->env->name: "NULL");
	LOG(llevDebug, "....node->prev: %p - %s, node->next: %p - %s\n", node->prev, node->prev? node->prev->name: "NULL", node->next, node->next? node->next->name: "NULL");
	LOG(llevDebug, "....node->inv: %p - %s, node->inv_rev: %p - %s\n", node->inv, node->inv? node->inv->name: "NULL", node->inv_rev, node->inv_rev? node->inv_rev->name: "NULL");

	/* see if the node has a parent before continuing */
	if (node->env)
	{
		SetPriorityWidget(node->env);

		/* Strip containers are sorted in a fixed order, and no part of any widget inside should be covered by a sibling.
		 * This means we don't need to bother moving the node to the front inside the container. */
		switch (node->env->WidgetSubtypeID)
		{
			case CONTAINER_STRIP_ID:
			case MENU_ID:
			case MENUITEM_ID:
				return;
		}
	}

	/* now we need to move our other node in front of the first sibling */
	if (!node->prev)
	{
		LOG(llevDebug, "..SetPriorityWidget(): Done (Node already at front).\n");
		return; /* no point continuing, node is already at the front */
	}

	/* Unlink node from its current position in the priority tree. */

	/* node is last sibling, clear the pointer of the previous sibling */
	if (!node->next)
	{
		/* node also has a parent pointing to it, hand the inv_rev pointer to the previous sibling */
		if (node->env)
		{
			node->env->inv_rev = node->prev;
		}
		/* no parent, this must be the foot then, so move it to the previous node */
		else
		{
			widget_list_foot = node->prev;
		}

		node->prev->next = NULL;
	}
	else
	{
		/* link up the adjacent nodes */
		node->prev->next = node->next;
		node->next->prev = node->prev;
	}

	/* Insert node at the head of its container, or make it the root node if it is not in a container. */

	/* Node is now the first sibling so the parent should point to it. */
	if (node->env)
	{
		node->next = node->env->inv;
		node->env->inv = node;
	}
	/* We are out of containers and this node is about to become the first sibling, which means it's taking the place of the root node. */
	else
	{
		node->next = widget_list_head;
		widget_list_head = node;
	}

	/* Point the former head node to this node. */
	node->next->prev = node;
	/* There's no siblings in front of node now. */
	node->prev = NULL;

	LOG(llevDebug, "..AFTER:\n");
	LOG(llevDebug, "....node: %p - %s\n", node, node->name);
	LOG(llevDebug, "....node->env: %p - %s\n", node->env, node->env? node->env->name: "NULL");
	LOG(llevDebug, "....node->prev: %p - %s, node->next: %p - %s\n", node->prev, node->prev? node->prev->name: "NULL", node->next, node->next? node->next->name: "NULL");
	LOG(llevDebug, "....node->inv: %p - %s, node->inv_rev: %p - %s\n", node->inv, node->inv? node->inv->name: "NULL", node->inv_rev, node->inv_rev? node->inv_rev->name: "NULL");

	LOG(llevDebug, "..SetPriorityWidget(): Done.\n");
}

void insert_widget_in_container(widgetdata *widget_container, widgetdata *widget)
{
    _widget_container *container;
	_widget_container_strip *container_strip;

	/* sanity checks */
	if (!widget_container || !widget)
	{
		return;
	}

	/* no, we don't want to end the universe just yet... */
	if (widget_container == widget)
	{
		return;
	}

	/* is the widget already in a container? */
	if (widget->env)
	{
		return;
	}

	/* if the widget isn't a container, get out of here */
	if (widget_container->WidgetTypeID != CONTAINER_ID)
	{
		return;
	}

	/* we have our container, now we attempt to place the widget inside it */
	container = CONTAINER(widget_container);

	/* check to see if the widget is compatible with it */
	if (container->widget_type != -1 && container->widget_type != widget->WidgetTypeID)
	{
		return;
	}

	/* if we get here, we now proceed to insert the widget into the container */

	/* snap the widget into the widget container if it is a strip container */
	if (widget_container->inv)
	{
		switch (widget_container->WidgetSubtypeID)
		{
			case CONTAINER_STRIP_ID:
			case MENU_ID:
			case MENUITEM_ID:
				container_strip = CONTAINER_STRIP(widget_container);

				/* container is horizontal, insert the widget to the right of the first widget in its inventory */
				if (container_strip->horizontal)
				{
					move_widget_rec(widget, widget_container->inv->x1 + widget_container->inv->wd + container_strip->inner_padding - widget->x1, widget_container->y1 + container->outer_padding_top - widget->y1);
				}
				/* otherwise the container is vertical, so insert the widget below the first child widget */
				else
				{
					move_widget_rec(widget, widget_container->x1 + container->outer_padding_left - widget->x1, widget_container->inv->y1 + widget_container->inv->ht + container_strip->inner_padding - widget->y1);
				}
				break;
		}
	}
    /* no widgets inside it yet, so snap it to the bounds of the container */
    else
	{
        move_widget(widget, widget_container->x1 + container->outer_padding_left - widget->x1, widget_container->y1 + container->outer_padding_top - widget->y1);
	}

	/* link up the adjacent nodes, there *should* be at least two nodes next to each other here so no sanity checks should be required */
	if (!widget->prev)
	{
		/* widget is no longer the root now, pass it on to the next widget */
		if (widget == widget_list_head)
		{
			widget_list_head = widget->next;
		}
		widget->next->prev = NULL;
	}
	else if (!widget->next)
	{
		/* widget is no longer the foot, move it to the previous widget */
		if (widget == widget_list_foot)
		{
			widget_list_foot = widget->prev;
		}
		widget->prev->next = NULL;
	}
	else
	{
		widget->prev->next = widget->next;
		widget->next->prev = widget->prev;
	}

	/* the widget to be placed inside will have a new sibling next to it, or NULL if it doesn't exist */
	widget->next = widget_container->inv;
	/* it's also going to be the first child node, so it has no siblings on the other side */
	widget->prev = NULL;

	/* if inventory doesn't exist, set the end child pointer too */
	if (!widget_container->inv)
	{
		widget_container->inv_rev = widget;
	}
	/* otherwise, link the first child in the inventory to the widget about to be inserted */
	else
	{
		widget_container->inv->prev = widget;
	}

	/* this new widget becomes the first widget in the container */
	widget_container->inv = widget;
	/* set the environment of the widget inside */
	widget->env = widget_container;

	/* resize the container to fit the new widget. a little dirty trick here,
	 * we just resize the widget inside by nothing and it will trigger the auto-resize */
	resize_widget(widget, RESIZE_RIGHT, widget->wd);
	resize_widget(widget, RESIZE_BOTTOM, widget->ht);
}

/** Get the outermost container the widget is inside. */
widgetdata *get_outermost_container(widgetdata *widget)
{
	widgetdata *tmp = widget;

	/* Sanity check. */
	if (!widget)
	{
		return NULL;
	}

	/* Get the outsidemost container if the widget is inside one. */
	while (tmp->env)
	{
		tmp = tmp->env;
		widget = tmp;
	}

	return widget;
}

/* wrapper function to get the outermost container the widget is inside before moving it */
void move_widget(widgetdata *widget, int x, int y)
{
	widget = get_outermost_container(widget);

	move_widget_rec(widget, x, y);
}

/* move all widgets inside the container with the container at the same time */
void move_widget_rec(widgetdata *widget, int x, int y)
{
	/* widget doesn't exist, means the parent node has no children */
	if (!widget)
	{
		return;
	}

	/* no movement needed */
	if (x == 0 && y == 0)
	{
		return;
	}

	/* move the widget */
	widget->x1 += x;
	widget->y1 += y;

	/* here, we want to walk through the inventory of the widget, if it exists.
	 * when we come across a widget, we move it like we did with the container.
	 * we loop until we reach the last sibling, but we also need to go recursive if we find a child node */
	for (widget = widget->inv; widget; widget = widget->next)
	{
		move_widget_rec(widget, x, y);
	}
}

void resize_widget(widgetdata *widget, int side, int offset)
{
	int x = widget->x1;
	int y = widget->y1;
	int width = widget->wd;
	int height = widget->ht;

	if (side & RESIZE_LEFT)
	{
		x = widget->x1 + widget->wd - offset;
		width = offset;
	}
	else if (side & RESIZE_RIGHT)
	{
		width = offset;
	}
	if (side & RESIZE_TOP)
	{
		y = widget->y1 + widget->ht - offset;
		height = offset;
	}
	else if (side & RESIZE_BOTTOM)
	{
		height = offset;
	}

	resize_widget_rec(widget, x, width, y, height);
}

void resize_widget_rec(widgetdata *widget, int x, int width, int y, int height)
{
	widgetdata *widget_container, *tmp, *cmp1, *cmp2, *cmp3, *cmp4;
	_widget_container_strip *container_strip = NULL;
	_widget_container *container = NULL;

	/* move the widget. this is the easy bit, watch as your eyes bleed when you see the next thing we have to do */
	widget->x1 = x;
	widget->y1 = y;
	widget->wd = width;
	widget->ht = height;

	WIDGET_REDRAW(widget);

	/* now we get our parent node if it exists */

	/* loop until we hit the first sibling */
	for (widget_container = widget; widget_container->prev; widget_container = widget_container->prev)
	{
		;
	}

	/* does the first sibling have a parent node? */
	if (widget_container->env)
	{
		/* ok, now we need to resize the parent too. but before we do this, we need to see if other widgets inside should prevent it from
		 * being resized. the code below is ugly, but necessary in order to calculate the new size of the container. and one more thing...
		 * MY EYES! THE GOGGLES DO NOTHING! */

		widget_container = widget_container->env;
		container = CONTAINER(widget_container);

		/* special case for strip containers */
		switch (widget_container->WidgetSubtypeID)
		{
			case CONTAINER_STRIP_ID:
			case MENU_ID:
			case MENUITEM_ID:
				container_strip = CONTAINER_STRIP(widget_container);

				/* we move all the widgets before or after the widget that got resized, depending on which side got the resize */
				if (container_strip->horizontal)
				{
					/* now move everything we come across */
					move_widget_rec(widget, 0, widget_container->y1 + container->outer_padding_top - widget->y1);

					/* every node before the widget we push right */
					for (tmp = widget->prev; tmp; tmp = tmp->prev)
					{
						move_widget_rec(tmp, tmp->next->x1 + tmp->next->wd - tmp->x1 + container_strip->inner_padding, widget_container->y1 + container->outer_padding_top - tmp->y1);
					}

					/* while every node after the widget we push left */
					for (tmp = widget->next; tmp; tmp = tmp->next)
					{
						move_widget_rec(tmp, tmp->prev->x1 - tmp->x1 - tmp->wd - container_strip->inner_padding, widget_container->y1 + container->outer_padding_top - tmp->y1);
					}

					/* we have to set this, otherwise stupid things happen */
					x = widget_container->inv_rev->x1;
					/* we don't want the container moving up or down in this case */
					y = widget_container->y1 + container->outer_padding_top;
				}
				else
				{
					/* now move everything we come across */
					move_widget_rec(widget, widget_container->x1 + container->outer_padding_left - widget->x1, 0);

					/* every node before the widget we push downwards */
					for (tmp = widget->prev; tmp; tmp = tmp->prev)
					{
						move_widget_rec(tmp, widget_container->x1 + container->outer_padding_left - tmp->x1, tmp->next->y1 + tmp->next->ht - tmp->y1 + container_strip->inner_padding);
					}

					/* while every node after the widget we push upwards */
					for (tmp = widget->next; tmp; tmp = tmp->next)
					{
						move_widget_rec(tmp, widget_container->x1 + container->outer_padding_left - tmp->x1, tmp->prev->y1 - tmp->y1 - tmp->ht - container_strip->inner_padding);
					}

					/* we don't want the container moving sideways in this case */
					x = widget_container->x1 + container->outer_padding_left;
					/* we have to set this, otherwise stupid things happen */
					y = widget_container->inv_rev->y1;
				}
				break;
		}

		/* TODO: add the buffer system so that this mess of code will only need to be executed after the user stops resizing the widget */
		cmp1 = cmp2 = cmp3 = cmp4 = widget;

		for (tmp = widget_container->inv; tmp; tmp = tmp->next)
		{
			/* widget's left x co-ordinate becomes greater than tmp's left x coordinate */
			if (cmp1->x1 > tmp->x1)
			{
				x = tmp->x1;
				width += cmp1->x1 - tmp->x1;
				cmp1 = tmp;
			}
			/* widget's top y co-ordinate becomes greater than tmp's top y coordinate */
			if (cmp2->y1 > tmp->y1)
			{
				y = tmp->y1;
				height += cmp2->y1 - tmp->y1;
				cmp2 = tmp;
			}
			/* widget's right x co-ordinate becomes less than tmp's right x coordinate */
			if (cmp3->x1 + cmp3->wd < tmp->x1 + tmp->wd)
			{
				width += tmp->x1 + tmp->wd - cmp3->x1 - cmp3->wd;
				cmp3 = tmp;
			}
			/* widget's bottom y co-ordinate becomes less than tmp's bottom y coordinate */
			if (cmp4->y1 + cmp4->ht < tmp->y1 + tmp->ht)
			{
				height += tmp->y1 + tmp->ht - cmp4->y1 - cmp4->ht;
				cmp4 = tmp;
			}
		}

		x -= container->outer_padding_left;
		y -= container->outer_padding_top;
		width += container->outer_padding_left + container->outer_padding_right;
		height += container->outer_padding_top + container->outer_padding_bottom;

		/* after all that, we now check to see if the parent needs to be resized before we waste even more resources going recursive */
		if (x != widget_container->x1 || y != widget_container->y1 || width != widget_container->wd || height != widget_container->ht)
		{
			resize_widget_rec(widget_container, x, width, y, height);
		}
	}
}

/** Creates a label with the given text, font and colour, and sets the size of the widget to the correct boundaries. */
widgetdata *add_label(char *text, _Font *font, int color)
{
	widgetdata *widget;
	_widget_label *label;

	widget = create_widget_object(LABEL_ID);
	label = LABEL(widget);

	label->text = text;

	label->font = font;
	label->color = color;

	resize_widget(widget, RESIZE_RIGHT, get_string_pixel_length(text, font));
	resize_widget(widget, RESIZE_BOTTOM, font->c[0].h);

	return widget;
}

/** Creates a bitmap. */
widgetdata *add_bitmap(int bitmap_id)
{
	widgetdata *widget;
	_widget_bitmap *bitmap;

	widget = create_widget_object(BITMAP_ID);
	bitmap = BITMAP(widget);

	bitmap->bitmap_id = bitmap_id;

	resize_widget(widget, RESIZE_RIGHT, Bitmaps[bitmap_id]->bitmap->w);
	resize_widget(widget, RESIZE_BOTTOM, Bitmaps[bitmap_id]->bitmap->h);

	return widget;
}

/** Initialises a menu widget. */
widgetdata *create_menu(int x, int y, widgetdata *owner)
{
	widgetdata *widget_menu = create_widget_object(MENU_ID);
	_widget_container *container_menu = CONTAINER(widget_menu);
	_widget_container_strip *container_strip_menu = CONTAINER_STRIP(widget_menu);

	/* Place the menu at these co-ordinates. */
	widget_menu->x1 = x;
	widget_menu->y1 = y;
	/* Point the menu to the owner. */
	(MENU(widget_menu))->owner = owner;
	/* Magic numbers for now, maybe it will be possible in future to customise this in files. */
	container_menu->outer_padding_left = 4;
	container_menu->outer_padding_right = 4;
	container_menu->outer_padding_top = 4;
	container_menu->outer_padding_bottom = 4;
	container_strip_menu->inner_padding = 0;

	return widget_menu;
}

/** Adds a menuitem to a menu. */
void add_menuitem(widgetdata *menu, char *text, void (*menu_func_ptr)(widgetdata *, int, int), int menu_type)
{
	widgetdata *widget_menuitem, *widget_label, *widget_bitmap, *tmp;
	_widget_container *container_menuitem, *container_menu;
	_widget_container_strip *container_strip_menuitem;
	_menuitem *menuitem;

	widget_menuitem = create_widget_object(MENUITEM_ID);

	container_menuitem = CONTAINER(widget_menuitem);
	container_strip_menuitem = CONTAINER_STRIP(widget_menuitem);

	/* Initialise attributes. */
	container_menuitem->outer_padding_left = 2;
	container_menuitem->outer_padding_right = 2;
	container_menuitem->outer_padding_top = 0;
	container_menuitem->outer_padding_bottom = 0;
	container_strip_menuitem->inner_padding = 4;
    container_strip_menuitem->horizontal = 1;

	widget_label = add_label(text, &SystemFont, COLOR_WHITE);
	/* This is really just test code to see if bitmaps work.
	 * Menuitems will later contain checkboxes later anyway,
	 * so this will probably evolve into proper code later. */
	widget_bitmap = add_bitmap(BITMAP_LOCK);

	insert_widget_in_container(widget_menuitem, widget_bitmap);
	insert_widget_in_container(widget_menuitem, widget_label);
	insert_widget_in_container(menu, widget_menuitem);

	/* Add the pointer to the function to the menuitem. */
	menuitem = MENUITEM(widget_menuitem);
	menuitem->menu_func_ptr = menu_func_ptr;
    menuitem->menu_type = menu_type;

	/* Sanity check. Menuitems should always exist inside a menu. */
	if (widget_menuitem->env && widget_menuitem->env->WidgetSubtypeID == MENU_ID)
	{
		container_menu = CONTAINER(widget_menuitem->env);

		/* Resize labels in each menuitem to the width of the menu. */
		for (tmp = widget_menuitem; tmp; tmp = tmp->next)
		{
			if (tmp->inv)
			{
				container_menuitem = CONTAINER(tmp);

				resize_widget(tmp->inv, RESIZE_RIGHT, menu->wd - tmp->inv_rev->wd - container_strip_menuitem->inner_padding - container_menu->outer_padding_left - container_menu->outer_padding_right - container_menuitem->outer_padding_left - container_menuitem->outer_padding_right);
			}
		}
	}
}

/** Placeholder for menu separators. */
void add_separator(widgetdata *widget)
{
    (void) widget;
}

/** Redraws all widgets of a particular type. */
void widget_redraw_all(int widget_type_id)
{
	widgetdata *widget = cur_widget[widget_type_id];
	for (; widget; widget = widget->type_next)
	{
		widget->redraw = 1;
	}
}

void menu_move_widget(widgetdata *widget, int x, int y)
{
	widget_event_start_move(widget, x, y);
}

void menu_create_widget(widgetdata *widget, int x, int y)
{
	(void) x;
	(void) y;
	create_widget_object(widget->WidgetSubtypeID);
}

void menu_remove_widget(widgetdata *widget, int x, int y)
{
	(void) x;
	(void) y;
	remove_widget_object(widget);
}

void menu_detach_widget(widgetdata *widget, int x, int y)
{
	(void) x;
	(void) y;
	detach_widget(widget);
}

void menu_set_say_filter(widgetdata *widget, int x, int y)
{
	(void) widget;
	(void) x;
	(void) y;
}

void menu_set_shout_filter(widgetdata *widget, int x, int y)
{
	(void) widget;
	(void) x;
	(void) y;
}

void menu_set_gsay_filter(widgetdata *widget, int x, int y)
{
	(void) widget;
	(void) x;
	(void) y;
}

void menu_set_tell_filter(widgetdata *widget, int x, int y)
{
	(void) widget;
	(void) x;
	(void) y;
}

void menu_set_channel_filter(widgetdata *widget, int x, int y)
{
	(void) widget;
	(void) x;
	(void) y;
}

void submenu_chatwindow_filters(widgetdata *widget, int x, int y)
{
	(void) x;
	(void) y;
	add_menuitem(widget, "Say", &menu_set_say_filter, MENU_CHECKBOX);
	add_menuitem(widget, "Shout", &menu_set_shout_filter, MENU_CHECKBOX);
	add_menuitem(widget, "Group", &menu_set_gsay_filter, MENU_CHECKBOX);
	add_menuitem(widget, "Tells", &menu_set_tell_filter, MENU_CHECKBOX);
	add_menuitem(widget, "Channels", &menu_set_channel_filter, MENU_CHECKBOX);
}
