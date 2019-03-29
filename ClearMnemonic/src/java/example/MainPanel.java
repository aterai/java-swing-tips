// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();

    JTextField textField = new JTextField("B");
    JButton button = new JButton("Button");
    button.addActionListener(e -> Toolkit.getDefaultToolkit().beep());

    JButton btnSetMnemonic = new JButton("setMnemonic(...)");
    btnSetMnemonic.addActionListener(e -> {
      String str = textField.getText().trim();
      if (str.isEmpty()) {
        str = button.getText();
      }
      // button.setMnemonic(str.charAt(0));
      button.setMnemonic(str.codePointAt(0));
    });
    JButton btnClearMnemonic = new JButton("clear Mnemonic");
    btnClearMnemonic.addActionListener(e -> {
      button.setMnemonic(0);
      // button.setMnemonic('\u0000');
      // button.setMnemonic('\0');
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("setMnemonic"));
    p.add(textField);
    p.add(btnSetMnemonic);
    p.add(btnClearMnemonic);

    add(button);
    add(p);
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
