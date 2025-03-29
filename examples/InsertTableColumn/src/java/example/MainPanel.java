// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // Java 8:
    // Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
    //   at WindowsTableHeaderUI$XPDefaultRenderer.paint(WindowsTableHeaderUI)
    // [JDK-8039383] NPE when changing Windows Theme
    // https://bugs.openjdk.org/browse/JDK-8039383
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
      @Override protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          @Override public void updateUI() {
            super.updateUI();
            EventQueue.invokeLater(() -> {
              TableCellRenderer renderer = getDefaultRenderer();
              setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
                Component c = renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                  JLabel l = (JLabel) c;
                  l.setText(convertToColumnTitle(column + 1));
                  l.setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
              });
            });
          }
        };
      }

      @Override public void updateUI() {
        super.updateUI();
        setAutoCreateColumnsFromModel(false);
        setAutoResizeMode(AUTO_RESIZE_OFF);
      }
    };
    // System.out.println(convertToColumnTitle(16_384)); // -> XFD
    table.setModel(new DefaultTableModel(5, 16_384));
    table.setValueAt("0-0", 0, 0);
    table.setValueAt("0-1", 0, 1);
    table.setValueAt("0-2", 0, 2);
    return table;
  }

  private static String convertToColumnTitle(int columnNumber) {
    assert columnNumber > 0 : "Input is not valid!";
    StringBuilder sb = new StringBuilder();
    int num = columnNumber;
    while (num > 0) {
      int mod = (num - 1) % 26;
      int code = 'A' + mod;
      sb.insert(0, (char) code);
      // Java 11: sb.insert(0, Character.toString(code));
      num = (num - mod) / 26;
    }
    return sb.toString();
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

class ColumnInsertLayerUI extends LayerUI<JScrollPane> {
  private static final Color LINE_COLOR = new Color(0x00_78_D7);
  private static final int LINE_WIDTH = 4;
  private final Rectangle2D line = new Rectangle2D.Double();
  private final Ellipse2D plus = new Ellipse2D.Double(0d, 0d, 10d, 10d);

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && !line.isEmpty()) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      JTableHeader header = ((JTable) scroll.getViewport().getView()).getTableHeader();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Point pt0 = line.getBounds().getLocation();
      Point pt1 = SwingUtilities.convertPoint(header, pt0, c);
      g2.translate(pt1.getX() - pt0.getX(), pt1.getY() - pt0.getY());
      // paint Insert Line
      g2.setPaint(LINE_COLOR);
      g2.fill(line);
      // paint Plus Icon
      g2.setPaint(Color.WHITE);
      g2.fill(plus);
      g2.setPaint(LINE_COLOR);
      double cx = plus.getCenterX();
      double cy = plus.getCenterY();
      double w2 = plus.getWidth() / 2d;
      double h2 = plus.getHeight() / 2d;
      g2.draw(new Line2D.Double(cx - w2, cy, cx + w2, cy));
      g2.draw(new Line2D.Double(cx, cy - h2, cx, cy + h2));
      g2.draw(plus);
      g2.dispose();
    }
  }

  private void updateLineLocation(JScrollPane scroll, Point loc) {
    JTable table = (JTable) scroll.getViewport().getView();
    JTableHeader header = table.getTableHeader();
    Rectangle rect = scroll.getVisibleRect();
    JScrollBar bar = scroll.getHorizontalScrollBar();
    int scrollHeight = bar.isVisible() ? bar.getHeight() : 0;
    Dimension d = new Dimension(LINE_WIDTH, rect.height - scrollHeight);
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (canInsert(header, loc, i, d)) {
        return;
      }
    }
  }

  private boolean canInsert(JTableHeader header, Point loc, int i, Dimension d) {
    Rectangle r = header.getHeaderRect(i);
    Rectangle r1 = getWestRect(r, i);
    Rectangle r2 = getEastRect(r);
    boolean hit = false;
    if (r1.contains(loc)) {
      updateInsertLineLocation(r1, loc, d, header);
      hit = true;
    } else if (r2.contains(loc)) {
      updateInsertLineLocation(r2, loc, d, header);
      hit = true;
    } else if (r.contains(loc)) {
      line.setFrame(0d, 0d, 0d, 0d);
      header.setCursor(Cursor.getDefaultCursor());
      hit = true;
    }
    return hit;
  }

  private Rectangle getWestRect(Rectangle r, int i) {
    Rectangle rect = r.getBounds();
    Rectangle bounds = plus.getBounds();
    if (i != 0) {
      rect.x -= bounds.width / 2;
    }
    rect.setSize(bounds.getSize());
    return rect;
  }

  private Rectangle getEastRect(Rectangle r) {
    Rectangle rect = r.getBounds();
    Rectangle bounds = plus.getBounds();
    rect.x += rect.width - bounds.width / 2;
    rect.setSize(bounds.getSize());
    return rect;
  }

  private void updateInsertLineLocation(Rectangle r, Point loc, Dimension d, Component c) {
    if (r.contains(loc)) {
      double cx = r.getCenterX();
      double cy = r.getCenterY();
      line.setFrame(cx - d.getWidth() / 2d, r.getY(), d.getWidth(), d.getHeight());
      double pw = plus.getWidth() / 2d;
      double ph = plus.getHeight() / 2d;
      plus.setFrameFromCenter(cx, cy, cx - pw, cy - ph);
      c.setCursor(Cursor.getDefaultCursor());
    } else {
      line.setFrame(0d, 0d, 0d, 0d);
      c.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
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
    if (e.getID() == MouseEvent.MOUSE_CLICKED) {
      JScrollPane scroll = l.getView();
      Point pt = e.getPoint();
      if (plus.contains(pt) && !line.isEmpty()) {
        JTable table = (JTable) scroll.getViewport().getView();
        TableModel model = table.getModel();
        int columnCount = table.getColumnCount();
        int maxColumn = model.getColumnCount();
        if (columnCount < maxColumn) {
          int idx = table.columnAtPoint(line.getBounds().getLocation());
          TableColumn column = new TableColumn(columnCount);
          column.setHeaderValue("Column" + columnCount);
          table.addColumn(column);
          table.moveColumn(columnCount, idx + 1);
          updateLineLocation(scroll, pt);
        }
      }
      l.repaint(scroll.getBounds());
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseMotionEvent(e, l);
    Component c = e.getComponent();
    int id = e.getID();
    JScrollPane scroll = l.getView();
    if (id == MouseEvent.MOUSE_MOVED && c instanceof JTableHeader) {
      updateLineLocation(scroll, e.getPoint());
    } else {
      line.setFrame(0d, 0d, 0d, 0d);
    }
    l.repaint(scroll.getBounds());
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
        ex.printStackTrace();
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
