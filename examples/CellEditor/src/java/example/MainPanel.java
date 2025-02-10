// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox modelCheck = new JCheckBox("isCellEditable return false");
    JTable table = new JTable(makeModel(modelCheck));
    TableColumn col = table.getColumnModel().getColumn(0);
    col.setMinWidth(50);
    col.setMaxWidth(50);
    col.setResizable(false);

    DefaultCellEditor dce = new DefaultCellEditor(new JTextField());
    JCheckBox objectCheck = new JCheckBox("setDefaultEditor(Object.class, null)");
    JCheckBox editableCheck = new JCheckBox("setEnabled(false)");
    ActionListener al = e -> {
      table.clearSelection();
      if (table.isEditing()) {
        table.getCellEditor().stopCellEditing();
      }
      table.setDefaultEditor(Object.class, objectCheck.isSelected() ? null : dce);
      table.setEnabled(!editableCheck.isSelected());
    };
    JPanel p = new JPanel(new GridLayout(3, 1));
    Stream.of(modelCheck, objectCheck, editableCheck).forEach(cb -> {
      cb.addActionListener(al);
      p.add(cb);
    });
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel(JCheckBox modelCheck) {
    String[] columnNames = {"No.", "Name", "Comment"};
    Object[][] data = {
        {0, "Name 0", "comment..."}, {1, "Name 1", "Test"},
        {2, "Name d", "ee"}, {3, "Name c", "Test cc"},
        {4, "Name b", "Test bb"}, {5, "Name a", "ff"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int col) {
        return col != 0 && !modelCheck.isSelected();
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
