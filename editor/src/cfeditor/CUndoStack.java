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

/**
 * The undo/redo stack that is used when undoing/redoing operations.
 * Every operation should add itself to the stack using the add() method.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class CUndoStack {
    /** The controller of the undo stack model. */
    private static CMainControl mStatic_control;
    /** The shared hashtable that maps levels to undo stacks. */
    private static Hashtable m_hashFromLevelToStack = new Hashtable();

    /** The undo stack. Contains all undoable operations. */
    private Vector m_undoStack = new Vector(10, 10);
    /** The redo stack. Contains all redoable operations. */
    private Vector m_redoStack = new Vector(10, 10);
    /** The maximum size of the stacks. */
    private int m_maxStackSize = 10;

    /**
     * Returns the undo stack for the given level controller.
     *@param level The level whose undo/redo stack is to be returned.
     *@return The undo/redo stack for the given level.
     */

    public static CUndoStack getInstance( CMapControl level ) {
        if ( m_hashFromLevelToStack.containsKey( level ) ) {
            return (CUndoStack) m_hashFromLevelToStack.get( level );
        }

        CUndoStack instance = new CUndoStack();
        m_hashFromLevelToStack.put( level, instance );
        return instance;
    }

    /**
     * Sets the main controller of all undo/redo stack models.
     *@param control The controller of all undo/redo stacks.
     */
    public static void setMainControl( CMainControl control ) {
        mStatic_control = control;
    }

    /**
     * Constructs an undo/redo stack.
     */
    private CUndoStack() {
    }

    /**
     * Returns the maximum stack size.
     *@return The maximum stack size.
     */
    int getMaxStackSize() {
        return m_maxStackSize;
    }

    /**
     * Sets the maximum stack size.
     *@param size The new maximum stack size.
     */
    void setMaxStackSize( int size ) {
        m_maxStackSize = size;
    }

    /**
     * Adds a new undoable/redoable operation to the undo/redo stack.
     *@param undoOp The new operation to be added to the stack.
     */
    public void add( IUndoable undoOp ) {
        m_undoStack.add( undoOp );
        m_redoStack.removeAllElements();
        if (m_undoStack.size() > m_maxStackSize ) {
            m_undoStack.removeElementAt( 0 );
        }

        if ( mStatic_control != null ) {
            mStatic_control.refreshMenusAndToolbars();
        }
    }

    /**
     * Returns whether the last operation in the undo stack can be undone.
     *@return True if the last operation in the undo stack can be undone, false
     *        if not.
     */
    public boolean canUndo() {
        if (m_undoStack.size() > 0) {
            IUndoable op = (IUndoable) m_undoStack.lastElement();
            return op.isUndoable();
        }

        return false;
    }

    /**
     * Returns the name of the last operation in the undo stack.
     *@return The name of the last operation in the undo stack.
     */
    public String getUndoName() {
        if ( canUndo() ) {
            IUndoable op = (IUndoable) m_undoStack.lastElement();
            return op.getName();
        }

        return "";
    }

    /**
     * Undoes the last operation in the undo stack.
     */
    public void undo() {
        if ( canUndo() ) {
            IUndoable op = (IUndoable) m_undoStack.lastElement();
            m_undoStack.removeElement( op );
            op.undo();
            m_redoStack.addElement( op );

            if ( mStatic_control != null ) {
                mStatic_control.refreshMenusAndToolbars();
            }
        }
    }

    /**
     * Returns whether the last operation in the redo stack can be redone.
     *@return True if the last operation in the redo stack can be redone, false
     *        if not.
     */
    public boolean canRedo() {
        if (m_redoStack.size() > 0) {
            IUndoable op = (IUndoable) m_redoStack.lastElement();
            return op.isRedoable();
        }

        return false;
    }

    /**
     * Returns the name of the last operation in the redo stack.
     *@return The name of the last operation in the redo stack.
     */
    public String getRedoName() {
        if ( canRedo() ) {
            IUndoable op = (IUndoable) m_redoStack.lastElement();
            return op.getName();
        }

        return "";
    }

    /**
     * Redoes the last operation in the redo stack.
     */
    public void redo() {
        if ( canRedo() ) {
            IUndoable op = (IUndoable) m_redoStack.lastElement();
            m_redoStack.removeElement( op );
            op.redo();
            m_undoStack.addElement( op );

            if ( mStatic_control != null ) {
                mStatic_control.refreshMenusAndToolbars();
            }
        }
    }
}
