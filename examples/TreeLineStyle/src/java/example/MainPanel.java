// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    String key = "JTree.lineStyle";

    JTree tree0 = new JTree();
    tree0.putClientProperty(key, "Angled");

    JTree tree1 = new JTree();
    tree1.putClientProperty(key, "Horizontal");

    JTree tree2 = new JTree();
    tree2.putClientProperty(key, "None");

    add(makeTitledPanel("Angled(default)", new JScrollPane(tree0)));
    add(makeTitledPanel("Horizontal", new JScrollPane(tree1)));
    add(makeTitledPanel("None", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
