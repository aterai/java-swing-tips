// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Object[] columnNames = {"String0", "String111", "String22222"};
    Object[][] data = {
        {"a", "bb", "ccc"}, {"dd", "ee", "ff"}, {"aa", "aaa", "a"}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setShowVerticalLines(false);
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    RowSorter<?> sorter = table.getRowSorter();
    if (sorter instanceof DefaultRowSorter) {
      for (int i = 0; i < table.getColumnCount(); i++) {
        ((DefaultRowSorter<?, ?>) sorter).setSortable(i, false);
      }
    }

    HyperlinkHeaderCellRenderer handler = new HyperlinkHeaderCellRenderer();
    table.getTableHeader().setDefaultRenderer(handler);
    table.getTableHeader().addMouseListener(handler);
    table.getTableHeader().addMouseMotionListener(handler);
    table.getTableHeader().setBackground(table.getBackground());

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());

    setOpaque(true);
    setBackground(table.getBackground());
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(scroll);
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

class HyperlinkHeaderCellRenderer extends DefaultTableCellRenderer implements MouseInputListener {
  private final Border border = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
      BorderFactory.createEmptyBorder(4, 1, 3, 2));
  private final Color alphaZero = new Color(0x0, true);
  private int col = -1;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String str = Objects.toString(value, "");
    String sort = "";
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    if (Objects.nonNull(sorter) && !sorter.getSortKeys().isEmpty()) {
      RowSorter.SortKey sortKey = sorter.getSortKeys().get(0);
      if (column == sortKey.getColumn()) {
        String k = sortKey.getSortOrder() == SortOrder.ASCENDING ? "▴" : "▾";
        sort = "<small>" + k;
      }
    }
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      if (this.col == column) {
        l.setText("<html><u><font color='blue'>" + str + "</u>" + sort);
      } else if (hasFocus) {
        l.setText("<html><font color='blue'>" + str + sort);
      } else {
        l.setText("<html>" + str + sort);
      }
      l.setHorizontalAlignment(LEADING);
      l.setOpaque(false);
      l.setBackground(alphaZero);
      l.setForeground(Color.BLACK);
      l.setBorder(border);
    }
    return c;
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 24;
    return d;
  }

  private Rectangle getTextRect(JTableHeader header, int idx) {
    JTable table = header.getTable();
    TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
    Object headerValue = header.getColumnModel().getColumn(idx).getHeaderValue();
    Component c = hr.getTableCellRendererComponent(table, headerValue, false, true, 0, idx);
    Rectangle viewRect = new Rectangle(header.getHeaderRect(idx));
    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      Insets ins = l.getInsets();
      viewRect.x += ins.left;
      viewRect.width -= ins.left + ins.right;
      SwingUtilities.layoutCompoundLabel(
          l,
          l.getFontMetrics(l.getFont()),
          l.getText(),
          l.getIcon(),
          l.getVerticalAlignment(),
          l.getHorizontalAlignment(),
          l.getVerticalTextPosition(),
          l.getHorizontalTextPosition(),
          viewRect,
          iconRect,
          textRect,
          l.getIconTextGap());
    }
    return textRect;
  }

  @Override public void mouseMoved(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    int ci = header.columnAtPoint(e.getPoint());
    col = getTextRect(header, ci).contains(e.getPoint()) ? ci : -1;
    header.repaint(header.getHeaderRect(ci));
  }

  @Override public void mouseExited(MouseEvent e) {
    col = -1;
    e.getComponent().repaint();
  }

  @Override public void mouseClicked(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    JTable table = header.getTable();
    int ci = header.columnAtPoint(e.getPoint());
    int idx = table.convertColumnIndexToModel(ci);
    if (getTextRect(header, ci).contains(e.getPoint())) {
      RowSorter<?> sorter = table.getRowSorter();
      if (sorter instanceof DefaultRowSorter) {
        ((DefaultRowSorter<?, ?>) sorter).setSortable(idx, true);
        sorter.toggleSortOrder(idx);
        ((DefaultRowSorter<?, ?>) sorter).setSortable(idx, false);
      }
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
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
