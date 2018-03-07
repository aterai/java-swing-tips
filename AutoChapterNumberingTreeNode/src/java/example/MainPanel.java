package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree(makeModel()) {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new ChapterNumberingTreeCellRenderer());
                setRootVisible(false);
            }
        };
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static TreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        root.add(new DefaultMutableTreeNode("Introductiion"));
        root.add(makePart());
        root.add(makePart());
        return new DefaultTreeModel(root);
    }
    private static DefaultMutableTreeNode makePart() {
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("Chapter");
        c1.add(new DefaultMutableTreeNode("Section"));
        c1.add(new DefaultMutableTreeNode("Section"));
        c1.add(new DefaultMutableTreeNode("Section"));

        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("Chapter");
        c2.add(new DefaultMutableTreeNode("aaaaaaaa"));
        c2.add(new DefaultMutableTreeNode("bbbb"));
        c2.add(new DefaultMutableTreeNode("cc"));

        DefaultMutableTreeNode p1 = new DefaultMutableTreeNode("Part");
        p1.add(c1);
        p1.add(c2);
        return p1;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
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

class ChapterNumberingTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final String MARK = "\u00a7"; // "§";
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            TreeNode[] tn = ((DefaultMutableTreeNode) value).getPath();
            String s = IntStream.range(1, tn.length) // ignore the root node by skipping index 0
                .map(i -> 1 + tn[i - 1].getIndex(tn[i]))
                .mapToObj(Objects::toString)
                .collect(Collectors.joining("."));
            l.setText(String.format("%s%s %s", MARK, s, value));
        }
        return l;
    }
}
