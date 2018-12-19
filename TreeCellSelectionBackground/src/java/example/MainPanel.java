// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    UIDefaults def = new UIDefaults();
    // def.put("Tree.selectionBackground", Color.WHITE);
    // def.put("Tree.selectionForeground", Color.GREEN);
    // def.put("Tree.opaque", Boolean.FALSE);
    // def.put("Tree:TreeCell[Enabled+Selected].textForeground", Color.GREEN);
    // def.put("Tree.rendererFillBackground", true);
    // def.put("Tree.repaintWholeRow", true);
    // def.put("Tree:TreeCell[Enabled+Selected].backgroundPainter", new Painter<JComponent>() {
    //   @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
    //     // g.setPaint(Color.RED);
    //     // g.fillRect(0, 0, w, h);
    //   }
    // });
    // def.put("Tree:TreeCell[Focused+Selected].backgroundPainter", new Painter<JComponent>() {
    //   @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
    //     // g.setPaint(Color.RED);
    //     // g.fillRect(0, 0, w, h);
    //   }
    // });
    // def.put("Tree[Enabled].collapsedIconPainter", null);
    // def.put("Tree[Enabled].expandedIconPainter", null);
    // def.put("Tree[Enabled+Selected].collapsedIconPainter", null);
    // def.put("Tree[Enabled+Selected].expandedIconPainter", null);
    JTree tree = new JTree();
    tree.putClientProperty("Nimbus.Overrides", def);
    tree.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
    tree.setBackground(Color.WHITE);

    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      private final Color selectionBackground = new Color(0x39_69_8A);
      @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
        JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
        if (selected) {
          c.setBackground(selectionBackground);
          c.setOpaque(true);
        } else {
          c.setOpaque(false);
        }
        return c;
      }
    });

    JSplitPane split = new JSplitPane();
    split.setResizeWeight(.5);
    split.setLeftComponent(new JScrollPane(new JTree()));
    split.setRightComponent(new JScrollPane(tree));
    add(split);
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
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
