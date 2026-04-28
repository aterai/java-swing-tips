// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // Sample user data
    String[] names = {"Alice", "Bob", "Carol", "Dave", "Eve"};
    Color[] colors = {
        new Color(0xFF_63_86),
        new Color(0x36_A2_EB),
        new Color(0xFF_CE_56),
        new Color(0x4B_C0_C0),
        new Color(0x99_66_FF),
    };

    // Create two groups: one with leading (left) foreground and one without
    JLayer<JPanel> layer1 = createAvatarGroup(names, colors, true);
    JLayer<JPanel> layer2 = createAvatarGroup(names, colors, false);
    add(layer1, BorderLayout.NORTH);
    add(layer2, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLayer<JPanel> createAvatarGroup(
      String[] names, Color[] colors, boolean leftForeground) {
    // Container for displaying avatars
    JPanel avatarPanel = new JPanel(new StackedLayout(0d, leftForeground));
    avatarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // leftForeground = true -> 0, 1, 2... (Add from left -> Left is front)
    // leftForeground = false -> n-1, n-2... (Add from right -> Right is front)
    int n = colors.length;
    int start = leftForeground ? 0 : n - 1;
    int end = leftForeground ? n : -1;
    int step = leftForeground ? 1 : -1;
    for (int i = start; i != end; i += step) {
      avatarPanel.add(createAvatarButton(i, names[i], colors[i]));
    }

    // Wrap with JLayer and apply animation UI
    return new JLayer<JPanel>(avatarPanel, new AvatarLayerUI());
  }

  private static JButton createAvatarButton(int i, String name, Color color) {
    // Generate icons with varying sizes (100x100 to 200x200)
    int randomSize = 100 + (i * 25);
    JButton button = new AvatarButton(new UserIcon(name, color, randomSize));
    button.setToolTipText("User " + name);
    button.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    return button;
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

// Custom Layout Manager
// Arranges components based on gapFraction (0.0=stacked, 1.0=spread)
class StackedLayout implements LayoutManager {
  private final boolean leftForeground;
  private double gapFraction;

  protected StackedLayout(double gapFraction, boolean leftForeground) {
    this.gapFraction = gapFraction;
    this.leftForeground = leftForeground;
  }

  public void setGapFraction(double gapFrac) {
    this.gapFraction = gapFrac;
  }

  @Override public void layoutContainer(Container parent) {
    int n = parent.getComponentCount();
    if (n > 0) {
      Insets insets = parent.getInsets();
      int x = insets.left;
      int y = insets.top;

      // leftForeground:
      //   true -> Arrange from index 0 upwards (Add order = Display order)
      //   false -> Arrange from index n-1 downwards (Reverse add order = Display order)
      // setBounds only affects position, not Z-order, ensuring stability.
      int start = leftForeground ? 0 : n - 1;
      int end = leftForeground ? n : -1;
      int step = leftForeground ? 1 : -1;
      for (int i = start; i != end; i += step) {
        Component c = parent.getComponent(i);
        Dimension d = c.getPreferredSize();
        c.setBounds(x, y, d.width, d.height);
        x += (int) (d.width * .6 + d.width * .4 * gapFraction);
      }
    }
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    Dimension size = new Dimension();
    int n = parent.getComponentCount();
    if (n > 0) {
      int totalWidth = 0;
      int maxHeight = 0;
      for (int i = 0; i < n; i++) {
        Component c = parent.getComponent(i);
        Dimension d = c.getPreferredSize();
        maxHeight = Math.max(maxHeight, d.height);
        if (i < n - 1) {
          // Add overlap for all but the last component
          totalWidth += (int) (d.width * .6 + d.width * .4 * gapFraction);
        } else {
          totalWidth += d.width;
        }
      }
      Insets insets = parent.getInsets();
      totalWidth += insets.left + insets.right;
      maxHeight += insets.top + insets.bottom;
      size.setSize(totalWidth, maxHeight);
    }
    return size;
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override public void addLayoutComponent(String name, Component comp) {
    // not used
  }

  @Override public void removeLayoutComponent(Component comp) {
    // not used
  }
}

// Circular Avatar Button
class AvatarButton extends JButton {
  private static final int DIAMETER = 24;
  private static final Insets INSETS = new Insets(2, 2, 2, 2);
  private transient JToolTip tip;

  protected AvatarButton(Icon icon) {
    super(icon);
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setBorderPainted(false);
    setFocusPainted(false);
  }

  @Override public Dimension getPreferredSize() {
    int w = DIAMETER + INSETS.left + INSETS.right;
    int h = DIAMETER + INSETS.top + INSETS.bottom;
    return new Dimension(w, h);
  }

  @Override public boolean contains(int x, int y) {
    return new Ellipse2D.Double(0, 0, getWidth(), getHeight()).contains(x, y);
  }

  @Override public JToolTip createToolTip() {
    if (tip == null) {
      tip = new BalloonToolTip();
      tip.setComponent(this);
    }
    return tip;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = getWidth();
    int h = getHeight();

    // 1. Draw the background (matches parent background)
    g2.setColor(getParent().getBackground());
    g2.fill(new Ellipse2D.Double(0, 0, w, h));

    // 2. Render with Soft Clipping
    GraphicsConfiguration gc = g2.getDeviceConfiguration();
    BufferedImage buffer = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    Graphics2D g2d = buffer.createGraphics();
    g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    g2d.setComposite(AlphaComposite.Src);
    g2d.fill(new Ellipse2D.Double(INSETS.left, INSETS.top, DIAMETER, DIAMETER));

    // Composite icon inside the circle using SrcAtop
    g2d.setComposite(AlphaComposite.SrcAtop);

    // Scale icon to fit the circle
    Icon icon = getIcon();
    double scale = (double) DIAMETER / Math.max(icon.getIconWidth(), icon.getIconHeight());

    AffineTransform at = AffineTransform.getTranslateInstance(INSETS.left, INSETS.top);
    at.scale(scale, scale);
    g2d.transform(at);

    icon.paintIcon(this, g2d, 0, 0);
    g2d.dispose();

    g2.drawImage(buffer, 0, 0, null);
    g2.dispose();
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    return Optional.ofNullable(getToolTipText(e)).map(toolTipText -> {
      JToolTip toolTip = createToolTip();
      toolTip.setTipText(toolTipText);
      Rectangle buttonBounds = SwingUtilities.calculateInnerArea(this, null);
      Dimension tooltipSize = toolTip.getPreferredSize();
      int centerX = (int) (buttonBounds.getCenterX() - tooltipSize.getWidth() / 2d);
      int topY = buttonBounds.y - tooltipSize.height - 2;
      return new Point(centerX, topY);
    }).orElse(null);
  }
}

// LayerUI that handles expansion animation on mouse hover
class AvatarLayerUI extends LayerUI<JPanel> {
  private final Timer timer = new Timer(15, null);
  private double currentFraction;
  private double targetFraction;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    JLayer<?> l = (JLayer<?>) c;
    l.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    timer.addActionListener(e -> updateAnimation((JPanel) l.getView()));
  }

  // Ease-Out animation: Moves 25% closer to target per frame
  private void updateAnimation(JPanel panel) {
    double diff = targetFraction - currentFraction;
    boolean isEnd = Math.abs(diff) < .1;
    if (isEnd) {
      currentFraction = targetFraction;
      timer.stop();
    } else {
      currentFraction += diff * .25;
    }
    StackedLayout layout = (StackedLayout) panel.getLayout();
    layout.setGapFraction(currentFraction);
    panel.revalidate();
    panel.repaint();
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
    if (e.getID() == MouseEvent.MOUSE_ENTERED) {
      startAnimation(1d);
    } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
      startAnimation(0d);
    }
  }

  private void startAnimation(double target) {
    this.targetFraction = target;
    if (!timer.isRunning()) {
      timer.start();
    }
  }
}

class UserIcon implements Icon {
  private final String name;
  private final Color color;
  private final int size;

  protected UserIcon(String name, Color color, int size) {
    this.name = name;
    this.color = color;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(color);
    g2.fillRect(x, y, size, size);

    // Placeholder cross lines
    g2.setColor(Color.RED);
    g2.drawLine(x, y + size / 2, x + size, y + size / 2);
    g2.drawLine(x + size / 2, y, x + size / 2, y + size);

    g2.setColor(Color.WHITE);
    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 2));
    FontMetrics fontMetrics = g2.getFontMetrics();
    String initial = name.substring(0, 1).toUpperCase(Locale.US);
    int textX = x + (size - fontMetrics.stringWidth(initial)) / 2;
    int textY = y + ((size - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();
    g2.drawString(initial, textX, textY);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}

class BalloonToolTip extends JToolTip {
  private static final int TRI_HEIGHT = 4;
  private transient HierarchyListener listener;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(c))
            .filter(BalloonToolTip::isHeavyWeight)
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
    setForeground(Color.WHITE);
    setBackground(new Color(0xC8_00_00_00, true));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5 + TRI_HEIGHT, 5));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 32;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    Shape shape = createBalloonShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fill(shape);
    g2.dispose();
    super.paintComponent(g);
  }

  private Shape createBalloonShape() {
    double w = getWidth() - 1d;
    double h = getHeight() - TRI_HEIGHT - 1d;
    double centerX = getWidth() * .5d;
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(centerX - TRI_HEIGHT, h);
    triangle.lineTo(centerX, h + TRI_HEIGHT);
    triangle.lineTo(centerX + TRI_HEIGHT, h);
    double arc = 10d;
    Area area = new Area(new RoundRectangle2D.Double(0d, 0d, w, h, arc, arc));
    area.add(new Area(triangle));
    return area;
  }

  private static boolean isHeavyWeight(Window w) {
    boolean isPopup = w.getType() == Window.Type.POPUP;
    GraphicsConfiguration gc = w.getGraphicsConfiguration();
    return gc != null && gc.isTranslucencyCapable() && isPopup;
  }
}
