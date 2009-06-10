/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 * Copyright (C) 2001  Andreas Vogl
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keränen)
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

package cfeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import cfeditor.textedit.scripteditor.ScriptEditControl;

/**
 * <code>CMainMenu</code implements the main menu of the application.
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMainMenu extends JMenuBar
{
    /** Controller of this menu view. */
    private CMainControl m_control;

    private JMenu menu_file;
    private JMenu menu_edit;
    private JMenu menu_map;
    private JMenu menu_pickmaps;
    private JMenu menu_collect;
    private JMenu menu_analyze;
    private JMenu menu_script;
    private JMenu menu_view;
    private JMenu menu_help;
    private JMenu menu_window;
    private JMenu menu_tools;

    private JMenuItem m_new;
    private JMenuItem m_open;
    private JMenuItem m_close;
    private JMenuItem m_revert;
    private JMenuItem m_save;
    private JMenuItem m_saveAs;
    private JMenuItem m_createImg;
    private JMenuItem m_options;
    private JMenuItem m_exit;

    private JMenuItem m_collectArch;
    private JMenuItem m_spellC;
    private JMenuItem m_viewTreasure;

    private JMenuItem m_scriptMenu;
    private JMenuItem m_analyzeMenu;
    private JMenuItem m_helpMenu;
    private JMenuItem m_aboutMenu;

    private JCheckBoxMenuItem m_autojoin;
    private JCheckBoxMenuItem m_gridToggle;
    private JMenuItem m_properties;
    private JMenuItem m_enterExit;
    private JMenuItem m_enterNorth;
    private JMenuItem m_enterEast;
    private JMenuItem m_enterWest;
    private JMenuItem m_enterSouth;
    private JMenuItem m_enterSouthEast;
    private JMenuItem m_enterSouthWest;
    private JMenuItem m_enterNorthEast;
    private JMenuItem m_enterNorthWest;

    private JCheckBoxMenuItem m_lockPickmaps;
    private JMenuItem m_newPickmap;
    private JMenuItem m_deletePickmap;
    private JMenuItem m_loadPickmap;
    private JMenuItem m_savePickmap;
    private JMenuItem m_revertPickmap;

    private JMenuItem m_undo;
    private JMenuItem m_redo;
    private JMenuItem m_clear;
    private JMenuItem m_cut;
    private JMenuItem m_copy;
    private JMenuItem m_paste;
    private JMenuItem m_replace;
    private JMenuItem m_fill_above;
    private JMenuItem m_fill_below;
    private JMenuItem m_fill_r_above;
    private JMenuItem m_fill_r_below;

    private JMenuItem m_font;
    private JCheckBoxMenuItem se_monster;
    private JCheckBoxMenuItem se_exit;
    private JCheckBoxMenuItem se_background;
    private JCheckBoxMenuItem se_door;
    private JCheckBoxMenuItem se_wall;
    private JCheckBoxMenuItem se_equip;
    private JCheckBoxMenuItem se_treasure;
    private JCheckBoxMenuItem se_connected;
    private JMenuItem m_show_all;

    private JMenuItem m_newWindow;
    private JMenuItem m_closeAll;

    private JMenuItem m_newscript;
    private JMenuItem m_editscript;
    private JMenuItem m_zoom;

    /**
     * Constructs a main menu.
     *@param control The controller of this main menu.
     */
    CMainMenu( CMainControl control )
    {
        m_control = control;  // reference to main control
        buildFileMenu();
        buildEditMenu();
        buildMapMenu();
        buildPickmapsMenu();
        buildResourceMenu();
        buildScriptMenu();
        buildToolsMenu();
        buildAnalyzeMenu();
        buildViewMenu();
        buildWindowMenu();
        buildHelpMenu();
    }

    /**
     * "File"-Menu
     */
    private void buildFileMenu()
    {
        menu_file = new JMenu("File");
        menu_file.setMnemonic('F');

        m_new = new JMenuItem("New...");
        m_new.setIcon( CGUIUtils.getIcon( IGUIConstants.NEW_LEVEL_SMALLICON ) );
        m_new.setMnemonic('N');
        m_new.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_N, Event.CTRL_MASK ) );
        m_new.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.newLevelWanted();
            }
        });
        menu_file.add(m_new);

        m_open = new JMenuItem("Open...");
        m_open.setIcon( CGUIUtils.getIcon( IGUIConstants.OPEN_LEVEL_SMALLICON ) );
        m_open.setMnemonic('O');
        m_open.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_O, Event.CTRL_MASK ) );
        m_open.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
               m_control.openFileWanted();
            }
        });
        menu_file.add( m_open );

        m_close = new JMenuItem( "Close" );
        m_close.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_close.setMnemonic( 'C' );
        m_close.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_F4, Event.CTRL_MASK ) );
        m_close.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                 m_control.closeCurrentLevelWanted();
            }
        });
        menu_file.add( m_close );

        menu_file.addSeparator();

        m_save = new JMenuItem("Save");
        m_save.setIcon( CGUIUtils.getIcon( IGUIConstants.SAVE_LEVEL_SMALLICON ) );
        m_save.setMnemonic('S');
        m_save.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_S, Event.CTRL_MASK ) );
        m_save.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                  m_control.saveCurrentLevelWanted();
            }
        });
        menu_file.add(m_save);

        m_saveAs = new JMenuItem("Save As...");
        m_saveAs.setIcon( CGUIUtils.getIcon( IGUIConstants.SAVE_LEVEL_AS_SMALLICON ) );
        m_saveAs.setMnemonic('A');
        m_saveAs.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_A, Event.CTRL_MASK ) );
        m_saveAs.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.saveCurrentLevelAsWanted();
            }
        });
        menu_file.add(m_saveAs);

        m_revert = new JMenuItem( "Revert" );
        m_revert.setMnemonic( 'R' );
        m_revert.setIcon( CGUIUtils.getIcon( IGUIConstants.REVERT_ICON ) );
        m_revert.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                 m_control.revertCurrentLevelWanted();
            }
        });
        menu_file.add( m_revert );

        m_createImg = new JMenuItem( "Create Image" );
        m_createImg.setMnemonic( 'I' );
        m_createImg.setIcon( CGUIUtils.getIcon( IGUIConstants.CREATE_IMAGE_SMALLICON ) );
        m_createImg.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                 m_control.createImageWanted();
            }
        });
        menu_file.add( m_createImg );
        menu_file.addSeparator();

        m_options = new JMenuItem("Options...");
        m_options.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_options.setMnemonic('O');
        m_options.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, Event.ALT_MASK ) );
        m_options.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.OptionsWanted();
            }
        });
        menu_file.add( m_options );

        menu_file.addSeparator();

        m_exit = new JMenuItem("Exit");
        m_exit.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_exit.setMnemonic('X');
        m_exit.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.exitWanted();
            }
        });
        menu_file.add(m_exit);
        add(menu_file);
    }

    /**
     * "Edit"-Menu
     */
    private void buildEditMenu() {
        menu_edit = new JMenu("Edit");
        menu_edit.setMnemonic('E');

        // Undo:
        m_undo = new JMenuItem("Undo");
        m_undo.setIcon( CGUIUtils.getIcon( IGUIConstants.UNDO_SMALLICON ) );
        m_undo.setMnemonic('U');
        m_undo.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_Z, Event.CTRL_MASK ) );
        m_undo.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                // m_control.undoWanted();
            }
        });
        menu_edit.add(m_undo);

        // Redo:
        m_redo = new JMenuItem("Redo");
        m_redo.setIcon( CGUIUtils.getIcon( IGUIConstants.REDO_SMALLICON ) );
        m_redo.setMnemonic('R');
        m_redo.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_Z, Event.CTRL_MASK | Event.SHIFT_MASK ) );
        m_redo.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                // m_control.redoWanted();
            }
        });
        menu_edit.add(m_redo);

        menu_edit.addSeparator();

        // Clear:
        m_clear = new JMenuItem("Clear");
        m_clear.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK));
        m_clear.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.clearWanted();
            }
        });
        menu_edit.add(m_clear);

        // Cut:
        m_cut = new JMenuItem("Cut");
        m_cut.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
        m_cut.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.cutWanted();
            }
        });
        menu_edit.add(m_cut);

        // Copy:
        m_copy = new JMenuItem("Copy");
        m_copy.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
        m_copy.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
              m_control.copyWanted();
            }
        });
        menu_edit.add(m_copy);

        // Paste:
        m_paste = new JMenuItem("Paste");
        m_paste.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
        m_paste.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
              m_control.pasteWanted();
            }
        });
        menu_edit.add(m_paste);

        menu_edit.addSeparator();

        // Replace:
        m_replace = new JMenuItem("Replace");
        m_replace.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));
        m_replace.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                m_control.replaceWanted();
            }
        });
        menu_edit.add(m_replace);

        // Fill:
        m_fill_above = new JMenuItem("Fill Above");
        m_fill_above.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_fill_above.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
        m_fill_above.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                m_control.fillWanted(false);
            }
        });
        menu_edit.add(m_fill_above);

        // Fill:
        m_fill_below = new JMenuItem("Fill Below");
        m_fill_below.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_fill_below.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK | Event.SHIFT_MASK));
        m_fill_below.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                m_control.fillWanted(true);
            }
        });
        menu_edit.add(m_fill_below);

        // Fill:
        m_fill_r_above = new JMenuItem("Random Fill Above");
        m_fill_r_above.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_fill_r_above.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                m_control.fillRandomWanted(false);
            }
        });
        menu_edit.add(m_fill_r_above);

        m_fill_r_below = new JMenuItem("Random Fill Below");
        m_fill_r_below.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_fill_r_below.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                m_control.fillRandomWanted(true);
            }
        });
        menu_edit.add(m_fill_r_below);


        add(menu_edit);
    }

    private void buildViewMenu() {
        menu_view = new JMenu("View");
        menu_view.setMnemonic('V');

        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        UIManager.LookAndFeelInfo[] aInfos = UIManager.getInstalledLookAndFeels();
        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem selectedMenuItem = null;
        String strSelectedLNFName =
            settings.getProperty( CMainView.SELECTED_LNF_KEY,
                UIManager.getCrossPlatformLookAndFeelClassName() );
        for ( int i = 0; i < aInfos.length; i++ )
        {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem( aInfos[i].getName() );
            m_control.setBoldFont(menuItem);
            if ( strSelectedLNFName.compareTo( aInfos[i].getClassName() ) == 0 )
            {
                menuItem.doClick();
            }

            menuItem.addActionListener( new CLNFActionListener( aInfos[i].getClassName()  ) );
            menu_view.add( menuItem );
            group.add( menuItem );
        }

        menu_view.addSeparator();

        // menu: choose font
        m_font = new JMenuItem("Change Font");
        m_font.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // open font-dialog and let user choose new font
                Font newfont = JFontChooser.showDialog(m_control.getMainView(), "Choose Font",
                                                       (m_control.getPlainFont()==null)?m_control.getMainView().getFont():m_control.getPlainFont());
                if (newfont != null && !(newfont.getName().equals(m_control.getMainView().getFont().getName())
                    && newfont.getSize() == m_control.getMainView().getFont().getSize())) {
                    // set new font
                    m_control.newPlainFont(new Font(newfont.getName(), Font.PLAIN, newfont.getSize()));
                    m_control.newBoldFont(new Font(newfont.getName(), Font.BOLD, newfont.getSize()));

                    JFontChooser.setUIFont(m_control.getPlainFont());

                    // update fonts
                    m_control.getMainView().updateMapArchPanelFont();
                    updateFont(true);
                    m_control.getMainView().updateStatusBarFont();
                    m_control.getMainView().updateArchPanelFont();
                    CFTreasureListTree.getInstance().updateFont();
                    ScriptEditControl.getInstance().updateGlobalFont();
                }
                else {
                    // update fonts
                    m_control.newPlainFont(JFontChooser.default_font);
                    m_control.newBoldFont(new Font(JFontChooser.default_font.getFontName(), Font.BOLD, JFontChooser.default_font.getSize()));
                    JFontChooser.setUIFont(m_control.getPlainFont());

                    m_control.getMainView().updateMapArchPanelFont();
                    updateFont(true);
                    m_control.getMainView().updateStatusBarFont();
                    m_control.getMainView().updateArchPanelFont();
                    CFTreasureListTree.getInstance().updateFont();
                    ScriptEditControl.getInstance().updateGlobalFont();

                    // revert to default fonts
                    m_control.newPlainFont(null);
                    m_control.newBoldFont(null);
                    CSettings.getInstance(IGUIConstants.APP_NAME).clearProperty(CMainControl.USE_FONT);
                }

                m_control.getMainView().refreshMapArchPanel();
                m_control.getMainView().RefreshMapTileList();
            }
        });
        menu_view.add(m_font);

        menu_view.addSeparator();

        se_monster = new JCheckBoxMenuItem("Show Monsters");
        se_monster.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_MONSTER));
        se_monster.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_monster.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_MONSTER);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_MONSTER);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_monster);

        se_exit = new JCheckBoxMenuItem("Show Exits");
        se_exit.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_EXIT));
        se_exit.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_exit.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_EXIT);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_EXIT);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_exit);

        se_background = new JCheckBoxMenuItem("Show Background");
        se_background.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_BACKGROUND));
        se_background.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_background.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_BACKGROUND);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_BACKGROUND);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_background);

        se_door = new JCheckBoxMenuItem("Show Doors & Keys");
        se_door.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_DOOR));
        se_door.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_door.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_DOOR);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_DOOR);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_door);
        se_wall = new JCheckBoxMenuItem("Show Wall");
        se_wall.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_WALL));
        se_wall.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_wall.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_WALL);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_WALL);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_wall);
        se_equip = new JCheckBoxMenuItem("Show Equipment");
        se_equip.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_EQUIP));
        se_equip.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_equip.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_EQUIP);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_EQUIP);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_equip);
        se_treasure = new JCheckBoxMenuItem("Show Treasure");
        se_treasure.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_TREASURE));
        se_treasure.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_treasure.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_TREASURE);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_TREASURE);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_treasure);
        se_connected = new JCheckBoxMenuItem("Show Connected");
        se_connected.setSelected(m_control.isTileEdit(IGUIConstants.TILE_EDIT_CONNECTED));
        se_connected.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(se_connected.isSelected())
                    m_control.select_edittype(IGUIConstants.TILE_EDIT_CONNECTED);
                else
                    m_control.unsetTileEdit(IGUIConstants.TILE_EDIT_CONNECTED);
                m_control.refreshCurrentMap();
            }
        } );
        menu_view.add(se_connected);
        menu_view.addSeparator();
        m_show_all = new JMenuItem("Reset View");
        m_show_all.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                // set tileEdit to zero (-> show all)
                m_control.tileEdit = 0;
                se_monster.setState(false);
                se_exit.setState(false);
                se_background.setState(false);
                se_door.setState(false);
                se_wall.setState(false);
                se_equip.setState(false);
                se_treasure.setState(false);
                se_connected.setState(false);
                m_control.refreshCurrentMap();  // redraw map
            }
        });
        menu_view.add(m_show_all);

        add(menu_view);
    }

    private void buildMapMenu() {
        menu_map = new JMenu("Map");
        menu_map.setMnemonic('M');

        m_autojoin = new JCheckBoxMenuItem("Auto-Joining");
        m_autojoin.setMnemonic('A');
        m_autojoin.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_J, Event.CTRL_MASK ) );
        m_autojoin.setSelected(m_control.getAutojoin());
        m_autojoin.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // toggle autojoining state on/off
                m_control.setAutojoin(m_autojoin.isSelected());
            }
        } );
        menu_map.add(m_autojoin);
        menu_map.addSeparator();

        m_gridToggle = new JCheckBoxMenuItem("Show Grid");
        m_gridToggle.setMnemonic('G');
        m_gridToggle.setSelected(m_control.isGridVisible());

        m_gridToggle.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.setGridVisibility( m_gridToggle.isSelected() );
            }
        } );
        menu_map.add(m_gridToggle);

        m_enterExit = new JMenuItem("Enter Exit");
        m_enterExit.setMnemonic('E');
        m_enterExit.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD5, Event.CTRL_MASK ) );
        m_enterExit.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterExitWanted();
            }
        });
        menu_map.add(m_enterExit);
        menu_map.addSeparator();

        m_enterNorth = new JMenuItem("Enter North Map");
        m_enterNorth.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD8, Event.CTRL_MASK ));
        m_enterNorth.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.NORTH);
            }
        });
        menu_map.add(m_enterNorth);

        m_enterEast = new JMenuItem("Enter East Map");
        m_enterEast.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, Event.CTRL_MASK ) );
        m_enterEast.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.EAST);
            }
        });
        menu_map.add(m_enterEast);

        m_enterSouth = new JMenuItem("Enter South Map");
        m_enterSouth.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD2, Event.CTRL_MASK ) );
        m_enterSouth.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.SOUTH);
            }
        });
        menu_map.add(m_enterSouth);

        m_enterWest = new JMenuItem("Enter West Map");
        m_enterWest.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD4, Event.CTRL_MASK ) );
        m_enterWest.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.WEST);
            }
        });
        menu_map.add(m_enterWest);

        m_enterNorthEast = new JMenuItem("Enter Northeast Map");
        m_enterNorthEast.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD9, Event.CTRL_MASK ) );
        m_enterNorthEast.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.NE);
            }
        });
        menu_map.add(m_enterNorthEast);

        m_enterSouthEast = new JMenuItem("Enter Southeast Map");
        m_enterSouthEast.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD3, Event.CTRL_MASK) );
        m_enterSouthEast.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.SE);
            }
        });
        menu_map.add(m_enterSouthEast);

        m_enterSouthWest = new JMenuItem("Enter Southwest Map");
        m_enterSouthWest.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_NUMPAD1, Event.CTRL_MASK ) );
        m_enterSouthWest.addActionListener( new ActionListener()
        {
          public void actionPerformed(ActionEvent event)
          {
            m_control.enterTileWanted(IGUIConstants.SW);
          }
        });
        menu_map.add(m_enterSouthWest);

        m_enterNorthWest = new JMenuItem("Enter Northwest Map");
        m_enterNorthWest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, Event.CTRL_MASK ) );
        m_enterNorthWest.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.enterTileWanted(IGUIConstants.NW);
            }
        });
        menu_map.add(m_enterNorthWest);


        menu_map.addSeparator();
        m_properties = new JMenuItem("Map Properties");
        m_properties.setMnemonic('P');
        m_properties.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_M, Event.CTRL_MASK ) );
        m_properties.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.mapPropertiesWanted();
            }
        });
        menu_map.add( m_properties );
        add(menu_map);
    }

    private void buildPickmapsMenu() {
        menu_pickmaps = new JMenu("Pickmaps");
        menu_pickmaps.setMnemonic('P');

        m_lockPickmaps = new JCheckBoxMenuItem("Lock All Pickmaps");
        m_lockPickmaps.setMnemonic('L');
        m_lockPickmaps.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_lockPickmaps.setSelected(new Boolean(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                          CMainControl.PICKMAPS_LOCKED, "false")).booleanValue());
        m_lockPickmaps.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // lock pickmaps
                m_control.setPickmapsLocked(m_lockPickmaps.isSelected());
                m_newPickmap.setEnabled(!m_lockPickmaps.isSelected());

                boolean isArchLoadComplete = ArchObjectStack.getLoadStatus() == ArchObjectStack.IS_COMPLETE;
                if (isArchLoadComplete) {
                    m_deletePickmap.setEnabled(!m_lockPickmaps.isSelected());
                    m_loadPickmap.setEnabled(!m_lockPickmaps.isSelected());
                    m_savePickmap.setEnabled(!m_lockPickmaps.isSelected());
                    m_revertPickmap.setEnabled(!m_lockPickmaps.isSelected());
                }
                else {
                    m_deletePickmap.setEnabled(false);
                    m_loadPickmap.setEnabled(false);
                    m_savePickmap.setEnabled(false);
                    m_revertPickmap.setEnabled(false);
                }

                // store this in the settings
                CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(CMainControl.PICKMAPS_LOCKED, m_lockPickmaps.isSelected()?"true":"false" );
            }
        } );
        menu_pickmaps.add(m_lockPickmaps);
        menu_pickmaps.addSeparator();

        m_newPickmap = new JMenuItem("Add New Pickmap");
        m_newPickmap.setMnemonic('N');
        m_newPickmap.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_newPickmap.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // create new pickmap
                m_control.newPickmapWanted();
            }
        });
        menu_pickmaps.add( m_newPickmap );

        m_deletePickmap = new JMenuItem("Close Active Pickmap");
        m_deletePickmap.setMnemonic('C');
        m_deletePickmap.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_deletePickmap.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // close pickmap
                m_control.closeActivePickmapWanted();
            }
        });
        menu_pickmaps.add( m_deletePickmap );
        menu_pickmaps.addSeparator();

        m_loadPickmap = new JMenuItem("Open Active Pickmap as Map");
        m_loadPickmap.setMnemonic('O');
        m_loadPickmap.setIcon( CGUIUtils.getIcon( IGUIConstants.EMPTY_SMALLICON ) );
        m_loadPickmap.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // load pickmap as normal map
                m_control.openActivePickmapAsMapWanted();
            }
        });
        menu_pickmaps.add( m_loadPickmap );

        m_savePickmap = new JMenuItem("Save Active Pickmap");
        m_savePickmap.setMnemonic('S');
        m_savePickmap.setIcon( CGUIUtils.getIcon( IGUIConstants.SAVE_LEVEL_SMALLICON ) );
        m_savePickmap.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // save pickmap
                m_control.saveActivePickmapWanted();
            }
        });
        menu_pickmaps.add( m_savePickmap );

        m_revertPickmap = new JMenuItem("Revert Active Pickmap");
        m_revertPickmap.setMnemonic('R');
        m_revertPickmap.setIcon( CGUIUtils.getIcon( IGUIConstants.REVERT_ICON ) );
        m_revertPickmap.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // revert pickmap
                m_control.revertActivePickmapWanted();
            }
        });
        menu_pickmaps.add( m_revertPickmap );

        add(menu_pickmaps);
    }

    private void buildResourceMenu() {
        menu_collect = new JMenu("Resources");
        menu_collect.setMnemonic('R');

        m_collectArch = new JMenuItem((IGUIConstants.isoView ? "Collect Arches" : "Collect CF Arches"));
        m_collectArch.setMnemonic('A');
        m_collectArch.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                menu_collect.setPopupMenuVisible(false);
                m_control.getMainView().update(m_control.getMainView().getGraphics());
                m_control.collectCFArches();
            }
        });
        menu_collect.add(m_collectArch);

        m_spellC = new JMenuItem("Collect Spells");
        m_spellC.setMnemonic('S');
        m_spellC.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                m_control.typelist.importSpellsWanted(m_control);
            }
        });
        menu_collect.add(m_spellC);
        menu_collect.addSeparator();

        m_viewTreasure = new JMenuItem("View Treasurelists");
        m_viewTreasure.setMnemonic('T');
        m_viewTreasure.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // show the treasurelist tree
                CFTreasureListTree.getInstance().showDialog();
            }
        });
        menu_collect.add(m_viewTreasure);

        add( menu_collect );
    }

    private void buildScriptMenu() {
        menu_script = new JMenu("Scriptfire");
        menu_script.setMnemonic('S');

        m_scriptMenu = new JMenuItem("under construction...");
        m_scriptMenu.setMnemonic('N');
        m_scriptMenu.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
            }
        });
        menu_script.add( m_scriptMenu );
        //add( menu_script );
    }

    private void buildAnalyzeMenu() {
        menu_analyze = new JMenu("Analyze");
        menu_analyze.setMnemonic('y');

        m_analyzeMenu = new JMenuItem("under construction...");
        //m_analyzeMenu.setMnemonic('A');
        m_analyzeMenu.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
            }
        });
        menu_analyze.add( m_analyzeMenu );
        add( menu_analyze );
    }
    /**
     * Create the tools menu
     */
    private void buildToolsMenu()
   {
       menu_tools = new JMenu("Tools");
       m_newscript = new JMenuItem("New Script");
       m_newscript.setMnemonic( 'P');
       m_newscript.addActionListener( new ActionListener()
       {
         public void actionPerformed(ActionEvent event)
         {
           ScriptEditControl.getInstance().openScriptNew();
         }
       });
       menu_tools.add(m_newscript);

       m_editscript = new JMenuItem("Edit Script");
       m_editscript.addActionListener( new ActionListener()
       {
         public void actionPerformed(ActionEvent event)
         {
           m_control.openFileWanted();
         }
       });
       menu_tools.add(m_editscript);

       menu_tools.addSeparator();
       m_zoom = new JMenuItem("Zoom");
       m_zoom.addActionListener( new ActionListener()
       {
         public void actionPerformed(ActionEvent event)
         {
           m_control.doZoom();
         }
       });
       menu_tools.add(m_zoom);
       add(menu_tools);
   }

    /**
     * Create the help-about window with the credits
     */
    private void buildHelpMenu() {
        menu_help = new JMenu("Help");
        menu_help.setMnemonic('H');

        m_helpMenu = new JMenuItem("Online Help");
        m_helpMenu.setMnemonic('H');
        m_helpMenu.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.openHelpWindow();
            }
        });
        menu_help.add( m_helpMenu );
        menu_help.addSeparator();
        m_aboutMenu = new JMenuItem("About...");
        m_aboutMenu.setMnemonic('A');
        m_aboutMenu.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.showMessage("About "+IGUIConstants.APP_NAME,
                                      " Version "+IGUIConstants.VERSION+"\n (c) 2001  Michael Toennies\n" +
                                      "      Andreas Vogl\n" + "      Peter Plischewsky\n" +"      Gecko\n");
            }
        });
        menu_help.add( m_aboutMenu );

        add(menu_help);
    }

    private void buildWindowMenu() {
        menu_window = new JMenu("Window");
        menu_window.setMnemonic('W');

        m_newWindow = new JMenuItem("New Window");
        m_newWindow.setMnemonic('N');
        m_newWindow.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_W, Event.SHIFT_MASK ) );
        m_newWindow.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
         //       m_control.newWindowWanted();
            }
        } );
        menu_window.add(m_newWindow);

        menu_window.addSeparator();

        m_closeAll = new JMenuItem("Close All");
        m_closeAll.setMnemonic('A');
        m_closeAll.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.closeAllLevelsWanted();
            }
        } );
        menu_window.add(m_closeAll);

        add(menu_window);
    }

    /**
     * Rebuilds the window menu.
     */
    private void rebuildWindowMenu()
    {
        menu_window.removeAll();
        menu_window.add( m_newWindow );
        menu_window.addSeparator();
        menu_window.add( m_closeAll );

        int index = 1;
        Enumeration windows = m_control.getAllLevelWindows();
        if ( windows != null )
        {
            menu_window.addSeparator();
            while( windows.hasMoreElements() )
            {
                final CMapViewIFrame view = ( CMapViewIFrame ) windows.nextElement();
                JMenuItem menuItem = null;
                if ( index < 10 )
                {
                    menuItem = new JMenuItem( ""+index+" "+view.getTitle() );
                    menuItem.setMnemonic('0'+index);
                }
                else
                {
                    menuItem = new JMenuItem( "  "+view.getTitle() );
                }
                index++;
                menuItem.addActionListener( new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        m_control.setCurrentLevelView( view );
                    }
                } );
                menu_window.add( menuItem );
            }
        }
    }

    /**
     * allows external access to dis-/enable the file->revert menu
     * @param state   enable state of the revert menu
     */
    public void setRevertEnabled(boolean state) {
        m_revert.setEnabled(state);
    }

    /**
     * allows external access to dis-/enable the menus related to active pickmap
     * @param state   true when there is an active pickmap
     */
    public void setActivePickmapsEnabled(boolean state) {
        if (!m_control.isPickmapsLocked()) {
            m_deletePickmap.setEnabled(state);
            m_loadPickmap.setEnabled(state);
            m_revertPickmap.setEnabled(state);
            m_savePickmap.setEnabled(state);
        }
    }

    /**
     * Refreshes the state of items in this toolbar.
     */
    public void refresh() {
        boolean isArchLoadComplete = ArchObjectStack.getLoadStatus() == ArchObjectStack.IS_COMPLETE;

        m_undo.setEnabled( false );
        m_redo.setEnabled( false );

        m_spellC.setEnabled( true );
        m_scriptMenu.setEnabled( false );
        m_analyzeMenu.setEnabled( false );
        m_newWindow.setEnabled( false );
        m_enterExit.setEnabled( false );

        // autojoining is only allowed if the definitions file is available
        if (m_control.joinlist != null)
            m_autojoin.setEnabled(true);
        else
            m_autojoin.setEnabled(false);

        // collect arches is only allowed if we run from individual archfiles
        if (!isArchLoadComplete || ArchObjectStack.isLoadedFromArchive())
            m_collectArch.setEnabled(false);
        else
            m_collectArch.setEnabled(true);

        if (isArchLoadComplete) {
            m_viewTreasure.setEnabled(true);
            if (!m_control.isPickmapsLocked() && m_control.getMainView().isPickmapActive()) {
                m_deletePickmap.setEnabled(true);
                m_loadPickmap.setEnabled(true);
                m_revertPickmap.setEnabled(true);
                m_savePickmap.setEnabled(true);
            }
            else {
                m_deletePickmap.setEnabled(false);
                m_loadPickmap.setEnabled(false);
                m_revertPickmap.setEnabled(false);
                m_savePickmap.setEnabled(false);
            }
        }
        else {
            m_viewTreasure.setEnabled(false);
            m_deletePickmap.setEnabled(false);
            m_loadPickmap.setEnabled(false);
            m_revertPickmap.setEnabled(false);
            m_savePickmap.setEnabled(false);
        }
        if (m_control.isPickmapsLocked())
            m_newPickmap.setEnabled(false);
        else
            m_newPickmap.setEnabled(true);

        // is there a valid open map view?
        if(m_control.m_currentMap == null)
        {
            m_save.setEnabled(false);
            m_close.setEnabled(false);
            m_saveAs.setEnabled(false);
            m_revert.setEnabled(false);
            m_createImg.setEnabled(false);
            m_properties.setEnabled(false);
            m_gridToggle.setEnabled(false);
            m_clear.setEnabled(false);
            m_cut.setEnabled(false);
            m_copy.setEnabled(false);
            m_paste.setEnabled(false);
            m_replace.setEnabled(false);
            m_fill_above.setEnabled(false);
            m_fill_below.setEnabled(false);
            m_fill_r_above.setEnabled(false);
            m_fill_r_below.setEnabled(false);
            m_enterExit.setEnabled(false);
            m_enterNorth.setEnabled(false);
            m_enterSouth.setEnabled(false);
            m_enterEast.setEnabled(false);
            m_enterWest.setEnabled(false);
            m_enterSouthWest.setEnabled(false);
            m_enterNorthWest.setEnabled(false);
            m_enterNorthEast.setEnabled(false);
            m_enterSouthEast.setEnabled(false);
        }
        else // yes...
        {
            m_gridToggle.setEnabled(true);
            m_createImg.setEnabled(true);
            boolean fLevelEdited = m_control.isLevelEdited();
            m_close.setEnabled( fLevelEdited );
            m_save.setEnabled( m_control.isPlainSaveEnabled() );
            m_saveAs.setEnabled( fLevelEdited );
            m_properties.setEnabled(true);
            m_replace.setEnabled(true);

            m_gridToggle.setSelected(m_control.isGridVisible() );

            // revert is only allowed when map has been modified
            if (m_control.m_currentMap.isLevelChanged())
                m_revert.setEnabled(true);
            else
                m_revert.setEnabled(false);

            // enter north/east/south/west map is only allowed when such tile-paths exist in the maparch
            JMenuItem tmenu = null;
            for (int direction = 0; direction < 8; direction++) {
                switch (direction) {
                    case IGUIConstants.NORTH: tmenu = m_enterNorth; break;
                    case IGUIConstants.EAST: tmenu = m_enterEast; break;
                    case IGUIConstants.SOUTH: tmenu = m_enterSouth; break;
                    case IGUIConstants.WEST: tmenu = m_enterWest; break;
                    case IGUIConstants.NE: tmenu = m_enterNorthEast; break;
                    case IGUIConstants.SE: tmenu = m_enterSouthEast; break;
                    case IGUIConstants.SW: tmenu = m_enterSouthWest; break;
                    case IGUIConstants.NW: tmenu = m_enterNorthWest; break;
                }
                if (m_control.m_currentMap.getMapTilePath(direction) != null &&
                    m_control.m_currentMap.getMapTilePath(direction).length() > 0)
                    tmenu.setEnabled(true);
                else
                    tmenu.setEnabled(false);
            }

            // Cut/Copy only when there is a highlighted tile selection
            if (m_control.m_currentMap.m_view.isHighlight()) {
                m_clear.setEnabled(true);
                m_cut.setEnabled(true);
                m_copy.setEnabled(true);
                m_fill_above.setEnabled(true);
                m_fill_below.setEnabled(true);
                m_fill_r_above.setEnabled(true);
                m_fill_r_below.setEnabled(true);
                m_enterExit.setEnabled(true);

                if (m_control.isCopyBuffer_empty())
                    m_paste.setEnabled(false);
                else
                    m_paste.setEnabled(true);
            }
            else {
                m_clear.setEnabled(false);
                m_cut.setEnabled(false);
                m_copy.setEnabled(false);
                m_paste.setEnabled(false);
                m_fill_above.setEnabled(false);
                m_fill_below.setEnabled(false);
                m_fill_r_above.setEnabled(false);
                m_fill_r_below.setEnabled(false);
                m_enterExit.setEnabled(false);
            }
        }

        rebuildWindowMenu();
    }

    /**
     * Redraws the whole menu with latest custom fonts
     * @param do_redraw    if true, menu is redrawn at the end
     */
    public void updateFont(boolean do_redraw)
    {
        m_control.setBoldFont(menu_file);
        m_control.setBoldFont(menu_edit);
        m_control.setBoldFont(menu_view);
        m_control.setBoldFont(menu_map);
        m_control.setBoldFont(menu_pickmaps);
        m_control.setBoldFont(menu_collect);
        m_control.setBoldFont(menu_analyze);
        m_control.setBoldFont(menu_script);
        m_control.setBoldFont(menu_help);
        m_control.setBoldFont(menu_window);

        m_control.setBoldFont(m_new);
        m_control.setBoldFont(m_open);
        m_control.setBoldFont(m_close);
        m_control.setBoldFont(m_save);
        m_control.setBoldFont(m_saveAs);
        m_control.setBoldFont(m_revert);
        m_control.setBoldFont(m_options);
        m_control.setBoldFont(m_exit);

        m_control.setBoldFont(m_newPickmap);
        m_control.setBoldFont(m_deletePickmap);
        m_control.setBoldFont(m_savePickmap);
        m_control.setBoldFont(m_revertPickmap);

        m_control.setBoldFont(m_collectArch);
        m_control.setBoldFont(m_spellC);
        m_control.setBoldFont(m_viewTreasure);

        m_control.setBoldFont(m_scriptMenu);
        m_control.setBoldFont(m_analyzeMenu);
        m_control.setBoldFont(m_helpMenu);
        m_control.setBoldFont(m_aboutMenu);

        m_control.setBoldFont(m_autojoin);
        m_control.setBoldFont(m_gridToggle);
        m_control.setBoldFont(m_properties);
        m_control.setBoldFont(m_enterExit);
        m_control.setBoldFont(m_enterNorth);
        m_control.setBoldFont(m_enterEast);
        m_control.setBoldFont(m_enterSouth);
        m_control.setBoldFont(m_enterWest);
        m_control.setBoldFont(m_enterNorthWest);
        m_control.setBoldFont(m_enterSouthWest);
        m_control.setBoldFont(m_enterNorthEast);
        m_control.setBoldFont(m_enterSouthEast);

        m_control.setBoldFont(m_undo);
        m_control.setBoldFont(m_redo);
        m_control.setBoldFont(m_clear);
        m_control.setBoldFont(m_cut);
        m_control.setBoldFont(m_copy);
        m_control.setBoldFont(m_paste);
        m_control.setBoldFont(m_replace);
        m_control.setBoldFont(m_fill_above);
        m_control.setBoldFont(m_fill_below);
        m_control.setBoldFont(m_fill_r_above);
        m_control.setBoldFont(m_fill_r_below);

        m_control.setBoldFont(m_font);
        m_control.setBoldFont(se_monster);
        m_control.setBoldFont(se_exit);
        m_control.setBoldFont(se_background);
        m_control.setBoldFont(se_door);
        m_control.setBoldFont(se_wall);
        m_control.setBoldFont(se_equip);
        m_control.setBoldFont(se_treasure);
        m_control.setBoldFont(se_connected);
        m_control.setBoldFont(m_show_all);

        m_control.setBoldFont(m_newWindow);
        m_control.setBoldFont(m_closeAll);

        if (do_redraw) refresh();
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        if (m_control.getPlainFont() != null) {
            settings.setProperty(CMainControl.USE_FONT, m_control.getPlainFont().getName()+"|"
                                 + m_control.getPlainFont().getSize());
        }
    }

    public class CLNFActionListener implements ActionListener
    {
        private String m_strClassName;

        public CLNFActionListener( String strClassName )
        {
            m_strClassName = strClassName;
        }

        public void actionPerformed(ActionEvent event)
        {
            m_control.setLookNFeel( m_strClassName );
            CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(
                CMainView.SELECTED_LNF_KEY, m_strClassName );
        }
    }
}
