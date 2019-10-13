// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String key = "Menu.cancelMode";

    String cancelMode = UIManager.getString(key);
    System.out.println(key + ": " + cancelMode);
    boolean defaultMode = "hideMenuTree".equals(cancelMode);
    JRadioButton hideMenuTreeRadio = makeRadioButton("hideMenuTree", defaultMode);
    JRadioButton hideLastSubmenuRadio = makeRadioButton("hideLastSubmenu", !defaultMode);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder(key));
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        JRadioButton r = (JRadioButton) e.getSource();
        UIManager.put(key, r.getText());
      }
    };
    ButtonGroup bg = new ButtonGroup();
    Stream.of(hideLastSubmenuRadio, hideMenuTreeRadio).forEach(r -> {
      r.addItemListener(handler);
      bg.add(r);
      box.add(r);
    });
    add(box);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(makeMenuBar()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JRadioButton makeRadioButton(String text, boolean selected) {
    return new JRadioButton(text, selected) {
      @Override public void updateUI() {
        super.updateUI();
        String mode = UIManager.getLookAndFeelDefaults().getString("Menu.cancelMode");
        setSelected(text.equals(mode));
      }
    };
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar bar = new JMenuBar();

    JMenu menu = bar.add(new JMenu("Test"));
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    JMenu sub = new JMenu("JMenu");
    sub.add("JMenuItem4");
    sub.add("JMenuItem5");
    menu.add(sub);
    menu.add("JMenuItem3");

    bar.add(LookAndFeelUtil.createLookAndFeelMenu());
    return bar;
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
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
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
    for (Window window: Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
