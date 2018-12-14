// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JCheckBox check = new JCheckBox("Adjust JPopupMenu location", true);
    check.setFocusPainted(false);

    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        System.out.println(c.getClass().getName());
        if (check.isSelected()) {
          Point p = new Point(x, y);
          Rectangle r = c.getBounds();
          Dimension d = getPreferredSize();
          if (p.x + d.width > r.width) {
            p.x -= d.width;
          }
          if (p.y + d.height > r.height) {
            p.y -= d.height;
          }
          super.show(c, Math.max(p.x, 0), Math.max(p.y, 0));
        } else {
          super.show(c, x, y);
        }
      }
    };
    popup.add("aaa");
    popup.add("bbbbbb");
    popup.add("cc");
    // setComponentPopupMenu(popup);

    JLabel label = new JLabel("aaaaaaaaaaaa");
    label.setOpaque(true);
    // label.setInheritsPopupMenu(true);
    // check.setInheritsPopupMenu(true);
    label.setComponentPopupMenu(popup);

    add(check, BorderLayout.NORTH);
    add(label);
    setBorder(BorderFactory.createLineBorder(Color.RED, 10));
    setPreferredSize(new Dimension(320, 240));
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
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
