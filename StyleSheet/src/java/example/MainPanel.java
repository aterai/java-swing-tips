package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule("body {font-size: 12pt;}");
    styleSheet.addRule(".highlight {color: red; background: green}");
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    // HTMLDocument htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    // editor.setDocument(htmlDocument);
    editor.setText(makeTestHtml());
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeTestHtml() {
    return "<html><body>"
         + "<div>0000000</div>"
         + "<div class='highlight'>1111111111</div>"
         + "<div>2222222222</div>"
         + "</body></html>";
  }

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
