// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box1 = Box.createHorizontalBox();
    box1.add(new JLabel("JLabel"));
    box1.add(new WavyLineSeparator(SwingConstants.VERTICAL));
    box1.add(Box.createHorizontalStrut(5));
    box1.add(new JTextField("**********"));
    box1.add(Box.createHorizontalGlue());
    box1.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    Box box2 = Box.createVerticalBox();
    box2.add(new JCheckBox("JCheckBox"));
    box2.add(new WavyLineSeparator());
    box2.add(new JLabel("JLabel: 12345678901234567890"));
    box2.add(Box.createVerticalGlue());
    box2.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    add(box1, BorderLayout.NORTH);
    add(box2, BorderLayout.SOUTH);
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

class WavyLineSeparator extends JSeparator {
  protected static final int ICON_WIDTH = 3;
  protected static final Icon HORIZONTAL_ICON = new WavyLineIcon();
  protected static final Icon VERTICAL_ICON = new WavyLineIcon(VERTICAL);

  protected WavyLineSeparator() {
    this(HORIZONTAL);
  }

  protected WavyLineSeparator(int orientation) {
    super(orientation);
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
  }

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    // g.setClip(0, 0, getWidth(), getHeight());
    int pos;
    Insets i = getInsets();
    if (getOrientation() == HORIZONTAL) {
      for (pos = i.left; getWidth() - pos > 0; pos += HORIZONTAL_ICON.getIconWidth()) {
        HORIZONTAL_ICON.paintIcon(this, g, pos, i.top);
      }
    } else {
      for (pos = i.top; getHeight() - pos > 0; pos += VERTICAL_ICON.getIconHeight()) {
        VERTICAL_ICON.paintIcon(this, g, i.left, pos);
      }
    }
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    return getOrientation() == HORIZONTAL
      ? new Dimension(30, ICON_WIDTH + i.top + i.bottom)
      : new Dimension(ICON_WIDTH + i.left + i.right, 30);
  }

  private static class WavyLineIcon implements Icon {
    private final Color sfc = UIManager.getColor("Separator.foreground");
    private final int orientation;

    protected WavyLineIcon() {
      this.orientation = HORIZONTAL;
    }

    protected WavyLineIcon(int orientation) {
      this.orientation = orientation;
    }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(sfc);
      if (orientation == VERTICAL) {
        g2.translate(x + getIconWidth(), y);
        g2.rotate(Math.PI / 2);
      } else {
        g2.translate(x, y);
      }
      g2.drawLine(0, 2, 0, 2);
      g2.drawLine(1, 1, 1, 1);
      g2.drawLine(2, 0, 3, 0);
      g2.drawLine(4, 1, 4, 1);
      g2.drawLine(5, 2, 5, 2);
      g2.dispose();
    }

    @Override public int getIconWidth() {
      return orientation == HORIZONTAL ? ICON_WIDTH * 2 : ICON_WIDTH;
    }

    @Override public int getIconHeight() {
      return orientation == HORIZONTAL ? ICON_WIDTH : ICON_WIDTH * 2;
    }
  }
}
