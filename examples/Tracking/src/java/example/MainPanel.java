// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String text = "1234567890";

    Map<TextAttribute, Object> attr1 = new ConcurrentHashMap<>();
    attr1.put(TextAttribute.TRACKING, TextAttribute.TRACKING_TIGHT);
    JLabel l1 = new JLabel(text + " TRACKING_TIGHT (-.04f)");
    l1.setFont(l1.getFont().deriveFont(attr1));

    Map<TextAttribute, Object> attr2 = new ConcurrentHashMap<>();
    attr2.put(TextAttribute.TRACKING, TextAttribute.TRACKING_LOOSE);
    JLabel l2 = new JLabel(text + " TRACKING_LOOSE (.04f)");
    l2.setFont(l2.getFont().deriveFont(attr2));

    JPanel p0 = new JPanel(new GridLayout(0, 1, 5, 5));
    p0.setBorder(BorderFactory.createTitledBorder("TextAttribute.TRACKING"));
    Stream.of(new JLabel(text + " Default"), l1, l2).forEach(p0::add);

    JLabel l3 = new JLabel(new BadgeIcon(128, Color.WHITE, new Color(0xAA_FF_32_32, true)));
    JLabel l4 = new JLabel(new BadgeIcon(256, Color.BLACK, new Color(0xAA_64_FF_64, true)));
    JLabel l5 = new JLabel(new BadgeIcon(1_024, Color.WHITE, new Color(0xAA_32_32_FF, true)));

    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("Tracking: -0.1"));
    Stream.of(l3, l4, l5).forEach(p1::add);

    JLabel l6 = new JLabel(new BadgeIcon2(128, Color.WHITE, new Color(0xAA_FF_32_32, true)));
    JLabel l7 = new JLabel(new BadgeIcon2(256, Color.BLACK, new Color(0xAA_64_FF_64, true)));
    JLabel l8 = new JLabel(new BadgeIcon2(1_024, Color.WHITE, new Color(0xAA_32_32_FF, true)));

    JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder("Scaled along the X axis direction: 0.95"));
    Stream.of(l6, l7, l8).forEach(p2::add);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(p0);
    box.add(Box.createVerticalStrut(10));
    box.add(p2);
    box.add(Box.createVerticalStrut(10));
    box.add(p1);

    add(box, BorderLayout.NORTH);
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

class BadgeIcon implements Icon {
  private final Color badgeBgc;
  private final Color badgeFgc;
  private final int value;

  protected BadgeIcon(int value, Color fgc, Color bgc) {
    this.value = value;
    this.badgeFgc = fgc;
    this.badgeBgc = bgc;
  }

  public String getText() {
    return value > 999 ? "1K+" : Integer.toString(value);
  }

  public Shape getBadgeShape() {
    return new Ellipse2D.Double(0d, 0d, getIconWidth(), getIconHeight());
  }

  public Shape getTextShape(Graphics2D g2) {
    String txt = getText();
    Map<TextAttribute, Object> attr = new ConcurrentHashMap<>();
    attr.put(TextAttribute.TRACKING, -.1f);
    Font font = txt.length() < 3 ? g2.getFont() : g2.getFont().deriveFont(attr);
    return new TextLayout(txt, font, g2.getFontRenderContext()).getOutline(null);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (value <= 0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // g2.setRenderingHint(
    // RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.translate(x, y);
    Shape badge = getBadgeShape();
    g2.setPaint(badgeBgc);
    g2.fill(badge);

    g2.setPaint(badgeFgc);
    Shape shape = getTextShape(g2);
    Rectangle2D b = shape.getBounds2D();
    double tx = getIconWidth() / 2d - b.getCenterX();
    double ty = getIconHeight() / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(tx, ty);
    g2.fill(toCenterAt.createTransformedShape(shape));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 24;
  }

  @Override public int getIconHeight() {
    return 24;
  }
}

class BadgeIcon2 extends BadgeIcon {
  protected BadgeIcon2(int value, Color fgc, Color bgc) {
    super(value, fgc, bgc);
  }

  @Override public Shape getTextShape(Graphics2D g2) {
    String txt = getText();
    AffineTransform at = txt.length() < 3 ? null : AffineTransform.getScaleInstance(.95, 1d);
    Font font = g2.getFont().deriveFont(at);
    return new TextLayout(txt, font, g2.getFontRenderContext()).getOutline(null);
  }
}
