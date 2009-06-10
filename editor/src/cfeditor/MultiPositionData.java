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
import java.util.StringTokenizer;

/**
 * The MultiPositionData class stores an array of numbers which is required
 * in order to calculate display positions of ISO multipart objects.
 * (This class is never used in Crossfire modus)
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class MultiPositionData {
    /** number of rows and columns in the array */
    public static final int X_DIM = 34;
    public static final int Y_DIM = 16;

    private static MultiPositionData instance = null;

    /** array with position data */
    private int[][] data;

    /**
     * private constructor: initialize array
     */
    private MultiPositionData() {
        data = new int[Y_DIM][X_DIM];
    }

    /**
     * This is the static init method. It is called once
     * in the global initialization phase.
     */
    public static synchronized void init() {
        if (instance == null) {
            instance = new MultiPositionData();
            instance.load();
        }
    }

    /**
     * Load the array-data from file. An error is reported when the numbers
     * in the file don't match expected array dimensions.
     */
    private void load() {
        CFileReader reader;

        // read datafile line by line, parsing numbers into the array
        try {
            reader = new CFileReader(CMainControl.getInstance().getArchDefaultFolder()+File.separator+IGUIConstants.CONFIG_DIR, IGUIConstants.ARCHDEF_FILE);
            String line = null; // read line of file
            String num;         // number, still in string format
            int xp = 0;         // x-index in the data array
            int yp = 0;         // y-index in the data array

            // read the whole file line by line
            while ((line = reader.getReader().readLine()) != null) {
                line = line.trim();
                if (line.length()>0 && !line.startsWith("#") && yp < Y_DIM) {
                    StringTokenizer s = new StringTokenizer(line, " ", false);
                    while (s != null && s.hasMoreTokens()) {
                        num = s.nextToken(); // get one number
                        if (num.length() > 0 && xp < X_DIM) {
                            try {
                                // parse and store it as integer
                                data[yp][xp] = Integer.parseInt(num);
                                xp++; // increase index
                            }
                            catch (NumberFormatException e) {}
                        }

                    }

                    // report if there haven't been enough numbers in this row
                    if (xp < X_DIM)
                        System.out.println("In file "+IGUIConstants.ARCHDEF_FILE+": Missing "+(X_DIM-xp)+" numbers in row "+(yp+1)+".");

                    xp = 0; yp++; // prepare indices for next row
                }
            }
            reader.close(); // close filereader

            // report if there haven't been enough rows in the file
            if (yp < Y_DIM)
                System.out.println("In file "+IGUIConstants.ARCHDEF_FILE+": Missing "+(Y_DIM-yp)+" entire rows of data.");

            // confirm load process
            System.out.println("Loaded multipart position data from '"+IGUIConstants.ARCHDEF_FILE+"'");
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
    }

    /**
     * Calculate the X-offset from the leftmost pixel of the big face image and
     * the default X-position (The default position is where a single-tile image would be put).
     *
     * @param shapeID        ID number for the multisquare shape (-> rows in position data file)
     * @param positionID     number of tile in the big bunch
     * @return               X-offset
     */
    public static synchronized int getXOffset(int shapeID, int positionID) {
        if (instance != null) {
            return instance.data[shapeID][2 + positionID*2];
        }
        return 0;
    }

    /**
     * Calculate the Y-offset from the topmost pixel of the big face image and
     * the default Y-position (The default position is where a single-tile image would be put).
     *
     * @param shapeID        ID number for the multisquare shape (-> rows in position data file)
     * @param positionID     number of tile in the big bunch
     * @return               Y-offset
     */
    public static synchronized int getYOffset(int shapeID, int positionID) {
        if (instance != null) {
            return (instance.data[shapeID][1] - IGUIConstants.TILE_ISO_YLEN - instance.data[shapeID][3 + positionID*2]);
        }
        return 0;
    }
}
