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

/**
 * Subclass of ArchObject to store multipart information.
 * This data is only needed by multiparts. When the editor is running,
 * usually a big number of ArchObjects exist - most of them single-tile
 * objects. The encapsulation of this "multpart-only" data can save
 * a little bit of memory.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class MultiArchData {
    private boolean refflag;             // true: this arch is a part of the tail - NOT the head

    private int refnr;                   // if != -1 - multi tile
                                         // if refnr == nodenr
                                         // then first tile
                                         // else refnr==first tile
    private int refx;                    // multi tile: offset pos from head
    private int refy;                    // sic!
    private int refcount;                // head: number of parts (>0 means it's ahead)
    private int refmaxx;                 // head: parts in x
    private int refmaxy;                 // head: parts in y
    private int refmaxxm;                // head: parts in x to count minus ref
    private int refmaxym;                // head: parts in y sic
    private int multiShapeID;            // the ID of the multiPositionData (only for multiparts in isoView!)
    private int multiPartNr;             // the part number for this tile (only for multiparts in isoView!)
    private boolean isLowestPart;        // lowest part of all multi tiles (only for multiparts in isoView!)

    private ArchObject head;             // multi tile, this is the head
    private ArchObject nextref;          // next multi tile of this arch in map

    /**
     * Constructor: Initializing the data
     */
    public MultiArchData() {
        refmaxx = refmaxy = 0;
        refnr = -1;
        refflag = false;
        refcount = 0;
        head = nextref = null;
        isLowestPart = false;
        refx=0;
        refy=0;
    }

    /**
     * @returns an identical copy of this MultiArchData object.
     * The links are not copied though! A cloned multipart needs to
     * be re-linked properly before it can be used!
     */
    public MultiArchData getClone() {
        MultiArchData clone = new MultiArchData();

        clone.refflag = refflag;              // true: this arch is a multi tile part
                                              // and NOT the head
        clone.refnr = refnr;                  // if != -1 - multi tile; if refnr == nodenr then first tile
                                              // else refnr==first tile; multi tile: offset pos from head
        clone.refy = refy;                    // sic!
        clone.refcount = refcount;            // head: number of parts
        clone.refmaxx = refmaxx;              // head: parts in x
        clone.refmaxy = refmaxy;              // head: parts in y
        clone.refmaxxm = refmaxxm;            // head: parts in x to count minus ref
        clone.refmaxym = refmaxym;            // head: parts in y sic
        clone.multiShapeID = multiShapeID;    // ID for the multiPositionData
        clone.multiPartNr = multiPartNr;      // part number for the multiPositionData
        clone.isLowestPart = isLowestPart;    // lowest part of all multi tiles

        return clone;
    }

    // --- GET/SET methods ---
    public boolean getRefflag() {return refflag;}
    public void setRefflag(boolean state) {refflag = state;}
    public boolean isLowestPart() {return isLowestPart;}
    public void setLowestPart(boolean state) {isLowestPart = state;}

    public int getRefNr() {return refnr;}
    public void setRefNr(int value) {refnr = value;}
    public int getRefX() {return refx;}
    public void setRefX(int value) {refx = value;}
    public int getRefY() {return refy;}
    public void setRefY(int value) {refy = value;}
    public int getRefCount() {return refcount;}
    public void setRefCount(int value) {refcount = value;}
    public int getRefMaxx() {return refmaxx;}
    public void setRefMaxx(int value) {refmaxx = value;}
    public int getRefMaxxm() {return refmaxxm;}
    public void setRefMaxxm(int value) {refmaxxm = value;}
    public int getRefMaxy() {return refmaxy;}
    public void setRefMaxy(int value) {refmaxy = value;}
    public int getRefMaxym() {return refmaxym;}
    public void setRefMaxym(int value) {refmaxym = value;}
    public int getMultiShapeID() {return multiShapeID;}
    public void setMultiShapeID(int value) {multiShapeID = value;}
    public int getMultiPartNr() {return multiPartNr;}
    public void setMultiPartNr(int value) {multiPartNr = value;}

    public ArchObject getHead() {return head;}
    public void setHead(ArchObject node) {head = node;}
    public ArchObject getNext() {return nextref;}
    public void setNext(ArchObject node) {nextref = node;}
}
