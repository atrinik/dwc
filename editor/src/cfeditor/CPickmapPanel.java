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
import java.util.*;
import java.io.*;

/**
 * The <code>CPickmapPanel</code> manages the pickmap panel
 * and most pickmap-related code in general.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CPickmapPanel {
    /** static instance of this class */
    private static CPickmapPanel instance;

    private CMainControl m_control; // main control reference

    private boolean loadComplete;   // true when all pickmaps have been loaded (at least one)

    private JTabbedPane tabpane = null;  // tab pane with pickmaps

    /** all open pickmaps (the map controllers get stored in the vector). */
    private Vector m_pickmaps = new Vector(1,2);

    /** the current active pickmap ontop */
    private CMapControl m_currentPickMap;

    /**
     * Constructor
     */
    public CPickmapPanel() {
        m_control = CMainControl.getInstance();
        instance = this;
        loadComplete = false;
        m_currentPickMap = null;
    }

    /**
     * @return instance of this class
     */
    public static CPickmapPanel getInstance() {
        return instance;
    }

    /**
     * @return true when loading process of pickmaps is complete,
     *         and at least one pickmap is available.
     */
    public boolean isLoadComplete() {
        return loadComplete;
    }

    /**
     * @return currently active pickmap (is on top),
     *         or null if there is no pickmap
     */
    public CMapControl getCurrentPickmap() {
        return m_currentPickMap;
    }

    /**
     * load all pickmaps and build the pickmap-panel in the process
     */
    public void loadPickmaps() {
        // the main-panel for pickmaps:
        tabpane = m_control.getMainView().getPickmapPanel();

        File pickmapDir = new File((IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.PICKMAP_DIR : IGUIConstants.PICKMAP_DIR));
        if (!pickmapDir.exists() || !pickmapDir.isDirectory()) {
            System.out.println("No pickmaps directory found.");
            return;
        }

        File []flist = pickmapDir.listFiles(); // list of files
        for (int i=0; i<flist.length; i++) {
            // open the pickmaps
            if (flist[i].isFile())
                openPickmap(flist[i]);
        }
        m_control.refreshMenusAndToolbars();

        // did we get something?
        if (!m_pickmaps.isEmpty()) {
            loadComplete = true;

            if (IGUIConstants.isoView)
                m_control.getMainView().movePickmapPanelToFront();
        }

        updateActivePickmap(); // make sure we know which one is on top
    }

    public boolean openPickmap(File mapFile) {
        return openPickmap(mapFile, -1);
    }

    /**
     * Open and load a pickmap from the given file
     * @param mapFile the map file
     * @param index the tab index where this pickmap should be inserted
     * @return true when pickmap was opened successfully
     */
    public boolean openPickmap(File mapFile, int index) {
        // open the pickmaps
        CMapViewBasic bmapview;
        ArchObject start;
        MapArchObject maparch;

        try {
            start = m_control.mapFileDecoder.decodeMapFile(mapFile, m_control); // parse mapfile
            maparch = m_control.mapFileDecoder.getMapArch();       // get map arch

            if(start == null) {
                // The map is totally empty
                bmapview = newPickmap(start, maparch, mapFile, index); // init the map
            } else {
                // go to ArchObjectParser and add the default arch list information to them
                if(m_control.collectTempList(start, mapFile)==false) // get face names, face id, etc.
                    return false;
                start = m_control.sortTempList(start);  // sort the list (put multiparts at the end)
                bmapview = newPickmap(start, maparch, mapFile, index);  // init the map
                m_control.deleteTempList(start);        // remove temp list connection
            }

            start = null;

            // looks like it worked, so we add a panel and display this pickmap
            if (bmapview != null) {
                if (index < 0 || index >= tabpane.getTabCount())
                    tabpane.addTab(mapFile.getName(), bmapview);
                else
                    tabpane.insertTab(mapFile.getName(), null, bmapview, null, index);
                  bmapview.getMapControl().is_pickmap = true;
                return true;
            }
        }
        catch (CGridderException e) {
            // loading failed - could be a system file in that directory,
            // or something else. Doesn't deserve more attention than a printout.
            if (!mapFile.getName().startsWith("."))
                System.out.println("Couldn't load Pickmap:\n"+e.getMessage());
            maparch = null; bmapview = null; start = null;
        }
        return false;
    }

    /**
     * Create a new pickmap and display it.
     * @param maparch MapArchObject containing map name and -size
     */
    public boolean addNewPickmap(MapArchObject maparch) {
        CMapViewBasic bmapview = null;

        File mapFile = new File((IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.PICKMAP_DIR+File.separator+maparch.getFileName() :
                                                         IGUIConstants.PICKMAP_DIR + File.separator + maparch.getFileName()));
        if (mapFile.exists()) {
            m_control.showMessage("Cannot Create Pickmap", "A pickmap named '"+mapFile+"' already exists.\n"+
                                  "Either remove the existing one or choose a different name.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!mapFile.getParentFile().exists())
            mapFile.getParentFile().mkdirs();

        bmapview = newPickmap(null, maparch, mapFile, -1);
        if (bmapview != null) {
            bmapview.getLevel().save();
            tabpane.addTab(mapFile.getName(), bmapview);
            setActivePickmap(tabpane.getTabCount()-1);
            loadComplete = true;
            return true;
        }
        return false;
    }

    /**
     * add a new pickmap
     * @param start    first ArchObject of the pickmap
     * @param maparch  the maparch of the pickmap
     * @param index    tab index to insert, -1 means add to the end
     * @return basic mapview
     */
    private CMapViewBasic newPickmap(ArchObject start, MapArchObject maparch, File mapFile, int index) {
        CMapControl map;
        try {
            map = new CMapControl(m_control, start, maparch, true);
            map.m_view.setAutoscrolls(true);
            map.setMapFile(mapFile);

            // add pickmap to vector
            if (index < 0 || index >= m_pickmaps.size())
                m_pickmaps.addElement(map);
            else
                m_pickmaps.insertElementAt(map, index);
            m_currentPickMap = map;
            map.m_model.resetLevelChangedFlag();

            return map.m_view.getBasicView();

        } catch( CGridderException error) {
            map = null;
            System.out.println("Failed to create new pickmap.");
            m_control.handleErrors( error );
        }
        return null;
    }

    /**
     * Close a pickmap: Remove it from the panel and the data vector.
     * @param map CMapControl of the pickmap to remove
     * @return true when closing successful
     */
    public boolean closePickmap(CMapControl map) {
        CMapControl tmpMap = null;
        boolean mapClosed = false;
        for (int i=0; m_pickmaps != null && !mapClosed && i<m_pickmaps.size(); i++) {
            tmpMap = (CMapControl)(m_pickmaps.elementAt(i));
            if (tmpMap == map) {
                m_pickmaps.removeElementAt(i);
                tabpane.remove(i);
                mapClosed = true;
            }
        }

        if (mapClosed) {
            updateActivePickmap();
        }

        return mapClosed;
    }

    /**
     * Get tab index of a pickmap in the JTabbedPane
     * @param map control
     * @return tab index of pickmap, or -1 if it doesn't exist
     */
    public int getPickmapTabIndex(CMapControl map) {
        CMapControl tmpMap = null;
        for (int i=0; m_pickmaps != null && i<m_pickmaps.size(); i++) {
            tmpMap = (CMapControl)(m_pickmaps.elementAt(i));
            if (tmpMap == map)
                return i;
        }
        return -1;
    }

    /**
     * Get tab index of a pickmap in the JTabbedPane by absolut file name
     * @param map control
     * @return tab index of pickmap, or -1 if it doesn't exist
     */
    public int getPickmapTabIndexByName(String name) {
        CMapControl tmpMap = null;
        for (int i=0; m_pickmaps != null && i<m_pickmaps.size(); i++) {
            tmpMap = (CMapControl)(m_pickmaps.elementAt(i));
            if (tmpMap.getMapFileName().compareTo(name) == 0)
                return i;
        }
        return -1;
    }

    /**
     * Get a pickmap in the JTabbedPane by the index
     * @param tab index number
     * @return the map
     */
    public CMapControl getPickmapByIndex(int index) {
        CMapControl tmpMap = null;
        for (int i=0; m_pickmaps != null && i<m_pickmaps.size(); i++) {
            tmpMap = (CMapControl)(m_pickmaps.elementAt(i));
            if (i == index)
                return tmpMap;
        }
        return null;
    }

    /**
     * Set pickmap with given tab index to be the active one (ontop)
     * @param index tab index
     */
    public void setActivePickmap(int index) {
        if (index >= 0 && index < tabpane.getTabCount())
        {
          tabpane.setSelectedIndex(index);
        }
    }

    /**
     * update info which pickmap is currently on top
     */
    private void updateActivePickmap() {
        CMapControl tmp;
        boolean foundMap = false;

        if (tabpane == null) return; // for safety, shouldn't happen
        if (m_pickmaps.size() == 0) {
            m_currentPickMap = null;
            return;
        }

        String newName = File.separator+tabpane.getTitleAt(tabpane.getSelectedIndex());

        for (int i=0; i<m_pickmaps.size() && foundMap == false; i++) {
            tmp = (CMapControl)m_pickmaps.elementAt(i);
            if (tmp != null && tmp.getMapFileName().endsWith(newName)) {
                // this is the new active pickmap
                m_currentPickMap = tmp; // <- new pickmap
                foundMap = true;
                //System.out.println("new pickmap: "+newName);
            }
        }

        if (!foundMap && m_control.getMainView().isPickmapActive() && loadComplete) {
            // error: the new selected pickmap couldn't be found
            System.out.println("Bad Error in CPickmapPanel.updateActivePickmap:");
            System.out.println("-> Selected pickmap couldn't be found!");
        }
    }

    /**
     * Add the PickmapSelectionListener to the pickmap tabbed panel
     * @param pickpane   the panel with pickmaps
     */
    public void addPickmapSelectionListener(JTabbedPane pickpane) {
        pickpane.addChangeListener(new PickmapSelectionListener(
            m_control.getMainView(), pickpane));
    }

    /**
     * Add the ArchNPickChangeListener to the panel containing both
     * arcglist and pickmaps.
     * @param pane   the left-side panel
     */
    public void addArchNPickChangeListener(JTabbedPane pane) {
        pane.addChangeListener(new ArchNPickChangeListener(
            m_control.getMainView(), pane));
    }

    // ------------------------ subclasses ------------------------

    /**
     * listener class to keep track of the currently active pickmap
     */
    public class PickmapSelectionListener implements ChangeListener{
        private CMainView mainview;    // main view
        private String active_pickmap; // file-name of active pickmap

        public PickmapSelectionListener(CMainView mv, JTabbedPane pane) {
            mainview = mv;
            tabpane = pane;
            active_pickmap = null;
        }

        public void stateChanged(ChangeEvent e) {
            if (active_pickmap == null || active_pickmap.length()<=0 ||
                !tabpane.getTitleAt(tabpane.getSelectedIndex()).equals(active_pickmap)) {
                // new pickmap is active
                updateActivePickmap();
            }
            CPickmapPanel.getInstance().getCurrentPickmap().m_view.setHotspot(-1,-1);
            CPickmapPanel.getInstance().getCurrentPickmap().m_view.unHighlight();
            m_control.showArchPanelQuickObject(null);       // send it to quick view
        }
    }

    /**
     * In the left-side panel, archlist and pickmaps are exclusive
     * (only the one being displayed is active).
     * This listener gets to know which of them is active and
     * keeps the main view informed whenever the state changes.
     */
    public class ArchNPickChangeListener implements ChangeListener {
        CMainView mainview;   // main view
        JTabbedPane tabpane;  // parent pane for archlist & pickmaps
        int selectedIndex;    // current state of selection

        /**
         * Constructor
         * @param mview    the main view
         * @param pane     the JTabbedPane containing both archlist and pickmaps
         */
        public ArchNPickChangeListener(CMainView mview, JTabbedPane pane) {
            mainview = mview;
            tabpane = pane;
            selectedIndex = tabpane.getSelectedIndex();
        }

        public void stateChanged(ChangeEvent e) {
            if (tabpane.getSelectedIndex() != selectedIndex) {
                // the state has changed, user has switched panels
                if (tabpane.getSelectedIndex() == 0)
                    mainview.setPickmapActive(false);
                else
                    mainview.setPickmapActive(true);

                selectedIndex = tabpane.getSelectedIndex(); // save new state
            }
        }
    }
}
