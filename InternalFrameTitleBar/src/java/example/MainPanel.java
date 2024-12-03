// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JButton closeButton = new JButton("close");
    closeButton.addActionListener(e -> {
      Component c = (Component) e.getSource();
      Optional.ofNullable(SwingUtilities.getWindowAncestor(c)) // .ifPresent(Window::dispose);
          .ifPresent(w -> w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING)));
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(new JTree()));
    p.add(closeButton, BorderLayout.SOUTH);

    JInternalFrame internal = makeInternalFrame();
    internal.getContentPane().add(p);
    internal.setVisible(true);

    add(internal);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame makeInternalFrame() {
    JInternalFrame internal = new JInternalFrame("@title@");
    BasicInternalFrameUI ui = (BasicInternalFrameUI) internal.getUI();
    Component title = ui.getNorthPane();
    for (MouseMotionListener l : title.getListeners(MouseMotionListener.class)) {
      title.removeMouseMotionListener(l);
    }
    DragWindowListener dwl = new DragWindowListener();
    title.addMouseListener(dwl);
    title.addMouseMotionListener(dwl);

    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    focusManager.addPropertyChangeListener(e -> {
      if (Objects.equals("activeWindow", e.getPropertyName())) {
        try {
          internal.setSelected(Objects.nonNull(e.getNewValue()));
        } catch (PropertyVetoException ex) {
          throw new IllegalStateException(ex);
        }
      }
    });

    // frame.addWindowListener(new WindowAdapter() {
    //   @Override public void windowLostFocus(FocusEvent e) {
    //     System.out.println("222222222");
    //     try {
    //       internal.setSelected(false);
    //     } catch (PropertyVetoException ex) {
    //       throw new IllegalStateException(ex);
    //     }
    //   }
    //   @Override public void windowGainedFocus(FocusEvent e) {
    //     System.out.println("111111111");
    //     try {
    //       internal.setSelected(true);
    //     } catch (PropertyVetoException ex) {
    //       throw new IllegalStateException(ex);
    //     }
    //   }
    // });
    // EventQueue.invokeLater(() -> {
    //   try {
    //     internal.setSelected(true);
    //   } catch (PropertyVetoException ex) {
    //     throw new IllegalStateException(ex);
    //   }
    //   // internal.requestFocusInWindow();
    // });
    return internal;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame();
    frame.setUndecorated(true);
    frame.setMinimumSize(new Dimension(300, 120));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    GraphicsConfiguration gc = frame.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      frame.setBackground(new Color(0x0, true));
    }
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}
