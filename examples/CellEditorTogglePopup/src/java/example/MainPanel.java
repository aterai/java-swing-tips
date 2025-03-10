// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"Item 0", "Item 1", "Item 2"};
    String[] columnNames = {"Default", "setEnabled", "String"};
    Object[][] data = {
        {model[0], model[0], "aaa"}, {model[1], model[2], "bbb"}
    };
    JTable table = new JTable(new DefaultTableModel(data, columnNames));
    table.setRowHeight(20);

    TableColumnModel cm = table.getColumnModel();
    cm.getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<>(model)));

    JComboBox<String> comboBox = new JComboBox<>(model);
    // comboBox.setEnabled(false);
    comboBox.addAncestorListener(new AncestorListener() {
      @Override public void ancestorAdded(AncestorEvent e) {
        Component c = e.getComponent();
        c.setEnabled(false);
        EventQueue.invokeLater(() -> c.setEnabled(true));
      }

      @Override public void ancestorRemoved(AncestorEvent e) {
        // OR: e.getComponent().setEnabled(false);
      }

      @Override public void ancestorMoved(AncestorEvent e) {
        /* not needed */
      }
    });
    cm.getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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
