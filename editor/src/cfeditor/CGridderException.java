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
 * <code>CGridderException</code> class that is used to transfer error messages
 * and other error information inside the Gridder application.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class CGridderException extends Exception {
    /** The originator of the error. */
    private Object m_originator;

    public CGridderException(String strMessage) {
        super(strMessage);
    }

    public CGridderException(String strMessage, Object originator) {
        super(strMessage);
        m_originator = originator;
    }

    public String getOriginator() {
        if (m_originator != null) {
            return m_originator.getClass().getName();
        }

        return "unknown";
    }
}
