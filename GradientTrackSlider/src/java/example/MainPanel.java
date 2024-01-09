// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private static final Color COLOR = new Color(0x32_C8_96_64, true);
  private static final Paint TEXTURE = TextureUtils.createCheckerTexture(6, COLOR);

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("Slider.horizontalThumbIcon", new Icon() {
      @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        /* Empty icon */
      }

      @Override public int getIconWidth() {
        return 15;
      }

      @Override public int getIconHeight() {
        return 64;
      }
    });
    // System.out.println(UIManager.get("Slider.trackWidth"));
    // System.out.println(UIManager.get("Slider.majorTickLength"));
    // System.out.println(UIManager.getInt("Slider.trackWidth"));
    // System.out.println(UIManager.getInt("Slider.majorTickLength"));
    UIManager.put("Slider.trackWidth", 64);
    UIManager.put("Slider.majorTickLength", 6);

    JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
    slider.setBackground(Color.GRAY);
    slider.setOpaque(false);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default:", slider));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Gradient translucent track JSlider:", makeSlider()));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private JSlider makeSlider() {
    return new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50) {
      private transient MouseAdapter handler;

      @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseWheelListener(handler);
        super.updateUI();
        setUI(new GradientPalletSliderUI());
        setBackground(Color.GRAY);
        setOpaque(false);
        handler = new MouseAdapter() {
          @Override public void mouseDragged(MouseEvent e) {
            e.getComponent().repaint();
          }

          @Override public void mouseWheelMoved(MouseWheelEvent e) {
            BoundedRangeModel m = ((JSlider) e.getComponent()).getModel();
            m.setValue(m.getValue() - e.getWheelRotation());
          }
        };
        addMouseMotionListener(handler);
        addMouseWheelListener(handler);
      }
    };
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(TEXTURE);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
    super.paintComponent(g);
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.setOpaque(false);
    p.add(c);
    return p;
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

class GradientPalletSliderUI extends MetalSliderUI {
  private static final int[] GRADIENT_PALLET = GradientPalletUtils.makeGradientPallet();
  // protected Color controlDarkShadow = MetalLookAndFeel.getControlDarkShadow();
  protected Color controlDarkShadow = new Color(0x64_64_64);
  // protected Color controlHighlight = MetalLookAndFeel.getControlHighlight();
  protected Color controlHighlight = new Color(0xC8_FF_C8);
  // protected Color controlShadow = MetalLookAndFeel.getControlShadow();
  protected Color controlShadow = new Color(0x00_64_00);

  @Override public void paintTrack(Graphics g) {
    // Color trackColor = !slider.isEnabled() ? controlShadow : slider.getForeground();
    // boolean leftToRight = MetalUtils.isLeftToRight(slider);

    g.translate(trackRect.x, trackRect.y);

    int trackLeft = 0;
    int trackTop = 0;
    int trackRight;
    int trackBottom;
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      trackBottom = trackRect.height - 1 - getThumbOverhang();
      trackTop = trackBottom - getTrackWidth() + 1;
      trackRight = trackRect.width - 1;
    } else {
      // if (leftToRight) {
      trackLeft = trackRect.width - getThumbOverhang() - getTrackWidth();
      trackRight = trackRect.width - getThumbOverhang() - 1;
      // } else {
      //   trackLeft = getThumbOverhang();
      //   trackRight = getThumbOverhang() + getTrackWidth() - 1;
      // }
      trackBottom = trackRect.height - 1;
    }

    // Draw the track
    paintTrackBase(g, trackTop, trackLeft, trackBottom, trackRight);

    // Draw the fill
    paintTrackFill(g, trackTop, trackLeft, trackBottom, trackRight);

    // Draw the highlight
    paintTrackHighlight(g, trackTop, trackLeft, trackBottom, trackRight);

    g.translate(-trackRect.x, -trackRect.y);
  }

  protected void paintTrackBase(Graphics g, int trTop, int trLeft, int trBottom, int trRight) {
    if (slider.isEnabled()) {
      g.setColor(controlDarkShadow);
      g.drawRect(trLeft, trTop, trRight - trLeft - 1, trBottom - trTop - 1);

      g.setColor(controlHighlight);
      g.drawLine(trLeft + 1, trBottom, trRight, trBottom);
      g.drawLine(trRight, trTop + 1, trRight, trBottom);

      g.setColor(controlShadow);
      g.drawLine(trLeft + 1, trTop + 1, trRight - 2, trTop + 1);
      g.drawLine(trLeft + 1, trTop + 1, trLeft + 1, trBottom - 2);
    } else {
      g.setColor(controlShadow);
      g.drawRect(trLeft, trTop, trRight - trLeft - 1, trBottom - trTop - 1);
    }
  }

  protected void paintTrackFill(Graphics g, int trTop, int trLeft, int trBottom, int trRight) {
    int middleOfThumb;
    int fillTop;
    int fillLeft;
    int fillBottom;
    int fillRight;

    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      middleOfThumb = thumbRect.x + thumbRect.width / 2;
      middleOfThumb -= trackRect.x; // To compensate for the g.translate()
      fillTop = trTop + 1;
      fillBottom = trBottom - 2;
      fillLeft = trLeft + 1;
      fillRight = middleOfThumb - 2;
    } else {
      middleOfThumb = thumbRect.y + thumbRect.height / 2;
      middleOfThumb -= trackRect.y; // To compensate for the g.translate()
      fillLeft = trLeft;
      fillRight = trRight - 1;
      fillTop = middleOfThumb;
      fillBottom = trBottom - 1;
    }

    // if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
    //   middleOfThumb = thumbRect.x + thumbRect.width / 2;
    //   middleOfThumb -= trackRect.x; // To compensate for the g.translate()
    //   fillTop = slider.isEnabled() ? trTop + 1 : trTop;
    //   fillBottom = slider.isEnabled() ? trBottom - 2 : trBottom - 1;
    //
    //   if (drawInverted()) {
    //     fillLeft = middleOfThumb;
    //     fillRight = slider.isEnabled() ? trRight - 2 : trRight - 1;
    //   } else {
    //     fillLeft = slider.isEnabled() ? trLeft +1 : trLeft;
    //     fillRight = middleOfThumb;
    //   }
    // } else {
    //   middleOfThumb = thumbRect.y + thumbRect.height / 2;
    //   middleOfThumb -= trackRect.y; // To compensate for the g.translate()
    //   fillLeft = slider.isEnabled() ? trLeft + 1 : trLeft;
    //   fillRight = slider.isEnabled() ? trRight - 2 : trRight - 1;
    //
    //   if (drawInverted()) {
    //     fillTop = slider.isEnabled() ? trTop + 1 : trTop;
    //     fillBottom = middleOfThumb;
    //   } else {
    //     fillTop = middleOfThumb;
    //     fillBottom = slider.isEnabled() ? trBottom - 2 : trBottom - 1;
    //   }
    // }

    if (slider.isEnabled()) {
      // g.setColor(slider.getBackground());
      // g.drawLine(fillLeft, fillTop, fillRight, fillTop);
      // g.drawLine(fillLeft, fillTop, fillLeft, fillBottom);

      float x = (fillRight - fillLeft) / (float) (trRight - trLeft);
      g.setColor(GradientPalletUtils.getColorFromPallet(GRADIENT_PALLET, x, 0x64 << 24));
      g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop);
    } else {
      g.setColor(controlShadow);
      g.fillRect(fillLeft, fillTop, fillRight - fillLeft, trBottom - trTop);
    }
  }

  protected void paintTrackHighlight(Graphics g, int top, int left, int bottom, int right) {
    int yy = top + (bottom - top) / 2;
    for (int i = 10; i >= 0; i--) {
      g.setColor(makeColor(i * .07f));
      g.drawLine(left + 2, yy, right - left - 2, yy);
      yy--;
    }
  }

  private static Color makeColor(float alpha) {
    return new Color(1f, 1f, 1f, alpha);
  }
}

final class GradientPalletUtils {
  private GradientPalletUtils() {
    /* HideUtilityClassConstructor */
  }

  public static int[] makeGradientPallet() {
    BufferedImage image = new BufferedImage(100, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    Point2D start = new Point2D.Float();
    Point2D end = new Point2D.Float(99f, 0f);
    float[] dist = {.0f, .5f, 1f};
    Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
    g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
    g2.fillRect(0, 0, 100, 1);
    g2.dispose();

    int width = image.getWidth(null);
    int[] pallet = new int[width];
    PixelGrabber pg = new PixelGrabber(image, 0, 0, width, 1, pallet, 0, width);
    try {
      pg.grabPixels();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
      Thread.currentThread().interrupt();
    }
    return pallet;
  }

  public static Color getColorFromPallet(int[] pallet, float x, int alpha) {
    // if (x < 0f || x > 1f) {
    //   throw new IllegalArgumentException("Parameter outside of expected range");
    // }
    int i = (int) (pallet.length * x);
    int max = pallet.length - 1;
    int index = Math.min(Math.max(i, 0), max);
    int pix = pallet[index] & 0x00_FF_FF_FF;
    // int alpha = 0x64 << 24;
    return new Color(alpha | pix, true);
  }
}

final class TextureUtils {
  private TextureUtils() {
    /* HideUtilityClassConstructor */
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }
}
