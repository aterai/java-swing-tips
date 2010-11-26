package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2,1));
        JTree tree1 = new JTree() {
            @Override public String getToolTipText(MouseEvent e) {
                Object o = null;
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if(path!=null) {
                    o = path.getLastPathComponent();
                }
                return (o==null)?null:"getToolTipText: "+o.toString();
            }
        };
        ToolTipManager.sharedInstance().registerComponent(tree1);

        JTree tree2 = new JTree();
        tree2.setCellRenderer(new ToolTipTreeCellRenderer(tree2.getCellRenderer()));
        //tree2.setToolTipText("dummy");
        ToolTipManager.sharedInstance().registerComponent(tree2);

        add(makeTitledPanel("Override getToolTipText", tree1));
        add(makeTitledPanel("Use TreeCellRenderer", tree2));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }

//     private static DefaultTreeModel makeModel() {
//         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
//         DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
//         DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
//         DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
//         set1.add(new DefaultMutableTreeNode("111111111"));
//         set1.add(new DefaultMutableTreeNode("22222222222"));
//         set1.add(new DefaultMutableTreeNode("33333"));
//         set2.add(new DefaultMutableTreeNode("asdfasdfas"));
//         set2.add(new DefaultMutableTreeNode("asdf"));
//         set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
//         set3.add(new DefaultMutableTreeNode("asdfasdf"));
//         set3.add(new DefaultMutableTreeNode("############"));
//         root.add(set1);
//         root.add(set2);
//         set2.add(set3);
//         return new DefaultTreeModel(root);
//     }

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

class ToolTipTreeCellRenderer implements TreeCellRenderer {
    private final TreeCellRenderer renderer;
    public ToolTipTreeCellRenderer(TreeCellRenderer renderer) {
        this.renderer = renderer;
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        c.setToolTipText((value==null)?null:"TreeCellRenderer: "+value.toString());
        return c;
    }
}
