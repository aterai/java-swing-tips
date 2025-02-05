// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/16x16.png");
    String text = String.format("<span>Hello <img src='%s' />!!!</span>", url);

    JEditorPane editor1 = makeEditorPane(new HTMLEditorKit(), text);
    JEditorPane editor2 = makeEditorPane(new ImgBaselineHtmlEditorKit(), text);

    add(new JScrollPane(editor1));
    add(new JScrollPane(editor2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane(HTMLEditorKit kit, String text) {
    JEditorPane editor = new JEditorPane();
    editor.setEditable(false);
    editor.setContentType("text/html");
    editor.setEditorKit(kit);
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setText(text);
    StyleSheet style = kit.getStyleSheet();
    style.addRule("span {color: orange;}");
    style.addRule("img {align: middle; valign: middle; vertical-align: middle;}");
    return editor;
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

class ImgBaselineHtmlEditorKit extends HTMLEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new HTMLEditorKit.HTMLFactory() {
      @SuppressWarnings("PMD.OnlyOneReturn")
      @Override public View create(Element elem) {
        // View view = super.create(elem);
        // if (view instanceof LabelView) {
        //   System.out.println("debug: " + view.getAlignment(View.Y_AXIS));
        // }
        AttributeSet attrs = elem.getAttributes();
        Object name = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object o = name == null ? attrs.getAttribute(StyleConstants.NameAttribute) : null;
        if (o instanceof HTML.Tag) {
          HTML.Tag kind = (HTML.Tag) o;
          if (kind == HTML.Tag.IMG) {
            return new ImageView(elem) {
              @Override public float getAlignment(int axis) {
                // .8125f magic number...
                return axis == Y_AXIS ? .8125f : super.getAlignment(axis);
              }
            };
          }
        }
        return super.create(elem);
      }
    };
  }
}
