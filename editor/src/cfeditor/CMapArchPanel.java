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
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/**
 * <code>CMapArchPanel</code> implements the panel that holds information
 * about the currently selected ArchObject on the map.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMapArchPanel extends JPanel {
    // constants for the 'task' parameter in editScriptWanted()
    public static final int SCRIPT_OPEN      = 0;
    public static final int SCRIPT_EDIT_PATH = 1;
    public static final int SCRIPT_REMOVE    = 2;

    // MAN! This is one of these java shitholes
    // without overruling getScrollableTracksViewportWidth(),
    // is JTextPanel not scrolling horizontal
    // i searched hours for the reason... the net is full of broken examples
    private JTextPane archEdit = new JTextPane()
    {
        public boolean getScrollableTracksViewportWidth()
        {
            if (getSize().width < getParent().getSize().width) {
                return true;
            }
            else {
                return false;
            }
        }
        public void setSize(Dimension d) {

            if (d.width < getParent().getSize().width) {
                d.width = getParent().getSize().width;
            }

            super.setSize(d);
        }
    };

    private static final String MAPARCHPANEL_LOCATION_KEY = "MainWindowMapArchPanel.dividerLocation";

    /** Controller of this subview. */
    private CMainControl m_control;
    private CMainView m_view;
    /** The "Import..." button. */

    private CFancyButton buttonF0;
    private CFancyButton buttonF1;
    private CFancyButton buttonF2;
    private CFancyButton buttonF3;
    private CFancyButton buttonF4;
    private CFancyButton buttonF5;
    private CFancyButton buttonF6;
    private CFancyButton buttonF7;
    private CFancyButton buttonF8;

    private CSplitPane m_splitPane;
    private JTabbedPane m_panelDesktop;
    private JPanel m_panelDesktop2;

    private Style m_CurrentAttributes;
    private Document doc;
    private JScrollPane scrollArchPane= new JScrollPane();

    private JPanel dirPanel;
    private JPanel archPanel;   // panel with name/face etc
    private JPanel textPanel;   // panel with message text
    private JPanel scriptPanel; // panel with scripts
    private JPanel animationPanel;

    private JTextArea archTextArea = new JTextArea(4, 25);  // arch text field
    private JTextField archNameField = new JTextField(14);  // arch name field
    private JTextField facingField = new JTextField(14);  // arch name field

//    private JLabel archInvCount = new JLabel();
    private JLabel archMapPos = new JLabel();

    private JLabel archFaceText = new JLabel();
    private JLabel archTypeText = new JLabel();
    private JLabel archAnimText = new JLabel();
//    private JLabel archTileText = new JLabel();
    private JPanel mapArchPanel = new JPanel();

    private CFancyButton submitChange;
    private CFancyButton invChange;
    private CFancyButton attrWin;

    // stuff for scripting tab
    private JButton s_new;
    private JButton s_path;
    private JButton s_modify;
    private JButton s_remove;
    private JList eventList;

    /* Build Panel */
    CMapArchPanel( CMainControl control , CMainView view)
    {
        m_control = control;
        m_view = view;
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        setLayout( new BorderLayout() );

        // scrollPane2 contains the document for archtext editing
        JScrollPane scrollPane2 = new JScrollPane(archEdit);
        scrollPane2.getViewport().add(archEdit);
        add(scrollPane2, BorderLayout.EAST);
        scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane2.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

        JScrollPane scrollPane3 = new JScrollPane(mapArchPanel);
        scrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane3.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );


        m_panelDesktop = new JTabbedPane( JTabbedPane.TOP );
        mapArchPanel.setLayout(new BorderLayout());
        m_splitPane = new CSplitPane(
            CSplitPane.HORIZONTAL_SPLIT,
            scrollPane3,
            scrollPane2);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,1));

        mapArchPanel.add(buttonPanel, BorderLayout.WEST);

        // our buttons
        submitChange = new CFancyButton("Apply", "apply object changes" ,null,null);
        submitChange.setFocusable(false);
        submitChange.setEnabled(true);
        submitChange.setForeground(java.awt.Color.blue);
        buttonPanel.add(submitChange);
        invChange = new CFancyButton("Add Inv", "add object to inventory" ,null,null);
        invChange.setFocusable(false);
        invChange.setEnabled(true);
        invChange.setForeground(java.awt.Color.blue);
        buttonPanel.add(invChange);
        attrWin = new CFancyButton("Attributes", "open attributes dialog" ,null,null);
        attrWin.setFocusable(false);
        attrWin.setEnabled(true);
        attrWin.setForeground(java.awt.Color.blue);
        buttonPanel.add(attrWin);

        mapArchPanel.add(m_panelDesktop, BorderLayout.CENTER);

        // setup our plain archPanel - we need to add later field to it
        // depending on the arch typ selected
        archPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        archPanel.setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        dirPanel = new JPanel();
        GridBagLayout gridbag2 = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        dirPanel.setLayout(gridbag2);
        c2.fill = GridBagConstraints.BOTH;
        c2.anchor = GridBagConstraints.NORTHWEST;

        animationPanel = new JPanel();

        //m_panelDesktop.add(scrollArchPane, "Arch"); // and add it to this panel object*/
         setupArchPanel();
         setupTextPanel();
         setupScriptPanel();
         setupAnimationPanel();

        m_panelDesktop.add(archPanel, "Arch");
        m_panelDesktop.add(textPanel, "Msg Text");

        m_panelDesktop.add(scriptPanel, "Scripts");

        m_panelDesktop.add(animationPanel, "Animation");

        // calculate default value in case there is no settings file
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int divLocation = Integer.parseInt(settings.getProperty(
                          MAPARCHPANEL_LOCATION_KEY, ""+(int)(0.49*screen.getWidth())));

        m_splitPane.setDividerLocation( divLocation );
        m_splitPane.setDividerSize( 5 );
        add( m_splitPane, BorderLayout.CENTER );

        updateFont(true);

        doc = archEdit.getDocument();

        submitChange.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                applyArchPanelChanges(m_view.getMapTileSelection());
            }
        });

        attrWin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                openAttrDialog(m_view.getMapTileSelection());
            }
        });

        invChange.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {

                ArchObject arch = m_control.getArchPanelSelection();
                if(arch == null ) // nothing selected?
                    return;

                // no single tile?
//                if(arch.getRefCount()>0 || arch.getRefFlag())
//                    return;

                ArchObject inv = m_view.getMapTileSelection();
                if (inv==null)
                    return;

                // if this is a multi-tail, we put the new arch into the head's inv.
                if (inv.getRefFlag() && inv.getMapMultiHead() != null)
                    inv = inv.getMapMultiHead();

                ArchObject invnew;
                if (arch.isDefaultArch()) {
                    // create a new copy of a defautl arch
                    invnew = m_control.getArchObjectStack().newArchObjectInstance(arch.getNodeNr() );
                }
                else {
                    // create clone from a pickmap
                    invnew = arch.getClone(inv.getMapX(), inv.getMapY());
                }

                m_control.archObjectParser.postParseMapArch(invnew, 0);
                inv.addInvObj(invnew);
                m_control.getMainView().setMapTileList(m_control.m_currentMap,inv.getMyID());
                m_control.m_currentMap.setLevelChangedFlag();  // the map has been modified
            }
        });

    }

	public void updateMapTileList()
	{
		ArchObject inv = m_view.getMapTileSelection();
		if (inv==null)
			return;

		// if this is a multi-tail, we put the new arch into the head's inv.
		if (inv.getRefFlag() && inv.getMapMultiHead() != null)
			inv = inv.getMapMultiHead();

		m_control.getMainView().setMapTileList(m_control.m_currentMap,inv.getMyID());
    }

    /**
     * When the "apply"-button on the ArchPanel (at the bottom of the window)
     * is pressed, this function updates the active arch object.
     *
     * @param active_arch       the currently selected arch
     */
    public void applyArchPanelChanges(ArchObject active_arch) {
        ArchObject arch;             // ArchObject that gets modified
        String old_ArchText;         // the old ArchText
        String old_msg;              // old arch msg
        String face;
        boolean need_redraw = false; // do we need a map-redraw? true/false
        int i;

        if(active_arch == null)
           return;

        // If the active arch is part of a multi, the mutli-head's stats
        // are taken instead:
        if (active_arch.getRefFlag() && active_arch.getMapMultiHead() != null)
            arch = active_arch.getMapMultiHead();
        else
            arch = active_arch;

        ArchObject defarch = m_control.getArch(arch.getNodeNr());
        if(defarch == null)    // hm, this should NOT happen
            return;

        old_ArchText = arch.getArchText(); // old ArchText
        old_msg = arch.getMsgText();       // old arch msg

        // We update all panels: name, face, msg and archText (more to come...)
        // the obj name:
        if(testForText(archNameField.getText())) // there is something in
        {
            // if this equal the default name...
            if(defarch.getObjName() != null)
            {
                if(archNameField.getText().compareTo(defarch.getObjName()) == 0)
                    arch.setObjName(null); // yes, we don't need it in map
                else
                    arch.setObjName(archNameField.getText());    // overrule in map arch
            }
            else if (archNameField.getText().compareTo(arch.getArchName()) == 0)
                arch.setObjName(null);
            else    // def is null, something is in panel, so we set it
                arch.setObjName(archNameField.getText());    // overrule in map arch
        }
        else
        {
            arch.setObjName(null); // nothing in, nothing in map arch
            // hm, there is no way yet to overrule a default arch name with "nothing"
            // like <name > ore <name "">..??
        }

        // the msg TEXT!! ("msg ... endmsg")
        // if there is an entry in the archTextArea (msg window), this
        // overrules the default text (if any)
        if(testForText(archTextArea.getText())) // there is something in the msg win
        {
            if(defarch.getMsgText() != null) {
                // trim text from message window
                String new_msgtext = archTextArea.getText();
                if (new_msgtext != null)
                    new_msgtext = new_msgtext.trim();

                if(new_msgtext.equals(defarch.getMsgText().trim()))
                    arch.deleteMsgText(); // yes, we don't need it in map
                else {
                    arch.resetMsgText();
                    arch.addMsgText(new_msgtext);
                }
            }
            else {
                arch.resetMsgText();
                arch.addMsgText(archTextArea.getText());
            }
        }
        else // there is nothing in the msg win
        {
            arch.deleteMsgText(); // always delete this...
            if(defarch.getMsgText() != null)
                arch.addMsgText(""); // but here we must overrule default with msg/endmsg (empty msg)
        }

        // read from archEdit (bottom right textwin) only the attributes
        // that differ from the default arch. These get stored into
        // the arche's archText (and finally in the map).
        arch.setArchText(defarch.diffArchText(archEdit.getText(), false));

        /* check we have a face and set the real face obj for fast access */
        face = arch.getAttributeString("face", null);
        // we have a new face name
        if( face != null)
        {
          if(arch.setRealFace(face) == true)
            need_redraw = true;
        }
        if (arch.getAttributeString("animation", null).length() > 0)
          arch.setAnimName(arch.getAttributeString("animation", null));
        else
          arch.setAnimName(defarch.getAnimName());

        if (arch.getAttributeString("direction", null).length() > 0)
        {
          int dir = arch.getAttributeValue("direction", null);
          if(defarch.getDirection() != dir)
            arch.setDirection(dir);
        }
        else
          arch.setDirection(defarch.getDirection());


        i = arch.getObjectFaceNr();
        arch.setObjectFace();
        if(i != arch.getObjectFaceNr())
          need_redraw = true;

        // we look for 'type' in the ArchText. In future maybe type should get
        // a seperate textfield
        if (arch.getAttributeString("type", null).length() > 0)
            arch.setArchTypNr(arch.getAttributeValue("type", null)); // specified type

        // Recalculate the edit_type value. It shall stay 100% accurate ;)
        arch.calculateEditType(m_control.m_currentMap.getActive_edit_type());

        // if the archtext changed, set the map changed flag
        if ((old_ArchText != null && !old_ArchText.equals(arch.getArchText())) ||
            (old_ArchText == null && arch.getArchText() != null) ||
            (old_msg!=null && !old_msg.equals(arch.getMsgText())) ||
            (old_msg == null && arch.getMsgText() != null))
            m_control.m_currentMap.setLevelChangedFlag();

        // there we go!!!!!
        // now here will be the special panels added!
        refresh();

		updateMapTileList();

        // if needed (due to face changes), we also redraw the map
        if (need_redraw)
        {
          m_control.getMainView().RefreshMapTileList();
          m_control.m_currentMap.repaint();
        }
    }

    // simple "white space test, to eliminate ' '
    // perhaps we should include here a real white space test
    public boolean testForText(String text)
    {
        for(int i=0;i <text.length();i++)
        {
            if(text.charAt(i) != ' ')
                return true;
        }
        return false;
    }

    private void setupAnimationPanel()
    {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();

      animationPanel.setLayout(gridbag);
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.weighty = 0;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.insets = new Insets(3, 3, 0, 0);

      archAnimText.setText("Animation: ");
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth=1;
      c.gridheight=1;
      gridbag.setConstraints(archAnimText, c);
      animationPanel.add(archAnimText);

    }

    /**
     * set up the arch panel entry of the lower window
     */
    private void setupArchPanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        GridBagLayout gridbag2 = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        //archPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        archPanel.setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 4, 0, 0);
        // add our elements
        dirPanel.setLayout(gridbag2);
        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 1.0;
        c2.weighty = 0;
        c2.anchor = GridBagConstraints.NORTHWEST;
        c2.insets = new Insets(3, 4, 0, 0);

        archNameField.setText("");
        archNameField.setForeground(Color.blue);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        c.gridheight=1;
        gridbag.setConstraints(archNameField, c);
        archPanel.add(archNameField);

        archFaceText.setText("");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        c.gridheight=1;
        gridbag.setConstraints(archFaceText, c);
        archPanel.add(archFaceText);

        archTypeText.setText("");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        c.gridheight=1;
        gridbag.setConstraints(archTypeText, c);
        archPanel.add(archTypeText);

        archMapPos.setText("");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=1;
        c.gridheight=1;
        gridbag.setConstraints(archMapPos, c);
        archPanel.add(archMapPos);

        buttonF7 = new CFancyButton(null , "direction 7",
                          IGUIConstants.DIRECTION_7_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),7);
                              }
                          });
        buttonF7.setBorderPainted(false);
        buttonF7.setFocusable(false);
        c2.gridx = 0;
        c2.gridy = 0;
        c2.gridwidth=1;
        c2.gridheight=1;
        gridbag2.setConstraints(buttonF7, c2);
        dirPanel.add(buttonF7);

        buttonF8 = new CFancyButton(null, "direction 8",
                          IGUIConstants.DIRECTION_8_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),8);
                              }
                          });
          buttonF8.setBorderPainted(false);
          buttonF8.setFocusable(false);
          c2.gridx = 1;
          c2.gridy = 0;
          gridbag2.setConstraints(buttonF8, c2);
        dirPanel.add(buttonF8);

        buttonF1 = new CFancyButton(null, "direction 1",
                          IGUIConstants.DIRECTION_1_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),1);
                              }
                          });
        c2.gridx = 2;
        c2.gridy = 0;
        buttonF1.setBorderPainted(false);
        buttonF1.setFocusable(false);
        gridbag2.setConstraints(buttonF1, c2);
        dirPanel.add(buttonF1);

        buttonF6 = new CFancyButton(null, "direction 6",
                          IGUIConstants.DIRECTION_6_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),6);
                              }
                          });
        c2.gridx = 0;
        c2.gridy = 1;
        buttonF6.setBorderPainted(false);
        buttonF6.setFocusable(false);
        gridbag2.setConstraints(buttonF6, c2);
        dirPanel.add(buttonF6);

        buttonF0 = new CFancyButton("0", "direction 0",
                          null,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),0);
                              }
                          });
        c2.gridx = 1;
        c2.gridy = 1;
        buttonF0.setFocusable(false);
        buttonF0.setBorderPainted(true);
        buttonF0.setFont(archNameField.getFont());
        gridbag2.setConstraints(buttonF0, c2);
        dirPanel.add(buttonF0);

        buttonF2 = new CFancyButton(null, "direction 2",
                          IGUIConstants.DIRECTION_2_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),2);
                              }
                          });
        c2.gridx = 2;
        c2.gridy = 1;
        buttonF2.setBorderPainted(false);
        buttonF2.setFocusable(false);
        gridbag2.setConstraints(buttonF2, c2);
        dirPanel.add(buttonF2);

        buttonF5 = new CFancyButton(null, "direction 5",
                          IGUIConstants.DIRECTION_5_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),5);
                              }
                          });
        c2.gridx = 0;
        c2.gridy = 2;
        buttonF5.setBorderPainted(false);
        buttonF5.setFocusable(false);
        gridbag2.setConstraints(buttonF5, c2);
        dirPanel.add(buttonF5);

        buttonF4 = new CFancyButton(null, "direction 4",
                          IGUIConstants.DIRECTION_4_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),4);
                              }
                          });
        c2.gridx = 1;
        c2.gridy = 2;
        buttonF4.setBorderPainted(false);
        buttonF4.setFocusable(false);
        gridbag2.setConstraints(buttonF4, c2);
        dirPanel.add(buttonF4);

        buttonF3 = new CFancyButton(null, "direction 3",
                          IGUIConstants.DIRECTION_3_ICON,
                          new ActionListener() {
                              public void actionPerformed(ActionEvent event) {
                              applyDirectionChanges(m_view.getMapTileSelection(),3);
                              }
                          });
        c2.gridx = 2;
        c2.gridy = 2;
        buttonF3.setBorderPainted(false);
        buttonF3.setFocusable(false);
        gridbag2.setConstraints(buttonF3, c2);
        dirPanel.add(buttonF3);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth=1;
        c.gridheight=5;
        gridbag.setConstraints(dirPanel, c);
        archPanel.add(dirPanel);
    }

    private void applyDirectionChanges(ArchObject arch, int dir)
    {
      String new_text = "", text = new String(archEdit.getText());
      int len = text.length();

      // exlude all possible previous direction command
      for(int i=0, s=0;i<len;i++) {
        if(text.charAt(i) == 0x0a) {
          if(i-s >0) {
            if(text.regionMatches(s,"direction", 0, 9)==false)
                new_text += text.substring(s,i)+"\n";
           }
          s=i+1;
        }
      }

      // add our direction
      // direction 0 is a special case: the server default value
      // of a new arch is direction 0. So, a server defarch has direction 0
      // even without a direction command in the default arch.
      if(dir ==0 && arch.getDefaultArch().getDirection() == 0)
        archEdit.setText(new_text);
      else
        archEdit.setText(new_text+"direction "+dir+"\n");
      applyArchPanelChanges(arch);
      SetMapArchPanelObject(arch);
    }

    /**
     * Open an attribute dialog window for the currently selected arch
     * @param arch     currently selected arch
     */
    private void openAttrDialog(ArchObject arch) {
        m_control.openAttrDialog(arch);
    }

    /**
     * set up the text panel entry of the lower window
     */
    private void setupTextPanel() {
        textPanel = new JPanel();   // new panel

        archTextArea.setText("");
        archTextArea.setForeground(Color.blue);

        // create ScrollPane for text scrolling
        JScrollPane sta = new JScrollPane(archTextArea);
        sta.setBorder(new EtchedBorder());
        sta.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sta.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //textPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        textPanel.add(sta);
    }

    /**
     * set up the script panel tab
     */
    private void setupScriptPanel() {
        scriptPanel = new JPanel();   // new panel
        scriptPanel.setLayout(new BoxLayout(scriptPanel, BoxLayout.X_AXIS));

        eventList = new JList();
        m_control.setPlainFont(eventList);

        // create ScrollPane for jlist scrolling
        JScrollPane ssa = new JScrollPane(eventList);
        ssa.setBorder(new EtchedBorder());
        ssa.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        ssa.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ssa.setPreferredSize(new Dimension(80, 40));

        // create buttons
        JPanel grid = new JPanel(new GridLayout(2, 2));
        s_new = new JButton("Create New");
        s_new.setMargin(new Insets(3, 3, 3, 3));
        s_new.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewScriptWanted();
            }
        });
        grid.add(s_new);

        s_path = new JButton("Edit Data");
        s_path.setMargin(new Insets(3, 3, 3, 3));
        s_path.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editScriptWanted(CMapArchPanel.SCRIPT_EDIT_PATH);
            }
        });
        grid.add(s_path);

        s_modify = new JButton("Edit Script");
        s_modify.setMargin(new Insets(3, 3, 3, 3));
        s_modify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editScriptWanted(CMapArchPanel.SCRIPT_OPEN);
            }
        });
        grid.add(s_modify);

        s_remove = new JButton("Remove Script");
        s_remove.setMargin(new Insets(3, 3, 3, 3));
        s_remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editScriptWanted(CMapArchPanel.SCRIPT_REMOVE);
            }
        });
        grid.add(s_remove);

        // disable all the buttons in the beginning
        s_new.setEnabled(false);
        s_modify.setEnabled(false);
        s_path.setEnabled(false);
        s_remove.setEnabled(false);

        grid.setPreferredSize(new Dimension((int)(Math.max(s_modify.getMinimumSize().getWidth()*2,
                                                           s_new.getMinimumSize().getWidth()*2)), 40));

        scriptPanel.add(ssa);
        scriptPanel.add(grid);
        scriptPanel.setPreferredSize(new Dimension(100, 40));
    }

    void appExitNotify() {
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        settings.setProperty( MAPARCHPANEL_LOCATION_KEY, ""+
            m_splitPane.getDividerLocation() );

    }

    void refresh() {
        SetMapArchPanelObject(m_view.getMapTileSelection());
        repaint();
    }

    /**
     * update the map arch panel to display the custom font
     * @param do_refresh   if true, the window is redrawn after setting the fonts
     */
    public void updateFont(boolean do_refresh) {
        m_control.setPlainFont(archTextArea);
        m_control.setPlainFont(archNameField);

        m_control.setBoldFont(m_panelDesktop);

        m_control.setPlainFont(archMapPos);
        archMapPos.setForeground(Color.black);
        m_control.setPlainFont(archTypeText);
        archTypeText.setForeground(Color.black);
        m_control.setPlainFont(archFaceText);
        archFaceText.setForeground(Color.black);

        m_control.setBoldFont(submitChange);
        m_control.setBoldFont(invChange);
        m_control.setBoldFont(attrWin);

        // doc needs to be set!

        // refresh if desired
        if (do_refresh) refresh();
    }

    /**
     * If an arch is selected, the MapArchPanels
     * (bottom right windows) get updated.
     *
     * @param active_arch      the selected arch
     */
    void SetMapArchPanelObject(ArchObject active_arch)
    {
        ArchObject arch;   // reference to the displayed archobject
        boolean hasMessage = false; // true when arch has a message text
        int i;             // tmp. variable

        // reset panel
        archNameField.setText("");
        archFaceText.setText("Image:");
        archTypeText.setText("Type:");
        archTextArea.setText("");
        archMapPos.setText("Status: ");

        // reset list
        archEdit.setEnabled(false);
        archEdit.setText("");
        Color coltmp = archPanel.getBackground();
        buttonF0.setBackground(coltmp);
        buttonF1.setBackground(coltmp);
        buttonF2.setBackground(coltmp);
        buttonF3.setBackground(coltmp);
        buttonF4.setBackground(coltmp);
        buttonF5.setBackground(coltmp);
        buttonF6.setBackground(coltmp);
        buttonF7.setBackground(coltmp);
        buttonF8.setBackground(coltmp);

        if (active_arch == null) {
            // an empty space has been selected - reset panel to empty state
            if (eventList != null && eventList.getModel() != null && eventList.getModel().getSize() > 0)
                eventList.setModel(new DefaultListModel()); // clear script event list

            if (s_new != null && s_remove != null) {
                s_new.setEnabled(false);
                s_modify.setEnabled(false);
                s_path.setEnabled(false);
                s_remove.setEnabled(false);
            }
            m_panelDesktop.setForegroundAt(0,java.awt.Color.black);
            m_panelDesktop.setForegroundAt(1,java.awt.Color.black);
            m_panelDesktop.setForegroundAt(2,java.awt.Color.black);
            m_panelDesktop.setForegroundAt(3,java.awt.Color.black);

            invChange.setEnabled(false);
            attrWin.setEnabled(false);
            submitChange.setEnabled(false);
            buttonF0.setEnabled(false);
            buttonF1.setEnabled(false);
            buttonF2.setEnabled(false);
            buttonF3.setEnabled(false);
            buttonF4.setEnabled(false);
            buttonF5.setEnabled(false);
            buttonF6.setEnabled(false);
            buttonF7.setEnabled(false);
            buttonF8.setEnabled(false);
            return;
        }
        // If the active arch is part of a multi, the mutli-head's stats
         // are displayed (Only the head can store information!).
         if (active_arch.getRefFlag() && active_arch.getMapMultiHead() != null)
             arch = active_arch.getMapMultiHead();
         else
             arch = active_arch;

        m_panelDesktop.setForegroundAt(0,java.awt.Color.blue);
        invChange.setEnabled(true);
        attrWin.setEnabled(true);
        submitChange.setEnabled(true);

        // atm we handle direction 0 not here
        if(arch.getHasDir())
        {
          int tmp = arch.getDirection();
          buttonF0.setEnabled(true);
          buttonF1.setEnabled(true);
          buttonF2.setEnabled(true);
          buttonF3.setEnabled(true);
          buttonF4.setEnabled(true);
          buttonF5.setEnabled(true);
          buttonF6.setEnabled(true);
          buttonF7.setEnabled(true);
          buttonF8.setEnabled(true);
          if(tmp==0)
            buttonF0.setBackground(java.awt.Color.orange);
          else if(tmp==1)
            buttonF1.setBackground(java.awt.Color.orange);
          else if(tmp==2)
            buttonF2.setBackground(java.awt.Color.orange);
          else if(tmp==3)
            buttonF3.setBackground(java.awt.Color.orange);
          else if(tmp==4)
            buttonF4.setBackground(java.awt.Color.orange);
          else if(tmp==5)
            buttonF5.setBackground(java.awt.Color.orange);
          else if(tmp==6)
            buttonF6.setBackground(java.awt.Color.orange);
          else if(tmp==7)
            buttonF7.setBackground(java.awt.Color.orange);
          else if(tmp==8)
            buttonF8.setBackground(java.awt.Color.orange);


        }
        else
        {
          buttonF0.setEnabled(false);
          buttonF1.setEnabled(false);
          buttonF2.setEnabled(false);
          buttonF3.setEnabled(false);
          buttonF4.setEnabled(false);
          buttonF5.setEnabled(false);
          buttonF6.setEnabled(false);
          buttonF7.setEnabled(false);
          buttonF8.setEnabled(false);
        }

        ArchObject defarch = m_control.getArch(arch.getNodeNr());

        // no text, we try to set the default text
        if(arch.getMsgText() == null && arch.getNodeNr() != -1) {
            archTextArea.setForeground(Color.black);
            if(defarch.getMsgText() == null)
                archTextArea.setText("");
            else {
                archTextArea.setText(defarch.getMsgText());
                hasMessage = true;
            }
        }
        else {
            archTextArea.setForeground(Color.blue);
            archTextArea.setText(arch.getMsgText());
            hasMessage = true;
        }
        archTextArea.setCaretPosition(0);
        if(hasMessage)
          m_panelDesktop.setForegroundAt(1,java.awt.Color.blue);
        // end msg text

        if(arch.getAnimName() != null || defarch.getAnimName() != null)
           m_panelDesktop.setForegroundAt(3,java.awt.Color.blue);

        // *** OBJECT NAME ***
        if(arch.getObjName() == null && arch.getNodeNr()!= -1)
        {
            archNameField.setForeground (Color.black);
            if(m_control.getArch(arch.getNodeNr()).getObjName() == null) {
                // arch name
                if (arch.getArchName() != null)
                    archNameField.setText(arch.getArchName());
                else
                    archNameField.setText("");
            }
            else {
                // default name
                archNameField.setText(defarch.getObjName());
            }
        }
        else {
            // object name ("special")
            archNameField.setForeground (Color.blue);
            archNameField.setText(arch.getObjName());
        } // end ObjName

/*
        if(arch.getAnimName()!=null)

        else if(arch.getAnimText()!=null)
          archNameField.setText(arch.getAnimText());
          else
            archNameField.setText(m_control.getArch(arch.getNodeNr()).getAnimText());
*/
        // set hint for "specials": scripts/inventory/message
        String specialText = "";
        String typeText = "";
        String faceText = "";
        String animText = "";
        ArchObject cont;
        if (arch.isScripted())
        {
          m_panelDesktop.setForegroundAt(2,java.awt.Color.blue);
          specialText += "(script)";
        }
        if (hasMessage)
            specialText += "(msg)";
        if ((i = arch.countInvObjects()) > 0)
            specialText += " (inv: "+i+")";
        if ((cont = arch.getContainer()) != null)
        {
          if(defarch.getObjName() == null)
          {
            if (cont.getArchName() != null)
              specialText += " (env: " + cont.getArchName() + ")";
            else
              specialText += " (env: ><)";
          }
          else
            specialText += " (env: " + defarch.getObjName() + ")";
        }

        archMapPos.setText("Status: in node "+arch.getMapX()+", "+arch.getMapY() +" "+specialText);

        if (arch.getNodeNr() != -1) {
            typeText += "Type: "+m_control.archObjectParser.getArchTypeName(arch.getArchTypNr())
                                 +" ("+arch.getArchTypNr()+") ["+arch.getArchName()+"]";
        }
        else
            typeText += "Type: <unknown>";

        // check for multi tile
        if(arch.getRefCount()>0 || arch.getRefFlag()) {
            // multi: print size
            typeText+=" ["+(arch.getRefMaxX()-arch.getRefMaxMX()+1)
                                 +"x"+(arch.getRefMaxY()-arch.getRefMaxMY()+1)+"]";
        }
        else {
            // single
            typeText +=" [single]";
        }

        archTypeText.setText(typeText);

        if(arch.getFaceObjName() == null)
        {
          faceText = "Image: >no face<";
        }
        else
        {
          int desc = arch.getFaceObjDesc();
          faceText = "Image: "+arch.getFaceObjName()+" (";
          if(desc == -1)
            faceText += "face not found!)";
          else if(desc == 3)
            faceText +="face)";
          else if(desc == 4)
            faceText +="defarch face)";
          else if(desc == 1)
            faceText +="anim)";
          else if(desc == 2)
            faceText +="defarch anim)";
        }
        archFaceText.setText(faceText);


        animText = "Animation: ";
        if(arch.getAnimName() != null)
        {
          animText +=arch.getAnimName();
          if(arch.getAnimNr() == -1)
          {
            m_panelDesktop.setForegroundAt(3,java.awt.Color.red);
            animText += " (** unknown animation **)";
          }
        }
        else if(defarch.getAnimName() != null)
        {
          animText +=defarch.getAnimName();
          if(defarch.getAnimNr() == -1)
          {
            m_panelDesktop.setForegroundAt(3,java.awt.Color.red);
            animText += " (** unknown animation **)";
          }
        }
        archAnimText.setText(animText);

        // drawing the arch's attributes in the text field (archEdit) at the bottom right
        archEdit.setEnabled(true);
        m_CurrentAttributes = archEdit.getStyle(StyleContext.DEFAULT_STYLE);
        try {
            if (m_control.getPlainFont() != null) {
                StyleConstants.setFontFamily(m_CurrentAttributes, m_control.getPlainFont().getFamily());
                StyleConstants.setFontSize(m_CurrentAttributes, m_control.getPlainFont().getSize());
            }

            // blue: the "special" attributes, differ from the default archetype
            StyleConstants.setForeground(m_CurrentAttributes, Color.blue);
            if(arch.getArchText() != null)
                doc.insertString(doc.getLength(), arch.getArchText(), m_CurrentAttributes);

            // doc.insertString(doc.getLength(), "ID#"+arch.getMyID()+ " inv#: "+arch.countInvObjects()+"\n", m_CurrentAttributes);

            // black: the attributes from the default archetype
            //        that don't exist among the "special" ones
            StyleConstants.setForeground(m_CurrentAttributes, Color.black);
            if(arch.getArchText() != null && m_control.getArchObjectStack().getArch(arch.getNodeNr()) != null)
                doc.insertString(doc.getLength(), arch.diffArchText(m_control.getArchObjectStack().getArch(arch.getNodeNr()).getArchText(), true), m_CurrentAttributes);
        } catch (BadLocationException e) {

        }
        archEdit.setCaretPosition(0);

        // ------ script panel ------
        if (arch.isScripted()) {
            eventList.removeAll();            // clear event list
            arch.addEventsToJList(eventList); // update JList to display all events

            s_modify.setEnabled(true);
            s_path.setEnabled(true);
            s_remove.setEnabled(true);
        }
        else if (eventList.getModel() != null && eventList.getModel().getSize() > 0) {
            eventList.setModel(new DefaultListModel()); // clear event list

            s_modify.setEnabled(false);
            s_path.setEnabled(false);
            s_remove.setEnabled(false);
        }
        s_new.setEnabled(true);
    }

    /**
     * This method is invoked when the user pressed the "edit script"/"path"/"remove"
     * button from the script panel. If there is a valid selection in the event list,
     * the appropriate action for this script is triggered.
     */
    public void editScriptWanted(int task) {
        ArchObject arch = m_control.getMainView().getMapTileSelection(); // get selected arch
        if (arch != null && arch.isMulti() && arch.getMapMultiHead() != null)
            arch = arch.getMapMultiHead(); // if it's a multi, always take the head

        // check for a valid selection in the event list
        if (arch != null && eventList.getModel() != null && eventList.getModel().getSize() > 0
            && eventList.getSelectedIndex() >= 0) {
            // there
            int index = eventList.getSelectedIndex();
            if (index >= 0) {
                if(arch.modifyEventScript(index, task, eventList, this)) {
					m_control.m_currentMap.setLevelChangedFlag();  // the map has been modified
					updateMapTileList();
				}
            }
        }
    }

    /**
     * This method is invoked when the user pressed the "new script" button.
     */
    public void addNewScriptWanted() {
        ArchObject arch = m_control.getMainView().getMapTileSelection(); // get selected arch
        if (arch != null) {
            if (arch.isMulti() && arch.getMapMultiHead() != null)
                arch = arch.getMapMultiHead(); // if it's a multi, always add scripts to head
            if(arch.addEventScript(eventList, this)) {
				m_control.m_currentMap.setLevelChangedFlag();  // the map has been modified
				updateMapTileList();
			}
        }
    }

    /**
     * Set enable/disable states for the four buttons in the script panel
     * @param newButton
     * @param modifyButton
     * @param pathButton
     * @param removeButton
     */
    public void setScriptPanelButtonState(boolean newButton, boolean modifyButton,
                                          boolean pathButton, boolean removeButton) {
        s_new.setEnabled(newButton);
        s_modify.setEnabled(modifyButton);
        s_path.setEnabled(pathButton);
        s_remove.setEnabled(removeButton);
    }

    /**
     * Splitpane class that keeps its size even upon L'n'F change.
     */
    public class CSplitPane extends JSplitPane
    {
        public CSplitPane( int newOrientation,
            Component newLeftComponent,
            Component newRightComponent )
        {
            super(newOrientation, newLeftComponent, newRightComponent );
        }

        /**
         * Overridden to store and restore the divider location upon
         * UI change.
         */
        public void updateUI()
        {
            int dividerLocation = getDividerLocation();
            int dividerSize     = getDividerSize();
            super.updateUI();
            setDividerLocation( dividerLocation );
            setDividerSize( dividerSize );
        }
    }
}
