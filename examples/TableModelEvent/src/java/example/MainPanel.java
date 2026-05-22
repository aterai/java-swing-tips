// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new HeaderCheckBoxTable(createModel());
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"}, {true, 2, "DDD"}, {false, 5, "CCC"},
        {true, 3, "EEE"}, {false, 6, "GGG"}, {true, 4, "FFF"}, {false, 7, "HHH"},
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

class HeaderCheckBoxTable extends JTable {
  private static final int CHECKBOX_COLUMN = 0;
  private transient HeaderCheckBoxHandler handler;

  protected HeaderCheckBoxTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
    // https://bugs.openjdk.org/browse/JDK-6788475
    // Set a temporary ColorUIResource to avoid this issue
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    getTableHeader().removeMouseListener(handler);
    TableModel m = getModel();
    if (Objects.nonNull(m)) {
      m.removeTableModelListener(handler);
    }
    super.updateUI();

    m = getModel();
    for (int i = 0; i < m.getColumnCount(); i++) {
      TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
      if (r instanceof Component) {
        SwingUtilities.updateComponentTreeUI((Component) r);
      }
    }
    TableColumn column = getColumnModel().getColumn(CHECKBOX_COLUMN);
    column.setHeaderRenderer(new HeaderRenderer());
    column.setHeaderValue(Status.INDETERMINATE);

    handler = new HeaderCheckBoxHandler(this, CHECKBOX_COLUMN);
    m.addTableModelListener(handler);
    getTableHeader().addMouseListener(handler);
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    if (c instanceof JCheckBox) {
      JCheckBox b = (JCheckBox) c;
      b.setBackground(getSelectionBackground());
      b.setBorderPainted(true);
    }
    return c;
  }
}

class HeaderRenderer implements TableCellRenderer {
  private final JCheckBox check = new JCheckBox();
  private final JLabel label = new JLabel("Check All");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof Status) {
      ((Status) value).configureHeaderCheckBox(check);
    } else {
      Status.INDETERMINATE.configureHeaderCheckBox(check);
    }
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setOpaque(false);
      check.setOpaque(false);
      boolean isSynth = check.getUI().getClass().getName().contains("Synth");
      if (isSynth) {
        check.setText(" ");
        check.setPreferredSize(l.getPreferredSize());
      }
      label.setOpaque(false);
      label.setIcon(new ComponentIcon(check));
      l.setIcon(new ComponentIcon(label));
      l.setText(null);
    }
    return c;
  }
}

class ComponentIcon implements Icon {
  private final Component cmp;

  protected ComponentIcon(Component cmp) {
    this.cmp = cmp;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Container parent = c.getParent();
    int iconWidth = getIconWidth();
    int iconHeight = getIconHeight();
    SwingUtilities.paintComponent(g, cmp, parent, x, y, iconWidth, iconHeight);
  }

  @Override public int getIconWidth() {
    return cmp.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return cmp.getPreferredSize().height;
    // Icon icon = UIManager.getIcon("CheckBox.icon");
    // return icon == null ? 20 : icon.getIconHeight();
  }
}

enum Status {
  SELECTED {
    @Override /* default */ void configureHeaderCheckBox(JCheckBox check) {
      check.setSelected(true);
      check.setEnabled(true);
    }
  },
  DESELECTED {
    @Override /* default */ void configureHeaderCheckBox(JCheckBox check) {
      check.setSelected(false);
      check.setEnabled(true);
    }
  },
  INDETERMINATE {
    @Override /* default */ void configureHeaderCheckBox(JCheckBox check) {
      check.setSelected(true);
      check.setEnabled(false);
    }
  };

  /* default */ abstract void configureHeaderCheckBox(JCheckBox check);
}

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
    super();
    add("add(true)").addActionListener(e -> addRowActionPerformed(true));
    add("add(false)").addActionListener(e -> addRowActionPerformed(false));
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }

  private void addRowActionPerformed(boolean isSelected) {
    JTable table = (JTable) getInvoker();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.addRow(new Object[] {isSelected, 0, ""});
    table.scrollRectToVisible(table.getCellRect(model.getRowCount() - 1, 0, true));
  }
}
