// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = new ScrollableBox(BoxLayout.Y_AXIS);
    Color borderColor = UIManager.getColor("controlShadow");
    box.setBorder(BorderFactory.createMatteBorder(10, 5, 5, 5, borderColor));
    Stream.of(
        new JLabel("<html>000<br>00<br>00"), new JButton("1"),
        new JCheckBox("2"), new JTextField("3"),
        new JComboBox<>(new String[] {"4", "44", "444"}), new JSpinner(),
        new JSlider(), new JProgressBar(0, 100)).forEach(c -> addDraggablePanel(box, c));
    add(new JScrollPane(new JLayer<>(box, new ReorderingLayerUI<>())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addDraggablePanel(Container parent, Component c) {
    JLabel l = new JLabel(new DragHandleIcon());
    l.setOpaque(true);
    l.setBackground(UIManager.getColor("controlHighlight"));
    l.setForeground(UIManager.getColor("controlDkShadow"));
    l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createLineBorder(UIManager.getColor("controlShadow"))));
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

// A Box that fits the width of the JViewport
// JLayer delegates its Scrollable methods to the view
class ScrollableBox extends Box implements Scrollable {
  private static final int UNIT_INCREMENT = 16;

  protected ScrollableBox(int axis) {
    super(axis);
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return UNIT_INCREMENT;
  }

  @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
  }

  @Override public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return false;
  }
}

// A grip icon made of two columns of dots
class DragHandleIcon implements Icon {
  private static final int DOT = 2;
  private static final int GAP = 2;
  private static final int COLS = 2;
  private static final int ROWS = 3;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(c.getForeground());
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        g2.fillRect(col * (DOT + GAP), row * (DOT + GAP), DOT, DOT);
      }
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return COLS * DOT + (COLS - 1) * GAP;
  }

  @Override public int getIconHeight() {
    return ROWS * DOT + (ROWS - 1) * GAP;
  }
}

class ReorderingLayerUI<V extends JComponent> extends LayerUI<V> {
  private final Rectangle innerRect = new Rectangle();
  private final Rectangle prevRect = new Rectangle();
  private final Rectangle draggingRect = new Rectangle();
  private final Point startPt = new Point();
  private final Point dragOffset = new Point();
  private final Container canvas = new JPanel();
  private final int dragThreshold = DragSource.getDragThreshold();

  private boolean isDragging;
  private Component draggingComponent;
  private Component fillerComponent;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && isDragging) {
      SwingUtilities.paintComponent(g, draggingComponent, canvas, draggingRect);
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
        // ignore the events of the child components such as JButton
        if (e.getComponent() instanceof JLayer<?>) {
          startPt.setLocation(e.getPoint());
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (isDragging) {
          // replace the temporary filler with the dragging panel
          int idx = parent.getComponentZOrder(fillerComponent);
          replaceComponent(parent, fillerComponent, draggingComponent, idx);
          isDragging = false;
        }
        break;
      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    Component c = e.getComponent();
    if (e.getID() == MouseEvent.MOUSE_DRAGGED && c instanceof JLayer<?>) {
      mouseDragged(l.getView(), e.getPoint());
    }
  }

  private void mouseDragged(JComponent parent, Point pt) {
    if (isDragging) {
      // keep the filler while the pointer stays in the half checked last time
      if (!prevRect.contains(pt)) {
        updateFillerLocation(parent, pt);
      }
      updateDraggingPanelLocation(parent, pt);
      // scroll the JViewport so that the dragging panel stays visible
      parent.scrollRectToVisible(draggingRect);
      parent.repaint();
    } else if (startPt.distance(pt) > dragThreshold) {
      startDragging(parent, pt);
    }
  }

  private void startDragging(JComponent parent, Point pt) {
    // drag the panel under the pressed point, not the one under the current point
    Component c = parent.getComponentAt(startPt);
    int index = parent.getComponentZOrder(c);
    if (index >= 0) {
      draggingComponent = c;
      isDragging = true;

      // The dragging panel is removed from the parent,
      // so its size is saved here to paint it in the JLayer
      Rectangle r = c.getBounds();
      draggingRect.setBounds(r);
      dragOffset.setLocation(startPt.x - r.x, startPt.y - r.y);
      prevRect.setBounds(0, 0, 0, 0);

      fillerComponent = Box.createRigidArea(r.getSize());
      replaceComponent(parent, c, fillerComponent, index);

      updateDraggingPanelLocation(parent, pt);
    }
  }

  private void updateDraggingPanelLocation(JComponent parent, Point pt) {
    int y = pt.y - dragOffset.y;
    Rectangle r = SwingUtilities.calculateInnerArea(parent, innerRect);
    int top = r.y;
    int bottom = r.y + r.height - draggingRect.height;
    draggingRect.setLocation(r.x, Math.min(Math.max(y, top), bottom));
    // Java 21: draggingRect.setLocation(r.x, Math.clamp(y, top, bottom));
  }

  private void updateFillerLocation(Container parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    int idx = parent.getComponentZOrder(c);
    if (idx >= 0 && !Objects.equals(c, fillerComponent)) {
      // remember the half of the panel under the pointer
      // to avoid moving the filler back and forth after the layout changes
      Rectangle r = c.getBounds();
      int half = Math.round(r.height / 2f);
      int y = pt.y < r.y + half ? r.y : r.y + half;
      prevRect.setBounds(r.x, y, r.width, half);
      // move the temporary filler to the location of the panel
      replaceComponent(parent, fillerComponent, fillerComponent, idx);
    }
  }

  private static void replaceComponent(Container parent, Component remove, Component add, int i) {
    parent.remove(remove);
    if (i >= 0 && add != null) {
      parent.add(add, i);
    }
    parent.revalidate();
    parent.repaint();
  }
}
