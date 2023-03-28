// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    List<String> icons = Arrays.asList(
        "example/wi0009-16.png",
        "example/wi0054-16.png",
        "example/wi0062-16.png",
        "example/wi0063-16.png",
        "example/wi0124-16.png",
        "example/wi0126-16.png");
    icons.forEach(s -> {
      Icon icon = new ImageIcon(makeImage(s));
      ShrinkLabel label = new ShrinkLabel(s, icon);
      tabbedPane.addTab(s, icon, new JLabel(s), s);
      tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
    });
    updateTabWidth(tabbedPane);
    tabbedPane.addChangeListener(e -> updateTabWidth((JTabbedPane) e.getSource()));
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateTabWidth(JTabbedPane tabs) {
    int tp = tabs.getTabPlacement();
    if (tp == SwingConstants.LEFT || tp == SwingConstants.RIGHT) {
      return;
    }
    int idx = tabs.getSelectedIndex();
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Component c = tabs.getTabComponentAt(i);
      if (c instanceof ShrinkLabel) {
        ((ShrinkLabel) c).setSelected(i == idx);
      }
    }
  }

  private static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
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

class ShrinkLabel extends JLabel {
  private boolean selected;

  protected ShrinkLabel(String title, Icon icon) {
    super(title, icon, LEFT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (!selected && getIcon() != null) {
      d.width = getIcon().getIconWidth();
    }
    return d;
  }

  public void setSelected(boolean active) {
    this.selected = active;
  }

  public boolean isSelected() {
    return selected;
  }
}
