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
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import com.sixlegs.image.png.PngImage;

/**
 * The <code>ArchObjectStack</code> contains all the default arches.
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ArchObjectStack
{
    // load status: is archstack fully loaded?
    public static final int IS_EMPTY = 0;
    public static final int IS_LOADING = 1;
    public static final int IS_COMPLETE = 2;
    private static int load_status = IS_EMPTY;

    // true when arches were loaded from the big collected archive files,
    // false when arches were loaded from individual archfiles
    private static boolean load_from_archive = false;

    // used to exclude our artifact arches from counts
    private static int artifact_count = 0;

    // this is the static part.. i want fast access later
    private ArchObjectNode[] archNodeList = new ArchObjectNode[10000];
    private FaceObject[] faceObjects = new FaceObject[10000];

    // The hash tables hold the name and the index for the field
    // if one can show me, that storing and accessing names AND objects
    // in the table is faster than in the static arrays, we can change this
    private static Hashtable faceHashTable = new Hashtable();
    private static Hashtable archHashTable = new Hashtable();

    private int archNodeListCount;      // ALL default arches loaded
    int archObjCount;                   // all objects, multi tile arches = 1 object
    private int faceListCount;          // all loaded face pictures

    private CMainControl m_control;
    private int folder_level;

    private int png_load_counter=0;     // counter of loaded pngs (for memory management)

    public ArchObjectStack(CMainControl control)
    {
        m_control = control;
        archNodeListCount = 0;
        faceListCount=0;
        archObjCount=0;
        ArchObject.setArchStack(this);   // add static reference to ArchObject
    };

    public void incArchObjCount() {archObjCount++;}

    public int getArchObjCount() {return(archObjCount);}
    public static int getLoadStatus() {return load_status;}
    public static boolean isLoadedFromArchive() {return load_from_archive;}

    public Hashtable getFaceHashTable() {return(faceHashTable);}
    public Hashtable getArchHashTable() {return(archHashTable);}

    public ImageIcon getFace(int i)
    {
        if(i >=0 && i <faceListCount)
            return(faceObjects[i].getFace());
        else
            return(null);
    }
    public String getFaceName(int i)
    {
        if(i >=0 && i <faceListCount)
            return(faceObjects[i].getName());
        else
            return(null);
    }


    public int getArchCount()
    {
        return(archNodeListCount);
    }

    public int getFaceCount()
    {
        return(faceListCount);
    }

    public ArchObjectNode getNextNode(ArchObjectNode node)
    {
        if(node == null)
            return(archNodeList[0]);
        else
        {
            int i = node.arch.getNodeNr();
            if(i+1 < archNodeListCount)
                return(archNodeList[i+1]);
            else
                return(null);
        }
    }

    public ArchObject getArch(int i)
    {
        if(i>= 0 && i < archNodeListCount)
            return(archNodeList[i].arch);
        else
            return(null);
    }

    public ArchObject newArchObjectInstance(int i)
    {
        if(i<0 && i>=  archNodeListCount)
            return(null);
        ArchObject arch = new ArchObject();
        arch.setArchName( new String(archNodeList[i].arch.getArchName()));
        arch.setNodeNr(archNodeList[i].arch.getNodeNr());
        return(arch);
    }

    // add a arch to our list
    public void addArchToList (ArchObject data)
    {
        ArchObjectNode newnode = new ArchObjectNode (data);
        newnode.arch.setNodeNr(archNodeListCount);
        archHashTable.put(data.getArchName(), new Integer(archNodeListCount));
        archNodeList[archNodeListCount++] = newnode;
    }

    /**
     * load the arches
     */
    public void loadArches() {
        Date time_start = new Date();  // get starting time
        load_status = IS_LOADING;      // status: loading
        System.out.println("Start to collect arches...");
        CMainStatusbar.getInstance().setText(" Loading Arches... ");

        // browse arch archive
        // load object from a arch file you found
        File f = new File(m_control.getArchDefaultFolder());
        folder_level=0;
        m_control.disableTabPane();

        // here we go...
        if (m_control.isArchLoadedFromCollection())
            loadArchFromCollected();    // collect arches & images from collection
        else
            loadArchFromFiles(f,0);     // collect arches & images from individual files

        // at this time only use Daimonin artifacts file in this way
        if (IGUIConstants.isoView)
            loadArchesFromArtifacts(m_control.getArchDefaultFolder()+File.separator+IGUIConstants.ARTIFACTS_FILE);

        m_control.enableTabPane();
        CMainStatusbar.getInstance().setText(" Sorting...");
        connectFaces();    // attach faces to arches
        System.gc();

        // load the autojoin lists
        CMainStatusbar.getInstance().setText(" Loading Autojoin Tables...");
        m_control.load_joinlist();

        CMainStatusbar.getInstance().setText(" Ready.");

        // print message if no arches were found
        if (getArchCount() == 0) {
            load_status = IS_EMPTY;         // status: stack is empty
            m_control.showMessage("No Archfiles", "No archfiles could be found. If you have no archfiles\n"+
                                                  "yet, you need to download them. If you do, make sure\n"+
                                                  "the path is correct under menu 'File->Options'.\n");
        }
        else {
            // display the time it took to collect arches:
            load_status = IS_COMPLETE;      // status: stack completed
            Date time_finish = new Date();  // get finishing time
            long diff = time_finish.getTime() - time_start.getTime();
            System.out.println("Arch collect took "+diff/1000.+" Seconds");

            // load pickmaps
            CPickmapPanel.getInstance().loadPickmaps();

            // load the treasurelists data
            CFTreasureListTree.init();
        }
    };

    /**
     * Load "pseudo arches" from file "artifacts"
     * WARNING: Don't include multi arches in the artifacts file
     * This code can't handle it nor the real server
     */
    private void loadArchesFromArtifacts(String fname) {
        int line_count=0;
        Integer index;
        ArchObject arch,def_arch;
        String thisLine, name="",def_arch_name="",obj_title="";
        m_control.addArchPanel("Artifacts");
        m_control.addArchPanelCombo("show all");

        //Open the file for reading
        try {
            FileReader fr =  new FileReader(fname);
            BufferedReader myInput = new BufferedReader(fr);

            // do the actual parsing
            //parseDefArchFromStream(myInput, 0);
            while ((thisLine = myInput.readLine()) != null) {
                line_count++;
                thisLine=thisLine.trim();

                // ignore white space lines or '#' comment lines
                if(!thisLine.regionMatches(0,"#", 0, 1) && thisLine.length() != 0) {
                    if(thisLine.regionMatches(0,"artifact ", 0, 9))
                        name = thisLine.substring(9);
                    if(thisLine.regionMatches(0,"def_arch ", 0, 9))
                        def_arch_name = thisLine.substring(9);

                    if(thisLine.regionMatches(0,"Object", 0, 6)) {
                        obj_title ="";
                        if(thisLine.length() >7)
                            obj_title = thisLine.substring(7);
                        // at this point we MUST have a legal name and def arch
                        index = null;
                        if(def_arch_name.length()>0)
                            index = (Integer) getArchHashTable().get(def_arch_name);
                        if(name.length() == 0 || def_arch_name.length() == 0 || index == null)
                        {
                          if(def_arch_name.length() != 0)
                            System.out.println("Artifacts file: Line "+line_count+" Object >"+def_arch_name+"<>"+obj_title+"< has missing name or def_arch");
                          else if(name.length() != 0)
                            System.out.println("Artifacts file: Line "+line_count+" Object >"+name+"<>"+obj_title+"< has missing name or def_arch");
                        }
                        else // the next line of our file is part of a arch parse until a "end" comes
                        {
                            // first get our base arch
                            def_arch = getArch(index.intValue());
                            // now the editor will do the same as the real server:
                            // get the default arch as base and parse the new values over it
                            // the extended functions of the artifacts file can be ignored here.
                            arch = m_control.archObjectParser.parseDefArchFromStream(myInput,def_arch,thisLine,name,0);
                            // note: in the parser is a small part where we handle the title setting
                            // and the reverse type setting for type == -1 (unique items marker in artifacts file)
                        }
                        name = "";
                        def_arch_name = "";
                    }
                }
            }

            myInput.close();
            fr.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Artifacts file could not be found");
        }
        catch (IOException e) {
            System.out.println("IOException in reading Artifacts!");
        }
    }

    /**
     * Load all arches and pngs from the collected files
     * "archtypes" and "crossfire.0"
     */
    private void loadArchFromCollected() {
        String line; // input line

        load_from_archive = true; // load from the collected files

        try {
            // open the resource file
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder() : IGUIConstants.CONFIG_DIR);
            CFileReader stream = new CFileReader(baseDir, IGUIConstants.ARCH_FILE);

            // load all arches
            m_control.archObjectParser.parseDefArchFromStream(stream.getReader(), 0);

            // close filestream
            stream.close();

            if (IGUIConstants.isoView)
                loadAllDaimoninPNGFromCollect();
            else
                loadAllCrossfirePNGFromCollect();
        }
        catch (FileNotFoundException e) {
            // no need for a message here, CFileReader takes care of this
        }
    }

    /**
     * this method loads the arches & faces recursively by
     * looping through the arch folder, collecting all the trash
     *
     * @param f       file path where we currently are
     * @param index   counter for arches
     */
    private void loadArchFromFiles(File f, int index) {
        int len;
        String name;

        load_from_archive = false; // don't load from the collected files
        name = f.getName();
        if (f.isDirectory()) {
            // now, setup the arch panels
            if(name.equalsIgnoreCase("cvs")!=true && name.equalsIgnoreCase("dev")!=true)
            {
                if(folder_level > 0 && folder_level < 2) // add first folders as panels
                {
                    m_control.addArchPanel(name);
                    m_control.addArchPanelCombo("show all");
                }
                if(folder_level > 0 && folder_level < 3) // add first folders as panels
                {
                    index = m_control.addArchPanelCombo(name);
                    // m_control.showMessage("LOAD FILE", "name: "+name+" index: "+index);
                }
                folder_level++;

                String[] children = f.list();
                for (int i=0; i<children.length; i++)
                    loadArchFromFiles(new File(f, children[i]),index);
                folder_level--;
            }
          }
          else {
            if(name.equalsIgnoreCase("cvs")!=true && name.equalsIgnoreCase("dev")!=true) {

                if((len = (int)name.length()) >= 5) {
                    if(name.regionMatches(len-5,".face", 0, 5)) {
                        //CMainStatusbar.getInstance().setText(" Loading Face: "+ name);
                        parseDefFace(f.getAbsolutePath());
                    }
                    if((len = (int)name.length()) >= 4) {
                        if(name.regionMatches(len-4,".arc", 0, 4)) {
                            //CMainStatusbar.getInstance().setText(" Loading Arch: "+ name);
                            m_control.archObjectParser.parseDefArch(f.getAbsolutePath(),index );
                        }
                        else if(name.regionMatches(len-4,".png", 0, 4)) {
                            //CMainStatusbar.getInstance().setText(" Loading PNG: "+ name);
                            if (m_control.imageSet == null || name.indexOf("."+m_control.imageSet+".") != -1)
                                addPNGFace(f.getAbsolutePath(),name );
                        }
                    }

                }
            }
        }
    }

    /**
     * Parsing face files (*.face). I think such files are no
     * longer used and so is this code.
     *
     * @param fname  filename
     */
    private void parseDefFace(String fname)
    {
        int x,y;
        String thisLine, thisLine2, anim_name=null, anim_text=null;
        boolean animflag=false;
        try
        {
        FileReader fr =  new FileReader(fname);
        BufferedReader myInput = new BufferedReader(fr);

        while ((thisLine2 = myInput.readLine()) != null)
        {
            // hm, thats ugly, but i need to cut off the damn white spaces for the names
            thisLine = thisLine2;
            /*
            if(thisLine2.length()>1)
            {
                for(x=0;x<thisLine2.length();x++)
                {
                    if(thisLine2.charAt(x) != ' ')
                        break;
                }
                for(y=thisLine2.length()-1;y>=0;y--)
                {
                    if(thisLine2.charAt(y) != ' ')
                        break;
                }
                thisLine = thisLine2.substring(x,y+1);
            }
            */
            thisLine = thisLine2.trim();

            // ignore all '#' lines as comment
            if(!thisLine.regionMatches(0,"#", 0, 1))
            {
                if(animflag)
                {
                    if(thisLine.regionMatches(0,"mina", 0, 4))
                    {
                        animflag = false;
                        //System.out.println("We found this>>  name: >"+anim_name+" \n>"+anim_text+"<\n");
                        m_control.animationObject.addAnimObject(anim_name, anim_text);

                    }
                    else
                        anim_text = ""+anim_text+thisLine+"\n";
                }

                if(thisLine.regionMatches(0,"animation ", 0, 10))
                {
                    anim_name = thisLine.substring(10).trim();
                    anim_text="";
                    animflag=true;
                }
            }

        }
        myInput.close();
        fr.close();
     } // end try
     catch (IOException e)
     {
              System.out.println("Error reading face file: " + e);
    }

    }

    /**
     * Loading all pngs from the big collected png file
     */
    private void loadAllCrossfirePNGFromCollect() {
        String face;  // face name of png

        ByteArrayInputStream pngdata; // byte input stream to read pngdata from the buffer
        try {
            // open a bytestream to the resource file
            String baseDir = (IGUIConstants.isoView ? CMainControl.getInstance().getArchDefaultFolder() : IGUIConstants.CONFIG_DIR);
            CFileInputStream stream = new CFileInputStream(baseDir, IGUIConstants.PNG_FILE);

            try {
                char c, l;        // read-in byte character
                byte[] buf;       // tmp. byte buffer to store the png data
                int t, size, r;
                String tag = "IMAGE";  // this is the starting string for a new png
                boolean face_complete; // true when a facename was read successfully
                boolean crappy_facepath = false; // true when facepathes with '.' detected

                // load all pngs
                do {
                    // first we parse the face name out of the ascii data
                    // that precedes the png byte-data
                    face = ""; c = '\0'; l = '\0'; r = '\0';
                    face_complete = false;
                    for (t=0; t<3; t++) {
                        do{
                            r = stream.read();
                        }while(r != -1 && (char)r != ' ');
                    }

                    do {
                        while((c != '.' || face.length() < 3) && r != -1) {
                            r = stream.read();
                            c = (char)r;
                            face += String.valueOf(c);
                        }

                        // r == -1 means End of File reached!
                        if (r != -1) {
                            for (t=0; t<3; t++)
                                face += String.valueOf((char)stream.read());
                            l = (char)stream.read(); // read the final linefeed, next char is PNG data!

                            if (face.indexOf("/") >= 0)
                                face = face.substring(face.lastIndexOf("/")+1);

                            // check the last three characters of face (->animation part) to
                            // see if this is really the end and not a '.' in the path

                            if (l == 10 || (!Character.isLetterOrDigit(l) && l != '/'))
                                face_complete = true;
                            else {
                                //System.out.println("face-path contained '.'");
                                face += l;
                                if (!crappy_facepath) crappy_facepath = true;
                                c = '\0';
                            }
                        }
                    } while (!face_complete && r != -1);

                    if (r != -1) {
                        // now we try to figure the size of the png data in byte,
                        // later we jump back to the beginning position and read those bytes
                        stream.getBufferedStream().mark(100000);         // mark position
                        t = 0; size=0;
                        do {
                            r = stream.read();      // read one character
                            c = (char)r;
                            size++;                 // count overall bytesize
                            if (c == tag.charAt(t))
                                t++;
                            else
                                t=0;
                        } while (t < tag.length() && r != -1);

                        // again, r == -1 means End of File reached
                        if (r != -1 || size > 0) {
                            if (r == -1) {
                                // for the last png, subtract one because last was EOF
                                size--;
                            }
                            else {
                                // for all other pngs, must subtract the size of next starting tag
                                size -= tag.length();
                            }
                            stream.getBufferedStream().reset(); // jump to beginning of png data

                            // now we create a buffer of appropriate size and
                            // dump all the png data into it
                            buf = new byte[size];
                            stream.getBufferedStream().read(buf);

                            // open bytestream to the now buffered png data
                            pngdata = new ByteArrayInputStream(buf);

                            PngImage png = new PngImage(pngdata); // read png data from bytestream

                            pngdata.close();
                            pngdata = null;
                            buf = null;

                            if (png == null)
                                System.out.println("failed to parse png'"+face+"'!");
                            // very important: need to free pixel-data after every step
                            png.setFlushAfterNextProduction(true);

                            // convert png to ImageIcon, then set face
                            ImageIcon im = new ImageIcon(Toolkit.getDefaultToolkit().createImage(png));

                            faceObjects[faceListCount] = new FaceObject(); // allocate space
                            faceObjects[faceListCount].setFace(im);
                            png=null;

                            // put this png into the list of faces
                            faceObjects[faceListCount].setName(face);
                            faceObjects[faceListCount].setPath("null");
                            faceHashTable.put(face, new Integer(faceListCount));
                            faceListCount++;
                            png_load_counter++;       // count num. of loaded pngs

                            // run garbage collector after every 300 loaded pngs
                            // (the jre really is stupid enough to exceed mem before doing this)
                            if (png_load_counter > 300) {
                                System.runFinalization();
                                System.gc();
                                png_load_counter=0;
                            }
                        }
                    }
                } while (r != -1); // loop till end of png file

                if (crappy_facepath) {
                    // print message if there were messy face-pathes with '.'
                    System.out.println("######\n# Some image-paths contain illegal characters in your\n"+
                                       "# collected png file '"+IGUIConstants.PNG_FILE+"'! Re-collect the arches\n"+
                                       "# for better loading performance.\n######");
                }

                // close stream
                stream.close();

                // make sure we free all unneeded memory
                System.runFinalization();
                System.gc();
            }
            catch (IOException e) {
                System.out.println("Read error in file '"+IGUIConstants.PNG_FILE+"'.");
                stream.close();
                return;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("file "+IGUIConstants.PNG_FILE+" not found!");
        }
    }

    /**
     * Loading all pngs from the big collected png file, ISO
     */
    private void loadAllDaimoninPNGFromCollect() {
        String face;  // face name of png

        ByteArrayInputStream pngdata; // byte input stream to read pngdata from the buffer
        try {
            // open a bytestream to the resource file
            CFileInputStream stream = new CFileInputStream(m_control.getArchDefaultFolder(), IGUIConstants.PNG_FILE);

            try {
                char c;          // read-in byte character
                byte[] buf;      // tmp. byte buffer to store the png data
                int t, size, r;
                String tag = "IMAGE"; // this is the starting string for a new png

                // load all pngs
                do {
                    // first we parse the face name out of the ascii data
                    // that precedes the png byte-data
                    face = ""; c = '\0'; r = '\0';
                    for (t=0; t<3; t++) {
                        do{
                            r = stream.read();
                        }while(r != -1 && (char)r != ' ');
                    }

                    do {
                        // if repeating the loop, add char which was supposed to be linefeed
                        if (face.length() > 0) face += String.valueOf(c);

                        // read till the first '.'
                        while(c != '.' && r != -1) {
                            r = stream.read();
                            c = (char)r;
                            face += String.valueOf(c);
                        }

                        // now after three chars the name might end (e.g. "name.111")
                        for (t=0; t<3 && r != -1; t++) {
                            r = stream.read();
                            c = (char)r;
                            if (c == '.') t = -1;

                            face += String.valueOf(c);
                        }

                        // read the final linefeed, next char is likely PNG data
                        if (r != -1)
                            c = (char)stream.read();

                    } while (r != -1 && (int)c != 10 && c != '\n' &&
                             !Character.isDigit(face.charAt(face.length()-1)) &&
                             !Character.isDigit(face.charAt(face.length()-2)) &&
                             !Character.isDigit(face.charAt(face.length()-3)));

                    // r == -1 means End of File reached!
                    if (r != -1) {
                        if (face.indexOf("/") > 0)
                            face = face.substring(face.lastIndexOf("/")+1);

                        // now we try to figure the size of the png data in byte,
                        // later we jump back to the beginning position and read those bytes
                        stream.getBufferedStream().mark(100000);       // mark position
                        t = 0; size=0;
                        do {
                            r = stream.read();      // read one character
                            c = (char)r;
                            size++;                 // count overall bytesize
                            if (c == tag.charAt(t))
                                t++;
                            else
                                t=0;
                        } while (t < tag.length() && r != -1);

                        // again, r == -1 means End of File reached
                        if (r != -1 || size > 0) {
                            if (r != -1)
                                size -= tag.length();  // must subtract the size of final string
                            else
                                size--;

                            stream.getBufferedStream().reset(); // jump to beginning of png data

                            // now we create a buffer of appropriate size and
                            // dump all the png data into it
                            buf = new byte[size];
                            stream.getBufferedStream().read(buf);

                            // open bytestream to the now buffered png data
                            pngdata = new ByteArrayInputStream(buf);

                            PngImage png = new PngImage(pngdata); // read png data from bytestream

                            pngdata.close();
                            pngdata = null;
                            buf = null;

                            if (png == null)
                                System.out.println("failed to parse png'"+face+"'!");
                            // very important: need to free pixel-data after every step
                            png.setFlushAfterNextProduction(true);

                            // convert png to ImageIcon, then set face
                            ImageIcon im = new ImageIcon(Toolkit.getDefaultToolkit().createImage(png));

                            faceObjects[faceListCount] = new FaceObject(); // allocate space
                            faceObjects[faceListCount].setFace(im);
                            png=null;

                            // put this png into the list of faces
                            faceObjects[faceListCount].setName(face);
                            faceObjects[faceListCount].setPath("null");
                            faceHashTable.put(face, new Integer(faceListCount));
                            faceListCount++;
                            png_load_counter++;       // count num. of loaded pngs

                            // run garbage collector after every 300 loaded pngs
                            // (the jre really is stupid enough to exceed mem before doing this)
                            if (png_load_counter > 300) {
                                System.runFinalization();
                                System.gc();
                                png_load_counter=0;
                            }
                        }
                    }
                } while (r != -1); // loop till end of png file

                // close stream
                stream.close();

                // make sure we free all unneeded memory
                System.runFinalization();
                System.gc();
            }
            catch (IOException e) {
                System.out.println("Read error in file '"+IGUIConstants.PNG_FILE+"'.");
                stream.close();
                return;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("file "+IGUIConstants.PNG_FILE+" not found!");
        }
    }

    /**
     * Load a png from the file, convert it to IconImage
     * and attach it to the facelist.
     *
     * @param fname     filename, absolute path
     * @param name      name of the png (e.g. blocked.111.png)
     */
    private void addPNGFace(String fname, String name) {
        try {
            // this is the png loader using the sixlegs library:
            FileInputStream stream;
            BufferedInputStream raw;

            faceObjects[faceListCount] = new FaceObject();
            try {
                stream = new FileInputStream(fname);
                raw = new BufferedInputStream(stream);

                PngImage png = new PngImage(raw); // read png data from file

                if (png == null)
                    System.out.println(fname+" is null!");
                // very important: need to free pixel-data after every step
                png.setFlushAfterNextProduction(true);

                // convert png to ImageIcon, then set face
                ImageIcon im = new ImageIcon(Toolkit.getDefaultToolkit().createImage(png));

                faceObjects[faceListCount].setFace(im);

                png=null;

                //im=null;
                raw.close(); raw=null;
                stream.close(); stream=null;
                //System.runFinalization();
                //System.gc();
            }
            catch  (IOException e) {
                System.out.println("Png Load error :"+fname);
                raw=null;
                return;
            }
        }
        catch (NullPointerException e) {
            // This is the "normal" png loader in case the sixlegs
            // lib makes bullshit, but this no longer happens. =)
            System.out.println("sixlegs loader failed for: "+fname);

            ImageIcon im = new ImageIcon(fname);
            faceObjects[faceListCount] = new FaceObject();
            faceObjects[faceListCount].setFace(im);
        }

        // generating the face name:
        String face;
        if (m_control.imageSet != null) {
            // we have to snip out the imageset-information here from
            // the 'name', and the ".png": (e.g. blocked.base.111.png -> blocked.111)
            int first_dot=0; int second_dot=0;
            for (int t=0; t<name.length() && second_dot == 0; t++) {
                if (name.charAt(t) == '.') {
                    if (first_dot == 0) first_dot = t;
                    else second_dot = t;
                }
            }

            if (first_dot != 0 && second_dot != 0)
                face = name.substring(0, first_dot) + name.substring(second_dot, name.length()-4);
            else
                face = name.substring(0, name.length()-4);
        }
        else {
            // no image set: we need only cut off the ".png"
            face = name.substring(0, name.length()-4);
        }

        faceObjects[faceListCount].setName(face);
        faceObjects[faceListCount].setPath(fname);
        faceHashTable.put(face, new Integer(faceListCount));
        faceListCount++;
        png_load_counter++;       // count num. of loaded pngs

        //System.runFinalization(); // free as much memory as possible

        // run garbage collector after every 300 loaded pngs
        // (the jre really is stupid enough to exceed mem before doing this)
        if (png_load_counter > 300) {
            System.runFinalization();
            System.gc();
            png_load_counter=0;
        }
    }

    public void connectFaces() {
        int i,s;
        String aname;
        Integer num;

        // run through arches
        for(s=0;s<archNodeListCount;s++) {
            aname = archNodeList[s].arch.getFaceRealName();
            if(aname != null) {
                num = (Integer) faceHashTable.get(aname);
                if(num!=null) {
                    archNodeList[s].arch.setRealFaceNr(num.intValue());
                }
            }
            archNodeList[s].arch.setObjectFace();

        }

    }

    /**
     * Wrapper method for arch collecting. Either CF or Daimonin method for
     * collecting arches is chosen here.
     */
    public void collectArches() {
        if (IGUIConstants.isoView)
            collectDaimoninArches();
        else
            collectCFArches();
    }

    /**
     * Collect the existing arches and create archive-files for editor use
     * as well as the CF server. The arches also get a special path variable
     * included which is used in the editor to cathegorize the arches.
     *
     * Output is: "archetypes", "crossfire.png"
     */
    private void collectCFArches() {
        ArchObject arch;
        String def ="00000", numstring, num;
        byte b[] = new byte[1024*50];            // hm, 50 kb should be enough
        int num_bytes, nr, tmp;
        File dfile = null;   // data file for writing

        CMainStatusbar.getInstance().setText("Collect Archfile: write archetypes");

        // open the process bar
        int updateProg = 0;  // counts when the progress bar should be next updated
        CollectProgressBar pbar = new CollectProgressBar(m_control.getArchObjectStack().getArchCount(),
                                            "Collecting Arches...");

        try {
            // create the resource-directorys if they don't exist yet
            if (IGUIConstants.CONFIG_DIR != null && IGUIConstants.CONFIG_DIR.length() > 0) {
                File dir = new File(IGUIConstants.CONFIG_DIR);
                if (!dir.exists() || !dir.isDirectory()) {
                    // create the config dir
                    dir = new File("resource"); dir.mkdir();
                    dir = new File(IGUIConstants.CONFIG_DIR); dir.mkdir();
                }

                dfile = new File(IGUIConstants.CONFIG_DIR+File.separator+IGUIConstants.ARCH_FILE);
            }
            else
                dfile = new File(IGUIConstants.ARCH_FILE);

            // now open the output-stream
            DataOutputStream binFile = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(dfile)));

            int count = 0; // count how much arches we've collected

            // loop through all existing ArchPanels and find all arches
            // along with their display-cathegory
            for(CArchPanel.panelNode node = CArchPanel.getStartPanelNode();
                node != null; node = node.next) {

                int[] numList = node.data.getListNodeNrArray();        // list of nodenumbers
                String[] cathList = node.data.getListCathegoryArray(); // list of cath. strings

                int multiparts = 0;

                // process every arch in this panel
                for (int i=0; i<numList.length; i++) {

                    arch = m_control.getArch(numList[i]);

                    if(arch.getRefFlag())
                        System.out.println("Collect Error: Multipart Tail in Panel found!");

                    binFile.writeBytes("Object "+arch.getArchName()+"\n");

                    if(arch.getObjName()!=null)
                        binFile.writeBytes("name "+arch.getObjName()+"\n");
   //                 if(arch.getFaceObjName() != null)
   //                     binFile.writeBytes("face "+arch.getFaceObjName()+"\n");
                    if(arch.getArchTypNr()>0)
                        binFile.writeBytes("type "+arch.getArchTypNr()+"\n");

                    // special: add a string-attribute with the display-cathegory
                    binFile.writeBytes("editor_folder "+node.getTitle()+"/"+cathList[i]+"\n");

                    // add message text
                    if (arch.getMsgText() != null && arch.getMsgText().trim().length() > 0) {
                        binFile.writeBytes("msg\n"+arch.getMsgText().trim()+"\nendmsg\n");
                    }

                    binFile.writeBytes(arch.getArchText());
                    if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                        binFile.writeBytes("\n");

                    binFile.writeBytes("end\n");
                    count++;
                    if (count % 100 == 0) pbar.setValue(count);

                    // if multi-head, we must attach the tail
                    if (arch.getRefCount() > 0) {
                        // process the multipart tail:
                        multiparts = arch.getRefCount();
                        for (int j=1; j <= multiparts; j++) {
                            arch = m_control.getArch(numList[i]+j);

                            if (!arch.getRefFlag()) {
                                System.out.println("Multipart object is too short!");
                                ArchObject before = m_control.getArch(numList[i]);
                                if (before != null) {
                                    System.out.println("-> "+multiparts+" tails expected for multipart: '"+before.getArchName()+"',");
                                    System.out.println("   but arch '"+arch.getArchName()+"' follows on position "+j+" and it's not a tail.");
                                }
                            }

                            binFile.writeBytes("More\n");

                            binFile.writeBytes("Object "+arch.getArchName()+"\n");

                            if(arch.getObjName()!=null)
                                binFile.writeBytes("name "+arch.getObjName()+"\n");
//                            if(arch.getFaceName() != null)
//                                binFile.writeBytes("face "+arch.getFaceObjName()+"\n");
                            if(arch.getArchTypNr()>0)
                                binFile.writeBytes("type "+arch.getArchTypNr()+"\n");

                            // special: add a string-attribute with the display-cathegory
                            //binFile.writeBytes("editor_folder "+node.getTitle()+"/"+cathList[i]+"\n");

                            binFile.writeBytes(arch.getArchText());
                            if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                                binFile.writeBytes("\n");

                            // position of multi relative to head
                            if(arch.getRefFlag()) {
                                if(arch.getRefX()!=0)
                                    binFile.writeBytes("x "+arch.getRefX()+"\n");
                                if(arch.getRefY()!=0)
                                    binFile.writeBytes("y "+arch.getRefY()+"\n");

                            }
                            binFile.writeBytes("end\n");
                            count++;
                            if (count % 100 == 0) pbar.setValue(count);
                        }
                    }

                }
            }

            // finally we need to get the "map"-arch, which is not in the panels
            boolean maparch_found = false;
            for (int i=0; i<m_control.getArchCount() && !maparch_found; i++) {
                arch = m_control.getArch(i);
                if (arch.getArchName().compareTo(ArchObjectParser.STARTARCH_NAME)==0) {
                    // process map arch
                    maparch_found = true;
                    count++;

                    binFile.writeBytes("Object "+arch.getArchName()+"\n");

                    // map object hack: x/y is normally a reference for multi
                    // part arches - i include this hack until we rework the
                    // arch objects with more useful script names
                    if(arch.getArchName().compareTo(ArchObjectParser.STARTARCH_NAME)==0) {
                        binFile.writeBytes("x "+arch.getRefX()+"\n");
                        binFile.writeBytes("y "+arch.getRefY()+"\n");
                    }

                    if(arch.getObjName()!=null)
                        binFile.writeBytes("name "+arch.getObjName()+"\n");
//                    if(arch.getFaceName() != null)
//                        binFile.writeBytes("face "+arch.getFaceObjName()+"\n");
                    if(arch.getArchTypNr()>0)
                        binFile.writeBytes("type "+arch.getArchTypNr()+"\n");

                    binFile.writeBytes(arch.getArchText());
                    if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                        binFile.writeBytes("\n");

                    binFile.writeBytes("end\n");
                }
            }

            // check if we still missed any arches
            if (count-m_control.getArchCount() != 0) {
                System.out.println((m_control.getArchCount()-count)+" arches have been missed during collect!");
            }
            pbar.setValue(count);

            binFile.close();
            binFile=null;
        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting: archfile\n");
            return;
        }
        /*
        try {
            CMainStatusbar.getInstance().setText("Collect Archfile: write animations");
            DataOutputStream binFile = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream("animations")));

            int count = m_control.animationObject.getAnimObjectCounter();

            // sort it...
            Arrays.sort(m_control.animationObject.nameStringTable(), 0,count);
            for(int i=0;i<count;i++) {
                nr = m_control.animationObject.findAnimObject(m_control.animationObject.getNameString(i));
                binFile.writeBytes("anim "+m_control.animationObject.getAnimObjectName(nr)+"\n");
                binFile.writeBytes(m_control.animationObject.getAnimObjectList(nr));
                binFile.writeBytes("mina\n");
            }
            binFile.close();
            binFile=null;

        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting: animation file\n");
            return;
        }
        */
        try {
            CMainStatusbar.getInstance().setText("Collect Archfile: write images");

            // create the resource-directorys if they don't exist yet
            dfile = null;
            if (IGUIConstants.CONFIG_DIR != null && IGUIConstants.CONFIG_DIR.length() > 0)
                dfile = new File(IGUIConstants.CONFIG_DIR+File.separator+IGUIConstants.PNG_FILE);
            else
                dfile = new File(IGUIConstants.PNG_FILE);

            FileInputStream fin = null;
            DataOutputStream binFile = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(dfile)));

            // write number 0 dummy entry
            String pname;
            num = Integer.toString(0);
            numstring = def.substring(0,5-num.length())+num;
            num_bytes = 0;

            try {
                    fin = new FileInputStream(faceObjects[0].getPath());
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("You have to give me the name of a file to open.");
                return;
            }
            catch (FileNotFoundException e) {
                System.out.println("Could not open input file " + faceObjects[0].getPath());
                return;
            }

            try {
                num_bytes = fin.read(b,0,50000);
            }
            catch (IOException e) {
                System.out.println("Unexpected exception: " + e);
                return;
            }

            if(num_bytes == -1) {
                System.out.println("Unexpected EOF: " + faceObjects[0].getPath()+" >> "+num_bytes);
                return;
            }
            fin.close();
            fin=null;
            binFile.writeBytes("IMAGE "+numstring+" "+num_bytes+" ./arch/system/bug.111\n");
            binFile.write(b,0,num_bytes);

            // now write all pngs into the file
            pbar.setLabel("Collecting Images...", 0, faceListCount);
            for(int i=0;i<faceListCount;i++) {
                fin = null;
                num_bytes = 0;

                try {
                    // try to open the png image file
                    fin = new FileInputStream(faceObjects[i].getPath());
                }
                catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println("You have to give me the name of a file to open.");
                    return;
                }
                catch (FileNotFoundException e) {
                    System.out.println("Could not open input file " + faceObjects[i].getPath());
                    return;
                }

                try {
                    // read size and byte-data from png file
                    num_bytes = fin.read(b,0,50000);
                }
                catch (IOException e) {
                    System.out.println("Unexpected exception: " + e);
                    return;
                }

                if(num_bytes == -1) {
                    System.out.println("Unexpected EOF: " + faceObjects[i].getPath()+" >> "+num_bytes);
                    return;
                }

                // close png file
                fin.close();
                fin=null;

                // create path string
                if ((tmp = faceObjects[i].getPath().indexOf("arch"+File.separator)) >= 0) {
                    // cut off the path before "arch"
                    pname = faceObjects[i].getPath().substring(tmp).replace('\\', '/').replace('.', '%');
                }
                else if ((tmp = faceObjects[i].getPath().indexOf("arch")) >= 0) {
                    // cut off the path before "arch"
                    pname = faceObjects[i].getPath().substring(tmp).replace('\\', '/').replace('.', '%');
                }
                else {
                    // hope nobody renames his arch-dir and ends up in here
                    pname = "arch"+faceObjects[i].getPath().substring(m_control.getArchDefaultFolder().length()).replace('\\', '/');
                }
                pname = "./"+pname.substring(0, pname.lastIndexOf("/")+1);

                // now write this png data into the big collected png file
                binFile.writeBytes("IMAGE "+numstring+" "+num_bytes+" "+pname+faceObjects[i].getName()+"\n");
                binFile.write(b,0,num_bytes);

                if (i % 100 == 0) pbar.setValue(i);
            }
            pbar.setValue(faceListCount); // finished
            pbar.finished(m_control.getArchCount(), faceListCount);

            // close png file
            binFile.close();
            binFile=null;
        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting images\n");
        }

        CMainStatusbar.getInstance().setText("Collect Arches: done.");
    }

    /**
     * Collect the existing arches and create archive-files for editor use
     * as well as theDaimonin server. The arches also get a special path variable
     * included which is used in the editor to cathegorize the arches.
     *
     * Output is: "archetypes", "atrinik.0"
     */
    private void collectDaimoninArches() {
        ArchObject arch;
        String def ="00000", numstring, num;
        byte b[] = new byte[1024*50];            // hm, 50 kb should be enough
        int num_bytes, nr;
        File dfile = null;   // data file for writing

        CMainStatusbar.getInstance().setText("Collect Archfile: write archetypes");

        // open the process bar
        int updateProg = 0;  // counts when the progress bar should be next updated
        CollectProgressBar pbar = new CollectProgressBar(m_control.getArchObjectStack().getArchCount(),
                                            "Collecting Arches...");

        try {
            artifact_count=0;
	        dfile = new File(m_control.getArchDefaultFolder()+File.separator+IGUIConstants.ARCH_FILE);
            // now open the output-stream
            DataOutputStream binFile = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(dfile)));

            int count = 0; // count how much arches we've collected

            // loop through all existing ArchPanels and find all arches
            // along with their display-cathegory
            for(CArchPanel.panelNode node = CArchPanel.getStartPanelNode();
                node != null; node = node.next) {

                int[] numList = node.data.getListNodeNrArray();        // list of nodenumbers
                String[] cathList = node.data.getListCathegoryArray(); // list of cath. strings

                int multiparts = 0;

                // process every arch in this panel
                for (int i=0; i<numList.length; i++) {

                    arch = m_control.getArch(numList[i]);

                    // exclude arches generated from artifacts file from collection
                    if(arch.getArtifactFlag()) {
                        artifact_count++;
                        continue;
                    }

                    if(arch.getRefFlag())
                        System.out.println("Collect Error: Multipart Tail in Panel found!");

                    binFile.writeBytes("Object "+arch.getArchName()+"\n");

                    if(arch.getObjName()!=null)
                        binFile.writeBytes("name "+arch.getObjName()+"\n");
 //                   if(arch.getFaceName() != null)
 //                       binFile.writeBytes("face "+arch.getFaceName()+"\n");
                    if(arch.getArchTypNr()>0)
                        binFile.writeBytes("type "+arch.getArchTypNr()+"\n");
                    if(arch.getMultiShapeID()>0)
                        binFile.writeBytes("mpart_id "+arch.getMultiShapeID()+"\n");
                    if(arch.getMultiPartNr()>0)
                        binFile.writeBytes("mpart_nr "+arch.getMultiPartNr()+"\n");

                    if(arch.getMsgText() != null) {
                    	binFile.writeBytes("msg\n");
                    	if(arch.getMsgText().length() >1) {
                        binFile.writeBytes(arch.getMsgText());
                        if(arch.getMsgText().lastIndexOf(0x0a)!=arch.getMsgText().length()-1)
                            binFile.writeBytes("\n");
                    	}
                    	binFile.writeBytes("endmsg\n");
                    }


                    // special: add a string-attribute with the display-cathegory
                    binFile.writeBytes("editor_folder "+node.getTitle()+"/"+cathList[i]+"\n");

                    binFile.writeBytes(arch.getArchText());
                    if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                        binFile.writeBytes("\n");

                    binFile.writeBytes("end\n");
                    count++;
                    if (count % 100 == 0) pbar.setValue(count);

                    // if multi-head, we must attach the tail
                    if (arch.getRefCount() > 0) {
                        // process the multipart tail:
                        multiparts = arch.getRefCount();
                        for (int j=1; j <= multiparts; j++) {
                            arch = m_control.getArch(numList[i]+j);

                            if (!arch.getRefFlag())
                                System.out.println("Multipart object too short!");

                            binFile.writeBytes("More\n");

                            binFile.writeBytes("Object "+arch.getArchName()+"\n");

                            if(arch.getObjName()!=null)
                                binFile.writeBytes("name "+arch.getObjName()+"\n");
 //                           if(arch.getFaceName() != null)
 //                               binFile.writeBytes("face "+arch.getFaceName()+"\n");
                            if(arch.getArchTypNr()>0)
                                binFile.writeBytes("type "+arch.getArchTypNr()+"\n");
                            if(arch.getMultiShapeID()>0)
                                binFile.writeBytes("mpart_id "+arch.getMultiShapeID()+"\n");
                            if(arch.getMultiPartNr()>0)
                                binFile.writeBytes("mpart_nr "+arch.getMultiPartNr()+"\n");

                            if(arch.getMsgText() != null) {
                                binFile.writeBytes("msg\n");
                                if(arch.getMsgText().length() >1) {
                                binFile.writeBytes(arch.getMsgText());
                                if(arch.getMsgText().lastIndexOf(0x0a)!=arch.getMsgText().length()-1)
                                    binFile.writeBytes("\n");
                                }
                                binFile.writeBytes("endmsg\n");
                            }

                            // special: add a string-attribute with the display-cathegory
                            //binFile.writeBytes("editor_folder "+node.getTitle()+"/"+cathList[i]+"\n");

                            binFile.writeBytes(arch.getArchText());
                            if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                                binFile.writeBytes("\n");

                            // position of multi relative to head
                            if(arch.getRefFlag()) {
                                if(arch.getRefX()!=0)
                                    binFile.writeBytes("x "+arch.getRefX()+"\n");
                                if(arch.getRefY()!=0)
                                    binFile.writeBytes("y "+arch.getRefY()+"\n");

                            }
                            binFile.writeBytes("end\n");
                            count++;
                            if (count % 100 == 0) pbar.setValue(count);
                        }
                    }

                }
            }

            // finally we need to get the "map"-arch, which is not in the panels
            boolean maparch_found = false;
            for (int i=0; i<m_control.getArchCount() && !maparch_found; i++) {
                arch = m_control.getArch(i);
                if (arch.getArchName().compareTo(ArchObjectParser.STARTARCH_NAME)==0) {
                    // process map arch
                    maparch_found = true;
                    count++;

                    binFile.writeBytes("Object "+arch.getArchName()+"\n");

                    // map object hack: x/y is normally a reference for multi
                    // part arches - i include this hack until we rework the
                    // arch objects with more useful script names
                    if(arch.getArchName().compareTo(ArchObjectParser.STARTARCH_NAME)==0) {
                        binFile.writeBytes("x "+arch.getRefX()+"\n");
                        binFile.writeBytes("y "+arch.getRefY()+"\n");
                    }

                    if(arch.getObjName()!=null)
                        binFile.writeBytes("name "+arch.getObjName()+"\n");
 //                   if(arch.getFaceName() != null)
 //                       binFile.writeBytes("face "+arch.getFaceName()+"\n");
                    if(arch.getArchTypNr()>0)
                        binFile.writeBytes("type "+arch.getArchTypNr()+"\n");

                    binFile.writeBytes(arch.getArchText());
                    if(arch.getArchText().lastIndexOf(0x0a) != arch.getArchText().length()-1)
                        binFile.writeBytes("\n");

                    binFile.writeBytes("end\n");
                }
            }

            // check if we still missed any arches
            if ((count+artifact_count)-m_control.getArchCount() != 0) {
                System.out.println((m_control.getArchCount()-count)+" arches have been missed during collect!");
            }
            pbar.setValue(count);

            binFile.close();
            binFile=null;
        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting: archfile\n");
            return;
        }

        try {
            CMainStatusbar.getInstance().setText("Collect Archfile: write animations");
            DataOutputStream binFile = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(m_control.getArchDefaultFolder()+File.separator+"animations")));

            int count_ani = m_control.animationObject.getAnimObjectCounter();

            // sort it...
            pbar.setLabel("Collecting Animations...", 0, count_ani);
            Arrays.sort(m_control.animationObject.nameStringTable(), 0,count_ani);
            for(int i=0;i<count_ani;i++) {
                nr = m_control.animationObject.findAnimObject(m_control.animationObject.getNameString(i));
                binFile.writeBytes("anim "+m_control.animationObject.getAnimObjectName(nr)+"\n");
                binFile.writeBytes(m_control.animationObject.getAnimObjectList(nr));
                binFile.writeBytes("mina\n");
                if (count_ani % 10 == 0) pbar.setValue(count_ani);
            }
            binFile.close();
            binFile=null;

        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting: animation file\n");
            return;
        }

        try {
            CMainStatusbar.getInstance().setText("Collect Archfile: write images");

            // create the resource-directorys if they don't exist yet
            dfile = null;

            FileWriter al =  new FileWriter(m_control.getArchDefaultFolder()+File.separator+IGUIConstants.BMAPS_FILE);
            BufferedWriter textFile = new BufferedWriter(al);

            dfile = new File(m_control.getArchDefaultFolder()+File.separator+IGUIConstants.PNG_FILE);
            FileInputStream fin = null;
            DataOutputStream binFile = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(dfile)));

            // write number 0 dummy entry
            String pname;
            num = Integer.toString(0);
            numstring = def.substring(0,5-num.length())+num;
            num_bytes = 0;

            try {
                fin = new FileInputStream(m_control.getArchDefaultFolder()+"/dev/editor/bug.101.png");
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("You have to give me the name of a file to open.");
                return;
            }
            catch (FileNotFoundException e) {
                System.out.println("Could not open input file " +m_control.getArchDefaultFolder()+"/dev/editor/bug.101");
                return;
            }

            try {
                num_bytes = fin.read(b,0,50000);
            }
            catch (IOException e) {
                System.out.println("Unexpected exception: " + e);
                return;
            }

            if(num_bytes == -1) {
                System.out.println("Unexpected EOF: arch/dev/editor/bug.101"+m_control.getArchDefaultFolder()+" >> "+num_bytes);
                return;
            }
            fin.close();
            fin=null;
            binFile.writeBytes("IMAGE "+numstring+" "+num_bytes+" bug.101\n");
            binFile.write(b,0,num_bytes);
            textFile.write(numstring+" bug.101\n");

            // now write all pngs into the file
            pbar.setLabel("Collecting Images...", 0, faceListCount);
            for(int i=0;i<faceListCount;i++) {
                fin = null;
                num_bytes = 0;
	        num = Integer.toString(i+1);
        	numstring = def.substring(0,5-num.length())+num;

                try {
                    // try to open the png image file
                    fin = new FileInputStream(faceObjects[i].getPath());
                }
                catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println("You have to give me the name of a file to open.");
                    return;
                }
                catch (FileNotFoundException e) {
                    System.out.println("Could not open input file " + faceObjects[i].getPath());
                    return;
                }

                try {
                    // read size and byte-data from png file
                    num_bytes = fin.read(b,0,50000);
                }
                catch (IOException e) {
                    System.out.println("Unexpected exception: " + e);
                    return;
                }

                if(num_bytes == -1) {
                    System.out.println("Unexpected EOF: " + faceObjects[i].getPath()+" >> "+num_bytes);
                    return;
                }

                // close png file
                fin.close();
                fin=null;

                // create path string
                // pname = "./arch"+faceObjects[i].getPath().substring(m_control.getArchDefaultFolder().length()).replace('\\', '/');
                // pname = pname.substring(0, pname.lastIndexOf("/")+1);

                // now write this png data into the big collected png file
                // binFile.writeBytes("IMAGE "+numstring+" "+num_bytes+" "+pname+faceObjects[i].getName()+"\n");
                binFile.writeBytes("IMAGE "+numstring+" "+num_bytes+" "+faceObjects[i].getName()+"\n");
                binFile.write(b,0,num_bytes);
	        textFile.write(numstring+" "+faceObjects[i].getName()+"\n");

                if (i % 100 == 0) pbar.setValue(i);
            }
            pbar.setValue(faceListCount); // finished
            pbar.finished(m_control.getArchCount(), faceListCount);

            // close png file
            binFile.close();
            binFile=null;
            textFile.close();
            textFile=null;
        }
        catch (IOException e) {
            System.out.println("Error: Exception collecting images\n");
        }

        CMainStatusbar.getInstance().setText("Collect Arches: done.");
    }

    /**
     * Subclass: CollectProcessBar is a popup dialog for the mainview
     * which displays a process bar while arches are collected
     */
    private class CollectProgressBar extends JDialog {
        JProgressBar bar;  // the progress bar
        JLabel label;

        /**
         * Constructor - builds the window layout and shows it
         * @param max   initial maximum of progress points
         * @param text  the initial label text
         */
        public CollectProgressBar(int max, String text) {
            super(m_control.getMainView(), "Collect CF Arches", false);
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // can't close

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 30, 15));
            label = new JLabel(text);
            label.setForeground(Color.black);
            panel.add(label);

            bar = new JProgressBar(0, max);
            bar.setValue(0);
            bar.setStringPainted(true);
            bar.setAlignmentY(JProgressBar.CENTER);
            panel.add(bar);

            getContentPane().add(panel);
            setSize(300, 140);
            setLocationRelativeTo(m_control.getMainView());

            setVisible(true);
            update(getGraphics());
        }

        /**
         * Set the progress value
         * @param val   new progress value
         */
        public void setValue(int val) {
            if (val > bar.getMaximum()) val = bar.getMaximum();
            bar.setValue(val);
            update(getGraphics());
        }

        /**
         * Set the label text, a new value and new maximum
         * @param text   new label text
         */
        public void setLabel(String text, int val, int max) {
            label.setText(text);
            bar.setMaximum(max);
            bar.setValue(val);
            update(getGraphics());
        }

        /**
         * After finishing the collect, ask wether to load from
         * the collection files in future
         * @param arch    number of loaded arches
         * @param image   number of loaded images
         */
        public void finished(int arch, int image) {
            setVisible(false);
            removeAll();

            if (IGUIConstants.isoView == false) {
                if ( m_control.askConfirm(
                                "Collect CF Arches",
                                "Successfully collected "+arch+" arches and "+image+" images.\n\n"+
                                "Do you want to load arches from this collection\n"+
                                "in future? (Doing so would reduce loading time.)") ) {
                    // set loading from collect files
                    CSettings.getInstance(IGUIConstants.APP_NAME).setProperty(CMainControl.LOAD_ARCH_COLL, "true");
                    m_control.readGlobalSettings(); // save this property
                }
                dispose(); // kill this dialog
            }
        }
    }

    public class ArchObjectNode
    {
        ArchObject arch;
        ArchObjectNode next;
        public ArchObjectNode (ArchObject data)
            {
            this.arch = data;
            this.next = null;
        }
    }; // End of class

}; // End of class stack
