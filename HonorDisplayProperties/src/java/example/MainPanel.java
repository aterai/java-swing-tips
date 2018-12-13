// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JEditorPane editor = new JEditorPane("text/html", "");
    editor.setFont(new Font("Serif", Font.PLAIN, 16));

    // StyleSheet styleSheet = new StyleSheet();
    // styleSheet.addRule("body {font-size: 16pt;}");
    // styleSheet.addRule("h1 {font-size: 64pt;}");
    // htmlEditorKit.setStyleSheet(styleSheet);
    // editor.setEditorKit(htmlEditorKit);

    StringBuilder buf = new StringBuilder(300);
    buf.append("<html>JEditorPane#setFont(new Font('Serif', Font.PLAIN, 16));<br />");
    HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor.getEditorKit();
    StyleSheet styles = htmlEditorKit.getStyleSheet();
    // System.out.println(styles);
    Enumeration<?> rules = styles.getStyleNames();
    while (rules.hasMoreElements()) {
      String name = Objects.toString(rules.nextElement());
      if ("body".equals(name)) {
        Style rule = styles.getRule(name);
        Enumeration<?> attrs = rule.getAttributeNames();
        while (attrs.hasMoreElements()) {
          Object a = attrs.nextElement();
          buf.append(String.format("%s: %s<br />", a, rule.getAttribute(a)));
        }
      }
    }
    editor.setText(buf.toString());

    JCheckBox check = new JCheckBox("JEditorPane.HONOR_DISPLAY_PROPERTIES");
    check.addActionListener(e -> {
      editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, ((JCheckBox) e.getSource()).isSelected());
      // HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor.getEditorKit();
      // StyleSheet styles = htmlEditorKit.getStyleSheet();
      // styles.addRule("body {font-size: 64pt;}");
    });

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
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
