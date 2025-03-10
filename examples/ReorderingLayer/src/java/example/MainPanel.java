// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.IntStream;
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

class ReorderingLayerUI<V extends JComponent> extends LayerUI<V> {
  private static final Rectangle TOP_HALF_RECT = new Rectangle();
  private static final Rectangle BOTTOM_HALF_RECT = new Rectangle();
  private static final Rectangle TEMP_RECT = new Rectangle();
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

  @SuppressWarnings("PMD.NullAssignment")
  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
    JComponent parent = l.getView();
    Component c = e.getComponent();
    switch (e.getID()) {
      case MouseEvent.MOUSE_PRESSED:
        if (parent.getComponentCount() > 0 && c instanceof JLayer<?>) {
          startPt.setLocation(e.getPoint());
          l.repaint();
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (Objects.nonNull(draggingComponent)) {
          // swap the dragging panel and the temporary filler
          int idx = parent.getComponentZOrder(fillerComponent);
          swapComponent(parent, fillerComponent, draggingComponent, idx);
          draggingComponent = null;
        }
        break;
      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    Component c = e.getComponent();
    if (e.getID() == MouseEvent.MOUSE_DRAGGED && c instanceof JLayer<?>) {
      mouseDragged(l, e.getPoint());
    }
  }

  private void mouseDragged(JLayer<? extends V> l, Point pt) {
    JComponent p = l.getView();
    if (Objects.isNull(draggingComponent)) {
      // MotionThreshold
      if (startPt.distance(pt) > dragThreshold) {
        startDragging(p, pt);
      }
      return;
    }

    if (!PREV_RECT.contains(pt)) {
      // update the filler panel location
      // updateFillerLocation(p, fillerComponent, pt);
      IntStream.range(0, p.getComponentCount())
          .filter(i -> {
            Component tc = p.getComponent(i);
            return !Objects.equals(tc, fillerComponent) || !tc.getBounds().contains(pt);
          })
          .map(i -> getTargetIndex(p.getComponent(i), pt, i))
          .filter(i -> i >= 0)
          .findFirst()
          .ifPresent(i -> swapComponent(p, fillerComponent, fillerComponent, i));
    }

    // update the dragging panel location
    updateDraggingPanelLocation(p, pt, dragOffset);
    p.repaint();
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
    swapComponent(parent, c, fillerComponent, index);

    updateDraggingPanelLocation(parent, pt, dragOffset);
  }

  private static void updateDraggingPanelLocation(JComponent p, Point pt, Point offset) {
    int y = pt.y - offset.y;
    Rectangle r = SwingUtilities.calculateInnerArea(p, TEMP_RECT);
    int bottom = r.y + r.height - DRAGGING_RECT.height;
    DRAGGING_RECT.setLocation(r.x, Math.min(Math.max(y, r.y), bottom));
  }

  // private static void updateFillerLocation(Container p, Component filler, Point pt) {
  //   IntStream.range(0, p.getComponentCount())
  //       .filter(i -> {
  //         Component c = p.getComponent(i);
  //         return !Objects.equals(c, filler) || !c.getBounds().contains(pt);
  //       })
  //       .map(i -> getTargetIndex(p.getComponent(i), pt, i))
  //       .filter(tgt -> tgt >= 0)
  //       .findFirst()
  //       .ifPresent(tgt -> swapComponent(p, filler, filler, tgt));
  // }

  // private static void updateFillerLocation(Container parent, Component filler, Point pt) {
  //   // change the temporary filler location
  //   for (int i = 0; i < parent.getComponentCount(); i++) {
  //     Component c = parent.getComponent(i);
  //     Rectangle r = c.getBounds();
  //     if (Objects.equals(c, filler) && r.contains(pt)) {
  //       return;
  //     }
  //     int tgt = getTargetIndex(r, pt, i);
  //     if (tgt >= 0) {
  //       swapComponent(parent, filler, filler, tgt);
  //       return;
  //     }
  //   }
  // }

  private static int getTargetIndex(Component c, Point pt, int i) {
    Rectangle r = c.getBounds();
    int ht2 = Math.round(r.height / 2f);
    TOP_HALF_RECT.setBounds(r.x, r.y, r.width, ht2);
    BOTTOM_HALF_RECT.setBounds(r.x, r.y + ht2, r.width, ht2);
    int idx = -1;
    if (TOP_HALF_RECT.contains(pt)) {
      PREV_RECT.setBounds(TOP_HALF_RECT);
      idx = i > 1 ? i : 0;
    } else if (BOTTOM_HALF_RECT.contains(pt)) {
      PREV_RECT.setBounds(BOTTOM_HALF_RECT);
      idx = i;
    }
    return idx;
  }

  private static void swapComponent(Container p, Component remove, Component add, int i) {
    p.remove(remove);
    if (i >= 0 && add != null) {
      p.add(add, i);
    }
    p.revalidate();
    p.repaint();
  }
}
