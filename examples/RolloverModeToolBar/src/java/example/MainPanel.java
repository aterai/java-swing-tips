// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = new JToolBar();
    // System.out.println(toolBar.isRollover());
    // EventQueue.invokeLater(() -> System.out.println(toolBar.isRollover()));

    JToggleButton tg1 = new JToggleButton("Tg1");
    JToggleButton tg2 = new JToggleButton("Tg2");
    JToggleButton tg3 = new JToggleButton("Tg3");
    JButton button = new JButton("Button");
    JRadioButton radio = new JRadioButton("RadioButton");

    Dimension d = new Dimension(2, 2);
    ButtonGroup bg = new ButtonGroup();
    Stream.of(tg1, tg2, tg3, button, radio).forEach(b -> {
      b.setFocusPainted(false);
      // b.setRolloverEnabled(false);
      // b.setContentAreaFilled(false);
      toolBar.add(b);
      toolBar.add(Box.createRigidArea(d));
      bg.add(b);
    });

    JCheckBox check = new JCheckBox("setRollover");
    check.addActionListener(e -> toolBar.setRollover(check.isSelected()));
    toolBar.add(Box.createGlue());
    toolBar.add(check);

    Box box = Box.createHorizontalBox();
    box.add(new JLabel("setRolloverEnabled(false)"));
    Stream.of(new JToggleButton("ToggleButton"), new JButton("Button")).forEach(b -> {
      // b.setFocusPainted(false);
      b.setRolloverEnabled(false);
      // b.setContentAreaFilled(false);
      box.add(b);
      box.add(Box.createRigidArea(d));
    });

    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
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
