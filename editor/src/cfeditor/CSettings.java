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

import java.io.*;
import java.util.*;

/**
 * Static class that contains all the settings (properties) that are needed.
 * The method loadSettings should be called before first use, but it is not
 * mandatory. The settings are saved to the file name given to the loadSettings
 * method when saveSettings is called.
 * Note: If loadSettings method has not been called,
 *       the properties are not saved!
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CSettings {

    /**
     * The properties object that contains the settings.
     */
    private Properties  m_properties = new Properties();

    /**
     * The file that is used to store the settings or null if none specified.
     */
    private String m_strFile;

    /**
     * The shared hashtable that maps filenames to settings instances.
     */
    private static Hashtable m_hashFromNameToInstance = new Hashtable();

    /**
     * Returns the settings instance for the given filename. If no such instance
     * is found one is created and the settings loaded from the file.
     *
     * @param strFile The filename whose settings object instance we want.
     * @return The settings instance for the given filename.
     */
    public static CSettings getInstance(String strFile) {
        if (m_hashFromNameToInstance.containsKey(strFile)) {
            return (CSettings) m_hashFromNameToInstance.get(strFile);
        }

        CSettings instance = new CSettings(strFile);
        instance.loadSettings();
        m_hashFromNameToInstance.put(strFile, instance);
        return instance;
    }

    /**
     * Constructs a settings object that uses the given filename to store and
     * load settings.  Default the location to the users home directory
     * (user.home) and a subdirectory called .cfeditor to follow the unix
     * standard configuration for resource files.
     *
     * @param strFile
     */
    private CSettings(String strFile) {
        StringBuffer buf = new StringBuffer(128);

        String home = System.getProperty("user.home");
        buf.append(home);
        buf.append(File.separator+IGUIConstants.APP_SETTINGS_DIR);
        File rc = new File(buf.toString());
        if (rc.isDirectory() == false) {
            rc.mkdir();
        }
        buf.append("/");
        buf.append(strFile);
        m_strFile = buf.toString();
    }

    /**
     * Loads the settings from the file.
     */
    public synchronized void loadSettings() {
        try {
            m_properties.load(new FileInputStream(m_strFile));
        } catch (IOException ioe) {
        }
    }

    /**
     * Saves the settings to the file name given as parameter to
     * <code>loadSettings</code>. If <code>loadSettings</code> has not been
     * called, the settings are not saved to anywhere.
     */
    public synchronized void saveSettings() {
        if (m_strFile == null) {
            return;
        }

        try {
            //System.out.println("SAVE SETTINGS: "+(new File(m_strFile)).getAbsolutePath());
            m_properties.store(new FileOutputStream(m_strFile), "");
        } catch (IOException ioe) {
        }
    }

    /**
     * Searches for the property with the specified key in this * property
     * list. The method returns <code>null</code> if the * property is not found.
     *
     * @param strKey The key that identifies the property.
     * @param defaultValue The default value to use.
     */
    public synchronized String getProperty(String strKey) {
        return m_properties.getProperty( strKey );
    }

    /**
     * Searches for the property with the specified key in this property
     * list. The method returns the given default value if the property is not
     * found and adds the default value with the key to properties.
     *
     * @param strKey The key that identifies the property.
     * @param strDefaultValue The default value to use.
     */
    public synchronized String getProperty(String strKey, String strDefaultValue) {
        String strValue = m_properties.getProperty( strKey );
        if ( strValue == null ) {
            setProperty( strKey, strDefaultValue );
            return strDefaultValue;
        }
        return strValue;
    }

    /**
     * Maps the specified key to the specified value. Neither the key nor the
     * value can be null. The value can be retrieved by calling the getProperty
     * method with a key that is equal to the original key.
     *
     * @param strKey     the key that identifies the property.
     * @param strValue   the value of the property.
     */
    public synchronized void setProperty(String strKey, String strValue) {
        m_properties.put( strKey, strValue );
    }

    /**
     * Removes the key-value pair of specified key from the property table.
     *
     * @param strKey    the key that identifies the property.
     */
    public synchronized void clearProperty(String strKey) {
        m_properties.remove( strKey );
    }
}
