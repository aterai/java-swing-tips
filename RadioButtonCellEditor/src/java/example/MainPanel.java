// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
      {1, "D", true}, {2, "B", false}, {3, "C", false},
      {4, "E", false}, {5, "A", false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public void setValueAt(Object v, int row, int column) {
        if (v instanceof Boolean) {
          for (int i = 0; i < getRowCount(); i++) {
            super.setValueAt(i == row, i, column);
          }
        } else {
          super.setValueAt(v, row, column);
        }
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        setAutoCreateRowSorter(true);
        TableColumn c = getColumnModel().getColumn(2);
        c.setCellRenderer(new RadioButtonsRenderer());
        c.setCellEditor(new RadioButtonsEditor());
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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

// delegation pattern
class RadioButtonsRenderer implements TableCellRenderer {
  private final JRadioButton renderer = new JRadioButton();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof Boolean) {
      renderer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
      renderer.setHorizontalAlignment(SwingConstants.CENTER);
      renderer.setSelected((Boolean) value);
    }
    return renderer;
  }
}

class RadioButtonsEditor extends AbstractCellEditor implements TableCellEditor {
  private final JRadioButton renderer = new JRadioButton();

  protected RadioButtonsEditor() {
    super();
    renderer.addActionListener(e -> fireEditingStopped());
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    if (value instanceof Boolean) {
      renderer.setBackground(table.getSelectionBackground());
      renderer.setHorizontalAlignment(SwingConstants.CENTER);
      renderer.setSelected((Boolean) value);
    }
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return renderer.isSelected();
  }
}

// // inheritence to extend a class
// class RadioButtonsRenderer extends JRadioButton implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//   }
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     if (value instanceof Boolean) {
//       setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//       setHorizontalAlignment(SwingConstants.CENTER);
//       setSelected((Boolean) value);
//     }
//     return this;
//   }
// }
//
// class RadioButtonsEditor extends JRadioButton implements TableCellEditor {
//   private ActionListener listener;
//   @Override public void updateUI() {
//     removeActionListener(listener);
//     super.updateUI();
//     setName("Table.cellRenderer");
//     listener = e -> fireEditingStopped();
//     addActionListener(listener);
//   }
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     if (value instanceof Boolean) {
//       setBackground(table.getSelectionBackground());
//       setHorizontalAlignment(SwingConstants.CENTER);
//       setSelected((Boolean) value);
//     }
//     return this;
//   }
//   @Override public Object getCellEditorValue() {
//     return isSelected();
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   // protected transient ChangeEvent changeEvent;
//   @Override public boolean isCellEditable(EventObject e) {
//     return true;
//   }
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
