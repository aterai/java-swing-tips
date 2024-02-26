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
  private final JCheckBox check = new JCheckBox(info);

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
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
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
        Dimension d = super.getPreferredSize();
        if (check.isSelected()) {
          // Java 9 error: package com.sun.java.swing.plaf.motif is not visible
          // boolean isMotif = getUI() instanceof MotifDesktopIconUI;
          boolean isMotif = getUI().getClass().getName().contains("MotifDesktopIconUI");
          if (isMotif) {
            d.setSize(64, 64 + 32);
          } else {
            d.setSize(ICON_SZ);
          }
        }
        return d;
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
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
