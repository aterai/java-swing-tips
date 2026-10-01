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
import java.util.logging.Logger;
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

    BadgeLabel information = new BadgeLabel(
        informationIcon, BadgePosition.SOUTH_EAST, 0);
    BadgeLabel error = new BadgeLabel(
        errorIcon, BadgePosition.SOUTH_EAST, 8);
    BadgeLabel question = new BadgeLabel(
        questionIcon, BadgePosition.SOUTH_WEST, 64);
    BadgeLabel warning = new BadgeLabel(
        warningIcon, BadgePosition.NORTH_EAST, 256);
    BadgeLabel information2 = new BadgeLabel(
        informationIcon, BadgePosition.NORTH_WEST, 1_024);
    LayerUI<BadgeLabel> ui = new BadgeLayerUI();
    Stream.of(information, error, question, warning, information2).forEach(l -> {
      l.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      add(new JLayer<>(l, ui));
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

class BadgeLabel extends JLabel {
  private final BadgePosition badgePosition;
  private final int count;

  protected BadgeLabel(Icon image, BadgePosition badgePosition, int count) {
    super(image);
    this.badgePosition = badgePosition;
    this.count = count;
  }

  public BadgePosition getBadgePosition() {
    return badgePosition;
  }

  public int getCount() {
    return count;
  }
}

class BadgeLayerUI extends LayerUI<BadgeLabel> {
  private static final Point OFFSET = new Point(6, 2);
  private static final Color BADGE_BACKGROUND = new Color(0xAA_FF_16_16, true);
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

      Icon badge = getBadgeIcon(label.getCount());
      Point pt = label.getBadgePosition().getLocation(iconRect, badge, OFFSET);
      badge.paintIcon(label, g2, pt.x, pt.y);
      g2.dispose();
    }
  }

  protected Icon getBadgeIcon(int count) {
    return new BadgeIcon(count, Color.WHITE, BADGE_BACKGROUND);
  }
}

class BadgeIconLayerUI extends BadgeLayerUI {
  private static final Color BADGE_BACKGROUND = new Color(0xAA_16_16_16, true);

  @Override protected Icon getBadgeIcon(int count) {
    return new BadgeIcon(count, Color.WHITE, BADGE_BACKGROUND) {
      @Override protected Shape getBadgeShape() {
        return new RoundRectangle2D.Double(
            0d, 0d, getIconWidth() - 1d, getIconHeight() - 1d, 5d, 5d);
      }
    };
  }
}

class BadgeIcon implements Icon {
  private static final int SIZE = 17;
  private final int value;
  private final Color foreground;
  private final Color background;

  protected BadgeIcon(int value, Color foreground, Color background) {
    this.value = value;
    this.foreground = foreground;
    this.background = background;
  }

  protected Shape getBadgeShape() {
    // Subtract 1px so that the outline stroke stays within the icon bounds
    return new Ellipse2D.Double(0d, 0d, getIconWidth() - 1d, getIconHeight() - 1d);
  }

  protected Shape getTextShape(Graphics2D g2) {
    // Java 12:
    // NumberFormat fmt = NumberFormat.getCompactNumberInstance(
    //    Locale.US, NumberFormat.Style.SHORT);
    // String txt = fmt.format(value);
    String txt = value < 1_000 ? Integer.toString(value) : Math.min(value / 1_000, 99) + "K";
    AffineTransform at = txt.length() < 3 ? null : AffineTransform.getScaleInstance(.66, 1d);
    return new TextLayout(txt, g2.getFont(), g2.getFontRenderContext()).getOutline(at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (value > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      Shape badge = getBadgeShape();
      g2.setPaint(background);
      g2.fill(badge);
      g2.setPaint(background.darker());
      g2.draw(badge);

      g2.setPaint(foreground);
      Shape shape = getTextShape(g2);
      Rectangle2D b = shape.getBounds2D();
      Rectangle2D r = badge.getBounds2D();
      double tx = r.getCenterX() - b.getCenterX();
      double ty = r.getCenterY() - b.getCenterY();
      AffineTransform toCenterAt = AffineTransform.getTranslateInstance(tx, ty);
      g2.fill(toCenterAt.createTransformedShape(shape));
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return SIZE;
  }

  @Override public int getIconHeight() {
    return SIZE;
  }
}

enum BadgePosition {
  NORTH_WEST {
    @Override public Point getLocation(Rectangle iconRect, Icon icon, Point offset) {
      return new Point(
          iconRect.x - offset.x,
          iconRect.y - offset.y);
    }
  },
  NORTH_EAST {
    @Override public Point getLocation(Rectangle iconRect, Icon icon, Point offset) {
      return new Point(
          iconRect.x + iconRect.width - icon.getIconWidth() + offset.x,
          iconRect.y - offset.y);
    }
  },
  SOUTH_EAST {
    @Override public Point getLocation(Rectangle iconRect, Icon icon, Point offset) {
      return new Point(
          iconRect.x + iconRect.width - icon.getIconWidth() + offset.x,
          iconRect.y + iconRect.height - icon.getIconHeight() + offset.y);
    }
  },
  SOUTH_WEST {
    @Override public Point getLocation(Rectangle iconRect, Icon icon, Point offset) {
      return new Point(
          iconRect.x - offset.x,
          iconRect.y + iconRect.height - icon.getIconHeight() + offset.y);
    }
  };

  public abstract Point getLocation(Rectangle iconRect, Icon icon, Point offset);
}
