// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join(" ",
      "This lesson provides an introduction to",
      "Graphical User Interface (GUI) programming with Swing and the NetBeans IDE.",
      "As you learned in the \"Hello World!\" lesson, the NetBeans IDE is a free,",
      "open-source, cross-platform integrated development environment with built-in",
      "support for the Java programming language."
  );

  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new DropCapLabel(TEXT);
    label.setFont(new Font(Font.SERIF, Font.PLAIN, 17));
    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(label);
    setBorder(BorderFactory.createLineBorder(new Color(0x64_64_C8_C8, true), 10));
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

class DropCapLabel extends JLabel {
  protected DropCapLabel(String text) {
    super(text);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getBackground());
    g2.fillRect(0, 0, getWidth(), getHeight());

    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    float x0 = rect.x;
    float y0 = rect.y;

    Font font = getFont();
    String txt = getText();

    FontRenderContext frc = g2.getFontRenderContext();
    Shape shape = new TextLayout(txt.substring(0, 1), font, frc).getOutline(null);

    AffineTransform at1 = AffineTransform.getScaleInstance(5d, 5d);
    Shape s1 = at1.createTransformedShape(shape);
    Rectangle firstLetter = s1.getBounds();
    firstLetter.grow(6, 2);

    AffineTransform at2 = AffineTransform.getTranslateInstance(x0, y0 + firstLetter.height);
    Shape s2 = at2.createTransformedShape(s1);
    g2.setPaint(getForeground());
    g2.fill(s2);

    float x = x0 + firstLetter.width;
    float y = y0;
    int w = rect.width - firstLetter.width;

    AttributedString as = new AttributedString(txt.substring(1));
    as.addAttribute(TextAttribute.FONT, font);
    AttributedCharacterIterator aci = as.getIterator();
    LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
    while (lbm.getPosition() < aci.getEndIndex()) {
      TextLayout tl = lbm.nextLayout(w);
      tl.draw(g2, x, y + tl.getAscent());
      y += tl.getDescent() + tl.getLeading() + tl.getAscent();
      if (y0 + firstLetter.height < y) {
        x = x0;
        w = rect.width;
      }
    }
    g2.dispose();
  }
}
