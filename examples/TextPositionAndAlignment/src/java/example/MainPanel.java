// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel("Test Test", new StarburstIcon(), SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(Color.WHITE);

    JComboBox<Vertical> vertAlignment = new JComboBox<>(Vertical.values());
    vertAlignment.setSelectedItem(Vertical.CENTER);

    JComboBox<Vertical> vertTextPosition = new JComboBox<>(Vertical.values());
    vertTextPosition.setSelectedItem(Vertical.CENTER);

    JComboBox<Horizontal> horizAlignment = new JComboBox<>(Horizontal.values());
    horizAlignment.setSelectedItem(Horizontal.CENTER);

    JComboBox<Horizontal> horizTextPosition = new JComboBox<>(Horizontal.values());
    horizTextPosition.setSelectedItem(Horizontal.TRAILING);

    ItemListener listener = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        label.setVerticalAlignment(getSelectedItem(vertAlignment).getAlignment());
        label.setVerticalTextPosition(getSelectedItem(vertTextPosition).getAlignment());
        label.setHorizontalAlignment(getSelectedItem(horizAlignment).getAlignment());
        label.setHorizontalTextPosition(getSelectedItem(horizTextPosition).getAlignment());
        label.repaint();
      }
    };
    Stream.of(vertAlignment, vertTextPosition, horizAlignment, horizTextPosition)
        .forEach(c -> c.addItemListener(listener));

    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(BorderFactory.createTitledBorder("JLabel Test"));
    p1.add(label);

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_END;

    JPanel p2 = new JPanel(new GridBagLayout());
    p2.add(new JLabel("setVerticalAlignment:"), c);
    p2.add(new JLabel("setVerticalTextPosition:"), c);
    p2.add(new JLabel("setHorizontalAlignment:"), c);
    p2.add(new JLabel("setHorizontalTextPosition:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p2.add(vertAlignment, c);
    p2.add(vertTextPosition, c);
    p2.add(horizAlignment, c);
    p2.add(horizTextPosition, c);

    add(p1);
    add(p2, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static <E> E getSelectedItem(JComboBox<E> combo) {
    return combo.getItemAt(combo.getSelectedIndex());
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

enum Vertical {
  TOP(SwingConstants.TOP),
  CENTER(SwingConstants.CENTER),
  BOTTOM(SwingConstants.BOTTOM);
  private final int alignment;

  Vertical(int alignment) {
    this.alignment = alignment;
  }

  public int getAlignment() {
    return alignment;
  }
}

enum Horizontal {
  LEFT(SwingConstants.LEFT),
  CENTER(SwingConstants.CENTER),
  RIGHT(SwingConstants.RIGHT),
  LEADING(SwingConstants.LEADING),
  TRAILING(SwingConstants.TRAILING);
  private final int alignment;

  Horizontal(int alignment) {
    this.alignment = alignment;
  }

  public int getAlignment() {
    return alignment;
  }
}

class StarburstIcon implements Icon {
  private static final int R2 = 24;
  private static final int R1 = 20;
  private static final int VC = 18;
  private final Shape star;

  protected StarburstIcon() {
    double agl = 0d;
    double add = Math.PI / VC;
    Path2D p = new Path2D.Double();
    p.moveTo(R2, 0d);
    for (int i = 0; i < VC * 2 - 1; i++) {
      agl += add;
      int r = i % 2 == 0 ? R1 : R2;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, R2, 0d);
    star = new Path2D.Double(p, at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.YELLOW);
    g2.fill(star);
    g2.setPaint(Color.BLACK);
    g2.draw(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 2 * R2;
  }

  @Override public int getIconHeight() {
    return 2 * R2;
  }
}
