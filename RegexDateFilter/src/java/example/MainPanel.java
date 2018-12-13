// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Calendar cal = Calendar.getInstance();
    cal.set(2002, 12 - 1, 31, 10, 30, 15);
    Date date = cal.getTime();
    cal.add(Calendar.DATE, -2);
    Date start = cal.getTime();
    cal.add(Calendar.DATE, 9);
    Date end = cal.getTime();

    System.out.println(date.toString()); // --> Tue Dec 31 10:30:15 JST 2002

    Object[][] data = {
      {date}, {start}, {end}
    };
    DefaultTableModel model = new DefaultTableModel(data, new String[] {"Date"}) {
      @Override public Class<?> getColumnClass(int column) {
        return Date.class;
      }
    };

    // // LocalDateTime test:
    // LocalDateTime d = LocalDateTime.of(2002, 12, 31, 0, 0);
    // Object[][] data = {
    //   {date, d},
    //   {start, d.minus(2, ChronoUnit.DAYS)},
    //   {end, d.plus(7, ChronoUnit.DAYS)}
    // };
    // DefaultTableModel model = new DefaultTableModel(data, new String[] {"Date", "LocalDateTime"}) {
    //   @Override public Class<?> getColumnClass(int column) {
    //     return column == 0 ? Date.class : column == 1 ? LocalDateTime.class : Object.class;
    //   }
    // };

    JTable table = new JTable(model) {
      @Override public String getToolTipText(MouseEvent e) {
        int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
        return getModel().getValueAt(row, 0).toString();
      }
    };
    TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);

    // RowFilter.regexFilter
    Matcher m1 = Pattern.compile("12").matcher(date.toString());
    System.out.println(m1.find()); // --> false

    Matcher m2 = Pattern.compile("Dec").matcher(date.toString());
    System.out.println(m2.find()); // --> true

    // a customized RegexFilter
    Matcher m3 = Pattern.compile("12").matcher(DateFormat.getDateInstance().format(date));
    System.out.println(m3.find()); // --> true

    JTextField field = new JTextField("(?i)12");

    JRadioButton r0 = new JRadioButton("null", true);
    r0.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        sorter.setRowFilter(null);
      }
    });

    JRadioButton r1 = new JRadioButton("RowFilter.regexFilter");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        sorter.setRowFilter(RowFilter.regexFilter(field.getText()));
      }
    });

    JRadioButton r2 = new JRadioButton("new RowFilter()");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        sorter.setRowFilter(new RegexDateFilter(Pattern.compile(field.getText())));
      }
    });

    Box box = Box.createVerticalBox(); // new JPanel(new GridLayout(2, 1, 5, 5));
    box.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(new JLabel("regex:"), BorderLayout.WEST);
    p1.add(field);

    JPanel p2 = new JPanel();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r0, r1, r2).forEach(rb -> {
      bg.add(rb);
      p2.add(rb);
    });
    box.add(p1);
    box.add(p2);

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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

class RegexDateFilter extends RowFilter<TableModel, Integer> {
  private final Matcher matcher;

  protected RegexDateFilter(Pattern pattern) {
    super();
    this.matcher = pattern.matcher("");
  }

  @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
    // TableModel m = entry.getModel();
    // for (int i = 0; i < m.getColumnCount(); i++) {
    for (int i = entry.getValueCount() - 1; i >= 0; i--) {
      Object v = entry.getValue(i);
      if (v instanceof Date) {
        matcher.reset(DateFormat.getDateInstance().format(v));
      } else {
        matcher.reset(entry.getStringValue(i));
      }
      if (matcher.find()) {
        return true;
      }
    }
    return false;
  }
}
