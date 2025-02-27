// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = makePopupMenu();
    JTree tree = new JTree();
    tree.setComponentPopupMenu(popup);
    add(new JScrollPane(tree));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopupMenu() {
    JPopupMenu p = new JPopupMenu();
    p.add("add").addActionListener(e -> {
      JTree tree = (JTree) p.getInvoker();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      TreePath path = tree.getSelectionPath();
      if (path != null) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("New node");
        model.insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
      }
    });
    p.addSeparator();
    p.add("remove").addActionListener(e -> {
      JTree tree = (JTree) p.getInvoker();
      TreePath[] paths = tree.getSelectionPaths();
      if (paths != null) {
        for (TreePath path : paths) {
          removeNode(tree, path);
        }
      }
    });
    p.addSeparator();
    p.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    p.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    return p;
  }

  private static void removeNode(JTree tree, TreePath path) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    if (!node.isRoot()) {
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      model.removeNodeFromParent(node);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("PopupMenuUI", "example.RoundedPopupMenuUI");
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

// final class RoundedPopupMenu extends JPopupMenu {
//   @Override public void updateUI() {
//     setBorder(null);
//     super.updateUI();
//     setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//   }
//
//   @Override public boolean isOpaque() {
//     return false;
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     // super.paintComponent(g);
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//         RenderingHints.VALUE_ANTIALIAS_ON);
//     g2.setPaint(getBackground());
//     Shape s = makeShape();
//     g2.fill(s);
//     g2.setPaint(Color.GRAY);
//     g2.draw(s);
//     g2.dispose();
//   }
//
//   @Override public void show(Component c, int x, int y) {
//     EventQueue.invokeLater(() -> {
//       Window top = SwingUtilities.getWindowAncestor(this);
//       // Popup$HeavyWeightWindow
//       if (top != null && top.getType() == Window.Type.POPUP) {
//         top.setBackground(new Color(0x0, true));
//       }
//     });
//     super.show(c, x, y);
//   }
//
//   private Shape makeShape() {
//     float w = getWidth() - 1f;
//     float h = getHeight() - 1f;
//     Insets i = getInsets();
//     float r = Math.min(i.top + i.left, i.bottom + i.right);
//     return new RoundRectangle2D.Float(0f, 0f, w, h, r, r);
//   }
// }

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
