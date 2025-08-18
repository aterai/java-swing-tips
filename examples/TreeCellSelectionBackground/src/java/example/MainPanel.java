// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

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
    JTree tree = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        TreeCellRenderer renderer = getCellRenderer();
        setCellRenderer(new BgcTreeCellRenderer(renderer));
      }
    };
    tree.putClientProperty("Nimbus.Overrides", def);
    tree.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);
    tree.setBackground(Color.WHITE);

    JScrollPane s1 = new JScrollPane(new JTree());
    JScrollPane s2 = new JScrollPane(tree);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    split.setResizeWeight(.5);

    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
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

class BgcTreeCellRenderer implements TreeCellRenderer {
  private final Color selectionBgc = new Color(0x39_69_8A);
  private final TreeCellRenderer renderer;

  protected BgcTreeCellRenderer(TreeCellRenderer renderer) {
    this.renderer = renderer;
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, isLeaf, row, hasFocus);
    if (selected) {
      c.setBackground(selectionBgc);
    }
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(selected);
    }
    return c;
  }
}
