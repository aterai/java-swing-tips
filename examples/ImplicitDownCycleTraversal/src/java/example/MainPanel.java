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

    JCheckBox check1 = new JCheckBox("ImplicitDownCycleTraversal");
    check1.addActionListener(e -> ftp.setImplicitDownCycleTraversal(check1.isSelected()));

    JPanel sub = new JPanel(new BorderLayout());
    sub.setBorder(BorderFactory.createTitledBorder("sub panel"));
    JCheckBox check2 = new JCheckBox("sub.FocusCycleRoot", true);
    check2.addActionListener(e -> sub.setFocusCycleRoot(check2.isSelected()));
    sub.setFocusCycleRoot(true);
    sub.add(new JScrollPane(new JTextArea("JTextArea")));
    sub.add(check2, BorderLayout.SOUTH);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JButton("JButton1"));
    box.add(new JButton("JButton2"));

    add(check1, BorderLayout.NORTH);
    add(sub);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
