package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        for (File fileSystemRoot: fileSystemView.getRoots()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new CheckBoxNode(fileSystemRoot, Status.DESELECTED));
            root.add(node);
            for (File file: fileSystemView.getFiles(fileSystemRoot, true)) {
                if (file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(new CheckBoxNode(file, Status.DESELECTED)));
                }
            }
        }
        treeModel.addTreeModelListener(new CheckBoxStatusUpdateListener());

        final JTree tree = new JTree(treeModel) {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                //???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new FileTreeCellRenderer(fileSystemView));
                setCellEditor(new CheckBoxNodeEditor(fileSystemView));
            }
        };
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
        //tree.setCellRenderer(new FileTreeCellRenderer(fileSystemView));
        //tree.setCellEditor(new CheckBoxNodeEditor(fileSystemView));
        tree.setEditable(true);

        tree.expandRow(0);
        //tree.setToggleClickCount(1);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(tree));
        add(new JButton(new AbstractAction("test") {
            @Override public void actionPerformed(ActionEvent ae) {
                System.out.println("------------------");
                searchTreeForCheckedNode(tree.getPathForRow(0));
//                 DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
//                 Enumeration e = root.breadthFirstEnumeration();
//                 while (e.hasMoreElements()) {
//                     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
//                     CheckBoxNode check = (CheckBoxNode) node.getUserObject();
//                     if (Objects.nonNull(check) && check.status == Status.SELECTED) {
//                         System.out.println(check.file.toString());
//                     }
//                 }
            }
        }), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(320, 240));
    }
    private static void searchTreeForCheckedNode(TreePath path) {
        Object o = path.getLastPathComponent();
        if (!(o instanceof DefaultMutableTreeNode)) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
        o = node.getUserObject();
        if (!(o instanceof CheckBoxNode)) {
            return;
        }
        CheckBoxNode check = (CheckBoxNode) o;
        if (check.status == Status.SELECTED) {
            System.out.println(check.file.toString());
        } else if (check.status == Status.INDETERMINATE && !node.isLeaf() && node.getChildCount() >= 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                searchTreeForCheckedNode(path.pathByAddingChild(e.nextElement()));
            }
        }
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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TriStateCheckBox extends JCheckBox {
    private Icon currentIcon;
    @Override public void updateUI() {
        currentIcon = getIcon();
        setIcon(null);
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if (Objects.nonNull(currentIcon)) {
                    setIcon(new IndeterminateIcon());
                }
                setOpaque(false);
            }
        });
    }
}

class IndeterminateIcon implements Icon {
    private static final Color FOREGROUND = new Color(50, 20, 255, 200); //TEST: UIManager.getColor("CheckBox.foreground");
    private static final int SIDE_MARGIN = 4;
    private static final int HEIGHT = 2;
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        int w = getIconWidth();
        int h = getIconHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(FOREGROUND);
        g2.translate(x, y);
        g2.fillRect(SIDE_MARGIN, (h - HEIGHT) / 2, w - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
        //g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return icon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return icon.getIconHeight();
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

class CheckBoxNode {
    public final File file;
    public final Status status;
    public CheckBoxNode(File file) {
        this.file = file;
        status = Status.INDETERMINATE;
    }
    public CheckBoxNode(File file, Status status) {
        this.file = file;
        this.status = status;
    }
    @Override public String toString() {
        return file.getName();
    }
}

class FolderSelectionListener implements TreeSelectionListener {
    private final FileSystemView fileSystemView;
    public FolderSelectionListener(FileSystemView fileSystemView) {
        this.fileSystemView = fileSystemView;
    }
    @Override public void valueChanged(TreeSelectionEvent e) {
        final JTree tree = (JTree) e.getSource();
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();

        if (!node.isLeaf()) {
            return;
        }
        CheckBoxNode check = (CheckBoxNode) node.getUserObject();
        if (Objects.isNull(check)) {
            return;
        }
        final File parent = check.file;
        if (!parent.isDirectory()) {
            return;
        }
        final Status parentStatus = check.status == Status.SELECTED ? Status.SELECTED : Status.DESELECTED;

        final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        Task worker = new Task(fileSystemView, parent) {
            @Override protected void process(List<File> chunks) {
                if (!tree.isDisplayable()) {
                    System.out.println("process: DISPOSE_ON_CLOSE");
                    cancel(true);
                    return;
                }
                for (File file: chunks) {
                    model.insertNodeInto(new DefaultMutableTreeNode(new CheckBoxNode(file, parentStatus)), node, node.getChildCount());
                    //node.add(new DefaultMutableTreeNode(new CheckBoxNode(file, parentStatus)));
                }
                //model.reload(parent); //= model.nodeStructureChanged(parent);
            }
        };
        worker.execute();
    }
}

class Task extends SwingWorker<String, File> {
    private final FileSystemView fileSystemView;
    private final File parent;
    public Task(FileSystemView fileSystemView, File parent) {
        super();
        this.fileSystemView = fileSystemView;
        this.parent = parent;
    }
    @Override public String doInBackground() {
        File[] children = fileSystemView.getFiles(parent, true);
        for (File child: children) {
            if (child.isDirectory()) {
                publish(child);
            }
        }
        return "done";
    }
}

class FileTreeCellRenderer extends TriStateCheckBox implements TreeCellRenderer {
    private final FileSystemView fileSystemView;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    public FileTreeCellRenderer(FileSystemView fileSystemView) {
        super();
        String uiName = getUI().getClass().getName();
        if (uiName.contains("Synth") && System.getProperty("java.version").startsWith("1.7.0")) {
            System.out.println("XXX: FocusBorder bug?, JDK 1.7.0, Nimbus start LnF");
            renderer.setBackgroundSelectionColor(new Color(0, 0, 0, 0));
        }
        this.fileSystemView = fileSystemView;
        panel.setFocusable(false);
        panel.setRequestFocusEnabled(false);
        panel.setOpaque(false);
        panel.add(this, BorderLayout.WEST);
        this.setOpaque(false);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        l.setFont(tree.getFont());
        if (value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                if (node.status == Status.INDETERMINATE) {
                    setIcon(new IndeterminateIcon());
                } else {
                    setIcon(null);
                }
                setText("");

                File file = (File) node.file;
                l.setIcon(fileSystemView.getSystemIcon(file));
                l.setText(fileSystemView.getSystemDisplayName(file));
                l.setToolTipText(file.getPath());
                setSelected(node.status == Status.SELECTED);
            }
            //panel.add(this, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public void updateUI() {
        super.updateUI();
        if (Objects.nonNull(panel)) {
            //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            panel.updateUI();
            //panel.add(this, BorderLayout.WEST);
        }
        setName("Tree.cellRenderer");
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if (System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }
}

class CheckBoxNodeEditor extends TriStateCheckBox implements TreeCellEditor {
    private final FileSystemView fileSystemView;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private File file;
    public CheckBoxNodeEditor(FileSystemView fileSystemView) {
        super();
        this.fileSystemView = fileSystemView;
        this.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
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
        //JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        JLabel l = (JLabel) renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        l.setFont(tree.getFont());
        setOpaque(false);
        if (value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                if (node.status == Status.INDETERMINATE) {
                    setIcon(new IndeterminateIcon());
                } else {
                    setIcon(null);
                }
                file = node.file;
                l.setIcon(fileSystemView.getSystemIcon(file));
                l.setText(fileSystemView.getSystemDisplayName(file));
                setSelected(node.status == Status.SELECTED);
            }
            //panel.add(this, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(file, isSelected() ? Status.SELECTED : Status.DESELECTED);
    }
    @Override public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent && e.getSource() instanceof JTree) {
            MouseEvent me = (MouseEvent) e;
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getPathForLocation(me.getX(), me.getY());
            Rectangle r = tree.getPathBounds(path);
            if (Objects.isNull(r)) {
                return false;
            }
            Dimension d = getPreferredSize();
            r.setSize(new Dimension(d.width, r.height));
            if (r.contains(me.getX(), me.getY())) {
                if (Objects.isNull(file) && System.getProperty("java.version").startsWith("1.7.0")) {
                    System.out.println("XXX: Java 7, only on first run\n" + getBounds());
                    setBounds(new Rectangle(0, 0, d.width, r.height));
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
        if (Objects.nonNull(panel)) {
            //panel.removeAll(); //??? Change to Nimbus LnF, JDK 1.6.0
            panel.updateUI();
            //panel.add(this, BorderLayout.WEST);
        }
        //???#1: JDK 1.6.0 bug??? @see 1.7.0 DefaultTreeCellRenderer#updateUI()
        //if (System.getProperty("java.version").startsWith("1.6.0")) {
        //    renderer = new DefaultTreeCellRenderer();
        //}
    }

    //Copied from AbstractCellEditor
//     protected EventListenerList listenerList = new EventListenerList();
//     protected transient ChangeEvent changeEvent;

    @Override public boolean shouldSelectCell(EventObject anEvent) {
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
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}

class CheckBoxStatusUpdateListener implements TreeModelListener {
    private boolean adjusting;
    @Override public void treeNodesChanged(TreeModelEvent e) {
        if (adjusting) {
            return;
        }
        adjusting = true;
        Object[] children = e.getChildren();
        DefaultTreeModel model = (DefaultTreeModel) e.getSource();

        DefaultMutableTreeNode node;
        CheckBoxNode c; // = (CheckBoxNode) node.getUserObject();
        if (Objects.nonNull(children) && children.length == 1) {
            node = (DefaultMutableTreeNode) children[0];
            c = (CheckBoxNode) node.getUserObject();
            TreePath parent = e.getTreePath();
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) parent.getLastPathComponent();
            while (Objects.nonNull(n)) {
                updateParentUserObject(n);
                DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) n.getParent();
                if (Objects.nonNull(tmp)) {
                    n = tmp;
                } else {
                    break;
                }
            }
            model.nodeChanged(n);
        } else {
            node = (DefaultMutableTreeNode) model.getRoot();
            c = (CheckBoxNode) node.getUserObject();
        }
        updateAllChildrenUserObject(node, c.status);
        model.nodeChanged(node);
        adjusting = false;
    }
    private void updateParentUserObject(DefaultMutableTreeNode parent) {
        Object userObject = parent.getUserObject();
        if (userObject instanceof CheckBoxNode) {
            File file = ((CheckBoxNode) userObject).file;
            int selectedCount = 0;
            int indeterminateCount = 0;
            Enumeration children = parent.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
                CheckBoxNode check = (CheckBoxNode) node.getUserObject();
                if (check.status == Status.INDETERMINATE) {
                    indeterminateCount++;
                    break;
                }
                if (check.status == Status.SELECTED) {
                    selectedCount++;
                }
            }
            if (indeterminateCount > 0) {
                parent.setUserObject(new CheckBoxNode(file));
            } else if (selectedCount == 0) {
                parent.setUserObject(new CheckBoxNode(file, Status.DESELECTED));
            } else if (selectedCount == parent.getChildCount()) {
                parent.setUserObject(new CheckBoxNode(file, Status.SELECTED));
            } else {
                parent.setUserObject(new CheckBoxNode(file));
            }
        }
    }
    private void updateAllChildrenUserObject(DefaultMutableTreeNode root, Status status) {
        Enumeration breadth = root.breadthFirstEnumeration();
        while (breadth.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) breadth.nextElement();
            if (root.equals(node)) {
                continue;
            }
            CheckBoxNode check = (CheckBoxNode) node.getUserObject();
            node.setUserObject(new CheckBoxNode(check.file, status));
        }
    }
    @Override public void treeNodesInserted(TreeModelEvent e)    { /* not needed */ }
    @Override public void treeNodesRemoved(TreeModelEvent e)     { /* not needed */ }
    @Override public void treeStructureChanged(TreeModelEvent e) { /* not needed */ }
}
