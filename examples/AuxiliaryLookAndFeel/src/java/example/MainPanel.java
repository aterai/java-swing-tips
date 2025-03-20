// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo = makeComboBox();
    UIManager.put("ComboBox.font", combo.getFont());
    String text = "<html>addAuxiliaryLookAndFeel<br>(Disable Right Click)";
    JCheckBox check = new JCheckBox(text);

    LookAndFeel auxLookAndFeel = new AuxiliaryWindowsLookAndFeel();
    UIManager.addPropertyChangeListener(e -> {
      if (Objects.equals("lookAndFeel", e.getPropertyName())) {
        if (isWindows(e.getNewValue())) {
          if (check.isSelected()) {
            UIManager.addAuxiliaryLookAndFeel(auxLookAndFeel);
          }
          check.setEnabled(true);
        } else {
          UIManager.removeAuxiliaryLookAndFeel(auxLookAndFeel);
          check.setEnabled(false);
        }
      }
    });
    check.addActionListener(e -> {
      String lnf = UIManager.getLookAndFeel().getName();
      if (((JCheckBox) e.getSource()).isSelected() && lnf.contains("Windows")) {
        UIManager.addAuxiliaryLookAndFeel(auxLookAndFeel);
      } else {
        UIManager.removeAuxiliaryLookAndFeel(auxLookAndFeel);
      }
      SwingUtilities.updateComponentTreeUI(getRootPane());
    });

    combo.setEditable(true);

    Box box = Box.createVerticalBox();
    box.add(check);
    box.add(Box.createVerticalStrut(5));
    box.add(combo);
    box.add(Box.createVerticalStrut(5));
    box.add(makeComboBox());
    box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean isWindows(Object info) {
    // boolean isWindows = Objects.equals("Windows", info);
    return info instanceof String && info.toString().contains("Windows");
  }

  private static JComboBox<String> makeComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("aaa aaa");
    model.addElement("aaa ab bb");
    model.addElement("aaa ab bb cc");
    model.addElement("1354123451234513512");
    model.addElement("bbb1");
    model.addElement("bbb12");
    return new JComboBox<>(model);
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
