// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    @SuppressWarnings("JdkObsolete")
    Dictionary<Integer, Component> labelTable = new Hashtable<>();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    Stream.of("wi0009-16.png", "wi0054-16.png", "wi0062-16.png",
              "wi0063-16.png", "wi0064-16.png", "wi0096-16.png",
              "wi0111-16.png", "wi0122-16.png", "wi0124-16.png",
              "wi0126-16.png")
        .forEach(s -> labelTable.put(labelTable.size(), makeLabel(s)));

    labelTable.put(labelTable.size(), new JButton("aaa"));
    JSlider slider1 = new JSlider(SwingConstants.VERTICAL, 0, 10, 0);
    slider1.setSnapToTicks(true);
    // slider1.setMajorTickSpacing(1);
    // slider1.setMinorTickSpacing(5);
    slider1.setPaintTicks(true);
    slider1.setLabelTable(labelTable);
    slider1.setPaintLabels(true);

    @SuppressWarnings("JdkObsolete")
    Dictionary<Integer, Component> labelTable2 = new Hashtable<>();
    // @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    // Hashtable labelTable2 = slider2.createStandardLabels(1);
    Stream.of("零", "壱", "弐", "参", "肆", "伍", "陸", "漆", "捌", "玖", "拾")
          .map(JLabel::new)
          .forEach(l -> {
            int idx = labelTable2.size();
            l.setForeground(new Color(250, 100 - idx * 10, 10));
            labelTable2.put(idx, l);
          });

    JSlider slider2 = new JSlider(0, 10, 0);
    // slider2.setForeground(Color.BLUE);
    slider2.setSnapToTicks(true);
    slider2.setLabelTable(labelTable2);
    slider2.setPaintTicks(true);
    slider2.setPaintLabels(true);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
    box.add(new JSlider(0, 100, 100));
    box.add(Box.createVerticalStrut(20));
    box.add(new JSlider());
    box.add(Box.createVerticalStrut(20));
    box.add(slider2);
    box.add(Box.createHorizontalGlue());

    add(slider1, BorderLayout.WEST);
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private JLabel makeLabel(String path) {
    return new JLabel(path, new ImageIcon(getClass().getResource(path)), SwingConstants.RIGHT);
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
