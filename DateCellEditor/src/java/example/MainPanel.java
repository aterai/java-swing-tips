// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        // setShowGrid(false);
        // setAutoCreateRowSorter(true);
        setSurrendersFocusOnKeystroke(true);
        TableCellRenderer r = getDefaultRenderer(Date.class);
        if (r instanceof JLabel) {
          ((JLabel) r).setHorizontalAlignment(SwingConstants.LEFT);
        }
        setDefaultEditor(Date.class, new SpinnerCellEditor());
      }
    };

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Integer", "String", "Date"};
    @SuppressWarnings("JavaUtilDate")
    Object[][] data = {
        {-1, "AAA", new Date()}, {2, "BBB", new Date()},
        {-9, "EEE", new Date()}, {1, "", new Date()},
        {10, "CCC", new Date()}, {7, "FFF", new Date()},
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

class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
  private final JSpinner spinner = new JSpinner(new SpinnerDateModel());

  protected SpinnerCellEditor() {
    super();
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy/MM/dd");
    spinner.setEditor(editor);
    spinner.setBorder(BorderFactory.createEmptyBorder());
    setArrowButtonEnabled(false);

    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    editor.getTextField().addFocusListener(new FocusListener() {
      @Override public void focusLost(FocusEvent e) {
        setArrowButtonEnabled(false);
      }

      @Override public void focusGained(FocusEvent e) {
        // System.out.println("getTextField");
        setArrowButtonEnabled(true);
        EventQueue.invokeLater(() -> {
          JTextField field = (JTextField) e.getComponent();
          field.setCaretPosition(8);
          field.setSelectionStart(8);
          field.setSelectionEnd(10);
        });
      }
    });
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
}
