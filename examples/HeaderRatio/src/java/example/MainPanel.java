// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    JTextField field = new JTextField("5 : 3 : 2");
    JCheckBox check = new JCheckBox("ComponentListener#componentResized(...)", true);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        if (check.isSelected()) {
          setTableHeaderColumnRatio(table, field.getText().trim());
        }
      }
    });

    JButton button = new JButton("revalidate");
    button.addActionListener(e -> setTableHeaderColumnRatio(table, field.getText().trim()));

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(new JLabel("Ratio:"), BorderLayout.WEST);
    p.add(field);
    p.add(button, BorderLayout.EAST);
    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.setBorder(BorderFactory.createTitledBorder("JTableHeader column width ratio"));
    panel.add(p);
    panel.add(check);

    add(panel, BorderLayout.NORTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
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

  public static void setTableHeaderColumnRatio(JTable table, String text) {
    TableColumnModel m = table.getColumnModel();
    List<Integer> list = getWidthRatioArray(text, m.getColumnCount());
    // System.out.println("a: " + m.getTotalColumnWidth());
    // System.out.println("b: " + table.getSize().width);
    int total = table.getSize().width; // m.getTotalColumnWidth();
    float ratio = total / (float) list.stream().mapToInt(Integer::intValue).sum();
    for (int i = 0; i < m.getColumnCount() - 1; i++) {
      TableColumn col = m.getColumn(i);
      int colWidth = Math.round(list.get(i) * ratio);
      // col.setMaxWidth(colWidth);
      col.setPreferredWidth(colWidth);
      total -= colWidth;
    }
    // m.getColumn(m.getColumnCount() - 1).setMaxWidth(total);
    m.getColumn(m.getColumnCount() - 1).setPreferredWidth(total);
    table.revalidate();
  }

  public static List<Integer> getWidthRatioArray(String text, int length) {
    Stream<Integer> stream;
    try {
      stream = Stream.concat(
          Stream.of(text.split(":"))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .map(Integer::valueOf),
          Stream.generate(() -> 1).limit(length)
      );
    } catch (NumberFormatException ex) {
      Toolkit.getDefaultToolkit().beep();
      String msg = "invalid value.\n" + ex.getMessage();
      JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
      stream = Stream.generate(() -> 1);
    }
    return stream.limit(length).collect(Collectors.toList());
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
