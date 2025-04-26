// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setRowHeight(36);
        setAutoCreateRowSorter(true);
        EventQueue.invokeLater(() -> {
          TableColumn column = getColumnModel().getColumn(0);
          column.setCellRenderer(new SpinnerRenderer());
          column.setCellEditor(new SpinnerEditor());
          column = getColumnModel().getColumn(1);
          column.setCellRenderer(new ButtonsRenderer());
          column.setCellEditor(new ButtonsEditor());
          repaint();
        });
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"JSpinner", "Buttons"};
    Object[][] data = {
        {50, 100}, {100, 50}, {30, 20}, {0, 100}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
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

class SpinnerPanel extends JPanel {
  private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(100, 0, 200, 1));

  protected SpinnerPanel() {
    super(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.insets = new Insets(0, 10, 0, 10);
    c.fill = GridBagConstraints.HORIZONTAL;
    add(spinner, c);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  // @Override public boolean isOpaque() {
  //   return true;
  // }

  public JSpinner getSpinner() {
    return spinner;
  }
}

class SpinnerRenderer implements TableCellRenderer {
  private final SpinnerPanel renderer = new SpinnerPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    renderer.getSpinner().setValue(value);
    return renderer;
  }
}

class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
  private final SpinnerPanel renderer = new SpinnerPanel();

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.setBackground(table.getSelectionBackground());
    renderer.getSpinner().setValue(value);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return renderer.getSpinner().getValue();
  }

  // @Override public boolean isCellEditable(EventObject e) {
  //   return true;
  // }

  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }

  @Override public boolean stopCellEditing() {
    boolean stopEditing = true;
    JSpinner spinner = renderer.getSpinner();
    try {
      spinner.commitEdit();
    } catch (ParseException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(spinner);
      stopEditing = false;
    }
    return stopEditing && super.stopCellEditing();
    // fireEditingStopped();
    // return true;
  }
}

// class SpinnerRenderer extends SpinnerPanel implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//     spinner.setValue((Integer) value);
//     return this;
//   }
// }
//
// class SpinnerEditor extends SpinnerPanel implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     this.setBackground(table.getSelectionBackground());
//     spinner.setValue((Integer) value);
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return spinner.getValue();
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   @Override public boolean isCellEditable(EventObject e) {
//     return true;
//   }
//
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     return true;
//   }
//
//   @Override public boolean stopCellEditing() {
//     try {
//       spinner.commitEdit();
//     } catch (ParseException ex) {
//       UIManager.getLookAndFeel().provideErrorFeedback(spinner);
//       return false;
//     }
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

class ButtonsPanel extends JPanel {
  public final JButton[] buttons = {new JButton("+"), new JButton("-")};
  public final JLabel label = new JLabel(" ", SwingConstants.RIGHT) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 50;
      return d;
    }
  };
  // /* default */ int counter = -1;
  public final AtomicInteger counter = new AtomicInteger(-1);

  protected ButtonsPanel() {
    super();
    add(label);
    for (JButton b : buttons) {
      b.setFocusable(false);
      b.setRolloverEnabled(false);
      add(b);
    }
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  // @Override public boolean isOpaque() {
  //   return true;
  // }
}

class ButtonsRenderer implements TableCellRenderer {
  private final ButtonsPanel renderer = new ButtonsPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Color bgc = isSelected ? table.getSelectionBackground() : table.getBackground();
    renderer.setBackground(bgc);
    Color fgc = isSelected ? table.getSelectionForeground() : table.getForeground();
    renderer.label.setForeground(fgc);
    renderer.label.setText(Objects.toString(value, ""));
    return renderer;
  }
}

class ButtonsEditor extends AbstractCellEditor implements TableCellEditor {
  private final ButtonsPanel renderer = new ButtonsPanel();

  protected ButtonsEditor() {
    super();
    renderer.buttons[0].addActionListener(e -> {
      int i = renderer.counter.incrementAndGet();
      renderer.label.setText(Integer.toString(i));
      fireEditingStopped();
    });

    renderer.buttons[1].addActionListener(e -> {
      int i = renderer.counter.decrementAndGet();
      renderer.label.setText(Integer.toString(i));
      fireEditingStopped();
    });

    renderer.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        fireEditingStopped();
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.setBackground(table.getSelectionBackground());
    renderer.label.setForeground(table.getSelectionForeground());
    if (value instanceof Integer) {
      int i = (int) value;
      renderer.counter.set(i);
      renderer.label.setText(Integer.toString(i));
    }
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return renderer.counter.intValue();
  }

  // // AbstractCellEditor
  // @Override public boolean isCellEditable(EventObject e) {
  //   return true;
  // }
  //
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
  // }
}

// class ButtonsRenderer extends ButtonsPanel implements TableCellRenderer {
//   @Override public void updateUI() {
//     super.updateUI();
//     setName("Table.cellRenderer");
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//     label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
//     label.setText(Objects.toString(value, ""));
//     return this;
//   }
// }
//
// class ButtonsEditor extends ButtonsPanel implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//   protected ButtonsEditor() {
//     super();
//     buttons.get(0).addActionListener(new ActionListener() {
//       @Override public void actionPerformed(ActionEvent e) {
//         i++;
//         label.setText(Integer.toString(i));
//         fireEditingStopped();
//       }
//     });
//
//     buttons.get(1).addActionListener(new ActionListener() {
//       @Override public void actionPerformed(ActionEvent e) {
//         i--;
//         label.setText(Integer.toString(i));
//         fireEditingStopped();
//       }
//     });
//
//     addMouseListener(new MouseAdapter() {
//       @Override public void mousePressed(MouseEvent e) {
//         fireEditingStopped();
//       }
//     });
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     this.setBackground(table.getSelectionBackground());
//     label.setForeground(table.getSelectionForeground());
//     i = (Integer) value;
//     label.setText(Integer.toString(i));
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return i;
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
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
