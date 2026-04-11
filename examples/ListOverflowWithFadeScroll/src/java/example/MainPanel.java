// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    DefaultListModel<String> model = new DefaultListModel<>();
    Stream.of(fonts).map(Font::getFontName).sorted().forEach(model::addElement);
    add(makeScrollPane(new JList<>(model)));
    add(new JLayer<>(makeScrollPane(new JList<>(model)), new FadeScrollLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private JScrollPane makeScrollPane(Component c) {
    return new JScrollPane(c) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
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

class FadeScrollLayerUI extends LayerUI<JScrollPane> {
  public static final int OVERFLOW = 32;

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle r = scroll.getViewportBorderBounds();
      BoundedRangeModel m = scroll.getVerticalScrollBar().getModel();

      // 1. Dynamically get background color of JList
      Color bgc = getBgColor(scroll);
      Color transparent = new Color(bgc.getRGB() & 0x00_FF_FF_FF, true);

      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setClip(r);

      // 2. Top edge fade (background color → transparent)
      if (m.getMinimum() < m.getValue()) {
        Paint topGrad = new GradientPaint(
            0f, r.y, bgc, 0f, r.y + OVERFLOW, transparent);
        g2.setPaint(topGrad);
        g2.fillRect(r.x, r.y, r.width, OVERFLOW);
      }

      // 3. Bottom edge fade (transparent → background color)
      if (m.getValue() + m.getExtent() < m.getMaximum()) {
        int fadeTop = r.y + r.height - OVERFLOW;
        Paint btmGrad = new GradientPaint(
            0f, fadeTop, transparent, 0f, r.y + r.height, bgc);
        g2.setPaint(btmGrad);
        g2.fillRect(r.x, fadeTop, r.width, OVERFLOW);
      }

      g2.dispose();
    }
  }

  private static Color getBgColor(JScrollPane scroll) {
    return Optional.ofNullable(scroll.getViewport().getView())
        .map(Component::getBackground)
        .orElseGet(() -> {
          Color fallback = UIManager.getColor("List.background");
          return fallback == null ? Color.WHITE : fallback;
        });
  }
}
