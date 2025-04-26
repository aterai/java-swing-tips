// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    Icon emptyIcon = new EmptyIcon();
    UIManager.put("Tree.openIcon", emptyIcon);
    UIManager.put("Tree.closedIcon", emptyIcon);
    UIManager.put("Tree.leafIcon", emptyIcon);
    UIManager.put("Tree.expandedIcon", emptyIcon);
    UIManager.put("Tree.collapsedIcon", emptyIcon);
    UIManager.put("Tree.leftChildIndent", 10);
    UIManager.put("Tree.rightChildIndent", 0);
    UIManager.put("Tree.paintLines", false);

    TreeModel model = makeModel();
    CardLayout cardLayout = new CardLayout();
    JPanel p = new JPanel(cardLayout);
    // https://ateraimemo.com/Swing/TraverseAllNodes.html
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // // Java 9: Collections.list(root.postorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.postorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(node -> Objects.toString(node.getUserObject()))
        .forEach(title -> p.add(new JLabel(title), title));

    JTree tree = new RowSelectionTree();
    tree.setModel(model);
    tree.setRowHeight(32);
    tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    // https://ateraimemo.com/Swing/ExpandAllNodes.html
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(e -> {
      // https://ateraimemo.com/Swing/CardLayoutTabbedPane.html
      Object o = e.getNewLeadSelectionPath().getLastPathComponent();
      if (o instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
        String title = Objects.toString(node.getUserObject());
        cardLayout.show(p, title);
      }
    });

    JScrollPane s1 = new JScrollPane(tree);
    JScrollPane s2 = new JScrollPane(p);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("1. Introduction");
    root.add(c1);

    DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("2. Chapter");
    c2.add(new DefaultMutableTreeNode("2.1. Section"));
    c2.add(new DefaultMutableTreeNode("2.2. Section"));
    c2.add(new DefaultMutableTreeNode("2.3. Section"));
    root.add(c2);

    DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("3. Chapter");
    c3.add(new DefaultMutableTreeNode("3.1. Section"));
    c3.add(new DefaultMutableTreeNode("3.2. Section"));
    c3.add(new DefaultMutableTreeNode("3.3. Section"));
    c3.add(new DefaultMutableTreeNode("3.4. Section"));
    root.add(c3);

    return new DefaultTreeModel(root);
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

// https://ateraimemo.com/Swing/TreeRowSelection.html
class RowSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x64_96_C8);
  private transient TreeWillExpandListener listener;

  @Override protected void paintComponent(Graphics g) {
    int[] sr = getSelectionRows();
    if (sr == null) {
      super.paintComponent(g);
      return;
    }
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(SELECTED_COLOR);
    Arrays.stream(sr).mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    super.paintComponent(g);
    if (hasFocus()) {
      Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
        Rectangle r = getRowBounds(getRowForPath(path));
        g2.setPaint(SELECTED_COLOR.darker());
        g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      });
    }
    g2.dispose();
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    removeTreeWillExpandListener(listener);
    super.updateUI();
    setUI(new BasicTreeUI() {
      @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
        Rectangle r = null;
        if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
          r = getTreePathBounds(path, tree.getInsets(), new Rectangle());
        }
        return r;
      }

      private Rectangle getTreePathBounds(TreePath path, Insets insets, Rectangle bounds) {
        Rectangle rect = treeState.getBounds(path, bounds);
        if (Objects.nonNull(rect)) {
          rect.width = tree.getWidth();
          rect.y += insets.top;
        }
        return rect;
      }
    });
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    TreeCellRenderer r = getCellRenderer();
    setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      Component c = r.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      c.setBackground(selected ? SELECTED_COLOR : tree.getBackground());
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(true);
      }
      return c;
    });
    setOpaque(false);
    setRootVisible(false);
    // https://ateraimemo.com/Swing/TreeNodeCollapseVeto.html
    listener = new TreeWillExpandListener() {
      @Override public void treeWillExpand(TreeExpansionEvent e) {
        // throw new ExpandVetoException(e, "Tree expansion cancelled");
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
        throw new ExpandVetoException(e, "Tree collapse cancelled");
      }
    };
    addTreeWillExpandListener(listener);
  }
}

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
