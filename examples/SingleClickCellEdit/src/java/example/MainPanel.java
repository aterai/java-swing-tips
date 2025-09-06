// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JTable table = new JTable(makeModel()) {
    @Override public void updateUI() {
      super.updateUI();
      setAutoCreateRowSorter(true);
      setRowSelectionAllowed(true);
      setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      // setFillsViewportHeight(true);
      setIntercellSpacing(new Dimension());
      setShowGrid(false);
      // setShowHorizontalLines(false);
      // setShowVerticalLines(false);
      putClientProperty("terminateEditOnFocusLost", true);
      Optional.ofNullable(getTableHeader())
          .ifPresent(header -> header.setReorderingAllowed(false));
      // TableColumn col = getColumnModel().getColumn(0);
      // col.setMinWidth(50);
      // col.setMaxWidth(50);
      // col.setResizable(false);
    }
  };
  private final TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
  private final TableCellEditor defaultEditor = table.getDefaultEditor(Object.class);
  private final UnderlineCellRenderer underlineRenderer = new UnderlineCellRenderer();

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox modelCheck = new JCheckBox("edit the cell on single click");
    modelCheck.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      initClickCountToStart(b);
    });
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(modelCheck, BorderLayout.NORTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B", "C"};
    Object[][] data = {
        {"aaa", "eee", "ddd"}, {"bbb", "fff", "ggg"}, {"ccc", "hhh", "iii"},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private void initClickCountToStart(boolean isSingleClick) {
    if (defaultEditor instanceof DefaultCellEditor) {
      if (isSingleClick) {
        table.setDefaultRenderer(Object.class, underlineRenderer);
        table.addMouseListener(underlineRenderer);
        table.addMouseMotionListener(underlineRenderer);
        ((DefaultCellEditor) defaultEditor).setClickCountToStart(1);
      } else {
        table.setDefaultRenderer(Object.class, defaultRenderer);
        table.removeMouseListener(underlineRenderer);
        table.removeMouseMotionListener(underlineRenderer);
        ((DefaultCellEditor) defaultEditor).setClickCountToStart(2);
      }
    }
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
