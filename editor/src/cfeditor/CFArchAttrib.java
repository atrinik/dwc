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

import java.io.*;
import org.jdom.*;

/**
  * This Class contains the data of one arch attribute.
  *
  * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
  */
public class CFArchAttrib implements Cloneable {
    // XML tag names
    public static final String XML_KEY_ARCH = "arch";
    public static final String XML_KEY_ARCH_BEGIN = "arch_begin";
    public static final String XML_KEY_ARCH_END = "arch_end";
    public static final String XML_KEY_EDITOR = "editor";
    public static final String XML_ATTR_TYPE = "type";
    public static final String XML_INPUT_LENGTH = "length";

    // possible data types for attribute "values":
    public static final int T_BOOL = 1;      // bool   (-> Checkbox)
    public static final int T_BOOL_SPEC = 2; // bool with customized true/false values
    public static final int T_INT = 3;       // int    (-> Textfield)
    public static final int T_FLOAT = 4;     // float  (-> Textfield)
    public static final int T_STRING = 5;    // string (-> Textfield)
    public static final int T_TEXT = 6;      // text   (-> Textarea)
    public static final int T_FIXED = 7;     // fixed string (no input)
    public static final int T_SPELL = 8;     // spell number (-> ComboBox)
    public static final int T_ZSPELL = 9;    // (like T_SPELL, except 0 is always 0, not magic bullet)

    public static final int T_BITMASK = 10;  // Bitmask (-> Special input with popup frame)
    public static final int T_LIST = 11;     // List (-> ComboBox)
    public static final int T_TREASURE = 12; // treasurelist  (-> Textfield and Tree)
    public static final int T_DBLLIST = 13; // double list  (-> 2 x ComboBox)

    private CFArchAttrib next; // next element in the list (/array)

    private String name_old;   // original attr. name (from the CF arches)
    private String ending_old; // for data_type = 'T_TEXT' this is the terminating string
                               // (example: 'endmsg' for arch message)
    private String name_new;   // new attr. name (for the user-friendly GUI)
    private int data_type;     // type of attribute data (T_BOOL/T_INT/...)

    private String text;       // descrption of this attr.

    private int input_length;  // optional attribute: set length for input JTextFields
    private String[] misc;     // string array for misc. use
                               // T_BOOL_SPEC uses misc[0]=true value, misc[1]=false value
                               // T_BITMASK uses misc[0] = bitmask name, T_LIST uses misc[0] = list name

    private int sec_id;        // identifier of the section this attribute is in
    private String section;    // name of the section this attribute is in

    /**
     * Constructor
     */
    public CFArchAttrib() {
        name_old=""; name_new="";
        String text = null;
        ending_old = null;
        misc = null;
        input_length = 0; // use default length
    }

    /**
     * Loading the data from an xml element into this object.
     *
     * @param root         the xml 'attribute' element
     * @param tlist        the archtype list
     * @param typeName     (descriptive) name of the type this attribute belongs to (e.g. "Weapon")
     * @return true if the parsing was successful
     */
    public boolean load(Element root, CFArchTypeList tlist, String typeName) {
        String atype = null; // name of the attribute type
        Attribute a1;
        Attribute a2;

        // parse the info text from the element's "body"
        parseText(root);

        // arch syntax key
        if ((a1 = root.getAttribute(XML_KEY_ARCH)) != null)
            name_old = a1.getValue().trim();

        // editor syntax key
        if ((a1 = root.getAttribute(XML_KEY_EDITOR)) != null)
            name_new = a1.getValue().trim();

        // type name
        if ((a1 = root.getAttribute(XML_ATTR_TYPE)) != null)
            atype = a1.getValue().trim();
        else {
            // error: no type
            System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has attribute missing '"+XML_ATTR_TYPE+"'.");
            return false;
        }

        // input length (optional)
        if ((a1 = root.getAttribute(XML_INPUT_LENGTH)) != null) {
            try {
                input_length = a1.getIntValue();
            }
            catch (DataConversionException de) {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has attribute with invalid length '"+a1.getValue()+"' (must be a number).");
            }
        }

        // which type of attribute is it?
        if (atype.equalsIgnoreCase("bool"))
            data_type = T_BOOL; // normal bool
        else if (atype.equalsIgnoreCase("bool_special")) {
            // customized boolean type:
            data_type = T_BOOL_SPEC;

            // parse true and false values:
            a1 = root.getAttribute("true");
            a2 = root.getAttribute("false");
            if (a1 == null || a2 == null) {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has bool_special attribute missing 'true' or 'false' value.");
                return false;
            }
            else {
                misc = new String[2]; // 'misc' string contains the values
                misc[0] = a1.getValue().trim(); // string for true
                misc[1] = a2.getValue().trim(); // string for false
            }
        }
        else if (atype.equalsIgnoreCase("int"))
            data_type = T_INT;
        else if (atype.equalsIgnoreCase("float"))
            data_type = T_FLOAT;
        else if (atype.equalsIgnoreCase("string"))
            data_type = T_STRING;
        else if (atype.equalsIgnoreCase("text")) {
            data_type = T_TEXT;
            // for text data, the terminating string has to be read too
            if ((a1 = root.getAttribute(XML_KEY_ARCH_BEGIN)) != null)
                name_old = a1.getValue().trim();

            if ((a1 = root.getAttribute(XML_KEY_ARCH_END)) != null)
                ending_old = a1.getValue().trim();

            if (name_old == null || name_old.length() == 0 ||
                ending_old == null || ending_old.length() == 0) {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has text attribute missing '"+XML_KEY_ARCH_BEGIN+"' or '"+XML_KEY_ARCH_END+"'.");
                return false;
            }
        }
        else if (atype.equalsIgnoreCase("fixed")) {
            // fixed attribute
            data_type = T_FIXED;
            if ((a1 = root.getAttribute("value")) != null)
                name_new = a1.getValue().trim();
            else {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has fixed attribute missing 'value'.");
                return false;
            }
        }
        else if (atype.equalsIgnoreCase("spell")) {
            // spell attribute
            if (tlist.getSpellNum() == null)
                data_type = T_INT; // if we have no spells, use an INT field instead
            else
                data_type = T_SPELL;
        }
        else if (atype.equalsIgnoreCase("nz_spell")) {
            // spell attribute
            if (tlist.getSpellNum() == null)
                data_type = T_INT; // if we have no spells, use an INT field instead
            else
                data_type = T_ZSPELL;
        }
        else if (atype.startsWith("bitmask")) {
            // got a bitmask attribute
            String bitmaskName = atype.substring(8).trim();

            if (tlist.getBitmaskTable().containsKey(bitmaskName)) {
                // the bitmask is well defined
                data_type = T_BITMASK;
                misc = new String[1];
                misc[0] = bitmaskName; // store bitmask name in misc[0]
            }
            else {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"', type "+typeName+": Bitmask \""+bitmaskName+"\" is undefined.");
            }
        }
        else if (atype.startsWith("list")) {
            // got a bitmask attribute
            String listName = atype.substring(5).trim();

            if (tlist.getListTable().containsKey(listName)) {
                // the list is well defined
                data_type = T_LIST;
                misc = new String[1];
                misc[0] = listName; // store list name in misc[0]
            }
            else {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"', type "+typeName+": List \""+listName+"\" is undefined.");
            }
        }
        else if (atype.startsWith("doublelist")) {
            // got a doublelist attribute
            String listNames = atype.substring(11).trim();
			int seppos = listNames.indexOf(',');

			if(seppos == -1 || seppos == listNames.length() - 1) {
				System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+", double list: '"+atype+"' does not contain two comma-separated lists.");
				return false;
			}
				
			
			String listName1 = listNames.substring(0,seppos);
			String listName2 = listNames.substring(seppos+1);

            if (tlist.getListTable().containsKey(listName1) && tlist.getListTable().containsKey(listName2)) {
                // the lists are well defined
                data_type = T_DBLLIST;
                misc = new String[2];
                misc[0] = listName1; // store list name in misc[0]
                misc[1] = listName2; // store list name in misc[1]
            }
            else {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"', type "+typeName+": List \""+listName1+"\" or \""+listName2+"\" is undefined.");
            }
        }
        else if (atype.equalsIgnoreCase("treasurelist"))
            data_type = T_TREASURE;
        else {
            // unknown type
            System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"': Type "+typeName+" has an attribute with unknown type: '"+atype+"'.");
            return false;
        }

        return true;
    }

    /**
     * This method reads the text out of the element's "body" and
     * cuts off whitespaces at front/end of lines.
     * This is a bit hacky but simple. Probably a more correct approach
     * would be to display the info text in an html context.
     *
     * @param root   xml attribute element
     */
    private void parseText(Element root) {
        // parse the text
        if (root.getText() == null)
            text = "";
        else {
            StringBuffer buff = new StringBuffer("");
            StringReader stream = null;
            BufferedReader bfread = null;
            try {
                // we read the text from a stringstream line by line
                stream = new StringReader(root.getText().trim());
                bfread = new BufferedReader(stream);
                String line;
                boolean is_first = true;
                while ((line = bfread.readLine()) != null) {
                    // delete extra spaces for every line seperately
                    buff.append((is_first ? "" : "\n")+line.trim());
                    if (is_first) is_first = false;
                }
                text = buff.toString();

                bfread.close();
                stream.close();
            }
            catch (EOFException e) {
                // this is actually not expected to happen
                text = buff.toString();
                try {
                    bfread.close();
                    stream.close();
                } catch (IOException ioe) {}
            }
            catch (IOException e) {
                System.out.println("Error in CFArchAttrib.parseText(): Cannot read from text stream.");
            }
        }
    }

    /**
     * assign this attribute to a section
     * @param id      section ID
     * @param sname   section name
     */
    public void setSection(int id, String sname) {
        sec_id = id;
        section = sname;
    }

    /**
     * get a new instance of this object with identical content
     * @return clone instance of this <code>CFArchAttrib</code>
     */
    public CFArchAttrib getClone() {
        CFArchAttrib newattr = new CFArchAttrib();

        newattr.next = next;

        newattr.name_old = name_old;
        newattr.ending_old = ending_old;
        newattr.name_new = name_new;
        newattr.data_type = data_type;
        newattr.input_length = input_length;

        newattr.text = text;

        // important: we don't create a new instance of 'misc', only a refernce
        newattr.misc = misc;

        newattr.sec_id = sec_id;
        newattr.section = section;

        return newattr;
    }

    public int getSecId() {return sec_id;}
    public void setSecId(int new_id) {sec_id = new_id;}
    public String getSecName() {return section;}

    public String getNameOld() {return name_old;}
    public String getEndingOld() {return ending_old;}
    public String getNameNew() {return name_new;}
    public int getDataType() {return data_type;}
    public String getText() {return text;}
    public String[] getMisc() {return misc;}
    public int getInputLength() {return input_length;}

    public CFArchAttrib getNext() {return next;}
    public void setNext(CFArchAttrib cf_attr) {next = cf_attr;}

    private void read_till(BufferedReader stream, String tag) throws IOException, EOFException {
        CFileReader.read_till(stream, tag, null);
    }
    private String reads_till(BufferedReader stream, String tag) throws IOException, EOFException {
        return CFileReader.reads_till(stream, tag);
    }
}