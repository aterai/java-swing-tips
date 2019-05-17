// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final ImageIcon icon;

  private MainPanel() {
    super(new BorderLayout());
    icon = new ImageIcon(getClass().getResource("16x16.png"));
    add(new JLabel("@title@"));
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override protected void paintComponent(Graphics g) {
    Dimension d = getSize();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    Image image = icon.getImage();
    for (int i = 0; i * w < d.width; i++) {
      for (int j = 0; j * h < d.height; j++) {
        g.drawImage(image, i * w, j * h, w, h, this);
      }
    }
    // for (int x = 0; x < d.width; x += w) {
    //   for (int y = 0; y < d.height; y += h) {
    //     g.drawImage(image, x, y, w, h, this);
    //   }
    // }
    super.paintComponent(g);
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
