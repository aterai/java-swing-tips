// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    Icon information = UIManager.getIcon("OptionPane.informationIcon");
    Icon error = UIManager.getIcon("OptionPane.errorIcon");
    Icon question = UIManager.getIcon("OptionPane.questionIcon");
    Icon warning = UIManager.getIcon("OptionPane.warningIcon");

    Stream.of(information, error, question, warning)
        .map(this::createBox)
        .forEach(this::add);

    setPreferredSize(new Dimension(320, 240));
  }

  private Box createBox(Icon icon) {
    Box box = Box.createHorizontalBox();

    box.add(Box.createHorizontalGlue());
    box.add(createLabel("0", icon));
    box.add(Box.createHorizontalStrut(16));
    box.add(createLabel(
        "180",
        new QuadrantRotateIcon(icon, QuadrantRotate.HORIZONTAL_FLIP)));
    box.add(Box.createHorizontalStrut(16));
    box.add(createLabel(
        "90",
        new QuadrantRotateIcon(icon, QuadrantRotate.CLOCKWISE)));
    box.add(Box.createHorizontalStrut(16));
    box.add(createLabel(
        "-90",
        new QuadrantRotateIcon(icon, QuadrantRotate.COUNTER_CLOCKWISE)));
    box.add(Box.createHorizontalGlue());

    return box;
  }

  private static JLabel createLabel(String title, Icon icon) {
    JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    return label;
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
      Logger.getGlobal().severe(ex::getMessage);
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
  CLOCKWISE(1) {
    @Override public void translate(Graphics2D g2, int w, int h) {
      g2.translate(h, 0);
    }

    @Override public int getWidth(Icon icon) {
      return icon.getIconHeight();
    }

    @Override public int getHeight(Icon icon) {
      return icon.getIconWidth();
    }
  },

  HORIZONTAL_FLIP(2) {
    @Override public void translate(Graphics2D g2, int w, int h) {
      g2.translate(w, h);
    }
  },

  COUNTER_CLOCKWISE(-1) {
    @Override public void translate(Graphics2D g2, int w, int h) {
      g2.translate(0, w);
    }

    @Override public int getWidth(Icon icon) {
      return icon.getIconHeight();
    }

    @Override public int getHeight(Icon icon) {
      return icon.getIconWidth();
    }
  };

  private final int quadrants;

  QuadrantRotate(int quadrants) {
    this.quadrants = quadrants;
  }

  public int getQuadrants() {
    return quadrants;
  }

  public void translate(Graphics2D g2, int w, int h) {
    // do nothing
  }

  public int getWidth(Icon icon) {
    return icon.getIconWidth();
  }

  public int getHeight(Icon icon) {
    return icon.getIconHeight();
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
    int width = icon.getIconWidth();
    int height = icon.getIconHeight();

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    rotate.translate(g2, width, height);
    g2.transform(AffineTransform.getQuadrantRotateInstance(rotate.getQuadrants()));
    icon.paintIcon(c, g2, 0, 0);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return rotate.getWidth(icon);
  }

  @Override public int getIconHeight() {
    return rotate.getHeight(icon);
  }
}
