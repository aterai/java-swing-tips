// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));

    Icon icon = new ColorIcon(Color.RED);

    JTree tree1 = new JTree();
    tree1.setEditable(true);
    tree1.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
        JLabel c = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
        c.setIcon(icon);
        return c;
      }
    });

    JTree tree2 = new JTree();
    tree2.setEditable(true);
    DefaultTreeCellRenderer renderer2 = new DefaultTreeCellRenderer();
    renderer2.setOpenIcon(icon);
    renderer2.setClosedIcon(icon);
    renderer2.setLeafIcon(icon);
    tree2.setCellRenderer(renderer2);

    JTree tree3 = new JTree();
    tree3.setEditable(true);
    DefaultTreeCellRenderer renderer3 = new DefaultTreeCellRenderer();
    renderer3.setOpenIcon(new ColorIcon(Color.GREEN));
    renderer3.setClosedIcon(new ColorIcon(Color.BLUE));
    renderer3.setLeafIcon(new ColorIcon(Color.ORANGE));
    tree3.setCellRenderer(renderer2);
    tree3.setCellEditor(new DefaultTreeCellEditor(tree3, renderer3));

    add(new JScrollPane(tree1));
    add(new JScrollPane(tree2));
    add(new JScrollPane(tree3));
    setPreferredSize(new Dimension(320, 240));
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
