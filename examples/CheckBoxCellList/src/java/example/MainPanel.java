// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box list1 = Box.createVerticalBox();

    DefaultListModel<CheckBoxNode> model = new DefaultListModel<>();
    JList<CheckBoxNode> list2 = new CheckBoxList(model);

    JTree list3 = new JTree() {
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
    };

    JPanel p = new JPanel(new GridLayout(1, 3));
    p.add(makeTitledPanel("Box", new JScrollPane(list1)));
    p.add(makeTitledPanel("JList", new JScrollPane(list2)));
    p.add(makeTitledPanel("JTree", new JScrollPane(list3)));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
    Stream.of("1", "22", "333", "4444", "55555", "6666", "777", "88", "9", "00")
        .forEach(title -> {
          boolean isSelected = title.length() % 2 == 0;
          JCheckBox c = new JCheckBox(title, isSelected);
          c.setAlignmentX(LEFT_ALIGNMENT);
          list1.add(c);
          model.addElement(new CheckBoxNode(title, isSelected));
          root.add(new DefaultMutableTreeNode(new CheckBoxNode(title, isSelected)));
        });
    list3.setModel(new DefaultTreeModel(root));

    add(new JLabel("JCheckBox in ", SwingConstants.CENTER), BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
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

final class CheckBoxNode {
  private final String text;
  private final boolean selected;

  /* default */ CheckBoxNode(String text, boolean selected) {
    this.text = text;
    this.selected = selected;
  }

  public String getText() {
    return text;
  }

  public boolean isSelected() {
    return selected;
  }

  @Override public String toString() {
    return text;
  }
}

class CheckBoxList extends JList<CheckBoxNode> {
  private transient CheckBoxCellRenderer renderer;
  private transient MouseListener handler;

  protected CheckBoxList(ListModel<CheckBoxNode> model) {
    super(model);
  }

  @Override public void updateUI() {
    setForeground(null);
    setBackground(null);
    setSelectionForeground(null);
    setSelectionBackground(null);
    removeMouseListener(handler);
    removeMouseListener(renderer);
    removeMouseMotionListener(renderer);
    super.updateUI();
    handler = new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (SwingUtilities.isLeftMouseButton(e) && index >= 0) {
          DefaultListModel<CheckBoxNode> m = (DefaultListModel<CheckBoxNode>) getModel();
          CheckBoxNode n = m.get(index);
          m.set(index, new CheckBoxNode(n.getText(), !n.isSelected()));
          repaint(getCellBounds(index, index));
        }
      }
    };
    addMouseListener(handler);
    renderer = new CheckBoxCellRenderer();
    setCellRenderer(renderer);
    addMouseListener(renderer);
    addMouseMotionListener(renderer);
    putClientProperty("List.isFileList", Boolean.TRUE);
  }

  // @see SwingUtilities2.pointOutsidePrefSize(...)
  private boolean pointOutsidePrefSize(Point p) {
    int i = locationToIndex(p);
    CheckBoxNode cbn = getModel().getElementAt(i);
    Component c = getCellRenderer().getListCellRendererComponent(this, cbn, i, false, false);
    Rectangle rect = getCellBounds(i, i);
    rect.width = c.getPreferredSize().width;
    return i < 0 || !rect.contains(p);
  }

  @Override protected void processMouseEvent(MouseEvent e) {
    if (!pointOutsidePrefSize(e.getPoint())) {
      super.processMouseEvent(e);
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e) {
    if (pointOutsidePrefSize(e.getPoint())) {
      MouseEvent ev = new MouseEvent(
          e.getComponent(), MouseEvent.MOUSE_EXITED, e.getWhen(),
          e.getModifiersEx(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
          e.getClickCount(), e.isPopupTrigger(), MouseEvent.NOBUTTON);
      super.processMouseEvent(ev);
    } else {
      super.processMouseMotionEvent(e);
    }
  }
}

class CheckBoxCellRenderer extends MouseAdapter implements ListCellRenderer<CheckBoxNode> {
  private final JCheckBox checkBox = new JCheckBox();
  private int rollOverRowIndex = -1;

  @Override public Component getListCellRendererComponent(JList<? extends CheckBoxNode> list, CheckBoxNode value, int index, boolean isSelected, boolean cellHasFocus) {
    checkBox.setOpaque(true);
    if (isSelected) {
      checkBox.setBackground(list.getSelectionBackground());
      checkBox.setForeground(list.getSelectionForeground());
    } else {
      checkBox.setBackground(list.getBackground());
      checkBox.setForeground(list.getForeground());
    }
    checkBox.setSelected(value.isSelected());
    checkBox.getModel().setRollover(index == rollOverRowIndex);
    checkBox.setText(value.getText());
    return checkBox;
  }

  @Override public void mouseExited(MouseEvent e) {
    if (rollOverRowIndex >= 0) {
      JList<?> l = (JList<?>) e.getComponent();
      l.repaint(l.getCellBounds(rollOverRowIndex, rollOverRowIndex));
      rollOverRowIndex = -1;
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    JList<?> l = (JList<?>) e.getComponent();
    int index = l.locationToIndex(e.getPoint());
    if (index != rollOverRowIndex) {
      rollOverRowIndex = index;
      l.repaint();
    }
  }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
  private final JCheckBox checkBox = new JCheckBox();
  private final TreeCellRenderer renderer = new DefaultTreeCellRenderer();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c;
    if (leaf && value instanceof DefaultMutableTreeNode) {
      checkBox.setOpaque(false);
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        checkBox.setText(node.getText());
        checkBox.setSelected(node.isSelected());
      }
      c = checkBox;
    } else {
      c = renderer.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
    }
    return c;
  }
}

// delegation pattern
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
    if (leaf && value instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        checkBox.setSelected(((CheckBoxNode) userObject).isSelected());
      } else {
        checkBox.setSelected(false);
      }
      checkBox.setText(value.toString());
    }
    return checkBox;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(checkBox.getText(), checkBox.isSelected());
  }

  @Override public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent;
  }

  // // AbstractCellEditor
  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }
  //
  // @Override public boolean stopCellEditing() {
  //   fireEditingStopped();
  //   return true;
  // }
  //
  // @Override public void cancelCellEditing() {
  //   fireEditingCanceled();
  //   }
}

// // inheritance to extend a class
// class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
//   private transient ActionListener handler;
//   @Override public void updateUI() {
//     removeActionListener(handler);
//     super.updateUI();
//     setOpaque(false);
//     setFocusable(false);
//     handler = e -> stopCellEditing();
//     addActionListener(handler);
//   }
//
//   @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//     if (leaf && value instanceof DefaultMutableTreeNode) {
//       Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//       if (userObject instanceof CheckBoxNode) {
//         this.setSelected(((CheckBoxNode) userObject).selected);
//       } else {
//         this.setSelected(false);
//       }
//       this.setText(value.toString());
//     }
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return new CheckBoxNode(getText(), isSelected());
//   }
//
//   @Override public boolean isCellEditable(EventObject e) {
//     return e instanceof MouseEvent;
//   }
//
//   // Copied from AbstractCellEditor
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
