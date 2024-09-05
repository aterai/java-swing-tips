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
    help.setAlignmentX(LEFT_ALIGNMENT);

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
    combo.setAlignmentX(LEFT_ALIGNMENT);

    JCheckBox check2 = new JCheckBox("isEditable");
    check2.setFocusable(false);
    check2.addActionListener(e -> combo.setEditable(check2.isSelected()));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
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
    DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
    IntStream.range(0, 10).mapToObj(i -> "item: " + i).forEach(m::addElement);
    return m;
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
