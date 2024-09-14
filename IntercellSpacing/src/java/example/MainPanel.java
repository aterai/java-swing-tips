// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JCheckBox) {
          c.setBackground(getSelectionBackground());
        }
        return c;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(true);
    table.setFillsViewportHeight(true);
    table.setFocusable(false);
    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(false);
    table.setIntercellSpacing(new Dimension());

    JCheckBox check1 = new JCheckBox("setShowVerticalLines");
    check1.addActionListener(e -> {
      Dimension d = table.getIntercellSpacing();
      if (((JCheckBox) e.getSource()).isSelected()) {
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, d.height));
      } else {
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, d.height));
      }
    });

    JCheckBox check2 = new JCheckBox("setShowHorizontalLines");
    check2.addActionListener(e -> {
      Dimension d = table.getIntercellSpacing();
      if (((JCheckBox) e.getSource()).isSelected()) {
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(d.width, 1));
      } else {
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(d.width, 0));
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(check1, BorderLayout.WEST);
    p.add(check2, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return column == 2;
      }

      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
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
