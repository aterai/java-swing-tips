// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JSplitPane sp = new JSplitPane();
    sp.setLeftComponent(new JScrollPane(new JTree()));
    sp.setRightComponent(new JScrollPane(new JTable(6, 3)));
    sp.setResizeWeight(.4);

    JCheckBox check = new JCheckBox("Keep DividerLocation", true);

    JButton button = new JButton("swap");
    button.setFocusable(false);
    button.addActionListener(e -> {
      Component left = sp.getLeftComponent();
      Component right = sp.getRightComponent();

      // sp.removeAll(); // Divider is also removed
      sp.remove(left);
      sp.remove(right);
      // or:
      // sp.setLeftComponent(null);
      // sp.setRightComponent(null);

      sp.setLeftComponent(right);
      sp.setRightComponent(left);

      sp.setResizeWeight(1d - sp.getResizeWeight());
      if (check.isSelected()) {
        sp.setDividerLocation(sp.getDividerLocation());
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(sp);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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
