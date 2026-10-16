// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TableModel model = makeModel();
    JTable table = new JTable(model);
    table.putClientProperty("JTable.autoStartsEdit", false);
    table.setAutoCreateRowSorter(true);
    table.addKeyListener(new TableNextMatchKeyHandler());
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"aaa", 15, true},
        {"bbb", 6, false}, {"abc", 92, true}, {"Bbb", 0, false},
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

// @see javax/swing/plaf/basic/BasicListUI.Handler
// @see javax/swing/plaf/basic/BasicTreeUI.Handler
class TableNextMatchKeyHandler extends KeyAdapter {
  // model column index of the search target, independent of column reordering
  private static final int TARGET_COLUMN = 0;
  private static final long TIME_FACTOR = 500L;
  private String prefix = "";
  private String typedString = "";
  private long lastTime;

  private static boolean isNavigationKey(KeyEvent e) {
    JTable table = (JTable) e.getComponent();
    InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
    return Objects.nonNull(im) && Objects.nonNull(im.get(key));
  }

  @Override public void keyPressed(KeyEvent e) {
    if (isNavigationKey(e)) {
      prefix = "";
      typedString = "";
      lastTime = 0L;
    }
  }

  @Override public void keyTyped(KeyEvent e) {
    JTable table = (JTable) e.getComponent();
    if (table.getRowCount() > 0 && isPrefixKey(e)) {
      boolean backward = e.isShiftDown();
      int startRow = updatePrefix(e.getKeyChar(), e.getWhen(), table.getSelectedRow(), backward);
      selectNextMatch(table, startRow, backward);
    }
  }

  private static boolean isPrefixKey(KeyEvent e) {
    boolean modified = e.isAltDown() || e.isControlDown() || e.isMetaDown();
    // Skip control characters such as Enter, Tab, Backspace or Escape
    return !modified && !Character.isISOControl(e.getKeyChar()) && !isNavigationKey(e);
  }

  private int updatePrefix(char c, long time, int selectedRow, boolean backward) {
    int startRow = selectedRow;
    if (time - lastTime < TIME_FACTOR) {
      typedString += c;
      if (prefix.length() == 1 && c == prefix.charAt(0)) {
        // Subsequent same key presses move the keyboard focus to the next
        // object that starts with the same letter.
        startRow += backward ? -1 : 1;
      } else {
        prefix = typedString;
      }
    } else {
      startRow += backward ? -1 : 1;
      typedString = String.valueOf(c);
      prefix = typedString;
    }
    lastTime = time;
    return startRow;
  }

  private void selectNextMatch(JTable table, int startRow, boolean backward) {
    int rowCount = table.getRowCount();
    int start = startRow;
    if (start < 0 || start >= rowCount) {
      start = backward ? rowCount - 1 : 0;
    }
    Position.Bias bias = backward ? Position.Bias.Backward : Position.Bias.Forward;
    // getNextMatch(...) searches all rows cyclically, so no extra wrap search is needed
    int row = getNextMatch(table, prefix, start, bias);
    if (row >= 0) {
      int column = Math.max(0, table.convertColumnIndexToView(TARGET_COLUMN));
      // JTable#changeSelection(...) also scrolls the cell to visible if autoscrolls is true
      table.changeSelection(row, column, false, false);
    }
  }

  // @see javax/swing/JList#getNextMatch(String prefix, int startIndex, Position.Bias bias)
  // @see javax/swing/JTree#getNextMatch(String prefix, int startIndex, Position.Bias bias)
  public static int getNextMatch(JTable table, String prefix, int startRow, Position.Bias bias) {
    Objects.requireNonNull(prefix, "Must supply non-null prefix");
    int rowCount = table.getRowCount();
    if (startRow < 0 || startRow >= rowCount) {
      throw new IllegalArgumentException("(0 <= startRow < rowCount) is false");
    }
    String upperPrefix = prefix.toUpperCase(Locale.ENGLISH);
    TableModel model = table.getModel();
    // start search from the next/previous element from the
    // selected element
    int increment = Position.Bias.Forward.equals(bias) ? 1 : -1;
    return IntStream.iterate(startRow, row -> (row + increment + rowCount) % rowCount)
        .limit(rowCount)
        .filter(row -> {
          // the row index is a view index, convert it for the sorted table
          Object value = model.getValueAt(table.convertRowIndexToModel(row), TARGET_COLUMN);
          String text = Objects.toString(value, "");
          return text.toUpperCase(Locale.ENGLISH).startsWith(upperPrefix);
        })
        .findFirst()
        .orElse(-1);
  }
}
