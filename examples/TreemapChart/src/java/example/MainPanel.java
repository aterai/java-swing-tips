// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    Random rnd = new Random();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(DefaultMutableTreeNode::isLeaf)
        .forEach(n -> {
          String title = Objects.toString(n.getUserObject(), "");
          n.setUserObject(new WeightedItem(title, 10 + rnd.nextInt(180)));
        });

    JScrollPane scroll = new JScrollPane(tree);
    scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    TreemapPanel treemap = new TreemapPanel(model);
    treemap.setBackground(Color.WHITE);
    treemap.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    tree.addTreeSelectionListener(e ->
        treemap.setSelectionPath(e.getNewLeadSelectionPath()));
    treemap.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        TreePath path = treemap.getPathAt(e.getPoint());
        if (path == null) {
          tree.clearSelection();
        } else {
          tree.setSelectionPath(path);
          tree.scrollPathToVisible(path);
        }
      }
    });

    add(scroll, BorderLayout.WEST);
    add(treemap);
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

class WeightedItem {
  private final String text;
  private final int weight;

  protected WeightedItem(String text, int weight) {
    this.text = text;
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }

  @Override public String toString() {
    return text;
  }
}

class TreemapPanel extends JPanel {
  private static final double GAP = 1d;
  private final transient TreeModel model;
  private final transient Map<DefaultMutableTreeNode, Rectangle2D> rectMap
      = new ConcurrentHashMap<>();
  private transient DefaultMutableTreeNode selection;

  protected TreemapPanel(TreeModel model) {
    super();
    this.model = model;
  }

  @SuppressWarnings("PMD.NullAssignment")
  public void setSelectionPath(TreePath path) {
    Object o = path == null ? null : path.getLastPathComponent();
    selection = o instanceof DefaultMutableTreeNode ? (DefaultMutableTreeNode) o : null;
    repaint();
  }

  public TreePath getPathAt(Point pt) {
    // Prefer the deepest node when nested rectangles overlap
    return rectMap.entrySet().stream()
        .filter(e -> e.getValue().contains(pt))
        .map(Map.Entry::getKey)
        .max(Comparator.comparingInt(DefaultMutableTreeNode::getLevel))
        .map(n -> new TreePath(n.getPath()))
        .orElse(null);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    Object root = model.getRoot();
    if (root instanceof DefaultMutableTreeNode && !rect.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      rectMap.clear();
      drawTreemap(g2, (DefaultMutableTreeNode) root, rect);
      drawSelection(g2);
      g2.dispose();
    }
  }

  private void drawTreemap(Graphics2D g2, DefaultMutableTreeNode root, Rectangle rect) {
    List<DefaultMutableTreeNode> children = getChildren(root);
    Rectangle2D area = new Rectangle2D.Double(
        rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    List<Rectangle2D> rects = SquarifyUtils.squarify(getWeights(children), area);
    double maxWeight = getMaxLeafWeight(root);
    for (int i = 0; i < children.size(); i++) {
      float hue = i / (float) children.size();
      drawNode(g2, children.get(i), rects.get(i), hue, maxWeight);
    }
  }

  private void drawNode(
      Graphics2D g2, DefaultMutableTreeNode node, Rectangle2D area, float hue, double maxWeight) {
    Rectangle2D rect = deflate(area, GAP);
    if (rect.isEmpty()) {
      return;
    }
    rectMap.put(node, rect);
    if (node.isLeaf()) {
      drawLeaf(g2, node, rect, hue, maxWeight);
    } else {
      List<DefaultMutableTreeNode> children = getChildren(node);
      List<Rectangle2D> rects = SquarifyUtils.squarify(getWeights(children), rect);
      for (int i = 0; i < children.size(); i++) {
        drawNode(g2, children.get(i), rects.get(i), hue, maxWeight);
      }
    }
  }

  private void drawLeaf(
      Graphics2D g2, DefaultMutableTreeNode node, Rectangle2D rect, float hue, double maxWeight) {
    // The bigger the weight, the more saturated the color
    float sat = (float) (.2f + .6f * getWeight(node) / maxWeight);
    Color color = Color.getHSBColor(hue, sat, .95f);
    g2.setColor(color);
    g2.fill(rect);
    g2.setColor(color.darker());
    g2.draw(rect);
    g2.setColor(getTextColor(color));
    drawLabel(g2, Objects.toString(node.getUserObject(), ""), rect);
  }

  private static Color getTextColor(Color bg) {
    // ITU-R BT.601 luma; use white text on dark backgrounds
    double luma = .299 * bg.getRed() + .587 * bg.getGreen() + .114 * bg.getBlue();
    return luma < 127.5 ? Color.WHITE : Color.BLACK;
  }

  private void drawSelection(Graphics2D g2) {
    Rectangle2D rect = selection == null ? null : rectMap.get(selection);
    if (rect != null) {
      if (!selection.isLeaf()) {
        g2.setColor(new Color(0x64_FF_FF_FF, true));
        g2.fill(rect);
      }
      g2.setStroke(new BasicStroke(2f));
      g2.setColor(Color.BLACK);
      g2.draw(rect);
    }
  }

  private static void drawLabel(Graphics2D g2, String txt, Rectangle2D rect) {
    FontMetrics fm = g2.getFontMetrics();
    Rectangle2D b = fm.getStringBounds(txt, g2);
    if (b.getWidth() < rect.getWidth() - 2d && b.getHeight() < rect.getHeight()) {
      float tx = (float) (rect.getCenterX() - b.getWidth() / 2d);
      float ty = (float) (rect.getCenterY() - b.getHeight() / 2d + fm.getAscent());
      g2.drawString(txt, tx, ty);
    }
  }

  private static List<DefaultMutableTreeNode> getChildren(DefaultMutableTreeNode node) {
    // Java 9: Collections.list(node.children()).stream()
    return Collections.list((Enumeration<?>) node.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .collect(Collectors.toList());
  }

  private static double getWeight(DefaultMutableTreeNode node) {
    double weight;
    if (node.isLeaf()) {
      Object uo = node.getUserObject();
      weight = uo instanceof WeightedItem ? ((WeightedItem) uo).getWeight() : 1d;
    } else {
      weight = getChildren(node).stream().mapToDouble(TreemapPanel::getWeight).sum();
    }
    return weight;
  }

  private static List<Double> getWeights(List<DefaultMutableTreeNode> nodes) {
    return nodes.stream().map(TreemapPanel::getWeight).collect(Collectors.toList());
  }

  private static double getMaxLeafWeight(DefaultMutableTreeNode root) {
    return Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(DefaultMutableTreeNode::isLeaf)
        .mapToDouble(TreemapPanel::getWeight)
        .max()
        .orElse(1d);
  }

  private static Rectangle2D deflate(Rectangle2D r, double d) {
    return new Rectangle2D.Double(
        r.getX() + d, r.getY() + d, r.getWidth() - d - d, r.getHeight() - d - d);
  }
}

// Squarified Treemaps: Mark Bruls, Kees Huizing, and Jarke J. van Wijk
// https://www.win.tue.nl/~vanwijk/stm.pdf
final class SquarifyUtils {
  private SquarifyUtils() {
    /* Singleton */
  }

  public static List<Rectangle2D> squarify(List<Double> weights, Rectangle2D area) {
    int size = weights.size();
    List<Rectangle2D> rects = new ArrayList<>(Collections.nCopies(size, area));
    double total = weights.stream().mapToDouble(Double::doubleValue).sum();
    if (total > 0d && !area.isEmpty()) {
      double scale = area.getWidth() * area.getHeight() / total;
      Rectangle2D rest = new Rectangle2D.Double();
      rest.setRect(area);
      int idx = 0;
      while (idx < size && !rest.isEmpty()) {
        idx = layoutRow(weights, scale, idx, rest, rects);
      }
    }
    return rects;
  }

  // Lay out one row, shrink the remaining area, and return the next start index
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static int layoutRow(
      List<Double> weights, double scale, int idx, Rectangle2D rest, List<Rectangle2D> rects) {
    boolean horizontal = rest.getWidth() >= rest.getHeight();
    double side = horizontal ? rest.getHeight() : rest.getWidth();
    // A row that ends where it starts would leave the remaining area
    // unchanged: consume at least one rectangle to avoid an infinite loop
    int end = Math.max(getRowEnd(weights, scale, idx, side), idx + 1);
    double rowSum = weights.subList(idx, end).stream()
        .mapToDouble(Double::doubleValue).sum() * scale;
    double thickness = rowSum / side;
    double offset = 0d;
    for (int i = idx; i < end; i++) {
      double len = weights.get(i) * scale / thickness;
      Rectangle2D r;
      if (horizontal) {
        r = new Rectangle2D.Double(rest.getX(), rest.getY() + offset, thickness, len);
      } else {
        r = new Rectangle2D.Double(rest.getX() + offset, rest.getY(), len, thickness);
      }
      rects.set(i, r);
      offset += len;
    }
    if (horizontal) {
      rest.setRect(rest.getX() + thickness, rest.getY(),
          rest.getWidth() - thickness, rest.getHeight());
    } else {
      rest.setRect(rest.getX(), rest.getY() + thickness,
          rest.getWidth(), rest.getHeight() - thickness);
    }
    return end;
  }

  // Grow the current row while the worst aspect ratio improves
  private static int getRowEnd(List<Double> weights, double scale, int idx, double side) {
    double rowSum = 0d;
    double min = Double.MAX_VALUE;
    double max = 0d;
    double worst = Double.MAX_VALUE;
    int end = idx;
    while (end < weights.size()) {
      double a = weights.get(end) * scale;
      double w = worstAspect(rowSum + a, Math.min(min, a), Math.max(max, a), side);
      if (w > worst) {
        break;
      }
      worst = w;
      rowSum += a;
      min = Math.min(min, a);
      max = Math.max(max, a);
      end += 1;
    }
    return end;
  }

  private static double worstAspect(double sum, double min, double max, double side) {
    double s2 = sum * sum;
    double w2 = side * side;
    return Math.max(w2 * max / s2, s2 / (w2 * min));
  }
}
