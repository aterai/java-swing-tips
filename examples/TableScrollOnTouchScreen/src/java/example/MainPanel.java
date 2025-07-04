// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      private transient TableTouchScreenHandler handler;
      @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        getSelectionModel().removeListSelectionListener(handler);
        super.updateUI();
        handler = new TableTouchScreenHandler(this);
        addMouseMotionListener(handler);
        addMouseListener(handler);
        getSelectionModel().addListSelectionListener(handler);
        setRowHeight(30);
      }
    };
    UIManager.put("ScrollBar.width", 30);
    add(new JScrollPane(table));
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

class TableTouchScreenHandler extends MouseAdapter implements ListSelectionListener {
  public static final int VELOCITY = 5;
  public static final int DELAY = 10;
  public static final double GRAVITY = .95;
  private final Cursor dc = Cursor.getDefaultCursor();
  private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Timer scroller;
  private final Point startPt = new Point();
  private final Point delta = new Point();

  protected TableTouchScreenHandler(JTable table) {
    super();
    this.scroller = new Timer(DELAY, e -> scroll(table, e));
  }

  private void scroll(JTable table, ActionEvent e) {
    JViewport viewport = (JViewport) SwingUtilities.getUnwrappedParent(table);
    Rectangle rect = viewport.getViewRect();
    rect.translate(-delta.x, -delta.y);
    table.scrollRectToVisible(rect);
    if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
      delta.setLocation((int) (delta.x * GRAVITY), (int) (delta.y * GRAVITY));
    } else {
      ((Timer) e.getSource()).stop();
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    // System.out.println("mousePressed: " + delta);
    Component c = e.getComponent();
    c.setCursor(hc);
    // c.setEnabled(false);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), p));
      scroller.stop();
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      delta.setLocation(VELOCITY * (cp.x - startPt.x), VELOCITY * (cp.y - startPt.y));
      // Point vp = viewport.getViewPosition();
      // vp.translate(startPt.x - cp.x, startPt.y - cp.y);
      // ((JComponent) c).scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
      Rectangle rect = viewport.getViewRect();
      rect.translate(startPt.x - cp.x, startPt.y - cp.y);
      ((JComponent) c).scrollRectToVisible(rect);
      startPt.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    // System.out.println("mouseReleased: " + delta);
    JTable c = (JTable) e.getComponent();
    c.setCursor(dc);
    // c.setEnabled(true);
    if (c.isEditing()) {
      delta.setLocation(0, 0);
    } else {
      scroller.start();
    }
  }

  @Override public void valueChanged(ListSelectionEvent e) {
    // System.out.println("\n  valueChanged: " + e.getValueIsAdjusting());
    if (scroller.isRunning()) {
      // System.out.println("isRunning");
      delta.setLocation(0, 0);
    }
    scroller.stop();
  }
}
