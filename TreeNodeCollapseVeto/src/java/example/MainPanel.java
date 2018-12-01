package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.IconUIResource;
import javax.swing.tree.ExpandVetoException;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2, 5, 5));
        add(new JScrollPane(new JTree()));
        add(new JScrollPane(makeTree()));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTree makeTree() {
        Icon emptyIcon = new EmptyIcon();
        UIManager.put("Tree.expandedIcon", new IconUIResource(emptyIcon));
        UIManager.put("Tree.collapsedIcon", new IconUIResource(emptyIcon));

        JTree tree = new JTree();
        tree.setEditable(true);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row++);
        }

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
                // throw new ExpandVetoException(e, "Tree expansion cancelled");
            }
            @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
                throw new ExpandVetoException(e, "Tree collapse cancelled");
            }
        });
        return tree;
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

class EmptyIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
    @Override public int getIconWidth() {
        return 0;
    }
    @Override public int getIconHeight() {
        return 0;
    }
}
