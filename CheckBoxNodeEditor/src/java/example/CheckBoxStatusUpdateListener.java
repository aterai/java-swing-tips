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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class CheckBoxStatusUpdateListener implements TreeModelListener {
  private boolean adjusting;

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting) {
      return;
    }
    adjusting = true;
    Object[] children = e.getChildren();
    DefaultTreeModel model = (DefaultTreeModel) e.getSource();

    DefaultMutableTreeNode node;
    CheckBoxNode c; // = (CheckBoxNode) node.getUserObject();
    boolean isNotRootAndOnlyOneNodeChanged = Objects.nonNull(children) && children.length == 1;
    if (isNotRootAndOnlyOneNodeChanged) {
      node = (DefaultMutableTreeNode) children[0];
      c = (CheckBoxNode) node.getUserObject();
      TreePath parent = e.getTreePath();
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getLastPathComponent();
      while (Objects.nonNull(n)) {
        updateParentUserObject(n);
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) n.getParent();
        if (Objects.nonNull(tmp)) {
          n = tmp;
        } else {
          break;
        }
      }
      model.nodeChanged(n);
    } else { // if (Objects.isNull(children)) {
      // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/TreeModelListener.html#treeNodesChanged-javax.swing.event.TreeModelEvent-
      // To indicate the root has changed, childIndices and children will be null.
      node = (DefaultMutableTreeNode) model.getRoot();
      c = (CheckBoxNode) node.getUserObject();
    }
    updateAllChildrenUserObject(node, c.getStatus());
    model.nodeChanged(node);
    adjusting = false;
  }
  // private void updateParentUserObject(DefaultMutableTreeNode parent) {
  //   int selectedCount = 0;
  //   // Java 9: Enumeration<TreeNode> children = parent.children();
  //   Enumeration<?> children = parent.children();
  //   while (children.hasMoreElements()) {
  //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
  //     CheckBoxNode check = (CheckBoxNode) node.getUserObject();
  //     if (check.status == Status.INDETERMINATE) {
  //       selectedCount = -1;
  //       break;
  //     }
  //     if (check.status == Status.SELECTED) {
  //       selectedCount++;
  //     }
  //   }
  //   String label = ((CheckBoxNode) parent.getUserObject()).label;
  //   if (selectedCount == 0) {
  //     parent.setUserObject(new CheckBoxNode(label, Status.DESELECTED));
  //   } else if (selectedCount == parent.getChildCount()) {
  //     parent.setUserObject(new CheckBoxNode(label, Status.SELECTED));
  //   } else {
  //     parent.setUserObject(new CheckBoxNode(label));
  //   }
  // }

  private void updateParentUserObject(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.children()).stream()
    List<Status> list = Collections.list((Enumeration<?>) parent.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(CheckBoxNode.class::isInstance)
        .map(CheckBoxNode.class::cast)
        .map(CheckBoxNode::getStatus)
        .collect(Collectors.toList());

    Object o = parent.getUserObject();
    if (o instanceof CheckBoxNode) {
      String label = ((CheckBoxNode) o).getLabel();
      if (list.stream().allMatch(s -> Objects.equals(s, Status.DESELECTED))) {
        parent.setUserObject(new CheckBoxNode(label, Status.DESELECTED));
      } else if (list.stream().allMatch(s -> Objects.equals(s, Status.SELECTED))) {
        parent.setUserObject(new CheckBoxNode(label, Status.SELECTED));
      } else {
        parent.setUserObject(new CheckBoxNode(label, Status.INDETERMINATE));
      }
    }
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode parent, Status status) {
    // Java 9: Collections.list(parent.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !Objects.equals(parent, node))
        .forEach(node -> {
          CheckBoxNode check = (CheckBoxNode) node.getUserObject();
          node.setUserObject(new CheckBoxNode(check.getLabel(), status));
        });
    // // Java 9: Enumeration<TreeNode> breadth = parent.breadthFirstEnumeration();
    // Enumeration<?> breadth = parent.breadthFirstEnumeration();
    // while (breadth.hasMoreElements()) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
    //   if (Objects.equals(parent, node)) {
    //     continue;
    //   }
    //   CheckBoxNode check = (CheckBoxNode) node.getUserObject();
    //   node.setUserObject(new CheckBoxNode(check.getLabel(), status));
    // }
  }

  @Override public void treeNodesInserted(TreeModelEvent e) { /* not needed */ }

  @Override public void treeNodesRemoved(TreeModelEvent e) { /* not needed */ }

  @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox();
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    l.setFont(tree.getFont());
    if (value instanceof DefaultMutableTreeNode) {
      panel.setFocusable(false);
      panel.setRequestFocusEnabled(false);
      panel.setOpaque(false);
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      checkBox.setFocusable(false);
      checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        l.setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
      }
      panel.add(checkBox, BorderLayout.WEST);
      panel.add(l);
      return panel;
    }
    return l;
  }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox() {
    protected transient ActionListener handler;
    @Override public void updateUI() {
      removeActionListener(handler);
      super.updateUI();
      setOpaque(false);
      setFocusable(false);
      handler = e -> stopCellEditing();
      addActionListener(handler);
    }
  };
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private String str;

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    l.setFont(tree.getFont());
    if (value instanceof DefaultMutableTreeNode) {
      panel.setFocusable(false);
      panel.setRequestFocusEnabled(false);
      panel.setOpaque(false);
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      // checkBox.setFocusable(false);
      // checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        l.setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
        str = node.getLabel();
      }
      panel.add(checkBox, BorderLayout.WEST);
      panel.add(l);
      return panel;
    }
    return l;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(str, checkBox.isSelected() ? Status.SELECTED : Status.DESELECTED);
  }

  @Override public boolean isCellEditable(EventObject e) {
    if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
      Point p = ((MouseEvent) e).getPoint();
      JTree tree = (JTree) e.getSource();
      TreePath path = tree.getPathForLocation(p.x, p.y);
      return Optional.ofNullable(tree.getPathBounds(path)).map(r -> {
        r.width = checkBox.getPreferredSize().width;
        return r.contains(p);
      }).orElse(false);
      // MouseEvent me = (MouseEvent) e;
      // JTree tree = (JTree) e.getSource();
      // TreePath path = tree.getPathForLocation(me.getX(), me.getY());
      // Rectangle r = tree.getPathBounds(path);
      // if (Objects.isNull(r)) {
      //   return false;
      // }
      // Dimension d = checkBox.getPreferredSize();
      // r.setSize(new Dimension(d.width, r.height));
      // if (r.contains(me.getPoint())) {
      //   return true;
      // }
    }
    return false;
  }
  // // AbstractCellEditor
  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }
  // @Override public boolean stopCellEditing() {
  //   fireEditingStopped();
  //   return true;
  // }
  // @Override public void cancelCellEditing() {
  //   fireEditingCanceled();
  // }
}

// // extends JCheckBox TreeCellRenderer Editor version
// class CheckBoxNodeRenderer extends TriStateCheckBox implements TreeCellRenderer {
//   private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//   private final JPanel panel = new JPanel(new BorderLayout());
//   protected CheckBoxNodeRenderer() {
//     super();
//     // Fixed?
//     // String uiName = getUI().getClass().getName();
//     // if (uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
//     //   System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
//     //   renderer.setBackgroundSelectionColor(new Color(0x0, true));
//     // }
//     panel.setFocusable(false);
//     panel.setRequestFocusEnabled(false);
//     panel.setOpaque(false);
//     panel.add(this, BorderLayout.WEST);
//     this.setOpaque(false);
//   }
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       this.setEnabled(tree.isEnabled());
//       this.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.status == Status.INDETERMINATE) {
//           setIcon(new IndeterminateIcon());
//         } else {
//           setIcon(null);
//         }
//         l.setText(node.label);
//         setSelected(node.status == Status.SELECTED);
//       }
//       // panel.add(this, BorderLayout.WEST);
//       panel.add(l);
//       return panel;
//     }
//     return l;
//   }
//   // Fixed?
//   // @Override public void updateUI() {
//   //   super.updateUI();
//   //   if (Objects.nonNull(panel)) {
//   //     // panel.removeAll(); // ??? Change to Nimbus LnF, JDK 1.6.0
//   //     panel.updateUI();
//   //     // panel.add(this, BorderLayout.WEST);
//   //   }
//   //   setName("Tree.cellRenderer");
//   //   // ???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//   //   // if (System.getProperty("java.version").startsWith("1.6.0")) {
//   //   //   renderer = new DefaultTreeCellRenderer();
//   //   // }
//   // }
// }
//
// class CheckBoxNodeEditor extends TriStateCheckBox implements TreeCellEditor {
//   private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//   private final JPanel panel = new JPanel(new BorderLayout());
//   private String str;
//
//   protected CheckBoxNodeEditor() {
//     super();
//     this.addActionListener(e -> stopCellEditing());
//     panel.setFocusable(false);
//     panel.setRequestFocusEnabled(false);
//     panel.setOpaque(false);
//     panel.add(this, BorderLayout.WEST);
//     this.setOpaque(false);
//   }
//   @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//     // JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       this.setEnabled(tree.isEnabled());
//       this.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.status == Status.INDETERMINATE) {
//           setIcon(new IndeterminateIcon());
//         } else {
//           setIcon(null);
//         }
//         l.setText(node.label);
//         setSelected(node.status == Status.SELECTED);
//         str = node.label;
//       }
//       // panel.add(this, BorderLayout.WEST);
//       panel.add(l);
//       return panel;
//     }
//     return l;
//   }
//   @Override public Object getCellEditorValue() {
//     return new CheckBoxNode(str, isSelected() ? Status.SELECTED : Status.DESELECTED);
//   }
//   @Override public boolean isCellEditable(EventObject e) {
//     if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
//       MouseEvent me = (MouseEvent) e;
//       JTree tree = (JTree) e.getSource();
//       TreePath path = tree.getPathForLocation(me.getX(), me.getY());
//       Rectangle r = tree.getPathBounds(path);
//       if (Objects.isNull(r)) {
//         return false;
//       }
//       Dimension d = getPreferredSize();
//       r.setSize(new Dimension(d.width, r.height));
//       if (r.contains(me.getX(), me.getY())) {
//         // Fixed: [JDK-8023474] First mousepress doesn't start editing in JTree - Java Bug System
//         //    https://bugs.openjdk.java.net/browse/JDK-8023474
//         // if (Objects.isNull(str) && System.getProperty("java.version").startsWith("1.7.0")) {
//         //   System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//         //   setBounds(new Rectangle(d.width, r.height));
//         // }
//         // System.out.println(getBounds());
//         return true;
//       }
//     }
//     return false;
//   }
//   // Fixed?
//   // @Override public void updateUI() {
//   //   super.updateUI();
//   //   setName("Tree.cellEditor");
//   //   if (Objects.nonNull(panel)) {
//   //     // panel.removeAll(); // ??? Change to Nimbus LnF, JDK 1.6.0
//   //     panel.updateUI();
//   //     // panel.add(this, BorderLayout.WEST);
//   //   }
//   //   // ???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//   //   // if (System.getProperty("java.version").startsWith("1.6.0")) {
//   //   //   renderer = new DefaultTreeCellRenderer();
//   //   // }
//   // }
//
//   // // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   // protected transient ChangeEvent changeEvent;
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     return true;
//   }
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//   @Override public void cancelCellEditing() {
//     fireEditingCanceled();
//   }
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//   public CellEditorListener[] getCellEditorListeners() {
//     return listenerList.getListeners(CellEditorListener.class);
//   }
//   protected void fireEditingStopped() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//       }
//     }
//   }
//   protected void fireEditingCanceled() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//       }
//     }
//   }
// }
//
// // extends JPanel TreeCellRenderer Editor version
// class CheckBoxNodeRenderer extends JPanel implements TreeCellRenderer {
//   private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//   private final TriStateCheckBox check = new TriStateCheckBox();
//   protected CheckBoxNodeRenderer() {
//     super(new BorderLayout());
//     String uiName = getUI().getClass().getName();
//     if (uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
//       System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
//       renderer.setBackgroundSelectionColor(new Color(0x0, true));
//     }
//     setFocusable(false);
//     setRequestFocusEnabled(false);
//     setOpaque(false);
//     add(check, BorderLayout.WEST);
//     check.setOpaque(false);
//   }
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       check.setEnabled(tree.isEnabled());
//       check.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.status == Status.INDETERMINATE) {
//           check.setIcon(new IndeterminateIcon());
//         } else {
//           check.setIcon(null);
//         }
//         l.setText(node.label);
//         check.setSelected(node.status == Status.SELECTED);
//       }
//       // add(this, BorderLayout.WEST);
//       add(l);
//       return this;
//     }
//     return l;
//   }
//   @Override public void updateUI() {
//     super.updateUI();
//     if (Objects.nonNull(check)) {
//       // removeAll(); // ??? Change to Nimbus LnF, JDK 1.6.0
//       check.updateUI();
//       // add(check, BorderLayout.WEST);
//     }
//     setName("Tree.cellRenderer");
//     // ???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//     // if (System.getProperty("java.version").startsWith("1.6.0")) {
//     //   renderer = new DefaultTreeCellRenderer();
//     // }
//   }
// }
//
// class CheckBoxNodeEditor extends JPanel implements TreeCellEditor {
//   private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//   private final TriStateCheckBox check = new TriStateCheckBox();
//   private String str = null;
//   protected CheckBoxNodeEditor() {
//     super(new BorderLayout());
//     check.addActionListener(e -> stopCellEditing());
//     setFocusable(false);
//     setRequestFocusEnabled(false);
//     setOpaque(false);
//     add(check, BorderLayout.WEST);
//     check.setOpaque(false);
//   }
//   @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       check.setEnabled(tree.isEnabled());
//       check.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.status == Status.INDETERMINATE) {
//           check.setIcon(new IndeterminateIcon());
//         } else {
//           check.setIcon(null);
//         }
//         l.setText(node.label);
//         check.setSelected(node.status == Status.SELECTED);
//         str = node.label;
//       }
//       // add(this, BorderLayout.WEST);
//       add(l);
//       return this;
//     }
//     return l;
//   }
//   @Override public Object getCellEditorValue() {
//     return new CheckBoxNode(str, check.isSelected() ? Status.SELECTED : Status.DESELECTED);
//   }
//   @Override public boolean isCellEditable(EventObject e) {
//     if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
//       MouseEvent me = (MouseEvent) e;
//       JTree tree = (JTree) e.getSource();
//       TreePath path = tree.getPathForLocation(me.getX(), me.getY());
//       Rectangle r = tree.getPathBounds(path);
//       if (Objects.isNull(r)) {
//         return false;
//       }
//       Dimension d = check.getPreferredSize();
//       r.setSize(new Dimension(d.width, r.height));
//       if (r.contains(me.getPoint())) {
//         // Fixed: [JDK-8023474] First mousepress doesn't start editing in JTree - Java Bug System
//         //    https://bugs.openjdk.java.net/browse/JDK-8023474
//         // if (Objects.isNull(str) && System.getProperty("java.version").startsWith("1.7.0")) {
//         //   System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//         //   check.setBounds(new Rectangle(d.width, r.height));
//         // }
//         // System.out.println(getBounds());
//         return true;
//       }
//     }
//     return false;
//   }
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Tree.cellEditor");
//     if (Objects.nonNull(check)) {
//       // removeAll(); // ??? Change to Nimbus LnF, JDK 1.6.0
//       check.updateUI();
//       // add(check, BorderLayout.WEST);
//     }
//     // ???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
//     // if (System.getProperty("java.version").startsWith("1.6.0")) {
//     //   renderer = new DefaultTreeCellRenderer();
//     // }
//   }
//
//   // Copied from AbstractCellEditor
//   protected EventListenerList listenerList = new EventListenerList();
//   protected transient ChangeEvent changeEvent;
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     return true;
//   }
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//   @Override public void cancelCellEditing() {
//     fireEditingCanceled();
//   }
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//   public CellEditorListener[] getCellEditorListeners() {
//     return listenerList.getListeners(CellEditorListener.class);
//   }
//   protected void fireEditingStopped() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//       }
//     }
//   }
//   protected void fireEditingCanceled() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//       }
//     }
//   }
// }
