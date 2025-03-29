// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel padding = new SpinnerNumberModel(0, 0, 50, 1);
  private final SpinnerNumberModel margin = new SpinnerNumberModel(15, 0, 50, 1);

  private MainPanel() {
    super(new BorderLayout());
    // System.out.println(UIManager.getInt("OptionPane.separatorPadding"));
    // System.out.println(UIManager.getInsets("OptionPane.contentMargins"));
    JPanel p = new JPanel();
    p.add(makeButton1());
    p.add(makeButton2());
    add(p);
    add(makeBox(), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JOptionPane makeOptionPane() {
    JLabel label = new JLabel("message1");
    label.setBorder(BorderFactory.createLineBorder(Color.RED));
    Component[] msg = {label, new JTextField("22"), new JButton("333")};
    return new JOptionPane(
        msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
  }

  private JButton makeButton1() {
    JOptionPane op = makeOptionPane();
    String title = "Default";
    JButton button = new JButton(title);
    button.addActionListener(e -> {
      // int m = margin.getNumber().intValue();
      // op.setBorder(BorderFactory.createLineBorder(Color.RED, m));
      JDialog dialog = op.createDialog(getRootPane(), title);
      dialog.setVisible(true);
    });
    return button;
  }

  private JButton makeButton2() {
    JOptionPane op = makeOptionPane();
    JButton button = new JButton("separatorPadding");
    button.addActionListener(e -> {
      UIDefaults d = new UIDefaults();
      int p = padding.getNumber().intValue();
      d.put("OptionPane.separatorPadding", p);
      int m = margin.getNumber().intValue();
      d.put("OptionPane.contentMargins", new InsetsUIResource(m, m, m, m));
      // Insets i = new InsetsUIResource(m, m, m, m);
      // d.put("OptionPane:\"OptionPane.separator\".contentMargins", i);
      // d.put("OptionPane:\"OptionPane.messageArea\".contentMargins", i);
      // d.put("OptionPane:\"OptionPane.messageArea\":\"OptionPane.label\".contentMargins", i);
      // d.put("OptionPane.buttonAreaBorder", BorderFactory.createMatteBorder(p, 0, 0, 0, bg));
      op.putClientProperty("Nimbus.Overrides", d);
      op.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
      SwingUtilities.updateComponentTreeUI(op);
      String t1 = "separatorPadding: " + p;
      String t2 = "contentMargins: " + m;
      JDialog dialog = op.createDialog(getRootPane(), t1 + " / " + t2);
      dialog.setVisible(true);
    });
    return button;
  }

  private Component makeBox() {
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.insets = new Insets(2, 2, 2, 0);
    c.anchor = GridBagConstraints.LINE_END;
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("OptionPane"));
    p.add(new JLabel("contentMargins:"), c);
    p.add(new JLabel("separatorPadding:"), c);
    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(new JSpinner(margin), c);
    p.add(new JSpinner(padding), c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
