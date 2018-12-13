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

    JToolBar toolbar = new JToolBar();
    System.out.println(toolbar.isRollover());
    EventQueue.invokeLater(() -> System.out.println(toolbar.isRollover()));
    // toolbar.setRollover(true);

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
      toolbar.add(b);
      toolbar.add(Box.createRigidArea(d));
      bg.add(b);
    });

    JCheckBox check = new JCheckBox("setRollover");
    check.addActionListener(e -> toolbar.setRollover(((AbstractButton) e.getSource()).isSelected()));
    toolbar.add(Box.createGlue());
    toolbar.add(check);

    Box box = Box.createHorizontalBox();
    box.add(new JLabel("setRolloverEnabled(false)"));
    Stream.of(new JToggleButton("ToggleButton"), new JButton("Button")).forEach(b -> {
      // b.setFocusPainted(false);
      b.setRolloverEnabled(false);
      // b.setContentAreaFilled(false);
      box.add(b);
      box.add(Box.createRigidArea(d));
    });

    add(toolbar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    add(box, BorderLayout.SOUTH);
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
