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
    Component edit = makeEditButtonBar(Arrays.asList(
        makeButton("Cut", new DefaultEditorKit.CutAction()),
        makeButton("Copy", new DefaultEditorKit.CopyAction()),
        makeButton("Paste", new DefaultEditorKit.PasteAction())));

    JMenu menu = new JMenu("File");
    menu.add("111111111");
    menu.addSeparator();
    menu.add(makeEditMenuItem(edit));
    menu.addSeparator();
    menu.add("22222");
    menu.add("3333");
    menu.add("4444444");

    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    return mb;
  }

  private static JMenuItem makeEditMenuItem(Component edit) {
    JMenuItem item = new JMenuItem("Edit") {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += edit.getPreferredSize().width;
        d.height = Math.max(edit.getPreferredSize().height, d.height);
        return d;
      }

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

    c.fill = GridBagConstraints.HORIZONTAL;
    item.add(Box.createHorizontalGlue(), c);
    c.fill = GridBagConstraints.NONE;
    item.add(edit, c);

    return item;
  }

  private static Component makeEditButtonBar(List<AbstractButton> list) {
    int size = list.size();
    JPanel p = new JPanel(new GridLayout(1, size, 0, 0)) {
      @Override public Dimension getMaximumSize() {
        return super.getPreferredSize();
      }
    };
    list.forEach(b -> {
      b.setIcon(new ToggleButtonBarCellIcon());
      p.add(b);
    });
    p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    p.setOpaque(false);

    return new JLayer<>(p, new EditMenuLayerUI<>(list.get(size - 1)));
  }

  private static AbstractButton makeButton(String title, Action action) {
    AbstractButton b = new JButton(action);
    b.addActionListener(e -> {
      Component a = (Component) e.getSource();
      Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, a);
      if (c instanceof JPopupMenu) {
        c.setVisible(false);
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

// https://ateraimemo.com/Swing/ToggleButtonBar.html
class ToggleButtonBarCellIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Container parent = c.getParent();
    if (Objects.isNull(parent)) {
      return;
    }
    int r = 8;
    int w = c.getWidth();
    int h = c.getHeight() - 1;

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Path2D p = new Path2D.Double();
    if (c == parent.getComponent(0)) {
      // :first-child
      p.moveTo(x, y + r);
      p.quadTo(x, y, x + r, y);
      p.lineTo(x + w, y);
      p.lineTo(x + w, y + h);
      p.lineTo(x + r, y + h);
      p.quadTo(x, y + h, x, y + h - r);
    } else if (c == parent.getComponent(parent.getComponentCount() - 1)) {
      // :last-child
      w--;
      p.moveTo(x, y);
      p.lineTo(x + w - r, y);
      p.quadTo(x + w, y, x + w, y + r);
      p.lineTo(x + w, y + h - r);
      p.quadTo(x + w, y + h, x + w - r, y + h);
      p.lineTo(x, y + h);
    } else {
      p.moveTo(x, y);
      p.lineTo(x + w, y);
      p.lineTo(x + w, y + h);
      p.lineTo(x, y + h);
    }
    p.closePath();
    Paint color = new Color(0x0, true);
    Paint borderColor = Color.GRAY.brighter();
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isPressed()) {
        color = new Color(0xC8_C8_FF);
      } else if (m.isSelected() || m.isRollover()) {
        borderColor = Color.GRAY;
      }
    }
    g2.setPaint(color);
    g2.fill(p);
    g2.setPaint(borderColor);
    g2.draw(p);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 40;
  }

  @Override public int getIconHeight() {
    return 20;
  }
}

class EditMenuLayerUI<V extends Component> extends LayerUI<V> {
  private final AbstractButton lastButton;
  private Shape shape;

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
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
        s = new Line2D.Double(r.x + r.width, r.y, r.x + r.width, r.y + r.height - 1);
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
