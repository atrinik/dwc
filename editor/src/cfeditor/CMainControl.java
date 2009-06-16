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

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;
import java.io.File;
import java.awt.event.*;
import java.beans.*;


import cfeditor.textedit.scripteditor.ScriptEditControl;

/**
 * The main controller of the level editor. Basically the main application.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMainControl extends JFrame {
    private static CMainControl mStatic_control = null;

    public static final String DOCU_VERSION_KEY = "docuVersion";

    public static final String MAP_DIR_KEY      = "mapDirectory";
    public static final String DEFAULT_MAP_DIR  = (IGUIConstants.isoView ? "../maps" : "maps");

    public static final String ARCH_DIR_KEY     = "archDirectory";
    public static final String DEFAULT_ARCH_DIR = (IGUIConstants.isoView ? "../arch" : "arch");

    public static final String SCRIPT_DIR_KEY   = "scriptDirectory";
    public static final String DEFAULT_SCRIPT_DIR = "script";

    public static final String USE_IMAGESET     = "useImageSet";
    public static final String USE_FONT         = "useFont";
    public static final String LOAD_ARCH_COLL   = "loadArchFromCollection";

    public static final String PICKMAPS_LOCKED  = "pickmapsLocked";

    /** The main view. */
    private CMainView m_view;
    int tileEdit;

    CMapFileDecode mapFileDecoder;
    CMapFileEncode mapFileEncoder;

    ArchObjectParser archObjectParser;
    AnimationObject animationObject;

    /** The parameters passed to this controller. */
    private String[] m_astrParams;

    // icons for the map and arch pictures . X= rectangle, normal = iso
    ImageIcon mapSelIcon;
    ImageIcon mapGridIcon;
    ImageIcon unknownTileIcon;
    ImageIcon nofaceTileIcon;

    ImageIcon mapSelIconX;
    ImageIcon mapGridIconX;
    ImageIcon unknownTileIconX;
    ImageIcon nofaceTileIconX;
    ImageIcon noarchTileIcon;
    ImageIcon noarchTileIconX;

    private ArchObjectStack archList; // the one and only arch list

    /** All open maps. */
    private Vector m_levels = new Vector(1,2);

    /** The current top map we are working with */
    CMapControl m_currentMap;

    /** The current main directory. */
    private File m_currentDir;
    String strCurrentDir;

    public Random m_generator = new Random(System.currentTimeMillis()+19580427);
    // resource directories
    public File m_mapDir;
    private File m_archDir;
    private File m_scriptDir;

    private String m_strMapDir;
    private String m_strArchDir;
    private String m_strScriptDir;
    private String m_strImageDir = null; // directory for saving map images

    // this flag indicates weither the user has ever changed the
    // active dir since the program started
    boolean has_changed_dir = false;

    private boolean autojoin = false; // indicates weither autojoining is on/off

    //private boolean isoMapViewDefault;
    String  imageSet;                 // Name of used Image Set (null = none)
    private boolean load_from_archive = true; // do we load arches from the collected archives?
    private boolean autoPopupDocu = false;    // time for an automated docu popup?

    // fonts used
    private Font plainFont = null;    // the plain font
    private Font boldFont = null;     // the bold font

    // buffer managing copy data
    private CopyBuffer copybuffer = new CopyBuffer(this);

    // head of linked list, containing the autojoin data
    AutojoinList joinlist = null;

    // the list of archtype-data (loaded from "types.txt")
    CFArchTypeList typelist = null;

    // pickmaps cannot be edited while lockedPickmaps is true
    private boolean pickmapsLocked = false;

    /**
     * Constructs the main controller and its model and view.
     * @param astrParams The parameters passed to this controller.
     */
    public CMainControl( String[] astrParams ) {
        archList = new ArchObjectStack(this);
        m_view = new CMainView( this );
        m_astrParams = astrParams;

        mStatic_control = this;

        tileEdit = 0;
    }

    /**
     * This static instance may only be accessed from this package
     * @return static instance of this class
     */
    static CMainControl getInstance() {
        return mStatic_control;
    }

    /**
     * Initialises this main controller.
     *@exception CGridderException Thrown if initialisation fails.
     */
    void init() throws CGridderException {
        // Register ourselves to the undo/redo stack
        CUndoStack.setMainControl( this );

        // Get the current directory
        strCurrentDir = System.getProperty( "user.dir" );
        m_currentDir = new File( strCurrentDir );

        readGlobalSettings();

        // initialize pickmap panel (needed early during the loading process)
        CPickmapPanel pickmappanel = new CPickmapPanel();

        // apply custom font
        String font_desc = CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(USE_FONT);
        if (font_desc != null) {
            int fontsize=0; // fontsize

            try {
                // try to parse fontsize
                fontsize = Integer.parseInt(font_desc.substring(font_desc.indexOf("|")+1));

                // okay, now set fonts
                newPlainFont(new Font(font_desc.substring(0, font_desc.indexOf("|")),
                             Font.PLAIN, fontsize));
                newBoldFont(new Font(font_desc.substring(0, font_desc.indexOf("|")),
                             Font.BOLD, fontsize));
            }catch (NumberFormatException e) {}
        }

        // set default font for all swing components
        if (getPlainFont() != null)
            JFontChooser.setUIFont(getPlainFont());

        // initialize & load
        if (IGUIConstants.isoView)
            MultiPositionData.init();

        // initialize the script-editor pad
        ScriptEditControl.init(getMapDefaultFolder(), this);

        // Initialise the main view
        m_view.init();
        loadDefTiles();

        if (autoPopupDocu) {
            // do an automated help popup because the docu version has increased
            // (people won't notice the docu otherwise - nobody expects a docu in opensource)
            this.openHelpWindow();
            autoPopupDocu = false;
        }

        // attach map encoder and decoder
        mapFileDecoder = new CMapFileDecode();
        mapFileEncoder = new CMapFileEncode(this);

        ArchObject.setMControl(this);
        // our global object parser
        archObjectParser = new ArchObjectParser(this);
        animationObject = new AnimationObject(this);

        // read in the type & type field definitions
        archObjectParser.loadTypeNumbers();
        //loadTypeDef();

        // load the list with archtype-data from "types.txt"
        typelist = new CFArchTypeList();

        ArchObject.setTypeList(typelist);  // set reference in ArchObject

        // now collect all arch you can find in the arch path!!
        System.gc();
        m_view.updateFocus(false); /*MTMT*/
    }
    /**
     * Loading the joinlist from file and attaching all to 'joinlist'.
     * (This method must not be called before all arches are loaded
     * into the ArchObjectStack 'archList'!)
     */
    void load_joinlist() {
        joinlist = new AutojoinList();
        if (!joinlist.loadList(archList))
            joinlist = null;
    }

    void moveTileUp(ArchObject arch, boolean refresh) {
        m_currentMap.m_model.moveTileUp(arch, refresh);
    }

    void moveTileDown(ArchObject arch, boolean refresh) {
        m_currentMap.m_model.moveTileDown(arch, refresh);
    }

    /**
     * collect CF arches
     */
    public void collectCFArches() {
        if (ArchObjectStack.getLoadStatus() != ArchObjectStack.IS_COMPLETE) {
            // must not collect arches while arch stack not complete
            showMessage("Arches still Loading",
                        "You have to wait for all arches to be loaded\n"+
                        "before you can collect them.");
            return;
        }

        archList.collectArches();
    }

    /**
     * Set the mapiew to show tiles of the given type.
     * (If no tileEdit is set, everything is displayed)
     *
     * @param v     tileedit bitmask of types to show
     */
    public void setTileEdit(int v) {
        tileEdit|=v;
    }

    /**
     * Set the mapiew to hide tiles of the given type.
     * (If no tileEdit is set, everything is displayed)
     *
     * @param v     tileedit bitmask of types to hide
     */
    public void unsetTileEdit(int v) {
        tileEdit&=~v;
    }

    /**
     * Get information on the current state of tileEdit:
     * Are tiles of type 'v' displayed?
     *
     * @param v     are tiles of this type displayed?
     * @return true if these tiles are currently displayed
     */
    public boolean isTileEdit(int v) {
        if(v==0) {
            if((tileEdit& IGUIConstants.TILE_EDIT_NONE)  != 0)
                return(true);
            else
                return(false);
        }
        if((tileEdit&v) != 0)
            return(true);
        else
            return(false);
    }

    /**
     * @return true when a tileEdit value is set, so that not
     * all tiles are displayed.
     */
    public boolean isTileEditSet() {
        return ((tileEdit & IGUIConstants.TILE_EDIT_NONE) == 0 &&
                tileEdit != 0);
    }

    /**
     * A new edit type was selected from the view menu. Now
     * we activate the new type and calculate it for each arch on
     * each map where this type has not yet been used.
     *
     * @param new_type     new selected edit type (should not be more than one)
     */
    void select_edittype(int new_type) {
        // calculate the new type for all opened maps:
        for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); ) {
            CMapControl level = (CMapControl) enu.nextElement();

            level.add_edit_type(new_type); // calculate new type
        }

        setTileEdit(new_type); // activate the new type for all views
    }

    public CMapControl getMapSaveStatusByPath(String path)
    {
      for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); )
      {
        CMapControl level = (CMapControl) enu.nextElement();
        try
        {
          if(level.mapFile.getCanonicalPath().toString().compareTo(path) == 0)
            return level;
        } catch (IOException e)
        {
          // ignore, get next map
        }
      }
      return null;
    }

    public void setMapSaveStatusByPath(String path)
    {
      for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); )
      {
        CMapControl level = (CMapControl) enu.nextElement();
        try
        {
          if(level.mapFile.getCanonicalPath().toString().compareTo(path) == 0)
          {
            level.save();
            return;
          }
        } catch (IOException e)
        {
          // ignore, get next map
        }
      }
    }

    // get/set autojoin state
    public void setAutojoin(boolean state) {
        autojoin = state;
    }

    public boolean getAutojoin() {
        return autojoin;
    }

    public void openHelpWindow()        {
        m_view.openHelpWindow();
    }

    // access to ArchNodeList
    // remark: i use before some weird access to it, use this instead when you find it
    public void addArchToList (ArchObject data) {
        archList.addArchToList (data);
    }

    public void incArchObjCount() {archList.incArchObjCount();}
    public int getArchObjCount() {return(archList.archObjCount);}
    public int getArchCount() {return(archList.getArchCount());}
    public ArchObject getArch(int i) {return(archList.getArch(i));}
    public CopyBuffer getCopyBuffer() {return copybuffer;}

    /** IMPORTANT: plainFont can be null! */
    public Font getPlainFont() {return plainFont;}
    /** IMPORTANT: boldFont can be null! */
    public Font getBoldFont() {return boldFont;}

    public void newPlainFont(Font newfont) {plainFont = newfont;}
    public void newBoldFont(Font newfont) {boldFont = newfont;}

    public boolean isBigFont() {return (((getPlainFont()==null)?JFontChooser.default_font.getSize():
                                        getPlainFont().getSize()) > 13);}

    void setMapAndArchPosition(int archid, int x, int y) {
        m_currentMap.m_view.setMapAndArchPosition(archid, x,y);
    }

    /**
     * Set all global settings (mostly read from the
     * 'CFJavaEditor.ini'-file)
     */
    void readGlobalSettings() {
        // Get the directories
        m_strMapDir = CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
            MAP_DIR_KEY, DEFAULT_MAP_DIR );
        m_strArchDir = CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
            ARCH_DIR_KEY, DEFAULT_ARCH_DIR );
        m_strScriptDir = CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
            SCRIPT_DIR_KEY, DEFAULT_SCRIPT_DIR );

        // set map dir
        if (m_strMapDir.length() > 0)
            m_mapDir = new File ( m_strMapDir );
        else {
            // if map dir not set, default to current dir
            if (!m_currentDir.exists())
                System.out.println("Error in readGlobalSettings(): current dir doesn't exist!");
            m_mapDir = new File(m_currentDir.getAbsolutePath());
        }
        // if mapdir has no absolute path, set it now
        if (m_currentDir.exists() && m_mapDir.getParent() == null &&
            !m_mapDir.isAbsolute() && !has_changed_dir)
            m_mapDir = new File(m_currentDir.getAbsolutePath(), m_mapDir.getPath());

        m_archDir =  new File (m_strArchDir);
        m_scriptDir =  new File (m_strScriptDir);

        imageSet = new String(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                              USE_IMAGESET, (IGUIConstants.isoView ? "none" : "base")));
        if (imageSet.equalsIgnoreCase("none")) imageSet = null;

        load_from_archive = new Boolean(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                              LOAD_ARCH_COLL, "false")).booleanValue();

        this.getMainView().setMapTileListBottom(new Boolean(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                            CMainView.MAPTILE_BOTTOM_KEY, "false")).booleanValue());

        setPickmapsLocked(new Boolean(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                          PICKMAPS_LOCKED, "false")).booleanValue());

        // docu version
        if (IGUIConstants.DOCU_VERSION > (new Integer(CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(
                              DOCU_VERSION_KEY, "0"))).intValue()) {
            // remember to open docu
            autoPopupDocu = true;
            // update docu version right now, because we want the help popup only one time
            CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(DOCU_VERSION_KEY, String.valueOf(IGUIConstants.DOCU_VERSION) );
        }
    }

    /**
     * Write global settings into the 'CFJavaEditor.ini'-file
     *
     * @param arch          path of arch directory
     * @param map           path of map directory
     * @param script        path of script directory
     * @param baseImageSet  true if base image set is used
     */
    void setGlobalSettings(String arch, String map, String script, boolean baseImageSet,
                           boolean load, boolean mapTileBottom) {
        map = map.replace('\\', '/');
        if (map.endsWith("/"))
            map = map.substring(0, map.length()-1); // path should not end with slash

        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(ARCH_DIR_KEY, arch );
        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(MAP_DIR_KEY, map );
        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(SCRIPT_DIR_KEY, script );

        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(USE_IMAGESET, baseImageSet?"base":"none" );
        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(LOAD_ARCH_COLL, load?"true":"false" );
        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(CMainView.MAPTILE_BOTTOM_KEY, mapTileBottom?"true":"false" );
        CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(PICKMAPS_LOCKED, pickmapsLocked?"true":"false" );

        readGlobalSettings();
        refreshMenusAndToolbars();
    }

    static class myMapFileFilter extends javax.swing.filechooser.FileFilter
    {
            /** Accept the filename and display on the dialog window*/
            public boolean accept(File f)
            {
                    String fileName = f.getName();
                    return ((!fileName.endsWith(".py") &&!fileName.endsWith(".text")&&!fileName.endsWith(".txt")) || f.isDirectory());
            }

            public String getDescription() { return "Map Files"; }
    }

    static class myPyFileFilter extends javax.swing.filechooser.FileFilter
    {
            /** Accept the filename and display on the dialog window*/
            public boolean accept(File f)
            {
                    String fileName = f.getName();
                    return (fileName.endsWith(".py") || f.isDirectory());
            }

            public String getDescription() { return "Python Script Files"; }
    }

    public String getMapDefaultFolder() {
        return(m_strMapDir);
    }

    public String getArchDefaultFolder() {
        return(m_strArchDir);
    }

    public String getScriptDefaultFolder() {
        return(m_strScriptDir);
    }

    public boolean isArchLoadedFromCollection() {
        return load_from_archive;
    }

    public boolean isPickmapsLocked() {
        return pickmapsLocked;
    }

    /**
     * refresh the active map view, if there is one
     */
    void refreshCurrentMap() {
        m_view.RefreshMapTileList(); // update tile window
        if (m_currentMap != null)
            m_currentMap.repaint();    // update map view (if there is one)
    }

    // ask arch panel which arch is selectd
    public ArchObject getArchPanelSelection() {
        return(m_view.getArchPanelSelection());
    }

    // setup quick view window
    public void showArchPanelQuickObject(ArchObject arch) {
        m_view.showArchPanelQuickObject(arch);
    }

    /**
     * set bold font to the given component
     * @param comp    component
     * @return        same component from parameter (but now with accurate font)
     */
    public JComponent setBoldFont(JComponent comp) {
        if (boldFont != null) comp.setFont(boldFont);
        return comp;
    }

    /**
     * set plain font to the given component
     * @param comp    component
     * @return        same component from parameter (but now with accurate font)
     */
    public JComponent setPlainFont(JComponent comp) {
        if (plainFont != null) comp.setFont(plainFont);
        return comp;
    }

    public String MsgToHtml(String msg) {
        return JFontChooser.MsgToHtml(msg, getPlainFont());
    }

    boolean addArchToMap (int archnr, int mapx, int mapy, int intern, boolean join) {
        return m_currentMap.addArchToMap(archnr, mapx, mapy, intern, join);
    }

    boolean insertArchToMap(ArchObject newarch, int archnr, ArchObject next, int mapx, int mapy, boolean join) {
        return  m_currentMap.insertArchToMap(newarch, archnr, next, mapx, mapy, join);
    }

    public void deleteMapArch(int index, int mapx, int mapy, boolean refresh_map, boolean join) {
        m_currentMap.deleteMapArch(index, mapx, mapy, refresh_map, join);
    }

    public ArchObject getMapArch(int index, int mapx, int mapy) {
        return(m_currentMap.getMapArch(index, mapx, mapy));
    }


    public CMainView getMainView() {
        return(m_view);
    }

    public void addArchPanel(String name) {
        m_view.addArchPanel(name);
    }

    public void disableTabPane() {
        m_view.disableTabPane();
    }

    public void enableTabPane() {
        m_view.enableTabPane();
    }

    public int addArchPanelCombo(String name) {
        return(m_view.addArchPanelCombo(name));
    }

    public void addArchPanelArch(int archnr, int index) {
        m_view.addArchPanelArch(archnr, index);
    }

    // selected arch in arch panel
    public int getPanelArch() {
        return(m_view.getPanelArch());
    }

    public ImageIcon getFace(int i) {
        return(archList.getFace(i));
    }

    public String getFaceName(int i) {
        return(archList.getFaceName(i));
    }

    public void SetStatusText(String string) {
        m_view.SetStatusText(string);
    }

    public ArchObjectStack getArchObjectStack() {
        return(archList);
    }

    /**
     * Sets the used look'n'feel to be the specified l'n'f.
     *@param strClassName The name of the l'n'f class name to use.
     */
    public void setLookNFeel( String strClassName ) {
        try {
            UIManager.setLookAndFeel( strClassName );
            SwingUtilities.updateComponentTreeUI( m_view );

            // update the look and feel for all open map views
            if (m_levels.size() > 0) {
                for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); ) {
                    CMapControl level = (CMapControl) enu.nextElement();
                    level.m_view.updateLookAndFeel();
                    System.out.println("map "+level.getMapName());
                }
            }
        } catch( UnsupportedLookAndFeelException noSuchLNF ) {
        } catch( IllegalAccessException noAccess ) {
        } catch( InstantiationException noInstance ) {
        } catch( ClassNotFoundException noClass ) {
        }
    }

    // Set grid of level, if there is a level
    public boolean isGridVisible() {
        if(m_currentMap == null)
            return(false);
        else
            return (m_currentMap.m_view.isGridVisible());
    }

    public void setGridVisibility( boolean fVisible ) {
        if(m_currentMap != null)
            m_currentMap.m_view.setGridVisibility(fVisible);
    }

    /**
     * Invoked when user wants to begin editing a new (empty) map.
     */
    public void newLevelWanted() {
        newLevelWanted(null);
    }

    /**
     * Invoked when user wants to begin editing a new (empty) map.
     * @param filename   desired filename for the new map, null if not specified
     */
    public void newLevelWanted(String filename) {
        try {
            CMainStatusbar.getInstance().setText(" Creating new map...");
            CNewMapDialog levelDialog = new CNewMapDialog(this, m_view, filename, CNewMapDialog.TYPE_CFMAP);
            CMainStatusbar.getInstance().setText("");
        } catch( CGridderException error ) {
            CMainStatusbar.getInstance().setText(" Failed to create new map!");
            handleErrors( error );
        }
    }

    /**
     * Invoked when user wants to open a new pickmap
     * @param filename   desired filename for the new map, null if not specified
     */
    public void newPickmapWanted() {
        try {
            CMainStatusbar.getInstance().setText(" Creating new pickmap...");
            CNewMapDialog levelDialog = new CNewMapDialog(this, m_view, null, CNewMapDialog.TYPE_PICKMAP);
            CMainStatusbar.getInstance().setText("");
        } catch( CGridderException error ) {
            CMainStatusbar.getInstance().setText(" Failed to create new pickmap!");
            handleErrors( error );
        }
    }

    /**
     * Begins the editing of a new Map.
     *
     * @param start        first object in the list of map objects.
     *                     'start' is null for new, empty maps
     * @param maparch      map arch
     * @return             map control of new map
     */
    public CMapControl newLevel(ArchObject start, MapArchObject maparch) {
        // Create a new level control and set the level view from that
        CMainStatusbar.getInstance().setText(" Creating new map "+maparch.getMapName());

        CMapControl map;
        try {
            map = new CMapControl(this, start, maparch, false);
            m_view.addLevelView( map.m_view ); // one view...
            map.m_view.setAutoscrolls(true);
            m_levels.addElement( map );
            setCurrentLevel( map );
            refreshMenusAndToolbars();

        } catch( CGridderException error) {
            map = null;
            CMainStatusbar.getInstance().setText("Failed to create new map.");
            handleErrors( error );
        }
        // The garbage collector might be nice here, but it consumes
        // so much time that it's unbearable.
        //System.gc();
        return(map);
    }

    /**
     * Invoked when the user wants to close the current level.
     */
    public void closeCurrentLevelWanted() {
        if ( m_currentMap != null ) {
            closeLevel(m_currentMap, false);
            if(m_currentMap!=null)
                m_view.setCurrentLevelView(m_currentMap.m_view);
        }
    }

    /**
     * Invoked when the user wants to close all levels.
     */
    public void closeAllLevelsWanted() {
        for (;m_levels.size() > 0; ) {
            closeLevel(m_currentMap, false);
            if(m_currentMap!=null)
                m_view.setCurrentLevelView(m_currentMap.m_view);
        }
    }

    /**
     * Invoked when the user wants to close the active pickmap
     */
    public void closeActivePickmapWanted() {
        if (!CPickmapPanel.getInstance().isLoadComplete()) {
            showMessage("Cannot close Pickmap", "Pickmaps aren't loaded.\n"+
                        "Either there are no pickmaps or the loading process is not complete.");
        }
        else {
            if (!getMainView().isPickmapActive()) {
                showMessage("Cannot close Pickmap", "Pickmaps are currently hidden.\n"+
                            "Please select a pickmap before activating this command.");
            }
            else {
                CMapControl activePickmap = CPickmapPanel.getInstance().getCurrentPickmap();
                if (activePickmap == null) {
                    showMessage("Cannot close Pickmap", "There are no pickmaps.");
                }
                else {
                    // if pickmap was modified, ask for confirmation:
                    if (!activePickmap.isLevelChanged() || askConfirm("Close Pickmap "+activePickmap.getMapFileName() +"?",
                        "If you close the pickmap '"+activePickmap.getMapFileName()+"', all recent\n"+
                        "changes will be lost. Do you really want to close it?")) {
                        File pickmapFile = activePickmap.mapFile;
                        // close pickmap
                        closeLevel(activePickmap, true);

                        // also delete pickmap file?
                        if (askConfirm("Delete File "+activePickmap.getMapFileName() +"?",
                            "The pickmap '"+activePickmap.getMapFileName()+"' has been closed.\n"+
                            "Do you also want to remove the pickmap file '"+activePickmap.getMapFileName()+"' from your harddisk?\n"+
                            "(Doing so will permanently delete the pickmap.)")) {
                            pickmapFile.delete();
                        }
                    }
                }
            }
        }
    }

    /**
     * Closes the given level.
     * @param level    the level to close.
     * @param forced   when true, user does not get asked and changes do not get saved
     * @return         true if closing successful
     */
    public boolean closeLevel(CMapControl level, boolean forced) {

        if(level == null) {
            showMessage("CLOSE LEVEL", "FIND NULL LEVEL : "+m_levels.size()+" our map: "+m_currentMap);
        }

        if (level != null && !forced && level.isLevelChanged()) {
            if (askConfirm("Do You Want To Save Changes?",
                           "Do you want to save changes to map "+
                           level.getMapName()+"?" )) {

                if (level.isPlainSaveEnabled()) {
                    level.save();
                } else {
                    saveLevelAsWanted(level);
                }
            }
        }

        if (level.isPickmap()) {
            // special case: close a pickmap
            CPickmapPanel.getInstance().closePickmap(level);
            level.levelCloseNotify();
        }
        else {
            // Notify the level about the closing
            m_view.setMapTileList(null,-1);
            level.levelCloseNotify();
            m_view.removeLevelView( level.m_view);
            m_levels.removeElement( level );
            m_currentMap = null;

            if ( m_levels.size() > 0 ) {
                // get next open map we can find and set it to m_currentMap
                for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); )
                    m_currentMap = (CMapControl) enu.nextElement();
            }
        }
        refreshMenusAndToolbars();
        System.gc();
        return true;
    }

    /**
     * Open active pickmap as normal map for extensive editing
     */
    public void openActivePickmapAsMapWanted() {
        if (!CPickmapPanel.getInstance().isLoadComplete()) {
            showMessage("Cannot open Pickmap", "Pickmaps aren't loaded.\n"+
                        "Either there are no pickmaps or the loading process is not complete.");
        }
        else {
            if (!getMainView().isPickmapActive()) {
                showMessage("Cannot open Pickmap", "Pickmaps are currently hidden.\n"+
                            "Please select a pickmap before activating this command.");
            }
            else {
                CMapControl activePickmap = CPickmapPanel.getInstance().getCurrentPickmap();
                if (activePickmap == null) {
                    showMessage("Cannot open Pickmap", "There are no pickmaps.");
                }
                else {
                    // open pickmap as map
                    File pickmapFile = activePickmap.mapFile;
                    if (!pickmapFile.exists()) {
                        if(askConfirm("Cannot open Pickmap", "The map file for '"+activePickmap.getMapFileName()+"' does not exist.\n"+
                                      "Do you want to create the file by saving this pickmap?")) {
                            saveActivePickmapWanted();
                            openFile(pickmapFile);
                        }
                    }
                    else
                        openFile(pickmapFile);
                }
            }
        }
    }

    /**
     * Invoked when user wants to open a file.
     */
    public void openFileWanted() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Map Or Script File");
        fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fileChooser.setMultiSelectionEnabled( false );
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new myPyFileFilter());
        fileChooser.setFileFilter(new myMapFileFilter());

        // default folder is the map-folder at first time, then the active folder
        if (!has_changed_dir && m_mapDir.exists()) {
            fileChooser.setCurrentDirectory( m_mapDir );
        } else if (m_currentDir.exists()) {
            fileChooser.setCurrentDirectory( m_currentDir );
        }

        int returnVal = fileChooser.showOpenDialog( m_view );
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if (ArchObjectStack.getLoadStatus() == ArchObjectStack.IS_LOADING) {
                // ArchStack still loading -> abort!
                showMessage("Cannot open Map", "Are you nuts?! :-)\n"+
                            "All arches have to be loaded before you can open a map.\n"+
                            "Just be patient and wait a few seconds...");
                return;
            }
            else if (ArchObjectStack.getLoadStatus() == ArchObjectStack.IS_EMPTY) {
                // ArchStack is empty -> abort!
                showMessage("Cannot open Map", "There are currently no arches avaliable!\n"+
                            "You need to have arches loaded before opening a map.\n"+
                            "Look into the online help on how to get Crossfire archfiles.");
                return;
            }

            has_changed_dir = true; // user has chosen an active dir
            File file = fileChooser.getSelectedFile();
            if ((file.getName().endsWith(".py") || file.getName().endsWith(".PY")) && !file.isDirectory()) {
                // user selected a python script - well, why not...
                m_currentDir = fileChooser.getCurrentDirectory();
                if (file.exists())
                    ScriptEditControl.getInstance().openScriptFile(file.getAbsolutePath());
                else
                    ScriptEditControl.getInstance().openScriptNew();
            }
            else {
                // it's a map file, most likely
                if (file.exists() && !file.isDirectory()) {
                    // everything okay do far, now open up that mapfile
                    m_currentDir = fileChooser.getCurrentDirectory();
                    openFile( file );
                }
                else {
                    // user entered a filename which doesn't yet exist -> create new map
                    newLevelWanted(file.getName());
                }
            }
        }
    }

    /**
     * load a mapfile
     * @param file      mapfile
     */
    void openFile(File file) {
        ArchObject start;
        MapArchObject maparch;

        try {
            start = mapFileDecoder.decodeMapFile(file, this); // parse mapfile
            maparch = mapFileDecoder.getMapArch();       // get map arch
        }
        catch (CGridderException e) {
            // popup display
            showMessage("Couldn't load Map", e.getMessage());
            return;
        }
        catch (OutOfMemoryError e) {
            // out of memory!! - display error
            showMessage("Out of Memory", "Not enough memory available to open more maps!\n"+
                        "You can increase the memory limit by using the '-mx' runtime flag.\n"+
                        "For example: 'java -mx128m -jar "+IGUIConstants.APP_NAME+".jar'",
                        JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ok, we have it all!!

        if(start == null) {
            // The map is totally empty
            newLevel(start, maparch); // init the map
        } else {
            // go to ArchObjectParser and add the default arch list information to them
            if(collectTempList(start, file)==false) // get face names, face id, etc.
                return;
            start = sortTempList(start);  // sort the list (put multiparts at the end)
            newLevel(start, maparch);     // init the map
            deleteTempList(start);            // remove temp list connection
        }

        // finally, show the map and refresh toolbars
        m_currentMap.setMapFile(file);
        m_currentMap.setActive_edit_type(tileEdit);   // map is loaded with current view settings
        m_currentMap.m_model.resetLevelChangedFlag();
        refreshMenusAndToolbars();
        start = null;
    }

    /**
     * Open an attribute dialog window for the specified arch
     *
     * @param arch      attr. window is opened for this arch
     */
    public void openAttrDialog(ArchObject arch) {
        if (typelist.is_empty()) {
            // types.txt is missing!
            showMessage("File Missing",
                        "The definitions-file \"types.txt\" is missing! The\n"+
                        "attribute interface doesn't work without that file.");
        }
        else if (arch != null && arch.getNodeNr() != -1) {
            CAttribDialog dwin = new CAttribDialog(typelist, arch,
                                                   getArch(arch.getNodeNr()),
                                                   this);
        }
    }

    // save map!
    public void encodeMapFile(File file, MapArchObject map, ArchObject[][] m_grid) {
        mapFileEncoder.encodeMapFile(file, map, m_grid);
    }

    /**
     * browse first through the default arch list and attach map arches to it
     * then browse through the face list and try to find the pictures
     */
    boolean collectTempList(ArchObject arch, File f) {
        String noarch="";
        boolean noarchflag=false;
        int noarchcount=0;
        ArchObject previous = null;  // previous arch in loop
        Integer index;
        int i;

        // first: attach our map sucker to a default arch we have loaded
        for(;arch != null;) {
            index = (Integer) archList.getArchHashTable().get(arch.getArchName());
            if(index == null) {
                                // we had an unknown arch here!!
                                noarch +=arch.getArchName()+"\n";
                                noarchflag=true;
                                noarchcount++;
                                previous.setTemp(arch.getTemp());
                                arch.setTemp(null);
                                arch = previous;
                                previous = arch;
                                arch = arch.getTemp();
                                continue;
            } else {
                i = index.intValue();
                arch.setNodeNr(i);     // our default arch!
            }
            // Ok, now is attached to default arch and loaded png
            // NOW we post parse the object...
            // (We calculate only edit types that are active, to save time)
            archObjectParser.postParseMapArch(arch, tileEdit);
            // now lets assign the visible face - perhaps we have still a anim
            arch.setObjectFace();

            archObjectParser.expandMulti(arch);

            // if there's a tail without head, we cut it out
            if (arch.getRefFlag() && arch.getMapMultiHead() == null
                && previous != null) {
                System.out.println("WARNING: Found multi-tail without head: '"+
                                   arch.getArchName()+"'");
                previous.setTemp(arch.getTemp());
                arch = previous;
            }

            // ahhh ... finished... next sucker
            previous = arch;        // save previous
            arch = arch.getTemp();  // next sucker
        }
        if(noarchflag)
          showMessage("Loading Map File "+ f.getName(),"\n Found "+noarchcount+" unknown arch.\nAutodelete illegal arch:\n"+noarch);

        return true;
    }

    /**
     * Sorting the temp list
     * @param start    head-element of temp list before sorting
     * @return         new head-element of temp list after sorting
     */
    public ArchObject sortTempList(ArchObject start) {
        return archObjectParser.sortTempList(start);
    }

    /**
     * Remove the tmp. pointer connection after map inserting.
     * The ArchObjects themselves remain.
     *
     * @param arch       first arch on the map(/list)
     */
    void deleteTempList(ArchObject arch) {
        ArchObject temp;

        for(;arch != null;) {
            temp = arch;
            arch = arch.getTemp();
            temp.setTemp(null);
        }
    }

    /**
     * Invoked when user wants to save the current level.
     */
    public void saveCurrentLevelWanted() {
        if ( m_currentMap == null ) {
            return;
        }

        m_currentMap.save();
    }

    /**
     * Save current active pickmap
     */
    public void saveActivePickmapWanted() {
        if (!CPickmapPanel.getInstance().isLoadComplete()) {
            showMessage("Cannot save Pickmap", "Pickmaps aren't loaded.\n"+
                        "Either there are no pickmaps or the loading process is not complete.");
        }
        else {
            if (!getMainView().isPickmapActive()) {
                showMessage("Cannot save Pickmap", "Pickmaps are currently hidden.\n"+
                            "Please select a pickmap before activating this command.");
            }
            else {
                CMapControl activePickmap = CPickmapPanel.getInstance().getCurrentPickmap();
                if (activePickmap == null) {
                    showMessage("Cannot save Pickmap", "There are no pickmaps.");
                }
                else {
                    activePickmap.save();
                }
            }
        }
    }

    /**
     * Invoked when user wants to save the current level to certain file.
     */
    public void saveCurrentLevelAsWanted() {
        saveLevelAsWanted( m_currentMap );
    }

    /**
     * Invoked when user wants to save a level to certain file.
     * @param level    map control of the map to be saved
     */
    public void saveLevelAsWanted( CMapControl level ) {
        if ( level == null ) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Map Or Script As");
        fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new myPyFileFilter());
        fileChooser.setFileFilter(new myMapFileFilter());

        // default folder is the map-folder at first time, then the active folder
        if (!has_changed_dir && m_mapDir.exists()) {
            fileChooser.setCurrentDirectory( m_mapDir );
        } else if (m_currentDir.exists()) {
            fileChooser.setCurrentDirectory( m_currentDir );
        }

        // if file already exists, select it
        if (level.mapFile!=null && level.mapFile.exists())
            fileChooser.setSelectedFile(level.mapFile);
        else
            fileChooser.setSelectedFile(new File(m_mapDir, level.getMapFileName()));

        int returnVal = fileChooser.showSaveDialog( m_view );
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            has_changed_dir = true; // user has chosen an active dir
            File file = fileChooser.getSelectedFile();
            level.saveAs( file );
            level.setMapFileName(file.getName()); // window title and file name
            //level.setMapName(file.getName());   // map name (internal)
            level.setMapFile(file);

            m_currentDir = fileChooser.getCurrentDirectory();
            refreshMenusAndToolbars();
        }
    }

    /**
     * Create an image of the current map and save it as file.
     * In this method, a filechooser is opened to let the user select
     * an output file name/path for the png image.
     */
    public void createImageWanted() {
        CMapControl mc = m_currentMap; // control of current map
        String filename = null;

        if (mc == null) {
            // there is no map open (should not happen due to disabled menus)
            showMessage("No Map Open", "You cannot create an image when there is no map open.");
        }
        else {
            try {
                if (m_strImageDir == null)
                    m_strImageDir = m_mapDir.getAbsolutePath();

                filename = m_strImageDir+File.separator+mc.getMapFileName()+".png";
                JFileChooser fileChooser = new JFileChooser(m_strImageDir);
                fileChooser.setDialogTitle("Save Image As");
                fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setSelectedFile(new File(filename));
                // set a file filter for "*.png" files
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public String getDescription() {
                        return "*.png";
                    }

                    public boolean accept(File f) {
                        if (f.isDirectory() || f.getName().endsWith(".png"))
                            return true;
                        return false;
                    }
                });

                int returnVal = fileChooser.showSaveDialog( m_view );
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    // got the filepath, now create image
                    filename = fileChooser.getSelectedFile().getAbsolutePath();
                    m_strImageDir = fileChooser.getSelectedFile().getParentFile().getAbsolutePath();
                    if (!filename.endsWith(".png"))
                        filename+=".png";
                    mc.m_view.printFullImage(filename);
                }
            }
            catch (IOException e) {
                showMessage("Couln't create Image", "The image could not be created because the"+
                            "file "+filename+" cannot be written.");
            }
        }
    }

    /**
     * Create an image of the current map and save it as file.
     * In this method, the filename is already given, so the image
     * is created directly (if possible).
     *
     * @param filename    Name of the png image file to create.
     */
    public void createImageWanted(String filename) {
        CMapControl mapc = m_currentMap; // control of current map

        if (mapc == null) {
            // there is no map open (should not happen due to disabled menus)
            showMessage("No Map Open", "You cannot create an image when there is no map open.");
        }
        else {
            try {
                if (!filename.endsWith(".png"))
                    filename+=".png";
                mapc.m_view.printFullImage(filename);
                System.out.println("Created image \""+filename+"\" of map "+mapc.getMapFileName()+".");
            }
            catch (IOException e) {
                showMessage("Couln't create Image", "The image could not be created because the"+
                            "file "+filename+" cannot be written.");
            }
        }
    }

    /**
     * Invoked when user wants to see/edit the level properties.
     */

    void mapPropertiesWanted() {
        showMapProperties( m_currentMap );
    }

    /**
     * Shows the given levels properties.
     *@param level The level whose properties we want.
     */
    void showMapProperties( CMapControl level ) {
        if ( level != null ) {
            try {
                CMapPropertiesDialog dialog =
                    new CMapPropertiesDialog( this, m_view, level );
            } catch( CGridderException error ) {
                handleErrors( error );
            }
        }
    }

    void OptionsWanted() {
        try {
            COptionDialog dialog =
                new COptionDialog( this, m_view);
        } catch( CGridderException error ) {
            handleErrors( error );
        }
    }

    /**
     * Sets the properties of the given level.
     *
     * @param level          map control
     * @param archText       map text
     * @param loreText       lore text
     * @param strMapTitle    map name
     * @param mapWidth       width of map
     * @param mapHeight      height of map
     * @param isIsoView      if true, the map is displayed in iso-view
     */
    void setLevelProperties( CMapControl level, String archText, String loreText, String strMapTitle,
                             int mapWidth, int mapHeight) {
        if ( level != null ) {
            level.setProperties(archText, loreText, strMapTitle, mapWidth, mapHeight);
            refreshMenusAndToolbars();
        }
    }

    void setPickmapsLocked(boolean state) {
        pickmapsLocked = state;
    }

    /**
     * Invoked when user wants to exit from the program.
     */
    void exitWanted() {
        if ( m_levels.size() > 0 ) {
            for (Enumeration enu = m_levels.elements(); enu.hasMoreElements(); ) {
                CMapControl level = (CMapControl) enu.nextElement();
                closeLevel(level, false);
            }
        }
        doExit();
    }

    /**
     * Exits from the program.
     */
    void doExit() {
        appExitNotify();
        System.exit(0);
    }

    /**
     * Try to load the map where the selected map-exit points to.
     */
    public void enterExitWanted() {
        ArchObject exit;    // selected exit object
        CMapControl oldmap; // store control of active map (to evt. close it later)
        String path;        // exit path
        int dx, dy;         // exit destination coords.

        exit = m_currentMap.m_model.getExit();

        if (exit == null) {
            // no exit found
            showMessage("No Exit Found", "There is no valid exit at the selected spot.");
            return;
        }

        path = exit.getAttributeString("slaying", getArch(exit.getNodeNr()));
        dx = exit.getAttributeValue("hp", getArch(exit.getNodeNr()));
        dy = exit.getAttributeValue("sp", getArch(exit.getNodeNr()));

		/* Gecko: fixed check for exits to the same map */
		File newfile = null;
		if (path.startsWith(File.pathSeparator) || path.startsWith("/")) {
			// we have an absolute path:
			newfile = new File(m_mapDir.getAbsolutePath(), path.substring(1));
		} else {
			// we have a relative path:
			if (m_currentMap.mapFile != null)
				newfile = new File(m_currentMap.mapFile.getParent(), path);
		}

        if (path.length() == 0 || (m_currentMap.mapFile != null && newfile != null &&
                                   newfile.equals(m_currentMap.mapFile))) {
            // path points to the same map
            if (dx==0 && dy==0)
                showMessage("Destination Invalid", "This exit points nowhere.");
            else if (m_currentMap.pointValid(dx, dy))
                m_currentMap.m_view.setHotspot(dx, dy);
            else
                showMessage("Destination Invalid", "The destination of this exit is outside the map.");
        }
        else {
            // path points to a different map
            if (newfile == null) {
				showMessage("Map not Saved", "Please save this map first.", JOptionPane.ERROR_MESSAGE);
				return;
            }

            if (!newfile.exists() || newfile.isDirectory()) {
                // The path is wrong
                showMessage("Invalid Path", "The specified path is invalid:\n");
                return;
            }

            /* its important to force the canonical file here or the
             * file path is added every time we use a ../ or a ./ .
             * This results in giant file names like "xx/../yy/../xx/../yy/.."
             * and after some times in buffer overflows.
             */
            try {
              newfile = newfile.getCanonicalFile();
            } catch (IOException e) {
                showMessage("Invalid Path", "Failed to load file for path.\n"+newfile.getAbsolutePath());
                return;
            }

            oldmap = m_currentMap; // store old map control
            openFile(newfile);     // open the new map

            if (dx==0 && dy==0) {
                // use the entry point defined by the map header
                dx=m_currentMap.m_model.getMapArchObject().getEnterX();
                dy=m_currentMap.m_model.getMapArchObject().getEnterY();
            }
            m_currentMap.m_view.setHotspot(dx, dy);  // set hotspot

            // Update the main view so the new map instantly pops up.
            m_view.update(m_view.getGraphics());

            closeLevel(oldmap, false); // close the old map
        }
        //System.out.println("exit: '"+path+"' "+dx+","+dy);
    }

    /**
     * Try to load the map where the specified map-tile path points to.
     * Usually this method can only be invoked when such a path exists.
     *
     * @param direction   the direction to go (see MapArchObject: 'tile_path')
     */
    public void enterTileWanted(int direction) {
        String path;        // exit path
        CMapControl oldmap; // store control of active map (to evt. close it later)

        path = m_currentMap.getMapTilePath(direction);

        if (path == null || path.length() == 0) {
            // tile direction not set (due to disabled menus this should normally not happen)
            showMessage("Destination Invalid", "There is no tile map in that direction.");
        }
        else {
            // path points to a different map
            File newfile;  // new mapfile to open

            if (path.startsWith(File.pathSeparator) || path.startsWith("/"))
            {
                // we have an absolute path:
                newfile = new File(m_mapDir.getAbsolutePath(), path.substring(1));
            } else
            {
                // we have a relative path:
                newfile = new File(m_currentMap.mapFile.getParent(), path);
            }

            if (!newfile.exists() || newfile.isDirectory()) {
                // The path is wrong
                showMessage("Invalid Path", "The specified path is invalid:\n"+newfile.getAbsolutePath());
                return;
            }

            /* its important to force the canonical file here or the
             * file path is added every time we use a ../ or a ./ .
             * This results in giant file names like "xx/../yy/../xx/../yy/.."
             * and after some times in buffer overflows.
             */
            oldmap = m_currentMap; // store old map control
            try {
              openFile(newfile.getCanonicalFile());
            } catch (IOException e) {
                showMessage("Invalid Path", "Failed to load file for tiled map.\n"+newfile.getAbsolutePath());
                return;
            }

            // set viewport view on the new map
            if (!IGUIConstants.isoView) {
                Rectangle scrollto = null; // new vieport rect
                JViewport newViewPort = m_currentMap.m_view.getViewPort();
                JViewport oldViewPort = oldmap.m_view.getViewPort();

                if (direction == IGUIConstants.SOUTH) {
                    scrollto = new Rectangle(oldViewPort.getViewRect().x, 0,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.NORTH) {
                    scrollto = new Rectangle(oldViewPort.getViewRect().x, newViewPort.getViewSize().height-oldViewPort.getViewRect().height,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.EAST) {
                    scrollto = new Rectangle(0, oldViewPort.getViewRect().y,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.WEST) {
                    scrollto = new Rectangle(newViewPort.getViewSize().width-oldViewPort.getViewRect().width, oldViewPort.getViewRect().y,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.NW) {
                    scrollto = new Rectangle(newViewPort.getViewSize().width-oldViewPort.getViewRect().width, newViewPort.getViewSize().height-oldViewPort.getViewRect().height,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.SW) {
                    scrollto = new Rectangle(newViewPort.getViewSize().width-oldViewPort.getViewRect().width, 0,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.NE) {
                    scrollto = new Rectangle(0, newViewPort.getViewSize().height-oldViewPort.getViewRect().height,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }
                else if (direction == IGUIConstants.SE) {
                    scrollto = new Rectangle(0, 0,
                                        oldViewPort.getViewRect().width, oldViewPort.getViewRect().height);
                }

                if (scrollto.x+scrollto.width > newViewPort.getViewSize().width)
                    scrollto.x = newViewPort.getViewSize().width - scrollto.width;
                if (scrollto.x < 0 )
                    scrollto.x = 0;
                if (scrollto.y+scrollto.height > newViewPort.getViewSize().height)
                    scrollto.y = newViewPort.getViewSize().height - scrollto.height;
                if (scrollto.y < 0 )
                    scrollto.y = 0;
                newViewPort.setViewPosition( scrollto.getLocation() );
                //getViewport().scrollRectToVisible(scrollto);
            }

            // Update the main view so the new map instantly pops up.
            m_view.update(m_view.getGraphics());

            closeLevel(oldmap, false); // close the old map
        }
    }


    /**
     * Gives focus to the previous window.
     */
    public void previousWindowWanted() {
        m_view.previousWindowWanted();
    }

    /**
     * Gives focus to the next window.
     */
    public void nextWindowWanted() {
        m_view.nextWindowWanted();
    }

    /**
     * Sets the given level view as the current one.
     * @param map     <code>CMapControl</code> of the new current map.
     */

    public void setCurrentLevel( CMapControl map ) {
        m_currentMap= map;
        refreshMenusAndToolbars();

        //        CMainStatusbar.getInstance().setLevelInfo( level );
    }

    /**
     * Invoked when user wants to revert the current map to previously saved state
     */
    public void revertCurrentLevelWanted() {
        CMapControl modmap = this.m_currentMap; // "modified map" to be reverted

        // ask for confirmation
        if ( askConfirm(
            "Revert "+modmap.getMapFileName() +"?",
            "If you revert the map '"+modmap.getMapFileName()+"' to it's last saved state, all\n"+
            "recent changes will be lost. Do you really want to revert this map?") ) {
            // okay, then do it:

            File mfile = modmap.mapFile; // store file
            closeLevel(modmap, true);    // close the old map
            openFile(mfile);             // open the new map

            // Update the main view so the new map instantly pops up.
            m_view.update(m_view.getGraphics());
        }
    }

    /**
     * Invoked when user wants to revert the current map to previously saved state
     */
    public void revertActivePickmapWanted() {
        if (!CPickmapPanel.getInstance().isLoadComplete()) {
            showMessage("Cannot revert Pickmap", "Pickmaps aren't loaded.\n"+
                        "Either there are no pickmaps or the loading process is not complete.");
        }
        else {
            if (!getMainView().isPickmapActive()) {
                showMessage("Cannot revert Pickmap", "Pickmaps are currently hidden.\n"+
                            "Please select a pickmap before activating this command.");
            }
            else {
                CMapControl activePickmap = CPickmapPanel.getInstance().getCurrentPickmap();
                if (activePickmap == null) {
                    showMessage("Cannot revert Pickmap", "There are no pickmaps.");
                }
                else {
                    // ask for confirmation
                    if (!activePickmap.isLevelChanged() || askConfirm(
                        "Revert Pickmap "+activePickmap.getMapFileName() +"?",
                        "If you revert the pickmap '"+activePickmap.getMapFileName()+"' to it's last saved state, all\n"+
                        "recent changes will be lost. Do you really want to revert this pickmap?") ) {
                        // okay, then do it:

                        File mfile = activePickmap.mapFile; // store file
                        if (!mfile.exists()) {
                            showMessage("Cannot revert pickmap", "The file for pickmap '"+activePickmap.getMapFileName()+"' doesn't exist.", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        int tabIndex = CPickmapPanel.getInstance().getPickmapTabIndex(activePickmap);
                        if (tabIndex >= 0) {
                            closeLevel(activePickmap, true);    // close the old map
                            CPickmapPanel.getInstance().openPickmap(mfile, tabIndex); // open the new map
                            CPickmapPanel.getInstance().setActivePickmap(tabIndex);

                            // Update the main view so the new map instantly pops up.
                            m_view.update(m_view.getGraphics());
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked when the user wants to undo a change in the current level.
     */
    void undoWanted() {
        if ( m_currentMap != null ) {
            m_currentMap.undo();
        }
    }

    /**
     * Invoked when the user wants to redo a change in the current level.
     */
    void redoWanted() {
        if ( m_currentMap != null ) {
            m_currentMap.redo();
        }
    }

    /**
     * Returns the name of the undo operation.
     *@return Name of the undo operation.
     */
    public String getUndoName() {
        if ( m_currentMap != null ) {
            return m_currentMap.getUndoName();
        }

        return "";
    }

    /**
     * Returns the name of the redo operation.
     *@return Name of the redo operation.
     */

    public String getRedoName() {
        if ( m_currentMap != null ) {
            return m_currentMap.getRedoName();
        }

        return "";
    }

    /**
     * Returns whether undo is possible or not.
     *@return True if undo is possible, false if not possible.
     */

    boolean isUndoPossible() {
        if ( m_currentMap != null ) {
            return m_currentMap.isUndoPossible();
        }

        return false;
    }

    /**
     * Returns whether redo is possible or not.
     *@return True if redo is possible, false if not possible.
     */

    boolean isRedoPossible() {
        if ( m_currentMap != null ) {
            return m_currentMap.isRedoPossible();
        }

        return false;
    }

    /**
     * "Clear" was selected from the Edit menu
     */
    public void clearWanted() {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.clear(m_currentMap);
    }

    /**
     * "Cut" was selected from the Edit menu
     */
    public void cutWanted() {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.cut(m_currentMap);
    }

    /**
     * "Copy" was selected from the Edit menu
     */
    public void copyWanted() {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.copy(m_currentMap);
    }

    /**
     * "Paste" was selected from the Edit menu
     */
    public void pasteWanted() {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.paste(m_currentMap);
    }

    /**
     * "Fill" was selected from the Edit menu
     * @param fill_below     true if "Fill Below" was activated, false if "Fill Above"
     */
    public void fillWanted(boolean fill_below) {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.fill(m_currentMap, fill_below,null,-1);
    }

    /**
     * "RandomFill" was selected from the Edit menu
     * @param fill_below     true if "Fill Below" was activated, false if "Fill Above"
     */
    public void fillRandomWanted(boolean fill_below) {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

          String title = "Random fill ";
          ArchObject arch = mStatic_control.getArchPanelSelection();
          CMapControl pmap = null;
          int rand = 100;

          /* if we have a single arch, use it as random seed.
           * We can throw the arch with % chance over the selected area.
           * If the arch is null, we look we have a selected pickmap.
           * if so, use the pickmap as random arch seed for the filler.
           */
          if(arch !=null)
          {
            title += "with arch " + arch.getArchName();
          }
          else
          {
            pmap = CPickmapPanel.getInstance().getCurrentPickmap();
            if(pmap != null)
            {
              title += "with pickmap " + pmap.getMapName();
            } /* ok ,we have a problem here: arch == null, pmap == null... */
            else
              return;
          }
        String Input=JOptionPane.showInputDialog(null,"Enter a fill seed value between 1-100\n(default is 100%)",title,JOptionPane.QUESTION_MESSAGE);
        if(Input != null && Input.length() >0)
        {
          try {
            rand = Integer.parseInt(Input);
            if (rand < 0)
              rand = 1;
            if (rand > 100)
              rand = 100;
          }
          catch (NumberFormatException e) {
              rand = 100;
          }
        }

        copybuffer.fill(m_currentMap, fill_below, pmap, rand);
    }

    /**
     * "Replace" was selected from the Edit menu
     */
    public void replaceWanted() {
        if (m_currentMap == null || m_currentMap.m_view == null)
            return;  // this should never be possible, but I just wanna make sure...

        copybuffer.replace(m_currentMap);
    }


    /**
     * is CopyBuffer empty?
     * @return true if the buffer is empty
     */
    public boolean isCopyBuffer_empty() {
        return copybuffer.isEmpty();
    }

    /**
     * Returns all level windows that exist in the main view.
     *@return All level windows
     */
    Enumeration getAllLevelWindows() {
        return m_view.getAllLevelWindows();
    }

    /**
     * Sets the given view to be the current level view.
     *@param view The new current level view.
     */

    void setCurrentLevelView( CMapViewIFrame view ) {
        m_view.setCurrentLevelView( view );
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
        m_view.appExitNotify(); // notify main view
        ScriptEditControl.getInstance().appExitNotify(); // notify scriptpad controller

        if (m_currentMap != null) {
            m_view.setMapTileList(null,-1);
            m_currentMap.appExitNotify();
        }

        // save settings
        CSettings.getInstance(IGUIConstants.APP_NAME).saveSettings();
    }

    public void refreshMenus() {
        m_view.refreshMenus();
    }

    /**
     * Refreshes the state of menu items and toolbar buttons.
     */
    public void refreshMenusAndToolbars() {
        m_view.refreshMenusAndToolbars();
    }

    /**
     * Relayouts and repaints the main view.
     */
    public void refreshMainView() {
        m_view.refresh();
    }

    /**
     * Returns whether a level is being edited or not.
     *@return True if a level is being edited or false if not.
     */
    boolean isLevelEdited() {
        return ( m_currentMap != null );
    }

    /**
     * Returns whether the level can be just saved (true) or does it need
     * to be saved as (false).
     *@return True if level can be just saved, false if not.
     */

    boolean isPlainSaveEnabled() {
        return ( ( m_currentMap != null ) && ( m_currentMap.isPlainSaveEnabled() ) );
    }


    /**
     * @return <code>ImageIcon</code> of the grid icon
     */
    public ImageIcon getGridIcon() {
        return(mapGridIcon);
    }

    public ImageIcon getUnknownTileIcon() {
        return(unknownTileIcon);
    }

    /**
     * load all system tile icons into temporare variables
     * for more convenient access
     */
    public void loadDefTiles() {
        mapGridIcon = CGUIUtils.getSysIcon(IGUIConstants.TILE_IGRID_TILE);
        mapSelIcon = CGUIUtils.getSysIcon(IGUIConstants.TILE_ISEL_TILE);
        unknownTileIcon = CGUIUtils.getSysIcon(IGUIConstants.TILE_IUNKNOWN);
        nofaceTileIcon = CGUIUtils.getSysIcon(IGUIConstants.TILE_INOFACE);
        noarchTileIcon = CGUIUtils.getSysIcon(IGUIConstants.TILE_INOARCH);

        mapGridIconX = CGUIUtils.getSysIcon(IGUIConstants.TILE_GRID_TILE);
        mapSelIconX = CGUIUtils.getSysIcon(IGUIConstants.TILE_SEL_TILE);
        unknownTileIconX = CGUIUtils.getSysIcon(IGUIConstants.TILE_UNKNOWN);
        nofaceTileIconX = CGUIUtils.getSysIcon(IGUIConstants.TILE_NOFACE);
        noarchTileIconX = CGUIUtils.getSysIcon(IGUIConstants.TILE_NOARCH);
    }

    public void handleErrors( CGridderException error ) {
        Toolkit.getDefaultToolkit().beep();
        m_view.showError( error );
    }

    /**
     * Shows the given message in the UI.
     * @param strTitle The title of the message.
     * @param strMessage The message to be shown.
     */
    public void showMessage(String strTitle, String strMessage ) {
        m_view.showMessage(strTitle, strMessage );
    }

    /**
     * Shows the given message in the UI.
     * @param strTitle     The title of the message.
     * @param strMessage   The message to be shown.
     * @param messageType  Type of message (see JOptionPane constants)
     */
    public void showMessage(String strTitle, String strMessage, int messageType) {
        m_view.showMessage(strTitle, strMessage, messageType);
    }

    /**
     * Shows the given confirmation message in the UI. The message
     * is a yes/no option.
     *@param strTitle The title of the message.
     *@param strMessage The message to be shown.
     *@return Ture if the user agrees, false if user disagrees.
     */
    public boolean askConfirm( String strTitle, String strMessage ) {
        return m_view.askConfirm( strTitle, strMessage );
    }

    /**
     *  Calls for zooming preview tool
     */
    public void doZoom()
    {
      if (m_currentMap == null){
        JOptionPane.showMessageDialog(m_view, "No map loaded! Please load a map!!","Error"
                                    ,JOptionPane.ERROR_MESSAGE);
        return;
      }
      CPreview zoom = new CPreview(this,m_currentMap.m_view.view.m_renderer.getFullImage());
    }



}
