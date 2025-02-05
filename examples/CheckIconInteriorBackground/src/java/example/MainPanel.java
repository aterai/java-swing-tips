// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("CheckBox.foreground", Color.RED);
    UIManager.put("RadioButton.foreground", Color.RED);
    UIManager.put("CheckBox.background", Color.GREEN);
    UIManager.put("RadioButton.background", Color.GREEN);
    UIManager.put("CheckBox.interiorBackground", Color.BLUE);
    UIManager.put("RadioButton.interiorBackground", Color.BLUE);

    JCheckBox check1 = new JCheckBox("JCheckBox1", true);
    JCheckBox check2 = new JCheckBox("JCheckBox2");
    JCheckBox check3 = new JCheckBox("JCheckBox3");
    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Arrays.asList(check1, check2, check3).forEach(c -> {
      c.setOpaque(false);
      box1.add(c);
      box1.add(Box.createVerticalStrut(5));
    });

    JRadioButton radio1 = new JRadioButton("JRadioButton1", true);
    JRadioButton radio2 = new JRadioButton("JRadioButton2");
    JRadioButton radio3 = new JRadioButton("JRadioButton3");
    ButtonGroup group = new ButtonGroup();
    Box box2 = Box.createHorizontalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Arrays.asList(radio1, radio2, radio3).forEach(c -> {
      c.setOpaque(false);
      group.add(c);
      box2.add(c);
      box2.add(Box.createVerticalStrut(5));
    });

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.add(box1);
    p.add(box2);
    add(p, BorderLayout.NORTH);

    JTextArea info = new JTextArea();
    info.append("CheckBox.foreground, Color.RED\n");
    info.append("CheckBox.background, Color.GREEN\n");
    info.append("CheckBox.interiorBackground, Color.BLUE\n");
    info.append("RadioButton.foreground, Color.RED\n");
    info.append("RadioButton.background, Color.GREEN\n");
    info.append("RadioButton.interiorBackground, Color.BLUE\n");
    add(new JScrollPane(info));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setMinimumSize(new Dimension(256, 100));
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
