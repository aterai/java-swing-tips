package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree();

        // UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
        // tree.addMouseListener(new MouseAdapter() {
        //     @Override public void mousePressed(MouseEvent e) {
        //         JTree tree = (JTree) e.getSource();
        //         TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        //         if (!tree.getSelectionModel().isPathSelected(path)) {
        //             tree.getSelectionModel().setSelectionPath(path);
        //         }
        //     }
        // });

        tree.setCellEditor(new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
            // @Override protected boolean shouldStartEditingTimer(EventObject e) {
            //     return false;
            // }
            // @Override protected boolean canEditImmediately(EventObject e) {
            //     // ((MouseEvent) e).getClickCount() - 2 >= 0
            //     return !(e instanceof MouseEvent) && super.canEditImmediately(e);
            // }
            @Override public boolean isCellEditable(EventObject e) {
                return !(e instanceof MouseEvent) && super.isCellEditable(e);
            }
        });
        tree.setEditable(true);
        tree.setComponentPopupMenu(new TreePopupMenu());

        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
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

class TreePopupMenu extends JPopupMenu {
    protected TreePath path;
    protected final JTextField textField = new JTextField();
    protected final JMenuItem editItem;
    protected final JMenuItem editDialogItem;

    protected TreePopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }

            @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }

            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });

        editItem = add("Edit");
        editItem.addActionListener(e -> {
            if (Objects.nonNull(path)) {
                JTree tree = (JTree) getInvoker();
                tree.startEditingAtPath(path);
            }
        });

        editDialogItem = add("Edit Dialog");
        editDialogItem.addActionListener(e -> {
            if (Objects.isNull(path)) {
                return;
            }
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
                textField.setText(leaf.getUserObject().toString());
                JTree tree = (JTree) getInvoker();
                int ret = JOptionPane.showConfirmDialog(tree, textField, "Rename", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ret == JOptionPane.OK_OPTION) {
                    Optional.ofNullable(textField.getText().trim())
                        .filter(str -> !str.isEmpty())
                        .ifPresent(str -> ((DefaultTreeModel) tree.getModel()).valueForPathChanged(path, str));
                    // String str = textField.getText().trim();
                    // if (!str.isEmpty()) {
                    //     ((DefaultTreeModel) tree.getModel()).valueForPathChanged(path, str);
                    // }
                }
            }
        });
        add("dummy");
    }

    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            // // Test:
            // path = tree.getPathForLocation(x, y);
            // if (tree.getSelectionModel().isPathSelected(path)) {
            //     super.show(c, x, y);
            // }
            TreePath[] tsp = tree.getSelectionPaths();
            path = tree.getPathForLocation(x, y); // Test: path = tree.getClosestPathForLocation(x, y);
            boolean isEditable = tsp.length == 1 && tsp[0].equals(path);
            // Test: if (Objects.nonNull(path) && Arrays.asList(tsp).contains(path)) {
            editItem.setEnabled(isEditable);
            editDialogItem.setEnabled(isEditable);
            super.show(c, x, y);
        }
    }
}
