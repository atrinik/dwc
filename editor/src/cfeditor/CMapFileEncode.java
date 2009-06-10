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
 * The <code>CMapFileEncode</code>
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMapFileEncode {
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    private CMainControl m_control;
    private String fname;

    public CMapFileEncode(CMainControl control) {
        m_control = control;
    };

    /**
     * write the whole map-data into a file
     *
     * @param file     mapfile
     * @param map      map header (<code>MapArchObject</code>)
     * @param m_grid   map grid (x,y-array of <code>ArchObject</code>s)
     */
    public void encodeMapFile(File file, MapArchObject map, ArchObject[][] m_grid) {
        try {
            if (file == null) {
                // if the file doesn't yet exist, we create it in the map-folder
                file = new File(m_control.getMapDefaultFolder(), map.getFileName());
            }

            fname = file.getAbsolutePath();
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            int x,y;
            ArchObject node;
            //ArchObject multi;

            // write map header: map arch
            map.writeMapArch(bufferedWriter);

            // first, write all one tile parts
            for(x=0;x<map.getWidth();x++) {
                for(y=0;y<map.getHeight();y++) {
                    node = m_grid[x][y];
                    for(;node!= null;) {
                        // only non muli suckers
                        if(node.getMapMultiHead() == null && node.getMapMultiNext()==null) {
                            if(!writeMapArch(node, false))
                                return;
                        }
                        node = node.getNextArch();
                    }

                }
            }

            // second, we drop the multi part suckers out
            for(x=0;x<map.getWidth();x++) {
                for(y=0;y<map.getHeight();y++) {
                    node = m_grid[x][y];
                    for(;node!= null;)
                        {
                            // search only for heads!
                            if(node.getRefCount() > 0 && node.getMapMultiNext()!=null) {
                                /* old version: Both heads and tails got written into the mapfile
                                   for(multi=node;;)
                                   {
                                   if(!writeMapArch(multi, false))
                                   return;
                                   multi = multi.getMapMultiNext();
                                   if(multi == null)
                                   break;
                                   bufferedWriter.write("More\n");

                                   }*/

                                // only the heads get stored in the mapfile
                                // (that's much more efficient)
                                if(!writeMapArch(node, false))
                                    return;
                            }
                            node = node.getNextArch();
                        }

                }
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            m_control.showMessage("Error Save Map","Error writing file "+fname+"\n");
        } catch (IOException e) {
            m_control.showMessage("Error Save Map","Error writing file "+fname+"\n");
        }
    }

    /**
     * walk through the inventory of an arch and write everything into the file
     *
     * @param start   the container arch whose inventory is to be written
     * @return        true if all writing actions successful
     */
    public boolean browseInvObjects(ArchObject start) {
        // cycle throught the whole inventory list:
        for(ArchObject arch = start.getStartInv();
            arch != null; arch = arch.getNextInv()) {
            // write the inventory arch (recursion possible here)
            if(writeMapArch(arch, true) == false)
                return false;
        }
        return true; // all ok
    }

    /**
     * here the map arch gets written into the file
     *
     * @param arch           <code>ArchObject</code> to be written into the map
     * @param is_inventory   is 'arch' inside a container? true/false
     * @return               true if arch was written successfully
     */
    boolean writeMapArch(ArchObject arch, boolean is_inventory) {
        ArchObject defarch = arch.getDefaultArch();

        try
            {
                // ok, we start with the standard parts... this is valid for all types
                bufferedWriter.write("arch "+arch.getArchName()+"\n");
                if (arch.getObjName() != null)
                    bufferedWriter.write("name "+arch.getObjName()+"\n");
//                if (arch.getFaceName() != null)
//                    bufferedWriter.write("face "+arch.getFaceName()+"\n");
                if (arch.getMsgText() != null && !arch.getMsgText().trim().equals(defarch.getMsgText()==null?"":defarch.getMsgText().trim())) {
                    // write message text
                    bufferedWriter.write("msg\n");
                    if(arch.getMsgText().trim().length() > 0) {
                        bufferedWriter.write(arch.getMsgText());
                        if(arch.getMsgText().lastIndexOf(0x0a) != arch.getMsgText().length()-1)
                            bufferedWriter.write("\n");
                    }
                    bufferedWriter.write("endmsg\n");
                }
                if (defarch != null && arch.getArchTypNr() != defarch.getArchTypNr()) {
                    // this arch has special type
                    if (arch.getArchText().indexOf("type ") >= 0) {
                        // oh oh - there might also be a type in the archtext which
                        // is conflicting. remove the type from the archtext

                        // open a reading stream for the archText
                        StringReader sread = new StringReader(arch.getArchText().toString());
                        BufferedReader sstream = new BufferedReader(sread);
                        String new_archtext = "";

                        try {
                            String line = null; // read line

                            do {
                                line = sstream.readLine(); // read one line

                                if (line != null && line.length() > 0) {
                                    line = line.trim();

                                    if (line.startsWith("type ")) {
                                        // ommit this line
                                    }
                                    else
                                        new_archtext += line+"\n"; // append this line
                                }
                            } while (line != null && line.length() > 0);

                            // close streams
                            sstream.close();
                            sread.close();

                            if (new_archtext.trim().length() == 0)
                                arch.setArchText("");
                            else
                                arch.setArchText(new_archtext.trim()+"\n");
                        }
                        catch (IOException e) {
                            System.out.println("Error in getSyntaxErrors: Cannot close StringReader");
                        }
                    }

                    // now append the type to the archtext
                    bufferedWriter.write("type "+arch.getArchTypNr()+"\n");
                }

                bufferedWriter.write(arch.getArchText());
                if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                    bufferedWriter.write("\n");

                if (!is_inventory) {
                                // map coordinates only belong into map arches (not inventory arches)
                    if(arch.getMapX()!=0)
                        bufferedWriter.write("x "+arch.getMapX()+"\n");
                    if(arch.getMapY()!=0)
                        bufferedWriter.write("y "+arch.getMapY()+"\n");
                }

                browseInvObjects(arch); // write his inventory inside this arch

                bufferedWriter.write("end\n");
            } catch (IOException e) {
                m_control.showMessage("Error Save Map","Error writing file "+fname+"\n");
                return false;
            }
        return true;
    }

};
