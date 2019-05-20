// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));

    JTree tree1 = new TooltipTree(new DefaultTreeModel(makeTreeRoot()));
    ToolTipManager.sharedInstance().registerComponent(tree1);

    JTree tree2 = new JTree(new DefaultTreeModel(makeTreeRoot())) {
      @Override public void updateUI() {
        super.updateUI();
        // setRowHeight(24);
        setCellRenderer(new TooltipTreeCellRenderer());
      }
    };
    ToolTipManager.sharedInstance().registerComponent(tree2);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeTitledPanel("Draw directly above the cell", tree1));
    p.add(makeTitledPanel("Default location", tree2));

    add(p);
    add(new JLabel("dummy panel"));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 00000004");
    set4.add(new DefaultMutableTreeNode("222222111111111111111122222"));
    set4.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
    set4.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
    set4.add(new DefaultMutableTreeNode("zzzzzzz"));

    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 00000001");
    set1.add(new DefaultMutableTreeNode("33333333333333333333333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 00000002");
    set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
    set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 00000003");
    set3.add(new DefaultMutableTreeNode("zzzzzzz"));
    set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
    set3.add(new DefaultMutableTreeNode("ccccccccc"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("eeeeeeeeeeeeeeeeeeeeeeeeeeeee"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("22222222222222222222222222222222222"));
    root.add(new DefaultMutableTreeNode("bbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
    return root;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(scroll);
    return p;
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TooltipTree extends JTree {
  protected final JLabel label = new JLabel() {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = getRowHeight();
      return d;
    }
  };

  protected TooltipTree(TreeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    // setRowHeight(24);
    setCellRenderer(new TooltipTreeCellRenderer());
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    int i = getRowForLocation(p.x, p.y);
    Rectangle cellBounds = getRowBounds(i);
    if (i >= 0 && Objects.nonNull(cellBounds) && cellBounds.contains(p.x, p.y)) {
      TreeSelectionModel tsm = getSelectionModel();
      Object node = getPathForRow(i).getLastPathComponent();
      // System.out.println(node);
      boolean hasFocus = hasFocus() && tsm.getLeadSelectionRow() == i;
      boolean isLeaf = getModel().isLeaf(node);
      TreeCellRenderer r = getCellRenderer();
      Component tcr = r.getTreeCellRendererComponent(this, node, isRowSelected(i), isExpanded(i), isLeaf, i, hasFocus);
      if (tcr instanceof JComponent && Objects.nonNull(((JComponent) tcr).getToolTipText())) {
        // System.out.println(((JComponent) tcr).getToolTipText());
        Point pt = cellBounds.getLocation();
        // label.setBorder(BorderFactory.createLineBorder(Color.RED));
        Insets ins = label.getInsets();
        pt.translate(-ins.left, -ins.top);
        label.setIcon(new RendererIcon(tcr, cellBounds));
        // System.out.println(pt);
        return pt;
      }
    }
    return null;
  }

  // @Override public String getToolTipText(MouseEvent e) {
  //   String str = super.getToolTipText(e);
  //   System.out.println("getToolTipText: " + str);
  //   return str;
  // }

  @Override public JToolTip createToolTip() {
    JToolTip tip = new JToolTip() {
      @Override public Dimension getPreferredSize() {
        return label.getPreferredSize();
      }
    };
    // System.out.println("createToolTip");
    tip.setBorder(BorderFactory.createEmptyBorder());
    tip.setLayout(new BorderLayout());
    tip.setComponent(this);
    tip.add(label);
    return tip;
  }
}

class TooltipTreeCellRenderer implements TreeCellRenderer {
  private final TreeCellRenderer renderer = new DefaultTreeCellRenderer();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    l.setToolTipText(value == null ? null : value.toString());
    // Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, tree);
    // if (c instanceof JScrollPane) {
    //   Insets i = l.getInsets();
    //   Rectangle rect = ((JScrollPane) c).getViewportBorderBounds();
    //   FontMetrics fm = l.getFontMetrics(l.getFont());
    //   String str = Objects.toString(value, "");
    //   l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
    // }
    return l;
  }
}

class RendererIcon implements Icon {
  private final Component renderer;
  private final Rectangle rect;

  protected RendererIcon(Component renderer, Rectangle rect) {
    this.renderer = renderer;
    this.rect = rect;
    rect.setLocation(0, 0);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof Container) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.clearRect(0, 0, c.getWidth(), c.getHeight());
      // g2.translate(x, y);
      SwingUtilities.paintComponent(g2, renderer, (Container) c, rect);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return renderer.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return renderer.getPreferredSize().height;
  }
}
