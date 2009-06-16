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
import java.awt.*;
import javax.swing.border.*;

/**
 * <code>CMainStatusbar</code> implements the main statusbar of the
 * application. Used to show one line text messages to the user about
 * progress, state etc. Also includes level info and memory info
 * panels.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CMainStatusbar extends JPanel {
    /** The most recent instance of this statusbar class. */
    private static CMainStatusbar mStatic_statusbar = null;

    /** Controller of this statusbar view. */
    private CMainControl m_control;
    /** The label that shows the one line text message. */
    private JLabel m_mainStatusLabel;
    /** The label that shows the level status. */
    private JLabel m_levelStatus;
    /** The label that shows the memory status. */
    private JLabel m_memStatus;

    /**
     * Constructs a statusbar that has the given main controller object set
     * as its controller.
     *@param control The controller of this view.
     */
    CMainStatusbar( CMainControl control ) throws CGridderException {
        m_control = control;

        setLayout( new BorderLayout() );

        JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        m_mainStatusLabel = new JLabel("                                  ");
        mainPanel.add( m_mainStatusLabel, BorderLayout.CENTER );

        add( mainPanel, BorderLayout.CENTER );

        JPanel infoPanel = new JPanel( new BorderLayout() );

        JPanel levelInfoPanel = new JPanel( new BorderLayout() );
        levelInfoPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        m_levelStatus = new JLabel("           ");
        levelInfoPanel.add( m_levelStatus, BorderLayout.CENTER );
        infoPanel.add( levelInfoPanel, BorderLayout.CENTER );

        JPanel memInfoPanel = new JPanel( new BorderLayout() );
        memInfoPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        m_memStatus = new JLabel("           ");
        memInfoPanel.add( m_memStatus, BorderLayout.CENTER );
        infoPanel.add( memInfoPanel, BorderLayout.WEST );

        mainPanel.add( infoPanel, BorderLayout.EAST );

        mStatic_statusbar = this;
        updateFont();
    }

    /**
     * Returns the instance of this statusbar once its created by someone.
     *@return The statusbar.
     */
    static CMainStatusbar getInstance() {
        return mStatic_statusbar;
    }

    /**
     * Sets the one line text displayed to the user.
     *@param strText The text to be set.
     */
    void setText( String strText ) {
        m_mainStatusLabel.setText( strText );
        repaint();
    }

    void setStatusText(String string ) {
        if(string == null) {
            m_levelStatus.setText(" ");
        } else {
            m_levelStatus.setText(string);
        }
    }

    /**
     * redraw status bar with latest custom fonts
     */
    public void updateFont() {
        m_control.setBoldFont(m_mainStatusLabel);
        m_control.setBoldFont(m_memStatus);
        refresh();
    }

    /**
     * Sets the level info panels text based on the information from the level
     * controller.
     *@param level The level whose info is to be displayed.
     */
    /*
      void setLevelInfo( CLevelControl level )
      {
      if ( level != null )
      {
      m_levelStatus.setText(
      "Level: "+level.getLevelWidth()+"x"+level.getLevelHeight()+
      " Tile: "+level.getTileWidth()+"x"+level.getTileHeight()+
      " Tile Data: "+level.getBitsPerTile()+
      " / "+level.getDataBits()+" bits"
      );
      }
      else
      {
      m_levelStatus.setText(
      "Level: -x- "+
      " Tile: -x- "+
      " Tile Data: - "+
      "/ - bits"
      );
      }
      repaint();
      }
    */

    /**
     * Refreshes the memory usage info panel.
     */
    void refresh() {
        Runtime runtime = Runtime.getRuntime();
        long freeMem = runtime.freeMemory();
        long totMem  = runtime.totalMemory();
        long usedMem = totMem-freeMem;

        m_memStatus.setText( " ( "+ m_control.getArchObjectStack().archObjCount
                             +"/"+m_control.getArchObjectStack().getArchCount()
                             +"/"+m_control.getArchObjectStack().getFaceCount()
                             +" ) Memory Use:"+
                             getMemoryString(usedMem)+"/"+getMemoryString( totMem ) );
        repaint();
    }

    /**
     * Returns the given memory amount (long) as a string scales the value
     * to be bytes, kilobytes or megabytes.
     */
    private String getMemoryString( long memory ) {
        String memUnit = "b";

        if ( memory > 1024 ) {
            memory /= 1024;
            memUnit = "kb";
        }

        if ( memory > 1024 ) {
            memory /= 1024;
            memUnit = "Mb";
        }

        return ""+memory+memUnit;
    }

    /**
     * Notifies that the application is about to exit.
     */
    void appExitNotify() {
    }
}


