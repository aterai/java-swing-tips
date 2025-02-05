// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  public static final int HEADER_HEIGHT = 32;

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(2, 1));

    JTable table1 = makeTable();
    // Bad: >>>>
    JTableHeader header = table1.getTableHeader();
    // Dimension d = header.getPreferredSize();
    // d.height = HEADER_HEIGHT;
    // header.setPreferredSize(d); // addColumn case test
    header.setPreferredSize(new Dimension(100, HEADER_HEIGHT));
    p.add(makeTitledPanel("Bad: JTableHeader#setPreferredSize(...)", new JScrollPane(table1)));
    // <<<<

    JTable table2 = makeTable();
    JScrollPane scroll = new JScrollPane(table2);
    scroll.setColumnHeader(new JViewport() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = HEADER_HEIGHT;
        return d;
      }
    });
    // // or
    // table2.setTableHeader(new JTableHeader(table2.getColumnModel()) {
    //   @Override public Dimension getPreferredSize() {
    //     Dimension d = super.getPreferredSize();
    //     d.height = HEADER_HEIGHT;
    //     return d;
    //   }
    // });
    p.add(makeTitledPanel("Override getPreferredSize()", scroll));

    JTextField info = new JTextField();
    info.setEditable(false);
    JButton button = new JButton("addColumn");
    button.addActionListener(e -> {
      table1.getColumnModel().addColumn(new TableColumn());
      table2.getColumnModel().addColumn(new TableColumn());
      info.setText(String.format("%s - %s", getDim(table1), getDim(table2)));
    });

    Box box = Box.createHorizontalBox();
    box.add(button);
    box.add(info);

    add(p);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String getDim(JTable t) {
    JTableHeader h = t.getTableHeader();
    Dimension d = h.getPreferredSize();
    return String.format("%dx%d", d.width, d.height);
  }

  private static JTable makeTable() {
    JTable table = new JTable(new DefaultTableModel(2, 20));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    return table;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
