// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String PATH = "toolbarButtonGraphics/general/";

  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = new JToolBar("ToolBarButton");
    toolBar.setFloatable(false);
    DragHandler dh = new DragHandler();
    toolBar.addMouseListener(dh);
    toolBar.addMouseMotionListener(dh);
    toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

    Stream.of(
        "Copy24.gif", "Cut24.gif", "Paste24.gif",
        "Delete24.gif", "Undo24.gif", "Redo24.gif",
        "Help24.gif", "Open24.gif", "Save24.gif"
    ).map(this::createToolBarButton).forEach(toolBar::add);

    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createToolBarButton(String name) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(PATH + name);
    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    JComponent label = new JLabel(icon);
    label.setOpaque(false);
    return label;
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

class DragHandler extends MouseAdapter {
  private static final Rectangle PREV_AREA = new Rectangle();
  private static final Rectangle NEXT_AREA = new Rectangle();
  private final JWindow window = new JWindow();
  private Component draggingComponent;
  private int index = -1;
  private Component gap;
  private final Point startPt = new Point();
  private final int dragThreshold = DragSource.getDragThreshold();

  @Override public void mousePressed(MouseEvent e) {
    Container parent = (Container) e.getComponent();
    if (parent.getComponentCount() > 0) {
      startPt.setLocation(e.getPoint());
      GraphicsConfiguration gc = window.getGraphicsConfiguration();
      if (gc != null && gc.isTranslucencyCapable()) {
        window.setBackground(new Color(0x0, true));
      }
    }
  }

  private void startDragging(Container parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    index = parent.getComponentZOrder(c);
    if (Objects.equals(c, parent) || index < 0) {
      return;
    }
    draggingComponent = c;
    gap = Box.createHorizontalStrut(c.getWidth());
    swapComponent(parent, c, gap, index);

    window.add(draggingComponent);
    window.pack();

    Dimension d = draggingComponent.getPreferredSize();
    Point p = new Point(pt.x - d.width / 2, pt.y - d.height / 2);
    SwingUtilities.convertPointToScreen(p, parent);
    window.setLocation(p);
    window.setVisible(true);
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();

    if (!window.isVisible() || Objects.isNull(draggingComponent)) {
      if (startPt.distance(pt) > dragThreshold) {
        startDragging(parent, pt);
      }
      return;
    }

    Dimension d = draggingComponent.getPreferredSize();
    Point p = new Point(pt.x - d.width / 2, pt.y - d.height / 2);
    SwingUtilities.convertPointToScreen(p, parent);
    window.setLocation(p);

    int idx = IntStream.range(0, parent.getComponentCount())
        .map(i -> getTargetIndex(parent, i, pt))
        .filter(i -> i >= 0)
        .findFirst()
        .orElse(-1);
    swapComponent(parent, gap, gap, idx);
  }

  @SuppressWarnings("PMD.NullAssignment")
  @Override public void mouseReleased(MouseEvent e) {
    if (!window.isVisible() || Objects.isNull(draggingComponent)) {
      return;
    }
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();
    int max = parent.getComponentCount();
    Component cmp = draggingComponent;
    draggingComponent = null;
    window.setVisible(false);
    int idx = IntStream.range(0, max)
        .map(i -> getTargetIndex(parent, i, pt))
        .filter(i -> i >= 0)
        .findFirst()
        .orElseGet(() -> parent.getBounds().contains(pt) ? max : index);
    swapComponent(parent, gap, cmp, idx);
  }

  private int getTargetIndex(Container parent, int i, Point pt) {
    Component c = parent.getComponent(i);
    Rectangle r = c.getBounds();
    int wd2 = r.width / 2;
    PREV_AREA.setBounds(r.x, r.y, wd2, r.height);
    NEXT_AREA.setBounds(r.x + wd2, r.y, wd2, r.height);
    int idx = -1;
    if (PREV_AREA.contains(pt)) {
      idx = i > 1 ? i : 0;
    } else if (NEXT_AREA.contains(pt)) {
      idx = i;
    }
    return idx;
  }

  private static void swapComponent(Container p, Component remove, Component add, int idx) {
    p.remove(remove);
    if (idx >= 0) {
      p.add(add, idx);
    }
    p.revalidate();
    p.repaint();
  }
}
