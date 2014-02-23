package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    private final JTextArea textArea = new JTextArea();
    private final TreeModel model = tree.getModel();
    private final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    public MainPanel() {
        super(new BorderLayout());
        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(new JScrollPane(tree));
        sp.setRightComponent(new JScrollPane(textArea));
        add(sp);
        add(makeButtonPanel(), BorderLayout.EAST);
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makeButtonPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new JButton(new AbstractAction("<html>depthFirst<br>postorder") {
            @Override public void actionPerformed(ActionEvent ev) {
                textArea.setText("");
                Enumeration e = root.depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                    textArea.append(node.toString() + "\n");
                }
            }
        }));
        p.add(new JButton(new AbstractAction("breadthFirst") {
            @Override public void actionPerformed(ActionEvent ev) {
                textArea.setText("");
                Enumeration e = root.breadthFirstEnumeration();
                while (e.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                    textArea.append(node.toString() + "\n");
                }
            }
        }));
//         p.add(new JButton(new AbstractAction("postorder") {
//             @Override public void actionPerformed(ActionEvent ev) {
//                 textArea.setText("");
//                 Enumeration e = root.postorderEnumeration();
//                 while (e.hasMoreElements()) {
//                     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//                     textArea.append(node.toString() + "\n");
//                 }
//             }
//         }));
        p.add(new JButton(new AbstractAction("preorder") {
            @Override public void actionPerformed(ActionEvent ev) {
                textArea.setText("");
                Enumeration e = root.preorderEnumeration();
                while (e.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
                    textArea.append(node.toString() + "\n");
                }
            }
        }));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(p, BorderLayout.NORTH);
        return panel;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
