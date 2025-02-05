// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTree tree1 = new JTree();
    expandTree(tree1);
    add(makeTitledPanel("Default", tree1));

    JTree tree2 = new JTree() {
      private transient MouseAdapter handler;

      @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        super.updateUI();
        handler = new DragScrollListener();
        addMouseMotionListener(handler);
        addMouseListener(handler);
      }
    };
    expandTree(tree2);
    add(makeTitledPanel("Drag scroll", tree2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void expandTree(JTree tree) {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(node -> new TreePath(((DefaultMutableTreeNode) node).getPath()))
        .forEach(path -> tree.expandRow(tree.getRowForPath(path)));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
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

class DragScrollListener extends MouseAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point pp = new Point();

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Optional.ofNullable(SwingUtilities.getUnwrappedParent(c))
        .filter(JViewport.class::isInstance).map(JViewport.class::cast)
        .ifPresent(v -> {
          Point cp = SwingUtilities.convertPoint(c, e.getPoint(), v);
          Point vp = v.getViewPosition();
          vp.translate(pp.x - cp.x, pp.y - cp.y);
          ((JComponent) c).scrollRectToVisible(new Rectangle(vp, v.getSize()));
          pp.setLocation(cp);
        });
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hndCursor);
    Optional.ofNullable(SwingUtilities.getUnwrappedParent(c))
        .filter(JViewport.class::isInstance).map(JViewport.class::cast)
        .ifPresent(v -> pp.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), v)));
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }
}
