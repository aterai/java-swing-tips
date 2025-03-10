// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 2, 2));
    JTree tree = new JTree();
    tree.setRowHeight(20);
    add(makeScrollPane(tree));
    add(makeScrollPane(new RoundedSelectionTree()));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeScrollPane(Component view) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBackground(Color.WHITE);
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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

class RoundedSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0xC8_00_78_D7, true);

  @Override protected void paintComponent(Graphics g) {
    int[] selectionRows = getSelectionRows();
    if (selectionRows != null) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(SELECTED_COLOR);
      Rectangle innerArea = SwingUtilities.calculateInnerArea(this, null);
      Area area = new Area();
      Arrays.stream(selectionRows)
          .mapToObj(this::getRowBounds)
          .map(r -> new Rectangle(innerArea.x, r.y, innerArea.width, r.height))
          .forEach(r -> area.add(new Area(r)));
      int arc = 10;
      for (Area a : singularization(area)) {
        Rectangle r = a.getBounds();
        g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, arc, arc);
      }
      // if (hasFocus()) {
      //   Optional.ofNullable(getLeadSelectionPath()).ifPresent(p -> {
      //     Rectangle r = getRowBounds(getRowForPath(p));
      //     g2.setPaint(SELECTED_COLOR.darker());
      //     g2.drawRoundRect(0, r.y, getWidth() - 1, r.height - 1, arc, arc);
      //   });
      // }
      g2.dispose();
    }
    super.paintComponent(g);
  }

  private static List<Area> singularization(Area rect) {
    List<Area> list = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = rect.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          list.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }

  @Override public void updateUI() {
    super.updateUI();
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    setCellRenderer(new TransparentTreeCellRenderer());
    setOpaque(false);
    setRowHeight(20);
    UIDefaults d = new UIDefaults();
    String key = "Tree:TreeCell[Enabled+Selected].backgroundPainter";
    d.put(key, new TransparentTreeCellPainter());
    putClientProperty("Nimbus.Overrides", d);
    putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);
    addTreeSelectionListener(e -> repaint());
  }
}

class TransparentTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final Color ALPHA_OF_ZERO = new Color(0x0, true);

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, false);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public Color getBackgroundSelectionColor() {
    return ALPHA_OF_ZERO;
  }

  @Override public Color getBackgroundNonSelectionColor() {
    return getBackgroundSelectionColor();
  }
}

class TransparentTreeCellPainter extends AbstractRegionPainter {
  @Override protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
    // Do nothing
  }

  @Override protected final PaintContext getPaintContext() {
    return null;
  }
}

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
