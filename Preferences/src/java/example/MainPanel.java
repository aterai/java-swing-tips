// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient WindowPreferencesHandler handler = new WindowPreferencesHandler();

  private MainPanel() {
    super(new BorderLayout());
    JButton clearButton = new JButton("Preferences#clear() and JFrame#dispose()");
    clearButton.addActionListener(e -> {
      try {
        handler.pref.clear();
        handler.pref.flush();
      } catch (BackingStoreException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
      Component c = (Component) e.getSource();
      Optional.ofNullable(SwingUtilities.getWindowAncestor(c)).ifPresent(Window::dispose);
    });

    JButton exitButton = new JButton("exit");
    exitButton.addActionListener(e -> {
      handler.saveLocation();
      Component c = (Component) e.getSource();
      Optional.ofNullable(SwingUtilities.getWindowAncestor(c)).ifPresent(Window::dispose);
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(clearButton);
    box.add(Box.createHorizontalStrut(2));
    box.add(exitButton);

    add(new JLabel("TEST"));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.SOUTH);

    EventQueue.invokeLater(() -> {
      // Component c = SwingUtilities.getRoot(getRootPane());
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        Window frame = (Window) c;
        frame.addWindowListener(handler);
        frame.addComponentListener(handler);
        handler.initFrameSizeAndLocation(frame);
      }
    });
  }

  @Override public Dimension getPreferredSize() {
    return handler.dim;
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
    // frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class WindowPreferencesHandler extends WindowAdapter implements ComponentListener {
  private static final String PREFIX = "xxx_";
  public final Preferences pref = Preferences.userNodeForPackage(getClass());
  public final Dimension dim = new Dimension(320, 240);
  public final Point pos = new Point();

  public void initFrameSizeAndLocation(Window frame) {
    GraphicsConfiguration gc = frame.getGraphicsConfiguration();
    if (gc != null) {
      int width = pref.getInt(PREFIX + "dimw", dim.width);
      int height = pref.getInt(PREFIX + "dimh", dim.height);
      dim.setSize(width, height);
      // setPreferredSize(dim);
      frame.pack();
      Rectangle screen = gc.getBounds();
      double dx = screen.getCenterX() - dim.width / 2d;
      double dy = screen.getCenterY() - dim.height / 2d;
      pos.setLocation(dx, dy);
      int px = pref.getInt(PREFIX + "locx", pos.x);
      int py = pref.getInt(PREFIX + "locy", pos.y);
      pos.setLocation(px, py);
      frame.setLocation(pos.x, pos.y);
    }
  }

  public void saveLocation() {
    pref.putInt(PREFIX + "locx", pos.x);
    pref.putInt(PREFIX + "locy", pos.y);
    pref.putInt(PREFIX + "dimw", dim.width);
    pref.putInt(PREFIX + "dimh", dim.height);
    try {
      pref.flush();
    } catch (BackingStoreException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
  }

  @Override public void componentHidden(ComponentEvent e) {
    /* not needed */
  }

  @Override public void componentMoved(ComponentEvent e) {
    Frame frame = (Frame) e.getComponent();
    if (frame.getExtendedState() == Frame.NORMAL) {
      Point pt = frame.getLocationOnScreen();
      if (pt.x < 0 || pt.y < 0) {
        return;
      }
      pos.setLocation(pt);
    }
  }

  @Override public void componentResized(ComponentEvent e) {
    JFrame frame = (JFrame) e.getComponent();
    if (frame.getExtendedState() == Frame.NORMAL) {
      dim.setSize(frame.getContentPane().getSize());
    }
  }

  @Override public void componentShown(ComponentEvent e) {
    /* not needed */
  }

  // @Override public void windowActivated(WindowEvent e) {
  //   /* not needed */
  // }

  // @Override public void windowClosed(WindowEvent e) {
  //   /* not needed */
  // }

  @Override public void windowClosing(WindowEvent e) {
    saveLocation();
    e.getWindow().dispose();
  }

  // @Override public void windowDeactivated(WindowEvent e) {
  //   /* not needed */
  // }

  // @Override public void windowDeiconified(WindowEvent e) {
  //   /* not needed */
  // }

  // @Override public void windowIconified(WindowEvent e) {
  //   /* not needed */
  // }

  // @Override public void windowOpened(WindowEvent e) {
  //   /* not needed */
  // }
}
