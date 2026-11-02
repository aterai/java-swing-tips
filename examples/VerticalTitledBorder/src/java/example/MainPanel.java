// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3, 5, 5));
    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(new JScrollPane(new JTree()));
    p1.setBorder(BorderFactory.createTitledBorder("TitledBorder 1234567890"));

    JPanel p2 = new JPanel(new BorderLayout());
    p2.add(new JScrollPane(new JTree()));
    p2.setBorder(new VerticalTitledBorder("VerticalTitledBorder 1234567890"));

    JPanel p3 = new JPanel(new BorderLayout());
    p3.add(new JScrollPane(new JTree()));
    p3.setBorder(BorderFactory.createTitledBorder(
        new VerticalTitledBorder("VerticalTitledBorder"), "TitledBorder"));

    add(p1);
    add(p2);
    add(p3);
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

class VerticalTitledBorder extends TitledBorder {
  private final JLabel label;

  protected VerticalTitledBorder(String title) {
    super(title);
    this.label = new JLabel(title);
    // Fill the label background so that the border line behind the title is hidden
    this.label.setOpaque(true);
    // this.label.putClientProperty(BasicHTML.propertyKey, null);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Border border = getBorder();
    String title = getTitle();
    if (Objects.isNull(title) || title.isEmpty() || Objects.isNull(border)) {
      super.paintBorder(c, g, x, y, width, height);
    } else {
      int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
      JLabel titleLabel = getTitleLabel(c);
      Dimension size = titleLabel.getPreferredSize();
      Insets borderInsets = getInnerBorderInsets(border, c, new Insets(0, 0, 0, 0));

      int labelH = size.height;
      int labelW = Math.min(height - borderInsets.top - borderInsets.bottom, size.width);
      int borderX = x + edge;
      int borderY = y + edge;
      int borderW = width - edge - edge;
      int borderH = height - edge - edge;

      // Center the title on the left border line, the same way as
      // TitledBorder centers the title on the top border line
      int labelX = x;
      int left = edge + borderInsets.left / 2 - labelH / 2;
      if (left < edge) {
        borderX -= left;
        borderW += left;
      } else {
        labelX += left;
      }
      border.paintBorder(c, g, borderX, borderY, borderW, borderH);

      Graphics2D g2 = (Graphics2D) g.create();
      // The (x, y) offset is not zero when nested in another border,
      // e.g. a TitledBorder or a CompoundBorder
      g2.translate(labelX, y + (height + labelW) / 2d);
      g2.transform(AffineTransform.getQuadrantRotateInstance(-1));
      // or: g2.rotate(-Math.PI / 2d);
      // or: g2.rotate(Math.toRadians(-90));
      titleLabel.setSize(labelW, labelH);
      titleLabel.paint(g2);
      g2.dispose();
    }
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    Border border = getBorder();
    Insets borderInsets = getInnerBorderInsets(border, c, insets);
    String title = getTitle();
    if (Objects.nonNull(title) && !title.isEmpty()) {
      int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
      Dimension size = getTitleLabel(c).getPreferredSize();
      if (borderInsets.left < size.height) {
        borderInsets.left = size.height - edge;
      }
      borderInsets.top += edge + TEXT_SPACING;
      borderInsets.left += edge + TEXT_SPACING;
      borderInsets.right += edge + TEXT_SPACING;
      borderInsets.bottom += edge + TEXT_SPACING;
    }
    return borderInsets;
  }

  // @see javax/swing/border/TitledBorder.java#getTitleColor(Component)
  private Color getTitleColor(Component c) {
    Color color = getTitleColor();
    if (Objects.isNull(color)) {
      color = UIManager.getColor("TitledBorder.titleColor");
    }
    if (Objects.isNull(color) && Objects.nonNull(c)) {
      color = c.getForeground();
    }
    return color;
  }

  private JLabel getTitleLabel(Component c) {
    label.setText(getTitle());
    label.setFont(getFont(c));
    label.setForeground(getTitleColor(c));
    if (Objects.nonNull(c)) {
      label.setComponentOrientation(c.getComponentOrientation());
      label.setEnabled(c.isEnabled());
      label.setBackground(c.getBackground());
    }
    return label;
  }

  // @see javax/swing/border/TitledBorder.java#getBorderInsets(Border, Component, Insets)
  // Reinitialize and return the given insets, as AbstractBorder#getBorderInsets(...) does
  private static Insets getInnerBorderInsets(Border border, Component c, Insets insets) {
    Insets ins = insets;
    if (Objects.isNull(border)) {
      ins.set(0, 0, 0, 0);
    } else if (border instanceof AbstractBorder) {
      ins = ((AbstractBorder) border).getBorderInsets(c, ins);
    } else {
      Insets i = border.getBorderInsets(c);
      ins.set(i.top, i.left, i.bottom, i.right);
    }
    return ins;
  }
}
