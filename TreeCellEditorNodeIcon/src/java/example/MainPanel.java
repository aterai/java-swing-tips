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

    JTree tree1 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = r.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (c instanceof JLabel) {
            ((JLabel) c).setIcon(icon);
          }
          return c;
        });
      }
    };
    tree1.setEditable(true);

    JTree tree2 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        r.setOpenIcon(icon);
        r.setClosedIcon(icon);
        r.setLeafIcon(icon);
        setCellRenderer(r);
      }
    };
    tree2.setEditable(true);

    JTree tree3 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        setCellEditor(null);
        super.updateUI();
        DefaultTreeCellRenderer r2 = new DefaultTreeCellRenderer();
        r2.setOpenIcon(icon);
        r2.setClosedIcon(icon);
        r2.setLeafIcon(icon);

        DefaultTreeCellRenderer r3 = new DefaultTreeCellRenderer();
        r3.setOpenIcon(new ColorIcon(Color.GREEN));
        r3.setClosedIcon(new ColorIcon(Color.BLUE));
        r3.setLeafIcon(new ColorIcon(Color.ORANGE));
        setCellRenderer(r2);
        setCellEditor(new DefaultTreeCellEditor(this, r3));
      }
    };
    tree3.setEditable(true);

    add(new JScrollPane(tree1));
    add(new JScrollPane(tree2));
    add(new JScrollPane(tree3));
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
