// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final Paint TEXTURE = ImageUtils.makeCheckerTexture();

  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = makeRoundedTextField1();
    field1.setText("1111111111111111");
    JTextField field2 = makeRoundedTextField2();
    field2.setText("2222222222222");
    JRadioButton r1 = new JRadioButton("default", true);
    JRadioButton r2 = new JRadioButton("setOpaque(false) + TexturePaint");
    ActionListener l = e -> {
      setOpaque(r1.equals(e.getSource()));
      repaint();
    };
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Stream.of(r1, r2).forEach(b -> {
      b.addActionListener(l);
      b.setOpaque(false);
      bg.add(b);
      box.add(b);
    });
    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.setOpaque(false);
    p.add(makeTitledPanel("Override: JTextField#paintComponent(...)", field1));
    p.add(makeTitledPanel("setBorder(new RoundedCornerBorder())", field2));
    add(p);
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(2, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  private JTextField makeRoundedTextField1() {
    return new JTextField(20) {
      // Unleash Your Creativity with Swing and the Java 2D API!
      // https://web.archive.org/web/20091205092230/http://java.sun.com/products/jfc/tsc/articles/swing2d/index.html
      @Override protected void paintComponent(Graphics g) {
        if (!isOpaque()) {
          int w = getWidth() - 1;
          int h = getHeight() - 1;
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setPaint(UIManager.getColor("TextField.background"));
          g2.fillRoundRect(0, 0, w, h, h, h);
          g2.setPaint(Color.GRAY);
          g2.drawRoundRect(0, 0, w, h, h, h);
          g2.dispose();
        }
        super.paintComponent(g);
      }

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        // setBackground(new Color(0x0, true));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
      }
    };
  }

  private JTextField makeRoundedTextField2() {
    return new JTextField(20) {
      @Override protected void paintComponent(Graphics g) {
        Border b = getBorder();
        if (!isOpaque() && b instanceof RoundedCornerBorder) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(getBackground());
          int w = getWidth() - 1;
          int h = getHeight() - 1;
          g2.fill(((RoundedCornerBorder) b).getBorderShape(0, 0, w, h));
          g2.dispose();
        }
        super.paintComponent(g);
      }

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(new RoundedCornerBorder());
      }
    };
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!isOpaque()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(TEXTURE);
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
    }
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

class RoundedCornerBorder extends AbstractBorder {
  private static final Paint ALPHA_ZERO = new Color(0x0, true);

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Shape border = getBorderShape(x, y, width - 1, height - 1);

    // Container parent = c.getParent();
    // if (Objects.nonNull(parent)) {
    //   // g2.setPaint(parent.getBackground());
    //   g2.setPaint(new Color(0x0, true));
    //   Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
    //   corner.subtract(new Area(border));
    //   g2.fill(corner);
    // }

    g2.setPaint(ALPHA_ZERO);
    // Area corner = new Area(border.getBounds2D());
    Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
    corner.subtract(new Area(border));
    g2.fill(corner);

    g2.setPaint(Color.GRAY);
    g2.draw(border);
    g2.dispose();
  }

  public Shape getBorderShape(int x, int y, int w, int h) {
    int r = h - 1; // h / 2;
    return new RoundRectangle2D.Double(x, y, w, h, r, r);
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(4, 8, 4, 8);
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(4, 8, 4, 8);
    return insets;
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static TexturePaint makeCheckerTexture() {
    int cs = 6;
    int sz = cs * cs;
    BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(new Color(0x32_64_64_64, true));
    g2.fillRect(0, 0, sz, sz);
    for (int i = 0; i * cs < sz; i++) {
      for (int j = 0; j * cs < sz; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(sz, sz));
  }
}
