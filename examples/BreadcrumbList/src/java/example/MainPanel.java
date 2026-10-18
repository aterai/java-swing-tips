// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Container breadcrumb = createContainer(10 + 1);

    JTree tree = new JTree();
    tree.setSelectionRow(0);
    tree.addTreeSelectionListener(e -> {
      Object o = tree.getLastSelectedPathComponent();
      // A leaf node is not added to the breadcrumb trail, keep the current one
      if (o instanceof TreeNode && !((TreeNode) o).isLeaf()) {
        initBreadcrumbList(breadcrumb, tree);
      }
    });

    initBreadcrumbList(breadcrumb, tree);
    add(new JLayer<>(breadcrumb, new BreadcrumbLayerUI<>()), BorderLayout.NORTH);

    Component c = createBreadcrumbList(Arrays.asList("aaa", "bb", "c"));
    add(c, BorderLayout.SOUTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Container createContainer(int overlap) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0)) {
      @Override public boolean isOptimizedDrawingEnabled() {
        // The child buttons overlap each other
        return false;
      }
    };
    p.setBorder(BorderFactory.createEmptyBorder(4, overlap + 4, 4, 4));
    p.setOpaque(false);
    return p;
  }

  private static void initBreadcrumbList(Container p, JTree tree) {
    p.removeAll();
    ButtonGroup bg = new ButtonGroup();
    Object[] nodes = Optional.ofNullable(tree.getSelectionPath())
        .map(TreePath::getPath).orElseGet(() -> new Object[0]);
    for (int i = 0; i < nodes.length; i++) {
      TreePath path = copyTreePath(nodes, i + 1);
      AbstractButton b = createButton(nodes[i].toString(), Color.ORANGE);
      b.addActionListener(e -> tree.setSelectionPath(path));
      p.add(b);
      bg.add(b);
      // Highlight the button of the current (last) node
      b.setSelected(i == nodes.length - 1);
    }
    p.revalidate();
    p.repaint();
  }

  private static TreePath copyTreePath(Object[] nodes, int newLength) {
    return new TreePath(Arrays.copyOf(nodes, newLength));
  }

  private static Component createBreadcrumbList(List<String> list) {
    Container p = createContainer(5 + 1);
    ButtonGroup bg = new ButtonGroup();
    list.forEach(title -> {
      AbstractButton b = createButton(title, Color.PINK);
      p.add(b);
      bg.add(b);
    });
    return p;
  }

  private static AbstractButton createButton(String title, Color color) {
    AbstractButton b = new JRadioButton(title) {
      @Override public boolean contains(int x, int y) {
        // Respond to the mouse only inside the arrow shape of the icon
        return Optional.ofNullable(getIcon())
            .filter(ArrowToggleButtonBarCellIcon.class::isInstance)
            .map(i -> ((ArrowToggleButtonBarCellIcon) i).getShape())
            .map(s -> s.contains(x, y))
            .orElseGet(() -> super.contains(x, y));
      }
    };
    b.setIcon(new ArrowToggleButtonBarCellIcon());
    b.setContentAreaFilled(false);
    b.setBorder(BorderFactory.createEmptyBorder());
    b.setVerticalAlignment(SwingConstants.CENTER);
    b.setVerticalTextPosition(SwingConstants.CENTER);
    b.setHorizontalAlignment(SwingConstants.CENTER);
    b.setHorizontalTextPosition(SwingConstants.CENTER);
    b.setFocusPainted(false);
    b.setOpaque(false);
    b.setBackground(color);
    return b;
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

// https://ateraimemo.com/Swing/ToggleButtonBar.html
// https://java-swing-tips.blogspot.com/2012/11/make-togglebuttonbar-with-jradiobuttons.html
class ArrowToggleButtonBarCellIcon implements Icon {
  public static final int TH = 10; // The height of a triangle
  private static final int HEIGHT = TH * 2 + 1;
  private static final int WIDTH = 100;
  private Shape shape;

  public Shape getShape() {
    return shape;
  }

  protected Shape createShape(Container parent, Component c, int x, int y) {
    int w = c.getWidth() - 1;
    int h = c.getHeight() - 1;
    double h2 = Math.round(h * .5);
    double w2 = TH;
    Path2D p = new Path2D.Double();
    p.moveTo(0d, 0d);
    p.lineTo(w - w2, 0d);
    p.lineTo(w, h2);
    p.lineTo(w - w2, h);
    p.lineTo(0d, h);
    if (!Objects.equals(c, parent.getComponent(0))) {
      p.lineTo(w2, h2);
    }
    p.closePath();
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Container parent = c.getParent();
    if (Objects.nonNull(parent)) {
      shape = createShape(parent, c, x, y);
      boolean selected = isSelectedOrRollover(c);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(selected ? c.getBackground() : parent.getBackground());
      g2.fill(shape);
      g2.setPaint(selected ? Color.GRAY : Color.GRAY.brighter());
      g2.draw(shape);
      g2.dispose();
    }
  }

  private static boolean isSelectedOrRollover(Component c) {
    boolean b = false;
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      b = m.isSelected() || m.isRollover();
    }
    return b;
  }

  @Override public int getIconWidth() {
    return WIDTH;
  }

  @Override public int getIconHeight() {
    return HEIGHT;
  }
}

class BreadcrumbLayerUI<V extends Component> extends LayerUI<V> {
  private transient Shape shape;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    Optional.ofNullable(shape).ifPresent(s -> {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(Color.GRAY);
      g2.draw(s);
      g2.dispose();
    });
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  private void update(MouseEvent e, JLayer<? extends V> l) {
    Shape s = null;
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
      case MouseEvent.MOUSE_MOVED:
        Component c = e.getComponent();
        if (c instanceof AbstractButton) {
          AbstractButton b = (AbstractButton) c;
          if (b.getIcon() instanceof ArrowToggleButtonBarCellIcon) {
            ArrowToggleButtonBarCellIcon icon = (ArrowToggleButtonBarCellIcon) b.getIcon();
            Rectangle r = c.getBounds();
            AffineTransform at = AffineTransform.getTranslateInstance(r.x, r.y);
            s = at.createTransformedShape(icon.getShape());
          }
        }
        break;
      default:
        break;
    }
    if (!Objects.equals(s, shape)) {
      shape = s;
      l.getView().repaint();
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
    update(e, l);
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    update(e, l);
  }
}
