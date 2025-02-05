// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(makeLabelTable(6, 4));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Component makeLabelTable(int row, int column) {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1;
    c.weightx = 1;

    float length = 5f;
    float spacing = 5f;
    float[] array = {length - 1f, spacing + 1f};
    BasicStroke dashedStroke = new BasicStroke(
        1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 2f, array, 0f);
    Border dashed = new StrokeMatteBorder(0, 0, 1, 1, dashedStroke, Color.BLACK);
    JPanel p = new JPanel(new GridBagLayout());
    for (c.gridy = 0; c.gridy < row; c.gridy++) {
      for (c.gridx = 0; c.gridx < column; c.gridx++) {
        JLabel l = makeLabel(String.format(Locale.ENGLISH, "%d%d", c.gridx, c.gridy));
        l.setBorder(BorderFactory.createCompoundBorder(
            dashed, BorderFactory.createEmptyBorder(1, 1, 0, 0)));
        p.add(l, c);
      }
    }
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(15, 15, 15 + 1, 15 + 1),
        new StrokeMatteBorder(1, 1, 0, 0, dashedStroke, Color.RED)));
    return p;
  }

  private static JLabel makeLabel(String title) {
    return new JLabel(title, SwingConstants.CENTER);
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

class StrokeMatteBorder extends EmptyBorder {
  private final transient BasicStroke stroke;
  private final Color color;

  protected StrokeMatteBorder(int t, int l, int b, int r, BasicStroke stroke, Color color) {
    super(t, l, b, r);
    this.stroke = stroke;
    this.color = color;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    float size = stroke.getLineWidth();
    if (size > 0) {
      Color fgc = Optional.ofNullable(this.color)
          .orElse(Optional.ofNullable(c).map(Component::getForeground).orElse(null));
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setStroke(this.stroke);
      g2.setPaint(fgc);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(x, y);

      int s = Math.round(size);
      int sd2 = Math.round(size / 2f);
      Insets insets = getBorderInsets(c);
      if (insets.top > 0) {
        g2.drawLine(0, sd2, width - s, sd2);
      }
      if (insets.left > 0) {
        g2.drawLine(sd2, sd2, sd2, height - s);
      }
      if (insets.bottom > 0) {
        g2.drawLine(0, height - s, width - s, height - s);
      }
      if (insets.right > 0) {
        g2.drawLine(width - sd2, 0, width - sd2, height - s);
      }
      g2.dispose();
    }
  }
}
