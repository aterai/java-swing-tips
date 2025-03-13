// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color AFC = new Color(0xEC_64_64);
  private final JPopupMenu popup = new JPopupMenu();

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("change accelerator") {
      @Override public void updateUI() {
        super.updateUI();
        changeAccelerator(isSelected());
      }
    };
    check.addActionListener(e -> {
      changeAccelerator(((JCheckBox) e.getSource()).isSelected());
      SwingUtilities.updateComponentTreeUI(getRootPane());
    });

    JMenu menu = LookAndFeelUtils.createLookAndFeelMenu();
    menu.setMnemonic('L');
    // menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
    JMenu sub = new JMenu("JMenu(M)");
    sub.setMnemonic('M');
    // sub.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0));
    KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0);
    KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
    KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.SHIFT_DOWN_MASK);
    KeyStroke ks4 = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0);
    KeyStroke ks5 = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
    sub.add("MenuItem1").setAccelerator(ks1);
    sub.add("MenuItem2").setAccelerator(ks2);
    sub.add("MenuItem3").setAccelerator(ks3);
    sub.add(new JCheckBoxMenuItem("JCheckBoxMenuItem")).setAccelerator(ks4);
    sub.add(new JRadioButtonMenuItem("JRadioButtonMenuItem")).setAccelerator(ks5);
    menu.add(sub);

    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    mb.add(sub);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JTree tree = new JTree();
    tree.setComponentPopupMenu(popup);
    popup.add("MenuItem4").setAccelerator(ks1);
    popup.add("MenuItem5").setAccelerator(ks2);
    popup.add("MenuItem6").setAccelerator(ks3);
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem")).setAccelerator(ks4);
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem")).setAccelerator(ks5);
    // java.lang.Error: setAccelerator() is not defined for JMenu.  Use setMnemonic() instead.
    // UIManager.put("Menu.acceleratorForeground", color) is meaningless?
    // JMenu menu2 = new JMenu("JMenu2");
    // menu2.add("MenuItem7");
    // popup.add(menu2).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0));

    add(new JScrollPane(tree));
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void changeAccelerator(boolean selected) {
    Color color1;
    Color color2;
    Font font;
    for (String prefix : Arrays.asList("MenuItem", "CheckBoxMenuItem", "RadioButtonMenuItem")) {
      String key1 = prefix + ".acceleratorForeground";
      String key2 = prefix + ".acceleratorSelectionForeground";
      String key3 = prefix + ".acceleratorFont";
      if (selected) {
        color1 = AFC;
        color2 = Color.WHITE;
        font = UIManager.getFont(key3);
        if (font != null) {
          font = font.deriveFont(10f);
        }
      } else {
        UIDefaults def = UIManager.getLookAndFeelDefaults();
        color1 = def.getColor(key1);
        color2 = def.getColor(key2);
        font = def.getFont(key3);
      }
      UIManager.put(key1, color1);
      UIManager.put(key2, color2);
      UIManager.put(key3, font);
    }
    SwingUtilities.updateComponentTreeUI(popup);
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
