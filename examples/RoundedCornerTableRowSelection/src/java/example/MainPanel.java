// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setRowHeight(24);
        setIntercellSpacing(new Dimension(0, 3));
        setShowGrid(false);
        setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        TableColumnModel columns = getColumnModel();
        TableCellRenderer r = new RoundSelectionRenderer();
        for (int i = 0; i < columns.getColumnCount(); i++) {
          columns.getColumn(i).setCellRenderer(r);
        }
      }
    };
    JScrollPane scroll = new JScrollPane(table);
    scroll.setBackground(Color.WHITE);
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setOpaque(true);
    setBackground(Color.WHITE);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"A", "B", "C", "Integer"};
    Object[][] data = {
        {"aaa", "aa", "a", 12},
        {"bbb", "bb", "b", 5},
        {"ccc", "cc", "c", 92},
        {"ddd", "dd", "d", 0}
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

class RoundSelectionRenderer extends DefaultTableCellRenderer {
  private static final double ARC = 6d;
  private Position pos;

  @Override public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground().brighter());
    double w = getWidth() - 1d;
    double h = getHeight() - 1d;
    Area area = pos.getArea(w, h, ARC);
    g2.fill(area);
    if (isFocusable()) {
      g2.setColor(getBackground());
      g2.draw(area);
    }
    super.paintComponent(g);
    g2.dispose();
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, false, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      l.setOpaque(false);
      pos = getPosition(table, column);
      boolean b = value instanceof Number;
      l.setHorizontalAlignment(b ? RIGHT : LEFT);
    }
    c.setFocusable(hasFocus || table.getSelectionModel().getLeadSelectionIndex() == row);
    return c;
  }

  private static Position getPosition(JTable table, int column) {
    boolean isFirst = column == 0;
    boolean isLast = column == table.getColumnCount() - 1;
    Position p;
    if (isFirst) {
      p = Position.FIRST;
    } else if (isLast) {
      p = Position.LAST;
    } else {
      p = Position.MIDDLE;
    }
    return p;
  }

  private enum Position {
    FIRST, MIDDLE, LAST;

    public Area getArea(double w, double h, double arc) {
      Area area = new Area();
      if (this == FIRST) {
        area.add(new Area(new Rectangle2D.Double(w - arc, 0d, arc + arc, h)));
        area.add(new Area(new RoundRectangle2D.Double(0d, 0d, w, h, arc, arc)));
      } else if (this == LAST) {
        area.add(new Area(new Rectangle2D.Double(-arc, 0d, arc + arc, h)));
        area.add(new Area(new RoundRectangle2D.Double(0d, 0d, w, h, arc, arc)));
      } else {
        area.add(new Area(new Rectangle2D.Double(-arc, 0d, w + arc + arc, h)));
      }
      return area;
    }
  }
}
