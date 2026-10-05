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
    List<JLabel> shadeCells = createCellList();
    List<JLabel> tintCells = createCellList();
    List<JLabel> luminanceCells = createCellList();
    JPanel p = new JPanel(new GridLayout(1, 0, 2, 2));
    p.add(createPalettePanel("Shade", shadeCells));
    p.add(createPalettePanel("Tint", tintCells));
    p.add(createPalettePanel("lumMod/lumOff", luminanceCells));
    Color color = new Color(0x70_AD_47);
    updateShadePalette(shadeCells, color);
    updateTintPalette(tintCells, color);
    updateLuminancePalette(luminanceCells, color);
    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      Color bgc = shadeCells.get(0).getBackground();
      // Java 21: Color bgc = shadeCells.getFirst().getBackground();
      Color c = JColorChooser.showDialog(getRootPane(), "title", bgc);
      if (c != null) {
        updateShadePalette(shadeCells, c);
        updateTintPalette(tintCells, c);
        updateLuminancePalette(luminanceCells, c);
      }
    });
    add(new JScrollPane(p));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel createPalettePanel(String title, List<JLabel> cells) {
    JPanel p = new JPanel(new GridLayout(0, 1, 1, 1));
    p.setBorder(BorderFactory.createTitledBorder(title));
    cells.forEach(p::add);
    return p;
  }

  private static List<JLabel> createCellList() {
    return Arrays.asList(
        createCell(), createCell(), createCell(),
        createCell(), createCell(), createCell());
  }

  private static void updateShadePalette(List<JLabel> cells, Color color) {
    setCellColor(cells.get(0), ColorUtils.getShadeColor(color, 1f), "");
    setCellColor(cells.get(1), ColorUtils.getShadeColor(color, .95f), "Darker 5%");
    setCellColor(cells.get(2), ColorUtils.getShadeColor(color, .85f), "Darker 15%");
    setCellColor(cells.get(3), ColorUtils.getShadeColor(color, .75f), "Darker 25%");
    setCellColor(cells.get(4), ColorUtils.getShadeColor(color, .65f), "Darker 35%");
    setCellColor(cells.get(5), ColorUtils.getShadeColor(color, .5f), "Darker 50%");
  }

  private static void updateTintPalette(List<JLabel> cells, Color color) {
    setCellColor(cells.get(0), ColorUtils.getTintColor(color, 0f), "");
    setCellColor(cells.get(1), ColorUtils.getTintColor(color, .8f), "Lighter 80%");
    setCellColor(cells.get(2), ColorUtils.getTintColor(color, .6f), "Lighter 60%");
    setCellColor(cells.get(3), ColorUtils.getTintColor(color, .4f), "Lighter 40%");
    setCellColor(cells.get(4), ColorUtils.getTintColor(color, -.25f), "Darker 25%");
    setCellColor(cells.get(5), ColorUtils.getTintColor(color, -.5f), "Darker 50%");
  }

  private static void updateLuminancePalette(List<JLabel> cells, Color color) {
    setCellColor(cells.get(0), ColorUtils.getLuminanceColor(color, 1d, 0d), "");
    setCellColor(cells.get(1), ColorUtils.getLuminanceColor(color, .2, .8), "Lighter 80%");
    setCellColor(cells.get(2), ColorUtils.getLuminanceColor(color, .4, .6), "Lighter 60%");
    setCellColor(cells.get(3), ColorUtils.getLuminanceColor(color, .6, .4), "Lighter 40%");
    setCellColor(cells.get(4), ColorUtils.getLuminanceColor(color, .75, 0d), "Darker 25%");
    setCellColor(cells.get(5), ColorUtils.getLuminanceColor(color, .5, 0d), "Darker 50%");
  }

  private static void setCellColor(JLabel cell, Color bg, String txt) {
    cell.setBackground(bg);
    cell.setForeground(ColorUtils.getContrastColor(bg));
    Color bc = Objects.equals(Color.WHITE, bg) ? Color.LIGHT_GRAY : bg;
    cell.setBorder(BorderFactory.createLineBorder(bc, 1));
    cell.setText(String.format("%s #%06X", txt, bg.getRGB() & 0xFF_FF_FF));
  }

  private static JLabel createCell() {
    JLabel label = new JLabel();
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

  // Blend the color with white: c + (255 - c) * tint
  // A negative tint is the same as a shade of (1 + tint)
  public static Color getTintColor(Color color, float tint) {
    Color c;
    boolean lighter = tint > 0f;
    if (lighter) {
      int r = Math.round(color.getRed() + (255 - color.getRed()) * tint);
      int g = Math.round(color.getGreen() + (255 - color.getGreen()) * tint);
      int b = Math.round(color.getBlue() + (255 - color.getBlue()) * tint);
      c = new Color(r, g, b);
    } else {
      c = getShadeColor(color, 1f + tint);
    }
    return c;
  }

  // Blend the color with black: c * shade
  public static Color getShadeColor(Color color, float shade) {
    int r = Math.round(color.getRed() * shade);
    int g = Math.round(color.getGreen() * shade);
    int b = Math.round(color.getBlue() * shade);
    return new Color(r, g, b);
  }

  // Convert to HSL and change the luminance: l * lumMod + lumOff
  public static Color getLuminanceColor(Color c, double lumMod, double lumOff) {
    double r = c.getRed() / 255d;
    double g = c.getGreen() / 255d;
    double b = c.getBlue() / 255d;
    double[] hsl = rgbToHsl(r, g, b);
    double lum = Math.min(Math.max(hsl[2] * lumMod + lumOff, 0d), 1d);
    return hslToRgb(hsl[0], hsl[1], lum);
  }

  // Black text on a light background, white text on a dark background
  public static Color getContrastColor(Color bg) {
    int y = (299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue()) / 1000;
    return y >= 128 ? Color.BLACK : Color.WHITE;
  }

  public static double[] rgbToHsl(double r, double g, double b) {
    double max = Math.max(Math.max(r, g), b);
    double min = Math.min(Math.min(r, g), b);
    double l = (max + min) / 2d;
    double h = 0d;
    double s = 0d;
    double d = max - min;
    if (max > min) {
      s = l > .5 ? d / (2d - max - min) : d / (max + min);
      if (max == r) {
        h = (g - b) / d + (g < b ? 6d : 0d);
      } else if (max == g) {
        h = (b - r) / d + 2d;
      } else {
        h = (r - g) / d + 4d;
      }
      h /= 6d;
    }
    return new double[] {h, s, l};
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

  // Truncate instead of rounding to get closer to the Word results
  public static int to255(double v) {
    int vv = (int) (255d * v + 1.0e-6);
    return Math.min(Math.max(vv, 0), 255);
    // Java 21: return Math.clamp(vv, 0, 255);
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
