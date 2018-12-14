// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenu makeSubMenu(String title) {
    JMenu menu = new JMenu(title);
    menu.add("Item 1");
    menu.add("Item 2");
    menu.add("Item 3");
    // // NimbusLookAndFeel
    // UIDefaults d = new UIDefaults();
    // d.put("Menu.arrowIcon", new ArrowIcon());
    // menu.putClientProperty("Nimbus.Overrides", d);
    // menu.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    return menu;
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
      // // NimbusLookAndFeel
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      // UIManager.getLookAndFeelDefaults().put("Menu.arrowIcon", new ArrowIcon());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(new JMenu("Menu 1")).add(makeSubMenu("SubMenu 1"));
    UIManager.put("Menu.arrowIcon", new ArrowIcon());
    menuBar.add(new JMenu("Menu 2")).add(makeSubMenu("SubMenu 2"));

    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(menuBar);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    // // NimbusLookAndFeel
    // Container parent = SwingUtilities.getUnwrappedParent(c);
    // if (parent instanceof JMenuBar) {
    //   return;
    // }

    Graphics2D g2 = (Graphics2D) g.create();
    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isSelected()) {
      g2.setPaint(Color.WHITE);
    } else {
      g2.setPaint(Color.GRAY);
    }

    int w = getIconWidth() / 2;
    Path2D p = new Path2D.Double();
    p.moveTo(0, 0);
    p.lineTo(w, w);
    p.lineTo(0, getIconHeight());
    p.closePath();

    g2.translate(x, y);
    g2.fill(p);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 8;
  }

  @Override public int getIconHeight() {
    return 8;
  }
}
