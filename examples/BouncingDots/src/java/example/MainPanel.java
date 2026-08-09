// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Ellipse2D;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int BLINK_DELAY_MS = 30;
  private static final long BLINK_PERIOD_MS = 1000L;
  private static final float MIN_ALPHA_RATIO = .5f;

  private MainPanel() {
    super(new GridBagLayout());
    int dotCount = 3;
    int width = 32;
    int height = 24;
    Icon icon = new BouncingDots(dotCount, width, height);
    JLabel label = new JLabel("Loading...", icon, SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    long blinkStartTime = System.currentTimeMillis();
    Timer blinkTimer = new Timer(
        BLINK_DELAY_MS, e -> updateBlinkColor(label, blinkStartTime));
    blinkTimer.start();

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  // Fades the label text between MIN_ALPHA_RATIO and full brightness on a sine wave.
  private void updateBlinkColor(JLabel label, long startTime) {
    long elapsed = System.currentTimeMillis() - startTime;
    double t = (Math.sin(elapsed / (double) BLINK_PERIOD_MS * 2 * Math.PI) + 1) / 2d;
    float ratio = (float) (MIN_ALPHA_RATIO + t * (1 - MIN_ALPHA_RATIO));
    // Look up the current color on every tick instead of caching it, so that
    // switching the Look and Feel at runtime is picked up. Caching it would
    // also break LookAndFeel#installColorsAndFont(...), which only replaces
    // a foreground that is still null or a UIResource.
    Color uiColor = UIManager.getColor("Label.foreground");
    Color foreground = uiColor != null ? uiColor : Color.GRAY;
    label.setForeground(interpolateColor(getBackground(), foreground, ratio));
  }

  /**
   * Interpolate between two colors and return an opaque result.
   * A translucent color must not be used here: Java2D only applies LCD subpixel
   * antialiasing to opaque paints, so the text would switch between subpixel and
   * grayscale rasterization and jitter vertically on the frames where the alpha
   * value happens to be exactly 255.
   */
  private static Color interpolateColor(Color from, Color to, float ratio) {
    return new Color(
        Math.round(from.getRed() + (to.getRed() - from.getRed()) * ratio),
        Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * ratio),
        Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * ratio));
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
 * Animated loading indicator icon with 3 (variable) bouncing dots.
 * The color of the dot is determined by {@link Component#getForeground()}
 * of the component to be drawn.
 * If explicitly set, use it, otherwise
 * Fallback to {@code UIManager.getColor("Label.foreground")}.
 */
class BouncingDots implements Icon {
  private static final int TIMER_DELAY_MS = 30;
  private static final long PERIOD_MS = 1000L;
  private static final double PHASE_OFFSET = .2;
  private static final double DOT_SIZE_RATIO = .3;

  private final int dotCount;
  private final int iconWidth;
  private final int iconHeight;
  private final Timer timer = new Timer(TIMER_DELAY_MS, new RepaintAction());
  private long startTimeMillis = -1L;

  // The component this icon was last painted on, used to start/stop the timer.
  private Component attachedComponent;
  private HierarchyListener hierarchyListener;

  /* default */ BouncingDots(int dotCount, int width, int height) {
    if (dotCount <= 0) {
      throw new IllegalArgumentException("dotCount must be positive: " + dotCount);
    }
    this.dotCount = dotCount;
    this.iconWidth = width;
    this.iconHeight = height;
    this.timer.setCoalesce(true);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    attachTo(c);

    if (startTimeMillis < 0) {
      startTimeMillis = System.currentTimeMillis();
    }
    if (c.isShowing() && !timer.isRunning()) {
      timer.start();
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(x, y);
      g2.setColor(resolveColor(c));

      double diameter = iconHeight * DOT_SIZE_RATIO;
      double maxBounce = (iconHeight - diameter) / 2d;
      double gap = dotCount > 0 ? (double) iconWidth / dotCount : iconWidth;

      long elapsed = System.currentTimeMillis() - startTimeMillis;

      Ellipse2D dot = new Ellipse2D.Double();
      for (int i = 0; i < dotCount; i++) {
        double phase = (double) elapsed / PERIOD_MS + i * PHASE_OFFSET;
        // Normalize sin to the range 0..1 and bounce upward
        double t = (Math.sin(phase * 2 * Math.PI) + 1) / 2d;
        double dy = maxBounce - t * maxBounce * 2;
        double dotCenterX = gap * i + gap / 2d;
        double dotCenterY = iconHeight / 2d + dy;
        double dotX = dotCenterX - diameter / 2d;
        double dotY = dotCenterY - diameter / 2d;
        dot.setFrame(dotX, dotY, diameter, diameter);
        g2.fill(dot);
      }
    } finally {
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return iconWidth;
  }

  @Override public int getIconHeight() {
    return iconHeight;
  }

  private Color resolveColor(Component c) {
    return Optional.ofNullable(c)
        .filter(Component::isForegroundSet)
        .map(Component::getForeground)
        .orElseGet(() -> {
          Color uiColor = UIManager.getColor("Label.foreground");
          return uiColor != null ? uiColor : Color.GRAY;
        });
  }

  /**
   * Hook into the component to be drawn and manage the start and stop of
   * the Timer depending on whether it is visible or hidden.
   * Ignore reattachments to the same component.
   */
  private void attachTo(Component c) {
    if (!Objects.equals(c, attachedComponent)) {
      if (attachedComponent != null && hierarchyListener != null) {
        attachedComponent.removeHierarchyListener(hierarchyListener);
      }
      attachedComponent = c;
      hierarchyListener = new ShowingStateListener();
      c.addHierarchyListener(hierarchyListener);
    }
  }

  private final class ShowingStateListener implements HierarchyListener {
    @Override public void hierarchyChanged(HierarchyEvent e) {
      long flags = e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED;
      if (flags != 0 && attachedComponent != null) {
        boolean showing = attachedComponent.isShowing();
        if (showing && !timer.isRunning()) {
          timer.start();
        } else if (!showing) {
          timer.stop();
        }
      }
    }
  }

  private final class RepaintAction implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      if (attachedComponent != null) {
        if (attachedComponent.isShowing()) {
          attachedComponent.repaint();
        } else {
          timer.stop();
        }
      }
    }
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
