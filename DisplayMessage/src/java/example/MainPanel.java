// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    MenuItem openItem = new MenuItem("OPEN");
    openItem.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        c.setVisible(true);
      }
    });

    MenuItem exitItem = new MenuItem("EXIT");
    exitItem.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        ((Window) c).dispose();
      }
      SystemTray tray = SystemTray.getSystemTray();
      for (TrayIcon icon: tray.getTrayIcons()) {
        tray.remove(icon);
      }
    });

    PopupMenu popup = new PopupMenu();
    popup.add(openItem);
    popup.add(exitItem);

    Image image = new ImageIcon(getClass().getResource("16x16.png")).getImage();
    try {
      SystemTray.getSystemTray().add(new TrayIcon(image, "TRAY", popup));
      // icon.addActionListener(e -> log.append(e.toString() + "\n"));
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }

    // ERROR, WARNING, INFO, NONE
    JComboBox<TrayIcon.MessageType> messageType = new JComboBox<>(TrayIcon.MessageType.values());

    JButton messageButton = new JButton("TrayIcon#displayMessage()");
    messageButton.addActionListener(e -> {
      TrayIcon[] icons = SystemTray.getSystemTray().getTrayIcons();
      if (icons.length > 0) {
        icons[0].displayMessage("caption", "text text", messageType.getItemAt(messageType.getSelectedIndex()));
      }
    });
    JPanel p = new JPanel();
    p.add(messageType);
    p.add(messageButton);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
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
    if (SystemTray.isSupported()) {
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
        @Override public void windowIconified(WindowEvent e) {
          e.getWindow().dispose();
        }
      });
    } else {
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    frame.getContentPane().add(new MainPanel());
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
