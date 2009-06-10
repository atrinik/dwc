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

import javax.swing.*;
import java.awt.*;

/**
 * <code>CDialogBase</code> is the baseclass for dialogs that center
 * on to their parent or to the screen if no parent is given.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:Michael.Keuchen@hamburg.de">Michael Keuchen</a>
 */
public class CDialogBase extends JDialog {

    public CDialogBase(Frame parentFrame, String title ) {
        super( parentFrame, title );
    }

    /**
     * Centers this dialog when showing.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            Window owner = getOwner();
            Rectangle ownerBounds;
            if (owner != null)
                ownerBounds = owner.getBounds();
            else
                ownerBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Dimension ownSize = getSize();
            Rectangle ownBounds = new Rectangle(ownSize);
            ownBounds.x = ownerBounds.x + (ownerBounds.width - ownSize.width)/2;
            ownBounds.y = ownerBounds.y + (ownerBounds.height - ownSize.height)/2;
            setBounds(ownBounds);
            super.setVisible(true);
        }
        else
            super.setVisible(false);
    }

}
