// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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
    JTable table0 = new JTable(model);
    table0.setCellSelectionEnabled(true);
    ListSelectionListener selectionListener0 = new AbstractTableCellSelectionListener() {
      @Override public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }
        int sr = table0.getSelectedRow();
        int sc = table0.getSelectedColumn();
        if (getRowColumnAdjusting(sr, sc)) {
          return;
        }
        Object o = table0.getValueAt(sr, sc);
        textArea.append(String.format("(%d, %d) %s%n", sr, sc, o));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    };
    table0.getSelectionModel().addListSelectionListener(selectionListener0);
    table0.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener0);

    JTable table1 = new JTable(model);
    table1.setCellSelectionEnabled(true);
    ListSelectionListener selectionListener1 = new AbstractTableCellSelectionListener() {
      @Override public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }
        int sr = table1.getSelectionModel().getLeadSelectionIndex();
        int sc = table1.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (getRowColumnAdjusting(sr, sc)) {
          return;
        }
        Object o = table1.getValueAt(sr, sc);
        textArea.append(String.format("(%d, %d) %s%n", sr, sc, o));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    };
    table1.getSelectionModel().addListSelectionListener(selectionListener1);
    table1.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener1);

    JTable table2 = new JTable(model);
    table2.setCellSelectionEnabled(true);
    table2.getSelectionModel().addListSelectionListener(e -> {
      if (e.getValueIsAdjusting()) {
        return;
      }
      textArea.append(String.format("row first, last: %d, %d%n", e.getFirstIndex(), e.getLastIndex()));
      ListSelectionModel m = (ListSelectionModel) e.getSource();
      textArea.append(String.format("row anchor->lead: %d->%d%n", m.getAnchorSelectionIndex(), m.getLeadSelectionIndex()));
      textArea.setCaretPosition(textArea.getDocument().getLength());
    });
    table2.getColumnModel().getSelectionModel().addListSelectionListener(e -> {
      if (e.getValueIsAdjusting()) {
        return;
      }
      textArea.append(String.format("column first, last: %d, %d%n", e.getFirstIndex(), e.getLastIndex()));
      ListSelectionModel m = (ListSelectionModel) e.getSource();
      textArea.append(String.format("column anchor->lead: %d->%d%n", m.getAnchorSelectionIndex(), m.getLeadSelectionIndex()));
      textArea.setCaretPosition(textArea.getDocument().getLength());
    });

    JTable table3 = new JTable(model) {
      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        textArea.append(String.format("changeSelection: %d, %d%n", rowIndex, columnIndex));
        textArea.setCaretPosition(textArea.getDocument().getLength());
      }
    };
    table3.setCellSelectionEnabled(true);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(e -> textArea.setText(""));
    tabbedPane.addTab("JTable", table0);
    tabbedPane.addTab("SelectionModel", table1);
    tabbedPane.addTab("Row/Column", table2);
    tabbedPane.addTab("changeSelection", table3);
    add(tabbedPane);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    return new DefaultTableModel() {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }

      @Override public int getRowCount() {
        return 6;
      }

      @Override public int getColumnCount() {
        return 7;
      }

      @Override public Object getValueAt(int row, int column) {
        return row * getColumnCount() + column;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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

  protected boolean getRowColumnAdjusting(int sr, int sc) {
    boolean flg = prevRow == sr && prevCol == sc;
    prevRow = sr;
    prevCol = sc;
    return flg;
  }
}
