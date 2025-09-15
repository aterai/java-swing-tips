// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea info = new JTextArea();
  private final JSpinner spinner = new JSpinner();

  private MainPanel() {
    super(new BorderLayout(5, 5));
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));
    box.add(spinner);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(makeCheckBox());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(info));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makeCheckBox() {
    String key = "Spinner.editorBorderPainted";
    JCheckBox check = new JCheckBox(key) {
      @Override public void updateUI() {
        super.updateUI();
        UIDefaults def = UIManager.getLookAndFeelDefaults();
        boolean b = def.getBoolean(key);
        Object o = def.get(key);
        String lnf = UIManager.getLookAndFeel().getClass().getName();
        String name = lnf.substring(lnf.lastIndexOf('.') + 1);
        info.append(String.format("%s: %s=%s%n", name, key, o == null ? null : b));
        setSelected(b);
        UIManager.put(key, b);
        SwingUtilities.updateComponentTreeUI(spinner);
      }
    };
    check.addActionListener(e -> {
      JCheckBox src = (JCheckBox) e.getSource();
      UIManager.put(key, src.isSelected());
      SwingUtilities.updateComponentTreeUI(spinner);
    });
    check.setOpaque(false);
    // Box box = Box.createHorizontalBox();
    // box.add(Box.createHorizontalGlue());
    // box.add(check);
    return check;
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
      Logger.getGlobal().severe(ex::getMessage);
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
        Logger.getGlobal().severe(ex::getMessage);
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
