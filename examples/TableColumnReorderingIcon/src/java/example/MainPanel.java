// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
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

class ColumnDragLayerUI extends LayerUI<JScrollPane> {
  private final Rectangle draggableRect = new Rectangle();
  private final Icon dragAreaIcon = new DragAreaIcon();

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
      int iw = dragAreaIcon.getIconWidth();
      int x = draggableRect.x + (draggableRect.width - iw) / 2;
      int y = draggableRect.y + 1;
      dragAreaIcon.paintIcon(c, g, x, y);
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseEvent(e, l);
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      int id = e.getID();
      if (id == MouseEvent.MOUSE_PRESSED) {
        updateIconAndCursor(header, e.getPoint(), l);
      } else if (id == MouseEvent.MOUSE_RELEASED) {
        header.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        clearDraggableRect(header);
      } else if (id == MouseEvent.MOUSE_EXITED && !isMouseButtonDown(e)) {
        // Hide the drag handle icon when the cursor leaves the header,
        // but keep it while dragging a column outside the header
        clearDraggableRect(header);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
        mouseDragged(e, l, header);
      } else if (e.getID() == MouseEvent.MOUSE_MOVED) {
        updateIconAndCursor(header, e.getPoint(), l);
        header.repaint();
      }
    }
  }

  private void mouseDragged(MouseEvent e, JLayer<? extends JScrollPane> l, JTableHeader header) {
    TableColumn draggedColumn = header.getDraggedColumn();
    if (!draggableRect.isEmpty() && draggedColumn != null) {
      // The dragged distance is updated by BasicTableHeaderUI after this
      // event is processed, so read it later on the EDT
      EventQueue.invokeLater(() -> {
        // Using columnAtPoint(...) would make the rectangle jump at the moment
        // the columns are swapped, so convert the model index of the dragged column
        int modelIndex = draggedColumn.getModelIndex();
        int viewIndex = header.getTable().convertColumnIndexToView(modelIndex);
        Rectangle rect = header.getHeaderRect(viewIndex);
        rect.x += header.getDraggedDistance();
        draggableRect.setBounds(SwingUtilities.convertRectangle(header, rect, l));
        header.repaint(rect);
      });
    } else {
      e.consume(); // Refuse to start drag
    }
  }

  private void updateIconAndCursor(JTableHeader header, Point pt, JLayer<?> l) {
    Rectangle r = header.getHeaderRect(header.columnAtPoint(pt));
    r.height /= 2;
    if (r.contains(pt)) {
      header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      draggableRect.setBounds(SwingUtilities.convertRectangle(header, r, l));
    } else {
      header.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      draggableRect.setSize(0, 0);
    }
  }

  private void clearDraggableRect(JTableHeader header) {
    if (!draggableRect.isEmpty()) {
      draggableRect.setSize(0, 0);
      header.repaint();
    }
  }

  private static boolean isMouseButtonDown(MouseEvent e) {
    int mask = InputEvent.BUTTON1_DOWN_MASK
        | InputEvent.BUTTON2_DOWN_MASK
        | InputEvent.BUTTON3_DOWN_MASK;
    return (e.getModifiersEx() & mask) != 0;
  }
}

class DragAreaIcon implements Icon {
  private static final Color SQUARE_COLOR = new Color(0x64_64_64_64, true);
  private static final int SQUARE_SIZE = 2;
  private static final int COLUMN_COUNT = 4;
  private static final int COLUMN_STEP = 4;
  private static final int ROW_STEP = 3;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(SQUARE_COLOR);
    // Center the 2 x 4 grid of squares horizontally
    int gridWidth = COLUMN_STEP * (COLUMN_COUNT - 1) + SQUARE_SIZE;
    int firstColumn = (getIconWidth() - gridWidth) / 2;
    int firstRow = 1;
    int secondRow = firstRow + ROW_STEP;
    for (int i = 0; i < COLUMN_COUNT; i++) {
      int column = firstColumn + i * COLUMN_STEP;
      g2.fillRect(column, firstRow, SQUARE_SIZE, SQUARE_SIZE);
      g2.fillRect(column, secondRow, SQUARE_SIZE, SQUARE_SIZE);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 12;
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
