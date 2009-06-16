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
import java.io.*;

/**
 * The <code>ArchObject</code> class handles the Crossfire arch objects.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ArchObject implements Cloneable {
    private static CMainControl m_control;          // reference to the main control

    public static final int TYPE_UNSET = -666; // means the arch has no type set

    private static ArchObjectStack archstack; // static reference to the archstack (-> default arches)
    private static CFArchTypeList typelist;   // static reference to the typelist (-> find syntax errors)
    static int my_id_counter=0;

    private int my_id;              // the one and only id
                                    // unique arch object id at editor runtime

    private String faceObjName;        // face name. can be come from animation or face
    private String faceRealName;    // object face name "face <name>"
    private String archName;        // arch Name
    private String objName;
    private String animName;        // animation <animName>

    private StringBuffer archText;  // the arch text (contains only the differences from default arch)
                                    // it is a rule that every line ends with a '\n', including the last line
    private StringBuffer msgText;   // msg text buffer
    private StringBuffer animText;  // anim text buffer
    private StringBuffer loreText;  // lore text buffer
    private int archTextCount;      // lines inserted in ArchText

    private int nodenr;                  // we are (internal) arch nr in node list
                                         // the nodenr determines the (default) archetype

    private int mapx;                    // if on map, we are here
    private int mapy;

    private MultiArchData multi;         // data for multi-arches - null for single-square arches

    private ScriptArchData script;       // data for scripted events - null if no events defined

    private int faceobjnr;                  // the index of faceImages[]
    private int facenr;                  // the index of faceImages[]
    private int animnr;
    private int faceobjdesc;
    private boolean setdir;
    private int direction;

    private boolean noface;

    private boolean editflag;            // if true, object is in a editor
                                         // for example in the map arch panel
    private boolean artifacts_flag;      // if set, this is not a "real" arch
                                         // It will NOT be included in the arch collection

    private ArchObject next;             // to chain ArchObjects in maps
    private ArchObject prev;             // same
    private ArchObject tempptr;          // we need this for chain the arches

    private ArchObject container;        // we are in this container
                                         // if null, we are map object
    private ArchObject inv_prev;         // if null, and container != null
                                         // we are first object in this container
    private ArchObject inv_next;         // next object.if null, we are last

    private ArchObject inv_start;        // our inventory, first object in
    private ArchObject inv_last;         // our inventory, last object in
    private int arch_type;               // CF object type of the arch

    private int intern_temp;             // used for drawing
    private int edit_type;               // for view settings
    private AutojoinList join;           // if nonzero, pointing to the list of autojoining
                                         // archetypes
    public ArchObject() {
        multi = null;         // this object stays 'null' for all single-tile arches
        script = null;        // this object stays 'null' unless there are events defined

        my_id=my_id_counter++; // increase ID counter for every new arch created

        archName = null;
        animName = null;
        archText = new StringBuffer("");
        msgText = animText = loreText = null;
        archTextCount = 0;    // lines inserted in archText

        direction = 0; // 0 is the default dir, also in the server
        animnr = -1;
        setdir = false;
        noface = false;
        objName = null;
        intern_temp = 0;
        container = inv_prev = inv_next = null;

        artifacts_flag = false; // will be true for arches from the artifacts file
        faceRealName = null;
        faceobjnr=-1;
        faceobjdesc=-1;
        faceObjName = null;        // if there is a face cmd, this is the face name
        facenr = -1;            // if we have a face AND we have loaded the face, this is his number.
                                // if faceName!=null and facenr==-1, we haven't loaded the face
                                // if faceName == null and facenr != -1, we got the face from default arch!

        editflag = false;
        join = null;            // no autojoin list per default
        arch_type = ArchObject.TYPE_UNSET; // type must be set
        nodenr = -1;            // as default we are not in a node list
        next = null;
        prev = null;
        tempptr = null;
        mapx=0;
        mapy=0;
        edit_type=0;

		script = new ScriptArchData(this);
    }

    // set static references: arch stack and typelist
    public static void setArchStack(ArchObjectStack stack) {archstack = stack;}
    public static ArchObjectStack getArchStack() {return archstack;}
    public static void setTypeList(CFArchTypeList tlist) {typelist = tlist;}
    public static void setMControl(CMainControl control){m_control = control;}

    // edit type is the editable value from arch
    // here it is used to identify the layer type
    public int getEditType() {
        return(edit_type);
    }

    /**
     * @return the default <code>ArchObject</code> for this arch
     */
    public ArchObject getDefaultArch() {
        return archstack.getArch(nodenr);
    }

    /**
     * @return true when this ArchObject is a default arch from the stack
     */
    public boolean isDefaultArch() {
        return (archstack.getArch(nodenr) == this);
    }

    /**
     * Get the EditType of an ArchObject (e.g. floor, monster, etc).
     * These are determined by the various attributes of the arch
     * (->archText).
     *
     * @param check_type    bitmask containing the edit type(s) to be calculated
     * @return new edit_type for this arch
     */
    public int calculateEditType(int check_type) {
        int no_pick, no_pass;                   // some attributes
        ArchObject defarch = getDefaultArch();  // default arch

        /* if one of the types in check_type already is in edit_type,
        // we exclude that one
        if ((check_type & edit_type) != 0)
            check_type -= (check_type & edit_type);*/

        // bail out if nothing to do
        if (check_type == 0) return(edit_type);

        if (edit_type == IGUIConstants.TILE_EDIT_NONE)
            edit_type = 0;
        else if (edit_type != 0) {
            // all flags from 'check_type' must be unset in this arch because they get recalculated now
            edit_type &= ~check_type;
        }

        if ((check_type & IGUIConstants.TILE_EDIT_BACKGROUND) != 0 &&
            getAttributeValue("is_floor", defarch) == 1 &&
            getAttributeValue("no_pick", defarch) == 1) {
            // Backgroud: floors
            edit_type |= IGUIConstants.TILE_EDIT_BACKGROUND;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_MONSTER) != 0 &&
            getAttributeValue("alive", defarch) == 1 &&
            (getAttributeValue("monster", defarch) == 1 ||
            getAttributeValue("generator", defarch) == 1)) {
            // Monster: monsters/npcs/generators
            edit_type |= IGUIConstants.TILE_EDIT_MONSTER;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_WALL) != 0 &&
            arch_type == 0 && getAttributeValue("no_pass", defarch) == 1) {
            // Walls
            edit_type |= IGUIConstants.TILE_EDIT_WALL;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_CONNECTED) != 0 &&
            getAttributeValue("connected", defarch) != 0) {
            // Connected Objects
            edit_type |= IGUIConstants.TILE_EDIT_CONNECTED;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_EXIT) != 0 &&
            arch_type == 66 || arch_type == 41 || arch_type == 95) {
            // Exit: teleporter/exit/trapdoors
            edit_type |= IGUIConstants.TILE_EDIT_EXIT;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_TREASURE) != 0 &&
            getAttributeValue("no_pick", defarch) == 0 && (arch_type == 4 ||
            arch_type == 5 || arch_type == 36 || arch_type == 60 ||
            arch_type == 85 || arch_type == 111 || arch_type == 123 ||
            arch_type == 124 || arch_type == 130)) {
            // Treasure: randomtreasure/money/gems/potions/spellbooks/scrolls
            edit_type |= IGUIConstants.TILE_EDIT_TREASURE;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_DOOR) != 0 &&
            arch_type == 20 || arch_type == 23 || arch_type == 26 ||
            arch_type == 91 || arch_type == 21 || arch_type == 24) {
        // Door: door/special door/gates  + keys
        edit_type |= IGUIConstants.TILE_EDIT_DOOR;
        }
        if ((check_type & IGUIConstants.TILE_EDIT_EQUIP) != 0 &&
            getAttributeValue("no_pick", defarch) == 0 && ((arch_type >= 13 &&
            arch_type <= 16) || arch_type == 33 || arch_type == 34 ||
            arch_type == 35 || arch_type == 39 || arch_type == 70 ||
            arch_type == 87 || arch_type == 99 || arch_type == 100 ||
            arch_type == 104 || arch_type == 109 || arch_type == 113 ||
            arch_type == 122 || arch_type == 3)) {
            // Equipment: weapons/armour/wands/rods
            edit_type |= IGUIConstants.TILE_EDIT_EQUIP;
        }

        return(edit_type);
    }

    /**
     * Get the value of an arch attribute from the archText
     * both of the arch itself and if n.e. in it's default arch.
     * If the attribute doesn't exist in either one, or the value is
     * not a number, zero is returned.
     *
     * @param attr        search for "attr &lt;value&gt;"
     * @param defarch     deault arch of 'this' arch
     *                    defarch=null means this *is* a default arch, or
     *                    we just want to ignore the default arch
     * @return &lt;value&gt;, zero if not found
     */
    public int getAttributeValue(String attr, ArchObject defarch) {
        String AText = archText.toString(); // The "real" Archtext from arch & defarch
        String line;                        // tmp string
        int i, j;
        int result=0;                       // returned value

        // Add all attributes from defarch that don't already exist in AText
        if (defarch != null)
            AText = AText + diffArchText(defarch.getArchText(), true);

        if (!AText.endsWith("\n"))
            AText = AText.concat("\n");  // string should end with '\n', see below
        attr = attr.trim() + " ";        // attr must be followed by space

        // Check line by line for the string 'attr'
        for (i=0, j=0; i<AText.length(); i++) {
            if (AText.charAt(i) == '\n') {
                line = AText.substring(j, i).trim();  // get one line from 'AText'

                try {
                    // try to read the value behind 'attr'
                    if (line.startsWith(attr)) {
                        result = Integer.parseInt(line.substring(attr.length()).trim());
                    }
                }
                catch (NumberFormatException e) {
                    result = 0;
                }

                j=i+1;
            }
        }
        return result;
    }

    /**
     * Get the String of an arch attribute from the archText
     * both of the arch itself and if n.e. in it's default arch.
     * If the attribute doesn't exist in either one, an empty
     * String "" is returned.
     *
     * @param attr          search for "attr &lt;string&gt;"
     * @param defarch       deault arch of 'this' arch
     *                      defarch=null means this *is* a default arch, or
     *                      we just want to ignore the default arch
     * @return &lt;string&gt;, "" if not found
     */
    public String getAttributeString(String attr, ArchObject defarch) {
        String AText = archText.toString(); // The "real" Archtext from arch & defarch
        String line;                        // tmp string
        int i, j;
        String result="";                   // returned String

        // Add all attributes from defarch that don't already exist in AText
        if (defarch != null)
            AText = AText + diffArchText(defarch.getArchText(), true);
        attr = attr.trim() + " ";      // attr must be followed by space

        if (!AText.endsWith("\n"))
            AText = AText.concat("\n");  // string should end with '\n', see below

        // Check line by line for the string 'attr'
        for (i=0, j=0; i<AText.length(); i++) {
            if (AText.charAt(i) == '\n') {
                line = AText.substring(j, i).trim();  // get one line from 'AText'

                // take the String behind 'attr'
                if (line.startsWith(attr))
                result = line.substring(attr.length()).trim();

                j=i+1;
            }
        }
        return result;
    }

    public void setEditType(int t) {edit_type = t;}
    public int getInternTemp(){return (intern_temp);}
    public void setInternTemp(int t) {intern_temp = t;}
    public int getMyID() {return(my_id);}
    public void setMyID(int num) {my_id = num;}
    public void setContainer(ArchObject cont) {container = cont;}
    public ArchObject getContainer() {return(container);}

    /**
     * Get the topmost container that 'this' arch is in.
     * @return the topmost container
     */
    public ArchObject getTopContainer() {
        ArchObject tmp_arch;

        for (tmp_arch = this; tmp_arch.getContainer() != null;
             tmp_arch = tmp_arch.getContainer());

        return(tmp_arch);
    }

    public void setArtifactFlag(boolean aflag) {
        artifacts_flag = aflag;
    }
    public boolean getArtifactFlag() {
        return(artifacts_flag);
    }

    public void setPrevInv(ArchObject p)
    {
        inv_prev = p;
    }
    public ArchObject getPrevInv()
    {
        return(inv_prev);
    }
    public void setNextInv(ArchObject n)
    {
        inv_next = n;
    }
    public ArchObject getNextInv()
    {
        return(inv_next);
    }

    public ArchObject getStartInv()
    {
        return(inv_start);
    }

    public ArchObject getLastInv()
    {
        return(inv_last);
    }

    /**
     * The given <code>ArchObject</code> 'arch' is placed as inventory
     * into 'this' <code>ArchObject</code>. (Keep in mind that 'arch'
     * has to be a free (unlinked) instance of <code>ArchObject</code>.)
     *
     * @param arch     the <code>ArchObject</code> to be placed in the inventory
     */
    public void addInvObj(ArchObject arch) {
        if(inv_start==null)        // we are the first one
            inv_start = arch;

        arch.inv_prev = inv_last;    // we connect to last one
        if(inv_last != null)
            inv_last.inv_next = arch;
        inv_last = arch;

        // force type change when a MONSTER is put in a spawn point
        if( typelist.getTypeOfArch(this).getTypeNr() == 81 &&
            typelist.getTypeOfArch(arch).getTypeNr() == 80 )
          arch.arch_type = 83; // change to SPAWN_POINT_MOB

        arch.setContainer(this);
        arch.setMapX(getMapX());
        arch.setMapY(getMapY());
    }

    /**
     * Remove 'this' arch from the inv.-list and delete all it's pointers
     */
    public void removeInvObj() {
        inv_start = null; // because we've already deleted the subtree before...
        inv_last = null;

        // unchain
        if(inv_prev != null)
            inv_prev.inv_next = inv_next;
        if(inv_next != null)
            inv_next.inv_prev = inv_prev;
        // if we are start point, reconnect the remaining chain to the container
        if(container.inv_start == this)
            container.inv_start = inv_next;        // ... null
        // if we are last point to the list, reconnect the chain
        if(container.inv_last == this)
            container.inv_last = inv_prev;

        container = null;
        inv_prev = null;
        inv_next = null;
    }

    /**
     *  browse through the inventory tree and count all elements
     *  @return number of objects in the inventory
     */
    public int countInvObjects() {
        int count=0;
        ArchObject arch = inv_start;    // we go to our start

        for(;arch != null;) {
            count++;                    // we have one in... to through his chain
            count += arch.countInvObjects();    // count his inventory
            arch = arch.getNextInv();        // get next of chain
        }
        return(count);
    }
    // ---- GET/SET methods for multi arches
    public int getRefMaxX() {
        if (multi != null)
            return multi.getRefMaxx();
        return 0;
    }
    public int getRefMaxY() {
        if (multi != null)
            return multi.getRefMaxy();
        return 0;
    }
    public int getRefMaxMX() {
        if (multi != null)
            return multi.getRefMaxxm();
        return 0;
    }
    public int getRefMaxMY() {
        if (multi != null)
            return multi.getRefMaxym();
        return 0;
    }

    public void setRefMaxMX(int x) {
        //if (multi == null) multi = new MultiArchData();
        multi.setRefMaxxm(x);
    }
    public void setRefMaxMY(int y) {
        //if (multi == null) multi = new MultiArchData();
        multi.setRefMaxym(y);
    }
    public void setRefMaxX(int x) {
        //if (multi == null) multi = new MultiArchData();

        if(x < 0 && x < multi.getRefMaxxm())
            multi.setRefMaxxm(x);
        else if(x > multi.getRefMaxx())
            multi.setRefMaxx(x);
    }
    public void setRefMaxY(int y) {
        //if (multi == null) multi = new MultiArchData();

        if(y < 0 && y < multi.getRefMaxym())
            multi.setRefMaxym(y);
        if(y > multi.getRefMaxy())
            multi.setRefMaxy(y);
    }

    public void setRefFlag(boolean bool) {
        //if (multi == null) multi = new MultiArchData();
        multi.setRefflag(bool);
    }

    public boolean getRefFlag() {
        if (multi != null)
            return multi.getRefflag();
        return false;
    }

    // this chained multi tiles on map for fast access. better then number and search trash
    public void setMapMultiHead(ArchObject arch) {
        if (multi == null) multi = new MultiArchData();
        multi.setHead(arch); // this points to head. Heads DON'T points to himself
    }
    public void setMapMultiNext(ArchObject arch) {
        if (multi == null) multi = new MultiArchData();
        multi.setNext(arch); // if this is null and head != null = last tile
    }

    public ArchObject getMapMultiHead() {
        if (multi != null)
            return multi.getHead(); // this points to head. Heads points to itsself
        return null;
    }
    public ArchObject getMapMultiNext() {
        if (multi != null)
            return multi.getNext(); // if this is null and head != null = last tile
        return null;
    }

    public int getMultiShapeID() {
        if (multi != null)
            return multi.getMultiShapeID();
        return 0;
    }
    public void setMultiShapeID(int value) {
        if (multi == null) multi = new MultiArchData();
        multi.setMultiShapeID(value);
    }
    public int getMultiPartNr() {
        if (multi != null)
            return multi.getMultiPartNr();
        return 0;
    }
    public void setMultiPartNr(int value) {
        if (multi == null) multi = new MultiArchData();
        multi.setMultiPartNr(value);
    }
    public boolean isLowestPart() {
        if (multi != null)
            return multi.isLowestPart();
        return false;
    }
    public void setLowestPart(boolean state) {
        if (multi == null) multi = new MultiArchData();
        multi.setLowestPart(state);
    }

    // Set Ref. Number of a multi part arch.
    // RefNr == -1 :     single tile
    // RefNr == NodeNr : head (first) tile of a multi tile arch
    // RefNr != NodeNr : part of multi tile arch
    public void setRefNr(int nr) {
        if (multi == null) {
            if (nr != -1) {
                multi = new MultiArchData();
                multi.setRefNr(nr);
            }
        }
        else
            multi.setRefNr(nr);
    }

    // Get Ref. Number of a multi part arch.
    // RefNr == -1 :     single tile
    // RefNr == NodeNr : head (first) tile of a multi tile arch
    // RefNr != NodeNr : part of multi tile arch
    public int getRefNr() {
        if (multi != null)
            return multi.getRefNr();
        return -1; // single tile
    }

    // refx/refy: Offset of this multi tile from head tile
    public void setRefX(int xoff) {
        if (multi == null) multi = new MultiArchData();
        multi.setRefX(xoff);
    }
    public void setRefY(int yoff) {
        if (multi == null) multi = new MultiArchData();
        multi.setRefY(yoff);
    }
    public int getRefX() {
        if (multi != null)
            return multi.getRefX();
        return 0;
    }
    public int getRefY() {
        if (multi != null)
            return multi.getRefY();
        return 0;
    }

    public void setRefCount(int count) {
        if (multi == null) {
            if (count != 0) {
                multi = new MultiArchData();
                multi.setRefCount(count);
            }
        }
        else
            multi.setRefCount(count);
    }

    /**
     * Returns number of parts for multipart heads.
     * (*.getRefCount() > 0) is often used as way to find multi-heads.
     * @return number of parts
     */
    public int getRefCount() {
        if (multi != null)
            return multi.getRefCount();
        return 0;
    }

    public boolean isMD() {
        return (multi != null);
    }

    /**
     * Initialize the multipart data object - must only be called for multipart arches
     */
    public void initMultiData() {
        if (multi == null) multi = new MultiArchData();
    }
    // ----- end multi-arch get/set -----

    public int getArchTypNr() {
        return(arch_type);
    }
    public void  setArchTypNr(int type) {
        arch_type= type;
    }

    public void setMapX(int x) {
        mapx=x;
    }

    public void setMapY(int y) {
        mapy=y;
    }
    public int getMapX() {
        return(mapx);
    }
    public int getMapY() {
        return(mapy);
    }
    public void setNextArch(ArchObject arch) {
        next = arch;
    }
    public void setPrevArch(ArchObject arch) {
        prev = arch;
    }

    public ArchObject getTemp() {
        return(tempptr);
    }
    public void setTemp(ArchObject temp) {
        tempptr = temp;
    }

    public ArchObject getNextArch()
    {
        return(next);
    }
    public ArchObject getPrevArch()
    {
        return(prev);
    }

    public int getDirection()
    {
      return direction;
    }

    public void setDirection(int d)
    {
      direction = d;
    }

    private void setFaceObjectFlag(boolean flag)
    {
      noface = flag;
    }

    public boolean getFaceObjectFlag()
    {
      return noface;
    }

    public int getFaceObjDesc()
    {
      return faceobjdesc;
    }
    /* Set the default face. Number is index of face list */
    public void setRealFaceNr(int nr)
    {
        facenr = nr;
    }
    /* Get the default face. Number is index of face list */
    public int getRealFaceNr()
    {
        return(facenr);
    }

    /* Set the default face. Number is index of face list */
    public void setObjectFaceNr(int nr)
    {
        faceobjnr = nr;
    }
    /* Get the default face. Number is index of face list */
    public int getObjectFaceNr()
    {
        return(faceobjnr);
    }

    /* Set Node number. Node number is the index of the default arch node list */
    public void setNodeNr(int nr)
    {
        nodenr = nr;
    }

    /* Get Node number. Node number is the index of the default arch node list */
    public int getNodeNr()
    {
        return(nodenr);
    }

    // Arch name
    public void setArchName(String name) {
        archName = name;
    }
    public String getArchName() {
        return(archName);
    }

    /**
     * Name which is best appropriate to describe this arch. (This can be
     * arch/object name  or default arch/object name)
     * @param defaultArch  the default arch (if available, faster to use it than look it up again)
     * @return best suitable descriptive name
     */
    public String getBestName(ArchObject defaultArch) {
        if (getObjName() != null && getObjName().length() > 0)
            return getObjName();
        else if (defaultArch != null && defaultArch.getObjName() != null && defaultArch.getObjName().length() > 0)
            return defaultArch.getObjName();
        else if (getArchName() != null && getArchName().length() > 0)
            return getArchName();
        else if (defaultArch != null && defaultArch.getArchName() != null)
            return defaultArch.getArchName();

        return "???"; // this case should actually never happen
    }

    // Obj name
    public void setObjName(String name) {
        objName = name;
    }
    public String getObjName() {
        return(objName);
    }

    // face name
    public void setFaceObjName(String name)
    {
        faceObjName = name;

    }
    public String getFaceObjName()
    {
        return(faceObjName);
    }

    // face name
    public void setFaceRealName(String name)
    {
        faceRealName = name;

    }
    public String getFaceRealName()
    {
        return(faceRealName);
    }

    /**
     * append 'text' to the archText of this arch
     * @param text     text to add
     */
    public void addArchText(String text)
    {
        archText.append(text);
    }

    /**
     * set 'text' = archText of this arch
     * @param text     set this text
     */
    public void setArchText(String text)
    {
        archText.delete(0,archText.length());
        archText.append(text);
    }

    /**
     * returns the archText of this arch as String
     * @return the archtext
     */
    public String getArchText()
    {
        return(archText.toString());
    }

    /**
     * deletes the archText of this arch
     */
    public void resetArchText()
    {
        archText.delete(0,archText.length());
    }

    /**
     * Set The "face <name>" field of a object.
     * We need this for fast access.
     * We set here the string name, the face number
     * and we will handle invalid/double names
     * including comparing with a default arch.
     * @param name
     */
    public boolean setRealFace(String name)
    {
      String face;
      Integer index=null;
      int i, tmp;

      face = getDefaultArch().getFaceRealName();
      i = getRealFaceNr();
      tmp = i;

      /* no name? we always use def arch face */
      if(name == null || name.trim().length() == 0)
      {
        setFaceRealName(null);
        setRealFaceNr(i);
        if(tmp != i)
          return true;
        else
          return false;
      }

      if(face != null) /* compare defarch face with name */
      {
        if (face.compareTo(name) == 0) /* same as def arch */
        {
          setFaceRealName(null);
          setRealFaceNr(i);
          if(tmp != i)
            return true;
          else
            return false;
        }
      }

      /* we must set face to name */
        try {
          index = (Integer)(m_control.getArchObjectStack().getFaceHashTable().get(name));
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {}

        setFaceRealName(name);
        if(index != null)
          setRealFaceNr(index.intValue());
        else
          setRealFaceNr(-1);

        if(tmp != i)
          return true;
        else
          return false;
}
    /**
     * we set here the real face of the objects,
     * depending on the set face and the set animation.
     * The rule is, that a active animation will overrule
     * the default face. We will catch it here.
     */
    public void setObjectFace()
    {
      String a_name = this.animName;
      String name = null;
      faceobjdesc = 1;
      // lets check we have in this object or the default arch of it an animation
      if(a_name == null)
      {
        faceobjdesc = 2;
        a_name = getDefaultArch().animName;
      }

      setFaceObjectFlag(true);
      setdir = false;

      if(a_name != null) // we have a animation - get the frame picture
      {
        setFaceObjectFlag(false);
        animnr = m_control.animationObject.findAnimObject(a_name);
        name = m_control.animationObject.getAnimFrame(animnr,direction);
        if(name == null)
        {
          faceobjdesc = -1;
          animnr = -1;
        }
        else
        {
          // we have a legal animation - enable direction keys in panel if turnable or direction anim
          if(m_control.animationObject.getAnimFacings(animnr) >0)
            setdir = true;
        }
      }
      else // ok, we fallback to the face picture
      {
        animnr = -1;
        name = this.getFaceRealName();
        faceobjdesc = 3;
        if(name == null)
        {
          faceobjdesc = 4;
          name = this.getDefaultArch().getFaceRealName();
        }
      }

      setFaceObjName(name);
      setObjectFaceNr(-1);

      if(name != null)
      {
        setFaceObjectFlag(false);
        Integer index = null;
        try {
          index = (Integer)(m_control.getArchObjectStack().getFaceHashTable().get(name));
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {}

        if(index != null)
          setObjectFaceNr(index.intValue());
        else
        {
          setObjectFaceNr( -1);
          faceobjdesc = -1;
        }
      }
      else
        faceobjdesc = -1;

      //System.out.println("AnimFace: "+name+"("+a_name+") --- " +m_control.animationObject.findAnimObject(a_name)+" ("+getObjectFaceNr()+")("+getFaceObjectFlag()+")");

/*      getDefaultArch()*/


    }

    private String diffTextString(String base, String str, boolean ignore_values)
    {
      int i, j;
      String line="";

      if (!base.endsWith("\n"))
        base = base.concat("\n");  // string should end with '\n'
      if (!line.endsWith("\n"))
        line = line.concat("\n");  // string should end with '\n'

        // Check line by line for comparison
        for (i=0, j=0; i<base.length(); i++) {
          if (base.charAt(i) == '\n') {
            line = base.substring(j, i).trim(); // get one line from base
            if (ignore_values)
            {
              if(str.compareTo(line.substring(0, line.indexOf(" ")+1))==0)
                return line;
            }
            else
            {
              if(str.compareTo(line)==0)
                return line;
            }
            j = i + 1;
          }
        }
        return null;
    }

    /**
    * Get all entries from the given archtext 'atxt' that don't exist
    * in 'this' archtext.
    *
    * @param atxt            archtext to compare 'this'-arch with
    * @param ignore_values   if true: the values in the archtext-entries
    *                                 are ignored at the comparison
    * @return all lines from 'atxt' that don't occur in 'this' arch
    */
   public String diffArchText(String atxt, boolean ignore_values) {
     String line = "";             // tmp string
     String result = "";           // return string
     String test;
     int i, j;
     int pos;
     char c;

     if (!atxt.endsWith("\n"))
       atxt = atxt.concat("\n");  // string should end with '\n', see below

     // Check line by line for comparison
     for (i=0, j=0; i<atxt.length(); i++) {
       if (atxt.charAt(i) == '\n') {
         line = atxt.substring(j, i).trim();  // get one line from 'atxt'

         // if 'line' does NOT exist in 'this' arch, we take it:
         if (ignore_values) {
           // cut away everything after the first space in that line
           // because we want to ignore the "value part":
           /*
             if (line.length() > 0 && line.indexOf(" ")>0 &&
                 this.getArchText().indexOf(line.substring(0, line.indexOf(" ")+1)) == -1)
            */
             if (line.length() > 0 && line.indexOf(" ")>0 &&
                 diffTextString(this.getArchText(),line.substring(0, line.indexOf(" ")+1), ignore_values)==null)
             result = result.concat(line + "\n");
         }
         else {
           // not ignoring the value-part
           // OLD: pos = this.getArchText().indexOf(line); // position of the matching line
           try {

             /* OLD:
              if (pos != -1)
                   c = this.getArchText().charAt(pos+line.length());
               else
                   c = '\0';
              */
               test = diffTextString(this.getArchText(),line, ignore_values);
               c= '\n';
               if(test!=null)
                 c = test.charAt(0);

               // An entry is taken if it doesn't exists in 'this' archtext
               // at all, or it doesn't have the same lenght
               // OLD: if (line.length() > 0 && (pos == -1 || (c != '\n' && c != '\0'))
               if (line.length() > 0 && (test == null || c == '\n'))
                   result = result.concat(line + "\n");
           }
           catch (StringIndexOutOfBoundsException e) {}
         }

         j=i+1;
       }
     }

     return result;
   }

    public void deleteMsgText()
    {
        msgText = null;
    }

    public void resetMsgText()
    {
        if(msgText == null)
            return;
        msgText.delete(0,msgText.length());
    }

    // MSGText!
    public void addMsgText(String text)
    {
        if(msgText == null)
            msgText = new StringBuffer();
        if(text == null)    // special, this adds a clear string
            return;
        msgText.append(text);
    }
    public String getMsgText()
    {
        if(msgText == null)
            return null;
        return(msgText.toString());
    }
    // ANIMText!

    // object can altered by "direction <value> "
    public boolean getHasDir()
    {
      return setdir;
    }

    // Object has an animation if != -1
    private void setAnimNr(int i)
    {
      animnr = i;
    }

    public int getAnimNr()
    {
      return animnr;
    }

    public void setAnimName(String text)
    {
      animName = text;
    }

    public String getAnimName()
    {
      return animName;
    }

    public void addAnimText(String text)
    {
        if(animText == null)
            animText = new StringBuffer("");
        animText.append(text);
    }
    public String getAnimText()
    {
        if(animText == null)
            return null;
        return(animText.toString());
    }

    // lore text
    public String getLoreText() {
        return(loreText.toString());
    }
    public void addLoreText(String text)
    {
        if(loreText == null)
            loreText = new StringBuffer("");
        loreText.append(text);
    }

    // number of string line in text buffer of this arch
    public int getArchTextCount() {
        return(archTextCount);
    }

    // set/get autojoin list
    public void setJoinList(AutojoinList jlist) {join = jlist;}
    public AutojoinList getJoinList() {return join;}

    /**
     * returns a new ArchObject, containing a copy of 'this' arch.
     * References of course cannot be copied that way!
     * This does NOT work for multipart objects.
     * Also note that the returned clone is not linked to any map.
     *
     * @param posx    map x coords. for the returned clone
     * @param posy    map y coords. for the returned clone
     * @return clone instance of this <code>ArchObject</code>
     */
    public ArchObject getClone(int posx, int posy) {
        ArchObject clone = new ArchObject();  // The clone is a new object!

        clone.faceObjName = faceObjName;            // face name
        clone.faceRealName = faceRealName;            // face name
        clone.archName = archName;            // arch Name
        clone.objName = objName;

        // the arch text (contains only the differences from default arch)
        if (archText != null)
            clone.archText = new StringBuffer(archText.toString());
        else
            clone.archText = null;
        clone.archTextCount = archTextCount;  // lines of text in archText

        // msg text buffer
        if (msgText != null)
            clone.msgText = new StringBuffer(msgText.toString());
        else
            clone.msgText = null;

        // anim text buffer
        if (animText != null)
            clone.animText = new StringBuffer(animText.toString());
        else
            clone.animText = null;

        if (multi != null)
            clone.multi = multi.getClone();   // clone multi data

        clone.script = new ScriptArchData(clone);

        clone.direction = direction;
        clone.faceobjdesc = faceobjdesc;
        clone.setdir = setdir;
        clone.animName = animName;
        clone.faceObjName = faceObjName;
        clone.faceRealName = faceRealName;
        clone.animnr = animnr;
        clone.nodenr = nodenr;                // node of the default arch
        clone.faceobjnr = faceobjnr;                // the index of faceImages[]
        clone.facenr = facenr;                // the index of faceImages[]
        clone.editflag = editflag;            // if true, object is in a editor
        clone.edit_type = edit_type;          // bitmask for view-settings

        clone.arch_type = arch_type;          // type attribute of the arch

        // set coords:
        clone.mapx = posx; clone.mapy = posy;

        // If 'this' arch is a container, we have to create clones
        // of the whole inventory (recursively) and link them in:
        if (inv_start != null) {
            for (ArchObject tmp = inv_start; tmp != null; tmp = tmp.inv_next)
                clone.addInvObj(tmp.getClone(posx, posy));
        }

        return clone;
    }

    /**
     * @return: true if 'this' arch is a container (has something
     *          inside), otherwise false
     */
    public boolean isContainer() {
        return (inv_start != null);
    }

    /**
     * @return: true if 'this' arch is part of a multisquare object
     */
    public boolean isMulti() {
        if (multi != null && (multi.getRefflag() || multi.getRefCount()>0))
            return true;
        return false;
    }

	/*
    public void addEvent(ArchObject newscript) {
        script.addEvent(newscript);
    }
*/

    /**
     * Check wether all events have enough data to be valid.
     * Invalid or insufficient ScriptedEvent objects get removed.
     */
    public void validateAllEvents() {
		script.validateAllEvents();
    }

    /**
     * @return true when this arch has one or more scripted events defined
     */
    public boolean isScripted() {
		for (ArchObject tmp = inv_start; tmp != null; tmp = tmp.inv_next)
			if(tmp.arch_type == 118)
				return true;
		return false;
    }

    /**
     * Set contents of JList to all existing scripted events.
     * @param list the JList which displays all events for this arch
     */
    public void addEventsToJList(JList list) {
        script.addEventsToJList(list);
    }

    /**
     * If there is a scripted event of the specified type, the script pad
     * is opened and the appropriate script displayed.
     *
     * @param eventType   type of event
     * @param task        ID number for task (open script/ edit path/ remove)
     * @param eventList   JList from the MapArchPanel (script tab) which displays the events
     * @param mapanel     the MapArchPanel
     */
    public boolean modifyEventScript(int eventType, int task, JList eventList, CMapArchPanel mapanel) {
		boolean changed = script.modifyEventScript(eventType, task, eventList);

		if (script.isEmpty())
			mapanel.setScriptPanelButtonState(true, false, false, false);
		mapanel.updateMapTileList();
		return changed;
	}

    /**
     * A popup is opened and the user can create a new scripting event
     * which gets attached to this arch.
     *
     * @param eventList   JList from the MapArchPanel (script tab) which displays the events
     * @param mapanel     the MapArchPanel
	 * @return true if a script was added, false if the user cancelled
     */
    public boolean addEventScript(JList eventList, CMapArchPanel mapanel) {
		boolean added = script.addEventScript(eventList, this);

        if (!script.isEmpty()) {
            mapanel.setScriptPanelButtonState(true, true, true, true);
            script.addEventsToJList(eventList);
        }
        else {
            mapanel.setScriptPanelButtonState(true, false, false, false);
        }

		return added;
    }

    /**
     * This method checks the archText for syntax errors. More
     * precisely: It reads every line in the archText and looks if
     * it matches the type-definitions (-> see CFArchTypeList) for this
     * arch. If there is no match, the line is considered wrong.
     * Of course the type-definitions will never be perfect, this
     * should be kept in mind.
     * Note that the default arch is ignored in the check. The default
     * arches should be correct, and even if not - it isn't the mapmaker to blame.
     *
     * @param type          the type structure belonging to this arch.
     *                      if null, the type is calculated in this function
     * @return A String with all lines which don't match the type-definitions.<br>
     *         If no such "errors" encountered, null is returned.
     */
    public String getSyntaxErrors(CFArchType type) {
        ArchObject defarch = getDefaultArch();

        if (typelist != null && archText != null && archText.length() > 0) {
            String errors = "";  // return value: all error lines

            // open a reading stream for the archText
            StringReader sread = new StringReader(archText.toString());
            BufferedReader sstream = new BufferedReader(sread);

            try {
                String line = null; // read line
                String attr_key;    // key-part of the attribute in 'line'
                boolean does_match; // true if line matches (/ is correct)

                if (type == null)
                    type = typelist.getTypeOfArch(this); // the type of this arch

                // System.out.println("Applying type: "+type.getTypeName());

                do {
                    line = sstream.readLine(); // read one line

                    if (line != null && line.length() > 0) {
                        line = line.trim();

                        // get only the key-part of the attribute
                        if (line.indexOf(" ") <= 0) {
                            // this line doesn't even have the proper format: "key value"
                            // we assume the missing value part means zero-value
                            attr_key = line;
                        }
                        else
                            attr_key = line.substring(0, line.indexOf(" "));

                        // System.out.print("read attribute '"+line+"' -> ");

                        // now check if there's a match in the definitions
                        does_match = false;
                        /* we exclude "direction" on the hard way */
                        if(attr_key.compareTo("direction")==0)
                          does_match = true;
                        else
                          {
                            for (int t = 0; t < type.getAttr().length && !does_match; t++) {
                              if (type.getAttr()[t].getNameOld().equals(attr_key)) {
                                // found a match:
                                does_match = true;
                              }
                            }
                          }

                        if (!does_match) {
                            errors += line.trim()+"\n"; // append line to the errors
                            /*
                            // the attribute doesn't match the definitions,
                            // now check if it is a negation of an entry in the default arch
                            if (line.indexOf(" ") >= 0) {
                                String attr_val = line.substring(line.indexOf(" ")).trim();
                                if (!(defarch.getAttributeString(attr_key, null).length() > 0 &&
                                     (attr_val.equals("0") || attr_val.equals("null") ||
                                      attr_val.equals("none"))))
                                    errors += line.trim()+"\n";    // append line to the errors
                            }
                            else
                                errors += line.trim()+"\n"; // append line to the errors
                            */
                        }
                    }
                } while (line != null && line.length() > 0);

                // close streams
                sstream.close();
                sread.close();
            }
            catch (IOException e) {
                System.out.println("Error in getSyntaxErrors: Cannot close StringReader");
            }

            // return errors, or null if empty
            if (errors.trim().length() == 0)
                return null;
            return errors;
        }
        else
            return null;
    }

}
