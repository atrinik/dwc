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

package cfeditor.textedit.scripteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.net.URL;
import java.io.File;

/**
 * <code>CGUtils</code> is a collection of GUI utility methods.
 * Mainly focusing on resource management.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CGUIUtils {

    /**
     * The instance of GUI utils used internally.
     */
    private static CGUIUtils mStatic_instance = new CGUIUtils();
    private static Hashtable m_nameToImage = new Hashtable();

    /**
     * @return the static instance of this class
     */
    public static CGUIUtils getInstance() {return mStatic_instance;}

    /**
     * Returns the image icon for the given icon name. Loads every icon
     * only once and uses hashtable to return the same instance if same
     * icon name is given.
     * Note: There must not be conflicting icon names from
     * different directorys.
     *
     * @param dirName         name of the directory the icon is in
     * @param strIconName     the icon name (propably one of the
     *                        constants defined in IGUIConstants).
     */
    private static ImageIcon getResourceIcon(String dirName, String strIconName) {
        // first, look if this icon is already available in the Hashtable
        if (m_nameToImage.containsKey(strIconName)) {
            return (ImageIcon) m_nameToImage.get(strIconName);
        }

        // looks like we need to load this icon
        ImageIcon icon = null;

        File imageFile = new File(dirName +File.separator+ strIconName);
        if (imageFile.exists()) {
            // image file exists in expected directory
            icon = new ImageIcon(imageFile.getAbsolutePath());
        }
        else {
            // image file is missing, so let's try to load it from jar
            URL imageResource = ClassLoader.getSystemResource(dirName.replace('\\', '/') +"/"+ strIconName);
            // note that 'getSystemResource' never recognises non-unix
            // style file-seperators (so much for platform-independency)

            if (imageResource != null) {
                icon = new ImageIcon(imageResource);
            }
            else if (dirName.indexOf(File.separator) >= 0){
                // let's try it again without first directory (okay, this may look
                // a bit weird, but usually this is the correct icon path in the jar)
                imageResource = ClassLoader.getSystemResource(dirName.substring(dirName.indexOf(File.separator)+1)
                                                              +"/"+ strIconName);
                if (imageResource != null) {
                    icon = new ImageIcon(imageResource);
                }
                else
                    System.out.println("Failed to load icon '"+strIconName+"'!");
            }
            else
                System.out.println("Failed to load icon '"+strIconName+"'!");
        }

        // put this icon into the Hashtable
        if (icon != null)
            m_nameToImage.put( strIconName, icon );
        return icon;
    }

    public static ImageIcon getIcon(String strIconName) {
        return getResourceIcon(IGUIConstants.ICON_DIR, strIconName);
    }

    public static ImageIcon getSysIcon(String strIconName) {
        return getResourceIcon(IGUIConstants.SYSTEM_DIR, strIconName);
    }

    /**
     * Returns the given image icon as a grayscaled image icon.
     *
     * @param icon The colour icon to be grayscaled.
     * @return The given icon as grayscaled version.
     */
    public static ImageIcon getGrayScaled(ImageIcon icon) {
        // Use the static instance to do the grayscaling
        return mStatic_instance._getGrayScaled( icon );
    }

    /**
     * Returns the given image icon as a grayscaled image icon.
     *
     * @param icon The colour icon to be grayscaled.
     * @return The given icon as grayscaled version.
     */
    private ImageIcon _getGrayScaled(ImageIcon icon) {
        CGrayScaleFilter filter = new CGrayScaleFilter();
        ImageProducer imageProducer =
            new FilteredImageSource(icon.getImage().getSource(), filter);
        Image grayIcon = Toolkit.getDefaultToolkit().createImage(imageProducer);
        return new ImageIcon(grayIcon);
    }


    /**
     * Grayscale filter to make grayscaled images.
     */
    private class CGrayScaleFilter extends RGBImageFilter {

        /**
         * Converts a single input pixel in the default RGB ColorModel
         * to a single output pixel with grayscaling.
         *
         * @param x The x-coordinate of the pixel.
         * @param y The y-coordinate of the pixel.
         * @param rgb The pixels colour in default RGB ColorModel.
         */
        public int filterRGB(int x, int y, int rgb) {
            float red   = (float) (( rgb >> 16) & 0xFF);
            float green = (float) (( rgb >> 8) & 0xFF);
            float blue  = (float) (rgb & 0xFF);

            int gray = (int)(0.33F * red + 0.33F * green + 0.33F * blue);

            if (gray < 0) gray = 0;
            if (gray > 255) gray = 255;
            return (rgb & 0xff000000) | (gray << 16) | (gray << 8) | (gray << 0);
        }
    }
}
