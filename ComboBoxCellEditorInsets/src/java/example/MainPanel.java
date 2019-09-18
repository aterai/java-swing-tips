// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"Border", "JPanel+JComboBox"};
    Object[][] data = {
      {"AAA", "a"}, {"CCC", "bbb"}, {"BBB", "c"}, {"ZZZ", "dd"}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        setRowHeight(36);
        setAutoCreateRowSorter(true);
        TableColumn column = getColumnModel().getColumn(0);
        column.setCellRenderer(makeComboTableCellRenderer(makeComboBox()));
        column.setCellEditor(new DefaultCellEditor(makeComboBox()));

        column = getColumnModel().getColumn(1);
        column.setCellRenderer(new ComboBoxCellRenderer());
        column.setCellEditor(new ComboBoxCellEditor());
      }
    };
    add(new JScrollPane(table));
    setBorder(BorderFactory.createTitledBorder("JComboBox in a Table Cell"));
    setPreferredSize(new Dimension(320, 240));
  }

  public static JComboBox<String> makeComboBox() {
    JComboBox<String> c = new JComboBox<>(new String[] {"11111", "222", "3"});
    c.setEditable(true);
    c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10), c.getBorder()));
    return c;
  }

  public static TableCellRenderer makeComboTableCellRenderer(JComboBox<String> combo) {
    return (table, value, isSelected, hasFocus, row, column) -> {
      combo.removeAllItems();
      JComponent editor = (JComponent) combo.getEditor().getEditorComponent();
      editor.setOpaque(true);
      if (isSelected) {
        editor.setForeground(table.getSelectionForeground());
        editor.setBackground(table.getSelectionBackground());
        // button.setBackground(table.getSelectionBackground());
      } else {
        editor.setForeground(table.getForeground());
        editor.setBackground(table.getBackground());
        // button.setBackground(bg);
      }
      combo.addItem(Objects.toString(value, ""));
      return combo;
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

class ComboBoxPanel extends JPanel {
  public final JComboBox<String> comboBox = new JComboBox<>(new String[] {"11111", "222", "3"});

  protected ComboBoxPanel() {
    super(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 1d;
    c.insets = new Insets(0, 10, 0, 10);
    c.fill = GridBagConstraints.HORIZONTAL;

    comboBox.setEditable(true);
    setOpaque(true);
    add(comboBox, c);
    comboBox.setSelectedIndex(0);
  }
}

// TEST:
// class ComboBoxPanel extends JPanel {
//   private String[] m = {"a", "b", "c"};
//   protected JComboBox<String> comboBox = new JComboBox<String>(m) {
//     @Override public Dimension getPreferredSize() {
//       Dimension d = super.getPreferredSize();
//       d.width = 40;
//       return d;
//     }
//   };
//
//   protected ComboBoxPanel() {
//     super();
//     setOpaque(true);
//     comboBox.setEditable(true);
//     add(comboBox);
//   }
// }

// delegation pattern
class ComboBoxCellRenderer implements TableCellRenderer {
  private final ComboBoxPanel panel = new ComboBoxPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    Optional.ofNullable(value).ifPresent(panel.comboBox::setSelectedItem);
    return panel;
  }
}

class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
  private final ComboBoxPanel panel = new ComboBoxPanel();

  protected ComboBoxCellEditor() {
    super();
    panel.comboBox.addActionListener(e -> fireEditingStopped());
    panel.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        fireEditingStopped();
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    panel.setBackground(table.getSelectionBackground());
    panel.comboBox.setSelectedItem(value);
    return panel;
  }

  @Override public Object getCellEditorValue() {
    return panel.comboBox.getSelectedItem();
  }

  @Override public boolean shouldSelectCell(EventObject anEvent) {
    if (anEvent instanceof MouseEvent) {
      MouseEvent e = (MouseEvent) anEvent;
      return e.getID() != MouseEvent.MOUSE_DRAGGED;
    }
    return true;
  }

  @Override public boolean stopCellEditing() {
    if (panel.comboBox.isEditable()) {
      panel.comboBox.actionPerformed(new ActionEvent(this, 0, ""));
    }
    fireEditingStopped();
    return true;
  }
}

// // inheritance to extend a class
// class ComboBoxCellRenderer extends ComboBoxPanel implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//   }
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//     Optional.ofNullable(value).ifPresent(comboBox::setSelectedItem);
//     return this;
//   }
// }
//
// class ComboBoxCellEditor extends ComboBoxPanel implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//
//   protected ComboBoxCellEditor() {
//     super();
//     comboBox.addActionListener(e -> fireEditingStopped());
//     addMouseListener(new MouseAdapter() {
//       @Override public void mousePressed(MouseEvent e) {
//         fireEditingStopped();
//       }
//     });
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     this.setBackground(table.getSelectionBackground());
//     comboBox.setSelectedItem(value);
//     return this;
//   }
//
//   // Copied from DefaultCellEditor.EditorDelegate
//   @Override public Object getCellEditorValue() {
//     return comboBox.getSelectedItem();
//   }
//
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     if (anEvent instanceof MouseEvent) {
//       MouseEvent e = (MouseEvent) anEvent;
//       return e.getID() != MouseEvent.MOUSE_DRAGGED;
//     }
//     return true;
//   }
//
//   @Override public boolean stopCellEditing() {
//     if (comboBox.isEditable()) {
//       comboBox.actionPerformed(new ActionEvent(this, 0, ""));
//     }
//     fireEditingStopped();
//     return true;
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   // protected transient ChangeEvent changeEvent;
//   @Override public boolean isCellEditable(EventObject e) {
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
//   protected final void fireEditingStopped() {
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
// class ComboCellRenderer extends JComboBox<String> implements TableCellRenderer {
//   // private final JTextField editor;
//   // private JButton button;
//   protected ComboCellRenderer() {
//     super();
//     setEditable(true);
//     // setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
//     setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10), getBorder()));
//     JComponent editor = (JComponent) getEditor().getEditorComponent();
//     editor.setBorder(BorderFactory.createEmptyBorder());
//     editor.setOpaque(true);
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     removeAllItems();
//     Component editor = getEditor().getEditorComponent();
//     if (isSelected) {
//       editor.setForeground(table.getSelectionForeground());
//       editor.setBackground(table.getSelectionBackground());
//       // button.setBackground(table.getSelectionBackground());
//     } else {
//       editor.setForeground(table.getForeground());
//       editor.setBackground(table.getBackground());
//       // button.setBackground(bg);
//     }
//     addItem(Objects.toString(value, ""));
//     return this;
//   }
//
//   // Overridden for performance reasons. ---->
//   @Override public boolean isOpaque() {
//     Color back = getBackground();
//     Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
//     if (o instanceof JTable) {
//       JTable table = (JTable) o;
//       boolean colorMatch = Objects.nonNull(back) && back.equals(table.getBackground()) && table.isOpaque();
//       return !colorMatch && super.isOpaque();
//     } else {
//       return super.isOpaque();
//     }
//   }
//
//   @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//     // System.out.println(propertyName);
//     // if ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
//     //   super.firePropertyChange(propertyName, oldValue, newValue);
//     // }
//   }
//
//   // @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   @Override public void repaint(long tm, int x, int y, int width, int height) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(Rectangle r) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint() {
//     /* Overridden for performance reasons. */
//   }
//
//   // @Override public void invalidate() {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   // @Override public void validate() {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   @Override public void revalidate() {
//     /* Overridden for performance reasons. */
//   }
//   // <---- Overridden for performance reasons.
// }
