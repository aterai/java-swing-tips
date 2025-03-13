// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    Icon information = UIManager.getIcon("OptionPane.informationIcon");
    Icon error = UIManager.getIcon("OptionPane.errorIcon");
    Icon question = UIManager.getIcon("OptionPane.questionIcon");
    Icon warning = UIManager.getIcon("OptionPane.warningIcon");
    Stream.of(information, error, question, warning).map(this::makeBox).forEach(this::add);
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeBox(Icon icon) {
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeLabel("0", icon));
    box.add(Box.createHorizontalStrut(16));
    box.add(makeLabel("180", new QuadrantRotateIcon(icon, QuadrantRotate.HORIZONTAL_FLIP)));
    box.add(Box.createHorizontalStrut(16));
    box.add(makeLabel("90", new QuadrantRotateIcon(icon, QuadrantRotate.CLOCKWISE)));
    box.add(Box.createHorizontalStrut(16));
    box.add(makeLabel("-90", new QuadrantRotateIcon(icon, QuadrantRotate.COUNTER_CLOCKWISE)));
    box.add(Box.createHorizontalGlue());
    return box;
  }

  private static JLabel makeLabel(String title, Icon icon) {
    JLabel l = new JLabel(title, icon, SwingConstants.CENTER);
    l.setVerticalTextPosition(SwingConstants.BOTTOM);
    l.setHorizontalTextPosition(SwingConstants.CENTER);
    return l;
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

enum QuadrantRotate {
  CLOCKWISE(1), HORIZONTAL_FLIP(2), COUNTER_CLOCKWISE(-1);
  private final int numquadrants;

  QuadrantRotate(int numquadrants) {
    this.numquadrants = numquadrants;
  }

  public int getNumQuadrants() {
    return numquadrants;
  }
}

class QuadrantRotateIcon implements Icon {
  private final QuadrantRotate rotate;
  private final Icon icon;

  protected QuadrantRotateIcon(Icon icon, QuadrantRotate rotate) {
    this.icon = icon;
    this.rotate = rotate;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    switch (rotate) {
      case CLOCKWISE:
        g2.translate(h, 0);
        break;
      case HORIZONTAL_FLIP:
        g2.translate(w, h);
        break;
      case COUNTER_CLOCKWISE:
        g2.translate(0, w);
        break;
      default:
        throw new AssertionError("Unknown QuadrantRotateIcon");
    }
    // AffineTransform at = g2.getTransform();
    // at.quadrantRotate(rotate.getNumQuadrants());
    // g2.setTransform(at);
    g2.transform(AffineTransform.getQuadrantRotateInstance(rotate.getNumQuadrants()));
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return rotate == QuadrantRotate.HORIZONTAL_FLIP ? icon.getIconWidth() : icon.getIconHeight();
  }

  @Override public int getIconHeight() {
    return rotate == QuadrantRotate.HORIZONTAL_FLIP ? icon.getIconHeight() : icon.getIconWidth();
  }
}
