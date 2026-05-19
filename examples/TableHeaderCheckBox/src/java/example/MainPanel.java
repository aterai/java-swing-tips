// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new HeaderCheckBoxTable(createModel());
    table.setFillsViewportHeight(true);
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
  private static final int MODEL_COLUMN_IDX = 0;
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
    TableColumn column = getColumnModel().getColumn(MODEL_COLUMN_IDX);
    column.setHeaderRenderer(new HeaderRenderer());
    column.setHeaderValue(Status.INDETERMINATE);

    handler = new HeaderCheckBoxHandler(this, MODEL_COLUMN_IDX);
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
  private final JCheckBox check = new JCheckBox("");
  private final JLabel label = new JLabel("Check All");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof Status) {
      ((Status) value).configureHeaderCheckBox(check);
    } else {
      Status.INDETERMINATE.configureHeaderCheckBox(check);
    }
    check.setOpaque(false);
    check.setFont(table.getFont());
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      label.setIcon(new ComponentIcon(check));
      l.setIcon(new ComponentIcon(label));
      l.setText(null); // XXX: Nimbus???
    }
    // System.out.println("getHeaderRect: " + table.getTableHeader().getHeaderRect(column));
    // System.out.println("getPreferredSize: " + l.getPreferredSize());
    // System.out.println("getMaximumSize: " + l.getMaximumSize());
    // System.out.println("----");
    // if (l.getPreferredSize().height > 1000) { // XXX: Nimbus???
    //   System.out.println(l.getPreferredSize().height);
    //   Rectangle rect = table.getTableHeader().getHeaderRect(column);
    //   l.setPreferredSize(new Dimension(0, rect.height));
    // }
    return c;
  }
}

class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
  private final JTable table;
  private final int targetColumnIndex;

  protected HeaderCheckBoxHandler(JTable table, int index) {
    super();
    this.table = table;
    this.targetColumnIndex = index;
  }

  @Override public void tableChanged(TableModelEvent e) {
    if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
      int vci = table.convertColumnIndexToView(targetColumnIndex);
      TableColumn column = table.getColumnModel().getColumn(vci);
      Object status = column.getHeaderValue();
      TableModel m = table.getModel();
      if (updateHeaderState(m, column, status)) {
        JTableHeader h = table.getTableHeader();
        h.repaint(h.getHeaderRect(vci));
      }
    }
  }

  private boolean updateHeaderState(TableModel model, TableColumn column, Object status) {
    boolean repaint;
    if (status == Status.INDETERMINATE) {
      repaint = updateIndeterminateHeaderState(model, column);
    } else {
      setIndeterminateHeader(column);
      repaint = true;
    }
    return repaint;
  }

  private void setIndeterminateHeader(TableColumn column) {
    column.setHeaderValue(Status.INDETERMINATE);
  }

  private boolean updateIndeterminateHeaderState(TableModel model, TableColumn column) {
    boolean repaint = false;
    Status status = resolveHeaderState(model);
    if (status != null) {
      column.setHeaderValue(status);
      repaint = true;
    }
    return repaint;
  }

  private Status resolveHeaderState(TableModel model) {
    Status status = null;
    int rowCount = model.getRowCount();
    if (rowCount > 0) {
      // List<Boolean> values = ((DefaultTableModel) model).getDataVector().stream()
      //     .map(v -> (Boolean) ((List<?>) v).get(targetColumnIndex))
      //     .limit(2)
      //     .distinct()
      //     .collect(Collectors.toList()); // Java 16: .toList();
      List<Boolean> values = IntStream.range(0, rowCount)
          .mapToObj(i -> Objects.equals(model.getValueAt(i, targetColumnIndex), true))
          .distinct()
          .limit(2)
          .collect(Collectors.toList()); // Java 16: .toList();
      if (values.size() == 1) {
        boolean isSelected = values.get(0); // Java 21: l.getFirst();
        status = isSelected ? Status.SELECTED : Status.DESELECTED;
      }
    }
    return status;
  }

  // private boolean fireUpdateEvent(TableModel m, TableColumn column, Object status) {
  //   if (status == Status.INDETERMINATE) {
  //     boolean selected = true;
  //     boolean deselected = true;
  //     for (int i = 0; i < m.getRowCount(); i++) {
  //       Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
  //       selected &= b;
  //       deselected &= !b;
  //       if (selected == deselected) {
  //         return false;
  //       }
  //     }
  //     if (deselected) {
  //       column.setHeaderValue(Status.DESELECTED);
  //     } else if (selected) {
  //       column.setHeaderValue(Status.SELECTED);
  //     } else {
  //       return false;
  //     }
  //   } else {
  //     column.setHeaderValue(Status.INDETERMINATE);
  //   }
  //   return true;
  // }

  @Override public void mouseClicked(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    if (!header.isEnabled()) {
      return;
    }
    JTable tbl = header.getTable();
    TableModel model = tbl.getModel();
    int vci = tbl.columnAtPoint(e.getPoint());
    int mci = tbl.convertColumnIndexToModel(vci);
    if (mci == targetColumnIndex && model.getRowCount() > 0) {
      TableColumn column = tbl.getColumnModel().getColumn(vci);
      boolean select = column.getHeaderValue() == Status.DESELECTED;
      toggleAllRows(model, mci, select);
      column.setHeaderValue(select ? Status.SELECTED : Status.DESELECTED);
      // header.repaint();
    }
  }

  private void toggleAllRows(TableModel model, int columnIndex, boolean selected) {
    for (int i = 0; i < model.getRowCount(); i++) {
      model.setValueAt(selected, i, columnIndex);
    }
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
