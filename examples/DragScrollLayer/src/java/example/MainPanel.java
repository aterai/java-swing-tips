// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    Box box1 = makeTestBox();
    box1.setBorder(BorderFactory.createTitledBorder("DragScrollListener"));
    MouseInputListener l = new DragScrollListener();
    box1.addMouseListener(l);
    box1.addMouseMotionListener(l);
    add(new JScrollPane(box1));

    Box box2 = makeTestBox();
    box2.setBorder(BorderFactory.createTitledBorder("DragScrollLayerUI"));
    add(new JLayer<>(new JScrollPane(box2), new DragScrollLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeTestBox() {
    JTabbedPane tab1 = new JTabbedPane();
    tab1.addTab("aaa", new JLabel("11111111111"));
    tab1.addTab("bbb", new JCheckBox("2222222222"));

    JTabbedPane tab2 = new JTabbedPane();
    tab2.addTab("ccc cc", new JLabel("3333"));
    tab2.addTab("ddd dd", new JLabel("444444444444"));

    JTree tree = new JTree();
    tree.setVisibleRowCount(5);

    Box box = Box.createVerticalBox();
    box.add(new JLabel("aaa aaa aaa aaa aaa aaa"));
    box.add(Box.createVerticalStrut(5));
    box.add(tab1);
    box.add(Box.createVerticalStrut(5));
    box.add(new JCheckBox("bbb bbb bbb bbb"));
    box.add(Box.createVerticalStrut(5));
    box.add(tab2);
    box.add(Box.createVerticalStrut(5));
    box.add(new JSlider(0, 100, 50));
    box.add(Box.createVerticalStrut(5));
    box.add(new JScrollPane(tree));
    box.add(Box.createVerticalStrut(5));
    box.add(new JButton("ccc ccc"));
    box.add(Box.createVerticalGlue());
    return box;
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

class DragScrollListener extends MouseInputAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point pp = new Point();

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      Rectangle rect = viewport.getViewRect();
      rect.translate(pp.x - cp.x, pp.y - cp.y);
      ((JComponent) c).scrollRectToVisible(rect);
      pp.setLocation(cp);
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hndCursor);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport viewport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      pp.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }
}

class DragScrollLayerUI extends LayerUI<JScrollPane> {
  private final Point startPt = new Point();

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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      mousePressed(e, l);
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      mouseDragged(e, l);
    }
  }

  private void mousePressed(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    boolean b = c instanceof JScrollBar || c instanceof JSlider;
    if (!b) {
      JViewport viewport = l.getView().getViewport();
      // Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      startPt.setLocation(viewport.getViewPosition());
    }
  }

  private void mouseDragged(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    boolean b = c instanceof JScrollBar || c instanceof JSlider;
    if (!b) {
      JViewport viewport = l.getView().getViewport();
      JComponent cmp = (JComponent) viewport.getView();
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), viewport);
      Rectangle rect = viewport.getViewRect();
      rect.translate(startPt.x - cp.x, startPt.y - cp.y);
      cmp.scrollRectToVisible(rect);
      startPt.setLocation(cp);
    }
  }
}
