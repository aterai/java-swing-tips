// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JTable table = makeTable();
  private final SpinnerNumberModel rowField;
  private final SpinnerNumberModel colField;
  private final JCheckBox toggle = new JCheckBox("toggle", false);
  private final JCheckBox extend = new JCheckBox("extend", false);

  private MainPanel() {
    super(new BorderLayout());
    rowField = new SpinnerNumberModel(1, 0, table.getRowCount() - 1, 1);
    colField = new SpinnerNumberModel(2, 0, table.getColumnCount() - 1, 1);

    String title = "JTable#changeSelection(int, int, boolean, boolean)";
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(makeToolBox(), BorderLayout.NORTH);
    panel.add(makeButtonPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeToolBox() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(new JLabel("row:"));
    box.add(new JSpinner(rowField));
    box.add(new JLabel(" col:"));
    box.add(new JSpinner(colField));
    box.add(toggle);
    box.add(extend);
    return box;
  }

  private JPanel makeButtonPanel() {
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
    return p;
  }

  private static JTable makeTable() {
    JTable table = new JTable(makeModel());
    table.setCellSelectionEnabled(true);
    String actionMapKey = "clear-selection";
    table.getActionMap().put(actionMapKey, new AbstractAction(actionMapKey) {
      @Override public void actionPerformed(ActionEvent e) {
        table.clearSelection();
        table.requestFocusInWindow();
      }
    });
    InputMap im = table.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionMapKey);
    return table;
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
        {"9, 0", "9, 1", "9, 2"},
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
