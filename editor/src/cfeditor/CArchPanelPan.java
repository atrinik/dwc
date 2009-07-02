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

public class CArchPanelPan extends JPanel {
    /** Controller of this subview. */
    private CMainControl m_control;

    private JList m_list;
    private DefaultListModel model;
    private JPanel m_panelDesktop;
    private JComboBox jbox;
    private StringBuffer list;
    private int listcounter;
    private int combo_counter;
    private CArchPanel m_panel;
    /* Build Panel */
    CArchPanelPan( CArchPanel control_panel, CMainControl control) {
        m_control = control;
        m_panel = control_panel;
        list = new StringBuffer("");
        listcounter=0;
        combo_counter=0;
        setLayout( new BorderLayout() );

        m_panelDesktop= new JPanel();
        m_panelDesktop.setLayout( new BorderLayout() );

        jbox = new JComboBox();
        m_control.setBoldFont(jbox);

        model = new DefaultListModel();
        m_list = new JList(model);
        m_list.setCellRenderer(new MyCellRenderer());
        m_list.setBackground(IGUIConstants.BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(m_list);
        m_panelDesktop.add(scrollPane,BorderLayout.CENTER);
        m_panelDesktop.add(jbox,BorderLayout.NORTH);
        scrollPane.setAutoscrolls(true);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

        jbox.setAutoscrolls(true);

        jbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showArchList();
            }
        });

        m_list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showArchListObject(e.getFirstIndex());
            }
        });

        // listening for mouse-click events in the ArchPanel list
        m_list.addMouseListener( new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                // In JDK 1.4 it is required to transfer focus back to mapview after
                // every click in the panel, otherwise the menu-shortcuts stop working
                if (m_control.m_currentMap != null &&
                    m_control.m_currentMap.m_view != null) {
		    m_control.m_currentMap.m_view.requestFocus(); // focus to mapview
                }
            }
        });

    }

    public ArchObject getArchListObject() {
        ArchObject arch = null;
        int index = m_list.getSelectedIndex();

        if(index != -1) {
            index = Integer.parseInt(m_list.getSelectedValue().toString());
            arch = m_control.getArchObjectStack().getArch(index);
        }
        return(arch);
    }


    public void showArchListObject(int index) {
        ArchObject arch = null;

        if(index != -1) {
            try {
                index = Integer.parseInt(m_list.getSelectedValue().toString());
                arch = m_control.getArchObjectStack().getArch(index);
            } catch (NullPointerException e) {
                /*
                System.out.println("NullPointerException in showArchListObject()!");
                This happens in JDK 1.4 when you select an arch in panel A,
                then select Panel B, then Panel A again. (why??)
                */
            } catch (NumberFormatException e) {}
        }
        m_control.showArchPanelQuickObject(arch);       // send it to quick view
    }


    // add this arch to list of (this) Jlist list
    public void addArchPanelArch(int archnr, int index) {
        String def = "00000";
        String num = Integer.toString(archnr);

        this.list.append(def.substring(0,5-num.length()));
        this.list.append(num);

        num = Integer.toString(index);
        this.list.append(def.substring(0,5-num.length()));
        this.list.append(num);

        this.listcounter++;
    }

    int addArchPanelCombo(String name) {
        this.setEnabled(false);
        this.jbox.addItem(name);
        this.setEnabled(true);
        return(combo_counter++);
    }

    JPanel getPanel() {
        return (m_panelDesktop);
    }

    JList getList() {
        return (m_list);
    }

    DefaultListModel getModel() {
        return (model);
    }

    /**
     * this is only needed when arche collection is run, so we want
     * to know to which cathegorys the arches belong to:
     * @return an array of nodenumbers from all arches in this panel
     */
    public int[] getListNodeNrArray() {
        int[] numList = new int[(int)(list.length()/10.)];

        for (int i=0; i<(int)(list.length()/10.); i++) {
            numList[i] = Integer.parseInt(list.substring(0+10*i, 5+10*i));
            //System.out.println(CMainControl.getInstance().getArchObjectStack().getArch(numList[i]).getArchName());
        }
        return numList;
    }

    /**
     * this is only needed when arche collection is run, so we want
     * to know to which cathegorys the arches belong to:
     * @return an array of the cathegorys of all arches in this panel<br>
     *         note that the same indices are used for same arches in 'getListNodeNrArray()'
     */
    public String[] getListCathegoryArray() {
        String[] cathList = new String[(int)(list.length()/10.)];

        int index;
        for (int i=0; i<(int)(list.length()/10.); i++) {
            try {
                index = Integer.parseInt(list.substring(5+10*i, 10+10*i));
                cathList[i] = jbox.getItemAt(index).toString().trim();
            }
            catch (NullPointerException e) {
                System.out.println("Nullpointer in getListCathegoryArray()!");
            }
        }
        return cathList;
    }

    void refresh() {
        repaint();
    }

    void showArchList() {
        int offset=0;
        int index = this.jbox.getSelectedIndex();

        this.model.removeAllElements();

        for(int i=0;i<this.listcounter;i++) {
            if(index >=0) {
                if(index == 0) {
                                //                                      this.model.addElement(this.list.substring(offset,offset+5) + "  I:"+this.list.substring(offset+5,offset+10) );
                    this.model.addElement(this.list.substring(offset,offset+5));
                } else {
                    if(index == Integer.parseInt(this.list.substring(offset+5,offset+10)))
                        this.model.addElement(this.list.substring(offset,offset+5));
                }
            }
            offset+=10;
        }

    }

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


            /* We additionally set the JLabels icon property here.
             */


            ArchObject arch = m_control.getArchObjectStack().getArch(
                                                                     Integer.parseInt(value.toString()));
            if(iss) {
                m_panel.selectedArch = Integer.parseInt(value.toString());                              m_control.SetStatusText(" "+value.toString()+" ");
            }
            m_control.setPlainFont(this);
            setText(arch.getArchName());

            if(arch.getFaceObjectFlag() == true) {
                if(IGUIConstants.isoView)
                    setIcon(m_control.nofaceTileIcon);
                else
                    setIcon(m_control.nofaceTileIconX);
            } else

            if(arch.getObjectFaceNr() == -1) {
                if(IGUIConstants.isoView)
                    setIcon(m_control.unknownTileIcon);
                else
                    setIcon(m_control.unknownTileIconX);
            }
            else
                setIcon(m_control.getFace(arch.getRealFaceNr()));


            return this;
        }
    }
}

