package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new MyEditorKit());
    editor.setText("1234123541341234123423\nadfasdfasdfasdf\nffas213441324dfasdfas\n\nretqewrqwr");
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

class MyEditorKit extends StyledEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new MyViewFactory();
  }
}

class MyViewFactory implements ViewFactory {
  @Override public View create(Element elem) {
    String kind = elem.getName();
    if (Objects.nonNull(kind)) {
      if (kind.equals(AbstractDocument.ContentElementName)) {
        return new LabelView(elem);
      } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new ParagraphWithEopmView(elem);
      } else if (kind.equals(AbstractDocument.SectionElementName)) {
        return new BoxView(elem, View.Y_AXIS);
      } else if (kind.equals(StyleConstants.ComponentElementName)) {
        return new ComponentView(elem);
      } else if (kind.equals(StyleConstants.IconElementName)) {
        return new IconView(elem);
      }
    }
    return new LabelView(elem);
  }
}

class ParagraphWithEopmView extends ParagraphView {
  // private static final ParagraphMarkIcon paragraphMarkIcon = new ParagraphMarkIcon();
  private static final Color MARK_COLOR = new Color(120, 130, 110);

  protected ParagraphWithEopmView(Element elem) {
    super(elem);
  }

  @Override public void paint(Graphics g, Shape allocation) {
    super.paint(g, allocation);
    paintCustomParagraph(g, allocation);
  }

  private void paintCustomParagraph(Graphics g, Shape a) {
    try {
      Shape paragraph = modelToView(getEndOffset(), a, Position.Bias.Backward);
      Rectangle r = Objects.nonNull(paragraph) ? paragraph.getBounds() : a.getBounds();
      int x = r.x;
      int y = r.y;
      int h = r.height;
      // paragraphMarkIcon.paintIcon(null, g, x, y);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(MARK_COLOR);
      g2.drawLine(x + 1, y + h / 2, x + 1, y + h - 4);
      g2.drawLine(x + 2, y + h / 2, x + 2, y + h - 5);
      g2.drawLine(x + 3, y + h - 6, x + 3, y + h - 6);
      g2.dispose();
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
}

// TEST:
// class ParagraphMarkIcon implements Icon {
//   private static final Color MARK_COLOR = new Color(120, 130, 110);
//   private final Polygon paragraphMark = new Polygon();
//   protected ParagraphMarkIcon() {
//     paragraphMark.addPoint(1, 7);
//     paragraphMark.addPoint(3, 7);
//     paragraphMark.addPoint(3, 11);
//     paragraphMark.addPoint(4, 11);
//     paragraphMark.addPoint(1, 14);
//   }
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setPaint(MARK_COLOR);
//     g2.translate(x, y);
//     g2.draw(paragraphMark);
//     g2.dispose();
//   }
//   @Override public int getIconWidth() {
//     return 3;
//   }
//   @Override public int getIconHeight() {
//     return 7;
//   }
// }
