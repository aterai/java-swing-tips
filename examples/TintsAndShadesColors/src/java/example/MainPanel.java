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
    List<JLabel> list1 = Arrays.asList(
        makeCell(), makeCell(), makeCell(),
        makeCell(), makeCell(), makeCell());
    List<JLabel> list2 = Arrays.asList(
        makeCell(), makeCell(), makeCell(),
        makeCell(), makeCell(), makeCell());
    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> showColorChooser(list1, list2));
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
    makePalette(list1, list2, new Color(0x70_AD_47));
    JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
    p.add(p1);
    p.add(p2);
    add(new JScrollPane(p));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void showColorChooser(List<JLabel> list1, List<JLabel> list2) {
    Component parent = getRootPane();
    String title = "JColorChooser";
    Color color = JColorChooser.showDialog(parent, title, list1.get(0).getBackground());
    if (color != null) {
      makePalette(list1, list2, color);
    }
  }

  private static void makePalette(List<JLabel> list1, List<JLabel> list2, Color color) {
    shade(list1.get(0), color, 1f, "");
    shade(list1.get(1), color, .95f, "Darker 5%");
    shade(list1.get(2), color, .85f, "Darker 15%");
    shade(list1.get(3), color, .75f, "Darker 25%");
    shade(list1.get(4), color, .65f, "Darker 35%");
    shade(list1.get(5), color, .5f, "Darker 50%");
    tint(list2.get(0), color, 0f, "");
    tint(list2.get(1), color, .8f, "Lighter 80%");
    tint(list2.get(2), color, .6f, "Lighter 80%");
    tint(list2.get(3), color, .4f, "Lighter 40%");
    tint(list2.get(4), color, -.25f, "Darker 25%");
    tint(list2.get(5), color, -.5f, "Darker 50%");
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
}
