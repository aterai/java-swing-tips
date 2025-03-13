// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/16x16.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    Icon icon = new ImageIcon(img);
    JLabel label1 = new JLabel(icon);
    JTextField field1 = new JTextField("1111111111111111") {
      @Override public void updateUI() {
        super.updateUI();
        add(label1);
      }
    };

    int w = icon.getIconWidth();
    Insets m = field1.getMargin();
    field1.setMargin(new Insets(m.top, m.left + w, m.bottom, m.right));
    label1.setCursor(Cursor.getDefaultCursor());
    label1.setBorder(BorderFactory.createEmptyBorder());
    label1.setBounds(m.left, m.top, w, icon.getIconHeight());

    JLabel label2 = new JLabel(icon);
    label2.setCursor(Cursor.getDefaultCursor());
    label2.setBorder(BorderFactory.createEmptyBorder());
    JTextField field2 = new JTextField("2222222222222222222222222222222222222") {
      @Override public void updateUI() {
        super.updateUI();
        removeAll();
        SpringLayout l = new SpringLayout();
        setLayout(l);
        Spring fw = l.getConstraint(SpringLayout.WIDTH, this);
        Spring fh = l.getConstraint(SpringLayout.HEIGHT, this);
        SpringLayout.Constraints c = l.getConstraints(label2);
        c.setConstraint(SpringLayout.WEST, fw);
        c.setConstraint(SpringLayout.SOUTH, fh);
        add(label2);
      }
    };
    m = field2.getMargin();
    field2.setMargin(new Insets(m.top + 2, m.left, m.bottom, m.right + w));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("Default", new JTextField("000000000000")));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("add Image(JLabel)", field1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("SpringLayout", field2));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 8 - w / 2, 8 - h / 2);
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
