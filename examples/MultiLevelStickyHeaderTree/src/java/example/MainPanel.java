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
    tree.setShowsRootHandles(true);
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
// Each header receives an independent push-up effect when the next expanded
// sibling node of the same depth approaches.
// 
// ### Push-up Algorithm
// - For each stickyPaths[i]:
// 1. Find the next expanded sibling nextSiblingPath[i] under the same parent.
// 2. If the Y-coordinate of nextSiblingPath[i] in the viewport goes below (i * HEADER_HEIGHT),
// 3. Record the overlap amount as pushOffsets[i] and shift stickyPaths[i] upwards.
@SuppressWarnings("PMD.OnlyOneReturn")
class StickyHeaderTreeLayerUI extends LayerUI<JScrollPane> {
  private static final int HEADER_HEIGHT = 24;
  private static final Color BORDER_COLOR = new Color(0x46_00_00_00, true);
  private static final int DEPTH = 4;

  // TreePath list of fixed headers to currently display.
  // (index 0 = top (depth 1), tail = deepest ancestor)
  private final List<TreePath> stickyPaths = new ArrayList<>();

  // Push-up amount (px) corresponding to stickyPaths[i].
  // When pushOffsets[i] > 0, stickyPaths[i] is shifted upwards by that amount,
  // and the corresponding nextSiblingPath is drawn just below it.
  private final List<Integer> pushOffsets = new ArrayList<>();

  // The TreePath of the next sibling header that pushes up stickyPaths[i].
  private final List<TreePath> nextSiblingPaths = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Updating Header State
  // -------------------------------------------------------------------------

  // Reconstructs the sticky header list and push-up information
  // from the top row of the viewport.
  private void updateHeader(JTree tree) {
    stickyPaths.clear();
    pushOffsets.clear();
    nextSiblingPaths.clear();

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
    int inheritedOffset = 0;

    for (int i = 0; i < stickyPaths.size(); i++) {
      TreePath sticky = stickyPaths.get(i);

      // Get the next expanded sibling under the same parent
      TreePath nextSibling = findNextExpandedSibling(tree, sticky);
      nextSiblingPaths.add(nextSibling);

      int stickyBaseY = i * HEADER_HEIGHT;
      int localOffset = computeOverlap(tree, nextSibling, topY, stickyBaseY);

      // If the parent is pushed up, the child must also be pushed up
      // to prevent visual overlapping
      int finalOffset = Math.max(localOffset, inheritedOffset);
      pushOffsets.add(finalOffset);
      inheritedOffset = finalOffset;
    }
  }

  // Calculate the amount that the next sibling header encroaches
  // on the current header area.
  private static int computeOverlap(
      JTree tree, TreePath nextSibling, int topY, int stickyBaseY) {
    if (nextSibling == null) {
      return 0;
    }

    int nextRow = tree.getRowForPath(nextSibling);
    Rectangle nr = tree.getRowBounds(nextRow);
    if (nr == null) {
      return 0;
    }

    int nextTopInView = nr.y - topY;
    int overlap = stickyBaseY + HEADER_HEIGHT - nextTopInView;
    return Math.min(Math.max(overlap, 0), HEADER_HEIGHT);
    // Java 21: return Math.clamp(overlap, 0, HEADER_HEIGHT);
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
    if (hints instanceof Map<?, ?> map) {
      g2.addRenderingHints(map);
    }

    JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
    Rectangle viewport = scroll.getViewport().getBounds();
    int w = viewport.width;
    int n = stickyPaths.size();

    // Draw from the bottom header up, overlaying upper headers on top (z-index)
    for (int i = n - 1; i >= 0; i--) {
      TreePath path = stickyPaths.get(i);
      int offset = pushOffsets.get(i);

      int baseY = viewport.y + i * HEADER_HEIGHT;
      int y = baseY - offset;

      int depthIdx = depthIndex(path);
      paintStickyHeader(g2, tree, c, path, viewport.x, y, w, HEADER_HEIGHT, depthIdx);

      // During push-up: Draw the next sibling header immediately below this header
      TreePath nextSibling = nextSiblingPaths.get(i);
      if (offset > 0 && nextSibling != null) {
        int nextY = baseY + HEADER_HEIGHT - offset;
        paintStickyHeader(g2, tree, c, nextSibling,
            viewport.x, nextY, w, HEADER_HEIGHT, depthIndex(nextSibling));
      }
    }
    g2.dispose();
  }

  // Draw one fixed header
  public static void paintStickyHeader(
      Graphics2D g2, JTree tree, JComponent layer, TreePath path,
      int x, int y, int w, int h, int depthIdx) {

    final Shape oldClip = g2.getClip();
    g2.setClip(x, y, w, h);
    g2.setPaint(Color.LIGHT_GRAY);
    g2.fillRect(x, y, w, h);

    // Lower border
    g2.setColor(BORDER_COLOR);
    g2.drawLine(x, y + h - 1, x + w - 1, y + h - 1);

    // Get icon and text via TreeCellRenderer
    TreeCellRenderer renderer = tree.getCellRenderer();
    Object node = path.getLastPathComponent();
    int row = tree.getRowForPath(path);
    boolean sel = tree.isPathSelected(path);
    boolean exp = tree.isExpanded(path);
    boolean leaf = tree.getModel().isLeaf(node);

    Component c = renderer.getTreeCellRendererComponent(
        tree, node, sel, exp, leaf, row, false);

    // Indentation according to depth (depth 0=0px, depth 1=10px...)
    int indent = depthIdx * 10;

    if (c instanceof JLabel label) {
      label.setOpaque(false);
      Icon icon = label.getIcon();
      int iconW = 0;
      int iconX = x + 6 + indent;
      if (icon != null) {
        int iconY = y + (h - icon.getIconHeight()) / 2;
        icon.paintIcon(layer, g2, iconX, iconY);
        iconW = icon.getIconWidth() + 4;
      }

      String text = label.getText();
      if (text != null && !text.isEmpty()) {
        FontMetrics fm = g2.getFontMetrics();
        int textX = iconX + iconW;
        int textY = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(UIManager.getColor("Tree.foreground"));
        g2.drawString(text, textX, textY);
      }
    } else {
      JPanel tmp = new JPanel();
      c.setSize(w - indent, h);
      Rectangle rect = new Rectangle(x + indent, y, w - indent, h);
      SwingUtilities.paintComponent(g2, c, tmp, rect);
    }
    g2.setClip(oldClip);
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
