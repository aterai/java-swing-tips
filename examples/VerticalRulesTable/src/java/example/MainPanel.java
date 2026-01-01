// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(new JScrollPane(makeTable()));
    add(new JLayer<>(new JScrollPane(makeTable()), new BorderPaintLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A1", "B1", "A2", "B2"};
    Object[][] data = {
        {1, 33, 5, 7},
        {2, 35, 6, 11},
        {3, 34, 7, 12},
        {4, 35, 8, 9},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static JTable makeTable() {
    return new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setFillsViewportHeight(true);
        setShowVerticalLines(false);
        setShowHorizontalLines(false);
        setIntercellSpacing(new Dimension());
        setAutoCreateRowSorter(true);
        getTableHeader().setReorderingAllowed(false);
      }

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JComponent) {
          ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 3));
        }
        return c;
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

class BorderPaintLayerUI extends LayerUI<JScrollPane> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      if (view instanceof JScrollPane) {
        JScrollPane scroll = (JScrollPane) view;
        JViewport viewport = scroll.getViewport();
        JTable table = (JTable) viewport.getView();
        paintVerticalRules(g, table, scroll);
      }
    }
  }

  private void paintVerticalRules(Graphics g, JTable table, JComponent parent) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(UIManager.getColor("Table.gridColor"));
    int columnCount = table.getModel().getColumnCount();
    if (columnCount % 2 == 0) {
      int center = columnCount / 2 - 1;
      double x1 = table.getCellRect(0, center, false).getMaxX();
      Rectangle r = SwingUtilities.calculateInnerArea(parent, null);
      g2.draw(new Line2D.Double(x1, r.getY(), x1, r.getHeight()));
      double x2 = x1 + 2d; // table.getCellRect(0, center + 1, false).getMinX() + 2d;
      g2.draw(new Line2D.Double(x2, r.getY(), x2, r.getHeight()));
      g2.dispose();
    }
  }
}
