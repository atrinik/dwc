/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keranen)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 */

package cfeditor.textedit.scripteditor;

import java.io.File;

/**
 * Defines common UI constants used in different dialogs and all used icon
 * files.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public interface IGUIConstants {
    /** Version number of the CFJavaEditor <br>
     *  should be increased when a certain amount of changes happened */
    public static final String VERSION = "0.1"; //Changed as of December 17th, 2004

    /** Internal version number of the included online-documentation <br>
     *  increasing the number causes an automated popup of the docu
     *  when users upgrade their editor and run for the first time */
    public static final int DOCU_VERSION = 1;

    public static final int TILE_ISO_XLEN = 48;
    public static final int TILE_ISO_XLEN2 = 24;
    public static final int TILE_ISO_YLEN = 23;
    public static final int TILE_ISO_YLEN2 = 12;

    public static final int TILE_EDIT_MONSTER = 1;
    public static final int TILE_EDIT_EXIT = 2;
    public static final int TILE_EDIT_BACKGROUND = 4;
    public static final int TILE_EDIT_DOOR = 8;
    public static final int TILE_EDIT_WALL = 16;
    public static final int TILE_EDIT_EQUIP = 32;
    public static final int TILE_EDIT_TREASURE = 64;
    public static final int TILE_EDIT_CONNECTED = 128;
    public static final int TILE_EDIT_NONE = 0xffff+1; // special case

    public static final int MAP_LEVEL_Z = 16;

    public static final int TILE_PATH_NORTH = 0;
    public static final int TILE_PATH_EAST = 1;
    public static final int TILE_PATH_SOUTH = 2;
    public static final int TILE_PATH_WEST = 3;
    public static final int TILE_PATH_NORTHEAST = 4;
    public static final int TILE_PATH_SOUTHEAST = 5;
    public static final int TILE_PATH_SOUTHWEST = 6;
    public static final int TILE_PATH_NORTHWEST = 7;

    /**
     * Very important: When isoView is true, whole editor is configured to display ISO maps.
     *                 When false, the editor is configured to display rectangular CF maps.
     */
    public static final boolean isoView = true;

    public static final String APP_NAME = "Atrinik Editor 0.1"; // application name
    public static final String APP_WINDOW_TITLE = "Atrinik Map & Arch Editor"; // application main window title
    public static final String APP_SETTINGS_DIR = ".cfeditor"; // name of directory with settings file

    /**
     * The amount of space to be left between dialog buttons.
     */
    public static final int SPACE_BETWEEN_BUTTONS = 4;

    /**
     * The amount of space to be left between dialog button groups.
     */
    public static final int SPACE_BETWEEN_BUTTON_GROUPS = 7;

    /**
     * The amount of space to be left between dialog buttons and the
     * rest of the dialog.
     */
    public static final int SPACE_BETWEEN_BUTTON_AREA_AND_MAIN_DIALOG = 7;

    /**
     * The height of rigid area between the two tab-panes on
     * the pickmap- and arch-panel.
     */
    public static final int SPACE_PICKARCH_TOP = 10;

    /**
     * The amount of space to be left around the dialog borders.
     */
    public static final int DIALOG_INSETS = 4;

    // two mouseclicks within 'DOUBLECLICK_MS' millisecs are considered a doubleclick
    public static final int DOUBLECLICK_MS = 300;

    // defaults for new maps
    public static final String DEF_MAPFNAME = "<new map>";  // default file name
    public static final int DEF_MAPSIZE = 10;   // default map size (both lenght & width)
    public static final int DEF_PICKMAP_WIDTH = 20;  // default width for pickmaps
    public static final int DEF_PICKMAP_HEIGHT = 20; // default height for pickmaps

    // The directory that contains the common-use icons.
    public static final String ICON_DIR = "resource"+File.separator+"icons";

    // The directory that contains the system icons.
    public static final String SYSTEM_DIR = "resource"+File.separator+"system";

    // The directory that contains all (html) helpfiles.
    public static final String HELP_DIR = "resource"+File.separator+"HelpFiles";

    // The directory that contains all pickmaps.
    // public static final String PICKMAP_DIR = "resource"+File.separator+"pickmaps";
    public static final String PICKMAP_DIR = "dev"+File.separator+"editor"+File.separator+"pickmaps";

    // The directory that contains all configuration files
    // (careful - if you change this, check directory creation for file writing)
    // public static final String CONFIG_DIR = "resource"+File.separator+"conf";
    public static final String CONFIG_DIR = "dev"+File.separator+"editor"+File.separator+"conf";

    // name of the configuration files:
    public static final String SPELL_FILE = "spells.xml";       // spell-numbers
    public static final String TYPENR_FILE = "typenumbers.xml"; // type-numbers
    public static final String TYPEDEF_FILE = "types.xml";      // type-definitions
    public static final String ARCHDEF_FILE = "archdef.dat";    // position-data of multiparts

    // name of the arch resource files (these get read and written in the arch dir)
    public static final String ARCH_FILE = "archetypes";        // file with all arches
    public static final String PNG_FILE = "atrinik.0";         // file with all pngs
    public static final String BMAPS_FILE = "bmaps";            // file with list of face names
    public static final String TREASURES_FILE = "treasures";    // treasurelists file
    public static final String PYTHONMENU_FILE = "cfpython_menu.def"; // python menu definitions
    public static final String ARTIFACTS_FILE = "artifacts";    // file with artifact definitions

    // Application image definitions
    public static final String STARTUP_IMAGE = "CFIntro.gif";

    // Background Color (for the Panels)
    public static final java.awt.Color BG_COLOR = new java.awt.Color(100, 219, 169);

    // color for float/int values (AttribDialog)
    public static final java.awt.Color FLOAT_COLOR = new java.awt.Color(19, 134, 0);
    public static final java.awt.Color INT_COLOR = new java.awt.Color(74, 70, 156);

    // application icon definitions (icon-dir)
    public static final String APP_ICON             = "CFIcon.gif";
    public static final String NEW_LEVEL_ICON       = "NewLevelIcon.gif";
    public static final String OPEN_LEVEL_ICON      = "OpenLevelIcon.gif";
    public static final String SAVE_LEVEL_ICON      = "SaveLevelIcon.gif";
    public static final String SAVE_LEVEL_AS_ICON   = "SaveLevelAsIcon.gif";
    public static final String UNDO_ICON            = "UndoIcon.gif";
    public static final String REDO_ICON            = "RedoIcon.gif";
    public static final String REVERT_ICON          = "RevertIcon.gif";
    public static final String NEXT_WINDOW_ICON     = "NextWindowIcon.gif";
    public static final String PREVIOUS_WINDOW_ICON = "PrevWindowIcon.gif";

    public static final String MOVE_UP_ICON = "MoveUp.gif";
    public static final String MOVE_DOWN_ICON = "MoveDown.gif";

    public static final String DIRECTION_1_ICON = "Dir1.gif";
    public static final String DIRECTION_2_ICON = "Dir2.gif";
    public static final String DIRECTION_3_ICON = "Dir3.gif";
    public static final String DIRECTION_4_ICON = "Dir4.gif";
    public static final String DIRECTION_5_ICON = "Dir5.gif";
    public static final String DIRECTION_6_ICON = "Dir6.gif";
    public static final String DIRECTION_7_ICON = "Dir7.gif";
    public static final String DIRECTION_8_ICON = "Dir8.gif";

    public static final String EMPTY_SMALLICON            = "EmptySmallIcon.gif";
    public static final String NEW_LEVEL_SMALLICON        = "NewLevelSmallIcon.gif";
    public static final String OPEN_LEVEL_SMALLICON       = "OpenLevelSmallIcon.gif";
    public static final String SAVE_LEVEL_SMALLICON       = "SaveLevelSmallIcon.gif";
    public static final String SAVE_LEVEL_AS_SMALLICON    = "SaveLevelAsSmallIcon.gif";
    public static final String UNDO_SMALLICON             = "UndoSmallIcon.gif";
    public static final String REDO_SMALLICON             = "RedoSmallIcon.gif";
    public static final String NEXT_WINDOW_SMALLICON      = "NextWindowSmallIcon.gif";
    public static final String PREVIOUS_WINDOW_SMALLICON  = "PrevWindowSmallIcon.gif";
    public static final String IMPORT_TILESET_SMALLICON   = "ImportTilesetSmallIcon.gif";
    public static final String REMOVE_TILESET_SMALLICON   = "RemoveTilesetSmallIcon.gif";
    public static final String CREATE_IMAGE_SMALLICON     = "CreateImageSmallIcon.gif";

    // tile icons from the system dir:
    public static final String TILE_IGRID_TILE = "igridtile.png";
    public static final String TILE_ISEL_TILE  = "iseltile.png";
    public static final String TILE_IUNKNOWN   = "iunknown.png";
    public static final String TILE_INOFACE    = "inoface.png";
    public static final String TILE_INOARCH    = "inoarch.png";

    public static final String TILE_GRID_TILE  = "gridtile.png";
    public static final String TILE_SEL_TILE   = "seltile.png";
    public static final String TILE_UNKNOWN    = "unknown.png";
    public static final String TILE_NOFACE     = "noface.png";
    public static final String TILE_NOARCH     = "noarch.png";

    public static final String TILE_TREASURE    = "treasure_list.png";
    public static final String TILE_TREASUREONE = "treasureone_list.png";
    public static final String TILE_TR_YES      = "treasure_yes.png";
    public static final String TILE_TR_NO       = "treasure_no.png";

    // --- misc. constants ---
    // directions (don't change the values. see tile_path[] in MapArchObject)
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int NE = 4;
    public static final int SE = 5;
    public static final int SW = 6;
    public static final int NW = 7;
}
