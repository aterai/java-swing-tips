// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private static final String PATH = "toolbarButtonGraphics/general/";
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = new JToolBar("ToolBarButton");
    toolBar.setFloatable(false);
    toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

    Stream.of(
        "Copy24.gif", "Cut24.gif", "Paste24.gif",
        "Delete24.gif", "Undo24.gif", "Redo24.gif",
        "Help24.gif", "Open24.gif", "Save24.gif"
    ).map(this::createToolBarButton).forEach(toolBar::add);

    log.setEditable(false);
    add(new JLayer<>(toolBar, new ReorderingLayerUI<>()), BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createToolBarButton(String name) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(PATH + name);
    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    JButton button = new JButton(icon);
    button.setFocusable(false);
    button.setToolTipText(name);
    // Fired only by a click, not by a drag that exceeds the drag threshold
    button.addActionListener(e -> log.append(name + " clicked\n"));
    return button;
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

class ReorderingLayerUI<V extends Container> extends LayerUI<V> {
  private final Point startPt = new Point();
  private final Point dragOffset = new Point();
  private final Point ghostPt = new Point();
  private final int dragThreshold = DragSource.getDragThreshold();
  private Component draggingComponent;
  private boolean isDragging;

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
    if (isDragging) {
      Component dc = draggingComponent;
      Container parent = dc.getParent();
      Graphics2D g2 = (Graphics2D) g.create();
      // Hide the original button to make it look like a gap
      Rectangle r = SwingUtilities.convertRectangle(parent, dc.getBounds(), c);
      g2.setPaint(parent.getBackground());
      g2.fill(r);
      // Paint the ghost of the dragging button
      g2.translate(ghostPt.x, ghostPt.y);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
      dc.paint(g2);
      g2.dispose();
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
    Container parent = l.getView();
    Component c = e.getComponent();
    int id = e.getID();
    if (id == MouseEvent.MOUSE_PRESSED) {
      boolean isChild = c != null && Objects.equals(c.getParent(), parent);
      if (SwingUtilities.isLeftMouseButton(e) && isChild) {
        // The candidate for dragging; the drag starts when the threshold is exceeded
        draggingComponent = c;
        dragOffset.setLocation(e.getPoint());
        startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), parent));
      }
    } else if (id == MouseEvent.MOUSE_RELEASED && isDragging) {
      isDragging = false;
      l.repaint();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    Component c = e.getComponent();
    boolean isLeftDragged = e.getID() == MouseEvent.MOUSE_DRAGGED
        && SwingUtilities.isLeftMouseButton(e);
    // The dragged events are dispatched to the component that received the press
    if (isLeftDragged && Objects.equals(c, draggingComponent)) {
      Container parent = l.getView();
      Point pt = SwingUtilities.convertPoint(c, e.getPoint(), parent);
      if (!isDragging && startPt.distance(pt) > dragThreshold) {
        startDragging(c);
      }
      if (isDragging) {
        rearrange(parent, pt);
        Point p = new Point(pt.x - dragOffset.x, pt.y - dragOffset.y);
        ghostPt.setLocation(SwingUtilities.convertPoint(parent, p, l));
        l.repaint();
      }
    }
  }

  private void startDragging(Component c) {
    isDragging = true;
    if (c instanceof AbstractButton) {
      // Cancel the click: DefaultButtonModel#setPressed(false) fires an ActionEvent
      // if the model is still armed, so the armed state must be cleared first.
      ButtonModel m = ((AbstractButton) c).getModel();
      m.setArmed(false);
      m.setPressed(false);
      m.setRollover(false);
    }
  }

  private void rearrange(Container parent, Point pt) {
    Component dc = draggingComponent;
    int cur = parent.getComponentZOrder(dc);
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      Rectangle r = c.getBounds();
      if (!Objects.equals(c, dc) && r.x <= pt.x && pt.x < r.x + r.width) {
        // Swap when the cursor passes the center of the adjacent component
        boolean isPrevArea = pt.x < r.x + r.width / 2;
        if (isPrevArea ? cur > i : cur < i) {
          // Container#setComponentZOrder(...) does not call removeNotify(),
          // so the button keeps receiving the mouse dragged events.
          parent.setComponentZOrder(dc, i);
          parent.revalidate();
        }
        break;
      }
    }
  }
}
