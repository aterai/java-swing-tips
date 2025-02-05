// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String html = "<html><h2>H2</h2>text<ul><li>list: %s</li></ul></html>";

    JEditorPane editor0 = makeEditorPane();
    editor0.setText(String.format(html, "Default"));

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/bullet.png");

    JEditorPane editor1 = makeEditorPane();
    EditorKit kit = editor1.getEditorKit();
    if (kit instanceof HTMLEditorKit && url != null) {
      HTMLEditorKit htmlEditorKit = (HTMLEditorKit) kit;
      StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
      styleSheet.addRule(String.format("ul{list-style-image:url(%s);margin:0px 20px;}", url));

      // styleSheet.addRule("ul{list-style-type:circle;margin:0px 20px;}");
      // styleSheet.addRule("ul{list-style-type:disc;margin:0px 20px;}");
      // styleSheet.addRule("ul{list-style-type:square;margin:0px 20px;}");
      // styleSheet.addRule("ul{list-style-type:decimal;margin:0px 20px;}");

      // Pseudo-element is not supported in javax.swing.text.html.CSS
      // styleSheet.addRule("ul{list-style-type:none;margin:0px 20px;}");
      // styleSheet.addRule("ul li:before{content: "\u00BB";}");
    }
    editor1.setText(String.format(html, "bullet.png"));

    add(new JScrollPane(editor0));
    add(new JScrollPane(editor1));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane() {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);
    return editorPane;
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
