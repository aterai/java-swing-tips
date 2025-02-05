// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    // System.out.println(UIManager.get("CheckBoxMenuItem.checkIconOffset"));
    // System.out.println(UIManager.get("CheckBoxMenuItem.afterCheckIconGap"));
    // System.out.println(UIManager.get("CheckBoxMenuItem.minimumTextOffset"));
    // System.out.println(UIManager.get("CheckBoxMenuItem.evenHeight"));

    // UIManager.put("MenuItem.checkIconOffset", 20);
    // UIManager.put("MenuItem.afterCheckIconGap", 20);
    UIManager.put("MenuItem.minimumTextOffset", 20 + 20 + 31 - 9);

    UIManager.put("CheckBoxMenuItem.afterCheckIconGap", 20);
    UIManager.put("CheckBoxMenuItem.checkIconOffset", 20);
    // UIManager.put("CheckBoxMenuItem.minimumTextOffset", 100);

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = makeMenu("JMenu");
    menuBar.add(menu);

    menu.add(makeMenu("JMenu 1"));
    menu.add(makeMenu("JMenu 2"));

    menuBar.add(menu);
    menuBar.add(makeMenu("JMenu 3"));
    return menuBar;
  }

  private static JMenu makeMenu(String title) {
    JMenu menu = new JMenu(title);
    menu.add(new JMenuItem("JMenuItem 1"));
    menu.add(new JMenuItem("JMenuItem 2"));
    menu.add(new JCheckBoxMenuItem("JCheckBoxMenuItem 1"));
    menu.add(new JCheckBoxMenuItem("JCheckBoxMenuItem 2"));

    JRadioButtonMenuItem rbmi1 = new JRadioButtonMenuItem("JRadioButtonMenuItem 1");
    JRadioButtonMenuItem rbmi2 = new JRadioButtonMenuItem("JRadioButtonMenuItem 2");
    ButtonGroup bg = new ButtonGroup();
    bg.add(rbmi1);
    bg.add(rbmi2);
    menu.add(rbmi1);
    menu.add(rbmi2);
    return menu;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
