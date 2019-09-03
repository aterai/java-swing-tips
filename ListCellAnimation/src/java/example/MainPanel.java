// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane scroll = new JScrollPane(makeList());
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> makeList() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("asdfasdfasdfsadfas");
    model.addElement("qwerqwerqwerqwerweqr");
    model.addElement("zxcvzxcbzxcvzxcbzxcbzxcbzxcvzxcbzxbzxcvzxcbzcvbzxcvzxcvzx");
    model.addElement("tryurtirtiriu");
    model.addElement("jhkghjkfhjkghjkhjk");
    model.addElement("bnm, bnmvmvbm, vbmfmvbmn");
    model.addElement("1234123541514354677697808967867895678474567356723456245624");
    model.addElement("qwerqwerrqwettrtrytru");
    model.addElement("tiutyityityoiuo");
    model.addElement("hjklgkghkghk");
    model.addElement("zxcvzxcvbvnvbmvbmbm");
    JList<String> list = new JList<>(model);
    list.setCellRenderer(new AnimeListCellRenderer<>(list));
    return list;
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AnimeListCellRenderer<E> extends JPanel implements ListCellRenderer<E>, HierarchyListener {
  private static final Color SELECTEDCOLOR = new Color(0xE6_E6_FF);
  protected final AnimeIcon icon = new AnimeIcon();
  private final MarqueeLabel label = new MarqueeLabel();
  private final Timer animator;
  protected final JList<E> list;
  private boolean running;
  private int animateIndex = -1;

  protected AnimeListCellRenderer(JList<E> l) {
    super(new BorderLayout());
    this.list = l;
    animator = new Timer(80, e -> {
      int i = list.getSelectedIndex();
      if (i >= 0) {
        running = true;
        list.repaint(list.getCellBounds(i, i));
      } else {
        running = false;
      }
    });
    setOpaque(true);
    add(icon, BorderLayout.WEST);
    add(label);
    list.addHierarchyListener(this);
    // animator.start();
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
      if (e.getComponent().isDisplayable()) {
        animator.start();
      } else {
        animator.stop();
      }
    }
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> l, E value, int index, boolean isSelected, boolean cellHasFocus) {
    setBackground(isSelected ? SELECTEDCOLOR : l.getBackground());
    label.setText(Objects.toString(value, ""));
    animateIndex = index;
    return this;
  }

  protected boolean isAnimatingCell() {
    return running && animateIndex == list.getSelectedIndex();
  }

  private class MarqueeLabel extends JLabel {
    private float xx;

    protected MarqueeLabel() {
      super();
      setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle r = list.getVisibleRect();
      int cw = r.width - icon.getPreferredSize().width;
      FontRenderContext frc = g2.getFontRenderContext();
      GlyphVector gv = getFont().createGlyphVector(frc, getText());
      if (isAnimatingCell() && gv.getVisualBounds().getWidth() > cw) {
        LineMetrics lm = getFont().getLineMetrics(getText(), frc);
        float yy = lm.getAscent() / 2f + (float) gv.getVisualBounds().getY();
        g2.drawGlyphVector(gv, cw - xx, getHeight() / 2f - yy);
        xx = cw + gv.getVisualBounds().getWidth() - xx > 0 ? xx + 8f : 0f;
      } else {
        super.paintComponent(g);
      }
      g2.dispose();
    }
  }

  private class AnimeIcon extends JComponent {
    private static final double R = 2d;
    private static final double SX = 1d;
    private static final double SY = 1d;
    private static final int WIDTH = (int) (R * 8 + SX * 2);
    private static final int HEIGHT = (int) (R * 8 + SY * 2);
    private final List<Shape> flipbookFrames = new ArrayList<>(Arrays.asList(
        new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

    protected AnimeIcon() {
      super();
      setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      setOpaque(false);
    }

    @Override public Dimension getPreferredSize() {
      return new Dimension(WIDTH + 2, HEIGHT);
    }

    @Override protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      // g2.setPaint(getBackground());
      // g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (isAnimatingCell()) {
        float alpha = .1f;
        for (Shape s: flipbookFrames) {
          g2.setPaint(makeColor(alpha));
          g2.fill(s);
          alpha += .1f;
        }
        // flipbookFrames.add(flipbookFrames.remove(0));
        Collections.rotate(flipbookFrames, 1);
      } else {
        g2.setPaint(new Color(0x99_99_99));
        for (Shape s: flipbookFrames) {
          g2.fill(s);
        }
      }
      g2.dispose();
    }
  }

  protected static Color makeColor(float alpha) {
    return new Color(.5f, .5f, .5f, alpha);
  }
}
