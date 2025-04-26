// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/duke.running.gif");
    Icon icon = Optional.ofNullable(url).<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
    JLabel label1 = new JLabel(icon);
    label1.setEnabled(false);
    label1.setBorder(BorderFactory.createTitledBorder("Default"));

    JLabel label2 = new JLabel(icon) {
      @Override public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        int info = infoflags;
        if (!isEnabled()) {
          info &= ~FRAMEBITS;
        }
        return super.imageUpdate(img, info, x, y, w, h);
      }
    };
    label2.setEnabled(false);
    label2.setBorder(BorderFactory.createTitledBorder("Override imageUpdate(...)"));

    JLabel label3 = new JLabel(icon);
    label3.setEnabled(false);
    label3.setBorder(BorderFactory.createTitledBorder("setDisabledIcon"));
    String path = "example/duke.running_frame_0001.gif";
    Image image = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    label3.setDisabledIcon(makeDisabledIcon(image));

    JCheckBox check = new JCheckBox("setEnabled");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      label1.setEnabled(c.isSelected());
      label2.setEnabled(c.isSelected());
      label3.setEnabled(c.isSelected());
    });
    JPanel p = new JPanel(new GridLayout(2, 2));
    p.add(label1);
    p.add(label2);
    p.add(label3);
    add(check, BorderLayout.NORTH);
    add(p);
    // setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }

  private static Icon makeDisabledIcon(Image img) {
    // BufferedImage source = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    // Graphics g = source.createGraphics();
    // g.drawImage(img, 0, 0, null);
    // g.dispose();
    // ColorConvertOp cc = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    // BufferedImage dst = cc.filter(source, null);
    // return new ImageIcon(dst);
    return new ImageIcon(GrayFilter.createDisabledImage(img));
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
    // frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
