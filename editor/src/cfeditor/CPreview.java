/*
 * Crossfire Java Editor.
 * Copyright (C) 2000  Michael Toennies
 * Copyright (C) 2001  Andreas Vogl
 * Copyright (C) 2004  Peter Plischewsky
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
import java.awt.event.*;

import cfeditor.IGUIConstants;
import com.visualtek.png.*; // visualtek PNGEncoder
/**
 * The map preview of the level editor. This allows the user to zoom in and out
 * of the map.
 * @author <a href="mailto:plischewsky@hotmail.com">Peter Plischewsky</a>
 */
public class CPreview{
  //The Frame for the zoom program
  private static JFrame p_frame = new JFrame ("Daimonin Map Previewer");
//The container for the frame
  private Container content;
  //toolkit just saves time when coding
  private Toolkit toolkit = Toolkit.getDefaultToolkit();
  //The tabbed-pane for the window
  private JTabbedPane tabbedPane;
  //This ImagePanel is how the map is put onto the screen
  private static ImagePanel previewArea;
  //This is where the final Image for the maps are stored
  private static Image mapImage, oldImage,map25,map50,map100,map200,map300;
  private CMainControl m_control;
  //Window properties:
  public static int defwidth, defheight;
  // JFrame UI Setup:
  private JMenuBar mb = new JMenuBar();
  private JMenu file = new JMenu("File");
  private JMenuItem save = new JMenuItem("Save");
  private JMenuItem quit = new JMenuItem("Quit");
  private JMenu zoom = new JMenu("Zoom");
  private JMenuItem zoom25 = new JMenuItem ("Zoom -400%");
  private JMenuItem zoom50 = new JMenuItem ("Zoom -200%");
  private JMenuItem zoom100 = new JMenuItem("  Normal  ");
  private JMenuItem zoom200 = new JMenuItem("Zoom +200%");
  private JMenuItem zoom300 = new JMenuItem("Zoom +300%");

  private int oldWidth,oldHeight;
  // Mouse co-ordinates
  public static int X = 0,Y = 0;
  // Mouse listener for the preview frame:
  private PListener listen = new PListener();
  // Sets up the screen controller
  public static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
  /**
   * Constructs the CPreview function. When m_Image is not null
   * it either increases or decreases the zoom. If m_Image is null
   * the the program will wait for a zoom factor to be chosen.
   */
  public CPreview(CMainControl m_control, Image getImage) {
    //Everything in this function initializes the frame and some variables
    this.m_control = m_control;
    // sets the icon for the frame
    ImageIcon icon = CGUIUtils.getIcon( IGUIConstants.APP_ICON );
    if (icon != null) {
      p_frame.setIconImage(icon.getImage() );
    }
    // adds a window listener and frees up some memory when frame is closed
    p_frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
      mapImage = null;
      System.gc();
      }
    });
    // Image from main control is given to the Image in this class
    oldImage = getImage;
    // Image Panel is given an image to work with
    previewArea = new ImagePanel(oldImage);
    // More setting up of the User Interface
    tabbedPane = new JTabbedPane(SwingConstants.LEFT);
    tabbedPane.setBackground(Color.blue);
    tabbedPane.setForeground(Color.white);
    content = p_frame.getContentPane();
    // gets the screen size
    defwidth = (int)(screen.getWidth());
    defheight = (int)(screen.getHeight());
    // buildUI is called to build the rest of the User Interface
    buildUI();
    // The frame windows settings are finalized
    p_frame.getContentPane().add(tabbedPane);
    p_frame.setBackground(Color.black);
    p_frame.setSize(defwidth,defheight);
    p_frame.setLocationRelativeTo (null);
    JOptionPane.showMessageDialog(p_frame, "The Viewing screen must be maximized for proper display.");
    // Tell the previewer to be visible.
    p_frame.setVisible(true);
    // Gets the map info
    getMapInfo();
    // Displays the map for the first time
    mapImage = oldImage;
    updateMap();
    // Adds the mouse listener to the frame:
    p_frame.addMouseListener(listen);
  }

  /**
   * This builds the user interface which will allow
   * the user to control the zoom.
   */
  public void buildUI()
  {
   /* These are not stable just yet..
     file.add(save);
    save.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
       try{
         saveImage(m_control.m_currentMap.getMapFileName());
       }
       catch(IOException err){
         JOptionPane.showMessageDialog(p_frame, "Could not save map image!","Error"
                                    ,JOptionPane.ERROR_MESSAGE);
           System.out.println("PNGException: "+err.getMessage());
       }
      } // Closes actionPerformed method
     }); // Closes addActionListener method

    file.add(quit);
    quit.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        p_frame.dispose();
      } // Closes actionPerformed method
     }); // Closes addActionListener method
    mb.add(file);
    */
    zoom25.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        X = 0;
        Y = 0;
        mapImage = map25;
        updateMap();
      } // Closes actionPerformed method
     }); // Closes addActionListener method
    zoom.add(zoom25);
    zoom50.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        X = 0;
        Y = 0;
        mapImage = map50;
        updateMap();
      } // Closes actionPerformed method
    }); // Closes addActionListener method
    zoom.add(zoom50);
    zoom.addSeparator();
    zoom100.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        X = 0;
        Y = 0;
        mapImage = map100;
        updateMap();
      } // Closes actionPerformed method
    }); // Closes addActionListener method
    zoom.add(zoom100);
    zoom.addSeparator();
    zoom200.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        X = 0;
        Y = 0;
        mapImage = map200;
        updateMap();
      } // Closes actionPerformed method
    }); // Closes addActionListener method
    zoom.add(zoom200);
    zoom300.addActionListener(new ActionListener()
    { // Opens addActionListener method
      public void actionPerformed(ActionEvent e)
      { // Opens actionPerformed method
        X = 0;
        Y = 0;
        mapImage = map300;
        updateMap();
      } // Closes actionPerformed method
    }); // Closes addActionListener method
    //zoom.add(zoom300);
    if (defwidth <=900 || defheight <= 700)
    {
      zoom300.setEnabled(false);
    }
    mb.add(zoom);
    p_frame.setJMenuBar(mb);
  }

  /**
   * This gets the current image being used.
   *
   */
  public void getMapInfo()
  {
    // Map Width:
    oldWidth = (oldImage.getWidth(null));
    // Map Height:
    oldHeight = (oldImage.getHeight(null));
    // Map Images with different scales
    map25 = oldImage.getScaledInstance(oldWidth/4, oldHeight/4, 0);
    map50 = oldImage.getScaledInstance(oldWidth/2, oldHeight/2, 0);
    map100 = oldImage.getScaledInstance(oldWidth, oldHeight, 0);
    map200 = oldImage.getScaledInstance(oldWidth*2, oldHeight*2, 0);
    map300 = oldImage.getScaledInstance(oldWidth*3, oldHeight*3, 0);
  }

  /**
   * This rescales the image to the appropriate zoom factor.
   * It uses the original map to rescale. (prevents blur and
   * abuse by the user)
   */

  public static void updateMap()
  {
    previewArea.setLoc(X,Y);
    previewArea.setImage(mapImage);
    p_frame.setContentPane(previewArea);
  }

  /**
    * @return an image of the entire mapview (Disabled)
    */
   public void saveImage(String filename) throws java.io.IOException {
       try {
           // create instance of PNGencoder:
           PNGEncoder pngEnc = new PNGEncoder(mapImage, filename);
           pngEnc.encode(); // encode image -> create file
       }
       catch (PNGException e) {
          JOptionPane.showMessageDialog(p_frame, "Could not save map image!","Error"
                                    ,JOptionPane.ERROR_MESSAGE);
           System.out.println("PNGException: "+e.getMessage());
       }
   }

}
/**
 * The mouse listener for the daimonin map zoom previewer
 */
class PListener implements MouseListener{
  private int X,Y;
  public void mouseClicked (MouseEvent e){
    X = e.getX();
    Y = e.getY();
    // UP-RIGHT
    if (X <= CPreview.defwidth / 2 && Y <= CPreview.defheight / 2){
      CPreview.X = CPreview.X - 30;
      CPreview.Y = CPreview.Y - 30;
    }
    // DOWN-RIGHT
    else if (X <= CPreview.defwidth / 2 && Y >= CPreview.defheight / 2){
      CPreview.X = CPreview.X - 30;
      CPreview.Y = CPreview.Y + 30;
    }
    // UP-LEFT
    else if (X >= CPreview.defwidth / 2 && Y <= CPreview.defheight / 2){
      CPreview.X = CPreview.X + 30;
      CPreview.Y = CPreview.Y - 30;
    }
    // DOWN-LEFT
    else if (X >= CPreview.defwidth / 2 && Y >= CPreview.defheight / 2){
      CPreview.X = CPreview.X + 30;
      CPreview.Y = CPreview.Y + 30;
    }
    CPreview.updateMap();

  }
  public void mouseEntered (MouseEvent e){
  }
  public void mouseExited (MouseEvent e){
  }
  public void mousePressed(MouseEvent e){
  }
  public void mouseReleased(MouseEvent e){
  }
}

/**
 * This class is used to output the transformend map onto the screen
 * it is used only by the CPreview class.
 */
class ImagePanel extends JPanel{
        private Image map;
        private int X,Y;
        public ImagePanel(Image img)
        {
                super();
                map = img;
                setLayout(new BorderLayout());
                setBackground(Color.white);
         }
        public void setLoc(int x, int y)
        {
          X = x;
          Y = y;
        }
        public void setImage(Image img)
        {
                map = img;
                repaint();
        }
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.drawImage(map, X, Y, this);
            g.setColor(Color.red);
            g.drawLine(0, (int) (CPreview.screen.getHeight() / 2) - 50,
                       (int) (CPreview.screen.getWidth()),
                       (int) (CPreview.screen.getHeight() / 2) - 50);
            g.drawLine( (int) (CPreview.screen.getWidth()) / 2, 0,
                        (int) (CPreview.screen.getWidth()) / 2,
                        (int) (CPreview.screen.getHeight()));
          }
      }
