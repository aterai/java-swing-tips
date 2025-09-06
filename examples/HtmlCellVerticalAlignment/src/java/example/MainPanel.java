// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String TEXT = "drag select table cells 1 22 333 4444 55555 666666 7777777";

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(16);
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      TableCellRenderer renderer = table.getDefaultRenderer(String.class);
      if (renderer instanceof JLabel && e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg.getSelection().getActionCommand();
        VerticalAlignment va = VerticalAlignment.valueOf(name);
        ((JLabel) renderer).setVerticalAlignment(va.getAlignment());
        table.repaint();
      }
    };
    JPanel p = new JPanel();
    Arrays.asList(VerticalAlignment.values()).forEach(v -> {
      String name = v.name();
      boolean selected = v == VerticalAlignment.CENTER;
      JRadioButton r = new JRadioButton(name, selected);
      r.setActionCommand(name);
      r.addItemListener(handler);
      bg.add(r);
      p.add(r);
    });
    add(new JScrollPane(table));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"html"};
    Object[][] data = {
        {"<html><font color=red>font color red</font><br /> " + TEXT},
        {"<html><font color=green>font color green</font> " + TEXT},
        {"<html><font color=blue>font color blue</font> " + TEXT},
        {"<html><font color=black>font color black</font><br />  " + TEXT},
        {"<html><font color=orange>font color orange</font> " + TEXT},
        {"<html><font color=gray>font color gray</font> " + TEXT},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
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

enum VerticalAlignment {
  TOP(SwingConstants.TOP),
  CENTER(SwingConstants.CENTER),
  BOTTOM(SwingConstants.BOTTOM);

  private final int alignment;

  VerticalAlignment(int alignment) {
    this.alignment = alignment;
  }

  public int getAlignment() {
    return alignment;
  }
}
