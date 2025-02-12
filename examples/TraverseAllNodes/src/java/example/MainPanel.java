// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();
    JTextArea textArea = new JTextArea();
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

    JButton depthFirst = new JButton("<html>depthFirst<br>postorder");
    depthFirst.addActionListener(ev -> {
      textArea.setText("");
      // Java 9: Collections.list(root.depthFirstEnumeration())
      Collections.list((Enumeration<?>) root.depthFirstEnumeration())
          .forEach(n -> textArea.append(String.format("%s%n", n)));
    });

    // JButton postorder = new JButton("postorder");
    // postorder.addActionListener(ev -> {
    //   textArea.setText("");
    //   // Java 9: Collections.list(root.postorderEnumeration())
    //   Collections.list((Enumeration<?>) root.postorderEnumeration())
    //       .forEach(n -> textArea.append(Objects.toString(n) + "\n"));
    // });

    JButton breadthFirst = new JButton("breadthFirst");
    breadthFirst.addActionListener(ev -> {
      textArea.setText("");
      // Java 9: Collections.list(root.breadthFirstEnumeration())
      Collections.list((Enumeration<?>) root.breadthFirstEnumeration())
          .forEach(n -> textArea.append(String.format("%s%n", n)));
    });

    JButton preorder = new JButton("preorder");
    preorder.addActionListener(ev -> {
      textArea.setText("");
      // Java 9: Collections.list(root.preorderEnumeration())
      Collections.list((Enumeration<?>) root.preorderEnumeration())
          .forEach(n -> textArea.append(String.format("%s%n", n)));
    });

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.add(depthFirst);
    p.add(breadthFirst);
    p.add(preorder);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.add(p, BorderLayout.NORTH);

    JScrollPane s1 = new JScrollPane(tree);
    JScrollPane s2 = new JScrollPane(textArea);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);

    add(split);
    add(panel, BorderLayout.EAST);
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
      Logger.getGlobal().severe(ex::getMessage);
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
