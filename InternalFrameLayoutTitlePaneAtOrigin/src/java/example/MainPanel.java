// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String key = "InternalFrame.layoutTitlePaneAtOrigin";
    JDesktopPane desktop = new JDesktopPane();
    boolean b = UIManager.getBoolean(key);
    JCheckBox check = new JCheckBox("InternalFrame TitlePane layout", b) {
      @Override public void updateUI() {
        super.updateUI();
        boolean b = UIManager.getLookAndFeelDefaults().getBoolean(key);
        setSelected(b);
        UIManager.put(key, b);
        SwingUtilities.updateComponentTreeUI(desktop);
      }
    };
    check.addActionListener(e -> {
      UIManager.put(key, ((JCheckBox) e.getSource()).isSelected());
      SwingUtilities.updateComponentTreeUI(desktop);
    });
    check.setOpaque(false);

    addFrame(desktop, 0, true);
    addFrame(desktop, 1, false);
    add(desktop);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    mb.add(check);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  private static void addFrame(JDesktopPane desktop, int idx, boolean resizable) {
    JInternalFrame f = new JInternalFrame("resizable: " + resizable, resizable, true, true, true);
    f.add(makePanel());
    f.setSize(240, 100);
    f.setLocation(10 + 60 * idx, 10 + 120 * idx);
    EventQueue.invokeLater(() -> f.setVisible(true));
    desktop.add(f);
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
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
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
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
