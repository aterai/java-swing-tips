// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel searchBox = new JPanel(new BorderLayout());
    LayoutAnimator handler = new LayoutAnimator(searchBox);
    JPanel p = new JPanel(handler) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.add(searchBox, BorderLayout.NORTH);

    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JScrollPane scroll = new JScrollPane(tree);
    scroll.setBorder(BorderFactory.createTitledBorder("Find... Ctrl+F"));
    p.add(scroll);

    JTextField field = makeTextField();
    Action findNextAction = new FindNextAction(tree, field);
    JButton button = new JButton(findNextAction);
    button.setFocusable(false);
    button.setToolTipText("Find next");
    button.setText("v");

    searchBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    searchBox.add(field);
    searchBox.add(button, BorderLayout.EAST);
    searchBox.setVisible(false);

    Timer animator = new Timer(5, handler);
    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap im = p.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers), "open-search-box");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-search-box");

    ActionMap am = p.getActionMap();
    am.put("open-search-box", new AbstractAction("Show/Hide Search Box") {
      @Override public void actionPerformed(ActionEvent e) {
        if (!animator.isRunning()) {
          handler.setShowing(!searchBox.isVisible());
          searchBox.setVisible(true);
          animator.start();
        }
      }
    });
    am.put("close-search-box", new AbstractAction("Hide Search Box") {
      @Override public void actionPerformed(ActionEvent e) {
        if (!animator.isRunning()) {
          handler.setShowing(false);
          animator.start();
        }
      }
    });

    field.getActionMap().put("find-next", findNextAction);
    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    field.getInputMap(WHEN_FOCUSED).put(enterKey, "find-next");
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private JTextField makeTextField() {
    return new JTextField("b", 10) {
      private transient AncestorListener listener;

      @Override public void updateUI() {
        removeAncestorListener(listener);
        super.updateUI();
        listener = new FocusAncestorListener();
        addAncestorListener(listener);
      }
    };
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LayoutAnimator extends BorderLayout implements ActionListener {
  private boolean showing = true;
  private final JComponent component;
  private int yy;
  private int counter;

  protected LayoutAnimator(JComponent c) {
    super();
    component = c;
  }

  public void setShowing(boolean b) {
    showing = b;
  }

  @Override public void actionPerformed(ActionEvent e) {
    Timer animator = (Timer) e.getSource();
    int height = component.getPreferredSize().height;
    if (showing) {
      yy = (int) (.5 + AnimationUtils.easeInOut(++counter / (double) height) * height);
      if (yy >= height) {
        yy = height;
        animator.stop();
      }
    } else {
      yy = (int) (.5 + AnimationUtils.easeInOut(--counter / (double) height) * height);
      if (yy <= 0) {
        yy = 0;
        animator.stop();
        component.setVisible(false);
      }
    }
    component.revalidate();
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int width = parent.getWidth();
      int height = parent.getHeight();
      int top = insets.top;
      int bottom = height - insets.bottom;
      int left = insets.left;
      int right = width - insets.right;
      Component nc = getLayoutComponent(parent, NORTH);
      if (Objects.nonNull(nc)) {
        Dimension d = nc.getPreferredSize();
        int vsw = UIManager.getInt("ScrollBar.width");
        nc.setBounds(right - d.width - vsw, yy - d.height, d.width, d.height);
      }
      Component cc = getLayoutComponent(parent, CENTER);
      if (Objects.nonNull(cc)) {
        cc.setBounds(left, top, right - left, bottom - top);
      }
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}

class FindNextAction extends AbstractAction {
  private final JTree tree;
  private final JTextField field;
  private final List<TreePath> matchedResults = new ArrayList<>();

  protected FindNextAction(JTree tree, JTextField field) {
    super();
    this.tree = tree;
    this.field = field;
  }

  @Override public void actionPerformed(ActionEvent e) {
    matchedResults.clear();
    TreePath selectedPath = tree.getSelectionPath();
    tree.clearSelection();
    searchTree(tree, tree.getPathForRow(0), field.getText(), matchedResults);
    if (!matchedResults.isEmpty()) {
      int nextIndex = 0;
      int size = matchedResults.size();
      for (int i = 0; i < size; i++) {
        if (matchedResults.get(i).equals(selectedPath)) {
          nextIndex = i + 1 < size ? i + 1 : 0;
          break;
        }
      }
      TreePath p = matchedResults.get(nextIndex);
      tree.addSelectionPath(p);
      tree.scrollPathToVisible(p);
    }
  }

  private static void searchTree(JTree tree, TreePath path, String q, List<TreePath> results) {
    Object o = path.getLastPathComponent();
    if (o instanceof TreeNode) {
      TreeNode node = (TreeNode) o;
      if (node.toString().startsWith(q)) {
        results.add(path);
        tree.expandPath(path.getParentPath());
      }
      if (!node.isLeaf()) {
        // Java 9: Collections.list(node.children())
        Collections.list((Enumeration<?>) node.children())
            .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q, results));
      }
    }
  }
}

final class AnimationUtils {
  private static final int N = 3;

  private AnimationUtils() {
    /* Singleton */
  }

  // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
  // Math: EaseIn EaseOut, EaseInOut and Bézier curves | Anima Entertainment GmbH
  // public static double easeIn(double t) {
  //   // range: 0.0 <= t <= 1.0
  //   return Math.pow(t, N);
  // }

  // public static double easeOut(double t) {
  //   return Math.pow(t - 1d, N) + 1d;
  // }

  public static double easeInOut(double t) {
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * intPow(t * 2d, N) : .5 * (intPow(t * 2d - 2d, N) + 2d);
  }

  // https://wiki.c2.com/?IntegerPowerAlgorithm
  public static double intPow(double base0, int exp0) {
    if (exp0 < 0) {
      throw new IllegalArgumentException("exp0 must be a positive integer or zero");
    }
    double base = base0;
    int exp = exp0;
    double result = 1d;
    for (; exp > 0; base *= base, exp >>>= 1) {
      if ((exp & 1) != 0) {
        result *= base;
      }
    }
    return result;
  }
}
