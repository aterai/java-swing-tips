// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
// import static javax.swing.GroupLayout.Alignment;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    // GroupLayout
    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("GroupLayout"));
    GroupLayout layout = new GroupLayout(p1);
    p1.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    JTextField tf1 = new JTextField();
    JTextField tf2 = new JTextField();
    JLabel label1 = new JLabel("0123456789_0123456789abc:");
    JLabel label2 = new JLabel("GroupLayout:");

    GroupLayout.SequentialGroup hgp = layout.createSequentialGroup();
    hgp.addGroup(layout.createParallelGroup()
        .addComponent(label1)
        .addComponent(label2));
    hgp.addGroup(layout.createParallelGroup()
        .addComponent(tf1)
        .addComponent(tf2));
    layout.setHorizontalGroup(hgp);

    GroupLayout.SequentialGroup vgp = layout.createSequentialGroup();
    vgp.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(label1)
        .addComponent(tf1));
    vgp.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(label2)
        .addComponent(tf2));
    layout.setVerticalGroup(vgp);

    // GridBagLayout
    JPanel p2 = new JPanel(new GridBagLayout());
    Border inside = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
    Border outside = BorderFactory.createTitledBorder("GridBagLayout");
    p2.setBorder(BorderFactory.createCompoundBorder(outside, inside));
    GridBagConstraints c = new GridBagConstraints();

    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_START;
    c.gridx = 0;

    JLabel label3 = new JLabel("0123456789_0123456789abc:");
    JLabel label4 = new JLabel("GridBagLayout:");
    p2.add(label3, c);
    p2.add(label4, c);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1d;
    c.gridx = 1;

    JTextField tf3 = new JTextField();
    JTextField tf4 = new JTextField();
    p2.add(tf3, c);
    p2.add(tf4, c);

    add(p1);
    add(p2);
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
