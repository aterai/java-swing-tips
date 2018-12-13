package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default", makeSlider(false)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Triangle Tick", makeSlider(true)));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JSlider makeSlider(boolean icon) {
    JSlider slider = new JSlider(0, 100);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(5);
    slider.setPaintLabels(true);
    slider.setSnapToTicks(true);
    slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    if (icon) {
      Dictionary<?, ?> dictionary = slider.getLabelTable();
      if (Objects.nonNull(dictionary)) {
        Icon tick = new TickIcon();
        Collections.list(dictionary.elements()).stream()
            .filter(JLabel.class::isInstance)
            .map(JLabel.class::cast)
            .forEach(label -> {
              label.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
              label.setIcon(tick);
              label.setIconTextGap(0);
              label.setVerticalAlignment(SwingConstants.TOP);
              label.setVerticalTextPosition(SwingConstants.BOTTOM);
              label.setHorizontalAlignment(SwingConstants.CENTER);
              label.setHorizontalTextPosition(SwingConstants.CENTER);
              label.setForeground(Color.RED);
            });
      }
    } else {
      slider.setPaintTicks(true);
      slider.setForeground(Color.BLUE);
    }
    return slider;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TickIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setColor(Color.BLUE);
    g2.drawLine(2, 0, 2, 2);
    g2.drawLine(1, 1, 3, 1);
    g2.drawLine(0, 2, 4, 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 5;
  }

  @Override public int getIconHeight() {
    return 3;
  }
}
