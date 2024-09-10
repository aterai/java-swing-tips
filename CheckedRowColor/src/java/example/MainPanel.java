// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int BOOLEAN_COLUMN = 2;

  private MainPanel() {
    super(new BorderLayout());
    JTable table = makeTable(makeModel());
    // TEST: JTable table = makeTable2(model);
    table.getModel().addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.UPDATE) {
        // System.out.println("TableModel: tableChanged");
        rowRepaint(table, table.convertRowIndexToView(e.getFirstRow()));
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

  private static TableModel makeModel() {
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

      @Override public boolean isCellEditable(int row, int col) {
        return col == BOOLEAN_COLUMN;
      }
    };
  }

  public static JTable makeTable(TableModel model) {
    return new JTable(model) {
      @Override public void updateUI() {
        // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
        // https://bugs.openjdk.org/browse/JDK-6788475
        // Set a temporary ColorUIResource to avoid this issue
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        TableModel m = getModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
          TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
          if (r instanceof Component) {
            SwingUtilities.updateComponentTreeUI((Component) r);
          }
        }
      }

      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component cmp = super.prepareEditor(editor, row, column);
        if (convertColumnIndexToModel(column) == BOOLEAN_COLUMN) {
          // System.out.println("JTable: prepareEditor");
          JCheckBox c = (JCheckBox) cmp;
          c.setBackground(c.isSelected() ? Color.ORANGE : getBackground());
        }
        return cmp;
      }

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        boolean b = (boolean) model.getValueAt(convertRowIndexToModel(row), BOOLEAN_COLUMN);
        c.setForeground(getForeground());
        c.setBackground(b ? Color.ORANGE : getBackground());
        return c;
      }
    };
  }

  // // TEST:
  // public static JTable makeTable2(TableModel model) {
  //   JTable table = new JTable(model);
  //   TableColumnModel columns = table.getColumnModel();
  //   TableCellRenderer r = new RowColorTableRenderer();
  //   for (int i = 0; i < columns.getColumnCount(); i++) {
  //     columns.getColumn(i).setCellRenderer(r);
  //   }
  //   return table;
  // }
  //
  // private static class RowColorTableRenderer extends DefaultTableCellRenderer {
  //   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
  //     Component c = super.getTableCellRendererComponent(
  //         table, value, isSelected, hasFocus, row, column);
  //     TableModel model = table.getModel();
  //     Boolean b = (Boolean) model.getValueAt(
  //         table.convertRowIndexToModel(row), BOOLEAN_COLUMN);
  //     c.setForeground(table.getForeground());
  //     c.setBackground(b ? Color.ORANGE : table.getBackground());
  //     return c;
  //   }
  // }

  private static void rowRepaint(JTable table, int row) {
    Rectangle r = table.getCellRect(row, 0, true);
    // r.height = table.getRowHeight();
    r.width = table.getWidth();
    table.repaint(r);
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
