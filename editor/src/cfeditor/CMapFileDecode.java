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

/**
 * This class handles the reading of a mapfile and parsing the data
 * into a list of ArchObjects.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMapFileDecode {
    private int maxxlen, maxylen;
    private ArchObject start;       // points to the very first arch of the map
    private ArchObject previous;    // always pointing to the previously read arch
    //private ArchObject first_arch;  // first arch for multis (last arch not being "more")
    private MapArchObject maparch;  // contains the map arch (see MapArchObject class)

    private CMainControl main_control;  // link to the main control (for archlist)

    public CMapFileDecode() {
    };

    public MapArchObject getMapArch() {
        return maparch;
    }

    /**
     * Loading a Crossfire map from the given mapfile.
     * This method returns a list of arches, connected with the temp pointers.
     *
     * @param file                 mapfile
     * @param m_ctrl               main control
     * @return                     first <code>ArchObject</code> in the list
     * @throws CGridderException   when file content invalid
     */
    public ArchObject decodeMapFile(File file, CMainControl m_ctrl) throws CGridderException {
        String thisLine;
        String fname=null;

        main_control = m_ctrl;  // set link to main control

        try {
            FileReader fr =  new FileReader(file);
            BufferedReader myInput = new BufferedReader(fr);

            maxxlen=maxylen=0;
            start = previous = null;
            //first_arch = null;
            maparch = null;

            // first of all we read the map arch (if that fails we throw an exception)
            maparch = new MapArchObject();
            if(file.getCanonicalPath().startsWith(main_control.m_mapDir.getCanonicalPath() ))
              fname = file.getCanonicalPath().substring(main_control.m_mapDir.getCanonicalPath().length());
            else
              fname = file.getCanonicalPath();
            if (!maparch.parseMapArch(myInput, fname))
                throw new CGridderException("The file '"+file.getName()+"' does not\n"+
                                            "contain a valid Crossfire map format!\n");
            //maparch.setName(file.getName());

            // now we read all the ArchObjects
            while((thisLine = myInput.readLine()) != null) {
                readArch(myInput, thisLine, null);  // all these are map arches
            }
            myInput.close();
            fr.close();

        } catch (IOException e) {
            System.out.println("Error Load: " + e);

            for(ArchObject tarch=start, temparch;tarch != null;) {
                temparch=tarch;
                temparch.setTemp(null);
                temparch.setMapMultiHead(null);
                temparch.setMapMultiNext(null);
                tarch = tarch.getTemp();
            }
            return null;
        }

        // finally... here we go
        // last action: if the map is bigger than the specified size in
        // the maparch, we set the true size: the maxxlen/maxylen counters.
        if(maparch.getWidth() < maxxlen+1)
            maparch.setWidth(maxxlen+1);
        if(maparch.getHeight() < maxylen+1)
            maparch.setHeight(maxylen+1);

        return start; // return first arch of the list
    }


    /**
     * our recursive accessible arch reader
     *
     * WARNING: this implementation should fail with multi head settings
     * if there are multi part items in the inventory (but this is not yet
     * included in CF!)
     *
     * @param myInput       input filestream
     * @param thisLine      first line of text, belonging to this new arch
     * @param container     container arch that contains this new arch - if any
     * @returns             the new parsed <code>ArchObject</code>
     * @throws IOException  when an I/O-error occured during file reading
     */
    public ArchObject readArch(BufferedReader myInput, String thisLine, ArchObject container)
        throws IOException {
        ArchObject arch;
        //String thisLine;
        boolean archflag, archmore, msgflag, animflag, scriptflag;
        int x,y, temp;

        archflag = false;
        archmore = false;

        msgflag = false;
        animflag = false;

        arch = null;

        try {
            // read the whole file, line by line
            while (thisLine != null) {

                if(thisLine.length() > 0) {
                    // remove whitespace at both ends
                    thisLine = thisLine.trim();

                    if(archflag == false) {
                        if(thisLine.regionMatches(0,"More", 0, 4)) {
                                // All arches started with "More" are ignored. We load only
                                // the heads and expand them according to the defaults.
                            archmore = true;
                        }
                        else if(thisLine.regionMatches(0,"arch ", 0, 5)) {
                                // kill white spaces afer arch...
                                // (hm, no command for this in java? )
                            for(x=5;x<thisLine.length();x++) {
                                if(thisLine.charAt(x) != ' ')
                                    break;
                            }

                            archflag = true;  // from now on we are inside an arch

                            if (!archmore) {
                                arch = new ArchObject();  // create a new instance

                                // if this is still -1 in the post parse, we have no direction command loaded
                                arch.setDirection(-1);
                                // our arch! it has a name!
                                arch.setArchName(thisLine.substring(x,thisLine.length()));

                                // ok, we have setup our arch, now check for inventory
                                arch.setContainer(container);
                            }
                        }

                    }
                    else if (!archmore) {
                        // here were are inside of an arch object (but not "More")...
                        if(msgflag == true) {
                            if(thisLine.regionMatches(0,"endmsg", 0, 6)) {
                                msgflag=false;
                            } else
                                arch.addMsgText(thisLine+"\n");
                        }
                        else if(animflag == true) {
                                // arch.addArchText(thisLine+"\n");
                            if(thisLine.regionMatches(0,"mina", 0, 4)) {
                                //arch.setAnimName(arch.getArchName());
                                animflag=false;
                            } else // we not include anim yet in panel
                                arch.addAnimText(thisLine+ "\n");
                        }
                        // ok, we had a full arch... don't care here about map or object
                        // now we test for a new arch - thats stuf in inventory
                        // or the end... thats the end of this shit
                        else if(thisLine.startsWith("arch ")) {
                            //System.out.println("GO INVENTORY: "+arch+" - "+thisLine);
                            arch.addInvObj(readArch(myInput, thisLine, arch));
                                //System.out.println("GO INVENTORY2: "+arch+" - "+thisLine);
                        }
                        else if(thisLine.startsWith("end")) {
                                // chain this to temp list
                            if(start == null)
                                start = arch;    // if start null, this is the first arch
                            if(previous != null)
                                previous.setTemp(arch);
                            previous = arch;
                            archflag = false;
                            archmore = false;

                                // System.out.println("LEAVE!: "+arch+" - "+thisLine);
                            return arch;
                        }
                        else if(thisLine.regionMatches(0,"msg", 0, 3)) {
                                // arch.addArchText(thisLine+"\n");
                            arch.addMsgText(""); // this init the msg text buffer
                                // in the case of msg/endmsg
                                // with no content to overrule def msg
                            msgflag=true;
                        }
                        // this is a MUST, because we overrule "anim" test
                        else if(thisLine.regionMatches(0,"animation", 0, 9)) {
                            arch.addArchText(thisLine+"\n");
                        }
                        else if(thisLine.regionMatches(0,"anim_speed", 0, 10)) {
                            arch.addArchText(thisLine+"\n");
                        }
                        else if(thisLine.regionMatches(0,"anim", 0, 4)) {
                                //      arch.addArchText(thisLine+"\n");
                            animflag=true;
                        }
                        else if(thisLine.startsWith("event_")) {
                            // here's something about a scripted event
							// TODO: handle import of old plugins? At least warn the poor user.
/*                            int i = thisLine.indexOf("_plugin");
                            int j = thisLine.indexOf(" ");
                            if (j > 0) {
                                if (i > 0) {
                                    // expecting: "event_type_plugin Name"
                                    String type = thisLine.substring(6, i);
                                    String plname = thisLine.substring(j+1).trim();
                                    arch.addEventPlugin(type, plname);
                                }
                                else {
                                    // expecting: "event_type filepath"
                                    String type = thisLine.substring(6, j);
                                    String path = thisLine.substring(j+1).trim();
                                    arch.addEventScript(type, path);
                                }
                            }
                            else {
                                System.out.println("WARNING: Arch "+ arch.getArchName()+" has incorrect event code '"+thisLine+"'");
                                arch.addArchText(thisLine+"\n"); // keep line, it might have a meaning after all
                            }*/
                        }
                        else if(thisLine.regionMatches(0,"x ", 0, 2)) {
                            temp = Integer.parseInt(thisLine.substring(2));
                            if(temp >maxxlen)
                                maxxlen = temp;
                            arch.setMapX(temp);
                        }
                        else if(thisLine.regionMatches(0,"y ", 0, 2)) {
                            temp = Integer.parseInt(thisLine.substring(2));
                            if(temp >maxylen)
                                maxylen = temp;
                            arch.setMapY(temp);
                        }
                        // MT: there should be here no type or? - AV: WRONG, there defenitly should be!
                        // yes, yes and i even added new face/anim handling now ;)
                        else if(thisLine.startsWith("type ")) {
                            arch.setArchTypNr(Integer.parseInt(thisLine.substring(5)));
                            // don't load it into the archtext!
                        }
                        else if(thisLine.startsWith("direction ")) {
                            arch.setDirection(Integer.parseInt(thisLine.substring(10)));
                            arch.addArchText(thisLine+"\n");
                        }
                        else if(thisLine.regionMatches(0,"face ", 0, 5)) {
                            for(x=4;x<thisLine.length();x++) {
                                if(thisLine.charAt(x) != ' ')
                                    break;
                            }
                            arch.setFaceRealName(thisLine.substring(x,thisLine.length()));
                            arch.addArchText(thisLine+"\n");
                        }
                        else
                            arch.addArchText(thisLine+"\n");
                    }
                    else {
                        // We are in a multipart tail arch ("more"), so we skip it:
                        if(thisLine.regionMatches(0,"end", 0, 3)) {
                            archflag = false;
                            archmore = false;
                        }
                    }
                }
                thisLine = myInput.readLine();
                if (thisLine != null) thisLine.trim();
            }
        }
        catch (IOException e) {
            System.out.println("Read Error while trying to load map: "+maparch.getFileName());
            throw e;  // we simply pass this exception to the calling function
        }

        return null; // this happens when the file end is reached
    }
};
