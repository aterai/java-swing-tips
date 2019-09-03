// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.getTableHeader().addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (Objects.isNull(sorter) || sorter.getSortKeys().isEmpty()) {
          return;
        }
        JTableHeader h = (JTableHeader) e.getComponent();
        TableColumnModel columnModel = h.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        // ArrayIndexOutOfBoundsException: -1
        if (viewColumn < 0) {
          return;
        }
        int column = columnModel.getColumn(viewColumn).getModelIndex();

        if (column != -1 && e.isShiftDown()) {
          // if (column != -1 && e.isControlDown()) {
          EventQueue.invokeLater(() -> sorter.setSortKeys(null));
        }
      }
    });

    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(60);
    col.setMaxWidth(60);
    col.setResizable(false);

    add(new JLabel("Shift + Click -> Clear Sorting State"), BorderLayout.NORTH);
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
