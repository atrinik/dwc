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
import java.util.*;

/**
 * Dialog used to ask the user the properties for the new level.
 * Contains a tabbed pane for creating a level either based on a template
 * or from a scratch.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CNewMapDialog extends CDialogBase {
    // map types:
    public static final int TYPE_CFMAP = 0;
    public static final int TYPE_PICKMAP = 1;

    /** The key value used to store the last used tile width to INI file. */
    public static final String DEFAULT_TILE_WIDTH_KEY =
        "NewLevelDialog.tileWidth";
    /** The key value used to store the last used tile height to INI file. */
    public static final String DEFAULT_TILE_HEIGHT_KEY =
        "NewLevelDialog.tileHeight";
    /** The key value used to store the last used level width to INI file. */
    public static final String DEFAULT_LEVEL_WIDTH_KEY =
        "NewLevelDialog.mapWidth";
    /** The key value used to store the last used level height to INI file. */
    public static final String DEFAULT_LEVEL_HEIGHT_KEY =
        "NewLevelDialog.mapHeight";

    /** The controller of this new level dialog view. */
    private CMainControl m_control;

    /** type of map to create: pickmap or normal map? */
    private int mapType;

    // Dialog UI Components
    private JTextField m_mapNameField;
    private JTextField m_fileNameField;
    private JTextField m_mapWidthField;
    private JTextField m_mapHeightField;
    private JTabbedPane m_tabbedPane;
    private JPanel m_newLevelFromScratchPanel;

    /**
     * Constructs a new level dialog. Builds the dialog UI.
     * @param control      the controller of this dialog.
     * @param parentFrame  the parent frame of this dialog.
     * @param filename     desired filename for new map, null if not specified
     */
    CNewMapDialog(CMainControl control, Frame parentFrame, String filename, int mapType)
        throws CGridderException {
        super(parentFrame, (mapType==TYPE_PICKMAP ? "Create New Pickap" : "Create New Map"));

        m_control = control;
        this.mapType = mapType;
        getContentPane().setLayout( new BorderLayout() );

        m_newLevelFromScratchPanel = new JPanel();
        m_newLevelFromScratchPanel.setLayout(new BoxLayout(m_newLevelFromScratchPanel, BoxLayout.Y_AXIS));

        m_newLevelFromScratchPanel.setBorder(new EmptyBorder(
                                                             IGUIConstants.DIALOG_INSETS,
                                                             IGUIConstants.DIALOG_INSETS,
                                                             IGUIConstants.DIALOG_INSETS,
                                                             IGUIConstants.DIALOG_INSETS ) );
        // file name panel
        JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (mapType==TYPE_PICKMAP)
            fileNamePanel.add(new JLabel("Pickmap Name:"));
        else
            fileNamePanel.add(new JLabel("File Name:"));
        if (filename != null && filename.length() > 0)
            m_fileNameField = new JTextField(filename, 16);
        else
            m_fileNameField = new JTextField(16);
        fileNamePanel.add(m_fileNameField);
        if (mapType==TYPE_PICKMAP)
        {
          m_newLevelFromScratchPanel.add(fileNamePanel);
          m_newLevelFromScratchPanel.add(Box.createVerticalStrut(5));
        }

        if (mapType == TYPE_CFMAP) {
            // map name panel
            JPanel mapNamePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            mapNamePanel.add(new JLabel("Map Name:"));
            m_mapNameField = new JTextField(16);
            mapNamePanel.add(m_mapNameField);
            m_newLevelFromScratchPanel.add(mapNamePanel);
            m_newLevelFromScratchPanel.add(Box.createVerticalStrut(5));
        }

        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);

        JPanel mapSizePanel = new JPanel(new GridLayout(2,2));
        mapSizePanel.setBorder( new CompoundBorder(new TitledBorder( new EtchedBorder(), (mapType==TYPE_PICKMAP ? "Pickmap Size" : "Map Size") ),
                                                   new EmptyBorder(
                                                                   IGUIConstants.DIALOG_INSETS,
                                                                   IGUIConstants.DIALOG_INSETS,
                                                                   IGUIConstants.DIALOG_INSETS,
                                                                   IGUIConstants.DIALOG_INSETS ) ) );
        mapSizePanel.add( new JLabel("Width:") );
        m_mapWidthField = new JTextField(3);
        if (mapType == TYPE_PICKMAP)
            m_mapWidthField.setText(String.valueOf(IGUIConstants.DEF_PICKMAP_WIDTH));
        else
            m_mapWidthField.setText(settings.getProperty(DEFAULT_LEVEL_WIDTH_KEY, ""+IGUIConstants.DEF_MAPSIZE ) );
        mapSizePanel.add( m_mapWidthField );
        mapSizePanel.add( new JLabel("Height:") );
        m_mapHeightField = new JTextField(3);
        if (mapType == TYPE_PICKMAP)
            m_mapHeightField.setText(String.valueOf(IGUIConstants.DEF_PICKMAP_HEIGHT));
        else
            m_mapHeightField.setText(settings.getProperty(DEFAULT_LEVEL_HEIGHT_KEY, ""+IGUIConstants.DEF_MAPSIZE ) );
        mapSizePanel.add( m_mapHeightField );
        m_newLevelFromScratchPanel.add(mapSizePanel);
        m_newLevelFromScratchPanel.add(Box.createVerticalStrut(5));

        getContentPane().add(m_newLevelFromScratchPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(IGUIConstants.SPACE_BETWEEN_BUTTON_AREA_AND_MAIN_DIALOG,
                                              IGUIConstants.DIALOG_INSETS,
                                              IGUIConstants.DIALOG_INSETS,
                                              IGUIConstants.DIALOG_INSETS ) );
        buttonPanel.setLayout(new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
        buttonPanel.add( Box.createGlue() );

        JButton button = new JButton("OK");
        button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if ( createNewLevel() ) {
                        dispose();
                    }
                }
            });
        buttonPanel.add( button );

        buttonPanel.add( Box.createRigidArea(new Dimension( IGUIConstants.SPACE_BETWEEN_BUTTONS ,1 ) ) );

        button = new JButton("Cancel");
        button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    dispose();
                }
            });
        buttonPanel.add( button );

        getContentPane().add( buttonPanel, BorderLayout.SOUTH );

        pack();
        setVisible(true);
    }

    /**
     * Checks the given values and creates a new level.
     *@return True if the level was created, false if the parameters were wrong.
     */
    private boolean createNewLevel() {

        if ( true ) {
            // Create a new level from scratch
            MapArchObject maparch = new MapArchObject();
            maparch.setWidth(IGUIConstants.DEF_MAPSIZE);
            maparch.setHeight(IGUIConstants.DEF_MAPSIZE);

            // Get and validate the level size
            try {
                // get size
                maparch.setWidth(Integer.parseInt(m_mapWidthField.getText()));
                maparch.setHeight(Integer.parseInt(m_mapHeightField.getText()));

                if (( maparch.getWidth() < 1 ) || ( maparch.getHeight() < 1 )) {
                    m_control.showMessage("Illegal Value",
                                          "Map dimensions must be in range [1,"+
                                          Integer.MAX_VALUE+"]." );
                    return false;
                }
            } catch(NumberFormatException illegalNumbers) {
                m_control.showMessage(
                                      "Illegal Value",
                                      "Map dimensions must be numerical integer values!" );
                return false;
            }

            // the mapmaker must enter a mapname to create a new map
            if (mapType == TYPE_CFMAP && (m_mapNameField.getText() == null || m_mapNameField.getText().length() <= 0)) {
                m_control.showMessage("Map Name is Missing",
                                      "You must enter a map name! This name will appear\n"+
                                      "in the game, so it should be a descriptive name.\n"+
                                      "Map names don't need to be unique.");
                return false;
            }

            // arches must be loaded to create a new map
            if (ArchObjectStack.getLoadStatus() != ArchObjectStack.IS_COMPLETE) {
                m_control.showMessage("Cannot Create Map",
                                      "All arches have to be loaded before you can open a map.\n"+
                                      "Just be patient and wait a few seconds...");
                return false;
            }

            if (mapType == TYPE_CFMAP) {
                CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
                settings.setProperty( DEFAULT_LEVEL_WIDTH_KEY,  ""+maparch.getWidth() );
                settings.setProperty( DEFAULT_LEVEL_HEIGHT_KEY, ""+maparch.getHeight() );
            }

            // set file name
            String strfileName = m_fileNameField.getText();
            if (strfileName.length() < 1) {
                strfileName = IGUIConstants.DEF_MAPFNAME;
            }
            maparch.setFileName(strfileName);

            // set map name
            if (mapType == TYPE_CFMAP)
                maparch.setMapName(m_mapNameField.getText());
            else if (mapType == TYPE_PICKMAP)
                maparch.setMapName("pickmap");

            // default map text:
            Calendar today = Calendar.getInstance(); // get current date
            if (!IGUIConstants.isoView)
                maparch.addText("Creator: CF Java Map Editor\n");
            else
                maparch.addText("Creator: Atrinik Map Editor\n");
            maparch.addText("Date:    "+(today.get(Calendar.MONTH)+1)+"/"+
                            today.get(Calendar.DAY_OF_MONTH)+"/"+today.get(Calendar.YEAR));

            if (mapType == TYPE_CFMAP)
                m_control.newLevel(null, maparch);
            else if (mapType == TYPE_PICKMAP) {
                return CPickmapPanel.getInstance().addNewPickmap(maparch);
            }
        }

        return true;
    }

}
