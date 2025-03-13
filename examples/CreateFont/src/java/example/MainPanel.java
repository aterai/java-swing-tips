// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextPane textPane = new JTextPane();

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    makeFont(cl.getResource("example/mona.ttf"))
        .ifPresent(font -> textPane.setFont(font.deriveFont(10f)));

    Optional.ofNullable(cl.getResource("example/bar.utf8.txt"))
        .ifPresent(url -> {
          try {
            // try (Reader br = new InputStreamReader(url.openStream(), UTF_8)) {
            try (Reader br = Files.newBufferedReader(Paths.get(url.toURI()))) {
              textPane.read(br, "text");
            }
          } catch (IOException | URISyntaxException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(textPane);
            textPane.setText(ex.getMessage());
          }
        });

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
    Optional<Font> op;
    if (url == null) {
      op = Optional.empty();
    } else {
      try (InputStream is = url.openStream()) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, is);
        op = Optional.of(font.deriveFont(12f));
      } catch (IOException | FontFormatException ex) {
        op = Optional.empty();
      }
    }
    return op;
  }

  // private static Optional<Font> makeFont(URL url) {
  //   return Optional.ofNullable(url)
  //       .map(u -> {
  //         Font font;
  //         try (InputStream is = url.openStream()) {
  //           font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
  //         } catch (IOException | FontFormatException ex) {
  //           font = null;
  //         }
  //         return font;
  //       });
  // }

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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
