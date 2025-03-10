// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = makeTable(makeModel());
    table.setDefaultRenderer(RowData.class, makeRenderer());
    RowSorter<? extends TableModel> sorter = table.getRowSorter();
    if (sorter instanceof TableRowSorter) {
      TableRowSorter<? extends TableModel> rs = (TableRowSorter<? extends TableModel>) sorter;
      rs.setComparator(0, Comparator.comparing(RowData::getPosition));
      rs.setComparator(1, Comparator.comparing(RowData::getTeam));
      rs.setComparator(2, Comparator.comparing(RowData::getMatches));
      rs.setComparator(3, Comparator.comparing(RowData::getWins));
      rs.setComparator(4, Comparator.comparing(RowData::getDraws));
      rs.setComparator(5, Comparator.comparing(RowData::getLosses));
      rs.setComparator(6, Comparator.comparing(RowData::getGoalsFor));
      rs.setComparator(7, Comparator.comparing(RowData::getGoalsAgainst));
      rs.setComparator(8, Comparator.comparing(RowData::getGoalDifference));
      rs.setComparator(9, Comparator.comparing(RowData::getPoints)
          .thenComparing(RowData::getGoalDifference));
    }
    // add(new JLayer<>(new JScrollPane(table), new BorderPaintLayerUI()));
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableCellRenderer makeRenderer() {
    return new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel && value instanceof RowData) {
          JLabel l = (JLabel) c;
          int col = table.convertColumnIndexToModel(column);
          l.setHorizontalAlignment(col == 1 ? LEADING : CENTER);
          l.setText(((RowData) value).toString(col));
        }
        return c;
      }
    };
  }

  private JTable makeTable(TableModel model) {
    return new JTable(model) {
      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        boolean isSelected = isRowSelected(row);
        if (!isSelected) {
          RowData data = (RowData) model.getValueAt(convertRowIndexToModel(row), 0);
          int num = data.getPosition();
          boolean promotion = num <= 2;
          boolean promotionPlayOff = num <= 6;
          boolean relegation = num >= 21;
          if (promotion) {
            c.setBackground(new Color(0xCF_F3_C0));
          } else if (promotionPlayOff) {
            c.setBackground(new Color(0xCB_F7_F5));
          } else if (relegation) {
            c.setBackground(new Color(0xFB_DC_DC));
          } else if (row % 2 == 0) {
            c.setBackground(Color.WHITE);
          } else {
            c.setBackground(new Color(0xF0_F0_F0));
          }
        }
        c.setForeground(Color.BLACK);
        if (c instanceof JLabel && column != 1) {
          ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        }
        return c;
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
        setSelectionBackground(new Color(0, 0, 100, 50));
        setAutoCreateRowSorter(true);
        setFocusable(false);
        initTableHeader(this);
      }
    };
  }

  private static void initTableHeader(JTable table) {
    JTableHeader header = table.getTableHeader();
    ((JLabel) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      boolean isNotTeam = i != 1;
      if (isNotTeam) {
        columnModel.getColumn(i).setMaxWidth(26);
      }
    }
  }

  private static TableModel makeModel() {
    String[] columnNames = {"#", "Team", "MP", "W", "D", "L", "F", "A", "GD", "P"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return RowData.class;
      }
    };
    addRow(model, new RowData(1, "Machida", 20, 7, 6, 57, 27));
    addRow(model, new RowData(2, "Iwata", 17, 11, 7, 61, 39));
    addRow(model, new RowData(3, "Shimizu", 16, 12, 6, 61, 27));
    addRow(model, new RowData(4, "Tokyo", 17, 9, 9, 47, 26));
    addRow(model, new RowData(5, "Nagasaki", 15, 10, 10, 58, 43));
    addRow(model, new RowData(6, "Chiba", 15, 9, 11, 46, 44));
    addRow(model, new RowData(7, "Kofu", 15, 7, 13, 49, 43));
    addRow(model, new RowData(8, "Okayama", 12, 15, 8, 43, 37));
    addRow(model, new RowData(9, "Yamagata", 16, 3, 16, 53, 49));
    addRow(model, new RowData(10, "Oita", 14, 9, 12, 46, 49));
    addRow(model, new RowData(11, "Gunma", 12, 12, 8, 36, 30));
    addRow(model, new RowData(12, "Mito", 11, 12, 12, 45, 53));
    addRow(model, new RowData(13, "Tochigi", 10, 12, 13, 35, 35));
    addRow(model, new RowData(14, "Tokushima", 8, 17, 10, 39, 46));
    addRow(model, new RowData(15, "Akita", 9, 13, 12, 27, 36));
    addRow(model, new RowData(16, "Sendai", 10, 10, 15, 40, 50));
    addRow(model, new RowData(17, "Fujieda", 11, 7, 15, 46, 57));
    addRow(model, new RowData(18, "Kumamoto", 9, 10, 16, 42, 45));
    addRow(model, new RowData(19, "Iwaki", 9, 10, 15, 33, 51));
    addRow(model, new RowData(20, "Yamaguchi", 8, 12, 15, 28, 55));
    addRow(model, new RowData(21, "Kanazawa", 9, 5, 19, 35, 55));
    addRow(model, new RowData(22, "Omiya", 7, 6, 22, 30, 60));
    return model;
  }

  private static void addRow(DefaultTableModel model, RowData data) {
    model.addRow(Collections.nCopies(10, data).toArray());
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

class RowData {
  private final int position;
  private final String team;
  private final int wins;
  private final int draws;
  private final int losses;
  private final int goalsFor;
  private final int goalsAgainst;

  protected RowData(
      int position,
      String team,
      int wins,
      int draws,
      int looses,
      int goalsFor,
      int goalsAgainst) {
    this.position = position;
    this.team = team;
    this.wins = wins;
    this.draws = draws;
    this.losses = looses;
    this.goalsFor = goalsFor;
    this.goalsAgainst = goalsAgainst;
  }

  public int getPosition() {
    return position;
  }

  public String getTeam() {
    return team;
  }

  public int getMatches() {
    return wins + draws + losses;
  }

  public int getWins() {
    return wins;
  }

  public int getDraws() {
    return draws;
  }

  public int getLosses() {
    return losses;
  }

  public int getGoalsFor() {
    return goalsFor;
  }

  public int getGoalsAgainst() {
    return goalsAgainst;
  }

  public int getGoalDifference() {
    return goalsFor - goalsAgainst;
  }

  public int getPoints() {
    return wins * 3 + draws;
  }

  @SuppressWarnings("PMD.CyclomaticComplexity")
  public String toString(int column) {
    String txt;
    switch (column) {
      case 0:
        txt = Integer.toString(getPosition());
        break;
      case 1:
        txt = getTeam();
        break;
      case 2:
        txt = Integer.toString(getMatches());
        break;
      case 3:
        txt = Integer.toString(getWins());
        break;
      case 4:
        txt = Integer.toString(getDraws());
        break;
      case 5:
        txt = Integer.toString(getLosses());
        break;
      case 6:
        txt = Integer.toString(getGoalsFor());
        break;
      case 7:
        txt = Integer.toString(getGoalsAgainst());
        break;
      case 8:
        int d = getGoalDifference();
        txt = d > 0 ? "+" + d : Integer.toString(d);
        break;
      default: // case 9:
        txt = Integer.toString(getPoints());
    }
    return txt;
  }
}
