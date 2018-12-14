// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private enum TreeDraws {
    DRAWS_FOCUS_BORDER_AROUND_ICON("Tree.drawsFocusBorderAroundIcon"),
    DRAW_DASHED_FOCUS_INDICATOR("Tree.drawDashedFocusIndicator");
    private final String key;
    /* default */ TreeDraws(String key) {
      this.key = key;
    }

    @Override public String toString() {
      return key;
    }
  }

  private final JCheckBox dfbaiCheck = new JCheckBox(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString());
  private final JCheckBox ddfiCheck = new JCheckBox(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString());

  private MainPanel() {
    super(new BorderLayout());

    JTree tree = new JTree();

    dfbaiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString()));
    dfbaiCheck.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      UIManager.put(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString(), b);
      SwingUtilities.updateComponentTreeUI(tree);
    });

    ddfiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString()));
    ddfiCheck.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      UIManager.put(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString(), b);
      SwingUtilities.updateComponentTreeUI(tree);
    });

    JPanel np = new JPanel(new GridLayout(2, 1));
    np.add(dfbaiCheck);
    np.add(ddfiCheck);

    add(np, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    if (Objects.nonNull(dfbaiCheck)) {
      dfbaiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString()));
    }
    if (Objects.nonNull(ddfiCheck)) {
      ddfiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString()));
    }
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
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
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
    ButtonGroup lafRadioGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
    }
    return menu;
  }

  private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafRadioGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
      }
    });
    lafRadioGroup.add(lafItem);
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
