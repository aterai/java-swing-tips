// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
  private final Object[][] data = {
    {true, 1, "BBB"}, {false, 12, "AAA"},
    {true, 2, "DDD"}, {false, 5, "CCC"},
    {true, 3, "EEE"}, {false, 6, "GGG"},
    {true, 4, "FFF"}, {false, 7, "HHH"}
  };
  private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model) {
    protected static final int CHECKBOX_COLUMN = 0;
    private transient HeaderCheckBoxHandler handler;
    @Override public void updateUI() {
      // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
      // https://bugs.openjdk.java.net/browse/JDK-6788475
      // XXX: set dummy ColorUIResource
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
  };

  public MainPanel() {
    super(new BorderLayout());
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class HeaderRenderer implements TableCellRenderer {
  private final JCheckBox check = new JCheckBox("");
  private final JLabel label = new JLabel("Check All");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof Status) {
      switch ((Status) value) {
        case SELECTED:
          check.setSelected(true);
          check.setEnabled(true);
          break;
        case DESELECTED:
          check.setSelected(false);
          check.setEnabled(true);
          break;
        case INDETERMINATE:
          check.setSelected(true);
          check.setEnabled(false);
          break;
        default:
          throw new AssertionError("Unknown Status");
      }
    } else {
      check.setSelected(true);
      check.setEnabled(false);
    }
    check.setOpaque(false);
    check.setFont(table.getFont());
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    JLabel l = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    label.setIcon(new ComponentIcon(check));
    l.setIcon(new ComponentIcon(label));
    l.setText(null); // XXX: Nimbus???
    // System.out.println("getHeaderRect: " + table.getTableHeader().getHeaderRect(column));
    // System.out.println("getPreferredSize: " + l.getPreferredSize());
    // System.out.println("getMaximumSize: " + l.getMaximumSize());
    // System.out.println("----");
    // if (l.getPreferredSize().height > 1000) { // XXX: Nimbus???
    //   System.out.println(l.getPreferredSize().height);
    //   Rectangle rect = table.getTableHeader().getHeaderRect(column);
    //   l.setPreferredSize(new Dimension(0, rect.height));
    // }
    return l;
  }
}

class ComponentIcon implements Icon {
  private final Component cmp;

  protected ComponentIcon(Component cmp) {
    this.cmp = cmp;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
  }

  @Override public int getIconWidth() {
    return cmp.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return cmp.getPreferredSize().height;
  }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
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
