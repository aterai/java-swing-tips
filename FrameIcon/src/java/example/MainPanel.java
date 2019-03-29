// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Image icon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("16x16.png"));
    JCheckBox check = new JCheckBox("setIconImage");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      Container w = c.getTopLevelAncestor();
      if (w instanceof Window) {
        ((Window) w).setIconImage(c.isSelected() ? icon : null);
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("Window#setIconImage(Image)"));
    p.add(check);
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
