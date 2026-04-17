// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    // Configure global Tree appearance
    UIManager.put("Tree.paintLines", true);
    UIManager.put("Tree.repaintWholeRow", true);
    UIManager.put("Tree.hash", Color.DARK_GRAY);

    JTree customTree = new JTree() {
      @Override public void updateUI() {
        super.updateUI();
        // Apply the custom UI for whole-row selection
        setUI(new WholeRowSelectableTreeUI());
      }
    };

    add(new JScrollPane(new JTree()));
    add(new JScrollPane(customTree));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

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

/**
 * A custom TreeUI that allows selection by clicking anywhere on the row.
 */
@SuppressWarnings("ReturnCount")
class WholeRowSelectableTreeUI extends BasicTreeUI {
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

    if (initialPath == null || paintingEnum == null) {
      paintDropLine(g);
      return;
    }

    drawingCache.clear();

    // Pass 1: Draw row contents (background and renderer)
    paintRows(g, paintBounds, insets, initialPath, paintingEnum);

    // Pass 2: Draw connecting lines and expand/collapse controls
    paintTreeStructure(g, paintBounds, insets, initialPath);

    paintDropLine(g);
    rendererPane.removeAll();
    drawingCache.clear();
  }

  private void paintRows(
      Graphics g, Rectangle paintBounds, Insets insets,
      TreePath initialPath, Enumeration<?> paintingEnum) {
    int row = treeState.getRowForPath(initialPath);
    int endY = paintBounds.y + paintBounds.height;
    TreeModel treeModel = tree.getModel();

    while (paintingEnum.hasMoreElements()) {
      TreePath path = (TreePath) paintingEnum.nextElement();
      Rectangle bounds = getPathBounds(tree, path);
      if (path == null || bounds == null) {
        break;
      }

      boolean isLeaf = treeModel.isLeaf(path.getLastPathComponent());
      boolean isExpanded = !isLeaf && treeState.getExpandedState(path);
      boolean hasBeenExpanded = !isLeaf && tree.hasBeenExpanded(path);

      paintRow(
          g, paintBounds, insets, bounds, path, row,
          isExpanded, hasBeenExpanded, isLeaf);

      if (bounds.y + bounds.height >= endY) {
        break;
      }
      row++;
    }
  }

  private void paintTreeStructure(
      Graphics g, Rectangle paintBounds, Insets insets, TreePath initialPath) {
    // Cache vertical lines for parent paths
    TreePath parentPath = initialPath.getParentPath();
    while (parentPath != null) {
      paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
      drawingCache.put(parentPath, Boolean.TRUE);
      parentPath = parentPath.getParentPath();
    }

    // Draw structure lines for visible paths
    Enumeration<?> paintingEnum = treeState.getVisiblePathsFrom(initialPath);
    int row = treeState.getRowForPath(initialPath);
    int endY = paintBounds.y + paintBounds.height;
    TreeModel treeModel = tree.getModel();

    while (paintingEnum.hasMoreElements()) {
      TreePath path = (TreePath) paintingEnum.nextElement();
      Rectangle bounds = getPathBounds(tree, path);
      if (path == null || bounds == null) {
        break;
      }

      boolean isLeaf = treeModel.isLeaf(path.getLastPathComponent());
      boolean isExpanded = !isLeaf && treeState.getExpandedState(path);
      boolean hasBeenExpanded = !isLeaf && tree.hasBeenExpanded(path);

      paintHorizontalLinesAndControls(
          g, paintBounds, insets, bounds, path, row,
          isExpanded, hasBeenExpanded, isLeaf);

      if (bounds.y + bounds.height >= endY) {
        break;
      }
      row++;
    }
  }

  @SuppressWarnings("ParameterNumber")
  private void paintHorizontalLinesAndControls(
      Graphics g, Rectangle paintBounds, Insets insets, Rectangle bounds,
      TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    TreePath parentPath = path.getParentPath();
    if (parentPath != null) {
      // Draw vertical line if not already in cache
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
  }

  // @see javax/swing/plaf/basic/BasicTreeUI#paintRow(...)
  @SuppressWarnings("ReturnCount")
  @Override protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    boolean isSelected = tree.isRowSelected(row);
    int fullWidth = tree.getWidth();

    // Handle row background when editing
    if (editingComponent != null && editingRow == row) {
      if (isSelected) {
        drawSelectionBackground(g, 0, bounds.y, fullWidth, bounds.height, Color.PINK);
      }
      return;
    }

    // Initialize cell renderer
    Component renderer = currentCellRenderer.getTreeCellRendererComponent(
        tree, path.getLastPathComponent(), isSelected, isExpanded, isLeaf, row, false);

    // Draw selection highlight across the full width
    if (isSelected) {
      Color bsc = getBackgroundSelectionColor(renderer);
      drawSelectionBackground(g, 0, bounds.y, fullWidth, bounds.height, bsc);
    }

    rendererPane.paintComponent(
        g, renderer, tree, bounds.x, bounds.y, bounds.width, bounds.height, true);

    // Draw focus indicator border
    if (tree.hasFocus() && getLeadSelectionRow() == row) {
      g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
      g.drawRect(0, bounds.y, fullWidth - 1, bounds.height - 1);
    }
  }

  private void drawSelectionBackground(Graphics g, int x, int y, int w, int h, Color color) {
    Color oldColor = g.getColor();
    g.setColor(color);
    g.fillRect(x, y, w, h);
    g.setColor(oldColor);
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
    };
  }

  /**
   * Converts mouse coordinates so that clicks outside the node label (but within the row)
   * are treated as clicks on the node itself.
   */
  @SuppressWarnings({"PMD.OnlyOneReturn", "ReturnCount"})
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
    if (bounds == null) {
      return e;
    }

    // Adjust the mouse X-coordinate to the center of the node bounds to trigger selection
    int centerX = (int) bounds.getCenterX();
    Rectangle rowExtendedBounds = new Rectangle(0, bounds.y, tree.getWidth(), bounds.height);

    if (rowExtendedBounds.contains(e.getPoint())) {
      return new MouseEvent(
          (Component) e.getSource(), e.getID(), e.getWhen(),
          e.getModifiers() | e.getModifiersEx(),
          centerX, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }
    return e;
  }

  private static Color getBackgroundSelectionColor(Component c) {
    return c instanceof DefaultTreeCellRenderer
        ? ((DefaultTreeCellRenderer) c).getBackgroundSelectionColor()
        : Color.LIGHT_GRAY;
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
        Logger.getGlobal().severe(ex::getMessage);
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
