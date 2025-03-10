// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(4, 3);
    // table.setAutoCreateRowSorter(true);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setColumnHeader(new JViewport() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 24;
        return d;
      }
    });
    add(new JLayer<>(scroll, new ColumnDragLayerUI()));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
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

class ColumnDragLayerUI extends LayerUI<JScrollPane> {
  private final Rectangle draggableRect = new Rectangle();

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
    if (!draggableRect.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      // g2.fill(draggableRect);
      Icon icon = new DragAreaIcon();
      int x = (int) (draggableRect.getCenterX() - icon.getIconWidth() / 2d);
      int y = draggableRect.y + 1;
      icon.paintIcon(c, g2, x, y);
      g2.dispose();
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseEvent(e, l);
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      if (e.getID() == MouseEvent.MOUSE_PRESSED) {
        Point pt = e.getPoint();
        updateIconAndCursor(header, pt, l);
      } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
        header.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        draggableRect.setSize(0, 0);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
        TableColumn draggedColumn = header.getDraggedColumn();
        if (!draggableRect.isEmpty() && draggedColumn != null) {
          EventQueue.invokeLater(() -> {
            int modelIndex = draggedColumn.getModelIndex();
            int viewIndex = header.getTable().convertColumnIndexToView(modelIndex);
            Rectangle rect = header.getHeaderRect(viewIndex);
            rect.x += header.getDraggedDistance();
            draggableRect.setRect(SwingUtilities.convertRectangle(header, rect, l));
            header.repaint(rect);
          });
        } else {
          e.consume(); // Refuse to start drag
        }
      } else if (e.getID() == MouseEvent.MOUSE_MOVED) {
        Point pt = e.getPoint();
        updateIconAndCursor(header, pt, l);
        header.repaint();
      }
    } else {
      c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      draggableRect.setSize(0, 0);
    }
  }

  private void updateIconAndCursor(JTableHeader header, Point pt, JLayer<?> l) {
    Rectangle r = header.getHeaderRect(header.columnAtPoint(pt));
    r.height /= 2;
    if (r.contains(pt)) {
      header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      draggableRect.setRect(SwingUtilities.convertRectangle(header, r, l));
    } else {
      header.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      draggableRect.setSize(0, 0);
    }
  }
}

class DragAreaIcon implements Icon {
  private static final Color SQUARE_COLOR = new Color(0x64_64_64_64, true);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    int count = 4;
    int diff = 3;
    int firstRow = 1;
    int secondRow = firstRow + diff;
    int firstColumn = (getIconWidth() - diff * count) / 2;
    for (int i = 0; i < count; i++) {
      int column = firstColumn + i * count;
      drawSquare(g2, column, firstRow);
      drawSquare(g2, column, secondRow);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 12;
  }

  private void drawSquare(Graphics g, int x, int y) {
    g.setColor(SQUARE_COLOR);
    g.fillRect(x, y, 2, 2);
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
