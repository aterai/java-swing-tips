// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    TableModel model = makeModel();
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(e -> textArea.setText(""));
    tabbedPane.addTab("JTable", makeTable0(model, textArea));
    tabbedPane.addTab("SelectionModel", makeTable1(model, textArea));
    tabbedPane.addTab("Row/Column", makeTable2(model, textArea));
    tabbedPane.addTab("changeSelection", makeTable3(model, textArea));
    add(tabbedPane);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable0(TableModel model, JTextArea textArea) {
    JTable table0 = new JTable(model);
    table0.setCellSelectionEnabled(true);
    ListSelectionListener lsl0 = new AbstractTableCellSelectionListener() {
      @Override public void valueChanged(ListSelectionEvent e) {
        int sr = table0.getSelectedRow();
        int sc = table0.getSelectedColumn();
        if (!e.getValueIsAdjusting() && updateRowColumnInfo(sr, sc)) {
          Object o = table0.getValueAt(sr, sc);
          textArea.append(String.format("(%d, %d) %s%n", sr, sc, o));
          textArea.setCaretPosition(textArea.getDocument().getLength());
        }
      }
    };
    table0.getSelectionModel().addListSelectionListener(lsl0);
    table0.getColumnModel().getSelectionModel().addListSelectionListener(lsl0);
    return table0;
  }

  private static JTable makeTable1(TableModel model, JTextArea textArea) {
    JTable table1 = new JTable(model);
    table1.setCellSelectionEnabled(true);
    ListSelectionListener lsl1 = new AbstractTableCellSelectionListener() {
      @Override public void valueChanged(ListSelectionEvent e) {
        int sr = table1.getSelectionModel().getLeadSelectionIndex();
        int sc = table1.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (!e.getValueIsAdjusting() && updateRowColumnInfo(sr, sc)) {
          Object o = table1.getValueAt(sr, sc);
          textArea.append(String.format("(%d, %d) %s%n", sr, sc, o));
          textArea.setCaretPosition(textArea.getDocument().getLength());
        }
      }
    };
    table1.getSelectionModel().addListSelectionListener(lsl1);
    table1.getColumnModel().getSelectionModel().addListSelectionListener(lsl1);
    return table1;
  }

  private static JTable makeTable2(TableModel model, JTextArea textArea) {
    JTable table2 = new JTable(model);
    table2.setCellSelectionEnabled(true);
    table2.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int firstIndex = e.getFirstIndex();
        int lastIndex = e.getLastIndex();
        textArea.append(String.format("row first, last: %d, %d%n", firstIndex, lastIndex));
        ListSelectionModel m = (ListSelectionModel) e.getSource();
        int asi = m.getAnchorSelectionIndex();
        int lsi = m.getLeadSelectionIndex();
        textArea.append(String.format("row anchor->lead: %d->%d%n", asi, lsi));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    });
    table2.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int firstIndex = e.getFirstIndex();
        int lastIndex = e.getLastIndex();
        textArea.append(String.format("column first, last: %d, %d%n", firstIndex, lastIndex));
        ListSelectionModel m = (ListSelectionModel) e.getSource();
        int asi = m.getAnchorSelectionIndex();
        int lsi = m.getLeadSelectionIndex();
        textArea.append(String.format("column anchor->lead: %d->%d%n", asi, lsi));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    });
    return table2;
  }

  private static JTable makeTable3(TableModel model, JTextArea textArea) {
    JTable table3 = new JTable(model) {
      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        textArea.append(String.format("changeSelection: %d, %d%n", rowIndex, columnIndex));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    };
    table3.setCellSelectionEnabled(true);
    return table3;
  }

  private static TableModel makeModel() {
    return new DefaultTableModel(6, 7) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }

      @Override public Object getValueAt(int row, int column) {
        return row * getColumnCount() + column;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
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

abstract class AbstractTableCellSelectionListener implements ListSelectionListener {
  private int prevRow = -1;
  private int prevCol = -1;

  protected boolean updateRowColumnInfo(int sr, int sc) {
    boolean flg = prevRow == sr && prevCol == sc;
    prevRow = sr;
    prevCol = sc;
    return !flg;
  }
}
