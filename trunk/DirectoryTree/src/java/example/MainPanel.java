package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        for(File fileSystemRoot: fileSystemView.getRoots()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
            root.add(node);
            for(File file: fileSystemView.getFiles(fileSystemRoot, true)) {
                if(file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }
        }

        JTree tree = new JTree(treeModel);
        tree.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        tree.setRootVisible(false);
        //http://stackoverflow.com/questions/6182110/file-browser-gui
        //java - File Browser GUI - Stack Overflow]
        tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
        tree.setCellRenderer(new FileTreeCellRenderer(tree.getCellRenderer(), fileSystemView));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class FolderSelectionListener implements TreeSelectionListener {
//     private JFrame frame = null;
    private final FileSystemView fileSystemView;
    public FolderSelectionListener(FileSystemView fileSystemView) {
        this.fileSystemView = fileSystemView;
    }
    @Override public void valueChanged(TreeSelectionEvent e) {
        final JTree tree = (JTree)e.getSource();
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
//         if(frame==null) {
//             frame = (JFrame)SwingUtilities.getWindowAncestor(tree);
//             frame.setGlassPane(new LockingGlassPane());
//         }
//         frame.getGlassPane().setVisible(true);

        //final TreePath path = e.getPath();

        if(!node.isLeaf()) {
            return;
        }
        final File parent = (File)node.getUserObject();
        if(!parent.isDirectory()) {
            return;
        }

        final DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        SwingWorker<String, File> worker = new Task(fileSystemView, parent) {
            @Override protected void process(List<File> chunks) {
                if(!tree.isDisplayable()) {
                    cancel(true);
                    return;
                }
                for(File file: chunks) {
                    model.insertNodeInto(new DefaultMutableTreeNode(file), node, node.getChildCount());
                    //node.add(new DefaultMutableTreeNode(file));
                }
                //model.reload(parent); //= model.nodeStructureChanged(parent);
                //tree.expandPath(path);
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
        for(File child: children) {
            if(child.isDirectory()) {
                publish(child);
//                 try{ //Test
//                     Thread.sleep(500);
//                 }catch(InterruptedException ex) {
//                     ex.printStackTrace();
//                 }
            }
        }
        return "done";
    }
}

class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    private final TreeCellRenderer renderer;
    private final FileSystemView fileSystemView;
    public FileTreeCellRenderer(TreeCellRenderer renderer, FileSystemView fileSystemView) {
        super();
        this.renderer = renderer;
        this.fileSystemView = fileSystemView;
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel c = (JLabel)renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
        if(isSelected) {
            c.setOpaque(false);
            c.setForeground(getTextSelectionColor());
            //c.setBackground(Color.BLUE); //getBackgroundSelectionColor());
        }else{
            c.setOpaque(true);
            c.setForeground(getTextNonSelectionColor());
            c.setBackground(getBackgroundNonSelectionColor());
        }
        if(value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object o = node.getUserObject();
            if(o instanceof File) {
                File file = (File)o;
                c.setIcon(fileSystemView.getSystemIcon(file));
                c.setText(fileSystemView.getSystemDisplayName(file));
                c.setToolTipText(file.getPath());
            }
        }
        return c;
    }
}

// class LockingGlassPane extends JComponent {
//     public LockingGlassPane() {
//         setOpaque(false);
//         setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
//             @Override public boolean accept(Component c) { return false; }
//         });
//         addKeyListener(new KeyAdapter() {});
//         addMouseListener(new MouseAdapter() {});
//         requestFocusInWindow();
//         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//     }
//     @Override public void setVisible(boolean flag) {
//         super.setVisible(flag);
//         setFocusTraversalPolicyProvider(flag);
//     }
// }
