// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Dimension ICON_SZ = new Dimension(150, 40);
  private final String info = String.format("JDesktopIcon: %dx%d", ICON_SZ.width, ICON_SZ.height);
  public final JCheckBox check = new JCheckBox(info);

  private MainPanel() {
    super(new BorderLayout());
    check.setOpaque(false);

    JDesktopPane desktop = new JDesktopPane();
    // desktop.setDesktopManager(new DefaultDesktopManager() {
    //   @Override protected Rectangle getBoundsForIconOf(JInternalFrame f) {
    //     Rectangle r = super.getBoundsForIconOf(f);
    //     r.width = 200;
    //     return r;
    //   }
    // });

    AtomicInteger idx = new AtomicInteger();
    JButton button = new JButton("add");
    button.addActionListener(e -> {
      int i = idx.getAndIncrement();
      desktop.add(createFrame("#" + i, i * 10, i * 10));
    });

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    mb.add(button);
    mb.add(Box.createHorizontalGlue());
    mb.add(check);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    addIconifiedFrame(desktop, createFrame("Frame", 30, 10));
    addIconifiedFrame(desktop, createFrame("Frame", 50, 30));
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private JInternalFrame createFrame(String t, int x, int y) {
    JInternalFrame f = new JInternalFrame(t, true, true, true, true);
    f.setDesktopIcon(new JInternalFrame.JDesktopIcon(f) {
      @Override public Dimension getPreferredSize() {
        if (!check.isSelected()) {
          return super.getPreferredSize();
        }
        // Java 9 error: package com.sun.java.swing.plaf.motif is not visible
        // if (getUI() instanceof MotifDesktopIconUI) {
        if (getUI().getClass().getName().contains("MotifDesktopIconUI")) {
          return new Dimension(64, 64 + 32);
        } else {
          return ICON_SZ;
        }
      }
    });
    f.setSize(200, 100);
    f.setLocation(x, y);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
  }

  private static void addIconifiedFrame(JDesktopPane desktop, JInternalFrame f) {
    desktop.add(f);
    try {
      f.setIcon(true);
    } catch (PropertyVetoException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("DesktopIcon.width", ICON_SZ.width);
      // TEST:
      // Font f = UIManager.getFont("InternalFrame.titleFont");
      // UIManager.put("InternalFrame.titleFont", f.deriveFont(30f));
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

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
