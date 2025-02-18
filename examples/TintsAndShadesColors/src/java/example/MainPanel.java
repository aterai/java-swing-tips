// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<JLabel> list1 = makeCellList();
    List<JLabel> list2 = makeCellList();
    List<JLabel> list3 = makeCellList();
    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      Color bgc = list1.get(0).getBackground();
      Color color = JColorChooser.showDialog(getRootPane(), "title", bgc);
      if (color != null) {
        makePalette1(list1, color);
        makePalette2(list2, color);
        makePalette3(list3, color);
      }
    });
    JPanel p1 = new JPanel(new GridLayout(0, 1, 1, 1));
    p1.setBorder(BorderFactory.createTitledBorder("Shade"));
    for (JLabel l : list1) {
      p1.add(l);
    }
    JPanel p2 = new JPanel(new GridLayout(0, 1, 1, 1));
    p2.setBorder(BorderFactory.createTitledBorder("Tint"));
    for (JLabel l : list2) {
      p2.add(l);
    }
    JPanel p3 = new JPanel(new GridLayout(0, 1, 1, 1));
    p3.setBorder(BorderFactory.createTitledBorder("lumMod/lumOff"));
    for (JLabel l : list3) {
      p3.add(l);
    }
    Color color = new Color(0x70_AD_47);
    makePalette1(list1, color);
    makePalette2(list2, color);
    makePalette3(list3, color);
    JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
    p.add(p1);
    p.add(p2);
    p.add(p3);
    add(new JScrollPane(p));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static List<JLabel> makeCellList() {
    return Arrays.asList(
        makeCell(), makeCell(), makeCell(),
        makeCell(), makeCell(), makeCell());
  }

  private static void makePalette1(List<JLabel> list, Color color) {
    shade(list.get(0), color, 1f, "");
    shade(list.get(1), color, .95f, "Darker 5%");
    shade(list.get(2), color, .85f, "Darker 15%");
    shade(list.get(3), color, .75f, "Darker 25%");
    shade(list.get(4), color, .65f, "Darker 35%");
    shade(list.get(5), color, .5f, "Darker 50%");
  }

  private static void makePalette2(List<JLabel> list, Color color) {
    tint(list.get(0), color, 0f, "");
    tint(list.get(1), color, .8f, "Lighter 80%");
    tint(list.get(2), color, .6f, "Lighter 60%");
    tint(list.get(3), color, .4f, "Lighter 40%");
    tint(list.get(4), color, -.25f, "Darker 25%");
    tint(list.get(5), color, -.5f, "Darker 50%");
  }

  private static void makePalette3(List<JLabel> list, Color color) {
    luminance(list.get(0), color, 1d, 0d, "");
    luminance(list.get(1), color, .2, .8, "Lighter 80%");
    luminance(list.get(2), color, .4, .6, "Lighter 60%");
    luminance(list.get(3), color, .6, .4, "Lighter 40%");
    luminance(list.get(4), color, .75, 0d, "Darker 25%");
    luminance(list.get(5), color, .5, 0d, "Darker 50%");
  }

  private static void tint(JLabel l, Color color, float tint, String txt) {
    Color c = ColorUtils.getTintColor(color, tint);
    l.setBackground(c);
    l.setText(String.format("%s #%06X", txt, c.getRGB() & 0xFF_FF_FF));
  }

  private static void shade(JLabel l, Color color, float shade, String txt) {
    Color c = ColorUtils.getShadeColor(color, shade);
    l.setBackground(c);
    l.setText(String.format("%s #%06X", txt, c.getRGB() & 0xFF_FF_FF));
  }

  private static void luminance(JLabel l, Color c, double lumMod, double lumOff, String s) {
    Color bg = ColorUtils.getLuminenceColor(c, lumMod, lumOff);
    l.setBackground(bg);
    l.setText(String.format("%s #%06X", s, bg.getRGB() & 0xFF_FF_FF));
  }

  private static JLabel makeCell() {
    JLabel label = new JLabel() {
      @Override public void setBackground(Color bg) {
        super.setBackground(bg);
        Color fg = Objects.equals(Color.BLACK, bg) ? Color.WHITE : Color.BLACK;
        setForeground(fg);
        Color c = Objects.equals(Color.WHITE, bg) ? Color.LIGHT_GRAY : bg;
        setBorder(BorderFactory.createLineBorder(c, 1));
      }
    };
    label.setOpaque(true);
    return label;
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

final class ColorUtils {
  private ColorUtils() {
    /* Singleton */
  }

  public static Color getTintColor(Color color, float tint) {
    Color c;
    boolean positive = tint > 0f;
    if (positive) {
      int v = (int) (tint * 255f + .5f);
      float t = 1f - tint;
      int r = (int) (color.getRed() * t) + v;
      int g = (int) (color.getGreen() * t) + v;
      int b = (int) (color.getBlue() * t) + v;
      c = new Color(r, g, b);
    } else {
      c = getShadeColor(color, 1f + tint);
    }
    return c;
  }

  public static Color getShadeColor(Color color, float shade) {
    float r = color.getRed() * shade;
    float g = color.getGreen() * shade;
    float b = color.getBlue() * shade;
    return new Color((int) r, (int) g, (int) b);
  }

  public static Color getLuminenceColor(Color c, double lumMod, double lumOff) {
    double r = c.getRed() / 255d;
    double g = c.getGreen() / 255d;
    double b = c.getBlue() / 255d;
    double[] hsl = rgbToHsl(r, g, b);
    double lum = hsl[2] * lumMod + lumOff;
    return hslToRgb(hsl[0], hsl[1], lum);
  }

  public static double[] rgbToHsl(double r, double g, double b) {
    double max = Math.max(Math.max(r, g), b);
    double min = Math.min(Math.min(r, g), b);
    double l = (max + min) / 2d;
    double[] v = new double[3];
    if (max == min) {
      v[0] = 0d;
      v[1] = 0d;
    } else {
      double d = max - min;
      double s = l > .5 ? d / (2d - max - min) : d / (max + min);
      double h;
      if (r > g && r > b) {
        h = (g - b) / d + (g < b ? 6d : 0d);
      } else if (g > b) {
        h = (b - r) / d + 2d;
      } else {
        h = (r - g) / d + 4d;
      }
      v[0] = h / 6d;
      v[1] = s;
    }
    v[2] = l;
    return v;
  }

  public static Color hslToRgb(double h, double s, double l) {
    Color c;
    boolean achromatic = Math.abs(s) <= 1.0e-6; // s == 0d
    if (achromatic) {
      int v = to255(l);
      c = new Color(v, v, v);
    } else {
      double q = l < .5 ? l * (1d + s) : l + s - l * s;
      double p = 2d * l - q;
      int r = to255(hueToRgb(p, q, h + 1d / 3d));
      int g = to255(hueToRgb(p, q, h));
      int b = to255(hueToRgb(p, q, h - 1d / 3d));
      c = new Color(r, g, b);
    }
    return c;
  }

  public static int to255(double v) {
    int vv = (int) Math.round(255d * v);
    return Math.max(0, Math.min(vv, 255));
  }

  public static double hueToRgb(double p, double q, double t) {
    double t1 = t < 0d ? t + 1d : t;
    double tt = t1 > 1d ? t1 - 1d : t1;
    double c;
    if (tt < 1d / 6d) {
      c = p + (q - p) * 6d * tt;
    } else if (tt < 1d / 2d) {
      c = q;
    } else if (tt < 2d / 3d) {
      c = p + (q - p) * (2d / 3d - tt) * 6d;
    } else {
      c = p;
    }
    return c;
  }
}