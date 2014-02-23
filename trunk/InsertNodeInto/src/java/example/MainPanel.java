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
    private final JTree tree1 = new JTree();
    private final JTree tree2 = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        JPanel p1 = new JPanel(new GridLayout(1, 2));
        p1.add(makeTitledScrollPane(tree1, "p.add(c) & m.reload(p)"));
        p1.add(makeTitledScrollPane(tree2, "m.insertNodeInto(c, p, p.size)"));

        JPanel p2 = new JPanel(new GridLayout(1, 2));
        p2.add(new JButton(new AbstractAction("expand all") {
            @Override public void actionPerformed(ActionEvent e) {
                expandAll(tree1);
                expandAll(tree2);
            }
        }));
        p2.add(new JButton(new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                Date date = new Date();

                DefaultTreeModel model1 = (DefaultTreeModel) tree1.getModel();
                DefaultMutableTreeNode parent1 = (DefaultMutableTreeNode) model1.getRoot();
                DefaultMutableTreeNode child1  = new DefaultMutableTreeNode(date);
                parent1.add(child1);
                model1.reload(parent1);
                tree1.scrollPathToVisible(new TreePath(child1.getPath()));

                DefaultTreeModel model2 = (DefaultTreeModel) tree2.getModel();
                DefaultMutableTreeNode parent2 = (DefaultMutableTreeNode) model2.getRoot();
                DefaultMutableTreeNode child2  = new DefaultMutableTreeNode(date);
                model2.insertNodeInto(child2, parent2, parent2.getChildCount());
                tree2.scrollPathToVisible(new TreePath(child2.getPath()));
            }
        }));

        add(p1);
        add(p2, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void expandAll(JTree tree) {
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }
    private static JScrollPane makeTitledScrollPane(JComponent c, String title) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
