package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree(getDefaultTreeModel());
    public MainPanel() {
        super(new BorderLayout());

        for(int i=0;i<tree.getRowCount();i++) tree.expandRow(i);

        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setCellEditor(new CheckBoxNodeEditor(tree));
        tree.setEditable(true);

        add(makeTitledPanel("JCheckBoxes as JTree Leaf Nodes", tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    protected static TreeModel getDefaultTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
        DefaultMutableTreeNode parent;

        parent = new DefaultMutableTreeNode("colors");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("blue",   false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("violet", false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("red",    false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("yellow", false)));

        parent = new DefaultMutableTreeNode("sports");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("basketball", true)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("soccer",     true)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("football",   true)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("hockey",     true)));

        parent = new DefaultMutableTreeNode("food");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("hot dogs", false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("pizza",    false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("ravioli",  false)));
        parent.add(new DefaultMutableTreeNode(new CheckBoxNode("bananas",  false)));
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

class CheckBoxNodeRenderer extends JCheckBox implements TreeCellRenderer {
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.tree = tree;
        if(leaf && value != null && value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            this.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                this.setText(node.text);
                this.setSelected(node.selected);
            }
            return this;
        }
        return renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
    private JTree tree = null;
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    @Override public void updateUI() {
        super.updateUI();
        setName("Tree.cellRenderer");
        //1.6.0_24 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        renderer = new DefaultTreeCellRenderer();
        if(tree!=null) { //update all node width???
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
            java.util.Enumeration depth = root.depthFirstEnumeration();
            while(depth.hasMoreElements()) {
                model.nodeChanged((TreeNode)depth.nextElement());
            }
        }
    }
}

class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
    private final JTree tree;
    public CheckBoxNodeEditor(JTree tree) {
        super();
        this.tree = tree;
        setFocusable(false);
        setRequestFocusEnabled(false);
        setOpaque(false);
        addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if(leaf && value != null && value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                this.setSelected(((CheckBoxNode)userObject).selected);
            }else{
                this.setSelected(false);
            }
            this.setText(value.toString());
        }
        return this;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(getText(), isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        if(e != null && e instanceof MouseEvent) {
            TreePath path = tree.getPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
            Object o = path.getLastPathComponent();
            if(o!=null && o instanceof TreeNode) {
                return ((TreeNode)o).isLeaf();
            }
        }
        return false;
    }

    //Copid from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //transient protected ChangeEvent changeEvent = null;

    @Override public boolean shouldSelectCell(java.util.EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void  cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return (CellEditorListener[])listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }
}

class CheckBoxNode {
    public final String text;
    public final boolean selected;
    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
//     public boolean isSelected() {
//         return selected;
//     }
//     public void setSelected(boolean newValue) {
//         selected = newValue;
//     }
//     public String getText() {
//         return text;
//     }
//     public void setText(String newValue) {
//         text = newValue;
//     }
    @Override public String toString() {
        return text;
    }
}

// class CheckBoxNodeRenderer extends DefaultTreeCellRenderer {
//     
//     
//     private final JCheckBox leafRenderer = new JCheckBox();
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         this.tree = tree;
//         //String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
//         if(leaf && value != null && value instanceof DefaultMutableTreeNode) {
//             Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
//             if(userObject instanceof CheckBoxNode) {
//                 leafRenderer.setEnabled(tree.isEnabled());
//                 leafRenderer.setFont(getFont());
//                 leafRenderer.setOpaque(false);
// //                 if(selected) {
// //                     leafRenderer.setForeground(getTextSelectionColor());
// //                     leafRenderer.setBackground(getBackgroundSelectionColor());
// //                 }else{
// //                     leafRenderer.setForeground(getTextNonSelectionColor());
// //                     leafRenderer.setBackground(getBackgroundNonSelectionColor());
// //                 }
//                 CheckBoxNode node = (CheckBoxNode)userObject;
//                 leafRenderer.setText(node.text); //leafRenderer.setText(stringValue);
//                 leafRenderer.setSelected(node.selected);
//                 return leafRenderer;
//             }
//         }
//         JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
// //         if(selected) {
// //             c.setForeground(getTextSelectionColor());
// //             c.setBackground(getBackgroundSelectionColor());
// //         }else{
// //             c.setForeground(getTextNonSelectionColor());
// //             c.setBackground(getBackgroundNonSelectionColor());
// //         }
//         
//         
//         
//         //c.setOpaque(false);
//         return c;
//     }
//     private JTree tree = null;
//     @Override public void updateUI() {
//         if(tree!=null) {
//             tree.setCellRenderer(null);
//             super.updateUI();
//             TreeCellRenderer r = tree.getCellRenderer();
//             
//         }
//         
//
//         
//         if(leafRenderer!=null) leafRenderer.updateUI();
//
//         //setLeafIcon(getDefaultLeafIcon());
//         setOpenIcon(getDefaultOpenIcon());
//         setClosedIcon(getDefaultClosedIcon());
//
// //         if(getTextSelectionColor() instanceof UIResource) {
// //             setTextSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
// //         }
// //         if(getTextNonSelectionColor() instanceof UIResource) {
// //             setTextNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textForeground"));
// //         }
// //         if(getBackgroundSelectionColor() instanceof UIResource) {
// //             setBackgroundSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
// //         }
// //         if(getBackgroundNonSelectionColor() instanceof UIResource) {
// //             setBackgroundNonSelectionColor(DefaultLookup.getColor(this, ui, "Tree.textBackground"));
// //         }
// //         if(getBorderSelectionColor() instanceof UIResource) {
// //             setBorderSelectionColor(DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
// //         }
// // //         drawsFocusBorderAroundIcon = DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
// // //         drawDashedFocusIndicator = DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
// // //         fillBackground = DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
// //         Insets margins = DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
// //         if(margins != null) {
// //             setBorder(new EmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
// //             //if(leafRenderer!=null) leafRenderer.setBorder(getBorder());
// //         }
//         setName("Tree.cellRenderer");
//         //if(tree!=null) tree.revalidate();
//     }
// }
