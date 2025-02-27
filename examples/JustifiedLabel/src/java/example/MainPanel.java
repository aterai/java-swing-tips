// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridBagLayout());
    Border inside = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
    Border outside = BorderFactory.createTitledBorder("JLabel text-align:justify");
    p.setBorder(BorderFactory.createCompoundBorder(outside, inside));

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    p.add(new JLabel("打率"), c);
    p.add(new JLabel("打率", SwingConstants.RIGHT), c);
    p.add(new JustifiedLabel("打率"), c);
    p.add(new JLabel("出塁率", SwingConstants.CENTER), c);
    p.add(new JustifiedLabel("出塁率"), c);
    p.add(new JustifiedLabel("チーム出塁率"), c);

    c.gridx = 1;
    c.weightx = 1d;
    p.add(new JTextField(), c);
    p.add(new JTextField(), c);
    p.add(new JTextField(), c);
    p.add(new JTextField(), c);
    p.add(new JTextField(), c);
    p.add(new JTextField(), c);

    add(p);
    add(new JustifiedLabel("あいうえおかきくけこ"), BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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

class JustifiedLabel extends JLabel {
  private transient GlyphVector gvText;
  private int prevWidth = -1;

  protected JustifiedLabel() {
    this(null);
  }

  protected JustifiedLabel(String str) {
    super(str);
  }

  @Override public void setText(String text) {
    super.setText(text);
    prevWidth = -1;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    Font font = getFont();
    Dimension d = getSize();
    Insets ins = getInsets();
    int w = d.width - ins.left - ins.right;
    if (w != prevWidth) {
      GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), getText());
      gvText = makeJustifiedGlyphVector(gv, w);
      prevWidth = w;
    }
    Optional.ofNullable(gvText).ifPresent(gv -> {
      g2.setPaint(getBackground());
      g2.fillRect(0, 0, d.width, d.height);
      g2.setPaint(getForeground());
      g2.drawGlyphVector(gv, ins.left, ins.top + font.getSize2D());
    });
    g2.dispose();
  }

  private static GlyphVector makeJustifiedGlyphVector(GlyphVector gv, float width) {
    Rectangle2D r = gv.getVisualBounds();
    float vw = (float) r.getWidth();
    if (width > vw) {
      int num = gv.getNumGlyphs();
      float xx = (width - vw) / (num - 1f);
      float pos = num == 1 ? (width - vw) * .5f : 0f;
      Point2D gmPos = new Point2D.Float();
      for (int i = 0; i < num; i++) {
        GlyphMetrics gm = gv.getGlyphMetrics(i);
        gmPos.setLocation(pos, 0f);
        gv.setGlyphPosition(i, gmPos);
        pos += gm.getAdvance() + xx;
      }
    }
    return gv;
  }
}
