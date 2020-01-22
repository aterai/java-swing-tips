// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;

public final class MainPanel extends JPanel {
  private static final String S = "https://ateraimemo.com/";
  private static final String S0 = "<a href='%s' color='%s'>%s</a><br>";
  private final String s1 = String.format(S0 + "aaaaaaaaaaaaaa<br>", S, "blue", S);
  private final String s2 = String.format(S0 + "cccc", S, "#0000FF", "bbbbbbbbbbb");
  private final JEditorPane editor = new JEditorPane("text/html", "<html>" + s1 + s2);

  public MainPanel() {
    super(new BorderLayout());
    editor.setEditable(false);
    // @see: BasicEditorPaneUI#propertyChange(PropertyChangeEvent evt) {
    //    if ("foreground".equals(name)) {
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

    editor.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        setElementColor(e.getSourceElement(), "red");
      } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
        setElementColor(e.getSourceElement(), "blue");
      } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        Toolkit.getDefaultToolkit().beep();
      }
      // ??? call BasicTextUI#modelChanged() ???
      editor.setForeground(Color.WHITE);
      editor.setForeground(Color.BLACK);
    });
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  private void setElementColor(Element element, String color) {
    AttributeSet attrs = element.getAttributes();
    Object o = attrs.getAttribute(HTML.Tag.A);
    if (o instanceof MutableAttributeSet) {
      MutableAttributeSet a = (MutableAttributeSet) o;
      a.addAttribute(HTML.Attribute.COLOR, color);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
