// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.logging.Logger;
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

class JustifiedLabel extends JLabel {
  private transient GlyphVector justifiedText;
  private int prevWidth = -1;

  protected JustifiedLabel(String text) {
    super(text);
  }

  @Override public void setText(String text) {
    super.setText(text);
    prevWidth = -1;
  }

  @Override public void setFont(Font font) {
    super.setFont(font);
    prevWidth = -1;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    // Use the same text antialiasing as the default JLabel rendering
    Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
    if (hints instanceof Map) {
      g2.addRenderingHints((Map<?, ?>) hints);
    }
    Insets ins = getInsets();
    int w = getWidth() - ins.left - ins.right;
    if (w != prevWidth) {
      justifiedText = createJustifiedGlyphVector(g2.getFontRenderContext(), w);
      prevWidth = w;
    }
    int baseline = getBaseline(getWidth(), getHeight());
    if (justifiedText == null || baseline < 0) {
      // Not enough space: fall back to the default JLabel rendering
      // (truncated with "...")
      super.paintComponent(g);
    } else {
      if (isOpaque()) {
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
      }
      g2.setPaint(getForeground());
      g2.drawGlyphVector(justifiedText, ins.left, baseline);
    }
    g2.dispose();
  }

  private GlyphVector createJustifiedGlyphVector(FontRenderContext frc, float width) {
    String text = getText();
    GlyphVector justified = null;
    if (text != null && !text.isEmpty()) {
      GlyphVector gv = getFont().createGlyphVector(frc, text);
      // Use the logical width (sum of the advances) so that the right edge
      // of the last glyph lines up with the right edge of the label
      float textWidth = (float) gv.getLogicalBounds().getWidth();
      if (width > textWidth) {
        int num = gv.getNumGlyphs();
        float extra = width - textWidth;
        // A single glyph is centered,
        // otherwise the extra space is spread evenly between glyphs
        float gap = num > 1 ? extra / (num - 1) : 0f;
        float pos = num > 1 ? 0f : extra / 2f;
        Point2D glyphPos = new Point2D.Float();
        for (int i = 0; i < num; i++) {
          glyphPos.setLocation(pos, 0f);
          gv.setGlyphPosition(i, glyphPos);
          pos += gv.getGlyphMetrics(i).getAdvance() + gap;
        }
        justified = gv;
      }
    }
    return justified;
  }
}
