// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree defaultTree = new JTree(new DefaultTreeModel(createTreeRoot())) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new ToolTipCellRenderer(getCellRenderer()));
      }
    };
    ToolTipManager.sharedInstance().registerComponent(defaultTree);

    JTree aboveCellTree = new AboveCellToolTipTree(new DefaultTreeModel(createTreeRoot()));
    ToolTipManager.sharedInstance().registerComponent(aboveCellTree);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(createTitledPanel("Default location", defaultTree));
    p.add(createTitledPanel("Draw directly above the cell", aboveCellTree));

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, p, new JPanel());
    split.setResizeWeight(.5);
    split.setDividerLocation(160);

    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultMutableTreeNode createTreeRoot() {
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 00000004");
    set4.add(new DefaultMutableTreeNode("222222111111111111111122222"));
    set4.add(new DefaultMutableTreeNode("00000000000"));
    set4.add(new DefaultMutableTreeNode("1111111111"));
    set4.add(new DefaultMutableTreeNode("22222222"));

    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 00000001");
    set1.add(new DefaultMutableTreeNode("33333333333333333333333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 00000002");
    set2.add(new DefaultMutableTreeNode("333333333"));
    set2.add(new DefaultMutableTreeNode("4444444444444"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 00000003");
    set3.add(new DefaultMutableTreeNode("5555555555"));
    set3.add(new DefaultMutableTreeNode("66666666666"));
    set3.add(new DefaultMutableTreeNode("7777777777"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("88888888888888888"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("9999999999999999999999999"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("22222222222222222222222222222222222"));
    root.add(new DefaultMutableTreeNode("1111111111111111111111111"));
    return root;
  }

  private static Component createTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(scroll);
    return p;
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

class ToolTipCellRenderer implements TreeCellRenderer {
  private final TreeCellRenderer renderer;

  protected ToolTipCellRenderer(TreeCellRenderer renderer) {
    this.renderer = renderer;
  }

  @Override public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (c instanceof JComponent) {
      ((JComponent) c).setToolTipText(Objects.toString(value, null));
    }
    return c;
  }
}

class AboveCellToolTipTree extends JTree {
  private final JLabel label = new JLabel();

  protected AboveCellToolTipTree(TreeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(new ToolTipCellRenderer(getCellRenderer()));
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    // getPathForLocation(...) returns null unless the point is inside the node cell bounds
    TreePath path = getPathForLocation(e.getX(), e.getY());
    Rectangle cellBounds = getPathBounds(path);
    Point pt = null;
    if (Objects.nonNull(path) && Objects.nonNull(cellBounds)) {
      int row = getRowForPath(path);
      Object node = path.getLastPathComponent();
      boolean hasFocus = hasFocus() && getLeadSelectionRow() == row;
      boolean isLeaf = getModel().isLeaf(node);
      Component c = getCellRenderer().getTreeCellRendererComponent(
          this, node, isRowSelected(row), isExpanded(row), isLeaf, row, hasFocus);
      if (c instanceof JComponent && Objects.nonNull(((JComponent) c).getToolTipText())) {
        Insets ins = label.getInsets();
        pt = cellBounds.getLocation();
        pt.translate(-ins.left, -ins.top);
        label.setIcon(new RendererIcon(c, this, cellBounds.getSize()));
      }
    }
    return pt;
  }

  @Override public JToolTip createToolTip() {
    JToolTip tip = new JToolTip() {
      @Override public Dimension getPreferredSize() {
        return label.getPreferredSize();
      }
    };
    tip.setBorder(BorderFactory.createEmptyBorder());
    tip.setLayout(new BorderLayout());
    tip.setComponent(this);
    tip.add(label);
    return tip;
  }
}

class RendererIcon implements Icon {
  private final Component renderer;
  private final Container parent;
  private final Dimension size;

  protected RendererIcon(Component renderer, Container parent, Dimension size) {
    this.renderer = renderer;
    this.parent = parent;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    // Fill with the tree background so the tooltip looks the same as the cell below it
    g2.setPaint(parent.getBackground());
    g2.fillRect(x, y, size.width, size.height);
    // Paint the renderer through the CellRendererPane of the tree, not of the JToolTip.
    // DefaultTreeCellRenderer discards FontUIResource and inherits the font of its parent,
    // so painting it in the JToolTip would use the JToolTip font, which differs from
    // the JTree font in some LookAndFeels (e.g. Metal) and does not fit the cell bounds.
    SwingUtilities.paintComponent(g2, renderer, parent, x, y, size.width, size.height);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size.width;
  }

  @Override public int getIconHeight() {
    return size.height;
  }
}
