// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(new Color(0x12_FF_FF_FF, true));
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle r = scroll.getViewportBorderBounds();
      BoundedRangeModel m = scroll.getVerticalScrollBar().getModel();
      g2.setClip(r);
      if (m.getMinimum() < m.getValue()) {
        for (int i = OVERFLOW; i > 0; i--) {
          g2.fillRect(0, r.y - i, r.width, OVERFLOW - i);
        }
      }
      if (m.getValue() + m.getExtent() < m.getMaximum()) {
        g2.translate(r.x, r.y + r.height - OVERFLOW);
        for (int i = 0; i < OVERFLOW; i++) {
          g2.fillRect(0, i, r.width, OVERFLOW - i);
        }
      }
    }
    g2.dispose();
  }
}
