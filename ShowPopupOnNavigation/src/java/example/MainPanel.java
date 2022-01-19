// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String SHOW_POPUP_NAVI = "ComboBox.showPopupOnNavigation";

  private MainPanel() {
    super(new BorderLayout());
    JLabel help = new JLabel("This setting only responds to the upwards arrow key↑");
    help.setAlignmentX(0f);

    JCheckBox check1 = new JCheckBox(SHOW_POPUP_NAVI);
    check1.setFocusable(false);
    check1.addActionListener(e -> UIManager.put(SHOW_POPUP_NAVI, check1.isSelected()));

    JComboBox<String> combo = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        boolean flg = UIManager.getLookAndFeelDefaults().getBoolean(SHOW_POPUP_NAVI);
        // System.out.println(flg);
        UIManager.put(SHOW_POPUP_NAVI, flg);
        check1.setSelected(flg);
      }
    };
    combo.setSelectedIndex(5);
    combo.setAlignmentX(0f);

    JCheckBox check2 = new JCheckBox("isEditable");
    check2.setFocusable(false);
    check2.addActionListener(e -> combo.setEditable(check2.isSelected()));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    Box box = Box.createVerticalBox();
    box.add(help);
    box.add(Box.createVerticalStrut(5));
    box.add(check1);
    box.add(Box.createVerticalStrut(5));
    box.add(check2);
    box.add(Box.createVerticalStrut(15));
    box.add(combo);
    box.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    IntStream.range(0, 10).forEach(i -> model.addElement("item: " + i));
    return model;
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
