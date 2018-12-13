// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTree tree1 = new JTree();
  private final JTree tree2 = new JTree();
  private final List<JTree> list = Arrays.asList(tree1, tree2);
  private final JCheckBox check = new JCheckBox("setRootVisible", true);

  private MainPanel() {
    super(new BorderLayout());

    tree1.setShowsRootHandles(true);
    tree2.setShowsRootHandles(false);

    check.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(tree -> tree.setRootVisible(flg));
    });

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(makeTitledPanel("setShowsRootHandles(true)", tree1));
    p.add(makeTitledPanel("setShowsRootHandles(false)", tree2));
    add(p);
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, JTree tree) {
    tree.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(new JScrollPane(tree));
    return p;
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
