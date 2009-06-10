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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * The panel that holds the map-tiles of the selected map square.
 * (The window to the right)
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMapTileList extends JPanel {
    /** Controller of this subview. */
    private CMainControl m_control;
    private CMainView m_view;
    /** The "Import..." button. */
    private JList m_list;
    private JScrollPane scrollPane;
    final DefaultListModel model;

    private String list_def_inv = "0000000001";
    private String list_def = "0000000000";
    private int post_select = -1;
    private int list_counter = 0;

    // time of last click in the panel (value in msec by Date.getTime())
    // and arch-ID of arch that received last click
    private long last_click = -1;
    private int last_click_id = -1;

    /* Build Panel */
    CMapTileList( CMainControl control , CMainView view) {
        m_control = control;
        m_view = view;
        setLayout( new BorderLayout() );

        model = new DefaultListModel();
        m_list = new JList(model);
        m_list.setCellRenderer(new MyCellRenderer());
        m_list.setBackground(Color.lightGray);
        scrollPane = new JScrollPane(m_list);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        add(scrollPane, BorderLayout.CENTER);

        JPanel dummy = new JPanel();
        if (m_view.isMapTileListBottom())
            dummy.setLayout(new GridLayout(2, 1));
        JScrollPane scrollPane2 = new JScrollPane(dummy);
        scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane2.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        if (!m_view.isMapTileListBottom())
            add(scrollPane2, BorderLayout.SOUTH); // put up/down buttons south
        else {
            add(scrollPane2, BorderLayout.WEST);  // put up/down buttons west
            scrollPane2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // don't show the border
        }

        CFancyButton buttonDown = new CFancyButton(null, "Move Tile Position Down",
                                  IGUIConstants.MOVE_DOWN_ICON,
                                  new ActionListener() {
                                      public void actionPerformed(ActionEvent event) {
                                          m_control.moveTileDown(getMapTileSelection(), true);
                                      }
                                  });
        buttonDown.setVerticalTextPosition( CFancyButton.BOTTOM );
        buttonDown.setHorizontalTextPosition( CFancyButton.CENTER );
        if (!m_view.isMapTileListBottom())
            dummy.add(buttonDown, BorderLayout.EAST);

        CFancyButton buttonUp = new CFancyButton(null, "Move Tile Position Up",
                                IGUIConstants.MOVE_UP_ICON,
                                new ActionListener() {
                                    public void actionPerformed(ActionEvent event) {
                                        m_control.moveTileUp(getMapTileSelection(), true);
                                     }
                                 });
        buttonUp.setVerticalTextPosition( CFancyButton.BOTTOM );
        buttonUp.setHorizontalTextPosition( CFancyButton.CENTER );
        if (!m_view.isMapTileListBottom())
            dummy.add(buttonUp, BorderLayout.WEST);
        else {
            dummy.add(buttonUp);
            dummy.add(buttonDown);
        }

        m_list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                m_view.SetMapArchPanelObject(getMapTileSelection());
            }
        });

        /**
         * listen for mouse events in the panel
         */
        m_list.addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getModifiers() == MouseEvent.BUTTON1_MASK &&
                    !e.isShiftDown() && !e.isControlDown()) {
                    // --- left mouse button: select arch ---
                    // first, check if this is a doubleclick
                    long this_click = (new Date()).getTime();
                    if (this_click - last_click < IGUIConstants.DOUBLECLICK_MS &&
                        last_click_id != -1 && last_click_id == getMapTileSelection().getMyID()) {
                        // doubleclick: open attribute window
                        m_control.openAttrDialog(getMapTileSelection());
                    }
                    else {
                        // single click: now make this arch selected
                        m_view.SetMapArchPanelObject(getMapTileSelection());
                    }

                    // save values for next click
                    last_click = this_click;
                    if (getMapTileSelection() != null)
                        last_click_id = getMapTileSelection().getMyID();
                    else
                        last_click_id = -1;
                }
                else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0 ||
                         ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && e.isShiftDown()) ) {
                    // --- right mouse button: insert arch ---
                    String entry;

                    if ((m_list.locationToIndex(e.getPoint()) >= 0 || m_list.getModel().getSize() > 0) &&
                        m_control.getArchPanelSelection() != null) {

                        // get the selected arch from the map
                        if (m_list.locationToIndex(e.getPoint()) < 0)
                            entry = model.getElementAt(0).toString();
                        else
                            entry = model.getElementAt(m_list.locationToIndex(e.getPoint())).toString();

                        int num = Integer.parseInt(entry.substring(0,10));
                        int mapx = Integer.parseInt(entry.substring(10,20));
                        int mapy = Integer.parseInt(entry.substring(20,30));

                        // find the lowest Y-coord. in list, to see if selection points below it
                        // (We need this because JDK 1.4 doesn't give locationToIndex -1 when below)
                        int lowestY = 0;
                        if (m_list.getFirstVisibleIndex() != -1) {
                            try {
                                Rectangle bounds = m_list.getCellBounds(m_list.getFirstVisibleIndex(),
                                                                        m_list.getLastVisibleIndex());
                                lowestY = (int)(bounds.getY()+bounds.getHeight());
                            }
                            catch (NullPointerException ne) {
                                // and JDK 1.3 throws a NullpointerException here - real shit!
                                lowestY = -10;
                            }
                        }

                        // insert arch next to selected position
                        if (m_list.locationToIndex(e.getPoint()) < 0 || (lowestY != -10 && (int)(e.getPoint().getY()) > lowestY)) {
                            m_control.insertArchToMap(m_control.getArchPanelSelection(), m_control.getPanelArch(), null, mapx, mapy, CMapModel.JOIN_ENABLE);
                        }
                        else
                            m_control.insertArchToMap(m_control.getArchPanelSelection(), m_control.getPanelArch(), m_control.getMapArch(num, mapx, mapy),
                                                      mapx, mapy, CMapModel.JOIN_ENABLE);

                        // refresh
                        m_control.m_currentMap.setLevelChangedFlag();  // the map has been modified
                        m_control.refreshCurrentMap();
                    }
                }
                else {
                    // --- middle mouse button: delete arch ---
                    deleteIndexFromList(m_list.locationToIndex(e.getPoint()));
                }
            }
        });


    }

    public ArchObject getMapTileSelection() {
        // find the selected entry if one
        int index = m_list.getSelectedIndex();
        if (index >= m_list.getModel().getSize() || index < 0
            || m_list.getModel().getSize() <= 0 || m_control.m_currentMap == null)
            return null;

        // parse selected entry and get the arch object
        String entry = model.getElementAt(index).toString();
        int num = Integer.parseInt(entry.substring(0,10));
        int mapx = Integer.parseInt(entry.substring(10,20));
        int mapy = Integer.parseInt(entry.substring(20,30));

        return(m_control.getMapArch(num,mapx,mapy));
    }


    //insert a tile index of list, this will delete it
    void deleteIndexFromList(int index) {
        if (index != -1 && index <m_list.getModel().getSize()) {
            ArchObject temp = getMapTileSelection();
            String entry = model.getElementAt(index).toString();
            int num = Integer.parseInt(entry.substring(0,10));
            int mapx = Integer.parseInt(entry.substring(10,20));
            int mapy = Integer.parseInt(entry.substring(20,30));
            m_control.deleteMapArch(num, mapx, mapy, true, CMapModel.JOIN_ENABLE);
            m_view.setMapTileList(m_control.m_currentMap, (temp==null ? -1 : temp.getMyID()));
        }
    }

    void appExitNotify() {

    }

    public int getPanelArch(int oldindex) {
        int index=-1;

        if(oldindex == -1)
            index = m_list.getFirstVisibleIndex();
        else if(m_list.getModel().getSize()<= oldindex)
            index = oldindex;
        if(index != -1 && m_list.getModel().getSize()>0)
            m_list.setSelectedIndex(index);
        return(m_list.getSelectedIndex());
    }

    void refresh() {
        int id = -1;
        ArchObject sel = getMapTileSelection();
        if(sel != null)
            id = getMapTileSelection().getMyID();
        m_view.setMapTileList(m_control.m_currentMap,id);
        repaint();
    }

    // get map start node, then list all arches from bottom up.
    // i used 10 digits to code the object ids, that should be enough for
    // running the editor a long time
    // (if arch id != -1, he trys to sit on this arch) ???
    public void setMapTileList(CMapControl map, int archid) {
        int sIndex = 0;              // index of the tile which is selected by default
        boolean foundSIndex = false; // true when 'sIndex' has been determined
        String liststring;
        String num, numx, numy;

        m_list.setEnabled(false);
        model.removeAllElements();
        if(map == null) {
            // mouse has been clicked outside the mapview
            getPanelArch(-1);
            m_list.setEnabled(true);
            m_view.refreshMapArchPanel();
            return;
        }

        post_select = -1;
        list_counter=0;
        ArchObject node=map.m_model.getMouseRightPosObject();

        // Jump to the end of the list
        for (; node != null && node.getNextArch() != null; node = node.getNextArch());

        // Now go through the list backwards and put all arches
        // on the panel in this order
        for(; node != null; node = node.getPrevArch()) {
            // add the node
            if(node.getMyID() == archid)
                post_select=list_counter;
            num = Integer.toString(node.getMyID());
            liststring = list_def.substring(0,10-num.length())+num;
            numx = Integer.toString(node.getMapX());
            liststring += list_def.substring(0,10-numx.length())+numx;
            numy = Integer.toString(node.getMapY());
            liststring += list_def.substring(0,10-numy.length())+numy;
            liststring += list_def;
            list_counter++;
            model.addElement(liststring);

            // if view-settings are applied, mark topmost "visible" tile for selection
            if (m_control.isTileEditSet() && !foundSIndex && m_control.isTileEdit(node.getEditType())) {
                sIndex = list_counter-1; // select this tile
                foundSIndex = true;      // this is it - don't select any other tile
            }

            addInvObjects(node, numx, numy,archid); // browse the inventory of the map object
        }

        if(post_select != -1) {
            m_list.setSelectedIndex(post_select);
        } else {
            // Per default, the topmost arch matching the view settings is selected.
            // (With no view settings applied, the very topmost arch is selected.)
            m_list.setSelectedIndex(sIndex);
        }

        // refresh the MapArchPanel to display the new arch
        m_view.refreshMapArchPanel();

        m_list.setEnabled(true);
    }

    /**
     * Add Inventory Objects to an arch in the MapTileList recursively.
     *
     * @param node      the arch where the inventory gets added
     * @param x         map location of 'node' as strings
     * @param y         map location of 'node' as strings
     * @param archid    node_nr of the highlighted arch (?)
     */
    public void addInvObjects(ArchObject node, String x, String y, int archid) {
        String liststring;
        String num;

        // if this is a multi-tile, we want to show the head's inventory
        ArchObject arch;
        if (node.getRefFlag() && node.getMapMultiHead() != null) {
            arch = node.getMapMultiHead().getStartInv(); // we go to heads inv.

            if (arch != null) {
                x = Integer.toString(arch.getMapX());
                y = Integer.toString(arch.getMapY());
            }
        } else
            arch = node.getStartInv();  // we go to our own inv. start

        for(;arch != null;) {
            if(arch.getMyID() == archid)
                post_select=list_counter;
            num = Integer.toString(arch.getMyID());
            liststring = list_def.substring(0,10-num.length())+num;
            liststring += list_def.substring(0,10-x.length())+x;
            liststring += list_def.substring(0,10-y.length())+y;
            liststring += list_def_inv;
            list_counter++;
            model.addElement(liststring);

            addInvObjects(arch, x, y, archid);
            arch = arch.getNextInv();               // get next of chain
        }
    }

    /**
     * Builds the cells in the map-tile panel.
     */
    class MyCellRenderer extends DefaultListCellRenderer {
        /* This is the only method defined by ListCellRenderer.  We just
         * reconfigure the Jlabel each time we're called.
         */
        public Component getListCellRendererComponent(
                                                      JList list,
                                                      Object value,   // value to display
                                                      int index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {

            /* The DefaultListCellRenderer class will take care of
             * the JLabels text property, it's foreground and background
             *colors, and so on.
             */
            super.getListCellRendererComponent(list, value, index, iss, chf);

            String entry = value.toString();
            int num = Integer.parseInt(entry.substring(0,10));
            int mapx = Integer.parseInt(entry.substring(10,20));
            int mapy = Integer.parseInt(entry.substring(20,30));
            int obj = Integer.parseInt(entry.substring(30,40));

            ArchObject arch = m_control.getMapArch(num, mapx, mapy);
            ArchObject tmp;
            //String label;

            // We must set a disabled Icon (even though we don't want it)
            // If unset, in JDK 1.4 swing tries to generate a greyed out version
            // of the lable-icon from the image-producer - causing a runtime error.
            if(IGUIConstants.isoView)
                setDisabledIcon(m_control.unknownTileIcon);
            else
                setDisabledIcon(m_control.unknownTileIconX);

            // arch==null should not happen, but it *can* happen when the active
            // window gets changed by user and java is still blitting here
            if(arch != null) {
                if(!iss && obj != 1)
                    this.setBackground(IGUIConstants.BG_COLOR);

                if (IGUIConstants.isoView && arch.isMulti() && arch.getMapMultiHead() != null)
                    arch = arch.getMapMultiHead();

                if(arch.getNodeNr()== -1) {
                    if(IGUIConstants.isoView)
                        setIcon(m_control.noarchTileIcon);
                    else
                        setIcon(m_control.noarchTileIconX);

                } else if(arch.getFaceObjectFlag() == true) {
                    if(IGUIConstants.isoView)
                        setIcon(m_control.nofaceTileIcon);
                    else
                        setIcon(m_control.nofaceTileIconX);

                } else if(arch.getObjectFaceNr() == -1) {
                    if(IGUIConstants.isoView)
                        setIcon(m_control.unknownTileIcon);
                    else
                        setIcon(m_control.unknownTileIconX);
                } else
                    setIcon(m_control.getFace(arch.getObjectFaceNr()));

                // In the map-tile-window the object names are displayed
                // next to the icons
                m_control.setPlainFont(this);
                if (arch.isMulti() && arch.getMapMultiHead() != null)
                    arch = arch.getMapMultiHead();

                if (arch.getObjName() != null && arch.getObjName().length()>0)
                    setText(arch.getObjName());       // special name
                else {
                    String defname = null;
                    if (arch.getNodeNr() != -1)
                        defname = m_control.getArch(arch.getNodeNr()).getObjName();

                    if (defname != null && defname.length()>0)
                        setText(defname);             // default name
                    else
                        setText(arch.getArchName());  // arch name
                }
            }

            return this;
        }
    }
}
