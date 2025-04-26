// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.plaf.basic.DefaultMenuLayout;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar mb = new JMenuBar();
    makeMenu(mb.add(new JMenu("Default")));
    // JMenu menu0 = makeMenu(mb.add(new JMenu("Default")));
    // Dimension d = menu0.getPopupMenu().getPreferredSize();
    // d.width = 200;
    // menu0.getPopupMenu().setPreferredSize(d);

    JMenu menu1 = makeMenu(mb.add(new JMenu("BoxHStrut")));
    menu1.add(Box.createHorizontalStrut(200));

    JMenu menu2 = makeMenu(mb.add(new JMenu("Override")));
    menu2.add(new JMenuItem("PreferredSize") {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 200;
        return d;
      }
    });

    JMenu menu3 = makeMenu(mb.add(new JMenu("Layout")));
    JPopupMenu popup = menu3.getPopupMenu();
    popup.setLayout(new DefaultMenuLayout(popup, BoxLayout.Y_AXIS) {
      @Override public Dimension preferredLayoutSize(Container target) {
        Dimension d = super.preferredLayoutSize(target);
        d.width = Math.max(200, d.width);
        return d;
      }
    });

    JMenu menu4 = mb.add(new JMenu("Html"));
    String s = "<html><table cellpadding='0' cellspacing='0' style='width:200'>Table";
    JMenuItem item = menu4.add(s);
    item.setMnemonic(KeyEvent.VK_T);
    // item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
    makeMenu(menu4);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private JMenu makeMenu(JMenu menu) {
    menu.add("Open").setMnemonic(KeyEvent.VK_O);
    menu.addSeparator();
    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    menu.add("Exit").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, modifiers));
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
