// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    MouseAdapter dh = new RearrangingHandler();
    box.addMouseListener(dh);
    box.addMouseMotionListener(dh);

    int idx = 0;
    for (Component c : Arrays.asList(
        new JLabel("<html>1<br>11<br>111"),
        new JButton("22"),
        new JCheckBox("333"),
        new JScrollPane(new JTextArea(4, 12)))) {
      box.add(createSortablePanel(idx++, c));
    }
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createSortablePanel(int i, Component c) {
    JLabel l = new JLabel(String.format(" %04d ", i));
    l.setOpaque(true);
    l.setBackground(Color.RED);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createLineBorder(Color.BLUE, 2)));
    p.add(l, BorderLayout.WEST);
    p.add(c);
    p.setOpaque(false);
    return p;
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

class RearrangingHandler extends MouseAdapter {
  private static final Rectangle R1 = new Rectangle();
  private static final Rectangle R2 = new Rectangle();
  private static final Rectangle PREV_RECT = new Rectangle();
  private final int dragThreshold = DragSource.getDragThreshold();
  private final JWindow window = new JWindow();
  private final Point startPt = new Point();
  private int index = -1;
  private Component draggingComponent;
  private Component gap;
  private final Point dragOffset = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (((Container) e.getComponent()).getComponentCount() > 0) {
      startPt.setLocation(e.getPoint());
    }
  }

  private void startDragging(Container parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    index = parent.getComponentZOrder(c);
    if (Objects.equals(c, parent) || index < 0) {
      return;
    }
    draggingComponent = c;
    Dimension d = draggingComponent.getSize();

    Point dp = draggingComponent.getLocation();
    dragOffset.setLocation(pt.x - dp.x, pt.y - dp.y);

    gap = Box.createRigidArea(d);
    swapComponent(parent, c, gap, index);

    GraphicsConfiguration gc = window.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      window.setBackground(new Color(0x0, true));
    }
    window.add(draggingComponent);
    // window.setSize(d);
    window.pack();

    updateWindowLocation(pt, parent);
    window.setVisible(true);
  }

  private void updateWindowLocation(Point pt, Component parent) {
    if (window.isVisible() && Objects.nonNull(draggingComponent)) {
      Point p = new Point(pt.x - dragOffset.x, pt.y - dragOffset.y);
      SwingUtilities.convertPointToScreen(p, parent);
      window.setLocation(p);
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();
    if (Objects.isNull(draggingComponent)) {
      if (startPt.distance(pt) > dragThreshold) {
        startDragging(parent, pt);
      }
      return;
    }
    updateWindowLocation(pt, parent);
    if (!PREV_RECT.contains(pt)) {
      IntStream.range(0, parent.getComponentCount())
          .filter(i -> {
            Component c = parent.getComponent(i);
            return !Objects.equals(c, gap) || !c.getBounds().contains(pt);
          })
          .map(i -> getTargetIndex(parent.getComponent(i), pt, i))
          .filter(i -> i >= 0)
          .findFirst()
          .ifPresent(i -> swapComponent(parent, gap, gap, i));
    }
  }

  @SuppressWarnings("PMD.NullAssignment")
  @Override public void mouseReleased(MouseEvent e) {
    dragOffset.setLocation(0, 0);
    PREV_RECT.setBounds(0, 0, 0, 0);
    window.setVisible(false);
    // if (!window.isVisible() || Objects.isNull(draggingComponent) {
    //   return;
    // }
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();
    Rectangle ppRect = parent.getParent().getBounds();
    int max = parent.getComponentCount();
    Component cmp = draggingComponent;
    draggingComponent = null;
    int idx = IntStream.range(0, max)
        .map(i -> {
          Component c = parent.getComponent(i);
          return Objects.equals(c, gap) ? i : getTargetIndex(c, pt, i);
        })
        .filter(i -> i >= 0)
        .findFirst()
        .orElseGet(() -> ppRect.contains(pt) ? max : index);
    swapComponent(parent, gap, cmp, idx);
    // for (int i = 0; i < parent.getComponentCount(); i++) {
    //   Component c = parent.getComponent(i);
    //   if (Objects.equals(c, gap)) {
    //     swapComponent(parent, gap, cmp, i);
    //     return;
    //   }
    //   int tgt = getTargetIndex(c.getBounds(), pt, i);
    //   if (tgt >= 0) {
    //     swapComponent(parent, gap, cmp, tgt);
    //     return;
    //   }
    // }
    // if (parent.getParent().getBounds().contains(pt)) {
    //   swapComponent(parent, gap, cmp, parent.getComponentCount());
    // } else {
    //   swapComponent(parent, gap, cmp, index);
    // }
  }

  private static int getTargetIndex(Component c, Point pt, int i) {
    Rectangle r = c.getBounds();
    int ht2 = Math.round(r.height / 2f);
    R1.setBounds(r.x, r.y, r.width, ht2);
    R2.setBounds(r.x, r.y + ht2, r.width, ht2);
    int idx = -1;
    if (R1.contains(pt)) {
      PREV_RECT.setBounds(R1);
      idx = i > 1 ? i : 0;
    } else if (R2.contains(pt)) {
      PREV_RECT.setBounds(R2);
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
