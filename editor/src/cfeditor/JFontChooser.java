/* JFontChooser.java
 *
 * Copyright (C) 1998-2001, The University of Sheffield.
 * Copyright (C) 2002, Andreas Vogl
 *
 * This file is based on a part of GATE (see http://gate.ac.uk/), which is
 * free software, licenced under the GNU Library General Public License,
 * Version 2, June 1991 (in the distribution as file licence.html,
 * and also available at http://gate.ac.uk/gate/licence.html).
 *
 * Valentin Tablan 06/04/2001
 *
 * $Id: JFontChooser.java,v 1.2 2004/11/18 22:58:38 avogl Exp $
 *
 */
package cfeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.*;

/**
 * Class for choosing Fonts
 *
 * @author University of Sheffield
 * @author <a href="mailto:andi.vogl@gmx.net">Andreas Vogl</a>
 */
public class JFontChooser extends JPanel {
    public static final Font default_font = new Font("Default", Font.PLAIN, 12);

    // GUI components:
    JComboBox familyCombo;
    JCheckBox italicChk;
    JCheckBox boldChk;
    JComboBox sizeCombo;
    JTextArea sampleTextArea;

    private Font fontValue;

    public JFontChooser(){
      this(UIManager.getFont("Button.font"));
    }

    public JFontChooser(Font initialFont){
      initLocalData();
      initGuiComponents(initialFont);
      initListeners();
      setFontValue(initialFont);
    }// public JFontChooser(Font initialFont)

    /**
     * Show Fontdialog, let user choose a font, then close and return chosen font.<br>
     * This is a static method. Use it like:<br><br>
     * <code>
     *   Font f = JFontChooser.showDialog(parent, "title", defaultFont);
     * </code>
     *
     * @param parent        parent Component
     * @param title         frame title
     * @param initialfont   initial/default font
     * @return              new font that the user has chosen
     */
    public static Font showDialog(Component parent, String title,
                                  Font initialfont){
        Window windowParent;
        if(parent instanceof Window) windowParent = (Window)parent;
        else windowParent = SwingUtilities.getWindowAncestor(parent);
        if(windowParent == null) throw new IllegalArgumentException(
            "The supplied parent component has no window ancestor");
        final JDialog dialog;

        if(windowParent instanceof Frame)
            dialog = new JDialog((Frame)windowParent, title, true);
        else
            dialog = new JDialog((Dialog)windowParent, title, true);

        dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(),
                                          BoxLayout.Y_AXIS));

        final JFontChooser fontChooser = new JFontChooser(initialfont);
        dialog.getContentPane().add(fontChooser);

        // buttons
        JButton okBtn = new JButton("OK");
        JButton defaultBtn = new JButton("Default");
        JPanel buttonsBox = new JPanel();
        buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
        buttonsBox.add(Box.createHorizontalGlue());
        buttonsBox.add(defaultBtn);
        buttonsBox.add(Box.createHorizontalStrut(30));
        buttonsBox.add(okBtn);
        buttonsBox.add(Box.createHorizontalGlue());
        buttonsBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        dialog.getContentPane().add(buttonsBox);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // actionlistener for the font choose-box
        fontChooser.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                dialog.pack();
            }
        });

        // actionlistener for the ok-button
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false); // dialog remains hidden till next time needed
            }
        });

        // actionlistener for the cancel-button
        defaultBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                fontChooser.setFontValue(null);
            }
        });

        dialog.setVisible(true);

        return fontChooser.getFontValue();
    } // showDialog

    protected void initLocalData() {
    }

    /**
     * initialize the GUI components
     */
    protected void initGuiComponents(Font initfont) {
        // container panel, containing the whole GUI
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        familyCombo = new JComboBox(
                          GraphicsEnvironment.getLocalGraphicsEnvironment().
                          getAvailableFontFamilyNames()
                        );
        familyCombo.setSelectedItem(UIManager.getFont("Label.font").getFamily());

        sizeCombo = new JComboBox(new String[]{"6", "8", "10", "12", "14", "16",
                                               "18", "20", "22", "24", "26"});
        sizeCombo.setSelectedItem(new Integer(
                          UIManager.getFont("Label.font").getSize()).toString());

        italicChk = new JCheckBox("<html><i>Italic</i></html>", false);
        boldChk = new JCheckBox("<html><b>Bold</b></html>", false);

        // font-box panel
        JPanel fontBox = new JPanel();
        fontBox.setLayout(new BoxLayout(fontBox, BoxLayout.X_AXIS));
        fontBox.add(familyCombo);
        fontBox.add(sizeCombo);
        fontBox.setBorder(BorderFactory.createTitledBorder(" Font "));
        panel.add(fontBox);
        panel.add(Box.createVerticalStrut(10));

        // bold/italic panel
        JPanel effectsBox = new JPanel();
        effectsBox.setLayout(new BoxLayout(effectsBox, BoxLayout.X_AXIS));
        effectsBox.add(italicChk);
        effectsBox.add(boldChk);
        effectsBox.setBorder(BorderFactory.createTitledBorder(" Effects "));
        panel.add(effectsBox);

        // sample panel
        sampleTextArea = new JTextArea("Type your sample here...");
        JPanel samplePanel = new JPanel(new GridLayout(1, 1));

        samplePanel.add(sampleTextArea);

        samplePanel.setBorder(BorderFactory.createTitledBorder(" Sample "));
        panel.add(samplePanel);
        panel.add(Box.createVerticalStrut(10));

        // add a small border around the whole thing
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        add(panel);
    }// initGuiComponents()

    /**
     * initialize the listeners
     */
    protected void initListeners(){
        familyCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont();
            }
        });

        sizeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont();
            }
        });

        boldChk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont();
            }
        });

        italicChk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFont();
            }
        });
    }

    protected void updateFont(){
      Map fontAttrs = new HashMap();
      fontAttrs.put(TextAttribute.FAMILY, (String)familyCombo.getSelectedItem());
      fontAttrs.put(TextAttribute.SIZE, new Float((String)sizeCombo.getSelectedItem()));

      if(boldChk.isSelected())
        fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
      else fontAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);

      if(italicChk.isSelected())
        fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
      else fontAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);

      Font newFont = new Font(fontAttrs);
      Font oldFont = fontValue;
      fontValue = newFont;
      sampleTextArea.setFont(newFont);
      String text = sampleTextArea.getText();
      sampleTextArea.setText("");
      sampleTextArea.setText(text);
      sampleTextArea.repaint(100);

      firePropertyChange("fontValue", oldFont, newFont);
    }//updateFont()

    /**
     * Test code
     */
    public static void main(String args[]){
      try{
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }catch(Exception e){
        e.printStackTrace();
      }
      final JFrame frame = new JFrame("Foo frame");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      JButton btn = new JButton("Show dialog");
      btn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.out.println(showDialog(frame, "Fonter",
                                        UIManager.getFont("Button.font")));
        }
      });
      frame.getContentPane().add(btn);
      frame.setSize(new Dimension(300, 300));
      frame.setVisible(true);
      System.out.println("Font: " + UIManager.getFont("Button.font"));
      showDialog(frame, "Fonter", UIManager.getFont("Button.font"));
    }// main

    /**
     * Set selected font
     * @param newfontValue    new font
     */
    public void setFontValue(java.awt.Font newfontValue) {
        if (newfontValue != null) {
            boldChk.setSelected(newfontValue.isBold());
            italicChk.setSelected(newfontValue.isItalic());
            familyCombo.setSelectedItem(newfontValue.getName());
            sizeCombo.setSelectedItem(Integer.toString(newfontValue.getSize()));
        }
        this.fontValue = newfontValue;
    }

    /**
     * convert fontsize into the htmal size which is most appropriate
     */
    public static int getHtmlSize(int fontsize) {
        if (fontsize <= 11)
            return 1;
        else if (fontsize <= 13)
            return 2;
        else if (fontsize <= 17)
            return 3;
        else if (fontsize <= 21)
            return 4;
        return 5;
    }

    /**
     * Convert a text-string message into a message with html (font)
     * tags to display in message-window-popups.
     * (This is currently not used.)
     *
     * @param msg    the ascii message text with '\n' linebreaks
     * @param f      the custom font used, or null for default
     * @return       the new message with html tags
     */
    public static String MsgToHtml(String msg, Font f) {
        if (f == null) return msg; // default font needs no tags to display

        int t; // temp. value
        while ((t = msg.indexOf("<")) >= 0) {
            msg = msg.substring(0, t)+"&lt;"+msg.substring(t+1);
        }
        while ((t = msg.indexOf(">")) >= 0) {
            msg = msg.substring(0, t)+"&gt;"+msg.substring(t+1);
        }

        String new_msg="";  // return value: new message with html tags
        while (msg.indexOf("\n") >= 0) {
            new_msg = new_msg + "<html><font color=black size=\""+getHtmlSize(f.getSize())+
                      "\" face=\""+f.getFontName()+"\"><B>"+msg.substring(0, msg.indexOf("\n"))+"</B></font></html>§";
            msg = msg.substring(msg.indexOf("\n")+1);
        }
        new_msg = new_msg + "<html><font color=black size=\""+getHtmlSize(f.getSize())+
                  "\" face=\""+f.getFontName()+"\"><B>"+msg+"</B></font></html>";

        new_msg = new_msg.replace('§', '\n');
        return new_msg;
    }

    /**
     * Set the default font for all Swing components.
     *
     */
    public static void setUIFont (Font new_font){
        javax.swing.plaf.FontUIResource plainf = new javax.swing.plaf.FontUIResource(
                    new_font.getFontName(), Font.PLAIN, new_font.getSize());
        javax.swing.plaf.FontUIResource boldf = new javax.swing.plaf.FontUIResource(
                    new_font.getFontName(), Font.BOLD, new_font.getSize());

        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                if(((javax.swing.plaf.FontUIResource)value).getStyle() == Font.PLAIN)
                    UIManager.put (key, plainf);
                else if (((javax.swing.plaf.FontUIResource)value).getStyle() == Font.BOLD)
                    UIManager.put (key, boldf);
            }
        }
    }

    public java.awt.Font getFontValue() {
        return fontValue;
    }
}
