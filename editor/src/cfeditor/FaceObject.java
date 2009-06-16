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

/**
 * The <code>FaceObject</code>
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class FaceObject {
    private ImageIcon face;
    private String name = new String("");               // name of face
    private String path;
    private int index;

    public FaceObject() {
        face = null;
        index = -1;
    };

    public ImageIcon getFace() {
        return(face);
    }

    /*  public void setFace(Image im)
        {
        face = new ImageIcon();
        face.setImage(im);
        im=null;
        }
    */
    public void setFace(ImageIcon im) {
        face = im;
    }

    public String getPath() {
        return(path);
    }

    public void setPath(String text) {
        path = text;
    }

    public String getName() {
        return(name);
    }

    public void setName(String text) {
        name = new String(text);
    }

    public int getIndex() {
        return(index);
    }

    public void setIndex(int i) {
        index = i;
    }
};

