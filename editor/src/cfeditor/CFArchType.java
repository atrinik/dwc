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
import java.util.Vector;
import java.util.Hashtable;
import org.jdom.*;

/**
 * Contains the data of one Crossfire Object-Type.
 * The data is read from a definitions file called 'types.txt'.
 * It is mainly used as info-base for the arch-attribute GUI.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFArchType {
    // XML tag names
    public static final String XML_TYPE = "type";
    public static final String XML_ATTRIBUTE = "attribute";
    public static final String XML_REQUIRED = "required";
    public static final String XML_IGNORE = "ignore";
    public static final String XML_IMPORT_TYPE = "import_type";
    public static final String XML_VALUE = "value";
    public static final String XML_DESC = "description";
    public static final String XML_USE = "use";
    public static final String XML_SECTION = "section";

    // file with type definitions:
    public static final String filename = IGUIConstants.TYPEDEF_FILE;

    private int typenr;        // type number of this CF type
    private String t_name;     // type name (artificial)
    private String[] t_attr;   // list of aditional attributes that an object
                               // must have, in order to be of this type:
                               // t_attr[0] is attr. name, t_attr[1] is attr. value, ...

    private CFArchType head;   // head Element (contains default attributes)
    private CFArchType next;   // next CFArchType in the list

    private CFArchType[] see = null; // links to similar types

    private String desc;       // descrption of this type
    private String use;        // notes on usage of this type

    private CFArchAttrib[] attr; // list of arch attributes (/array))

    private int section_num;   // number of attribute-sections

    /**
     * Konstructor
     * @param head_type     the head element of the list
     */
    public CFArchType(CFArchType head_type) {
        next=null;
        desc=null; use=null;
        typenr=0; t_name="";
        t_attr=null;
        section_num=2;      // there's always the "general" and "special" sections (even if empty)

        head = head_type;   // set head of list
    }

    /**
     * Loading the data from an xml type element into this CFArchType.
     * Since users are expected to edit the type definitions, I have tried
     * to design the parser to be somewhat robust and provide error feedback.
     *
     * @param root      the xml 'type' element which is going to be parsed
     * @param tlist     archtype list
     * @return true if the object was parsed correctly and can be used
     */
    public boolean load(Element root, CFArchTypeList tlist) {
        // this vector is used to store a temporare linked list of attributes
        Vector attr_list = new Vector();

        // for internal section handling:
        java.util.Vector sec_names = new java.util.Vector(); // list of section names
        boolean in_section=false;  // true while section active
        String section="?";        // name of the current section
        Hashtable ignoreTable = new Hashtable(); // ignore list

        String import_name = null; // name of type to import from (or null)
        java.util.List children; // list of children elements
        Element elem;            // xml element
        Attribute a1;
        Attribute a2;
        int i;                   // index

        if (root.getName().equalsIgnoreCase("default_type")) {
            // special case: default type (this one contains the default attribs)
            typenr = -1;
            t_name = "default";

            //System.out.println("type: default");
        }
        else {
            try {
                // parse the type name
                t_name = root.getAttribute("name").getValue().trim();

                // parse the type number
                typenr = Integer.parseInt(root.getAttribute("number").getValue().trim());

                //System.out.println("reading type: "+t_name+", "+typenr);

                // parse 'required' attributes
                Element required = root.getChild(XML_REQUIRED);
                if (required != null) {
                    children = required.getChildren(XML_ATTRIBUTE);
                    Vector tmp = new Vector();
                    for (i=0; children != null && i<children.size(); i++) {
                        elem = (Element)(children.get(i));
                        a1 = elem.getAttribute(CFArchAttrib.XML_KEY_ARCH);
                        a2 = elem.getAttribute(XML_VALUE);
                        if (a1 == null || a2 == null)
                            System.out.println("In '"+XML_REQUIRED+"' element of type "+t_name+": "+XML_ATTRIBUTE+" missing '"+CFArchAttrib.XML_KEY_ARCH+"' or '"+XML_VALUE+"'.");
                        else {
                            tmp.addElement(a1.getValue().trim());
                            tmp.addElement(a2.getValue().trim());
                        }
                    }

                    // create array and copy vector content into the array: (key1, value1, key2, value2, ...)
                    if (!tmp.isEmpty()) {
                        t_attr = new String[tmp.size()];  // initialize Strings
                        for (i=0; i<tmp.size(); i++)
                            t_attr[i] = (String)(tmp.elementAt(i));
                    }
                }
            }
            catch (NumberFormatException e) {
                // parsing type number failed:
                System.out.println("In "+IGUIConstants.TYPEDEF_FILE+": Type "+t_name+" has invalid type number '"+root.getAttribute("number").getValue()+"'.");
                return false;
            }

            // parse 'ignore' elements
            Element signore = root.getChild(XML_IGNORE);
            if (signore != null) {
                // load all attributes in the ignore section
                children = signore.getChildren(XML_ATTRIBUTE);
                for (i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    a1 = elem.getAttribute(CFArchAttrib.XML_KEY_ARCH);
                    if (a1 == null)
                        System.out.println("In '"+XML_IGNORE+"' section of type "+t_name+": "+XML_ATTRIBUTE+" missing '"+CFArchAttrib.XML_KEY_ARCH+"'.");
                    else {
                        ignoreTable.put(a1.getValue().trim(), "");
                    }
                }

                // load attributes from ignore lists
                children = signore.getChildren("ignore_list");
                for (i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    a1 = elem.getAttribute("name");
                    if (a1 == null)
                        System.out.println("In '"+XML_IGNORE+"' section of type "+t_name+": ignore_list missing 'name'.");
                    else if (tlist.getIgnoreListTable().containsKey(a1.getValue().trim())) {
                        // just copy everything from ignorelist to this ignore section
                        Vector ignlist = (Vector)(tlist.getIgnoreListTable().get(a1.getValue().trim()));
                        for (int k=0; k<ignlist.size(); k++) {
                            ignoreTable.put((String)(ignlist.elementAt(k)), "");
                        }
                    }
                    else
                        System.out.println("In '"+XML_IGNORE+"' section of type "+t_name+": ignore_list with name \""+a1.getValue()+"\" is undefined.");
                }
            }
        }

        // load description
        if ((elem = root.getChild(XML_DESC)) != null) {
            desc = elem.getText().trim();
        }

        // load use
        if ((elem = root.getChild(XML_USE)) != null)
            use = elem.getText().trim();

        // load import_type
        if ((elem = root.getChild(XML_IMPORT_TYPE)) != null) {
            a1 = elem.getAttribute("name");
            if (a1 == null)
                System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': Type "+t_name+" has "+XML_IMPORT_TYPE+" element without 'name'.");
            else
                import_name = a1.getValue().trim();
        }

        // now get all children and proccess them in order:
        children = root.getChildren();
        for (i=0; children != null && i<children.size(); i++) {
            elem = (Element)(children.get(i));
            // attribute directly in type element
            if (elem.getName().equalsIgnoreCase(XML_ATTRIBUTE)) {
                parseAttribute(elem, sec_names, false, null, attr_list, tlist);
            }

            // attributes in a section
            if (elem.getName().equalsIgnoreCase(XML_SECTION) && elem.hasChildren()) {
                a1 = elem.getAttribute("name");
                if (a1 == null) {
                    System.out.println("In "+IGUIConstants.TYPEDEF_FILE+": Type "+t_name+" contains a "+XML_SECTION+" missing 'name'.");
                    in_section = false;  // we'll treat the attributes as "sectionless"
                }
                else {
                    // get section name
                    section = a1.getValue().trim();
                    in_section = true;  // we are now inside a section
                    section_num++;      // increment number of valid sections
                    sec_names.addElement(section);  // tmp. store name
                }

                // parse all attributes in the section
                java.util.List schildren = elem.getChildren();
                for (int k=0; schildren != null && k<schildren.size(); k++) {
                    elem = (Element)(schildren.get(k));
                    if (elem.getName().equalsIgnoreCase(XML_ATTRIBUTE)) {
                        // got an attribute element possibly in a section
                        parseAttribute(elem, sec_names, in_section, section, attr_list, tlist);
                    }
                }
            }
        }

        // ------ now generate the array of attributes: ------
        // calculate how many attributes we've got
        int j=0; int t=0;
        int num_def=0;      // number of default attribs which are not ignored
        int import_num = 0; // number if imported attribs

        j = attr_list.size();

        // don't forget about the default attribs
        if (head != null && head.attr != null && head.attr.length > 0) {
            // create an array to store the refernces to the default atrribs:
            CFArchAttrib[] def_list = new CFArchAttrib[head.attr.length];

            for (; t < head.attr.length; t++) {
                // add all attributes from the default_type which are not in the ignoreTable
                if (!ignoreTable.containsKey(head.attr[t].getNameOld())) {
                    def_list[num_def] = head.attr[t];
                        j++; num_def++;
                }
            }

            // now also count the importet attribs
            CFArchAttrib[] import_list = null;  // imported attribs array

            if (import_name != null) {
                CFArchType imp_type = tlist.getHead();
                boolean found_type = false;
                // search through all known types, looking for import type
                for (; !found_type && imp_type!=null && imp_type!=this;
                     imp_type = (found_type)?imp_type:imp_type.getNext()) {
                    if (imp_type.getTypeName().equalsIgnoreCase(import_name))
                        found_type = true;
                }

                if (found_type) {
                    // initialize array to store imported attribs
                    import_list = new CFArchAttrib[imp_type.attr.length];

                    for (int z=0; z<imp_type.attr.length; z++) {
                        if (!imp_type.attr[z].getSecName().equalsIgnoreCase("general")) {
                            // import this attrib:
                            if (!imp_type.attr[z].getSecName().equalsIgnoreCase("general") &&
                                !imp_type.attr[z].getSecName().equalsIgnoreCase("special") &&
                                !sec_names.contains(imp_type.attr[z].getSecName())) {
                                section_num++; // increment number of valid sections
                                sec_names.addElement(imp_type.attr[z].getSecName());
                            }

                            import_list[import_num] = imp_type.attr[z].getClone();

                            // get section id
                            int new_id = sec_names.indexOf(imp_type.attr[z].getSecName());
                            if (new_id >= 0)
                                import_list[import_num].setSecId(new_id+2);

                            import_num++; j++;
                        }
                    }
                }
                else {
                    System.out.println("Syntax Error in file '"+filename+"' ("+t_name+"):");
                    System.out.println("    import type \""+import_name+"\" not found!");
                }
            }

            attr = new CFArchAttrib[j];   // create array of appropriate size

            // first put in the references to the default attribs:
            for (int k = num_def; k > 0; k--) {
                attr[num_def-k] = def_list[num_def-k];
                //System.out.println("*** ("+(num_def-k)+") "+attr[num_def-k].getNameNew());
            }

            // next put in the references of imported arches (at end of array)
            for (int k = 0; k < import_num; k++) {
                attr[j-import_num + k] = import_list[k];
                //System.out.println("*** ("+(j-import_num + k)+") "+attr[j-import_num + k].getNameNew());
            }
        }
        else {
            attr = new CFArchAttrib[j];   // create array of appropriate size
        }

        // put the list of the (non-default) CFArchAttribs into an array:
        for (i=0; num_def<j && i<attr_list.size(); num_def++, i++) {
            attr[num_def] = (CFArchAttrib)(attr_list.elementAt(i));
        }

        return true; // archtype was parsed correctly
    }

    /**
     * Parse an xml attribute element. If parsing succeeds, the new CFArchAttrib is
     * added to the temporare linked list provided by the parameters (see below).
     * Assigment of sections to attributes also happens in this method.
     *
     * @param elem          the xml attribute element
     * @param sec_names     vector storing all section names
     * @param in_section    true if this attribute belongs to a (custom-defined) section
     * @param section       name of the section (only relevant if 'in_section'==true)
     * @param attr_list     linked list of attributes
     * @param tlist         arch type list
     */
    private void parseAttribute(Element elem, Vector sec_names, boolean in_section, String section,
                                Vector attr_list, CFArchTypeList tlist) {
        // create new instance
        CFArchAttrib attrib = new CFArchAttrib();

        // parse attribute
        if (attrib.load(elem, tlist, t_name)) {
            // add this attribute to the list:
            if (attr_list.size() > 0)
                ((CFArchAttrib)(attr_list.get(attr_list.size()-1))).setNext(attrib);
            attr_list.addElement(attrib);

            // parsing succeeded, now assign this attribute to a section
            if (attrib.getDataType() == CFArchAttrib.T_TEXT) {
                // text attributes all have their own section
                attrib.setSection(section_num, attrib.getNameNew());
                section_num++;
                sec_names.addElement(attrib.getNameNew());  // tmp. store name
            }
            else if (in_section) {
                // if the attribute is in a section, so be it:
                attrib.setSection(section_num-1, section);
            }
            else if (typenr == -1) {
                // default attributes go into the "General" section
                attrib.setSection(0, "General");
            }
            else {
                // sectionless stuff goes into the "Special" section
                attrib.setSection(1, "Special");
            }

            /*
            Attribute a = elem.getAttribute("arch");
            System.out.println("attribute "+(a==null? "null" : a.getValue())+", section "+attrib.getSecId()+" = '"+attrib.getSecName()+"'");
            */
        }
    }

    /**
     * Create the documentation to this ArchObject-type
     * @return the full html-text to be (parsed and) displayed
     */
    public String createHtmlDocu() {
        String text = ""; // String containing the whole html text

        // there are two possible text sizes: normal and large
        boolean big_font = CMainControl.getInstance().isBigFont();

        // write header (not displayed)
        text += "<HTML>\n<HEAD>\n<META NAME=\"CFJavaEditor\" CONTENT=\"tmp\">\n<TITLE>";
        text += "Type: "+t_name;
        text += "</HEAD>\n<BODY>\n";

        text += "<H1 align=center color=navy>Type: "+t_name+"</H1>\n"; // title

        if (desc != null && desc.trim().length() > 0) {
            text += "<P><H"+(big_font?2:3)+" color=navy>Functionality of "+t_name+":</H"+(big_font?2:3)+">";
            if (big_font) text += "<font size=\"4\">";
            text += desc.trim()+"</P>";
            if (big_font) text += "</font>";
        }
        if (use != null && use.trim().length() > 0) {
            text += "<P><H"+(big_font?2:3)+" color=navy>Notes on Usage:</H"+(big_font?2:3)+">";
            if (big_font) text += "<font size=\"4\">";
            text += use.trim()+"</P>";
            if (big_font) text += "</font>";
        }

        /*
        if (attr != null && attr.length > 0) {
            // create table with attributes
            text += "<br><br><TABLE BORDER>";
            for (int i=0; i < attr.length; i++) {
                if (attr[i].getDataType() != CFArchAttrib.T_FIXED &&
                    !attr[i].getSecName().equalsIgnoreCase("general"))
                    text += "<TR><B>"+attr[i].getNameNew()+":</B><br>"+attr[i].getText()+"</TR>";
            }
            text += "</TABLE>";
        }*/

        text += "</BODY>\n</HTML>\n";

        //System.out.println(text);
        return text;
    }

    // convenience functions:
    public CFArchType getNext() {return next;}
    public void setNext(CFArchType cf_type) {next = cf_type;}

    public CFArchAttrib[] getAttr() {return attr;}

    public int getSectionNum() {return section_num;}
    public int getTypeNr() {return typenr;}
    public String getTypeName() {return t_name;}
    public String[] getTypeAttr() {return t_attr;}
    public String getTypeDesc() {return desc;}
    public String getTypeUse() {return use;}

    private void read_till(BufferedReader stream, String tag) throws IOException, EOFException {
        CFileReader.read_till(stream, tag, null);
    }
    private String reads_till(BufferedReader stream, String tag) throws IOException, EOFException {
        return CFileReader.reads_till(stream, tag);
    }
}
