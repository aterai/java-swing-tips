// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      private transient RowHeaderRenderer handler;
      @Override public void updateUI() {
        getColumnModel().getColumn(0).setCellRenderer(null);
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        handler = new RowHeaderRenderer();
        getColumnModel().getColumn(0).setCellRenderer(handler);
        addMouseListener(handler);
        addMouseMotionListener(handler);
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(24);
    // table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // table.getTableHeader().setReorderingAllowed(false);

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"ccc", 92, true}, {"ddd", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column != 0;
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

class RowHeaderRenderer extends MouseAdapter implements TableCellRenderer {
  private final JLabel renderer = new JLabel();
  private int rollOverRowIndex = -1;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer tcr = table.getTableHeader().getDefaultRenderer();
    boolean focus = row == rollOverRowIndex; // || hasFocus;
    Component c = tcr.getTableCellRendererComponent(table, value, isSelected, focus, -1, -1);
    if (c instanceof JComponent && tcr.getClass().getName().contains("XPDefaultRenderer")) {
      ((JComponent) c).setOpaque(!focus);
      renderer.setIcon(new ComponentIcon(c));
      c = renderer;
    }
    return c;
  }

  // @Override public void mouseMoved(MouseEvent e) {
  //   JTable table = (JTable) e.getSource();
  //   Point pt = e.getPoint();
  //   int column = table.convertColumnIndexToModel(table.columnAtPoint(pt));
  //   rollOverRowIndex = column == 0 ? table.rowAtPoint(pt) : -1;
  //   table.repaint();
  // }

  // @Override public void mouseExited(MouseEvent e) {
  //   JTable table = (JTable) e.getSource();
  //   rollOverRowIndex = -1;
  //   table.repaint();
  // }

  @Override public void mouseMoved(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    int row = table.rowAtPoint(pt);
    int col = table.columnAtPoint(pt);
    int prevRow = rollOverRowIndex;
    int column = table.convertColumnIndexToModel(col);
    rollOverRowIndex = column == 0 ? row : -1;
    Rectangle repaintRect;
    if (column == 0 && row != prevRow) {
      if (rollOverRowIndex >= 0) {
        Rectangle r = table.getCellRect(rollOverRowIndex, col, false);
        repaintRect = prevRow >= 0 ? r.union(table.getCellRect(prevRow, col, false)) : r;
      } else {
        repaintRect = table.getCellRect(prevRow, col, false);
      }
    } else {
      repaintRect = table.getCellRect(prevRow, 0, false);
    }
    table.repaint(repaintRect);
  }

  @Override public void mouseExited(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    int col = table.columnAtPoint(pt);
    int column = table.convertColumnIndexToModel(col);
    if (column != 0) {
      return;
    }
    if (rollOverRowIndex >= 0) {
      table.repaint(table.getCellRect(rollOverRowIndex, col, false));
    }
    rollOverRowIndex = -1;
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
    return 4000; // Short.MAX_VALUE;
  }

  @Override public int getIconHeight() {
    return cmp.getPreferredSize().height + 4; // XXX: +4 for Windows 7
  }
}
