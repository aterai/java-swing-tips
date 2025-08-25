// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1, 5, 5));
    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(makeRoundedTitledBorder("Title001", 4));
    p1.add(new JScrollPane(new JTree()));
    JPanel p2 = new JPanel(new BorderLayout());
    p2.setBorder(makeRoundedTitledBorder("Title(2)", 8));
    p2.add(new JScrollPane(new JTable(5, 3)));
    JScrollPane scroll = new JScrollPane(new JTextArea(10, 20));
    scroll.setBorder(new RoundedBorder(8));
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    JLayer<JScrollPane> p3 = new JLayer<>(scroll, new TitleLayerUI("Title: 3", 8));
    Stream.of(p1, p2, p3).forEach(this::add);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Border makeRoundedTitledBorder(String text, int arc) {
    TitledBorder b = new RoundedTitledBorder(text, arc);
    b.setTitlePosition(TitledBorder.BELOW_TOP);
    b.setTitleColor(Color.WHITE);
    return b;
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

class RoundedTitledBorder extends TitledBorder {
  private final JLabel label = new JLabel(" ");
  private final int arc;

  protected RoundedTitledBorder(String title, int arc) {
    super(new RoundedBorder(arc), title);
    this.arc = arc;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Border b = getBorder();
    if (b instanceof RoundedBorder) {
      Dimension d = getLabel(c).getPreferredSize();
      Insets i = b.getBorderInsets(c);
      int a2 = arc * 2;
      int w = d.width + i.left + i.right + a2;
      int h = d.height + i.top + i.bottom;
      g2.setClip(((RoundedBorder) b).getBorderShape(x + i.left, y + i.top, w, h));
      g2.setPaint(Color.GRAY);
      Shape titleBg = new RoundRectangle2D.Float(x - a2, y - a2, w + a2, h + a2, arc, arc);
      g2.fill(titleBg);
      g2.dispose();
    }
    g2.dispose();
    super.paintBorder(c, g, x, y, width, height);
  }

  private JLabel getLabel(Component c) {
    this.label.setText(getTitle());
    this.label.setFont(getFont(c));
    this.label.setComponentOrientation(c.getComponentOrientation());
    this.label.setEnabled(c.isEnabled());
    return this.label;
  }
}

class RoundedBorder extends EmptyBorder {
  private static final Paint ALPHA_ZERO = new Color(0x0, true);
  private final int arc;

  protected RoundedBorder(int arc) {
    super(2, 2, 2, 2);
    this.arc = arc;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Shape border = getBorderShape(x, y, width, height);
    g2.setPaint(ALPHA_ZERO);
    Area clear = new Area(new Rectangle2D.Double(x, y, width, height));
    clear.subtract(new Area(border));
    g2.fill(clear);
    g2.setPaint(Color.GRAY);
    g2.setStroke(new BasicStroke(1.5f));
    g2.draw(border);
    g2.dispose();
  }

  protected Shape getBorderShape(int x, int y, int w, int h) {
    return new RoundRectangle2D.Double(x, y, w - 1, h - 1, arc, arc);
  }
}

class TitleLayerUI extends LayerUI<JScrollPane> {
  private final JLabel label;

  protected TitleLayerUI(String title, int arc) {
    super();
    label = new RoundedLabel(title, arc);
    label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    // label.setOpaque(false);
    label.setForeground(Color.WHITE);
    label.setBackground(Color.GRAY);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane sp = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle r = SwingUtilities.calculateInnerArea(sp, sp.getBounds());
      if (r != null && !sp.getViewport().getView().hasFocus()) {
        Dimension d = label.getPreferredSize();
        SwingUtilities.paintComponent(g, label, sp, r.x - 1, r.y - 1, d.width, d.height);
      }
    }
  }

  @Override public void updateUI(JLayer<? extends JScrollPane> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(label);
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.FOCUS_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends JScrollPane> l) {
    super.processFocusEvent(e, l);
    l.getView().repaint();
  }

  private static class RoundedLabel extends JLabel {
    private final int arc;

    public RoundedLabel(String title, int arc) {
      super(title);
      this.arc = arc;
    }

    @Override protected void paintComponent(Graphics g) {
      if (!isOpaque()) {
        Dimension d = getPreferredSize();
        int w = d.width - 1;
        int h = d.height - 1;
        int h2 = h / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, w, h2);
        g2.fillRoundRect(-arc, 0, w + arc, h, arc, arc);
        g2.dispose();
      }
      super.paintComponent(g);
    }
  }
}
