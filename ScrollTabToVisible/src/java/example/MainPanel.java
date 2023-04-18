// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    IntStream.range(0, 100).forEach(i -> tabbedPane.addTab("title" + i, new JLabel("label" + i)));

    JCheckBox check = new JCheckBox("setSelectedIndex");
    check.setHorizontalAlignment(SwingConstants.RIGHT);

    JSlider slider = new JSlider(0, tabbedPane.getTabCount() - 1, 50);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(5);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.addChangeListener(e -> {
      int i = ((JSlider) e.getSource()).getValue();
      if (check.isSelected()) {
        tabbedPane.setSelectedIndex(i);
      }
      scrollTabAt(tabbedPane, i);
    });

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("Scroll Slider"));
    p.add(check, BorderLayout.SOUTH);
    p.add(slider, BorderLayout.NORTH);
    add(p, BorderLayout.NORTH);
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void scrollTabAt(JTabbedPane tabbedPane, int index) {
    Component cmp = null;
    for (Component c : tabbedPane.getComponents()) {
      if (Objects.equals("TabbedPane.scrollableViewport", c.getName())) {
        cmp = c;
        break;
      }
    }
    if (cmp instanceof JViewport) {
      JViewport viewport = (JViewport) cmp;
      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        tabbedPane.setForegroundAt(i, i == index ? Color.RED : Color.BLACK);
      }
      Dimension d = tabbedPane.getSize();
      Rectangle r = tabbedPane.getBoundsAt(index);
      int gw = (d.width - r.width) / 2;
      r.grow(gw, 0);
      viewport.scrollRectToVisible(r);
    }
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
