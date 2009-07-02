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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jdom.*;

/**
 * This class manages bitmask values which appear in Crossfire
 * arch attributes. Attacktype, spellpath and material are such
 * bitmasks. They are disguised for the user, with the help of
 * the attribute dialog.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CAttribBitmask {
    // maximum number of characters in a line before linebreak (see getText())
    //private static final int MAX_CHARS_PER_LINE = 50;
    private static final int MAX_CHARS_PER_LINE = 35;

    private String[] bit_name;    // array of the names of bitmask-entries

    private int maxvalue;         // max. possible value
    private int number;           // number of bitmask entrys (not counting zero)

    /**
     * Konstructor of a bitmask (DEPRECATED!)
     * @param name     array of names for the bit-entries
     */
    public CAttribBitmask(String[] name) {
        // initialize arrays
        bit_name = new String[name.length+1];
        //bit_value = new int[name.length+1];

        // the zero-bit is always <none>
        bit_name[0] = "<none>";
        //bit_value[0] = 0;

        for (int i=0; i<name.length; i++) {
            // fill array and assign 2^i values
            bit_name[i+1] = name[i];
            //bit_value[i+1] = (int)(Math.pow(2., (double)(i-1)));
        }

        number = bit_name.length-1;
        maxvalue = ((int)Math.pow(2., (double)(bit_name.length+1)))-1;
    }

    /**
     * Konstructor of a bitmask from XML element
     * @param root      xml bitmask element
     */
    public CAttribBitmask(Element root) {
        int i;

        java.util.List entries = root.getChildren("entry");
        if (entries != null && entries.size() > 0) {
            Element elem;
            Attribute a;

            // find highest bit number
            int max_bit = 0;
            for (i=0; entries != null && i<entries.size(); i++) {
                elem = (Element)(entries.get(i));
                if ((a = elem.getAttribute("bit")) == null || elem.getAttribute("name") == null) {
                    System.out.println("Parse error: Found bitmask entry without 'bit' or 'name'.");
                    elem.detach(); // remove element from DOM tree
                }
                else {
                    try {
                        int bit = a.getIntValue();
                        if (bit > max_bit)
                            max_bit = bit; // this is the highest bit so far
                    }
                    catch (DataConversionException e) {
                        System.out.println("Parse error: Bitmask bit '"+a.getValue()+"' ("+elem.getAttribute("name").getValue()+") is not an integer.");
                        elem.detach(); // remove element from DOM tree
                    }
                }
            }
            entries = root.getChildren("entry"); // retake list, in case we detached some elements

            // initialize array
            bit_name = new String[max_bit+2];

            // initialize names array - the zero-bit always stays <none>
            for (i=0; i<max_bit+2; i++)
                bit_name[i] = "<none>";

            for (i=0; entries != null && i<entries.size(); i++) {
                elem = (Element)(entries.get(i));
                // fill array and assign 2^i values
                try {
                    bit_name[elem.getAttribute("bit").getIntValue()+1] = elem.getAttribute("name").getValue();
                }
                catch (DataConversionException e) {}
            }

            /* printout
            System.out.println("bitmask '"+root.getAttribute("name")+"':");
            for (i=0; i<bit_name.length; i++) {
                System.out.println("   "+i+" = "+bit_name[i]);
            }*/

            number = bit_name.length-1;
            maxvalue = ((int)Math.pow(2., (double)(bit_name.length+1)))-1;
        }
        else {
            // Error: this is an "empty" bitmask
            System.out.println("Error in \""+IGUIConstants.TYPEDEF_FILE+"\": Found a bitmask without content!");
            bit_name = new String[1];
            bit_name[0] = "<none>";
            number = 0; maxvalue = 0;
        }
    }

    /**
     * check wether the given bit-index is an active bit in the bitmask
     * @param index    index of the bit to check (range from 1-'number')
     * @param mask     bitmask to check against
     * @return
     */
    private boolean is_active(int index, int mask) {
        return ((int)(Math.pow(2., (double)(index-1))) & mask) != 0;
    }

    /**
     * Display the appropriate text for a given bitmask value.
     * The text is put into a non-editable textarea.
     *
     * @param value     bitmask value
     * @param textf     if non-null, this textarea is used for drawing,
     *                  if null, a new textarea is created and returned
     * @return <code>JTextArea</code> with all entries belonging to the bitmask
     *         and proper dimensions
     */
    public JTextArea getText(int value, JTextArea textf) {
        String text = " "; // text string to return
        int rows = 1;      // rows of textarea
        int columns = 1;   // columns of textarea (actually not needed)

        // is bitmask empty?
        if (value <= 0) {
            text += bit_name[0];
            columns = text.length();
        }
        else {
            // value too big?
            if (value > maxvalue) {
                System.out.println("bitmask value "+value+" is too big.");
            }

            boolean linebreak = false;
            int linelength = 0;  // length of last line

            for (int i=1; i<bit_name.length; i++) {
                if (is_active(i, value)) {
                    // is a linebreak required?
                    linelength = text.indexOf("\n") >= 0 ? text.substring(text.lastIndexOf("\n")).length() : text.length();
                    if (linelength + bit_name[i].length()+2 > MAX_CHARS_PER_LINE) {
                        text += ", \n ";
                        linebreak = true;
                        rows++;
                    }
                    else
                        linebreak = false;

                    // append text
                    text += ((text.length()<=1 || linebreak)?"":", ")+ bit_name[i];

                    linelength = text.indexOf("\n") >= 0 ? text.substring(text.lastIndexOf("\n")).length() : text.length();
                    if (linelength > columns) {
                        columns = linelength;
                    }
                }
            }
        }

        // create JTextArea (setting columns results in textarea being too wide)
        if (textf == null) {
            textf = new JTextArea(text+" ", rows, 18);

            // set colors, border and stuff
            textf.setForeground(java.awt.Color.black);
            textf.setBackground(CMainControl.getInstance().getMainView().getBackground());
            textf.setEditable(false);
            textf.setBorder(BorderFactory.createLineBorder(java.awt.Color.gray));
        }
        else {
            // textarea already exists, so change it
            textf.setText(text+" ");
            textf.setRows(rows);
            textf.setColumns(18);
        }

        return textf;
    }

    /**
     * Open a popup frame to select bitmask-entries via chooseboxes.
     *
     * @param frame       a new <code>JInternalFrame</code>
     * @param init_value  initial value for the bitmask
     */
    public void popup_frame(CAttribDialog attrDialog, CAttribDialog.BitmaskAttrib gui_attr) {
        String title = "Choose "+gui_attr.ref.getNameNew().substring(0, 1).toUpperCase()+gui_attr.ref.getNameNew().substring(1);
        JDialog frame = new JDialog(attrDialog, title, true);
        frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // panels
        JPanel main_panel = new JPanel();
        main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
        JPanel grid_panel = new JPanel(new GridLayout(1, 2));
        JPanel left_list  = new JPanel();  // left column of checkboxes
        left_list.setLayout(new BoxLayout(left_list, BoxLayout.Y_AXIS));
        JPanel right_list = new JPanel();  // right column of checkboxes
        right_list.setLayout(new BoxLayout(right_list, BoxLayout.Y_AXIS));
        JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // create checkboxes
        JCheckBox[] checkbox = new JCheckBox[number];
        for (int i=0; i<number; i++) {
            checkbox[i] = new JCheckBox(" "+bit_name[i+1]);
            checkbox[i].setSelected(is_active(i+1, gui_attr.getValue()));
            if (i % 2 == 0)
                left_list.add(checkbox[i]);
            else
                right_list.add(checkbox[i]);
        }
        grid_panel.add(left_list);
        grid_panel.add(right_list);
        grid_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main_panel.add(grid_panel);

        // buttons
        JButton ok_button = new JButton("Ok");
        JButton cancel_button = new JButton("Cancel");
        button_panel.add(ok_button);
        button_panel.add(cancel_button);
        main_panel.add(button_panel);

        // attach action listener to the buttons
        PopupFrameAL listener = new PopupFrameAL(frame, gui_attr, this, checkbox, attrDialog);
        ok_button.addActionListener(listener);
        cancel_button.addActionListener(listener);

        // hurl all this litter into the frame, pack and show it
        frame.getContentPane().add(main_panel);
        frame.pack();
        frame.setLocationRelativeTo(attrDialog);
        frame.setVisible(true);
    }

    /**
     * Subclass: Action-listener for the buttons in the popup frame
     * where changes to the bitmask can be selected.
     */
    private class PopupFrameAL implements ActionListener {
        CAttribBitmask bitmask;   // reference to this CAttribBitmask instance
        JDialog frame;            // reference to the popup dialog frame
        CAttribDialog.BitmaskAttrib gui_attr;  // gui attribute instance
        JCheckBox[] checkbox;     // array of checkboxes on the frame
        CAttribDialog attrDialog; // instance of attribute dialog (parent frame)

        /**
         * Contructor
         * @param new_frame      thepopup dialog frame
         * @param new_gui_attr   gui attribute instance
         * @param new_mask       this CAttribBitmask instance
         * @param boxarray       array of checkboxes in the popup frame
         * @param new_attrD      attribute dialog frame
         */
        public PopupFrameAL(JDialog new_frame, CAttribDialog.BitmaskAttrib new_gui_attr,
                            CAttribBitmask new_mask, JCheckBox[] boxarray, CAttribDialog new_attrD) {
            frame = new_frame;
            gui_attr = new_gui_attr;
            bitmask = new_mask;
            checkbox = boxarray;
            attrDialog = new_attrD;
        }

        /**
         * a button was pressed
         * @param event    the occured <code>ActionEvent</code> (button pressed)
         */
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() instanceof JButton) {
                // check if the okay button was pressed
                if (((JButton)event.getSource()).getText().equalsIgnoreCase("Ok")) {
                    // calculate the new bitmask value:
                    int new_value = 0;
                    for (int i=0; i<bitmask.number; i++) {
                        if (checkbox[i].isSelected())
                            new_value |= (int)(Math.pow(2., (double)(i)));
                    }

                    // update the text component in the CAttribDialog with the new values
                    bitmask.getText(new_value, gui_attr.text);
                    attrDialog.update(attrDialog.getGraphics());
                    gui_attr.setValue(new_value);
                }

                // nuke the dialog frame
                frame.dispose();
                frame = null;
            }
        }
    }
}
