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
    JPopupMenu popup = makePopupMenu(check);
    // setComponentPopupMenu(popup);

    JLabel label = new JLabel("JLabel: 1234567890");
    label.setOpaque(true);
    // label.setInheritsPopupMenu(true);
    // check.setInheritsPopupMenu(true);
    label.setComponentPopupMenu(popup);

    add(check, BorderLayout.NORTH);
    add(label);
    setBorder(BorderFactory.createLineBorder(Color.RED, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopupMenu(JCheckBox check) {
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
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
    popup.add("JMenuItem: 11111");
    popup.add("JMenuItem: 222");
    popup.add("JMenuItem: 3");
    return popup;
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
