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
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Icon icon = new ImageIcon(makeImage());
    JLabel label = makeImageLabel(icon);
    Component scroll = makeTopLeftScrollPane(label);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTopLeftScrollPane(Component view) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    int w = scroll.getVerticalScrollBar().getPreferredSize().width;
    JPanel header = new JPanel(new BorderLayout());
    header.add(Box.createHorizontalStrut(w), BorderLayout.WEST);
    header.add(scroll.getHorizontalScrollBar());
    JPanel p = new JPanel(new BorderLayout());
    p.add(header, BorderLayout.NORTH);
    p.add(scroll);
    return p;
  }

  private static JLabel makeImageLabel(Icon icon) {
    return new JLabel(icon) {
      private transient MouseAdapter listener;

      @Override public void updateUI() {
        removeMouseMotionListener(listener);
        removeMouseListener(listener);
        super.updateUI();
        listener = new DragScrollListener();
        addMouseMotionListener(listener);
        addMouseListener(listener);
      }
    };
  }

  private static Image makeImage() {
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
  }

  private static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
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

class DragScrollListener extends MouseAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point startPt = new Point();

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      Rectangle rect = viewport.getViewRect();
      rect.translate(startPt.x - cp.x, startPt.y - cp.y);
      ((JComponent) c).scrollRectToVisible(rect);
      startPt.setLocation(cp);
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hndCursor);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      startPt.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
