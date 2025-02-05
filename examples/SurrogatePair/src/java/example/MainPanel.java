// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  private MainPanel() {
    super(new BorderLayout());
    StyleSheet styleSheet = new StyleSheet();
    // styleSheet.addRule("body {font-size: 24pt; font-family: IPAexGothic;}");
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    JEditorPane editor1 = new JEditorPane();
    editor1.setEditorKit(htmlEditorKit);

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/SurrogatePair.html");
    if (url == null) {
      editor1.setText(makeTestHtml());
    } else {
      try (Reader r = Files.newBufferedReader(Paths.get(url.toURI()))) {
        editor1.read(r, "html");
      } catch (IOException | URISyntaxException ex) {
        editor1.setText(makeTestHtml());
      }
    }

    JEditorPane editor2 = new JEditorPane();
    // editor2.setFont(new Font("IPAexGothic", Font.PLAIN, 24));
    editor2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor2.setText("(\uD85B\uDE40) (\u26E40)\n(\uD842\uDF9F) (\u20B9F)");
    // editor2.setText("(𦹀) (𦹀)\n(𠮟) (𠮟)");

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(makeTitledPanel("Numeric character reference", editor1));
    p.add(makeTitledPanel("Unicode escapes", editor2));

    JButton button = new JButton("browse: SurrogatePair.html");
    button.addActionListener(e -> browseCacheFile(url));

    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private String makeTestHtml() {
    String s1 = "&#xD85B;&#xDE40;";
    String s2 = "&#x26E40;";
    String s3 = "&#xD842;&#xDF9F;";
    String s4 = "&#x00020B9F;";
    return String.format("<html><p>(%s) (%s)<br/>(%s) (%s)", s1, s2, s3, s4);
  }

  private static void browseCacheFile(URL url) {
    if (Desktop.isDesktopSupported()) {
      try (InputStream in = new BufferedInputStream(url.openStream())) {
        Path path = Files.createTempFile("_tmp", ".html");
        path.toFile().deleteOnExit();
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        Desktop.getDesktop().browse(path.toUri());
      } catch (IOException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
      // try {
      //   File tmp = File.createTempFile("_tmp", ".html");
      //   tmp.deleteOnExit();
      //   try (InputStream in = new BufferedInputStream(url.openStream());
      //      OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp))) {
      //     byte buf[] = new byte[256];
      //     int len;
      //     while ((len = in.read(buf)) != -1) {
      //       out.write(buf, 0, len);
      //     }
      //     out.flush();
      //     Desktop.getDesktop().browse(tmp.toURI());
      //   }
      // } catch (IOException ex) {
      //   ex.printStackTrace();
      // }
    }
  }

  private static Component makeTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
  }

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
