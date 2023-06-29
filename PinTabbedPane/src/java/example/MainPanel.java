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
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
    //   tabbedPane.setUI(new WindowsTabbedPaneUI() {
    //     @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    //       int defaultWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
    //       int selectedIndex = tabPane.getSelectedIndex();
    //       boolean isSelected = selectedIndex == tabIndex;
    //       if (isSelected) {
    //         return defaultWidth + 100;
    //       } else {
    //         return defaultWidth;
    //       }
    //     }
    //     // @Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
    //     //   Rectangle tabRect = super.getTabBounds(pane, i);
    //     //   tabRect.translate(0, -16);
    //     //   tabRect.height = 16;
    //     //   return tabRect;
    //     // }
    //     // @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
    //     //   // Rectangle tabRect = rects[tabIndex];
    //     //   int selectedIndex = tabPane.getSelectedIndex();
    //     //   boolean isSelected = selectedIndex == tabIndex;
    //     //   if (isSelected) {
    //     //     // JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT
    //     //     rects[tabIndex].width += 16;
    //     //   }
    //     //   super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    //     // }
    //   });
    // }

    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    List<String> icons = Arrays.asList(
        "wi0009-16.png", "wi0054-16.png", "wi0062-16.png",
        "wi0063-16.png", "wi0124-16.png", "wi0126-16.png");
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    icons.forEach(s -> tabbedPane.addTab(s, new ImageIcon(makeImage(s)), new JLabel(s), s));
    tabbedPane.setComponentPopupMenu(new PinTabPopupMenu());
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource("example/" + path)).map(url -> {
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

final class PinTabPopupMenu extends JPopupMenu {
  private final JMenuItem pinTabMenuItem = new JCheckBoxMenuItem("pin tab");
  // private final Action newTabAction = new AbstractAction("new tab") {
  //   @Override public void actionPerformed(ActionEvent e) {
  //     JTabbedPane t = (JTabbedPane) getInvoker();
  //     int count = t.getTabCount();
  //     String title = "Tab " + count;
  //     t.addTab(title, new JLabel(title));
  //     t.setTabComponentAt(count, new ButtonTabComponent(t));
  //   }
  // };

  /* default */ PinTabPopupMenu() {
    super();
    add(pinTabMenuItem).addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
      int idx = t.getSelectedIndex();
      Component cmp = t.getComponentAt(idx);
      Component tab = t.getTabComponentAt(idx);
      Icon icon = t.getIconAt(idx);
      String tip = t.getToolTipTextAt(idx);
      boolean flg = t.isEnabledAt(idx);
      int i = searchNewSelectedIndex(t, idx, check.isSelected());
      t.remove(idx);
      t.insertTab(check.isSelected() ? "" : tip, icon, cmp, tip, i);
      t.setTabComponentAt(i, tab);
      t.setEnabledAt(i, flg);
      if (flg) {
        t.setSelectedIndex(i);
      }
    });
    addSeparator();
    add("close all").addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      for (int i = t.getTabCount() - 1; i >= 0; i--) {
        if (!isEmpty(t.getTitleAt(i))) {
          t.removeTabAt(i);
        }
      }
    });
  }

  public static int searchNewSelectedIndex(JTabbedPane t, int idx, boolean dir) {
    int i;
    if (dir) {
      for (i = 0; i < idx; i++) {
        if (!isSelectedPinTab(t, i)) {
          break;
        }
      }
    } else {
      for (i = t.getTabCount() - 1; i > idx; i--) {
        if (isSelectedPinTab(t, i)) {
          break;
        }
      }
    }
    return i;
  }

  private static boolean isSelectedPinTab(JTabbedPane t, int idx) {
    return idx >= 0 && idx == t.getSelectedIndex() && isEmpty(t.getTitleAt(idx));
  }

  private static boolean isEmpty(String s) {
    return Objects.isNull(s) || s.isEmpty();
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane t = (JTabbedPane) c;
      int idx = t.indexAtLocation(x, y);
      pinTabMenuItem.setEnabled(idx >= 0);
      pinTabMenuItem.setSelected(isSelectedPinTab(t, idx));
      super.show(c, x, y);
    }
  }
}
