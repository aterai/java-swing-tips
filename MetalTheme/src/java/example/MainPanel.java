// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.plaf.synth.SynthInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    addFrame(desktop, 0);
    addFrame(desktop, 1);

    EventQueue.invokeLater(() -> {
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
      getRootPane().setJMenuBar(menuBar);
    });

    // UIManager.put("OptionPane.errorDialog.titlePane.background", Color.GREEN);
    // UIManager.put("OptionPane.errorDialog.titlePane.foreground", Color.RED);
    // UIManager.put("OptionPane.errorDialog.titlePane.shadow", Color.BLUE);
    // JOptionPane.showInternalMessageDialog(
    //     desktop, "alert", "alert", JOptionPane.ERROR_MESSAGE);

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addFrame(JDesktopPane desktop, int idx) {
    JInternalFrame frame = new JInternalFrame("JInternalFrame", true, true, true, true) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof SynthInternalFrameUI) {
          // UIDefaults d1 = new UIDefaults();
          // d1.put("InternalFrameTitlePane.disabled", Color.RED.darker());
          // d1.put("InternalFrameTitlePane.foreground", Color.RED);
          // putClientProperty("Nimbus.Overrides", d1);
          JComponent titleBar = ((BasicInternalFrameUI) getUI()).getNorthPane();
          UIDefaults d = new UIDefaults();
          d.put("InternalFrame:InternalFrameTitlePane[Enabled].textForeground", Color.GREEN);
          // d.put(
          //   "InternalFrame:InternalFrameTitlePane[Enabled+WindowNotFocused].textForeground",
          //   red
          // );
          titleBar.putClientProperty("Nimbus.Overrides", d);
        }
      }
    };
    frame.add(makePanel());
    frame.setSize(240, 100);
    frame.setLocation(10 + 60 * idx, 5 + 105 * idx);
    desktop.add(frame);
    EventQueue.invokeLater(() -> frame.setVisible(true));
    // desktop.getDesktopManager().activateFrame(frame);
  }

  private static Component makePanel() {
    JPanel p = new JPanel();
    p.add(new JLabel("label"));
    p.add(new JButton("button"));
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // BasicLookAndFeel, WindowsLookAndFeel
    UIManager.put("InternalFrame.activeTitleForeground", Color.RED);
    UIManager.put("InternalFrame.inactiveTitleForeground", Color.WHITE);
    // MetalLookAndFeel
    MetalLookAndFeel.setCurrentTheme(new OceanTheme() {
      @Override public ColorUIResource getWindowTitleForeground() {
        return new ColorUIResource(Color.RED.brighter()); // getBlack();
      }

      @Override public ColorUIResource getWindowTitleInactiveForeground() {
        return new ColorUIResource(Color.ORANGE.darker()); // getBlack();
      }
    });
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
