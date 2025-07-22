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
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/9-0.gif";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    ImageIcon icon9 = makeImageIcon(url);
    ImageIcon animatedIcon = url == null ? icon9 : new ImageIcon(url);

    JTextArea textArea = new JTextArea();
    JButton button = new JButton(icon9) {
      @Override protected void fireStateChanged() {
        ButtonModel m = getModel();
        if (isRolloverEnabled() && m.isRollover()) {
          textArea.append("JButton: Rollover, Image: flush\n");
          animatedIcon.getImage().flush();
        }
        super.fireStateChanged();
      }
    };
    button.setRolloverIcon(animatedIcon);
    button.setPressedIcon(new PressedIcon(icon9));

    JLabel label = new JLabel(animatedIcon);
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        textArea.append("JLabel: mousePressed, Image: flush\n");
        animatedIcon.getImage().flush();
        e.getComponent().repaint();
      }
    });

    JPanel p = new JPanel(new GridLayout(1, 2, 5, 5));
    p.add(makeTitledPanel("JButton#setRolloverIcon", button));
    p.add(makeTitledPanel("mousePressed: flush", label));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ImageIcon makeImageIcon(URL url) {
    Image img = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    return new ImageIcon(img);
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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

class PressedIcon implements Icon {
  private final ImageIcon icon;

  protected PressedIcon(ImageIcon icon) {
    this.icon = icon;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.BLACK);
    g2.fillRect(x, y, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
