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

/**
 * <code>CMainTolbar</code> implements the main toolbar of the application.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMainToolbar extends JToolBar {
    /** The key used to store the icons/labels setting to the INI file. */
    public static final String SHOW_ICONS_AND_LABELS_KEY = "MainToolbar.iconsAndLabels";
    /** Enumerated icons/labels mode. Shows both icons and labels. */
    public static final int SHOW_ICONS_AND_LABELS = 0;
    /** Enumerated icons/labels mode. Shows only icons. */
    public static final int SHOW_ICONS_ONLY       = 1;
    /** Enumerated icons/labels mode. Shows only labels. */
    public static final int SHOW_LABELS_ONLY      = 2;

    /** The controller of this toolbar view. */
    private CMainControl m_control;
    /** The current icon/labels mode. */
    private int m_eIconAndLabelVisibility = SHOW_ICONS_ONLY;

    /** The popup */
    private CPopupMenu m_popupMenu;
    // UI elements
    private JButton m_new;
    private JButton m_open;
    private JButton m_save;
    private JButton m_saveAs;
    private JButton m_undo;
    private JButton m_redo;
    private JButton m_nextWindow;
    private JButton m_prevWindow;

    /**
     * Constructs a new toolbar.
     *@param control The controller of this toolbar.
     */
    CMainToolbar( CMainControl control )  throws CGridderException {
        super(JToolBar.VERTICAL);  // construct with vertical alignment
        m_control = control;

        m_eIconAndLabelVisibility = Integer.parseInt(
                CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                                                                                                               SHOW_ICONS_AND_LABELS_KEY, ""+SHOW_ICONS_ONLY) );
        m_popupMenu = new CPopupMenu();
        addMouseListener( new MouseAdapter() {
                public void mousePressed(MouseEvent event) {
                    if ( ( event.getModifiers() & MouseEvent.META_MASK ) != 0 ) {
                        m_popupMenu.show( CMainToolbar.this, event.getX(), event.getY() );
                    }
                }
            } );

        rebuild();
    }

    /**
     * Checks if the label should be visible.
     *@param strLabel The label text.
     *@return The label text or null if labels should not be visible.
     */
    private String checkLabel( String strLabel ) {
        if ( m_eIconAndLabelVisibility == SHOW_ICONS_ONLY ) {
            return null;
        }

        return strLabel;
    }

    /**
     * Checks if the icon should be visible.
     *@param strIcon The icon name.
     *@return The icon name or null if icons should not be visible.
     */
    private String checkIcon( String strIcon ) {
        if ( m_eIconAndLabelVisibility == SHOW_LABELS_ONLY ) {
            return null;
        }

        return strIcon;
    }

    /**
     * Rebuilds the toolbar by first removing the buttons and
     * then adding them again.
     */
    private void rebuild() {
        removeAll();

        setMargin( new Insets(
                              IGUIConstants.DIALOG_INSETS,
                              0,
                              IGUIConstants.DIALOG_INSETS,
                              IGUIConstants.DIALOG_INSETS ) );

        Vector buttons = new Vector( 10, 2 );
        m_new = new CFancyButton(
                                 checkLabel( "New" ),
                                 "New Map",
                                 checkIcon( IGUIConstants.NEW_LEVEL_ICON ),
                                 new ActionListener() {
                                         public void actionPerformed(ActionEvent event) {
                                             m_control.newLevelWanted();
                                         }
                                     } );
        m_new.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_new.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_new );
        add(m_new);

        m_open = new CFancyButton(
                                  checkLabel( "Open" ),
                                  "Open Map File",
                                  checkIcon( IGUIConstants.OPEN_LEVEL_ICON ),
                                  new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              m_control.openFileWanted();
                                          }
                                      } );
        m_open.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_open.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_open );
        add(m_open);

        m_save = new CFancyButton(
                                  checkLabel( "Save" ),
                                  "Save Map File",
                                  checkIcon( IGUIConstants.SAVE_LEVEL_ICON ),
                                  new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              m_control.saveCurrentLevelWanted();
                                          }
                                      } );
        m_save.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_save.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_save );
        add(m_save);

        m_saveAs = new CFancyButton(
                                    checkLabel( "Save As" ),
                                    "Save Map File As",
                                    checkIcon( IGUIConstants.SAVE_LEVEL_AS_ICON ),
                                    new ActionListener() {
                                            public void actionPerformed(ActionEvent event) {
                                                m_control.saveCurrentLevelAsWanted();
                                            }
                                        } );
        m_saveAs.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_saveAs.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_saveAs );
        add(m_saveAs);

        addSeparator();

        m_undo = new CFancyButton(
                                  checkLabel( "Undo" ),
                                  "Undo Operation",
                                  checkIcon( IGUIConstants.UNDO_ICON ),
                                  new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              m_control.undoWanted();
                                          }
                                      } );
        m_undo.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_undo.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_undo );
        add(m_undo);

        m_redo = new CFancyButton(
                                  checkLabel( "Redo" ),
                                  "Redo Operation",
                                  checkIcon( IGUIConstants.REDO_ICON ),
                                  new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              m_control.redoWanted();
                                          }
                                      } );
        m_redo.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_redo.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_redo );
        add(m_redo);

        addSeparator();

        m_prevWindow = new CFancyButton(
                                        checkLabel( "Prev" ),
                                        "Show Previous Window",
                                        checkIcon( IGUIConstants.PREVIOUS_WINDOW_ICON ),
                                        new ActionListener() {
                                                public void actionPerformed(ActionEvent event) {
                                                    m_control.previousWindowWanted();
                                                }
                                            } );
        m_prevWindow.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_prevWindow.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_prevWindow );
        add(m_prevWindow);

        m_nextWindow = new CFancyButton(
                                        checkLabel( "Next" ),
                                        "Show Next Window",
                                        checkIcon( IGUIConstants.NEXT_WINDOW_ICON ),
                                        new ActionListener() {
                                                public void actionPerformed(ActionEvent event) {
                                                    m_control.nextWindowWanted();
                                                }
                                            } );
        m_nextWindow.setVerticalTextPosition( CFancyButton.BOTTOM );
        m_nextWindow.setHorizontalTextPosition( CFancyButton.CENTER );
        buttons.addElement( m_nextWindow );
        add(m_nextWindow);

        doLayout();

        // If icons and labels are visible make buttons square shaped
        if ( m_eIconAndLabelVisibility == SHOW_ICONS_AND_LABELS ) {
            int maxWidth = 16;
            for ( Enumeration enu = buttons.elements();
                  enu.hasMoreElements(); ){
                JButton button = (JButton) enu.nextElement();
                maxWidth = Math.max( maxWidth, button.getWidth() );
            }

            for ( Enumeration enu = buttons.elements();
                  enu.hasMoreElements(); ) {
                JButton button = (JButton) enu.nextElement();
                Dimension size = new Dimension( maxWidth, maxWidth );
                button.setSize( size );
                button.setMinimumSize( size );
                button.setMaximumSize( size );
                button.setPreferredSize( size );
            }
        }

        refresh();
        doLayout();
        repaint();
        m_control.refreshMainView();
    }

    /**
     * Sets the icon and label visibility.
     *@param eVisibility One of the enumerated SHOW_xxx values.
     */
    void setIconAndLabelVisibility( int eVisibility ) {
        switch( eVisibility ) {
        case SHOW_ICONS_AND_LABELS:
        case SHOW_ICONS_ONLY:
        case SHOW_LABELS_ONLY:
            m_eIconAndLabelVisibility = eVisibility;
            CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(
                                  SHOW_ICONS_AND_LABELS_KEY, ""+eVisibility );
            break;
        default:
            return;
        }

        rebuild();
    }

    /**
     * Refreshes the state of items in this toolbar.
     */
    void refresh() {
        boolean fLevelEdited = m_control.isLevelEdited();
        m_save.setEnabled( m_control.isPlainSaveEnabled() );
        m_saveAs.setEnabled( fLevelEdited );

        m_undo.setEnabled( m_control.isUndoPossible() );
        m_undo.setToolTipText( "Undo " + m_control.getUndoName() );
        m_redo.setEnabled( m_control.isRedoPossible() );
        m_redo.setToolTipText( "Redo " + m_control.getRedoName() );
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
    }

    /**
     * The popup menu that is shown on right click
     * upon this toolbar.
     */
    public class CPopupMenu extends JPopupMenu {
        JRadioButtonMenuItem m_iconsAndLabels;
        JRadioButtonMenuItem m_iconsOnly;
        JRadioButtonMenuItem m_labelsOnly;
        ButtonGroup m_group;

        /**
         * Constructs the popup menu with the appropriate menuitems.
         */
        public CPopupMenu() {
            m_group = new ButtonGroup();
            m_iconsAndLabels  = new JRadioButtonMenuItem( "Icons&Labels" );
            m_iconsAndLabels.addActionListener(
                                               new ActionListener() {
                                                       public void actionPerformed(ActionEvent event) {
                                                           setIconAndLabelVisibility( SHOW_ICONS_AND_LABELS );
                                                       }
                                                   } );
            m_group.add( m_iconsAndLabels );
            add( m_iconsAndLabels );
            m_iconsOnly       = new JRadioButtonMenuItem( "Icons" );
            m_iconsOnly.addActionListener(
                                          new ActionListener() {
                                                  public void actionPerformed(ActionEvent event) {
                                                      setIconAndLabelVisibility( SHOW_ICONS_ONLY );
                                                  }
                                              } );
            m_group.add( m_iconsOnly );
            add( m_iconsOnly );
            m_labelsOnly      = new JRadioButtonMenuItem( "Labels" );
            m_labelsOnly.addActionListener(
                                           new ActionListener() {
                                                   public void actionPerformed(ActionEvent event) {
                                                       setIconAndLabelVisibility( SHOW_LABELS_ONLY );
                                                   }
                                               } );
            m_group.add( m_labelsOnly );
            add( m_labelsOnly );

            refresh();
        }

        /**
         * Refreshes the states of the menuitems.
         */
        void refresh() {
            switch(m_eIconAndLabelVisibility) {
            case SHOW_ICONS_AND_LABELS:
                m_group.setSelected( m_iconsAndLabels.getModel(), true );
                m_group.setSelected( m_iconsOnly.getModel(), false );
                m_group.setSelected( m_labelsOnly.getModel(), false );
                break;
            case SHOW_ICONS_ONLY:
                m_group.setSelected( m_iconsAndLabels.getModel(), false );
                m_group.setSelected( m_iconsOnly.getModel(), true );
                m_group.setSelected( m_labelsOnly.getModel(), false );
                break;
            case SHOW_LABELS_ONLY:
                m_group.setSelected( m_iconsAndLabels.getModel(), false );
                m_group.setSelected( m_iconsOnly.getModel(), false );
                m_group.setSelected( m_labelsOnly.getModel(), true );
                break;
            }
        }
    }
}
