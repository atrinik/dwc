/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 * Copyright (C) 2001  Andreas Vogl
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

package cfeditor;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * The main view of the level editor. Contains the "desktop" for internal level
 * windows, tile palette, menu, status- and toolbar.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMainView extends JFrame implements InternalFrameListener {
    /** The key used to store the selected L'n'F to INI file. */
    public static final String SELECTED_LNF_KEY = "MainWindow.lnfClass";
    /** The key used to store the main windows X-coordinate to INI file. */
    private static final String WINDOW_X      = "MainWindow.x";
    /** The key used to store the main windows Y-coordinate to INI file. */
    private static final String WINDOW_Y      = "MainWindow.y";
    /** The key used to store the main windows width to INI file. */
    private static final String WINDOW_WIDTH  = "MainWindow.width";
    /** The key used to store the main windows height to INI file. */
    private static final String WINDOW_HEIGHT = "MainWindow.height";
    /** The key used to store the main windows divider location to the INI file. */
    private static final String DIVIDER_LOCATION_KEY = "MainWindow.dividerLocation";
    private static final String DIVIDER_LOCATION_KEY2 = "MainWindow.dividerLocation2";
    private static final String DIVIDER_LOCATION_KEY3 = "MainWindow.dividerLocation3";
    /** key for info weither map-tile panel is seperate or at bottom. */
    public static final String MAPTILE_BOTTOM_KEY = "MapTileBottom";

    /** Border size for the split panes */
    private static final int BORDER_SIZE = 5;

    /** The controller of this view. */
    private CMainControl    m_control;
    /** The main toolbar. */
    private CMainToolbar    m_toolbar;
    /** The main statusbar. */
    private CMainStatusbar  m_statusbar;
    /** The panel that contains the palette view. */
    public CArchPanel m_archPanel;
    /** The desktop that holds all level views. */
    private JDesktopPane    m_mapDesktop;
    /** The main menu. */
    private CMainMenu       m_menu;
    /** The split pane. */
    private CSplitPane m_splitPane;
    /** All open level views. */
    private CSplitPane m_splitDownPane;
    /** All open level views. */
    private CSplitPane m_splitRightPane;
    /** All open level views. */
    private Vector m_mapViews = new Vector(1,2);
    /** Currently focused level view. */
    private CMapViewIFrame m_focusedMapView;

    private CMapTileList m_mapPanel;       // list of objects on map (right side)
    private CMapArchPanel m_mapArchPanel;  // attribute panel (bottom)
    private JTabbedPane m_pickmapPanel;    // panel with pickmaps

    // true when pickmap is active, false when archlist is active
    private boolean pickmap_active = false;

    // when true, archpanel is merged into the bottom panel
    private boolean maptile_bottom = true;

    /**
     * Constructs the main view and registers the given main controller.
     *@param control The controller of this view.
     */
    CMainView(CMainControl control) {
        super(IGUIConstants.APP_NAME+" - "+IGUIConstants.APP_WINDOW_TITLE);
        m_control = control;

        ImageIcon icon = CGUIUtils.getIcon( IGUIConstants.APP_ICON );
        if (icon != null) {
            setIconImage( icon.getImage() );
        }

        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                m_control.exitWanted();
            }
        });
    }

    /**
     * Initialises (builds) this view.
     * @exception CGridderException thrown if initialisation fails.
     */
    void init() throws CGridderException {
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);

        // set look and feel
        String strSelectedLNFName = settings.getProperty(CMainView.SELECTED_LNF_KEY,
                                                         UIManager.getCrossPlatformLookAndFeelClassName() );
        m_control.setLookNFeel( strSelectedLNFName );

        // calculate some default values in case there is no settings file
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int defwidth = (int)(0.9*screen.getWidth());
        int defheight = (int)(0.9*screen.getHeight());

        // define divider locations
        int divLocationRight = Integer.parseInt(settings.getProperty( DIVIDER_LOCATION_KEY3, ""+(int)(defwidth*0.62) ));
        int divLocationDown = Integer.parseInt(settings.getProperty( DIVIDER_LOCATION_KEY2, ""+(int)(defheight*0.76) ));
        int divLocation = Integer.parseInt(settings.getProperty( DIVIDER_LOCATION_KEY, ""+(int)(defwidth*0.17) ));

        // get the old location and size
        int x = Integer.parseInt( settings.getProperty(WINDOW_X, ""+(int)((screen.getWidth()-defwidth)/2.)));
        int y = Integer.parseInt( settings.getProperty(WINDOW_Y, ""+(int)((screen.getHeight()-defheight)/2.)));
        int width = Integer.parseInt( settings.getProperty(WINDOW_WIDTH, ""+defwidth));
        int height = Integer.parseInt( settings.getProperty(WINDOW_HEIGHT, ""+defheight));

        // Add all the subviews
        getContentPane().setLayout( new BorderLayout() );

        m_toolbar = new CMainToolbar (m_control );
        //getContentPane().add( m_toolbar, BorderLayout.NORTH );
        getContentPane().add( m_toolbar, BorderLayout.WEST );  // put it left

        m_statusbar = new CMainStatusbar( m_control );
        getContentPane().add( m_statusbar, BorderLayout.SOUTH );

        m_menu = new CMainMenu( m_control );
        setJMenuBar( m_menu );

        ReplaceDialog.init( m_control ); // initialize replace dialog

        // initialize pickmap panel
        m_pickmapPanel = new JTabbedPane(JTabbedPane.TOP);
        m_pickmapPanel.setBorder(BorderFactory.createEmptyBorder(IGUIConstants.SPACE_PICKARCH_TOP, 0, 0, 0));
        CPickmapPanel.getInstance().addPickmapSelectionListener(m_pickmapPanel);

        // Build the placeholder for tile palette
        m_archPanel = new CArchPanel( m_control );
        m_mapPanel = new CMapTileList( m_control , this);

        m_mapDesktop = new JDesktopPane();

        if (!maptile_bottom) {
            // the map tile list is on the right side
            m_splitRightPane = new CSplitPane(CSplitPane.HORIZONTAL_SPLIT,
                                          m_mapDesktop,
                                          m_mapPanel);

            m_splitRightPane.setDividerLocation( divLocationRight );
            m_splitRightPane.setDividerSize( BORDER_SIZE );

            getContentPane().add( m_splitRightPane, BorderLayout.CENTER);

            m_mapArchPanel = new CMapArchPanel( m_control , this);
            m_splitDownPane = new CSplitPane(CSplitPane.VERTICAL_SPLIT,
                                             m_splitRightPane,
                                             m_mapArchPanel);

            m_splitDownPane.setDividerLocation( divLocationDown );
            m_splitDownPane.setDividerSize( BORDER_SIZE );

            getContentPane().add( m_splitDownPane, BorderLayout.CENTER);

            m_splitPane = new CSplitPane(CSplitPane.HORIZONTAL_SPLIT,
                                         m_archPanel,m_splitDownPane);

            m_splitPane.setDividerLocation( divLocation );
            m_splitPane.setDividerSize( BORDER_SIZE );
            getContentPane().add( m_splitPane, BorderLayout.CENTER );
        }
        else {
            // the map tile list is merged into the bottom panel
            m_mapArchPanel = new CMapArchPanel( m_control , this);
            m_splitRightPane = new CSplitPane(CSplitPane.HORIZONTAL_SPLIT,
                                              m_mapArchPanel,
                                              m_mapPanel);

            m_splitRightPane.setDividerLocation( divLocationRight );
            m_splitRightPane.setDividerSize( BORDER_SIZE );

            // split off the bottom panel
            m_splitDownPane = new CSplitPane(CSplitPane.VERTICAL_SPLIT,
                                             m_mapDesktop,
                                             m_splitRightPane);

            m_splitDownPane.setDividerLocation( divLocationDown );
            m_splitDownPane.setDividerSize( BORDER_SIZE );
            getContentPane().add( m_splitDownPane, BorderLayout.CENTER);

            // split off the left arch panel
            m_splitPane = new CSplitPane(CSplitPane.HORIZONTAL_SPLIT,
                                         m_archPanel,m_splitDownPane);

            m_splitPane.setDividerLocation( divLocation );
            m_splitPane.setDividerSize( BORDER_SIZE );
            getContentPane().add( m_splitPane, BorderLayout.CENTER );
        }

        // set bounds (location and size) of the main frame
        setBounds(x, y, width, height);
        setVisible(true);
        //     CStartupScreen startupScreen = new CStartupScreen( this );
        //   startupScreen.show();
    }

    /**
     * open the online help window
     */
    public void openHelpWindow() {
        JFrame help = new CFHelp(this, null, false);  // initialize the frame
        help.setVisible(true);                                  // show the window
    }

    public void setPickmapActive(boolean state) {
        pickmap_active = state;
        m_menu.setActivePickmapsEnabled(state);
    }

    /**
     * @return true when pickmap is active, false when archlist is active
     */
    public boolean isPickmapActive() {
        return pickmap_active;
    }

    /**
     * Move the pickmap panel in front of the default-archpanel
     */
    public void movePickmapPanelToFront() {
        m_archPanel.movePickmapPanelToFront();
    }

    /**
     * @returns the active arch in the left-side panel.
     * This can either be a default arch from the archlist, or
     * a custom arch from a pickmap.
     * IMPORTANT: The returned ArchObject is not a clone. A copy
     * must be generated before inserting such an arch to the map.
     */
    public ArchObject getArchPanelSelection() {

        if ((isPickmapActive() || m_archPanel.getArchPanelSelection() == null)
            && CPickmapPanel.getInstance().isLoadComplete() &&
            CPickmapPanel.getInstance().getCurrentPickmap() != null) {
            // get the active pickmap
            CMapControl pmap = CPickmapPanel.getInstance().getCurrentPickmap();

            if (pmap != null && pmap.m_view.isHighlight()) {
                // now try to get the topmost object
                ArchObject arch = null;
                if (pmap.pointValid(pmap.m_view.getHighlightStart().x, pmap.m_view.getHighlightStart().y))
                    arch = pmap.m_model.getMapGrid()[pmap.m_view.getHighlightStart().x][pmap.m_view.getHighlightStart().y];
                if (arch != null) {
                    // so here we return the arch from the pickmap
                    return arch;
                }
            }
            m_control.showArchPanelQuickObject(null);       // send it to quick view
            return null;
        }

        // return the arch from the archlist in any case the pickmap is
        // either not active or didn't work
        return(m_archPanel.getArchPanelSelection());
    }

    public void showArchPanelQuickObject(ArchObject arch) {
        m_archPanel.showArchPanelQuickObject(arch);
    }

    /**
     * @return the panel with all pickmaps
     */
    public JTabbedPane getPickmapPanel() {
        return m_pickmapPanel;
    }

    // show a arch in the arch map panel
    void SetMapArchPanelObject(ArchObject arch) {
        m_mapArchPanel.SetMapArchPanelObject(arch);
    }

    // access mape tile list ...
    public void setMapTileList(CMapControl map,int archid) {
        m_mapPanel.setMapTileList(map, archid);
    }

    public void RefreshMapTileList() {
        m_mapPanel.refresh();
    }

    public ArchObject getMapTileSelection() {

        return(m_mapPanel.getMapTileSelection());
    }

    public void addArchPanel(String name) {
        m_archPanel.addPanel(name);
    }

    public void disableTabPane() {
        m_archPanel.disableTabPane();

    }

    public void enableTabPane() {
        m_archPanel.enableTabPane();
    }

    public int addArchPanelCombo(String name) {
        return(m_archPanel.addArchPanelCombo(name));
    }

    public void addArchPanelArch(int archnr, int index) {
        m_archPanel.addArchPanelArch(archnr, index);
    }

    // selected arch in arch panel
    public int getPanelArch() {
        return(m_archPanel.getPanelArch());
    }

    public void SetStatusText(String string) {
        m_statusbar.setStatusText(string);
    }

    // is the map tile list in the bottom panel
    public boolean isMapTileListBottom() {
        return maptile_bottom;
    }

    // is the map tile list in the bottom panel
    public void setMapTileListBottom(boolean state) {
        maptile_bottom = state;
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {

        // Store the location and size
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        Rectangle bounds = getBounds();
        settings.setProperty( WINDOW_X, ""+bounds.x );
        settings.setProperty( WINDOW_Y, ""+bounds.y );
        settings.setProperty( WINDOW_WIDTH, ""+bounds.width );
        settings.setProperty( WINDOW_HEIGHT, ""+bounds.height );
        settings.setProperty( DIVIDER_LOCATION_KEY, ""+
                              m_splitPane.getDividerLocation() );
        settings.setProperty( DIVIDER_LOCATION_KEY2, ""+
                              m_splitDownPane.getDividerLocation() );
        settings.setProperty( DIVIDER_LOCATION_KEY3, ""+
                              m_splitRightPane.getDividerLocation() );

        m_archPanel.appExitNotify();
        m_mapArchPanel.appExitNotify();
        m_mapPanel.appExitNotify();
        m_toolbar.appExitNotify();
        m_statusbar.appExitNotify();
        m_menu.appExitNotify();
    }

    /**
     * Refreshes the state of menu items and toolbar buttons.
     * Beware!! Executing this function eats up a lot of time!
     * Avoid using it whenever possible.
     */
    void refreshMenusAndToolbars() {
        m_archPanel.refresh();
        m_mapPanel.refresh();
        m_mapArchPanel.refresh();
        m_menu.refresh();
        m_toolbar.refresh();
        m_statusbar.refresh();
    }

    /**
     * refresh the map arch panel (bottom window)
     */
    void refreshMapArchPanel() {
        m_mapArchPanel.refresh();
    }

    /**
     * redraw map arch panel with latest custom fonts
     */
    void updateMapArchPanelFont() {
        m_mapArchPanel.updateFont(true);
    }

    /**
     * redraw status bar with latest custom fonts
     */
    void updateStatusBarFont() {
        m_statusbar.updateFont();
    }

    /**
     * redraw arch panel with latest custom fonts
     */
    void updateArchPanelFont() {
        m_archPanel.updateFont();
    }

    void setRevertMenuEnabled(boolean state) {
        m_menu.setRevertEnabled(state);
    }

    /**
     * Refreshes the state of the menu only.
     */
    void refreshMenus() {
        m_menu.refresh();
    }

    /**
     * Relayouts and repaints the main view and
     * all its components.
     */
    void refresh() {
        if ( ( m_toolbar != null ) && ( m_statusbar != null ) &&
             ( m_menu != null ) ) {
            m_toolbar.doLayout();
            m_statusbar.doLayout();
            m_archPanel.doLayout();
            m_mapPanel.doLayout();
            m_mapArchPanel.doLayout();
            m_menu.doLayout();
            doLayout();

            m_toolbar.repaint();
            m_statusbar.repaint();
            m_archPanel.repaint();
            m_mapPanel.repaint();
            m_mapArchPanel.repaint();
            m_menu.repaint();
            repaint();
        } else {
            doLayout();
            repaint();
        }
    }

    /**
     * Adds the level view.
     * @param mapView    the map view to add
     */
    public void addLevelView( CMapViewIFrame mapView ) {
        m_mapViews.add( mapView );
        mapView.addInternalFrameListener( this );
        m_mapDesktop.add( mapView );

        // set bounds to maximum size
        if (!maptile_bottom) {
            mapView.setBounds(0, 0, m_mapPanel.getX()-BORDER_SIZE-2,
                              m_mapArchPanel.getY()-BORDER_SIZE-4);
        }
        else {
            mapView.setBounds(0, 0, m_mapDesktop.getWidth()-2,
                              m_mapDesktop.getHeight()-2);
        }
        //mapView.setBounds( 0, 0, 320, 240 );
        mapView.setVisible( true );
        setCurrentLevelView( mapView );
    }

    /**
     * Removes (closes) the level view.
     * @param mapView     the map view to be removed (closed).
     */
    void removeLevelView( CMapViewIFrame mapView ) {
        if ( !m_mapViews.contains( mapView ) ) {
            return;
        }

        m_mapViews.removeElement( mapView );
        mapView.closeNotify();
        m_mapDesktop.remove( mapView );
        mapView.dispose();
        m_mapDesktop.repaint();

        updateFocus( true );
    }

    /**
     * Returns all level windows that exist in the main view.
     *@return All level windows
     */

    Enumeration getAllLevelWindows() {
        if ( ( m_mapViews != null ) && ( m_mapViews.size() > 0 ) ) {
            return m_mapViews.elements();
        }

        return null;
    }

    /**
     * Shows the given error in the UI.
     *@param error The error to be shown.
     */
    void showError( CGridderException error ) {
        JOptionPane.showConfirmDialog(
                                      this,
                                      error.getMessage(),
                                      IGUIConstants.APP_NAME+" Error in "+error.getOriginator(),
                                      JOptionPane.OK_OPTION,
                                      JOptionPane.WARNING_MESSAGE );
    }

    /**
     * Shows the given message in the UI.
     * @param strTitle      The title of the message.
     * @param strMessage    The message to be shown.
     * @param messageType   Type of message (see JOptionPane constants), defines icon used
     */
    public void showMessage(String strTitle, String strMessage, int messageType) {
        JOptionPane.showMessageDialog(this, strMessage, strTitle, messageType);
    }

    public void showMessage(String strTitle, String strMessage) {
        JOptionPane.showMessageDialog(this, strMessage, strTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the given confirmation message in the UI. The message
     * is a yes/no option.
     *@param strTitle The title of the message.
     *@param strMessage The message to be shown.
     *@return Ture if the user agrees, false if user disagrees.
     */
    public boolean askConfirm( String strTitle, String strMessage ) {
        return ( JOptionPane.showConfirmDialog(
                                               this,
                                               strMessage,
                                               strTitle,
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.YES_OPTION );
    }

    /**
     * Updates the focus to the first non-iconified level window.
     *@param fCareAboutIconification True if the focus update should ignore
     *       all windows iconified by the user.
     */
    void updateFocus( boolean fCareAboutIconification ) {

        m_focusedMapView = null;

        // Show the next level (if such exists)
        for (Enumeration enu = m_mapViews.elements(); enu.hasMoreElements();) {
            CMapViewIFrame view = (CMapViewIFrame) enu.nextElement();

            if ( view.isIcon() ) {
                if ( !fCareAboutIconification ) {
                    try {
                        view.setIcon( false );
                        m_control.setCurrentLevel( view.getMapControl() );
                        view.moveToFront();
                        view.setVisible(true);
                        return;
                    } catch( java.beans.PropertyVetoException cantUniconify ) {
                    }
                }
            } else {
                m_control.setCurrentLevel( view.getMapControl() );
                view.setVisible(true);
                view.moveToFront();
                return;
            }
        }

        // No non-iconified level windows found
        m_focusedMapView = null;
        m_control.setCurrentLevel( null );
    }

    /**
     * Gives focus to the next window.
     */
    public void previousWindowWanted() {

        if ( m_mapViews.size() > 1 ) {
            Object view = m_mapViews.firstElement();
            m_mapViews.removeElement( view );
            m_mapViews.addElement( view );
        }
        updateFocus( false );
    }

    /**
     * Gives focus to the previous window.
     */
    public void nextWindowWanted() {

        if ( m_mapViews.size() > 1 ) {
            Object view = m_mapViews.lastElement();
            m_mapViews.removeElement( view );
            m_mapViews.insertElementAt( view, 0 );
        }

        updateFocus( false );
    }

    /**
     * Sets the given level view as the current one.
     *@param view The new current level view.
     */
    public void setCurrentLevelView( CMapViewIFrame view ) {
        m_mapViews.removeElement( view );
        m_mapViews.insertElementAt( view, 0 );

        // Deiconify if necessary
        if ( view.isIcon() ) {
            try {
                view.setIcon( false );
                view.setVisible(true);
                return;
            } catch( java.beans.PropertyVetoException cantUniconify ) {
            }
        }
        updateFocus( true );
    }

    /**
     * Notifies that the level views focus is lost it is inserted
     * as the second in line to the level view vector.
     *@param view The level view who lost the focus.
     */

    public void levelViewFocusLostNotify( CMapViewIFrame view ) {

        if ( m_mapViews.size() > 1 ) {
            m_mapViews.removeElement( view );
            m_mapViews.addElement( view );
            updateFocus( true );
        }

    }

    /**
     * Notifies that the given level view is now set as the current one.
     *@param view The new current level view.
     */

    public void levelViewFocusGainedNotify( CMapViewIFrame view ) {

        m_mapViews.removeElement( view );
        m_mapViews.insertElementAt( view, 0 );
        m_focusedMapView  = view;
        CMapControl level = view.getLevel();
        m_control.setCurrentLevel( level );
        //    m_statusbar.setLevelInfo( level );

    }

    /**
     * Invoked when the internal frame view is opened.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameOpened(InternalFrameEvent event) {
    }

    /**
     * Invoked when the internal frame view is closing.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameClosing(InternalFrameEvent event) {

        CMapViewIFrame view = (CMapViewIFrame) event.getSource();
        removeLevelView( view );

    }

    /**
     * Invoked when the internal frame view is closed.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameClosed(InternalFrameEvent event) {
    }

    /**
     * Invoked when the internal frame view is iconified.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameIconified(InternalFrameEvent event) {
        CMapViewIFrame view = (CMapViewIFrame) event.getSource();
        levelViewFocusLostNotify( view );

    }

    /**
     * Invoked when the internal frame view is deiconified.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameDeiconified(InternalFrameEvent event) {
    }

    /**
     * Invoked when the internal frame view is activated.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameActivated(InternalFrameEvent event)
    {
        CMapViewIFrame view = (CMapViewIFrame) event.getSource();
        levelViewFocusGainedNotify( view );
    }

    /**
     * Invoked when the internal frame view is deactivated.
     * @param event    the occurred <code>InternalFrameEvent</code>
     */
    public void internalFrameDeactivated(InternalFrameEvent event) {
    }

    /**
     * Splitpane class that keeps its size even upon L'n'F change.
     */
    public class CSplitPane extends JSplitPane {
        public CSplitPane( int newOrientation,
                           Component newLeftComponent,
                           Component newRightComponent ) {
            super(newOrientation, newLeftComponent, newRightComponent );
        }

        /**
         * Overridden to store and restore the divider location upon
         * UI change.
         */
        public void updateUI() {
            int dividerLocation = getDividerLocation();
            int dividerSize     = getDividerSize();
            super.updateUI();
            setDividerLocation( dividerLocation );
            setDividerSize( dividerSize );
        }
    }

}
