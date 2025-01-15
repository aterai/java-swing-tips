// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new ClippedTitleTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    addTab(tabs, "JTree", "example/wi0009-32.png", new JScrollPane(new JTree()));
    addTab(tabs, "JTextArea", "example/wi0054-32.png", new JScrollPane(new JTextArea()));
    addTab(tabs, "Preference", "example/wi0062-32.png", new JScrollPane(new JTree()));
    addTab(tabs, "Help", "example/wi0063-32.png", new JScrollPane(new JTextArea()));

    // tabs.addTab(makeTitle("Title", "wi0009-32.png"), new JLabel("a"));
    // tabs.addTab(makeTitle("Help", "wi0054-32.png"), new JLabel("b"));

    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addTab(JTabbedPane tabbedPane, String title, String iconPath, Component c) {
    tabbedPane.addTab(title, c);
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage bi = Optional.ofNullable(cl.getResource(iconPath)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    JLabel label = new JLabel(title, new ImageIcon(bi), SwingConstants.CENTER);
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    // label.setVerticalAlignment(SwingConstants.CENTER);
    // label.setHorizontalAlignment(SwingConstants.CENTER);
    tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
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

  // private String makeTitle(String t, String p) {
  //   return "<html><center><img src='" + getClass().getResource(p) + "'/><br/>" + t;
  // }

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

class ClippedTitleTabbedPane extends JTabbedPane {
  protected ClippedTitleTabbedPane() {
    super();
  }

  // protected ClippedTitleTabbedPane(int tabPlacement) {
  //   super(tabPlacement);
  // }

  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext context = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(context, null);
  }

  private Insets getTabInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB));
  }

  private Insets getTabAreaInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabAreaInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB_AREA));
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount == 0 || !isVisible()) {
      super.doLayout();
      return;
    }
    Insets tabInsets = getTabInsets();
    Insets tabAreaInsets = getTabAreaInsets();
    Insets ins = getInsets();
    int tabPlacement = getTabPlacement();
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - ins.left - ins.right;
    int tabWidth; // = tabInsets.left + tabInsets.right + 3;
    int gap;

    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = areaWidth / 4;
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  // @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
  //   super.insertTab(title, icon, component, Objects.toString(tip, title), index);
  //   setTabComponentAt(index, new JLabel(title, icon, SwingConstants.CENTER));
  // }

  private void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        int a = i == getTabCount() - 1 ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}
