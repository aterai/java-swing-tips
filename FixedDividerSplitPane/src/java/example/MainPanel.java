// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(new JTree()));
    sp.setBottomComponent(new JScrollPane(new JTextArea()));
    sp.setOneTouchExpandable(true);

    JCheckBox check1 = new JCheckBox("setEnabled(...)", true);
    check1.addActionListener(e -> sp.setEnabled(((JCheckBox) e.getSource()).isSelected()));

    int dividerSize = UIManager.getInt("SplitPane.dividerSize");
    JCheckBox check2 = new JCheckBox("setDividerSize(0)");
    check2.addActionListener(e -> sp.setDividerSize(((JCheckBox) e.getSource()).isSelected() ? 0 : dividerSize));

    // // TEST:
    // Component divider = ((BasicSplitPaneUI) sp.getUI()).getDivider();
    // JCheckBox check3 = new JCheckBox("Divider#setEnabled(...)", true);
    // check3.addActionListener(e -> divider.setEnabled(((JCheckBox) e.getSource()).isSelected()));

    JPanel p = new JPanel(new GridLayout(1, 0));
    p.setBorder(BorderFactory.createTitledBorder("JSplitPane"));
    p.add(check1);
    p.add(check2);

    add(p, BorderLayout.NORTH);
    add(sp);
    setPreferredSize(new Dimension(320, 240));

    EventQueue.invokeLater(() -> sp.setDividerLocation(.5));
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
