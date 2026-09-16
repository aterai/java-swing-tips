// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new StandingsTable(createModel());
    table.setAutoCreateRowSorter(true);
    add(new JLayer<>(new JScrollPane(table), new BorderPaintLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    String[] columnNames = {"#", "Team", "MP", "W", "D", "L", "F", "A", "GD", "P"};
    Object[][] data = {
        {1, "Machida", 33, 20, 7, 6, 57, 27, +30, 67},
        {2, "Iwata", 35, 17, 11, 7, 61, 39, +22, 62},
        {3, "Shimizu", 34, 16, 12, 6, 61, 27, +34, 60},
        {4, "Tokyo", 35, 17, 9, 9, 47, 26, +21, 60},
        {5, "Nagasaki", 35, 15, 10, 10, 58, 43, +15, 55},
        {6, "Chiba", 35, 15, 9, 11, 46, 44, +2, 54},
        {7, "Kofu", 35, 15, 7, 13, 49, 43, +6, 52},
        {8, "Okayama", 35, 12, 15, 8, 43, 37, +6, 51},
        {9, "Yamagata", 35, 16, 3, 16, 53, 49, +4, 51},
        {10, "Oita", 35, 14, 9, 12, 46, 49, -3, 51},
        {11, "Gunma", 32, 12, 12, 8, 36, 30, +6, 48},
        {12, "Mito", 35, 11, 12, 12, 45, 53, -8, 45},
        {13, "Tochigi", 35, 10, 12, 13, 35, 35, +0, 42},
        {14, "Tokushima", 35, 8, 17, 10, 39, 46, -7, 41},
        {15, "Akita", 34, 9, 13, 12, 27, 36, -9, 40},
        {16, "Sendai", 35, 10, 10, 15, 40, 50, -10, 40},
        {17, "Fujieda", 33, 11, 7, 15, 46, 57, -11, 40},
        {18, "Kumamoto", 35, 9, 10, 16, 42, 45, -3, 37},
        {19, "Iwaki", 34, 9, 10, 15, 33, 51, -18, 37},
        {20, "Yamaguchi", 35, 8, 12, 15, 28, 55, -27, 36},
        {21, "Kanazawa", 33, 9, 5, 19, 35, 55, -20, 32},
        {22, "Omiya", 35, 7, 6, 22, 30, 60, -30, 27},
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

class StandingsTable extends JTable {
  private static final Color PROMOTION = new Color(0xCF_F3_C0);
  private static final Color PROMOTION_PLAYOFF = new Color(0xCB_F7_F5);
  private static final Color RELEGATION = new Color(0xFB_DC_DC);
  private static final Color ODD_ROW = new Color(0xF0_F0_F0);

  protected StandingsTable(TableModel model) {
    super(model);
  }

  @Override public Component prepareRenderer(
      TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    if (!isRowSelected(row)) {
      Object position = getModel().getValueAt(convertRowIndexToModel(row), 0);
      c.setBackground(getRowBackground((Integer) position, row));
    }
    c.setForeground(Color.BLACK);
    // use the model index so that the alignment survives column reordering
    boolean isTeamColumn = convertColumnIndexToModel(column) == 1;
    if (c instanceof JLabel) {
      ((JLabel) c).setHorizontalAlignment(
          isTeamColumn ? SwingConstants.LEADING : SwingConstants.CENTER);
    }
    return c;
  }

  private static Color getRowBackground(int position, int row) {
    boolean promotion = position <= 2;
    boolean promotionPlayoff = position <= 6;
    boolean relegation = position >= 21;
    Color color;
    if (promotion) {
      color = PROMOTION;
    } else if (promotionPlayoff) {
      color = PROMOTION_PLAYOFF;
    } else if (relegation) {
      color = RELEGATION;
    } else if (row % 2 == 0) {
      color = Color.WHITE;
    } else {
      color = ODD_ROW;
    }
    return color;
  }

  @Override public boolean isCellEditable(int row, int column) {
    return false;
  }

  @Override public void updateUI() {
    super.updateUI();
    setFillsViewportHeight(true);
    setShowVerticalLines(false);
    setShowHorizontalLines(false);
    setIntercellSpacing(new Dimension());
    setSelectionForeground(getForeground());
    setSelectionBackground(new Color(0x32_00_00_64, true));
    setFocusable(false);
    initTableColumns(this);
  }

  private static void initTableColumns(JTable table) {
    JTableHeader header = table.getTableHeader();
    TableCellRenderer renderer = header.getDefaultRenderer();
    if (renderer instanceof JLabel) {
      ((JLabel) renderer).setHorizontalAlignment(SwingConstants.CENTER);
    }
    TableColumnModel columnModel = table.getColumnModel();
    IntStream.range(0, columnModel.getColumnCount())
        .filter(i -> i != 1)
        .forEach(i -> columnModel.getColumn(i).setMaxWidth(26));
    // goal difference: prefix positive values with "+"
    columnModel.getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected, boolean hasFocus, int row, int col) {
        Object txt = value instanceof Integer && (Integer) value > 0 ? "+" + value : value;
        return super.getTableCellRendererComponent(tbl, txt, selected, hasFocus, row, col);
      }
    });
  }
}

class BorderPaintLayerUI extends LayerUI<JScrollPane> {
  private static final int POSITION_COLUMN = 0;
  private static final int POINTS_COLUMN = 9;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    getTable(c).ifPresent(table -> {
      List<? extends RowSorter.SortKey> keys = getSortKeys(table);
      if (keys.isEmpty() || isStandingsOrder(keys.get(0))) {
        paintLines(g, c, table, true);
      } else if (isReversedStandingsOrder(keys.get(0))) {
        paintLines(g, c, table, false);
      }
    });
  }

  private static List<? extends RowSorter.SortKey> getSortKeys(JTable table) {
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    return sorter == null ? Collections.emptyList() : sorter.getSortKeys();
  }

  // rows are ordered from first to last place
  private static boolean isStandingsOrder(RowSorter.SortKey key) {
    int column = key.getColumn();
    SortOrder order = key.getSortOrder();
    return column == POSITION_COLUMN && order == SortOrder.ASCENDING
        || column == POINTS_COLUMN && order == SortOrder.DESCENDING;
  }

  // rows are ordered from last to first place
  private static boolean isReversedStandingsOrder(RowSorter.SortKey key) {
    int column = key.getColumn();
    SortOrder order = key.getSortOrder();
    return column == POSITION_COLUMN && order == SortOrder.DESCENDING
        || column == POINTS_COLUMN && order == SortOrder.ASCENDING;
  }

  private static void paintLines(
      Graphics g, Component layer, JTable table, boolean ascending) {
    Graphics2D g2 = (Graphics2D) g.create();
    for (Boundary b : Boundary.values()) {
      g2.setPaint(b.getColor());
      g2.draw(createUnderline(layer, table, b.getViewRow(table.getRowCount(), ascending)));
    }
    g2.dispose();
  }

  private static Optional<JTable> getTable(Component c) {
    return Optional.of(c)
        .filter(JLayer.class::isInstance)
        .map(layer -> ((JLayer<?>) layer).getView())
        .filter(JScrollPane.class::isInstance)
        .map(scroll -> ((JScrollPane) scroll).getViewport().getView())
        .filter(JTable.class::isInstance)
        .map(JTable.class::cast);
  }

  private static Line2D createUnderline(Component c, JTable table, int row) {
    Rectangle r0 = table.getCellRect(row, 0, false);
    Rectangle r1 = table.getCellRect(row, table.getColumnCount() - 1, false);
    Rectangle r = SwingUtilities.convertRectangle(table, r0.union(r1), c);
    return new Line2D.Double(r.getX(), r.getMaxY(), r.getMaxX(), r.getMaxY());
  }
}

// a line is drawn below the row of the last team in each zone
enum Boundary {
  PROMOTION(2, Color.GREEN.darker()),
  PROMOTION_PLAYOFF(6, Color.BLUE.darker()),
  SAFETY(20, Color.RED.darker());

  private final int lastPosition;
  private final Color color;

  Boundary(int lastPosition, Color color) {
    this.lastPosition = lastPosition;
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public int getViewRow(int rowCount, boolean ascending) {
    return ascending ? lastPosition - 1 : rowCount - lastPosition - 1;
  }
}
