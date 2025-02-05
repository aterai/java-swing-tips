// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);

    add(new JScrollPane(new ZoomAndPanePanel(icon)));
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

class ZoomAndPanePanel extends JPanel {
  private final AffineTransform zoomTransform = new AffineTransform();
  private final transient Icon icon;
  private final Rectangle imageRect;
  private transient ZoomHandler handler;
  private transient DragScrollListener listener;

  protected ZoomAndPanePanel(Icon icon) {
    super();
    this.icon = icon;
    this.imageRect = new Rectangle(icon.getIconWidth(), icon.getIconHeight());
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();

    // use: AffineTransform#concatenate(...) and Graphics2D#setTransform(...)
    // https://docs.oracle.com/javase/8/docs/api/java/awt/geom/AffineTransform.html#concatenate-java.awt.geom.AffineTransform-
    AffineTransform at = g2.getTransform();
    at.concatenate(zoomTransform);
    g2.setTransform(at);
    icon.paintIcon(this, g2, 0, 0);
    // g2.drawImage(img, 0, 0, this);

    g2.setPaint(new Color(0x55_FF_00_00, true));
    Rectangle r = new Rectangle(500, 140, 150, 150);
    g2.fill(r);

    // or use: Graphics2D#drawImage(Image, AffineTransform, ImageObserver)
    // https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html#drawImage-java.awt.Image-java.awt.geom.AffineTransform-java.awt.image.ImageObserver-
    // g2.drawImage(img, zoomTransform, this);
    // or: g2.drawRenderedImage((RenderedImage) img, zoomTransform);
    // g2.fill(zoomTransform.createTransformedShape(r));

    // BAD EXAMPLE
    // g2.setTransform(zoomTransform);
    // g2.drawImage(img, 0, 0, this);

    g2.dispose();
  }

  @Override public Dimension getPreferredSize() {
    Rectangle r = zoomTransform.createTransformedShape(imageRect).getBounds();
    return new Dimension(r.width, r.height);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    removeMouseWheelListener(handler);
    super.updateUI();
    listener = new DragScrollListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
    handler = new ZoomHandler();
    addMouseWheelListener(handler);
  }

  protected class ZoomHandler extends MouseAdapter {
    private static final double ZOOM_FACTOR = 1.2;
    private static final int MIN = -10;
    private static final int MAX = 10;
    private static final int EXT = 1;
    private final BoundedRangeModel range = new DefaultBoundedRangeModel(0, EXT, MIN, MAX + EXT);

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      double dir = e.getPreciseWheelRotation();
      int z = range.getValue();
      range.setValue(z + EXT * (dir > 0 ? -1 : 1));
      if (z != range.getValue()) {
        Component c = e.getComponent();
        Container p = SwingUtilities.getAncestorOfClass(JViewport.class, c);
        if (p instanceof JViewport) {
          JViewport viewport = (JViewport) p;
          Rectangle ovr = viewport.getViewRect();
          double s = dir > 0 ? 1d / ZOOM_FACTOR : ZOOM_FACTOR;
          zoomTransform.scale(s, s);
          // double s = 1d + range.getValue() * .1;
          // zoomTransform.setToScale(s, s);
          AffineTransform at = AffineTransform.getScaleInstance(s, s);
          Rectangle nvr = at.createTransformedShape(ovr).getBounds();
          Point vp = nvr.getLocation();
          vp.translate((nvr.width - ovr.width) / 2, (nvr.height - ovr.height) / 2);
          viewport.setViewPosition(vp);
          c.revalidate();
          c.repaint();
        }
      }
    }
  }
}

class DragScrollListener extends MouseAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point pp = new Point();

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport vport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
      Point vp = vport.getViewPosition();
      vp.translate(pp.x - cp.x, pp.y - cp.y);
      ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
      pp.setLocation(cp);
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hndCursor);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport vport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
      pp.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }
}

// How to Use Icons (The Java™ Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/icon.html
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
