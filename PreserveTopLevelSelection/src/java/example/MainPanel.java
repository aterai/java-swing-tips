// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String key = "Menu.preserveTopLevelSelection";
    Boolean b = UIManager.getBoolean(key);
    // System.out.println(key + ": " + b);
    JCheckBox keepTopLvlSel = new JCheckBox(key, b) {
      @Override public void updateUI() {
        super.updateUI();
        setSelected(UIManager.getLookAndFeelDefaults().getBoolean(key));
        UIManager.put(key, isSelected());
      }
    };
    keepTopLvlSel.addActionListener(e ->
        UIManager.put(key, ((JCheckBox) e.getSource()).isSelected()));

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(makeMenuBar()));
    add(keepTopLvlSel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar bar = new JMenuBar();

    JMenu menu = bar.add(new JMenu("File"));
    menu.add("Open");
    menu.add("Save");
    menu.add("Exit");

    menu = bar.add(new JMenu("Edit"));
    menu.add("Undo");
    menu.add("Redo");
    menu.addSeparator();
    menu.add("Cut");
    menu.add("Copy");
    menu.add("Paste");
    menu.add("Delete");

    menu = bar.add(new JMenu("Test"));
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    JMenu sub = new JMenu("JMenu");
    sub.add("JMenuItem4");
    sub.add("JMenuItem5");
    menu.add(sub);
    menu.add("JMenuItem3");

    bar.add(LookAndFeelUtils.createLookAndFeelMenu());

    return bar;
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
