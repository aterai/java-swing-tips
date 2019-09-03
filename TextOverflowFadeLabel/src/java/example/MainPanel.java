// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String text = "012345678901234567890123456789012345678901234567890123456789";
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("defalut JLabel ellipsis", new JLabel(text)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("html JLabel fade out", new FadeOutLabel("<html>" + text)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("JLabel TextLayout fade out", new TextOverflowFadeLabel(text)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("JLabel BufferedImage fade out", new FadingOutLabel(text)));
    box.add(Box.createVerticalGlue());

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
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

class FadeOutLabel extends JLabel {
  private static final int LENGTH = 20;
  private static final float DIFF = .05f;

  protected FadeOutLabel(String text) {
    super(text);
  }

  @Override public void paintComponent(Graphics g) {
    Insets i = getInsets();
    int w = getWidth() - i.left - i.right;
    int h = getHeight() - i.top - i.bottom;

    Rectangle rect = new Rectangle(i.left, i.top, w - LENGTH, h);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setFont(g.getFont());
    g2.setClip(rect);
    g2.setComposite(AlphaComposite.SrcOver.derive(.99f));
    super.paintComponent(g2);

    rect.width = 1;
    float alpha = 1f;
    for (int x = w - LENGTH; x < w; x++) {
      rect.x = x;
      alpha = Math.max(0f, alpha - DIFF);
      g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
      g2.setClip(rect);
      super.paintComponent(g2);
    }
    g2.dispose();
  }
}

class TextOverflowFadeLabel extends JLabel {
  private static final int LENGTH = 20;
  private static final float DIFF = .05f;

  protected TextOverflowFadeLabel(String text) {
    super(text);
  }

  @Override public void paintComponent(Graphics g) {
    Insets i = getInsets();
    int w = getWidth() - i.left - i.right;
    int h = getHeight() - i.top - i.bottom;
    Rectangle rect = new Rectangle(i.left, i.top, w - LENGTH, h);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setFont(g.getFont());
    g2.setPaint(getForeground());

    FontRenderContext frc = g2.getFontRenderContext();
    TextLayout tl = new TextLayout(getText(), getFont(), frc);
    int baseline = getBaseline(w, h);

    g2.setClip(rect);
    tl.draw(g2, i.left, baseline);

    rect.width = 1;
    float alpha = 1f;
    for (int x = w - LENGTH; x < w; x++) {
      rect.x = x;
      alpha = Math.max(0f, alpha - DIFF);
      g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
      g2.setClip(rect);
      tl.draw(g2, i.left, baseline);
    }
    g2.dispose();
  }
}

class FadingOutLabel extends JLabel {
  private static final int LENGTH = 20;
  private final Dimension dim = new Dimension();
  private transient Image buffer;

  protected FadingOutLabel(String text) {
    super(text);
  }

  @Override public void paintComponent(Graphics g) {
    // super.paintComponent(g);
    int w = getWidth();
    int h = getHeight();
    if (Objects.isNull(buffer) || dim.width != w || dim.height != h) {
      dim.setSize(w, h);
      buffer = updateImage(dim);
    }
    g.drawImage(buffer, 0, 0, this);
  }

  private BufferedImage updateImage(Dimension d) {
    BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setFont(getFont());
    g2.setPaint(getForeground());
    FontRenderContext frc = g2.getFontRenderContext();
    TextLayout tl = new TextLayout(getText(), getFont(), frc);
    int baseline = getBaseline(d.width, d.height);
    tl.draw(g2, getInsets().left, baseline);
    g2.dispose();

    int spx = Math.max(0, d.width - LENGTH);
    for (int x = 0; x < LENGTH; x++) {
      double factor = 1d - x / (double) LENGTH;
      for (int y = 0; y < d.height; y++) {
        int argb = img.getRGB(spx + x, y);
        int rgb = argb & 0x00_FF_FF_FF;
        int a = (argb >> 24) & 0xFF;
        img.setRGB(spx + x, y, ((int) (a * factor) << 24) | rgb);
      }
    }
    return img;
  }
}
