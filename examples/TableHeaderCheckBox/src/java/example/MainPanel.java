// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"}, {true, 2, "DDD"}, {false, 5, "CCC"},
        {true, 3, "EEE"}, {false, 6, "GGG"}, {true, 4, "FFF"}, {false, 7, "HHH"}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      private static final int MODEL_COLUMN_IDX = 0;
      private transient HeaderCheckBoxHandler handler;

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
    };
    table.setFillsViewportHeight(true);
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
      if (m instanceof DefaultTableModel && checkRepaint((DefaultTableModel) m, column, status)) {
        JTableHeader h = table.getTableHeader();
        h.repaint(h.getHeaderRect(vci));
      }
    }
  }

  private boolean checkRepaint(DefaultTableModel m, TableColumn column, Object status) {
    boolean repaint;
    if (status == Status.INDETERMINATE) {
      List<?> data = m.getDataVector();
      List<Boolean> l = data.stream()
          .map(v -> (Boolean) ((List<?>) v).get(targetColumnIndex))
          .distinct()
          .collect(Collectors.toList());
      repaint = l.size() == 1;
      if (repaint) {
        boolean isSelected = l.get(0);
        column.setHeaderValue(isSelected ? Status.SELECTED : Status.DESELECTED);
      }
    } else {
      column.setHeaderValue(Status.INDETERMINATE);
      repaint = true;
    }
    return repaint;
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
    JTable tbl = header.getTable();
    TableModel m = tbl.getModel();
    int vci = tbl.columnAtPoint(e.getPoint());
    int mci = tbl.convertColumnIndexToModel(vci);
    if (mci == targetColumnIndex && m.getRowCount() > 0) {
      TableColumnModel columnModel = tbl.getColumnModel();
      TableColumn column = columnModel.getColumn(vci);
      boolean b = column.getHeaderValue() == Status.DESELECTED;
      for (int i = 0; i < m.getRowCount(); i++) {
        m.setValueAt(b, i, mci);
      }
      column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
      // header.repaint();
    }
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

enum Status {
  SELECTED, DESELECTED, INDETERMINATE
}
