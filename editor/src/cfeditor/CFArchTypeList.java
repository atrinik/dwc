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
import java.util.*;
import javax.swing.JFileChooser;

import org.jdom.*;
import org.jdom.input.*;

/**
 * This class handles all the CFArchTypes and makes
 * them conveniently accessible.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFArchTypeList {
    private CFArchType head = new CFArchType(null);     // head of CFArchType list (head contains default type)

    // spell info:
    private String[] spell_name;     // array of spell names (these all begin with a ' ' space!)
    private int[]    spell_num;      // array of spell numbers

    private Hashtable bitmaskTable;  // table with CAttrBitmask objects (value) sorted by name (key)
    private Hashtable listTable;     // table with Vector objects for lists (value) sorted by name (key)
    private Hashtable ignoreListTable;     // table with Vector objects for ignore_lists (value) sorted by name (key)

    private int length = 0;    // Number of types in the list

    /**
     * Constructor - Parsing all the data from the xml definitions
     * file 'types.xml'.
     */
    CFArchTypeList() {
        CFArchType cf_type = head;    // index of CFArchType list
        boolean head_loaded = false;  // true when the default type (=head) is loaded
        CFileReader fread = null;     // file reader for "types.txt"

        // initialize the arrays of "special-data"
        bitmaskTable = new Hashtable();
        listTable = new Hashtable();
        ignoreListTable = new Hashtable();

        loadSpellsFromXML();      // load spells from file

        try {
            // open ascii filestream to the xml data
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
            fread = new CFileReader(baseDir, IGUIConstants.TYPEDEF_FILE);

            // parse xml document
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(true);
            Document doc = builder.build(fread.getReader());

            // start parsing the xml
            Element root = doc.getRootElement();
            Element elem;
            Attribute a;
            if (root == null || !root.getName().equalsIgnoreCase("types")) {
                System.out.println("File '"+IGUIConstants.TYPEDEF_FILE+"' lacks root element 'types'.");
            }
            else {
                java.util.List children;
                // parse all bitmask elements
                children = root.getChildren("bitmask");
                for (int i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    if (elem.getAttribute("name") == null)
                        System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': cannot load bitmask element without 'name'.");
                    else
                        bitmaskTable.put(elem.getAttribute("name").getValue(), new CAttribBitmask(elem));
                }

                // parse all list elements
                children = root.getChildren("list");
                for (int i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    if (elem.getAttribute("name") == null)
                        System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': cannot load list element without 'name'.");
                    else {
                        Vector list = parseListFromElement(elem);
                        if (list != null && list.size() > 0)
                            listTable.put(elem.getAttribute("name").getValue(), list);
                    }
                }

                // parse default type
                elem = root.getChild("default_type");
                if (elem == null)
                    System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': default_type element is missing!");
                else {
                    // create a new CFArchType element
                    CFArchType new_type = new CFArchType(head);

                    // attach the new CFArchType element to the list
                    // if we manage to parse it properly from the file
                    if (head.load(elem, this)) {
                        cf_type = head;
                        //length++;    // our list is now one element longer
                    }
                }

                // parse ignore lists
                children = root.getChildren("ignore_list");
                for (int i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    if (elem.getAttribute("name") == null)
                        System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': cannot load ignore_list element without 'name'.");
                    else {
                        String lname = elem.getAttribute("name").getValue().trim();
                        java.util.List children2 = elem.getChildren(CFArchType.XML_ATTRIBUTE);
                        if (children2 != null && children2.size() > 0) {
                            // load all attribute entries
                            Vector content = new Vector();
                            for (int k=0; k<children2.size(); k++) {
                                elem = (Element)(children2.get(k));
                                if ((a = elem.getAttribute(CFArchAttrib.XML_KEY_ARCH)) != null)
                                    content.addElement(a.getValue().trim());
                                else
                                    System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': ignore_list '"+lname+"' has "+CFArchType.XML_ATTRIBUTE+" missing '"+CFArchAttrib.XML_KEY_ARCH+"'.");
                            }
                            // now add the list vector to the ignoreListTable:
                            ignoreListTable.put(lname, content);
                        }
                    }
                }

                // parse all type elements
                children = root.getChildren("type");
                for (int i=0; children != null && i<children.size(); i++) {
                    elem = (Element)(children.get(i));
                    if (elem.getAttribute("name") == null || elem.getAttribute("number") == null)
                        System.out.println("In file '"+IGUIConstants.TYPEDEF_FILE+"': found type element without 'name' or 'number'.");
                    else {
                        // create a new CFArchType element
                        CFArchType new_type = new CFArchType(head);

                        // attach the new CFArchType element to the list
                        // if we manage to parse it properly from the file
                        if (new_type.load(elem, this)) {
                            cf_type.setNext(new_type);
                            cf_type = cf_type.getNext();
                            length++;    // our list is now one element longer
                        }
                    }
                }

                System.out.println("Loaded "+length+" types from '"+IGUIConstants.TYPEDEF_FILE+"'");
            }
        }
        catch (JDOMException e) {
            System.out.println("Parsing error in '"+IGUIConstants.TYPEDEF_FILE+"':\n"+e.getMessage()+"\n");
        }
        catch (IOException e) {
            System.out.println("Cannot read file '"+IGUIConstants.TYPEDEF_FILE+"'!");
        }

        // close data stream
        if (fread != null) fread.close();
        ignoreListTable = null; // this was only needed during load phase
    }

    /**
     * Parse a list vector from an xml list element.
     * @param root
     */
    private Vector parseListFromElement(Element root) {
        Vector list = new Vector(); // list vector
        int num;        // number for list element
        String string;  // string for list element

        Element elem;
        Attribute a;
        java.util.List entries = root.getChildren("entry");
        for (int i=0; entries != null && i<entries.size(); i++) {
            elem = (Element)(entries.get(i));
            if (elem.getAttribute("value") == null || elem.getAttribute("name") == null) {
                System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"', list "+root.getAttribute("name").getValue()+": found entry missing 'value' or 'name'.");
            }
            else {
                // every list entry adds value (Integer) and name (String) to the vector
                try {
                    num = elem.getAttribute("value").getIntValue();
                    list.addElement(new Integer(num));
                    list.addElement(" "+elem.getAttribute("name").getValue().trim());
                }
                catch (DataConversionException e) {
                    System.out.println("In '"+IGUIConstants.TYPEDEF_FILE+"', list "+root.getAttribute("name").getValue()+": value '"+elem.getAttribute("value").getValue()+"' is not an integer.");
                }
            }
        }

        return list;
    }

    /**
     * @return: true if this typelist contains no data
     */
    public boolean is_empty() {
        return (head == null || head.getNext() == null);
    }

    /**
     * @return: Number of CFArchTypes in the list.
     *          (Not counting the default type.)
     */
    public int getLength() {
        return length;
    }

    public CFArchType getHead() {return head;}
    public String[] getSpellName() {return spell_name;}
    public int[] getSpellNum() {return spell_num;}
    public Hashtable getBitmaskTable() {return bitmaskTable;}
    public Hashtable getListTable() {return listTable;}
    public Hashtable getIgnoreListTable() {return ignoreListTable;}

    /**
     * Read the spells from "spells.xml" into the arrays 'spell_name'
     * and 'spell_num'. This method is called at startup
     */
    public void loadSpellsFromXML() {
        spell_name = null; spell_num = null;
        CFileReader reader = null;  // input reader
        int spnum = 0;              // number of spells

        try {
            // open reading stream to the spells xml file
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
            reader = new CFileReader(baseDir, IGUIConstants.SPELL_FILE);

            // parse xml document
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader.getReader());

            // retrieve the spell data from the xml
            Element root = doc.getRootElement();
            Element spell_elem;
            Attribute a;
            if (root == null || !root.getName().equalsIgnoreCase("spells")) {
                System.out.println("File '"+IGUIConstants.SPELL_FILE+"' lacks root element 'spells'.");
            }
            else {
                List spells = root.getChildren("spell");
                if (spells == null || spells.size() == 0) {
                    System.out.println("File '"+IGUIConstants.SPELL_FILE+"' has no content.");
                }
                else {
                    // initialize array with appropriate size
                    spell_name = new String[spells.size()+1];
                    spell_num = new int[spells.size()+1];

                    // first element is <none>
                    spell_name[0] = " <none>";
                    spell_num[0] = -1;

                    int i; int j;
                    for (j=0, i=1; j<spells.size(); j++) {
                        spell_elem = (Element)(spells.get(j));

                        if (spell_elem.getAttribute("id") == null) {
                            System.out.println("In File '"+IGUIConstants.SPELL_FILE+"': Found 'spell' element without 'id'");
                        }
                        else if (spell_elem.getAttribute("name") == null) {
                            System.out.println("In File '"+IGUIConstants.SPELL_FILE+"': Found 'spell' element without 'name'");
                        }
                        else {
                            try {
                                // parse spell number and -name
                                spell_num[i] = spell_elem.getAttribute("id").getIntValue();
                                spell_name[i] = spell_elem.getAttribute("name").getValue().trim();
                                i++;
                            } catch (DataConversionException de) {
                                System.out.println("Parsing error in '"+IGUIConstants.SPELL_FILE+"':\n   spell id '"+spell_elem.getAttribute("id").getValue()+"' is not an integer.");
                            }
                        }
                    }

                    // loading successful
                    i--;
                    if (i == spells.size())
                        System.out.println("Loaded "+i+" spells from '"+IGUIConstants.SPELL_FILE+"'");
                    else
                        System.out.println("Loaded "+i+" of "+spells.size()+" defined spells from '"+IGUIConstants.SPELL_FILE+"'");
                }
            }
        }
        catch (JDOMException e) {
            System.out.println("Parsing error in '"+IGUIConstants.SPELL_FILE+"':\n"+e.getMessage()+"\n");
        }
        catch (IOException e) {
            System.out.println("Cannot read file '"+IGUIConstants.SPELL_FILE+"'!");
        }
        if (reader != null)
            reader.close();
    }

    /**
     * Opens a file chooser to select the spellist file,
     * then import spells.
     * @param m_control     main control
     */
    public void importSpellsWanted(CMainControl m_control) {
        // open a file chooser window
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open CF Spellist File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new SpellFileFilter());  // apply file filter
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnVal = fileChooser.showOpenDialog(m_control.getMainView());

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            // now import spells from selected file
            File spellfile = fileChooser.getSelectedFile();
            int spnum = importSpells(spellfile);
            if (spnum > 0) {
                // yeah it worked
                m_control.showMessage("Collect Spells",
                                      "Successfully collected "+spnum+" spells.");
            }
            else {
                // spell collect failed
                m_control.showMessage("Collect Spells",
                                      "Collecting spells failed!\n"+
                                      "Maybe the specified file is of wrong format.");
            }
        }
    }

    /**
     * Read all spells from a CF spellist file and write an
     * alphabetical list into "spells.def"
     *
     * @param spellfile     spellfile to read
     * @return              number of successfully collected spells
     */
    private int importSpells(File spellfile) {
        java.util.Vector list=null;       // growable array of spellnames+numbers
        String tmp;                       // tmp String for spell names

        FileWriter fileWriter;            // file writer for "spells.def"
        BufferedWriter bufferedWriter;    // buffered writer

        if (spellfile.getName().equalsIgnoreCase("spellist.h")) {
            list = new java.util.Vector();
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;

            try {
                fileReader = new FileReader(spellfile.getAbsolutePath());
                bufferedReader = new BufferedReader(fileReader);

                String name;  // spellnames
                CFileReader.read_till(bufferedReader, "spell spells", null);
                CFileReader.read_till(bufferedReader, "{", null);

                // reading spellnames one after the other,
                // this loop is terminated by an EOFException
                int i=0;    // index for insertion in the vector
                for (int counter=0; true; counter++) {
                    CFileReader.read_till(bufferedReader, "{", "}");
                    CFileReader.read_till(bufferedReader, "\"", null);
                    name = CFileReader.reads_till(bufferedReader, "\"");
                    CFileReader.read_till(bufferedReader, "}", null);

                    name = name.trim();
                    // now insert this string lexographically into the vector
                    for (i=0; i<list.size(); i++) {
                        tmp = (String)list.elementAt(i);
                        tmp = tmp.substring(tmp.indexOf(" ")+1);

                        if (name.compareTo(tmp) < 0) {
                            // everything okay, now insert
                            list.insertElementAt((counter+" "+name), i);
                            i=list.size()+10; // end for
                        }
                        else if (name.compareTo(tmp) == 0) {
                            // this spell already exist in the list
                            i=list.size()+10; // end for
                            counter--;  // next loop with same counter value
                        }
                    }

                    // if no insertion spot found, add to end of list
                    if (i<list.size()+10)
                        list.insertElementAt((counter+" "+name), list.size());
                }
            }
            catch (FileNotFoundException e) {
                System.out.println("File '"+spellfile.getAbsolutePath()+"' not found!");
            }
            catch (EOFException e) {
                // end of file/spell struct reached
                try {
                    fileReader.close();
                    bufferedReader.close();
                } catch (IOException ioe) {}
            }
            catch (IOException e) {
                System.out.println("Cannot read file '"+spellfile.getAbsolutePath()+"'!");
            }
        }

        // --------- now write the "spells.def" file ---------
        if (list != null && list.size()>0) {
            File dfile = null;
            try {
                // create new file for writing (replaces old one if existent)
                if (IGUIConstants.CONFIG_DIR != null && IGUIConstants.CONFIG_DIR.length() > 0) {
					String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
					
					File dir = new File(baseDir);					
					if (!dir.exists() || !dir.isDirectory()) {
                        // create the config dir
                        dir = new File("resource"); dir.mkdir();
                        dir = new File(IGUIConstants.CONFIG_DIR); dir.mkdir();
                    }

                    dfile = new File(baseDir+File.separator+IGUIConstants.SPELL_FILE);
                }
                else
                    dfile = new File(IGUIConstants.SPELL_FILE);

                fileWriter = new FileWriter(dfile);
                bufferedWriter = new BufferedWriter(fileWriter);

                // header:
                bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                bufferedWriter.write("<spells>\n<!--\n");
                bufferedWriter.write("  ##########################################################\n");
                bufferedWriter.write("  #   You may add new spells to this file, but there's no  #\n");
                bufferedWriter.write("  #  need to do it because the file can be autogenerated.  #\n");
                bufferedWriter.write("  # In the editor, select menu \"Resources->Collect Spells\" #\n");
                bufferedWriter.write("  #        to generate a new version of this file.         #\n");
                bufferedWriter.write("  ##########################################################\n-->\n");

                // write spell-entries:
                String name;   // spell names
                String id;     // spell numbers
                String space;  // space-buffer to have names starting on equal position
                for (int i=0; i<list.size(); i++) {
                    tmp = String.valueOf(list.elementAt(i));
                    id = tmp.substring(0, tmp.indexOf(" ")).trim();
                    name = tmp.substring(tmp.indexOf(" ")+1).trim();
                    space = "";
                    if (id.length() == 1)
                        space = "  ";
                    else if (id.length() == 2)
                        space = " ";
                    bufferedWriter.write("  <spell id=\""+id+"\""+space+" name=\""+name+"\" />\n");
                }
                bufferedWriter.write("</spells>");

                bufferedWriter.close();
                fileWriter.close();
                return list.size();
            }
            catch (IOException e) {
                System.out.println("Cannot write file '"+dfile.getAbsolutePath()+"'!");
            }
        }

        return 0;
    }

    /**
     * Find and return the type-structure (<code>CFArchType</code>) that
     * matches for the given arch. This is not only a comparison between
     * type numbers - special type-attributes must also be dealt with.
     *
     * IMPORTANT: A similar-but-nonequal version of this code is used in
     * CAttribDialog. Hence, if modifying this method, the appropriate parts
     * in CAttribDialog must also be updated and vice-versa.
     *
     * @param arch   the arch to find the type for
     * @return       the <code>CFArchType</code> which belongs to this arch,
     *               or the first (misc) type if no match is found.
     */
    public CFArchType getTypeOfArch(ArchObject arch) {
        // check if the type of the object is present in the definitions
        CFArchType tmp = getHead().getNext(); // tmp cycles through all types
        CFArchType type = tmp;  // return value: the type of the arch, first one (misc) if no other found

        ArchObject defarch = arch.getDefaultArch();
        boolean type_found = false;

        for (; tmp != null && !type_found; tmp = tmp.getNext()) {
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

                        if (!archvalue.equals(tmp.getTypeAttr()[t+1]) &&
                            !(tmp.getTypeAttr()[t+1].equals("0") && archvalue.length()==0)) {
                            match = false;
                        }
                    }

                    // we've got a match after all
                    if (match) type_found = true;
                }

                // we found our type
                if (type_found) {
                    type = tmp; // this is the type we are looking for
                }
            }
        }

        return type;
    }

    /**
     * Find and return the type-structure (<code>CFArchType</code>) that
     * matches the given 'type_name'. These type-names are "artificial"
     * names, defined in "types.txt". They appear in the type selection box
     * in the attribute-dialog.
     *
     * @return       the <code>CFArchType</code> that matches,
     *               or the first (misc) type if no match is found.
     */
    public CFArchType getTypeByName(String type_name) {
        // return: matching type or first type (misc) if no other found
        CFArchType type = getHead().getNext();
        boolean type_found = false; // true when type is found

        for (CFArchType tmp = type; tmp != null && !type_found; tmp = tmp.getNext()) {
            if (tmp.getTypeName().equals(type_name.trim())) {
                // we found our type
                type = tmp;
                type_found = true;
            }
        }

        return type;
    }

    /**
     * Subclass: FileFilter to accept only known CF spellist files
     */
    class SpellFileFilter extends javax.swing.filechooser.FileFilter {
        public SpellFileFilter() {}

        /**
         * The description of this filter. For example: "JPG and GIF Images"
         * @return filter description
         */
        public String getDescription() {
            return "spellist.h";
        }

        /**
         * Whether the given file is accepted by this filter
         * @param f    any file
         * @return     true if the file is accepted as spellist
         */
        public boolean accept(File f) {
            if (f.isDirectory() ||
                f.getName().equalsIgnoreCase("spellist.h"))
                return true;
            return false;
        }
    }
}
