// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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
        Color selectionBgc = new Color(0x39_69_8A);
        TreeCellRenderer r = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, isLeaf, row, focused) -> {
          Component c = r.getTreeCellRendererComponent(
              tree, value, selected, expanded, isLeaf, row, focused);
          if (selected) {
            c.setBackground(selectionBgc);
          }
          if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(selected);
          }
          return c;
        });
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
