// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/duke.gif";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

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

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
