// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel {
  public void start(JFrame frame) {
    String path = "example/splash.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);

    JWindow splashScreen = createSplashScreen(frame, icon);
    splashScreen.setVisible(true);
    new SwingWorker<Void, Void>() {
      @Override protected Void doInBackground() throws Exception {
        Thread.sleep(6000);
        return null;
      }

      @Override protected void done() {
        showFrame(frame);
        // hideSplash();
        splashScreen.setVisible(false);
        splashScreen.dispose();
      }
    }.execute();
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
      Window w = (Window) c.getTopLevelAncestor();
      // w.dispose();
      // System.exit(0);
      // w.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(...));
      w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
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

  public static JWindow createSplashScreen(Frame frame, Icon icon) {
    DragWindowListener dwl = new DragWindowListener();

    JLabel label = new JLabel(icon);
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
  //   JMenu menu = new JMenu("FFF");
  //   menu.setMnemonic(KeyEvent.VK_F);
  //   menuBar.add(menu);
  //
  //   JMenuItem menuItem = new JMenuItem("NNN");
  //   menuItem.setMnemonic(KeyEvent.VK_N);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("MMM");
  //   menuItem.setMnemonic(KeyEvent.VK_M);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("UUU");
  //   menuItem.setMnemonic(KeyEvent.VK_U);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   menuItem = new JMenuItem("III");
  //   menuItem.setMnemonic(KeyEvent.VK_I);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK));
  //   menu.add(menuItem);
  //
  //   return menuBar;
  // }

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
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
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
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}
