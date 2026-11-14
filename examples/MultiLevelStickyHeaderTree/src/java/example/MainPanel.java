// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = createTree();
    tree.setRootVisible(false);
    tree.setShowsRootHandles(false);
    tree.setRowHeight(24);
    expandAll(tree);

    JScrollPane scroll = new JScrollPane(tree);
    scroll.getVerticalScrollBar().setUnitIncrement(tree.getRowHeight());

    StickyHeaderTreeLayerUI layerUI = new StickyHeaderTreeLayerUI();
    JLayer<JScrollPane> layer = new JLayer<>(scroll, layerUI);
    scroll.getViewport().addChangeListener(e -> layer.repaint());

    add(layer);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree createTree() {
    DefaultMutableTreeNode root = createTreeNode("Root");
    String[] catNames = {
        "Fruits",
        "Vegetables",
        "Meat/Fish",
        "Dairy products",
        "Grains/Bread",
    };
    String[][] subNames = {
        {"Domestic", "Imported", "Berries"},
        {"Root vegetables", "Leaf vegetables", "Fruit vegetables"},
        {"Meat", "Seafood"},
        {"Milk", "Cheese", "Other"},
        {"Rice", "Noodles", "Bread"},
    };
    String[][][] items = {
        {
            {"Apple", "Orange", "Peach", "Pear", "Grape", "Persimmon"},
            {"banana", "mango", "pineapple", "kiwi", "papaya"},
            {"Strawberry", "Blueberry", "Raspberry", "Cranberry"},
        },
        {
            {"Carrot", "Potato", "Sweet potato", "Radish", "Burdock"},
            {"Spinach", "Komatsuna", "Lettuce", "Cabbage", "Chinese cabbage"},
            {"Tomato", "Cucumber", "Eggplant", "Bell pepper", "Zucchini"},
        },
        {
            {"Chicken thigh", "Pork belly", "Beef loin", "Lamb", "Mixed ground"},
            {"Salmon", "Tuna", "Octopus", "Squid", "Shrimp", "Scallop"},
        },
        {
            {"Milk", "Skim milk", "Processed milk"},
            {"Camembert", "Gouda", "Mozzarella", "Parmesan"},
            {"Butter", "Yogurt", "Fresh cream", "Sour cream"},
        },
        {
            {"KOSHIHIKARI", "AKITAKOMACHI", "HITOMEBOTE", "BrownRice"},
            {"Udon", "Soba", "Pasta", "Ramen", "Somen"},
            {"Bread", "Baguette", "Croissant", "Bagel"},
        },
    };

    for (int c = 0; c < catNames.length; c++) {
      DefaultMutableTreeNode catNode = createTreeNode(catNames[c]);
      for (int s = 0; s < subNames[c].length; s++) {
        DefaultMutableTreeNode subNode = createTreeNode(subNames[c][s]);
        for (String item : items[c][s]) {
          subNode.add(createTreeNode(item));
        }
        catNode.add(subNode);
      }
      root.add(catNode);
    }
    return new JTree(root);
  }

  private static DefaultMutableTreeNode createTreeNode(String name) {
    return new DefaultMutableTreeNode(name);
  }

  private static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
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

// LayerUI that displays a multi-level sticky header for a JTree.
// Traces ancestors from the top row's TreePath in the viewport and overlays
// all expanded non-leaf ancestors as sticky headers.
// Each header receives a push-up effect when the next expanded sibling node
// of the same depth, or of one of its ancestors, approaches.
// The headers have the same background as the JTree and no border lines:
// a gradient shadow below them makes them look raised.
//
// ### Push-up Algorithm
// - For each stickyPaths[i] (from the outermost):
// 1. Find the next expanded sibling under the same parent.
// 2. Keep the smallest Y-coordinate in the viewport of the siblings found so far
//    (the siblings of the ancestors included).
// 3. If that Y-coordinate goes above the bottom of the header ((i + 1) * HEADER_HEIGHT),
//    record the overlap amount as pushOffsets[i] and shift stickyPaths[i] upwards.
// 4. The headers are clipped at that Y-coordinate, so the sibling and the rows
//    below it are the JTree's own rows.
@SuppressWarnings({"PMD.OnlyOneReturn", "ReturnCount"})
class StickyHeaderTreeLayerUI extends LayerUI<JScrollPane> {
  private static final int HEADER_HEIGHT = 24;
  private static final int SHADOW_HEIGHT = 6;
  private static final Color SHADOW_COLOR = new Color(0x40_00_00_00, true);
  private static final Color TRANSPARENT = new Color(0x0, true);
  private static final int DEPTH = 4;

  // TreePath list of fixed headers to currently display.
  // (index 0 = top (depth 1), tail = deepest ancestor)
  private final List<TreePath> stickyPaths = new ArrayList<>();

  // Push-up amount (px) corresponding to stickyPaths[i].
  // When pushOffsets[i] > 0, stickyPaths[i] is shifted upwards by that amount.
  private final List<Integer> pushOffsets = new ArrayList<>();

  // Y-coordinate in the viewport of the nearest "next expanded sibling" of the
  // sticky headers, or Integer.MAX_VALUE if there is none. The sticky headers
  // are never drawn below it, so the JTree's own rows are visible from there.
  private int pushLimit = Integer.MAX_VALUE;

  // -------------------------------------------------------------------------
  // Updating Header State
  // -------------------------------------------------------------------------

  // Reconstructs the sticky header list and push-up information
  // from the top row of the viewport.
  private void updateHeader(JTree tree) {
    stickyPaths.clear();
    pushOffsets.clear();
    pushLimit = Integer.MAX_VALUE;

    if (tree == null) {
      return;
    }

    Rectangle viewRect = tree.getVisibleRect();
    int topY = viewRect.y;
    int topRow = tree.getClosestRowForLocation(viewRect.x, topY);
    if (topRow < 0) {
      return;
    }

    TreePath topPath = tree.getPathForRow(topRow);
    if (topPath == null) {
      return;
    }

    collectStickyPaths(tree, topPath);

    if (!stickyPaths.isEmpty()) {
      calculatePushOffsets(tree, viewRect.y);
    }
  }

  // Collect headers to be fixed from the ancestor chain from the top to topPath.
  private void collectStickyPaths(JTree tree, TreePath topPath) {
    TreeModel model = tree.getModel();
    TreePath current = topPath;
    while (current != null) {
      Object node = current.getLastPathComponent();
      // Do not make the root node itself a header (assuming rootVisible=false)
      if (current.getParentPath() != null && !model.isLeaf(node) && tree.isExpanded(current)) {
        // To add backwards, either reverse it later or always insert it at the beginning.
        stickyPaths.add(0, current);
        // Java 21: stickyPaths.addFirst(current);
      }
      current = current.getParentPath();
    }
  }

  // Calculate the amount of collision/uplift with the "next expanded sibling"
  // for each fixed header.
  private void calculatePushOffsets(JTree tree, int topY) {
    // The top of the nearest "next expanded sibling" of this header and of its
    // ancestors: the next sibling of an ancestor also ends the subtree of this
    // header, so it must push this header up as well.
    int limit = Integer.MAX_VALUE;
    for (int i = 0; i < stickyPaths.size(); i++) {
      limit = Math.min(limit, getNextSiblingTop(tree, stickyPaths.get(i), topY));
      // The amount that the sibling encroaches on the current header area.
      int overlap = (i + 1) * HEADER_HEIGHT - limit;
      pushOffsets.add(Math.min(Math.max(overlap, 0), HEADER_HEIGHT));
      // Java 21: pushOffsets.add(Math.clamp(overlap, 0, HEADER_HEIGHT));
    }
    pushLimit = limit;
  }

  // Returns the Y-coordinate in the viewport of the "next expanded sibling"
  // of the specified TreePath, or Integer.MAX_VALUE if there is none.
  private static int getNextSiblingTop(JTree tree, TreePath path, int topY) {
    return Optional.ofNullable(findNextExpandedSibling(tree, path))
        .map(nextSibling -> tree.getRowBounds(tree.getRowForPath(nextSibling)))
        .map(nr -> nr.y - topY)
        .orElse(Integer.MAX_VALUE);
  }

  // Returns the "next expanded sibling" under the same parent
  // as the specified TreePath.
  private static TreePath findNextExpandedSibling(JTree tree, TreePath path) {
    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return null;
    }
    Object parent = parentPath.getLastPathComponent();
    Object current = path.getLastPathComponent();
    TreeModel model = tree.getModel();
    int childCount = model.getChildCount(parent);
    boolean found = false;
    for (int i = 0; i < childCount; i++) {
      Object child = model.getChild(parent, i);
      if (found) {
        // Return the first expanded sibling found after current
        TreePath siblingPath = parentPath.pathByAddingChild(child);
        if (!model.isLeaf(child) && tree.isExpanded(siblingPath)) {
          return siblingPath;
        }
      }
      if (Objects.equals(child, current)) {
        found = true;
      }
    }
    return null;
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);

    JTree tree = getTree(c);
    if (tree == null) {
      return;
    }

    updateHeader(tree);

    if (stickyPaths.isEmpty()) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Object hints = toolkit.getDesktopProperty("awt.font.desktophints");
    if (hints instanceof Map) {
      g2.addRenderingHints((Map<?, ?>) hints);
    }

    JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
    Rectangle viewport = scroll.getViewport().getBounds();
    g2.clip(viewport);
    int n = stickyPaths.size();

    // The innermost header is always the lowest one, even while it is pushed up.
    // The rows from the pushing sibling downwards are the JTree's own rows,
    // so the sticky header area must end at the top of that sibling.
    int bottom = Math.min(n * HEADER_HEIGHT - pushOffsets.get(n - 1), pushLimit);

    Graphics2D g3 = (Graphics2D) g2.create();
    g3.clipRect(viewport.x, viewport.y, viewport.width, bottom);
    Rectangle r = new Rectangle(viewport.x, viewport.y, viewport.width, HEADER_HEIGHT);
    // Draw from the bottom header up, overlaying upper headers on top (z-index)
    for (int i = n - 1; i >= 0; i--) {
      TreePath path = stickyPaths.get(i);
      r.y = viewport.y + i * HEADER_HEIGHT - pushOffsets.get(i);
      paintStickyHeader(g3, tree, path, r, depthIndex(path));
    }
    g3.dispose();

    paintShadow(g2, viewport, viewport.y + bottom);
    g2.dispose();
  }

  // Draw one fixed header with the same background as the JTree
  public static void paintStickyHeader(
      Graphics2D g, JTree tree, TreePath path, Rectangle r, int depthIdx) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.clip(r);
    g2.setPaint(tree.getBackground());
    g2.fill(r);

    // Get icon and text via TreeCellRenderer
    Component c = getTreeCellRendererComponent(tree, path);

    if (c instanceof JLabel) {
      JLabel label = cloneLabel((JLabel) c, tree);
      int leftChildIndent = UIManager.getInt("Tree.leftChildIndent");
      int rightChildIndent = UIManager.getInt("Tree.rightChildIndent");
      // leftChildIndent + rightChildIndent = 16px
      // Indentation according to depth (depth 0=0px, depth 1=16px...)
      int indent = depthIdx * (leftChildIndent + rightChildIndent);
      label.setSize(r.width - indent, r.height);
      Rectangle rect = new Rectangle(r.x + indent, r.y, r.width - indent, r.height);
      SwingUtilities.paintComponent(g2, label, new JPanel(), rect);
    }
    g2.dispose();
  }

  // Draw a gradient shadow below the sticky headers instead of border lines.
  private static void paintShadow(Graphics2D g, Rectangle area, int y) {
    g.setPaint(new GradientPaint(0f, y, SHADOW_COLOR, 0f, y + SHADOW_HEIGHT, TRANSPARENT));
    g.fillRect(area.x, y, area.width, SHADOW_HEIGHT);
  }

  private static JLabel cloneLabel(JLabel src, JTree tree) {
    JLabel label = new JLabel(src.getText());
    label.setIcon(src.getIcon());
    label.setIconTextGap(src.getIconTextGap());
    label.setOpaque(true);
    label.setBackground(tree.getBackground());
    label.setForeground(tree.getForeground());
    return label;
  }

  private static Component getTreeCellRendererComponent(JTree tree, TreePath path) {
    TreeCellRenderer renderer = tree.getCellRenderer();
    Object node = path.getLastPathComponent();
    int row = tree.getRowForPath(path);
    boolean sel = tree.isPathSelected(path);
    boolean exp = tree.isExpanded(path);
    boolean leaf = tree.getModel().isLeaf(node);
    return renderer.getTreeCellRendererComponent(
        tree, node, sel, exp, leaf, row, false);
  }

  // Returns the depth index of the TreePath (0 = just below the root)
  private static int depthIndex(TreePath path) {
    int index = path.getPathCount() - 2;
    int max = DEPTH - 1;
    return Math.min(Math.max(index, 0), max);
    // Java 21: return Math.clamp(index, 0, max);
  }

  // Get the JTree inside the JLayer (Null if not found)
  private static JTree getTree(JComponent c) {
    return Optional.ofNullable(c)
        .filter(JLayer.class::isInstance).map(JLayer.class::cast)
        .map(JLayer::getView)
        .filter(JScrollPane.class::isInstance).map(JScrollPane.class::cast)
        .map(scroll -> scroll.getViewport().getView())
        .filter(JTree.class::isInstance).map(JTree.class::cast)
        .orElse(null);
  }
}
