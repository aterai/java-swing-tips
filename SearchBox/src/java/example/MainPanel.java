package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree       = new JTree(makeModel());
    private final JTextField field = new JTextField("asd", 10);
    private final JButton button   = new JButton();
    private final JButton showHideButton = new JButton();
    private final List<TreePath> rollOverPathLists = new ArrayList<>();

    private Timer animator;
    private boolean isHidden = true;
    private final JPanel controls  = new JPanel(new BorderLayout(5, 5) {
        private int controlsHeight;
        private int controlsPreferredHeight;
        @Override public Dimension preferredLayoutSize(Container target) {
            //synchronized (target.getTreeLock()) {
            Dimension ps = super.preferredLayoutSize(target);
            controlsPreferredHeight = ps.height;
            if (animator != null) {
                if (isHidden) {
                    if (controls.getHeight() < controlsPreferredHeight) {
                        controlsHeight += 5;
                    }
                } else {
                    if (controls.getHeight() > 0) {
                        controlsHeight -= 5;
                    }
                }
                if (controlsHeight<=0) {
                    controlsHeight = 0;
                    animator.stop();
                } else if (controlsHeight >= controlsPreferredHeight) {
                    controlsHeight = controlsPreferredHeight;
                    animator.stop();
                }
            }
            ps.height = controlsHeight;
            return ps;
        }
    });
    private final Action findNextAction = new AbstractAction("Find Next") {
        @Override public void actionPerformed(ActionEvent e) {
            TreePath selectedPath = tree.getSelectionPath();
            tree.clearSelection();
            rollOverPathLists.clear();
            searchTree(tree, tree.getPathForRow(0), field.getText(), rollOverPathLists);
            if (!rollOverPathLists.isEmpty()) {
                int nextIndex = 0;
                int size = rollOverPathLists.size();
                for (int i = 0; i < size; i++) {
                    if (rollOverPathLists.get(i).equals(selectedPath)) {
                        nextIndex = i + 1<size ? i + 1 : 0;
                        break;
                    }
                }
                TreePath p = rollOverPathLists.get(nextIndex);
                tree.addSelectionPath(p);
                tree.scrollPathToVisible(p);
            }
        }
    };
    private final Action showHideAction = new AbstractAction("Show/Hide Search Box") {
        @Override public void actionPerformed(ActionEvent e) {
            if (animator != null && animator.isRunning()) {
                return;
            }
            isHidden = controls.getHeight() == 0;
            animator = new Timer(5, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    controls.revalidate();
                }
            });
            animator.start();
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        button.setAction(findNextAction);
        button.setFocusable(false);

        field.getActionMap().put("find-next", findNextAction);
        field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "find-next");
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 SwingUtilities.getRootPane(button).setDefaultButton(button);
//             }
//         });

        controls.setBorder(BorderFactory.createTitledBorder("Search down"));
        controls.add(new JLabel("Find what:"), BorderLayout.WEST);
        controls.add(field);
        controls.add(button, BorderLayout.EAST);

        showHideButton.setAction(showHideAction);
        showHideButton.setFocusable(false);

        InputMap imap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), "open-searchbox");
        getActionMap().put("open-searchbox", showHideAction);

        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        add(showHideButton, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultTreeModel makeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(new DefaultMutableTreeNode("33333"));
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
    private static void searchTree(JTree tree, TreePath path, String q, List<TreePath> rollOverPathLists) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (node == null) {
            return;
        }
        if (node.toString().startsWith(q)) {
            rollOverPathLists.add(path);
            tree.expandPath(path.getParentPath());
        }
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                searchTree(tree, path.pathByAddingChild(e.nextElement()), q, rollOverPathLists);
            }
        }
    }

//     //<blockquote cite="https://forums.oracle.com/thread/1357454"
//     //           title="how to get everything in DefaultTreeNode">
//     public void traverse(JTree tree) {
//         TreeModel model = tree.getModel();
//         Object root;
//         if (model != null) {
//             root = model.getRoot();
//             walk(model, root);
//         } else {
//             System.out.println("Tree is empty.");
//         }
//     }
//     protected void walk(TreeModel model, Object o) {
//         int cc = model.getChildCount(o);
//         for (int i=0; i < cc; i++) {
//             DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(o, i);
//             if (model.isLeaf(child)) {
//                 System.out.println(child);
//             } else {
//                 System.out.println(child);
//                 walk(model, child);
//             }
//         }
//     }
//     //<blockquote />
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
