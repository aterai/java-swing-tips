// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("Slider.onlyLeftMouseButtonDrag", Boolean.TRUE);

    JSlider slider1 = makeSlider(SwingConstants.HORIZONTAL);
    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put("Slider.paintValue", Boolean.TRUE);
    slider1.putClientProperty("Nimbus.Overrides", d);

    JSlider slider2 = makeSlider(SwingConstants.VERTICAL);
    slider2.putClientProperty("Nimbus.Overrides", d);

    JSlider slider3 = makeSlider(SwingConstants.HORIZONTAL);
    MouseAdapter ma = new SliderPopupListener();
    slider3.addMouseMotionListener(ma);
    slider3.addMouseListener(ma);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", makeSlider(SwingConstants.HORIZONTAL)));
    box.add(makeTitledPanel("Slider.paintValue", slider1));
    box.add(makeTitledPanel("Show ToolTip", slider3));
    box.add(Box.createVerticalGlue());
    EventQueue.invokeLater(() -> {
      box.revalidate();
      box.repaint();
    });

    Box p = Box.createHorizontalBox();
    p.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    p.add(box);
    p.add(slider2);

    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private JSlider makeSlider(int orientation) {
    JSlider slider = new JSlider(orientation);
    slider.setPaintTrack(true);
    slider.setPaintLabels(false);
    slider.setPaintTicks(true);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(5);
    return slider;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

// https://ateraimemo.com/Swing/SliderToolTips.html
// https://github.com/aterai/java-swing-tips/blob/master/SliderToolTips/src/java/example/MainPanel.java
class SliderPopupListener extends MouseAdapter {
  private final JWindow toolTip = new JWindow();
  private final JLabel label = new JLabel(" ", SwingConstants.CENTER) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 32;
      return d;
    }
  };

  protected SliderPopupListener() {
    super();
    label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    toolTip.add(label);
    toolTip.pack();
  }

  protected void updateToolTip(MouseEvent e) {
    JSlider slider = (JSlider) e.getComponent();
    label.setText(String.format("%03d", slider.getValue()));
    Point pt = e.getPoint();
    pt.y = (int) SwingUtilities.calculateInnerArea(slider, null).getCenterY();
    SwingUtilities.convertPointToScreen(pt, e.getComponent());
    int h2 = slider.getPreferredSize().height / 2;
    Dimension d = label.getPreferredSize();
    pt.translate(-d.width / 2, -d.height - h2);
    toolTip.setLocation(pt);
  }

  @Override public void mouseDragged(MouseEvent e) {
    updateToolTip(e);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      toolTip.setVisible(true);
      updateToolTip(e);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    toolTip.setVisible(false);
  }
}
