// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));

    UIManager.put("Tree.paintLines", Boolean.TRUE);
    UIManager.put("Tree.lineTypeDashed", Boolean.TRUE);
    UIManager.put("Tree.line", Color.GREEN);
    UIManager.put("Tree.hash", Color.RED);

    JTree tree0 = new JTree();

    JTree tree1 = new JTree();
    // tree1.putClientProperty("JTree.lineStyle", "Angled");
    tree1.putClientProperty("JTree.lineStyle", "Horizontal");
    // tree1.putClientProperty("JTree.lineStyle", "None");

    JTree tree2 = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        UIManager.put("Tree.lineTypeDashed", Boolean.FALSE);
        setUI(new BasicTreeUI() {
          private final Stroke horizontalLine = new BasicStroke(2f);
          private final Stroke verticalLine = new BasicStroke(5f);
          @Override public Color getHashColor() {
            return Color.BLUE;
          }

          @Override protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(horizontalLine);
            super.paintHorizontalPartOfLeg(g2, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
            g2.dispose();
          }

          @Override protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(verticalLine);
            super.paintVerticalPartOfLeg(g2, clipBounds, insets, path);
            g2.dispose();
          }
        });
      }
    };

    add(makeTitledPanel("lineTypeDashed", new JScrollPane(tree0)));
    add(makeTitledPanel("lineStyle", new JScrollPane(tree1)));
    add(makeTitledPanel("BasicTreeUI", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
