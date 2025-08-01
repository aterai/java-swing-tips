// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JTextField textField = new JTextField("B", 1);
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
    btnClearMnemonic.addActionListener(e -> button.setMnemonic(0));
    // btnClearMnemonic.addActionListener(e -> button.setMnemonic('\u0000'));
    // btnClearMnemonic.addActionListener(e -> button.setMnemonic('\0'));

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("setMnemonic"));
    p.add(textField);
    p.add(btnSetMnemonic);
    p.add(btnClearMnemonic);

    add(button);
    add(p);
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
      Logger.getGlobal().severe(ex::getMessage);
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
