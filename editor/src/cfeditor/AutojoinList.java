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

/**
 * The <code>AutojoinList</code> class contains a list of (typically wall-)arches which
 * do autojoining.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class AutojoinList {
    private static final String filename = "autojoin.txt";  // file with autojoin lists

    // bitmask constants for nodenr index
    private static final int NORTH = 1;
    private static final int EAST = 2;
    private static final int SOUTH = 4;
    private static final int WEST = 8;

    private ArchObjectStack stack;  // reference to the stack of default arches

    private AutojoinList next;      // next autojoin list

    // the arches' node numbers, interpreted in following order (0-15):
    //index: 0, 1, 2,  3, 4,  5,  6,   7, 8,  9, 10,  11, 12,  13,  14,   15
    //means: 0, N, E, NE, S, NS, ES, NES, W, WN, WE, WNE, SW, SWN, ESW, NESW
    // (0 = no connection, N = north, E = east, S = south, W = west)
    private int[] nodenr;

    /**
     * Konstructor
     */
    public AutojoinList () {
        stack = null;   // pointer to stack of default arches
        next = null;    // pointer to next element
        nodenr = null;  // this array gets initialized in loadList (when needed)
    }

    /**
     * Load all the autojoin lists from the datafile.
     * This method will attach additional AutojoinList objects
     * to the (head) instance which it is called from.
     * The links from the default arches to their appropriate
     * AutojoinLists also get set here.
     *
     * Note that this method takes only split seconds to execute,
     * as it uses the arch hashtable to look up the default arches. :-)
     *
     * @param archstack    the stack of default arches
     * @return: true if at least one autojoin list was successfully loaded
     */
    public boolean loadList(ArchObjectStack archstack) {
        boolean success = false;       // return value (true if >=1 list loaded)

        try {
            String line;                   // line of file
            boolean section_flag = false;  // true while section (start ... end) read
            int[] nbuf = new int[16];      // buffer for nodenumbers of 16 arches
            int count = 0;                 // counter index
            AutojoinList jlist = this;     // index of the linked list of AutojoinLists
                                           // (starting at 'this')

            // open the resource file
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
            CFileReader stream = new CFileReader(baseDir, filename);

            // read the file
            while ((line = stream.getReader().readLine()) != null) {
                if (!line.startsWith("#") && line.length() > 0) {
                    line = line.trim();  // remove whitespace at both ends

                    if (!section_flag) {
                        // we are outside a section
                        if (line.equals("start")) {
                            section_flag = true;
                            count = 0;
                        }
                    }
                    else {
                        // we are inside a section
                        if (line.equals("end")) {
                            if (count == 16) {
                                // we have 16 valid entries, now stick it into an AutojoinList
                                if (success == true) {
                                    // attach a new element to the linked list
                                    jlist.next = new AutojoinList();
                                    jlist = jlist.next;
                                }

                                jlist.stack = archstack;         // set link to arch stack
                                jlist.nodenr = new int[16];  // array to store the nodenrs.
                                for (int i=0; i<16; i++) {
                                    jlist.nodenr[i] = nbuf[i];  // store nodenr. in the list
                                    archstack.getArch(nbuf[i]).setJoinList(jlist); // set the link in the def. arch

                                    //System.out.println("("+i+") arch: '"+archstack.getArch(nbuf[i]).getArchName()+"' -> nr: ("+nbuf[i]+")");
                                }

                                success = true;  // we did it!
                            }
                            else if (count > 16)
                                System.out.println("In file "+filename+": List with more than 16 valid entries!");
                            else if (archstack.getArchCount() >= 1)
                                System.out.println("In file "+filename+": List with less than 16 valid entries!");
                            section_flag = false;
                        }
                        else if (count < 16){
                            // add a new archid to the buffer
                            Object entry = null;        // hashtable entry

                            try {
                                // try to parse the arch node_nr from the hashtable
                                entry = archstack.getArchHashTable().get(line);   // get hashtable entry
                                if (entry != null) {
                                    nbuf[count] = Integer.parseInt(entry.toString());  // get int value

                                    if (!archstack.getArch(nbuf[count]).isMulti())
                                        count++; // no multipart, this one's okay
                                    else
                                        System.out.println("In file "+filename+": Arch '"+line+"' is a multipart.");
                                }
                                else {
                                    // (If no arches exist at all, errormessages are suppressed here)
                                    if (archstack.getArchCount() >= 1)
                                        System.out.println("In file "+filename+": Arch '"+line+"' not found.");
                                }
                            }
                            catch (NumberFormatException e) {
                                // parsing failed (should not happen)
                                System.out.println("ArchObjectStack Hashtable entry '"+entry.toString()+"' not parseable as int!");
                            }
                        }
                        else
                            count++; // too many arches
                    }
                }
            }

            // close filestream
            stream.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Autojoin definitions file '"+filename+"' not found.");
            return false;
        }
        catch (IOException e) {
            System.out.println("Read error in file '"+filename+"'.");
            return false;
        }

        if (success) return true;  // we have loaded one or more lists

        return false;              // nothing got loaded
    }

    /**
     * Do autojoining on insertion of a default arch on the map.
     * All arches around the insert point get adjusted, and the
     * node_nr of the correct arch to be inserted is returned.
     * This method must be called from the appropriate element of the
     * AutojoinList, best use the link from the default arch.
     *
     * @param x        Location of the insert point on the map
     * @param y        Location of the insert point on the map
     * @param map      Data model of the map
     *
     * @return node_nr of the (def.) arch to be inserted at x, y
     *         -1 if there's already an arch of this list on x, y
     */
    public int join_insert(CMapModel map, int x, int y) {
        int new_index = 0;      // return value, see above
        ArchObject arch = null; // temp. arch

        // if there already is an arch of this list at x, y -> abort
        if (findArchOfJoinlist(map, x, y) != null)
            return -1; // we don't want same arches over each other

        // now do the joining in all four directions:
        if (map.pointValid(x, y-1)) {
            if ((arch = findArchOfJoinlist(map, x, y-1)) != null) {
                new_index = add_dir(new_index, NORTH);
                connectArch(arch, nodenr[add_dir(get_index(arch.getNodeNr()), SOUTH)]);
            }
        }

        if (map.pointValid(x+1, y)) {
            if ((arch = findArchOfJoinlist(map, x+1, y)) != null) {
                new_index = add_dir(new_index, EAST);
                connectArch(arch, nodenr[add_dir(get_index(arch.getNodeNr()), WEST)]);
            }
        }

        if (map.pointValid(x, y+1)) {
            if ((arch = findArchOfJoinlist(map, x, y+1)) != null) {
                new_index = add_dir(new_index, SOUTH);
                connectArch(arch, nodenr[add_dir(get_index(arch.getNodeNr()), NORTH)]);
            }
        }

        if (map.pointValid(x-1, y)) {
            if ((arch = findArchOfJoinlist(map, x-1, y)) != null) {
                new_index = add_dir(new_index, WEST);
                connectArch(arch, nodenr[add_dir(get_index(arch.getNodeNr()), EAST)]);
            }
        }

        return nodenr[new_index];
    }

    /**
     * Do autojoining on deletion of an ArchObject on the map.
     * All arches around the insert point get adjusted.
     * This method must be called from the appropriate element of the
     * AutojoinList, best use the link from the default arch.
     *
     * @param x        Location of the insert point on the map
     * @param y        Location of the insert point on the map
     * @param map      Data model of the map
     */
    public void join_delete(CMapModel map, int x, int y) {
        ArchObject arch = null; // temp. arch

        // do the joining in all four directions:
        if (map.pointValid(x, y-1)) {
            if ((arch = findArchOfJoinlist(map, x, y-1)) != null) {
                connectArch(arch, nodenr[remove_dir(get_index(arch.getNodeNr()), SOUTH)]);
            }
        }

        if (map.pointValid(x+1, y)) {
            if ((arch = findArchOfJoinlist(map, x+1, y)) != null) {
                connectArch(arch, nodenr[remove_dir(get_index(arch.getNodeNr()), WEST)]);
            }
        }

        if (map.pointValid(x, y+1)) {
            if ((arch = findArchOfJoinlist(map, x, y+1)) != null) {
                connectArch(arch, nodenr[remove_dir(get_index(arch.getNodeNr()), NORTH)]);
            }
        }

        if (map.pointValid(x-1, y)) {
            if ((arch = findArchOfJoinlist(map, x-1, y)) != null) {
                connectArch(arch, nodenr[remove_dir(get_index(arch.getNodeNr()), EAST)]);
            }
        }
    }

    /**
     * Looking up the given node in the nodenr-array of this class.
     *
     * @param node     node to lookup
     * @return index of the node in the array.
     */
    private int get_index(int node) {
        int i;

        for (i = 0; i < 16 && node != nodenr[i]; i++);

        if (node != nodenr[i]) {
            System.out.println("Error in AutojoinList.get_index: index not found");
            return 0;
        }

        return i;
    }

    /**
     * Checks if the index (=bitmask) contains the following direction
     */
    private boolean has_dir(int index, int direction) {
        if((index & direction) != 0) return true;
        return false;
    }

    /**
     * add direction to the index
     */
    private int add_dir(int index, int direction) {
        return index|=direction;
    }

    /**
     * remove direction from the index
     */
    private int remove_dir(int index, int direction) {
        return index&=~direction;
    }

    /**
     * Looks for an arch at map-position (x, y) which is part
     * of this AutojoinList.
     *
     * @param map       the data model of the map
     * @param x         location to search
     * @param y         location to search
     * @return arch which is part of this joinlist, null if no such arch exists
     */
    private ArchObject findArchOfJoinlist(CMapModel map, int x, int y) {
        ArchObject tmp_arch;

        if(map.m_mapGrid==null || !map.pointValid(x, y))
            return null;

        if (map.m_mapGrid[x][y] != null) {
            // go to the topmost tile
            for (tmp_arch = map.m_mapGrid[x][y]; tmp_arch != null &&
                     tmp_arch.getNextArch() != null; tmp_arch = tmp_arch.getNextArch());

            // we look through the arches at the given location (top to bottom):
            for(; tmp_arch != null; tmp_arch = tmp_arch.getPrevArch()) {
                if(stack.getArch(tmp_arch.getNodeNr()).getJoinList() == this)
                    return tmp_arch; // we found an arch
            }
        }

        return null; // nothing found
    }

    /**
     * Add/remove a certain connection of the given arch
     * by changing archtype and face.
     */
    private void connectArch(ArchObject arch, int new_nodenr) {
        ArchObject defarch = stack.getArch(new_nodenr); // new default arch

        if (arch.getNodeNr() != new_nodenr) {
            // set new archtype
            arch.setNodeNr(new_nodenr);
            arch.setArchName(defarch.getArchName());

            // set face of new default arch
            arch.setRealFaceNr(defarch.getRealFaceNr());
            arch.setFaceRealName(defarch.getFaceRealName());
            /*
            if (arch.getFaceName() != null && arch.getFaceName().length() > 0)
                arch.setFaceName(null);
          */
        }
    }

}
