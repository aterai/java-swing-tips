package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final DefaultMutableTreeNode root = makeTreeRoot();
    private final JTree tree = new JTree(new DefaultTreeModel(makeTreeRoot()));
    private final JCheckBox check = new JCheckBox();
    public MainPanel() {
        super(new BorderLayout());
        check.setHorizontalAlignment(SwingConstants.RIGHT);
        check.setAction(new AbstractAction("sort tree") {
            @Override public void actionPerformed(ActionEvent e) {
                tree.setModel(new DefaultTreeModel(check.isSelected()?sortTree(makeTreeRoot()):root));
                expandAll(tree);
            }
        });
        add(check, BorderLayout.SOUTH);
        add(makeTitledPanel("Sort JTree", tree));
        expandAll(tree);
        setPreferredSize(new Dimension(320, 200));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    //Swing - How to sort jTree Nodes>http://forums.sun.com/thread.jspa?threadID=566391
    public static DefaultMutableTreeNode sortTree(DefaultMutableTreeNode root) {
        for(int i=0;i<root.getChildCount();i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            String nt = node.getUserObject().toString();
            for(int j=0; j<i; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                String np = prevNode.getUserObject().toString();
                if(nt.compareToIgnoreCase(np)<0) {
                    root.insert(node, j);
                    root.insert(prevNode, i);
                }
            }
            if(node.getChildCount() > 0) node = sortTree(node);
        }
        return root;
    }
    private static DefaultMutableTreeNode makeTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode("33333"));
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set3.add(new DefaultMutableTreeNode("zzzzzzz"));
        set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
        set3.add(new DefaultMutableTreeNode("ccccccccc"));
        root.add(set3);
        root.add(set1);
        root.add(set2);
        return root;
    }
    private void expandAll(JTree tree) {
        int row = 0;
        while(row<tree.getRowCount()) {
            tree.expandRow(row);
            row++;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
