package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        for(File fileSystemRoot: fileSystemView.getRoots()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new CheckBoxNode(fileSystemRoot, false));
            root.add(node);
            for(File file: fileSystemView.getFiles(fileSystemRoot, true)) {
                if(file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(new CheckBoxNode(file, false)));
                }
            }
        }

        JTree tree = new JTree(treeModel) {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                //???#1: JDK 1.6.0 bug??? Nimbus LnF
                setCellRenderer(new FileTreeCellRenderer(fileSystemView));
                setCellEditor(new CheckBoxNodeEditor(fileSystemView));
            }
        };
        tree.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
        //tree.setCellRenderer(new FileTreeCellRenderer(fileSystemView));
        //tree.setCellEditor(new CheckBoxNodeEditor(fileSystemView));
        tree.setEditable(true);

        tree.expandRow(0);
        //tree.setToggleClickCount(1);

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

class FolderSelectionListener implements TreeSelectionListener{
    private final FileSystemView fileSystemView;
    public FolderSelectionListener(FileSystemView fileSystemView) {
        this.fileSystemView = fileSystemView;
    }
    @Override public void valueChanged(TreeSelectionEvent e) {
        final JTree tree = (JTree)e.getSource();
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
        final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

        if(!node.isLeaf()) return;
        final File parent = ((CheckBoxNode)node.getUserObject()).file;
        if(!parent.isDirectory()) return;

        SwingWorker<String, File> worker = new SwingWorker<String, File>() {
            @Override public String doInBackground() {
                File[] children = fileSystemView.getFiles(parent, true);
                for(File child: children) {
                    if(child.isDirectory()) {
                        publish(child);
                    }
                }
                return "done";
            }
            @Override protected void process(java.util.List<File> chunks) {
                for(File file: chunks) {
                    node.add(new DefaultMutableTreeNode(new CheckBoxNode(file, false)));
                }
                model.nodeStructureChanged(node);
            }
        };
        worker.execute();
    }
}

class FileTreeCellRenderer extends JCheckBox implements TreeCellRenderer {
    private final FileSystemView fileSystemView;
    private final JPanel panel = new JPanel(new BorderLayout());
    private JTree tree = null;
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    public FileTreeCellRenderer(FileSystemView fileSystemView) {
        super();
        this.fileSystemView = fileSystemView;
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
                File file = (File)node.file;
                l.setIcon(fileSystemView.getSystemIcon(file));
                l.setText(fileSystemView.getSystemDisplayName(file));
                l.setToolTipText(file.getPath());
                setText("");
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

class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
    private final FileSystemView fileSystemView;
    private final JPanel panel = new JPanel(new BorderLayout());
    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
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
        //JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        JLabel l = (JLabel)renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        l.setFont(tree.getFont());
        setOpaque(false);
        if(value != null && value instanceof DefaultMutableTreeNode) {
            this.setEnabled(tree.isEnabled());
            this.setFont(tree.getFont());
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                file = node.file;
                l.setIcon(fileSystemView.getSystemIcon(file));
                l.setText(fileSystemView.getSystemDisplayName(file));
                setSelected(node.selected);
            }
            //panel.add(this, BorderLayout.WEST);
            panel.add(l);
            return panel;
        }
        return l;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(file, isSelected());
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
                if(file==null && System.getProperty("java.version").startsWith("1.7.0")) {
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

class CheckBoxNode {
    public final File file;
    public final boolean selected;
    public CheckBoxNode(File file, boolean selected) {
        this.file = file;
        this.selected = selected;
    }
    @Override public String toString() {
        return file.getName();
    }
}
