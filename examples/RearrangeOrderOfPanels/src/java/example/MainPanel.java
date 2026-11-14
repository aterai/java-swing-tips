// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    MouseAdapter dh = new RearrangingHandler();
    box.addMouseListener(dh);
    box.addMouseMotionListener(dh);
    Stream.of(
        new JLabel("<html>1<br>11<br>111"),
        new JButton("22"),
        new JCheckBox("333"),
        new JScrollPane(new JTextArea(4, 12))).forEach(c -> box.add(createSortablePanel(c)));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createSortablePanel(Component c) {
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

class RearrangingHandler extends MouseAdapter {
  private final Rectangle prevRect = new Rectangle();
  private final int dragThreshold = DragSource.getDragThreshold();
  private final JWindow window = new JWindow();
  private final Point startPt = new Point();
  private final Point dragOffset = new Point();
  private boolean isDragging;
  private Component draggingComponent;
  private Component gap;

  @Override public void mousePressed(MouseEvent e) {
    startPt.setLocation(e.getPoint());
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();
    if (isDragging) {
      updateWindowLocation(pt, parent);
      // keep the gap while the pointer stays in the half checked last time
      if (!prevRect.contains(pt)) {
        updateGapLocation(parent, pt);
      }
    } else if (startPt.distance(pt) > dragThreshold) {
      startDragging(parent, pt);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    // a click without dragging does nothing
    if (isDragging) {
      window.setVisible(false);
      // replace the temporary gap with the dragging panel
      Container parent = (Container) e.getComponent();
      int idx = parent.getComponentZOrder(gap);
      replaceComponent(parent, gap, draggingComponent, idx);
      isDragging = false;
    }
  }

  private void startDragging(Container parent, Point pt) {
    // drag the panel under the pressed point, not the one under the current point
    Component c = parent.getComponentAt(startPt);
    int index = parent.getComponentZOrder(c);
    if (index >= 0) {
      isDragging = true;
      draggingComponent = c;
      prevRect.setBounds(0, 0, 0, 0);

      Rectangle r = c.getBounds();
      dragOffset.setLocation(startPt.x - r.x, startPt.y - r.y);

      gap = Box.createRigidArea(r.getSize());
      replaceComponent(parent, c, gap, index);

      GraphicsConfiguration gc = window.getGraphicsConfiguration();
      if (gc != null && gc.isTranslucencyCapable()) {
        window.setBackground(new Color(0x0, true));
      }
      window.add(draggingComponent);
      window.pack();

      // set the location before the window is shown
      updateWindowLocation(pt, parent);
      window.setVisible(true);
    }
  }

  private void updateWindowLocation(Point pt, Component parent) {
    Point p = new Point(pt.x - dragOffset.x, pt.y - dragOffset.y);
    SwingUtilities.convertPointToScreen(p, parent);
    window.setLocation(p);
  }

  private void updateGapLocation(Container parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    int idx = parent.getComponentZOrder(c);
    if (idx >= 0 && !Objects.equals(c, gap)) {
      // remember the half of the panel under the pointer
      // to avoid moving the gap back and forth after the layout changes
      Rectangle r = c.getBounds();
      int half = Math.round(r.height / 2f);
      int y = pt.y < r.y + half ? r.y : r.y + half;
      prevRect.setBounds(r.x, y, r.width, half);
      // move the temporary gap to the location of the panel
      replaceComponent(parent, gap, gap, idx);
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
