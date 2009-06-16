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

import java.util.Vector;
import java.io.*;
import javax.swing.JFileChooser;

import cfeditor.textedit.textarea.*;
import cfeditor.IGUIConstants;
import cfeditor.CSettings;
import cfeditor.CMainControl;

/**
 * ScriptEditControl - Manages events and dataflow for the script editor entity.
 * There's always at most only one frame open. Additional files get attached
 * to the tab bar.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ScriptEditControl {
    // static instance of this class (there's only one controller and max. one open window)
    private static ScriptEditControl instance = null;

    // last active popup is stored here
    private static CFPythonPopup activePopup = null;

    private static boolean isStandAlone; // if true, this script pad is run stand-alone (without CFJavaEditor)

    // default directory to store new scripts in. also used as starting dir for 'open' command
    private static String defaultScriptDir;

    private CMainControl m_control; // cfeditor main control (is null in stand-alone configuration!)
    public ScriptEditView view;    // view (window with textareas)
    private Vector opened;          // open tabs, contains absolute filenames (or "<>") in order left to right

    /**
     * Constructor is private, instance is created by calling 'init()'
     */
    private ScriptEditControl(CMainControl m_control) {
        opened = new Vector(); // start with empty vector
        this.m_control = m_control;
        view = new ScriptEditView(this); // initialize window
    }

    /**
     * Init method initializes static instance of this controller.
     * Has to be called before once using this class.
     *
     * @param mapDefFolder map default folder
     */
    public static void init(String mapDefFolder, CMainControl main_control) {
        if (instance == null) {
            isStandAlone = false;
            instance = new ScriptEditControl(main_control);
            defaultScriptDir = mapDefFolder; // set map default folder
        }
    }

    /**
     * Special private Init method, which allows stand-alone configuration.
     * If run stand-alone, window close operations will terminate the application.
     *
     * @param mapDefFolder  map default folder
     */
    private static void init(String mapDefFolder) {
        if (instance == null) {
            isStandAlone = true;
            instance = new ScriptEditControl(null);
            defaultScriptDir = mapDefFolder; // set map default folder
        }
    }

    /**
     * @return static instance of this class
     */
    public static ScriptEditControl getInstance() {
        return instance;
    }

    public boolean isStandAlone() {
        return isStandAlone;
    }

    /**
     * @return instance of cfeditor main control (is null for stand-alone configuration!)
     */
    CMainControl getMainControl() {
        return m_control;
    }

    /**
     * Notifies that the application is about to exit.
     */
    public void appExitNotify() {
        view.appExitNotify(); // notify view
    }

    /**
     * Register last active popup. When the script pad frame is hidden,
     * this popup will be closed (if still open).
     * @param p   active popup to register
     */
    public void registerActivePopup(CFPythonPopup p) {
        activePopup = p;
    }

    /**
     * Open a new empty python script document
     */
    public void openScriptNew() {
        opened.addElement(new String("<>")); // this script has no filename assigned yet
        view.addTab("<New Script>", null);
    }

    /**
     * Open a new empty python script document
     */
    public void openScriptFile(String pathname) {
        File f = new File(pathname);

        if (f.exists() && f.isFile()) {
            opened.addElement(new String(f.getAbsolutePath()));
            view.addTab(f.getName(), f);
        }
        else {
            System.out.println("Error in ScriptEditControl.openScriptFile():");
            System.out.println("   File '"+pathname+"' doesn't exist.");
        }
    }

    /**
     * Open a file which is chosen by the user.
     */
    public void openUserWanted() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Script File");
        fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fileChooser.setMultiSelectionEnabled( false );

        // set file filter for ".py" ending
        fileChooser.setFileFilter(new FileFilterPython());

        // set default folder for new scripts
        File baseDir = new File(defaultScriptDir);
        if (baseDir != null && baseDir.exists() && baseDir.isDirectory())
            fileChooser.setCurrentDirectory(baseDir);

        int returnVal = fileChooser.showOpenDialog(view);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists() && !file.isDirectory()) {
                // everything okay do far, now open up that scriptfile
                openScriptFile(file.getAbsolutePath());
            }
            else {
                // user entered a filename which doesn't yet exist -> open new script
                openScriptNew();
            }
        }
    }

    /**
     * Close the active script-tab
     */
    public void closeActiveTab() {
        if (view.getSelectedIndex() >= 0 && opened.size() > 0)
            opened.removeElementAt(view.getSelectedIndex()); // dump the filename
        view.closeActiveTab(); // close tab in the view

        // hide view when last tab has been closed
        if (view.getTabCount() <= 0) {
            if (activePopup != null && (activePopup.isShowing() || activePopup.getMenu().isShowing()))
                activePopup.getMenu().setVisible(false);
            view.setVisible(false);
            if (isStandAlone()) {
                // if running in stand-alone mode, the application gets terminated at this point
                appExitNotify();
                // save settings
                CSettings.getInstance(IGUIConstants.APP_NAME).saveSettings();
                System.exit(0); // exit
            }
        }
    }

    /**
     * Close all opened script-tabs
     */
    public void closeAllTabs() {
        // simply keep closing active tabs till none are left
        while (view.getSelectedIndex() >= 0 || opened.size() > 0)
            closeActiveTab();
    }

    /**
     * Open a filebrowser and prompt the user for a location/name to
     * store this file. If everything goes fine, the file is saved.
     */
    public void saveAsActiveTab() {
        String activePath = getActiveFilePath(); // active file path ('null' if undefined)
        String text = getActiveTextArea().getText(); // Store text data to ensure that the right text is saved later.
                                                     // User could switch tabs or type text in the meantime, who knows.
        int tabIndex = view.getSelectedIndex(); // save tab-index of this script

        // create the file-chooser dialog:
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Script File As");
        fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fileChooser.setMultiSelectionEnabled(false);

        // set file filter for ".py" ending
        fileChooser.setFileFilter(new FileFilterPython());

        // if file already exists, select it
        if (activePath != null) {
            File f = new File(activePath);

            if (f != null && f.getParentFile().exists() && f.getParentFile().isDirectory()) {
                fileChooser.setCurrentDirectory(f.getParentFile());
                fileChooser.setSelectedFile(f); // select this name
            }
        }
        else {
            // set default folder for new scripts
            File baseDir = new File(defaultScriptDir);
            if (baseDir != null && baseDir.exists() && baseDir.isDirectory())
                fileChooser.setCurrentDirectory(baseDir);
        }

        int returnVal = fileChooser.showSaveDialog(view);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".py")) {
                // We have to attach the ".py" ending to this file
                String fname = file.getAbsolutePath();
                file = null;
                file = new File(fname + ".py");
            }

            // now it is our duty to doublecheck if user attempts to overwrite
            if (!file.exists() || (activePath != null && file.getAbsolutePath().equals(activePath)) ||
                view.askConfirm("Overwrite?", "A file named \""+file.getName()+"\" already exists.\n"+
                                              "Are you sure you want to overwrite it?")) {
                // looks like we can finally save the data
                saveTextToFile(file, text);

                // now update the filepath for this open script tab
                if (tabIndex >= 0 && opened.size() > tabIndex) {
                    // it might be nice here to check the content of the document at 'index',
                    // to make sure it stayed the same
                    //String path = (String)(opened.elementAt(tabIndex));

                    // set new path
                    opened.setElementAt(new String(file.getAbsolutePath()), tabIndex);
                    view.setTitleAt(tabIndex, file.getName());

                    view.refreshMenuBar();
                }
            }
        }
    }

    /**
     * Save the active script-tab to the stored filepath
     */
    public void saveActiveTab() {
        if (getActiveFilePath() != null) {
            File f = new File(getActiveFilePath());            // get active path
            saveTextToFile(f, getActiveTextArea().getText());  // write text to file
        }
        else {
            System.out.println("ScriptEditControl.saveActiveTab(): Cannot save file without name!");
            // Path is missing? This shouldn't happen, but let's do a saveAs instead...
            saveAsActiveTab();
        }
    }

    /**
     * Write the given text into the specified file
     * @param file       text gets saved into this file
     * @param text       text to be saved
     */
    public void saveTextToFile(File f, String text) {
        if (text == null) text = "";

        if (f != null) {
            try {
                if (!f.exists()) {
                    if (!f.createNewFile()) {
                        // failed to create new file
                        System.out.println("Couldn't create file '"+f.getName()+"'!");
                    }
                }

                FileWriter fwrite = new FileWriter(f);

                fwrite.write(text);

                fwrite.close();
            }
            catch (IOException e) {
                // tell the user because it is important to know that saving failed
                view.showMessage("Write Error", "The file \""+f.getName()+"\" could not be written.\n"+
                                                "Please use the 'Save As...' menu.", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
        else
            System.out.println("ScriptEditControl.saveTextToFile(): Cannot save - File is NULL!");
    }

    /**
     * @return currently active JEditTextArea, or null if none are open
     */
    JEditTextArea getActiveTextArea() {
        return (view.getActiveTextArea());
    }

    /**
     * @return file path of active tab, null if no path is available
     */
    String getActiveFilePath() {
        if (view != null && opened != null && view.getSelectedIndex() >= 0 && opened.size() > 0) {
            // get stored path
            String path = (String)(opened.elementAt(view.getSelectedIndex()));

            if (path == null || path.length() == 0 || path.equals("<>"))
                return null;
            else
                return path;
        }
        return null;
    }
    /**
     * This calls the showMessage funtion from view
     * @param strTitle String
     * @param strMessage String
     */
    public void showMessage(String strTitle, String strMessage ) {
       view.showMessage(strTitle, strMessage );
     }


    /**
     * Global font has been changed, update the menu bar
     */
    public void updateGlobalFont() {
        view.updateGlobalFont();
    }

    /**
     * Main method for testing purpose
     * @param args
     */
    public static void main(String[] args) {
        init(System.getProperty("user.dir"));
        getInstance().openScriptNew();
        getInstance().openScriptNew();
        getInstance().openScriptFile("maps\\GuildMage.py");

        getInstance().view.refreshMenuBar();
    }

    /**
     * FileFilterPython is a subclass which filters *.py files
     * int the JFileChooser dialog.
     */
    public class FileFilterPython extends javax.swing.filechooser.FileFilter {
        public String getDescription() {
            return "*.py";
        }

        public boolean accept(File f) {
            if (f.isDirectory() || f.getName().endsWith(".py"))
                return true;
            return false;
        }
    }
}
