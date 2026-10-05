// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane scroll = new JScrollPane(makeTable());
    add(new JLayer<>(scroll, new ColumnInsertLayerUI()));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    JTable table = new JTable(5, 3) {
      @Override public void updateUI() {
        super.updateUI();
        setAutoCreateColumnsFromModel(false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
      }

      @Override public void columnAdded(TableColumnModelEvent e) {
        super.columnAdded(e);
        updateHeaderValues(getColumnModel());
      }

      @Override public void columnMoved(TableColumnModelEvent e) {
        super.columnMoved(e);
        if (e.getFromIndex() != e.getToIndex()) {
          updateHeaderValues(getColumnModel());
        }
      }
    };
    // System.out.println(ColumnTitles.toColumnTitle(16_384)); // -> XFD
    table.setModel(new DefaultTableModel(5, 16_384));
    table.setValueAt("0-0", 0, 0);
    table.setValueAt("0-1", 0, 1);
    table.setValueAt("0-2", 0, 2);
    return table;
  }

  // Name the columns in view order (A, B, ..., Z, AA, ...)
  private static void updateHeaderValues(TableColumnModel columnModel) {
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      columnModel.getColumn(i).setHeaderValue(ColumnTitles.toColumnTitle(i + 1));
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

final class ColumnTitles {
  private static final int RADIX = 26;

  private ColumnTitles() {
    /* Singleton */
  }

  // Bijective base-26: 1 -> A, 26 -> Z, 27 -> AA, 16384 -> XFD
  public static String toColumnTitle(int columnNumber) {
    if (columnNumber <= 0) {
      throw new IllegalArgumentException("columnNumber must be positive: " + columnNumber);
    }
    StringBuilder sb = new StringBuilder();
    for (int n = columnNumber; n > 0; n = (n - 1) / RADIX) {
      sb.append((char) ('A' + (n - 1) % RADIX));
    }
    return sb.reverse().toString();
  }
}

class ColumnInsertLayerUI extends LayerUI<JScrollPane> {
  private static final Color LINE_COLOR = new Color(0x00_78_D7);
  private static final int LINE_WIDTH = 4;
  private static final int PLUS_SIZE = 10;
  private final Rectangle2D line = new Rectangle2D.Double();
  private final Ellipse2D plus = new Ellipse2D.Double();
  private int insertIndex = -1;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (insertIndex >= 0 && c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // Do not paint over the scroll bars
      Rectangle clip = scroll.getViewport().getBounds();
      JViewport columnHeader = scroll.getColumnHeader();
      if (columnHeader != null) {
        clip.add(columnHeader.getBounds());
      }
      g2.clip(SwingUtilities.convertRectangle(scroll, clip, c));
      // line and plus are in the JTableHeader coordinate system
      JTableHeader header = getTable(scroll).getTableHeader();
      Point pt = SwingUtilities.convertPoint(header, 0, 0, c);
      g2.translate(pt.x, pt.y);
      // paint Insert Line
      g2.setPaint(LINE_COLOR);
      g2.fill(line);
      // paint Plus Icon
      g2.setPaint(Color.WHITE);
      g2.fill(plus);
      g2.setPaint(LINE_COLOR);
      double cx = plus.getCenterX();
      double cy = plus.getCenterY();
      double r = plus.getWidth() / 2d;
      g2.draw(new Line2D.Double(cx - r, cy, cx + r, cy));
      g2.draw(new Line2D.Double(cx, cy - r, cx, cy + r));
      g2.draw(plus);
      g2.dispose();
    }
  }

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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseEvent(e, l);
    Component c = e.getComponent();
    int id = e.getID();
    if (id == MouseEvent.MOUSE_CLICKED && c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      Point pt = e.getPoint();
      if (insertIndex >= 0 && plus.contains(pt)) {
        insertColumn(header.getTable(), insertIndex);
        updateInsertLocation(l.getView(), header, pt);
        l.repaint();
      }
    } else if (id == MouseEvent.MOUSE_EXITED && c instanceof JTableHeader) {
      clearInsertLocation(l);
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseMotionEvent(e, l);
    Component c = e.getComponent();
    if (e.getID() == MouseEvent.MOUSE_MOVED && c instanceof JTableHeader) {
      updateInsertLocation(l.getView(), (JTableHeader) c, e.getPoint());
      l.repaint();
    } else {
      clearInsertLocation(l);
    }
  }

  private void clearInsertLocation(JLayer<? extends JScrollPane> l) {
    if (insertIndex >= 0) {
      insertIndex = -1;
      l.repaint();
    }
  }

  private void updateInsertLocation(JScrollPane scroll, JTableHeader header, Point pt) {
    insertIndex = getInsertIndex(header, pt);
    if (insertIndex >= 0) {
      int x = getBoundaryX(header, insertIndex);
      int height = header.getHeight() + scroll.getViewport().getHeight();
      line.setFrame(Math.max(0, x - LINE_WIDTH / 2), 0d, LINE_WIDTH, height);
      double cx = Math.max(x, PLUS_SIZE / 2d);
      double cy = header.getHeight() / 2d;
      plus.setFrame(cx - PLUS_SIZE / 2d, cy - PLUS_SIZE / 2d, PLUS_SIZE, PLUS_SIZE);
    }
  }

  // Returns the view index at which a new column is inserted, or -1 if the
  // point is not near a column boundary
  private static int getInsertIndex(JTableHeader header, Point pt) {
    int column = header.columnAtPoint(pt);
    int index = -1;
    if (column >= 0) {
      Rectangle r = header.getHeaderRect(column);
      // The left edge of the first column has no column on its left side,
      // so the whole hit area is placed inside the first column
      int west = column == 0 ? PLUS_SIZE : PLUS_SIZE / 2;
      if (pt.x < r.x + west) {
        index = column;
      } else if (pt.x >= r.x + r.width - PLUS_SIZE / 2) {
        index = column + 1;
      }
    }
    return index;
  }

  private static int getBoundaryX(JTableHeader header, int index) {
    int x;
    if (index == 0) {
      x = header.getHeaderRect(0).x;
    } else {
      Rectangle r = header.getHeaderRect(index - 1);
      x = r.x + r.width;
    }
    return x;
  }

  // JTable and TableColumnModel have no method to insert a TableColumn at
  // the specified position, so add it to the end and then move it
  private static void insertColumn(JTable table, int index) {
    int viewCount = table.getColumnCount();
    if (viewCount < table.getModel().getColumnCount()) {
      table.addColumn(new TableColumn(viewCount));
      table.moveColumn(viewCount, index);
    }
  }

  private static JTable getTable(JScrollPane scroll) {
    return (JTable) scroll.getViewport().getView();
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
