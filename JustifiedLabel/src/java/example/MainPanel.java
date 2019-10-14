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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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

  private static GlyphVector makeJustifiedGlyphVector(GlyphVector gv, int width) {
    Rectangle2D r = gv.getVisualBounds();
    float jw = (float) width;
    float vw = (float) r.getWidth();
    if (jw > vw) {
      int num = gv.getNumGlyphs();
      float xx = (jw - vw) / (float) (num - 1);
      float xpos = num == 1 ? (jw - vw) * .5f : 0f;
      Point2D gmPos = new Point2D.Float();
      for (int i = 0; i < num; i++) {
        GlyphMetrics gm = gv.getGlyphMetrics(i);
        gmPos.setLocation(xpos, 0);
        gv.setGlyphPosition(i, gmPos);
        xpos += gm.getAdvance() + xx;
      }
    }
    return gv;
  }
}
