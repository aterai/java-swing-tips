// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setRowHeight(24);
    table.setAutoCreateRowSorter(true);

    TableColumn col = table.getColumnModel().getColumn(1);
    col.setCellRenderer(new ComboCellRenderer());
    col.setCellEditor(new ComboCellEditor());

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Column1", "Column2"};
    Object[][] data = {
        {"colors", makeModel("blue", "violet", "red", "yellow")},
        {"sports", makeModel("basketball", "soccer", "football", "hockey")},
        {"food", makeModel("hot dogs", "pizza", "ravioli", "bananas")},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 1 ? DefaultComboBoxModel.class : String.class;
      }
    };
  }

  private static DefaultComboBoxModel<String> makeModel(String... items) {
    return new DefaultComboBoxModel<String>(items) {
      @Override public String toString() {
        return Objects.toString(getSelectedItem(), "");
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

class ComboCellRenderer implements TableCellRenderer {
  private final JComboBox<String> combo = new JComboBox<>();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    combo.removeAllItems();
    if (value instanceof DefaultComboBoxModel) {
      combo.addItem(Objects.toString(((DefaultComboBoxModel<?>) value).getSelectedItem()));
    }
    return combo;
  }
}

class ComboCellEditor extends AbstractCellEditor implements TableCellEditor {
  private final JComboBox<String> combo = new JComboBox<>();

  protected ComboCellEditor() {
    super();
    combo.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    combo.setEditable(true);
    combo.addActionListener(e -> fireEditingStopped());
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    // combo.setBackground(table.getSelectionBackground());
    if (value instanceof ComboBoxModel) {
      @SuppressWarnings("unchecked")
      ComboBoxModel<String> m = (ComboBoxModel<String>) value;
      combo.setModel(m);
    }
    return combo;
  }

  @Override public Object getCellEditorValue() {
    DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) combo.getModel();
    if (combo.isEditable()) {
      String str = Objects.toString(combo.getEditor().getItem(), "");
      if (!str.isEmpty() && m.getIndexOf(str) < 0) {
        m.insertElementAt(str, 0);
        combo.setSelectedIndex(0);
      }
    }
    return m;
  }
}
