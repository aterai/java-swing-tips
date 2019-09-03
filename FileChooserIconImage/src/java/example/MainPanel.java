// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final List<Image> IMAGE_LIST = Arrays.asList(
      makeImage(16, Color.RED),
      makeImage(18, Color.GREEN),
      makeImage(20, Color.YELLOW),
      makeImage(24, Color.PINK),
      makeImage(32, Color.ORANGE),
      makeImage(40, Color.CYAN),
      makeImage(64, Color.MAGENTA));
  // TEST:
  // private static final List<Image> IMAGE_LIST2 = Arrays.asList(
  //   makeImage(64, Color.RED),
  //   makeImage(32, Color.GREEN),
  //   makeImage(24, Color.PINK),
  //   makeImage(20, Color.ORANGE),
  //   makeImage(18, Color.CYAN));

  private MainPanel() {
    super(new BorderLayout());

    JButton button1 = new JButton("default icon");
    button1.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.showOpenDialog(getRootPane());
    });

    JButton button2 = new JButton("makeImage(16, Color.WHITE)");
    button2.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser() {
        @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
          JDialog dialog = super.createDialog(parent);
          dialog.setIconImage(makeImage(16, Color.WHITE));
          return dialog;
        }
      };
      fileChooser.showOpenDialog(getRootPane());
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showOpenDialog(...)"));
    p.add(button1);
    p.add(button2);

    DefaultListModel<Icon> icons = new DefaultListModel<>();
    IMAGE_LIST.stream().map(ImageIcon::new).forEach(icons::addElement);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JList<>(icons)));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Image makeImage(int size, Color color) {
    // BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(color);
    g2.fill(new Ellipse2D.Double(0, 0, size - 1, size - 1));
    FontRenderContext frc = g2.getFontRenderContext();
    Font font = g2.getFont().deriveFont(AffineTransform.getScaleInstance(.8, 1d));
    Shape s = new TextLayout(Integer.toString(size), font, frc).getOutline(null);
    g2.setPaint(Color.BLACK);
    Rectangle2D b = s.getBounds();
    Point2D p = new Point2D.Double(b.getX() + b.getWidth() / 2d, b.getY() + b.getHeight() / 2d);
    AffineTransform toCenter = AffineTransform.getTranslateInstance(size / 2d - p.getX(), size / 2d - p.getY());
    g2.fill(toCenter.createTransformedShape(s));
    g2.dispose();
    return image;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("@title@");
    // @see https://stackoverflow.com/questions/18224184/sizes-of-frame-icons-used-in-swing
    frame.setIconImages(IMAGE_LIST);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
