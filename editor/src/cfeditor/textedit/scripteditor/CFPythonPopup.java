/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keranen)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
package cfeditor.textedit.scripteditor;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.BadLocationException;
import javax.swing.plaf.basic.BasicComboPopup;
import java.util.Vector;
import java.io.*;

import cfeditor.CFileReader;
import cfeditor.IGUIConstants;
import cfeditor.CMainControl;
import cfeditor.CSettings;

/**
 * This class implements a popup window which shows all python
 * methods in the 'CFPython' package.
 *
 * As JPopupMenus are not scrollable, the implementation of the
 * combo box popup menu (standard UI) is used here. This is not the
 * perfect approach as it imposes some unwanted limitations.
 * However, the "perfect approach" would require full coding of a
 * JWindow rendered as a popup menu - which is an extremely
 * time consuming task.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFPythonPopup extends JComboBox {
    private static ScriptEditControl control; // control object

    // list of menu entries (all CFPython commands)
    private static String[] menuEntries = null;

    private CFPythonPopupMenu menu;

    private boolean isReady = false; // true when this menu has been fully initialized
    private int caretPos;            // caret position in document where this popup was opened

    /**
     * Constructor - builds the CFPython popup menu
     */
    public CFPythonPopup() {
        super();
        setBackground(java.awt.Color.white); // white background
        //System.out.println("Initializing CFPythonPopup...");

        control = ScriptEditControl.getInstance();

        // make sure the commandlist is initialized
        if (menuEntries == null) loadCommandlist();

        menu = new CFPythonPopupMenu(this);

        if (menuEntries != null) {
            for (int i=0; i<menuEntries.length; i++) {
                addItem(" "+menuEntries[i]);
            }
        }

        // listener for selection events
        addActionListener(new MenuActionListener(this));

        if (menuEntries != null && menuEntries.length > 0)
            isReady = true; // this menu is now ready for use

        setRequestFocusEnabled(true);
    }

    /**
     * Load the list of CFPython commands from the datafile
     */
    public static void loadCommandlist() {
        CFileReader reader = null; // file reader
        Vector cmdList = new Vector(); // temporare list to store commands
        int k;

        try {
            String isoArchDefFolder = "";
            if (IGUIConstants.isoView) {
                if (!control.isStandAlone())
                    isoArchDefFolder = control.getMainControl().getArchDefaultFolder();
                else
                    isoArchDefFolder = CSettings.getInstance(IGUIConstants.APP_NAME).getProperty(CMainControl.ARCH_DIR_KEY, CMainControl.DEFAULT_ARCH_DIR);
            }

            String baseDir = (IGUIConstants.isoView ? isoArchDefFolder+File.separator+IGUIConstants.CONFIG_DIR : IGUIConstants.CONFIG_DIR);
            reader = new CFileReader(baseDir, IGUIConstants.PYTHONMENU_FILE);
            String line;  // tmp string for reading lines

            // read file into the cmdList vector:
            line = reader.getReader().readLine(); // read first line
            while (line != null) {
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#")) {
                    // ATM, the descriptive info about method headers is cut out
                    // (TODO: parse and show the full info in a statusbar)
                    if ((k = line.indexOf("(")) > 0)
                        line = line.substring(0, k) + "()";
                    else {
                        System.out.println("Parse error in "+IGUIConstants.PYTHONMENU_FILE+":");
                        System.out.println("   \""+line+"\" missing '()'");
                        line += "()"; // that line is probably garbage, but will work
                    }
                    cmdList.addElement(line);
                }

                // read next line
                line = reader.getReader().readLine();
            }
            sortVector(cmdList);

            // now create the 'menuEntries' array
            if (cmdList.size() > 0) {
                menuEntries = new String[cmdList.size()]; // set array dimensio

                for (int i=0; i < cmdList.size(); i++) {
                    menuEntries[i] = (String)(cmdList.elementAt(i));
                }
                cmdList = null;
            }

            // close file reader
            reader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File '"+IGUIConstants.PYTHONMENU_FILE+"' not found.");
            return;
        }
        catch (EOFException e) {
            // end of file/spell struct reached
            reader.close();
        }
        catch (IOException e) {
            System.out.println("Cannot read file '"+IGUIConstants.PYTHONMENU_FILE+"'!");
            return;
        }
    }

    /**
     * Sort a string vector in alphabetical order. The used sorting strategy
     * is bubblesort because the expected number of objects is low (~300).
     *
     * @param v     Vector to be sorted
     */
    private static void sortVector(Vector v) {
        int i; int j;
        Object node = null;

        for (j=0; j<v.size()+1; j++) {
            for (i=0; i<v.size()-1; i++) {
                if ( ((String)v.elementAt(i)).compareToIgnoreCase(
                     (String)v.elementAt(i+1)) > 0) {
                    node = v.elementAt(i); v.setElementAt(v.elementAt(i+1), i);
                    v.setElementAt(node, i+1);
                }
            }
        }
    }

    /**
     * @return true when this popup menu has been fully initialized and is ready for use
     */
    public boolean isInitialized() {
        return isReady;
    }

    /**
     * Set the caret position where this menu has been invoked
     * @param pos   caret position in the document
     */
    public void setCaretPosition(int pos) {
        this.caretPos = pos;
        this.getMenu().requestFocus();
        control.registerActivePopup(this);
    }

    public CFPythonPopupMenu getMenu() {
        return menu;
    }

    // ------------------------ SUBCLASSES -----------------------

    /**
     * Subclass MenuActionListener handles the actionevents for the menu items.
     */
    private class MenuActionListener implements ActionListener {
        private CFPythonPopup popup;
        private boolean ignore; // while true, all ActionEvents get ignored

        public MenuActionListener (CFPythonPopup popup) {
            this.popup = popup;
            ignore = false;
        }

        public void actionPerformed(ActionEvent e) {
            if (!ignore) {
                // get method name to insert
                String method = popup.getSelectedItem().toString();
                method = method.substring(0, method.indexOf("(")).trim() + "()";

                try {
                    // insert method into the document
                    control.getActiveTextArea().getDocument().insertString(caretPos, method, null);
                }
                catch (BadLocationException ex) {
                    System.out.println("BadLocationException");
                }

                ignore = true;
                popup.setSelectedIndex(0);
                ignore = false;
                popup.getMenu().setVisible(false); // in some JRE versions, this doesn't happen automatically
            }
        }
    }

    /**
     * Menu class, inhertits from JPopupMenu
     */
    public class CFPythonPopupMenu extends BasicComboPopup {
        JComboBox comboBox;

        public CFPythonPopupMenu(JComboBox box) {
            super(box);
            this.comboBox = box;
            //this.addMouseListener(new ListenerMenuClick());
            this.setPreferredSize(new Dimension(220, 200));
        }
    }

}
