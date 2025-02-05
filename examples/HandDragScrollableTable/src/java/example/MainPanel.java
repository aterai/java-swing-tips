// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      private transient MouseAdapter handler;
      @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        super.updateUI();
        handler = new DragScrollingListener(this);
        addMouseMotionListener(handler);
        addMouseListener(handler);
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    JScrollPane scroll = new JScrollPane(table);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    IntStream.range(0, 1000)
        .mapToObj(i -> new Object[] {"Java Swing", i, i % 2 == 0})
        .forEach(model::addRow);
    return model;
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

class DragScrollingListener extends MouseAdapter {
  public static final int VELOCITY = 5;
  public static final int DELAY = 10;
  public static final double GRAVITY = .95;
  private final Cursor dc = Cursor.getDefaultCursor();
  private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Timer scrollTimer;
  private final Point startPt = new Point();
  private final Point delta = new Point();

  protected DragScrollingListener(JComponent c) {
    super();
    this.scrollTimer = new Timer(DELAY, e -> {
      JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(c);
      Point vp = viewport.getViewPosition();
      vp.translate(-delta.x, -delta.y);
      c.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
      if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
        delta.setLocation((int) (delta.x * GRAVITY), (int) (delta.y * GRAVITY));
      } else {
        ((Timer) e.getSource()).stop();
      }
    });
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hc);
    c.setEnabled(false);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), p));
      scrollTimer.stop();
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      Point vp = viewport.getViewPosition();
      int dx = cp.x - startPt.x;
      int dy = cp.y - startPt.y;
      vp.translate(-dx, -dy);
      delta.setLocation(dx * VELOCITY, dy * VELOCITY);
      Rectangle rect = viewport.getBounds();
      rect.setLocation(vp);
      ((JComponent) c).scrollRectToVisible(rect);
      startPt.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(dc);
    c.setEnabled(true);
    scrollTimer.start();
  }
}
