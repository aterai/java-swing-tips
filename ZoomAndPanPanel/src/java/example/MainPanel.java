// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Image img = Optional.ofNullable(getClass().getResource("CRW_3857_JFR.jpg"))
        .map(u -> {
          try {
            return ImageIO.read(u);
          } catch (IOException ex) {
            return makeMissingImage();
          }
        }).orElseGet(MainPanel::makeMissingImage);

    add(new JScrollPane(new ZoomAndPanePanel(img)));
    setPreferredSize(new Dimension(320, 240));
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

class ZoomAndPanePanel extends JPanel {
  private final AffineTransform zoomTransform = new AffineTransform();
  private final transient Image img;
  private final Rectangle imageRect;
  private transient ZoomHandler handler;
  private transient DragScrollListener listener;

  protected ZoomAndPanePanel(Image img) {
    super();
    this.img = img;
    this.imageRect = new Rectangle(img.getWidth(this), img.getHeight(this));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new Color(0x55_FF_00_00, true));
    Rectangle r = new Rectangle(500, 140, 150, 150);

    // use: AffineTransform#concatenate(...) and Graphics2D#setTransform(...)
    // https://docs.oracle.com/javase/8/docs/api/java/awt/geom/AffineTransform.html#concatenate-java.awt.geom.AffineTransform-
    // AffineTransform at = g2.getTransform();
    // at.concatenate(zoomTransform);
    // g2.setTransform(at);
    // g2.drawImage(img, 0, 0, this); // icon.paintIcon(this, g2, 0, 0);
    // g2.fill(r);

    // or use: Graphics2D#drawImage(Image, AffineTransform, ImageObserver)
    // https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html#drawImage-java.awt.Image-java.awt.geom.AffineTransform-java.awt.image.ImageObserver-
    g2.drawImage(img, zoomTransform, this); // or: g2.drawRenderedImage((RenderedImage) img, zoomTransform);
    g2.fill(zoomTransform.createTransformedShape(r));

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
    private static final double ZOOM_MULTIPLICATION_FACTOR = 1.2;
    private static final int MIN_ZOOM = -10;
    private static final int MAX_ZOOM = 10;
    private static final int EXTENT = 1;
    private final BoundedRangeModel zoomRange = new DefaultBoundedRangeModel(0, EXTENT, MIN_ZOOM, MAX_ZOOM + EXTENT);

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      int dir = e.getWheelRotation();
      int z = zoomRange.getValue();
      zoomRange.setValue(z + EXTENT * (dir > 0 ? -1 : 1));
      if (z != zoomRange.getValue()) {
        Component c = e.getComponent();
        Container p = SwingUtilities.getAncestorOfClass(JViewport.class, c);
        if (p instanceof JViewport) {
          JViewport vport = (JViewport) p;
          Rectangle ovr = vport.getViewRect();
          double s = dir > 0 ? 1d / ZOOM_MULTIPLICATION_FACTOR : ZOOM_MULTIPLICATION_FACTOR;
          zoomTransform.scale(s, s);
          // double s = 1d + zoomRange.getValue() * .1;
          // zoomTransform.setToScale(s, s);
          Rectangle nvr = AffineTransform.getScaleInstance(s, s).createTransformedShape(ovr).getBounds();
          Point vp = nvr.getLocation();
          vp.translate((nvr.width - ovr.width) / 2, (nvr.height - ovr.height) / 2);
          vport.setViewPosition(vp);
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
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
