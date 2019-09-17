// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"Integer", "Integer", "Boolean"};
    Object[][] data = {
      {50, 50, false}, {13, 13, true}, {0, 0, false},
      {20, 20, true}, {99, 99, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column != 0;
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setAutoCreateRowSorter(true);
        setRowHeight(26);
        getColumnModel().getColumn(1).setCellRenderer(new SliderRenderer());
        getColumnModel().getColumn(1).setCellEditor(new SliderEditor());
      }

      @Override public Color getSelectionBackground() {
        Color bc = super.getSelectionBackground();
        return Optional.ofNullable(bc).map(Color::brighter).orElse(bc);
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
class SliderRenderer implements TableCellRenderer {
  private final JSlider renderer = new JSlider();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    if (value instanceof Integer) {
      renderer.setValue((Integer) value);
    }
    return renderer;
  }
}

class SliderEditor extends AbstractCellEditor implements TableCellEditor {
  private final JSlider renderer = new JSlider();
  private int prev;

  protected SliderEditor() {
    super();
    renderer.setOpaque(true);
    renderer.addChangeListener(e -> {
      Object o = SwingUtilities.getAncestorOfClass(JTable.class, renderer);
      if (o instanceof JTable) {
        JTable table = (JTable) o;
        int value = renderer.getValue();
        if (table.isEditing() && value != prev) {
          int row = table.convertRowIndexToModel(table.getEditingRow());
          table.getModel().setValueAt(value, row, 0);
          table.getModel().setValueAt(value, row, 1);
          prev = value;
        }
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.setValue((Integer) value);
    renderer.setBackground(table.getSelectionBackground());
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return renderer.getValue();
  }
}

// // inheritance to extend a class
// class SliderRenderer extends JSlider implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//     setOpaque(true);
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//     if (value instanceof Integer) {
//       this.setValue(((Integer) value).intValue());
//     }
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
//     // // System.out.println(propertyName);
//     // if ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
//     //   super.firePropertyChange(propertyName, oldValue, newValue);
//     // }
//   }
//
//   @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
//     /* Overridden for performance reasons. */
//   }
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
//   @Override public void invalidate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void validate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void revalidate() {
//     /* Overridden for performance reasons. */
//   }
//   // <---- Overridden for performance reasons.
// }
//
// class SliderEditor extends JSlider implements TableCellEditor {
//   private transient ChangeListener handler;
//   private int prev;
//   @Override public void updateUI() {
//     removeChangeListener(handler);
//     super.updateUI();
//     setOpaque(true);
//     handler = e -> {
//       Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
//       if (o instanceof JTable) {
//         JTable table = (JTable) o;
//         int value = getValue();
//         if (table.isEditing() && value != prev) {
//           int row = table.convertRowIndexToModel(table.getEditingRow());
//           table.getModel().setValueAt(value, row, 0);
//           table.getModel().setValueAt(value, row, 1);
//           prev = value;
//         }
//       }
//     };
//     addChangeListener(handler);
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     Integer i = (Integer) value;
//     this.setBackground(table.getSelectionBackground());
//     this.setValue(i.intValue());
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return Integer.valueOf(getValue());
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   // protected transient ChangeEvent changeEvent;
//   @Override public boolean isCellEditable(EventObject e) {
//     return true;
//   }
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
