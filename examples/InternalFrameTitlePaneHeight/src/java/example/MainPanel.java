// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    addFrame(desktop, 0);
    addFrame(desktop, 1);

    // InternalFrame.titleFont
    // InternalFrame.titleButtonHeight
    // InternalFrame.titleButtonWidth
    // InternalFrame.titlePaneHeight
    String key = "InternalFrame.titlePaneHeight";
    int height = UIManager.getLookAndFeelDefaults().getInt(key);
    SpinnerNumberModel model = new SpinnerNumberModel(height, 0, 50, 1);
    model.addChangeListener(e -> {
      int v = model.getNumber().intValue();
      UIManager.put(key, v);
      // UIManager.put("InternalFrame.titleButtonWidth", v);
      // UIManager.put("InternalFrame.titleButtonHeight", v);
      SwingUtilities.updateComponentTreeUI(desktop);
    });
    JSpinner spinner = new JSpinner(model) {
      @Override public void updateUI() {
        super.updateUI();
        int h = UIManager.getLookAndFeelDefaults().getInt(key);
        UIManager.put(key, h);
        model.setValue(h);
        SwingUtilities.updateComponentTreeUI(desktop);
      }
    };

    EventQueue.invokeLater(() -> {
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
      menuBar.add(spinner);
      getRootPane().setJMenuBar(menuBar);
    });

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addFrame(JDesktopPane desktop, int idx) {
    JInternalFrame frame = new JInternalFrame("JInternalFrame", true, true, true, true);
    frame.add(makePanel());
    frame.setSize(240, 100);
    frame.setLocation(10 + 60 * idx, 5 + 105 * idx);
    desktop.add(frame);
    EventQueue.invokeLater(() -> frame.setVisible(true));
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
