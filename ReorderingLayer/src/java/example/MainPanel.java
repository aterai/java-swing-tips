// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createMatteBorder(10, 5, 5, 5, Color.GREEN));
    Stream.of(
        new JLabel("<html>000<br>00<br>00"), new JButton("1"),
        new JCheckBox("2"), new JTextField("3")).forEach(c -> addDraggablePanel(box, c));
    add(new JLayer<>(box, new ReorderingLayerUI<>()), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addDraggablePanel(Container parent, Component c) {
    int idx = parent.getComponentCount();
    JLabel l = new JLabel(String.format(" %04d ", idx));
    l.setOpaque(true);
    l.setBackground(Color.RED);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createLineBorder(Color.BLUE, 2)));
    p.add(l, BorderLayout.WEST);
    p.add(c);
    p.setOpaque(false);
    parent.add(p);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ReorderingLayerUI<V extends JComponent> extends LayerUI<V> {
  private static final Rectangle TOP_HALF_RECT = new Rectangle();
  private static final Rectangle BOTTOM_HALF_RECT = new Rectangle();
  private static final Rectangle INNER_RECT = new Rectangle();
  private static final Rectangle PREV_RECT = new Rectangle();
  private static final Rectangle DRAGGING_RECT = new Rectangle();
  private final Point startPt = new Point();
  private final Point dragOffset = new Point();
  private final Container canvas = new JPanel();
  private final int dragThreshold = DragSource.getDragThreshold();

  private Component draggingComponent;
  private Component fillerComponent;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && Objects.nonNull(draggingComponent)) {
      SwingUtilities.paintComponent(g, draggingComponent, canvas, DRAGGING_RECT);
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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
    JComponent parent = l.getView();
    switch (e.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        if (parent.getComponentCount() > 0) {
          startPt.setLocation(e.getPoint());
          l.repaint();
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (Objects.nonNull(draggingComponent)) {
          // swap the dragging panel and the dummy filler
          int idx = parent.getComponentZOrder(fillerComponent);
          replaceComponents(parent, fillerComponent, draggingComponent, idx);
          draggingComponent = null;
        }
        break;
      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    if (e.getID() != MouseEvent.MOUSE_DRAGGED) {
      return;
    }
    JComponent parent = l.getView();
    Point pt = e.getPoint();
    if (Objects.isNull(draggingComponent)) {
      // MotionThreshold
      if (startPt.distance(pt) > dragThreshold) {
        startDragging(parent, pt);
      }
      return;
    }

    // update the filler panel location
    if (!PREV_RECT.contains(pt)) {
      updateFillerLocation(parent, fillerComponent, pt);
    }

    // update the dragging panel location
    updateDraggingPanelLocation(parent, pt, dragOffset);
    parent.repaint();
  }

  private void startDragging(JComponent parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    int index = parent.getComponentZOrder(c);
    if (Objects.equals(c, parent) || index < 0) {
      return;
    }
    draggingComponent = c;

    Rectangle r = draggingComponent.getBounds();
    DRAGGING_RECT.setBounds(r); // save draggingComponent size
    dragOffset.setLocation(pt.x - r.x, pt.y - r.y);

    fillerComponent = Box.createRigidArea(r.getSize());
    replaceComponents(parent, c, fillerComponent, index);

    updateDraggingPanelLocation(parent, pt, dragOffset);
  }

  private static void updateDraggingPanelLocation(JComponent parent, Point pt, Point dragOffset) {
    Insets i = parent.getInsets();
    Rectangle r = SwingUtilities.calculateInnerArea(parent, INNER_RECT);
    int x = r.x;
    int y = pt.y - dragOffset.y;
    int h = DRAGGING_RECT.height;
    int yy;
    if (y < i.top) {
      yy = i.top;
    } else {
      yy = r.contains(x, y + h) ? y : r.height + i.top - h;
    }
    DRAGGING_RECT.setLocation(x, yy);
  }

  private static void updateFillerLocation(Container parent, Component filler, Point pt) {
    // change the dummy filler location
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      Rectangle r = c.getBounds();
      if (Objects.equals(c, filler) && r.contains(pt)) {
        return;
      }
      int tgt = getTargetIndex(r, pt, i);
      if (tgt >= 0) {
        replaceComponents(parent, filler, filler, tgt);
        return;
      }
    }
  }

  private static int getTargetIndex(Rectangle r, Point pt, int i) {
    int ht2 = Math.round(r.height / 2f);
    TOP_HALF_RECT.setBounds(r.x, r.y, r.width, ht2);
    BOTTOM_HALF_RECT.setBounds(r.x, r.y + ht2, r.width, ht2);
    if (TOP_HALF_RECT.contains(pt)) {
      PREV_RECT.setBounds(TOP_HALF_RECT);
      return i > 1 ? i : 0;
    } else if (BOTTOM_HALF_RECT.contains(pt)) {
      PREV_RECT.setBounds(BOTTOM_HALF_RECT);
      return i;
    }
    return -1;
  }

  private static void replaceComponents(Container p, Component remove, Component add, int idx) {
    p.remove(remove);
    p.add(add, idx);
    p.revalidate();
    p.repaint();
  }
}
