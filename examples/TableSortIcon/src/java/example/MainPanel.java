// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final Icon EMPTY_ICON = new EmptyIcon();

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    JButton clearButton = new JButton("clear SortKeys");
    clearButton.addActionListener(e -> table.getRowSorter().setSortKeys(null));

    add(makeRadioPane(table), BorderLayout.NORTH);
    add(clearButton, BorderLayout.SOUTH);
    add(new JScrollPane(table));
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

  private Box makeRadioPane(JTable table) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL ascendingPath = cl.getResource("example/ascending.png");
    URL descendingPath = cl.getResource("example/descending.png");
    JRadioButton r0 = new JRadioButton("Default", true);
    JRadioButton r1 = new JRadioButton("Empty");
    JRadioButton r2 = new JRadioButton("Custom");
    ActionListener al = e -> {
      JRadioButton r = (JRadioButton) e.getSource();
      Icon ascending;
      Icon descending;
      if (r.equals(r2) && ascendingPath != null && descendingPath != null) {
        ascending = new IconUIResource(new ImageIcon(ascendingPath));
        descending = new IconUIResource(new ImageIcon(descendingPath));
      } else if (r.equals(r1)) {
        ascending = new IconUIResource(EMPTY_ICON);
        descending = new IconUIResource(EMPTY_ICON);
      } else { // if (r.equals(r0)) { // default
        ascending = UIManager.getLookAndFeelDefaults().getIcon("Table.ascendingSortIcon");
        descending = UIManager.getLookAndFeelDefaults().getIcon("Table.descendingSortIcon");
      }
      UIManager.put("Table.ascendingSortIcon", ascending);
      UIManager.put("Table.descendingSortIcon", descending);
      table.getTableHeader().repaint();
    };
    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    ButtonGroup bg = new ButtonGroup();
    box1.add(new JLabel("Table Sort Icon: "));
    Stream.of(r0, r1, r2).forEach(rb -> {
      box1.add(rb);
      box1.add(Box.createHorizontalStrut(5));
      bg.add(rb);
      rb.addActionListener(al);
    });
    box1.add(Box.createHorizontalGlue());
    return box1;
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

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
