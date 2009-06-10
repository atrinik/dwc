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

import java.util.*;
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;

/**
 * The <code>ArchObjectParser</code> class handles the parsing of arches.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ArchObjectParser {

    // name of the system-arch containing path of starting map
    public static final String STARTARCH_NAME = "map";

    // table with type numbers as keys (Integer), and type names as values (String)
    private Hashtable archTypeNumbers;

    private CMainControl m_control;

    /**
     * Constructor
     * @param control   main control
     */
    public ArchObjectParser(CMainControl control) {
        m_control = control;
        archTypeNumbers = null;
    }

    /**
     * Parse the typenumbers file (associate names with type numbers)
     * Type numbers and names are stored as key-value pairs in the
     * Hashtable 'archTypeNumbers'.
     */
    public void loadTypeNumbers() {
        CFileReader reader = null;  // input reader
        archTypeNumbers = new Hashtable();

        try {
            // open reading stream to the spells xml file
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
            reader = new CFileReader(baseDir, IGUIConstants.TYPENR_FILE);

            // parse xml document
            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(true);
            Document doc = builder.build(reader.getReader());

            // retrieve the spell data from the xml
            Element root = doc.getRootElement();
            Element type_elem;
            Attribute a;
            if (root == null || !root.getName().equalsIgnoreCase("typenumbers")) {
                System.out.println("File '"+IGUIConstants.TYPENR_FILE+"' lacks root element 'typenumbers'.");
            }
            else {
                java.util.List types = root.getChildren("type");
                if (types == null || types.size() == 0) {
                    System.out.println("File '"+IGUIConstants.TYPENR_FILE+"' has no content.");
                }
                else {
                    // process all 'type' elements from the xml file
                    int i;
                    for (i=0; i<types.size(); i++) {
                        type_elem = (Element)(types.get(i));

                        if (type_elem.getAttribute("number") == null) {
                            System.out.println("In File '"+IGUIConstants.TYPENR_FILE+"': Found 'type' element without 'number'");
                        }
                        else if (type_elem.getAttribute("name") == null) {
                            System.out.println("In File '"+IGUIConstants.TYPENR_FILE+"': Found 'type' element without 'name'");
                        }
                        else {
                            try {
                                // parse type number and -name, then add it to the table 'archTypeNumbers'
                                Integer typenum = new Integer(type_elem.getAttribute("number").getIntValue());
                                archTypeNumbers.put(typenum, type_elem.getAttribute("name").getValue());
                            }
                            catch (DataConversionException de) {
                                System.out.println("Parsing error in '"+IGUIConstants.TYPENR_FILE+"':\n   type number '"+type_elem.getAttribute("number").getValue()+"' is not an integer.");
                            }
                        }
                    }

                    // loading successful
                    //System.out.println(""+i+" typenumbers loaded.");
                }
            }
        }
        catch (JDOMException e) {
            System.out.println("Parsing error in '"+IGUIConstants.TYPENR_FILE+"':\n"+e.getMessage());
        }
        catch (IOException e) {
            System.out.println("Cannot read file '"+IGUIConstants.TYPENR_FILE+"'!");
        }
        if (reader != null)
            reader.close();
    }

    /**
     * Lookup the name of an archtype.
     * @param index   type number
     * @return name of this type, as defined in "typenumbers.xml"
     */
    public String getArchTypeName(int index) {
        Integer tnum = new Integer(index);

        if (archTypeNumbers != null && archTypeNumbers.containsKey(tnum)) {
            return (String)(archTypeNumbers.get(tnum));
        }
        return "*UNKNOWN"+index+"*"; // this type is unknown
    }

    /**
     * Read a default arch from a file
     *
     * @param fname       filename of the arch definition file (*.arc)
     * @param index       current subdirectory-index on the ArchPanel (-> ComboBoxes)
     */
    public void parseDefArch(String fname, int index) {
        String thisLine, thisLine2;
        boolean parsearch, archmore, msgflag, animflag, scriptflag;
        int first_arch, archmore_count,x,y;
        ArchObject arch;

        //Open the file for reading
        try {
            FileReader fr =  new FileReader(fname);
            BufferedReader myInput = new BufferedReader(fr);

            // do the actual parsing
            parseDefArchFromStream(myInput, index);

            myInput.close();
            fr.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Archfile "+fname+" could not be found");
        }
        catch (IOException e) {
            System.out.println("IOException in parseDefArch!");
        }
    }

    public void parseDefArchFromStream(BufferedReader myInput, int index) {
        parseDefArchFromStream(myInput, null, null, null, index);
    }

    /**
     * Here we take a default arch from a filestream, parses the data and
     * put the result on the arch stack in CMainControl.
     *
     * @param myInput     <code>BufferedReader</code> file stream of arch data
     * @param def_arch    default arch (only for artifacts)
     * @param line        first line, pre-parsed (only for artifacts)
     * @param arch_name   arch-object name (only for artifacts)
     * @param index       current index on the ArchPanel
     */
    public ArchObject parseDefArchFromStream(BufferedReader myInput, ArchObject def_arch, String line, String arch_name, int index) {
        String thisLine;  // read line with trimmed spaces
        String thisLine2; // read line, original spaces
        String oldCath, newCath; // display cathegory
        boolean parsearch, archmore, msgflag, animflag, loreflag, scriptflag, isNewCathegory;
        int first_arch, archmore_count,x,y;
        ArchObject arch;

        //Open the file for reading
        try {
            msgflag = false;
            loreflag = false;
            animflag = false;
            parsearch = false;    // we try to find a arch object
            archmore = false;     // no arch before...
            first_arch = -1;      // we have no multi part/multi file monsters, this is first
            archmore_count = 0;   // thats part nr x of first_arch multi arch
            oldCath = "<xxx>";    // old cathegory
            newCath = "no cathegory info";  // default cathegory
            isNewCathegory = true;

            // start with new clean ArchObject instance
            if(def_arch == null)
                arch = new ArchObject();
            else
                arch = def_arch.getClone(0,0);
            arch.resetArchText();

            if(line==null)
                thisLine2 = myInput.readLine();
            else
                thisLine2 = line; // pre read "Object" from artifacts file loader

            while (thisLine2 != null) {
                thisLine = thisLine2.trim();

                if(thisLine.regionMatches(0,"#", 0, 1)) {
                    // skip comments
                    thisLine2 = myInput.readLine();
                    continue;
                }

                if(parsearch == false) {

                    if(thisLine.regionMatches(0,"More", 0, 4)) {
                        if(first_arch == -1) {
                            first_arch = m_control.getArchCount()-1; // if more && !-1 last was first
                            m_control.getArch(first_arch).setRefNr(first_arch);
                        }
                        m_control.getArch(first_arch).setRefCount(m_control.getArch(first_arch).getRefCount()+1);

                        archmore = true;
                    } else if(thisLine.regionMatches(0,"Object", 0, 6)) {
                        if(arch == null)
                            arch = new ArchObject();

                        parsearch = true;
                        if(archmore == true) {
                            archmore_count++;
                            //  System.out.println("multi part object: part "+ archmore_count);
                        } else {
                            first_arch = -1;
                            archmore_count = 0;
                        }

                        if(arch_name == null)
                            arch.setArchName(thisLine.substring(7));
                        else
                            arch.setArchName(arch_name);
                    }

                }
                else {
                    if(msgflag == true) {
                        if(thisLine.regionMatches(0,"endmsg", 0, 6)) {
                            msgflag=false;
                        }
                        else
                            arch.addMsgText(thisLine2+"\n"); // thisLine2 allows leading whitespaces
                    }
                    else if(animflag == true) {
                        if(thisLine.regionMatches(0,"mina", 0, 4)) {
                            m_control.animationObject.addAnimObject(
                                                                    arch.getArchName(), arch.getAnimText());

                            arch.addArchText("animation "+arch.getArchName()+"\n");
                            arch.setAnimName(arch.getArchName());

                           // here we must add this to AnimationObject
                            // and add Animation cmd here!
                            animflag=false;
                        } else
                            arch.addAnimText(thisLine+"\n");
                    }
                    else if(loreflag == true) {
                        if(thisLine.regionMatches(0,"endlore", 0, 7)) {
                            loreflag=false;
                        }
                        else {
                            arch.addLoreText(thisLine+"\n");
                        }
                    }
                    else if(thisLine.regionMatches(0,"Object", 0, 6)) {
                        System.out.println("ERROR: Find inventory Object in def arch: "+thisLine);
                    }
                    else if(thisLine.regionMatches(0,"end", 0, 3)) {
                        //if(arch.getArchTypNr() == 0)
                        //System.out.println("Arch "+ arch.getArchName()+" has no type info!");
                        // we got full arch
                        parsearch = false; // we write this sucker

                        arch.setRefNr(first_arch);
                        if(first_arch != -1) {
                            // add to head our x/y position so he can setup refmax
                            m_control.getArch(first_arch).setRefMaxX(arch.getRefX());
                            m_control.getArch(first_arch).setRefMaxY(arch.getRefY());
                            arch.setRefFlag(true);      // mark it as ref
                        } else {
                            m_control.incArchObjCount();

                            // add arch to the archpanel - only if it is not the map arch
                            if (!arch.getArchName().equals(STARTARCH_NAME)) {
                                if (!ArchObjectStack.isLoadedFromArchive() || arch_name != null) {
                                    // loading from individual files, so we simply add it to list
                                    // ArchObjectStack.loadArchFromFiles() takes care of the panels
                                    m_control.addArchPanelArch(m_control.getArchCount(), index);
                                }
                                else {
                                    // loading from collected files, so we need process panels here
                                    if (isNewCathegory) {
                                        String folder = newCath; // main folder of new cath
                                        if (newCath.indexOf("/") > 0)
                                            folder = newCath.substring(0, newCath.indexOf("/"));

                                        if (!oldCath.startsWith(folder)) {
                                            // an entire new panel must be opened
                                            m_control.addArchPanel(folder);
                                            m_control.addArchPanelCombo("show all");
                                            m_control.addArchPanelCombo(folder);
                                            index = 1;
                                        }

                                        if (newCath.indexOf("/") > 0) {
                                            folder = newCath.substring(newCath.indexOf("/")+1);
                                            if (newCath.startsWith(folder))
                                                index = 1; // add to the base folder
                                            else if (!oldCath.endsWith(folder)) {
                                                // a new JComboBox must be added
                                                index = m_control.addArchPanelCombo(folder);
                                            }
                                        }
                                        else
                                            index = 1; // add to the base folder

                                        isNewCathegory = false;
                                        oldCath = newCath;
                                    }
                                    m_control.addArchPanelArch(m_control.getArchCount(), index);
                                }
                            }
                        }
                        postParseDefArch(arch);
                        m_control.addArchToList(arch);

                        if (IGUIConstants.isoView && archmore)
                            calcLowestMulti(arch);
                        archmore = false;  // we assume this is last... but perhaps..

                        // if this arch was from Artifacts file - return here:
                        if(arch_name != null) {
                            arch.setArtifactFlag(true);
                            // here we add all unchanged arch text lines from def_arch back to arch
                            arch.addArchText(arch.diffArchText(def_arch.getArchText(),true));
                            return arch;
                        }
                        arch = null;
                    }
                    else {
                        if(thisLine.regionMatches(0,"msg", 0, 3)) {
                            msgflag=true;
                        }
                        else if(thisLine.regionMatches(0,"animation", 0, 9)) {
                            arch.addArchText(thisLine+"\n");
                            arch.setAnimName(thisLine.substring(10).trim()+"\n");
                        }
                        else if(thisLine.regionMatches(0,"anim_speed", 0, 10)) {
                            arch.addArchText(thisLine+"\n");
                        }
                        else if(thisLine.regionMatches(0,"anim", 0, 4)) {
                            animflag=true;
                        }
                        else if(thisLine.equals("lore")) {
                            loreflag=true;
                        } else if(thisLine.regionMatches(0,"visibility ", 0, 11)) {
                            //  System.out.println("Remove visibility: "+arch.getArchName());
                        } else if(thisLine.regionMatches(0,"magicmap ", 0, 9)) {
                            //  System.out.println("Remove magicmap: "+arch.getArchName());
                        } else if(thisLine.regionMatches(0,"color_fg ", 0, 9)) {
                            //  System.out.println("Remove color_fg: "+arch.getArchName());
                        } else if(thisLine.regionMatches(0,"color_bg ", 0, 9)) {
                            //  System.out.println("Remove color_bg: "+arch.getArchName());
                        } else if(thisLine.regionMatches(0,"x ", 0, 2)) {
                            if(!archmore && !arch.getArchName().equals(STARTARCH_NAME)) {
                                System.out.println("Find x cmd in single tile or head (add it to arch text): "+ arch.getArchName());
                                arch.addArchText(thisLine+"\n");
                            }
                            arch.setRefX(Integer.parseInt(thisLine.substring(2)));
                        } else if(thisLine.regionMatches(0,"y ", 0, 2)) {
                            if(!archmore && !arch.getArchName().equals(STARTARCH_NAME)) {
                                System.out.println("Find y cmd in single tile or head (add it to arch text): "+ arch.getArchName());
                                arch.addArchText(thisLine+"\n");
                            }
                            arch.setRefY(Integer.parseInt(thisLine.substring(2)));
                        } else if(thisLine.regionMatches(0,"type ", 0, 5)== true) {
                            try {
                                int i = Integer.parseInt(thisLine.substring(5));
                                arch.setArchTypNr(i);
                                if(i == 0)
                                    System.out.println("WARNING: Arch "+ arch.getArchName()+" type number is zero. ("+thisLine.substring(5)+")");
                            } catch (Exception e) {
                                System.out.println("WARNING: Arch "+ arch.getArchName()+" has a invalid type nr. ("+thisLine.substring(5)+")");
                                arch.addArchText(thisLine+"\n");
                            }
                          } else if(thisLine.regionMatches(0,"direction ", 0, 10)== true) {
                            int i=0;
                              try {
                                i = Integer.parseInt(thisLine.substring(10));
                                arch.setDirection(i);
                            } catch (Exception e) {
                                System.out.println("WARNING: Arch "+ arch.getArchName()+" has a invalid direction. ("+thisLine.substring(10)+")");
                            }
                            arch.addArchText(thisLine+"\n");
                        } else if(thisLine.regionMatches(0,"face ", 0, 5)) {
                            for(x=4;x<thisLine.length();x++) {
                                if(thisLine.charAt(x) != ' ')
                                    break;
                            }
                            arch.setFaceRealName(thisLine.substring(x,thisLine.length()));
                            arch.addArchText(thisLine+"\n");
                        } else if(thisLine.startsWith("editor_folder ")) {
                            // the display cathegory (= "folder" the arch belongs to)
                            newCath = thisLine.substring(14).trim();
                            if (!newCath.equals(oldCath))
                                isNewCathegory = true; // this arch has a new cathegory
                        } else if (IGUIConstants.isoView && thisLine.startsWith("mpart_id ")) {
                            // shape ID for multiparts
                            try {
                                int i = Integer.parseInt(thisLine.substring(9).trim());
                                arch.setMultiShapeID(i);

                                if(i <= 0 || i >= MultiPositionData.Y_DIM)
                                    System.out.println("WARNING: Arch "+ arch.getArchName()+" mpart_id number is '"+thisLine.substring(9)+"'");
                            } catch (Exception e) {
                                System.out.println("WARNING: Arch "+ arch.getArchName()+" has a invalid mpart_id ("+thisLine.substring(9)+")");
                                arch.addArchText(thisLine+"\n");
                            }
                        } else if (IGUIConstants.isoView && thisLine.startsWith("mpart_nr ")) {
                            // part nr for multiparts
                            try {
                                int i = Integer.parseInt(thisLine.substring(9).trim());
                                arch.setMultiPartNr(i);
                            } catch (Exception e) {
                                System.out.println("WARNING: Arch "+ arch.getArchName()+" has a invalid mpart_nr ("+thisLine.substring(9)+")");
                                arch.addArchText(thisLine+"\n");
                            }
                        } else
                            arch.addArchText(thisLine+"\n");
                        //System.out.println("add String: "+thisLine);
                    }
                }
                thisLine2 = myInput.readLine();
            } // while loop ends here
        }  catch (IOException e) {
            System.out.println("Error: " + e);
        }
        return null;
    }

    /**
     * I drop this here ... we got 2 functions now but i want the hardcore parsing
     * cut off from load parsing
     *
     * @param arch     default arch to be parsed
     */
    public void postParseDefArch(ArchObject arch) {
        boolean scriptflag = false;
        String text = new String(arch.getArchText());
        arch.resetArchText();
        int len = text.length();

        // if no type was set, zero is taken
        if (arch.getArchTypNr() == ArchObject.TYPE_UNSET)
            arch.setArchTypNr(0);

        for(int i=0, s=0;i<len;i++) {

            if(text.charAt(i) == 0x0a) {
                if(i-s >0) {
                    if(scriptflag == true) {
                        arch.addArchText(text.substring(s,i)+"\n");
                        if(text.regionMatches(s,"end_script_", 0, 11))
                            scriptflag=false;
                    } else if(text.regionMatches(s,"start_script_", 0, 13)) {
                        arch.addArchText(text.substring(s,i)+"\n");
                        scriptflag=true;
                    } else if(text.regionMatches(s,"editable ", 0, 9)) {
                        arch.setEditType(Integer.parseInt(text.substring(s+9,i)));
                    } else if(text.regionMatches(s,"name ", 0, 5)) {
                        arch.setObjName(text.substring(s+5,i));
                    } else {
                        arch.addArchText(text.substring(s,i)+"\n");
                    }

                }
                s=i+1;
            }
        }

        // default arches don't get an edit_type (not worth the time)
        // they get one assigned as soon as put on a map though.
        arch.setEditType(IGUIConstants.TILE_EDIT_NONE);
    }

    /**
     * Here we go... thats the hard one
     * we copy the needed values from default arch to map arch
     * we setup the panels to show the right values
     *
     * first: the anim list... we want handle this later so we ignore this yet
     * and let it in the arch text windows for handwork
     *
     * 2nd: msg/msgend
     * we had in the map a msg/msgend cmd or not
     * because we init our msg buffer only when one cmd come in
     * we have a null-ptr or a text
     * if text, this will overrule ALWAYS the default text
     * if null, copy the default text in arch, so the user can see and edit it
     * if at save time msg-maparch == msg-default, we ignore it
     * in every other case (even "" text) we save
     *
     * @param arch           map arch to be parsed
     * @param edit_type      edit type(s) to be calculated for the arch
     */
    public void postParseMapArch(ArchObject arch, int edit_type) {
        if(arch.getNodeNr() == -1)
            return;
        String text = new String(arch.getArchText());
        int len = text.length();
        ArchObject defarch = m_control.getArch(arch.getNodeNr());

        arch.resetArchText();
        // so, lets check the stuff a last time
        for(int i=0, s=0;i<len;i++) {

            if(text.charAt(i) == 0x0a) {
                if(i-s >0) {
                  if(text.regionMatches(s,"animation ", 0, 9)) {
                      arch.setAnimName(text.substring(s+10,i));
                      arch.addArchText(text.substring(s,i)+"\n");
                  }
                  else  if(text.regionMatches(s,"name ", 0, 5)) {
                        arch.setObjName(text.substring(s+5,i));
                    } else  {
                        // this is an unparsed arch attribute, it has to stay in the archtext
                        arch.addArchText(text.substring(s,i)+"\n");
                    }
                }
                s=i+1;
            }
        }
        if(arch.getDirection() == -1) // still the invalid direction!
          arch.setDirection(defarch.getDirection());
        arch.setRealFace(arch.getFaceRealName());
        // if the type is still unset, then we take the default one
	if (arch.getArchTypNr() == ArchObject.TYPE_UNSET)
            arch.setArchTypNr(defarch.getArchTypNr());

        // if the type is still unset, then we take the default one
	if (IGUIConstants.isoView) {
            if (defarch.getMultiShapeID() > 0 && arch.getMultiShapeID() == 0)
                arch.setMultiShapeID(defarch.getMultiShapeID());
            if (defarch.getMultiPartNr() > 0 && arch.getMultiPartNr() == 0)
                arch.setMultiPartNr(defarch.getMultiPartNr());
            if (defarch.isLowestPart())
                arch.setLowestPart(true);
        }

        if (arch.isMulti() || defarch.isMulti()) {
            if (!arch.isMD()) arch.initMultiData(); // make sure the MultiArchData is initialized

            arch.setRefCount(defarch.getRefCount());
            arch.setRefFlag(defarch.getRefFlag());
            arch.setRefMaxX(defarch.getRefMaxX());
            arch.setRefMaxY(defarch.getRefMaxY());
            arch.setRefMaxMX(defarch.getRefMaxMX());
            arch.setRefMaxMY(defarch.getRefMaxMY());
            arch.setRefX(defarch.getRefX());
            arch.setRefY(defarch.getRefY());
        }

       // arch.setEditType(defarch.getEditType());

        // validate the ScriptedEvents
        arch.validateAllEvents();

        // we don't handle anim yet, so attach then back to archtext
        if(arch.getAnimText() != null) {
            arch.addArchText("anim\n");
            arch.addArchText(arch.getAnimText());
            arch.addArchText("mina\n");
        }

        // Finally, we calculate the desired edit_type of the arch
        if (arch.getRefFlag() && arch.getMapMultiHead() != null)
            arch.setEditType(arch.getMapMultiHead().getEditType()); // copy from head
        else if (edit_type != 0)
            arch.setEditType(arch.calculateEditType(edit_type));    // calculate new
    }

    /**
     * If the given arch is a multipart head, we generate the appropriate
     * tail (from the arch stack) and attach it. The new arches get added
     * to the temp list, not a map. This method should only be called
     * after map-loading.
     * The ArchObjectStack should be fully initialized at this point.
     *
     * @param arch     multipart head that needs tail attached
     */
    public void expandMulti(ArchObject arch) {

      if(arch.getContainer() != null)
        return;

        ArchObject defarch = m_control.getArch(arch.getNodeNr());  // default arch

        // is it a multi head?
        if (arch != null && defarch != null && defarch.getRefCount() > 0 &&
            arch.getMapMultiNext() == null &&  arch.getMapMultiHead() == null) {
            // we have a multi head and need to insert his tail now

            int count = defarch.getRefCount();  // how many parts have we got
            ArchObject newarch = null;  // newly inserted arch
            ArchObject oldarch = arch;  // previous arch
            ArchObject tmp_next = arch.getTemp();  // next arch on tmp list after multi

            // do insertion for all non-head parts of the multi
            for (int c=1; c<=count; c++) {
                newarch = m_control.getArchObjectStack().newArchObjectInstance(defarch.getNodeNr()+c);

                newarch.setMapMultiHead(arch);    // set link to multi head
                oldarch.setMapMultiNext(newarch); // set link between multi arches
                oldarch.setTemp(newarch);         // attach to temp list

                                // set map position (x, y)
                newarch.setMapX(arch.getMapX() + m_control.getArchObjectStack().getArch(defarch.getNodeNr()+c).getRefX());
                newarch.setMapY(arch.getMapY() + m_control.getArchObjectStack().getArch(defarch.getNodeNr()+c).getRefY());

                oldarch = newarch;  // next loop oldarch = current newarch

                                // now attach the default arch and stuff
                                // (don't need edit type as we copy from head)
                m_control.archObjectParser.postParseMapArch(newarch, 0);
            }
            newarch.setTemp(tmp_next); // re-attach the the last multi to the tmp list
        }
    }

    /**
     * Sort the temp list of arches, placing all multiparts at the end.
     * This is neccessary to assure multiparts are always on top.
     * Note: This method assumes that all pieces of one multipart are
     * listed in a row. (Which is okay as long as the loader puts it that way.)
     *
     * @param start    First arch in the list
     * @return the sorting might eventually change the starting
     *         arch. Therefore, the new start arch is returned.
     */
    public ArchObject sortTempList(ArchObject start) {
        ArchObject last_before = null; // the last arch on the list (before sorting)
        ArchObject last = null;        // the very last arch on the list
        ArchObject new_start = null;   // return value: new start arch of list
        boolean found_multi = false;   // true when multi found (see below)
        boolean order_wrong = false;   // is the order correct or not?

        // First we cycle through the whole list and check if the
        // order is already correct or not (only Crossedit saves in wrong order).
        // This takes practically no time and assures we don't waste any.
        for (ArchObject tmp = start; tmp != null; tmp = tmp.getTemp()) {
            if (!found_multi && tmp.isMulti())
                found_multi = true;  // first multi appeared
            else if (found_multi && !tmp.isMulti() &&
                     (tmp.getContainer() == null || !tmp.getTopContainer().isMulti()))
                order_wrong = true;  // order is wrong (found non-multi after multi)

            if (tmp.getTemp() == null)
                last_before = last = tmp;
        }

        // if the last arch is a multi, it must be the head, not the tail
        if (last_before.getRefFlag() && last_before.getMapMultiHead() != null)
            last_before = last_before.getMapMultiHead();

        if (order_wrong) {
            // The order is wrong, so we gotta correct it now
            ArchObject previous = null;  // previous arch in loop
            boolean increment = true;    // if true, do increment after loop

            System.out.println("Resorted multipart arches.");

            for (ArchObject tmp = start; tmp != null && tmp != last_before;) {
                if (tmp.getRefCount() > 0 && tmp.getMapMultiNext() != null) {
                    // Got a multi head - Now we move his ass to the end of the list:
                    last.setTemp(tmp);  // attach the head to the end
                    for (; tmp.getMapMultiNext() != null; tmp = tmp.getMapMultiNext());

                    // close the list: linking previous to the arch after the multi
                    if (previous == null || tmp.getMapMultiHead() == start) {
                        // very special case: the start arch is a multi
                        previous = tmp.getTemp();  // continue after this first multi
                        start = tmp.getTemp();     // start arch is now this one
                        increment = false;
                    } else
                        previous.setTemp(tmp.getTemp());

                    tmp.setTemp(null);  // terminate tail
                    last = tmp;         // last arch is now this one
                    tmp = previous;     // we continue our search after previous
                }

                previous = tmp;  // next loop tmp is previous

                if (increment)
                    tmp = tmp.getTemp();  // jump to next node
                else
                    increment = true;

                                // make sure we don't ever get into an infinite loop
                if (tmp != null && tmp.getTemp() == tmp) {
                    System.out.println("sortTempList: Arch '"+tmp.getArchName()+"' pointing on itself!");
                    tmp = null;  // exit the loop
                }
            }
        }

        // return the new starting arch
        return start;
    }

    /**
     * Calculate the lowest part of this multi-arch. This lowest part is needed
     * because in ISO view, the big image is drawn for it's lowest part, in order
     * to get the overlappings correct.
     *
     * (TODO: This method is called repeatedly for each multipart. It would be
     * better if it was called only once per multipart.)
     *
     * @param arch   last tail part of this multi
     */
    private void calcLowestMulti(ArchObject arch) {
        ArchObject tmp = arch;
        int minYOffset = 10000; // minimal offset
        int count = 0;      // count number of tiles
        int t;              // tmp. storage

        // 1.step: find the maximal y-offest
        while (tmp.getRefCount() <= 0 && tmp.getNodeNr() > 0) {
            t = MultiPositionData.getYOffset(tmp.getMultiShapeID(), tmp.getMultiPartNr());
            if (t < minYOffset) minYOffset = t;

            // get next multipart piece
            tmp = m_control.getArch(tmp.getNodeNr()-1);
            count++;
        }
        t = MultiPositionData.getYOffset(tmp.getMultiShapeID(), tmp.getMultiPartNr());
        if (t < minYOffset) minYOffset = t;

        // 2.step: set 'lowestPart' flag for all tiles with maximum offset
        for (int i=0; count >= 0; count--, i++) {
            t = MultiPositionData.getYOffset(tmp.getMultiShapeID(), tmp.getMultiPartNr());
            if (t <= minYOffset) {
                tmp.setLowestPart(true);
            }
            else
                tmp.setLowestPart(false);
            tmp = m_control.getArch(tmp.getNodeNr()+1);
        }

    }
};
