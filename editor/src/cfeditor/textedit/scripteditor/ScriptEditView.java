/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 *
 * (code based on: Gridder. 2D grid based level editor. (C) 2000  Pasi Keränen)
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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.util.Vector;
import java.io.*;

import cfeditor.textedit.textarea.*;
import cfeditor.CGUIUtils;
import cfeditor.IGUIConstants;
import cfeditor.CSettings;

/**
 * The Python script editor frame. This class should only exist
 * in ScriptEditControl. No other class should refer to it.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ScriptEditView extends JFrame {
    /** key used to store the main windows X-coordinate to settings-file. */
    private static final String WINDOW_X      = "ScriptPadWindow.x";
    /** key used to store the main windows Y-coordinate to settings-file. */
    private static final String WINDOW_Y      = "ScriptPadWindow.y";
    /** key used to store the main windows width to settings-file. */
    private static final String WINDOW_WIDTH  = "ScriptPadWindow.width";
    /** key used to store the main windows height to settings-file. */
    private static final String WINDOW_HEIGHT = "ScriptPadWindow.height";

    private ScriptEditControl control;
    private ScriptEditMenuBar menuBar; // the menu bar
    private JTabbedPane tabPane;       // tab pane
    private Vector textAreas;          // list of 'JEditTextArea' objects, same order as tabs

    /**
     * Build frame but keep it hidden (it is shown when first file is opened)
     */
    public ScriptEditView(ScriptEditControl control) {
        super("Script Pad"); // window title
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.control = control;
        textAreas = new Vector();
        menuBar = new ScriptEditMenuBar(control);   // build menu bar
        setJMenuBar(menuBar);                       // add menu bar to frame

        tabPane = new JTabbedPane(JTabbedPane.TOP); // init tab pane
        tabPane.addChangeListener(new EditTabListener(control, this));

        // set the window icon
        ImageIcon icon = CGUIUtils.getIcon("Script.gif");
        if (icon != null) {
            setIconImage( icon.getImage() );
        }

        getContentPane().add(tabPane);
        addWindowListener(new EditWindowListener(control)); // add listener for close box

        // calculate some default values in case there is no settings file
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int defwidth = (int)(0.6*screen.getWidth());
        int defheight = (int)(0.8*screen.getHeight());

        // get the old location and size
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        int x = Integer.parseInt( settings.getProperty(WINDOW_X, ""+(int)((screen.getWidth()-defwidth)/2.)));
        int y = Integer.parseInt( settings.getProperty(WINDOW_Y, ""+(int)((screen.getHeight()-defheight)/2.)));
        int width = Integer.parseInt( settings.getProperty(WINDOW_WIDTH, ""+defwidth));
        int height = Integer.parseInt( settings.getProperty(WINDOW_HEIGHT, ""+defheight));

        this.setBounds(x, y, width, height);
    }

    /**
     * Add a new TextArea Panel to the TabbedPane.
     * @param title    title of this script (filename)
     * @param f        file where this script is stored, null if new script opened
     */
    public void addTab(String title, File f) {
        JEditTextArea ta = new JEditTextArea(); // open new TextArea
        //ta.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
        ta.setDocument(new SyntaxDocument());
        ta.getDocument().setTokenMarker(new PythonTokenMarker());
        boolean isFirstTimeShowing = false; // true when frame was hidden and now is showing

        tabPane.addTab(title, ta);
        if (getTabCount() <= 1 || !isShowing()) {
            this.setVisible(true);
            isFirstTimeShowing = true;
        }

        tabPane.setSelectedIndex(getTabCount()-1);

        // very important: There must be a drawing update after showing the frame, to make
        // sure the graphics context is fully initialized before calling 'setEditingFocus()'
        //if (isFirstTimeShowing)
        this.update(this.getGraphics());

        textAreas.addElement(ta);

        if (f != null && f.exists()) {
            // print file into this document
            try {
                // open ascii streams
                FileReader fread = new FileReader(f);
                BufferedReader bfread = new BufferedReader(fread);

                boolean firstLine = true;
                String line = bfread.readLine();
                StringBuffer buff = new StringBuffer("");
                while (line != null) {
                    if (!firstLine)
                        buff.append("\n");
                    else
                        firstLine = false;
                    buff.append(line);
                    line = bfread.readLine();

                }

                // close filestreams
                bfread.close();
                fread.close();

                // insert buffer into the document
                ta.getDocument().insertString(0, buff.toString(), null);
            }
            catch (FileNotFoundException e) {
                System.out.println("ScriptEditView.addTab(): File '"+f.getName()+"' not found.");
            }
            catch (IOException e) {
                System.out.println("ScriptEditView.addTab(): I/O-Error while reading '"+f.getName()+"'.");
            }
            catch (javax.swing.text.BadLocationException e) {
                System.out.println("ScriptEditView.addTab(): Bad Location in Document!");
            }
        }

        ta.setEditingFocus(); // set focus to TextArea in order to respond to keypress-events
        //ta.scrollToCaret();   // make sure the window shows caret (top left corner)

        toFront(); // bring window to front
        refreshMenuBar();
    }

    /**
     * Close the active script-tab
     */
    public void closeActiveTab() {
        if (textAreas.size() > 0) {
            // remove textArea
            textAreas.removeElementAt(tabPane.getSelectedIndex());
            tabPane.remove(tabPane.getSelectedIndex());
        }
        else
            setVisible(false);
    }

    /**
     * @return the currently active TextArea (in front)
     */
    public JEditTextArea getActiveTextArea() {
        if (getTabCount() > 0) {
            return ((JEditTextArea)(textAreas.elementAt(tabPane.getSelectedIndex())));
        }

        return null; // no window is open
    }

    /**
     * @return index of selected tab pane
     */
    public int getSelectedIndex() {
        return tabPane.getSelectedIndex();
    }

    /**
     * @return Number of open tabs.
     */
    public int getTabCount() {
        return tabPane.getTabCount();
    }

    /**
     * Set the title of the tab at specified index
     * @param index    index of the tab to change title
     * @param title    new title string
     */
    public void setTitleAt(int index, String title) {
        tabPane.setTitleAt(index, title);
    }

    /**
     * refresh the menu bar (update enable/disable state of all menus)
     */
    public void refreshMenuBar() {
        menuBar.updateFont(true);
    }

    /**
     * update the global fonts (-> menu bar and tab labels)
     */
    public void updateGlobalFont() {
        if (!control.isStandAlone()) {
            control.getMainControl().setBoldFont(tabPane);
        }

        menuBar.updateFont(true);
    }

    /**
     * Shows the given confirmation message as popup frame. The message is a yes/no option.
     * The parent frame is disabled until the user picks an answer.
     *
     * @param title       the title of the message.
     * @param message     the message to be shown.
     * @return            true if the user agrees, false if user disagrees.
     */
    public boolean askConfirm(String title, String message) {
        return (JOptionPane.showConfirmDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE ) == JOptionPane.YES_OPTION );
    }

    /**
     * Shows the given message in the UI.
     * @param title         the title of the message.
     * @param message       the message to be shown.
     * @param messageType   Type of message (see JOptionPane constants), defines icon used
     */
    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Notifies that the application is about to exit.
     * The window settings get saved to the settings file here.
     */
    void appExitNotify() {
        // store location and size
        CSettings settings = CSettings.getInstance(IGUIConstants.APP_NAME);
        Rectangle bounds = getBounds(); // window frame bounds
        settings.setProperty( WINDOW_X, ""+bounds.x );
        settings.setProperty( WINDOW_Y, ""+bounds.y );
        settings.setProperty( WINDOW_WIDTH, ""+bounds.width );
        settings.setProperty( WINDOW_HEIGHT, ""+bounds.height );
    }

    // --------------------------------------

    /**
     * Subclass: Listener for ChangeEvents in the tabPane
     */
    private class EditTabListener implements ChangeListener {
        ScriptEditControl control; // controler
        ScriptEditView view;       // view
        int index;                 // index of selected tab

        public EditTabListener(ScriptEditControl control, ScriptEditView view) {
            this.control = control;
            this.view = view;
            index = view.getSelectedIndex();
        }

        public void stateChanged(ChangeEvent e) {
            if (index != view.getSelectedIndex()) {
                // active selected tab has changed
                menuBar.refresh(); // refresh state of menus
                index = view.getSelectedIndex(); // update index
            }
        }
    }

    /**
     * Subclass: Listener for closebox on the window
     */
    private class EditWindowListener implements WindowListener {
        ScriptEditControl control; // controler

        /**
         * Constructor
         * @param control ScriptEditControl
         */
        public EditWindowListener(ScriptEditControl control) {
            this.control = control;
        }

        /**
         * Window closebox has been clicked
         * @param e  WindowEvent
         */
        public void windowClosing(WindowEvent e) {
            control.closeAllTabs();
        }

        public void windowClosed(WindowEvent e) {
            control.closeAllTabs(); // (just make sure tabs are removed...)
        }
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowIconified(WindowEvent e) {}
        public void windowOpened(WindowEvent e) {}
    }
}
