// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JRadioButton r1 = new JRadioButton("img=null");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setIconImage(null);
      }
    });
    JRadioButton r2 = new JRadioButton("img=new ImageIcon(\"\").getImage()");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setIconImage(new ImageIcon("").getImage()); // JDK 1.5
      }
    });

    JRadioButton r3 = new JRadioButton("img=new BufferedImage(1, 1, TYPE_INT_ARGB)");
    r3.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)); // size=(1x1)
      }
    });

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/16x16transparent.png");
    Image image = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    JRadioButton r4 = new JRadioButton("img=toolkit.createImage(url_16x16transparent)", true);
    r4.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        // setIconImage(Toolkit.getDefaultToolkit().createImage(url)); // 16x16transparent.png
        setIconImage(image);
      }
    });

    if (url != null) {
      EventQueue.invokeLater(() -> setIconImage(Toolkit.getDefaultToolkit().createImage(url)));
    }

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder("frame.setIconImage(img)"));
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, r2, r3, r4).forEach(b -> {
      bg.add(b);
      box.add(b);
      box.add(Box.createVerticalStrut(5));
    });
    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeMissingImage() {
    return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
  }

  public void setIconImage(Image image) {
    Container c = getTopLevelAncestor();
    if (c instanceof Frame) {
      ((Frame) c).setIconImage(image);
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
