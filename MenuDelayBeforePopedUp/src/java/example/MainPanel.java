// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JPopupMenu popup = new JPopupMenu();
    JMenu sub0 = new JMenu("JMenu(Default)");
    sub0.add("JMenuItem:0");
    sub0.add("JMenuItem:1");
    popup.add(sub0);

    JMenu sub1 = makeMenu("JMenu(0..2000)", 2000);
    sub1.add("JMenuItem:2");
    sub1.add("JMenuItem:3");
    popup.add(sub1);
    setComponentPopupMenu(popup);

    SpinnerNumberModel model = new SpinnerNumberModel(2000, 0, 2000, 100);
    JSpinner spinner = new JSpinner(model);
    model.addChangeListener(e -> sub1.setDelay(model.getNumber().intValue()));
    add(spinner);

    JMenuBar mb = new JMenuBar();
    mb.add(makeTopLevelMenu());
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenu makeTopLevelMenu() {
    JMenu menu = new JMenu("JMenu#setDelay(...)");
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");

    JMenu sub = new JMenu("JMenu(Default)");
    sub.add("JMenuItem4");
    sub.add("JMenuItem5");
    menu.add(sub);

    JMenu sub0 = makeMenu("JMenu(0)", 0);
    sub0.add("JMenuItem6");
    sub0.add("JMenuItem7");
    menu.add(sub0);

    JMenu sub1 = makeMenu("JMenu(2000)", 2000);
    sub1.add("JMenuItem8");
    sub1.add("JMenuItem9");
    menu.add(sub1);

    JMenu sub2 = makeMenu("JMenu(500)", 500);
    sub2.add("JMenuItem10");
    sub2.add("JMenuItem11");
    menu.add(sub2);

    menu.add("JMenuItem3");
    return menu;
  }

  private static JMenu makeMenu(String title, int delay) {
    JMenu menu = new JMenu(title);
    menu.setDelay(delay);
    return menu;
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
