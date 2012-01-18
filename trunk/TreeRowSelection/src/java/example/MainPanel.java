package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private static final Color SELC = new Color(100,150,200);
    private final JTree tree = new JTree() {
        @Override public void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0,0,getWidth(),getHeight());
            if(getSelectionCount()>0) {
                for(int i: getSelectionRows()) {
                    Rectangle r = getRowBounds(i);
                    g.setColor(SELC);
                    g.fillRect(0, r.y, getWidth(), r.height);
                }
            }
            super.paintComponent(g);
            if(getLeadSelectionPath()!=null) {
                Rectangle r = getRowBounds(getRowForPath(getLeadSelectionPath()));
                g.setColor(SELC.darker());
                g.drawRect(0, r.y, getWidth()-1, r.height-1);
            }
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        tree.setUI(new BasicTreeUI() {
            @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
                if(tree != null && treeState != null) {
                    return getPathBounds(path, tree.getInsets(), new Rectangle());
                }
                return null;
            }
            private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
                bounds = treeState.getBounds(path, bounds);
                if(bounds != null) {
                    bounds.width = tree.getWidth();
                    bounds.y += insets.top;
                }
                return bounds;
            }
        });

        tree.setOpaque(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {
                JLabel l = (JLabel)super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
                l.setBackground(selected?SELC:tree.getBackground());
                l.setOpaque(true);
                return l;
            }
        });

        JPanel p = new JPanel(new GridLayout(1,2));
        p.add(new JScrollPane(new JTree()));
        p.add(new JScrollPane(tree));
        add(p);
        setPreferredSize(new Dimension(320, 240));
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
