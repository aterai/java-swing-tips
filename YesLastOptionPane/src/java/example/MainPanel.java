// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String KEY = "OptionPane.isYesLast";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JButton defaultButton = new JButton(KEY + ": false(default)");
    defaultButton.addActionListener(e -> {
      UIManager.put(KEY, Boolean.FALSE);
      String str = JOptionPane.showInputDialog(getRootPane(), KEY + ": false");
      log.setText(str);
    });

    JButton yesLastButton = new JButton(KEY + ": true");
    yesLastButton.addActionListener(e -> {
      UIManager.put(KEY, Boolean.TRUE);
      String str = JOptionPane.showInputDialog(getRootPane(), KEY + ": true");
      log.setText(str);
    });

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
    p.add(defaultButton);
    p.add(yesLastButton);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    UIManager.put(KEY, null);
    super.updateUI();
    Boolean b = UIManager.getLookAndFeelDefaults().getBoolean(KEY);
    System.out.println(KEY + ": " + b);
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
