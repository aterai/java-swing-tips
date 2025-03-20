// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new AutoWrapTable(makeModel());
    JScrollPane scroll = new JScrollPane(table) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Default", "AutoWrap"};
    Object[][] data = {
        {"123456789012345678901234567890", "123456789012345678901234567890"},
        {"1111", "22222222222222222222222222222222222222222222222222222222"},
        {"3333333", "----------------------------------------------0"},
        {"4444444444444444444", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>|"},
    };
    return new DefaultTableModel(data, columnNames);
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

class AutoWrapTable extends JTable {
  private static final int AUTO_WRAP_COLUMN = 1;
  private final Color evenColor = new Color(0xE6_F0_FF);

  protected AutoWrapTable(TableModel model) {
    super(model);
  }

  @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
    Component c = super.prepareRenderer(tcr, row, column);
    if (isRowSelected(row)) {
      c.setForeground(getSelectionForeground());
      c.setBackground(getSelectionBackground());
    } else {
      c.setForeground(getForeground());
      c.setBackground(row % 2 == 0 ? evenColor : getBackground());
    }
    return c;
  }

  @Override public void updateUI() {
    getColumnModel().getColumn(AUTO_WRAP_COLUMN).setCellRenderer(null);
    super.updateUI();
    setEnabled(false);
    setShowGrid(false);
    getColumnModel().getColumn(AUTO_WRAP_COLUMN).setCellRenderer(new TextAreaCellRenderer());
  }
}

// class AutoWrapTable extends JTable {
//   private static final int AUTO_WRAP_COLUMN = 1;
//
//   protected AutoWrapTable(TableModel model) {
//     super(model);
//   }
//
//   @Override public void updateUI() {
//     getColumnModel().getColumn(AUTO_WRAP_COLUMN).setCellRenderer(null);
//     super.updateUI();
//     setEnabled(false);
//     setShowGrid(false);
//     getColumnModel().getColumn(AUTO_WRAP_COLUMN).setCellRenderer(new TextAreaCellRenderer());
//   }
//
//   @Override public void doLayout() {
//     initPreferredHeight();
//     super.doLayout();
//   }
//
//   @Override public void columnMarginChanged(ChangeEvent e) {
//     super.columnMarginChanged(e);
//     initPreferredHeight();
//   }
//
//   private void initPreferredHeight() {
//     for (int row = 0; row < getRowCount(); row++) {
//       int maximum_height = 0;
//       for (int col = 0; col < getColumnModel().getColumnCount(); col++) {
//         Component c = prepareRenderer(getCellRenderer(row, col), row, col);
//         if (c instanceof JTextArea) {
//           JTextArea a = (JTextArea) c;
//           int h = getPreferredHeight(a);
//           maximum_height = Math.max(maximum_height, h);
//         }
//       }
//       setRowHeight(row, maximum_height);
//     }
//   }
//
//   // https://tips4java.wordpress.com/2008/10/26/text-utilities/
//   private int getPreferredHeight(JTextComponent c) {
//     Insets insets = c.getInsets();
//     // Insets margin = c.getMargin();
//     // System.out.println(insets);
//     View view = c.getUI().getRootView(c).getView(0);
//     float f = view.getPreferredSpan(View.Y_AXIS);
//     // System.out.println(f);
//     int preferredHeight = (int) f;
//     return preferredHeight + insets.top + insets.bottom;
//   }
// }

// delegation pattern
class TextAreaCellRenderer implements TableCellRenderer {
  // public static class UIResource extends TextAreaCellRenderer implements UIResource {}
  private final JTextArea renderer = new JTextArea();
  private final List<List<Integer>> rowAndCellHeights = new ArrayList<>();

  protected TextAreaCellRenderer() {
    super();
    renderer.setLineWrap(true);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    // renderer.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
    // renderer.setMargin(new Insets(2, 2, 2, 2));
    // renderer.setBorder(BorderFactory.createEmptyBorder());
    renderer.setName("Table.cellRenderer");
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.setFont(table.getFont());
    renderer.setText(Objects.toString(value, ""));
    adjustRowHeight(table, row, column);
    return renderer;
  }

  // Calculate the new preferred height for a given row, and sets the height on the table.
  // http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
  private void adjustRowHeight(JTable table, int row, int column) {
    // The trick for this to work properly is to set the width of the column to the
    // text area. The reason for this is that getPreferredSize(), without a width tries
    // to place all the text in one line. By setting the size with the width of the column,
    // getPreferredSize() returns the proper height which the row should have in
    // order to make room for the text.
    // int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
    // int cWidth = table.getCellRect(row, column, false).width; // Ignore IntercellSpacing
    // renderer.setSize(new Dimension(cWidth, 1000));
    renderer.setBounds(table.getCellRect(row, column, false));
    // renderer.doLayout();

    int preferredHeight = renderer.getPreferredSize().height;
    while (rowAndCellHeights.size() <= row) {
      rowAndCellHeights.add(createMutableList(column));
    }
    List<Integer> list = rowAndCellHeights.get(row);
    while (list.size() <= column) {
      list.add(0);
    }
    list.set(column, preferredHeight);
    int max = list.stream().max(Integer::compare).orElse(0);
    if (table.getRowHeight(row) != max) {
      table.setRowHeight(row, max);
    }
  }

  private static <E> List<E> createMutableList(int initialCapacity) {
    return new ArrayList<>(initialCapacity);
  }
}

// // inheritance to extend a class.
// class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
//   private final List<List<Integer>> rowAndCellHeights = new ArrayList<>();
//
//   // public static class UIResource extends TextAreaCellRenderer implements UIResource {}
//
//   @Override public void updateUI() {
//     super.updateUI();
//     setLineWrap(true);
//     setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//     // setBorder(BorderFactory.createLineBorder(Color.RED, 2));
//     // setMargin(new Insets(2, 2, 2, 2));
//     // setBorder(BorderFactory.createEmptyBorder());
//     setName("Table.cellRenderer");
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     setFont(table.getFont());
//     setText(Objects.toString(value, ""));
//     adjustRowHeight(table, row, column);
//     return this;
//   }
//
//   /**
//    * Calculate the new preferred height for a given row, and sets the height on the table.
//    * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
//    */
//   private void adjustRowHeight(JTable table, int row, int column) {
//     // The trick for this to work properly is to set the width of the column to the
//     // text area. The reason for this is that getPreferredSize(), without a width tries
//     // to place all the text in one line. By setting the size with the width of the column,
//     // getPreferredSize() returns the proper height which the row should have in
//     // order to make room for the text.
//     // int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
//     // int cWidth = table.getCellRect(row, column, false).width; // Ignore IntercellSpacing
//     // setSize(new Dimension(cWidth, 1000));
//
//     setBounds(table.getCellRect(row, column, false));
//     // doLayout();
//
//     int preferredHeight = getPreferredSize().height;
//     while (rowAndCellHeights.size() <= row) {
//       rowAndCellHeights.add(new ArrayList<>(column));
//     }
//     List<Integer> list = rowAndCellHeights.get(row);
//     while (list.size() <= column) {
//       list.add(0);
//     }
//     list.set(column, preferredHeight);
//     int max = list.stream().max(Integer::compare).orElse(0);
//     if (table.getRowHeight(row) != max) {
//       table.setRowHeight(row, max);
//     }
//   }
//
//   // Overridden for performance reasons. ---->
//   @Override public boolean isOpaque() {
//     Color back = getBackground();
//     Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
//     if (o instanceof JTable) {
//       JTable t = (JTable) o;
//       boolean colorMatch = back != null && back.equals(t.getBackground()) && t.isOpaque();
//       return !colorMatch && super.isOpaque();
//     } else {
//       return super.isOpaque();
//     }
//   }
//
//   @Override protected void firePropertyChange(String property, Object ov, Object nv) {
//     if ("document".equals(property)) {
//       super.firePropertyChange(property, ov, nv);
//     } else if (("font".equals(property) || "foreground".equals(property)) && ov != nv) {
//       super.firePropertyChange(property, ov, nv);
//     }
//   }
//
//   @Override public void firePropertyChange(String propertyName, boolean ov, boolean nv) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(long tm, int x, int y, int width, int height) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(Rectangle r) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void invalidate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void validate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void revalidate() {
//     /* Overridden for performance reasons. */
//   }
//   // <---- Overridden for performance reasons.
// }
