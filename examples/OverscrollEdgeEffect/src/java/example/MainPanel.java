// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel(new ImageIcon(createMissingImage()));

    JScrollPane scroll = new JScrollPane(label) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };

    JViewport viewport = scroll.getViewport();
    KineticScrollingListener kineticListener = new KineticScrollingListener(label);
    viewport.addMouseMotionListener(kineticListener);
    viewport.addMouseListener(kineticListener);
    viewport.addHierarchyListener(kineticListener);

    OverscrollEdgeEffectLayerUI layerUi = new OverscrollEdgeEffectLayerUI();
    JLayer<JScrollPane> layer = new JLayer<>(scroll, layerUi);

    EdgeOrientation[] values = EdgeOrientation.values();
    JComboBox<EdgeOrientation> orientationCombo = new JComboBox<>(values);
    orientationCombo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        layerUi.setEdgeOrientation((EdgeOrientation) e.getItem());
        layer.repaint();
      }
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("EdgeOrientation: "));
    box.add(Box.createHorizontalStrut(2));
    box.add(orientationCombo);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(box, BorderLayout.NORTH);
    add(layer);
    scroll.setPreferredSize(new Dimension(320, 240));
  }

  private static Image createMissingImage() {
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

// Which pair of edges (top/bottom or left/right) draws the overscroll effect
enum EdgeOrientation { VERTICAL, HORIZONTAL }

class KineticScrollingListener extends MouseAdapter implements HierarchyListener {
  protected static final int SPEED = 4;
  protected static final int DELAY = 10;
  protected static final double DECELERATION = .8;
  private final Cursor defaultCursor;
  private final Cursor dragCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Timer scrollTimer = new Timer(DELAY, this::scroll);
  private final JComponent view;
  private final Point dragStartPoint = new Point();
  private final Point scrollVelocity = new Point();

  protected KineticScrollingListener(JComponent view) {
    super();
    this.view = view;
    this.defaultCursor = view.getCursor();
  }

  private void scroll(ActionEvent e) {
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(view);
    Rectangle rect = viewport.getViewRect();
    rect.translate(-scrollVelocity.x, -scrollVelocity.y);
    view.scrollRectToVisible(rect);
    if (Math.abs(scrollVelocity.x) > 0 || Math.abs(scrollVelocity.y) > 0) {
      double dx = scrollVelocity.x * DECELERATION;
      double dy = scrollVelocity.y * DECELERATION;
      scrollVelocity.setLocation((int) dx, (int) dy);
    } else {
      ((Timer) e.getSource()).stop();
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(dragCursor);
    dragStartPoint.setLocation(e.getPoint());
    scrollTimer.stop();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    int sx = SPEED * (pt.x - dragStartPoint.x);
    int sy = SPEED * (pt.y - dragStartPoint.y);
    scrollVelocity.setLocation(sx, sy);
    JViewport viewport = (JViewport) e.getComponent();
    Rectangle rect = viewport.getViewRect();
    rect.translate(dragStartPoint.x - pt.x, dragStartPoint.y - pt.y);
    view.scrollRectToVisible(rect);
    dragStartPoint.setLocation(pt);
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defaultCursor);
    scrollTimer.start();
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    long displayability = e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED;
    if (displayability != 0 && !e.getComponent().isDisplayable()) {
      scrollTimer.stop();
    }
  }
}

class OverscrollEdgeEffectLayerUI extends LayerUI<JScrollPane> {
  private static final double OVERSCROLL_RATIO = 1d / 8d;
  private static final double OVAL_ASPECT_RATIO = 2.2;
  private static final double SHRINK_RATIO = .67;
  private static final double SHRINK_STEP = .5;

  private final Color overscrollColor = new Color(0xAA_AA_EE_FF, true);
  private final Point dragPoint = new Point();
  private final Timer shrinkTimer = new Timer(20, null);
  private final Ellipse2D overscrollOval = new Ellipse2D.Double();
  private EdgeOrientation edgeOrientation = EdgeOrientation.VERTICAL;
  private boolean atLeadingEdge;
  private double overscrollSize;
  private int prevDelta;

  protected void setEdgeOrientation(EdgeOrientation orientation) {
    this.edgeOrientation = orientation;
    overscrollSize = 0d;
    prevDelta = 0;
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && overscrollSize > 0d) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle r = scroll.getViewport().getViewRect();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(overscrollColor);
      // Re-derive the position from the current viewport size so the oval
      // still touches the edge even if the layer has been resized.
      boolean vertical = edgeOrientation == EdgeOrientation.VERTICAL;
      double bound = vertical ? r.getHeight() : r.getWidth();
      double pos = atLeadingEdge ? -overscrollSize : bound - overscrollSize;
      Point2D pt = new Point();
      Dimension2D dim = new Dimension();
      if (vertical) {
        pt.setLocation(overscrollOval.getX(), pos);
        dim.setSize(overscrollOval.getWidth(), overscrollSize * 2d);
      } else {
        pt.setLocation(pos, overscrollOval.getY());
        dim.setSize(overscrollSize * 2d, overscrollOval.getHeight());
      }
      overscrollOval.setFrame(pt, dim);
      g2.fill(overscrollOval);
      g2.dispose();
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getComponent() instanceof JViewport) {
      int id = e.getID();
      if (id == MouseEvent.MOUSE_PRESSED) {
        dragPoint.setLocation(e.getPoint());
      } else if (overscrollSize > 0d && id == MouseEvent.MOUSE_RELEASED) {
        shrinkOverscroll(l);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    boolean isDragged = e.getID() == MouseEvent.MOUSE_DRAGGED;
    if (c instanceof JViewport && isDragged && !shrinkTimer.isRunning()) {
      JViewport viewport = l.getView().getViewport();
      Dimension viewSize = viewport.getView().getSize();
      Rectangle viewRect = viewport.getViewRect();
      Point p = SwingUtilities.convertPoint(c, e.getPoint(), l.getView());
      updateOverscroll(e, p, viewRect, viewSize, l);
    }
  }

  // Handles both VERTICAL (top/bottom) and HORIZONTAL (left/right) edges by
  // treating the scroll axis as the "primary" axis and the perpendicular one
  // as the "secondary" axis, then swapping them back when painting the oval.
  private void updateOverscroll(
      MouseEvent e, Point p, Rectangle r, Dimension viewSize, JLayer<? extends JScrollPane> l) {
    boolean vertical = edgeOrientation == EdgeOrientation.VERTICAL;
    int delta = vertical ? e.getPoint().y - dragPoint.y : e.getPoint().x - dragPoint.x;
    OverscrollAxis axis = new OverscrollAxis(vertical, p, r, viewSize);
    if (isDragReversed(delta)) {
      // The primary-axis drag direction has been reversed
      shrinkOverscroll(l);
    } else if (axis.primaryPos == 0 && delta >= 0) {
      // leading edge (top or left)
      atLeadingEdge = true;
      overscrollSize = Math.min(axis.primarySize, axis.primary) * OVERSCROLL_RATIO;
      double secondaryStart = axis.secondaryStart;
      double secondaryLen = axis.secondaryLen;
      double primaryLen = overscrollSize * 2d;
      setOvalFrame(vertical, secondaryStart, -overscrollSize, secondaryLen, primaryLen);
    } else if (axis.primaryBound == axis.primaryPos + axis.primarySize && delta <= 0) {
      // trailing edge (bottom or right)
      atLeadingEdge = false;
      double remaining = axis.primarySize - axis.primary;
      overscrollSize = Math.min(axis.primarySize, remaining) * OVERSCROLL_RATIO;
      double primaryStart = axis.primarySize - overscrollSize;
      double secondaryStart = axis.secondaryStart;
      double secondaryLen = axis.secondaryLen;
      double primaryLen = overscrollSize * 2d;
      setOvalFrame(vertical, secondaryStart, primaryStart, secondaryLen, primaryLen);
    }
    dragPoint.setLocation(e.getPoint());
    prevDelta = delta;
    l.repaint();
  }

  private void setOvalFrame(
      boolean vertical, double secondaryPos, double primaryPos,
      double secondaryLen, double primaryLen) {
    if (vertical) {
      overscrollOval.setFrame(secondaryPos, primaryPos, secondaryLen, primaryLen);
    } else {
      overscrollOval.setFrame(primaryPos, secondaryPos, primaryLen, secondaryLen);
    }
  }

  // Bundles the drag-axis metrics needed to size and position the overscroll
  // oval, resolving the VERTICAL/HORIZONTAL orientation only once per drag.
  private static final class OverscrollAxis {
    private final double primary;
    private final double primarySize;
    private final double primaryPos;
    private final double primaryBound;
    private final double secondaryStart;
    private final double secondaryLen;

    private OverscrollAxis(boolean vertical, Point p, Rectangle r, Dimension viewSize) {
      primary = vertical ? p.getY() : p.getX();
      primarySize = vertical ? r.getHeight() : r.getWidth();
      primaryPos = vertical ? r.y : r.x;
      primaryBound = vertical ? viewSize.height : viewSize.width;
      double secondary = vertical ? p.getX() : p.getY();
      double secondarySize = vertical ? r.getWidth() : r.getHeight();
      double halfSecondary = Math.max(secondary, secondarySize - secondary);
      secondaryStart = secondary - halfSecondary;
      secondaryLen = halfSecondary * OVAL_ASPECT_RATIO;
    }
  }

  private boolean isDragReversed(int delta) {
    boolean b1 = prevDelta > 0 && delta < 0;
    boolean b2 = prevDelta < 0 && delta > 0;
    return b1 || b2;
  }

  private void shrinkOverscroll(JLayer<? extends JScrollPane> l) {
    if (overscrollSize > 0d && !shrinkTimer.isRunning()) {
      shrinkTimer.addActionListener(e -> shrinkAnimation(l));
      shrinkTimer.start();
    }
  }

  private void shrinkAnimation(JLayer<? extends JScrollPane> l) {
    if (overscrollSize > 0d && shrinkTimer.isRunning()) {
      overscrollSize = Math.max(overscrollSize * SHRINK_RATIO - SHRINK_STEP, 0d);
      l.repaint();
    } else {
      shrinkTimer.stop();
      for (ActionListener a : shrinkTimer.getActionListeners()) {
        shrinkTimer.removeActionListener(a);
      }
    }
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
    return 640;
  }

  @Override public int getIconHeight() {
    return 1024;
  }
}
