// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    Box verticalBox = Box.createVerticalBox();
    Map<String, Component> map = new ConcurrentHashMap<>();
    JTree tree = makeTree();
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(n -> {
          String title = Objects.toString(n.getUserObject(), "");
          n.setUserObject(new CheckBoxNode(title, false, !n.isLeaf()));
          if (!n.isRoot()) {
            JCheckBox c = new JCheckBox(title, false);
            map.put(title, c);
            if (!n.isLeaf()) {
              verticalBox.add(Box.createVerticalStrut(5));
              c.addActionListener(e -> {
                boolean selected = ((JCheckBox) e.getSource()).isSelected();
                Collections.list((Enumeration<?>) n.children()).stream()
                    .filter(DefaultMutableTreeNode.class::isInstance)
                    .map(DefaultMutableTreeNode.class::cast)
                    .forEach(child -> {
                      CheckBoxNode cn = (CheckBoxNode) child.getUserObject();
                      map.get(cn.getText()).setEnabled(selected);
                    });
              });
            }
            c.setEnabled(!n.isLeaf());
            Box box = Box.createHorizontalBox();
            box.add(Box.createHorizontalStrut((n.getLevel() - 1) * 16));
            box.setAlignmentX(LEFT_ALIGNMENT);
            box.add(c);
            verticalBox.add(box);
          }
        });

    JPanel p = new JPanel(new BorderLayout());
    p.add(verticalBox, BorderLayout.NORTH);
    add(makeTitledPanel("Box", new JScrollPane(p)));
    add(makeTitledPanel("JTree", new JScrollPane(tree)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree makeTree() {
    JTree tree = new CheckBoxTree();
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
    tree.getModel().addTreeModelListener(new CheckBoxStatusUpdateListener());
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        throw new ExpandVetoException(e, "Tree expansion cancelled");
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
        throw new ExpandVetoException(e, "Tree collapse cancelled");
      }
    });
    return tree;
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

class CheckBoxNode {
  private final String text;
  private final boolean selected;
  private final boolean enabled;

  protected CheckBoxNode(String text, boolean selected, boolean enabled) {
    this.text = text;
    this.selected = selected;
    this.enabled = enabled;
  }

  public String getText() {
    return text;
  }

  public boolean isSelected() {
    return selected;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override public String toString() {
    return text;
  }
}

class CheckBoxTree extends JTree {
  @Override public void updateUI() {
    setCellRenderer(null);
    setCellEditor(null);
    super.updateUI();
    setEditable(true);
    setRootVisible(false);
    setShowsRootHandles(false);
    setCellRenderer(new CheckBoxNodeRenderer());
    setCellEditor(new CheckBoxNodeEditor());
  }

  @Override public boolean isPathEditable(TreePath path) {
    return Optional.ofNullable(path.getLastPathComponent())
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(n -> ((DefaultMutableTreeNode) n).getUserObject())
        .filter(CheckBoxNode.class::isInstance)
        .map(uo -> ((CheckBoxNode) uo).isEnabled())
        .orElse(false);
  }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private final JCheckBox checkBox = new JCheckBox();
  private final TreeCellRenderer renderer = new DefaultTreeCellRenderer();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c;
    if (value instanceof DefaultMutableTreeNode) {
      checkBox.setOpaque(false);
      Object uo = ((DefaultMutableTreeNode) value).getUserObject();
      if (uo instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) uo;
        checkBox.setText(node.getText());
        checkBox.setSelected(node.isSelected());
        checkBox.setEnabled(node.isEnabled());
      } else {
        checkBox.setText(Objects.toString(value));
      }
      c = checkBox;
    } else {
      c = renderer.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
    }
    return c;
  }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private final JCheckBox checkBox = new JCheckBox() {
    private transient ActionListener handler;

    @Override public void updateUI() {
      removeActionListener(handler);
      super.updateUI();
      setOpaque(false);
      setFocusable(false);
      handler = e -> stopCellEditing();
      addActionListener(handler);
    }
  };

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    if (value instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        checkBox.setEnabled(node.isEnabled());
        checkBox.setSelected(node.isSelected());
      } else {
        checkBox.setSelected(false);
      }
      checkBox.setText(value.toString());
    }
    return checkBox;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(checkBox.getText(), checkBox.isSelected(), checkBox.isEnabled());
  }

  @Override public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent;
    // boolean b = e instanceof MouseEvent;
    // if (b) {
    //   MouseEvent me = (MouseEvent) e;
    //   b = Optional.ofNullable(me.getComponent())
    //       .filter(JTree.class::isInstance)
    //       .map(c -> ((JTree) c).getPathForLocation(me.getX(), me.getY()))
    //       .map(TreePath::getLastPathComponent)
    //       .filter(DefaultMutableTreeNode.class::isInstance)
    //       .map(node -> ((DefaultMutableTreeNode) node).getUserObject())
    //       .filter(CheckBoxNode.class::isInstance)
    //       .map(uo -> ((CheckBoxNode) uo).isEnabled())
    //       .orElse(false);
    // }
    // return b;
  }
}

class CheckBoxStatusUpdateListener implements TreeModelListener {
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting.get()) {
      return;
    }
    adjusting.set(true);

    DefaultTreeModel model = (DefaultTreeModel) e.getSource();
    Object[] children = e.getChildren();
    boolean isOneNodeSelected = Objects.nonNull(children) && children.length == 1;
    Object current = isOneNodeSelected ? children[0] : model.getRoot();
    if (current instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) current;
      CheckBoxNode c = (CheckBoxNode) node.getUserObject();
      for (Object child : e.getChildren()) {
        updateAllChildrenUserObject((DefaultMutableTreeNode) child, c.isSelected());
      }
      model.nodeChanged((DefaultMutableTreeNode) e.getTreePath().getLastPathComponent());
    }
    adjusting.set(false);
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode parent, boolean enabled) {
    // Java 9: Collections.list(parent.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !Objects.equals(parent, node))
        .forEach(node -> {
          CheckBoxNode check = (CheckBoxNode) node.getUserObject();
          node.setUserObject(new CheckBoxNode(check.getText(), check.isSelected(), enabled));
        });
  }

  @Override public void treeNodesInserted(TreeModelEvent e) {
    /* not needed */
  }

  @Override public void treeNodesRemoved(TreeModelEvent e) {
    /* not needed */
  }

  @Override public void treeStructureChanged(TreeModelEvent e) {
    /* not needed */
  }
}
