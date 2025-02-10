// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new FlowLayout(FlowLayout.CENTER, 50, 50));
    Icon informationIcon = UIManager.getIcon("OptionPane.informationIcon");
    Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
    Icon questionIcon = UIManager.getIcon("OptionPane.questionIcon");
    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");

    BadgeLabel error = new BadgeLabel(errorIcon);
    BadgeLabel error1 = new BadgeLabel(errorIcon, "beta");
    // error1.setText("default");
    BadgeLabel question = new BadgeLabel(questionIcon, "RC1");
    BadgeLabel warning = new BadgeLabel(warningIcon, "beta");
    BadgeLabel information = new BadgeLabel(informationIcon, "alpha");
    BadgeLabel information2 = new BadgeLabel(informationIcon, "RC2");
    Stream.of(error, error1, question, warning, information, information2).forEach(this::add);

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

class BadgeLabel extends JLabel {
  private final Color ribbonColor = new Color(0xAA_FF_64_00, true);
  private final String ribbonText;

  protected BadgeLabel(Icon image) {
    super(image);
    this.ribbonText = "";
  }

  protected BadgeLabel(Icon image, String ribbonText) {
    super(image);
    this.ribbonText = ribbonText;
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    setVerticalAlignment(CENTER);
    setVerticalTextPosition(BOTTOM);
    setHorizontalAlignment(CENTER);
    setHorizontalTextPosition(CENTER);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.WHITE);
    g2.fill(getShape());
    super.paintComponent(g);

    if (!ribbonText.isEmpty()) {
      Dimension d = getSize();
      float fontSize = 10f;
      int cx = (d.width - (int) fontSize) / 2;
      double theta = Math.toRadians(45d);

      Font font = g2.getFont().deriveFont(fontSize);
      g2.setFont(font);
      FontRenderContext frc = new FontRenderContext(null, true, true);

      Shape ribbon = new Rectangle2D.Double(cx, -fontSize, d.width, fontSize);
      AffineTransform at = AffineTransform.getRotateInstance(theta, cx, 0);
      g2.setPaint(ribbonColor);
      g2.fill(at.createTransformedShape(ribbon));

      TextLayout tl = new TextLayout(ribbonText, font, frc);
      g2.setPaint(Color.WHITE);
      Rectangle2D r = tl.getOutline(null).getBounds2D();
      double dx = cx + (d.width - cx) / Math.sqrt(2d) - r.getWidth() / 2d;
      double dy = fontSize / 2d + r.getY();
      Shape s = tl.getOutline(AffineTransform.getTranslateInstance(dx, dy));
      g2.fill(at.createTransformedShape(s));
    }
    g2.dispose();
  }

  @Override public boolean isOpaque() {
    return false;
  }

  protected Shape getShape() {
    Dimension d = getSize();
    double r = d.width / 2d;
    return new RoundRectangle2D.Double(0d, 0d, d.width - 1d, d.height - 1d, r, r);
  }
}
