// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup0 = new JPopupMenu();
    initPopupMenu(popup0);

    DropShadowPopupMenu popup1 = new DropShadowPopupMenu();
    initPopupMenu(popup1);

    String path = "example/test.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    JLabel label = new JLabel(icon);
    label.setComponentPopupMenu(popup1);

    JCheckBox check = new JCheckBox("Paint Shadow", true);
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      label.setComponentPopupMenu(c.isSelected() ? popup1 : popup0);
    });
    add(check, BorderLayout.NORTH);
    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initPopupMenu(JPopupMenu p) {
    Stream.of("Open", "Save", "Close").map(s -> s.concat("(sample)")).forEach(p::add);
    // [JDK-6595814] Nimbus LAF: Renderers, MenuSeparators, colors rollup bug - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-6595814
    // Fixed 6u10: p.add(new JSeparator());
    p.addSeparator();
    p.add("Exit").addActionListener(e -> {
      JMenuItem m = (JMenuItem) e.getSource();
      JPopupMenu pop = (JPopupMenu) SwingUtilities.getUnwrappedParent(m);
      Optional.ofNullable(SwingUtilities.getWindowAncestor(pop.getInvoker()))
          .ifPresent(Window::dispose);
    });
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

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setStroke(new BasicStroke(w / 8f));
    g2.setColor(Color.RED);
    g2.translate(x, y);
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}

/*
final class DropShadowPopupMenu extends JPopupMenu {
  private static final int OFFSET = 4;
  private transient BufferedImage shadow;
  private transient Border inner;
  @Override public boolean isOpaque() {
    return false;
  }

  @Override public void updateUI() {
    // clear shadow border
    inner = null;
    setBorder(null);
    super.updateUI();
  }

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g); // ???: Windows LnF
    Graphics2D g2 = (Graphics2D) g.create();
    g2.drawImage(shadow, 0, 0, this);
    g2.setPaint(getBackground()); // ??? 1.7.0_03
    g2.fillRect(0, 0, getWidth() - OFFSET, getHeight() - OFFSET);
    g2.dispose();
  }

  @Override public void show(Component c, int x, int y) {
    if (Objects.isNull(inner)) {
      inner = getBorder();
    }
    setBorder(makeShadowBorder(c, new Point(x, y)));

    Dimension d = getPreferredSize();
    int w = d.width;
    int h = d.height;
    if (Objects.isNull(shadow) || shadow.getWidth() != w || shadow.getHeight() != h) {
      shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = shadow.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
      g2.setPaint(Color.BLACK);
      for (int i = 0; i < OFFSET; i++) {
        g2.fillRoundRect(OFFSET, OFFSET, w - OFFSET - OFFSET + i, h - OFFSET - OFFSET + i, 4, 4);
      }
      g2.dispose();
    }
    super.show(c, x, y);
  }

  private Border makeShadowBorder(Component c, Point p) {
    Rectangle r = SwingUtilities.getWindowAncestor(c).getBounds();
    Dimension d = this.getPreferredSize();
    SwingUtilities.convertPointToScreen(p, c);
    // System.out.println(r + " : " + p);
    // pointed out by sawshun
    Border outer;
    if (r.contains(p.x, p.y, d.width + OFFSET, d.height + OFFSET)) {
      outer = BorderFactory.createEmptyBorder(0, 0, OFFSET, OFFSET);
    } else {
      outer = new ShadowBorder(OFFSET, OFFSET, this, p);
    }
    return BorderFactory.createCompoundBorder(outer, inner);
  }
}

class ShadowBorder extends AbstractBorder {
  private final int xoff;
  private final int yoff;
  private final transient BufferedImage screen;
  private transient BufferedImage shadow;

  protected ShadowBorder(int x, int y, JComponent c, Point p) {
    super();
    this.xoff = x;
    this.yoff = y;
    BufferedImage bi = null;
    try {
      Robot robot = new Robot();
      Dimension d = c.getPreferredSize();
      bi = robot.createScreenCapture(new Rectangle(p.x, p.y, d.width + xoff, d.height + yoff));
    } catch (AWTException ex) {
      throw new IllegalStateException(ex);
    }
    screen = bi;
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(0, 0, xoff, yoff);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    if (Objects.isNull(screen)) {
      return;
    }
    if (Objects.isNull(shadow) || shadow.getWidth() != w || shadow.getHeight() != h) {
      shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = shadow.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
      g2.setPaint(Color.BLACK);
      for (int i = 0; i < xoff; i++) {
        g2.fillRoundRect(xoff, xoff, w - xoff - xoff + i, h - xoff - xoff + i, 4, 4);
      }
      g2.dispose();
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.drawImage(screen, 0, 0, c);
    g2.drawImage(shadow, 0, 0, c);
    g2.setPaint(c.getBackground()); // ??? 1.7.0_03
    g2.fillRect(x, y, w - xoff, h - yoff);
    g2.dispose();
  }
}
/*/
// JDK 1.7.0: JPopupMenu#setBackground(new Color(0x0, true));
final class DropShadowPopupMenu extends JPopupMenu {
  private static final int OFFSET = 4;
  private final Dimension dim = new Dimension();
  private transient BufferedImage shadow;

  @Override public void updateUI() {
    setBorder(null);
    super.updateUI();
    Border inner = getBorder();
    Border outer = BorderFactory.createEmptyBorder(0, 0, OFFSET, OFFSET);
    setBorder(BorderFactory.createCompoundBorder(outer, inner));
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.drawImage(shadow, 0, 0, this);
    g2.setPaint(getBackground()); // ??? 1.7.0_03
    g2.fillRect(0, 0, getWidth() - OFFSET, getHeight() - OFFSET);
    g2.dispose();
  }

  @Override public void show(Component c, int x, int y) {
    Dimension d = getPreferredSize();
    int w = d.width;
    int h = d.height;
    if (dim.width != w || dim.height != h) {
      dim.setSize(w, h);
      shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = shadow.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
      g2.setPaint(Color.BLACK);
      for (int i = 0; i < OFFSET; i++) {
        g2.fillRoundRect(OFFSET, OFFSET, w - OFFSET - OFFSET + i, h - OFFSET - OFFSET + i, 4, 4);
      }
      g2.dispose();
    }
    EventQueue.invokeLater(() -> Optional.ofNullable(SwingUtilities.getWindowAncestor(this))
        .filter(top -> {
          boolean isHeavyWeight = top.getType() == Window.Type.POPUP;
          GraphicsConfiguration gc = top.getGraphicsConfiguration();
          return gc != null && gc.isTranslucencyCapable() && isHeavyWeight;
        })
        .ifPresent(top -> top.setBackground(new Color(0x0, true))));
    super.show(c, x, y);
  }
}
//*/
