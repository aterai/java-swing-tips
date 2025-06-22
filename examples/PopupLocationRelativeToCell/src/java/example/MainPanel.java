// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("JTable", makeTablePanel());
    tabs.addTab("JTree", makeTreePanel());
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeTablePanel() {
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(new JScrollPane(initTable(new JTable(5, 5))));
    p.add(new JScrollPane(initTable(new PopupLocationTable(5, 5))));
    return p;
  }

  private static JTable initTable(JTable table) {
    table.setCellSelectionEnabled(true);
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    return table;
  }

  private static JPanel makeTreePanel() {
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(new JScrollPane(initTree(new JTree())));
    p.add(new JScrollPane(initTree(new PopupLocationTree())));
    return p;
  }

  private static JTree initTree(JTree tree) {
    tree.setEditable(true);
    JPopupMenu popup = new JPopupMenu();
    popup.add("clearSelection()").addActionListener(e -> tree.clearSelection());
    popup.addSeparator();
    popup.add("JMenuItem1");
    popup.add("JMenuItem2");
    tree.setComponentPopupMenu(popup);
    return tree;
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

class PopupLocationTable extends JTable {
  private static final List<Integer> IGNORE_KEYS = Arrays.asList(
      KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3,
      KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6,
      KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9,
      KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12,
      KeyEvent.VK_F13, KeyEvent.VK_F14, KeyEvent.VK_F15,
      KeyEvent.VK_F16, KeyEvent.VK_F17, KeyEvent.VK_F18,
      KeyEvent.VK_F19, KeyEvent.VK_F20, KeyEvent.VK_F21,
      KeyEvent.VK_F22, KeyEvent.VK_F23, KeyEvent.VK_CONTEXT_MENU);

  protected PopupLocationTable(int numRows, int numColumns) {
    super(numRows, numColumns);
  }

  @Override public Point getPopupLocation(MouseEvent e) {
    Rectangle r = getLeadSelectionCellRect();
    boolean b = e == null && !r.isEmpty();
    return b ? getKeyPopupLocation(r) : super.getPopupLocation(e);
  }

  @Override public boolean editCellAt(int row, int column, EventObject e) {
    return !isIgnoreKeys(e) && super.editCellAt(row, column, e);
  }

  private Point getKeyPopupLocation(Rectangle r) {
    double px = getCellSelectionEnabled() ? r.getMaxX() : getBounds().getCenterX();
    return new Point((int) px, (int) r.getMaxY());
  }

  private Rectangle getLeadSelectionCellRect() {
    int row = getSelectionModel().getLeadSelectionIndex();
    int col = getColumnModel().getSelectionModel().getLeadSelectionIndex();
    return getCellRect(row, col, false);
  }

  private static boolean isIgnoreKeys(EventObject e) {
    return e instanceof KeyEvent && IGNORE_KEYS.contains(((KeyEvent) e).getKeyCode());
  }
}

final class TablePopupMenu extends JPopupMenu {
  /* default */ TablePopupMenu() {
    super();
    JMenuItem check = new JCheckBoxMenuItem("setCellSelectionEnabled", true);
    add(check).addActionListener(e -> {
      boolean b = ((JCheckBoxMenuItem) e.getSource()).isSelected();
      JTable table = (JTable) getInvoker();
      table.setCellSelectionEnabled(b);
      table.setRowSelectionAllowed(true);
    });
    // add("clearSelection").addActionListener(e -> {
    //   JTable table = (JTable) getInvoker();
    //   table.clearSelection();
    // });
    add("clearSelectionAndLeadAnchor").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      clearSelectionAndLeadAnchor(table);
    });
  }

  private static void clearSelectionAndLeadAnchor(JTable table) {
    ListSelectionModel selectionModel = table.getSelectionModel();
    ListSelectionModel colSelectionModel = table.getColumnModel().getSelectionModel();
    selectionModel.setValueIsAdjusting(true);
    colSelectionModel.setValueIsAdjusting(true);
    table.clearSelection();
    selectionModel.setAnchorSelectionIndex(-1);
    selectionModel.setLeadSelectionIndex(-1);
    colSelectionModel.setAnchorSelectionIndex(-1);
    colSelectionModel.setLeadSelectionIndex(-1);
    selectionModel.setValueIsAdjusting(false);
    colSelectionModel.setValueIsAdjusting(false);
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      super.show(c, x, y);
    }
  }
}

class PopupLocationTree extends JTree {
  @Override public Point getPopupLocation(MouseEvent e) {
    Rectangle r = getRowBounds(getLeadSelectionRow());
    boolean b = e == null && r != null;
    return b ? getKeyPopupLocation(r) : super.getPopupLocation(e);
  }

  private Point getKeyPopupLocation(Rectangle r) {
    return new Point((int) r.getMinX(), (int) r.getMaxY());
  }
}
