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
import javax.swing.*;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0, 2, 2));
    JTree tree = new JTree();
    tree.setRowHeight(20);
    add(makeScrollPane(tree));
    add(makeScrollPane(new RoundedSelectionTree0()));
    add(makeScrollPane(new RoundedSelectionTree()));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeScrollPane(Component view) {
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

class RoundedSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0xC8_00_78_D7, true);

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
      double arc = 4d;
      for (Area a : GeomUtils.singularization(area)) {
        List<Point2D> lst = GeomUtils.convertAreaToPoint2DList(a);
        GeomUtils.flatteningStepsOnRightSide(lst, arc * 2d);
        g2.fill(GeomUtils.convertRoundedPath(lst, arc));
        // g2.fill(GeomUtils.drawRoundedPolygon(lst, arc));
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
    putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);
    addTreeSelectionListener(e -> repaint());
  }
}

class RoundedSelectionTree0 extends JTree {
  private static final Color SELECTED_COLOR = new Color(0xC8_00_78_D7, true);

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
      double arc = 4d;
      for (Area a : GeomUtils.singularization(area)) {
        List<Point2D> lst = GeomUtils.convertAreaToPoint2DList(a);
        // GeomUtils.convertFlatten(lst, arc * 2d);
        g2.fill(GeomUtils.convertRoundedPath(lst, arc));
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
    putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);
    addTreeSelectionListener(e -> repaint());
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
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
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
        ex.printStackTrace();
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
  private GeomUtils() {
    /* Singleton */
  }

  public static List<Point2D> convertAreaToPoint2DList(Area area) {
    List<Point2D> list = new ArrayList<>();
    PathIterator pi = area.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          list.add(new Point2D.Double(coords[0], coords[1]));
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }

  public static void flatteningStepsOnRightSide(List<Point2D> list, double arc) {
    int sz = list.size();
    for (int i = 0; i < sz; i++) {
      int i1 = (i + 1) % sz;
      int i2 = (i + 2) % sz;
      int i3 = (i + 3) % sz;
      Point2D pt0 = list.get(i);
      Point2D pt1 = list.get(i1);
      Point2D pt2 = list.get(i2);
      Point2D pt3 = list.get(i3);
      double dx1 = pt2.getX() - pt1.getX();
      if (Math.abs(dx1) > 1.0e-1 && Math.abs(dx1) < arc) {
        double max = Math.max(pt0.getX(), pt2.getX());
        replace(list, i, max, pt0.getY());
        replace(list, i1, max, pt1.getY());
        replace(list, i2, max, pt2.getY());
        replace(list, i3, max, pt3.getY());
      }
    }
  }

  private static void replace(List<Point2D> list, int i, double x, double y) {
    list.remove(i);
    list.add(i, new Point2D.Double(x, y));
  }

  /**
   * Rounding the corners of a Rectilinear Polygon.
   */
  public static Path2D convertRoundedPath(List<Point2D> list, double arc) {
    double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...;
    double akv = arc - arc * kappa;
    int sz = list.size();
    Point2D pt0 = list.get(0);
    Path2D path = new Path2D.Double();
    path.moveTo(pt0.getX() + arc, pt0.getY());
    for (int i = 0; i < sz; i++) {
      Point2D prv = list.get((i - 1 + sz) % sz);
      Point2D cur = list.get(i);
      Point2D nxt = list.get((i + 1) % sz);
      double dx0 = signum(cur.getX() - prv.getX(), arc);
      double dy0 = signum(cur.getY() - prv.getY(), arc);
      double dx1 = signum(nxt.getX() - cur.getX(), arc);
      double dy1 = signum(nxt.getY() - cur.getY(), arc);
      path.curveTo(
          cur.getX() - dx0 * akv, cur.getY() - dy0 * akv,
          cur.getX() + dx1 * akv, cur.getY() + dy1 * akv,
          cur.getX() + dx1 * arc, cur.getY() + dy1 * arc);
      path.lineTo(nxt.getX() - dx1 * arc, nxt.getY() - dy1 * arc);
    }
    path.closePath();
    return path;
  }

  private static double signum(double v, double arc) {
    return Math.abs(v) < arc ? 0d : Math.signum(v);
  }

  public static List<Area> singularization(Area rect) {
    List<Area> list = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = rect.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          list.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }

  // // https://stackoverflow.com/questions/26995884/polygon-with-rounded-corners
  // public static Path2D drawRoundedPolygon(List<Point2D> points, double radius) {
  //   List<Point2D> closed = convertToClosed(points, radius);
  //   Point2D pt0 = points.get(0);
  //   Path2D path = new Path2D.Double();
  //   path.moveTo(pt0.getX(), pt0.getY());
  //   for (int i = 0, last = closed.size(); i < last; i += 3) {
  //     Point2D p1 = closed.get(i);
  //     Point2D p2 = closed.get(i + 1);
  //     Point2D p3 = closed.get(i + 2);
  //     // rounded isosceles triangle connector values:
  //     double[] c = roundIsosceles(p1, p2, p3, .75);
  //     // tell Processing that we have points to add to our shape:
  //     path.lineTo(p1.getX(), p1.getY());
  //     path.curveTo(c[0], c[1], c[2], c[3], p3.getX(), p3.getY());
  //   }
  //   path.closePath();
  //   return path;
  // }
  //
  // public static List<Point2D> convertToClosed(List<Point2D> points, double radius) {
  //   // this value *actually* depends on the angle between the lines.
  //   // a 180-degree angle means f can be 1, a 10-degree angle needs
  //   // an f closer to 4!
  //   // double f = 2.5f;
  //   List<Point2D> closed = new ArrayList<>();
  //   int last = points.size();
  //   for (int i = 0; i < last; i++) {
  //     Point2D p1 = points.get(i);
  //     Point2D p2 = points.get((i + 1) % last);
  //     Point2D p3 = points.get((i + 2) % last);
  //     double dx1 = p2.getX() - p1.getX();
  //     double dy1 = p2.getY() - p1.getY();
  //     double m1 = Math.hypot(dx1, dy1);
  //     closed.add(new Point2D.Double(
  //         p2.getX() - radius * dx1 / m1,
  //         p2.getY() - radius * dy1 / m1));
  //     closed.add(p2);
  //     double dx2 = p3.getX() - p2.getX();
  //     double dy2 = p3.getY() - p2.getY();
  //     double m2 = Math.hypot(dx2, dy2);
  //     closed.add(new Point2D.Double(
  //         p2.getX() + radius * dx2 / m2,
  //         p2.getY() + radius * dy2 / m2));
  //   }
  //   return closed;
  // }
  //
  // public static double[] roundIsosceles(Point2D p1, Point2D p2, Point2D p3, double t) {
  //   double mt = 1d - t;
  //   double c1x = mt * p1.getX() + t * p2.getX();
  //   double c1y = mt * p1.getY() + t * p2.getY();
  //   double c2x = mt * p3.getX() + t * p2.getX();
  //   double c2y = mt * p3.getY() + t * p2.getY();
  //   return new double[]{
  //       c1x, c1y, c2x, c2y
  //   };
  // }
}
