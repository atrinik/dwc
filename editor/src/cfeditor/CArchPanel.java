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

/**
 * The <code>CArchPanel</code> holds the tile palette.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CArchPanel extends JPanel {
    private static final String ARCHPANEL_LOCATION_KEY = "MainWindowArchPanel.dividerLocation";
    /** Controller of this subview. */
    private CMainControl m_control;
    /** The "Import..." button. */

    int selectedArch;
    private JTabbedPane m_archAndPickPane; // panel holding both archlist and pickmaps
    private CSplitPane m_splitPane; // our split pane
    private JTabbedPane m_tabDesktop; // the tab panel with arch lists
    private CArchQuickView archQuickPanel; // data/view of selected objects in tab panel
    static private panelNode panelNodeStart; // list of arch panels
    private panelNode panelNodeLast;
    public CArchPanelPan m_selectedPanel; // the active panel

    /* Build Panel */
    public CArchPanel( CMainControl control ) {
        m_control = control;
        panelNodeStart = null;
        panelNodeLast = null;
        m_selectedPanel=null;
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        selectedArch = -1;

        setLayout(new BorderLayout());
        m_tabDesktop = new JTabbedPane(JTabbedPane.TOP);
        m_tabDesktop.setBorder(BorderFactory.createEmptyBorder(IGUIConstants.SPACE_PICKARCH_TOP, 0, 0, 0));
        m_control.setBoldFont(m_tabDesktop);
        archQuickPanel = new CArchQuickView(control);

        // m_archAndPickPane is the panel containing both archpanel and pickmaps
        m_archAndPickPane = new JTabbedPane(JTabbedPane.TOP);
        m_control.setBoldFont(m_archAndPickPane);
        m_archAndPickPane.addTab(" Arch List ", m_tabDesktop);
        m_archAndPickPane.addTab(" Pickmaps ", m_control.getMainView().getPickmapPanel());
        // this listener informs the mainview which panel is active: archlist or pickmaps?
        CPickmapPanel.getInstance().addArchNPickChangeListener(m_archAndPickPane);

        m_splitPane = new CSplitPane(CSplitPane.VERTICAL_SPLIT,
                                     m_archAndPickPane,
                                     archQuickPanel);

        // calculate default value in case there is no settings file
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int divLocation = Integer.parseInt(settings.getProperty(ARCHPANEL_LOCATION_KEY, ""+(int)(0.77*0.9*screen.getHeight())));

        m_splitPane.setDividerLocation(divLocation);
        m_splitPane.setDividerSize(5);
        add(m_splitPane, BorderLayout.CENTER);

        // we must set the list of the selected list depend on combo selection
        m_tabDesktop.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane tp = (JTabbedPane)e.getSource();

                selectedArch=-1;
                panelNode node = panelNodeStart;
                for(int i=0;i<tp.getSelectedIndex();i++)
                    node=node.next;
                m_selectedPanel=node.data;
                if(m_selectedPanel != null)
                    m_selectedPanel.showArchList();
            }
        });
    }

    static public panelNode getStartPanelNode() {
        return panelNodeStart;
    }

    /**
     * Move the pickmap panel in front of the default-archpanel
     */
    public void movePickmapPanelToFront() {
        if (m_archAndPickPane != null && m_archAndPickPane.getTabCount() > 1)
            m_archAndPickPane.setSelectedIndex(1);
    }

    public ArchObject getArchPanelSelection() {
        if(m_selectedPanel == null)
            return null;
        return(m_selectedPanel.getArchListObject());

    }

    public void showArchPanelQuickObject(ArchObject arch) {
        archQuickPanel.showArchPanelQuickObject(arch);
    }

    public int addArchPanelCombo(String name) {
        return(m_selectedPanel.addArchPanelCombo(name));
    }

    public void addArchPanelArch(int archnr, int index) {
        m_selectedPanel.addArchPanelArch(archnr, index);
    }

    public void disableTabPane() {
        m_tabDesktop.setEnabled(false);
    }

    public void enableTabPane() {
        m_tabDesktop.setEnabled(true);
        if(m_selectedPanel != null)
            m_selectedPanel.showArchList();
    }

    public void addPanel(String name) {
        panelNode newnode = new panelNode(new CArchPanelPan(this, m_control), name);

        // chain it to our list of panels
        if(panelNodeStart == null)
            panelNodeStart= newnode;
        if(panelNodeLast != null)
            panelNodeLast.next = newnode;
        panelNodeLast = newnode;
        this.m_tabDesktop.add(newnode.data.getPanel(),name);
        // make it aktive
        this.m_tabDesktop.setSelectedIndex(m_tabDesktop.getTabCount()-1);
        m_selectedPanel = newnode.data;

    }

    void appExitNotify() {
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        settings.setProperty( ARCHPANEL_LOCATION_KEY, ""+
                              m_splitPane.getDividerLocation() );
    }


    public int getPanelArch() {
        return(selectedArch);
    }

    void refresh() {
        archQuickPanel.refresh();       // why is this not working??? Look in CArchQuickView...
        repaint();
    }

    public void updateFont() {
        m_control.setBoldFont(m_tabDesktop);
        refresh();
    }

    public class panelNode {
        CArchPanelPan data;
        panelNode next;  // next node
        String title;    // title of this panelNode

        public  panelNode(CArchPanelPan data, String title) {
            this.data = data;
            this.title = title;
            this.next = null;
        }

        public String getTitle() {
            return title;
        }
    } // End of class stackNode

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
