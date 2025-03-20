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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // // JDK 1.6.0
    // JScrollPane scroll = new JScrollPane(new JLabel(makeIcon()));
    // JDK 1.7.0 or later
    JScrollPane scroll = new JScrollPane(new JLabel(makeIcon())) {
      @Override protected JViewport createViewport() {
        return makeViewport();
      }
    };
    HandScrollListener hsl1 = new HandScrollListener();
    JViewport viewport = scroll.getViewport();
    viewport.addMouseMotionListener(hsl1);
    viewport.addMouseListener(hsl1);

    JRadioButton radio = new JRadioButton("scrollRectToVisible", true);
    radio.addItemListener(e -> hsl1.setWithinRangeMode(e.getStateChange() == ItemEvent.SELECTED));

    Box box = Box.createHorizontalBox();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(radio, new JRadioButton("setViewPosition")).forEach(r -> {
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

  private JViewport makeViewport() {
    AtomicBoolean isAdjusting = new AtomicBoolean();
    return new JViewport() {
      private static final boolean WEIGHT_MIXING = false;

      @Override public void revalidate() {
        if (!WEIGHT_MIXING && isAdjusting.get()) {
          return;
        }
        super.revalidate();
      }

      @Override public void setViewPosition(Point p) {
        if (WEIGHT_MIXING) {
          super.setViewPosition(p);
        } else {
          isAdjusting.set(true);
          super.setViewPosition(p);
          isAdjusting.set(false);
        }
      }
    };
  }

  private static Icon makeIcon() {
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
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

class HandScrollListener extends MouseAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point pp = new Point();
  private boolean withinRangeMode = true;

  @Override public void mouseDragged(MouseEvent e) {
    JViewport viewport = (JViewport) e.getComponent();
    Point cp = e.getPoint();
    // Point vp = SwingUtilities.convertPoint(viewport, 0, 0, label);
    Point vp = viewport.getViewPosition();
    vp.translate(pp.x - cp.x, pp.y - cp.y);
    Component c = SwingUtilities.getUnwrappedView(viewport);
    if (withinRangeMode && c instanceof JComponent) {
      ((JComponent) c).scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
    } else {
      viewport.setViewPosition(vp);
    }
    pp.setLocation(cp);
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(hndCursor);
    pp.setLocation(e.getPoint());
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }

  public void setWithinRangeMode(boolean b) {
    withinRangeMode = b;
  }
}

// // TEST:
// class DragScrollListener extends MouseAdapter {
//   private final Cursor defCursor = Cursor.getDefaultCursor();
//   private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//   private final Point pp = new Point();
//   @Override public void mouseDragged(MouseEvent e) {
//     Component c = e.getComponent();
//     Container p = SwingUtilities.getUnwrappedParent(c);
//     if (p instanceof JViewport) {
//       JViewport vport = (JViewport) p;
//       Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
//       Point vp = vport.getViewPosition();
//       vp.translate(pp.x - cp.x, pp.y - cp.y);
//       ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
//       pp.setLocation(cp);
//     }
//   }
//
//   @Override public void mousePressed(MouseEvent e) {
//     Component c = e.getComponent();
//     c.setCursor(hndCursor);
//     Container p = SwingUtilities.getUnwrappedParent(c);
//     if (p instanceof JViewport) {
//       JViewport vport = (JViewport) p;
//       Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
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
