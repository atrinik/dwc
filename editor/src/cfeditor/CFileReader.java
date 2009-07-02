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
 * Class for opening and reading a buffered stream to an ASCII resource-file.
 * If no such file is found, the file is loaded from the jar archive.
 *
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFileReader {
    private String filename;           // name of the resource file
    private String filedir;            // directory of the resource file
    private boolean is_ascii = false;  // true if the resource was opened as ascii file
    private boolean is_jar = false;    // true if resource was opened from jar archive

    // streams
    private FileReader fileRead;
    private InputStream inStream;
    private InputStreamReader streamRead;
    private BufferedReader read;

    /**
     * Constructor: Open an ascii-stream to the specified resource file.
     *
     * @param dname   name of directory that the file is in
     *                (null means the file is located in the editor root dir)
     * @param fname   name of the resource file
     */
    public CFileReader(String dname, String fname) throws FileNotFoundException {
        this.filename = fname;
        this.filedir = dname;
        fileRead = null;
        inStream = null;
        streamRead = null;
        read = null;

        // now try to open the file
        open();
    }

    /**
     * open the resource file for reading
     */
    private void open() throws FileNotFoundException {
        try {
            // first we look if the resource is available as normal ascii-file
            // in the specified directory
            fileRead = new FileReader(filedir +File.separator+ filename);
            is_ascii = true;
        }
        catch(FileNotFoundException e) {
            try {
                // second we look if the resource is available as normal ascii-file
                // in the editor's root directory
                fileRead = new FileReader(filename);
                is_ascii = true;
            }
            catch(FileNotFoundException ex) {
                // if there is no ascii file at all, we try to load it from
                // the system-jar-archive:
                inStream = ClassLoader.getSystemResourceAsStream(filename);
                if (inStream != null) {
                    streamRead = new InputStreamReader(inStream);
                    is_jar = true;
                }
            }
        }

        if (is_ascii) {
            read = new BufferedReader(fileRead);
        }
        else if (is_jar) {
            read = new BufferedReader(streamRead);
        }
        else {
            System.out.println("File \""+filename+"\" was not found!");
            throw new FileNotFoundException();
        }
    }

    /**
     * @return <code>BufferedReader</code> to the resource file
     */
    public BufferedReader getReader() {return read;}

    /**
     * read_till - reads characters from the BufferedReader stream
     * till 'tag' is found. If found, the method returns with
     * stream pointing right after the appearance of 'tag'.
     *
     * @param stream          ascii input stream to read from
     * @param tag             stop reading at the string 'tag'
     * @param abort           throw <code>EOFException</code> at string 'abort' (this can be null)
     * @throws IOException    an I/O-error occurred while reading the file
     * @throws EOFException   the end of file was reached, or the 'abort' string
     *                        has been encountered
     */
    public static void read_till(BufferedReader stream, String tag, String abort)
                                 throws IOException, EOFException {
        int c;     // character value, read from the stream
        int t=0;   // tag index
        int a=0;   // abort index

        if (abort != null) {
            // look both for 'tag' and 'abort'
            do {
                c = stream.read();  // read one character
                if ((char)c == tag.charAt(t))
                    t++;
                else
                    t=0;

                if ((char)c == abort.charAt(a))
                    a++;
                else
                    a=0;
            } while (t < tag.length() && a < abort.length() && c != -1);
        }
        else {
            // look only for 'tag'
            do {
                c = stream.read();  // read one character
                if ((char)c == tag.charAt(t))
                    t++;
                else
                    t=0;
            } while (t < tag.length() && c != -1);
        }

        // if we did not find the tag, an EOFException is thrown
        if (c == -1) throw new EOFException();

        // if we found the string 'abort', throw EOFException as well
        if (abort != null && a == abort.length()) throw new EOFException();
    }

    /**
     * reads_till - reads characters from the BufferedReader stream
     * till 'tag' is found. Similar to read_till(), except that the
     * read String is returned. 'tag' is not included in the returned
     * String.
     *
     * @param stream           ascii input stream to read from
     * @param tag              stop reading at the string 'tag'
     * @return                 the string between the starting pos. of 'stream'
     *                         (inclusive) and the first character of 'tag' (exclusive).
     * @throws IOException     an I/O-error occurred while reading the file
     * @throws EOFException    the end of file was reached
     */
    public static String reads_till(BufferedReader stream, String tag)
                                    throws IOException, EOFException {
        String r = ""; // returned string
        int c;         // character value, read from the stream
        int t=0;       // index

        long count = 0;        // counter (to realize when shooting past EOF)
        long maxCount = 10000; // bail out when counter exceeds this value

        do {
            c = stream.read();  // read one character
            r = r + String.valueOf((char)c); // add character 'c' to the string
            if ((char)c == tag.charAt(t))
                t++;
            else
                t=0;
        } while (t < tag.length() && c != -1 && count++ < maxCount);

        // if we did not find the tag, an EOFException is thrown
        if (c == -1 || count++ >= maxCount)
            throw new EOFException();
        else {
            // cut 'tag' off, at the end of the string
            r = r.substring(0, r.length() - tag.length());
        }

        return r;
    }

    // wrapper method for read_till, using BufferReader from 'this'
    public void read_till(String tag, String abort) throws IOException, EOFException {
        read_till(read, tag, abort);
    }

    // wrapper method for reads_till, using BufferReader from 'this'
    public String reads_till(String tag) throws IOException, EOFException {
        return reads_till(read, tag);
    }

    /**
     * close all open streams, print errormessages if closing failed
     */
    public void close() {
        if (read != null) {
            try{read.close();}catch(IOException e) {
                System.out.println("Couldn't close BufferedReader for \""+filename+"\".");
            }
        }
        if (fileRead != null) {
            try{fileRead.close();}catch(IOException e) {
                System.out.println("Couldn't close FileReader for \""+filename+"\".");
            }
        }
        if (streamRead != null) {
            try{streamRead.close();}catch(IOException e) {
                System.out.println("Couldn't close InputStreamReader for \""+filename+"\".");
            }
        }
        if (inStream != null) {
            try{inStream.close();}catch(IOException e) {
                System.out.println("Couldn't close InputStream for \""+filename+"\".");
            }
        }
    }
}
