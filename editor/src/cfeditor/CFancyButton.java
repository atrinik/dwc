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
import java.awt.event.*;

/**
 * <code>CFancyButton</code> implements fancy button that has a cool rollover
 * effect enabled under Windows l'n'f.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 */
public class CFancyButton extends JButton {
    ImageIcon m_normalIcon;
    ImageIcon m_rolloverIcon;
    boolean m_fMouseOver = false;
    boolean m_fWindowsLNF = false;

    static MouseListener mStatic_rolloverListener = new MouseListener() {
            public void mouseClicked(MouseEvent event) {
            }

            public void mousePressed(MouseEvent event) {
            }

            public void mouseReleased(MouseEvent event) {
            }

            public void mouseEntered(MouseEvent event) {
                CFancyButton button = (CFancyButton) event.getSource();
                button.m_fMouseOver = true;

                if ( button.m_rolloverIcon != null ) {
                    button.setIcon( button.m_rolloverIcon );
                }

                if ( button.m_fWindowsLNF ) {
                    button.setBorderPainted( button.isEnabled() );
                }
            }

            public void mouseExited(MouseEvent event) {
                CFancyButton button = (CFancyButton) event.getSource();

                button.m_fMouseOver = false;

                if ( button.m_normalIcon != null ) {
                    button.setIcon( button.m_normalIcon );
                }

                if ( button.m_fWindowsLNF ) {
                    button.setBorderPainted( false );
                }
            }
        };

    public CFancyButton( String strLabel, ActionListener actionListener ) {
        this( strLabel, null, null, actionListener );
    }

    public CFancyButton(
                        String strLabel,
                        String strToolTip,
                        ActionListener actionListener) {
        this( strLabel, strToolTip, null, actionListener );
    }

    public CFancyButton(
                        String strLabel,
                        String strToolTip,
                        String strIcon,
                        ActionListener actionListener) {
        super( strLabel );
        if ( strToolTip != null ) {
            setToolTipText( strToolTip );
        }

        if ( strIcon != null ) {
            m_rolloverIcon = CGUIUtils.getIcon( strIcon );
            setFancyIcon( m_rolloverIcon );
        }

        if ( actionListener != null )
            {
                addActionListener( actionListener );
            } else {
                setEnabled( false );
            }

        Insets insets = getInsets();
        insets.top    = 2;
        insets.left   = insets.top;
        insets.right  = insets.top;
        insets.bottom = insets.top;
        this.setMargin( insets );

        m_fWindowsLNF =
            ( UIManager.getLookAndFeel().getID().compareToIgnoreCase("Windows") == 0 );

        addMouseListener( mStatic_rolloverListener );

        if ( m_fWindowsLNF ) {
            setBorderPainted( m_fMouseOver );
        }
    }

    /**
     * Sets the fancy icon (automatically calculates the grayscaled normal icon )
     *@icon The icon to be used as the rollover icon.
     */
    public void setFancyIcon( ImageIcon icon ) {
        m_rolloverIcon = icon;

        if ( m_rolloverIcon != null ) {
            m_normalIcon = CGUIUtils.getGrayScaled( m_rolloverIcon );
        } else {
            m_normalIcon = null;
        }

        super.setIcon( m_normalIcon );
    }

    /**
     * Preserve rollover icons over UI changes.
     */
    public void updateUI() {
        super.updateUI();

        m_fWindowsLNF = ( UIManager.getLookAndFeel().getID().compareToIgnoreCase("Windows") == 0 );

        if ( m_fWindowsLNF ) {
            setBorderPainted( m_fMouseOver );
        } else {
            setBorderPainted( true );
        }

        if ( m_fMouseOver ) {
            setIcon( m_rolloverIcon );
        } else {
            setIcon( m_normalIcon );
        }
    }

    public void setEnabled( boolean fEnabled ) {
        super.setEnabled( fEnabled );
        setRolloverIcon( m_rolloverIcon );
        setIcon( m_normalIcon );
    }
}


