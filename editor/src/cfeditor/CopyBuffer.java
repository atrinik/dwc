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

import java.awt.*;
import java.util.*;
/**
 * This class manages the cut/copy/paste actions in maps. The data is
 * stored in an ordinary map-object.  The CopyBuffer instance is
 * currently attached to CMainControl.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CopyBuffer {
    // Constants for the CopyNCut function
    private static final int DO_CLEAR = 1;
    private static final int DO_CUT = 2;
    private static final int DO_COPY = 3;

    private CMainControl main_ctrl; // reference to main control
    private CMapModel map_data;     // map object to store the arches
    private CMapControl buff_ctrl;  // map-control structure for map_data
    private boolean is_empty;       // Is the CopyBuffer empty?
    private ArchObject head;        // head of map
    private MapArchObject maparch;  // dummy for maparch

    int buf_width;          // buffer's width  (counted in tiles)
    int buf_height;                     // buffer's height (counted in tiles)

    /**
     * Konstructor
     * @param main_control    main control
     */
    public CopyBuffer (CMainControl main_control) {
        is_empty = true;          // Buffer is empty
        main_ctrl = main_control; // link main control in

        head = new ArchObject();
        maparch = new MapArchObject();
        buf_width=0; buf_height=0;
        maparch.setWidth(buf_width);
        maparch.setHeight(buf_height);
        maparch.setFileName("cb");
        maparch.setMapName("cb");

        map_data = new CMapModel(main_ctrl, buff_ctrl, head, maparch);
    }

    /**
     * get the info weither CopyBuffer is empty
     * @return true if the copybuffer is empty
     */
    public boolean isEmpty() {
        return is_empty;
    }

    /**
     * Executing the Clear command
     * @param m_ctrl    MapControl of the active map where we copy from
     */
    public void clear (CMapControl m_ctrl) {
        CopyNCut(m_ctrl, DO_CLEAR);
    }

    /**
     * Excecuting the Cut command
     * @param m_ctrl    MapControl of the active map where we copy from
     */
    public void cut (CMapControl m_ctrl) {
        CopyNCut(m_ctrl, DO_CUT);
    }

    /**
     * Excecuting the Copy command
     * @param m_ctrl    MapControl of the active map where we copy from
     */
    public void copy (CMapControl m_ctrl) {
        CopyNCut(m_ctrl, DO_COPY);
    }

    /**
     *
     * CopyNCut implements clear, cut and copy in one function
     * (since they are so similar).
     *
     * @param m_ctrl     MapControl of the active map where we copy from
     * @param mode       defines if we have a cut, copy or paste action
     */
    private void CopyNCut (CMapControl m_ctrl, int mode) {
        Point startp = m_ctrl.m_view.getHighlightStart();  // start of highlighted rect
        Point offset = m_ctrl.m_view.getHighlightOffset(); // offset of rect from startp
        int posx, posy;              // Index
        ArchObject arch, clone, tmp; // tmp. arch reference

        if (!m_ctrl.m_view.isHighlight()) return;  // should actually never happen

        // convert negative 'offset' into positive by flipping 'startp'
        if (offset.x < 0) {
            startp.x += offset.x;
            offset.x = Math.abs(offset.x);
        }
        if (offset.y < 0) {
            startp.y += offset.y;
            offset.y = Math.abs(offset.y);
        }

        // Prepare the buffer (if it's a cut or copy)
        if (mode == DO_CUT || mode == DO_COPY) {
            if (is_empty) {
                is_empty = false;  // from now on the buffer is never empty again
                main_ctrl.getMainView().refreshMenus();  // "Paste" enabled
            }

            // recycle and resize buffer
            try {
                map_data.freeMapArchObject();      // free old map (/buffer) object
                buf_width = Math.abs(offset.x)+1;  // new width
                buf_height = Math.abs(offset.y)+1; // new height
                maparch.setWidth(buf_width);
                maparch.setHeight(buf_height);
                map_data = null; buff_ctrl = null; // free objects (at least that is the plan and theory)
                map_data = new CMapModel(main_ctrl, buff_ctrl,
                                         null, maparch);  // new MapModel
                buff_ctrl = new CMapControl(main_ctrl, null, maparch, false); // new MapControl
            }
            catch (CGridderException e) {}
        }

        // cycle through all tile coordinates which are highlighted:
        for (posx = startp.x; posx-startp.x <= offset.x; posx++) {
            for (posy = startp.y; posy-startp.y <= offset.y; posy++) {
                                // do the copying for one tile position:
                arch = m_ctrl.getMapGrid()[posx][posy];

                while (arch != null) {
                    // store a clone of the arch in the CopyBuffer
                    // (for multiparts, only the heads get copied into the buffer)
                    // arches that don't match the view settings are ignored!
                    if ((mode == DO_CUT || mode == DO_COPY) &&
                        !arch.getRefFlag() && arch.getContainer() == null &&
                        (main_ctrl.tileEdit == 0 || main_ctrl.isTileEdit(arch.getEditType()))) {
                        // copy this arch
                        clone = arch.getClone(Math.abs(posx-startp.x), Math.abs(posy-startp.y));
                        buff_ctrl.addArchObjectToMap(clone);
                    }

                    // delete the arch if we have a "cut" or "clear" command
                    // again, arches that don't match the view settings are ignored
                    if ((mode == DO_CLEAR || mode == DO_CUT) && (main_ctrl.tileEdit == 0 ||
                                                                 main_ctrl.isTileEdit(arch.getEditType()))) {
                        // store next arch in tmp
                        tmp = arch.getNextArch();

                        // delete arch (without redrawing the map)
                        // For CUT we don't delete multi tails of multis which are left or
                        // above the head (we would miss to copy them otherwise).
                        if (mode == DO_CLEAR || !arch.isMulti() || arch.getRefCount() > 0 ||
                            (arch.getMapMultiHead() != null && arch.getRefX() >= 0 && arch.getRefY() >= 0))
                            m_ctrl.deleteMapArch(arch.getMyID(), posx, posy, false, CMapModel.JOIN_DISABLE);

                        arch = tmp; // next arch
                    }
                    else
                        arch = arch.getNextArch(); // process next arch on that same spot
                }
            }
        }

        // finally redraw the map
        if (mode != DO_COPY)
            main_ctrl.refreshCurrentMap();
    }

    /**
     * Excecuting the Paste command
     *
     * @param m_ctrl    MapControl of the active map we paste on
     */
    public void paste (CMapControl m_ctrl) {
        Point startp = m_ctrl.m_view.getHighlightStart();  // start of highlighted rect
        int posx, posy;            // Index
        ArchObject arch, clone;    // tmp. arch reference

        if (!m_ctrl.m_view.isHighlight()) return;  // should actually never happen

        // cycle through all tile coordinates which are highlighted:
        for (posx = startp.x; Math.abs(posx-startp.x) < buf_width; posx++) {
            for (posy = startp.y; Math.abs(posy-startp.y) < buf_height; posy++) {
                                // paste the archs if on the map:
                if (m_ctrl.pointValid(posx, posy)) {
                    arch = buff_ctrl.getMapGrid()[posx-startp.x][posy-startp.y];
                    while (arch != null) {
                        if (!arch.isMulti()) {
                            // read arch from buffer and stick in on the map
                            clone = arch.getClone(posx, posy);
                            m_ctrl.addArchObjectToMap(clone);
                        }
                        arch = arch.getNextArch(); // go to next arch
                    }
                }
            }
        }

        // now we loop through the tiles again and paste all multis found
        for (posx = startp.x; Math.abs(posx-startp.x) < buf_width; posx++) {
            for (posy = startp.y; Math.abs(posy-startp.y) < buf_height; posy++) {
                                // paste the archs if on the map:
                if (m_ctrl.pointValid(posx, posy)) {
                    arch = buff_ctrl.getMapGrid()[posx-startp.x][posy-startp.y];
                    while (arch != null) {
                        if (arch.getRefCount() > 0) {
                            // first we clone the head
                            clone = arch.getClone(posx, posy);

                            // second we insert a default multi on the map
                            if (m_ctrl.addArchToMap(arch.getNodeNr(), posx, posy, -1, CMapModel.JOIN_DISABLE)) {
                                // third we chop off the default head and attach our clone
                                ArchObject old_head;    // get default head
                                for (old_head = m_ctrl.getMapGrid()[posx][posy]; old_head != null &&
                                         old_head.getNextArch() != null; old_head = old_head.getNextArch());

                                if (old_head != null && old_head.getNodeNr() == arch.getNodeNr()) {
                                    // replace old head with our clone:
                                    if (old_head.getPrevArch() != null) {
                                        old_head.getPrevArch().setNextArch(clone);
                                        clone.setPrevArch(old_head.getPrevArch());
                                    }
                                    else {
                                        // no previous arch, we must stick it on the grid
                                        m_ctrl.getMapGrid()[posx][posy] = clone;
                                    }

                                    if (old_head.getNextArch() != null) {
                                        old_head.getNextArch().setPrevArch(clone);
                                        clone.setNextArch(old_head.getNextArch());
                                    }

                                    clone.setMyID(old_head.getMyID()); // pass ID to new head
                                    clone.setMapMultiNext(old_head.getMapMultiNext()); // set link to tail

                                    for (ArchObject tmp = clone.getMapMultiNext(); tmp != null;
                                         tmp = tmp.getMapMultiNext())
                                        tmp.setMapMultiHead(clone); // all tails point to new head

                                    // delete old head:
                                    old_head.setNextArch(null); old_head.setPrevArch(null);
                                    old_head.setMapMultiNext(null); old_head.setNextInv(null);
                                    old_head = null;
                                }
                                else
                                    System.out.println("Error in CopyBuffer.paste(): Couldn't find multi-head after insertion!");
                            }
                        }
                        arch = arch.getNextArch(); // go to next arch
                    }
                }
            }
        }

        // now the map and toolbars must be redrawn
        m_ctrl.repaint();
        main_ctrl.getMainView().RefreshMapTileList();
    }

    /**
     * Excecuting the Fill command
     *
     * @param m_ctrl       MapControl of the active map we paste on
     * @param fill_below   if true, the filling content is placed *below* the existing map
     */
    public void fill (CMapControl m_ctrl, boolean fill_below, CMapControl seed, int rand) {
        Point startp = m_ctrl.m_view.getHighlightStart();  // start of highlighted rect
        Point offset = m_ctrl.m_view.getHighlightOffset(); // offset of rect from startp
        int posx, posy, max=0;            // Index
        ArchObject arch, clone;    // tmp. arch reference

        if (!m_ctrl.m_view.isHighlight()) return;  // should actually never happen
        if (seed == null && main_ctrl.getArchPanelSelection() == null) return; // no selected arch to fill with

        // If offset is zero and map-spot empty, a floodfill is done
        if (offset.x == 0 && offset.y == 0 &&
            m_ctrl.getMapGrid()[startp.x][startp.y] == null) {
            arch = main_ctrl.getArchPanelSelection();
            if (arch != null)
                floodfill(m_ctrl, startp.x, startp.y, arch, arch.isDefaultArch()); // floodfill
        }
        else {
            // Rectangular fill:
            // convert negative 'offset' into positive by flipping 'startp'
            if (offset.x < 0) {
                startp.x += offset.x;
                offset.x = Math.abs(offset.x);
            }
            if (offset.y < 0) {
                startp.y += offset.y;
                offset.y = Math.abs(offset.y);
            }

            // get the arch to fill with
            boolean is_defarch = false;
            arch = null;
            if(seed == null)
            {
              arch = main_ctrl.getArchPanelSelection();
              is_defarch = arch.isDefaultArch();
            }
            else
            {
              max = countMapArches(seed) * 2;
              if(max==0)
                return;
            }

            // cycle through all tile coordinates which are highlighted:
            for (posx = startp.x; posx-startp.x <= offset.x; posx++) {
                for (posy = startp.y; posy-startp.y <= offset.y; posy++) {
                    // Insert the new arch into the map
                    if(rand != -1 && rand != 100 && rand < (main_ctrl.m_generator.nextInt(100)+1) )
                      continue;
                    if(seed != null)
                    {
                      arch = getRandomMapArch(seed, max);
                      is_defarch = arch.isDefaultArch();
                    }
                    if (is_defarch) {
                        m_ctrl.addArchToMap(main_ctrl.getPanelArch(), posx, posy, 0,
                                            CMapModel.JOIN_DISABLE, fill_below);
                    }
                    else {
                        // insert arch-clone from pickmap
                        m_ctrl.addArchObjectToMap(arch.getClone(posx, posy), fill_below);
                    }
                }
            }
        }

        // now the map and toolbars must be redrawn
        m_ctrl.repaint();
        main_ctrl.getMainView().RefreshMapTileList();
    }

    private int countMapArches(CMapControl  base_map)
    {
      MapArchObject map = base_map.m_model.getMapArchObject();
      ArchObject node;
      ArchObject[][] m_grid = base_map.m_model.getMapGrid();
      int x,y,count=0;

      for(x=0;x<map.getWidth();x++) {
          for(y=0;y<map.getHeight();y++) {
              node = m_grid[x][y];
              for(;node!= null;) {
                  // only non muli suckers
                  if((node.getMapMultiHead() == null && node.getMapMultiNext()==null) ||
                     (node.getRefCount() > 0 && node.getMapMultiNext()!=null)) {
                    count++;
                  }
                  node = node.getNextArch();
              }
          }
      }

      return count;
    }

    private ArchObject getRandomMapArch(CMapControl base_map, int max)
    {
      MapArchObject map = base_map.m_model.getMapArchObject();
      ArchObject node;
      ArchObject[][] m_grid = base_map.m_model.getMapGrid();
      int x,y,count=0, rand = main_ctrl.m_generator.nextInt(max);

      for(;;)
      {
        for (x = 0; x < map.getWidth(); x++) {
          for (y = 0; y < map.getHeight(); y++) {
            node = m_grid[x][y];
            for (; node != null; ) {
              // only non muli suckers
              if ( (node.getMapMultiHead() == null && node.getMapMultiNext() == null) ||
                  (node.getRefCount() > 0 && node.getMapMultiNext() != null)) {
                count++;
                if (count >= rand)
                  return node;

              }
              node = node.getNextArch();
            }
          }
        }
      }
    }

    /**
     * Floodfill the map, starting at the highlighted square
     *
     * Okay, yes, this algorithm is as inefficient as it could be.
     * But it is short and easy. And CF maps are so small anyways.
     *
     * @param m_ctrl     MapControl of the active map we paste on
     * @param x          starting x-coord for floodfill
     * @param y          starting y-coord for floodfill
     * @param arch       ArchObject to fill with
     * @param is_defarch true when 'arch' is a default arch
     */
    private void floodfill (CMapControl m_ctrl, int x, int y, ArchObject arch,
                            boolean is_defarch) {
        // insert new arch to x,y
        //m_ctrl.addArchToMap(main_ctrl.getPanelArch(), x, y, 0, CMapModel.JOIN_DISABLE);
        if (is_defarch) {
            m_ctrl.addArchToMap(arch.getNodeNr(), x, y, 0, CMapModel.JOIN_DISABLE);
        }
        else {
            // insert arch-clone from pickmap
            m_ctrl.addArchObjectToMap(arch.getClone(x, y));
        }

        // now go recursive into all four directions
        if (m_ctrl.pointValid(x-1, y) && m_ctrl.getMapGrid()[x-1][y] == null)
            floodfill (m_ctrl, x-1, y, arch, is_defarch);
        if (m_ctrl.pointValid(x, y-1) && m_ctrl.getMapGrid()[x][y-1] == null)
            floodfill (m_ctrl, x, y-1, arch, is_defarch);
        if (m_ctrl.pointValid(x+1, y) && m_ctrl.getMapGrid()[x+1][y] == null)
            floodfill (m_ctrl, x+1, y, arch, is_defarch);
        if (m_ctrl.pointValid(x, y+1) && m_ctrl.getMapGrid()[x][y+1] == null)
            floodfill (m_ctrl, x, y+1, arch, is_defarch);
    }

    /**
     * Replace objects on the map
     *
     * @param m_ctrl   MapControl of the active map where the action was invoked
     */
    public void replace(CMapControl m_ctrl) {
        ReplaceDialog.getInstance().display(m_ctrl);
    }
}
