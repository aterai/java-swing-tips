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
import javax.swing.*;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override protected JTableHeader createDefaultTableHeader() {
        TableColumnModel cm = getColumnModel();
        ColumnGroup name = new ColumnGroup("Name");
        name.add(cm.getColumn(1));
        name.add(cm.getColumn(2));
        ColumnGroup lang = new ColumnGroup("Language");
        lang.add(cm.getColumn(3));
        ColumnGroup other = new ColumnGroup("Others");
        other.add(cm.getColumn(4));
        other.add(cm.getColumn(5));
        lang.add(other);
        GroupableTableHeader header = new GroupableTableHeader(cm);
        header.addColumnGroup(name);
        header.addColumnGroup(lang);
        return header;
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"SNo.", "1", "2", "Native", "2", "3"};
    Object[][] data = {
        {"119", "foo", "bar", "ja", "ko", "zh"},
        {"911", "bar", "foo", "en", "fr", "pt"},
    };
    return new DefaultTableModel(data, columnNames);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (UnsupportedLookAndFeelException ignored) {
    //   Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //   ex.printStackTrace();
    //   return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
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
    setUI(new GroupableTableHeaderUI());
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
    TableColumnModel cm = header.getColumnModel();
    if (cm.getColumnCount() <= 0) {
      return;
    }
    Rectangle clip = g.getClipBounds();
    Point left = clip.getLocation();
    Point right = new Point(clip.x + clip.width - 1, clip.y);
    // columnAtPoint(...) returns -1 if the point is outside the columns
    // (e.g. AUTO_RESIZE_OFF and the header is wider than the total column width)
    int colMin = Math.max(header.columnAtPoint(left), 0);
    int colMax = header.columnAtPoint(right);
    if (colMax < 0) {
      colMax = cm.getColumnCount() - 1;
    }

    Rectangle cellRect = header.getHeaderRect(colMin);
    int headerY = cellRect.y;
    int headerHeight = cellRect.height;

    Map<ColumnGroup, Rectangle> groupRects = new ConcurrentHashMap<>();
    for (int column = colMin; column <= colMax; column++) {
      TableColumn tc = cm.getColumn(column);
      cellRect.y = headerY;
      cellRect.setSize(tc.getWidth(), headerHeight);

      int groupHeight = 0;
      for (ColumnGroup cg : getColumnGroups(tc)) {
        int y = cellRect.y;
        // The clip area may start in the middle of a group,
        // so the group rectangle must start from its leftmost column
        Rectangle groupRect = groupRects.computeIfAbsent(cg, k -> createGroupRect(k, y));
        paintCellGroup(g, groupRect, cg);
        groupHeight += groupRect.height;
        cellRect.height = headerHeight - groupHeight;
        cellRect.y = headerY + groupHeight;
      }
      paintColumnCell(g, cellRect, column);
      cellRect.x += cellRect.width;
    }
  }

  private List<ColumnGroup> getColumnGroups(TableColumn tc) {
    return ((GroupableTableHeader) header).getColumnGroups(tc);
  }

  private Rectangle createGroupRect(ColumnGroup cg, int y) {
    Dimension d = cg.getSize(header);
    return new Rectangle(getGroupX(cg), y, d.width, d.height);
  }

  private int getGroupX(ColumnGroup cg) {
    TableColumnModel cm = header.getColumnModel();
    int x = 0;
    for (int column = 0; column < cm.getColumnCount(); column++) {
      TableColumn tc = cm.getColumn(column);
      if (getColumnGroups(tc).contains(cg)) {
        break;
      }
      x += tc.getWidth();
    }
    return x;
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private Component getColumnRendererComponent(int columnIndex) {
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
    Component c = getColumnRendererComponent(columnIndex);
    rendererPane.paintComponent(g, c, header, rect.x, rect.y, rect.width, rect.height, true);
  }

  private void paintCellGroup(Graphics g, Rectangle rect, ColumnGroup columnGroup) {
    TableCellRenderer r = header.getDefaultRenderer();
    Object o = columnGroup.getHeaderValue();
    JTable table = header.getTable();
    Component c = r.getTableCellRendererComponent(table, o, false, false, -1, -1);
    rendererPane.paintComponent(g, c, header, rect.x, rect.y, rect.width, rect.height, true);
  }

  private int calcHeaderHeight() {
    int height = 0;
    TableColumnModel columnModel = header.getColumnModel();
    for (int column = 0; column < columnModel.getColumnCount(); column++) {
      TableColumn tc = columnModel.getColumn(column);
      Component comp = getColumnRendererComponent(column);
      int rendererHeight = comp.getPreferredSize().height;
      for (ColumnGroup cg : getColumnGroups(tc)) {
        rendererHeight += cg.getSize(header).height;
      }
      height = Math.max(height, rendererHeight);
    }
    return height;
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private Dimension createHeaderSize(long width) {
    long w = Math.min(width, Integer.MAX_VALUE);
    return new Dimension((int) w, calcHeaderHeight());
  }

  @Override public Dimension getPreferredSize(JComponent c) {
    long width = Collections.list(header.getColumnModel().getColumns()).stream()
        .mapToLong(TableColumn::getPreferredWidth).sum();
    // long width = 0;
    // Enumeration<TableColumn> enumeration = header.getColumnModel().getColumns();
    // while (enumeration.hasMoreElements()) {
    //   TableColumn tc = (TableColumn) enumeration.nextElement();
    //   width += tc.getPreferredWidth();
    // }
    return createHeaderSize(width);
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
