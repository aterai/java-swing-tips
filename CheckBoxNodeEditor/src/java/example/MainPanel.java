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
    public MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                //???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new CheckBoxNodeRenderer());
                setCellEditor(new CheckBoxNodeEditor());
            }
        };
        TreeModel model = tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
            Object o = node.getUserObject();
            if(o instanceof String) {
                node.setUserObject(new CheckBoxNode((String)o, false));
            }
        }
        tree.setEditable(true);
        tree.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

        //???#1: JDK 1.6.0 bug??? Nimbus LnF
        //tree.setCellRenderer(new CheckBoxNodeRenderer());
        //tree.setCellEditor(new CheckBoxNodeEditor());

        tree.expandRow(0);
        //tree.setToggleClickCount(1);

        model.addTreeModelListener(new TreeModelListener() {
            private boolean adjusting = false;
            @Override public void treeNodesChanged(TreeModelEvent e) {
                if(adjusting) return;
                adjusting = true;
                TreePath parent = e.getTreePath();
                Object[] children = e.getChildren();
                DefaultTreeModel model = (DefaultTreeModel)e.getSource();

                DefaultMutableTreeNode node;
                CheckBoxNode c; // = (CheckBoxNode)node.getUserObject();
                if(children!=null && children.length==1) {
                    node = (DefaultMutableTreeNode)children[0];
                    c = (CheckBoxNode)node.getUserObject();
                    updateParentUserObject(model, (DefaultMutableTreeNode)parent.getLastPathComponent());
                }else{
                    node = (DefaultMutableTreeNode)model.getRoot();
                    c = (CheckBoxNode)node.getUserObject();
                }
                updateAllChildrenUserObject(model, node, c.selected);
                adjusting = false;
            }
            private void updateParentUserObject(DefaultTreeModel model, DefaultMutableTreeNode parent) {
                CheckBoxNode check = (CheckBoxNode)parent.getUserObject();
                int selectedCount = 0;
                java.util.Enumeration children = parent.children();
                while(children.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
                    CheckBoxNode cbn = (CheckBoxNode)node.getUserObject();
                    if(cbn.selected) selectedCount++;
                }
                if(selectedCount==0) {
                    parent.setUserObject(new CheckBoxNode(check.str, false));
                }else if(selectedCount==parent.getChildCount()) {
                    parent.setUserObject(new CheckBoxNode(check.str, true));
                }else{
                    parent.setUserObject(new CheckBoxNode(check.str));
                }
                model.nodeChanged(parent);
                //model.valueForPathChanged(parent, newValue);
            }
            private void updateAllChildrenUserObject(DefaultTreeModel model, DefaultMutableTreeNode root, boolean isSelected) {
                java.util.Enumeration breadth = root.breadthFirstEnumeration();
                while(breadth.hasMoreElements()) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)breadth.nextElement();
                    if(root==node) {
                        continue;
                    }
                    CheckBoxNode check = (CheckBoxNode)node.getUserObject();
                    node.setUserObject(new CheckBoxNode(check.str, isSelected));
                    model.nodeChanged(node);
                }
            }
            @Override public void treeNodesInserted(TreeModelEvent e) {}
            @Override public void treeNodesRemoved(TreeModelEvent e) {}
            @Override public void treeStructureChanged(TreeModelEvent e) {}
        });

        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(new JScrollPane(tree));
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

class TriStateCheckBox extends JCheckBox{
    private Icon currentIcon;
    @Override public void updateUI() {
        currentIcon = getIcon();
        setIcon(null);
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if(currentIcon!=null) {
                    setIcon(new IndeterminateIcon());
                }
                setOpaque(false);
            }
        });
    }
}

class IndeterminateIcon implements Icon {
    private final Color FOREGROUND = new Color(50,20,255,200); //TEST: UIManager.getColor("CheckBox.foreground");
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        int w = getIconWidth(), h = getIconHeight();
        int a = 4, b = 2;
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(FOREGROUND);
        g2.translate(x, y);
        g2.fillRect(a, (h-b)/2, w-a-a, b);
        g2.translate(-x, -y);
    }
    @Override public int getIconWidth()  { return icon.getIconWidth();  }
    @Override public int getIconHeight() { return icon.getIconHeight(); }
}

class CheckBoxNode{
    public final String str;
    public final boolean selected;
    public final boolean indeterminate;
    public CheckBoxNode(String str) {
        this.str = str;
        this.selected = false;
        this.indeterminate = true;
    }
    public CheckBoxNode(String str, boolean selected) {
        this.str = str;
        this.selected = selected;
        this.indeterminate = false;
    }
    @Override public String toString() {
        return str;
    }
}

//*
// extends JCheckBox TreeCellRenderer Editor version
class CheckBoxNodeRenderer extends TriStateCheckBox implements TreeCellRenderer {
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final JPanel panel = new JPanel(new BorderLayout());
    private JTree tree = null;
    public CheckBoxNodeRenderer() {
        super();
        String uiName = getUI().getClass().getName();
        if(uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
            System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            renderer.setBackgroundSelectionColor(new Color(0,0,0,0));
        }
        panel.setFocusable(false);
        panel.setRequestFocusEnabled(false);
        panel.setOpaque(false);
        panel.add(this, BorderLayout.WEST);
        this.setOpaque(false);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.tree = tree;
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        l.setFont(tree.getFont());
        if(value != null && value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                if(!node.selected && node.indeterminate) {
                    setIcon(new IndeterminateIcon());
                }else{
                    setIcon(null);
                }
                l.setText(node.str);
                setSelected(node.selected);
            }
            //panel.add(this, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public void updateUI() {
        super.updateUI();
        if(panel!=null) {
            //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            panel.updateUI();
            //panel.add(this, BorderLayout.WEST);
        }
        setName("Tree.cellRenderer");
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if(System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }
}

class CheckBoxNodeEditor extends TriStateCheckBox implements TreeCellEditor {
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final JPanel panel = new JPanel(new BorderLayout());
    private String str = null;
    public CheckBoxNodeEditor() {
        super();
        this.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //System.out.println("actionPerformed: stopCellEditing");
                stopCellEditing();
            }
        });
        panel.setFocusable(false);
        panel.setRequestFocusEnabled(false);
        panel.setOpaque(false);
        panel.add(this, BorderLayout.WEST);
        this.setOpaque(false);
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        //JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        l.setFont(tree.getFont());
        if(value != null && value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                if(!node.selected && node.indeterminate) {
                    setIcon(new IndeterminateIcon());
                }else{
                    setIcon(null);
                }
                l.setText(node.str);
                setSelected(node.selected);
                str = node.str;
            }
            //panel.add(this, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(str, isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        if(e != null && e instanceof MouseEvent && e.getSource() instanceof JTree) {
            MouseEvent me = (MouseEvent)e;
            JTree tree = (JTree)e.getSource();
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());
            Rectangle r = tree.getPathBounds(path);
            if(r==null) return false;
            Dimension d = getPreferredSize();
            r.setSize(new Dimension(d.width, r.height));
            if(r.contains(me.getX(), me.getY())) {
                if(str==null && System.getProperty("java.version").startsWith("1.7.0")) {
                    System.out.println("XXX: Java 7, only on first run\n"+getBounds());
                    setBounds(new Rectangle(0,0,d.width,r.height));
                }
                //System.out.println(getBounds());
                return true;
            }
        }
        return false;
    }
    @Override public void updateUI() {
        super.updateUI();
        setName("Tree.cellEditor");
        if(panel!=null) {
            //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            panel.updateUI();
            //panel.add(this, BorderLayout.WEST);
        }
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if(System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }

    //Copid from AbstractCellEditor
//     protected EventListenerList listenerList = new EventListenerList();
//     transient protected ChangeEvent changeEvent = null;
    @Override public boolean shouldSelectCell(java.util.EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
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
/*/
// extends JPanel TreeCellRenderer Editor version
class CheckBoxNodeRenderer extends JPanel implements TreeCellRenderer {
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final TriStateCheckBox check = new TriStateCheckBox();
    public CheckBoxNodeRenderer() {
        super(new BorderLayout());
        String uiName = getUI().getClass().getName();
        if(uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
            System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            renderer.setBackgroundSelectionColor(new Color(0,0,0,0));
        }
        setFocusable(false);
        setRequestFocusEnabled(false);
        setOpaque(false);
        add(check, BorderLayout.WEST);
        check.setOpaque(false);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        l.setFont(tree.getFont());
        if(value != null && value instanceof DefaultMutableTreeNode) {
            check.setEnabled(tree.isEnabled());
            check.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                if(!node.selected && node.indeterminate) {
                    check.setIcon(new IndeterminateIcon());
                }else{
                    check.setIcon(null);
                }
                l.setText(node.str);
                check.setSelected(node.selected);
            }
            //add(this, BorderLayout.WEST);
            add(l);
            return this;
        }
        return l;
    }
    @Override public void updateUI() {
        super.updateUI();
        if(check!=null) {
            //removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            check.updateUI();
            //add(check, BorderLayout.WEST);
        }
        setName("Tree.cellRenderer");
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if(System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }
}

class CheckBoxNodeEditor extends JPanel implements TreeCellEditor {
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final TriStateCheckBox check = new TriStateCheckBox();
    private String str = null;
    public CheckBoxNodeEditor() {
        super(new BorderLayout());
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //System.out.println("actionPerformed: stopCellEditing");
                stopCellEditing();
            }
        });
        setFocusable(false);
        setRequestFocusEnabled(false);
        setOpaque(false);
        add(check, BorderLayout.WEST);
        check.setOpaque(false);
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        //JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        l.setFont(tree.getFont());
        if(value != null && value instanceof DefaultMutableTreeNode) {
            check.setEnabled(tree.isEnabled());
            check.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                if(!node.selected && node.indeterminate) {
                    check.setIcon(new IndeterminateIcon());
                }else{
                    check.setIcon(null);
                }
                l.setText(node.str);
                check.setSelected(node.selected);
                str = node.str;
            }
            //add(this, BorderLayout.WEST);
            add(l);
            return this;
        }
        return l;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(str, check.isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        if(e != null && e instanceof MouseEvent && e.getSource() instanceof JTree) {
            MouseEvent me = (MouseEvent)e;
            JTree tree = (JTree)e.getSource();
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());
            Rectangle r = tree.getPathBounds(path);
            if(r==null) return false;
            Dimension d = check.getPreferredSize();
            r.setSize(new Dimension(d.width, r.height));
            if(r.contains(me.getX(), me.getY())) {
                if(str==null && System.getProperty("java.version").startsWith("1.7.0")) {
                    System.out.println("XXX: Java 7, only on first run\n"+getBounds());
                    check.setBounds(new Rectangle(0,0,d.width,r.height));
                }
                //System.out.println(getBounds());
                return true;
            }
        }
        return false;
    }
    @Override public void updateUI() {
        super.updateUI();
        setName("Tree.cellEditor");
        if(check!=null) {
            //removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            check.updateUI();
            //add(check, BorderLayout.WEST);
        }
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if(System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }

    //Copid from AbstractCellEditor
    protected EventListenerList listenerList = new EventListenerList();
    transient protected ChangeEvent changeEvent = null;
    @Override public boolean shouldSelectCell(java.util.EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
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
//*/
