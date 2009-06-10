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

import java.util.Vector;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import cfeditor.textedit.scripteditor.ScriptEditControl;

/**
 * Subclass of ArchObject to store and manage information about scripted events.
 * This data is only needed for those arches with one or more events defined.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class ScriptArchData {
    // popup frame to edit script paths:
    private static JDialog pathFrame = null;
    private static JTextField inputFPath = null;
    private static JTextField inputPlName = null;
    private static JTextField inputPlOptions = null;
    private static pathButtonListener ccListener;
    private static pathButtonListener okListener;

    // popup frame for new scripts:
    private static JDialog newScriptFrame = null;
    private static JLabel nsHeading = null;
    private static pathButtonListener nsOkListener = null;
    private static JComboBox eventTypeBox = null;
    private static JComboBox pluginNameBox = null;
    private static JTextField inputNFPath = null;
    private static JTextField inputNFOptions = null;

	private final static String[] allEventTypes = new String[] {" apply", " attack",
		" death", " drop", " pickup", " say", " stop", " time", " throw", " trigger", " close", " timer"};

	private ArchObject owner;
	private boolean changed;

    /**
     * Constructor
     */
    public ScriptArchData(ArchObject owner) {
		this.owner = owner;
        if (eventTypeBox == null)
            initEventTypeBoxes();
    }

    /**
     * Initialize the JComboBox with the event types
     */
    private synchronized void initEventTypeBoxes() {
        pluginNameBox = new JComboBox(new String[] {" Python"});
        pluginNameBox.setSelectedIndex(0);
        pluginNameBox.setBackground(Color.white);

        eventTypeBox = new JComboBox(allEventTypes);
        eventTypeBox.setSelectedIndex(6);
        eventTypeBox.setBackground(Color.white);
    }

	/*
	 * Event data:
	 * name = plugin name
	 * slaying = options
	 * sub_type = event type
	 * race = script name (path)
	 */

    /**
     * Search 'eventList' for an existing ScriptedEvent of the specified event type.
     *
     * @param eventType   look for a ScriptedEvent of this type
     * @return            index of ScriptedEvent in 'eventList' if found, -1 if not found
     */
    private ArchObject getScriptedEvent(int eventType) {
		for (ArchObject tmp = owner.getStartInv(); tmp != null; tmp = tmp.getNextInv())
			if(tmp.getArchTypNr() == 118 &&
					(tmp.getAttributeValue("sub_type", owner.getDefaultArch())) == eventType)
				return tmp;
		return null;
    }

    /**
     * This method is called for each arch after loading a map. It checks
     * wether all events have enough data to be valid.
     * Invalid or insufficient ScriptedEvent objects get removed.
     *
     * @return true when at least one valid event remains, false when all events
     *         were invalid (and deleted)
     */
    public void validateAllEvents() {
        ScriptedEvent se; // tmp. event object
		ArchObject next;
		for (ArchObject tmp = owner.getStartInv(); tmp != null; tmp = next) {
			next = tmp.getNextInv();

			if(tmp.getArchTypNr() == 118) {
                se = new ScriptedEvent(tmp);
                // validate this event
                if (se.isValid() == false) {
                    // this event is invalid
                    System.out.println("-> Deleting invalid event...");
					tmp.removeInvObj();
                }
            }
        }
    }

    /**
     * Set all ScriptedEvents to appear in the given JList
     * This method should be fast because it may be executed when user clicks on map objects
     *
     * @param list   JList
     */
    public void addEventsToJList(JList list) {
		Vector content = new Vector();
		for (ArchObject tmp = owner.getStartInv(); tmp != null; tmp = tmp.getNextInv())
			if(tmp.getArchTypNr() == 118)
				content.addElement(" "+typeName(tmp.getAttributeValue("sub_type", owner.getDefaultArch())));

        list.setListData(content);
        list.setSelectedIndex(0);
    }

    public static JDialog getPathFrame() {
        return pathFrame;
    }

    /**
     * If there is a scripted event of the specified type, the script pad
     * is opened and the appropriate script displayed.
     *
     * @param eventIndex  index of event in the owner's inventory
     * @param task        ID number for task (open script/ edit path/ remove)
     * @param panelList   JList from the MapArchPanel (script tab) which displays the events
	 * @return true if a change was done, false otherwise.
     */
    public boolean modifyEventScript(int eventIndex, int task, JList panelList) {
        ArchObject oldEvent = null;
		changed = false;

		/* Find the event object */
		for(oldEvent = owner.getStartInv(); oldEvent != null; oldEvent = oldEvent.getNextInv()) {
			if(oldEvent.getArchTypNr() == 118) {
				if(eventIndex == 0)
					break;
				else
					eventIndex--;
			}
		}

        if (oldEvent != null) {
            ScriptedEvent event = new ScriptedEvent(oldEvent);
            // now decide what to do:
            if (task == CMapArchPanel.SCRIPT_OPEN)
                event.openScript();
            else if (task == CMapArchPanel.SCRIPT_EDIT_PATH) {
                // show popup to edit file path and plugin name
                event.editScriptPath();
            }
            else if (task == CMapArchPanel.SCRIPT_REMOVE) {
                // first ask for confirmation
                boolean breakpath = (event.getFilePath().length() > 15);
                if (JOptionPane.showConfirmDialog(CMainControl.getInstance().getMainView(),
                            "Are you sure you want to remove this \""+typeName(event.getEventType())+"\" event which is\n"+
                            "linked to the script: '"+event.getFilePath()+"'?\n"+
                            "(The script file itself is not going to be deleted)", "Confirm", JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                    // remove this event from the ArchObject
					oldEvent.removeInvObj();
                    this.addEventsToJList(panelList); // update panel JList
					changed = true;
                }
            }
        }
        else
            System.out.println("Error in ScriptArchData.modifyEventScript(): No event selected?");

		return changed;
    }

    /**
     * Try to create a reasonable default script name for lazy users :-)
     * @param archName the best suitable name for the arch (see ArchObject.getBestName())
     * @return a nice default script name without whitespaces
     */
    private String chooseDefaultScriptName(String archName) {
        String defScriptName = archName.trim();
        int i = 0;
        if ((i = defScriptName.indexOf(" ")) >= 0) {
            if (defScriptName.length() > 12 || defScriptName.lastIndexOf(" ") != i) {
                // if there are several whitespaces or the name is too long, just cut off the end
                defScriptName = defScriptName.substring(0, i);
            }
            else {
                // if there is only one whitespace in a short name, remove whitespace
                defScriptName = defScriptName.substring(0, i) + defScriptName.substring(i+1, i+2).toUpperCase() + defScriptName.substring(i+2);
            }
        }
        if (defScriptName.length() >= 3)
            defScriptName = defScriptName.substring(0, 1).toUpperCase() + defScriptName.substring(1);
        defScriptName = defScriptName + "Script.py";

        return defScriptName;
    }

    /**
     * This method is called when the user selects a new event to be created.
     * The path relative to the map dir is calculated, and if reasonable,
     * a relative path is created (relative to the map the event is on).
     * @param f   script file
     * @return    local event path
     */
    private String localizeEventPath(File f) {
        CMainControl m_control = CMainControl.getInstance(); // main control
        File localMapDir = m_control.m_currentMap.mapFile.getParentFile(); // local map directory
        File mapDir = new File(m_control.getMapDefaultFolder()); // global map directory
        File tmp;
        String path = "";

        if (!mapDir.exists()) {
            System.out.println("Map directory '"+mapDir.getAbsolutePath()+"' does not exist!");
            return f.getName();
        }

        // find out if the scriptfile is in a subdirectory of the map file
        for (tmp = f.getParentFile(); tmp != null && !tmp.getAbsolutePath().equalsIgnoreCase(localMapDir.getAbsolutePath());
             tmp = tmp.getParentFile());

        if (tmp == null) {
            // scriptfile is NOT in a subirectory of mapfile -> absolute path
            path = f.getAbsolutePath().substring(mapDir.getAbsolutePath().length());
            path = path.replace('\\', '/');
            if (!path.startsWith("/"))
                path = "/"+path; // leading slash
        }
        else {
            // scriptfile is in a subirectory of mapfile -> relative path
            path = f.getAbsolutePath().substring(localMapDir.getAbsolutePath().length());
            path = path.replace('\\', '/');
            while (path.length() > 0 && path.startsWith("/"))
                path = path.substring(1); // no leading slash
        }

        return path;
    }

    /**
     * A popup is opened and the user can create a new scripting event
     * which gets attached to this arch.
     *
     * @param panelList   JList from the MapArchPanel (script tab) which displays the events
	 * @return true if a script was added, false if the user cancelled
     */
    public boolean addEventScript(JList panelList, ArchObject arch) {
        String archName = arch.getBestName(arch.getDefaultArch());
        // create a reasonable default script name for lazy users :-)
        String defScriptName = chooseDefaultScriptName(archName);

		changed = false;

        if (newScriptFrame == null) {
            // initialize popup frame
            newScriptFrame = new JDialog(CMainControl.getInstance().getMainView(), "New Scripted Event", true);
            newScriptFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

            JPanel main_panel = new JPanel();
            main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
            main_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

            // first line: heading
            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            nsHeading = new JLabel("New scripted event for \""+archName+"\":");
            nsHeading.setForeground(Color.black);
            line.add(nsHeading);
            main_panel.add(line);

            // event type
            main_panel.add(Box.createVerticalStrut(10));
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel tlabel = new JLabel("Event Type: ");
            line.add(tlabel);
            line.add(eventTypeBox);
            //main_panel.add(line);
            line.add(Box.createHorizontalStrut(10));

            // plugin name
            tlabel = new JLabel("Plugin: ");
            line.add(tlabel);
            line.add(pluginNameBox);
            main_panel.add(line);

            // path
            main_panel.add(Box.createVerticalStrut(5));
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tlabel = new JLabel("Script File:");
            line.add(tlabel);
            main_panel.add(line);
            inputNFPath = new JTextField(defScriptName, 20);
            JButton browseb = new JButton("...");
            browseb.setMargin(new Insets(0, 10, 0, 10));
            browseb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    File home = CMainControl.getInstance().m_currentMap.mapFile.getParentFile(); // map dir

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select Script File");
                    fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                    fileChooser.setCurrentDirectory(home);
                    fileChooser.setMultiSelectionEnabled( false );
                    fileChooser.setFileFilter(new FileFilterPython());

                    if (fileChooser.showOpenDialog(newScriptFrame) == JFileChooser.APPROVE_OPTION) {
                        // user has selected a file
                        File f = fileChooser.getSelectedFile();
                        inputNFPath.setText(localizeEventPath(f));
                    }
                }
            });
            line.add(inputNFPath);
            line.add(browseb);
            main_panel.add(line);

			// options
            main_panel.add(Box.createVerticalStrut(5));
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            line.add(new JLabel("Script Options:"));
            inputNFOptions = new JTextField("", 20);
            line.add(inputNFOptions);
            main_panel.add(line);

            // description
            line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            tlabel = new JLabel("When you specify an existing file, the new event will be linked");
            textPanel.add(tlabel);
            tlabel = new JLabel("to that existing script. Otherwise a new script file is created.");
            textPanel.add(tlabel);
            line.add(textPanel);
            main_panel.add(line);

            // button panel:
            main_panel.add(Box.createVerticalStrut(10));
            line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton nsOkButton = new JButton("OK");
            nsOkListener = new pathButtonListener(true, newScriptFrame, this);
            nsOkButton.addActionListener(nsOkListener);
            line.add(nsOkButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new pathButtonListener(false, newScriptFrame, null));
            line.add(cancelButton);
            main_panel.add(line);

            newScriptFrame.getContentPane().add(main_panel);
            newScriptFrame.pack();
            newScriptFrame.setLocationRelativeTo(CMainControl.getInstance().getMainView());
            newScriptFrame.setVisible(true);
        }
        else {
            // just set fields and show
            nsHeading.setText("New scripted event for \""+archName+"\":");
            inputNFPath.setText(defScriptName);
            nsOkListener.setSdata(this);
            newScriptFrame.toFront();
            newScriptFrame.setVisible(true);
        }
		return changed;
    }

    /**
     * The user has chosen to create a new event, now it is to be done.
     * @param frame    the parent window of the create-new-event dialog.
     */
    public boolean createNewEvent(JDialog frame) {
        CMainControl m_control = CMainControl.getInstance(); // main control
        String scriptPath = inputNFPath.getText().trim();
        String scriptOptions = inputNFOptions.getText().trim();
        int eventType = eventTypeBox.getSelectedIndex() + 1;
        String pluginName = ((String)(pluginNameBox.getSelectedItem())).trim();

        File localMapDir = m_control.m_currentMap.mapFile.getParentFile(); // local map directory
        String absScriptPath; // the absolute script path
        ArchObject replaceObject = null; // event to replace ('null' means no replace)

		changed = false;

        // first check if that event type is not already in use
        replaceObject = getScriptedEvent(eventType);
        if (replaceObject != null) {
            // collision with existing event -> ask user: replace?
             if (JOptionPane.showConfirmDialog(frame, "An event of type \""+typeName(eventType)+"\" already exists for this object.\n"+
                                             "Do you want to replace the existing event?", "Event exists",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION) {
                // bail out
                return false;
            }
        }

        // convert localized scriptPath into real, absolute path
        scriptPath = scriptPath.replace('\\', '/');
        if (scriptPath.startsWith("/")) {
            // script path is absolute
            File mapDir = new File(m_control.getMapDefaultFolder()); // global map directory
            if (!mapDir.exists()) {
                // if map dir doesn't exist, this is not going to work
                frame.setVisible(false);
                m_control.showMessage("Invalid Map Directory", "The map directory '"+mapDir+"' does not exist!\n"+
                                      "Please select menu 'File->Options...' and correct that.", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else {
                absScriptPath = mapDir.getAbsolutePath() + scriptPath;
            }
        }
        else {
            // script path is relative
            absScriptPath = localMapDir.getAbsolutePath() + File.separator + scriptPath;
        }

        // now check if the specified path points to an existing script
        File newScriptFile = new File(absScriptPath);
        if (!newScriptFile.exists() && !absScriptPath.endsWith(".py")) {
            absScriptPath += ".py";
            scriptPath += ".py";
            newScriptFile = new File(absScriptPath);
        }

        if (newScriptFile.exists()) {
            if (newScriptFile.isFile()) {
                // file exists -> link it to the event
                ScriptedEvent event = new ScriptedEvent(eventType, pluginName, scriptPath, scriptOptions);
                if (replaceObject != null)
					replaceObject.removeInvObj();
				owner.addInvObj(event.getEventArch());
                frame.setVisible(false); // close dialog
            }
        }
        else {
            if (!absScriptPath.endsWith(".py")) {
                absScriptPath += ".py";
                scriptPath += ".py";
                newScriptFile = new File(absScriptPath);
            }

            // file does not exist -> aks user: create new file?
            if (JOptionPane.showConfirmDialog(frame, "Create new script '"+newScriptFile.getName()+"'?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                // okay, create new script file and open it
                boolean couldCreateFile = false; // true when file creation successful
                try {
                    // try to create new empty file
                    couldCreateFile = newScriptFile.createNewFile();
                }
                catch (IOException e) {}

                if (!couldCreateFile) {
                    JOptionPane.showMessageDialog(frame, "File '"+newScriptFile.getName()+"' could not be created.\n"+
                                "Please check your path and write premissions.",
                                "Cannot create file", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    // file has been created, now link it to the event
                    ScriptedEvent event = new ScriptedEvent(eventType, pluginName, scriptPath, scriptOptions);
                    if (replaceObject != null)
						replaceObject.removeInvObj();
					owner.addInvObj(event.getEventArch());
                    frame.setVisible(false); // close dialog

                    // open new script file
                    ScriptEditControl.getInstance().openScriptFile(newScriptFile.getAbsolutePath());
                }
            }
        }

		return changed;
    }

    /**
     * (Note that empty ScriptArchData objects always are removed ASAP)
     * @return true when this ScriptArchData contains no events
     */
    public boolean isEmpty() {
		for (ArchObject tmp = owner.getStartInv(); tmp != null; tmp = tmp.getNextInv())
			if(tmp.getArchTypNr() == 118)
				return false;
		return true;
    }

	private String typeName(int eventType) {
		if(eventType > 0 && eventType <= allEventTypes.length)
			return allEventTypes[eventType-1];
		else
			return "<invalid type>";
	}

    // ------------------------------- SUBCLASSES ------------------------------------

    /**
     * Subclass which stores information about one scripted event
     */
    private class ScriptedEvent {
		private ArchObject event; // Our event object

        /**
         * Construct a ScriptedEvent of given type (This is used for map-loading)
         * @param event
         */
        public ScriptedEvent(ArchObject event) {
            this.event = event;
        }

        /**
         * Construct a fully initialized ScriptedEvent
         * @param eventType
         * @param pluginName
         * @param options
         * @param filePath
         */
        public ScriptedEvent(int eventType, String pluginName, String filePath, String options) {
			event = new ArchObject();
			event.setArchTypNr(118);
			event.setArchName("event_obj");

			Integer index = (Integer)ArchObject.getArchStack().getArchHashTable().get("event_obj");
            if(index != null) {
                event.setNodeNr(index.intValue());
                event.setObjectFace();
			}

			setEventData(eventType, pluginName, filePath, options);
        }

        /**
         * Validate this event object: Check if there is sufficient data.
         * @return true if this object is valid, otherwise false.
         */
        public boolean isValid() {
			String options = getOptions();
			String filePath = getFilePath();
			String pluginName = getPluginName();
			int eventType = getEventType();

            if (filePath == null || filePath.length() <= 0) {
                System.out.println("Map Error: Found "+typeName(eventType)+" event without file name!");
                return false;
            }

            if (pluginName == null || pluginName.length() <= 0) {
				pluginName = "Python";
                System.out.println("Found "+typeName(eventType)+" without plugin name. Setting to \"Python\".");
            }

            filePath.replace('\\', '/'); // just make sure there are only slashes: '/'

			setEventData(eventType, pluginName, filePath, options);

            return true;
        }

        /**
         * Opens the script pad to display the script for this event.
         */
        public void openScript() {
            CMainControl m_control = CMainControl.getInstance(); // refernce to main control
            String path = ""; // file path to script file
			String filePath = getFilePath();

            // trying to get the absolute path to scriptfile:
            if (filePath.startsWith("/")) {
                // filepath is absolue (to map base directory):
                path = m_control.getMapDefaultFolder();
                path += filePath;
            }
            else {
                // file path is relative to map dir
                path = m_control.m_currentMap.mapFile.getParentFile().getAbsolutePath(); // map dir
                if (!path.endsWith("/"))
                    path = path + "/"; // append slash to map dir
                path += filePath; // append relative path to map dir
                path = path.replace('\\', '/'); // make sure there's only one kind of slash
            }

            // now see if that file really exists:
            File scriptFile = new File(path);
            if (scriptFile.exists() && scriptFile.isFile())
                ScriptEditControl.getInstance().openScriptFile(scriptFile.getAbsolutePath());
            else {
                // file does not exist!
                m_control.showMessage("Script file not found",
                            "The file '"+path+"' does not exist.\nPlease correct the path.", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Edit path and plugin name for this event. A popup dialog is shown
         * with input textfields for file path and plugin name.
         */
        public void editScriptPath() {
            if (pathFrame == null) {
                // initialize popup frame
                pathFrame = new JDialog(CMainControl.getInstance().getMainView(), "Script Path", true);
                pathFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

                JPanel main_panel = new JPanel();
                main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
                main_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

                // input line: script path
                JPanel line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JLabel text = new JLabel("Script:");
                line.add(text);
                inputFPath = new JTextField(getFilePath(), 20);
                line.add(inputFPath);
                main_panel.add(line);

				// input line: plugin options
                line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                text = new JLabel("Script options:");
                line.add(text);
                inputPlOptions = new JTextField(getOptions(), 20);
                line.add(inputPlOptions);
                main_panel.add(line);
                main_panel.add(Box.createVerticalStrut(5));

                // input line: plugin name
                line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                text = new JLabel("Plugin name:");
                line.add(text);
                inputPlName = new JTextField(getPluginName(), 20);
                line.add(inputPlName);
                main_panel.add(line);
                main_panel.add(Box.createVerticalStrut(5));

                // button panel:
                line = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton okButton = new JButton("OK");
                okListener = new pathButtonListener(true, pathFrame, null);
                okListener.setTargetEvent(this);
                okButton.addActionListener(okListener);
                line.add(okButton);

                JButton cancelButton = new JButton("Cancel");
                ccListener = new pathButtonListener(false, pathFrame, null);
                ccListener.setTargetEvent(this);
                cancelButton.addActionListener(ccListener);
                line.add(cancelButton);
                main_panel.add(line);

                pathFrame.getContentPane().add(main_panel);
                pathFrame.pack();
                pathFrame.setLocationRelativeTo(CMainControl.getInstance().getMainView());
                pathFrame.setVisible(true);
            }
            else {
                // just set fields and show
                okListener.setTargetEvent(this);
                ccListener.setTargetEvent(this);
                inputFPath.setText(getFilePath());
                inputPlName.setText(getPluginName());
                inputPlOptions.setText(getOptions());
                pathFrame.toFront();
                pathFrame.setVisible(true);
            }
        }

        /**
         * Set event path and plgin name according to user input
         * from popup dialog
         */
        public void modifyEventPath() {
            String newPath = inputFPath.getText().trim();
            String newPlName = inputPlName.getText().trim();
            String newPlOptions = inputPlOptions.getText().trim();

            if (newPath.length() > 0) {
                setFilePath(newPath);
            }
            if (newPlName.length() > 0) {
                setPluginName(newPlName);
            }
            if (newPlOptions.length() > 0) {
                setOptions(newPlOptions);
            }
        }

        // GET/SET methods
		public ArchObject getEventArch() {return event;}

        public int getEventType() {return event.getAttributeValue("sub_type", event.getDefaultArch());}
        public String getPluginName() {return event.getBestName(event.getDefaultArch());}
        public String getFilePath() {return event.getAttributeString("race", event.getDefaultArch());}
        public String getOptions() {return event.getAttributeString("slaying", event.getDefaultArch());}

        public void setPluginName(String name) {setEventData(getEventType(), name, getFilePath(), getOptions());}
        public void setFilePath(String path) {setEventData(getEventType(), getPluginName(), path, getOptions());}
        public void setOptions(String opts) {setEventData(getEventType(), getPluginName(), getFilePath(), opts);}

		private void setEventData(int eventType, String pluginName, String filePath, String options) {
			event.resetArchText();
			if(pluginName != null && pluginName.length() > 0)
				event.setObjName(pluginName);
			if(filePath != null && filePath.length() > 0)
				event.addArchText("race " + filePath + "\n");
			if(options != null && options.length() > 0)
				event.addArchText("slaying " + options + "\n");
			event.addArchText("sub_type " + eventType + "\n");
		}
    }

    /**
     * Small class, listening for button-press events in the
     * popup frame for script paths or create-new-event frame
     */
    private class pathButtonListener implements ActionListener {
        private JDialog frame = null;
        private boolean isOkButton = false;
        private ScriptedEvent target = null; // target event
        private ScriptArchData sdata = null; // script arch data

        /**
         * Constructor
         * @param isOkButton   true for ok-buttons
         * @param frame        frame this listener belongs to
         * @param sdata        this is only set for the ok-button of "create new" frame, otherwise null
         */
        public pathButtonListener(boolean isOkButton, JDialog frame, ScriptArchData sdata) {
            this.isOkButton = isOkButton;
            this.frame = frame;
            this.sdata = sdata;
        }

        public void setTargetEvent(ScriptedEvent newt) {target = newt;}

        public void setSdata(ScriptArchData newsd) {sdata = newsd;}

        public void actionPerformed(ActionEvent e) {
            if (isOkButton && sdata == null && target != null) {
                target.modifyEventPath(); // ok button for modifying path
				changed = true;
			}

            if (isOkButton && sdata != null) {
                // ok button for creating a new event/script
                sdata.createNewEvent(frame);
				changed = true;
            }
            else
                frame.setVisible(false); // hide dialog
        }
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
