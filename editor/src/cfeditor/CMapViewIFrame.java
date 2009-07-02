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
import java.awt.*;

/**
 * The <code>CMapViewIFrame</code> is mainly a wrapper class which
 * creates a basic mapview (-> CMapViewBasic) and displays it in a
 * <code>JInternalFrame</code>.
 * Admittedly the implementation is not as clean as it could be,
 * because the CMapViewBasic is still tied with this object to a
 * certain degree. Full seperation would be nasty to do.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
class CMapViewIFrame extends JInternalFrame implements CMapViewInterface {
    /** The controller of this view. */
    private CMapControl m_control;
    private CMainControl main_control;

    public CMapViewBasic view; // the underlying mapview object

    /**
     * Constructs a level view.
     *
     * @param mc          the main controller
     * @param control     the controller of this view
     */
    CMapViewIFrame (CMainControl mc, CMapControl control) {
        // set title
        super(control.getMapFileName()+" [ "+control.getMapName()+" ]", true, true, true, true );
        m_control = control;
        main_control = mc;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // create instance of "real" view object and add it to the frame
        view = new CMapViewBasic(mc, control, this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(view, BorderLayout.CENTER);
    }

    /**
     * Okay, this is a bit of a hack. True seperation of mapview and
     * frame-component would really mess up the code, or maybe make
     * it "cleaner" but blow things up tenfold.
     * @return basic view component
     */
    public CMapViewBasic getBasicView() {
        return view;
    }

    /**
     * Update the Map-Window Title (according to name and changeFlag)
     */
    public void updateTitle() {

      if(view == null || m_control == null)
        return;

        String strTitle = m_control.getMapFileName()+" [ "+m_control.getMapName()+" ]";

        if (view != null && view.getChangedFlag())
            strTitle += "*";   // * map has changed

        setTitle( strTitle );  // display new title
    }

    /**
     * Returns the controller of this view.
     *@return The controller of this view.
     */
    public CMapControl getLevel() {
        return m_control;
    }

    // following a bunch of wrapper methods which just pass access
    // to the basic mapview object 'view':
    public Point getMapMouseRightPos() {return view.getMapMouseRightPos();}
    public JViewport getViewPort() {return view.getViewport();}
    public Dimension getSize() {return view.getSize();}
    public boolean isGridVisible() {return view.isGridVisible();}
    public void updateLookAndFeel() {view.updateLookAndFeel();}
    public boolean isHighlight() {return view.isHighlight();}
    public void unHighlight() {view.unHighlight();}
    public int getActive_edit_type() {return view.getActive_edit_type();}
    public CMapControl getMapControl() {return m_control;}
    public void setGridVisibility(boolean fVisible) {view.setGridVisibility(fVisible);}
    public void appExitNotify() {view.appExitNotify();}
    public void closeNotify() {view.closeNotify();}
    public Point getHighlightStart() {return view.getHighlightStart();}
    public Point getHighlightOffset() {return view.getHighlightOffset();}
    public void setHotspot(int dx, int dy) {view.setHotspot(dx, dy);}
    public void printFullImage(String filename) throws java.io.IOException {view.printFullImage(filename);}
    void refreshDataFromModel() {view.refreshDataFromModel();}
    void changedFlagNotify() {view.changedFlagNotify();}
    void setMapAndArchPosition(int archid, int x, int y) {
        view.setMapAndArchPosition(archid, x, y);
    }
    Point[] calcArchRedraw(ArchObject arch) {return view.calcArchRedraw(arch);}
    Point[] calcRectRedraw(int ax, int ay, int bx, int by) {
        return view.calcRectRedraw(ax, ay, bx, by);
    }
    public void paintTileArray(Point[] tile) {
        view.paintTileArray(tile);
    }

}
