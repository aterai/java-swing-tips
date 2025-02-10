// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar menuBar = new JMenuBar() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        // setLayout(new GridLayout(0, 1, 2, 2));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      }
    };
    initMenuBar(menuBar);
    MenuListener listener = new VerticalMenuListener();
    for (MenuElement m : menuBar.getSubElements()) {
      if (m instanceof JMenu) {
        JMenu menu = (JMenu) m;
        menu.addMenuListener(listener);
        Dimension d = menu.getMaximumSize();
        d.width = Short.MAX_VALUE;
        menu.setMaximumSize(d);
      }
    }

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JPanel p = new JPanel(new BorderLayout());
    p.add(menuBar, BorderLayout.NORTH);

    // JMenuBar empty = new JMenuBar();
    // empty.add(new JMenu());
    // p.add(empty);
    // p.add(new JMenuBar());

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setLeftComponent(p);
    split.setRightComponent(new JScrollPane(new JTree()));
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initMenuBar(JMenuBar menuBar) {
    JMenu menu1 = new JMenu("question");
    menu1.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
    menuBar.add(menu1);
    JMenuItem item1 = new JMenuItem("warning", UIManager.getIcon("OptionPane.warningIcon"));
    menu1.add(item1);
    JMenuItem item2 = new JMenuItem("error", UIManager.getIcon("OptionPane.errorIcon"));
    menu1.add(item2);

    JMenu menu2 = new JMenu("warning");
    menu2.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
    menuBar.add(menu2);

    JMenu menu3 = new JMenu("error");
    menu3.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
    menuBar.add(menu3);

    JMenu menu4 = new JMenu("information");
    menu4.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
    menuBar.add(menu4);
    menu4.add(makeSubMenu());

    menuBar.add(new JSeparator(SwingConstants.HORIZONTAL));
    menuBar.add(makeSubMenu());
  }

  private static JMenu makeSubMenu() {
    JMenu sub1 = new JMenu("JMenu1");
    sub1.add("MenuItem1");
    sub1.add("MenuItem2");
    JMenu sub2 = new JMenu("JMenu2");
    sub2.add("MenuItem3");
    sub2.add("MenuItem4");
    sub1.add(sub2);
    return sub1;
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

class VerticalMenuListener implements MenuListener {
  @Override public void menuSelected(MenuEvent e) {
    Object src = e.getSource();
    if (src instanceof JMenu && ((JMenu) src).isTopLevelMenu()) {
      EventQueue.invokeLater(() -> {
        JMenu menu = (JMenu) src;
        Point loc = menu.getLocationOnScreen();
        loc.x += menu.getWidth();
        menu.getPopupMenu().setLocation(loc);
      });
    }
  }

  @Override public void menuDeselected(MenuEvent e) {
    /* Do nothing */
  }

  @Override public void menuCanceled(MenuEvent e) {
    /* Do nothing */
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
