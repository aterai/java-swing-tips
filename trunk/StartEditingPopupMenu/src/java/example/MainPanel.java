package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
        JTree tree = new JTree();
        tree.setCellEditor(new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer)tree.getCellRenderer()) {
//             @Override protected boolean shouldStartEditingTimer(EventObject e) {
//                 return false;
//             }
//             @Override protected boolean canEditImmediately(EventObject e) {
//                 //((MouseEvent)e).getClickCount()>2
//                 return (e instanceof MouseEvent)?false:super.canEditImmediately(e);
//             }
            @Override public boolean isCellEditable(EventObject e) {
                return (e instanceof MouseEvent)?false:super.isCellEditable(e);
            }
        });
        tree.setEditable(true);
        tree.setComponentPopupMenu(new TreePopupMenu());

        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 200));
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

class TreePopupMenu extends JPopupMenu {
    private TreePath path;
    private final JTextField textField = new JTextField();
    public TreePopupMenu() {
        super();
        add(new JMenuItem(new AbstractAction("Edit") {
            @Override public void actionPerformed(ActionEvent e) {
                JTree tree = (JTree)getInvoker();
                if(path!=null) tree.startEditingAtPath(path);
            }
        }));
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent event) {}
            @Override public void ancestorRemoved(AncestorEvent e) {}
        });
        add(new JMenuItem(new AbstractAction("Edit Dialog") {
            @Override public void actionPerformed(ActionEvent e) {
                JTree tree = (JTree)getInvoker();
                if(path==null) return;
                Object node = path.getLastPathComponent();
                if(node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) node;
                    textField.setText(leaf.getUserObject().toString());
                    int result = JOptionPane.showConfirmDialog(tree, textField, "Rename", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if(result==JOptionPane.OK_OPTION) {
                        String str = textField.getText();
                        if(!str.trim().isEmpty()) ((DefaultTreeModel)tree.getModel()).valueForPathChanged(path, str);
                    }
                }
            }
        }));
    }
    @Override public void show(Component c, int x, int y) {
        JTree tree = (JTree)c;
        TreePath[] tsp = tree.getSelectionPaths();
        if(tsp!=null && tsp.length>0) {
            path = tree.getPathForLocation(x, y); //path = tree.getClosestPathForLocation(x, y);
            if(tsp[tsp.length-1].equals(path)) { //Test: if(path!=null && java.util.Arrays.asList(tsp).contains(path)) {
                super.show(c, x, y);
            }
        }
    }
}
