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
    System.out.println(key + ": " + b);
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
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(MenuBarUtil.createMenuBar());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class MenuBarUtil {
  private MenuBarUtil() {
    /* Singleton */
  }

  public static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    mb.add(createFileMenu());
    mb.add(createEditMenu());
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
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

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    menu.setMnemonic(KeyEvent.VK_L);
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setMnemonic(lafName.codePointAt(0));
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
    });
    lafGroup.add(lafItem);
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
    for (Window window: Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
