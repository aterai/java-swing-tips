// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Point hotSpot = new Point(16, 16);

    BufferedImage bi1 = makeStringBufferedImage("?");
    JButton button1 = new JButton("?");
    button1.setCursor(getToolkit().createCustomCursor(bi1, hotSpot, "?"));

    BufferedImage bi2 = makeOvalBufferedImage();
    JButton button2 = new JButton("Oval");
    button2.setCursor(getToolkit().createCustomCursor(bi2, hotSpot, "oval"));
    button2.setIcon(new ImageIcon(bi2));

    Icon icon = new GreenBlueIcon();
    BufferedImage bi3 = makeIconBufferedImage(icon);
    JButton button3 = new JButton("Rect");
    button3.setCursor(getToolkit().createCustomCursor(bi3, hotSpot, "rect"));
    button3.setIcon(icon);

    JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
    p.add(makeTitledPanel("String", button1));
    p.add(makeTitledPanel("drawOval", button2));
    p.add(makeTitledPanel("paintIcon", button3));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  public static BufferedImage makeStringBufferedImage(String str) {
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    g2.setPaint(Color.BLACK);
    g2.drawString(str, 16, 28);
    g2.dispose();
    return bi;
  }

  private static BufferedImage makeOvalBufferedImage() {
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    g2.setPaint(Color.RED);
    g2.drawOval(8, 8, 16, 16);
    g2.dispose();
    return bi;
  }

  private static BufferedImage makeIconBufferedImage(Icon icon) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    icon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class GreenBlueIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.GREEN);
    g2.fillRect(8, 8, 8, 8);
    g2.setPaint(Color.BLUE);
    g2.fillRect(16, 16, 8, 8);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
