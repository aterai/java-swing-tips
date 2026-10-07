// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0, 2, 2));
    JTree tree = new JTree();
    tree.setRowHeight(20);
    add(createScrollPane(tree));
    add(createScrollPane(new RoundedSelectionTree(false)));
    add(createScrollPane(new RoundedSelectionTree(true)));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane createScrollPane(Component view) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBackground(Color.WHITE);
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    return scroll;
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

final class RoundedSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0xC8_00_78_D7, true);
  private static final double ARC = 4d;
  private final boolean flatten;

  /* default */ RoundedSelectionTree(boolean flatten) {
    super();
    this.flatten = flatten;
    // Register the listener here instead of updateUI() so that it is not
    // added again each time the LookAndFeel is changed.
    addTreeSelectionListener(e -> repaint());
  }

  @Override protected void paintComponent(Graphics g) {
    int[] selectionRows = getSelectionRows();
    if (selectionRows != null && selectionRows.length > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(SELECTED_COLOR);
      Area area = new Area();
      Arrays.stream(selectionRows)
          .mapToObj(this::getRowBounds)
          .forEach(r -> area.add(new Area(r)));
      for (List<Point2D> polygon : GeomUtils.splitIntoPolygons(area)) {
        if (flatten) {
          GeomUtils.snapShortSteps(polygon, ARC * 2d);
        }
        g2.fill(GeomUtils.convertRoundedPath(polygon, ARC));
      }
      g2.dispose();
    }
    super.paintComponent(g);
  }

  @Override public void updateUI() {
    super.updateUI();
    setCellRenderer(new TransparentTreeCellRenderer());
    setOpaque(false);
    setRowHeight(20);
    UIDefaults d = new UIDefaults();
    String key = "Tree:TreeCell[Enabled+Selected].backgroundPainter";
    d.put(key, new TransparentTreeCellPainter());
    putClientProperty("Nimbus.Overrides", d);
    putClientProperty("Nimbus.Overrides.InheritDefaults", false);
  }
}

class TransparentTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final Color ALPHA_OF_ZERO = new Color(0x0, true);

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, false);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public Color getBackgroundNonSelectionColor() {
    return ALPHA_OF_ZERO;
  }

  @Override public Color getBackgroundSelectionColor() {
    return getBackgroundNonSelectionColor();
  }
}

class TransparentTreeCellPainter extends AbstractRegionPainter {
  @Override protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
    // Do nothing
  }

  @Override protected final PaintContext getPaintContext() {
    return null;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}

final class GeomUtils {
  private static final double KAPPA = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...

  private GeomUtils() {
    /* Singleton */
  }

  // Decompose a multi-loop Area into a list of polygons (single-loop vertex lists).
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public static List<List<Point2D>> splitIntoPolygons(Area area) {
    List<List<Point2D>> polygons = new ArrayList<>();
    List<Point2D> polygon = new ArrayList<>();
    PathIterator pi = area.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          polygon.add(new Point2D.Double(coords[0], coords[1]));
          break;
        case PathIterator.SEG_CLOSE:
          if (!polygon.isEmpty()) {
            polygons.add(polygon);
            polygon = new ArrayList<>();
          }
          break;
        default:
          break;
      }
      pi.next();
    }
    return polygons;
  }

  // Align a short step between rows with the outer X-coordinate
  // (the larger one on the right side, the smaller one on the left side).
  public static void snapShortSteps(List<Point2D> polygon, double arc) {
    int sz = polygon.size();
    for (int i = 0; i < sz; i++) {
      int i1 = (i + 1) % sz;
      int i2 = (i + 2) % sz;
      int i3 = (i + 3) % sz;
      Point2D pt0 = polygon.get(i);
      Point2D pt1 = polygon.get(i1);
      Point2D pt2 = polygon.get(i2);
      Point2D pt3 = polygon.get(i3);
      double dx1 = pt2.getX() - pt1.getX();
      double dy0 = pt1.getY() - pt0.getY();
      double dy2 = pt3.getY() - pt2.getY();
      // A step has vertical edges in the same direction on both sides of
      // the horizontal edge, otherwise it is the top or bottom edge of a row
      // and a narrow selection (e.g. a single "i") must not be collapsed.
      boolean isStep = dy0 * dy2 > 0d;
      if (isStep && Math.abs(dx1) > 1.0e-1 && Math.abs(dx1) < arc) {
        // The outline of an Area runs counterclockwise on the screen,
        // so the upward vertical edges are on the right side.
        double x = dy0 < 0d
            ? Math.max(pt0.getX(), pt2.getX())
            : Math.min(pt0.getX(), pt2.getX());
        replace(polygon, i, x, pt0.getY());
        replace(polygon, i1, x, pt1.getY());
        replace(polygon, i2, x, pt2.getY());
        replace(polygon, i3, x, pt3.getY());
      }
    }
  }

  private static void replace(List<Point2D> list, int i, double x, double y) {
    list.set(i, new Point2D.Double(x, y));
  }

  // Rounding the corners of a Rectilinear Polygon.
  public static Path2D convertRoundedPath(List<Point2D> list, double arc) {
    double akv = arc - arc * KAPPA;
    int sz = list.size();
    Point2D pt0 = list.get(0);
    Path2D path = new Path2D.Double();
    path.moveTo(pt0.getX() + arc, pt0.getY());
    for (int i = 0; i < sz; i++) {
      Point2D prv = list.get((i - 1 + sz) % sz);
      Point2D cur = list.get(i);
      Point2D nxt = list.get((i + 1) % sz);
      double dx0 = clampedSignum(cur.getX() - prv.getX(), arc);
      double dy0 = clampedSignum(cur.getY() - prv.getY(), arc);
      double dx1 = clampedSignum(nxt.getX() - cur.getX(), arc);
      double dy1 = clampedSignum(nxt.getY() - cur.getY(), arc);
      path.curveTo(
          cur.getX() - dx0 * akv, cur.getY() - dy0 * akv,
          cur.getX() + dx1 * akv, cur.getY() + dy1 * akv,
          cur.getX() + dx1 * arc, cur.getY() + dy1 * arc);
      path.lineTo(nxt.getX() - dx1 * arc, nxt.getY() - dy1 * arc);
    }
    path.closePath();
    return path;
  }

  // Return 0 if less than the arc.
  private static double clampedSignum(double v, double arc) {
    return Math.abs(v) < arc ? 0d : Math.signum(v);
  }
}
