// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final Color BG_COLOR = new Color(0x64_FF_CC_CC, true);
  private transient TexturePaint texture;

  private MainPanel() {
    super(new GridBagLayout());
    JTextField field0 = new JTextField("000000000000");
    field0.setBackground(BG_COLOR);

    JTextField field1 = new JTextField("1111111111111111111111");
    field1.setOpaque(false);
    field1.setBackground(BG_COLOR);

    JTextField field2 = new JTextField("2222222222222222") {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    field2.setOpaque(false);
    field2.setBackground(BG_COLOR);

    Border inside = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
    Border outside = BorderFactory.createTitledBorder("setBackground(1.0, 0.8, 0.8, 0.2)");
    setBorder(BorderFactory.createCompoundBorder(outside, inside));
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.insets = new Insets(15, 15, 15, 0);
    c.anchor = GridBagConstraints.LINE_START;
    add(new JLabel("0. setOpaque(true)"), c);
    add(new JLabel("1. setOpaque(false)"), c);
    add(new JLabel("2. 1+paintComponent"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(field0, c);
    add(field1, c);
    add(field2, c);

    setPreferredSize(new Dimension(320, 240));
  }

  public TexturePaint makeTexturePaint() {
    // unkaku_w.gif https://www.viva-edo.com/komon/edokomon.html
    String path = "example/unkaku_w.gif";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage bi = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (Objects.isNull(texture)) {
      texture = makeTexturePaint();
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(texture);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
