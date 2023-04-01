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
      String name = rules.nextElement().toString();
      if (Objects.equals("body", name)) {
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
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, flg);
    });
    // check.addActionListener(e -> {
    //   HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
    //   kit.getStyleSheet().addRule("body {font-size: 64pt;}");
    // });

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
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
