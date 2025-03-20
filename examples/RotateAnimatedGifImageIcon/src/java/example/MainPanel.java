// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 2));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/duke.running.gif");
    assert url != null;
    ImageIcon imageIcon = new ImageIcon(url);
    JLabel label0 = new JLabel(imageIcon);
    label0.setBorder(BorderFactory.createTitledBorder("Default ImageIcon"));

    JLabel label1 = new JLabel(new ClockwiseRotateIcon(imageIcon));
    label1.setBorder(BorderFactory.createTitledBorder("Wrapping with another Icon"));

    Image img = imageIcon.getImage();
    JPanel label2 = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        g2.setTransform(AffineTransform.getQuadrantRotateInstance(1, x, y));
        int x2 = x - img.getWidth(this) / 2;
        int y2 = y - img.getHeight(this) / 2;
        // imageIcon.paintIcon(this, g2, x2, y2);
        g2.drawImage(img, x2, y2, this);
        g2.dispose();
      }
    };
    label2.setBorder(BorderFactory.createTitledBorder("Override JPanel#paintComponent(...)"));

    Icon icon3 = new ImageIcon(url) {
      @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
      @Override public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + getIconHeight(), y);
        g2.transform(AffineTransform.getQuadrantRotateInstance(1));
        super.paintIcon(c, g2, 0, 0);
        g2.dispose();
      }

      @Override public int getIconWidth() {
        return super.getIconHeight();
      }

      @Override public int getIconHeight() {
        return super.getIconWidth();
      }
    };
    JLabel label3 = new JLabel(icon3);
    label3.setBorder(BorderFactory.createTitledBorder("Override ImageIcon#paintIcon(...)"));

    add(label0);
    add(label1);
    add(label2);
    add(label3);
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

class ClockwiseRotateIcon implements Icon {
  private final Icon icon;

  protected ClockwiseRotateIcon(Icon icon) {
    this.icon = icon;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x + icon.getIconHeight(), y);
    g2.transform(AffineTransform.getQuadrantRotateInstance(1));
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconHeight();
  }

  @Override public int getIconHeight() {
    return icon.getIconWidth();
  }
}
