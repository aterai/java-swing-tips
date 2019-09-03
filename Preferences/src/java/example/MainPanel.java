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

  public MainPanel() {
    super(new BorderLayout());

    JButton clearButton = new JButton("Preferences#clear() and JFrame#dispose()");
    clearButton.addActionListener(e -> {
      try {
        handler.prefs.clear();
        handler.prefs.flush();
      } catch (BackingStoreException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
      Optional.ofNullable(SwingUtilities.getWindowAncestor((Component) e.getSource())).ifPresent(Window::dispose);
    });

    JButton exitButton = new JButton("exit");
    exitButton.addActionListener(e -> {
      handler.saveLocation();
      Optional.ofNullable(SwingUtilities.getWindowAncestor((Component) e.getSource())).ifPresent(Window::dispose);
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
      if (c instanceof JFrame) {
        JFrame frame = (JFrame) c;
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
  public final Preferences prefs = Preferences.userNodeForPackage(getClass());
  public final Dimension dim = new Dimension(320, 240);
  public final Point pos = new Point();

  public void initFrameSizeAndLocation(JFrame frame) {
    int wdim = prefs.getInt(PREFIX + "dimw", dim.width);
    int hdim = prefs.getInt(PREFIX + "dimh", dim.height);
    dim.setSize(wdim, hdim);
    // setPreferredSize(dim);
    frame.pack();

    Rectangle screen = frame.getGraphicsConfiguration().getBounds();
    pos.setLocation(screen.x + screen.width / 2 - dim.width / 2, screen.y + screen.height / 2 - dim.height / 2);
    int xpos = prefs.getInt(PREFIX + "locx", pos.x);
    int ypos = prefs.getInt(PREFIX + "locy", pos.y);
    pos.setLocation(xpos, ypos);
    frame.setLocation(pos.x, pos.y);
  }

  public void saveLocation() {
    prefs.putInt(PREFIX + "locx", pos.x);
    prefs.putInt(PREFIX + "locy", pos.y);
    prefs.putInt(PREFIX + "dimw", dim.width);
    prefs.putInt(PREFIX + "dimh", dim.height);
    try {
      prefs.flush();
    } catch (BackingStoreException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
  }

  @Override public void componentHidden(ComponentEvent e) {
    /* not needed */
  }

  @Override public void componentMoved(ComponentEvent e) {
    JFrame frame = (JFrame) e.getComponent();
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
