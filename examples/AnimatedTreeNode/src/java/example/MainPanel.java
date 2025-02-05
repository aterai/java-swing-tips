// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ImageIcon icon = makeImageIcon("example/restore_to_background_color.gif");
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    Object uo0 = new IconNode("default", icon);
    DefaultMutableTreeNode s0 = new DefaultMutableTreeNode(uo0);
    Object uo1 = new IconNode("setImageObserver", icon);
    DefaultMutableTreeNode s1 = new DefaultMutableTreeNode(uo1);
    root.add(s0);
    root.add(s1);
    JTree tree = new JTree(new DefaultTreeModel(root)) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        TreeCellRenderer renderer = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = renderer.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (c instanceof JLabel && value instanceof DefaultMutableTreeNode) {
            updateLabel((JLabel) c, ((DefaultMutableTreeNode) value).getUserObject());
          }
          return c;
        });
      }
    };
    TreePath path = new TreePath(s1.getPath());
    // Wastefulness: icon.setImageObserver((ImageObserver) tree);
    icon.setImageObserver((img, flags, x, y, w, h) -> {
      if (!tree.isShowing()) {
        return false;
      }
      Rectangle cellRect = tree.getPathBounds(path);
      if ((flags & (FRAMEBITS | ALLBITS)) != 0 && Objects.nonNull(cellRect)) {
        tree.repaint(cellRect);
      }
      return (flags & (ALLBITS | ABORT)) == 0;
    });
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateLabel(JLabel label, Object o) {
    if (o instanceof IconNode) {
      IconNode uo = (IconNode) o;
      label.setText(Objects.toString(uo.getTitle(), ""));
      label.setIcon(uo.getIcon());
    }
  }

  public static ImageIcon makeImageIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path))
        .map(ImageIcon::new)
        .orElseGet(() -> new ImageIcon(makeMissingImage()));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class IconNode {
  private final String title;
  private final Icon icon;

  // protected NodeObject(String title) {
  //   this(title, null);
  // }

  protected IconNode(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }
}
