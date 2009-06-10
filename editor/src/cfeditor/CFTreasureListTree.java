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

import javax.swing.tree.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.Font;

/**
 * The CFTreasureListTree class fully manages the Crossfire treausrelists.
 * CF datafile "treasures" gets parsed into a JTree structure.
 *
 * @author Andreas Vogl
 * @version
 */
public class CFTreasureListTree extends JTree {
    private static final int UNSET = -1; // unset values
    public static final String NONE_SYM = "<none>"; // string displayed in attribute dialog for "none"

    private static CFTreasureListTree instance; // static instance of this object

    // root-node
    private static DefaultMutableTreeNode root = new DefaultMutableTreeNode("Treasurelists:");

    // hashtable containing references to all treasurelists (tree nodes)
    private static Hashtable treasureTable = null;

    // hashtable temporarily containing names of special treasurelists (like gods' lists)
    // as keys and folder-nodes where to put these treasurelists as values
    private Hashtable specialTreasureLists = null;

    // all syntax-errors encountered during datafile-parsing get written in this log
    private StringBuffer errorLog = null;

    private Vector needSecondLink;
    private boolean processSecondLinking;

    // dialog window containing the tree
    private JDialog frame;

    // the cell renderer
    private TreasureCellRenderer renderer;

    // buttons in the dialog window
    private JButton okButton;
    private JButton noneButton;
    private JButton cancelButton;

    // the textfield in the attribute dialog where the result gets written to
    private JTextField input;           // input textfield
    private CAttribDialog parentDialog; // parent attr. dialog window (can be null)

    private int tListCount;   // number of treasurelists
    private boolean isEmpty;  // is this tree empty?
    private boolean hasBeenDisplayed; // true when the dialog has been displayed at least once

    /**
     * Construct an empty instance of this object. This method is private,
     * so it is impossible to create non-static instances of this class.
     */
    private CFTreasureListTree() {
        super(root);
        isEmpty = true; // three is empty
        hasBeenDisplayed = false;
        tListCount = 0;
        errorLog = new StringBuffer("");
        parentDialog = null;

        // draw thin blue lines connecting the nodes
        putClientProperty("JTree.lineStyle", "Angled");

        // font
        CMainControl.getInstance().setPlainFont(this);

        needSecondLink = new Vector();
        processSecondLinking = false;

        // set special cell renderer
        renderer = new TreasureCellRenderer();
        this.setCellRenderer(renderer);
    }

    /**
     * Initialize the static instance of this class. Only one instance
     * can be created. The datafile gets parsed here.
     */
    public static synchronized void init() {
        if (instance == null) {
            instance = new CFTreasureListTree();
            instance.initSpecialTreasureLists();
            instance.parseTreasures();
        }
    }

    /**
     * This method fills the 'specialTreasureLists' hashtable with the names
     * of all treasurelists which are special and belong into a special subfolder.
     * TODO: It would be nice to read this information from a simple datafile.
     */
    public synchronized void initSpecialTreasureLists() {
        specialTreasureLists = new Hashtable();

        TreasureTreeNode godFolder = new TreasureTreeNode("God Intervention", TreasureObj.FOLDER);
        root.add(godFolder);
        TreasureTreeNode dragonFolder = new TreasureTreeNode("Dragon Player Evolution", TreasureObj.FOLDER);
        root.add(dragonFolder);
        TreasureTreeNode playerFolder = new TreasureTreeNode("Player Creation", TreasureObj.FOLDER);
        root.add(playerFolder);

        specialTreasureLists.put("Valriel", godFolder);
        specialTreasureLists.put("Gorokh", godFolder);
        specialTreasureLists.put("Devourers", godFolder);
        specialTreasureLists.put("Sorig", godFolder);
        specialTreasureLists.put("Ruggilli", godFolder);
        specialTreasureLists.put("Gaea", godFolder);
        specialTreasureLists.put("Mostrai", godFolder);
        specialTreasureLists.put("Lythander", godFolder);
        specialTreasureLists.put("Gnarg", godFolder);
        specialTreasureLists.put("dragon_ability_fire", dragonFolder);
        specialTreasureLists.put("dragon_ability_cold", dragonFolder);
        specialTreasureLists.put("dragon_ability_elec", dragonFolder);
        specialTreasureLists.put("dragon_ability_poison", dragonFolder);
        specialTreasureLists.put("troll_player_items", playerFolder);
        specialTreasureLists.put("gnome_player_items", playerFolder);
        specialTreasureLists.put("dwarf_player_items", playerFolder);
        specialTreasureLists.put("half_orc_player_items", playerFolder);
        specialTreasureLists.put("elf_player_items", playerFolder);
        specialTreasureLists.put("wraith_player_items", playerFolder);
        specialTreasureLists.put("northman_player_items", playerFolder);
        specialTreasureLists.put("fireborn_player_items", playerFolder);
        specialTreasureLists.put("quetzalcoatl_player_items", playerFolder);
        specialTreasureLists.put("dragon_player_items", playerFolder);
    }

    /**
     * @return static instance of this tree
     */
    public static CFTreasureListTree getInstance() {
        return instance;
    }

    /**
     * Update font for the JTree nodes
     */
    public void updateFont() {
        renderer.updateFont();
    }

    /**
     * @return The parent attribute dialog attached to this dialog.
     * If no attribute dialog is attached, or this dialog is hidden,
     * null is returned.
     */
    public static CAttribDialog getParentDialog() {
        if (instance != null && instance.frame != null && instance.frame.isShowing())
            return instance.parentDialog;
        else
            return null;
    }

    /**
     * Hide the Treasurelists window, if not already hidden.
     */
    public static void hideDialog() {
        if (instance != null && instance.frame != null && instance.frame.isShowing())
            instance.frame.setVisible(false);
    }

    /**
     * Check if a certain treasurelist exists
     * @param name  Name of a treasurelist
     * @return      True when the treasurelists with the given name exists
     */
    public boolean containsTreasureList(String name) {
        return treasureTable.containsKey(name);
    }

    /**
     * Parse the treasure-data from the CF file "treasures.txt" into
     * this JTree instance.
     * This method must be called AFTER arch-loading is complete!
     *
     * @return True when parsing succeeded so that at least one treasurelist
     *         has been parsed. False when there is no data and tree remains empty.
     */
    private boolean parseTreasures() {
        CFileReader reader;    // resource file reader
        TreasureTreeNode node = null;    // tmp. treenode
        Vector tmpList = new Vector();   // tmp. container for all treasurelists
        Vector needLink = new Vector();  // all sub-treasurelist nodes that need linking
        treasureTable = new Hashtable(); // hashtable for all treasureTreeNodes

        int i=0;

        // first step: parsing datafile, adding all treasurelists to the tmpList vector
        try {
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder() : IGUIConstants.CONFIG_DIR);
            reader = new CFileReader(baseDir, IGUIConstants.TREASURES_FILE);
            String line = null; // read line of file

            // read the whole file line by line
            while ((line = reader.getReader().readLine()) != null) {
                line = line.trim();
                if (line.length()>0 && !line.startsWith("#")) {
                    // reading outside of treasurelist
                    if (line.startsWith("treasure") && (i=line.indexOf(" ")) != -1) {
                        // start of a new treasure section
                        node = new TreasureTreeNode(line.substring(i).trim(),
                               line.startsWith("treasureone") ? TreasureObj.TREASUREONE_LIST : TreasureObj.TREASURE_LIST);
                        tmpList.addElement(node); // put this node to tmplist vector
                        treasureTable.put(node.getTreasureObj().getName(), node); // put it into hashtable
                        tListCount++;

                        // read this treasurelist till the very end
                        readInsideList(node, reader.getReader(), needLink);
                    }
                    else {
                        errorLog.append("After treasurelist "+node.getTreasureObj().getName()
                                        +": unexpected line:\n\""+line+"\"\n");
                    }
                }
            }

            reader.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }

        // second step: sort alphabetically and link sub-treasurelist entries
        if (tListCount > 0) {
            sortVector(tmpList);

            // Loop through all treasureone lists and calculate the real ratio
            // of chances (Summed up to be 100%). Also attach lists to tree model.
            TreasureTreeNode realNode = null;
            for (i=0; i<tmpList.size(); i++) {
                realNode = (TreasureTreeNode)tmpList.elementAt(i);
                if (realNode.getTreasureObj().getType() == TreasureObj.TREASUREONE_LIST)
                    recalculateChances(realNode);

                // check for special treasurelists, which are put in subfolders
                if (specialTreasureLists.containsKey(realNode.getTreasureObj().getName()))
                    ((TreasureTreeNode)(specialTreasureLists.get(realNode.getTreasureObj().getName()))).add(realNode);
                else
                    root.add(realNode); // normal treasurelist - attach to root node
            }

            // link the sub-lists first time
            realNode = null;
            for (i=0; i<needLink.size(); i++) {
                // 'node' is a sub-treasurelist which needs to be linked to it's content
                node = (TreasureTreeNode)needLink.elementAt(i);

                // 'realNode' is the real instance of that treasurelist
                realNode = (TreasureTreeNode)treasureTable.get(node.getTreasureObj().getName());

                if (realNode != null) {
                    // set accurate type of treausrelist (one/multi)
                    node.getTreasureObj().setType(realNode.getTreasureObj().getType());

                    for (Enumeration clist = realNode.children(); clist != null & clist.hasMoreElements();)
                        node.add(((TreasureTreeNode)clist.nextElement()).getClone());
                }
            }

            // do second linking to link all what is left
            realNode = null;
            processSecondLinking = true;
            int x = needSecondLink.size();
            for (i=0; i<x; i++) {
                // 'node' is a sub-treasurelist which needs to be linked to it's content
                node = (TreasureTreeNode)needSecondLink.elementAt(i);

                // 'realNode' is the real instance of that treasurelist
                realNode = (TreasureTreeNode)treasureTable.get(node.getTreasureObj().getName());

                if (realNode != null) {
                    // set accurate type of treausrelist (one/multi)
                    node.getTreasureObj().setType(realNode.getTreasureObj().getType());

                    for (Enumeration clist = realNode.children(); clist != null & clist.hasMoreElements();)
                        node.add(((TreasureTreeNode)clist.nextElement()).getClone());
                }
            }
        }

        needSecondLink = null;
        if (errorLog.toString().trim().length() > 0) {
            System.out.println("Syntax errors in treasurelist file:");
            System.out.print(errorLog.toString());
        }
        System.out.println(tListCount+" treasurelists loaded.");

        // free unused memory
        specialTreasureLists = null;

        return false;
    }

    /**
     * Recalculate the chances of objects in a treasureone list.
     * The new chances always sum up to 100% total.
     * @param listNode node of the treasureone list
     */
    private void recalculateChances(TreasureTreeNode listNode) {
        int cnum = listNode.getSiblingCount()-1; // number of child-objects in the treasureone list
        int sumChances = 0; // sum of chances
        double corrector;   // corrector value
        TreasureObj content = null;

        // calculate the sum of all chances
        for (Enumeration clist = listNode.children(); clist != null && clist.hasMoreElements();) {
            content = ((TreasureTreeNode)clist.nextElement()).getTreasureObj();
            if (content.getChance() == UNSET) {
                content.setChance(100);
                sumChances += 100;
            }
            else
                sumChances += content.getChance();
        }

        corrector = 100./sumChances; // corrector value

        // now apply the correcting factor to all chances
        for (Enumeration clist = listNode.children(); clist != null && clist.hasMoreElements();) {
            content = ((TreasureTreeNode)clist.nextElement()).getTreasureObj();
            content.setChance((int)Math.round(content.getChance() * corrector));
        }
    }

    /**
     * Sort the TreasureObjects in the given vector in alphabetical order.
     * Names of the TreasureObjects get compared. The used sorting strategy
     * is bubblesort because the expected number of objects is low (~300).
     *
     * @param v     Vector to be sorted
     */
    private void sortVector(Vector v) {
        int i; int j;
        Object node = null;

        for (j=0; j<v.size()+1; j++) {
            for (i=0; i<v.size()-1; i++) {
                if (((TreasureTreeNode)v.elementAt(i)).getTreasureObj().getName().compareToIgnoreCase(
                    ((TreasureTreeNode)v.elementAt(i+1)).getTreasureObj().getName()) > 0) {
                    node = v.elementAt(i); v.setElementAt(v.elementAt(i+1), i);
                    v.setElementAt(node, i+1);
                }
            }
        }
    }

    /**
     * Read and parse the text inside a treasurelist
     * @param pnode      parent treenode
     * @param needLink   vector containing all sub-treasurelist nodes which need linking
     */
    private void readInsideList(TreasureTreeNode parentNode, BufferedReader reader, Vector needLink) throws IOException {
        String line; // read line of file
        TreasureTreeNode node = null; // tmp. treenode
        TreasureTreeNode subNode = null; // subnode for YES/NO case

        boolean insideArch = false;

        while ((line = reader.readLine()) != null && !(line = line.trim()).equals("end")) {
            if (line.length()>0 && !line.startsWith("#")) {
                if (!insideArch) {
                    // reading outside of an arch
                    if (line.startsWith("arch ")) {
                        node = new TreasureTreeNode(line.substring(line.indexOf(" ")).trim(), TreasureObj.ARCH);
                        parentNode.add(node);
                        insideArch = true;
                    }
                    else if (line.startsWith("list ")) {
                        String newName = line.substring(line.indexOf(" ")).trim(); // name of this contained list
                        node = new TreasureTreeNode(newName, TreasureObj.TREASURE_LIST);
                        parentNode.add(node);
                        needLink.add(node); // this node needs to be linked to it's content later

                        // check for potential infinite loops by lists containing itself
                        if (node.getTreasureObj().isTreasureList && parentNode.getTreasureObj().getName().equals(newName)) {
                            node.getTreasureObj().setHasLoop(true);
                            //errorLog.append("Treasurelist "+parentNode.getTreasureObj().getName()+" contains itself.\n");
                        }

                        insideArch = true;
                    }
                    else {
                        if (!IGUIConstants.isoView) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()
                                            +": unknown line:\n\""+line+"\"\n");
                        }
                    }
                }
                else {
                    // reading inside an arch-section
                    if (line.equals("more"))
                        insideArch = false;
                    else if (line.startsWith("chance")) {
                        try {
                            node.getTreasureObj().setChance(Integer.parseInt(line.substring(line.indexOf(" ")+1).trim()));
                        }
                        catch (NumberFormatException e) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()+": arch "
                                            + node.getTreasureObj().getName() +" chance is not a number.\n");
                        }
                    }
                    else if (line.startsWith("nrof")) {
                        try {
                            node.getTreasureObj().setNrof(Integer.parseInt(line.substring(line.indexOf(" ")+1).trim()));
                        }
                        catch (NumberFormatException e) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()+": arch "
                                            + node.getTreasureObj().getName() +" nrof value is not a number.\n");
                        }
                    }
                    else if (line.startsWith("magic")) {
                        try {
                            node.getTreasureObj().setMagic(Integer.parseInt(line.substring(line.indexOf(" ")+1).trim()));
                        }
                        catch (NumberFormatException e) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()+": arch "
                                            + node.getTreasureObj().getName() +" magic value is not a number.\n");
                        }
                    }
                    else if (line.equals("no")) {
                        // if fist arch not generated, process subtree
                        int chance = UNSET;
                        if (node.getTreasureObj().getChance() == UNSET) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()+": arch "
                                            + node.getTreasureObj().getName() +" has NO-list but chance is unset!\n");
                            chance = 0;
                        }
                        else
                            chance = 100-node.getTreasureObj().getChance();

                        subNode = new TreasureTreeNode("NO", TreasureObj.NO);
                        subNode.getTreasureObj().setChance(chance);
                        node.add(subNode);

                        // parse this subtree
                        readInsideList(subNode, reader, needLink);
                    }
                    else if (line.equals("yes")) {
                        // if fist arch not generated, process subtree
                        subNode = new TreasureTreeNode("YES", TreasureObj.YES);
                        subNode.getTreasureObj().setChance(node.getTreasureObj().getChance());
                        node.add(subNode);

                        // parse this subtree
                        readInsideList(subNode, reader, needLink);
                    }
                    else {
                        if (!IGUIConstants.isoView) {
                            errorLog.append("in list "+parentNode.getTreasureObj().getName()+", arch "
                                            + node.getTreasureObj().getName()+": unexpected line:\n\""+line+"\"\n");
                        }
                    }
                }
            }
        }
    }

    /**
     * Wrapper method for showing the dialog from the Resource menu.
     * In this case, no (parental) attribute dialog is attached.
     */
    public void showDialog() {
        showDialog(null, null);
    }

    /**
     * Show the Dialog window containing this tree. The user can select
     * a treasurelist which is returned as String.
     * The dialog window is built only ONCE, then hidden/shown as needed.
     * As a side-effect, only one treasurelist window can be open at a time.
     * When a second window is opened, the first one gets (re-)moved.
     *
     * @param listName    Name of the treasurelist to be displayed in expanded state.
     *                    If 'listName' matches no existing treasurelist,
     * @param parent      Parent frame (attribute dialog)
     */
    public synchronized void showDialog(JTextField input, CAttribDialog parent) {
        this.input = input;    // set textfield for input/output
        parentDialog = parent; // set parent attribute dialog (or null if there is none)

        if (!hasBeenDisplayed) {
            // open a popup dialog which tmporarily disables all other frames
            frame = new JDialog(CMainControl.getInstance().getMainView(), "Treasurelists", false);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // click on closebox hides dialog

            this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
            JScrollPane scrollPane = new JScrollPane(this);
            scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // split display: tree/buttons
            JPanel buttonPanel = buildButtonPanel();
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                  scrollPane, buttonPanel);
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerLocation((int)(frame.getHeight()-buttonPanel.getMinimumSize().height-4));

            splitPane.setDividerSize(4);
            splitPane.setResizeWeight(1);

            // show the splitted pane (tree and buttons)
            frame.getContentPane().add(splitPane);

            this.expandPath(new TreePath(root));
        }
        else {
            if (frame.isShowing()) frame.setVisible(false); // frame should be hidden at this point

            // collapse everything except root
            expandPath(new TreePath(root));
            for (int i = getRowCount()-1; i > 0; i--)
                collapseRow(i);
        }

        if (input == null) {
            // hide select/none buttons when there is no related attribute dialog
            okButton.setVisible(false);
            noneButton.setVisible(false);
        }
        else {
            okButton.setVisible(true);
            noneButton.setVisible(true);
        }

        // center dialog relative to parent window
        frame.setSize(470, 550);
        if (parent != null)
            frame.setLocationRelativeTo(parent);
        else
            frame.setLocationRelativeTo(CMainControl.getInstance().getMainView());

        if (input != null) {
            String listName = input.getText().trim(); // name of pre-selected list
            if (treasureTable.containsKey(listName)) {
                // set the position of the scrollbar
                DefaultMutableTreeNode[] nnode = new DefaultMutableTreeNode[2];
                nnode[0] = root;
                nnode[1] = (DefaultMutableTreeNode)treasureTable.get(listName);
                TreePath tpath = new TreePath(nnode);
                expandPath(tpath);        // expand pre-selected treasurelist
                setSelectionPath(tpath);  // select it too

                if (!hasBeenDisplayed) {
                    // If this is the first time, the frame has to be packed,
                    // otherwise no scrolling would be possible (see below).
                    frame.pack();
                    frame.setSize(470, 550);
                    setSelectionPath(tpath);
                }

                // scroll the tree to the pre-selected treasurelist
                scrollRowToVisible(this.getRowCount()-1);
                scrollPathToVisible(tpath);
                frame.setVisible(true); // display the window
            }
            else {
                // no valid treasurelist is preselected:
                scrollRowToVisible(0);  // show first row (root node)
                setSelectionPath(null); // nothing selected
                frame.setVisible(true);
            }
        }
        else {
            // no parent attribute dialog exists:
            scrollRowToVisible(0);  // show first row (root node)
            setSelectionPath(null); // nothing selected
            frame.setVisible(true);
        }

        hasBeenDisplayed = true; // in future, the dialog doesn't need to be rebuilt
    }

    /**
     * Building the button panel (bottom-line of the dialog window).
     * @return the JPanel containing all buttons
     */
    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout()); // main panel, containing everything

        JPanel leftSide = new JPanel();  // panel containing left-side buttons
        JPanel rightSide = new JPanel(); // panel containing right-side buttons

        // build right side buttons
        okButton = new JButton("Select");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // print the currently selected treasurelist into the attribute dialog
                    String result = getSelectedTreasureList();
                    if (result != null)
                        input.setText(" "+getSelectedTreasureList());
                    frame.setVisible(false);
                }
                catch (CGridderException ex) {
                    // user selected an invalid treasurelist - do nothing
                }
            }
        });
        rightSide.add(okButton);

        noneButton = new JButton("None");
        noneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // print "none" into the attribute dialog
                input.setText(" "+NONE_SYM);
                frame.setVisible(false);
            }
        });
        rightSide.add(noneButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
        rightSide.add(cancelButton);

        // left right side buttons
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // open the help window
                JFrame help = new CFHelp(CMainControl.getInstance().getMainView(), "treasurelists.html", false);
                help.setVisible(true); // show the window
            }
        });
        leftSide.add(helpButton);
        JButton testButton = new JButton("Test");
        leftSide.add(testButton);
        testButton.setEnabled(false); // disable test button until implemented

        // attach left/right sides
        buttonPanel.add(leftSide, BorderLayout.WEST);
        buttonPanel.add(rightSide, BorderLayout.EAST);
        return buttonPanel; // return the full panel
    }

    /**
     * @return The name of the currently selected treasurelist.
     * If nothing is selected, null is returned.
     * @throws CGridderException when user selected an invalid treasurelist (e.g. a god-list)
     */
    private String getSelectedTreasureList() throws CGridderException {
        // return null when nothing is selected
        if(isSelectionEmpty()) return null;

        DefaultMutableTreeNode parentNode = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getSelectionPath().getLastPathComponent();
        TreasureTreeNode trNode = null;

        // when the root node is selected, also return null
        if (node == root) return null;

        // climb up the treepath to the last node before root - this is the treasurelist
        while ((parentNode = (DefaultMutableTreeNode)(node.getParent())) != root) {
            node = parentNode;
        }

        // When a treasurelist inside a special-subfolder (like "God Intervention")
        // is selected, also return null because those must not be used on maps.
        trNode = (TreasureTreeNode)node;
        if (trNode.getTreasureObj().getType() == TreasureObj.FOLDER) {
            CMainControl.getInstance().showMessage("Invalid Selection",
                            "The "+trNode.getTreasureObj().getName()+" treasurelists must not be used in maps.\n"+
                            "These lists are reserved for internal use in the Crossfire server.");
            throw new CGridderException("invalid selection");
        }

        return trNode.getTreasureObj().getName();
    }

    // ======================== SUBCLASSES ==========================

    /**
     * Subclass: Nodes in the CFTreasureListTree.
     * Each node contains a TreauserObj as content.
     */
    private class TreasureTreeNode extends DefaultMutableTreeNode {
        private TreasureObj content; // content object

        /**
         * Construct tree node with specified content object
         */
        public TreasureTreeNode(TreasureObj content) {
            super();
            this.content = content;
        }

        /**
         * Construct tree node and content object
         *
         * @param name    name of content object
         * @param type    type of content object (see TreasureObj constants)
         */
        public TreasureTreeNode(String name, int type) {
            super();
            this.content = new TreasureObj(name, type);
        }

        /**
         * @return a new cloned instance of this object
         */
        public TreasureTreeNode getClone() {
            // clone this object
            TreasureTreeNode clone = new TreasureTreeNode(this.getTreasureObj());
            Enumeration clist; // enumeration to loop through children nodes

            // also clone all children nodes and link them properly
            for (clist = children(); clist != null && clist.hasMoreElements();) {
                clone.add(((TreasureTreeNode)clist.nextElement()).getClone());
            }

            // if this is a list without children it will need second linking
            if (!processSecondLinking && getTreasureObj().isTreasureList() &&
                !getTreasureObj().getName().equalsIgnoreCase("NONE") &&
                !getTreasureObj().hasLoop()) {
                // this is a list, let's see if there are children
                if (children() == null || !children().hasMoreElements()) {
                    // no children-nodes of any kind, needs second linking
                    needSecondLink.add(clone);
                }
                else {
                    // has children, but any *real* children, or just YES/NO?
                    boolean has_children = false; // true when this list has real children
                    TreasureObj content;          // tmp. storage for children's TreasureObj

                    for (clist = children(); clist != null && clist.hasMoreElements() && !has_children;) {
                        // check for real children, other than YES/NO objects
                        content = ((TreasureTreeNode)clist.nextElement()).getTreasureObj();
                        if (content.getType() != TreasureObj.YES && content.getType() != TreasureObj.NO)
                            has_children = true; // found a real child
                    }

                    if (!has_children) {
                        // this is a list with nothing but YES/NO in it, needs linking
                        needSecondLink.add(clone);
                    }
                }
            }

            return clone;
        }

        public String toString() {
            return content.toString();
        }

        public TreasureObj getTreasureObj() {return content;}
    }

    /**
     * Subclass: UserObject (= content object) for nodes in the CFTreasureListTree
     * These can be either treasurelists (containers), arches, or yes/no containers.
     */
    private class TreasureObj extends Object {
        public static final int TREASURE_LIST = 0;
        public static final int TREASUREONE_LIST = 1;
        public static final int ARCH = 2;
        public static final int YES = 3;
        public static final int NO = 4;
        public static final int FOLDER = 5;

        private int type; // type must be one of the above

        private String name; // name of this list/arch
        private boolean isTreasureList = false; // true when either treasure or treasureone
        private int chance;           // chance value from datafile
        private int relativeChance;   // relative chance (total is always 100% per list)
        private int nrof;             // maximum number of generated items
        private int magic;            // maximum magic bonus?

        private boolean hasLoop;      // true when this is a list containing itself (-> infinit loop)

        /**
         * Constructor for treasurelist objects
         * @param name
         * @param isTreasureList
         * @param isMultiList
         */
        public TreasureObj(String name, int type) {
            this.type = type;
            this.name = name;
            if (type == TREASURE_LIST || type == TREASUREONE_LIST)
                this.isTreasureList = true;
            chance = UNSET; nrof = UNSET; magic = UNSET;
            hasLoop = false;
        }

        /**
         * @return String representation of this treasure object. This is
         * what gets displayed on the tree.
         */
        public String toString() {
            return (nrof == UNSET ? "" : nrof+" ") + name + (type==TREASUREONE_LIST ? " [one]" : "")
                   + (magic == UNSET ? "" : " +"+magic) + (chance == UNSET ? "" : " ("+chance+" %)");
        }

        // --- GET/SET methods: ---
        public void setChance(int value) {chance = value;}
        public int getChance() {return chance;}
        public void setMagic(int value) {magic = value;}
        public int getMagic() {return magic;}
        public void setType(int value) {type = value;}
        public int getType() {return type;}
        public void setNrof(int value) {nrof = value;}

        public boolean hasLoop() {return hasLoop;}
        public void setHasLoop(boolean state) {hasLoop = state;}

        public boolean isTreasureList() {return isTreasureList;}
        public String getName() {return name;}
    }

    /**
     * This cell renderer is responsible for drawing the treasure-object
     * cells in the JTree.
     */
    public class TreasureCellRenderer extends DefaultTreeCellRenderer {
        ImageIcon tlistIcon;    // icon for treasurelists
        ImageIcon tlistOneIcon; // icon for treasure-one-lists
        ImageIcon yesIcon;      // icon for "YES" objects
        ImageIcon noIcon;       // icon for "NO" objects
        ImageIcon noface;       // icon for faceless arches
        ImageIcon noarch;       // icon for unknown arches
        Font plain;             // plain (editor-)font
        Font bold;              // bold (editor-)font

        /**
         * Constructor: Load icons and initialize fonts
         */
        public TreasureCellRenderer() {
            plain = CMainControl.getInstance().getPlainFont();
            if (plain == null)
                plain = JFontChooser.default_font;
            bold = new Font(plain.getName(), Font.BOLD, plain.getSize());

            // get icons
            tlistIcon = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_TREASURE);
            tlistOneIcon = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_TREASUREONE);
            yesIcon = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_TR_YES);
            noIcon = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_TR_NO);
            noface = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_NOFACE);
            noarch = cfeditor.CGUIUtils.getSysIcon(IGUIConstants.TILE_NOARCH);
        }

        /**
         * Set current font provided by MainControl
         */
        public void updateFont() {
            plain = CMainControl.getInstance().getPlainFont();
            bold = new Font(plain.getName(), Font.BOLD, plain.getSize());
        }

        /**
         * The cell-drawing method.
         */
        public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // first use the standard renderer to paint it all
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

            setFont(plain);
            if (node.isRoot()) {
                this.setForeground(java.awt.Color.gray);
                setIcon(null);
            }
            else {
                // node content object of this cell
                TreasureObj content = ((TreasureTreeNode)value).getTreasureObj();

                // now apply customized icons
                if (content.getType() == TreasureObj.YES) {
                    //setIcon(tutorialIcon);
                    setForeground(java.awt.Color.gray);
                    setIcon(yesIcon);
                }
                else if (content.getType() == TreasureObj.NO) {
                    //setIcon(tutorialIcon);
                    setForeground(java.awt.Color.gray);
                    setIcon(noIcon);
                }
                else if (content.getType() == TreasureObj.TREASURE_LIST) {
                    if (content.getName().equalsIgnoreCase("none")) {
                        setIcon(null);
                        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                    }
                    else {
                        setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                        setIcon(tlistIcon);
                    }
                }
                else if (content.getType() == TreasureObj.TREASUREONE_LIST) {
                    if (content.getName().equalsIgnoreCase("none")) {
                        setIcon(null);
                        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                    }
                    else {
                        setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                        setIcon(tlistOneIcon);
                    }
                }
                else if (content.getType() != TreasureObj.FOLDER) {
                    // normal arch: display the face icon
                    Integer num = (Integer)(CMainControl.getInstance().getArchObjectStack().getArchHashTable().get(content.getName()));
                    if (num != null && ArchObjectStack.getLoadStatus() == ArchObjectStack.IS_COMPLETE) {
                        ArchObject arch = CMainControl.getInstance().getArch(num.intValue());
                        if (arch != null) {
                            if (num.intValue() != -1 && !arch.getFaceObjectFlag())
                                setIcon(CMainControl.getInstance().getFace(arch.getObjectFaceNr()));
                            else
                                setIcon(noface);
                        }
                        else
                            setIcon(noarch);
                    }
                    else
                        setIcon(noarch);
                    // small empty border to keep icons seperated:
                    setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                }

                // set bold/plain font style
                if (node.getParent() == root) {
                    if (content.getType() != TreasureObj.FOLDER)
                        setFont(bold);
                }
                else {
                    // parent is not root
                    TreasureTreeNode parent = (TreasureTreeNode)(node.getParent());
                    if (parent.getTreasureObj().getType() == TreasureObj.FOLDER)
                        setFont(bold);
                }
            }

            return this;
        }
    }

}