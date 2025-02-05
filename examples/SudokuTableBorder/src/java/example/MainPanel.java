// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int BW1 = 1;
  public static final int BW2 = 2;
  public static final int CELL_SZ = 18;

  private MainPanel() {
    super(new GridBagLayout());
    String[] columnNames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    Integer[][] data = {
        {5, 3, 0, 0, 7, 0, 0, 0, 0},
        {6, 0, 0, 1, 9, 5, 0, 0, 0},
        {0, 9, 8, 0, 0, 0, 0, 6, 0},
        {8, 0, 0, 0, 6, 0, 0, 0, 3},
        {4, 0, 0, 8, 0, 3, 0, 0, 1},
        {7, 0, 0, 0, 2, 0, 0, 0, 6},
        {0, 6, 0, 0, 0, 0, 2, 8, 0},
        {0, 0, 0, 4, 1, 9, 0, 0, 5},
        {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return data[row][column] == 0;
      }
    };
    JTable table = new JTable(model) {
      @Override public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
      }
    };
    for (int i = 0; i < table.getRowCount(); i++) {
      int a = (i + 1) % 3 == 0 ? BW2 : BW1;
      table.setRowHeight(i, CELL_SZ + a);
    }

    table.setCellSelectionEnabled(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getTableHeader().setReorderingAllowed(false);
    table.setBorder(BorderFactory.createEmptyBorder());

    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(false);

    table.setIntercellSpacing(new Dimension());
    table.setRowMargin(0);
    table.getColumnModel().setColumnMargin(0);

    JTextField editor = new JTextField();
    editor.setHorizontalAlignment(SwingConstants.CENTER);
    // editor.setBorder(BorderFactory.createLineBorder(Color.RED));
    table.setDefaultEditor(Integer.class, new DefaultCellEditor(editor) {
      @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Object v = Objects.equals(value, 0) ? "" : value;
        return super.getTableCellEditorComponent(table, v, isSelected, row, column);
      }

      @Override public Object getCellEditorValue() {
        return editor.getText().isEmpty() ? 0 : super.getCellEditorValue();
      }
    });
    table.setDefaultRenderer(Integer.class, new SudokuCellRenderer(data));

    TableColumnModel m = table.getColumnModel();
    m.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    for (int i = 0; i < m.getColumnCount(); i++) {
      TableColumn col = m.getColumn(i);
      int a = (i + 1) % 3 == 0 ? BW2 : BW1;
      col.setPreferredWidth(CELL_SZ + a);
      col.setResizable(false);
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createMatteBorder(BW2, BW2, 0, 0, Color.BLACK));
    scroll.setColumnHeader(new JViewport());
    scroll.getColumnHeader().setVisible(false);

    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static class SudokuCellRenderer extends DefaultTableCellRenderer {
    private final Border b0 = BorderFactory.createMatteBorder(0, 0, BW1, BW1, Color.GRAY);
    private final Border b1 = BorderFactory.createMatteBorder(0, 0, BW2, BW2, Color.BLACK);
    private final Border b2 = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, BW2, 0, Color.BLACK),
        BorderFactory.createMatteBorder(0, 0, 0, BW1, Color.GRAY));
    private final Border b3 = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, BW2, Color.BLACK),
        BorderFactory.createMatteBorder(0, 0, BW1, 0, Color.GRAY));
    private final Integer[][] mask;

    @SuppressWarnings("PMD.UseVarargs")
    protected SudokuCellRenderer(Integer[][] src) {
      super();
      Integer[][] dst = new Integer[src.length][src[0].length];
      for (int i = 0; i < src.length; i++) {
        System.arraycopy(src[i], 0, dst[i], 0, src[0].length);
      }
      this.mask = dst;
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      boolean isEditable = mask[row][column] == 0;
      Component c = super.getTableCellRendererComponent(
          table, value, isEditable && isSelected, hasFocus, row, column);
      c.setFont(isEditable ? c.getFont() : c.getFont().deriveFont(Font.BOLD));
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(CENTER);
        if (isEditable && Objects.equals(value, 0)) {
          l.setText(" ");
        }
        boolean rf = (row + 1) % 3 == 0;
        boolean cf = (column + 1) % 3 == 0;
        if (rf && cf) {
          l.setBorder(b1);
        } else if (rf) {
          l.setBorder(b2);
        } else if (cf) {
          l.setBorder(b3);
        } else {
          l.setBorder(b0);
        }
      }
      return c;
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
