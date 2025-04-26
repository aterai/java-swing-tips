// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1));
    TableModel model = makeModel();
    JTable table1 = new PropertyTable(model);
    JTable table2 = new PropertyTable(model);
    table2.setTransferHandler(new HtmlTableTransferHandler());
    add(new JScrollPane(table1));
    add(new JScrollPane(table2));
    add(new JScrollPane(new JEditorPane("text/html", "")));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Type", "Value"};
    @SuppressWarnings("JavaUtilDate")
    Object[][] data = {
        {"String", "text"},
        {"Date", new Date()},
        {"Integer", 12},
        {"Double", 3.45},
        {"Boolean", Boolean.TRUE},
        {"Color", Color.RED}
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

class PropertyTable extends JTable {
  private static final int TARGET_COLUMN = 1;
  private Class<?> editingClass;

  protected PropertyTable(TableModel model) {
    super(model);
  }

  // public PropertyTable(Object[][] data, String[] columnNames) {
  //   super(data, columnNames);
  // }

  private Class<?> getClassAt(int row, int column) {
    int mc = convertColumnIndexToModel(column);
    int mr = convertRowIndexToModel(row);
    TableModel m = getModel();
    return mc == TARGET_COLUMN ? m.getValueAt(mr, mc).getClass() : null;
  }

  @Override public void updateUI() {
    // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
    // https://bugs.openjdk.org/browse/JDK-6788475
    // Set a temporary ColorUIResource to avoid this issue
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    super.updateUI();
    setDefaultRenderer(Color.class, new ColorRenderer());
    setDefaultEditor(Color.class, new ColorEditor());
    setDefaultEditor(Date.class, new DateEditor());
  }

  @Override public TableCellRenderer getCellRenderer(int row, int column) {
    // boolean isTarget = convertColumnIndexToModel(column) == TARGET_COLUMN;
    // return isTarget
    //     ? getDefaultRenderer(getClassAt(row, column))
    //     : super.getCellRenderer(row, column);
    Class<?> clz = getClassAt(row, column);
    return Objects.nonNull(clz)
        ? getDefaultRenderer(clz)
        : super.getCellRenderer(row, column);
  }

  // https://stackoverflow.com/questions/1464691/property-list-gui-component-in-swing
  // This method is also invoked by the editor when the value in the editor
  // component is saved in the TableModel. The class was saved when the
  // editor was invoked so the proper class can be created.
  @Override public TableCellEditor getCellEditor(int row, int column) {
    editingClass = getClassAt(row, column);
    // return convertColumnIndexToModel(column) == TARGET_COL_IDX
    return Objects.nonNull(editingClass)
        ? getDefaultEditor(editingClass)
        : super.getCellEditor(row, column);
  }

  @Override public Class<?> getColumnClass(int column) {
    return convertColumnIndexToModel(column) == TARGET_COLUMN
        ? editingClass
        : super.getColumnClass(column);
  }
}

// delegation pattern
class DateEditor extends AbstractCellEditor implements TableCellEditor {
  private final JSpinner spinner;

  protected DateEditor() {
    super();
    spinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy/MM/dd");
    spinner.setEditor(editor);
    setArrowButtonEnabled(false);
    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);

    editor.getTextField().addFocusListener(new FocusListener() {
      @Override public void focusLost(FocusEvent e) {
        setArrowButtonEnabled(false);
      }

      @Override public void focusGained(FocusEvent e) {
        setArrowButtonEnabled(true);
        EventQueue.invokeLater(() -> {
          JTextField field = (JTextField) e.getComponent();
          field.setCaretPosition(8);
          field.setSelectionStart(8);
          field.setSelectionEnd(10);
        });
      }
    });
    spinner.setBorder(BorderFactory.createEmptyBorder());
  }

  protected final void setArrowButtonEnabled(boolean flag) {
    for (Component c : spinner.getComponents()) {
      if (c instanceof JButton) {
        c.setEnabled(flag);
      }
    }
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    spinner.setValue(value);
    return spinner;
  }

  @Override public Object getCellEditorValue() {
    return spinner.getValue();
  }

  // // AbstractCellEditor
  // @Override public boolean isCellEditable(EventObject e) {
  //   return true;
  // }

  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }

  @Override public boolean stopCellEditing() {
    boolean stopEditing = true;
    try {
      spinner.commitEdit();
    } catch (ParseException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(spinner);
      stopEditing = false;
    }
    return stopEditing && super.stopCellEditing();
  }

  // @Override public void cancelCellEditing() {
  //   fireEditingCanceled();
  // }
}

// // inheritance to extend a class
// class DateEditor extends JSpinner implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//   private final JSpinner.DateEditor editor;
//
//   protected DateEditor() {
//     super(new SpinnerDateModel());
//     editor = new JSpinner.DateEditor(this, "yyyy/MM/dd");
//     setEditor(editor);
//     setArrowButtonEnabled(false);
//     editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
//
//     editor.getTextField().addFocusListener(new FocusListener() {
//       @Override public void focusLost(FocusEvent e) {
//         setArrowButtonEnabled(false);
//       }
//
//       @Override public void focusGained(FocusEvent e) {
//         // System.out.println("getTextField");
//         setArrowButtonEnabled(true);
//         EventQueue.invokeLater(new Runnable() {
//           @Override public void run() {
//             editor.getTextField().setCaretPosition(8);
//             editor.getTextField().setSelectionStart(8);
//             editor.getTextField().setSelectionEnd(10);
//           }
//         });
//       }
//     });
//     setBorder(BorderFactory.createEmptyBorder());
//   }
//
//   private void setArrowButtonEnabled(boolean flag) {
//     for (Component c : getComponents()) {
//       if (c instanceof JButton) {
//         ((JButton) c).setEnabled(flag);
//       }
//     }
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     setValue(value);
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return getValue();
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
//     try {
//       commitEdit();
//     } catch (ParseException ex) {
//       UIManager.getLookAndFeel().provideErrorFeedback(this);
//       return false;
//       // // Edited value is invalid, spinner.getValue() will return
//       // // the last valid value, you could revert the spinner to show that:
//       // editor.getTextField().setValue(getValue());
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

class ColorRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (value instanceof Color && c instanceof JLabel) {
      Color color = (Color) value;
      JLabel l = (JLabel) c;
      l.setIcon(new ColorIcon(color));
      l.setText(String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
    }
    return c;
  }
}

// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorEditor.java
class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private static final String EDIT = "edit";
  private final JButton button = new JButton();
  private final JColorChooser colorChooser;
  private final JDialog dialog;
  private Color currentColor;

  protected ColorEditor() {
    super();
    // Set up the editor (from the table's point of view),
    // which is a button.
    // This button brings up the color chooser dialog,
    // which is the editor from the user's point of view.
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    // button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    button.setOpaque(false);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setHorizontalTextPosition(SwingConstants.RIGHT);

    // Set up the dialog that the button brings up.
    colorChooser = new JColorChooser();
    dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null);
  }

  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  @Override public void actionPerformed(ActionEvent e) {
    if (EDIT.equals(e.getActionCommand())) {
      // The user has clicked the cell, so
      // bring up the dialog.
      button.setBackground(currentColor);
      button.setIcon(new ColorIcon(currentColor));
      colorChooser.setColor(currentColor);
      dialog.setVisible(true);

      // Make the renderer reappear.
      fireEditingStopped();
    } else { // User pressed dialog's "OK" button.
      currentColor = colorChooser.getColor();
    }
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  @Override public Object getCellEditorValue() {
    return currentColor;
  }

  // Implement the one method defined by TableCellEditor.
  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    currentColor = (Color) value;
    button.setIcon(new ColorIcon(currentColor));
    int r = currentColor.getRed();
    int g = currentColor.getGreen();
    int b = currentColor.getBlue();
    button.setText(String.format("(%d, %d, %d)", r, g, b));
    return button;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 10;
  }

  @Override public int getIconHeight() {
    return 10;
  }
}
