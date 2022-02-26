// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree() {
      private boolean rollover;
      private transient MouseAdapter rolloverHandler;
      @Override public void updateUI() {
        removeMouseListener(rolloverHandler);
        super.updateUI();
        rolloverHandler = new MouseAdapter() {
          @Override public void mouseEntered(MouseEvent e) {
            rollover = true;
            repaint();
          }

          @Override public void mouseExited(MouseEvent e) {
            rollover = false;
            repaint();
          }
        };
        addMouseListener(rolloverHandler);
        setUI(new BasicTreeUI() {
          @Override protected boolean shouldPaintExpandControl(TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
            return rollover && super.shouldPaintExpandControl(
                path, row, isExpanded, hasBeenExpanded, isLeaf);
          }

          @Override protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
            if (rollover) {
              super.paintHorizontalLine(g, c, y, left, right);
            }
          }

          @Override protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
            if (rollover) {
              super.paintVerticalLine(g, c, x, top, bottom);
            }
          }
        });
      }
    };

    Component c1 = makeTitledPanel(new JTree(), "Default");
    Component c2 = makeTitledPanel(tree, "Paint the lines that connect the nodes during rollover");
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, c1, c2);
    sp.setResizeWeight(.5);
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeTitledPanel(Component view, String title) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBorder(BorderFactory.createTitledBorder(title));
    return scroll;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
