package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTree tree = new JTree(new DefaultTreeModel(makeTreeRoot())) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new ProgressBarRenderer());
        setRowHeight(-1);
      }
    };

    JButton button = new JButton("start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      ExecutorService executor = Executors.newCachedThreadPool();
      new SwingWorker<Boolean, Void>() {
        @Override protected Boolean doInBackground() throws InterruptedException {
          DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
          // Java 9: Enumeration<TreeNode> e = root.breadthFirstEnumeration();
          Enumeration<?> e = root.breadthFirstEnumeration();
          while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (!root.equals(node) && !model.isLeaf(node)) {
              executor.execute(new NodeProgressWorker(tree, node));
            }
          }
          executor.shutdown();
          return executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        @Override protected void done() {
          b.setEnabled(true);
        }
      }.execute();
    });

    add(new JScrollPane(tree));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");
    set1.add(new DefaultMutableTreeNode("3333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
    set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("zzzzzzz"));
    set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
    set3.add(new DefaultMutableTreeNode("ccccccccc"));

    set4.add(new DefaultMutableTreeNode("22222222222"));
    set4.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
    set4.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
    set4.add(new DefaultMutableTreeNode("zzzzzzz"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("xxxxxxxxxxxxx"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("222222222222"));
    root.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
    return root;
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

class NodeProgressWorker extends SwingWorker<TreeNode, Integer> {
  private final int lengthOfTask;
  private final int sleepDummy = new Random().nextInt(100) + 1;
  private final JTree tree;
  private final DefaultTreeModel model;
  private final DefaultMutableTreeNode treeNode;

  protected NodeProgressWorker(JTree tree, DefaultMutableTreeNode treeNode) {
    super();
    this.lengthOfTask = 120;
    this.tree = tree;
    this.model = (DefaultTreeModel) tree.getModel();
    this.treeNode = treeNode;
  }

  @Override protected TreeNode doInBackground() throws InterruptedException {
    int current = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      try {
        Thread.sleep(sleepDummy);
      } catch (InterruptedException ex) {
        break;
      }
      publish(100 * current++ / lengthOfTask);
    }
    return treeNode; // sleepDummy * lengthOfTask;
  }

  @Override protected void process(List<Integer> c) {
    String title = treeNode.getUserObject().toString();
    Integer i = (Integer) c.get(c.size() - 1);
    ProgressObject o = new ProgressObject(title, i);
    treeNode.setUserObject(o);
    model.nodeChanged(treeNode);
    // valueForPathChanged(path, str);
  }

  @Override protected void done() {
    try {
      TreeNode n = get();
      tree.expandPath(new TreePath(model.getPathToRoot(n)));
    } catch (InterruptedException | ExecutionException ex) {
      ex.printStackTrace();
    }
  }
}

class ProgressBarRenderer extends DefaultTreeCellRenderer {
  protected int nodeWidth = 100;
  protected static int barHeight = 4;
  private final JProgressBar progress = new JProgressBar(0, 100) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.setSize(nodeWidth, barHeight);
      return d;
    }

    @Override public void updateUI() {
      super.updateUI();
      setUI(new BasicProgressBarUI());
      setStringPainted(true);
      setString("");
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder());
    }
  };
  private final Container renderer = new JPanel(new BorderLayout()) {
    @Override public void updateUI() {
      super.updateUI();
      setOpaque(false);
    }
  };

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    Object o = ((DefaultMutableTreeNode) value).getUserObject();
    if (o instanceof ProgressObject) {
      ProgressObject n = (ProgressObject) o;
      int i = n.getValue();
      progress.setValue(i);

      FontMetrics metrics = c.getFontMetrics(c.getFont());
      int ww = getX() + getIcon().getIconWidth() + getIconTextGap() + metrics.stringWidth(n.title);
      nodeWidth = ww;

      renderer.removeAll();
      renderer.add(c);
      if (i < progress.getMaximum()) {
        renderer.add(progress, BorderLayout.SOUTH);
      }
      // renderer.add(i < progress.getMaximum() ? progress : Box.createVerticalStrut(barHeight), BorderLayout.SOUTH);
      c = renderer;
    }
    return c;
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 18;
    return d;
  }
}

class ProgressObject {
  public final String title;
  private int value;

  protected ProgressObject() {
    this("", 0);
  }

  protected ProgressObject(String title, int value) {
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
