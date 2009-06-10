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
import java.awt.*;

/**
 * The <code>CArchQuickView</code> holds the tile palette.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class CArchQuickView extends JPanel {
    /** Controller of this subview. */
    private CMainControl m_control;
    private JLabel archArchNameText = new JLabel();
    private JLabel archObjNameText = new JLabel();
    private JLabel archTypeText = new JLabel();
    private JLabel archTileText = new JLabel();

    public CArchQuickView( CMainControl control ) {
        m_control = control;
        setLayout( new BorderLayout(1,1) );

        // set font
        m_control.setPlainFont(archArchNameText);
        m_control.setPlainFont(archObjNameText);
        m_control.setPlainFont(archTypeText);
        m_control.setPlainFont(archTileText);

        // setup a panel
        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0,0,0,0);
        c.anchor = GridBagConstraints.WEST;
        // add our elements
        archObjNameText.setText("<html><font color=black>Name:</font></html>");
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(archObjNameText, c);
        panel.add(archObjNameText);

        archArchNameText.setText("<html><font color=black>Arch:</font></html>");
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(archArchNameText, c);
        panel.add(archArchNameText);

        archTypeText.setText("<html><font color=black>Type:</font></html>");
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(archTypeText, c);
        panel.add(archTypeText);

        archTileText.setText("<html><font color=black>Tile:</font></html>");
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(archTileText, c);
        panel.add(archTileText);

        // put it in a scroller
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setMinimumSize(new Dimension(1,1));
        add(scrollPane); // and add it to this panel object
    }

    // show quick info of this arch
    void showArchPanelQuickObject(ArchObject arch) {
        if(arch == null) {
          if(m_control.getMainView().isPickmapActive())
            archObjNameText.setText("<html><font color=black>Name: <font color=blue>-random pick-</font></font></html>");
          else
            archObjNameText.setText("<html><font color=black>Name:</font></html>");
          archArchNameText.setText("<html><font color=black>Arch:</font></html>");
          archTypeText.setText("<html><font color=black>Type:</font></html>");
          archTileText.setText("<html><font color=black>Tile:</font></html>");
          return;
        }
        archArchNameText.setText("<html><font color=black>Arch: "+arch.getArchName()+"</font></html>");
        if(arch.getObjName() == null || arch.getObjName().length() <= 0) {
            ArchObject def = arch.getDefaultArch();
            if (def == null || def == arch)
                archObjNameText.setText("<html><font color=black>Name: -none- </font></html>");
            else if (def.getObjName() != null && def.getObjName().length()>0)
                archObjNameText.setText("<html><font color=black>Name: "+def.getObjName()+"</font></html>");
            else if (def.getArchName() != null && def.getArchName().length()>0)
                archObjNameText.setText("<html><font color=black>Name: "+def.getArchName()+"</font></html>");
            else
                archObjNameText.setText("<html><font color=black>Name: -none- </font></html>");
        }
        else
            archObjNameText.setText("<html><font color=black>Name: "+arch.getObjName()+"</font></html>");

        archTypeText.setText("<html><font color=black>Type: "+
                             m_control.archObjectParser.getArchTypeName(arch.getArchTypNr())
                             +" ("+arch.getArchTypNr()+") </font></html>");

        if(arch.getRefCount() >0) {
            archTileText.setText("<html><font color=black>Tile: </font><font color=green> multi</font><font color=black> ("+
                                 +(arch.getRefCount()+1)+" parts) ("+(arch.getRefMaxX()-arch.getRefMaxMX()+1)+","+(arch.getRefMaxY()-arch.getRefMaxMY()+1)
                                 +")</font></html>");
        } else {
            archTileText.setText("<html><font color=black>Tile: single"
                                 +"</font></html>");
        }

        // notify ReplaceDialog
        if (ReplaceDialog.isBuilt() && ReplaceDialog.getInstance().isShowing())
            ReplaceDialog.getInstance().updateArchSelection(arch, false);
    }

    void refresh() {
        m_control.showArchPanelQuickObject(m_control.getArchPanelSelection());
        repaint();
    }
}
