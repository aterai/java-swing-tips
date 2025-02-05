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
    JTextArea log = new JTextArea() {
      @Override public void updateUI() {
        UIManager.put(KEY, null);
        super.updateUI();
        boolean b = UIManager.getLookAndFeelDefaults().getBoolean(KEY);
        EventQueue.invokeLater(() -> setText(KEY + ": " + b));
      }
    };

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
