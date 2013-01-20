package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.plaf.metal.*;

public class MainPanel extends JPanel {
    private static final String TAG = "<html><b>";
    public MainPanel() {
        super(new BorderLayout());

        JCheckBox check = new JCheckBox("swing.boldMetal");
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //http://docs.oracle.com/javase/jp/6/api/javax/swing/plaf/metal/DefaultMetalTheme.html
                JCheckBox c = (JCheckBox)e.getSource();
                UIManager.put("swing.boldMetal", c.isSelected());
                // re-install the Metal Look and Feel
                try{
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
                // Update the ComponentUIs for all Components. This
                // needs to be invoked for all windows.
                SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(c));
            }
        });

        JTree tree = new JTree();
        tree.setComponentPopupMenu(new TreePopupMenu());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createTitledBorder("TitledBorder"));
        tabbedPane.addTab(TAG+"JTree", new JScrollPane(tree));
        tabbedPane.addTab("JLabel",    new JLabel("JLabel"));
        tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea("JTextArea")));
        tabbedPane.addTab("JButton",   new JScrollPane(new JButton("JButton")));
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JTabbedPane t = (JTabbedPane)e.getSource();
                int index = t.getSelectedIndex();
                for(int i=0;i<t.getTabCount();i++) {
                    String title = t.getTitleAt(i);
                    if(i==index) {
                        t.setTitleAt(i, TAG+title);
                    }else if(title.startsWith(TAG)) {
                        t.setTitleAt(i, title.substring(TAG.length()));
                    }
                }
            }
        });
        add(check, BorderLayout.NORTH);
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        //XXX: UIManager.put("swing.boldMetal", Boolean.FALSE);
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TreePopupMenu extends JPopupMenu {
    private final JTextField textField = new JTextField(24);
    private TreePath path;
    public TreePopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent event) {}
            @Override public void ancestorRemoved(AncestorEvent e) {}
        });
        add(new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                JTree tree = (JTree)getInvoker();
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)path.getLastPathComponent();
                DefaultMutableTreeNode child  = new DefaultMutableTreeNode("New node");
                //model.insertNodeInto(child, parent, 0);
                parent.add(child);
                model.nodeStructureChanged(parent);
                tree.expandPath(path);
            }
        });
        add(new JMenuItem(new AbstractAction("edit") {
            @Override public void actionPerformed(ActionEvent e) {
                JTree tree = (JTree)getInvoker();
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                //if(path==null) return;
                Object node = path.getLastPathComponent();
                if(node instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)node;
                    textField.setText(leaf.getUserObject().toString());
                    int result = JOptionPane.showConfirmDialog(
                        tree, textField, "edit",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if(result==JOptionPane.OK_OPTION) {
                        String str = textField.getText();
                        if(!str.trim().isEmpty()) {
                            model.valueForPathChanged(path, str);
                            //leaf.setUserObject(str);
                            //model.nodeChanged(leaf);
                        }
                    }
                }
            }
        }));
        addSeparator();
        add(new AbstractAction("remove") {
            @Override public void actionPerformed(ActionEvent e) {
                JTree tree = (JTree)getInvoker();
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                //if(path.getParentPath()!=null) {
                if(!node.isRoot()) {
                    model.removeNodeFromParent(node);
                }
            }
        });
    }
    @Override public void show(Component c, int x, int y) {
        JTree tree = (JTree)c;
        TreePath[] tsp = tree.getSelectionPaths();
        if(tsp!=null) {
            path = tree.getPathForLocation(x, y);
            if(path!=null && Arrays.asList(tsp).contains(path)) {
                super.show(c, x, y);
            }
        }
    }
}
