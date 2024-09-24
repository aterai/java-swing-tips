// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

// http://www2.gol.com/users/tame/swing/examples/JTableExamples2.html
public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn column = getColumnModel().getColumn(1);
        column.setCellRenderer(new RadioButtonsRenderer());
        column.setCellEditor(new RadioButtonsEditor());
      }
    };
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    // if (System.getProperty("java.version").startsWith("1.6.0")) {
    //   // 1.6.0_xx bug? column header click -> edit cancel?
    //   table.getTableHeader().addMouseListener(new MouseAdapter() {
    //     @Override public void mousePressed(MouseEvent e) {
    //       if (table.isEditing()) {
    //         table.getCellEditor().stopCellEditing();
    //       }
    //     }
    //   });
    // }

    // table.addMouseListener(new MouseAdapter() {
    //   @Override public void mouseReleased(MouseEvent e) {
    //     JTable t = (JTable) e.getComponent();
    //     Point pt = e.getPoint();
    //     int row = t.rowAtPoint(pt);
    //     int col = t.columnAtPoint(pt);
    //     if (t.convertRowIndexToModel(row) >= 0 && t.convertColumnIndexToModel(col) == 1) {
    //       TableCellEditor ce = t.getCellEditor(row, col);
    //       // https://tips4java.wordpress.com/2009/07/12/table-button-column/
    //       ce.stopCellEditing();
    //       Component c = ce.getTableCellEditorComponent(t, null, true, row, col);
    //       Point p = SwingUtilities.convertPoint(t, pt, c);
    //       Component b = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
    //       if (b instanceof JRadioButton) {
    //         ((JRadioButton) b).doClick();
    //       }
    //     }
    //   }
    // });
    // RadioButtonEditorRenderer rbe = new RadioButtonEditorRenderer();
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Integer", "Answer"};
    Object[][] data = {
        {1, Answer.A}, {2, Answer.B}, {3, Answer.C}, {4, Answer.C}, {5, Answer.A}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
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

enum Answer {
  A, B, C
}

class RadioButtonsPanel extends JPanel {
  private final ButtonGroup group = new ButtonGroup();

  protected RadioButtonsPanel() {
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    initButtons();
  }

  public String getSelectedActionCommand() {
    return group.getSelection().getActionCommand();
  }

  private void initButtons() {
    group.clearSelection();
    Collections.list(group.getElements()).forEach(group::remove);
    removeAll();
    for (Answer a : Answer.values()) {
      JRadioButton b = makeButton(a.name());
      add(b);
      group.add(b);
    }
  }

  private static JRadioButton makeButton(String title) {
    JRadioButton b = new JRadioButton(title);
    b.setActionCommand(title);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    return b;
  }

  public void updateSelectedButton(Object v) {
    if (v instanceof Answer) {
      initButtons();
      ((JRadioButton) getComponent(((Answer) v).ordinal())).setSelected(true);
      // switch ((Answer) v) {
      //   case A:
      //     ((JRadioButton) getComponent(0)).setSelected(true);
      //     break;
      //   case B:
      //     ((JRadioButton) getComponent(1)).setSelected(true);
      //     break;
      //   case C:
      //     ((JRadioButton) getComponent(2)).setSelected(true);
      //     break;
      //   default:
      //     break;
      // }
    }
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void removeAll() {
    super.removeAll();
  }
}

// delegation pattern
class RadioButtonsRenderer implements TableCellRenderer {
  private final RadioButtonsPanel renderer = new RadioButtonsPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.updateSelectedButton(value);
    return renderer;
  }
}

class RadioButtonsEditor extends AbstractCellEditor implements TableCellEditor {
  private final RadioButtonsPanel renderer = new RadioButtonsPanel();

  protected RadioButtonsEditor() {
    super();
    ActionListener al = e -> fireEditingStopped();
    for (Component c : renderer.getComponents()) {
      ((JRadioButton) c).addActionListener(al);
    }
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.updateSelectedButton(value);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return Answer.valueOf(renderer.getSelectedActionCommand());
  }
}

// // inheritance to extend a class
// class RadioButtonsRenderer extends RadioButtonsPanel implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     updateSelectedButton(value);
//     return this;
//   }
// }
//
// class RadioButtonsEditor extends RadioButtonsPanel implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//
//   protected RadioButtonsEditor() {
//     super();
//     ActionListener al = e -> fireEditingStopped();
//     for (AbstractButton b : buttons) {
//       b.addActionListener(al);
//     }
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     updateSelectedButton(value);
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return Answer.valueOf(bg.getSelection().getActionCommand());
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
