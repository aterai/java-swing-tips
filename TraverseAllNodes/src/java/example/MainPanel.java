package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
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
            // Java 9: Collections.list(root.depthFirstEnumeration()).stream()
            Collections.list((Enumeration<?>) root.depthFirstEnumeration()).stream()
                .forEach(n -> textArea.append(Objects.toString(n) + "\n"));

            // // Java 9: Enumeration<TreeNode> e = root.depthFirstEnumeration();
            // Enumeration<?> e = root.depthFirstEnumeration();
            // while (e.hasMoreElements()) {
            //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            //     textArea.append(node.toString() + "\n");
            // }
        });

        // JButton postorder = new JButton("postorder");
        // postorder.addActionListener(ev -> {
        //     textArea.setText("");
        //     // Java 9: Collections.list(root.postorderEnumeration()).stream()
        //     Collections.list((Enumeration<?>) root.postorderEnumeration()).stream()
        //         .forEach(n -> textArea.append(Objects.toString(n) + "\n"));
        //
        //     // // Java 9: Enumeration<TreeNode> e = root.postorderEnumeration();
        //     // Enumeration<?> e = root.postorderEnumeration();
        //     // while (e.hasMoreElements()) {
        //     //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
        //     //     textArea.append(node.toString() + "\n");
        //     // }
        // });

        JButton breadthFirst = new JButton("breadthFirst");
        breadthFirst.addActionListener(ev -> {
            textArea.setText("");
            // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
            Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
                .forEach(n -> textArea.append(Objects.toString(n) + "\n"));

            // // Java 9: Enumeration<TreeNode> e = root.breadthFirstEnumeration();
            // Enumeration<?> e = root.breadthFirstEnumeration();
            // while (e.hasMoreElements()) {
            //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            //     textArea.append(node.toString() + "\n");
            // }
        });

        JButton preorder = new JButton("preorder");
        preorder.addActionListener(ev -> {
            textArea.setText("");
            // Java 9: Collections.list(root.preorderEnumeration()).stream()
            Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
                .forEach(n -> textArea.append(Objects.toString(n) + "\n"));

            // // Java 9: Enumeration<TreeNode> e = root.preorderEnumeration();
            // Enumeration<?> e = root.preorderEnumeration();
            // while (e.hasMoreElements()) {
            //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            //     textArea.append(node.toString() + "\n");
            // }
        });

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(depthFirst);
        p.add(breadthFirst);
        p.add(preorder);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(p, BorderLayout.NORTH);

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(new JScrollPane(tree));
        sp.setRightComponent(new JScrollPane(textArea));
        add(sp);
        add(panel, BorderLayout.EAST);
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
