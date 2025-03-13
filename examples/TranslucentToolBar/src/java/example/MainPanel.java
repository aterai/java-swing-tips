// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    JToolBar toolBar = makeToolBar();
    JLabel label = new LabelWithToolBox(icon, toolBar);
    label.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0xDE_DE_DE)),
        BorderFactory.createLineBorder(Color.WHITE, 4)));
    label.add(toolBar);
    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  private JToolBar makeToolBar() {
    JToolBar toolBar = new TranslucentToolBar();
    // toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
    toolBar.add(Box.createGlue());
    toolBar.add(makeToolButton("ATTACHMENT_16x16-32.png"));
    toolBar.add(Box.createHorizontalStrut(2));
    toolBar.add(makeToolButton("RECYCLE BIN - EMPTY_16x16-32.png"));
    return toolBar;
  }

  private JButton makeToolButton(String name) {
    String path = "example/" + name;
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    ImageIcon icon = new ImageIcon(image);
    JButton b = new JButton();
    b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    // b.addChangeListener(new ChangeListener() {
    //   @Override public void stateChanged(ChangeEvent e) {
    //     JButton button = (JButton) e.getSource();
    //     ButtonModel model = button.getModel();
    //     if (button.isRolloverEnabled() && model.isRollover()) {
    //       button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    //     } else {
    //       button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    //     }
    //   }
    // });
    b.setIcon(makeRolloverIcon(icon));
    b.setRolloverIcon(icon);
    b.setContentAreaFilled(false);
    // b.setBorderPainted(false);
    b.setFocusPainted(false);
    b.setFocusable(false);
    b.setToolTipText(name);
    return b;
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }

  private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
    int w = srcIcon.getIconWidth();
    int h = srcIcon.getIconHeight();
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    srcIcon.paintIcon(null, g2, 0, 0);
    float[] scaleFactors = {.5f, .5f, .5f, 1f};
    float[] offsets = {0f, 0f, 0f, 0f};
    RescaleOp op = new RescaleOp(scaleFactors, offsets, g2.getRenderingHints());
    g2.dispose();
    return new ImageIcon(op.filter(img, null));
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TranslucentToolBar extends JToolBar {
  private transient MouseListener listener;

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getBackground());
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
    super.paintComponent(g);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    super.updateUI();
    listener = new ParentDispatchMouseListener();
    addMouseListener(listener);
    setFloatable(false);
    setOpaque(false);
    setBackground(new Color(0x0, true));
    setForeground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
  }
}

class LabelWithToolBox extends JLabel {
  public static final int DELAY = 8;
  protected final Timer animator = new Timer(DELAY, null);
  protected boolean isHidden;
  protected int counter;
  protected int yy;
  private transient ToolBoxHandler handler;

  protected LabelWithToolBox(Icon icon, JToolBar toolBox) {
    super(icon);
    animator.addActionListener(e -> {
      int height = toolBox.getPreferredSize().height;
      if (isHidden) {
        double a = AnimationUtils.easeInOut(++counter / (double) height);
        yy = (int) (.5 + a * height);
        toolBox.setBackground(new Color(0f, 0f, 0f, (float) (.6 * a)));
        if (yy >= height) {
          yy = height;
          animator.stop();
        }
      } else {
        double a = AnimationUtils.easeInOut(--counter / (double) height);
        yy = (int) (.5 + a * height);
        toolBox.setBackground(new Color(0f, 0f, 0f, (float) (.6 * a)));
        if (yy <= 0) {
          yy = 0;
          animator.stop();
        }
      }
      toolBox.revalidate();
    });
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    addHierarchyListener(handler);
    super.updateUI();
    setLayout(new OverlayLayout(this) {
      @Override public void layoutContainer(Container parent) {
        // Insets insets = parent.getInsets();
        int nc = parent.getComponentCount();
        if (nc == 0) {
          return;
        }
        int width = parent.getWidth(); // - insets.left - insets.right;
        int height = parent.getHeight(); // - insets.left - insets.right;
        int x = 0; // insets.left; int y = insets.top;
        // for (int i = 0; i < nc; i++) {
        Component c = parent.getComponent(0); // = toolBox;
        c.setBounds(x, height - yy, width, c.getPreferredSize().height);
        // }
      }
    });
    handler = new ToolBoxHandler();
    addMouseListener(handler);
    addHierarchyListener(handler);
  }

  private final class ToolBoxHandler extends MouseAdapter implements HierarchyListener {
    @Override public void mouseEntered(MouseEvent e) {
      if (!animator.isRunning()) { // && yy != toolBox.getPreferredSize().height) {
        isHidden = true;
        animator.start();
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      if (!contains(e.getPoint())) { // !animator.isRunning()) {
        isHidden = false;
        animator.start();
      }
    }

    @Override public void hierarchyChanged(HierarchyEvent e) {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    }
  }
}

class ParentDispatchMouseListener extends MouseAdapter {
  @Override public void mouseEntered(MouseEvent e) {
    dispatchMouseEvent(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    dispatchMouseEvent(e);
  }

  private void dispatchMouseEvent(MouseEvent e) {
    Component src = e.getComponent();
    Optional.ofNullable(SwingUtilities.getUnwrappedParent(src)).ifPresent(tgt ->
        tgt.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, tgt)));
  }
}

final class AnimationUtils {
  private static final int N = 3;

  private AnimationUtils() {
    /* Singleton */
  }

  // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
  // Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
  public static double easeIn(double t) {
    // range: 0.0 <= t <= 1.0
    return Math.pow(t, N);
  }

  public static double easeOut(double t) {
    return Math.pow(t - 1d, N) + 1d;
  }

  public static double easeInOut(double t) {
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * intPow(t * 2d, N) : .5 * (intPow(t * 2d - 2d, N) + 2d);
  }

  // https://wiki.c2.com/?IntegerPowerAlgorithm
  public static double intPow(double da, int ib) {
    int b = ib;
    if (b < 0) {
      // return d / intPow(a, -b);
      throw new IllegalArgumentException("B must be a positive integer or zero");
    }
    double a = da;
    double d = 1d;
    for (; b > 0; a *= a, b >>>= 1) {
      if ((b & 1) != 0) {
        d *= a;
      }
    }
    return d;
  }

  // public static double delta(double t) {
  //   return 1d - Math.sin(Math.acos(t));
  // }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 240;
  }

  @Override public int getIconHeight() {
    return 160;
  }
}
