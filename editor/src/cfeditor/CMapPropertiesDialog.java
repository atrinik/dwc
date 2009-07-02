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

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog used to ask the user the properties for the new level.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class CMapPropertiesDialog extends CDialogBase {
    public static final String CENTER_MAP_KEY = "MapProperties.centerMapOnResize";
    private CMainControl m_control;
    private CMapControl m_level;

    // input components, see MapArchObject for expl. of purpose
    private JTextArea m_textArea;                             // the msg text/arch text
    private JTextArea m_loreArea;                             // the lore text
    private JTextField m_nameField        = new JTextField(); // name of arch
	private JTextField m_bgmusicField     = new JTextField(); // background music
    private JTextField m_levelWidthField  = new JTextField(); // len x
    private JTextField m_levelHeightField = new JTextField(); // len y
    private JCheckBox  m_unique           = new JCheckBox();  // map unique
    private JCheckBox  m_outdoor          = new JCheckBox();  // map outdoor

    private JTextField m_enterX           = new JTextField(); // enter x
    private JTextField m_enterY           = new JTextField(); // enter y
    private JTextField m_swapTime         = new JTextField(); // swap time
    private JTextField m_resetTimeout     = new JTextField(); // reset timeout
    private JTextField m_difficulty       = new JTextField(); // map difficulty
    private JTextField m_darkness         = new JTextField(); // darkness
    private JCheckBox  m_fixedReset       = new JCheckBox();  // fixed reset

    private JTextField[] m_tilePath       = new JTextField[8]; // tile paths

    // only for CF:
    private JTextField m_temp             = new JTextField(); // temperature
    private JTextField m_pressure         = new JTextField(); // pressure
    private JTextField m_humid            = new JTextField(); // humidity
    private JTextField m_windspeed        = new JTextField(); // wind speed
    private JTextField m_winddir          = new JTextField(); // wind direction
    private JTextField m_sky              = new JTextField(); // sky setting

    // only for ISO:
    private JCheckBox m_no_save          = new JCheckBox();
    private JCheckBox m_no_magic          = new JCheckBox();
    private JCheckBox m_no_priest         = new JCheckBox();
    private JCheckBox m_no_summon         = new JCheckBox();
    private JCheckBox m_no_harm           = new JCheckBox();
    private JCheckBox m_fixed_login       = new JCheckBox();
    private JCheckBox m_perm_death        = new JCheckBox();
    private JCheckBox m_ultra_death       = new JCheckBox();
    private JCheckBox m_ultimate_death    = new JCheckBox();
    private JCheckBox m_pvp               = new JCheckBox();

    private String[] tile_link_name       = new String[8];
    private int[] rev_link       = new int[8];
    private int[][][] tile_link       = new int[8][2][2];

    /**
     * Constructs the map-options dialog.
     *
     * @param control      The controller of this dialog.
     * @param parentFrame  The parent frame of this dialog.
     * @param level        The level whose properties are shown/edited.
     * @throws CGridderException if something goes wrong (not implemented yet)
     */
    CMapPropertiesDialog(CMainControl control, Frame parentFrame,
                         CMapControl level) throws CGridderException {
        // set title
        super( parentFrame, ""+level.getMapFileName()+" - Map Properties" );
        MapArchObject map = level.m_model.m_mapArch;  // map arch object

        m_control = control;   // main control
        m_level   = level;     // map control

        getContentPane().setLayout(new BorderLayout());

        // main panel (gridbag)
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel mainPanel = new JPanel(gridbag);
            //mainPanel.setLayout( new CardLayout() );
            mainPanel.setBorder(new EmptyBorder(
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS,
                IGUIConstants.DIALOG_INSETS ) );

            // 1. map panel:
            JPanel mapPanel = new JPanel( new GridLayout( 7, 1 ) );
            mapPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Map" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

            createPanelLine(mapPanel, m_nameField, 16, m_level.getMapName(), "Name:   ");
			createPanelLine(mapPanel, m_bgmusicField, 16, m_level.getBackgroundMusic(), "Background Music:   ");
            createPanelLine(mapPanel, m_levelWidthField, 10,
                            String.valueOf(m_level.getMapWidth()), "Width:   ");
            createPanelLine(mapPanel, m_levelHeightField, 10,
                            String.valueOf(m_level.getMapHeight()), "Height:   ");
            createPanelCBox(mapPanel, m_unique, map.isUnique(), " Unique Map");
            createPanelCBox(mapPanel, m_outdoor, map.isOutdoor(), " Outdoor Map");
            createPanelCBox(mapPanel, m_fixedReset, map.isFixedReset(), " Fixed Reset");

            // set constraints
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0; c.weighty = 1.0;
            gridbag.setConstraints(mapPanel, c);
            mainPanel.add(mapPanel);

            // 2. options panel:
            JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
            tabPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 0));

            JPanel optionPanel = new JPanel(new GridLayout(1, 1));
            if (IGUIConstants.isoView) {
                optionPanel.setBorder( new CompoundBorder(
                    new TitledBorder( new EtchedBorder(), "Options" ),
                    new EmptyBorder(
                        IGUIConstants.DIALOG_INSETS,
                        IGUIConstants.DIALOG_INSETS,
                        IGUIConstants.DIALOG_INSETS,
                        IGUIConstants.DIALOG_INSETS ) ) );
            }
            JPanel optionPanel2 = new JPanel();
            optionPanel2.setLayout(new BoxLayout(optionPanel2, BoxLayout.Y_AXIS));
            JScrollPane scrollPane2 = new JScrollPane(optionPanel2);
            scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane2.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

            createPanelLine(optionPanel2, m_enterX, 10,
                            String.valueOf(map.getEnterX()), "Enter X:   ");
            createPanelLine(optionPanel2, m_enterY, 10,
                            String.valueOf(map.getEnterY()), "Enter Y:   ");
            createPanelLine(optionPanel2, m_difficulty, 10,
                            String.valueOf(map.getDifficulty()), "Difficulty:   ");
            createPanelLine(optionPanel2, m_darkness, 10,
                            String.valueOf(map.getDarkness()), "Darkness:   ");
            createPanelLine(optionPanel2, m_swapTime, 10,
                            String.valueOf(map.getSwapTime()), "Swap Time:   ");
            createPanelLine(optionPanel2, m_resetTimeout, 10,
                            String.valueOf(map.getResetTimeout()), "Reset Timeout:   ");

            if (!IGUIConstants.isoView) {
                createPanelLine(optionPanel2, m_temp, 10,
                                String.valueOf(map.getTemp()), "Temperature:   ");
                createPanelLine(optionPanel2, m_pressure, 10,
                                String.valueOf(map.getPressure()), "Pressure:   ");
                createPanelLine(optionPanel2, m_humid, 10,
                                String.valueOf(map.getHumid()), "Humidity:   ");
                createPanelLine(optionPanel2, m_windspeed, 10,
                                String.valueOf(map.getWindspeed()), "Wind Speed:   ");
                createPanelLine(optionPanel2, m_winddir, 10,
                                String.valueOf(map.getWinddir()), "Wind Direction:   ");
                createPanelLine(optionPanel2, m_sky, 10,
                                String.valueOf(map.getSky()), "Sky Setting:   ");
            }
            else {
              createPanelCBox(optionPanel2, m_no_save, map.isNoSave(), " No Save");
              createPanelCBox(optionPanel2, m_no_magic, map.isNoMagic(), " No Magic");
                createPanelCBox(optionPanel2, m_no_priest, map.isNoPriest(), " No Prayers");
                createPanelCBox(optionPanel2, m_no_harm, map.isNoHarm(), " No Harmful Spells");
                createPanelCBox(optionPanel2, m_no_summon, map.isNoSummon(), " No Summoning");
                createPanelCBox(optionPanel2, m_fixed_login, map.isFixedLogin(), " Fixed Login");
                createPanelCBox(optionPanel2, m_perm_death, map.isPermDeath(), " Permanent Death");
                createPanelCBox(optionPanel2, m_ultra_death, map.isUltraDeath(), " Ultra Death");
                createPanelCBox(optionPanel2, m_ultimate_death, map.isUltimateDeath(), " Instant Death");
                createPanelCBox(optionPanel2, m_pvp, map.isPvp(), " PvP Enabled");
            }

            optionPanel.add(scrollPane2);

            /* path names */
            tile_link_name[0] = "North";
            tile_link_name[1] = "East";
            tile_link_name[2] = "South";
            tile_link_name[3] = "West";
            tile_link_name[4] = "Northeast";
            tile_link_name[5] = "Southeast";
            tile_link_name[6] = "Southwest";
            tile_link_name[7] = "Northwest";

            /* reverse link list */
            rev_link[0] = IGUIConstants.TILE_PATH_SOUTH;
            rev_link[1] = IGUIConstants.TILE_PATH_WEST;
            rev_link[2] = IGUIConstants.TILE_PATH_NORTH;
            rev_link[3] = IGUIConstants.TILE_PATH_EAST;
            rev_link[4] = IGUIConstants.TILE_PATH_SOUTHWEST;
            rev_link[5] = IGUIConstants.TILE_PATH_NORTHWEST;
            rev_link[6] = IGUIConstants.TILE_PATH_NORTHEAST;
            rev_link[7] = IGUIConstants.TILE_PATH_SOUTHEAST;

            /* fine list of indirect links von maps around to other maps around us */
            tile_link[IGUIConstants.TILE_PATH_NORTH][0][0] = IGUIConstants.TILE_PATH_NORTHWEST;
            tile_link[IGUIConstants.TILE_PATH_NORTH][0][1] = IGUIConstants.TILE_PATH_WEST;
            tile_link[IGUIConstants.TILE_PATH_NORTH][1][0] = IGUIConstants.TILE_PATH_NORTHEAST;
            tile_link[IGUIConstants.TILE_PATH_NORTH][1][1] = IGUIConstants.TILE_PATH_EAST;

            tile_link[IGUIConstants.TILE_PATH_NORTHEAST][0][0] = IGUIConstants.TILE_PATH_NORTH;
            tile_link[IGUIConstants.TILE_PATH_NORTHEAST][0][1] = IGUIConstants.TILE_PATH_WEST;
            tile_link[IGUIConstants.TILE_PATH_NORTHEAST][1][0] = IGUIConstants.TILE_PATH_EAST;
            tile_link[IGUIConstants.TILE_PATH_NORTHEAST][1][1] = IGUIConstants.TILE_PATH_SOUTH;

            tile_link[IGUIConstants.TILE_PATH_EAST][0][0] = IGUIConstants.TILE_PATH_NORTHEAST;
            tile_link[IGUIConstants.TILE_PATH_EAST][0][1] = IGUIConstants.TILE_PATH_NORTH;
            tile_link[IGUIConstants.TILE_PATH_EAST][1][0] = IGUIConstants.TILE_PATH_SOUTHEAST;
            tile_link[IGUIConstants.TILE_PATH_EAST][1][1] = IGUIConstants.TILE_PATH_SOUTH;

            tile_link[IGUIConstants.TILE_PATH_SOUTHEAST][0][0] = IGUIConstants.TILE_PATH_EAST;
            tile_link[IGUIConstants.TILE_PATH_SOUTHEAST][0][1] = IGUIConstants.TILE_PATH_NORTH;
            tile_link[IGUIConstants.TILE_PATH_SOUTHEAST][1][0] = IGUIConstants.TILE_PATH_SOUTH;
            tile_link[IGUIConstants.TILE_PATH_SOUTHEAST][1][1] = IGUIConstants.TILE_PATH_WEST;

            tile_link[IGUIConstants.TILE_PATH_SOUTH][0][0] = IGUIConstants.TILE_PATH_SOUTHEAST;
            tile_link[IGUIConstants.TILE_PATH_SOUTH][0][1] = IGUIConstants.TILE_PATH_EAST;
            tile_link[IGUIConstants.TILE_PATH_SOUTH][1][0] = IGUIConstants.TILE_PATH_SOUTHWEST;
            tile_link[IGUIConstants.TILE_PATH_SOUTH][1][1] = IGUIConstants.TILE_PATH_WEST;

            tile_link[IGUIConstants.TILE_PATH_SOUTHWEST][0][0] = IGUIConstants.TILE_PATH_SOUTH;
            tile_link[IGUIConstants.TILE_PATH_SOUTHWEST][0][1] = IGUIConstants.TILE_PATH_EAST;
            tile_link[IGUIConstants.TILE_PATH_SOUTHWEST][1][0] = IGUIConstants.TILE_PATH_WEST;
            tile_link[IGUIConstants.TILE_PATH_SOUTHWEST][1][1] = IGUIConstants.TILE_PATH_NORTH;

            tile_link[IGUIConstants.TILE_PATH_WEST][0][0] = IGUIConstants.TILE_PATH_SOUTHWEST;
            tile_link[IGUIConstants.TILE_PATH_WEST][0][1] = IGUIConstants.TILE_PATH_SOUTH;
            tile_link[IGUIConstants.TILE_PATH_WEST][1][0] = IGUIConstants.TILE_PATH_NORTHWEST;
            tile_link[IGUIConstants.TILE_PATH_WEST][1][1] = IGUIConstants.TILE_PATH_NORTH;

            tile_link[IGUIConstants.TILE_PATH_NORTHWEST][0][0] = IGUIConstants.TILE_PATH_WEST;
            tile_link[IGUIConstants.TILE_PATH_NORTHWEST][0][1] = IGUIConstants.TILE_PATH_SOUTH;
            tile_link[IGUIConstants.TILE_PATH_NORTHWEST][1][0] = IGUIConstants.TILE_PATH_NORTH;
            tile_link[IGUIConstants.TILE_PATH_NORTHWEST][1][1] = IGUIConstants.TILE_PATH_EAST;

            if (m_control.isBigFont())
                optionPanel.setPreferredSize(new Dimension(280, 185));
            else
                optionPanel.setPreferredSize(new Dimension(240, 185));

            // set constraints
            c.weightx = 1.0; c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER; //end row
            m_loreArea = new JTextArea();
            if (IGUIConstants.isoView) {
                gridbag.setConstraints(optionPanel, c);
                mainPanel.add(optionPanel);
            }
            else {
                gridbag.setConstraints(tabPane, c);

                tabPane.addTab("Options", optionPanel);
                m_loreArea.setText( m_level.getMapLore() );
                m_loreArea.setCaretPosition(0);
                m_loreArea.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 0));
                JScrollPane lscrollPane = new JScrollPane(m_loreArea);
                lscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                lscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                lscrollPane.setPreferredSize(new Dimension(20, 20));

                tabPane.addTab("Lore", lscrollPane);
                mainPanel.add(tabPane);
            }

            // 3. map text panel:
            JPanel mapDataPanel = new JPanel( new BorderLayout( 1,1 ) );
            mapDataPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Map Text" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

            JPanel labelPanel = new JPanel( new BorderLayout( 1,1 ) );
            m_textArea = new JTextArea(4,4);
            m_textArea.setText( m_level.getMapText() );
            m_textArea.setCaretPosition(0);
            m_textArea.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 0));
            JScrollPane    scrollPane = new JScrollPane(m_textArea);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            labelPanel.add( scrollPane, BorderLayout.CENTER);
            mapDataPanel.add(labelPanel);

            // set constraints
            c.weightx = 1.0; c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.RELATIVE;
            gridbag.setConstraints(mapDataPanel, c);
            mainPanel.add(mapDataPanel);

            // 4. map tiling panel:
            JPanel tilePathPanel = new JPanel( new GridLayout( 5, 2 ) );
            tilePathPanel.setBorder( new CompoundBorder(
                new TitledBorder( new EtchedBorder(), "Paths for Map-Tiling" ),
                new EmptyBorder(
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS,
                    IGUIConstants.DIALOG_INSETS ) ) );

            createPanelLine(tilePathPanel, m_tilePath[0]=new JTextField(), 16, map.getTilePath(0), "North:   ");
            createPanelLine(tilePathPanel, m_tilePath[1]=new JTextField(), 16, map.getTilePath(1), "East:   ");
            createPanelLine(tilePathPanel, m_tilePath[2]=new JTextField(), 16, map.getTilePath(2), "South:   ");
            createPanelLine(tilePathPanel, m_tilePath[3]=new JTextField(), 16, map.getTilePath(3), "West:   ");
            createPanelLine(tilePathPanel, m_tilePath[4]=new JTextField(), 16, map.getTilePath(4), "Northeast:   ");
            createPanelLine(tilePathPanel, m_tilePath[5]=new JTextField(), 16, map.getTilePath(5), "Southeast:   ");
            createPanelLine(tilePathPanel, m_tilePath[6]=new JTextField(), 16, map.getTilePath(6), "Southwest:   ");
            createPanelLine(tilePathPanel, m_tilePath[7]=new JTextField(), 16, map.getTilePath(7), "Northwest:   ");

            CFancyButton attachMap = new CFancyButton("Attach Map", "Automatic attach the map in all possible directions",
                                                    null,
                                                    new ActionListener() {
                                                        public void actionPerformed(ActionEvent event) {
                                                            attachTiledMap();
                                                        }
                                                    });
            tilePathPanel.add(attachMap);

            CFancyButton clearPath = new CFancyButton("Clear Pathes", "clear all path names",
                                                    null,
                                                    new ActionListener() {
                                                        public void actionPerformed(ActionEvent event) {
                                                            clearPathes();
                                                        }
                                                    });
            tilePathPanel.add(clearPath);

            // set constraints
            c.weightx = 1.0; c.weighty = 1.0;
            gridbag.setConstraints(tilePathPanel, c);
            mainPanel.add(tilePathPanel);

        getContentPane().add( mainPanel, BorderLayout.CENTER );

        // build the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add( Box.createGlue() );
        JPanel left_buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel right_buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Help button
        JButton button = new JButton("Help");
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // open the help window
                JFrame help = new CFHelp(m_control.getMainView(), "tut_mapattr.html", false);
                help.setVisible(true); // show the window
            }
        });
        left_buttons.add( button );

        // OK button
        button = new JButton("OK");
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // try to apply changes, then kill window
                if (modifyMapProperties()) {
                    dispose();
                }
            }
        });
        right_buttons.add( button );

        // Restore Button
        button = new JButton("Restore");
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // restore to saved values from maparch
                restoreMapProperties();
            }
        });
        right_buttons.add( button );

        // Cancel Button
        button = new JButton("Cancel");
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // kill window
                dispose();
            }
        });
        right_buttons.add( button );

        buttonPanel.add(left_buttons, BorderLayout.WEST);
        buttonPanel.add(right_buttons, BorderLayout.EAST);

        getContentPane().add( buttonPanel, BorderLayout.SOUTH );

        setBounds(m_control.getMainView().getX()+(m_control.getMainView().getWidth()-545)/2,
                  m_control.getMainView().getY()+(m_control.getMainView().getHeight()-480)/2,
                  545, 480);
        pack();
        setVisible(true);
    }

    void attachTiledMap()
    {
      int i,ii;
      String link, map_path;
      boolean repeat_flag=true;
      TileMapHeader[] tileHeaders       = new TileMapHeader[8]; // tile paths

      if(m_level.mapFile == null) // map is created but was never saved
      {
        m_control.showMessage("Missing File Path", "Can't attach unsaved map.\nMap must be saved first to the map folder!\n",JOptionPane.WARNING_MESSAGE);
        return;
      }

      /* first action: we go around all 8 links and try to load the maps */
      for(i=0;i<8;i++)
      {
        String path;

        tileHeaders[i] = null;
        path = m_tilePath[i].getText();
        if (path==null || path.length()==0)
          continue;

        /* lets test there is a map with this name & path */
        map_path = getCanonicalTilePath(path);
        if(map_path == null)
        {
          m_control.showMessage("Error creating Map Path", "Please control the map path\n"+path,JOptionPane.ERROR_MESSAGE);
          return;
        }
        /* run through all loaded maps and see we have a map with this path
         * if unsaved, ask for stop or autosave
         */
        CMapControl level = m_control.getMapSaveStatusByPath(map_path);
        if(level != null) // opened map in the editor !
        {
          if (!m_control.askConfirm("Map is loaded",
                                   "The map "+path+" is opened in the editor\nShould i autosave & update the map?"))
            return;
          m_control.setMapSaveStatusByPath(map_path);
        }

        /* now lets check we have perhaps this map opened in the editor - force a "autosave" or stop */

        /* at last load the map and its links */
        tileHeaders[i] = openMapFileHeader(path);
        if(tileHeaders[i] == null || tileHeaders[i].maparch == null)
        {
          m_control.showMessage("Can't find Map", "Please control your map file\n"+map_path,JOptionPane.ERROR_MESSAGE);
          return;
        }
        tileHeaders[i].map = level;
      }


      /* We have loaded all 8 direct linked maps around our map.
       * now, lets check free spaces. We try to "fill" them
       * by checking the "side" path links of the loaded ones.
      */
     while(repeat_flag == true)
     {
       repeat_flag = false;
       for (i = 0; i < 8; i++) {
         if (tileHeaders[i] != null) {
           for (ii = 0; ii < 2; ii++) {

             /*
             m_control.showMessage("Trying: ", "Base: "+ tile_link_name[i]
                                   +"\nchecking: "+tile_link_name[tile_link[i][ii][0]]
                                   +"\nldir: "+ tile_link_name[tile_link[i][ii][1]]);
              */
             if (tileHeaders[tile_link[i][ii][0]] == null &&
                 tileHeaders[i].maparch.getTilePath(tile_link[i][ii][1]) != null) {
               link = createTilePath(tileHeaders[i],tileHeaders[i].maparch.getTilePath(tile_link[i][ii][1]));
               /* lets test there is a map with this name & path */

               map_path = getCanonicalTilePath(link);
               if(map_path == null)
               {
                 m_control.showMessage("Error creating Map Path", "Please control the map path\n"+link,JOptionPane.ERROR_MESSAGE);
                 return;
               }
               /* run through all loaded maps and see we have a map with this path
                * if unsaved, ask for stop or autosave
                */
               CMapControl level = m_control.getMapSaveStatusByPath(map_path);
               if(level != null) // opened map in the editor !
               {
                 if (!m_control.askConfirm("Map is loaded",
                                          "The map "+link+" is opened in the editor\nShould i autosave & update the map?"))
                   return;
                 m_control.setMapSaveStatusByPath(map_path);
               }

              tileHeaders[tile_link[i][ii][0]] = openMapFileHeader(link);
              /*
               m_control.showMessage("Trying: ", "Base: "+ tile_link_name[i]
                                     +"\nchecking: "+tile_link_name[tile_link[i][ii][0]]
                                     +"\ndir: "+ tile_link_name[tile_link[i][ii][1]]
                                     +"\nlink: "+link);
               */
               if (tileHeaders[tile_link[i][ii][0]] != null) // if now != null, we have successful loaded the linked map
               {
                 tileHeaders[tile_link[i][ii][0]].map = level;
                 repeat_flag = true;
               }
               else {
                 if (m_control.askConfirm("Invalid Tile Link",
                                          "In " + tile_link_name[i] + ": " + tileHeaders[i].link_path
                                          +"\nthe link to "+ tile_link_name[tile_link[i][ii][0]] + " is invalid."
                                          +"\nLink to "+tile_link_name[tile_link[i][ii][1]]+" is "
                                          +tileHeaders[i].maparch.getTilePath(tile_link[i][ii][1])
                                          +"\ngenerated link:" + link
                                          + "\nShould we stop attaching?")) {
                   return;
                 }
               }
             }
           }
         }
       }
     }
     /* now we have all maps around our starting map loaded.
      * As next step we check all links so we are sure all is legal.
      * is all ok, we set the new links and save our maps.
      * We *also* must check loaded maps and set the links there too!
      */
     for(i=0;i<8;i++)
     {
       if (tileHeaders[i] != null) {
         for (ii = 0; ii < 2; ii++) {
           // TODO: check links
         }
      }
     }

     /* finally... set the links! */
     for(i=0;i<8;i++)
     {
       if (tileHeaders[i] != null)
       {
         /* generate a valid path relativ to both map positions */
         try {
           link = getTilePath(m_level.mapFile.getCanonicalPath(), tileHeaders[i].mapfile.getCanonicalPath());
           /* set the link of our source map to the map around */
           m_tilePath[i].setText(link);
           /* generate again a valid path relativ to both map positions */
           link = getTilePath(tileHeaders[i].mapfile.getCanonicalPath(),m_level.mapFile.getCanonicalPath());
           tileHeaders[i].maparch.setTilePath(rev_link[i],link);
           if(tileHeaders[i].map != null) // update links in loadd maps too
             tileHeaders[i].map.m_model.m_mapArch.setTilePath(rev_link[i], link);
       } catch (IOException e) {
         m_control.showMessage("Can't Get Path","Fatal Error: "+e.getMessage(),JOptionPane.ERROR_MESSAGE);
         return;
       }
       }
     }

     /* all done! now we write all back */
     FileWriter fileWriter;
     BufferedWriter bufferedWriter;

     for(i=0;i<8;i++)
     {
       if (tileHeaders[i] != null)
       {
         String fname, tail;
         try {
           fname = tileHeaders[i].mapfile.getCanonicalPath();
           fileWriter = new FileWriter(tileHeaders[i].mapfile);
            bufferedWriter = new BufferedWriter(fileWriter);
            tileHeaders[i].maparch.writeMapArch(bufferedWriter);
            tail = tileHeaders[i].maptail.toString().trim();
            if(tail.length() > 0) {
                bufferedWriter.write(tail);
                if(tail.lastIndexOf(0x0a) != tail.length()-1)
                    bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter.close();
       } catch (IOException e) {
         m_control.showMessage("Can't Write Map","Fatal Error: "+e.getMessage(),JOptionPane.ERROR_MESSAGE);
         return;
       }
       }
     }
    }

    private String getTilePath(String base, String link)
    {
      String mapdir, path="", first, second, sep;
      int i,ii, pos, pos2;

      i = base.lastIndexOf("/");
      ii = base.lastIndexOf(File.separator);
      pos = i;
      if(ii > i)
        pos = ii;

      i = link.lastIndexOf("/");
      ii = link.lastIndexOf(File.separator);
      pos2 = i;
      if(ii > i)
        pos2 = ii;

      try {
        mapdir = m_control.m_mapDir.getCanonicalPath();
        first = base.substring(mapdir.length(),pos).trim();
        second = link.substring(mapdir.length(),pos2).trim();
        /*m_control.showMessage("PATH", mapdir.length()+" - "+pos+"\n:: "+first+"\n:: "+second+"\n"+base+"\n"+link);*/
    } catch (IOException e) {
      m_control.showMessage("Can't Write Map","Fatal Error: "+e.getMessage(),JOptionPane.ERROR_MESSAGE);
      return null;
    }

    /* funny lame glitch in java... */
    sep = File.separator;
    if(sep.compareTo("\\") == 0)
       sep = "\\\\";

      /* our map is in root - second is higher or same level */
      if(first.length() == 0)
        return link.substring(mapdir.length()).trim().replaceAll(sep,"/");
      /* same folder... we return the name without '/' */
      if(first.compareTo(second) == 0)
        return link.substring(pos2+1).trim().replaceAll(sep,"/");
      /* second is subfolder of first */
      if(second.startsWith(first))
        return link.substring(pos+1).trim().replaceAll(sep,"/");
      /* in any other case we return a absolute path */
      return link.substring(mapdir.length()).trim().replaceAll(sep,"/");
    }

    private String createTilePath (TileMapHeader map, String link_path)
    {
      /* our link is a absolut call - not much to do */
      if (link_path.startsWith(File.pathSeparator) || link_path.startsWith("/"))
        return link_path;

      /* is a relative call - so we must attach the folder part of map file name to it */
      String path = map.link_path.substring(0,map.link_path.length()-map.maparch.getFileName().length());
      if(!path.endsWith("/") && !path.endsWith(File.separator) )
         path+=File.separator;
      path +=link_path;

      /*m_control.showMessage("Path", "map: "+map.link_path+"\nfilename: "+map.maparch.getFileName()+"\nlink: "+link_path+"\nPATH: "+path);*/

      return path;
    }

    private String getCanonicalTilePath(String path)
    {
      File newfile;

      if (path.startsWith(File.pathSeparator) || path.startsWith("/"))
        newfile = new File(m_control.m_mapDir.getAbsolutePath(), path.substring(1));
      else
        newfile = new File(m_level.mapFile.getParent(), path);

      try
      {
          return newfile.getCanonicalFile().toString();
      } catch (IOException e)
      {
          return null;
      }
    }

    private TileMapHeader openMapFileHeader(String path)
    {
      File newfile;
      TileMapHeader maphead=null;

      if (path.startsWith(File.pathSeparator) || path.startsWith("/"))
        newfile = new File(m_control.m_mapDir.getAbsolutePath(), path.substring(1));
      else
        newfile = new File(m_level.mapFile.getParent(), path);

      try {
        try {
          maphead = loadMapFileHeader(newfile.getCanonicalFile());
        } catch (IOException e) {
          maphead = null;
          /*m_control.showMessage("Invalid Path", "Failed to load file for tiled map.\n"+newfile.getAbsolutePath());*/
        }

      }
      catch (CGridderException e) {
        maphead = null;
       /* m_control.showMessage("Couldn't load Map", e.getMessage());*/
      }

      if(maphead != null)
      {
        maphead.mapfile = newfile;
        maphead.link_path = path;

        for(int x=0;x<8;x++)
        {
          if(maphead.maparch.getTilePath(x) != null && maphead.maparch.getTilePath(x).length() == 0)
            maphead.maparch.setTilePath(x, null);
        }
      }

      return maphead;
    }

    private TileMapHeader loadMapFileHeader(File file) throws CGridderException {
        String thisLine;

        TileMapHeader maphead = null;

        try {
            FileReader fr =  new FileReader(file);
            BufferedReader myInput = new BufferedReader(fr);

            // first of all we read the map arch (if that fails we throw an exception)
            MapArchObject maparch = new MapArchObject();
            if (!maparch.parseMapArch(myInput, file.getName()))
                throw new CGridderException("The file '"+file.getName()+"' does not\n"+
                                            "contain a valid Atrinik map format!\n");

            maphead = new TileMapHeader(maparch);
            // now we store the map arches as tail
            while((thisLine = myInput.readLine()) != null)
            {
              if(thisLine.lastIndexOf(0x0a) != thisLine.length()-1)
                thisLine += "\n";
              maphead.maptail.append(thisLine);
            }

            myInput.close();
            fr.close();

        } catch (IOException e) {
          maphead = null;
          /*m_control.showMessage("Can't find map:","Link: "+file.getName()+"\n"+e.getMessage());*/
        }
        return maphead;
    }

    void clearPathes()
    {
      for (int i=0; i<8; i++) {
        m_tilePath[i].setText("");
      }
      m_control.refreshMenus();
    }

    /**
     * create an "attribute"-line (format: <label> <textfield>)
     * and insert it into the given parent layout
     *
     * @param parent         parent panel where line gets inserted
     * @param textField      textfield
     * @param n              lenght of textfield
     * @param defaultText    initial text in textfield
     * @param label          (attribute-)label
     */
     void createPanelLine(JPanel parent, JTextField textField, int n,
                                 String defaultText, String label) {
        JPanel lineLayout = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // layout for this line
        lineLayout.add(new JLabel(label));  // create label (attr. name)
        //textField = new JTextField(n);      // create text field
        textField.setColumns(n);
        textField.setText(defaultText);     // insert default text
        lineLayout.add(textField);          // add textf. to line
        parent.add(lineLayout);             // add line to parent layout
    }

    /**
     * create a checkbox-line and insert it into the given parent layout.
     * similar to createPanelLine
     *
     * @param parent        parent panel where checkbox gets inserted
     * @param checkBox      checkbox
     * @param state         initial state
     * @param label         (attribute-)label
     */
    void createPanelCBox(JPanel parent, JCheckBox checkBox,
                                 boolean state, String label) {
        JPanel lineLayout = new JPanel(new FlowLayout(FlowLayout.CENTER)); // layout for this line
        //checkBox = new JCheckBox(label);  // create checkbox
        checkBox.setText(label);
        checkBox.setSelected(state);      // set to current state
        lineLayout.add(checkBox);         // add checkbox to line
        parent.add(lineLayout);           // add line to parent layout
    }

    /**
     * Checks the given values and modifies the current level.
     * @return true if the level properties were edited,
     *         false if the parameters were wrong.
     */
    private boolean modifyMapProperties() {
        MapArchObject map = m_level.m_model.m_mapArch;  // map arch object
        boolean modifyTilepaths = false; // true when map tile-paths were modified

        // tmp variables for parsing
        int t_width, t_height;
        int t_enter_x, t_enter_y;
        int t_reset_timeout, t_swap_time;
        int t_difficulty, t_darkness;
        int t_temp, t_pressure, t_humid;
        int t_winds, t_windd, t_sky;

        // first check if the entries are all okay
        try {
            // try to parse everything
            t_width = parseProperty(m_levelWidthField.getText(), "Width");
            t_height = parseProperty(m_levelHeightField.getText(), "Height");
            t_enter_x = parseProperty(m_enterX.getText(), "Enter X");
            t_enter_y = parseProperty(m_enterY.getText(), "Enter Y");
            t_swap_time = parseProperty(m_swapTime.getText(), "Swap Time");
            t_reset_timeout = parseProperty(m_resetTimeout.getText(), "Reset Timeout");
            t_difficulty = parseProperty(m_difficulty.getText(), "Difficulty");
            t_darkness = parseProperty(m_darkness.getText(), "Darkness");

            if (!IGUIConstants.isoView) {
                t_temp = parseProperty(m_temp.getText(), "Temperature");
                t_pressure = parseProperty(m_pressure.getText(), "Pressure");
                t_humid = parseProperty(m_humid.getText(), "Humidity");
                t_winds = parseProperty(m_windspeed.getText(), "Wind Speed");
                t_windd = parseProperty(m_winddir.getText(), "Wind Direction");
                t_sky = parseProperty(m_sky.getText(), "Sky Setting");
            }
            else {
                t_temp = 0; t_pressure = 0;
                t_humid = 0; t_winds = 0;
                t_windd = 0; t_sky = 0;
            }

            // Now do some sanity checks:
            if (t_width < 1 || t_height < 1) {
                m_control.showMessage(
                    "Illegal Value",
                    "Level dimensions must be greater than zero." );
                return false;
            }
            /*
            if (t_darkness > 1000) {
                m_control.showMessage(
                    "Illegal Value",
                    "Darkness level must be in range 0-1000." );
                return false;
            }*/
            if (m_nameField.getText().length() == 0) {
                m_control.showMessage(
                    "missing Map Name",
                    "You must specify a Map Name." );
                return false;
            }
        }
        catch(CGridderException e) {
            m_control.showMessage("Invalid Entry", e.getMessage());
            return false;
        }

        // if the mapsize has been modified, see if we should ask for a confirm
        if (m_level.checkResizeMap(t_width, t_height)) {
            if (askConfirmResize(t_width, t_height) == false) {
                // resizing has been cancelled
                t_width = m_level.getMapWidth();
                t_height = m_level.getMapHeight();
            }
        }

        // now that all is well, write the new values into the maparch
        m_control.setLevelProperties (m_level, m_textArea.getText(), m_loreArea.getText(),
                                      m_nameField.getText(), t_width, t_height);

		map.setBackgroundMusic(m_bgmusicField.getText());
        map.setEnterX(t_enter_x);
        map.setEnterY(t_enter_y);
        map.setResetTimeout(t_reset_timeout);
        map.setSwapTime(t_swap_time);
        map.setDifficulty(t_difficulty);
        map.setFixedReset(m_fixedReset.isSelected());
        map.setDarkness(t_darkness);
        map.setUnique(m_unique.isSelected());
        map.setOutdoor(m_outdoor.isSelected());

        map.setTemp(t_temp);
        map.setPressure(t_pressure);
        map.setHumid(t_humid);
        map.setWindspeed(t_winds);
        map.setWinddir(t_windd);
        map.setSky(t_sky);

        if (IGUIConstants.isoView) {
            // these flags are for atrinik only
            map.setNoSave(m_no_save.isSelected());
            map.setNoMagic(m_no_magic.isSelected());
            map.setNoPriest(m_no_priest.isSelected());
            map.setNoHarm(m_no_harm.isSelected());
            map.setNoSummon(m_no_summon.isSelected());
            map.setFixedLogin(m_fixed_login.isSelected());
            map.setPermDeath(m_perm_death.isSelected());
            map.setUltraDeath(m_ultra_death.isSelected());
            map.setUltimateDeath(m_ultimate_death.isSelected());
            map.setPvp(m_pvp.isSelected());
        }

        // update tilepaths
        for (int i=0; i<8; i++) {
            if ((map.getTilePath(i)==null && (m_tilePath[i].getText()==null || m_tilePath[i].getText().length()==0))
                || (map.getTilePath(i) != null && !map.getTilePath(i).equals(m_tilePath[i].getText())))
                modifyTilepaths = true;
            map.setTilePath(i, m_tilePath[i].getText());
        }

        // refresh menus if tilepaths have been modified
        if (modifyTilepaths)
            m_control.refreshMenus();

        // set flag that map has changed
        m_level.setLevelChangedFlag();

        return true;
    }

    /**
     * This is a simple string-to-int parser that throws
     * CGridder- instead of NumberFormatExceptions, with appropriate
     * errormessage. Used in modifyMapProperties().
     *
     * @param s       string to be parse
     * @param label   attribute label for errormessage
     * @return        value of String 's', zero if 's' is empty
     * @throws CGridderException when parsing fails
     */
    private int parseProperty(String s, String label) throws CGridderException {
        int r;  // return value

        if (s.length() == 0)
            return 0;   // empty string is interpreted as zero

        try {
            r = Integer.parseInt(s);  // trying to parse
        }
        catch(NumberFormatException illegalNumbers) {
            // 's' is not a number
            throw new CGridderException(label+": '"+s+"' is not a numerical integer value.");
        }

        // negative values are not allowed
        if (r < 0 && label != "Darkness")
            throw new CGridderException(label+": '"+s+"' is negative.");

        return r; // everything okay
    }

    /**
     * Reset all map properties to the saved values in the maparch
     */
    private void restoreMapProperties() {
        MapArchObject map = m_level.m_model.m_mapArch;  // map arch object

        m_textArea.setText(m_level.getMapText());
        m_loreArea.setText(m_level.getMapLore());
        m_nameField.setText(m_level.getMapName());
		m_bgmusicField.setText(m_level.getBackgroundMusic());
        m_levelWidthField.setText(""+map.getWidth());
        m_levelHeightField.setText(""+map.getHeight());
        m_enterX.setText(""+map.getEnterX());
        m_enterY.setText(""+map.getEnterY());
        m_swapTime.setText(""+map.getSwapTime());
        m_resetTimeout.setText(""+map.getResetTimeout());
        m_darkness.setText(""+map.getDarkness());
        m_difficulty.setText(""+map.getDifficulty());

        m_unique.setSelected(map.isUnique());
        m_outdoor.setSelected(map.isOutdoor());
        m_fixedReset.setSelected(map.isFixedReset());

        if (!IGUIConstants.isoView) {
            m_temp.setText(""+map.getTemp());
            m_pressure.setText(""+map.getPressure());
            m_humid.setText(""+map.getHumid());
            m_windspeed.setText(""+map.getWindspeed());
            m_winddir.setText(""+map.getWinddir());
            m_sky.setText(""+map.getSky());
        }
        else {
          m_no_save.setSelected(map.isNoSave());
          m_no_magic.setSelected(map.isNoMagic());
            m_no_priest.setSelected(map.isNoPriest());
            m_no_harm.setSelected(map.isNoHarm());
            m_no_summon.setSelected(map.isNoSummon());
            m_fixed_login.setSelected(map.isFixedLogin());
            m_perm_death.setSelected(map.isPermDeath());
            m_ultra_death.setSelected(map.isUltraDeath());
            m_ultimate_death.setSelected(map.isUltimateDeath());
            m_pvp.setSelected(map.isPvp());
        }

        for (int i=0; i<8; i++)
            m_tilePath[i].setText(""+map.getTilePath(i));
    }

    /**
     * Open a popup and ask user to confirm his map-resizing selection.
     * This popup dialog disables all other windows (and threads).
     *
     * @return true if user confirmed, false if user cancelled resize
     */
    private boolean askConfirmResize(int newWidth, int newHeight) {
        if (JOptionPane.showConfirmDialog(this,
                                 "You selected a new map size of "+newWidth+"x"+newHeight+". If the map was\n"+
                                 "resized in this way, some objects would get cut off and deleted.\n"+
                                 "Are you really sure you want this?",
                                 "Confirm",
                                 JOptionPane.YES_NO_OPTION,
                                 JOptionPane.INFORMATION_MESSAGE)
                    == JOptionPane.YES_OPTION)
            return true;
        return false;
    }

    private class TileMapHeader
    {
      MapArchObject maparch;
      String link_path;
      File mapfile;
      StringBuffer maptail;
      CMapControl map; // map loaded in editor
      public TileMapHeader (MapArchObject maphead)
      {
        this.maparch = maphead;
        this.link_path = null;
        this.mapfile = null;
        this.maptail = new StringBuffer("");
        map = null;
        }
    }; // End of class
}
