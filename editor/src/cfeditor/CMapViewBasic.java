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
import java.awt.event.*;
import java.util.*;

import com.visualtek.png.*; // visualtek PNGEncoder

/**
 * <code>CMapViewBasic</code> is the true mapview object. However,
 * it is not bound to a certain type of frame (in order to allow maps
 * be displayed in different types of frames).
 * An instance of this class must only exist in "wrapper classes"
 * like CMapViewIFrame which create a frame, showing the mapview.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
class CMapViewBasic extends JScrollPane {
    /** The controller of this view. */
    private CMapControl m_control;
    private CMainControl main_control;
    ArchObject[][] m_mapGrid;

    private int m_mapWidth;
    private int m_mapHeight;

    /** The tile palette renderer. */
    public CLevelRenderer m_renderer;
    //private JScrollPane m_scrollPane;
    private Rectangle m_previewRect;
    private boolean m_fChanged = false;
    private boolean showMapGrid;

    // this is needed to check the diamond tile areas and convert rectangles to diamond
    private String[] gridMapMask = new String[23];
    private int[][] gridMapOffset = {{-1,0},{0,0},{0,-1},{0,+1},{+1,0}};

    private Point mapMousePos = new Point();
    Point mapMouseRightPos = new Point();       // coordinates of selected tile on the map
    Point mapMouseRightOff = new Point();       // offset from the selected tile while left-click draging
    private Point mapMousePosOff = new Point();

    private boolean highlight_on;    // Is map-tile selection highlighted? true/false
    private boolean[] need_mpanel_update; // indicates that the mapArchPanel needs
    // to be updated when mousebutton is released
    private int draw_intern_count;
    private int draw_intern_drag;

    // interface for the mapview frame
    private CMapViewInterface frameInterface;

    /**
     * Constructs a level view.
     *
     * @param mc          the main controller
     * @param control     the controller of this view
     */
    CMapViewBasic (CMainControl mc, CMapControl control, CMapViewInterface fi) {
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_control = control;
        main_control = mc;
        frameInterface = fi;
        showMapGrid = false;

        highlight_on = false;
        need_mpanel_update = new boolean[3];
        need_mpanel_update[0] = false;
        need_mpanel_update[1] = false;
        need_mpanel_update[2] = false;
        draw_intern_drag=-1;
        draw_intern_count = 0;
        mapMousePos.x = -1;
        mapMousePos.y= -1;
        mapMouseRightPos.x = -1;
        mapMouseRightPos.y = -1;
        mapMouseRightOff.x = 0;
        mapMouseRightOff.y = 0;
        mapMousePosOff.x = -1;
        mapMousePosOff.y= -1;

        gridMapMask[ 0] = "000000000000000000000011112222222222222222222222";
        gridMapMask[ 1] = "000000000000000000001111111122222222222222222222";
        gridMapMask[ 2] = "000000000000000000111111111111222222222222222222";
        gridMapMask[ 3] = "000000000000000011111111111111112222222222222222";
        gridMapMask[ 4] = "000000000000001111111111111111111122222222222222";
        gridMapMask[ 5] = "000000000000111111111111111111111111222222222222";
        gridMapMask[ 6] = "000000000011111111111111111111111111112222222222";
        gridMapMask[ 7] = "000000001111111111111111111111111111111122222222";
        gridMapMask[ 8] = "000000111111111111111111111111111111111111222222";
        gridMapMask[ 9] = "000011111111111111111111111111111111111111112222";
        gridMapMask[10] = "001111111111111111111111111111111111111111111122";
        gridMapMask[11] = "111111111111111111111111111111111111111111111111";
        gridMapMask[12] = "331111111111111111111111111111111111111111111144";
        gridMapMask[13] = "333311111111111111111111111111111111111111114444";
        gridMapMask[14] = "333333111111111111111111111111111111111111444444";
        gridMapMask[15] = "333333331111111111111111111111111111111144444444";
        gridMapMask[16] = "333333333311111111111111111111111111114444444444";
        gridMapMask[17] = "333333333333111111111111111111111111444444444444";
        gridMapMask[18] = "333333333333331111111111111111111144444444444444";
        gridMapMask[19] = "333333333333333311111111111111114444444444444444";
        gridMapMask[20] = "333333333333333333111111111111444444444444444444";
        gridMapMask[21] = "333333333333333333331111111144444444444444444444";
        gridMapMask[22] = "333333333333333333333311114444444444444444444444";

        if (isPickmap()) {
            setBackground(IGUIConstants.BG_COLOR);
        }
        m_renderer = new CLevelRenderer(isPickmap());

        setViewportView(m_renderer);
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

        refreshDataFromModel();

        // set the pixel increment scrolling for clicking once on a scrollbar arrow
        if (!IGUIConstants.isoView) {
            getVerticalScrollBar().setUnitIncrement(32);
            getHorizontalScrollBar().setUnitIncrement(32);
        }
        else {
            getVerticalScrollBar().setUnitIncrement(IGUIConstants.TILE_ISO_YLEN);
            getHorizontalScrollBar().setUnitIncrement(IGUIConstants.TILE_ISO_XLEN);
        }

        CListener listener = new CListener();
        m_renderer.addMouseListener( listener );
        if (!isPickmap())
            m_renderer.addMouseMotionListener( listener );
        m_renderer.updateLookAndFeel();
    }

    public boolean getChangedFlag() {
        return m_fChanged;
    }
    public void setChangedFlag(boolean state) {
        m_fChanged = state;
    }

    public Point getMapMouseRightPos() {
        return mapMouseRightPos;
    }

    public boolean isPickmap() {
        return m_control.isPickmap();
    }

    /**
     * is the map grid visible?
     * @return true if map grid is visible
     */
    public boolean isGridVisible() {
        return showMapGrid;
    }

    /**
     * Update for a new look and feel (-> view menu)
     */
    public void updateLookAndFeel () {
        m_renderer.updateLookAndFeel();
    }

    /**
     * is there a highlighted area in this mapview?
     * @return true if there is a highlighted area
     */
    public boolean isHighlight() {
        return highlight_on;
    }

    /**
     * turn the highlight off
     */
    public void unHighlight() {
        if (highlight_on) {
            highlight_on = false;
            main_control.getMainView().refreshMenus();
        }

        // repaint map to display selection no more
        m_control.repaint();
        main_control.getMainView().setMapTileList(main_control.m_currentMap,-1);
    }

    public int getActive_edit_type() {
        return m_control.getActive_edit_type();
    }

    public CMapControl getMapControl() {
        return m_control;
    }

    public void setGridVisibility( boolean fVisible ) {
        showMapGrid = fVisible;

        this.repaint();
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
    }

    /**
     * Notifies that this level view is about to be closed.
     */
    void closeNotify() {
        m_control.viewCloseNotify(this);
    }

    /**
     * Returns the controller of this view.
     *@return The controller of this view.
     */
    public CMapControl getLevel() {
        return m_control;
    }

    /**
     * @return the coord. of the starting point of the
     *         highlighted tile-selection
     */
    public Point getHighlightStart() {
        return mapMouseRightPos;
    }

    /**
     * @return the coord. of the end point of the highlighted tile-selection
     */
    public Point getHighlightOffset() {
        return mapMouseRightOff;
    }

    /**
     * Highlight the tile at given koordinates (just as
     * if it had been selected with leftclick). Besides, the mapview is
     * always centered on the koordinates as far as possible.
     *
     * @param dx     x-coordinate
     * @param dy     y-coordinate
     */
    public void setHotspot(int dx, int dy) {
        // set the highlighted spot:
        mapMouseRightPos.x = dx;
        mapMouseRightPos.y = dy;
        mapMouseRightOff.x = 0;
        mapMouseRightOff.y = 0;

        if(dx == -1 || dy == -1)
          return;

        // set scroll position accordingly to center on target
        Rectangle scrollto;
        if (!IGUIConstants.isoView) {
            scrollto = new Rectangle ( (dx+1)*32 +16 - getViewport().getViewRect().width/2 ,
                                       (dy+1)*32 +16 - getViewport().getViewRect().height/2,
                                        getViewport().getViewRect().width,
                                        getViewport().getViewRect().height);
            if (scrollto.x+scrollto.width > getViewport().getViewSize().width)
                scrollto.x = getViewport().getViewSize().width - scrollto.width;
            if (scrollto.x < 0 )
                scrollto.x = 0;
            if (scrollto.y+scrollto.height > getViewport().getViewSize().height)
                scrollto.y = getViewport().getViewSize().height - scrollto.height;
            if (scrollto.y < 0 )
                scrollto.y = 0;
            getViewport().setViewPosition( scrollto.getLocation() );
            //getViewport().scrollRectToVisible(scrollto);
        }

        if (!highlight_on) {
            highlight_on = true;
            main_control.getMainView().refreshMenus();
        }

        // repaint map to display selection
        m_control.repaint();
        main_control.getMainView().setMapTileList(main_control.m_currentMap,-1);
    }

    /**
     * @return an image of the entire mapview
     */
    public void printFullImage(String filename) throws java.io.IOException {
        try {
            // create instance of PNGencoder:
            PNGEncoder pngEnc = new PNGEncoder(m_renderer.getFullImage(), filename);
            pngEnc.encode(); // encode image -> create file
        }
        catch (PNGException e) {
            m_control.showMessage("Png Error", "The image could not be created.");
            System.out.println("PNGException: "+e.getMessage());
        }
    }

    /**
     * Refreshes the data in the view from the model.
     */
    void refreshDataFromModel() {
        Dimension forcedSize;
        m_mapWidth    = m_control.getMapWidth();
        m_mapHeight   = m_control.getMapHeight();

        m_mapGrid     = m_control.getMapGrid();

        // define how much drawing space we need for the map
        if(m_control.getIsoView() ) {
            forcedSize =
                new Dimension( (isPickmap() ? 3+2*IGUIConstants.TILE_ISO_YLEN : m_mapHeight*IGUIConstants.TILE_ISO_YLEN)
                                  + m_mapWidth*IGUIConstants.TILE_ISO_YLEN+IGUIConstants.TILE_ISO_XLEN,
                              ((isPickmap() ? java.lang.Math.max(m_mapHeight-2, 0) : m_mapHeight)*IGUIConstants.TILE_ISO_YLEN + (m_mapWidth-m_mapHeight)*IGUIConstants.TILE_ISO_YLEN2)+1+IGUIConstants.TILE_ISO_XLEN );
        } else {
            forcedSize =
                new Dimension(m_mapWidth*32+(isPickmap()?0:64), m_mapHeight*32+(isPickmap()?0:64));
        }
        m_renderer.setPreferredSize( forcedSize );
        m_renderer.setMinimumSize( forcedSize );
        changedFlagNotify();
        repaint();
    }

    /**
     * Notifies that the level changed flag has been changed.
     */
    void changedFlagNotify() {
        m_fChanged = m_control.isLevelChanged();
        frameInterface.updateTitle();
    }

    void setMapAndArchPosition(int archid, int x, int y) {
        mapMouseRightPos.x = x;
        mapMouseRightPos.y = y;
        if(x == -1 || y == -1)
            main_control.getMainView().setMapTileList(null, -1);
        else
            main_control.getMainView().setMapTileList(main_control.m_currentMap, archid);
        repaint();
    }

    /**
     * Determine which map-squares need to be redrawn after
     * inserting/deleting the given ArchObject.
     *
     * @param arch     the arch that is inserted or deleted
     * @return         a <code>Point</code> array containing the coords of the
     *                 tiles which need to be redrawn
     */
    Point[] calcArchRedraw(ArchObject arch) {
        if (arch == null) return null;  // safety check

        Point[] redraw = null;          // return value (coords needing redraw)

        if (main_control.getAutojoin() &&
            main_control.getArch(arch.getNodeNr()).getJoinList() != null) {
            // this arch does autojoining:

            // first look how many we need
            int num = 1;
            if (m_control.pointValid(arch.getMapX()+1, arch.getMapY())) num++;
            if (m_control.pointValid(arch.getMapX(), arch.getMapY()+1)) num++;
            if (m_control.pointValid(arch.getMapX()-1, arch.getMapY())) num++;
            if (m_control.pointValid(arch.getMapX(), arch.getMapY()-1)) num++;

            // now get the coordinates
            redraw = new Point[num];    // create instance of needed size
            redraw[--num] = new Point(arch.getMapX(), arch.getMapY()); // center square
            if (m_control.pointValid(arch.getMapX()+1, arch.getMapY()))
                redraw[--num] = new Point(arch.getMapX()+1, arch.getMapY());
            if (m_control.pointValid(arch.getMapX(), arch.getMapY()+1))
                redraw[--num] = new Point(arch.getMapX(), arch.getMapY()+1);
            if (m_control.pointValid(arch.getMapX()-1, arch.getMapY()))
                redraw[--num] = new Point(arch.getMapX()-1, arch.getMapY());
            if (m_control.pointValid(arch.getMapX(), arch.getMapY()-1))
                redraw[--num] = new Point(arch.getMapX(), arch.getMapY()-1);
        } else if (arch.isMulti()) {
            // this arch is a multi:
            int num = 0;
            if (arch.getRefFlag() && arch.getMapMultiHead() != null)
                arch = arch.getMapMultiHead(); // make sure we got the head

            num = arch.getRefCount()+1;  // get number of parts
            if (num <= 1) return null; // safety check

            redraw = new Point[num];   // create instance of needed size
            for (ArchObject tmp = arch; num > 0 && tmp != null;
                 num--, tmp = tmp.getMapMultiNext()) {
                redraw[num-1] = new Point(tmp.getMapX(), tmp.getMapY());
            }
        } else {
            // just an ordinary single-square arch
            redraw = new Point[1];
            redraw[0] = new Point(arch.getMapX(), arch.getMapY());
        }

        return redraw;
    }

    /**
     * Determine which map-squares need to be painted if the given
     * rectangle is to be redrawn.
     *
     * @param ax    map x-coords. of first corner of the rect
     * @param ay    map y-coords. of first corner of the rect
     * @param bx    map x-coords. of second corner of the rect (opposing edge to ax, ay)
     * @param by    map y-coords. of second corner of the rect (opposing edge to ax, ay)
     * @return      a <code>Point</code> array containing the coords of the
     *              tiles which need to be redrawn
     */
    Point[] calcRectRedraw(int ax, int ay, int bx, int by) {
        Point[] redraw = null;          // return value (coords needing redraw)
        int i = 0;                      // counter

        // get rect. coords of highlighted area: top(left) and bot(tomright) corner
        int topx = Math.min(ax, bx);    // left
        int topy = Math.min(ay, by);    // top
        int botx = Math.max(ax, bx);    // right
        int boty = Math.max(ay, by);    // bottom

        redraw = new Point[(botx-topx+1) * (boty-topy+1)];  // create instance of needed size

        for (int posx = topx; posx <= botx; posx++) {
            for (int posy = topy; posy <= boty; posy++) {
                                // pack all tiles from the rect into the Point-array
                redraw[i] = new Point(posx, posy);
                i++;
            }
        }

        return redraw;
    }

    /**
     * Paint a given array of map-squares. This is much more efficient than
     * repainting the whole map and should be used wherever possible.
     *
     * @param tile      a <code>Point</code> array containing map coords of
     *                  all tiles to draw
     */
    public void paintTileArray(Point[] tile) {
        if (tile != null) {
            if(m_control.getIsoView() == true) {
                // for iso maps we still repaint the whole view :((
                repaint();
            } else {
                // for rectangular maps we paint only the needed tiles
                // System.out.println("-> redraw map:");
                if (tile.length <= 25) {
                    for (int i = tile.length-1; i >= 0; i--) {
                        // paint the tiles
                        if (m_control.m_model.pointValid(tile[i].x, tile[i].y))
                            m_renderer.paintTile(tile[i].x, tile[i].y);
                        //System.out.println("redraw: ("+tile[i].x+", "+tile[i].y+")");
                    }
                } else
                    repaint(); // at some point it's more efficient to draw the whole thing
            }
        }
    }

    /**
     * The component that does the actual rendering of tiles in the palette.
     */
    public class CLevelRenderer extends JComponent {
        // 32x32 Image and Icon for temprary storing tiles before drawing
        private java.awt.Image tmp_image;
        private Graphics       tmp_grfx;
        private ImageIcon      tmp_icon;

        private boolean is_pickmap; // true if the map is a pickmap (those have different layout!)
        private int bOffset;        // offset to map borders (32 for std. rect. maps, 0 for pickmaps)

        /**
         * Constructor
         */
        CLevelRenderer (boolean pickmap){
            is_pickmap = pickmap;
            // initialize the tmp. graphic buffer
            tmp_image = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            tmp_grfx  = tmp_image.getGraphics();
            if (!pickmap)
                tmp_grfx.setColor(main_control.getMainView().getBackground());  // background color of window
            else {
                tmp_grfx.setColor(IGUIConstants.BG_COLOR);
            }
            tmp_icon  = new ImageIcon();

            if (!pickmap)
                bOffset = 32;
            else
                bOffset = 0;
        }

        /**
         * This method is called when the look and feel has changed
         */
        public void updateLookAndFeel () {
            // update background color of window
            if (!is_pickmap)
                tmp_grfx.setColor(getBackground());

        }

        /**
         * @return wether rendered map is a pickmap
         */
        public boolean isPickmap() {
            return is_pickmap;
        }

        /**
         * @return an image of the entire mapview
         */
        public Image getFullImage() {
            int storeOffset; // tmp. storage to save map offset

            int mapWidth  = 32*getMapControl().getMapWidth();  // map width for standard view
            int mapHeight = 32*getMapControl().getMapHeight(); // map height for standard view

            if (IGUIConstants.isoView) {
                // set map dimensions for iso view
                mapWidth = getMapControl().getMapHeight()*IGUIConstants.TILE_ISO_YLEN
                           + getMapControl().getMapWidth()*IGUIConstants.TILE_ISO_YLEN+IGUIConstants.TILE_ISO_XLEN;
                mapHeight = (getMapControl().getMapHeight()*IGUIConstants.TILE_ISO_YLEN + (getMapControl().getMapWidth()
                            - getMapControl().getMapHeight())*IGUIConstants.TILE_ISO_YLEN2)+1+IGUIConstants.TILE_ISO_XLEN;
            }

            // first create a storing place for the image
            Image bufImage = new java.awt.image.BufferedImage(mapWidth, mapHeight,
                                                              java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics bufGrfx = bufImage.getGraphics();
            bufGrfx.setColor(Color.white);
            bufGrfx.setColor(Color.white);
            bufGrfx.fillRect(0,0, mapWidth, mapHeight);

            // paint the mapview into the image
            storeOffset = bOffset; bOffset = 0;
            paintComponent(bufGrfx, true);
            bOffset = storeOffset;
            //return bufImage.getScaledInstance(16*getMapControl().getMapWidth(), 16*getMapControl().getMapHeight(), Image.SCALE_SMOOTH);
            return bufImage;
        }

        public void paintComponent(Graphics grfx) {
            paintComponent(grfx, false);
        }

        /**
         * Paints this component.
         * @param grfx          The graphics context to paint to.
         * @param is_snapshot   true when this drawing is for a "screenshot"-image, false for normal drawing
         */
        public void paintComponent(Graphics grfx, boolean is_snapshot) {
            int xstart, ystart, yoff, xoff;
            ImageIcon img;
            ArchObject node;

            ArchObjectStack archlist = main_control.getArchObjectStack();

            if (is_pickmap) {
                // draw greenisch background for pickmaps
                grfx.setColor(IGUIConstants.BG_COLOR);
                grfx.fillRect(0,0, getWidth(), getHeight());
            }

            if(m_control.getIsoView() == true) {
                // ---------- draw iso map --------------
                ArchObject tmpNode = null; // tmp. store for node while switching in multiparts

                for (int y = 0; y < m_mapHeight; y++ ) {
                    if (!is_pickmap)
                        xstart = (m_mapHeight)*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2;
                    else
                        xstart = 3+2*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2;
                    ystart = y*IGUIConstants.TILE_ISO_YLEN2 + (is_pickmap ? 0 : IGUIConstants.TILE_ISO_YLEN);
                    for (int x = 0; x < m_mapWidth; x++ ) {
                        if(m_mapGrid[x][y]==null) {
                            // empty square
                            if (!is_pickmap)
                                main_control.getUnknownTileIcon().paintIcon(this, grfx, xstart,ystart);
                        } else {
                            node = m_mapGrid[x][y];
                            for(;node != null;) {
                                tmpNode = null;
                                if (node.isMulti() && node.getRefCount()==0 && node.getMapMultiHead() != null) {
                                    // this is a multipart tail:
                                    tmpNode = node;                // save old node
                                    node = node.getMapMultiHead(); // go to multipart head
                                }

                                if(main_control.isTileEdit(node.getEditType()) || main_control.tileEdit == 0
                                   || is_pickmap) {
                                    if(node.getNodeNr()== -1) {
                                        main_control.noarchTileIcon.paintIcon(
                                                                              this, grfx, xstart,ystart);
                                    } else if(node.getFaceObjectFlag()== true) {
                                        main_control.nofaceTileIcon.paintIcon(
                                                                              this, grfx, xstart,ystart);
                                    } else {
                                        if(node.getObjectFaceNr() == -1) {
                                            main_control.getUnknownTileIcon().paintIcon(
                                                                                        this, grfx, xstart,ystart);
                                        } else {
                                            yoff=0;
                                            img = archlist.getFace(node.getObjectFaceNr());
                                            if(img.getIconHeight() > IGUIConstants.TILE_ISO_YLEN)
                                                yoff = img.getIconHeight()-IGUIConstants.TILE_ISO_YLEN;

                                            if (node.isMulti() && node.getRefCount() > 0) {
                                                // multipart images have to be painted with correct offset
                                                if ((tmpNode != null && tmpNode.isLowestPart()) || node.isLowestPart()) {
                                                    img.paintIcon(this, grfx, xstart - MultiPositionData.getXOffset(node.getMultiShapeID(), (tmpNode==null?node.getMultiPartNr():tmpNode.getMultiPartNr())),
                                                                  ystart-yoff + MultiPositionData.getYOffset(node.getMultiShapeID(), (tmpNode==null?node.getMultiPartNr():tmpNode.getMultiPartNr())));
                                                }
                                            }
                                            else
                                            {
                                              xoff=0;
                                              if(img.getIconWidth() > IGUIConstants.TILE_ISO_XLEN)
                                                xoff = (img.getIconWidth()-IGUIConstants.TILE_ISO_XLEN)/2;
                                              img.paintIcon(this, grfx, xstart-xoff, ystart - yoff);
                                            }
                                        }
                                    }
                                }

                                // proceed to next arch
                                if (tmpNode != null)
                                    node = tmpNode.getNextArch();
                                else
                                    node = node.getNextArch();
                            }
                        }
                        xstart +=IGUIConstants.TILE_ISO_XLEN2;
                        ystart +=IGUIConstants.TILE_ISO_YLEN2;
                    }
                }

                if ( showMapGrid ) {
                    // draw iso grid
                    grfx.setColor(Color.black);

                    for (int x = 0; x <= m_mapWidth; x++ ) {
                        grfx.drawLine((m_mapHeight)*IGUIConstants.TILE_ISO_YLEN+ (x+1) * IGUIConstants.TILE_ISO_XLEN2 - 1,
                                      IGUIConstants.TILE_ISO_YLEN+ x * IGUIConstants.TILE_ISO_YLEN2 - 1,
                                      (m_mapHeight)*IGUIConstants.TILE_ISO_YLEN- (m_mapHeight) *IGUIConstants.TILE_ISO_XLEN2
                                      + (x+1) * IGUIConstants.TILE_ISO_XLEN2,
                                      (m_mapHeight) *IGUIConstants.TILE_ISO_YLEN2+IGUIConstants.TILE_ISO_YLEN
                                      + x * IGUIConstants.TILE_ISO_YLEN2 - 1);
                    }

                    for (int y = 0; y <= m_mapHeight; y++ ) {
                        grfx.drawLine((m_mapHeight)*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2
                                      + IGUIConstants.TILE_ISO_XLEN2 - 1,
                                      y *IGUIConstants.TILE_ISO_YLEN2+IGUIConstants.TILE_ISO_YLEN - 1,
                                      (m_mapHeight)*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2
                                      + (m_mapWidth+1) * IGUIConstants.TILE_ISO_XLEN2,
                                      y*IGUIConstants.TILE_ISO_YLEN2+IGUIConstants.TILE_ISO_YLEN
                                      + (m_mapWidth) * IGUIConstants.TILE_ISO_YLEN2 - 1);
                    }
                }
                if(highlight_on && mapMouseRightPos.y != -1 && mapMouseRightPos.x != -1) {
                    // Highlight the selected area
                    PaintHighlightArea(grfx);
                }

                /*
                  if(mapMousePos.y != -1 && mapMousePos.x != -1)
                  {
                  main_control.getGridIcon().paintIcon(
                  this, grfx, (m_mapHeight-1)*26+mapMousePos.x*26-mapMousePos.y*26+26,
                  mapMousePos.x*13+mapMousePos.y*13+26);
                  }
                */
            }
            else {
                // ---------- draw rectangular  map --------------
                // this vector contains all heads of multi-tiles with oversized images
                Vector oversizedMultiHeads = new Vector();

                for (int y = 0; y < m_mapHeight; y++ ) {
                    for (int x = 0; x < m_mapWidth; x++ ) {
                        if(m_mapGrid[x][y]==null) {
                            // empty square
                            if (is_pickmap)
                                grfx.fillRect(x*32+bOffset,y*32+bOffset, 32, 32);
                            else
                                main_control.unknownTileIconX.paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                        }
                        else {
                            node = m_mapGrid[x][y];
                            for(;node != null;) {
                                if(main_control.isTileEdit(node.getEditType()) || main_control.tileEdit == 0
                                   || is_pickmap) {
                                    if(node.getNodeNr()== -1) {
                                        main_control.noarchTileIconX.paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                                    }
                                    else if(node.getFaceObjectFlag()== true) {
                                        main_control.nofaceTileIconX.paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                                    }
                                    else {
                                        if(node.getObjectFaceNr() == -1) {
                                            main_control.unknownTileIconX.paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                                        }
                                        else {
                                            // draw object face
                                            img = archlist.getFace(node.getObjectFaceNr());
                                            if (!node.isMulti() || (img.getIconWidth() == 32 && img.getIconHeight() == 32))
                                                archlist.getFace(node.getObjectFaceNr()).paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                                            else if (node.getRefCount() > 0)
                                                oversizedMultiHeads.addElement(node); // store oversized arches for later
                                        }
                                    }
                                }
                                node = node.getNextArch();
                            }
                        }
                    }
                }
                // at the end, we have to draw the oversized multipart images on top of the rest
                for (int i=0; i<oversizedMultiHeads.size(); i++) {
                    node = (ArchObject)(oversizedMultiHeads.elementAt(i));
                    archlist.getFace(node.getObjectFaceNr()).paintIcon(this, grfx, node.getMapX()*32+bOffset, node.getMapY()*32+bOffset);
                }
                oversizedMultiHeads = null;

                // grid lines
                if (showMapGrid && !is_snapshot) {
                    for (int x = 0; x <= m_mapWidth; x++ ) {
                        grfx.drawLine(
                                      x*32+bOffset,
                                      0+bOffset,
                                      x*32+bOffset,
                                      m_mapHeight*32+bOffset);
                    }
                    for (int y = 0; y <= m_mapHeight; y++ ) {
                        grfx.drawLine(
                                      0+bOffset,
                                      y*32+bOffset,
                                      m_mapWidth*32+bOffset,
                                      y*32+bOffset);
                    }
                }
                if(highlight_on && mapMouseRightPos.y != -1 && mapMouseRightPos.x != -1 && !is_snapshot) {
                    // Highlight the selected area
                    PaintHighlightArea(grfx);
                }

            }
        }

        /**
         * Paints only one tile of the map. This is an incredible
         * time-saver for insert/select/delete tile actions.
         *
         * Important: This method currently works only for standard
         * (rectangular) view. Iso has overlapping tiles which makes
         * this a lot more difficult. :(
         *
         * @param x    map coordinates for the tile to draw
         * @param y    map coordinates for the tile to draw
         */
        public void paintTile(int x, int y) {
            int xstart, ystart, yoff;
            ArchObject node;               // index arch
            Graphics grfx = getGraphics(); // graphics context for drawing in the mapview

            ArchObjectStack archlist = main_control.getArchObjectStack(); // arch stack

            if(m_control.getIsoView() == true) {
                // Hey you! Yes you, behind the screen! - please
                // implement this part :-)
                // Hey, no time AV! :)
            }
            else {
                // ---------- draw tile for rectangular view (non-iso) --------------
                ImageIcon img = null; // tmp image

                // first, draw the object's faces:
                if(m_mapGrid[x][y]==null) {
                    // draw the empty-tile icon (directly to the mapview)
                    if (is_pickmap) {
                        grfx.setColor(IGUIConstants.BG_COLOR);
                        grfx.fillRect(x*32+bOffset,y*32+bOffset, 32, 32);
                    }
                    else
                        main_control.unknownTileIconX.paintIcon(this, grfx, x*32+bOffset,y*32+bOffset);
                } else {
                    node = m_mapGrid[x][y];

                    tmp_grfx.fillRect(0,0, 32, 32);
                    // loop through all arches on that square and draw em
                    for(;node != null;) {
                        if(main_control.isTileEdit(node.getEditType()) || main_control.tileEdit == 0) {
                            if(node.getNodeNr()== -1) {
                                main_control.noarchTileIconX.paintIcon(this, tmp_grfx, 0,0);
                            } else if(node.getFaceObjectFlag()== true) {
                                main_control.nofaceTileIconX.paintIcon(this, tmp_grfx, 0,0);
                            } else {
                                if(node.getObjectFaceNr() == -1) {
                                    main_control.unknownTileIconX.paintIcon(this, tmp_grfx, 0,0);
                                } else {
                                    img = archlist.getFace(node.getObjectFaceNr());
                                    if (!node.isMulti() || (img.getIconWidth() == 32 && img.getIconHeight() == 32)
                                        || node.getRefCount() > 0)
                                        img.paintIcon(this, tmp_grfx, 0,0);
                                    else {
                                        // this is an oversized image and not the head, so it must be shifted
                                        img.paintIcon(this, tmp_grfx, -32*node.getRefX(), -32*node.getRefY());
                                    }
                                }
                            }
                        }

                        node = node.getNextArch(); // next arch
                    }
                }

                // We have been drawing to the tmp. buffer, now convert it to
                // an ImageIcon and paint it into the mapview:
                if (m_mapGrid[x][y] != null) {
                    tmp_icon.setImage(tmp_image);
                    tmp_icon.paintIcon(this, getGraphics(), x*32+bOffset, y*32+bOffset);
                }

                // if grid is active, draw grid lines (right and bottom)
                if ( showMapGrid ) {
                    // horizontal:
                    grfx.drawLine(x*32+bOffset, y*32+bOffset, x*32+bOffset, y*32+32+bOffset);
                    // vertical:
                    grfx.drawLine(x*32+bOffset, y*32+bOffset, x*32+32+bOffset, y*32+bOffset);
                }

                // if tile is highlighted, draw the highlight icon
                PaintHighlightTile(getGraphics(), x, y);
            }
        }

        /**
         * If the given map-square is highlighted, the highligh-icon
         * is drawn on that square.
         *
         * @param x        map coords of the square
         * @param y        map coords of the square
         * @param grfx     graphics context to draw in
         */
        public void PaintHighlightTile(Graphics grfx, int x, int y) {
            // if tile is highlighted, draw the highlight icon:
            if(highlight_on && mapMouseRightPos.y != -1 && mapMouseRightPos.x != -1) {
                // get rect. coords of highlighted area: top(left) and bot(tomright) corner
                int topx = Math.min(mapMouseRightPos.x, mapMouseRightPos.x + mapMouseRightOff.x);
                int topy = Math.min(mapMouseRightPos.y, mapMouseRightPos.y + mapMouseRightOff.y);
                int botx = Math.max(mapMouseRightPos.x, mapMouseRightPos.x + mapMouseRightOff.x);
                int boty = Math.max(mapMouseRightPos.y, mapMouseRightPos.y + mapMouseRightOff.y);

                // Highlight the selected square
                if (x >= topx && x <= botx && y >= topy && y <= boty) {
                    if (m_control.getIsoView() == false)
                        main_control.mapSelIconX.paintIcon( this, grfx, x*32+bOffset, y*32+bOffset);
                    else {
                        // not used yet
                    }

                }
            }
        }

        /**
         * Painting the highlited (selected) area on the map
         * @param grfx      graphics context of mapview
         */
        public void PaintHighlightArea(Graphics grfx) {
            int posx = mapMouseRightPos.x; // coord. of the dragging-startpoint
            int posy = mapMouseRightPos.y;
            int sign_x, sign_y;            // sign = "direction" of the dragging offset

            if (mapMouseRightOff.x > 0) sign_x=1; else if (mapMouseRightOff.x < 0) sign_x=-1; else sign_x=0;
            if (mapMouseRightOff.y > 0) sign_y=1; else if (mapMouseRightOff.y < 0) sign_y=-1; else sign_y=0;

            // Highlight all tiles that are in the rectangle between drag-start and mouse
            for (posx = mapMouseRightPos.x; Math.abs(posx-mapMouseRightPos.x)
                     <= Math.abs(mapMouseRightOff.x); posx += sign_x) {
                for (posy = mapMouseRightPos.y; Math.abs(posy-mapMouseRightPos.y)
                         <= Math.abs(mapMouseRightOff.y); posy += sign_y) {
                    // Draw the Icon:
                    if(m_control.getIsoView() == true) {
                        // in Iso-View
                        main_control.mapSelIcon.paintIcon(this, grfx,
                                (is_pickmap ? 3+2*IGUIConstants.TILE_ISO_YLEN-posy*IGUIConstants.TILE_ISO_XLEN2 :
                                              (m_mapHeight)*IGUIConstants.TILE_ISO_YLEN-posy*IGUIConstants.TILE_ISO_XLEN2)
                                     + posx * IGUIConstants.TILE_ISO_XLEN2,
                                posy*IGUIConstants.TILE_ISO_YLEN2 + (is_pickmap? 0 : IGUIConstants.TILE_ISO_YLEN)
                                     + posx * IGUIConstants.TILE_ISO_YLEN2);
                    }
                    else {
                        // Rectangular view
                        main_control.mapSelIconX.paintIcon( this, grfx, posx*32+bOffset, posy*32+bOffset);
                    }

                    if (sign_y==0) break;
                }
                if (sign_x==0) break;
            }
        }

        /**
         * Returns the map location at the given point or null if no map location
         * is at the point.
         * @param point The coordinates in the renderer view.
         * @return The map location.
         */

        // i'll change it to a fixed point... i want use this for mouse tracking
        // and don't want have millions of mallocs here
        // this should be reworked more intelligent later
        public Point getTileLocationAt( Point point ) {
            int xstart, ystart;

            mapMousePos.x = -1;
            mapMousePos.y= -1;
            mapMousePosOff.x = -1;
            mapMousePosOff.y= -1;

            if (  m_mapGrid == null) // no map, no data
                return null;

            Point mpoint = new Point();
            mpoint.x = mapMousePos.x;
            mpoint.y = mapMousePos.y;

            if(m_control.getIsoView() == true) {
                // most lame way to find the map point
                // but i can't remember the algorithm way for iso on the fly
                // i had done it before, but i forget
                for (int y = 0; y < m_mapHeight; y++ ) {
                    if (!is_pickmap)
                        xstart = (m_mapHeight)*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2;
                    else
                        xstart = 3+2*IGUIConstants.TILE_ISO_YLEN-y*IGUIConstants.TILE_ISO_XLEN2;
                    ystart = y*IGUIConstants.TILE_ISO_YLEN2 + (is_pickmap? 0 : IGUIConstants.TILE_ISO_YLEN);
                    for (int x = 0; x < m_mapWidth; x++ ) {
                        if(point.x >= xstart && point.x < xstart+IGUIConstants.TILE_ISO_XLEN &&
                           point.y >= ystart && point.y < ystart+IGUIConstants.TILE_ISO_YLEN )
                            {
                                // get the offsets in the rectangle
                                mapMousePosOff.x = point.x-xstart;
                                mapMousePosOff.y = point.y-ystart;

                                // generate the offset index from a string tile mask
                                int temp = Integer.parseInt(gridMapMask[point.y-ystart].substring(point.x-xstart,(point.x-xstart)+1));
                                // add the offsets
                                mapMousePos.x = x +gridMapOffset[temp][0];
                                mapMousePos.y = y +gridMapOffset[temp][1];

                                // ok, now check map borders and set to -1/-1 when not in map
                                if(mapMousePos.x<0  || mapMousePos.x>=m_mapWidth ||
                                   mapMousePos.y<0  || mapMousePos.y>=m_mapHeight)
                                    {
                                        mapMousePos.x = -1;
                                        mapMousePos.y= -1;
                                        mapMousePosOff.x = -1;
                                        mapMousePosOff.y= -1;
                                    }

                                mpoint.x = mapMousePos.x;
                                mpoint.y = mapMousePos.y;
                                return mpoint;

                            }
                        xstart +=IGUIConstants.TILE_ISO_XLEN2;
                        ystart +=IGUIConstants.TILE_ISO_YLEN2;
                    }
                }
            } else {
                if(point.x>=bOffset && point.x<(m_mapWidth*32)+bOffset && point.y>=bOffset && point.y<(m_mapHeight*32)+bOffset) {
                    mapMousePos.x = (point.x-bOffset)/32;
                    mapMousePos.y = (point.y-bOffset)/32;

                    mapMousePosOff.x = (point.x-bOffset)-mapMousePos.x*32;
                    mapMousePosOff.y = (point.y-bOffset)-mapMousePos.y*32;
                }
            }

            mpoint.x = mapMousePos.x;
            mpoint.y = mapMousePos.y;
            return mpoint;
        }
    }

    /**
     * The mouse listener for the view.
     */
    public class CListener implements MouseListener, MouseMotionListener {
        private boolean m_fFilling = false;
        private Point m_startMapLoc = null;
        private String insertArchName = ""; // name of arch being inserted

        public void mouseClicked(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
            changedFlagNotify();          // set flag: map has changed
            Point clickPoint = event.getPoint();
            Point mapLoc     = m_renderer.getTileLocationAt( clickPoint );
            Point[] need_redraw = null;  // array of tile coords which need to be redrawn
            Point[] pre_redraw = null;   // array of tile coords which need to be redrawn
            boolean need_full_repaint = false;  // true if full repaint of map needed

            if(mapLoc != null) {
                // in "locked pickmaps" mode, pickmaps react only to leftclicks
                if (m_control.isPickmap() && main_control.isPickmapsLocked()
                    && !(event.getModifiers() == MouseEvent.BUTTON1_MASK && !event.isShiftDown() && !event.isControlDown()))
                    return;

                if (highlight_on && !m_control.getIsoView()) {
                    // if we had a selected rect, we must remove it:
                    if (Math.abs(mapMouseRightOff.x+1)*Math.abs(mapMouseRightOff.y+1) <= 16) {
                        // the rect is relatively small, so we remove it per-tile
                        pre_redraw = calcRectRedraw(mapMouseRightPos.x, mapMouseRightPos.y,
                                                    mapMouseRightPos.x+mapMouseRightOff.x,
                                                    mapMouseRightPos.y+mapMouseRightOff.y);
                        //paintTileArray(need_redraw);
                        //need_redraw = null;
                    } else {
                        // the rect is big, we redraw the whole map later
                        need_full_repaint = true;
                    }
                }

                // right mouse button: insert arch
                if ((event.getModifiers() & MouseEvent.BUTTON3_MASK) != 0 ||
                    ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && event.isShiftDown()) ) {
                    m_previewRect    = new Rectangle();
                    m_previewRect.x      = mapLoc.x;
                    m_previewRect.y      = mapLoc.y;
                    m_previewRect.width  = 1;
                    m_previewRect.height = 1;
                    m_startMapLoc        = mapLoc;

                    if (highlight_on && !m_control.isPickmap()) {
                        highlight_on = false;  // no longer highlighting tiles
                        main_control.getMainView().refreshMenus();
                    }

                    if(mapLoc.x != -1 && mapLoc.y != -1) {
                        if (m_control.isPickmap()) {
                            // insert on pickmap
                            need_redraw = insertMapArchToPickmap(mapLoc.x, mapLoc.y);
                        }
                        else {
                            // insert on normal map
                            mapMouseRightPos.x = mapLoc.x;
                            mapMouseRightPos.y = mapLoc.y;

                            need_redraw = insertSelArchToMap(mapLoc.x, mapLoc.y, true);

                            // get name of inserted arch (this is display in status bar while dragging)
                            ArchObject insArch = null;
                            if ((insArch = main_control.getArchPanelSelection()) != null) {
                                if (insArch.getObjName() != null && insArch.getObjName().length()>0)
                                    insertArchName = main_control.getArchPanelSelection().getObjName();
                                else if (insArch.getArchName() != null && insArch.getArchName().length()>0)
                                    insertArchName = main_control.getArchPanelSelection().getArchName();
                                else if ((insArch = insArch.getDefaultArch()) != null)
                                    insertArchName = main_control.getArchPanelSelection().getArchName();
                                else
                                    insertArchName = "unkown";
                            }
                            else
                                insertArchName = "nothing";

                            main_control.getMainView().setMapTileList(main_control.m_currentMap, -1);
                        }
                    }
                }
                else if (event.getModifiers() == MouseEvent.BUTTON1_MASK &&
                    !event.isShiftDown() && !event.isControlDown()) {

                    // left mouse button: select tiles
                    mapMouseRightPos.x = mapLoc.x;  // position of selected tile
                    mapMouseRightPos.y = mapLoc.y;
                    mapMouseRightOff.x = 0;         // dragging offset
                    mapMouseRightOff.y = 0;

                    if(mapLoc.x == -1 || mapLoc.y == -1) {
                        if (!m_renderer.isPickmap()) {
                            main_control.getMainView().setMapTileList(null,-1);

                            if (highlight_on) {
                                highlight_on = false;
                                main_control.getMainView().refreshMenus();
                            }
                        }
                    } else {
                        if (!m_control.isPickmap())
                            main_control.getMainView().setMapTileList(main_control.m_currentMap,-1);

                        if (!highlight_on) {
                            highlight_on = true;  // highlight the selected area
                            main_control.getMainView().refreshMenus();
                        }
                        // paint the highlight-icon
                        m_renderer.PaintHighlightTile(m_renderer.getGraphics(), mapLoc.x, mapLoc.y);

                        if (m_renderer.isPickmap()) {
                            // an arch of a pickmap was selected
                            main_control.showArchPanelQuickObject(m_control.getMapGrid()[mapLoc.x][mapLoc.y]);
                        }

                        if (m_control.getIsoView()) need_full_repaint = true;
                    }
                } else  {
                    // middle mouse button: delete arch
                    // (I intenionally used "else" here. BUTTON2_MASK seems not to work for some systems)
                    ArchObject tmp_arch;

                    if (highlight_on && !m_control.isPickmap()) {
                        highlight_on = false;  // no longer highlighting tiles
                        main_control.getMainView().refreshMenus();
                    }

                    if(mapLoc.x != -1 && mapLoc.y != -1) {
                        if (!m_control.isPickmap()) {
                            mapMouseRightPos.x = mapLoc.x;
                            mapMouseRightPos.y = mapLoc.y;
                        }

                        // delete the topmost arch (matching the view settings) on that square
                        // and redraw the map
                        tmp_arch = m_control.getMapGrid()[mapLoc.x][mapLoc.y];
                        if (tmp_arch != null) {
                                // go to the topmost arch (end of the list)
                            for (; tmp_arch.getNextArch() != null; tmp_arch = tmp_arch.getNextArch());

                                // now search backwards for matching view settings
                            for (; tmp_arch != null && main_control.tileEdit != 0 &&
                                     !main_control.isTileEdit(tmp_arch.getEditType()); tmp_arch = tmp_arch.getPrevArch());
                            if (tmp_arch != null) {
                                need_redraw = calcArchRedraw(tmp_arch);  // get redraw info
                                m_control.deleteMapArch(tmp_arch.getMyID(), mapLoc.x, mapLoc.y, false, CMapModel.JOIN_ENABLE);
                            }

                                // update mapArch panel
                            main_control.getMainView().setMapTileList(main_control.m_currentMap,-1);
                        }
                    }
                }
            } else
                main_control.getMainView().setMapTileList(null,-1); // for secure...
                                /*
                                  CUndoStack.getInstance( m_control ).add(
                                  new CPaintOp( m_previewRect,
                                  aOrigData,
                                  (short) m_control.getSelectedTile() ) );
                                */
            endFill();

            // finally redraw the map:
            if (!need_full_repaint) {
                                // redraw only tiles that need an update
                paintTileArray(pre_redraw);
                paintTileArray(need_redraw);
            } else
                repaint(); // redraw full map
        }

        /**
         * This method gets called whenever a mousebutton is released
         * @param event      the occurred <code>MouseEvent</code>
         */
        public void mouseReleased(MouseEvent event) {
            int button_nr;  // Number of released mouse button

            if (event.getModifiers() == MouseEvent.BUTTON1_MASK)
                button_nr = 0;  // left button
            else if (event.getModifiers() == MouseEvent.BUTTON3_MASK)
                button_nr = 2;  // right button
            else
                button_nr = 1;  // middle

            // We update the mapArchPanel here and not in the dragging method,
            // because it would considerably slow down the dragging-action.
            if (need_mpanel_update[button_nr]) {
                main_control.getMainView().setMapTileList(main_control.m_currentMap,-1);
                need_mpanel_update[button_nr] = false;
            }
        }

        public void mouseEntered( MouseEvent event ) {
        }

        public void mouseExited( MouseEvent event ) {
            // endFill();
        }

        /**
         * Listen for Mouse move events (no dragging)
         * @param event      the occurred <code>MouseEvent</code>
         */
        public void mouseMoved( MouseEvent event ) {
            int xstart, ystart,xp,yp,xt,yt;

            Point dragPoint = event.getPoint();
            xp = (int)dragPoint.getX();
            yp = (int)dragPoint.getY();

            Point temp = m_renderer.getTileLocationAt(dragPoint);

            // draw Mouse coordinates into the status bar (bottom of window)
            if (mapMousePos.x == -1 && mapMousePos.y == -1)
                CMainStatusbar.getInstance().setText(" Mouse off Map");
            else
                CMainStatusbar.getInstance().setText(" Mouse x:"+ (int) xp + " y:" +(int) yp + "   MAP x:"+mapMousePos.x+" y:"+mapMousePos.y);
            // repaint(); // we should blt here with clipping...
            // this paints the whole map again, but we want only the marker
        }

        /**
         * Listen for mouse drag events on the Map View
         * @param event      the occurred <code>MouseEvent</code>
         */
        public void mouseDragged( MouseEvent event ) {
            int xstart, ystart, xp, yp, xt, yt;

            changedFlagNotify();
            draw_intern_drag=draw_intern_count;
            Point[] need_redraw = null;   // array of tile coords which need to be redrawn

            Point dragPoint = event.getPoint();  // Mouse pointer
            xp = (int)dragPoint.getX();
            yp = (int)dragPoint.getY();
            Point temp = m_renderer.getTileLocationAt(dragPoint);  // tile under Mouse pointer

            if ( event.getModifiers() == MouseEvent.BUTTON1_MASK ) {
                // Left Mouse Button: Selected area gets highlighted

                // Update offset (from starting-dragpoint) only when mouse moved
                // over new tile and mouse is still on the map
                if ((temp.x != -1 && temp.y != -1) &&
                    (mapMouseRightOff.x != temp.x - mapMouseRightPos.x ||
                     mapMouseRightOff.y != temp.y - mapMouseRightPos.y)) {
                    // update offset and redraw mapview
                    mapMouseRightOff.x = temp.x - mapMouseRightPos.x;
                    mapMouseRightOff.y = temp.y - mapMouseRightPos.y;

                    repaint();
                }

                // print location infos on status bar
                if (highlight_on && (mapMouseRightOff.x != 0 || mapMouseRightOff.y != 0)) {
                    CMainStatusbar.getInstance().setText(" Mouse x:"+ (int) xp + " y:" +(int) yp + "   MAP x:"+mapMousePos.x+" y:"+mapMousePos.y
                                                         + "   Selected x:"+(int)(Math.abs(mapMouseRightOff.x)+1)+" y:"+(int)(Math.abs(mapMouseRightOff.y+1)));
                }
                else
                    CMainStatusbar.getInstance().setText(" Mouse x:"+ (int) xp + " y:" +(int) yp + "   MAP x:"+mapMousePos.x+" y:"+mapMousePos.y);
            }
            else if (event.getModifiers() == MouseEvent.BUTTON3_MASK) {
                // Right Mouse Button: Arches get inserted all the way

                CMainStatusbar.getInstance().setText("Mouse x:"+ (int) xp + " y:" +(int) yp + "   MAP x:"+mapMousePos.x+" y:"+mapMousePos.y
                                                     +"   Insert: "+insertArchName);

                if(temp.x != mapMouseRightPos.x ||mapMouseRightPos.y != temp.y) {
                    mapMouseRightPos.x = temp.x;
                    mapMouseRightPos.y = temp.y;

                    if(temp.x == -1 ||temp.y == -1)
                        main_control.getMainView().setMapTileList(null,-1);
                    else {
                        need_redraw = insertSelArchToMap(temp.x, temp.y, false);
                        need_mpanel_update[2] = true; // when dragging is done, update map panel
                    }
                }
            }
            else {
                // Middle Mouse Button: Arches get deleted all the way

                if(temp.x != mapMouseRightPos.x || mapMouseRightPos.y != temp.y) {
                    mapMouseRightPos.x = temp.x;
                    mapMouseRightPos.y = temp.y;

                    if(temp.x == -1 ||temp.y == -1)
                        main_control.getMainView().setMapTileList(null,-1);
                    else {
                        // delete the topmost arch (matching the view settings)
                        // on that square and redraw the map
                        ArchObject tmp_arch = m_control.getMapGrid()[temp.x][temp.y];
                        if (tmp_arch != null) {
                            // go to the topmost arch (end of the list)
                            for (; tmp_arch.getNextArch() != null; tmp_arch = tmp_arch.getNextArch());

                            // now search backwards for matching view settings
                            for (; tmp_arch != null && main_control.tileEdit != 0 &&
                                     !main_control.isTileEdit(tmp_arch.getEditType()); tmp_arch = tmp_arch.getPrevArch());
                            if (tmp_arch != null) {
                                need_redraw = calcArchRedraw(tmp_arch);  // get redraw info
                                m_control.deleteMapArch(tmp_arch.getMyID(), temp.x, temp.y,
                                                        false, CMapModel.JOIN_ENABLE);
                            }
                        }

                        need_mpanel_update[1] = true; // when dragging is done, update map panel
                    }
                }
            }

            // finally redraw all map-squares that need an update
            paintTileArray(need_redraw);
        }

        /**
         * Take the currently selected arch (from archlist or pickmap) and insert
         * it to the defined spot on the currently active map.
         * Redraw info is stored to 'need_redraw', no views get redrawn/updated here.
         * @param mapx         x-tile-coordinate in map
         * @param mapy         y-tile-coordinate in map
         * @param allow_many   when true, it is possible to insert same arches many times.
         *                     when false, only one arch of a kind can be inserted
         * @return array of coordinates for tiles that need to be redrawn
         */
        private Point[] insertSelArchToMap(int mapx, int mapy, boolean allow_many) {
            Point[] need_redraw = null; // returned array of points which need redraw

            // this is the arch that would get inserted from pickmap, but it also could
            // be a default arch (when pickmap has no selection)
            ArchObject newarch = main_control.getArchPanelSelection();

            if (!main_control.getMainView().isPickmapActive() || m_control.isPickmap() ||
                (newarch != null && newarch.isDefaultArch())) {
                // insert default arch from archlist:
                if(m_control.addArchToMap(main_control.getPanelArch(), mapx, mapy, allow_many?-1:draw_intern_count,
                                          CMapModel.JOIN_ENABLE) == false) {
                    //  main_control.getMainView().m_mapPanel.setMapArchList(null);
                    //  Toolkit.getDefaultToolkit().beep();
                }
                else {
                    // insertion successful, now get redraw info
                    newarch = null;  // inserted arch
                    for (newarch = m_control.getMapGrid()[mapx][mapy];
                         newarch.getNextArch() != null; newarch = newarch.getNextArch());
                    if (newarch != null) {
                        // now we've got the inserted arch
                        need_redraw = calcArchRedraw(newarch);
                    }
                }
            }
            else {
                // insert custom arch from the pickmap:
                if (newarch != null) {
                    boolean insert_allowed = true; // are we allowed to insert this?
                    if (!allow_many) {
                        // check if there is already an arch of that kind
                        for (ArchObject t = m_control.getMapGrid()[mapx][mapy];
                             t != null; t = t.getNextArch()) {
                            if (t.getNodeNr() == newarch.getNodeNr() &&
                                t.getArchTypNr() == newarch.getArchTypNr())
                                insert_allowed = false; // there's a match - don't insert a second one
                        }
                    }

                    if (insert_allowed) {
                        if (!newarch.isMulti()) {
                            // insert single tile from pickmap
                            newarch = newarch.getClone(mapx, mapy);
                            m_control.addArchObjectToMap(newarch);
                            need_redraw = calcArchRedraw(newarch);
                        }
                        else {
                            // insert multi tile from pickmap:
                            if (newarch.getMapMultiHead() != null)
                                newarch = newarch.getMapMultiHead();
                            // first insert default arch from archlist
                            if(m_control.addArchToMap(newarch.getNodeNr(), mapx, mapy, allow_many?-1:draw_intern_count,
                                                      CMapModel.JOIN_DISABLE) == false) {
                                // do nothing
                            }
                            else {
                                // insertion successful, now get redraw info
                                ArchObject newdef;  // new inserted default arch
                                for (newdef = m_control.getMapGrid()[mapx][mapy];
                                     newdef.getNextArch() != null; newdef = newdef.getNextArch());
                                if (newdef != null) {
                                    // now we've got the inserted arch, copy the custom stuff from pickmap
                                    newdef.setArchText(newarch.getArchText());
                                    newdef.setObjName(newarch.getObjName());
                                    newdef.resetMsgText();
                                    newdef.addMsgText(newarch.getMsgText());

                                    need_redraw = calcArchRedraw(newdef);
                                }
                            }

                        }
                    }
                }
            }
            return need_redraw;
        }

        /**
         * This method is only called for pickmaps. Take the currently
         * highlighted arch on the map (if any) and insert it on this pickmap.
         * Redraw info is stored to 'need_redraw', no views get redrawn/updated here.
         * @param mapx         x-tile-coordinate in pickmap
         * @param mapy         y-tile-coordinate in pickmap
         * @return array of coordinates for tiles that need to be redrawn
         */
        private Point[] insertMapArchToPickmap(int mapx, int mapy) {
            Point[] need_redraw = null; // returned array of points which need redraw

            ArchObject newarch = null;
            CMapControl current_map = main_control.m_currentMap;
            // insertion is only allowed for valid *empty* squares
            if (current_map != null && m_control.isPickmap() && m_control.pointValid(mapx, mapy)
                && m_control.getMapGrid()[mapx][mapy] == null) {
                // get the currently selected map arch
                newarch = main_control.getMainView().getMapTileSelection();
                if (newarch != null) {
                    if (!newarch.isMulti()) {
                        // insert single tile from pickmap
                        newarch = newarch.getClone(mapx, mapy);
                        m_control.addArchObjectToMap(newarch);
                        need_redraw = calcArchRedraw(newarch);
                    }
                    else {
                        // insert multi tile from pickmap:
                        if (newarch.getMapMultiHead() != null)
                            newarch = newarch.getMapMultiHead();

                        // check if all spaces are free that the multi will occupy
                        boolean allSpacesFree = true;
                        for (int dx=mapx; dx-mapx <= newarch.getRefMaxX(); dx++) {
                            for (int dy=mapy; dy-mapy <= newarch.getRefMaxY(); dy++) {
                                if (!m_control.pointValid(dx, dy) || m_control.getMapGrid()[dx][dy] != null)
                                    allSpacesFree = false;
                            }
                        }

                        // first insert default arch from archlist
                        if(!allSpacesFree || m_control.addArchToMap(newarch.getNodeNr(), mapx, mapy, -1,
                                                  CMapModel.JOIN_DISABLE) == false) {
                            // do nothing
                        }
                        else {
                            // insertion successful, now get redraw info
                            ArchObject newdef;  // new inserted default arch
                            for (newdef = m_control.getMapGrid()[mapx][mapy];
                                 newdef.getNextArch() != null; newdef = newdef.getNextArch());
                            if (newdef != null) {
                                // now we've got the inserted arch, copy the custom stuff from pickmap
                                newdef.setArchText(newarch.getArchText());
                                newdef.setObjName(newarch.getObjName());
                                newdef.resetMsgText();
                                newdef.addMsgText(newarch.getMsgText());

                                need_redraw = calcArchRedraw(newdef);
                            }
                        }
                    }
                }
            }

            return need_redraw;
        }

        void endFill() {
            m_fFilling = false;
            m_previewRect = null;
            m_startMapLoc = null;
            // repaint();
        }

        boolean isFilling() {
            return m_fFilling;
        }
    }

    public class CPaintOp implements IUndoable {

        Rectangle m_fillRect;
        int[][] m_aOrigData;
        int     m_tileIndex;

        CPaintOp( Rectangle fillRect, int[][] aOrigData, int tileIndex ) {
            m_fillRect  = fillRect;
            m_aOrigData = aOrigData;
            m_tileIndex = tileIndex;
        }

        public void undo() {
            for (int y = 0; y < m_fillRect.height; y++ ) {
                for (int x = 0; x < m_fillRect.width; x++ ) {
                    // m_levelGrid[x + m_fillRect.x][y + m_fillRect.y] =
                    // m_aOrigData[x][y];
                    // repaint();
                }
            }
        }

        public void redo() {
            for (int y = m_fillRect.y;
                 y < m_fillRect.height + m_fillRect.y;
                 y++ ) {
                for (int x = m_fillRect.x;
                     x < m_fillRect.width + m_fillRect.x;
                     x++ ) {
                    //                m_levelGrid[x][y] = m_tileIndex;
                    //       repaint();
                }
            }
        }

        public boolean isUndoable() {
            return true;
        }

        public boolean isRedoable() {
            return true;
        }

        public String getName() {
            return "Paint";
        }

    }

}
