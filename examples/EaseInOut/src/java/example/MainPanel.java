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

    String txt = "Mini-size 86Key Japanese Keyboard\n  Model No: DE-SK-86BK\n  SERIAL NO: 0000";
    ImageCaptionLabel label = new ImageCaptionLabel(icon, txt);
    label.add(label.getTextArea());

    add(label);
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ImageCaptionLabel extends JLabel {
  private final JTextArea textArea = new JTextArea() {
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
      setFont(getFont().deriveFont(11f));
      setOpaque(false);
      setEditable(false);
      setBackground(new Color(0x0, true));
      setForeground(Color.WHITE);
      setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
      listener = new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) {
          dispatchMouseEvent(e);
        }

        @Override public void mouseExited(MouseEvent e) {
          dispatchMouseEvent(e);
        }
      };
      addMouseListener(listener);
    }
  };
  private transient LabelHandler handler;

  protected ImageCaptionLabel(Icon icon, String caption) {
    super(icon);
    textArea.setText(caption);
  }

  protected void dispatchMouseEvent(MouseEvent e) {
    Component src = e.getComponent();
    // this: target Component;
    this.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, this));
  }

  public JTextArea getTextArea() {
    return textArea;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeHierarchyListener(handler);
    super.updateUI();
    handler = new LabelHandler();
    addMouseListener(handler);
    addHierarchyListener(handler);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0xDE_DE_DE)),
        BorderFactory.createLineBorder(Color.WHITE, 4)));
    setLayout(new OverlayLayout(this) {
      @Override public void layoutContainer(Container parent) {
        // Insets insets = parent.getInsets();
        int num = parent.getComponentCount();
        if (num == 0) {
          return;
        }
        int width = parent.getWidth(); // - insets.left - insets.right;
        int height = parent.getHeight(); // - insets.left - insets.right;
        int x = 0; // insets.left; int y = insets.top;
        int tah = handler.getTextAreaHeight();
        // for (int i = 0; i < num; i++) {
        Component c = parent.getComponent(0); // = textArea;
        c.setBounds(x, height - tah, width, c.getPreferredSize().height);
        // }
      }
    });
  }

  private final class LabelHandler extends MouseAdapter implements HierarchyListener {
    private final Timer animator = new Timer(10, e -> updateTextAreaLocation());
    // private final Component textArea;
    private int areaHeight;
    private int count;
    private int direction;

    private void updateTextAreaLocation() {
      double height = textArea.getPreferredSize().getHeight();
      double a = AnimationUtils.easeInOut(count / height);
      count += direction;
      areaHeight = (int) (.5 + a * height);
      textArea.setBackground(new Color(0f, 0f, 0f, (float) (.6 * a)));
      if (direction > 0) { // show
        if (areaHeight >= textArea.getPreferredSize().height) {
          areaHeight = textArea.getPreferredSize().height;
          animator.stop();
        }
      } else { // hide
        if (areaHeight <= 0) {
          areaHeight = 0;
          animator.stop();
        }
      }
      Container p = SwingUtilities.getUnwrappedParent(textArea);
      p.revalidate();
      p.repaint();
    }

    public int getTextAreaHeight() {
      return areaHeight;
    }

    @Override public void mouseEntered(MouseEvent e) {
      if (animator.isRunning() || isMaxHeight()) {
        return;
      }
      this.direction = 2;
      animator.start();
    }

    @Override public void mouseExited(MouseEvent e) {
      Component c = e.getComponent();
      if (animator.isRunning() || c.contains(e.getPoint()) && isMaxHeight()) {
        return;
      }
      this.direction = -2;
      animator.start();
    }

    private boolean isMaxHeight() {
      return areaHeight == textArea.getPreferredSize().height;
    }

    @Override public void hierarchyChanged(HierarchyEvent e) {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    }
  }
}

final class AnimationUtils {
  private static final int N = 3;

  private AnimationUtils() {
    /* Singleton */
  }

  // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
  // Math: EaseIn EaseOut, EaseInOut and Bézier curves | Anima Entertainment GmbH
  // public static double easeIn(double t) {
  //   // range: 0.0 <= t <= 1.0
  //   return Math.pow(t, N);
  // }

  // public static double easeOut(double t) {
  //   return Math.pow(t - 1d, N) + 1d;
  // }

  // public static double easeInOut(double t) {
  //   boolean isFirstHalf = t < .5;
  //   return isFirstHalf ? .5 * Math.pow(t * 2d, N) : .5 * (Math.pow(t * 2d - 2d, N) + 2d);
  // }

  public static double easeInOut(double t) {
    boolean isFirstHalf = t < .5;
    return isFirstHalf ? .5 * intPow(t * 2d, N) : .5 * (intPow(t * 2d - 2d, N) + 2d);
  }

  // https://wiki.c2.com/?IntegerPowerAlgorithm
  public static double intPow(double base0, int exp0) {
    if (exp0 < 0) {
      throw new IllegalArgumentException("exp must be a positive integer or zero");
    }
    double base = base0;
    int exp = exp0;
    double result = 1d;
    for (; exp > 0; base *= base, exp >>>= 1) {
      if ((exp & 1) != 0) {
        result *= base;
      }
    }
    return result;
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
