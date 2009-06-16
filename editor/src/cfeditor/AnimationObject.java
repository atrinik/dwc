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

import java.util.*;

/*
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.io.IOException;
*/

/**
 * The <code>AnimationObject</code>
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class AnimationObject {
    private AnimObjectNode[] animObjects = new AnimObjectNode[10000];
    private String[] nameTable = new String[10000];
    // The hash tables hold the name and the index for the field
    // if one can show me, that storing and accessing names AND objects
    // in the table is faster than in the static arrays, we can change this
    static Hashtable animHashTable = new Hashtable();

    private int animObjCount;
    private CMainControl m_control;


    public AnimationObject(CMainControl control) {
        m_control = control;
        animObjCount=0;

    };

    public void addAnimObject(String name, String list) {
      int frames=0, ind,count,facings=0,face_count = 0, len = list.length();
        animObjects[animObjCount] = new AnimObjectNode();

        animObjects[animObjCount].frames = new String[33];
        animObjects[animObjCount].animName = new String(name);
        animObjects[animObjCount].animList = new String(list);
        nameTable[animObjCount] = new String(name);
        animHashTable.put(name, new Integer(animObjCount)); // put it in list

        for(int i=0, s=0;i<len;i++) {
          if(list.charAt(i) == 0x0a) {
            face_count++;
            if(i-s >0) {
              if(list.regionMatches(s,"facings ", 0, 8)) {
                facings = Integer.parseInt(list.substring(s+8,i));
              }
              s=i+1;
            }
          }
        }
        animObjects[animObjCount].facings = facings;
        animObjects[animObjCount].faces = face_count;
//        System.out.println("ANIM: "+name+" --> F:"+facings+" --> C:"+face_count);

        // pre assign frame faces
        ind = 0;
        /* facings 0 means our animation is not an animation
         * but a "picture list" which will handled internal
         * by the server. For example the stages of a button.
         * a button is always in "direction 0" because a round
         * plate has no direction. So, no need to use a facings 9
         * animation here.
        */
        if(facings >0) /* "real animation", sort in the first picture of each frame */
        {
          frames = (face_count - 1) / facings;
          count = 2;
        }
        else /* can be single frame anim OR turnable object */
        {
          count = 1;
          frames = 1;
          if(face_count > 9)
            face_count = 9;
        }

          for(int i=0, s=0;i<len;i++) {
            if(list.charAt(i) == 0x0a) {
              if(i-s >0) {
                if(--count==0)
                {
                   animObjects[animObjCount].frames[ind] = list.substring(s, i);
//                  System.out.println("frame "+ind+" >"+animObjects[animObjCount].frames[ind]+"<");
                  if(facings == 0)
                  {
                    if(--face_count<=0)
                      break;
                  }
                  count = frames;
                   ind++;
                 }
                 s=i+1;
               }
             }
           }
        animObjCount++;
    }

    public int getAnimFacings(int i)
    {
      return animObjects[i].facings;
    }

    public String getAnimFrame(int i, int frame) {
      if(i<0)
        return null;
      return animObjects[i].frames[frame];
    }

    public String[] nameStringTable() {
        return(nameTable);
    }

    public String getNameString(int i) {
        return(nameTable[i]);
    }

    public int getAnimObjectCounter() {
        return(animObjCount);
    }

    public String getAnimObjectList(int i) {
        return(animObjects[i].animList);
    }

    public String getAnimObjectName(int i) {
        return(animObjects[i].animName);
    }

    public int findAnimObject(String name){
      Integer num = null;
      try {
        num = (Integer) animHashTable.get(name);
      } catch (NumberFormatException e) {
      } catch (NullPointerException e) {}

        if(num == null || name == null)
          return -1;
        return(num.intValue());
    }
}; // End of class

class AnimObjectNode {
    String animName;
    String animList;    // A List of "\n" connected strings
    String[] frames;
    int facings;
    int faces;

    public AnimObjectNode () {
        animName=null;
        animList=null;
    }
}; // End of class

