// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int FIXED_RANGE = 2;
  private static final String ES = "";

  private MainPanel() {
    super(new BorderLayout());
    // RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    JTable fixedTable = new JTable(table.getModel());
    fixedTable.setSelectionModel(table.getSelectionModel());
    fixedTable.setRowSorter(table.getRowSorter());
    fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fixedTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    for (int i = table.getColumnCount() - 1; i >= 0; i--) {
      if (i < FIXED_RANGE) {
        table.removeColumn(table.getColumnModel().getColumn(i));
        fixedTable.getColumnModel().getColumn(i).setResizable(false);
      } else {
        fixedTable.removeColumn(fixedTable.getColumnModel().getColumn(i));
      }
    }

    JScrollPane scroll = new JScrollPane(table);
    // JViewport viewport = new JViewport();
    // viewport.setView(fixedTable);
    // viewport.setPreferredSize(fixedTable.getPreferredSize());
    // scroll.setRowHeader(viewport);

    fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
    scroll.setRowHeaderView(fixedTable);
    scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.getRowHeader().setBackground(Color.WHITE);

    // <blockquote cite="https://tips4java.wordpress.com/2008/11/05/fixed-column-table/">
    // @author Rob Camick
    scroll.getRowHeader().addChangeListener(e -> {
      JViewport viewport = (JViewport) e.getSource();
      scroll.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    });
    // </blockquote>

    JButton addButton = new JButton("add 0..<100");
    addButton.addActionListener(e -> {
      table.getRowSorter().setSortKeys(null);
      DefaultTableModel m = (DefaultTableModel) table.getModel();
      IntStream.range(0, 100)
          .mapToObj(i -> new Object[] {i, i + 1, "A" + i, "B" + i})
          .forEach(m::addRow);
    });

    add(scroll);
    add(addButton, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    // <blockquote cite="FixedColumnExample.java">
    // @author Nobuo Tamemasa
    Object[][] data = {
        {1, 11, "A", ES, ES, ES, ES, ES},
        {2, 22, ES, "B", ES, ES, ES, ES},
        {3, 33, ES, ES, "C", ES, ES, ES},
        {4, 1, ES, ES, ES, "D", ES, ES},
        {5, 55, ES, ES, ES, ES, "E", ES},
        {6, 66, ES, ES, ES, ES, ES, "F"}
    };
    String[] columnNames = {"fixed 1", "fixed 2", "A", "B", "C", "D", "E", "F"};
    // </blockquote>
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column < FIXED_RANGE ? Integer.class : Object.class;
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
