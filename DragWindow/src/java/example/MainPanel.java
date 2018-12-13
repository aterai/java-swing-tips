// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public final class MainPanel {
  public void start(JFrame frame) {
    ImageIcon img = new ImageIcon(MainPanel.class.getResource("splash.png"));
    JWindow splashScreen = createSplashScreen(frame, img);
    splashScreen.setVisible(true);

    new Thread() {
      @Override public void run() {
        try {
          // dummy long task
          Thread.sleep(6000);
          EventQueue.invokeAndWait(() -> {
            showFrame(frame);
            // hideSplash();
            splashScreen.setVisible(false);
            splashScreen.dispose();
          });
        } catch (InterruptedException | InvocationTargetException ex) {
          ex.printStackTrace();
        }
      }
    }.start();
  }

  private static Component makeUI() {
    JLabel label = new JLabel("Draggable Label (@title@)");
    DragWindowListener dwl = new DragWindowListener();
    label.addMouseListener(dwl);
    label.addMouseMotionListener(dwl);
    label.setOpaque(true);
    label.setForeground(Color.WHITE);
    label.setBackground(Color.BLUE);
    label.setBorder(BorderFactory.createEmptyBorder(5, 16 + 5, 5, 2));

    JButton button = new JButton("Exit");
    button.addActionListener(e -> {
      JComponent c = (JComponent) e.getSource();
      JFrame frame = (JFrame) c.getTopLevelAncestor();
      // frame.dispose();
      // System.exit(0);
      // frame.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
      frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JPanel p = new JPanel(new BorderLayout());
    p.add(label, BorderLayout.NORTH);
    p.add(box, BorderLayout.SOUTH);
    p.add(new JLabel("Alt+Space => System Menu"));
    return p;
  }

  public static JWindow createSplashScreen(JFrame frame, ImageIcon img) {
    DragWindowListener dwl = new DragWindowListener();

    JLabel label = new JLabel(img);
    label.addMouseListener(dwl);
    label.addMouseMotionListener(dwl);

    JWindow window = new JWindow(frame);
    window.getContentPane().add(label);
    window.pack();
    window.setLocationRelativeTo(null);
    return window;
  }

  public static void showFrame(JFrame frame) {
    frame.getContentPane().add(makeUI());
    frame.setMinimumSize(new Dimension(100, 100));
    frame.setSize(320, 240);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  // public static JMenuBar createMenuBar() {
  //   JMenuBar menuBar = new JMenuBar();
  //   JMenu menu = new JMenu("FFFFFF");
  //   menu.setMnemonic(KeyEvent.VK_F);
  //   menuBar.add(menu);
  //
  //   JMenuItem menuItem = new JMenuItem("NNNNNNNNN");
  //   menuItem.setMnemonic(KeyEvent.VK_N);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("MMMMMMMM");
  //   menuItem.setMnemonic(KeyEvent.VK_M);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("UUUUUU");
  //   menuItem.setMnemonic(KeyEvent.VK_U);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("IIIIIIIIII");
  //   menuItem.setMnemonic(KeyEvent.VK_I);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   return menuBar;
  // }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame();
    frame.setUndecorated(true);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    // frame.getContentPane().add(new MainPanel(frame));
    // frame.setMinimumSize(new Dimension(100, 100));
    // frame.setSize(320, 240);
    // frame.setLocationRelativeTo(null);
    // frame.setVisible(true);
    new MainPanel().start(frame);
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
      Window window = (Window) c;
      Point pt = window.getLocation();
      window.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}
