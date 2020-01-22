// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public class MainPanel extends JPanel {
  private final JDialog dialog = new JDialog();
  private final Timer animator = new Timer(100, null);
  private final transient Image[] images = new Image[4];
  private final transient TrayIcon icon;
  private int idx;

  private MainPanel() {
    super();
    setPreferredSize(new Dimension(320, 240));

    if (!SystemTray.isSupported()) {
      throw new UnsupportedOperationException("SystemTray is not supported");
    }

    Class<?> clz = MainPanel.class;
    images[0] = new ImageIcon(clz.getResource("16x16.png")).getImage();
    images[1] = new ImageIcon(clz.getResource("16x16l.png")).getImage();
    images[2] = images[0];
    images[3] = new ImageIcon(clz.getResource("16x16r.png")).getImage();

    dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    dialog.setSize(new Dimension(120, 100));
    dialog.setLocationRelativeTo(null);
    dialog.setTitle("TEST: JDialog");

    // TEST: icon = new TrayIcon(new ImageIcon(getClass().getResource("animated.gif")).getImage(), "TRAY", popup);
    icon = new TrayIcon(images[0], "TRAY", makeTrayPopupMenu());
    animator.addActionListener(e -> {
      icon.setImage(images[idx]);
      idx = (idx + 1) % images.length;
    });
    try {
      SystemTray.getSystemTray().add(icon);
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected final PopupMenu makeTrayPopupMenu() {
    MenuItem item1 = new MenuItem("Open:Frame");
    item1.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        c.setVisible(true);
      }
    });

    MenuItem item2 = new MenuItem("Open:Dialog");
    item2.addActionListener(e -> dialog.setVisible(true));

    MenuItem item3 = new MenuItem("Animation:Start");
    item3.addActionListener(e -> animator.start());

    MenuItem item4 = new MenuItem("Animation:Stop");
    item4.addActionListener(e -> {
      animator.stop();
      icon.setImage(images[0]);
    });

    MenuItem item5 = new MenuItem("Exit");
    item5.addActionListener(e -> {
      animator.stop();
      SystemTray tray = SystemTray.getSystemTray();
      Stream.of(tray.getTrayIcons()).forEach(tray::remove);
      Stream.of(Frame.getFrames()).forEach(Frame::dispose);
    });

    PopupMenu popup = new PopupMenu();
    popup.add(item1);
    popup.add(item2);
    popup.addSeparator();
    popup.add(item3);
    popup.add(item4);
    popup.addSeparator();
    popup.add(item5);

    return popup;
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
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
