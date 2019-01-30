// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String SHOW_MNEMONICS = "Button.showMnemonics";
  private final JCheckBox showMnemonicsCheck = new JCheckBox(SHOW_MNEMONICS);

  private MainPanel() {
    super();
    showMnemonicsCheck.setSelected(UIManager.getBoolean(SHOW_MNEMONICS));
    showMnemonicsCheck.setMnemonic(KeyEvent.VK_B);
    showMnemonicsCheck.addActionListener(e -> {
      UIManager.put(SHOW_MNEMONICS, ((JCheckBox) e.getSource()).isSelected());
      if (UIManager.getLookAndFeel() instanceof WindowsLookAndFeel) {
        // System.out.println("isMnemonicHidden: " + WindowsLookAndFeel.isMnemonicHidden());
        WindowsLookAndFeel.setMnemonicHidden(true);
        // SwingUtilities.getRoot(this).repaint();
        Container c = getTopLevelAncestor();
        if (c != null) {
          c.repaint();
        }
      }
    });
    add(showMnemonicsCheck);

    JButton button = new JButton("Dummy");
    button.setMnemonic(KeyEvent.VK_D);
    add(button);

    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    mb.add(createMenu("File", Arrays.asList("Open", "Save", "Exit")));
    mb.add(createMenu("Edit", Arrays.asList("Cut", "Copy", "Paste", "Delete")));
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    mb.add(Box.createGlue());
    mb.add(createMenu("Help", Arrays.asList("Version", "About")));
    return mb;
  }

  private static JMenu createMenu(String title, List<String> list) {
    JMenu menu = new JMenu(title);
    menu.setMnemonic(title.codePointAt(0));
    for (String s: list) {
      menu.add(s).setMnemonic(s.codePointAt(0));
    }
    return menu;
  }

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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(createMenuBar());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() { /* Singleton */ }

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
