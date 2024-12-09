// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(new DefaultTableModel(5, 3));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateRowSorter(true);
    // table.setBackground(Color.BLUE);
    // table.setOpaque(false);
    // table.setBackground(scroll.getBackground());

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBackground(Color.RED);
    scroll.getViewport().setBackground(Color.GREEN);
    // scroll.getViewport().setComponentPopupMenu(makePop());
    // scroll.setComponentPopupMenu(makePop());

    add(makeToolBox(table), BorderLayout.NORTH);
    add(makeColorBox(table), BorderLayout.SOUTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeToolBox(JTable table) {
    JCheckBox check = new JCheckBox("FillsViewportHeight");
    check.addActionListener(e -> table.setFillsViewportHeight(check.isSelected()));

    JButton button = new JButton("clearSelection");
    button.addActionListener(e -> table.clearSelection());

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(button);
    return box;
  }

  private static Component makeColorBox(JTable table) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p.add(new JLabel("table.setBackground: "));

    JRadioButton r1 = new JRadioButton("WHITE", true);
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setBackground(Color.WHITE);
      }
    });

    JRadioButton r2 = new JRadioButton("BLUE");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setBackground(Color.BLUE);
      }
    });

    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, r2).forEach(r -> {
      bg.add(r);
      p.add(r);
    });
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
