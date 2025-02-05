// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
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
          // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
          Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
              .filter(DefaultMutableTreeNode.class::isInstance)
              .map(DefaultMutableTreeNode.class::cast)
              .filter(node -> !Objects.equals(root, node) && !model.isLeaf(node))
              .forEach(node -> executor.execute(makeWorker(tree, node)));
          // // Java 9: Enumeration<TreeNode> e = root.breadthFirstEnumeration();
          // Enumeration<?> e = root.breadthFirstEnumeration();
          // while (e.hasMoreElements()) {
          //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
          //   if (!root.equals(node) && !model.isLeaf(node)) {
          //     executor.execute(makeWorker(tree, node));
          //   }
          // }
          executor.shutdown();
          return executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        private SwingWorker<TreeNode, Integer> makeWorker(JTree t, DefaultMutableTreeNode n) {
          return new NodeProgressWorker(t, n);
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
    set2.add(new DefaultMutableTreeNode("4444444444444"));
    set2.add(new DefaultMutableTreeNode("5555555"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("zzz"));
    set3.add(new DefaultMutableTreeNode("aaa"));
    set3.add(new DefaultMutableTreeNode("ccc"));

    set4.add(new DefaultMutableTreeNode("22222222222"));
    set4.add(new DefaultMutableTreeNode("ddd"));
    set4.add(new DefaultMutableTreeNode("6666666"));
    set4.add(new DefaultMutableTreeNode("eee"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("9999999"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("888888888"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("222222222222"));
    root.add(new DefaultMutableTreeNode("777777"));
    return root;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
  private final Random rnd = new Random();
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
      doSomething();
      publish(100 * current++ / lengthOfTask);
    }
    return treeNode;
  }

  private void doSomething() throws InterruptedException {
    int iv = rnd.nextInt(100) + 1;
    Thread.sleep(iv);
  }

  @Override protected void process(List<Integer> c) {
    String title = treeNode.getUserObject().toString();
    Integer i = c.get(c.size() - 1);
    ProgressObject o = new ProgressObject(title, i);
    treeNode.setUserObject(o);
    model.nodeChanged(treeNode);
    // model.valueForPathChanged(path, str);
  }

  @Override protected void done() {
    try {
      TreeNode n = get();
      tree.expandPath(new TreePath(model.getPathToRoot(n)));
    } catch (InterruptedException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(tree);
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      ex.printStackTrace();
      UIManager.getLookAndFeel().provideErrorFeedback(tree);
    }
  }
}

class ProgressBarRenderer extends DefaultTreeCellRenderer {
  // protected int nodeWidth = 100;
  protected static final int BAR_HEIGHT = 4;
  private final JProgressBar progress = new JProgressBar() {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      // d.setSize(nodeWidth, BAR_HEIGHT);
      d.height = BAR_HEIGHT;
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
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    Object o = ((DefaultMutableTreeNode) value).getUserObject();
    if (o instanceof ProgressObject) {
      ProgressObject n = (ProgressObject) o;
      int i = n.getValue();
      progress.setValue(i);

      // int titleWidth = c.getFontMetrics(c.getFont()).stringWidth(n.getTitle());
      // int ww = getX() + getIcon().getIconWidth() + getIconTextGap() + titleWidth;
      // nodeWidth = ww;

      renderer.removeAll();
      renderer.add(c);
      if (i < progress.getMaximum()) {
        renderer.add(progress, BorderLayout.SOUTH);
      }
      // Component cmp = i < max ? progress : Box.createVerticalStrut(BAR_HEIGHT);
      // renderer.add(cmp, BorderLayout.SOUTH);
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
  private final String title;
  private final int value;

  // protected ProgressObject() {
  //   this("", 0);
  // }

  protected ProgressObject(String title, int value) {
    this.title = title;
    this.value = value;
  }

  // public void setValue(int value) {
  //   this.value = value;
  // }

  public int getValue() {
    return value;
  }

  // public String getTitle() {
  //   return title;
  // }

  @Override public String toString() {
    return title;
  }
}
