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

/**
 * This dialog manages the replace action
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ReplaceDialog extends JDialog {
    // matching criteria
    public static final int MATCH_ARCH_NAME = 0;
    public static final int MATCH_OBJ_NAME = 1;

    private static ReplaceDialog instance = null;

    CMainControl m_control;
    private boolean isBuilt;

    private ArchObject replaceArch; // objects will be replaced by this arch

    private JLabel rfHeading;
    private JLabel rfArchName;
    private JLabel iconLabel;
    private JLabel colonLabel;
    private JComboBox replaceCriteria;
    private JComboBox replaceWithBox;
    private JComboBox replaceEntireBox;
    private JTextField replaceInput1;
    private OkButtonAL okButtonListener;

    /**
     * Construct instance
     * @param cm
     */
    private ReplaceDialog(CMainControl cm) {
        super(cm.getMainView(), "Replace", false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        m_control = cm;
        isBuilt = false;
        rfArchName = null;
        replaceArch = null;
    }

    public static void init(CMainControl cm) {
        if (instance == null) {
            instance = new ReplaceDialog(cm);
        }
    }

    /**
     * @return true when this frame has been fully built
     */
    public static boolean isBuilt() {
        return (instance != null && instance.isBuilt);
    }

    public static ReplaceDialog getInstance() {
        return instance;
    }

    /**
     * Replace objects on the map
     *
     * @param m_ctrl   MapControl of the active map where the action was invoked
     */
    public void display(CMapControl m_ctrl) {
        replaceArch = m_control.getArchPanelSelection(); // selected arch

        if (!isBuilt) {
            JPanel main_panel = new JPanel();
            main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
            main_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

            // first line: heading
            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel labelon = new JLabel("On");
            labelon.setForeground(Color.black);
            line.add(labelon);
            line.add(Box.createVerticalStrut(3));
            replaceEntireBox = new JComboBox(new String[] {"entire map", "selected squares of"});
            replaceEntireBox.setBackground(Color.white);
            if (m_ctrl.m_view.isHighlight())
                replaceEntireBox.setSelectedIndex(1);
            else
                replaceEntireBox.setSelectedIndex(0);
            line.add(replaceEntireBox);
            line.add(Box.createVerticalStrut(3));
            rfHeading = new JLabel("\""+m_ctrl.getMapFileName()+"\":");
            rfHeading.setForeground(Color.black);
            line.add(rfHeading);
            main_panel.add(line);

            // second line: replace what?
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label1 = new JLabel("delete objects with");
            line.add(label1);
            line.add(Box.createVerticalStrut(5));

            replaceCriteria = new JComboBox(new String[] {"default arch", "name"});
            replaceCriteria.setSelectedIndex(0);
            replaceCriteria.setBackground(Color.white);
            line.add(replaceCriteria);
            line.add(Box.createVerticalStrut(5));

            replaceInput1 = new JTextField(20);
            line.add(replaceInput1);
            main_panel.add(line);

            // third line: replace by?
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label2 = new JLabel("and replace by");
            line.add(label2);
            line.add(Box.createVerticalStrut(5));
            replaceWithBox = new JComboBox(new String[] {"object", "nothing"});
            if (replaceArch == null)
                replaceWithBox.setSelectedIndex(1);
            else
                replaceWithBox.setSelectedIndex(0);
            replaceWithBox.setBackground(Color.white);
            replaceWithBox.addItemListener(new ReplaceWithBoxAL(this, replaceWithBox));
            line.add(replaceWithBox);

            iconLabel = new JLabel();
            if (replaceArch != null) {
                colonLabel = new JLabel(":");
                iconLabel.setIcon(m_control.getArchObjectStack().getFace(replaceArch.getObjectFaceNr()));
                rfArchName = new JLabel(" "+replaceArch.getBestName(replaceArch.getDefaultArch()));
            }
            else {
                colonLabel = new JLabel("");
                rfArchName = new JLabel("");
            }
            line.add(colonLabel);
            line.add(Box.createVerticalStrut(5));
            rfArchName.setForeground(Color.black);
            m_control.setPlainFont(rfArchName);
            line.add(iconLabel);
            line.add(rfArchName);
            main_panel.add(line);

            // button panel:
            main_panel.add(Box.createVerticalStrut(10));
            line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okButton = new JButton("OK");
            okButtonListener = new OkButtonAL(this, m_ctrl);
            okButton.addActionListener(okButtonListener);

            line.add(okButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setVisible(false);
                }
            });
            line.add(cancelButton);
            main_panel.add(line);

            getContentPane().add(main_panel);
            pack();
            setLocationRelativeTo(m_control.getMainView());
            setVisible(true);
            isBuilt = true;
        }
        else {
            // just set fields and show
            rfHeading.setText("\""+m_ctrl.getMapFileName()+"\":");
            replaceInput1.setText("");

            okButtonListener.setMapControl(m_ctrl);
            if (replaceArch == null) {
                replaceWithBox.setSelectedIndex(1);
                iconLabel.setIcon(null);
                rfArchName.setText("");
                colonLabel.setText("");
            }
            else {
                replaceWithBox.setSelectedIndex(0);
                iconLabel.setIcon(m_control.getArchObjectStack().getFace(replaceArch.getObjectFaceNr()));
                rfArchName.setText(" "+replaceArch.getBestName(replaceArch.getDefaultArch()));
                colonLabel.setText(":");
            }

            if (m_ctrl.m_view.isHighlight())
                replaceEntireBox.setSelectedIndex(1); // selected squares
            else
                replaceEntireBox.setSelectedIndex(0); // entire map

            pack();
            toFront();
            setVisible(true);
        }
    }

    /**
     * Update which arch is displayed as replace object.
     *
     * @param newArch      the new 'replaceArch' to be shown and stored
     * @param alwaysPack   if false, the frame is packed only when icon size changed
     *                     if true, the frame is always packed (packing resizes but also causes flicker)
     */
    public void updateArchSelection(ArchObject newArch, boolean alwaysPack) {
        if (isShowing() && replaceWithBox.getSelectedIndex() == 0) {
            replaceArch = newArch;
            if (newArch != null) {
                Icon oldIcon = iconLabel.getIcon();

                iconLabel.setIcon(m_control.getArchObjectStack().getFace(newArch.getObjectFaceNr()));
                rfArchName.setText(" "+newArch.getBestName(newArch.getDefaultArch()));
                colonLabel.setText(":");

                // pack frame only if height of icon changed
                if (alwaysPack || (oldIcon==null && iconLabel.getIcon()!=null) || (oldIcon!=null && iconLabel.getIcon()==null) ||
                    (oldIcon != iconLabel.getIcon() && oldIcon.getIconHeight() != iconLabel.getIcon().getIconHeight()))
                    pack();
            }
            else {
                replaceWithBox.setSelectedIndex(1);
                iconLabel.setIcon(null);
                rfArchName.setText("");
                colonLabel.setText("");
            }
        }
    }

    /**
     * This method performs the actual replace action on a map.
     *
     * @param mc              MapControl of the map where the action was invoked
     * @param matchCriteria   matching criteria for replace
     * @param matchString     this is what to search for
     * @param entireMap       if true, the entire map is affected - if false, only highlighted area
     * @param deleteOnly      if true matching arches get only deleted and not replaced
     * @return number of arches that have been replaced
     */
    private int doReplace(CMapControl mc, int matchCriteria, String matchString, boolean entireMap, boolean deleteOnly) {
        int posx, posy;     // Index
        ArchObject node;    // tmp. arch reference
        ArchObject prevArch, nextArch;
        int replaceCount = 0; // count how many arches have been replaced

        if (mc == null) return 0;

        // define area of effect
        Point startp, offset;
        if (entireMap) {
            startp = new Point(0, 0);
            offset = new Point(mc.getMapWidth()-1, mc.getMapHeight()-1);
        }
        else {
            startp = mc.m_view.getHighlightStart();  // start of highlighted rect
            offset = mc.m_view.getHighlightOffset(); // offset of rect from startp
        }

        // convert negative 'offset' into positive by flipping 'startp'
        if (offset.x < 0) {
            startp.x += offset.x;
            offset.x = Math.abs(offset.x);
        }
        if (offset.y < 0) {
            startp.y += offset.y;
            offset.y = Math.abs(offset.y);
        }

        // cycle through all tile coordinates between startpoint and offset:
        for (posx = startp.x; posx-startp.x <= offset.x; posx++) {
            for (posy = startp.y; posy-startp.y <= offset.y; posy++) {

                for (node = mc.getMapGrid()[posx][posy]; node != null;) {
                    if ((!node.isMulti() || node.getRefCount() > 0) &&
                        ((matchCriteria == MATCH_ARCH_NAME && node.getArchName() != null &&
                        node.getArchName().equalsIgnoreCase(matchString)) ||
                        (matchCriteria == MATCH_OBJ_NAME &&
                        node.getBestName(node.getDefaultArch()).equalsIgnoreCase(matchString))) ) {
                        // first, delete the old arch
                        nextArch = node.getNextArch();
                        prevArch = node.getPrevArch();
                        mc.deleteMapArch(node.getMyID(), posx, posy, false, false);

                        if (replaceArch != null && !deleteOnly) {
                            // insert replacement object
                            if (replaceArch.isMulti()) {
                                // multi's cannot be inserted properly, so we just put them ontop
                                if (replaceArch.getRefCount() == 0 && replaceArch.getMapMultiHead() != null)
                                    replaceArch = replaceArch.getMapMultiHead();
                                mc.addArchToMap(replaceArch.getNodeNr(), posx, posy, 0, false);

                                // TODO: if from pickmap it could have special attributes -> copy them
                            }
                            else
                                mc.insertArchToMap(replaceArch, 0, prevArch, posx, posy, false);
                        }
                        replaceCount++;
                        node = nextArch;
                    }
                    else
                        node = node.getNextArch();
                }
            }
        }

        // now the map and toolbars must be redrawn
        mc.repaint();
        m_control.getMainView().RefreshMapTileList();
        return replaceCount;
    }

    // ----------------------- SUBCLASSES -----------------------

    /**
     * Item-listener for the "replace with"-selection box
     */
    private class ReplaceWithBoxAL implements ItemListener {
        ReplaceDialog frame;    // the entire frame
        int lastSelectedIndex;
        JComboBox box;

        /**
         * Constructor
         * @param frame        the dialog mainframe
         */
        public ReplaceWithBoxAL(ReplaceDialog frame, JComboBox box) {
            this.frame = frame;
            this.box = box;
            lastSelectedIndex = box.getSelectedIndex();
        }

        /**
         * a (new) type has been selected in the box
         * @param event    the occured <code>ItemEvent</code> (button pressed)
         */
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED &&
                lastSelectedIndex != box.getSelectedIndex()) {
                if (box.getSelectedIndex() == 0) {
                    // replace with arch
                    replaceArch = m_control.getArchPanelSelection(); // selected arch
                    updateArchSelection(replaceArch, true);
                }
                else if (box.getSelectedIndex() == 1) {
                    // replace with nothing
                    iconLabel.setIcon(null);
                    rfArchName.setText("");
                    colonLabel.setText("");
                    pack();
                }
                lastSelectedIndex = box.getSelectedIndex();
            }
        }
    }

    /**
     * Listener class for the ok-button of the replace dialog
     */
    public class OkButtonAL implements ActionListener {
        CMapControl mc;          // map control
        ReplaceDialog frame;    // the entire frame

        public OkButtonAL(ReplaceDialog frame, CMapControl mc) {
            this.frame = frame;
            this.mc = mc;
        }

        public void setMapControl(CMapControl mc) {
            this.mc = mc;
        }

        public void actionPerformed(ActionEvent event) {
            int replaceCount; // how many arches have been replaced
            int matchCriteria = 0;
            String matchString = replaceInput1.getText().trim();
            boolean deleteOnly = (frame.replaceWithBox.getSelectedIndex() == 1);
            boolean entireMap = (replaceEntireBox.getSelectedIndex() == 0 ? true : false);

            if (mc == null || mc.isClosing()) {
                frame.setVisible(false);
                m_control.showMessage("Replace", "Map \""+mc.getMapFileName()+"\" is no longer available.\n", JOptionPane.ERROR_MESSAGE);
            }
            else if (!entireMap && !mc.m_view.isHighlight()) {
                // user selected "replace highlighted" but nothing is highlighted
                m_control.showMessage("Replace", "You chose to replace on selected squares of\n"+
                                      "map \""+mc.getMapFileName()+"\", but there is no selected area.", JOptionPane.ERROR_MESSAGE);
            }
            else {
                if (replaceCriteria.getSelectedIndex() == 0)
                    matchCriteria = ReplaceDialog.MATCH_ARCH_NAME;
                else if (replaceCriteria.getSelectedIndex() == 1)
                    matchCriteria = ReplaceDialog.MATCH_OBJ_NAME;

                replaceCount = frame.doReplace(mc, matchCriteria, matchString, entireMap, deleteOnly);
                if (replaceCount > 0) {
                    frame.setVisible(false);
                    if (replaceCount == 1)
                        m_control.showMessage("Replace", "1 object has been replaced.");
                    else
                        m_control.showMessage("Replace", replaceCount+" objects have been replaced.");
                }
                else {
                    m_control.showMessage("Replace", "No matching object found.", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
