// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.DefaultEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    Component edit = MenuBarUtils.createButtonBar(Arrays.asList(
        MenuBarUtils.createButton("Cut", new DefaultEditorKit.CutAction()),
        MenuBarUtils.createButton("Copy", new DefaultEditorKit.CopyAction()),
        MenuBarUtils.createButton("Paste", new DefaultEditorKit.PasteAction())));
    JMenu menu = new JMenu("File");
    menu.add("JMenuItem 1");
    menu.add("JMenuItem 2");
    menu.addSeparator();
    menu.add(MenuBarUtils.createGridBagMenuItem(edit));
    menu.addSeparator();
    menu.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    menu.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    return mb;
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

final class MenuBarUtils {
  private MenuBarUtils() {
    /* Singleton */
  }

  // Wraps a disabled "Edit" JMenuItem around comp so the button bar
  // renders inline in the menu.
  public static JMenuItem createGridBagMenuItem(Component comp) {
    JMenuItem item = new JMenuItem("Edit") {
      @Override public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width += comp.getPreferredSize().width;
        dim.height = Math.max(comp.getPreferredSize().height, dim.height);
        return dim;
      }

      // Keep the label black even while disabled/rollover/pressed.
      @Override protected void fireStateChanged() {
        setForeground(Color.BLACK);
        super.fireStateChanged();
      }
    };
    item.setEnabled(false);

    GridBagConstraints c = new GridBagConstraints();
    item.setLayout(new GridBagLayout());
    c.anchor = GridBagConstraints.LINE_END;
    // c.gridx = GridBagConstraints.RELATIVE;
    c.weightx = 1d;

    // Push comp to the right edge of the menu item with a stretching glue component.
    c.fill = GridBagConstraints.HORIZONTAL;
    item.add(Box.createHorizontalGlue(), c);
    c.fill = GridBagConstraints.NONE;
    item.add(comp, c);

    return item;
  }

  // Lays out buttons in a single row, wrapped in a JLayer that draws a separator on hover.
  public static Component createButtonBar(List<AbstractButton> buttons) {
    int size = buttons.size();
    JPanel p = new JPanel(new GridLayout(1, size, 0, 0)) {
      @Override public Dimension getMaximumSize() {
        return super.getPreferredSize();
      }
    };
    buttons.forEach(button -> {
      button.setIcon(new ToggleButtonBarCellIcon());
      p.add(button);
    });
    p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    p.setOpaque(false);

    return new JLayer<>(p, new EditMenuLayerUI<>(buttons.get(size - 1)));
  }

  public static AbstractButton createButton(String title, Action action) {
    AbstractButton b = new JButton(action);
    // Close the enclosing popup menu once the action has been triggered.
    b.addActionListener(e -> {
      Component src = (Component) e.getSource();
      Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, src);
      if (popup instanceof JPopupMenu) {
        popup.setVisible(false);
      }
    });
    b.setText(title);
    // b.setVerticalAlignment(SwingConstants.CENTER);
    // b.setVerticalTextPosition(SwingConstants.CENTER);
    // b.setHorizontalAlignment(SwingConstants.CENTER);
    b.setHorizontalTextPosition(SwingConstants.CENTER);
    b.setBorder(BorderFactory.createEmptyBorder());
    b.setContentAreaFilled(false);
    b.setFocusPainted(false);
    b.setOpaque(false);
    return b;
  }
}

// Draws a segmented-button background: only the outer edges of the first/last
// buttons in the bar are rounded, so the row reads as one continuous pill.
// https://ateraimemo.com/Swing/ToggleButtonBar.html
class ToggleButtonBarCellIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Container parent = c.getParent();
    if (Objects.nonNull(parent)) {
      paintPath(c, g, createButtonPath(c, parent, x, y));
    }
  }

  private static void paintPath(Component c, Graphics g, Path2D path) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Paint fillColor = new Color(0x0, true);
    Paint borderColor = Color.GRAY.brighter();
    if (c instanceof AbstractButton) {
      ButtonModel model = ((AbstractButton) c).getModel();
      if (model.isPressed()) {
        fillColor = new Color(0xC8_C8_FF);
      } else if (model.isSelected() || model.isRollover()) {
        borderColor = Color.GRAY;
      }
    }
    g2.setPaint(fillColor);
    g2.fill(path);
    g2.setPaint(borderColor);
    g2.draw(path);
    g2.dispose();
  }

  private static Path2D createButtonPath(Component c, Container parent, int x, int y) {
    double r = 4d;
    // Bezier control-point offset that approximates a quarter circle of radius r.
    double rr = r * 4d * (Math.sqrt(2d) - 1d) / 3d; // = r * .5522;
    double w = c.getWidth();
    double h = c.getHeight() - 1d;
    Path2D path = new Path2D.Double();
    if (Objects.equals(c, parent.getComponent(0))) {
      // Round only the left corners for the first button.
      path.moveTo(x, y + r);
      path.curveTo(x, y + r - rr, x + r - rr, y, x + r, y);
      path.lineTo(x + w, y);
      path.lineTo(x + w, y + h);
      path.lineTo(x + r, y + h);
      path.curveTo(x + r - rr, y + h, x, y + h - r + rr, x, y + h - r);
    } else if (Objects.equals(c, parent.getComponent(parent.getComponentCount() - 1))) {
      // Round only the right corners for the last button.
      w--;
      path.moveTo(x, y);
      path.lineTo(x + w - r, y);
      path.curveTo(x + w - r + rr, y, x + w, y + r - rr, x + w, y + r);
      path.lineTo(x + w, y + h - r);
      path.curveTo(x + w, y + h - r + rr, x + w - r + rr, y + h, x + w - r, y + h);
      path.lineTo(x, y + h);
    } else {
      // Square corners for buttons in the middle of the bar.
      path.moveTo(x, y);
      path.lineTo(x + w, y);
      path.lineTo(x + w, y + h);
      path.lineTo(x, y + h);
    }
    path.closePath();
    return path;
  }

  @Override public int getIconWidth() {
    return 40;
  }

  @Override public int getIconHeight() {
    return 20;
  }
}

// Draws a vertical separator line along the right edge of a hovered button,
// except for the last button in the bar.
class EditMenuLayerUI<V extends Component> extends LayerUI<V> {
  private final AbstractButton lastButton;
  private transient Shape shape;

  protected EditMenuLayerUI(AbstractButton button) {
    super();
    this.lastButton = button;
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (Objects.nonNull(shape)) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(Color.GRAY);
      g2.draw(shape);
      g2.dispose();
    }
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
    int id = e.getID();
    Shape s = null;
    if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_MOVED) {
      Component c = e.getComponent();
      if (!Objects.equals(c, lastButton)) {
        Rectangle r = c.getBounds();
        s = new Line2D.Double(r.getMaxX(), r.getMinY(), r.getMaxX(), r.getMaxY() - 1d);
      }
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
