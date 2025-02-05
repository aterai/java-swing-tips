// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private static final int START_HEIGHT = 8;
  private static final int END_HEIGHT = 16;
  private static final int DELAY = 10;

  private MainPanel() {
    super(new GridLayout(1, 2));
    JTree tree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        setRowHeight(-1);
        setCellRenderer(new HeightTreeCellRenderer());
      }
    };
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) root.breadthFirstEnumeration())
        .stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(n -> n.setUserObject(makeUserObject(n, END_HEIGHT)));
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override public void treeWillExpand(TreeExpansionEvent e) {
        Object o = e.getPath().getLastPathComponent();
        if (o instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode) o;
          List<DefaultMutableTreeNode> list = getTreeNodes(parent);
          parent.setUserObject(makeUserObject(parent, END_HEIGHT));
          list.forEach(n -> n.setUserObject(makeUserObject(n, START_HEIGHT)));
          startExpand(e, list);
        }
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
        Object c = e.getPath().getLastPathComponent();
        if (c instanceof DefaultMutableTreeNode) {
          List<DefaultMutableTreeNode> list = getTreeNodes((DefaultMutableTreeNode) c);
          boolean b = list
              .stream()
              .anyMatch(n -> {
                Object uo = n.getUserObject();
                return uo instanceof SizeNode && ((SizeNode) uo).height == END_HEIGHT;
              });
          if (b) {
            startCollapse(e, list);
            throw new ExpandVetoException(e);
          }
        }
      }
    });
    add(makeTitledPanel("Default", new JScrollPane(new JTree())));
    add(makeTitledPanel("Expand/Collapse Animations", new JScrollPane(tree)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static List<DefaultMutableTreeNode> getTreeNodes(DefaultMutableTreeNode parent) {
    return Collections.list((Enumeration<?>) parent.children())
        .stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .collect(Collectors.toList());
  }

  private static Object makeUserObject(DefaultMutableTreeNode node, int height) {
    String title = Objects.toString(node.getUserObject());
    return new SizeNode(title, height);
  }

  private static void startExpand(TreeExpansionEvent e, List<? extends TreeNode> list) {
    JTree tree = (JTree) e.getSource();
    TreeModel model = tree.getModel();
    AtomicInteger height = new AtomicInteger(START_HEIGHT);
    new Timer(DELAY, ev -> {
      int h = height.getAndIncrement();
      if (h <= END_HEIGHT) {
        updateNodeHeight(list, model, h);
      } else {
        ((Timer) ev.getSource()).stop();
      }
    }).start();
  }

  private static void startCollapse(TreeExpansionEvent e, List<? extends TreeNode> list) {
    JTree tree = (JTree) e.getSource();
    TreePath path = e.getPath();
    TreeModel model = tree.getModel();
    AtomicInteger height = new AtomicInteger(END_HEIGHT);
    new Timer(DELAY, ev -> {
      int h = height.getAndDecrement();
      if (h >= START_HEIGHT) {
        updateNodeHeight(list, model, h);
      } else {
        ((Timer) ev.getSource()).stop();
        tree.collapsePath(path);
      }
    }).start();
  }

  private static void updateNodeHeight(List<? extends TreeNode> l, TreeModel m, int h) {
    l.stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(n -> {
          Object uo = makeUserObject(n, h);
          m.valueForPathChanged(new TreePath(n.getPath()), uo);
        });
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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

class HeightTreeCellRenderer extends DefaultTreeCellRenderer {
  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object uo = node.getUserObject();
    if (c instanceof JLabel && uo instanceof SizeNode) {
      JLabel l = (JLabel) c;
      SizeNode n = (SizeNode) uo;
      l.setPreferredSize(null); // reset prev preferred size
      l.setText(n.label); // recalculate preferred size
      // Font font = l.getFont();
      // FontRenderContext frc = l.getFontMetrics(font).getFontRenderContext();
      // Rectangle2D r = font.getStringBounds(n.label, frc);
      // int iconWidth = 20 + 2;
      // int w = iconWidth + (int) r.getWidth();
      // int h = n.height;
      // System.out.println(w + " : " + l.getPreferredSize().width);
      // l.setPreferredSize(new Dimension(w, h));
      Dimension d = l.getPreferredSize();
      d.height = n.height;
      l.setPreferredSize(d);
    }
    return c;
  }
}

class SizeNode {
  public final String label;
  public final int height;

  protected SizeNode(String label, int height) {
    this.label = label;
    this.height = height;
  }

  @Override public String toString() {
    return label;
  }
}
