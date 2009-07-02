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

import java.util.*;
import java.awt.Point;

/**
 * The level model that represents a level.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
class CMapModel {
    // Constants for autojoin enable/disable
    public static final boolean JOIN_ENABLE = true;
    public static final boolean JOIN_DISABLE = false;

    // list of exit types (Integer values)
    private static Set exitTypes = new HashSet();

    /** Level grid data. */
    MapArchObject m_mapArch=null;        // the MapArchObject
    ArchObject[][] m_mapGrid = null;     // containing all arches grid-wise
    /** The width of the level (in tiles). */
    private int m_mapWidth = 8;
    /** The height of the level (in tiles). */
    private int m_mapHeight = 8;

    /** Flag that indicates if the level has been changed since last save. */
    private boolean m_fLevelChanged = false;

    private boolean isoView;

    private CMainControl main_control = null;
    private CMapControl m_control = null;

    /**
     * Constructs a level model.
     *
     * @param mc            main controler
     * @param control       the controler of this view
     * @param start         first element in the <code>ArchObject</code> list of this map
     * @param map           the map header (<code>MapArchObject</code>)
     */
    CMapModel (CMainControl mc, CMapControl control, ArchObject start,
               MapArchObject map) {
        main_control = mc;
        m_control = control;
        m_mapWidth = map.getWidth();
        m_mapHeight = map.getHeight();
        m_fLevelChanged = false;
        isoView = IGUIConstants.isoView; // is ISO view applied?

        m_mapGrid = new ArchObject[m_mapWidth][m_mapHeight];
        initMap(m_mapWidth, m_mapHeight);

        addArchListToMap(start, map); // init mapArchObject and (when not new map) the arch list
        setMapX(m_mapWidth);
        setMapY(m_mapHeight);

        // init static component
        if (exitTypes.isEmpty())
            init();
    }

    /**
     * Initialize static components
     */
    private static void init() {
        synchronized(exitTypes) {
            if (exitTypes.isEmpty()) {
                exitTypes.add(new Integer(41)); // teleporter
                exitTypes.add(new Integer(66)); // exit
                exitTypes.add(new Integer(94)); // pit
                exitTypes.add(new Integer(95)); // trapdoor
            }
        }
    }

    ArchObject getMouseRightPosObject() {
        int mx = m_control.m_view.getMapMouseRightPos().x;
        int my = m_control.m_view.getMapMouseRightPos().y;

        if(mx != -1 && my != -1 && pointValid(mx, my))
            return(m_mapGrid[mx][my]);
        else
            return(null);
    }

    void setNewMapText( String str ) {
        m_mapArch.resetText();
        m_mapArch.addText(str);
    }

    // text of map arch object!
    public String getMapText() {
        return(m_mapArch.getText());
    }

    // WARNING: If we change map name , this is not changed
    public void setMapX(int len) {
        m_mapArch.setWidth(len);
    }

    public void setMapY(int len) {
        m_mapArch.setHeight(len);
    }

    void setIsoView(boolean iso) {
        isoView = iso;
    }

    boolean getIsoView() {
        return(isoView);
    }

    /**
     * place the loaded arches from the list onto the map
     *
     * @param arch         first element in the <code>ArchObject</code> list
     * @param maparch      the map header (<code>MapArchObject</code>)
     */
    void addArchListToMap(ArchObject arch, MapArchObject maparch) {
        // map arch
        m_mapArch = maparch;

        // arch objects
        if (!maparch.size_null()) {
            for(;arch != null;) {
                if(arch.getContainer() == null) // only map arches....
                    addArchObjectToMap(arch, false);
                arch = arch.getTemp();      // next sucker
            }
        }
    }


    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
    }

    /**
     * Notifies that the level is about to be closed.
     */
    void levelCloseNotify() {
        if(m_mapGrid != null)
            {
                freeMapArchObject();    // free all data here
            }
    }

    /**
     * Resets the level changed flag to false.
     */
    void resetLevelChangedFlag() {
        if (m_fLevelChanged == true) {
            // change status and update title bar
            m_fLevelChanged = false;
            m_control.m_view.changedFlagNotify();
        }
    }

    /**
     * sets the level changed flag to true.
     */
    public void setLevelChangedFlag() {
        if (m_fLevelChanged == false) {
            // change status and update title bar
            m_fLevelChanged = true;
            m_control.m_view.changedFlagNotify();

            // enable menu file->revert to revert the map
            if (main_control.m_currentMap == m_control)
                main_control.getMainView().setRevertMenuEnabled(true);
        }
    }

    /**
     * Returns whether the level has changed since it was last saved or not.
     *@return True if level has changed, false if not.
     */
    boolean isLevelChanged() {
        return m_fLevelChanged;
    }

    /**
     * Returns the width of the level.
     *@return The width of the level.
     */

    int getMapWidth() {
        return m_mapWidth;
    }

    /**
     * Returns the 2D level grid.
     *@return The 2D level grid.
     */
    ArchObject[][] getMapGrid() {
        return m_mapGrid;
    }

    /**
     * Returns the height of the level.
     *@return The height of the level.
     */

    int getMapHeight() {
        return m_mapHeight;
    }

    void initMap(int map_lenx, int map_leny) {
        for(int y=0;y<map_leny;y++) {
            for(int x=0;x<map_lenx;x++) {
                m_mapGrid[x][y] = null;
            }
        }
    }

    // this sucker test for map fit. Multi tiles can't be set if going out of borders
    boolean testArchToMap(int archnr, int xx, int yy, int intern) {
        int count, temp;                        // count of multi tile. 0= single tile
        int mapx, mapy;
        ArchObject node;

        count = main_control.getArchObjectStack().getArch(archnr).getRefCount();

        for(int c=0;c<=count;c++) {
            mapx = xx+main_control.getArchObjectStack().getArch(archnr+c).getRefX();
            mapy = yy+main_control.getArchObjectStack().getArch(archnr+c).getRefY();
            // outside map
            if(mapx<0 || mapy<0 || mapx>=m_mapWidth || mapy>=m_mapHeight)
                return false;

            temp = -1;
            if(intern != -1)         // we use this different - only one object
                temp = archnr+c; // of one type on a map position when dragged

            // run through map parts and test for intern counter
            // if == intern, this is painted from this action
            node = m_mapGrid[mapx][mapy];
            for(int i=0;node != null;i++) {
                if(node.getNodeNr() == temp)
                    return false;
                node = node.getNextArch();
            }
        }
        return true;
    }

    /**
     * Move the given arch up on the map. (Note that this
     * actually means moving "down" in the linked list)
     *
     * @param prev        arch to be moved
     * @param refresh     if true, map and panel get refreshed
     */
    void moveTileUp(ArchObject prev, boolean refresh) {

        if(prev == null)
            return;

        if(prev.getContainer() != null)
            return;

        ArchObject arch = prev.getNextArch();
        if(arch == null)
            return;

        prev.setNextArch( arch.getNextArch());
        arch.setPrevArch( prev.getPrevArch());
        arch.setNextArch( prev);
        prev.setPrevArch( arch);
        if(prev.getNextArch() != null)
            prev.getNextArch().setPrevArch(prev);
        if(arch.getPrevArch() != null)
            arch.getPrevArch().setNextArch(arch);
        else
            m_mapGrid[arch.getMapX()][arch.getMapY()]=arch;

        if (refresh)
            main_control.refreshCurrentMap();
    }

    /**
     * Move the given arch down on the map. (Note that this
     * actually means moving "up" in the linked list)
     *
     * @param arch       arch to be moved
     * @param refresh    if true, map and panel get refreshed
     */
    void moveTileDown(ArchObject arch, boolean refresh) {

        if(arch == null)
            return;

        if(arch.getContainer() != null)
            return;

        ArchObject prev = arch.getPrevArch();

        if(prev == null)
            return;

        prev.setNextArch( arch.getNextArch());
        arch.setPrevArch( prev.getPrevArch());
        arch.setNextArch( prev);
        prev.setPrevArch( arch);
        if(prev.getNextArch() != null)
            prev.getNextArch().setPrevArch(prev);
        if(arch.getPrevArch() != null)
            arch.getPrevArch().setNextArch(arch);
        else
            m_mapGrid[arch.getMapX()][arch.getMapY()]=arch;

        if (refresh)
            main_control.refreshCurrentMap();
    }


    /**
     * Add a new arch to the map. Including multi tile arches. This
     * function allows only to choose from the default arches (->archnr).
     *
     * @param archnr       ID from a default arch on the ArchObjectStack
     * @param xx           insert-location on this map
     * @param yy           insert-location on this map
     * @param intern       if NOT '-1': only one arch of the same kind can be
     *                     inserted per square
     * @param join         if set to JOIN_ENABLE auto-joining is supported
     * @param isert_below  true: new arch is inserted on top, false: new arch is inserted below
     * @return             true if insertion successful, false if not
     */
    boolean addArchToMap(int archnr, int xx, int yy, int intern, boolean join,
                         boolean insert_below) {
        ArchObject node;        // node we attach the suckers
        ArchObject newarch, oldarch, startarch; // our new suckers (copys, not ref ptrs)
        int count;                      // count of multi tile. 0= single tile
        int mapx, mapy;

        if(archnr <0 || archnr >=main_control.getArchObjectStack().getArchCount()
           || xx ==-1 || yy==-1  || xx>=m_mapWidth ||yy>=m_mapHeight)
            return false;
        if(testArchToMap(archnr, xx, yy, intern)==false)
            return false;

        newarch = main_control.getArchObjectStack().getArch(archnr); // temp. store this arch

        if (main_control.getAutojoin() && join == JOIN_ENABLE && main_control.joinlist != null
            && newarch.getJoinList() != null && !newarch.isMulti()) {
            // do autojoining if enabled
            archnr = newarch.getJoinList().join_insert(this, xx, yy);
            if (archnr == -1) return false; // only one autojoin type per square allowed
        }

        count = main_control.getArchObjectStack().getArch(archnr).getRefCount();

        newarch = null;
        oldarch = null;
        startarch = null;
        for(int c=0;c<=count;c++) {
            mapx = xx+main_control.getArchObjectStack().getArch(archnr+c).getRefX();
            mapy = yy+main_control.getArchObjectStack().getArch(archnr+c).getRefY();
            newarch = main_control.getArchObjectStack().newArchObjectInstance(archnr+c);
            if(oldarch != null) {
                newarch.setMapMultiHead(startarch);
                oldarch.setMapMultiNext(newarch);
            } else
                startarch = newarch;
            oldarch = newarch;

            // insert it in map or add to arch in map
            node = m_mapGrid[mapx][mapy];
            if(node == null) {
                m_mapGrid[mapx][mapy] = newarch;
                m_mapGrid[mapx][mapy].setMapX(mapx);
                m_mapGrid[mapx][mapy].setMapY(mapy);
            } else {
                if (!insert_below) {
                    // if we want to insert on top, we need to get last node element
                    for(int i=0;node.getNextArch() != null;i++) {
                        node = node.getNextArch();
                    }

                    node.setNextArch(newarch);
                    node.getNextArch().setPrevArch(node);
                    node.getNextArch().setMapX(mapx);
                    node.getNextArch().setMapY(mapy);
                }
                else {
                    //ArchObject tmp = node;
                    node.setPrevArch(newarch);
                    newarch.setNextArch(node);
                    m_mapGrid[mapx][mapy] = newarch;
                    newarch.setMapX(mapx);
                    newarch.setMapY(mapy);
                }
            }

            newarch.setDirection(newarch.getDefaultArch().getDirection());
            main_control.archObjectParser.postParseMapArch(newarch, m_control.getActive_edit_type());
            newarch.setObjectFace();
            node = null;
        }
        oldarch=null;
        newarch=null;
        // Level data has changed
        setLevelChangedFlag();
        return true;
    }

    /**
     * Insert a new arch to the map at a specified position. This
     * function allows either to choose from the default arches (->archnr)
     * or to insert a copy from an existing arch (->newarch).
     * It also works for container-inventory.
     *
     * @param newarch        A clone copy of this ArchObject gets inserted to the map.
     *                       It can be an arch directly from a pickmap or even a default arch.
     *                       if ('newarch'==null) the default arch of number 'archnr' gets inserted
     * @param archnr         ID from a default arch to get inserted. This value gets
     *                       used ONLY when 'newarch' is null
     * @param next           the new arch gets inserted before 'next'
     *                       if ('next'==null) the arch gets inserted at bottom
     *                       -> 'next' must be an arch from the map! (or null)
     * @param mapx           map position to insert the new arch
     * @param mapy           map position to insert the new arch
     * @param join           if set to JOIN_ENABLE auto-joining is supported
     * @return               true if insertion was successful
     */
    boolean insertArchToMap(ArchObject newarch, int archnr, ArchObject next, int mapx, int mapy, boolean join) {
        boolean r;  // return value
        ArchObject node;

        // map coords must be valid
        if (!m_control.pointValid(mapx, mapy))
            return false;

          System.out.println("insert: "+archnr);

        if (next == null || next.getContainer() == null) {
            // next is not in a container

            // put arch on the map
            if (newarch == null || newarch.isDefaultArch()) {
                // just make sure we never insert an uninitialized default arch from the stack
                if (newarch != null && newarch.isDefaultArch())
                    archnr = newarch.getNodeNr();
                // insert a new instance of the default arch (number 'archnr')
                if (!m_control.addArchToMap(archnr, mapx, mapy, -1, join))
                    return false;
            }
            else {
                // insert the given 'newarch' (multis not allowed here yet - sorry)
                if (!newarch.isMulti()) {
                    newarch = newarch.getClone(mapx, mapy); // create a clone
                    m_control.addArchObjectToMap(newarch);  // insert it to the map
                }
                else
                    return false; // tried to insert multi (probably from pickmap)
            }

            // jump to the end of the list (-> grab "topmost" arch)
            for (node = m_mapGrid[mapx][mapy];
                 node != null && node.getNextArch() != null;
                 node = node.getNextArch());

            // now move the arch down till it meets the insert position
            for (; node != null && node.getPrevArch() != null &&
                     (next == null || node.getPrevArch().getMyID() != next.getMyID());
                 moveTileDown(node, false));

            setLevelChangedFlag();  // the map has been modified
            return true;
        }
        else {
            // insert the new arch into the inventory of a map arch
            ArchObject invnew; // new arch to be inserted
            if (newarch == null || newarch.isDefaultArch()) {
                if (newarch != null && newarch.isDefaultArch())
                    archnr = newarch.getNodeNr();
                // create a new copy of a defautl arch
                invnew = main_control.getArchObjectStack().newArchObjectInstance(archnr);
            }
            else {
                // create clone from a pickmap
                if (!newarch.isMulti())
                    invnew = newarch.getClone(mapx, mapy);
                else
                  return false;
            }

            next.getContainer().addInvObj(invnew);
            main_control.archObjectParser.postParseMapArch(invnew, m_control.getActive_edit_type());
            main_control.getMainView().setMapTileList(main_control.m_currentMap, invnew.getMyID());

            setLevelChangedFlag();  // the map has been modified
            return true;
        }
    }

    /**
     * Link an existing arch to the map. Including multi tile arches. This
     * function allows to insert any given arch (can be non-default).
     * Make sure that the given 'arch' is a new and unlinked object.
     *
     * @param arch         the new arch to be linked onto the map.
     *                     (Dest. coordinates must be set (arch.mapx/y)!)
     * @param insert_below true: new arch is inserted on top, false: new arch is inserted below
     */
    void addArchObjectToMap(ArchObject arch, boolean insert_below) {
        ArchObject node;        // node we attach the suckers
        ArchObject newarch, oldarch, startarch; // our new suckers (copys, not ref ptrs)
        int mapx, mapy;

        // Make sure this arch has the proper edit_type
        if (arch.getEditType() == IGUIConstants.TILE_EDIT_NONE)
            arch.calculateEditType(m_control.getActive_edit_type());

        arch.setObjectFace();
        newarch = arch;
        oldarch = null;
        startarch = null;

        for(int c=0;c<=0;c++) {
            mapx = arch.getMapX();
            mapy = arch.getMapY();

            if(oldarch != null) {
                newarch.setMapMultiHead(startarch);
                oldarch.setMapMultiNext(newarch);
            } else
                startarch = newarch;
            oldarch = newarch;

            // insert it in map or add to arch in map
            node = m_mapGrid[mapx][mapy];
            if(node == null) {
                m_mapGrid[mapx][mapy] = newarch;
                m_mapGrid[mapx][mapy].setMapX(mapx);
                m_mapGrid[mapx][mapy].setMapY(mapy);
            }
            else {
                if (!insert_below) {
                    // if we want to insert on top, we need to get last node element
                    for(int i=0;node.getNextArch() != null;i++) {
                        node = node.getNextArch();
                    }

                    node.setNextArch(newarch);
                    node.getNextArch().setPrevArch(node);
                    node.getNextArch().setMapX(mapx);
                    node.getNextArch().setMapY(mapy);
                }
                else {
                    // insert below, so just put it on first place
                    node.setPrevArch(newarch);
                    newarch.setNextArch(node);
                    m_mapGrid[mapx][mapy] = newarch;
                    newarch.setMapX(mapx);
                    newarch.setMapY(mapy);
                }
            }

            // calculate edit type
            if (newarch.getRefFlag() && newarch.getMapMultiHead() != null)
                newarch.setEditType(newarch.getMapMultiHead().getEditType()); // copy from head
            else if (m_control.getActive_edit_type() != 0)
                newarch.setEditType(newarch.calculateEditType(m_control.getActive_edit_type())); // calculate new

            node = null;
        }
        oldarch=null;
        newarch=null;
        // Level data has changed
        m_fLevelChanged = true;
        return;
    }

    /**
     * Get the arch from the map with the specified 'id', at location (xx, yy).
     *
     * @param xx      x-location
     * @param yy      y-location
     * @param id      ID number of arch (-> <code>arch.getMyID()</code>)
     * @return        the specified arch, or null if not found
     */
    public ArchObject getMapArch(int id, int xx, int yy) {
        ArchObject node, temp;

        if(m_mapGrid==null || xx <0 || xx>= m_mapWidth || yy <0 || yy >= m_mapHeight)
            return null;

        node = m_mapGrid[xx][yy];

        // first, try to find the tile we had selected
        for(int i=0;node != null;i++) {
            if(node.getMyID() == id) // is it this map tile
                break;
            // no, lets check his inventory
            if((temp=findInvObject(node, id)) != null)
                return(temp);

            node = node.getNextArch();
        }

        return node;
    }

    public ArchObject findInvObject(ArchObject node, int id) {
        ArchObject temp, arch = node.getStartInv();     // we go to our start

        for(;arch != null;) {
            if(arch.getMyID() == id)
                return(arch);
            if((temp=findInvObject(arch , id)) != null)
                return(temp);
            arch = arch.getNextInv();               // get next of chain
        }
        return null;
    }

    /**
     * Delete an existing arch from the map. (If the specified
     * arch doesn't exist, nothing happens.)
     * (This includes deletion of multiparts and inventory.)
     *
     * @param id            ID of the arch to be removed (->arch.getMyID())
     * @param xx            location of the arch to be removed
     * @param yy            location of the arch to be removed
     * @param refresh_map   If true, mapview is redrawn after deletion.
     *                      keep in mind: drawing consumes time!
     * @param join          if set to JOIN_ENABLE auto-joining is supported
     */
    public void deleteMapArch(int id, int xx, int yy, boolean refresh_map, boolean join) {
        ArchObject node, temp;

        node = m_mapGrid[xx][yy];

        // first, try to find the tile we had selected
        for(;node != null;) {
            if(node.getMyID() == id) {
                deleteArchMapObject(node);

                // do autojoining
                temp = main_control.getArchObjectStack().getArch(node.getNodeNr()); // get defarch
                if (main_control.getAutojoin() && join == JOIN_ENABLE && main_control.joinlist != null
                    && temp.getJoinList() != null && !temp.isMulti()) {
                    // remove connections to the deleted arch
                    temp.getJoinList().join_delete(this, xx, yy);
                }

                break;
            }

            // no, lets check his inventory
            if((temp=findInvObject(node, id)) != null){
                deleteInvObject(temp);
                break;
            }
            node = node.getNextArch();
        }
        setLevelChangedFlag();    // the map has been modified

        if (refresh_map) {
            main_control.refreshCurrentMap();  // redraw
        }
    }

    /**
     * Remove an inventory object (= Any object inside a container).
     * If it's a container itself, remove the whole inventory and all
     * sub containers too.
     *
     * @param node     object to be deleted
     */
    public void deleteInvObject(ArchObject node) {
        // for better security:
        if(node == null) return;

        // if this is a container by itself, we delete the inventory too
        if(node.getStartInv() != null)
            deleteContainerContent(node.getStartInv());

        // now remove this element from the list
        node.removeInvObj(); // clear all ptrs

        return;
    }

    /**
     * Removes the whole inventory of a container, including all
     * subcontainers. (Should only be used in 'deleteInvObject()')
     *
     * @param start     this should be the first inv.-item
     *                  of the container that is to be emptied.
     */
    private void deleteContainerContent(ArchObject start) {
        ArchObject temp, arch;

        for(arch = start; arch != null;) {
            // if this is a container again, we delete it recursive
            if(arch.getStartInv() != null)
                deleteContainerContent(arch.getStartInv());

            // now remove this element from the chain
            temp = arch.getNextInv();           // save next element
            arch.removeInvObj();                        // clear all ptrs
            arch = temp;                                        // move on to next element
        }
        return;
    }

    /**
     * This deletes an arch object attached to a map - multi tiles
     * as well. This method is called by deleteMapArch().
     * @param node     object to be deleted
     */
    private void deleteArchMapObject(ArchObject node) {
        ArchObject prev, next, temp;
        int mapx, mapy;

        // if this is a multi tile, jump to head for removing all
        if(node.getMapMultiHead() != null)
            node = node.getMapMultiHead();

        // now delete tile or multi tile suckers
        for(;node != null;) {
            // first, we set the map positions
            mapx = node.getMapX();
            mapy = node.getMapY();
            // second, we remove our arch from the map position chain
            prev = node.getPrevArch();
            next = node.getNextArch();

            if (next == node) {
                                // should not happen, but better we double-check to prevent infinite loop
                System.out.println("Arch "+node.getArchName()+" with 'next' pointing on itself!");
                next = null;
            }

            if(prev==null) {
                                // if some up (or null), put this on start
                m_mapGrid[mapx][mapy] = next;
                if(next != null) // if there one up, mark them as starter
                    next.setPrevArch(null);
            } else {
                                // we chain next to prev
                prev.setNextArch(next);
                if(next != null) // we are last... no next
                    next.setPrevArch(prev);
            }
            // remove all inventory...
            deleteInvObject(node.getStartInv());

            node.setNextArch(null);
            node.setPrevArch(null);
            temp = node.getMapMultiNext(); // we save our next part (or null=finished)
            node.setMapMultiHead(null);
            node.setMapMultiNext(null);
            node.setTemp(null);         // just in case...
            node = null;                                // i have no idea how good the garbage collection
            node = temp;                                // is, so i do here some VERY safe stuff
            temp = null;
        }
        prev = null;
        next = null;
        node = null;

        // Level data has changed
        setLevelChangedFlag();
    }

    /**
     * Delete the instance of 'this' object. Freeing the memory by deleting
     * every single ArchObject of the map...
     */
    public void freeMapArchObject() {
        ArchObject node;
        int x,y;

        for(x=0;x<m_mapWidth;x++) {
            for(y=0;y<m_mapHeight;y++) {
                node = m_mapGrid[x][y];
                for(;node != null;) {
                    deleteArchMapObject(node);
                    node = null;
                    node = m_mapGrid[x][y];
                }

            }
        }
    }

    public void setFileName(String strFileName) {
        m_mapArch.setFileName(strFileName);
    }

    public String getFileName() {
        return m_mapArch.getFileName();
    }

    public void setMapName(String name) {
        m_mapArch.setMapName(name);
    }

    public String getMapName() {
        return m_mapArch.getMapName();
    }

	public String getBackgroundMusic() {
        return m_mapArch.getBackgroundMusic();
    }

    public MapArchObject getMapArchObject() {
        return m_mapArch;
    }

    public boolean pointValid(int posx, int posy) {
        return m_control.pointValid(posx, posy);
    }

    /**
     * Searching for a valid exit at the highlighted map-spot.
     * (This can be a teleporter, exit, pit etc.)
     *
     * @return: ArchObject   exit-arch if existent, otherwise null
     */
    public ArchObject getExit() {
        Point hspot = m_control.m_view.getHighlightStart();  // selected spot
        ArchObject exit;            // exit object

        if (hspot.x < 0 || hspot.y < 0)
            return null;  // out of map

        // first, check if the selected arch is a valid exit
        exit = main_control.getMainView().getMapTileSelection();
        if (exit == null || !exitTypes.contains(new Integer(exit.getArchTypNr()))) {
            // if not, we check the whole selected spot for an exit
            for (exit = m_mapGrid[hspot.x][hspot.y]; exit != null &&
                 !exitTypes.contains(new Integer(exit.getArchTypNr())); exit = exit.getNextArch());
        }

        if (exit == null || !exitTypes.contains(new Integer(exit.getArchTypNr())))
            exit = null; // make sure it's either an exit, or null

        // if we have a multipart exit, return the head
        if (exit != null && exit.getRefFlag() && exit.getMapMultiHead() != null)
            exit = exit.getMapMultiHead();

        return exit;
    }

    /**
     * Check if objects get cut off if the map was resized to the given bounds.
     * @param newWidth      the new level width.
     * @param newHeight     the new level height.
     * @return true if objects would be cut off
     */
    public boolean checkResizeMap(int newWidth, int newHeight) {
        boolean object_found = false;  // return value: object found?
        int x, y;                      // index coordinates

        if (m_mapWidth > newWidth) {
            // search the right stripe (as far as being cut off)
            for (x=newWidth; x < m_mapWidth; x++) {
                for (y=0; y < m_mapHeight; y++) {
                    if (m_mapGrid[x][y] != null)
                        object_found = true;
                }
            }
        }
        if (m_mapHeight > newHeight) {
            // search the bottom stripe (as far as being cut off)
            for (y=newHeight; y < m_mapHeight; y++) {
                for (x=0; x < m_mapWidth; x++) {
                    if (m_mapGrid[x][y] != null)
                        object_found = true;
                }
            }
        }

        return object_found;
    }

    /**
     * Resize this map to the new size. If any bounds are smaller than
     * before, the map gets cut on the right and bottom side.
     * Accordingly, new space is attached to right and bottom.
     *
     * @param newWidth    new map width
     * @param newHeight   new map height
     */
    void resizeMap(int newWidth, int newHeight) {
        ArchObject tmp;
        ArchObject[][] newGrid; // the new mapgrid, replacing the old one
        int x, y;               // index coordinates

        // don't allow negative values
        if (newWidth < 0) newWidth=0;
        if (newHeight < 0) newHeight=0;

        // no other thread may access this mapmodel while resizing
        synchronized(this) {
            // first delete all arches in the area that will get cut off
            // (this is especially important to remove all multipart-objects
            // reaching into that area)
            if (m_mapWidth > newWidth) {
                // clear out the right stripe (as far as being cut off)
                for (x=newWidth; x < m_mapWidth; x++) {
                    for (y=0; y < m_mapHeight; y++) {
                        // for every map square: delete all arches on it
                        for (ArchObject node = m_mapGrid[x][y]; node != null;) {
                            tmp = node;                 // save this arch
                            node = node.getNextArch();  // jump to next arch
                            deleteMapArch(tmp.getMyID(), x, y, false, false);   // delete previous one
                        }
                    }
                }
            }

            if (m_mapHeight > newHeight) {
                // clear out the bottom stripe (as far as being cut off)
                // (and yes, there is an area getting sweeped twice - and yes it could
                // be optimized you smartass - but I don't care!)
                for (y=newHeight; y < m_mapHeight; y++) {
                    for (x=0; x < m_mapWidth; x++) {
                        // for every map square: delete all arches on it
                        for (ArchObject node = m_mapGrid[x][y]; node != null;) {
                            tmp = node;                 // save this arch
                            node = node.getNextArch();  // jump to next arch
                            deleteMapArch(tmp.getMyID(), x, y, false, false);   // delete previous one
                        }
                    }
                }
            }

            // Now the critical step: create an ArchObject array of new dimension,
            // copy all objects and set it to replace the current one.
            newGrid = new ArchObject[newWidth][newHeight];
            // fill it with nulls
            for(x=0; x<newWidth; x++) {
                for (y=0; y<newHeight; y++)
                    newGrid[x][y] = null;
            }

            // relink all arches to the new grid
            for(x=0; x < Math.min(newWidth, m_mapWidth); x++) {
                for (y=0; y < Math.min(newHeight, m_mapHeight); y++)
                    newGrid[x][y] = m_mapGrid[x][y];
            }

            // replace old grid by new one
            m_mapGrid = null;
            m_mapGrid = newGrid;
            newGrid = null;

            // adjust the map and model attributes
            m_mapHeight = newHeight;
            m_mapWidth = newWidth;
            m_mapArch.setWidth(newWidth);
            m_mapArch.setHeight(newHeight);

            // an important last point: if there is a highlighted area in a region
            // that got cut off - unhilight it!
            int mposx = m_control.m_view.getHighlightStart().x;
            int mposy = m_control.m_view.getHighlightStart().y;
            if (!pointValid(mposx, mposy) ||
                !pointValid(mposx + m_control.m_view.getHighlightOffset().x,
                            mposy + m_control.m_view.getHighlightOffset().y)) {
                m_control.m_view.unHighlight();
            }

            setLevelChangedFlag(); // the map changed for sure!
        }
    }
}
