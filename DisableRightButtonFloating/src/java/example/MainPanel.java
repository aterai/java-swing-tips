// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = makeToolBar("Override createDockingListener()");
    toolBar.setUI(new BasicToolBarUI() {
      @Override protected MouseInputListener createDockingListener() {
        return new DockingListener2(toolBar, super.createDockingListener());
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(new JTree()));
    p.add(toolBar, BorderLayout.NORTH);
    p.add(makeToolBar("DisableRightButtonDraggedOut"), BorderLayout.SOUTH);
    p.add(makeToolBar("Default"), BorderLayout.WEST);

    add(new JLayer<>(p, new DisableRightButtonDragOutLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private JToolBar makeToolBar(String title) {
    JToolBar toolBar = new JToolBar(title);
    toolBar.add(new JLabel(title));
    toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolBar.add(new JButton("Button"));
    toolBar.add(Box.createGlue());

    JPopupMenu popup = new JPopupMenu();
    popup.add("Item 1");
    popup.add("Item 2");
    popup.add("Item 3");
    toolBar.setComponentPopupMenu(popup);
    return toolBar;
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

class DockingListener2 extends MouseInputAdapter {
  private final JToolBar toolBar;
  private final MouseInputListener listener;

  protected DockingListener2(JToolBar toolBar, MouseInputListener listener) {
    super();
    this.toolBar = toolBar;
    this.listener = listener;
  }

  private boolean cancelDrag(MouseEvent e) {
    return !toolBar.isEnabled() || !SwingUtilities.isLeftMouseButton(e);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (cancelDrag(e)) {
      return;
    }
    listener.mousePressed(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (cancelDrag(e)) {
      return;
    }
    listener.mouseDragged(e);
  }

  @Override public void mouseReleased(MouseEvent e) {
    if (cancelDrag(e)) {
      return;
    }
    listener.mouseReleased(e);
  }
}

class DisableRightButtonDragOutLayerUI extends LayerUI<JPanel> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
    Component c = e.getComponent();
    if (c instanceof JToolBar) {
      boolean dragEvent = e.getID() == MouseEvent.MOUSE_DRAGGED;
      boolean leftButton = SwingUtilities.isLeftMouseButton(e);
      boolean checkName = "DisableRightButtonDraggedOut".equals(c.getName());
      if (dragEvent && !leftButton && checkName) {
        e.consume();
      }
    }
  }
}
