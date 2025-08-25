// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table1 = makeTable1();
    JTable table2 = makeTable2();
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("HeaderRenderer", new JScrollPane(table1));
    tabs.addTab("JLayer", new JLayer<>(new JScrollPane(table2), new SortingLayerUI()));
    add(tabs);
    List<JTable> l = Arrays.asList(table1, table2);
    JButton btn = new JButton("clear SortKeys");
    btn.addActionListener(e -> l.forEach(t -> t.getRowSorter().setSortKeys(null)));
    add(btn, BorderLayout.SOUTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable1() {
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumnModel cm = getColumnModel();
        TableCellRenderer r = new ColumnHeaderRenderer();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          cm.getColumn(i).setHeaderRenderer(r);
        }
      }
    };
    table.setAutoCreateRowSorter(true);
    return table;
  }

  private static JTable makeTable2() {
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    return table;
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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

class ColumnHeaderRenderer implements TableCellRenderer {
  private static final Color SORTING_BGC = new Color(0xA4_CF_EF);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    boolean b = isSortingColumn(table, column) || isSelected;
    Component c = r.getTableCellRendererComponent(table, value, b, hasFocus, row, column);
    if (b) {
      c.setBackground(SORTING_BGC);
    }
    return c;
  }

  private static boolean isSortingColumn(JTable table, int column) {
    return Optional.ofNullable(table.getRowSorter()).map(RowSorter::getSortKeys)
        .filter(keys -> !keys.isEmpty())
        .map(keys -> keys.get(0).getColumn())
        .map(i -> column == table.convertColumnIndexToView(i)).orElse(false);
  }
}

class SortingLayerUI extends LayerUI<JScrollPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      Rectangle r = getTable(layer)
          .map(table -> getSortingColumnBounds(layer, table))
          .orElseGet(Rectangle::new);
      if (!r.isEmpty()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(0x64_FE_AE_FF, true));
        g2.fill(r);
        g2.dispose();
      }
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseEvent(e, l);
    Component c = e.getComponent();
    if (c instanceof JTableHeader && e.getID() == MouseEvent.MOUSE_CLICKED) {
      c.repaint();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (c instanceof JTableHeader && e.getID() == MouseEvent.MOUSE_DRAGGED) {
      c.repaint();
    }
  }

  private static Optional<JTable> getTable(JLayer<?> layer) {
    return Optional.ofNullable(layer.getView())
        .filter(JScrollPane.class::isInstance)
        .map(JScrollPane.class::cast)
        .map(JScrollPane::getViewport)
        .map(JViewport::getView)
        .filter(JTable.class::isInstance)
        .map(JTable.class::cast);
  }

  private static int getSortingColumnIndex(JTable table) {
    return Optional.ofNullable(table.getRowSorter())
        .map(RowSorter::getSortKeys)
        .filter(keys -> !keys.isEmpty())
        .map(keys -> keys.get(0).getColumn())
        .map(table::convertColumnIndexToView)
        .orElse(-1);
  }

  private static Rectangle getSortingColumnBounds(JLayer<?> layer, JTable table) {
    Rectangle rect = new Rectangle();
    int sortingColumn = getSortingColumnIndex(table);
    if (sortingColumn >= 0) {
      Rectangle r = getSortingRect(table, sortingColumn);
      int h = r.height / 6;
      r.y += r.height - h;
      r.height = h;
      rect.setRect(SwingUtilities.convertRectangle(table.getTableHeader(), r, layer));
    }
    return rect;
  }

  private static Rectangle getSortingRect(JTable table, int sortingColumn) {
    JTableHeader header = table.getTableHeader();
    Rectangle r = header.getHeaderRect(sortingColumn);
    TableColumn draggedColumn = header.getDraggedColumn();
    if (draggedColumn != null) {
      int modelIndex = draggedColumn.getModelIndex();
      int viewIndex = table.convertColumnIndexToView(modelIndex);
      if (viewIndex == sortingColumn) {
        r.x += header.getDraggedDistance();
      }
    }
    return r;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
