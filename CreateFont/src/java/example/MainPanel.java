package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextPane textpane = new JTextPane();
    public MainPanel() {
        super(new BorderLayout());

        makeFont(getClass().getResource("mona.ttf")).ifPresent(font -> {
            System.out.println(font.toString());
            textpane.setFont(font.deriveFont(10f));
            //textpane.setDocument(doc);
        });

        URL url = getClass().getResource("bar.utf8.txt");
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            textpane.read(reader, "text");
        } catch (IOException ex) {
            ex.printStackTrace();
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

//     private static Font makeFontOld(URL url) {
//         Font font = null;
//         InputStream is = null;
//         try {
//             is = url.openStream();
//             font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
//             is.close();
//         } catch (IOException | FontFormatException ex) {
//             ex.printStackTrace();
//         } finally {
//             if (is != null) {
//                 try {
//                     is.close();
//                 } catch (IOException ex) {
//                     ex.printStackTrace();
//                 }
//             }
//         }
//         return font;
//     }

    private static Optional<Font> makeFont(URL url) {
        try (InputStream is = url.openStream()) {
            return Optional.of(Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f));
        } catch (IOException | FontFormatException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

//     private static Document makeDocument(URL url, String encoding) {
//         DefaultStyledDocument doc = new DefaultStyledDocument();
//         try (Reader reader = new InputStreamReader(url.openStream(), encoding);
//              Scanner scanner = new Scanner(reader)) {
//             while (scanner.hasNextLine()) {
//                 doc.insertString(doc.getLength(), String.format("%s%n", scanner.nextLine()), null);
//             }
// //             char[] buff = new char[4096];
// //             int nch;
// //             while ((nch = reader.read(buff, 0, buff.length)) != -1) {
// //                 doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
// //             }
//             //reader.close();
//         } catch (IOException | BadLocationException ex) {
//             ex.printStackTrace();
//         }
//         return doc;
//     }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
