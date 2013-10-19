package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTextField textField = new JTextField("soccer");
    public MainPanel() {
        super(new BorderLayout());
        final JTree tree1 = new JTree();
        tree1.setExpandsSelectedPaths(false);

        final JTree tree2 = new JTree();
        tree2.setExpandsSelectedPaths(true);

        JPanel p1 = new JPanel(new GridLayout(1, 2));
        p1.setBorder(BorderFactory.createTitledBorder("setExpandsSelectedPaths"));
        p1.add(makeTitledScrollPane(tree1, "false"));
        p1.add(makeTitledScrollPane(tree2, "true"));

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(textField);
        p2.add(new JButton(new AbstractAction("Select") {
            @Override public void actionPerformed(ActionEvent e) {
                String q = textField.getText().trim();
                searchTree(tree1, tree1.getPathForRow(0), q);
                searchTree(tree2, tree2.getPathForRow(0), q);
            }
        }), BorderLayout.EAST);

        add(p1);
        add(p2, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JScrollPane makeTitledScrollPane(JComponent c, String title) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }
    private static void searchTree(JTree tree, TreePath path, String q) {
        TreeNode node = (TreeNode)path.getLastPathComponent();
        if(node==null) return;
        if(node.toString().equals(q)) {
            tree.addSelectionPath(path);
        }
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                searchTree(tree, path.pathByAddingChild(e.nextElement()), q);
            }
        }
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
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
