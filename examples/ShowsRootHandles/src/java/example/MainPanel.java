// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree1 = new JTree();
    tree1.setShowsRootHandles(true);

    JTree tree2 = new JTree();
    tree2.setShowsRootHandles(false);

    JCheckBox check = new JCheckBox("setRootVisible", true);
    check.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      tree1.setRootVisible(flg);
      tree2.setRootVisible(flg);
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
