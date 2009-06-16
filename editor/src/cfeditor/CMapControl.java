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

import java.io.*;

/**
 * The <code>CMapControl</code>
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
class CMapControl {
    /** The main controller of this subcontroller. */
    private CMainControl m_control;
    CMapModel m_model;                          // model (= map data of this level)
    CMapViewIFrame m_view;                      // the view of the map
    File mapFile;

    // contains the edit types that have already been (requested and) calculated
    // (edit types get calculated only when needed to save time)
    private int active_edit_type;

    /** Flag that indicates whether the level is closing or not. */
    private boolean m_fLevelClosing = false;

    /** Flag that indicates whether this is a pickmap or not. */
    public boolean is_pickmap;

    /**
     * Constructs a new Map.
     *
     * @param control      the controller of this map
     * @param maparch      the <code>MapArchObject</code> of the map
     * @param startObj     chained list of Objects (ArchObjects) which should be filled in map
     * @param is_pick      true if this is a pickmap
     * @throws CGridderException    if something went wrong
     */
    CMapControl(CMainControl control, ArchObject startObj,
                MapArchObject maparch, boolean is_pick) throws CGridderException {
        m_control = control;
        active_edit_type = 0;     // start with no edit types (saves time)
        is_pickmap = is_pick;     // is this a pickmap?
        // we create model (= data)
        m_model = new CMapModel( m_control, this, startObj, maparch);
        // and create a view (= window)
        m_view = new CMapViewIFrame( m_control, this);
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
        m_view.appExitNotify();         // drop the view
        m_model.appExitNotify();        // and then the model
    }

    public boolean isPickmap() {
        return is_pickmap;
    }

    public boolean isGridVisible() {
        return (m_view.isGridVisible());
    }

    public void setGridVisibility(boolean fVisible) {
        m_view.setGridVisibility(fVisible);
    }

    public int getActive_edit_type() {
        return active_edit_type;
    }

    public void setActive_edit_type(int new_type) {
        active_edit_type = new_type;
    }

    /**
     * Checks if the given edit type is active or not
     * @param check_type      edit type
     * @return                true if this edit type is active
     */
    public boolean has_edit_type(int check_type) {
        if((active_edit_type & check_type) != 0)
            return true;
        return false;
    }

    /**
     * Add edit type to the bitmask of active types. If this is a
     * new type, it gets calculated for every arch on the map.
     * Once it is calculated, we save that state in 'active_edit_type'
     * so we don't need to do it again.
     *
     * @param new_type     new edit type
     */
    public void add_edit_type(int new_type) {
        // calculate only if needed
        if (!has_edit_type(new_type)) {
            ArchObject arch;   // index arch
            int posx, posy;    // coordinates

            for (posx = 0; posx < m_model.getMapWidth(); posx++) {
                for (posy = 0; posy < m_model.getMapHeight(); posy++) {
                    for (arch = m_model.getMapGrid()[posx][posy]; arch != null;
                         arch = arch.getNextArch()) {
                        // calculate the new edit type
                        if (arch.getRefFlag() && arch.getMapMultiHead() != null) {
                            // multi tails get the value from their head
                            arch.setEditType(arch.getMapMultiHead().calculateEditType(new_type));
                        }
                        else
                            arch.calculateEditType(new_type);
                    }
                }
            }
            // from now on we have this type, so we don't have to calculate it again
            active_edit_type|=new_type;
        }
    }

    /**
     * Notifies that a view has been closed.
     * @param view The view that was closed.
     */
    void viewCloseNotify( CMapViewBasic view ) {
        if ( !m_fLevelClosing ) {
            m_control.closeLevel(this, false);
        }
    }

    // text of map arch object!
    public String getMapText() {
        return(m_model.getMapText());
    }

    // text of map arch object!
    public String getMapLore() {
        return(m_model.getMapArchObject().getLore());
    }

    /**
     * Handles the given error.
     * @param error   a generic error
     */
    public void handleErrors( CGridderException error ) {
        m_control.handleErrors( error );
    }

    /**
     * Shows the given message in the UI.
     * @param strTitle The title of the message.
     * @param strMessage The message to be shown.
     */
    public void showMessage( String strTitle, String strMessage ) {
        m_control.showMessage( strTitle, strMessage );
    }

    /**
     * Refreshes the state of menu items and toolbar buttons.
     */
    void refreshMenusAndToolbars() {
        m_control.refreshMenusAndToolbars();
    }

    boolean getIsoView() {
        return(m_model.getIsoView());
    }

    void levelCloseNotify() {
        m_fLevelClosing = true;
        m_model.levelCloseNotify();
    }

    /**
     * @return true when this map has been closed
     */
    public boolean isClosing() {
        return m_fLevelClosing;
    }

    void freeMapArchObject() {
        m_model.freeMapArchObject();
    }

    boolean addArchToMap(int archnr, int mapx, int mapy, int intern, boolean join,
                         boolean insert_below) {
        return m_model.addArchToMap(archnr, mapx, mapy, intern, join, insert_below);
    }

    /**
     * wrapper method for addArchToMap, always inserting new arches on top
     */
    public boolean addArchToMap(int archnr, int xx, int yy, int intern, boolean join) {
        return addArchToMap(archnr, xx, yy, intern, join, false);
    }

    public boolean insertArchToMap(ArchObject newarch, int archnr, ArchObject next, int mapx, int mapy, boolean join) {
        return  m_model.insertArchToMap(newarch, archnr, next, mapx, mapy, join);
    }

    public void addArchObjectToMap(ArchObject arch, boolean insert_below) {
        m_model.addArchObjectToMap(arch, insert_below);
    }

    public void addArchObjectToMap(ArchObject arch) {
        m_model.addArchObjectToMap(arch, false);
    }

    public void deleteMapArch(int index, int mapx, int mapy,
                              boolean refresh_map, boolean join) {
        m_model.deleteMapArch(index, mapx, mapy, refresh_map, join);
    }

    public ArchObject getMapArch(int index, int mapx, int mapy) {
        return(m_model.getMapArch(index, mapx, mapy));
    }

    public String getMapTilePath(int direction) {
        return(m_model.getMapArchObject().getTilePath(direction));
    }

    /**
     * Returns the level grid data from the model.
     * @return The level grid data from the model.
     */
    ArchObject[][] getMapGrid() {
        return m_model.getMapGrid();
    }

    /**
     * Returns whether the level has changed since it was last saved or not.
     * @return True if level has changed, false if not.
     */
    boolean isLevelChanged() {
        return m_model.isLevelChanged();
    }

    /**
     * Returns whether the level can be just saved (true) or does it need
     * to be saved as (false).
     *@return True if level can be just saved, false if not.
     */
    boolean isPlainSaveEnabled() {
        if(m_control.m_currentMap.mapFile==null || m_control.m_currentMap.getMapFileName() == null ||
           m_control.m_currentMap.getMapFileName().compareTo(IGUIConstants.DEF_MAPFNAME)==0)
            return(false);
        else
            return(true);
    }

    /**
     * Set the properties (name, maptext and size) of this map.
     *
     * @param archText       map text
     * @param loreText       lore text
     * @param strMapTitle    map name
     * @param mapWidth       width of map
     * @param mapHeight      height of map
     */
    void setProperties(String archText, String loreText, String strMapTitle,
                       int mapWidth, int mapHeight) {
        // resize this map
        if (mapWidth != getMapWidth() || mapHeight != getMapHeight()) {
            resizeMap(mapWidth, mapHeight);
        }

        setNewMapText(archText); // change map text
        setNewLoreText(loreText); // change lore text

        // change map name (does not change the filename anymore)
        strMapTitle = strMapTitle.trim();
        setMapName(strMapTitle);

        notifyViews();  // update
    }

    /**
     * Undoes a change in the level.
     */
    void undo() {
        CUndoStack.getInstance( this ).undo();
    }

    /**
     * Redoes a change in the level.
     */
    void redo() {
        CUndoStack.getInstance( this ).redo();
    }

    /**
     * Returns the name of the undo operation.
     * @return Name of the undo operation.
     */
    public String getUndoName() {
        return CUndoStack.getInstance( this ).getUndoName();
    }

    /**
     * Returns the name of the redo operation.
     * @return Name of the redo operation.
     */
    public String getRedoName() {
        return CUndoStack.getInstance( this ).getRedoName();
    }

    /**
     * Returns whether undo is possible or not.
     *@return True if undo is possible, false if not possible.
     */
    boolean isUndoPossible() {
        return CUndoStack.getInstance( this ).canUndo();
    }

    /**
     * Returns whether redo is possible or not.
     *@return True if redo is possible, false if not possible.
     */
    boolean isRedoPossible() {
        return CUndoStack.getInstance( this ).canRedo();
    }

    /**
     * Repaints the view.
     */
    void repaint() {
        m_view.repaint();
    }

    /**
     * Notifies the view that data has changed in the model.
     */
    void notifyViews() {
        m_view.refreshDataFromModel();
    }

    /**
     * Returns the width of the level.
     * @return   map width.
     */
    int getMapWidth() {
        return m_model.getMapWidth();
    }

    /**
     * Returns the height of the level.
     * @return   map height.
     */
    int getMapHeight() {
        return m_model.getMapHeight();
    }

    /**
     * Check if the coordinates posx, posy are valid
     * (located within the borders of the map).
     *
     * @param posx    x coordinate
     * @param posy    y coordinate
     * @return        true if this point is located within the map boundaries
     */
    public boolean pointValid(int posx, int posy) {
        return (posx>=0 && posy>=0 && posx<m_model.getMapWidth() &&
                posy<m_model.getMapHeight());
    }

    public boolean checkResizeMap(int newWidth, int newHeight) {
        return m_model.checkResizeMap(newWidth, newHeight);
    }

    /**
     * Resize the map.
     * @param newWidth      the new level width.
     * @param newHeight     the new level height.
     */
    void resizeMap(int newWidth, int newHeight) {
        m_model.resizeMap(newWidth, newHeight);
    }

    /**
     * @return The level name.
     */
    public String getMapName() {
        return m_model.getMapName();
    }

	public String getBackgroundMusic() {
        return m_model.getBackgroundMusic();
    }

    /**
     * Sets the level name.
     * @param strName The level name.
     */
    public void setMapName( String strName ) {
        m_model.setMapName( strName );
    }

    public String getMapFileName() {
        return m_model.getFileName();
    }

    public void setMapFileName(String fname) {
        m_view.setTitle(fname+" [ "+m_control.m_currentMap.getMapName()+" ]");
        m_model.setFileName(fname);
    }

    public void setNewMapText( String str ) {
        m_model.setNewMapText( str );
    }

    public void setNewLoreText( String str ) {
        m_model.getMapArchObject().setLore(str);
    }

    public void setLevelChangedFlag() {m_model.setLevelChangedFlag();}

    public void save() {
       if (isPickmap())
         CMainStatusbar.getInstance().setText("Saving pickmap '" + getMapFileName() +"'...");
       else
           CMainStatusbar.getInstance().setText("Saving map '"+getMapFileName()+"'...");
       m_control.encodeMapFile(mapFile, m_model.m_mapArch, getMapGrid());
       /* if we open a pickmap in the editor, is handled as normal map.
        * to find out its original a pickmap we check the file name.
        */
       if (isPickmap())
       {
         // this is called when we do a "add-new-pickmap from the pickmap menu
         // we need to link the full path & save the map at creation time
         // so the pickmap menu can handle it right
         try {
           m_model.setFileName(mapFile.getCanonicalPath());
         }
         catch (IOException e)
         {
         }
       }
       else
       {
         int i = CPickmapPanel.getInstance().getPickmapTabIndexByName(this.getMapFileName());

         // -1: this map is not a pickmap - every other value is the tab index
         if(i!=-1)
         {
           CPickmapPanel.getInstance().closePickmap(CPickmapPanel.getInstance().getPickmapByIndex(i));
           CPickmapPanel.getInstance().openPickmap(mapFile, -1); // open the new map
           CPickmapPanel.getInstance().setActivePickmap(
                           CPickmapPanel.getInstance().getPickmapTabIndexByName(this.getMapFileName()));
           // Update the main view so the new map instantly pops up.
           m_view.update(m_view.getGraphics());
         }
       }
       m_model.resetLevelChangedFlag();
       m_view.changedFlagNotify();
   }

    /**
     * Saves the file with the given file name.
     * @param file The file to be saved to.
     */
    void saveAs(java.io.File file) {
        CMainStatusbar.getInstance().setText("Saving the map to a file...");
        m_control.encodeMapFile(file, m_model.m_mapArch, getMapGrid());
        m_model.resetLevelChangedFlag();
        m_view.changedFlagNotify();
    }

    void setMapFile(File file) {
        mapFile = file;
    }
}
