// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"A", "B", "C"};
    Object[][] data = {
        {"aaa", "eee", "ddd"}, {"bbb", "fff", "ggg"}, {"ccc", "hhh", "iii"}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    // table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    // table.setShowHorizontalLines(false);
    // table.setShowVerticalLines(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);

    // TableColumn col = table.getColumnModel().getColumn(0);
    // col.setMinWidth(50);
    // col.setMaxWidth(50);
    // col.setResizable(false);

    TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
    UnderlineCellRenderer underlineRenderer = new UnderlineCellRenderer();
    DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);

    JCheckBox modelCheck = new JCheckBox("edit the cell on single click");
    modelCheck.addActionListener(e -> {
      if (modelCheck.isSelected()) {
        table.setDefaultRenderer(Object.class, underlineRenderer);
        table.addMouseListener(underlineRenderer);
        table.addMouseMotionListener(underlineRenderer);
        ce.setClickCountToStart(1);
      } else {
        table.setDefaultRenderer(Object.class, defaultRenderer);
        table.removeMouseListener(underlineRenderer);
        table.removeMouseMotionListener(underlineRenderer);
        ce.setClickCountToStart(2);
      }
    });
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(modelCheck, BorderLayout.NORTH);
    add(scrollPane);
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

class UnderlineCellRenderer extends DefaultTableCellRenderer
    implements MouseListener, MouseMotionListener {
  private int viewRowIndex = -1;
  private int viewColumnIndex = -1;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      String str = Objects.toString(value, "");
      if (!table.isEditing() && viewRowIndex == row && viewColumnIndex == column) {
        ((JLabel) c).setText("<html><u>" + str);
      } else {
        ((JLabel) c).setText(str);
      }
    }
    return c;
  }

  @Override public void mouseMoved(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    viewRowIndex = table.rowAtPoint(pt);
    viewColumnIndex = table.columnAtPoint(pt);
    if (viewRowIndex < 0 || viewColumnIndex < 0) {
      viewRowIndex = -1;
      viewColumnIndex = -1;
    }
    table.repaint();
  }

  @Override public void mouseExited(MouseEvent e) {
    viewRowIndex = -1;
    viewColumnIndex = -1;
    e.getComponent().repaint();
  }

  @Override public void mouseDragged(MouseEvent e) {
    /* not needed */
  }

  @Override public void mouseClicked(MouseEvent e) {
    /* not needed */
  }

  @Override public void mouseEntered(MouseEvent e) {
    /* not needed */
  }

  @Override public void mousePressed(MouseEvent e) {
    /* not needed */
  }

  @Override public void mouseReleased(MouseEvent e) {
    /* not needed */
  }
}
