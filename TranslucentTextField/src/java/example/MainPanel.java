// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final Color BG_COLOR = new Color(1f, .8f, .8f, .2f);
  private transient TexturePaint texture;

  public MainPanel() {
    super(new GridBagLayout());

    JTextField field0 = new JTextField("aaaaaaaaa");
    field0.setBackground(BG_COLOR);

    JTextField field1 = new JTextField("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
    field1.setOpaque(false);
    field1.setBackground(BG_COLOR);

    JTextField field2 = new JTextField("cccccccccccccccccccccc") {
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

  private TexturePaint makeTexturePaint() {
    // Viva! edo > http://www.viva-edo.com/komon/edokomon.html
    URL url = getClass().getResource("unkaku_w.gif");
    BufferedImage bi = null;
    try {
      bi = ImageIO.read(url);
      // bi = makeBufferedImage(ImageIO.read(url), new float[] {1f, 1f, .5f});
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new IllegalArgumentException(ex);
    }
    return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
