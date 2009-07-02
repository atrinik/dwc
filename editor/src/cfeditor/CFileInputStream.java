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

/**
 * Class for opening and reading a buffered stream to a BINARY resource-file.
 * If no such file is found, the file is loaded from the jar archive.
 * This class is very similar to CFileReader.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFileInputStream {
    private String filename;           // name of the resource file
    private String filedir;            // directory of the resource file
    private boolean is_binfile = false;  // true if the resource was opened as binary file
    private boolean is_jar = false;      // true if resource was opened from jar archive

    // streams
    private FileInputStream fileInput;  // binary file input stream
    private InputStream inStream;       // basic binary input stream
    private BufferedInputStream stream; // buffered stream

    /**
     * Constructor: Open a byte-stream to the specified resource file.
     *
     * @param dname   name of directory that the file is in
     *                (null means the file is located in the editor root dir)
     * @param fname   name of the resource file
     */
    public CFileInputStream(String dname, String fname) throws FileNotFoundException {
        this.filename = fname;
        this.filedir = dname;
        fileInput = null;
        inStream = null;
        stream = null;

        // now try to open the file
        open();
    }

    /**
     * open the resource file for reading
     */
    private void open() throws FileNotFoundException {
        try {
            // first we look if the resource is available as normal binary-file
            // in the specified directory
            fileInput = new FileInputStream(filedir +File.separator+ filename);
            stream = new BufferedInputStream(fileInput);
            is_binfile = true;
        }
        catch(FileNotFoundException e) {
            try {
                // second we look if the resource is available as normal binary-file
                // in the editor's root directory
                fileInput = new FileInputStream(filename);
                stream = new BufferedInputStream(fileInput);
                is_binfile = true;
            }
            catch(FileNotFoundException ex) {
                // if there is no binary file at all, we try to load it from
                // the system-jar-archive:
                inStream = ClassLoader.getSystemResourceAsStream(filename);
                if (inStream != null) {
                    stream = new BufferedInputStream(inStream);
                    is_jar = true;
                }
            }
        }

        if (!is_binfile && !is_jar) {
            System.out.println("File \""+filename+"\" was not found!");
            throw new FileNotFoundException();
        }
    }

    /**
     * @return <code>BufferedInputStream</code> to the resource file
     */
    public BufferedInputStream getBufferedStream() {return stream;}

    /**
     * Read one byte from the stream
     * (Wrapper method for <code>BufferedInputStream.read()</code>)
     * @return one read byte-character
     */
    public int read() throws IOException {return stream.read();}

    /**
     * close all open streams, print errormessages if closing failed
     */
    public void close() {
        if (stream != null) {
            try{stream.close();}catch(IOException e) {
                System.out.println("Couldn't close BufferedInputStream for \""+filename+"\".");
            }
        }
        if (inStream != null) {
            try{inStream.close();}catch(IOException e) {
                System.out.println("Couldn't close InputStream for \""+filename+"\".");
            }
        }
        if (fileInput != null) {
            try{fileInput.close();}catch(IOException e) {
                System.out.println("Couldn't close FileInputStream for \""+filename+"\".");
            }
        }
    }
}
