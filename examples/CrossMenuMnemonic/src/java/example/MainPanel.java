// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String key = "Menu.crossMenuMnemonic";
    boolean b = UIManager.getBoolean(key);
    // System.out.println(key + ": " + b);
    JCheckBox check = new JCheckBox(key, b) {
      @Override public void updateUI() {
        super.updateUI();
        setSelected(UIManager.getLookAndFeelDefaults().getBoolean(key));
        UIManager.put(key, isSelected());
      }
    };
    check.addActionListener(e -> {
      UIManager.put(key, ((JCheckBox) e.getSource()).isSelected());
      SwingUtilities.updateComponentTreeUI(getRootPane().getJMenuBar());
    });
    add(check);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(MenuBarUtils.createMenuBar()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class MenuBarUtils {
  private MenuBarUtils() {
    /* Singleton */
  }

  public static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    mb.add(createFileMenu());
    mb.add(createEditMenu());
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(Box.createGlue());
    mb.add(createHelpMenu());
    return mb;
  }

  private static JMenu createFileMenu() {
    JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    menu.add("New").setMnemonic(KeyEvent.VK_N);
    menu.add("Open").setMnemonic(KeyEvent.VK_O);
    return menu;
  }

  private static JMenu createEditMenu() {
    JMenu menu = new JMenu("Edit");
    menu.setMnemonic(KeyEvent.VK_E);
    menu.add("Cut").setMnemonic(KeyEvent.VK_T);
    menu.add("Copy").setMnemonic(KeyEvent.VK_C);
    menu.add("Paste").setMnemonic(KeyEvent.VK_P);
    menu.add("Delete").setMnemonic(KeyEvent.VK_D);
    return menu;
  }

  private static JMenu createHelpMenu() {
    JMenu menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);
    menu.add("About").setMnemonic(KeyEvent.VK_A);
    menu.add("Version").setMnemonic(KeyEvent.VK_V);
    return menu;
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
