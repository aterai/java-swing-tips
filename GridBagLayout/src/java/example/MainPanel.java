// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int GAP = 5;

  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"000", "1111", "22222", "333333"};
    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(20));
    box.add(makeBorderLayoutPanel(new JComboBox<>(model), new JButton("Open")));
    box.add(Box.createVerticalStrut(20));
    box.add(makeGridBagLayoutPanel(new JComboBox<>(model), new JButton("Open")));
    box.add(Box.createVerticalStrut(20));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeBorderLayoutPanel(JComponent cmp, JButton btn) {
    JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
    panel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
    panel.add(new JLabel("BorderLayout:"), BorderLayout.WEST);
    panel.add(cmp);
    panel.add(btn, BorderLayout.EAST);
    Dimension d = panel.getPreferredSize();
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
    return panel;
  }

  public static Component makeGridBagLayoutPanel(JComponent cmp, JButton btn) {
    GridBagConstraints c = new GridBagConstraints();
    JPanel panel = new JPanel(new GridBagLayout());

    // c.gridheight = 1;
    // c.gridwidth = 1;
    // c.gridy = 0;

    // c.gridx = 0;
    // c.weightx = 0d;
    c.insets = new Insets(GAP, GAP, GAP, 0);
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(new JLabel("GridBagLayout:"), c);

    // c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(cmp, c);

    // c.gridx = 2;
    c.weightx = 0d;
    c.insets = new Insets(GAP, GAP, GAP, GAP);
    panel.add(btn, c);

    return panel;
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
    frame.setMinimumSize(new Dimension(300, 120));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
