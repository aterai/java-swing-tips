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
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    CompactTabbedPane tabbedPane = new CompactTabbedPane();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    List<String> icons = Arrays.asList(
        "example/wi0009-16.png",
        "example/wi0054-16.png",
        "example/wi0062-16.png",
        "example/wi0063-16.png",
        "example/wi0124-16.png",
        "example/wi0126-16.png");
    tabbedPane.initializeTabs(icons);
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
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

class CompactTabbedPane extends JTabbedPane {
  @Override public void updateUI() {
    super.updateUI();
    setTabPlacement(TOP);
    setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
    // addChangeListener(e -> updateTabWidth());
  }

  @Override public void doLayout() {
    super.doLayout();
    updateTabSelectionState();
  }

  protected void initializeTabs(List<String> iconPaths) {
    iconPaths.forEach(s -> {
      Icon icon = new ImageIcon(loadImage(s));
      CollapsibleTabLabel label = new CollapsibleTabLabel(s, icon);
      addTab(s, icon, new JLabel(s), s);
      setTabComponentAt(getTabCount() - 1, label);
    });
    updateTabSelectionState();
  }

  private static Image loadImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      Image img;
      try (InputStream s = url.openStream()) {
        img = ImageIO.read(s);
      } catch (IOException ex) {
        img = createMissingImage();
      }
      return img;
    }).orElseGet(CompactTabbedPane::createMissingImage);
  }

  private static Image createMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }

  private void updateTabSelectionState() {
    int placement = getTabPlacement();
    if (placement == TOP || placement == BOTTOM) {
      int selectedIndex = getSelectedIndex();
      for (int i = 0; i < getTabCount(); i++) {
        Component c = getTabComponentAt(i);
        if (c instanceof CollapsibleTabLabel) {
          ((CollapsibleTabLabel) c).setTabSelected(i == selectedIndex);
        }
      }
    }
  }
}

class CollapsibleTabLabel extends JLabel {
  private boolean tabSelected;

  protected CollapsibleTabLabel(String title, Icon icon) {
    super(title, icon, LEFT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (!tabSelected && getIcon() != null) {
      d.width = getIcon().getIconWidth();
    }
    return d;
  }

  public void setTabSelected(boolean active) {
    this.tabSelected = active;
  }
}
