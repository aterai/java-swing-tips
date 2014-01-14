package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,2));
        JTree tree1 = new JTree();
        tree1.setEditable(true);

        JTree tree2 = new JTree();
        tree2.setCellEditor(new LeafTreeCellEditor(tree2, (DefaultTreeCellRenderer)tree2.getCellRenderer()));
        tree2.setEditable(true);

        add(makeTitledPanel("DefaultTreeCellEditor", tree1));
        add(makeTitledPanel("LeafTreeCellEditor",    tree2));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
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

class LeafTreeCellEditor extends DefaultTreeCellEditor {
    public LeafTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    @Override public boolean isCellEditable(EventObject e) {
        boolean b = super.isCellEditable(e);
        Object o = tree.getLastSelectedPathComponent();
        if(b && o!=null && o instanceof TreeNode) {
            return ((TreeNode)o).isLeaf();
        }else{
            return b;
        }
    }
}
