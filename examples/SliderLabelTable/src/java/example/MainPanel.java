// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // @SuppressWarnings("JdkObsolete")
    // Dictionary<Integer, Component> labelTable = new Hashtable<>();
    // // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    // Stream.of("wi0009-16.png", "wi0054-16.png", "wi0062-16.png",
    //         "wi0063-16.png", "wi0064-16.png", "wi0096-16.png",
    //         "wi0111-16.png", "wi0122-16.png", "wi0124-16.png",
    //         "wi0126-16.png")
    //     .forEach(s -> labelTable.put(labelTable.size(), makeLabel(s)));
    //  labelTable.put(labelTable.size(), new JButton("aaa"));
    List<Icon> list1 = Stream.of(
        "wi0009-16.png", "wi0054-16.png", "wi0062-16.png",
        "wi0063-16.png", "wi0064-16.png", "wi0096-16.png",
        "wi0111-16.png", "wi0122-16.png", "wi0124-16.png",
        "wi0126-16.png").map(MainPanel::makeIcon).collect(Collectors.toList());
    JSlider slider1 = new JSlider(SwingConstants.VERTICAL, 0, list1.size() - 1, 0);
    slider1.setSnapToTicks(true);
    slider1.setMajorTickSpacing(1);
    // slider1.setMinorTickSpacing(5);
    slider1.setPaintTicks(true);
    // slider1.setLabelTable(labelTable);
    slider1.setPaintLabels(true);
    Object labelTable1 = slider1.getLabelTable();
    if (labelTable1 instanceof Map) {
      ((Map<?, ?>) labelTable1).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          int idx = (Integer) key;
          JLabel l = (JLabel) value;
          l.setIcon(list1.get(idx));
          l.setText(null);
          l.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        }
      });
    }
    slider1.setLabelTable(slider1.getLabelTable()); // Update LabelTable

    // @SuppressWarnings("JdkObsolete")
    // Dictionary<Integer, Component> labelTable2 = new Hashtable<>();
    // // @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    // // Hashtable labelTable2 = slider2.createStandardLabels(1);
    // Stream.of("零", "壱", "弐", "参", "肆", "伍", "陸", "漆", "捌", "玖", "拾")
    //     .map(JLabel::new)
    //     .forEach(l -> {
    //       int idx = labelTable2.size();
    //       l.setForeground(new Color(250, 100 - idx * 10, 10));
    //       labelTable2.put(idx, l);
    //     });
    String[] list2 = {"零", "壱", "弐", "参", "肆", "伍", "陸", "漆", "捌", "玖", "拾"};
    JSlider slider2 = new JSlider(0, list2.length - 1, 0);
    // slider2.setForeground(Color.BLUE);
    slider2.setMajorTickSpacing(1);
    slider2.setSnapToTicks(true);
    // slider2.setLabelTable(labelTable2);
    slider2.setPaintTicks(true);
    slider2.setPaintLabels(true);
    Object labelTable2 = slider2.getLabelTable();
    if (labelTable2 instanceof Map) {
      ((Map<?, ?>) labelTable2).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          int idx = (Integer) key;
          JLabel l = (JLabel) value;
          l.setText(list2[idx]);
          l.setForeground(new Color(250, 100 - idx * 10, 10));
        }
      });
    }
    slider2.setLabelTable(slider2.getLabelTable()); // Update LabelTable

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
    box.add(new JSlider());
    box.add(Box.createVerticalStrut(20));
    box.add(slider2);
    box.add(Box.createHorizontalGlue());

    add(slider1, BorderLayout.WEST);
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Icon makeIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    String p = "example/" + path;
    return new ImageIcon(Optional.ofNullable(cl.getResource(p)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage));
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
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
