// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    LayoutFocusTraversalPolicy ftp = new LayoutFocusTraversalPolicy();
    ftp.setImplicitDownCycleTraversal(false);
    setFocusCycleRoot(true);
    // setFocusTraversalPolicyProvider(true);
    setFocusTraversalPolicy(ftp);

    JCheckBox check = new JCheckBox("ImplicitDownCycleTraversal");
    check.addActionListener(e -> ftp.setImplicitDownCycleTraversal(((JCheckBox) e.getSource()).isSelected()));

    JPanel sub = new JPanel(new BorderLayout());
    sub.setBorder(BorderFactory.createTitledBorder("sub panel"));
    JCheckBox checkFocusCycleRoot = new JCheckBox("sub.FocusCycleRoot", true);
    checkFocusCycleRoot.addActionListener(e -> sub.setFocusCycleRoot(((JCheckBox) e.getSource()).isSelected()));
    sub.setFocusCycleRoot(true);
    sub.add(new JScrollPane(new JTextArea("dummy")));
    sub.add(checkFocusCycleRoot, BorderLayout.SOUTH);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JButton("JButton1"));
    box.add(new JButton("JButton2"));

    add(check, BorderLayout.NORTH);
    add(sub);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
