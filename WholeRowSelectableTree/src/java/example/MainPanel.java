// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    UIManager.put("Tree.paintLines", Boolean.TRUE);
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    UIManager.put("Tree.hash", Color.DARK_GRAY);

    JTree tree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new WholeRowSelectableTreeUI());
      }
    };

    add(new JScrollPane(new JTree()));
    add(new JScrollPane(tree));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
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

class WholeRowSelectableTreeUI extends BasicTreeUI {
  @SuppressWarnings({
      "PMD.CognitiveComplexity",
      "PMD.CyclomaticComplexity",
      "PMD.ExcessiveMethodLength",
      "PMD.NPathComplexity",
      "PMD.NcssCount"
  })
  @Override public void paint(Graphics g, JComponent c) {
    // @see javax/swing/plaf/synth/SynthTreeUI#paint(SynthContext ctx, Graphics g)
    // if (tree != c) {
    //   throw new InternalError("incorrect component");
    // }

    // Should never happen if installed for a UI
    // if (treeState == null) {
    //   return;
    // }

    // paintContext = context;
    // updateLeadSelectionRow();
    Rectangle paintBounds = g.getClipBounds();
    Insets insets = tree.getInsets();
    TreePath initialPath = getClosestPathForLocation(tree, 0, paintBounds.y);
    Enumeration<?> paintingEnum = treeState.getVisiblePathsFrom(initialPath);
    int row = treeState.getRowForPath(initialPath);
    int endY = paintBounds.y + paintBounds.height;
    TreeModel treeModel = tree.getModel();
    // SynthContext cellContext = getContext(tree, Region.TREE_CELL);

    drawingCache.clear();
    // setHashColor(context.getStyle().getColor(context, ColorType.FOREGROUND));

    if (initialPath != null && paintingEnum != null) {
      // First pass, draw the rows
      boolean done = false;
      boolean isExpanded;
      boolean hasBeenExpanded;
      boolean isLeaf;
      // Rectangle rowBounds = new Rectangle(0, 0, tree.getWidth(), 0);
      Rectangle bounds;
      TreePath path;
      // TreeCellRenderer renderer = tree.getCellRenderer();
      // DefaultTreeCellRenderer tcr = null;
      // if (renderer instanceof DefaultTreeCellRenderer) {
      //   tcr = (DefaultTreeCellRenderer) renderer;
      // }

      // configureRenderer(cellContext);
      while (!done && paintingEnum.hasMoreElements()) {
        path = (TreePath) paintingEnum.nextElement();
        bounds = getPathBounds(tree, path);
        if (path != null && bounds != null) {
          isLeaf = treeModel.isLeaf(path.getLastPathComponent());
          if (isLeaf) {
            isExpanded = hasBeenExpanded = false;
          } else {
            isExpanded = treeState.getExpandedState(path);
            hasBeenExpanded = tree.hasBeenExpanded(path);
          }
          // rowBounds.y = bounds.y;
          // rowBounds.height = bounds.height;
          // paintRow(renderer, tcr, context, cellContext, g,
          //          paintBounds, insets, bounds, rowBounds, path,
          //          row, isExpanded, hasBeenExpanded, isLeaf);
          paintRow(
              g, paintBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
          if (bounds.y + bounds.height >= endY) {
            done = true;
          }
        } else {
          done = true;
        }
        row++;
      }

      // Draw the connecting lines and controls.
      // Find each parent and have them draw a line to their last child
      // boolean rootVisible = tree.isRootVisible();
      TreePath parentPath = initialPath;
      parentPath = parentPath.getParentPath();
      while (parentPath != null) {
        paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
        drawingCache.put(parentPath, Boolean.TRUE);
        parentPath = parentPath.getParentPath();
      }
      done = false;
      paintingEnum = treeState.getVisiblePathsFrom(initialPath);
      while (!done && paintingEnum.hasMoreElements()) {
        path = (TreePath) paintingEnum.nextElement();
        bounds = getPathBounds(tree, path);
        if (path != null && bounds != null) {
          isLeaf = treeModel.isLeaf(path.getLastPathComponent());
          if (isLeaf) {
            isExpanded = hasBeenExpanded = false;
          } else {
            isExpanded = treeState.getExpandedState(path);
            hasBeenExpanded = tree.hasBeenExpanded(path);
          }
          // See if the vertical line to the parent has been drawn.
          parentPath = path.getParentPath();
          if (parentPath != null) {
            if (drawingCache.get(parentPath) == null) {
              paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
              drawingCache.put(parentPath, Boolean.TRUE);
            }
            paintHorizontalPartOfLeg(
                g, paintBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
          } else if (tree.isRootVisible() && row == 0) {
            paintHorizontalPartOfLeg(
                g, paintBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
          }
          if (shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
            paintExpandControl(
                g, paintBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
          }
          if (bounds.y + bounds.height >= endY) {
            done = true;
          }
        } else {
          done = true;
        }
        row++;
      }
    }

    paintDropLine(g);

    // Empty out the renderer pane, allowing renderers to be gc'ed.
    rendererPane.removeAll();

    drawingCache.clear();
  }

  // @see javax/swing/plaf/basic/BasicTreeUI#paintRow(...)
  @Override protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    boolean isSelected = tree.isRowSelected(row);

    // Don't paint the renderer if editing this row.
    if (editingComponent != null && editingRow == row) {
      if (isSelected) {
        Color oldColor = g.getColor();
        g.setColor(Color.PINK);
        g.fillRect(0, bounds.y, tree.getWidth(), bounds.height);
        g.setColor(oldColor);
      }
      return;
    }

    int leadIndex;
    if (tree.hasFocus()) {
      leadIndex = getLeadSelectionRow();
    } else {
      leadIndex = -1;
    }
    boolean hasFocus = leadIndex == row;

    Object lastPathComponent = path.getLastPathComponent();
    Component component = currentCellRenderer.getTreeCellRendererComponent(
        tree, lastPathComponent, isSelected, isExpanded, isLeaf, row, false);

    if (isSelected) {
      Color oldColor = g.getColor();
      g.setColor(getBackgroundSelectionColor(component));
      g.fillRect(0, bounds.y, tree.getWidth(), bounds.height);
      g.setColor(oldColor);
    }

    rendererPane.paintComponent(
        g, component, tree, bounds.x, bounds.y, bounds.width, bounds.height, true);

    if (hasFocus) {
      g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
      g.drawRect(0, bounds.y, tree.getWidth() - 1, bounds.height - 1);
    }
  }

  private static Color getBackgroundSelectionColor(Component component) {
    Color color = Color.LIGHT_GRAY;
    if (component instanceof DefaultTreeCellRenderer) {
      color = ((DefaultTreeCellRenderer) component).getBackgroundSelectionColor();
    }
    return color;
  }

  @Override protected MouseListener createMouseListener() {
    return new MouseHandler() {
      @Override public void mousePressed(MouseEvent e) {
        super.mousePressed(convertMouseEvent(e));
      }

      @Override public void mouseReleased(MouseEvent e) {
        super.mouseReleased(convertMouseEvent(e));
      }

      @Override public void mouseDragged(MouseEvent e) {
        super.mouseDragged(convertMouseEvent(e));
      }

      private MouseEvent convertMouseEvent(MouseEvent e) {
        if (!tree.isEnabled() || !SwingUtilities.isLeftMouseButton(e) || e.isConsumed()) {
          return e;
        }

        int x = e.getX();
        int y = e.getY();
        TreePath path = getClosestPathForLocation(tree, x, y);
        if (path == null || isLocationInExpandControl(path, x, y)) {
          return e;
        }

        Rectangle bounds = getPathBounds(tree, path);
        int newX = (int) bounds.getCenterX();
        bounds.x = 0;
        bounds.width = tree.getWidth();
        if (bounds.contains(e.getPoint())) {
          return new MouseEvent(
              e.getComponent(), e.getID(), e.getWhen(),
              e.getModifiers() | e.getModifiersEx(),
              newX, e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
        } else {
          return e;
        }
      }
    };
  }
}

final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
