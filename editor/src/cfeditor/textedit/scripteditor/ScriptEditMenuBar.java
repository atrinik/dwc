/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keranen)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import cfeditor.textedit.textarea.*;

/**
 * This class implements the MenuBar of the script editor.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ScriptEditMenuBar extends JMenuBar {
    private ScriptEditControl control; // controler object

    // File menu:
    private JMenu menu_file;
    private JMenuItem m_new;
    private JMenuItem m_open;
    private JMenuItem m_save_as;
    private JMenuItem m_save;
    private JMenuItem m_close;
    private JMenuItem m_close_all;

    // Edit menu:
    private JMenu menu_edit;
    private JMenuItem m_cut;
    private JMenuItem m_copy;
    private JMenuItem m_paste;
    private JMenuItem m_find;

    // Settings menu:
    private JMenu menu_settings;
    private JMenuItem m_font;
    private JMenuItem m_colors;

    // Help Menu:
    private JMenu menu_help;
    private JMenuItem m_onlineHelp;
    private JMenuItem m_about;

    /**
     * Constructor - Builds the MenuBar
     * @param control
     */
    public ScriptEditMenuBar(ScriptEditControl control) {
        this.control = control;  // reference to ScriptEditControl control
        buildFileMenu();
        buildEditMenu();
        buildSettingsMenu();
        buildHelpMenu();
        refresh();
    }

    /**
     * Build File menu
     */
    private void buildFileMenu() {
        menu_file = new JMenu("File");

        m_new = new JMenuItem("New Script");
        m_new.setMnemonic('N');
        m_new.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                  // open new script
                  control.openScriptNew();
            }
        });
        menu_file.add(m_new);

        m_open = new JMenuItem("Open");
        m_open.setMnemonic('O');
        m_open.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                  // open new script
                  control.openUserWanted();
            }
        });
        menu_file.add(m_open);

        menu_file.addSeparator(); // ------

        m_save_as = new JMenuItem("Save As...");
        //m_save.setIcon( CGUIUtils.getIcon( IGUIConstants.SAVE_LEVEL_SMALLICON ) );
        m_save_as.setMnemonic('v');
        m_save_as.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // save as
                control.saveAsActiveTab();
            }
        });
        menu_file.add(m_save_as);

        m_save = new JMenuItem("Save");
        //m_save.setIcon( CGUIUtils.getIcon( IGUIConstants.SAVE_LEVEL_SMALLICON ) );
        m_save.setMnemonic('S');
        m_save.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_S, Event.CTRL_MASK ) );
        m_save.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // save
                control.saveActiveTab();
            }
        });
        menu_file.add(m_save);

        menu_file.addSeparator(); // ------

        m_close = new JMenuItem( "Close" );
        m_close.setMnemonic( 'C' );
        m_close.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_Q, Event.CTRL_MASK ) );
        m_close.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // close
                control.closeActiveTab();
            }
        });
        menu_file.add( m_close );

        m_close_all = new JMenuItem( "Close All" );
        m_close_all.setMnemonic( 'l' );
        m_close_all.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // close
                control.closeAllTabs();
            }
        });
        menu_file.add( m_close_all );

        add(menu_file);
    }

    /**
     * Build Edit Menu
     */
    private void buildEditMenu() {
        menu_edit = new JMenu("Edit");

        m_cut = new JMenuItem("Cut");
        m_cut.setMnemonic('t');
        m_cut.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_X, Event.CTRL_MASK ) );
        m_cut.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // cut
                JEditTextArea activeTA = control.getActiveTextArea();
                if (activeTA != null)
                    InputHandler.getAction("cut").actionPerformed(new ActionEvent(activeTA, 0, "cut"));
            }
        });
        menu_edit.add(m_cut);

        m_copy = new JMenuItem("Copy");
        m_copy.setMnemonic('C');
        m_copy.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_C, Event.CTRL_MASK ) );
        m_copy.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // copy
                JEditTextArea activeTA = control.getActiveTextArea();
                if (activeTA != null)
                    InputHandler.getAction("copy").actionPerformed(new ActionEvent(activeTA, 0, "copy"));
            }
        });
        menu_edit.add(m_copy);

        m_paste = new JMenuItem("Paste");
        m_paste.setMnemonic('P');
        m_paste.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_V, Event.CTRL_MASK ) );
        m_paste.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                // paste
                JEditTextArea activeTA = control.getActiveTextArea();
                if (activeTA != null)
                    InputHandler.getAction("paste").actionPerformed(new ActionEvent(activeTA, 0, "paste"));
            }
        });
        menu_edit.add(m_paste);

        menu_edit.addSeparator(); // ------

        m_find = new JMenuItem("Find");
        m_find.setMnemonic('F');
        m_find.setAccelerator(
            KeyStroke.getKeyStroke( KeyEvent.VK_F, Event.CTRL_MASK ) );
        m_find.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                 // find
                 System.out.print("find...");
            }
        });
        menu_edit.add(m_find);

        add(menu_edit);
    }

    /**
     * Build Edit Menu
     */
    private void buildSettingsMenu() {
        menu_settings = new JMenu("Settings");

        m_font = new JMenuItem("Font");
        m_font.setMnemonic('F');
        m_font.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                 // set font
            }
        });
        menu_settings.add(m_font);

        m_colors = new JMenuItem("Colors");
        m_colors.setMnemonic('C');
        m_colors.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                 // set colors
            }
        });
        menu_settings.add(m_colors);

        add(menu_settings);
    }

    /**
     *  This builds the help menu.
     */
    private void buildHelpMenu(){
      menu_help = new JMenu("Help");
      m_onlineHelp = new JMenuItem("Online Help");
      m_onlineHelp.setMnemonic('H');
      m_onlineHelp.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                 // calls help frame
                 JFrame help = new ScriptHelp(control.view, null, false);  // initialize the frame
                 help.setVisible(true);                                  // show the window
            }
        });
        menu_help.add(m_onlineHelp);

      m_about = new JMenuItem("About");
      m_about.setMnemonic('A');
      m_about.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
              control.showMessage("About "+IGUIConstants.APP_NAME,
                                      " Version "+IGUIConstants.VERSION+"\n (c) 2004  Michael Toennies\n" +
                                      "      Andreas Vogl\n" + "      Peter Plischewsky\n" +"      Gecko\n");

            }
        });
        menu_help.add(m_about);
        add(menu_help);
    }

    /**
     * Refreshes the enable/disable state of all menus.
     */
    public void refresh() {
        m_find.setEnabled(false);
        m_colors.setEnabled(false);
        m_font.setEnabled(false);

        // see if there is a path for direct 'save'
        if (control.getActiveFilePath() != null)
            m_save.setEnabled(true);
        else
            m_save.setEnabled(false);
    }

    /**
     * Redraws the whole menu with latest custom fonts
     * @param do_redraw    if true, menu is redrawn at the end
     */
    public void updateFont(boolean do_redraw) {
        if (!control.isStandAlone()) {
            // File menu:
            control.getMainControl().setBoldFont(menu_file);
            control.getMainControl().setBoldFont(m_new);
            control.getMainControl().setBoldFont(m_open);
            control.getMainControl().setBoldFont(m_save_as);
            control.getMainControl().setBoldFont(m_save);
            control.getMainControl().setBoldFont(m_close);
            control.getMainControl().setBoldFont(m_close_all);

            // Edit menu:
            control.getMainControl().setBoldFont(menu_edit);
            control.getMainControl().setBoldFont(m_cut);
            control.getMainControl().setBoldFont(m_copy);
            control.getMainControl().setBoldFont(m_paste);
            control.getMainControl().setBoldFont(m_find);

            // Edit menu:
            control.getMainControl().setBoldFont(menu_settings);
            control.getMainControl().setBoldFont(m_font);
            control.getMainControl().setBoldFont(m_colors);
        }

        if (do_redraw) {
            // redraw the menu bar
            refresh();
            if (getGraphics() != null)
                update(getGraphics());
        }
    }
}
