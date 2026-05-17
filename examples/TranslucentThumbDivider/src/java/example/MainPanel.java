// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage source = Optional.ofNullable(cl.getResource(path)).map(url -> {
      BufferedImage buf;
      try (InputStream s = url.openStream()) {
        buf = ImageIO.read(s);
      } catch (IOException ex) {
        buf = createMissingImage();
      }
      return buf;
    }).orElseGet(MainPanel::createMissingImage);

    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    ColorConvertOp colorConvert = new ColorConvertOp(colorSpace, null);
    Image destination = colorConvert.filter(source, null);

    Component before = new BeforeCanvas(source);
    Component after = new AfterCanvas(destination);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, before, after);
    split.setContinuousLayout(true);
    split.setResizeWeight(.5);
    split.setDividerSize(0);

    DividerLocationDragLayerUI layerUI = new DividerLocationDragLayerUI();
    JCheckBox check = new JCheckBox("Paint custom divider");
    check.addActionListener(e -> layerUI.setCustomDividerEnabled(check.isSelected()));

    add(new JLayer<>(split, layerUI));
    add(check, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static BufferedImage createMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  private static Point calcCenterOffset(Dimension size, int iw, int ih) {
    return new Point((size.width - iw) / 2, (size.height - ih) / 2);
  }

  private static final class BeforeCanvas extends JComponent {
    private final Image image;

    private BeforeCanvas(Image image) {
      this.image = image;
    }

    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      int iw = image.getWidth(this);
      int ih = image.getHeight(this);
      Component parent = SwingUtilities.getUnwrappedParent(this);
      Point center = calcCenterOffset(parent.getSize(), iw, ih);
      g.drawImage(image, center.x, center.y, iw, ih, this);
    }
  }

  private static final class AfterCanvas extends JComponent {
    private final Image image;

    private AfterCanvas(Image image) {
      this.image = image;
    }

    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Component c = SwingUtilities.getUnwrappedParent(this);
      if (c instanceof JComponent) {
        Graphics2D g2 = (Graphics2D) g.create();
        JComponent p = (JComponent) c;
        g2.translate(-getLocation().x + p.getInsets().left, 0);
        int iw = image.getWidth(this);
        int ih = image.getHeight(this);
        Component parent = SwingUtilities.getUnwrappedParent(this);
        Point center = calcCenterOffset(parent.getSize(), iw, ih);
        g2.drawImage(image, center.x, center.y, iw, ih, this);
        g2.dispose();
      }
    }
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

class DividerLocationDragLayerUI extends LayerUI<JSplitPane> {
  public static final double THUMB_RADIUS = 25d;
  public static final double ARROW_SIZE = 8d;
  public static final int ICON_GAP_DIVISOR = 5;
  private final Point startPt = new Point();
  private final Cursor dc = Cursor.getDefaultCursor();
  private final Cursor wc = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
  private final Ellipse2D thumb = new Ellipse2D.Double();
  private int dividerLocation;
  private boolean isDragging;
  private boolean isEnter;
  private boolean isCustomDivider;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> l = (JLayer<?>) c;
      l.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if ((isEnter || isDragging) && c instanceof JLayer) {
      updateThumbLocation((JSplitPane) ((JLayer<?>) c).getView(), thumb);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(new Color(0x64_FF_64_64, true));
      g2.fill(thumb);
      if (isCustomDivider) {
        paintCustomDivider(g2);
      }
      g2.dispose();
    }
  }

  private void paintCustomDivider(Graphics2D g2) {
    g2.setStroke(new BasicStroke(5f));
    g2.setPaint(Color.WHITE);
    g2.draw(thumb);

    double cx = thumb.getCenterX();
    double cy = thumb.getCenterY();

    Line2D line = new Line2D.Double(cx, 0d, cx, thumb.getMinY());
    g2.draw(line);

    double mx = cx - thumb.getWidth() / 4d + ARROW_SIZE / 2d;
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(mx, cy - ARROW_SIZE);
    triangle.lineTo(mx - ARROW_SIZE, cy);
    triangle.lineTo(mx, cy + ARROW_SIZE);
    triangle.lineTo(mx, cy - ARROW_SIZE);
    triangle.closePath();
    g2.fill(triangle);

    AffineTransform at = AffineTransform.getQuadrantRotateInstance(2, cx, cy);
    g2.draw(at.createTransformedShape(line));
    g2.fill(at.createTransformedShape(triangle));
  }

  public void setCustomDividerEnabled(boolean flg) {
    this.isCustomDivider = flg;
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JSplitPane> l) {
    JSplitPane splitPane = l.getView();
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        isEnter = true;
        break;
      case MouseEvent.MOUSE_EXITED:
        isEnter = false;
        break;
      case MouseEvent.MOUSE_RELEASED:
        isDragging = false;
        break;
      case MouseEvent.MOUSE_PRESSED:
        handleMousePressed(e, splitPane);
        break;
      default:
        break;
    }
    // // Java 14:
    // switch (e.getID()) {
    //   case MouseEvent.MOUSE_ENTERED -> isEnter = true;
    //   case MouseEvent.MOUSE_EXITED  -> isEnter = false;
    //   case MouseEvent.MOUSE_RELEASED -> isDragging = false;
    //   case MouseEvent.MOUSE_PRESSED -> handleMousePressed(e, splitPane);
    // }
    splitPane.repaint();
  }

  private void handleMousePressed(MouseEvent e, JSplitPane splitPane) {
    Component c = e.getComponent();
    if (isDraggableComponent(splitPane, c)) {
      Point pt = SwingUtilities.convertPoint(c, e.getPoint(), splitPane);
      isDragging = thumb.contains(pt);
      startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), splitPane));
      dividerLocation = splitPane.getDividerLocation();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JSplitPane> l) {
    JSplitPane sp = l.getView();
    Component c = e.getComponent();
    Point pt = SwingUtilities.convertPoint(c, e.getPoint(), sp);
    int id = e.getID();
    if (id == MouseEvent.MOUSE_MOVED) {
      sp.setCursor(thumb.contains(e.getPoint()) ? wc : dc);
    } else if (isDragging && isDraggableComponent(sp, c) && id == MouseEvent.MOUSE_DRAGGED) {
      boolean isHorizontal = sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
      int delta = isHorizontal ? pt.x - startPt.x : pt.y - startPt.y;
      sp.setDividerLocation(Math.max(0, dividerLocation + delta));
    }
  }

  private static boolean isDraggableComponent(JSplitPane sp, Component c) {
    return Objects.equals(sp, c) || Objects.equals(sp, SwingUtilities.getUnwrappedParent(c));
  }

  private static void updateThumbLocation(JSplitPane splitPane, Ellipse2D thumb) {
    int pos = splitPane.getDividerLocation();
    Dimension dim = splitPane.getSize();
    double r2 = THUMB_RADIUS + THUMB_RADIUS;
    if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
      thumb.setFrame(pos - THUMB_RADIUS, (dim.height - r2)/ 2d, r2, r2);
    } else {
      thumb.setFrame((dim.width - r2) / 2d, pos - THUMB_RADIUS, r2, r2);
    }
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / DividerLocationDragLayerUI.ICON_GAP_DIVISOR;
    g2.setColor(Color.ORANGE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.CYAN);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
