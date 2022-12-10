// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    int ow = UIManager.getIcon("Tree.openIcon").getIconWidth();
    int iw = 32;
    int ih = 24;

    Icon icon0 = new ColorIcon(Color.GREEN, new Dimension(iw, ih));
    JTree tree0 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setRowHeight(0);
        TreeCellRenderer r = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = r.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (c instanceof JLabel && Objects.equals(value, tree.getModel().getRoot())) {
            ((JLabel) c).setIcon(icon0);
          }
          return c;
        });
      }
    };
    tree0.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    Icon icon1 = new ColorIcon(Color.GREEN, new Dimension(ow, ih));
    Icon icon2 = new ColorIcon(new Color(0x55_00_00_AA, true), new Dimension(iw, ih));
    JTree tree = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setRowHeight(0);
        TreeCellRenderer r = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = r.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (c instanceof JLabel && Objects.equals(value, tree.getModel().getRoot())) {
            JLabel label = (JLabel) c;
            label.setIcon(icon1);
            label.setIconTextGap(2 + (iw - icon1.getIconWidth()) / 2);
          }
          return c;
        });
      }
    };
    // TEST:
    // tree.setBorder(BorderFactory.createMatteBorder(1, 1 + (iw - ow) / 2, 1, 1, Color.RED));
    tree.setBorder(BorderFactory.createEmptyBorder(1, 1 + (iw - ow) / 2, 1, 1));

    LayerUI<JTree> layerUI = new LayerUI<JTree>() {
      @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        Graphics2D g2 = (Graphics2D) g.create();
        icon2.paintIcon(c, g2, 1, 1);
        g2.dispose();
      }
    };

    add(new JScrollPane(tree0));
    add(new JScrollPane(new JLayer<>(tree, layerUI)));
    setPreferredSize(new Dimension(320, 240));
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

class ColorIcon implements Icon {
  private final Color color;
  private final Dimension dim;

  protected ColorIcon(Color color, Dimension dim) {
    this.color = color;
    this.dim = dim;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setColor(color);
    g2.fillRect(1, 1, dim.width - 2, dim.height - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return dim.width;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }
}
