package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.*;

class MainPanel extends JPanel {
    private final JTextPane textpane = new JTextPane();
    public MainPanel() {
        super(new BorderLayout());
        Font font    = makeFont(getClass().getResource("mona.ttf"));
        Document doc = makeDocument(getClass().getResource("bar.utf8.txt"), "UTF-8");
        if(font!=null) {
            System.out.println(font.toString());
            textpane.setFont(font.deriveFont(10.0f));
            textpane.setDocument(doc);
        }
        add(new JScrollPane(textpane));
        setPreferredSize(new Dimension(320, 240));
    }

    //         TreeMap fontMap = new TreeMap();
    //         fontMap.put(font.getFamily(), font);
    //         StyleContext sc = new StyleContext();
    //         Style style = sc.addStyle("Mona Style", null);
    //         StyleConstants.setFontFamily(style, font.getFamily());
    //         StyleConstants.setFontSize(style, 12);
    //         FontDocument doc = new FontDocument(sc);
    //         doc.setLogicalStyle(0, style);
    //         textpane.setDocument(doc);

//*
    private static Font makeFont(URL url) {
        Font font = null;
        InputStream is = null;
        try{
            is = url.openStream();
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12.0f);
            is.close();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(FontFormatException ffe) {
            ffe.printStackTrace();
        }finally{
            if(is!=null) {
                try{
                    is.close();
                }catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return font;
    }
/*/ // JDK 1.7.0
    private static Font makeFont(URL url) {
        Font font = null;
        try(InputStream is = url.openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12.0f);
        }catch(IOException | FontFormatException ex) {
            ex.printStackTrace();
        }
        return font;
    }
//*/

    private static Document makeDocument(URL url, String encoding) {
        DefaultStyledDocument doc = new DefaultStyledDocument();
        Reader reader = null;
        try{
            reader = new InputStreamReader(url.openStream(), encoding);
            char[] buff = new char[4096];
            int nch;
            while((nch = reader.read(buff, 0, buff.length)) != -1) {
                doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
            }
            reader.close();
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            if(reader!=null) {
                try{
                    reader.close();
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return doc;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
