// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // @SuppressWarnings("PMD.UseProperClassLoader")
    // URL url1 = getClass().getClassLoader().getResource("example/test.png");
    URL url1 = Thread.currentThread().getContextClassLoader().getResource("example/test.png");
    JLabel label1 = new JLabel(Objects.toString(url1));
    label1.setIcon(makeIcon(url1));
    label1.setVerticalTextPosition(SwingConstants.TOP);
    label1.setHorizontalTextPosition(SwingConstants.CENTER);

    URL url2 = getClass().getResource("test.png");
    JLabel label2 = new JLabel(Objects.toString(url2));
    label2.setIcon(makeIcon(url2));
    label2.setVerticalTextPosition(SwingConstants.TOP);
    label2.setHorizontalTextPosition(SwingConstants.CENTER);

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.add(makeTitledPanel("getClassLoader().getResource(\"example/test.png\")", label1));
    p.add(makeTitledPanel("getClass().getResource(\"test.png\")", label2));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Icon makeIcon(URL url) {
    return Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("html.missingImage");
      }
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
