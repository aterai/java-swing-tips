// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setCellSelectionEnabled(true);
        setAutoCreateRowSorter(true);
        JTableHeader header = getTableHeader();
        TableColumnModel cm = header.getColumnModel();
        ColumnHeaderRenderer r = new ColumnHeaderRenderer();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          cm.getColumn(i).setHeaderRenderer(r);
        }
        cm.getSelectionModel().addListSelectionListener(e -> header.repaint());
      }
    };
    initMap(table, "ascendant", "ctrl UP", SortOrder.ASCENDING);
    initMap(table, "descendant", "ctrl DOWN", SortOrder.DESCENDING);
    initMap(table, "unsorted", "F9", SortOrder.UNSORTED);
    add(new JScrollPane(table));
    add(new JScrollPane(new JTextArea(makeHelp())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeHelp() {
    return String.join("\n",
        "JTableHeader, toggleSortOrder, SPACE(default)",
        "JTableHeader, selectColumnToLeft, LEFT(default)",
        "JTableHeader, selectColumnToRight, RIGHT(default)",
        "JTableHeader, focusTable, ESCAPE(default)",
        "JTableHeader, ascendant, ctrl UP",
        "JTableHeader, descendant, ctrl DOWN",
        "JTableHeader, unsorted, F9",
        "JTable, F8: focusHeader(default)",
        "JTable, ascendant, ctrl UP",
        "JTable, descendant, ctrl DOWN",
        "JTable, unsorted, F9"
    );
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static void initMap(JTable table, String key, String ks, SortOrder order) {
    Action a = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        columnSort(e, order);
      }
    };
    table.getActionMap().put(key, a);
    InputMap im1 = table.getInputMap(WHEN_FOCUSED);
    im1.put(KeyStroke.getKeyStroke(ks), key);
    JTableHeader header = table.getTableHeader();
    header.getActionMap().put(key, a);
    InputMap im2 = header.getInputMap(WHEN_FOCUSED);
    im2.put(KeyStroke.getKeyStroke(ks), key);
  }

  private static void columnSort(ActionEvent e, SortOrder order) {
    Object o = e.getSource();
    if (o instanceof JTable) {
      JTable table = (JTable) o;
      JTableHeader header = table.getTableHeader();
      if (header != null) {
        table.getActionMap().get("focusHeader").actionPerformed(e);
        int col = table.getSelectedColumn();
        sort(table, col, order);
        int id = ActionEvent.ACTION_PERFORMED;
        String cmd = "focusTable";
        ActionEvent ae = new ActionEvent(header, id, cmd);
        header.getActionMap().get(cmd).actionPerformed(ae);
      }
    } else if (o instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) o;
      JTable table = header.getTable();
      int col = getSelectedColumnIndex(header);
      sort(table, col, order);
    }
  }

  private static void sort(JTable table, int col, SortOrder order) {
    if (col >= 0) {
      RowSorter.SortKey sortKey = new RowSorter.SortKey(col, order);
      table.getRowSorter().setSortKeys(Collections.singletonList(sortKey));
    }
  }

  private static int getSelectedColumnIndex(JTableHeader header) {
    int col = -1;
    TableColumnModel cm = header.getColumnModel();
    for (int i = 0; i < cm.getColumnCount(); i++) {
      TableCellRenderer r = cm.getColumn(i).getHeaderRenderer();
      if (r instanceof ColumnHeaderRenderer) {
        col = ((ColumnHeaderRenderer) r).getSelectedColumnIndex();
        if (col >= 0) {
          break;
        }
      }
    }
    return col;
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColumnHeaderRenderer implements TableCellRenderer {
  private int selectedIndex = -1;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (hasFocus) {
      selectedIndex = column;
    }
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    return r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }

  public int getSelectedColumnIndex() {
    return selectedIndex;
  }
}
