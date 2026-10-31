// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Default", new JScrollPane(createTable(createPopupMenu())));
    tabbedPane.addTab("MouseListener", new JScrollPane(createMouseListenerTable()));
    tabbedPane.addTab("PopupMenuListener", new JScrollPane(createPopupMenuListenerTable()));
    JScrollPane scroll = new JScrollPane(createTable(createPopupMenu()));
    tabbedPane.addTab("JLayer", new JLayer<>(scroll, new RightMouseButtonLayerUI()));
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable createTable(JPopupMenu popup) {
    JTable table = new JTable(10, 5);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(popup);
    return table;
  }

  private static JTable createMouseListenerTable() {
    JTable table = createTable(createPopupMenu());
    table.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        Component c = e.getComponent();
        if (c instanceof JTable && SwingUtilities.isRightMouseButton(e)) {
          TableUtils.updateSelection((JTable) c, e.getPoint());
        }
      }
    });
    return table;
  }

  private static JTable createPopupMenuListenerTable() {
    JPopupMenu popup = createPopupMenu();
    JTable table = createTable(popup);
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // The location of the JPopupMenu is not yet determined at this point
        SwingUtilities.invokeLater(() -> {
          Point pt = SwingUtilities.convertPoint(popup, new Point(), table);
          TableUtils.updateSelection(table, pt);
        });
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }
    });
    return table;
  }

  private static JPopupMenu createPopupMenu() {
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
    // Update the selection only once on MOUSE_PRESSED, not on MOUSE_RELEASED or MOUSE_CLICKED
    boolean pressed = e.getID() == MouseEvent.MOUSE_PRESSED;
    if (pressed && c instanceof JTable && SwingUtilities.isRightMouseButton(e)) {
      TableUtils.updateSelection((JTable) c, e.getPoint());
    }
  }
}

final class TableUtils {
  private TableUtils() {
    /* Singleton */
  }

  public static void updateSelection(JTable table, Point pt) {
    if (table.isEditing()) {
      table.removeEditor();
    }
    int row = table.rowAtPoint(pt);
    int column = table.columnAtPoint(pt);
    // rowAtPoint(...) or columnAtPoint(...) returns -1 outside the cell area
    if (row < 0 || column < 0) {
      table.clearSelection();
    } else if (!table.isRowSelected(row)) {
      table.changeSelection(row, column, false, false);
    }
  }
}
