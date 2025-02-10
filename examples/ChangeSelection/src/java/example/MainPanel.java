// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setCellSelectionEnabled(true);

    String actionMapKey = "clear-selection";
    table.getActionMap().put(actionMapKey, new AbstractAction(actionMapKey) {
      @Override public void actionPerformed(ActionEvent e) {
        table.clearSelection();
        requestFocusInWindow();
      }
    });
    InputMap im = table.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionMapKey);

    int rowCount = table.getRowCount() - 1;
    int colCount = table.getColumnCount() - 1;
    SpinnerNumberModel rowField = new SpinnerNumberModel(1, 0, rowCount, 1);
    SpinnerNumberModel colField = new SpinnerNumberModel(2, 0, colCount, 1);
    JCheckBox toggle = new JCheckBox("toggle", false);
    JCheckBox extend = new JCheckBox("extend", false);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(new JLabel("row:"));
    box.add(new JSpinner(rowField));
    box.add(new JLabel(" col:"));
    box.add(new JSpinner(colField));
    box.add(toggle);
    box.add(extend);

    JButton changeSelection = new JButton("changeSelection");
    changeSelection.addActionListener(e -> {
      int row = rowField.getNumber().intValue();
      int col = colField.getNumber().intValue();
      // col = table.convertColumnIndexToModel(col);
      table.changeSelection(row, col, toggle.isSelected(), extend.isSelected());
      table.requestFocusInWindow();
      table.repaint();
    });

    JButton clear = new JButton("clear(Esc)");
    clear.addActionListener(e -> {
      table.clearSelection();
      requestFocusInWindow();
    });

    JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    p.add(changeSelection);
    p.add(clear);

    String title = "JTable#changeSelection(int, int, boolean, boolean)";
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(box, BorderLayout.NORTH);
    panel.add(p, BorderLayout.SOUTH);

    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B", "C"};
    Object[][] data = {
        {"0, 0", "0, 1", "0, 2"},
        {"1, 0", "1, 1", "1, 2"},
        {"2, 0", "2, 1", "2, 2"},
        {"3, 0", "3, 1", "3, 2"},
        {"4, 0", "4, 1", "4, 2"},
        {"5, 0", "5, 1", "5, 2"},
        {"6, 0", "6, 1", "6, 2"},
        {"7, 0", "7, 1", "7, 2"},
        {"8, 0", "8, 1", "8, 2"},
        {"9, 0", "9, 1", "9, 2"}
    };
    return new DefaultTableModel(data, columnNames) {
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
