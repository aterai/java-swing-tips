// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final int BOOLEAN_COLUMN = 2;
  private static final Color CHECKED_COLOR = Color.ORANGE;

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new CheckedRowColorTable(createModel());
    table.getModel().addTableModelListener(e -> {
      // Only a change in the check box column affects the row background color
      if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == BOOLEAN_COLUMN) {
        if (e.getFirstRow() == e.getLastRow()) {
          repaintRow(table, table.convertRowIndexToView(e.getFirstRow()));
        } else {
          table.repaint();
        }
      }
    });
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension());
    table.setRowSelectionAllowed(true);
    // table.setSurrendersFocusOnKeystroke(true);
    // table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    String[] columnNames = {"String", "Number", "Boolean"};
    Object[][] data = {
        {"aaa", 1, false}, {"bbb", 20, false},
        {"ccc", 2, false}, {"ddd", 3, false},
        {"aaa", 1, false}, {"bbb", 20, false},
        {"ccc", 2, false}, {"ddd", 3, false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == BOOLEAN_COLUMN;
      }
    };
  }

  private static final class CheckedRowColorTable extends JTable {
    private CheckedRowColorTable(TableModel model) {
      super(model);
    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
      Component c = super.prepareRenderer(renderer, row, column);
      // Keep the selection colors set by the renderer for the selected rows
      if (!isRowSelected(row)) {
        Object value = getModel().getValueAt(convertRowIndexToModel(row), BOOLEAN_COLUMN);
        c.setForeground(getForeground());
        c.setBackground(Boolean.TRUE.equals(value) ? CHECKED_COLOR : getBackground());
      }
      return c;
    }

    @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
      Component c = super.prepareEditor(editor, row, column);
      updateEditorBackground(c, row);
      return c;
    }

    @Override public void valueChanged(ListSelectionEvent e) {
      super.valueChanged(e);
      // A mouse press starts editing before the row is selected,
      // so the editor background also has to follow the selection change
      if (isEditing()) {
        updateEditorBackground(getEditorComponent(), getEditingRow());
      }
    }

    private void updateEditorBackground(Component c, int row) {
      if (c instanceof JCheckBox) {
        Color bgc;
        if (isRowSelected(row)) {
          bgc = getSelectionBackground();
        } else {
          bgc = ((JCheckBox) c).isSelected() ? CHECKED_COLOR : getBackground();
        }
        c.setBackground(bgc);
      }
    }
  }

  private static void repaintRow(JTable table, int viewRow) {
    // The row may be filtered out by the RowSorter
    if (viewRow >= 0) {
      Rectangle r = table.getCellRect(viewRow, 0, true);
      table.repaint(0, r.y, table.getWidth(), r.height);
    }
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
      Logger.getGlobal().severe(ex::getMessage);
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
