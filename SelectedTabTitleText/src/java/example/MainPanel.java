// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
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
    // tabs.setTabPlacement(SwingConstants.LEFT);
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    List<TabTitle> titles = Arrays.asList(
        new TabTitle("JTree", cl.getResource("example/wi0009-32.png")),
        new TabTitle("JTextArea", cl.getResource("example/wi0054-32.png")),
        new TabTitle("JTable", cl.getResource("example/wi0062-32.png")),
        new TabTitle("JSplitPane", cl.getResource("example/wi0063-32.png"))
    );

    addTab(tabs, titles.get(0), new JScrollPane(new JTree()));
    addTab(tabs, titles.get(1), new JScrollPane(new JTextArea()));
    addTab(tabs, titles.get(2), new JScrollPane(new JTable(8, 3)));
    addTab(tabs, titles.get(3), new JSplitPane());
    tabs.setSelectedIndex(-1);
    EventQueue.invokeLater(() -> tabs.setSelectedIndex(0));

    tabs.addChangeListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
      if (tabbedPane.getTabCount() <= 0) {
        return;
      }
      int idx = tabbedPane.getSelectedIndex();
      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        Component c = tabbedPane.getTabComponentAt(i);
        if (c instanceof JLabel) {
          ((JLabel) c).setText(i == idx ? tabbedPane.getTitleAt(i) : null);
        }
      }
    });

    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addTab(JTabbedPane tabbedPane, TabTitle tt, Component c) {
    tabbedPane.addTab(tt.getTitle(), c);
    Icon icon = Optional.ofNullable(tt.getUrl())
        .<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
    JLabel label = new JLabel(null, icon, SwingConstants.CENTER) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Container c = SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (c instanceof JTabbedPane) {
          int tp = ((JTabbedPane) c).getTabPlacement();
          if (tp == TOP || tp == BOTTOM) {
            d.height = icon.getIconHeight() + tabbedPane.getFont().getSize() + getIconTextGap();
          }
        }
        return d;
      }
    };
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("TabbedPane.tabInsets", new Insets(8, 2, 2, 2));
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

class TabTitle {
  private final String title;
  private final URL url;

  protected TabTitle(String title, URL url) {
    this.title = title;
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public URL getUrl() {
    return url;
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  protected ClippedTitleTabbedPane() {
    super();
  }

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
    Insets insets = getInsets();
    int tabPlacement = getTabPlacement();
    int tabSize;
    int gap;
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      int h = getHeight() - tabAreaInsets.top - tabAreaInsets.bottom - insets.top - insets.bottom;
      tabSize = h / tabCount;
      gap = h - tabSize * tabCount;
      tabSize -= tabInsets.top + tabInsets.bottom + 3;
    } else { // TOP || BOTTOM
      int w = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
      tabSize = w / tabCount;
      gap = w - tabSize * tabCount;
      tabSize -= tabInsets.left + tabInsets.right + 3;
    }
    updateAllTabWidth(tabSize, gap);
    super.doLayout();
  }

  private void updateAllTabWidth(int tabSize, int gap) {
    boolean isSide = getTabPlacement() == LEFT || getTabPlacement() == RIGHT;
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        int a = i == getTabCount() - 1 ? rest : 1;
        int w = rest > 0 ? tabSize + a : tabSize;
        if (isSide) {
          dim.setSize(tab.getPreferredSize().width, w);
        } else {
          dim.setSize(w, tab.getPreferredSize().height);
        }
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}

// public final class MainPanel extends JPanel {
//   private MainPanel() {
//     super(new BorderLayout());
//     JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
//     ClassLoader cl = Thread.currentThread().getContextClassLoader();
//     // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
//     List<TabTitle> titles = Arrays.asList(
//         new TabTitle("JTree", cl.getResource("example/wi0009-32.png")),
//         new TabTitle("JTextArea", cl.getResource("example/wi0054-32.png")),
//         new TabTitle("JTable", cl.getResource("example/wi0062-32.png")),
//         new TabTitle("JSplitPane", cl.getResource("example/wi0063-32.png"))
//     );
//
//     tabs.addTab(makeTitle(titles.get(0), true), new JScrollPane(new JTree()));
//     tabs.addTab(makeTitle(titles.get(1), false), new JScrollPane(new JTextArea()));
//     tabs.addTab(makeTitle(titles.get(2), false), new JScrollPane(new JTable(8, 3)));
//     tabs.addTab(makeTitle(titles.get(3), false), new JSplitPane());
//     tabs.setSelectedIndex(0);
//
//     tabs.addChangeListener(e -> {
//       JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
//       if (tabbedPane.getTabCount() <= 0) {
//         return;
//       }
//       int idx = tabbedPane.getSelectedIndex();
//       for (int i = 0; i < tabbedPane.getTabCount(); i++) {
//         tabbedPane.setTitleAt(i, makeTitle(titles.get(i), i == idx));
//       }
//     });
//
//     add(tabs);
//     setPreferredSize(new Dimension(320, 240));
//   }
//
//   private static String makeTitle(TabTitle tt, boolean isSelected) {
//     String title = "";
//     if (isSelected) {
//       title = "<br/>" + tt.title;
//     }
//     return "<html><center><img src='" + tt.url + "'/>" + title;
//   }
//
//   public static void main(String[] args) {
//     EventQueue.invokeLater(MainPanel::createAndShowGui);
//   }
//
//   private static void createAndShowGui() {
//     try {
//       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//     } catch (UnsupportedLookAndFeelException ignored) {
//       Toolkit.getDefaultToolkit().beep();
//     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
//       ex.printStackTrace();
//       return;
//     }
//     JFrame frame = new JFrame("@title@");
//     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     frame.getContentPane().add(new MainPanel());
//     frame.pack();
//     frame.setLocationRelativeTo(null);
//     frame.setVisible(true);
//   }
// }
