// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public MainPanel() {
    super(new BorderLayout());

    BufferedImage image;
    try {
      image = ImageIO.read(getClass().getResource("duke.gif"));
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }
    JLabel icon = makeLabelIcon(image);
    icon.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    icon.setSize(icon.getPreferredSize());
    icon.setLocation(20, 20);

    JDesktopPane desktop = new JDesktopPane();
    desktop.setOpaque(false);
    desktop.add(icon);
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private JLabel makeLabelIcon(BufferedImage image) {
    MouseAdapter handler = new ComponentMoveHandler();
    return new JLabel(new ImageIcon(image)) {
      @Override public boolean contains(int x, int y) {
        return super.contains(x, y) && ((image.getRGB(x, y) >> 24) & 0xFF) != 0;
      }

      @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        // handler = new ComponentMoveHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
      }
    };
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ComponentMoveHandler extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    startPt.setLocation(e.getPoint());
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    c.setLocation(c.getX() - startPt.x + e.getX(), c.getY() - startPt.y + e.getY());
  }
}
