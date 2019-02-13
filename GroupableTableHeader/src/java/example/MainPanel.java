// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    // http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html
    String[] columnNames = {"SNo.", "1", "2", "Native", "2", "3"};
    Object[][] data = {
      {"119", "foo", "bar", "ja", "ko", "zh"},
      {"911", "bar", "foo", "en", "fr", "pt"}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames);
    JTable table = new JTable(model) {
      @Override protected JTableHeader createDefaultTableHeader() {
        TableColumnModel cm = getColumnModel();
        ColumnGroup gname = new ColumnGroup("Name");
        gname.add(cm.getColumn(1));
        gname.add(cm.getColumn(2));

        ColumnGroup glang = new ColumnGroup("Language");
        glang.add(cm.getColumn(3));

        ColumnGroup gother = new ColumnGroup("Others");
        gother.add(cm.getColumn(4));
        gother.add(cm.getColumn(5));

        glang.add(gother);

        GroupableTableHeader header = new GroupableTableHeader(cm);
        header.addColumnGroup(gname);
        header.addColumnGroup(glang);
        return header;
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
    //   ex.printStackTrace();
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
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">GroupableTableHeader</a>
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
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

  public List<?> getColumnGroups(TableColumn col) {
    for (ColumnGroup cg: columnGroups) {
      List<?> groups = cg.getColumnGroupList(col, new ArrayList<>());
      if (!groups.isEmpty()) {
        return groups;
      }
    }
    return Collections.emptyList();
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
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">GroupableTableHeaderUI</a>
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 */
class GroupableTableHeaderUI extends BasicTableHeaderUI {
  @Override public void paint(Graphics g, JComponent c) {
    Rectangle clip = g.getClipBounds();
    Point left = clip.getLocation();
    Point right = new Point(clip.x + clip.width - 1, clip.y);
    TableColumnModel cm = header.getColumnModel();
    int colMin = header.columnAtPoint(left);
    int colMax = header.columnAtPoint(right);

    Rectangle cellRect = header.getHeaderRect(colMin);
    int headerY = cellRect.y;
    int headerHeight = cellRect.height;

    Map<ColumnGroup, Rectangle> h = new ConcurrentHashMap<>();
    // int columnMargin = header.getColumnModel().getColumnMargin();
    // int columnWidth;
    for (int column = colMin; column <= colMax; column++) {
      TableColumn tc = cm.getColumn(column);
      cellRect.y = headerY;
      cellRect.setSize(tc.getWidth(), headerHeight);

      int groupHeight = 0;
      for (Object o: ((GroupableTableHeader) header).getColumnGroups(tc)) {
        ColumnGroup cg = (ColumnGroup) o;
        Rectangle groupRect = Optional.ofNullable(h.get(cg))
            .orElseGet(() -> {
              Rectangle r = new Rectangle(cellRect.getLocation(), cg.getSize(header));
              h.put(cg, r);
              return r;
            });

        paintCellGroup(g, groupRect, cg);
        groupHeight += groupRect.height;
        cellRect.height = headerHeight - groupHeight;
        cellRect.y = groupHeight;
      }
      paintCell(g, cellRect, column);
      cellRect.x += cellRect.width;
    }
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private Component getHeaderRenderer(int columnIndex) {
    TableColumn tc = header.getColumnModel().getColumn(columnIndex);
    TableCellRenderer r = Optional.ofNullable(tc.getHeaderRenderer()).orElseGet(() -> header.getDefaultRenderer());
    boolean hasFocus = !header.isPaintingForPrint() && header.hasFocus();
    // && (columnIndex == getSelectedColumnIndex())
    return r.getTableCellRendererComponent(header.getTable(), tc.getHeaderValue(), false, hasFocus, -1, columnIndex);
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
    Component component = getHeaderRenderer(columnIndex);
    rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
  }

  private void paintCellGroup(Graphics g, Rectangle cellRect, ColumnGroup columnGroup) {
    TableCellRenderer r = header.getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(header.getTable(), columnGroup.getHeaderValue(), false, false, -1, -1);
    rendererPane.paintComponent(g, c, header, cellRect.x, cellRect.y, cellRect.width, cellRect.height, true);
  }

  private int getHeaderHeight() {
    int height = 0;
    TableColumnModel columnModel = header.getColumnModel();
    for (int column = 0; column < columnModel.getColumnCount(); column++) {
      TableColumn tc = columnModel.getColumn(column);
      Component comp = getHeaderRenderer(column);
      int rendererHeight = comp.getPreferredSize().height;
      for (Object o: ((GroupableTableHeader) header).getColumnGroups(tc)) {
        ColumnGroup cg = (ColumnGroup) o;
        rendererHeight += cg.getSize(header).height;
      }
      height = Math.max(height, rendererHeight);
    }
    return height;
  }

  // Copied from javax/swing/plaf/basic/BasicTableHeaderUI.java
  private Dimension createHeaderSize(long width) {
    long w = Math.min(width, Integer.MAX_VALUE);
    return new Dimension((int) w, getHeaderHeight());
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
 * @see <a href="http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html">ColumnGroup</a>
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 * @author aterai aterai@outlook.com
 */
class ColumnGroup {
  protected final List<Object> list = new ArrayList<>();
  protected final String text;

  protected ColumnGroup(String text) {
    this.text = text;
  }

  /**
   * Add TableColumn or ColumnGroup.
   * @param obj TableColumn or ColumnGroup
   */
  public void add(Object obj) {
    Optional.ofNullable(obj).ifPresent(list::add);
  }

  public List<?> getColumnGroupList(TableColumn c, List<Object> g) {
    g.add(this);
    if (list.contains(c)) {
      return g;
    }
    for (Object obj: list) {
      if (obj instanceof ColumnGroup) {
        List<?> groups = ((ColumnGroup) obj).getColumnGroupList(c, new ArrayList<>(g));
        if (!groups.isEmpty()) {
          return groups;
        }
      }
    }
    return Collections.emptyList();
  }

  public Object getHeaderValue() {
    return text;
  }

  public Dimension getSize(JTableHeader header) {
    TableCellRenderer r = header.getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(header.getTable(), getHeaderValue(), false, false, -1, -1);
    int width = 0;
    for (Object obj: list) {
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
