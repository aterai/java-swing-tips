// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String title = "TitledBorder";
    Box box = Box.createVerticalBox();
    box.add(new TitledSeparator(title, TitledBorder.DEFAULT_POSITION));
    box.add(new JCheckBox("JCheckBox 0"));
    box.add(new JCheckBox("JCheckBox 1"));
    box.add(Box.createVerticalStrut(10));

    Color color = new Color(0x64_B4_C8);
    box.add(new TitledSeparator(title + " ABOVE_TOP", color, TitledBorder.ABOVE_TOP));
    box.add(new JCheckBox("JCheckBox 2"));
    box.add(new JCheckBox("JCheckBox 3"));
    box.add(Box.createVerticalStrut(10));

    box.add(new JSeparator());
    box.add(new JCheckBox("JCheckBox 4"));
    box.add(new JCheckBox("JCheckBox 5"));

    add(box, BorderLayout.NORTH);
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

class TitledSeparator extends JLabel {
  private final String title;
  private final Color target;
  private final int titlePosition;

  protected TitledSeparator(String title, int titlePosition) {
    this(title, null, titlePosition);
  }

  protected TitledSeparator(String title, Color target, int titlePosition) {
    super();
    this.title = title;
    this.target = target;
    this.titlePosition = titlePosition;
    // updateBorder();
  }

  private void updateBorder() {
    // int height = new JSeparator().getPreferredSize().height;
    Icon icon = new TitledSeparatorIcon();
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createMatteBorder(icon.getIconHeight(), 0, 0, 0, icon),
        title,
        TitledBorder.DEFAULT_JUSTIFICATION,
        titlePosition));
  }

  @Override public Dimension getMaximumSize() {
    Dimension d = super.getPreferredSize();
    d.width = Short.MAX_VALUE;
    return d;
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(this::updateBorder);
  }

  private final class TitledSeparatorIcon implements Icon {
    private int width = -1;
    private Paint painter1;
    private Paint painter2;

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      int w = c.getWidth();
      if (w != width || Objects.isNull(painter1) || Objects.isNull(painter2)) {
        width = w;
        Point2D start = new Point2D.Float();
        Point2D end = new Point2D.Float(width, 0f);
        float[] dist = {0f, 1f};
        Color bgc = getBackground();
        Color ec = Optional.ofNullable(bgc).orElse(UIManager.getColor("Panel.background"));
        Color sc = Optional.ofNullable(target).orElse(ec);
        painter1 = new LinearGradientPaint(start, end, dist, new Color[] {sc.darker(), ec});
        painter2 = new LinearGradientPaint(start, end, dist, new Color[] {sc.brighter(), ec});
      }
      int h = getIconHeight() / 2;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      g2.setPaint(painter1);
      g2.fillRect(x, y, width, getIconHeight());
      g2.setPaint(painter2);
      g2.fillRect(x, y + h, width, getIconHeight() - h);
      g2.dispose();
    }

    @Override public int getIconWidth() {
      return 200; // sample width
    }

    @Override public int getIconHeight() {
      return 2;
    }
  }
}
