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

  private MainPanel() {
    super();
    JCheckBox check = new JCheckBox(SHOW_MNEMONICS);
    check.setSelected(UIManager.getBoolean(SHOW_MNEMONICS));
    check.setMnemonic(KeyEvent.VK_B);
    check.addActionListener(e -> {
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
    add(check);

    JButton button = new JButton("JButton");
    button.setMnemonic(KeyEvent.VK_J);
    add(button);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    mb.add(createMenu("File", Arrays.asList("Open", "Save", "Exit")));
    mb.add(createMenu("Edit", Arrays.asList("Cut", "Copy", "Paste", "Delete")));
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(Box.createGlue());
    mb.add(createMenu("Help", Arrays.asList("Version", "About")));
    return mb;
  }

  private static JMenu createMenu(String title, List<String> list) {
    JMenu menu = new JMenu(title);
    menu.setMnemonic(title.codePointAt(0));
    for (String s : list) {
      menu.add(s).setMnemonic(s.codePointAt(0));
    }
    return menu;
  }

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
