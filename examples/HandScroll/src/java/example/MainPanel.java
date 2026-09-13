// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // // JDK 1.6.0: the default JViewport can be scrolled beyond the view edges
    // JScrollPane scroll = new JScrollPane(new JLabel(createIcon()));
    // JDK 1.7.0 or later
    JScrollPane scroll = new JScrollPane(new JLabel(createIcon())) {
      @Override protected JViewport createViewport() {
        return new OverscrollViewport();
      }
    };
    HandDragScrollListener listener = new HandDragScrollListener();
    JViewport viewport = scroll.getViewport();
    viewport.addMouseMotionListener(listener);
    viewport.addMouseListener(listener);

    JRadioButton r1 = new JRadioButton("scrollRectToVisible", true);
    r1.addItemListener(e -> {
      boolean b = e.getStateChange() == ItemEvent.SELECTED;
      listener.setBoundedMode(b);
    });

    Box box = Box.createHorizontalBox();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, new JRadioButton("setViewPosition")).forEach(r -> {
      box.add(r);
      bg.add(r);
    });

    // // TEST:
    // MouseAdapter hsl2 = new DragScrollListener();
    // label.addMouseMotionListener(hsl2);
    // label.addMouseListener(hsl2);
    add(scroll);
    add(box, BorderLayout.NORTH);
    scroll.setPreferredSize(new Dimension(320, 240));
  }

  private static Icon createIcon() {
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      Icon i;
      try (InputStream s = u.openStream()) {
        i = new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        i = new MissingIcon();
      }
      return i;
    }).orElseGet(MissingIcon::new);
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

// JViewport#setViewPosition(Point) calls revalidate() since JDK 1.7.0 (to keep
// heavyweight/lightweight mixing consistent), and ViewportLayout then clamps
// the view position back inside the view bounds. Skip that revalidate() while
// the position is being set so the view can be scrolled beyond its edges.
class OverscrollViewport extends JViewport {
  private static final boolean WEIGHT_MIXING = false;
  private boolean adjusting;

  @Override public void revalidate() {
    if (WEIGHT_MIXING || !adjusting) {
      super.revalidate();
    }
  }

  @Override public void setViewPosition(Point p) {
    adjusting = true;
    super.setViewPosition(p);
    adjusting = false;
  }
}

class HandDragScrollListener extends MouseAdapter {
  private final Cursor defaultCursor = Cursor.getDefaultCursor();
  private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point prevPt = new Point();
  // true: JComponent#scrollRectToVisible(Rectangle) keeps the viewport inside
  // the view bounds; false: JViewport#setViewPosition(Point) allows overscroll
  private boolean boundedMode = true;

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(handCursor);
    prevPt.setLocation(e.getPoint());
  }

  @Override public void mouseDragged(MouseEvent e) {
    JViewport viewport = (JViewport) e.getComponent();
    Point pt = e.getPoint();
    // Move the view in the opposite direction of the mouse so the image
    // follows the cursor
    Rectangle rect = viewport.getViewRect();
    rect.translate(prevPt.x - pt.x, prevPt.y - pt.y);
    Component view = SwingUtilities.getUnwrappedView(viewport);
    if (boundedMode && view instanceof JComponent) {
      ((JComponent) view).scrollRectToVisible(rect);
    } else {
      viewport.setViewPosition(rect.getLocation());
    }
    prevPt.setLocation(pt);
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defaultCursor);
  }

  public void setBoundedMode(boolean b) {
    boundedMode = b;
  }
}

// // TEST:
// class DragScrollListener extends MouseAdapter {
//   private final Cursor defCursor = Cursor.getDefaultCursor();
//   private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//   private final Point pp = new Point();
//   @Override public void mouseDragged(MouseEvent e) {
//     Component c = e.getComponent();
//     Container p = SwingUtilities.getUnwrappedParent(c);
//     if (p instanceof JViewport) {
//       JViewport viewport = (JViewport) p;
//       Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
//       Point vp = viewport.getViewPosition();
//       vp.translate(pp.x - cp.x, pp.y - cp.y);
//       ((JComponent) c).scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
//       pp.setLocation(cp);
//     }
//   }
//
//   @Override public void mousePressed(MouseEvent e) {
//     Component c = e.getComponent();
//     c.setCursor(handCursor);
//     Container p = SwingUtilities.getUnwrappedParent(c);
//     if (p instanceof JViewport) {
//       JViewport viewport = (JViewport) p;
//       Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
//       pp.setLocation(cp);
//     }
//   }
//
//   @Override public void mouseReleased(MouseEvent e) {
//     e.getComponent().setCursor(defCursor);
//   }
// }

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
