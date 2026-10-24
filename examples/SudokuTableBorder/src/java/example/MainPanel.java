// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int BORDER_WIDTH1 = 1;
  public static final int BORDER_WIDTH2 = 2;
  public static final int CELL_SIZE = 18;

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
        {0, 0, 0, 0, 8, 0, 0, 7, 9},
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return data[row][column] == 0;
      }
    };
    JTable table = createSudokuTable(model);
    JTextField editor = new JTextField();
    editor.setHorizontalAlignment(SwingConstants.CENTER);
    table.setDefaultEditor(Integer.class, new SudokuCellEditor(editor));
    table.setDefaultRenderer(Integer.class, new SudokuCellRenderer(data));

    TableColumnModel columnModel = table.getColumnModel();
    columnModel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      TableColumn col = columnModel.getColumn(i);
      col.setPreferredWidth(getCellSize(i));
      col.setResizable(false);
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    Border outerBorder = BorderFactory.createMatteBorder(
        BORDER_WIDTH2, BORDER_WIDTH2, 0, 0, Color.BLACK);
    scroll.setViewportBorder(outerBorder);
    scroll.setColumnHeader(new JViewport());
    scroll.getColumnHeader().setVisible(false);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  // The cell size including the right or bottom border width,
  // every third row or column has a thick block border.
  private static int getCellSize(int index) {
    boolean isBlockEdge = (index + 1) % 3 == 0;
    return CELL_SIZE + (isBlockEdge ? BORDER_WIDTH2 : BORDER_WIDTH1);
  }

  private static JTable createSudokuTable(TableModel model) {
    JTable table = new JTable(model) {
      @Override public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
      }

      @Override public void updateUI() {
        super.updateUI();
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        getTableHeader().setReorderingAllowed(false);
        setBorder(BorderFactory.createEmptyBorder());
        setShowGrid(false);
        // also sets the row margin and the column margin to 0
        setIntercellSpacing(new Dimension());
      }
    };
    for (int i = 0; i < table.getRowCount(); i++) {
      table.setRowHeight(i, getCellSize(i));
    }
    return table;
  }

  private static final class SudokuCellEditor extends DefaultCellEditor {
    private final JTextField textField;

    private SudokuCellEditor(JTextField textField) {
      super(textField);
      this.textField = textField;
    }

    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      textField.setBorder(new LineBorder(Color.BLACK));
      Object v = Objects.equals(value, 0) ? "" : value;
      return super.getTableCellEditorComponent(table, v, isSelected, row, column);
    }

    // Accept only an empty string or a single digit from 1 to 9,
    // otherwise keep editing and show a red border like JTable.GenericEditor.
    @Override public boolean stopCellEditing() {
      String text = textField.getText().trim();
      boolean isValid = text.isEmpty() || text.matches("[1-9]");
      boolean stopped = isValid && super.stopCellEditing();
      if (!isValid) {
        textField.setBorder(new LineBorder(Color.RED));
      }
      return stopped;
    }

    // Store an Integer instead of the String returned by DefaultCellEditor,
    // an empty cell is represented by 0.
    @Override public Object getCellEditorValue() {
      String text = textField.getText().trim();
      return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
  }

  private static final class SudokuCellRenderer extends DefaultTableCellRenderer {
    private final Border cellBorder = BorderFactory.createMatteBorder(
        0, 0, BORDER_WIDTH1, BORDER_WIDTH1, Color.GRAY);
    private final Border blockCornerBorder = BorderFactory.createMatteBorder(
        0, 0, BORDER_WIDTH2, BORDER_WIDTH2, Color.BLACK);
    private final Border blockBottomBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, BORDER_WIDTH2, 0, Color.BLACK),
        BorderFactory.createMatteBorder(0, 0, 0, BORDER_WIDTH1, Color.GRAY));
    private final Border blockRightBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, BORDER_WIDTH2, Color.BLACK),
        BorderFactory.createMatteBorder(0, 0, BORDER_WIDTH1, 0, Color.GRAY));
    private final Integer[][] puzzle;

    @SuppressWarnings("PMD.UseVarargs")
    private SudokuCellRenderer(Integer[][] src) {
      super();
      Integer[][] dst = new Integer[src.length][src[0].length];
      for (int i = 0; i < src.length; i++) {
        System.arraycopy(src[i], 0, dst[i], 0, src[0].length);
      }
      this.puzzle = dst;
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      boolean isEditable = puzzle[row][column] == 0;
      Component c = super.getTableCellRendererComponent(
          table, value, isEditable && isSelected, hasFocus, row, column);
      c.setFont(isEditable ? c.getFont() : c.getFont().deriveFont(Font.BOLD));
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setHorizontalAlignment(CENTER);
        if (isEditable && Objects.equals(value, 0)) {
          l.setText(" ");
        }
        boolean isBlockBottom = (row + 1) % 3 == 0;
        boolean isBlockRight = (column + 1) % 3 == 0;
        if (isBlockBottom && isBlockRight) {
          l.setBorder(blockCornerBorder);
        } else if (isBlockBottom) {
          l.setBorder(blockBottomBorder);
        } else if (isBlockRight) {
          l.setBorder(blockRightBorder);
        } else {
          l.setBorder(cellBorder);
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
