// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label1 = makeImageLabel();
    JScrollPane scroll = makeScrollPane(label1);
    ViewportDragScrollListener l = new ViewportDragScrollListener();
    JViewport v = scroll.getViewport();
    v.addMouseMotionListener(l);
    v.addMouseListener(l);
    v.addHierarchyListener(l);

    JLabel label2 = makeImageLabel();
    ComponentDragScrollListener l2 = new ComponentDragScrollListener();
    label2.addMouseMotionListener(l2);
    label2.addMouseListener(l2);
    label2.addHierarchyListener(l2);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("ViewportDragScrollListener", scroll);
    tabbedPane.addTab("ComponentDragScrollListener", makeScrollPane(label2));

    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeImageLabel() {
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    return new JLabel(icon);
  }

  private static JScrollPane makeScrollPane(Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    return scroll;
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ViewportDragScrollListener extends MouseAdapter implements HierarchyListener {
  private static final int SPEED = 4;
  private static final int DELAY = 10;
  private static final Cursor DC = Cursor.getDefaultCursor();
  private static final Cursor HC = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point startPt = new Point();
  private final Point move = new Point();
  private final Timer scroller = new Timer(DELAY, null);
  private ActionListener listener;

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
      scroller.stop();
      scroller.removeActionListener(listener);
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    JViewport vport = (JViewport) e.getComponent();
    JComponent c = (JComponent) vport.getView();
    Point pt = e.getPoint();
    int dx = startPt.x - pt.x;
    int dy = startPt.y - pt.y;
    Point vp = vport.getViewPosition();
    vp.translate(dx, dy);
    c.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
    move.setLocation(SPEED * dx, SPEED * dy);
    startPt.setLocation(pt);
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(HC);
    startPt.setLocation(e.getPoint());
    move.setLocation(0, 0);
    scroller.stop();
    scroller.removeActionListener(listener);
  }

  @Override public void mouseReleased(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(DC);
    if (c instanceof JViewport) {
      JViewport vport = (JViewport) c;
      JComponent label = (JComponent) vport.getView();
      listener = event -> {
        Point vp = vport.getViewPosition(); // = SwingUtilities.convertPoint(vport, 0, 0, label);
        vp.translate(move.x, move.y);
        label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        // vport.setViewPosition(vp);
      };
      scroller.addActionListener(listener);
      scroller.start();
    }
  }

  @Override public void mouseExited(MouseEvent e) {
    e.getComponent().setCursor(DC);
    move.setLocation(0, 0);
    scroller.stop();
    scroller.removeActionListener(listener);
  }
}

class ComponentDragScrollListener extends MouseAdapter implements HierarchyListener {
  private static final int SPEED = 4;
  private static final int DELAY = 10;
  private static final Cursor DC = Cursor.getDefaultCursor();
  private static final Cursor HC = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point startPt = new Point();
  private final Point move = new Point();
  private final Timer scroller = new Timer(DELAY, null);
  private ActionListener listener;

  @Override public void hierarchyChanged(HierarchyEvent e) {
    boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
    if (b && !e.getComponent().isDisplayable()) {
      scroller.stop();
      scroller.removeActionListener(listener);
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    scroller.stop();
    scroller.removeActionListener(listener);
    JComponent jc = (JComponent) e.getComponent();
    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, jc);
    if (c instanceof JViewport) {
      JViewport vport = (JViewport) c;
      Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
      int dx = startPt.x - cp.x;
      int dy = startPt.y - cp.y;
      Point vp = vport.getViewPosition();
      vp.translate(dx, dy);
      jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
      move.setLocation(SPEED * dx, SPEED * dy);
      startPt.setLocation(cp);
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    scroller.stop();
    scroller.removeActionListener(listener);
    move.setLocation(0, 0);
    Component c = e.getComponent();
    c.setCursor(HC);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport vport = (JViewport) p;
      startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), vport));
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(DC);
    listener = event -> {
      Container p = SwingUtilities.getUnwrappedParent(c);
      if (p instanceof JViewport) {
        JViewport vport = (JViewport) p;
        Point vp = vport.getViewPosition();
        vp.translate(move.x, move.y);
        ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
      }
    };
    scroller.addActionListener(listener);
    scroller.start();
  }

  @Override public void mouseExited(MouseEvent e) {
    scroller.stop();
    scroller.removeActionListener(listener);
    e.getComponent().setCursor(DC);
    move.setLocation(0, 0);
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
