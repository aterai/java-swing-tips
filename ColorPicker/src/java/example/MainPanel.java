// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  private MainPanel() {
    super(new BorderLayout());
    JTextField field = new JTextField("#FFFFFF");
    field.setEditable(false);
    field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    field.setColumns(8);
    JLabel sample = new JLabel(new ColorIcon(Color.WHITE));

    JPanel box = new JPanel();
    box.add(sample);
    box.add(field);

    String path = "example/duke.gif";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    JLabel label = new JLabel(new ImageIcon(image));
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        updateLabelRect(label);
        Point pt = e.getPoint();
        if (iconRect.contains(pt)) {
          int argb = image.getRGB(pt.x - iconRect.x, pt.y - iconRect.y);
          field.setText(String.format("#%06X", argb & 0x00_FF_FF_FF));
          sample.setIcon(new ColorIcon(new Color(argb, true)));
        }
      }
    });
    add(label);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public void updateLabelRect(JLabel c) {
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);
    SwingUtilities.calculateInnerArea(c, viewRect);
    SwingUtilities.layoutCompoundLabel(
        c,
        c.getFontMetrics(c.getFont()),
        c.getText(),
        c.getIcon(),
        c.getVerticalAlignment(),
        c.getHorizontalAlignment(),
        c.getVerticalTextPosition(),
        c.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        c.getIconTextGap());
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setPaint(Color.BLACK);
    g2.drawRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
