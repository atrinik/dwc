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
 * A generic interface for undoable/redoable operations.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public interface IUndoable {
    /**
     * Returns whether the operation is undoable.
     *@return True if the operation is undoable, false if not.
     */
    public boolean isUndoable();

    /**
     * Undoes the operation (if that is possible).
     */
    public void undo();

    /**
     * Returns whether the operation is redoable.
     *@return True if the operation is redoable, false if not.
     */
    public boolean isRedoable();

    /**
     * Reapplies the operation (if that is possible).
     */
    public void redo();

    /**
     * Returns the operations short (about 8 chars max) name. The name is used
     * to show the operation in the menuitems and toolbar buttons tooltips.
     *@return The name of the operation.
     */
    public String getName();
}
