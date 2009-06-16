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
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * CAttribDialog poses the GUI for CF object attributes
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CAttribDialog extends JDialog {
    public static int width = 395;
    public static int height = 505;
    public static int button_bar_height = 40;
    public static int inventory_width = 100;

    public static final int divider_size = 3;

    // store width of input-textfields, then JChooseBoxes are set to this width too
    public static int textFieldWidth = 0;
    public static final int textFieldColumns = 18; // number of columns for textfields

    // pixel height of all JChooseBoxes
    public static int chooseBoxHeight = 25;

    // the center pane of the dialog can either show the attribute
    // input-interface or the summary of nonzero attributes
    // the 'display_summary' flag indicates what is shown: true->summary, false->interface
    private boolean display_summary = false;

    CMainControl m_control;          // reference to the main control
    private CFArchTypeList typelist; // reference to the list of CF type-data

    private DialogAttrib attr_head;  // head of the list of attribute-GUI components
    private DialogAttrib attr_tail;  // head of the list of attribute-GUI components

    private JComboBox typesel;       // selection box for type
    private JTextField nameTF;       // textfield for arch name
    private JTextField defarchTF;    // textfield for name of default arch
    private JLabel image_panel;      // panel for object's face (png)

    private ArchObject arch;
    private ArchObject defarch;

    private CFArchType type;    // reference to the type data
    private int type_nr;        // the type nr. to be applied to the object
                                // this differs from the ArchObject if the type is undefined
    private int list_nr;        // the position of this type in the type list

    // buttons:
    private JButton help_button;
    private JButton summary_button;
    private JButton ok_button;
    private JButton apply_button;
    private JButton cancel_button;

    // central tabbed pane (the place where all the attribute tabs are)
    private JTabbedPane tabbedPane;

    // central pane, this is the parent component of above tabbed pane
    private JScrollPane pane_center;

    // text pane where the summary is displayed
    private JTextPane summaryTP;

    /**
     * Constructor: Creates the GUI layout and
     * draws the dialog window.
     *
     * @param at_list      the list of CF type-data
     * @param aobj         the ArchObject to be displayed by this dialog
     * @param defaobj      the default ArchObject of 'arch'
     * @param mcntrl       main control
     */
    CAttribDialog(CFArchTypeList at_list, ArchObject aobj, ArchObject defaobj,
                  CMainControl mcntrl) {
        super(mcntrl.getMainView(), "CF Attribute Dialog", false);

        // when close-box is selected, execute the 'closeDialog' method and nothing else
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                closeDialog();
            }
        });

        boolean type_valid = false;  // true if the type is valid
        attr_head = null;
        list_nr=0;

        m_control = mcntrl;          // reference to the main control
        typelist = at_list;          // refernce to the typelist

        CAttribDialog.setDefaultBounds();          // set width/height etc

        // refernce to the ArchObject
        if (aobj.isMulti() && aobj.getMapMultiHead() != null) {
            arch = aobj.getMapMultiHead();
            defarch = m_control.getArchObjectStack().getArch(arch.getNodeNr());
        }
        else {
            arch = aobj;
            defarch = defaobj;     // refernce to the default ArchObject
        }

        // check if the type of the object is present in the definitions
        CFArchType tmp = typelist.getHead().getNext();
        //type_nr = 0;   // for invalid types, we take "type 0"
        type = tmp;
        boolean type_found = false;
        for (int i=0; tmp != null && !type_found; tmp = tmp.getNext(), i++) {
            if (tmp.getTypeNr() == arch.getArchTypNr()) {
                if (tmp.getTypeAttr() == null) {
                    // no type-attributes, so we only compared type-numbers
                    type_found = true;
                }
                else {
                    // check if all the type-attributes match
                    int args_num = (int)(tmp.getTypeAttr().length/2.);
                    boolean match = true;
                    String archvalue;

                    //System.out.println("# type: "+tmp.getTypeName());

                    for (int t=0; t<args_num*2; t+=2) {
                        archvalue = arch.getAttributeString(tmp.getTypeAttr()[t], defarch);

                        //System.out.println("  arch: '"+archvalue+"', type: '"+tmp.getTypeAttr()[t+1]+"'");

                        if (!archvalue.equals(tmp.getTypeAttr()[t+1]) &&
                            !(tmp.getTypeAttr()[t+1].equals("0") && archvalue.length()==0)) {
                            match = false;
                            //System.out.println("-> attr: "+tmp.getTypeAttr()[t]+" NO match");
                        }
                        else {
                            //System.out.println("-> attr: "+tmp.getTypeAttr()[t]+" YES match");
                        }
                    }

                    // we've got a match after all
                    if (match) type_found = true;
                }

                // we found our type, now save all infos we need
                if (type_found) {
                    type_valid = true;  // this is an existing type
                    type = tmp;         // save reference to the type-data
                    type_nr = arch.getArchTypNr();  // CF type number
                    list_nr = i;        // save position in list
                }
            }
        }

        // get the type data
        //type = typelist.getType(type_nr);

        JPanel layout = new JPanel(new BorderLayout()); // face, name & type

        // first split top-left and -right
        JScrollPane pane_left = build_header();
        JScrollPane pane_right = build_inv();

        //Create a split pane with the two scroll panes in it.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              pane_left, pane_right);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(width-inventory_width-2*divider_size);
        splitPane.setDividerSize(divider_size+1);

        //Provide minimum sizes for the two components in the split pane
        pane_left.setMinimumSize(new Dimension(width-inventory_width-2*divider_size, 120));
        pane_right.setMinimumSize(new Dimension(inventory_width, 120));

        // Now split horizontally
        //JScrollPane pane_center = build_attr();
        pane_center = build_attr();

        //Create a split pane with the two scroll panes in it.
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                               splitPane, pane_center);
        splitPane2.setOneTouchExpandable(false);
        splitPane2.setDividerLocation(126);
        splitPane2.setDividerSize(divider_size);

        // split horizontally again
        JScrollPane pane_buttons = build_buttons();
        JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                               splitPane2, pane_buttons);
        splitPane3.setOneTouchExpandable(false);
        //splitPane3.setDividerLocation(height-button_bar_height-34);
        splitPane3.setDividerLocation((int)(this.getHeight()-pane_buttons.getMinimumSize().height-divider_size));
        splitPane3.setDividerSize(divider_size);
        splitPane3.setResizeWeight(1);

        getContentPane().add(splitPane3);

        // initialize the summary TextPane
        summaryTP = new JTextPane();
        summaryTP.setForeground(java.awt.Color.black);
        summaryTP.setBackground(CMainControl.getInstance().getMainView().getBackground());
        summaryTP.setEditable(false);
        summaryTP.setBorder(BorderFactory.createEmptyBorder(3, 15, 0, 0));

        this.setResizable(true);
        // now draw the whole thing:
        setBounds(m_control.getMainView().getX()+(m_control.getMainView().getWidth()-width)/2,
                  m_control.getMainView().getY()+(m_control.getMainView().getHeight()-height)/2 - 20,
                  width, height);
        setVisible(true);
    }

    /**
     * This method sets the default bounds for the dialog frame
     * in proportion to the size of the currently used font.
     * Not the perfect approach, but it's impossible to have swing do it
     * right automatically. Having the user define it would be a
     * lot more difficult, and probably not very convenient.
     */
    public static void setDefaultBounds() {
        int fontsize; // size of current font

        if (CMainControl.getInstance().getPlainFont() != null)
            fontsize = CMainControl.getInstance().getPlainFont().getSize();
        else
            fontsize = JFontChooser.default_font.getSize();

        if (fontsize <= 13) {
            // this is for fonts <= 13 (includes default font)
            width = 395;
            height = 505+30;
            button_bar_height = 40;
            inventory_width = 95;
            chooseBoxHeight = 23;
        }
        else {
            // this is for the somewhat bigger fonts (>= 14)
            width = 395+50;
            height = 505+100;
            button_bar_height = 40+5;
            inventory_width = 120;
            chooseBoxHeight = 25;
        }
    }

    /**
     * Construct the Combo box of the available arch-types
     * @param boxWidth   width of the JChooseBox in pixels
     * @return a <code>JPanel</code> with the combo box in it
     */
    private JPanel build_TypesBox(int boxWidth) {
        JPanel lineLayout = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // layout for this line
        String []namelist = new String[typelist.getLength()];  // list of typenames

        // read all type names
        CFArchType tmp = typelist.getHead().getNext();
        int selection = -1;   // position of selected type in the list
        for (int i=0; tmp != null; tmp = tmp.getNext(), i++) {
            namelist[i] = " "+tmp.getTypeName();
        }

        // the active type appears selected in the box
        selection = list_nr;

        lineLayout.add(new JLabel("Type: "));  // create label

        typesel = new JComboBox(namelist);     // set "content"
        typesel.setPreferredSize(new Dimension(boxWidth, chooseBoxHeight));
        typesel.setSelectedIndex(selection); // set active selection

        //typesel.setKeySelectionManager(new StringKeyManager(typesel));

        typesel.setBackground(java.awt.Color.white);  // white background
        typesel.setName("Types");

        // the listener:
        typesel.addItemListener(new TypesBoxAL(this, arch, defarch));

        lineLayout.add(typesel);
        return lineLayout;
    }

    /**
     * Construct the Combo box of the available spells
     *
     * @param attr     spell-attribute
     * @return the completed <code>JComboBox</code>
     */
    private JComboBox build_SpellBox(CFArchAttrib attr) {
        int active=0;   // active selection in the combo box

        // first parse the spell-number value from arch
        int spnum=arch.getAttributeValue(attr.getNameOld(), defarch); // spell number

        if (spnum < 0 || spnum >= typelist.getSpellNum().length-1)
            spnum = 0;  // undefined spellnumbers be zero

        // do we have "none" spell?
        if (spnum == 0 && (arch.getAttributeString(attr.getNameOld(), defarch).length() == 0
            || attr.getDataType() == CFArchAttrib.T_ZSPELL)) {
            active = 0;
        }
        else {
            // now look up the spell-number in the array of spells
            for (int i=0; i<typelist.getSpellNum().length; i++) {
                if (typelist.getSpellNum()[i] == spnum) {
                    active = i;                           // set selection
                    i = typelist.getSpellNum().length+10; // end loop
                }
            }
        }

        JComboBox spellsel = new JComboBox(typelist.getSpellName());  // set "content"
        spellsel.setPreferredSize(new Dimension((textFieldWidth==0 ? 197 : textFieldWidth), chooseBoxHeight));
        spellsel.setSelectedIndex(active); // set active selection
        spellsel.setMaximumRowCount(10);
        spellsel.setKeySelectionManager(new StringKeyManager(spellsel));

        spellsel.setBackground(java.awt.Color.white);  // white background
        spellsel.setName(attr.getNameNew());

        return spellsel;
    }

    /**
     * Construct the Combo box for arrays of "list data" (this is used for T_LIST)
     *
     * @param attr      list attribute
     * @paramlistData   Vector with list items and corresponding values
     * @return the completed <code>JComboBox</code>
     */
    private JComboBox build_ArrayBox(CFArchAttrib attr, Vector listData) {
        // build the array of list-items
        String []array = new String[(int)(listData.size()/2.)];
        boolean hasSelection = false;
        int active = arch.getAttributeValue(attr.getNameOld(), defarch);

        for (int i=0; i<array.length; i++) {
            array[i] = (String)(listData.elementAt(i*2 + 1)); // put string to array
            if (!hasSelection && ((Integer)(listData.elementAt(i*2))).intValue() == active) {
                hasSelection = true; // the selection is valid
                active = i;          // set selection to this index in the array
            }
        }
        // if there is no valid pre-selection, show first element of list
        if (!hasSelection) active = 0;

        JComboBox arraysel = new JComboBox(array);  // set "content"
        arraysel.setPreferredSize(new Dimension((textFieldWidth==0 ? 197 : textFieldWidth), chooseBoxHeight));
        arraysel.setSelectedIndex(active); // set active selection
        arraysel.setMaximumRowCount(10);
        arraysel.setKeySelectionManager(new StringKeyManager(arraysel));

        arraysel.setBackground(java.awt.Color.white);  // white background
        arraysel.setName(attr.getNameNew());

        return arraysel;
    }

    /**
     * Construct the Panel for bitmask values (this is used for T_BITMASK)
     *
     * @param attr        spell-attribute
     * @param gui_attr    the gui-instance of the bitmask attribute
     * @param bitmask     the bitmask data for this attribute
     * @param main_panel  the panel to put everything in
     * @return the completed <code>JPanel</code>
     */
    private void build_Bitmask(CFArchAttrib attr, BitmaskAttrib gui_attr, CAttribBitmask bitmask, JPanel main_panel) {
        // initialize bitmask value
        gui_attr.setValue(arch.getAttributeValue(attr.getNameOld(), defarch));
        gui_attr.bitmask = bitmask;

        // add button
        JButton button = new JButton(attr.getNameNew()+":");
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        button.setMargin(new Insets(0, 3, 0, 3));
        button.addActionListener(new MaskChangeAL(gui_attr, this));
        main_panel.add(button);

        // add the text field
        gui_attr.text = bitmask.getText(gui_attr.getValue(), null);
        main_panel.add(gui_attr.text);
    }

    /**
     * Construct the upper left part of the attribute dialog,
     * containing name, type, defarch name and face.
     *
     * @return a <code>JScrollPane</code> with the upper left part of the dialog window
     */
    private JScrollPane build_header() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));   // the final thing, in a panel

        JPanel layout1 = new JPanel(new BorderLayout()); // face, name & type

        image_panel = new JLabel(m_control.getArchObjectStack().getFace(arch.getObjectFaceNr()));
        image_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        layout1.add(image_panel, BorderLayout.WEST);

          JPanel layout2 = new JPanel(new GridLayout(2, 1));

            JPanel layout3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            layout3.add(new JLabel("Name: "));  // create label
            if (arch.getObjName() != null && arch.getObjName().length() > 0)
                nameTF = new JTextField(arch.getObjName(), 16);
            else if (defarch.getObjName() != null && defarch.getObjName().length() > 0)
                nameTF = new JTextField(defarch.getObjName(), 16);
            else
                nameTF = new JTextField(defarch.getArchName(), 16);
            nameTF.setEditable(false);
            layout3.add(nameTF);

          layout2.add(layout3);            // name field
          layout2.add(build_TypesBox(nameTF.getPreferredSize().width));   // build type-selection box
          layout2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        //layout1.add(image_panel, BorderLayout.WEST);
        layout1.add(layout2, BorderLayout.EAST);

        header.add(layout1);

          JPanel layout4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
          layout4.add(new JLabel("Default Arch: "));  // create label
          defarchTF = new JTextField(defarch.getArchName(), 16);
          defarchTF.setEditable(false);
          layout4.add(defarchTF);

        JPanel layout5 = new JPanel();
        layout5.add(layout4);
        header.add(layout5);
        header.setPreferredSize(new Dimension(width-inventory_width-2*divider_size, 70));

        // finally put the result into a (non-scrollable)
        // scrollpane to get the size right
        JScrollPane scrollPane = new JScrollPane(header);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        return(scrollPane);
    }

    /**
     * Construct the upper right part of the attribute dialog,
     * containing the object's inventory.
     * @return a <code>JScrollPane</code> with the upper right part of the dialog window
     */
    private JScrollPane build_inv() {
        JPanel inv = new JPanel();   // the final thing, in a panel
        inv.add(new JLabel("Inventory:"));  // create label

        JScrollPane scrollPane = new JScrollPane(inv);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setSize(80, 70);

        return(scrollPane);
    }

    /**
     * Construct the central part of the attribute dialog,
     * containing the object's arch attributes.
     *
     * @return When this method is called the first time: a <code>JScrollPane</code>
     *         with the central part of the dialog window.
     *         All further calls rebuild the existing tabbedpane and return null.
     */
    private JScrollPane build_attr() {
        boolean initial_call = false;

        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
            initial_call = true;
        }

        for (int i=0; i<type.getSectionNum(); i++) {
            Component panel1 = makeAttribPanel(i);
            if (panel1 != null) tabbedPane.addTab(getSectionName(i), null, panel1);
        }

        // set selected tab
        tabbedPane.setSelectedIndex(0);

        if (initial_call) {
            // if this is the first time call:
            // create a scrollpane and put the tabbedpane inside
            JScrollPane scrollPane = new JScrollPane(tabbedPane);
            scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setSize(width, height-70-button_bar_height);

            return(scrollPane);
        }
        else
            return null;
    }

    /**
     * This method creates an attribute panel for one section
     * of attributes. If the section is empty, null is returned.
     *
     * @param sec_id     the identifier of the section
     * @return a <code>Component</code> containing the attribute panel
     */
    private Component makeAttribPanel(int sec_id) {
        int number = 0;   // number of attributes in this section
        boolean is_text = false;      // true if this section contains a textfield
        boolean has_bitmask = false;  // true if this section contains a bitmask attribute
        Component full_panel = null;  // return object: the full attrib. panel

        // first we check how many attribs this section has
        for (int i=0; type.getAttr().length > i; i++) {
            if (type.getAttr()[i].getSecId() == sec_id) {
                // count number of attributes
                if (type.getAttr()[i].getDataType() != CFArchAttrib.T_FIXED)
                    number++;
                // check for bitmask attributes
                if (!has_bitmask &&
                    (type.getAttr()[i].getDataType() == CFArchAttrib.T_BITMASK))
                    has_bitmask = true;
            }
        }
        if (number == 0) return null;

        // All attribute-"lines" go into this panel:
        // We choose the boxlayout only for tabs with bitmasks, because we
        // need it there. For all other cases, the gridlayout is better.
        JPanel panel = new JPanel();
        if (has_bitmask)
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        else
            panel.setLayout(new GridLayout(number, 1));

        // now add the entrys, line by line
        for (int i=0; type.getAttr().length > i; i++) {
            if (type.getAttr()[i].getSecId() == sec_id) {
                JPanel panel2 = null;  // tmp. Panel for the layout

                // now create the attribute-GUI-instance
                DialogAttrib new_attr=null;
                int d_type = type.getAttr()[i].getDataType(); // data type of the attribute

                if (d_type == CFArchAttrib.T_TEXT) {
                    // special case: we've got a text section
                    is_text = true; // text section (need special embedding without additional scrollbars)
                    new_attr = new TextAttrib();
                    new_attr.ref = type.getAttr()[i];
                    JTextArea input;

                    // note that the textarea is initialized with rows/columns: 1,1
                    // this is pretty weird, but seems the only way to achieve desired behaviour
                    if (type.getAttr()[i].getNameOld().equalsIgnoreCase("msg")) {
                        if (defarch.getMsgText() != null && defarch.getMsgText().length()>0
                            && (arch.getMsgText() == null || arch.getMsgText().trim().length()==0))
                            input = new JTextArea(defarch.getMsgText(), 1, 1);
                        else
                            input = new JTextArea(arch.getMsgText(), 1, 1);
                    }
                    else
                        input = new JTextArea(1, 1);

                    input.setBorder(BorderFactory.createEmptyBorder(3, 7, 0, 0));

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));

                    JScrollPane scrollPane = new JScrollPane(input);
                    scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

                    //Create a split pane with the two scroll panes in it.
                    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                          scrollPane, new_attr.help_button);
                    splitPane.setDividerSize(0);
                    splitPane.setResizeWeight(1);
                    splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

                    ((TextAttrib)new_attr).input = input;

                    full_panel = splitPane; // return the splitpane
                }

                if (d_type == CFArchAttrib.T_BOOL ||
                    d_type == CFArchAttrib.T_BOOL_SPEC) {
                    // create an attribute line for BOOL
                    panel2 = new JPanel(new BorderLayout()); // need this layout for consistent resize-behaviour
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    new_attr = new BoolAttrib();
                    JCheckBox input;
                    if (d_type == CFArchAttrib.T_BOOL) {
                        // normal bool
                        input = new JCheckBox(" "+type.getAttr()[i].getNameNew(),
                                              (arch.getAttributeValue(type.getAttr()[i].getNameOld(), defarch)==1)?true:false);
                    }
                    else {
                        // parse values for customized bool
                        String true_val = type.getAttr()[i].getMisc()[0];

                        if (true_val.equals("0")) {
                            String attr_string = arch.getAttributeString(type.getAttr()[i].getNameOld(), defarch);
                            input = new JCheckBox(" "+type.getAttr()[i].getNameNew(),
                                                  (attr_string.length()==0 || attr_string.equals("0"))?true:false);
                        }
                        else {
                            input = new JCheckBox(" "+type.getAttr()[i].getNameNew(), (arch.getAttributeString(
                                                  type.getAttr()[i].getNameOld(), defarch).equals(true_val))?true:false);
                        }
                    }

                    new_attr.help_button = new JButton("?");
                    new_attr.ref = type.getAttr()[i];
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));

                    panel3.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel3.add(new_attr.help_button);
                    // create the offset from help-button and checkbox:
                    panel3.add(Box.createHorizontalStrut((int)(width/3.3)));
                    panel3.add(input); // add checkbox
                    panel2.add(panel3, BorderLayout.WEST);

                    ((BoolAttrib)new_attr).input = input;
                }
                else if (d_type == CFArchAttrib.T_INT) {
                    // create an attribute line for INT
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    JLabel label = new JLabel(type.getAttr()[i].getNameNew()+": ");
                    label.setForeground(IGUIConstants.INT_COLOR);
                    panel3.add(label);   // add label

                    new_attr = new IntAttrib();
                    new_attr.ref = type.getAttr()[i];
                    JTextField input;

                    // parse value from arch
                    int fieldLength = (type.getAttr()[i].getInputLength()==0 ? textFieldColumns : type.getAttr()[i].getInputLength());

                    int attrval = arch.getAttributeValue(type.getAttr()[i].getNameOld(), defarch);
                    if (attrval != 0)
                        input = new JTextField(String.valueOf(attrval) , fieldLength);
                    else
                        input = new JTextField("" , fieldLength);
                    panel3.add(input);   // add textfield

                    // store width of textfield
                    if (textFieldWidth == 0 && type.getAttr()[i].getInputLength() == 0)
                        textFieldWidth = input.getPreferredSize().width;

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));
                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);
                    ((IntAttrib)new_attr).input = input;
                }
                else if (d_type == CFArchAttrib.T_STRING ||
                         d_type == CFArchAttrib.T_FLOAT) {
                    // create an attribute line for STRING
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    JLabel label = new JLabel(type.getAttr()[i].getNameNew()+": ");

                    if (d_type == CFArchAttrib.T_STRING) {
                        new_attr = new StringAttrib();
                        label.setForeground(Color.black);
                    }
                    else {
                        new_attr = new FloatAttrib();
                        label.setForeground(IGUIConstants.FLOAT_COLOR);
                    }

                    panel3.add(label);   // add label
                    new_attr.ref = type.getAttr()[i];
                    JTextField input;

                    // parse String from arch
                    String dtxt;
                    if (type.getAttr()[i].getNameOld().equalsIgnoreCase("name")) {
                        if (arch.getObjName() != null && arch.getObjName().length()>0)
                            dtxt = arch.getObjName();
                        else if (defarch.getObjName() != null && defarch.getObjName().length()>0)
                            dtxt = defarch.getObjName();
                        else
                            dtxt = defarch.getArchName();
                    }

                    else if (type.getAttr()[i].getNameOld().equalsIgnoreCase("face")) {
                        if (arch.getFaceRealName() != null && arch.getFaceRealName().length()>0)
                            dtxt = arch.getFaceRealName();
                        else
                            dtxt = defarch.getFaceRealName();
                    }

                    else
                        dtxt = arch.getAttributeString(type.getAttr()[i].getNameOld(), defarch);


                    input = new JTextField(dtxt, textFieldColumns);
                    panel3.add(input);   // add textfield

                    // store width of textfield
                    if (textFieldWidth == 0)
                        textFieldWidth = input.getPreferredSize().width;

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));

                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);

                    if (d_type == CFArchAttrib.T_STRING)
                        ((StringAttrib)new_attr).input = input;
                    else
                        ((FloatAttrib)new_attr).input = input;
                }
                else if (d_type == CFArchAttrib.T_SPELL ||
                         d_type == CFArchAttrib.T_ZSPELL ||
                         d_type == CFArchAttrib.T_LIST) {
                    // create an attribute line for a combo box entry
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    JLabel label = new JLabel(type.getAttr()[i].getNameNew()+": ");
                    label.setForeground(IGUIConstants.INT_COLOR);
                    panel3.add(label);   // add label

                    new_attr = new ListAttrib();
                    new_attr.ref = type.getAttr()[i];

                    // create ComboBox with parsed selection
                    JComboBox input = null;
                    if (d_type == CFArchAttrib.T_SPELL || d_type == CFArchAttrib.T_ZSPELL)
                        input = build_SpellBox(type.getAttr()[i]);
                    else if (d_type == CFArchAttrib.T_LIST) {
                        if (type.getAttr()[i].getMisc() != null && typelist.getListTable().containsKey((String)(type.getAttr()[i].getMisc()[0]))) {
                            // build the list from vector data
                            input = build_ArrayBox(type.getAttr()[i], (Vector)(typelist.getListTable().get(
                                                   (String)(type.getAttr()[i].getMisc()[0]))));
                        }
                        else {
                            // error: list data is missing or corrupt
                            panel3.add(new JLabel("Error: Undefined List"));
                            panel3.add(Box.createHorizontalStrut(50));
                        }
                    }

                    panel3.add(input);   // add spellbox

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));
                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);
                    ((ListAttrib)new_attr).input = input;
                }
                else if (d_type == CFArchAttrib.T_DBLLIST) {
                    // create an attribute line for a double list entry
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new BorderLayout());
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JPanel panel5 = new JPanel(new BorderLayout());

                    JLabel label = new JLabel(type.getAttr()[i].getNameNew()+": ");
                    label.setForeground(IGUIConstants.INT_COLOR);
					label.setVerticalAlignment(SwingConstants.TOP);
					panel3.add(label, BorderLayout.WEST);

                    new_attr = new DoubleListAttrib();
                    new_attr.ref = type.getAttr()[i];

                    // create ComboBox with parsed selection
                    JComboBox inputs[] = new JComboBox[2];
					if (type.getAttr()[i].getMisc() != null && typelist.getListTable().containsKey((String)(type.getAttr()[i].getMisc()[0])) && typelist.getListTable().containsKey((String)(type.getAttr()[i].getMisc()[1]))) {
						// Hack to set preselected if available
						int active = arch.getAttributeValue(type.getAttr()[i].getNameOld(), defarch);
						int[] activepart = {active & 0x0F, active & 0xF0};

						// build the lists from vector data
						for(int j=0; j<2; j++) {
							Vector listData = (Vector)(typelist.getListTable().get( (String)(type.getAttr()[i].getMisc()[j])));
							inputs[j] = build_ArrayBox(type.getAttr()[i], listData);

							for (int k=0; k<listData.size()/2.0; k++) {
								if (((Integer)(listData.elementAt(k*2))).intValue() == activepart[j]) {
									inputs[j].setSelectedIndex(k); // set active selection
									break;
								}
							}
						}
						panel5.add(inputs[0], BorderLayout.NORTH);
						panel5.add(inputs[1], BorderLayout.SOUTH);
//						panel3.add(Box.createVerticalStrut(panel5.getMinimumSize().height), BorderLayout.SOUTH);   // add label
						panel3.add(panel5, BorderLayout.EAST);
					} else {
						// error: list data is missing or corrupt
						panel3.add(new JLabel("Error: Undefined List"));
						panel3.add(Box.createHorizontalStrut(50));
					}

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));
                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);
                    ((DoubleListAttrib)new_attr).inputs = inputs;
                }
                else if (d_type == CFArchAttrib.T_BITMASK) {
                    // create an attribute entry for a bitmask
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    new_attr = new BitmaskAttrib();
                    new_attr.ref = type.getAttr()[i];

                    // create bitmask-entry in the gui
                    //JButton input = null;
                    CAttribBitmask bitmask = null;
                    if (type.getAttr()[i].getMisc() != null && typelist.getBitmaskTable().containsKey((String)(type.getAttr()[i].getMisc()[0]))) {
                        // fetch the bitmask data, then build the attribute panel
                        bitmask = (CAttribBitmask)(typelist.getBitmaskTable().get((String)(type.getAttr()[i].getMisc()[0])));
                        build_Bitmask(type.getAttr()[i], (BitmaskAttrib)new_attr, bitmask, panel3);
                    }
                    else {
                        // error: bitmask data is missing or corrupt
                        panel3.add(new JLabel("Error: Undefined Bitmask"));
                        panel3.add(Box.createHorizontalStrut(50));
                    }

                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));
                    new_attr.help_button.setAlignmentY(JButton.CENTER_ALIGNMENT);
                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);
                    //((BitmaskAttrib)new_attr).input = input;
                }
                else if (d_type == CFArchAttrib.T_TREASURE) {
                    // create an attribute entry for a treasurelist
                    panel2 = new JPanel(new BorderLayout());
                    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    // button to view treasurelist tree
                    JButton viewTTree = new JButton("treasurelist:");
                    viewTTree.setMargin(new Insets(0, 3, 0, 3));

                    new_attr = new StringAttrib();
                    new_attr.ref = type.getAttr()[i];

                    // textfield (no direct input, text is set by the treasurelist dialog)
                    JTextField input;
                    String treasureName = arch.getAttributeString(type.getAttr()[i].getNameOld(), defarch);
                    if (treasureName.trim().length() == 0 || treasureName.trim().equalsIgnoreCase("none"))
                        treasureName = CFTreasureListTree.NONE_SYM;
                    input = new JTextField(" "+treasureName, textFieldColumns);
                    input.setEditable(false);
                    viewTTree.addActionListener(new ViewTreasurelistAL((StringAttrib)new_attr, this));

                    panel3.add(viewTTree); // add button
                    panel3.add(input);     // add textfield
                    new_attr.help_button = new JButton("?");
                    new_attr.help_button.setMargin(new Insets(0, 3, 0, 3));

                    panel4.add(Box.createRigidArea(new Dimension(3 , 20)));
                    panel4.add(new_attr.help_button);

                    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    panel2.add(panel3, BorderLayout.EAST);
                    panel2.add(panel4, BorderLayout.WEST);

                    ((StringAttrib)new_attr).input = input;
                }


                if (new_attr != null) {
                    if (new_attr.help_button != null) {
                        // help button: popup Info Window when clicked
                        new_attr.help_button.addActionListener(
                            new HelpAL(new_attr.ref));
                    }

                    // now attach the new attribute into the list
                    if (attr_head == null)
                        attr_head = attr_tail = new_attr;
                    else {
                        attr_tail.next = new_attr;
                        attr_tail = attr_tail.next;
                    }

                    if (!is_text) {
                        panel2.setSize(new Dimension(width-70, 20));
                        panel.add(panel2);
                    }
                }
            }
        }

        if (!is_text) {
            // for non-text panels: put everything into a scrollpane
            JScrollPane scrollPane = new JScrollPane(panel);
            JScrollPane scrollPane2 = new JScrollPane(scrollPane);
            scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // don't show the border

            scrollPane2.setPreferredSize(new Dimension(20, 20));
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            full_panel = scrollPane2; // return this scrollpane
        }

        return full_panel; // return the full panel
    }

    /**
     * Looks up the section name from the ID
     *
     * @param sec_id      ID of the section
     * @return name of that section
     */
    private String getSectionName(int sec_id) {
        for (int i=0; type.getAttr().length > i; i++) {
            if (type.getAttr()[i].getSecId() == sec_id) {
                // we've got the string, now capitalize the first letter
                String s = type.getAttr()[i].getSecName();
                if (s.length() > 1) {
                    s = s.substring(0, 1).toUpperCase() + s.substring(1);
                }

                return s;
            }
        }

        return "???";
    }

    /**
     * Construct the lower part of the attribute dialog,
     * containing the buttons (help, default, okay, apply, cancel).
     * @return a <code>JScrollPane</code> with the lower part of the attribute dialog
     */
    private JScrollPane build_buttons() {
        JPanel total_bar = new JPanel(new BorderLayout()); // all buttons
        JPanel right_bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));  // right side
        JPanel left_bar = new JPanel(new FlowLayout(FlowLayout.LEFT));    // left side

        help_button = new JButton("Help");
        summary_button = new JButton("Summary");
        ok_button = new JButton("Ok");
        apply_button = new JButton("Apply");
        cancel_button = new JButton("Cancel");

        left_bar.add(help_button);
        left_bar.add(summary_button);
        right_bar.add(ok_button);
        right_bar.add(apply_button);
        right_bar.add(cancel_button);

        // put left and right side buttons in the total bar
        total_bar.add(left_bar, BorderLayout.WEST);
        total_bar.add(right_bar, BorderLayout.EAST);
        total_bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // listen for button events
        cancel_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // close frame
                closeDialog();
            }
        });

        apply_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // write settings into the ArchObject
                apply_settings();
                m_control.getMainView().RefreshMapTileList();
                m_control.m_currentMap.repaint();
              }
        });

        ok_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // write settings into the ArchObject, then exit
                if (apply_settings() == true) {
                  m_control.getMainView().RefreshMapTileList();
                  m_control.m_currentMap.repaint();
                    closeDialog();
                }
            }
        });

        help_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // create html-docu on the current type and display it
                JFrame ahelp = new CFHelp(m_control.getMainView(),
                                          type.createHtmlDocu(), CMainControl.getInstance().isBigFont());
                ahelp.setVisible(true);  // show the window
            }
        });

        summary_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // switch to show the summary of attributes
                toggleSummary();
            }
        });

        //summary_button.setEnabled(false);
        //help_button.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(total_bar);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        //scrollPane.setSize(width, button_bar_height);
        scrollPane.setMinimumSize(scrollPane.getPreferredSize());

        return(scrollPane);
    }

    /**
     * Spawns a popup-message to display the help text
     * of an attribute
     *
     * @param title     name of attribute
     * @param msg       message text
     */
    private void popup_help(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg,
                                      "help: "+title,
                                      JOptionPane.PLAIN_MESSAGE );
    }

    /**
     * Switch between the input-interface for all attributes
     * and the summary list of all nonzero attributes.
     */
    private void toggleSummary() {
        if (!display_summary) {
            // interface is displayed, switch to summary
            Document doc = summaryTP.getDocument();

            try {
                // clear document
                if (doc.getLength() > 0)
                    doc.remove(0, doc.getLength());

                Style docStyle = summaryTP.getStyle(StyleContext.DEFAULT_STYLE);
                if (m_control.getPlainFont() != null) {
                    StyleConstants.setFontFamily(docStyle, m_control.getPlainFont().getFamily());
                    StyleConstants.setFontSize(docStyle, m_control.getPlainFont().getSize());
                    //StyleConstants.setBold(docStyle, true);
                }
                StyleConstants.setForeground(docStyle, Color.black);

                // now loop through all attributes and write out nonzero ones
                for (DialogAttrib attr = attr_head; attr != null; attr = attr.next) {
                    if (attr.ref.getDataType() == CFArchAttrib.T_BOOL ||
                        attr.ref.getDataType() == CFArchAttrib.T_BOOL_SPEC) {
                        boolean value = ((BoolAttrib)attr).input.isSelected(); // true/false
                        if (value == true) {
                            doc.insertString(doc.getLength(), "<"+attr.ref.getNameNew()+">\n", docStyle);
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_INT) {
                        String value = ((IntAttrib)attr).input.getText(); // the attrib value
                        if (value != null && value.length()>0 && !value.equals("0")) {
                            doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                             value+"\n", docStyle);
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_FLOAT) {
                        String value = ((FloatAttrib)attr).input.getText(); // the attrib value
                        if (value != null && value.length()>0) {
                            try {
                                // parse float value
                                double dval = Double.parseDouble(value);
                                if (dval != 0) {
                                    doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                                     value+"\n", docStyle);
                                }
                            }
                            catch(NumberFormatException e) {}
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_STRING) {
                        String value = ((StringAttrib)attr).input.getText(); // the attrib value
                        if (value != null && value.length()>0) {
                            doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                             value+"\n", docStyle);
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_SPELL ||
                        attr.ref.getDataType() == CFArchAttrib.T_ZSPELL ||
                        attr.ref.getDataType() == CFArchAttrib.T_LIST) {
                        String value = ((ListAttrib)attr).input.getSelectedItem().toString().trim(); // the attrib value
                        if (value != null && value.length()>0 && !value.startsWith("<")) {
                            doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                             value+"\n", docStyle);
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_DBLLIST) {
                        String value1 = ((DoubleListAttrib)attr).inputs[0].getSelectedItem().toString().trim();
                        String value2 = ((DoubleListAttrib)attr).inputs[1].getSelectedItem().toString().trim();
						String out = null;
                        if (value1 != null && value1.length()>0 && !value1.startsWith("<"))
							out = value1;
                        if (value2 != null && value2.length()>0 && !value2.startsWith("<")) {
							if(out == null)
								out = value2;
							else
								out += " / " + value2;
						}
						if(out != null)
							doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+out+"\n", docStyle);
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_BITMASK) {
                        String value = ((BitmaskAttrib)attr).text.getText().trim();
                        if (value != null && value.length()>0 && !value.startsWith("<")) {
                            doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                             value+"\n", docStyle);
                        }
                    }
                    if (attr.ref.getDataType() == CFArchAttrib.T_TREASURE) {
                        String value = ((StringAttrib)attr).input.getText().trim(); // the attrib value
                        if (value != null && value.length()>0 && !value.equals(CFTreasureListTree.NONE_SYM)) {
                            doc.insertString(doc.getLength(), attr.ref.getNameNew()+" = "+
                                             value+"\n", docStyle);
                        }
                    }
                }
            }
            catch (BadLocationException e) {
                System.out.println("toggleSummary: Bad Location in Document!");
            }

            summaryTP.setCaretPosition(0); // this prevents the document from scrolling down
            pane_center.setViewportView(summaryTP);
            pane_center.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            summary_button.setText("Input");
            pane_center.validate();
            update(getGraphics());
            display_summary = true;
        }
        else {
            // summary is displayed, switch to interface
            pane_center.setViewportView(tabbedPane);
            pane_center.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            summary_button.setText("Summary");
            pane_center.validate();
            update(getGraphics());
            display_summary = false;
        }
    }

    /**
     * This method is called when the "apply"-button has been
     * pressed. All the settings from the dialog get
     * written into the ArchObject.
     *
     * @return true if the settings were applied, false if error occurred
     */
    private boolean apply_settings() {
        String old_ArchText = arch.getArchText();  // the old ArchText
        String new_ArchText = "";  // the new ArchText for the ArchObject
        String new_name = null;    // new object name
        String new_face = null;    // new face name
        String old_msg = arch.getMsgText(); // old arch msg
        String new_msg = null;     // new arch msg
        String errors = null;      // syntax errors in the archtext
        int d_type;
        CFArchType type_struct = typelist.getTypeOfArch(arch); // the type structure for this arch

        try {
            for (DialogAttrib attr = attr_head; attr != null; attr = attr.next) {
                d_type = attr.ref.getDataType(); // attribute's data type

                if (d_type == CFArchAttrib.T_BOOL) {
                    // a boolean attribute (flag)
                    if (((BoolAttrib)attr).input.isSelected() !=
                        (defarch.getAttributeValue(attr.ref.getNameOld(), null)==1)?true:false)
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+
                        (((BoolAttrib)attr).input.isSelected()?1:0)+"\n";
                }
                else if (d_type == CFArchAttrib.T_BOOL_SPEC) {
                    // a boolean attribute with customized true/false values
                    String val_string;  // value-string to apply
                    if (((BoolAttrib)attr).input.isSelected())
                        val_string = attr.ref.getMisc()[0]; // true string
                    else
                        val_string = attr.ref.getMisc()[1]; // false string
                    // now see if we need to write it into the archtext or not
                    if ((val_string.equals("0") && !(defarch.getAttributeString(attr.ref.getNameOld(), null).length()==0))
                        || (!val_string.equals("0") && !defarch.getAttributeString(attr.ref.getNameOld(), null).equals(val_string)))
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+val_string+"\n";
                }
                else if (d_type == CFArchAttrib.T_INT ||
                         d_type == CFArchAttrib.T_FLOAT) {
                    // an int attribute
                    if (d_type == CFArchAttrib.T_INT && ((IntAttrib)attr).input.getText().trim().length() == 0) {
                        if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != 0)
                          new_ArchText = new_ArchText + attr.ref.getNameOld() + " 0\n";
                    }
                    else if (d_type == CFArchAttrib.T_FLOAT && ((FloatAttrib)attr).input.getText().trim().length() == 0) {
                        if (defarch.getAttributeString(attr.ref.getNameOld(), null).length() > 0)
                            new_ArchText = new_ArchText+attr.ref.getNameOld()+" 0.0\n";
                    }
                    else {
                        try {
                            if (d_type == CFArchAttrib.T_INT) {
                                int value = 0;      // int value from the input form
                                // try to parse String to Int
                                value = Integer.parseInt(((IntAttrib)attr).input.getText().trim());
                                if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != value)
                                    new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+value+"\n";
                            }
                            else {
                                double value = 0;      // value from the input form
                                double def_value = 0;  // value from the default arch
                                // try to parse floating point
                                value = Double.parseDouble(((FloatAttrib)attr).input.getText().trim());
                                String defValueStr = defarch.getAttributeString(attr.ref.getNameOld(), null).trim();
                                if (defValueStr.length() > 0)
                                    def_value = Double.parseDouble(defValueStr);
                                if (value != def_value)
                                    new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+value+"\n";
                            }
                        }
                        catch (NumberFormatException e) {
                            // parsing failed: wrong entry!!
                            JOptionPane.showMessageDialog(this, "Attribute '"+attr.ref.getNameNew()+
                                                          "' must be a number!", "Input Error",
                                                          JOptionPane.ERROR_MESSAGE);
                            throw new CGridderException(""); // bail out
                        }
                    }
                }
                else if (d_type == CFArchAttrib.T_STRING) {
                    // a String attribute
                    String inline = ((StringAttrib)attr).input.getText().trim();

                    if (inline != null) {
                        if (attr.ref.getNameOld().equalsIgnoreCase("name")) {
                            // special case #1: "name"-textfield
                            if (defarch.getObjName() != null && defarch.getObjName().length()>0) {
                                if(!inline.equalsIgnoreCase(defarch.getObjName()))
                                    new_name = inline;
                                else
                                    new_name = "";
                            }
                            else if (!inline.equalsIgnoreCase(defarch.getArchName()))
                                new_name = inline;
                            else
                                new_name = "";
                        }
                        else if (attr.ref.getNameOld().equalsIgnoreCase("animation")) {
                          if (inline.length() >0 && !inline.equalsIgnoreCase(defarch.getAttributeString(attr.ref.getNameOld(), null)))
                          {
                            new_ArchText = new_ArchText + attr.ref.getNameOld() + " " + inline + "\n";
                            arch.setAnimName(inline);
                          }
                          else
                            arch.setAnimName(defarch.getAnimName());
                        }
                        else if (attr.ref.getNameOld().equalsIgnoreCase("face")) {
                            if(inline.length() >0)
                            {
                              // decide we have to add a "face <name>" string to the arch text
                              // Note, that the realFaceName itself is set below
                              if(defarch.getFaceRealName() == null || defarch.getFaceRealName().compareTo(inline.trim())!=0)
                                new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+inline+"\n";
                            }
                            new_face = inline;
                        }

                        else {
                            if (!inline.equalsIgnoreCase(defarch.getAttributeString(attr.ref.getNameOld(), null)))
                                new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+inline+"\n";
                        }
                    }
                }
                else if (d_type == CFArchAttrib.T_TEXT) {
                    // a String attribute
                    if (attr.ref.getNameOld().equalsIgnoreCase("msg") &&
                        ((TextAttrib)attr).input.getText().trim().length() > 0) {
                            new_msg = ((TextAttrib)attr).input.getText().trim();
                    }
                }
                else if (d_type == CFArchAttrib.T_SPELL || d_type == CFArchAttrib.T_ZSPELL ||
                         d_type == CFArchAttrib.T_LIST) {
                    // get attribute value that should go into the arch
                    int attr_val;  // attribute value
                    if (d_type == CFArchAttrib.T_SPELL || d_type == CFArchAttrib.T_ZSPELL)
                        attr_val = typelist.getSpellNum()[((ListAttrib)attr).input.getSelectedIndex()];
                    else {
                        // get selected index of ComboBox
                        attr_val = ((ListAttrib)attr).input.getSelectedIndex();
                        // fetch value according to this list entry:
                        attr_val = ((Integer)((Vector)(typelist.getListTable().get(attr.ref.getMisc()[0]))).elementAt(2*attr_val)).intValue();
                    }

                    if (attr_val == -1 || (attr_val == 0 && d_type != CFArchAttrib.T_SPELL
                                           && d_type != CFArchAttrib.T_ZSPELL)) {
                        if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != 0)
                            new_ArchText = new_ArchText+attr.ref.getNameOld()+" 0\n";
                    }
                    else if (attr_val == 0)
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" 0\n";
                    else if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != attr_val)
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+attr_val+"\n";
                }
                else if (d_type == CFArchAttrib.T_DBLLIST) {
                    int val1, val2, combined_val;

					val1 = ((DoubleListAttrib)attr).inputs[0].getSelectedIndex();
					val1 = ((Integer)((Vector)(typelist.getListTable().get(attr.ref.getMisc()[0]))).elementAt(2*val1)).intValue();
					val2 = ((DoubleListAttrib)attr).inputs[1].getSelectedIndex();
					val2 = ((Integer)((Vector)(typelist.getListTable().get(attr.ref.getMisc()[1]))).elementAt(2*val2)).intValue();
					combined_val = val1 + val2;

                    if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != combined_val)
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+combined_val+"\n";
                }
                else if (d_type == CFArchAttrib.T_BITMASK) {
                    // a bitmask attribute (similar to integer, but easier because no parsing needed)
                    int value = ((BitmaskAttrib)attr).getValue(); // get bitmask value

                    if (defarch.getAttributeValue(attr.ref.getNameOld(), null) != value)
                        new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+value+"\n";
                }
                else if (d_type == CFArchAttrib.T_TREASURE) {
                    // a treasurelist attribute
                    String inline = ((StringAttrib)attr).input.getText().trim(); // input string

                    if (inline != null) {
                        boolean isNone = (inline.equals(CFTreasureListTree.NONE_SYM) || inline.length()==0);

                        if (!isNone && !CFTreasureListTree.getInstance().containsTreasureList(inline) &&
                            !inline.equalsIgnoreCase(defarch.getAttributeString(attr.ref.getNameOld(), null))) {
                            // The user has specified a WRONG treasurelist name, and it does not come
                            // from the default arch. -> Error and out.
                            JOptionPane.showMessageDialog(this, "In attribute '"+attr.ref.getNameNew()+"':\n"+
                                                          "'"+inline+"' is not a known treasurelist name!", "Input Error",
                                                          JOptionPane.ERROR_MESSAGE);
                            throw new CGridderException(""); // bail out
                        }

                        if (!inline.equalsIgnoreCase(defarch.getAttributeString(attr.ref.getNameOld(), null))
                            && !(isNone && defarch.getAttributeString(attr.ref.getNameOld(), null).length()==0)) {
                            if (isNone)
                                new_ArchText = new_ArchText+attr.ref.getNameOld()+" none\n";
                            else
                                new_ArchText = new_ArchText+attr.ref.getNameOld()+" "+inline+"\n";
                        }
                    }
                }
            }

            // Also write all the 'fixed' attributes into the archtext
            for (int i=0; type.getAttr().length > i; i++) {
                // ### TODO: for changed types, copy fixed attributes over default arches ###
                if (type.getAttr()[i].getDataType() == CFArchAttrib.T_FIXED) {
                    String defaultValue = defarch.getAttributeString(type.getAttr()[i].getNameOld(), null);
                    if (defaultValue.length()==0 || (arch.getArchTypNr() != defarch.getArchTypNr() &&
                        !defaultValue.equalsIgnoreCase(type.getAttr()[i].getNameNew()))) {
                        // usually, fixed attributes are only applied when *not* defined in the defarch.
                        // the reason behind this is: if the default arch violates our fixed attribute,
                        // we assume the default arch is "right" and we are "wrong". The typedefs aren't that trustworthy.
                        // BUT - if the arch has a changed type, the defarch has lost it's credibility.
                        // So, in this special case, the fixed attribute applies always.
                        new_ArchText = new_ArchText+type.getAttr()[i].getNameOld()+" "+
                                       type.getAttr()[i].getNameNew()+"\n";
                    }
                }
            }


            /* we have excluded direction hard coded from the attribut panel
             * because we have a better interface in the arch panel.
             * But we need to add when needed the arch text - we do it here.
             */
              if(arch.getDirection()!=defarch.getDirection())
                new_ArchText = new_ArchText + "direction "+arch.getDirection()+"\n";
            // before we modify the archtext, we look for errors and save them.
            // later the user must confirm wether to keep or dump those errors
            errors = arch.getSyntaxErrors(type_struct);

            // --- parsing succeeded, now we write it into the arch/map ---
            arch.setArchText(new_ArchText);
            //arch.setArchTypNr();
            if (new_name != null) {
                if (new_name.length()==0)
                    arch.setObjName(null);
                else
                    arch.setObjName(new_name);
            }

            if(new_face != null)
              arch.setRealFace(new_face);

              // now lets assign the visible face - perhaps we have still a anim
            arch.setObjectFace();
            image_panel.setIcon(m_control.getArchObjectStack().getFace(arch.getObjectFaceNr()));

            if (new_msg != null) {
                // set new msg text only when it is not equal to default arch
                if (!new_msg.trim().equals(defarch.getMsgText()==null?"":defarch.getMsgText().trim())) {
                    arch.deleteMsgText();
                    arch.addMsgText(new_msg);
                }
                else
                    arch.deleteMsgText();
            }
            else if (defarch.getMsgText() != null && defarch.getMsgText().trim().length() > 0) {
                // we must override defarch msg by an empty msg
                arch.deleteMsgText();
                arch.addMsgText("");
            }
            else
                arch.deleteMsgText(); // all empty

            // deal with syntax errors now
            if (errors != null) {
                if (type_struct == typelist.getHead().getNext()) {
                    // for generic (misc) type, all errors are automatically kept.
                    // "misc" is no real type - it is more a default mask for unknown types
                    arch.addArchText(errors.trim()+"\n");
                }
                else {
                    // open a popup dialog and ask user to decide what to do with his errors
                    askConfirmErrors(errors);
                }
            }

            // if the archtext changed, set the map changed flag
            if ((old_ArchText != null && !old_ArchText.equals(arch.getArchText())) ||
                (old_ArchText == null && arch.getArchText() != null) ||
                (old_msg!=null && !old_msg.equals(arch.getMsgText())) ||
                (old_msg == null && arch.getMsgText() != null))
                m_control.m_currentMap.setLevelChangedFlag();

            // recalculate the edit_type value
            arch.calculateEditType(m_control.m_currentMap.getActive_edit_type());

            m_control.getMainView().refreshMapArchPanel();
            return true; // apply succeeded
        }
        catch (CGridderException e) {}
        return false; // error (-> try again)
    }

    /**
     * Open a popup dialog and ask the user to confirm (or modify)
     * the encountered syntax errors.
     * If the user chooses to keep any errors, these get attached
     * to the archtext by the actionlistener (<code>ConfirmErrorsAL</code>).
     *
     * Note that this method does not fork off with a new thread.
     * It freezes the parent frames (and threads) until the popup
     * window is closed, which mimics non-event-driven behaviour.
     *
     * @param errors   a textual list of the encountered errors
     */
    private void askConfirmErrors(String errors) {
        JDialog frame = new JDialog(this, "Syntax Errors", true);    // dialog freezes parents
        frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // closing is handled by listener

        JPanel main_panel = new JPanel();
        main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
        JPanel header_panel = new JPanel(new GridLayout(2, 1));
        JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // create header labels
        JLabel header1 = new JLabel("The following lines from the archtext appear to be wrong.");
        header1.setForeground(Color.black);
        JLabel header2 = new JLabel("They do not match the type definitions:");
        header2.setForeground(Color.black);
        header_panel.add(header1);
        header_panel.add(header2);

        // create textarea for showing errors
        JTextArea textarea = new JTextArea(errors, 7, 25);
        textarea.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 0));
        JScrollPane scrollPane = new JScrollPane(textarea);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE );
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // create buttons
        JButton dump_button = new JButton("Dump All Errors");
        JButton keep_button = new JButton("Keep Above Text");
        button_panel.add(dump_button);
        button_panel.add(keep_button);

        // attach actionlistener to the buttons (and the frame)
        ConfirmErrorsAL listener = new ConfirmErrorsAL(frame, arch, errors, keep_button, textarea);
        keep_button.addActionListener(listener);
        dump_button.addActionListener(listener);
        frame.addWindowListener(listener);

        // stick panels together
        main_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main_panel.add(header_panel);
        main_panel.add(Box.createVerticalStrut(10));
        main_panel.add(scrollPane);
        main_panel.add(Box.createVerticalStrut(10));
        main_panel.add(button_panel);

        frame.getContentPane().add(main_panel);

        // pack, position and show the popup
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    /**
     * close this dialog frame
     */
    private void closeDialog() {
        // if a treasurelist window is attached to this dialog, hide it
        if (CFTreasureListTree.getParentDialog() == this)
            CFTreasureListTree.hideDialog();

        attr_head = null; attr_tail = null;
        this.dispose();
        try {
            this.finalize();
        } catch (Throwable e) {}
        //System.gc();
    }

    // ============================ Subclasses ============================

    /**
     * This (Sub)Class contains the data of one arch attribute.
     */
    public class DialogAttrib {
        public CFArchAttrib ref;    // reference to the attribute data

        public DialogAttrib next;   // next element in the list
        public JButton help_button; // help button

        /**
         * Constructor
         */
        DialogAttrib () {
            next = null;
            ref = null;
            help_button = null;
        }
    }

    /**
     * This (Sub)Class implements the dialog attribute of type BOOL.
     */
    public class BoolAttrib extends DialogAttrib {
        public JCheckBox input;  // the input means of bool is a checkbox
    }

    /**
     * This (Sub)Class implements the dialog attribute of type INT.
     */
    public class IntAttrib extends DialogAttrib {
        public JTextField input;  // the input means of int is a textfield
    }

    /**
     * This (Sub)Class implements the dialog attribute of type FLOAT.
     */
    public class FloatAttrib extends DialogAttrib {
        public JTextField input;  // the input means of int is a textfield
    }

    /**
     * This (Sub)Class implements the dialog attribute of type STRING.
     */
    public class StringAttrib extends DialogAttrib {
        public JTextField input;  // the input means of string is a textfield
    }

    /**
     * This (Sub)Class implements the dialog attribute of type TEXT.
     */
    public class TextAttrib extends DialogAttrib {
        public JTextArea input;  // the input means of text is a textarea
    }

    /**
     * This (Sub)Class implements the dialog attribute of types with
     * selection lists (ComboBoxes) to choose from.
     */
    public class ListAttrib extends DialogAttrib {
        public JComboBox input;  // the input means of spell is a combo box
    }

	/**
     * This (Sub)Class implements the dialog attribute of types with
     * selection lists (ComboBoxes) to choose from.
     */
    public class DoubleListAttrib extends DialogAttrib {
        public JComboBox[] inputs;  // the input means of spell is a combo box
    }

    /**
     * This (Sub)Class implements the dialog attribute of types with
     * bitmasks to choose from.
     */
    public class BitmaskAttrib extends DialogAttrib {
        public JTextArea text;          // the display component for bitmask-contents
        public int value;               // the active bitmask value
        private CAttribBitmask bitmask; // reference to the bitmask data

        public int getValue() {return value;}
        public void setValue(int new_value) {value = new_value;}
    }

    /**
     * Action-listener for help-buttons
     */
    private class HelpAL implements ActionListener {
        CFArchAttrib attrib; // attribute structure

        /**
         * construktor
         * @param a     the arch attribute where this help button belongs to
         */
        public HelpAL(CFArchAttrib a) {
            attrib = a;
        }

        /**
         * help button was pressed
         * @param event    the occured <code>ActionEvent</code> (button pressed)
         */
        public void actionPerformed(ActionEvent event) {
            if (attrib != null)
                popup_help(attrib.getNameNew(), attrib.getText());
        }
    }

    /**
     * Action-listener for the change buttons of bitmasks
     */
    public class MaskChangeAL implements ActionListener {
        BitmaskAttrib BmAttr; // attribute structure
        CAttribDialog dialog; // reference to this dialog instance

        /**
         * Constructor
         * @param new_attr     the GUI-bitmask attribute where the change button belongs to
         */
        public MaskChangeAL(BitmaskAttrib new_attr, CAttribDialog new_dialog) {
            BmAttr = new_attr;
            dialog = new_dialog;
        }

        /**
         * help button was pressed
         * @param event    the occured <code>ActionEvent</code> (button pressed)
         */
        public void actionPerformed(ActionEvent event) {
            if (BmAttr != null) {
                BmAttr.bitmask.popup_frame(dialog, BmAttr);
            }
        }
    }

    /**
     * Action-listener for the buttons on treasurelists. When such a button is
     * pressed, the dialog with treasurelists pops up.
     */
    public class ViewTreasurelistAL implements ActionListener {
        StringAttrib strAttr; // attribute structure
        CAttribDialog dialog; // reference to this dialog instance

        /**
         * Constructor
         * @param new_attr     the GUI-string attribute where the treasurelist button belongs to
         */
        public ViewTreasurelistAL(StringAttrib attr, CAttribDialog dialog) {
            strAttr = attr;
            this.dialog = dialog;
        }

        /**
         * treasurelist button was pressed
         * @param event    the occured <code>ActionEvent</code> (button pressed)
         */
        public void actionPerformed(ActionEvent event) {
            if (strAttr != null) {
                CFTreasureListTree.getInstance().showDialog(strAttr.input, dialog);
            }
        }
    }

    /**
     * Action-listener for the buttons in the ConfirmErrors popup dialog
     * and also Window-listener for the closebox of the dialog
     * (which would equal a "keep all" button)
     */
    public class ConfirmErrorsAL implements ActionListener, WindowListener {
        JDialog dialog;       // the popup dialog itself
        JButton keep_button;  // button "keep what is in the textfield"
        JTextArea text;       // textfield containing the error-text to keep
        ArchObject arch;      // the according arch
        String all_errors;    // list of all errors

        /**
         * Constructor
         * @param dl           the popu dialog
         * @param arch_new     the arch which has the error to be added
         * @param errors       list of all errors (= initial content of the textarea)
         * @param keep_b       button "keep what is in the textfield"
         * @param text         textfield containing the error-text to keep
         */
        public ConfirmErrorsAL(JDialog dl, ArchObject arch_new, String errors,
                               JButton keep_b, JTextArea text_new) {
            arch = arch_new;
            keep_button = keep_b;
            text = text_new;
            dialog = dl;
            all_errors = errors;
        }

        /**
         * a button was pressed
         * @param event    the occured <code>ActionEvent</code> (button pressed)
         */
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == keep_button) {
                // the user pressed "keep", so we append the contents
                // of the textfield to the archtext
                if (text.getText() != null && text.getText().trim().length() > 0)
                    arch.addArchText(text.getText().trim()+"\n");
            }

            // nuke the popup dialog
            dialog.dispose();
            dialog = null;
        }

        /**
         * user wants to kill the window via closebox -
         * in this case, we keep all errors in the arch because it must
         * be assumed that the user did not properly think about them yet.
         * @param event    the occured <code>WindowEvent</code>
         */
        public void windowClosing(WindowEvent event) {
            arch.addArchText(all_errors.trim()+"\n");

            // nuke the popup dialog
            dialog.dispose();
            dialog = null;
        }

        public void windowDeactivated(WindowEvent event) {}
        public void windowActivated(WindowEvent event) {}
        public void windowClosed(WindowEvent event) {}
        public void windowOpened(WindowEvent event) {}
        public void windowIconified(WindowEvent event) {}
        public void windowDeiconified(WindowEvent event) {}
    }


    /**
     * Item-listener for the type-selection box on the attribute-dialog
     */
    private class TypesBoxAL implements ItemListener {
        CAttribDialog frame;    // the entire frame
        ArchObject arch;        // the according arch
        ArchObject defarch;     // default arch of 'arch'
        String deselected;      // the latest deselected item

        public boolean ignore_event;   // while true, this listener ignores all events

        /**
         * Constructor
         * @param frame        the attribute-dialog mainframe
         * @param arch_new     the arch which has the error to be added
         * @param defarch_new  the default arch of 'arch'
         */
        public TypesBoxAL(CAttribDialog frame_new, ArchObject arch_new, ArchObject defarch_new) {
            frame = frame_new;
            arch = arch_new;
            defarch = defarch_new;
            ignore_event = false;
        }

        /**
         * a (new) type has been selected in the box
         * @param event    the occured <code>ItemEvent</code> (button pressed)
         */
        public void itemStateChanged(ItemEvent event) {
            if (ignore_event) return;

            if (event.getStateChange() == ItemEvent.DESELECTED) {
                // remember the deselected type
                deselected = ((String)event.getItem()).trim();
            }
            else if (event.getStateChange() == ItemEvent.SELECTED &&
                     !((String)event.getItem()).equals(deselected)) {
                // new type was selected
                // first, get new type structure
                CFArchType new_type = typelist.getTypeByName((String)event.getItem());

                frame.typesel.hidePopup();
                frame.update(frame.getGraphics());

                if (deselected == null) deselected = frame.type.getTypeName();
                if (JOptionPane.showConfirmDialog(frame,
                                 "Do you really want to change the type of this\n"+
                                 "object from \""+deselected+"\" to \""+new_type.getTypeName()+"\"?",
                                 "Confirm",
                                 JOptionPane.YES_NO_OPTION,
                                 JOptionPane.INFORMATION_MESSAGE)
                    == JOptionPane.YES_OPTION) {
                    // change is confirmed, now get it on...
                    frame.type = new_type;   // set new type structure

                    // change the arch to be of the new type:
                    arch.setArchTypNr(new_type.getTypeNr());
                    frame.type_nr = new_type.getTypeNr();
                    attr_head = null; // clear list of attributes

                    list_nr = frame.typesel.getSelectedIndex();

                    // rebuild the dialog frame to show the new tabs
                    frame.tabbedPane.removeAll();
                    frame.build_attr();
                    frame.tabbedPane.validate();
                    frame.update(frame.getGraphics());

                    // recalculate the edit_type value
                    arch.calculateEditType(m_control.m_currentMap.getActive_edit_type());
                    // also update the arch panel (bottom)
                    m_control.getMainView().refreshMapArchPanel();
                    // map content has changed
                    m_control.m_currentMap.setLevelChangedFlag();
                }
                else {
                    // change not wanted -> reset the choosebox
                    ignore_event = true; // ignore events thrown by the forced reset
                    frame.typesel.setSelectedIndex(list_nr);
                    ignore_event = false;
                }
            }
        }
    }

    /**
     * class to manage the select-per-keystroke in a JComboBox
     * (The default KeySelectionManager fails because all strings
     * start with whitespace ' ')
     *
     * Unfortunately, this class cannot be used anymore because
     * it does not work together with the listener <code>TypesBoxAL</code>
     */
    private class StringKeyManager implements JComboBox.KeySelectionManager {
        JComboBox box;   // reference to ComboBox

        public StringKeyManager(JComboBox my_box) {box = my_box;}

        /*
         * For any key, we select the first entry with
         * identical first character (after ' ')
         */
        public int selectionForKey(char aKey, ComboBoxModel aModel) {
            for (int i=0; i<aModel.getSize(); i++) {
                if (((String)aModel.getElementAt(i)).toLowerCase().charAt(1) == aKey) {
                    //type_listener.ignore_event = true;
                    box.setSelectedIndex(i); // should happen automatically, but doesn't
                    //type_listener.ignore_event = false;
                    //type_listener.listen_action = true;
                    return i;
                }
            }
            return -1;  // no match found
        }
    }
}
