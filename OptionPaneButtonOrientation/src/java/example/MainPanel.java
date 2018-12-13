// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();
    // TEST: UIManager.put("OptionPane.isYesLast", Boolean.TRUE);

    String key = "OptionPane.buttonOrientation";

    JButton defaultButton = new JButton("Default");
    defaultButton.addActionListener(e -> {
      Integer iv = UIManager.getLookAndFeelDefaults().getInt(key);
      System.out.println(iv);
      UIManager.put(key, iv);
      String str = JOptionPane.showInputDialog(getRootPane(), "Default");
      log.setText(str);
    });

    JButton rightButton = new JButton("RIGHT");
    rightButton.addActionListener(e -> {
      UIManager.put(key, SwingConstants.RIGHT);
      String str = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: RIGHT");
      log.setText(str);
    });

    JButton centerButton = new JButton("CENTER");
    centerButton.addActionListener(e -> {
      UIManager.put(key, SwingConstants.CENTER);
      String str = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: CENTER");
      log.setText(str);
    });

    JButton leftButton = new JButton("LEFT");
    leftButton.addActionListener(e -> {
      UIManager.put(key, SwingConstants.LEFT);
      String str = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: LEFT");
      log.setText(str);
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
    p.add(defaultButton);
    p.add(rightButton);
    p.add(centerButton);
    p.add(leftButton);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    UIManager.put("OptionPane.buttonOrientation", null);
    super.updateUI();
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
