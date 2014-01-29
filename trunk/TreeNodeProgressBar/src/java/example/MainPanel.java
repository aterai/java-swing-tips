package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree() {
        @Override public void updateUI() {
            super.updateUI();
            setCellRenderer(new ProgressBarRenderer());
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        tree.setModel(new DefaultTreeModel(makeTreeRoot()));

        add(new JScrollPane(tree));
        add(new JButton(new AbstractAction("start") {
            @Override public void actionPerformed(ActionEvent ev) {
                final ExecutorService executor = Executors.newCachedThreadPool();
                final JButton b = (JButton)ev.getSource();
                b.setEnabled(false);
                (new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() throws InterruptedException {
                         DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                         DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
                         Enumeration e = root.breadthFirstEnumeration();
                         while(e.hasMoreElements()) {
                             DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
                             if(!root.equals(node ) && !model.isLeaf(node)) {
                                 executor.execute(new NodeProgressWorker(tree, node));
                             }
                         }
                         executor.shutdown();
                         return executor.awaitTermination(1, TimeUnit.MINUTES);
                     }
                    @Override protected void done() {
                        b.setEnabled(true);
                    }
                }).execute();
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static DefaultMutableTreeNode makeTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");

        set1.add(new DefaultMutableTreeNode("3333333333333333"));
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(set4);
        set1.add(new DefaultMutableTreeNode("222222"));
        set1.add(new DefaultMutableTreeNode("222222222"));
        set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set3.add(new DefaultMutableTreeNode("zzzzzzz"));
        set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
        set3.add(new DefaultMutableTreeNode("ccccccccc"));

        set4.add(new DefaultMutableTreeNode("22222222222"));
        set4.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set4.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set4.add(new DefaultMutableTreeNode("zzzzzzz"));

        root.add(new DefaultMutableTreeNode("xxxxxxxxxxxxx"));
        root.add(set3);
        root.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        root.add(set1);
        root.add(set2);
        root.add(new DefaultMutableTreeNode("222222222222"));
        root.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        return root;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class NodeProgressWorker extends SwingWorker<TreeNode, Integer> {
    private static final int lengthOfTask = 120;
    private final int sleepDummy = new Random().nextInt(100) + 1;
    private final JTree tree;
    private final DefaultTreeModel model;
    private final DefaultMutableTreeNode treeNode;
    public NodeProgressWorker(JTree tree, DefaultMutableTreeNode treeNode) {
        super();
        this.tree = tree;
        this.model = (DefaultTreeModel)tree.getModel();
        this.treeNode = treeNode;
    }
    @Override protected TreeNode doInBackground() throws InterruptedException {
         int current = 0;
         while(current <= lengthOfTask && !isCancelled()) {
             try{
                 Thread.sleep(sleepDummy);
             }catch(InterruptedException ie) {
                 break;
             }
             publish(100 * current++ / lengthOfTask);
         }
         return treeNode; //sleepDummy * lengthOfTask;
     }
    @Override protected void process(List<Integer> c) {
        String title = treeNode.getUserObject().toString();
        Integer i = (Integer)c.get(c.size() - 1);
        ProgressObject o = new ProgressObject(title, i);
        treeNode.setUserObject(o);
        model.nodeChanged(treeNode);
        //valueForPathChanged(path, str);
    }
    @Override protected void done() {
        try{
            TreeNode n = get();
            tree.expandPath(new TreePath(model.getPathToRoot(n)));
        }catch(InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }
}

class ProgressBarRenderer extends DefaultTreeCellRenderer {
    private int nodeWidth = 100;
    private static int barHeight = 4;
    private final JProgressBar b = new JProgressBar(0, 100) {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = barHeight;
            d.width  = nodeWidth;
            return d;
        }
        @Override public void updateUI() {
            super.updateUI();
            setUI(new BasicProgressBarUI());
        }
    };
    private final JPanel p = new JPanel(new BorderLayout());
    public ProgressBarRenderer() {
        super();
        b.setOpaque(false);
        p.setOpaque(false);
        b.setStringPainted(true);
        b.setString("");
        b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        Object o = ((DefaultMutableTreeNode)value).getUserObject();
        if(o instanceof ProgressObject) {
            ProgressObject n = (ProgressObject)o;
            int i = n.getValue();
            b.setValue(i);

            FontMetrics metrics = c.getFontMetrics(c.getFont());
            int ww = getX() + getIcon().getIconWidth() + getIconTextGap() + metrics.stringWidth(n.title);
            nodeWidth = ww;

            p.removeAll();
            p.add(c);
            p.add(i<100 ? b : Box.createVerticalStrut(barHeight), BorderLayout.SOUTH);
            c = p;
        }
        return c;
    }
}

class ProgressObject {
    public final String title;
    public int value;
    public ProgressObject() {
        this("", 0);
    }
    public ProgressObject(String title, int value) {
        this.title = title;
        this.value = value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    @Override public String toString() {
        return title;
    }
}
