package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree() {
        @Override public void updateUI() {
            setCellRenderer(null);
            setCellEditor(null);
            super.updateUI();
            //???#1: JDK 1.6.0 bug??? Nimbus LnF
            setCellRenderer(new SelectionColorTreeCellRenderer());
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        tree.setModel(makeModel());
        tree.setCellRenderer(new SelectionColorTreeCellRenderer());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode(Color.ORANGE);
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode(Color.RED));
        set1.add(new DefaultMutableTreeNode(Color.GREEN));
        set1.add(new DefaultMutableTreeNode(Color.BLUE));
        set2.add(new DefaultMutableTreeNode("asdfasdfas"));
        set2.add(new DefaultMutableTreeNode("asdf"));
        set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
        set3.add(new DefaultMutableTreeNode("qwerqwer"));
        set3.add(new DefaultMutableTreeNode("zvxcvzxcvzxzxcvzxcv"));
        root.add(set1);
        root.add(set2);
        set2.add(set3);
        return new DefaultTreeModel(root);
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
        }catch(ClassNotFoundException | InstantiationException |
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

class SelectionColorTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        if(isSelected) {
            setParticularCondition(value);
            c.setForeground(getTextSelectionColor());
            c.setBackground(getBackgroundSelectionColor());
            if(leaf && value.toString().startsWith("a")) {
                c.setOpaque(true);
                c.setBackground(Color.RED);
            }else{
                c.setOpaque(false);
                c.setBackground(getBackgroundSelectionColor());
            }
        }else{
            c.setForeground(getTextNonSelectionColor());
            c.setBackground(getBackgroundNonSelectionColor());
        }
        return c;
    }
    private Color color = null;
    private void setParticularCondition(Object value) {
        if(value instanceof DefaultMutableTreeNode) {
            Object uo = ((DefaultMutableTreeNode)value).getUserObject();
            if(uo instanceof Color) {
                color = (Color)uo;
                return;
            }
        }
        color = null;
    }
    @Override public Color getBackgroundSelectionColor() {
        return color!=null ? color : super.getBackgroundSelectionColor();
    }
}
