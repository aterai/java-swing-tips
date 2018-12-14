// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    JLabel label1 = new JLabel("Mail Adress:", SwingConstants.RIGHT);
    label1.setDisplayedMnemonic('M');
    Component textField1 = new JTextField(12);
    label1.setLabelFor(textField1);
    addRow(label1, textField1, p, c);

    JLabel label2 = new JLabel("Password:", SwingConstants.RIGHT);
    label2.setDisplayedMnemonic('P');
    Component textField2 = new JPasswordField(12);
    label2.setLabelFor(textField2);
    addRow(label2, textField2, p, c);

    JLabel label3 = new JLabel("Dummy:", SwingConstants.RIGHT);
    Component textField3 = new JTextField(12);
    addRow(label3, textField3, p, c);

    JLabel label4 = new JLabel("ComboBox:", SwingConstants.RIGHT);
    label4.setDisplayedMnemonic('C');
    Component comboBox = new JComboBox<String>();
    addRow(label4, comboBox, p, c);

    JButton button = new JButton("JComboBox#requestFocusInWindow() Test");
    button.addActionListener(e -> comboBox.requestFocusInWindow());

    add(button, BorderLayout.SOUTH);
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addRow(Component c1, Component c2, Container p, GridBagConstraints c) {
    c.gridx = 0;
    c.weightx = 0d;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.EAST;
    p.add(c1, c);

    c.gridx = 1;
    c.weightx = 1d;
    c.insets = new Insets(5, 5, 5, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(c2, c);
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
