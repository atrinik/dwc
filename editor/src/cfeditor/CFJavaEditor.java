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

import java.io.*;

/**
 * MAIN CLASS
 * The launcher that launches the whole level editor application.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @version $Revision: 1.10 $
 */
public class CFJavaEditor {
    /**
     * The main method that is invoked by the Java Runtime.
     * @param astrParams The command line parameters given to the level editor.
     */
    public static void main(String[] astrParams) {
        String infile = null;  // map file name to open initially
        String outfile = null; // if specified in the parameters, create image of map and exit
        for (int i=0; i < astrParams.length; i++) {
            // get command line parameters for "infile" (map to open initially)
            // and "outfile" (image created from infile map)
            if (astrParams[i].compareTo("-infile") == 0 && i < astrParams.length-1)
                infile = astrParams[++i];
            else if (astrParams[i].compareTo("-outfile") == 0 && i < astrParams.length-1)
                outfile = astrParams[++i];
            else
                System.out.println("Got unknown option: " + astrParams[i] + "\n");
        }
        // print jre version, for easier recognition of jre-specific problems
        System.out.println("Running java version "+System.getProperty("java.version"));
        
        try {
            // Create the application and give it the parameters
            CMainControl controller = new CMainControl(astrParams);

            // Initialise the application
            controller.init();
            controller.refreshMenusAndToolbars();
            controller.getArchObjectStack().loadArches();
            System.gc();

            // process commandline parameters:
            if (infile != null)
                controller.openFile(new File(infile)); // open initial map
            if (outfile != null) {
                controller.createImageWanted(outfile); // create map image
                System.exit(0);                        // exit
            }

            controller.refreshMenusAndToolbars();

        } catch(CGridderException exception) {
            System.out.println(""+exception.getMessage());
        }
    }
}
