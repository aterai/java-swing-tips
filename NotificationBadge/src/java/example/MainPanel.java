// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 5));
    Icon informationIcon = UIManager.getIcon("OptionPane.informationIcon");
    Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
    Icon questionIcon = UIManager.getIcon("OptionPane.questionIcon");
    Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");

    BadgeLabel information = new BadgeLabel(informationIcon, BadgePosition.SOUTH_EAST, 0);
    BadgeLabel error = new BadgeLabel(errorIcon, BadgePosition.SOUTH_EAST, 8);
    BadgeLabel question = new BadgeLabel(questionIcon, BadgePosition.SOUTH_WEST, 64);
    BadgeLabel warning = new BadgeLabel(warningIcon, BadgePosition.NORTH_EAST, 256);
    BadgeLabel information2 = new BadgeLabel(informationIcon, BadgePosition.NORTH_WEST, 1_024);
    LayerUI<BadgeLabel> ui = new BadgeLayerUI();
    Stream.of(information, error, question, warning, information2).forEach(label -> {
      label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      add(new JLayer<>(label, ui));
    });

    LayerUI<BadgeLabel> ui2 = new BadgeIconLayerUI();
    Stream.of(informationIcon, errorIcon, questionIcon, warningIcon)
        .map(icon -> new BadgeLabel(icon, BadgePosition.SOUTH_EAST, 128))
        .forEach(label -> {
          label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
          add(new JLayer<>(label, ui2));
        });
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
  private final BadgePosition pos;
  private final int counter;

  protected BadgeLabel(Icon image, BadgePosition pos, int counter) {
    super(image);
    this.pos = pos;
    this.counter = counter;
  }

  public BadgePosition getBadgePosition() {
    return pos;
  }

  // public void setCounter(int counter) {
  //   this.counter = counter;
  // }

  public int getCounter() {
    return counter;
  }
}

class BadgeLayerUI extends LayerUI<BadgeLabel> {
  private static final Point OFFSET = new Point(6, 2);
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      iconRect.setBounds(0, 0, 0, 0);
      textRect.setBounds(0, 0, 0, 0);

      BadgeLabel label = (BadgeLabel) ((JLayer<?>) c).getView();
      SwingUtilities.calculateInnerArea(label, viewRect);
      SwingUtilities.layoutCompoundLabel(
          label,
          label.getFontMetrics(label.getFont()),
          label.getText(),
          label.getIcon(),
          label.getVerticalAlignment(),
          label.getHorizontalAlignment(),
          label.getVerticalTextPosition(),
          label.getHorizontalTextPosition(),
          viewRect,
          iconRect,
          textRect,
          label.getIconTextGap());

      Icon badge = getBadgeIcon(label.getCounter());
      Point pt = getBadgeLocation(label.getBadgePosition(), badge);
      g2.translate(pt.x, pt.y);
      badge.paintIcon(label, g2, 0, 0);
      g2.dispose();
    }
  }

  protected Icon getBadgeIcon(int count) {
    return new BadgeIcon(count, Color.WHITE, new Color(0xAA_FF_16_16, true));
  }

  protected Point getBadgeLocation(BadgePosition pos, Icon icon) {
    int x;
    int y;
    switch (pos) {
      case NORTH_WEST:
        x = iconRect.x - OFFSET.x;
        y = iconRect.y - OFFSET.y;
        break;
      case NORTH_EAST:
        x = iconRect.x + iconRect.width - icon.getIconWidth() + OFFSET.x;
        y = iconRect.y - OFFSET.y;
        break;
      case SOUTH_WEST:
        x = iconRect.x - OFFSET.x;
        y = iconRect.y + iconRect.height - icon.getIconHeight() + OFFSET.y;
        break;
      case SOUTH_EAST:
      default:
        x = iconRect.x + iconRect.width - icon.getIconWidth() + OFFSET.x;
        y = iconRect.y + iconRect.height - icon.getIconHeight() + OFFSET.y;
        break;
    }
    return new Point(x, y);
  }
}

class BadgeIconLayerUI extends BadgeLayerUI {
  @Override protected Icon getBadgeIcon(int count) {
    return new BadgeIcon(count, Color.WHITE, new Color(0xAA_16_16_16, true)) {
      @Override protected Shape getBadgeShape() {
        return new RoundRectangle2D.Double(0, 0, getIconWidth(), getIconHeight(), 5, 5);
      }
    };
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

  protected Shape getBadgeShape() {
    return new Ellipse2D.Double(0d, 0d, getIconWidth(), getIconHeight());
  }

  protected Shape getTextShape(Graphics2D g2) {
    // Java 12:
    // NumberFormat fmt = NumberFormat.getCompactNumberInstance(
    //    Locale.US, NumberFormat.Style.SHORT);
    // String txt = fmt.format(value);
    String txt = value > 999 ? "1K" : Integer.toString(value);
    AffineTransform at = txt.length() < 3 ? null : AffineTransform.getScaleInstance(.66, 1d);
    return new TextLayout(txt, g2.getFont(), g2.getFontRenderContext()).getOutline(at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (value <= 0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    Shape badge = getBadgeShape();
    g2.setPaint(badgeBgc);
    g2.fill(badge);
    g2.setPaint(badgeBgc.darker());
    g2.draw(badge);

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
    return 17;
  }

  @Override public int getIconHeight() {
    return 17;
  }
}

enum BadgePosition {
  NORTH_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST
}
