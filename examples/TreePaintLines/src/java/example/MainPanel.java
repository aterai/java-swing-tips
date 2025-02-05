// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("Tree.paintLines", Boolean.FALSE);

    JTree tree = new JTree();
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }

    JCheckBox check = new JCheckBox("Tree.paintLines");
    check.addActionListener(e -> {
      UIManager.put("Tree.paintLines", ((JCheckBox) e.getSource()).isSelected());
      SwingUtilities.updateComponentTreeUI(tree);
    });

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(new JScrollPane(tree));
    p.add(new JScrollPane(new JTree()));

    add(check, BorderLayout.NORTH);
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
