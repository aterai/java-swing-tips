// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(10, 5);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(makePopupMenu());

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Default", new JScrollPane(table));
    tabbedPane.addTab("MouseListener", new JScrollPane(makeTable1()));
    tabbedPane.addTab("PopupMenuListener", new JScrollPane(makeTable2()));
    JTable table3 = new JTable(10, 5);
    table3.setFillsViewportHeight(true);
    table3.setComponentPopupMenu(makePopupMenu());
    JScrollPane scroll = new JScrollPane(table3);
    tabbedPane.addTab("JLayer", new JLayer<>(scroll, new RightMouseButtonLayerUI()));

    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable1() {
    JTable table = new JTable(10, 5);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(makePopupMenu());
    table.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        Component c = e.getComponent();
        if (c instanceof JTable && SwingUtilities.isRightMouseButton(e)) {
          JTable table = (JTable) c;
          if (table.isEditing()) {
            table.removeEditor();
            // table.editingCanceled(null);
            // table.editingStopped(null);
            // table.getCellEditor().cancelCellEditing();
            // table.getCellEditor().stopCellEditing();
          }
          Point pt = e.getPoint();
          Rectangle r = TableUtils.getCellArea(table);
          if (r.contains(pt)) {
            int currentRow = table.rowAtPoint(pt);
            int currentColumn = table.columnAtPoint(pt);
            if (TableUtils.noneMatch(table.getSelectedRows(), currentRow)) {
              table.changeSelection(currentRow, currentColumn, false, false);
            }
          } else {
            table.clearSelection();
          }
        }
      }
    });
    return table;
  }

  private static JTable makeTable2() {
    JTable table = new JTable(10, 5);
    table.setFillsViewportHeight(true);
    JPopupMenu popup = makePopupMenu();
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (table.isEditing()) {
          table.removeEditor();
        }
        SwingUtilities.invokeLater(() -> {
          Point pt = SwingUtilities.convertPoint(popup, new Point(), table);
          Rectangle r = TableUtils.getCellArea(table);
          if (r.contains(pt)) {
            int currentRow = table.rowAtPoint(pt);
            int currentColumn = table.columnAtPoint(pt);
            if (TableUtils.noneMatch(table.getSelectedRows(), currentRow)) {
              table.changeSelection(currentRow, currentColumn, false, false);
            }
          } else {
            table.clearSelection();
          }
        });
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }
    });
    table.setComponentPopupMenu(popup);
    return table;
  }

  private static JPopupMenu makePopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("clearSelection").addActionListener(e -> {
      Component c = popup.getInvoker();
      if (c instanceof JTable) {
        ((JTable) c).clearSelection();
      }
    });
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    return popup;
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

class RightMouseButtonLayerUI extends LayerUI<JScrollPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (c instanceof JTable && SwingUtilities.isRightMouseButton(e)) {
      JTable table = (JTable) c;
      if (table.isEditing()) {
        table.removeEditor();
      }
      Point pt = e.getPoint();
      Rectangle r = TableUtils.getCellArea(table);
      if (r.contains(pt)) {
        int currentRow = table.rowAtPoint(pt);
        int currentColumn = table.columnAtPoint(pt);
        if (TableUtils.noneMatch(table.getSelectedRows(), currentRow)) {
          table.changeSelection(currentRow, currentColumn, false, false);
        }
      } else {
        table.clearSelection();
      }
    } else {
      super.processMouseEvent(e, l);
    }
  }
}

final class TableUtils {
  private TableUtils() {
    /* Singleton */
  }

  public static Rectangle getCellArea(JTable table) {
    Rectangle start = table.getCellRect(0, 0, true);
    int rc = table.getRowCount();
    int cc = table.getColumnCount();
    Rectangle end = table.getCellRect(rc - 1, cc - 1, true);
    return start.union(end);
  }

  public static boolean noneMatch(int[] selectedRows, int currentRow) {
    return IntStream.of(selectedRows).noneMatch(i -> i == currentRow);
    // for (int i : selectedRows) {
    //   if (i == currentRow) {
    //     return false;
    //   }
    // }
    // return true;
  }
}
