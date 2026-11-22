// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(createModel()) {
      @Override protected JTableHeader createDefaultTableHeader() {
        return createTableHeader(getColumnModel());
      }

      @Override public void updateUI() {
        super.updateUI();
        // Horizontal lines are painted by the MatteBorder of each cell
        setShowHorizontalLines(false);
        setShowVerticalLines(true);
        setIntercellSpacing(new Dimension(1, 0));
        setGridColor(HierarchicalCellRenderer.LINE_COLOR);
        setRowSelectionAllowed(false);
        setFocusable(false);
        setDefaultRenderer(Object.class, new ValueCellRenderer());
        setDefaultRenderer(RowNode.class, new HierarchicalCellRenderer());
      }
    };
    table.setRowHeight(20);
    table.getTableHeader().setResizingAllowed(false);
    table.getColumnModel().getColumn(0).setPreferredWidth(180);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTableHeader createTableHeader(TableColumnModel cm) {
    ColumnGroup month = new ColumnGroup("2026/06");
    month.add(cm.getColumn(1));
    month.add(cm.getColumn(2));
    ColumnGroup total = new ColumnGroup("2026/04 - 2026/06");
    total.add(cm.getColumn(3));
    total.add(cm.getColumn(4));
    GroupableTableHeader header = new GroupableTableHeader(cm);
    header.addColumnGroup(month);
    header.addColumnGroup(total);
    return header;
  }

  private static TableModel createModel() {
    String[] columnNames = {"", "Result", "YoY", "Result", "YoY"};
    Object[][] data = {
        {new RowNode("Shipments (k units)", 0), 962, 67.7, 2023, 76.9},
        {new RowNode("Desktop PC", 1), 81, 60.4, 204, 65.5},
        {new RowNode("All-in-one", 2), 8, 57.9, 36, 87.3},
        {new RowNode("Standalone", 2), 73, 60.7, 169, 62.3},
        {new RowNode("Notebook PC", 1), 881, 68.4, 1819, 78.5},
        {new RowNode("(Share)", 1, true), 91.6, null, 89.9, null},
        {new RowNode("Mobile notebook", 2), 550, 57.5, 1012, 66.3},
        {new RowNode("Other notebook", 2), 331, 100.1, 807, 102.1},
        {new RowNode("Shipment value (100M yen)", 0), 1203, 92.1, 2672, 95.3},
        {new RowNode("Desktop PC", 1), 138, 83.9, 331, 80.3},
        {new RowNode("All-in-one", 2), 17, 75.7, 69, 97.0},
        {new RowNode("Standalone", 2), 121, 85.2, 262, 76.9},
        {new RowNode("Notebook PC", 1), 1065, 93.2, 2341, 97.8},
        {new RowNode("Mobile notebook", 2), 613, 81.8, 1196, 85.8},
        {new RowNode("Other notebook", 2), 452, 115.2, 1145, 114.5},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 0 ? RowNode.class : Object.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
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

class RowNode {
  private final String title;
  private final int depth;
  private final boolean continuation;

  protected RowNode(String title, int depth) {
    this(title, depth, false);
  }

  /**
   * Creates a row header node.
   *
   * @param title the row title
   * @param depth the hierarchy level, 0 is the top level
   * @param continuation true if this row continues the previous row (no top line)
   */
  protected RowNode(String title, int depth, boolean continuation) {
    this.title = title;
    this.depth = depth;
    this.continuation = continuation;
  }

  public String getTitle() {
    return title;
  }

  public int getDepth() {
    return depth;
  }

  public boolean isContinuation() {
    return continuation;
  }

  @Override public String toString() {
    return title;
  }
}

class HierarchicalCellRenderer extends DefaultTableCellRenderer {
  public static final Color LINE_COLOR = Color.GRAY;
  public static final int INDENT = 16;
  // Tiled by the MatteBorder to paint the vertical lines of the ancestor levels
  private static final Icon GUIDE_ICON = new GuideIcon(INDENT, LINE_COLOR);
  // The x coordinate where the bottom line starts, or -1 if there is no bottom line
  private int bottomLineX = -1;

  @Override public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, false, false, row, column);
    bottomLineX = -1;
    if (value instanceof RowNode) {
      RowNode node = (RowNode) value;
      setText(node.getTitle());
      int depth = node.getDepth();
      Border b = BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 1, 0, 0, LINE_COLOR),
          BorderFactory.createEmptyBorder(0, 4, 1, 4));
      if (depth > 0) {
        Border guide = BorderFactory.createMatteBorder(0, INDENT * depth, 0, 0, GUIDE_ICON);
        b = BorderFactory.createCompoundBorder(guide, b);
      }
      setBorder(b);
      if (hasBottomLine(table, row)) {
        // The bottom line starts at the indent of the next row so that
        // it looks like the top line of the next row
        bottomLineX = INDENT * getNextDepth(table, row);
      }
    }
    return this;
  }

  @Override public void paint(Graphics g) {
    super.paint(g);
    if (bottomLineX >= 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(LINE_COLOR);
      int y = getHeight() - 1;
      g2.drawLine(bottomLineX, y, getWidth() - 1, y);
      g2.dispose();
    }
  }

  private static int getNextDepth(JTable table, int row) {
    int next = row + 1;
    Object o = next < table.getRowCount() ? table.getValueAt(next, 0) : null;
    return o instanceof RowNode ? ((RowNode) o).getDepth() : 0;
  }

  // The line between the header and the first row is painted by the JTableHeader,
  // and each row paints its bottom line unless the next row continues this row
  public static boolean hasBottomLine(JTable table, int row) {
    int next = row + 1;
    if (next >= table.getRowCount()) {
      return true;
    }
    Object o = table.getValueAt(next, 0);
    return !(o instanceof RowNode && ((RowNode) o).isContinuation());
  }
}

class ValueCellRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, false, false, row, column);
    setHorizontalAlignment(RIGHT);
    if (value instanceof Integer) {
      setText(String.format("%,d", value));
    } else if (value instanceof Double) {
      setText(String.format("%.1f%%", value));
    }
    int bottom = HierarchicalCellRenderer.hasBottomLine(table, row) ? 1 : 0;
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, bottom, 0, HierarchicalCellRenderer.LINE_COLOR),
        BorderFactory.createEmptyBorder(0, 4, 1 - bottom, 4)));
    return this;
  }
}

class HeaderCellRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, false, false, row, column);
    setHorizontalAlignment(CENTER);
    Optional.ofNullable(table).map(JTable::getTableHeader).ifPresent(header -> {
      setForeground(header.getForeground());
      setBackground(header.getBackground());
      setFont(header.getFont());
    });
    int left = column == 0 ? 1 : 0;
    // The column cells (column >= 0) paint the bottom line that separates the header
    // from the table body, the group cells (column == -1) leave it to the column cells
    int bottom = column >= 0 ? 1 : 0;
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, left, bottom, 1, HierarchicalCellRenderer.LINE_COLOR),
        BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    return this;
  }
}

class GuideIcon implements Icon {
  private final int width;
  private final Color color;

  protected GuideIcon(int width, Color color) {
    this.width = width;
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(color);
    g2.drawLine(x, y, x, y + getIconHeight() - 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return 1;
  }
}

/**
 * GroupableTableHeader.
 *
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 * @version 1.0 10/20/98
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">GroupableTableHeader</a>
 */
class GroupableTableHeader extends JTableHeader {
  private final List<ColumnGroup> columnGroups = new ArrayList<>();

  protected GroupableTableHeader(TableColumnModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    // Replacing the UI delegate uninstalls the LookAndFeel specific header renderer
    // (Synth: HeaderRenderer, Windows: XPDefaultRenderer), so use a renderer that
    // paints the same cell lines as the table body on every LookAndFeel
    setUI(new GroupableTableHeaderUI());
    setDefaultRenderer(new HeaderCellRenderer());
  }

  // [java] BooleanGetMethodName: Don't report bad method names on @Override #97
  // https://github.com/pmd/pmd/pull/97
  @Override public boolean getReorderingAllowed() {
    return false;
  }

  @Override public void setReorderingAllowed(boolean b) {
    super.setReorderingAllowed(false);
  }

  protected void addColumnGroup(ColumnGroup g) {
    columnGroups.add(g);
  }

  public List<ColumnGroup> getColumnGroups(TableColumn col) {
    return columnGroups
        .stream()
        .map(cg -> cg.getColumnGroupList(col, createMutableList()))
        .filter(groups -> !groups.isEmpty())
        .findFirst()
        .orElse(Collections.emptyList());
    // for (ColumnGroup cg : columnGroups) {
    //   List<ColumnGroup> groups = cg.getColumnGroupList(col, createMutableList());
    //   if (!groups.isEmpty()) {
    //     return groups;
    //   }
    // }
    // return Collections.emptyList();
  }

  private static <E> List<E> createMutableList() {
    return new ArrayList<>();
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
  }

  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
  }
}

/**
 * GroupableTableHeaderUI.
 *
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 * @version 1.0 10/20/98
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">GroupableTableHeaderUI</a>
 */
class GroupableTableHeaderUI extends BasicTableHeaderUI {
  @Override public void paint(Graphics g, JComponent c) {
    Rectangle clip = g.getClipBounds();
    TableColumnModel cm = header.getColumnModel();
    int headerHeight = header.getHeight();
    // Always lay out the cells from the first column so that a group cell starts at
    // the left edge of its first column even if the clip bounds start in the middle
    // of the group, and paint only the cells that intersect the clip bounds
    Map<ColumnGroup, Rectangle> groupRects = new ConcurrentHashMap<>();
    Rectangle cellRect = new Rectangle();
    for (int column = 0; column < cm.getColumnCount(); column++) {
      TableColumn tc = cm.getColumn(column);
      cellRect.setBounds(cellRect.x, 0, tc.getWidth(), headerHeight);
      for (ColumnGroup cg : ((GroupableTableHeader) header).getColumnGroups(tc)) {
        Rectangle groupRect = groupRects.get(cg);
        if (groupRect == null) {
          // The first column of the group: paint the group cell only once
          groupRect = createRect(cellRect.getLocation(), cg.getSize(header));
          groupRects.put(cg, groupRect);
          if (groupRect.intersects(clip)) {
            paintGroupCell(g, groupRect, cg);
          }
        }
        cellRect.y += groupRect.height;
        cellRect.height -= groupRect.height;
      }
      if (cellRect.intersects(clip)) {
        paintColumnCell(g, cellRect, column);
      }
      cellRect.x += cellRect.width;
    }
  }

  private static Rectangle createRect(Point p, Dimension d) {
    return new Rectangle(p, d);
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private Component getHeaderRendererComponent(int columnIndex) {
    TableColumn tc = header.getColumnModel().getColumn(columnIndex);
    TableCellRenderer r = Optional.ofNullable(tc.getHeaderRenderer())
        .orElseGet(header::getDefaultRenderer);
    boolean hasFocus = !header.isPaintingForPrint() && header.hasFocus();
    // && (columnIndex == getSelectedColumnIndex())
    JTable table = header.getTable();
    Object hv = tc.getHeaderValue();
    return r.getTableCellRendererComponent(table, hv, false, hasFocus, -1, columnIndex);
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private void paintColumnCell(Graphics g, Rectangle rect, int columnIndex) {
    Component c = getHeaderRendererComponent(columnIndex);
    rendererPane.paintComponent(g, c, header, rect.x, rect.y, rect.width, rect.height, true);
  }

  private void paintGroupCell(Graphics g, Rectangle rect, ColumnGroup columnGroup) {
    TableCellRenderer r = header.getDefaultRenderer();
    Object o = columnGroup.getHeaderValue();
    JTable table = header.getTable();
    Component c = r.getTableCellRendererComponent(table, o, false, false, -1, -1);
    rendererPane.paintComponent(g, c, header, rect.x, rect.y, rect.width, rect.height, true);
  }

  private int getPreferredHeaderHeight() {
    int height = 0;
    TableColumnModel columnModel = header.getColumnModel();
    for (int column = 0; column < columnModel.getColumnCount(); column++) {
      TableColumn tc = columnModel.getColumn(column);
      Component comp = getHeaderRendererComponent(column);
      int rendererHeight = comp.getPreferredSize().height;
      for (ColumnGroup cg : ((GroupableTableHeader) header).getColumnGroups(tc)) {
        rendererHeight += cg.getSize(header).height;
      }
      height = Math.max(height, rendererHeight);
    }
    return height;
  }

  @Override public Dimension getPreferredSize(JComponent c) {
    long width = Collections.list(header.getColumnModel().getColumns()).stream()
        .mapToLong(TableColumn::getPreferredWidth).sum();
    return new Dimension((int) Math.min(width, Integer.MAX_VALUE), getPreferredHeaderHeight());
  }
}

/**
 * ColumnGroup.
 *
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 * @version 1.0 10/20/98
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">ColumnGroup</a>
 */
class ColumnGroup {
  private final List<Object> children = new ArrayList<>();
  private final String text;

  protected ColumnGroup(String text) {
    this.text = text;
  }

  /**
   * Add TableColumn or ColumnGroup.
   *
   * @param obj TableColumn or ColumnGroup
   */
  public void add(Object obj) {
    Optional.ofNullable(obj).ifPresent(children::add);
  }

  public List<ColumnGroup> getColumnGroupList(TableColumn c, List<ColumnGroup> g) {
    g.add(this);
    return children.contains(c) ? g : children.stream()
        .filter(ColumnGroup.class::isInstance)
        .map(ColumnGroup.class::cast)
        .map(cg -> cg.getColumnGroupList(c, createMutableList(g)))
        .filter(groups -> !groups.isEmpty())
        .findFirst()
        .orElse(Collections.emptyList());
  }

  private static <E> List<E> createMutableList(Collection<? extends E> c) {
    return new ArrayList<>(c);
  }

  public Object getHeaderValue() {
    return text;
  }

  public Dimension getSize(JTableHeader header) {
    TableCellRenderer r = header.getDefaultRenderer();
    JTable table = header.getTable();
    Component c = r.getTableCellRendererComponent(
        table, getHeaderValue(), false, false, -1, -1);
    int width = 0;
    for (Object obj : children) {
      if (obj instanceof TableColumn) {
        TableColumn tc = (TableColumn) obj;
        width += tc.getWidth();
      } else {
        width += ((ColumnGroup) obj).getSize(header).width;
      }
    }
    return new Dimension(width, c.getPreferredSize().height);
  }
}
