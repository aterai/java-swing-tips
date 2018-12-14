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
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JSplitPane split = new JSplitPane();
    split.setContinuousLayout(true);
    split.setResizeWeight(.5);
    split.setDividerSize(0);

    ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));

    BufferedImage source = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = source.createGraphics();
    g.drawImage(icon.getImage(), 0, 0, null);
    g.dispose();
    ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    BufferedImage destination = colorConvert.filter(source, null);

    Component beforeCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int iw = icon.getIconWidth();
        int ih = icon.getIconHeight();
        Dimension dim = split.getSize();
        int x = (dim.width - iw) / 2;
        int y = (dim.height - ih) / 2;
        g.drawImage(icon.getImage(), x, y, iw, ih, this);
      }
    };
    split.setLeftComponent(beforeCanvas);

    Component afterCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Insets ins = split.getBorder().getBorderInsets(split);
        g2.translate(-getLocation().x + ins.left, 0);

        int iw = destination.getWidth(this);
        int ih = destination.getHeight(this);
        Dimension dim = split.getSize();
        int x = (dim.width - iw) / 2;
        int y = (dim.height - ih) / 2;
        g2.drawImage(destination, x, y, iw, ih, this);
        g2.dispose();
      }
    };
    split.setRightComponent(afterCanvas);

    DividerLocationDragLayerUI layerUI = new DividerLocationDragLayerUI();
    JCheckBox check = new JCheckBox("Paint divider");
    check.addActionListener(e -> layerUI.setPaintDividerEnabled(((JCheckBox) e.getSource()).isSelected()));

    add(new JLayer<>(split, layerUI));
    add(check, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
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

class DividerLocationDragLayerUI extends LayerUI<JSplitPane> {
  private static final int R = 25;
  private final Point startPt = new Point();
  private final Cursor dc = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private final Cursor wc = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
  private final Ellipse2D thumb = new Ellipse2D.Double();
  private int dividerLocation;
  private boolean isDragging;
  private boolean isEnter;
  private boolean dividerEnabled;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
      updateThumbLocation(((JLayer<?>) c).getView(), thumb);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(new Color(255, 100, 100, 100));
      g2.fill(thumb);
      if (dividerEnabled) {
        paintDivider(g2);
      }
      g2.dispose();
    }
  }

  private void paintDivider(Graphics2D g2) {
    g2.setStroke(new BasicStroke(5f));
    g2.setPaint(Color.WHITE);
    g2.draw(thumb);

    double cx = thumb.getCenterX();
    double cy = thumb.getCenterY();

    Line2D line = new Line2D.Double(cx, 0d, cx, thumb.getMinY());
    g2.draw(line);

    double v = 8d;
    double mx = cx - thumb.getWidth() / 4d + v / 2d;
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(mx, cy - v);
    triangle.lineTo(mx - v,  cy);
    triangle.lineTo(mx, cy + v);
    triangle.lineTo(mx, cy - v);
    triangle.closePath();
    g2.fill(triangle);

    AffineTransform at = AffineTransform.getQuadrantRotateInstance(2, cx, cy);
    g2.draw(at.createTransformedShape(line));
    g2.fill(at.createTransformedShape(triangle));
  }

  public void setPaintDividerEnabled(boolean flg) {
    this.dividerEnabled = flg;
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
        Component c = e.getComponent();
        if (isDraggableComponent(splitPane, c)) {
          Point pt = SwingUtilities.convertPoint(c, e.getPoint(), splitPane);
          isDragging = thumb.contains(pt);
          startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), splitPane));
          dividerLocation = splitPane.getDividerLocation();
        }
        break;
      default:
        break;
    }
    splitPane.repaint();
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JSplitPane> l) {
    JSplitPane splitPane = l.getView();
    Component c = e.getComponent();
    Point pt = SwingUtilities.convertPoint(c, e.getPoint(), splitPane);
    if (e.getID() == MouseEvent.MOUSE_MOVED) {
      splitPane.setCursor(thumb.contains(e.getPoint()) ? wc : dc);
    } else if (isDragging && isDraggableComponent(splitPane, c) && e.getID() == MouseEvent.MOUSE_DRAGGED) {
      int delta = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? pt.x - startPt.x : pt.y - startPt.y;
      splitPane.setDividerLocation(Math.max(0, dividerLocation + delta));
    }
  }

  private static boolean isDraggableComponent(JSplitPane splitPane, Component c) {
    return Objects.equals(splitPane, c) || Objects.equals(splitPane, SwingUtilities.getUnwrappedParent(c));
  }

  private static void updateThumbLocation(Component c, Ellipse2D thumb) {
    if (c instanceof JSplitPane) {
      JSplitPane splitPane = (JSplitPane) c;
      int pos = splitPane.getDividerLocation();
      if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
        thumb.setFrame(pos - R, splitPane.getHeight() / 2 - R, R + R, R + R);
      } else {
        thumb.setFrame(splitPane.getWidth() / 2 - R, pos - R, R + R, R + R);
      }
    }
  }
}
