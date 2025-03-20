// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<Component> list = Arrays.asList(
        new JMenuItem("JMenuItem 1"),
        new JMenuItem("JMenuItem 2"),
        new JMenuItem("JMenuItem 3"),
        new JMenuItem("JMenuItem 4"),
        new JMenuItem("JMenuItem 5"));
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        Point popupLocation = getInvokerOrigin(x, y, c.getLocationOnScreen());
        // typo popupPostionFixDisabled should be popupPositionFixDisabled
        // if (popupPositionFixDisabled || GraphicsEnvironment.isHeadless()) ...
        Rectangle scrBounds = getScreenBounds(c, popupLocation);
        Dimension popupSize = getPreferredSize();
        long ly = popupLocation.y;
        long popupBottomY = ly + popupSize.height;
        Point p = new Point(x, y);
        removeAll();
        if (popupBottomY > scrBounds.y + scrBounds.height) {
          p.translate(-popupSize.width, -popupSize.height);
          for (int i = list.size() - 1; i >= 0; i--) {
            add(list.get(i));
          }
        } else {
          list.forEach(this::add);
        }
        super.show(c, p.x, p.y);
      }
    };
    list.forEach(popup::add);
    setComponentPopupMenu(popup);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Rectangle getScreenBounds(Component c, Point popupLocation) {
    Rectangle scrBounds;
    GraphicsConfiguration gc = getCurrentGraphicsConfiguration2(c, popupLocation);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    if (gc != null) {
      // If we have GraphicsConfiguration use it to get screen bounds
      scrBounds = gc.getBounds();
    } else {
      scrBounds = new Rectangle(toolkit.getScreenSize());
    }
    Insets scrInsets = toolkit.getScreenInsets(gc);
    scrBounds.x += scrInsets.left;
    scrBounds.y += scrInsets.top;
    scrBounds.width -= scrInsets.left + scrInsets.right;
    scrBounds.height -= scrInsets.top + scrInsets.bottom;
    return scrBounds;
  }

  // @see JPopupMenu#getCurrentGraphicsConfiguration(Point)
  private static GraphicsConfiguration getCurrentGraphicsConfiguration2(Component c, Point p) {
    GraphicsConfiguration gc = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (GraphicsDevice graphicsDevice : gd) {
      if (graphicsDevice.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
        GraphicsConfiguration dgc = graphicsDevice.getDefaultConfiguration();
        if (dgc.getBounds().contains(p)) {
          gc = dgc;
          break;
        }
      }
    }
    // If not found, and we have invoker, ask invoker about his gc
    if (gc == null && c != null) {
      gc = c.getGraphicsConfiguration();
    }
    return gc;
  }

  // @see JPopupMenu#show(Component invoker, int x, int y)
  // To avoid integer overflow
  private static Point getInvokerOrigin(long x, long y, Point invokerOrigin) {
    long lx = invokerOrigin.x + x;
    long ly = invokerOrigin.y + y;
    if (lx > Integer.MAX_VALUE) {
      lx = Integer.MAX_VALUE;
    }
    if (lx < Integer.MIN_VALUE) {
      lx = Integer.MIN_VALUE;
    }
    if (ly > Integer.MAX_VALUE) {
      ly = Integer.MAX_VALUE;
    }
    if (ly < Integer.MIN_VALUE) {
      ly = Integer.MIN_VALUE;
    }
    return new Point((int) lx, (int) ly);
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
