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
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog used to ask the user the properties for the new level.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class COptionDialog extends CDialogBase
{
    private CMainControl m_control;
    private JTextField m_archField;
    private JTextField m_mapField;
    private JTextField m_scriptField;
    private JCheckBox m_loadArches;     // load arches from collected archives?
    private JComboBox m_ImageSet;       // selection box for Image Set
    private JCheckBox m_mapPanelBottom; // is map-tile panel in bottom panel?

    /**
     * Constructs a new option dialog.
     *@param control The controller of this dialog.
     *@param parentFrame The parent frame of this dialog.
     *@param level The level whose properties are shown/edited.
     */
    COptionDialog( CMainControl control, Frame parentFrame) throws CGridderException
    {
        super( parentFrame, "Options" );
        m_control = control;
        getContentPane().setLayout( new BorderLayout() );

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS ) );
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // --- Resource Path ---
        JPanel optionPathPanel = new JPanel();
        optionPathPanel.setLayout(new BoxLayout(optionPathPanel, BoxLayout.Y_AXIS));
        optionPathPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Resource Path" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

            JPanel archPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT) );
            archPanel.add(new JLabel("Archfiles: "));
            m_archField = new JTextField(16);
            m_archField.setText(m_control.getArchDefaultFolder());
            archPanel.add(m_archField );
            optionPathPanel.add(archPanel);

            JPanel mapPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT) );
            mapPanel.add(new JLabel("Maps: "));
            m_mapField = new JTextField(16);
            m_mapField.setText(m_control.getMapDefaultFolder());
            mapPanel.add(m_mapField );
            optionPathPanel.add(mapPanel);

            JPanel scriptPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT) );
            scriptPanel.add(new JLabel("Scripts: "));
            m_scriptField = new JTextField(16);
            m_scriptField.setText(m_control.getScriptDefaultFolder());
            scriptPanel.add(m_scriptField);
            if (IGUIConstants.isoView) {
                optionPathPanel.add(scriptPanel);
            }

        mainPanel.add(optionPathPanel);

        // --- globals ---
        JPanel optionPartPanel = new JPanel( new GridLayout( 2, 1 ) );
        optionPartPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Globals" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

        JPanel cbox;
        if (m_control.imageSet != null)
            cbox = build_ImageSetBox(m_control.imageSet.equalsIgnoreCase("base"));
        else
            cbox = build_ImageSetBox(false);
        optionPartPanel.add(cbox);

        m_loadArches = new JCheckBox(" Load Arches from Collection");
        m_loadArches.setSelected(m_control.isArchLoadedFromCollection());
        m_loadArches.addActionListener(new selectArchLoadAL(m_loadArches, this));
        optionPartPanel.add(m_loadArches);
        if (m_control.isArchLoadedFromCollection()) m_archField.setEnabled(false);

        mainPanel.add(optionPartPanel);

        // --- layout settings ---
        JPanel optionLayoutPanel = new JPanel( new GridLayout( 1, 1 ) );
        optionLayoutPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Layout Settings" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

        m_mapPanelBottom = new JCheckBox(" Seperate Map-Tile Panel");
        m_mapPanelBottom.setSelected(!m_control.getMainView().isMapTileListBottom());
        optionLayoutPanel.add(m_mapPanelBottom);
        mainPanel.add(optionLayoutPanel);

        // --- button line ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(
            IGUIConstants.SPACE_BETWEEN_BUTTON_AREA_AND_MAIN_DIALOG,
            IGUIConstants.DIALOG_INSETS,
            IGUIConstants.DIALOG_INSETS,
            IGUIConstants.DIALOG_INSETS ) );
        buttonPanel.setLayout(new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
        buttonPanel.add( Box.createGlue() );

        /*
        JButton button = new JButton("Restore");
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
            }
        });
        buttonPanel.add(button);

        buttonPanel.add( Box.createRigidArea(
            new Dimension( IGUIConstants.SPACE_BETWEEN_BUTTON_GROUPS, 1 ) ) );
        */
        JButton button = new JButton("OK");
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                m_control.setGlobalSettings(m_archField.getText(),
                        m_mapField.getText(), m_scriptField.getText(),
                        (m_ImageSet.getSelectedIndex()==0)?false:true ,
                        m_loadArches.isSelected(), !m_mapPanelBottom.isSelected());
                dispose();
            }
        });
        buttonPanel.add(button);

        buttonPanel.add( Box.createRigidArea(
            new Dimension( IGUIConstants.SPACE_BETWEEN_BUTTONS ,1 ) ) );

        button = new JButton("Cancel");
        button.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                dispose();
            }
        });
        buttonPanel.add(button);

        getContentPane().add( mainPanel, BorderLayout.CENTER );
        getContentPane().add( buttonPanel, BorderLayout.SOUTH );
        pack();
        setVisible(true);
    }

    /**
     * Construct the Combo box for the selection of Image Sets
     */
    private JPanel build_ImageSetBox(boolean base_set) {
        JPanel lineLayout = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // layout for this line

        // list of available Image Sets:
        String []list = new String[2];
        list[0] = " Disabled";
        list[1] = " Base";

        lineLayout.add(new JLabel("Use Image Set: "));  // create label

        m_ImageSet = new JComboBox(list);     // set "content"
        m_ImageSet.setPreferredSize(new Dimension(150, 25));
        m_ImageSet.setSelectedIndex(base_set? 1 : 0); // set active selection

        m_ImageSet.setBackground(java.awt.Color.white);  // white background
        m_ImageSet.setName("Image Set");

        lineLayout.add(m_ImageSet);
        return lineLayout;
    }

    /**
     * Action-listener for the checkbox "Load Arches from Collection"
     * While it is selected, the arch path should be disabled
     */
    private class selectArchLoadAL implements ActionListener {
        COptionDialog frame; // the frame (options dialog window)
        JCheckBox cbox;      // input checkbox

        /**
         * Constructor
         * @param state    current state of checkbox
         */
        public selectArchLoadAL(JCheckBox cbox, COptionDialog frame) {
            this.cbox = cbox;
            this.frame = frame;
        }

        public void actionPerformed(ActionEvent event) {
            // state has changed
            if (cbox.isSelected()) {
                m_archField.setEnabled(false);
                frame.update(frame.getGraphics());
            }
            else {
                m_archField.setEnabled(true);
                frame.update(frame.getGraphics());
            }
        }
    }
}
