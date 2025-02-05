// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane s1 = new JScrollPane(new JTree());
    JScrollPane s2 = new JScrollPane(new JTable(6, 3));

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    split.setResizeWeight(.4);

    JCheckBox check = new JCheckBox("Keep DividerLocation", true);

    JButton button = new JButton("swap");
    button.setFocusable(false);
    button.addActionListener(e -> {
      Component left = split.getLeftComponent();
      Component right = split.getRightComponent();

      // split.removeAll(); // Divider is also removed
      split.remove(left);
      split.remove(right);
      // or:
      // split.setLeftComponent(null);
      // split.setRightComponent(null);

      split.setLeftComponent(right);
      split.setRightComponent(left);

      split.setResizeWeight(1d - split.getResizeWeight());
      if (check.isSelected()) {
        split.setDividerLocation(split.getDividerLocation());
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(split);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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
