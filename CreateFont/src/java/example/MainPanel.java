package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextPane textPane = new JTextPane();

    makeFont(getClass().getResource("mona.ttf")).ifPresent(font -> {
      System.out.println(font.toString());
      textPane.setFont(font.deriveFont(10f));
      // textPane.setDocument(doc);
    });

    URL url = getClass().getResource("bar.utf8.txt");
    try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
      textPane.read(reader, "text");
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    // TreeMap fontMap = new TreeMap();
    // fontMap.put(font.getFamily(), font);
    // StyleContext sc = new StyleContext();
    // Style style = sc.addStyle("Mona Style", null);
    // StyleConstants.setFontFamily(style, font.getFamily());
    // StyleConstants.setFontSize(style, 12);
    // FontDocument doc = new FontDocument(sc);
    // doc.setLogicalStyle(0, style);
    // textPane.setDocument(doc);

    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }
  // private static Font makeFontOld(URL url) {
  //   Font font = null;
  //   InputStream is = null;
  //   try {
  //     is = url.openStream();
  //     font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
  //     is.close();
  //   } catch (IOException | FontFormatException ex) {
  //     ex.printStackTrace();
  //   } finally {
  //     if (is != null) {
  //       try {
  //         is.close();
  //       } catch (IOException ex) {
  //         ex.printStackTrace();
  //       }
  //     }
  //   }
  //   return font;
  // }

  private static Optional<Font> makeFont(URL url) {
    try (InputStream is = url.openStream()) {
      return Optional.of(Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f));
    } catch (IOException | FontFormatException ex) {
      ex.printStackTrace();
    }
    return Optional.empty();
  }
  // private static Document makeDocument(URL url, String encoding) {
  //   DefaultStyledDocument doc = new DefaultStyledDocument();
  //   try (Reader reader = new InputStreamReader(url.openStream(), encoding);
  //      Scanner scanner = new Scanner(reader)) {
  //     while (scanner.hasNextLine()) {
  //       doc.insertString(doc.getLength(), String.format("%s%n", scanner.nextLine()), null);
  //     }
  //     // char[] buff = new char[4096];
  //     // int nch;
  //     // while ((nch = reader.read(buff, 0, buff.length)) != -1) {
  //     //   doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
  //     // }
  //     // reader.close();
  //   } catch (IOException | BadLocationException ex) {
  //     ex.printStackTrace();
  //   }
  //   return doc;
  // }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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
