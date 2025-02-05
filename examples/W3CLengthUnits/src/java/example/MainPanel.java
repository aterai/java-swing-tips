// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JEditorPane editor = new JEditorPane("text/html", "");

    HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor.getEditorKit();
    StyleSheet styles = htmlEditorKit.getStyleSheet();
    styles.addRule(".number {font-size: 14}");
    styles.addRule(".pt {font-size: 14pt}");
    styles.addRule(".em {font-size: 1.2em}");
    styles.addRule(".percentage {font-size: 120%}");

    String html = "<html><h3>h3 {font-size: medium}</h3>"
        + "<h3 class='number'>h3 {font-size: 14}</h3>"
        + "<h3 class='pt'>h3 {font-size: 14pt}</h3>"
        + "<h3 class='em'>h3 {font-size: 1.2em}</h3>"
        + "<h3 class='percentage'>h3 {font-size: 120%}</h3>";
    editor.setText(html);

    // StringBuilder buf = new StringBuilder(300);
    // Enumeration<?> rules = styles.getStyleNames();
    // while (rules.hasMoreElements()) {
    //   String name = rules.nextElement().toString();
    //   Style rule = styles.getRule(name);
    //   Enumeration<?> attrs = rule.getAttributeNames();
    //   while (attrs.hasMoreElements()) {
    //     Object a = attrs.nextElement();
    //     buf.append(String.format("%s: %s<br />", a, rule.getAttribute(a)));
    //   }
    // }
    // editor.setText(buf.toString());

    JCheckBox check = new JCheckBox("JEditorPane.W3C_LENGTH_UNITS");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      editor.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, b);
    });

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  // @see javax/swing/text/html/CSS.java
  // static class LengthUnit implements Serializable {
  //   static Hashtable<String, Float> lengthMapping = new Hashtable<String, Float>(6);
  //   static Hashtable<String, Float> w3cLengthMapping = new Hashtable<String, Float>(6);
  //   static {
  //     lengthMapping.put("pt", Float.valueOf(1f));
  //     // Not sure about 1.3, determined by experiementation.
  //     lengthMapping.put("px", Float.valueOf(1.3f));
  //     lengthMapping.put("mm", Float.valueOf(2.83464f));
  //     lengthMapping.put("cm", Float.valueOf(28.3464f));
  //     lengthMapping.put("pc", Float.valueOf(12f));
  //     lengthMapping.put("in", Float.valueOf(72f));
  //     int res = 72;
  //     try {
  //       res = Toolkit.getDefaultToolkit().getScreenResolution();
  //     } catch (HeadlessException e) {
  //     }
  //     // mapping according to the CSS2 spec
  //     w3cLengthMapping.put("pt", Float.valueOf(res/72f));
  //     w3cLengthMapping.put("px", Float.valueOf(1f));
  //     w3cLengthMapping.put("mm", Float.valueOf(res/25.4f));
  //     w3cLengthMapping.put("cm", Float.valueOf(res/2.54f));
  //     w3cLengthMapping.put("pc", Float.valueOf(res/6f));
  //     w3cLengthMapping.put("in", Float.valueOf((float) res));
  //   }
  //   // ...
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
