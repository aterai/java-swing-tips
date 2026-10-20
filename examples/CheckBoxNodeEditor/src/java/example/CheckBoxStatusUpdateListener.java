// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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

public final class CheckBoxStatusUpdateListener implements TreeModelListener {
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void treeNodesChanged(TreeModelEvent e) {
    if (adjusting.get()) {
      return;
    }
    adjusting.set(true);
    try {
      DefaultTreeModel model = (DefaultTreeModel) e.getSource();
      // https://docs.oracle.com/javase/8/docs/api/javax/swing/event/TreeModelListener.html#treeNodesChanged-javax.swing.event.TreeModelEvent-
      // To indicate the root has changed, childIndices and children will be null.
      Object[] children = e.getChildren();
      boolean isRoot = Objects.isNull(children);
      Object[] changedNodes = isRoot ? new Object[] {model.getRoot()} : children;

      // Update the status of all descendant nodes to be the same as the changed node status
      for (Object o : changedNodes) {
        if (o instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
          Object userObject = node.getUserObject();
          if (userObject instanceof CheckBoxNode) {
            updateAllChildrenUserObject(node, ((CheckBoxNode) userObject).getStatus());
            model.nodeChanged(node);
          }
        }
      }

      // If the parent node exists, update the status of the ancestor nodes up to the root
      if (!isRoot) {
        for (TreePath path = e.getTreePath(); Objects.nonNull(path); path = path.getParentPath()) {
          Object o = path.getLastPathComponent();
          if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) o;
            updateParentUserObject(parent);
            model.nodeChanged(parent);
          }
        }
      }
    } finally {
      // Always reset the flag, otherwise this listener stops working after an exception
      adjusting.set(false);
    }
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
    Object o = parent.getUserObject();
    if (o instanceof CheckBoxNode) {
      // Java 9: Collections.list(parent.children()).stream()
      Set<Status> set = Collections.list((Enumeration<?>) parent.children()).stream()
          .filter(DefaultMutableTreeNode.class::isInstance)
          .map(DefaultMutableTreeNode.class::cast)
          .map(DefaultMutableTreeNode::getUserObject)
          .filter(CheckBoxNode.class::isInstance)
          .map(CheckBoxNode.class::cast)
          .map(CheckBoxNode::getStatus)
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(Status.class)));
      // SELECTED or DESELECTED only if all children have that same status
      Status status = set.size() == 1 ? set.iterator().next() : Status.INDETERMINATE;
      String label = ((CheckBoxNode) o).getLabel();
      parent.setUserObject(new CheckBoxNode(label, status));
    }
  }

  private void updateAllChildrenUserObject(DefaultMutableTreeNode parent, Status status) {
    // Java 9: Collections.list(parent.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.breadthFirstEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !Objects.equals(parent, node))
        .forEach(node -> {
          Object o = node.getUserObject();
          if (o instanceof CheckBoxNode) {
            node.setUserObject(new CheckBoxNode(((CheckBoxNode) o).getLabel(), status));
          }
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

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox();
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

  protected CheckBoxNodeRenderer() {
    // These properties do not depend on the node, so set them only once
    panel.setFocusable(false);
    panel.setRequestFocusEnabled(false);
    panel.setOpaque(false);
    panel.add(checkBox, BorderLayout.WEST);
    checkBox.setFocusable(false);
    checkBox.setOpaque(false);
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      JLabel l = (JLabel) c;
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        boolean indeterminate = node.getStatus() == Status.INDETERMINATE;
        checkBox.setIcon(indeterminate ? new IndeterminateIcon() : null);
        l.setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
      }
      panel.add(l);
      c = panel;
    }
    c.setFont(tree.getFont());
    return c;
  }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final TriStateCheckBox checkBox = new TriStateCheckBox() {
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
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private String label;

  protected CheckBoxNodeEditor() {
    super();
    panel.setFocusable(false);
    panel.setRequestFocusEnabled(false);
    panel.setOpaque(false);
    panel.add(checkBox, BorderLayout.WEST);
  }

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, true, expanded, leaf, row, true);
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      // checkBox.setFocusable(false);
      // checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        boolean indeterminate = node.getStatus() == Status.INDETERMINATE;
        checkBox.setIcon(indeterminate ? new IndeterminateIcon() : null);
        ((JLabel) c).setText(node.getLabel());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
        label = node.getLabel();
      }
      panel.add(c);
      c = panel;
    }
    c.setFont(tree.getFont());
    return c;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(label, checkBox.isSelected() ? Status.SELECTED : Status.DESELECTED);
  }

  @Override public boolean isCellEditable(EventObject e) {
    boolean editable = false;
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      editable = Optional.ofNullable(me.getComponent())
          .filter(JTree.class::isInstance)
          .map(JTree.class::cast)
          .map(t -> t.getPathBounds(t.getPathForLocation(me.getX(), me.getY())))
          .map(r -> {
            r.width = checkBox.getPreferredSize().width;
            return r.contains(me.getPoint());
          })
          .orElse(false);
    }
    return editable;
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
//     panel.setFocusable(false);
//     panel.setRequestFocusEnabled(false);
//     panel.setOpaque(false);
//     panel.add(this, BorderLayout.WEST);
//     this.setOpaque(false);
//   }
//
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(
//         tree, value, selected, expanded, leaf, row, hasFocus);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       this.setEnabled(tree.isEnabled());
//       this.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.getStatus() == Status.INDETERMINATE) {
//           setIcon(new IndeterminateIcon());
//         } else {
//           setIcon(null);
//         }
//         l.setText(node.getLabel());
//         setSelected(node.getStatus() == Status.SELECTED);
//       }
//       // panel.add(this, BorderLayout.WEST);
//       panel.add(l);
//       return panel;
//     }
//     return l;
//   }
//
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
//
//   @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//     Component c = renderer.getTreeCellRendererComponent(
//         tree, value, true, expanded, leaf, row, true);
//     c.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       this.setEnabled(tree.isEnabled());
//       this.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode && c instanceof JLabel) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.getStatus() == Status.INDETERMINATE) {
//           setIcon(new IndeterminateIcon());
//         } else {
//           setIcon(null);
//         }
//         ((JLabel) c).setText(node.getLabel());
//         setSelected(node.getStatus() == Status.SELECTED);
//         str = node.getLabel();
//       }
//       // panel.add(this, BorderLayout.WEST);
//       panel.add(c);
//       return panel;
//     }
//     return c;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return new CheckBoxNode(str, isSelected() ? Status.SELECTED : Status.DESELECTED);
//   }
//
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
//         // Fixed: First mousepress doesn't start editing in JTree - Java Bug System
//         //    https://bugs.openjdk.org/browse/JDK-8023474
//         // if (str == null && System.getProperty("java.version").startsWith("1.7.0")) {
//         //   System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//         //   setBounds(new Rectangle(d.width, r.height));
//         // }
//         // System.out.println(getBounds());
//         return true;
//       }
//     }
//     return false;
//   }
//
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
//
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//
//   @Override public void cancelCellEditing() {
//     fireEditingCanceled();
//   }
//
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//
//   public CellEditorListener[] getCellEditorListeners() {
//     return listenerList.getListeners(CellEditorListener.class);
//   }
//
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
//
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
//
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     JLabel l = (JLabel) renderer.getTreeCellRendererComponent(
//         tree, value, selected, expanded, leaf, row, hasFocus);
//     l.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode) {
//       check.setEnabled(tree.isEnabled());
//       check.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.getStatus() == Status.INDETERMINATE) {
//           check.setIcon(new IndeterminateIcon());
//         } else {
//           check.setIcon(null);
//         }
//         l.setText(node.getLabel());
//         check.setSelected(node.getStatus() == Status.SELECTED);
//       }
//       // add(this, BorderLayout.WEST);
//       add(l);
//       return this;
//     }
//     return l;
//   }
//
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
//   private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
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
//
//   @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//     Component c = renderer.getTreeCellRendererComponent(
//         tree, value, true, expanded, leaf, row, true);
//     c.setFont(tree.getFont());
//     if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
//       check.setEnabled(tree.isEnabled());
//       check.setFont(tree.getFont());
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         CheckBoxNode node = (CheckBoxNode) userObject;
//         if (node.getStatus() == Status.INDETERMINATE) {
//           check.setIcon(new IndeterminateIcon());
//         } else {
//           check.setIcon(null);
//         }
//         ((JLabel) c).setText(node.getLabel());
//         check.setSelected(node.getStatus() == Status.SELECTED);
//         str = node.getLabel();
//       }
//       // add(this, BorderLayout.WEST);
//       add(c);
//       return this;
//     }
//     return c;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return new CheckBoxNode(str, check.isSelected() ? Status.SELECTED : Status.DESELECTED);
//   }
//
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
//         // Fixed: First mousepress doesn't start editing in JTree - Java Bug System
//         //    https://bugs.openjdk.org/browse/JDK-8023474
//         // boolean version = System.getProperty("java.version").startsWith("1.7.0");
//         // if (Objects.isNull(str) && version) {
//         //   System.out.println("XXX: Java 7, only on first run\n" + getBounds());
//         //   check.setBounds(new Rectangle(d.width, r.height));
//         // }
//         // System.out.println(getBounds());
//         return true;
//       }
//     }
//     return false;
//   }
//
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
//   private transient ChangeEvent changeEvent;
//
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     return true;
//   }
//
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//
//   @Override public void cancelCellEditing() {
//     fireEditingCanceled();
//   }
//
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//
//   public CellEditorListener[] getCellEditorListeners() {
//     return listenerList.getListeners(CellEditorListener.class);
//   }
//
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
//
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
