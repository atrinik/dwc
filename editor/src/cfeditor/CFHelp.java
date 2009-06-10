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

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import javax.swing.text.*;
import javax.swing.event.*;

/**
 * <code>CFHelp</code> implements the Help Window is a seperate frame with html content.
 *
 * @author <a href="mailto:michael.toennies@nord-com.net">Michael Toennies</a>
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class CFHelp extends JFrame {

    /**
     * Konstructor
     *
     * @param main_view    the main view
     * @param big_size     if true, the frame gets bigger than normal
     *                     (useful in combination with larger fonts)
     * @param fname        may contain different things:
     *                     1. null -> the file "start.html" is opened
     *                     2. File name of a html-file to be opened
     *                     3. html-text to be displayed directly (no file)
     *                        (this text must start with "<HTML>")
     */
    public CFHelp(JFrame main_view, String fname, boolean big_size) {
        super("Help");    // super constructor

        // set application icon
        setIconImage(CGUIUtils.getIcon(IGUIConstants.APP_ICON).getImage());

        Rectangle mvb = main_view.getBounds(); // get main view bounds
        if (!big_size)
            setBounds(mvb.x + (mvb.width)/2 - 260, mvb.y + 70, 520, 600); // standard
        else
            setBounds(mvb.x + (mvb.width)/2 - 300, mvb.y + 70, 600, 750); // big size

        if (fname == null) fname = "start.html";
        HtmlPane html;
        if (fname.startsWith("<HTML>"))
            html = new HtmlPane("text/html", fname); // direct text
        else
            html = new HtmlPane(fname);  // read html file

        setContentPane(html);
    }

}

class HtmlPane extends JScrollPane implements HyperlinkListener {
    JEditorPane html;

    /**
     * Konstructor to load the html-file <fname> and display
     * it's contents in this HtmlPane
     *
     * @param fname     Name of the hmtl-file
     */
    public HtmlPane(String fname) {
        try {
            // first looking for the html file in extracted form
            File f = new File(IGUIConstants.HELP_DIR +File.separator+ fname);
            if (f.exists()) {
                // file exists in expected directory
                String s = f.getAbsolutePath();
                s = "file:"+s;
                html = new JEditorPane(s);
            }
            else {
                // file missing, so let's look if we can get it from the jar
                URL url = ClassLoader.getSystemResource(IGUIConstants.HELP_DIR.replace('\\', '/') +"/"+ fname);

                if (url != null) {
                    html = new JEditorPane(url);
                }
                else{
                    // let's try it again without first directory
                    System.out.println("trying: HelpFiles/"+fname);
                    url = ClassLoader.getSystemResource("HelpFiles/"+fname);
                    if (url != null) {
                        html = new JEditorPane(url);
                    }
                    else
                        System.out.println("Failed to open help file '"+fname+"'!");
                }
            }

            html.setEditable(false);
            html.addHyperlinkListener(this);
            JViewport vp = getViewport();

            // under windows, the content of the panel get detroyed after scrolling
            // this will avoid this problem!
            // but it will make the scrolling slower
            // so test this!
            this.getViewport().putClientProperty("EnableWindowBlit", new Boolean(true));
            vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

            vp.add(html);
            setAutoscrolls(true);
        } catch (NullPointerException e) {
            // failed to open the html file
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    /**
     * Konstructor to load the html-file <fname> and display
     * it's contents in this HtmlPane
     *
     * @param type      mime-type of the given text (e.g. "text/html")
     * @param text      text to display (can be html-text for example)
     */
    public HtmlPane(String type, String text) {
            // open new JEditorPane
            html = new JEditorPane(type, text);
            html.setEditable(false);
            html.addHyperlinkListener(this);
            JViewport vp = getViewport();

            // under windows, the content of the panel get detroyed after scrolling
            // this will avoid this problem!
            // but it will make the scrolling slower
            // so test this!
            this.getViewport().putClientProperty("EnableWindowBlit", new Boolean(true));
            vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE );

            vp.add(html);
            //vp.setView(html);
            //vp.setViewPosition(new Point(0, 0));

            setAutoscrolls(true);
    }

    /**
     * Notification of a change relative to a
     * hyperlink.
     * @param e     occurred <code>HyperlinkEvent</code>
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            linkActivated(e.getURL());
        }
    }

    /**
     * Follows the reference in an
     * link.  The given url is the requested reference.
     * By default this calls <a href="#setPage">setPage</a>,
     * and if an exception is thrown the original previous
     * document is restored and a beep sounded.  If an
     * attempt was made to follow a link, but it represented
     * a malformed url, this method will be called with a
     * null argument.
     *
     * @param u the URL to follow
     */
    protected void linkActivated(URL u) {
        Cursor c = html.getCursor();
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        html.setCursor(waitCursor);
        SwingUtilities.invokeLater(new PageLoader(u, c));
    }

    /**
     * temporary class that loads synchronously (although
     * later than the request so that a cursor change
     * can be done).
     */
    class PageLoader implements Runnable {

        PageLoader(URL u, Cursor c) {
            url = u;
            cursor = c;
        }

        public void run() {
            if (url == null) {
                // restore the original cursor
                html.setCursor(cursor);

                // PENDING(prinz) remove this hack when
                // automatic validation is activated.
                Container parent = html.getParent();
                parent.repaint();
            } else {
                Document doc = html.getDocument();
                try {
                    html.setPage(url);
                } catch (IOException ioe) {
                    html.setDocument(doc);
                    getToolkit().beep();
                } finally {
                    // schedule the cursor to revert after
                    // the paint has happended.
                    url = null;
                    SwingUtilities.invokeLater(this);
                }
            }
        }

        URL url;
        Cursor cursor;
    }

}
