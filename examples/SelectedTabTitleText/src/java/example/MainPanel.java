// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
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
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    addTab(tabs, "JTree", "example/wi0009-32.png", new JScrollPane(new JTree()));
    addTab(tabs, "JTextArea", "example/wi0054-32.png", new JScrollPane(new JTextArea()));
    addTab(tabs, "JTable", "example/wi0062-32.png", new JScrollPane(new JTable(8, 3)));
    addTab(tabs, "JSplitPane", "example/wi0063-32.png", new JSplitPane());
    tabs.addChangeListener(e -> updateSelectedTabTitle((JTabbedPane) e.getSource()));
    updateSelectedTabTitle(tabs);
    tabs.setComponentPopupMenu(createTabPlacementPopup());
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateSelectedTabTitle(JTabbedPane tabs) {
    int idx = tabs.getSelectedIndex();
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Component c = tabs.getTabComponentAt(i);
      if (c instanceof JLabel) {
        ((JLabel) c).setText(i == idx ? tabs.getTitleAt(i) : null);
      }
    }
  }

  private static void addTab(JTabbedPane tabs, String title, String path, Component c) {
    tabs.addTab(title, c);
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    tabs.setTabComponentAt(tabs.getTabCount() - 1, new IconTabLabel(title, url));
  }

  private static JPopupMenu createTabPlacementPopup() {
    JPopupMenu popup = new JPopupMenu();
    ButtonGroup group = new ButtonGroup();
    popup.add(createTabPlacementItem(popup, group, "TOP", SwingConstants.TOP));
    popup.add(createTabPlacementItem(popup, group, "LEFT", SwingConstants.LEFT));
    return popup;
  }

  private static JMenuItem createTabPlacementItem(
      JPopupMenu popup, ButtonGroup group, String name, int placement) {
    JMenuItem item = new JRadioButtonMenuItem(name, placement == SwingConstants.TOP);
    item.addActionListener(e -> {
      Component c = popup.getInvoker();
      if (c instanceof JTabbedPane) {
        ((JTabbedPane) c).setTabPlacement(placement);
      }
    });
    group.add(item);
    return item;
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

class IconTabLabel extends JLabel {
  private final String title;

  protected IconTabLabel(String title, URL url) {
    super(null, createIcon(url), CENTER);
    this.title = title;
  }

  private static Icon createIcon(URL url) {
    return Optional.ofNullable(url)
        .<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }

  @Override public void updateUI() {
    super.updateUI();
    setVerticalTextPosition(BOTTOM);
    setHorizontalTextPosition(CENTER);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    Container c = SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
    if (c instanceof JTabbedPane) {
      // Fix the size along the cross axis so that the tab does not
      // grow or shrink when the title is shown or hidden
      Icon icon = getIcon();
      FontMetrics fm = getFontMetrics(getFont());
      int height = icon.getIconHeight() + getIconTextGap() + fm.getHeight();
      int tabPlacement = ((JTabbedPane) c).getTabPlacement();
      if (tabPlacement == TOP || tabPlacement == BOTTOM) {
        d.height = height;
      } else {
        d.width = Math.max(icon.getIconWidth(), fm.stringWidth(title));
        // Never shrink below the icon + title height: the tabs scroll instead of clipping
        d.height = Math.max(d.height, height);
      }
    }
    return d;
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext ctx = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(ctx, null);
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
    if (tabCount > 0 && isVisible()) {
      Insets tabInsets = getTabInsets();
      Insets tabAreaInsets = getTabAreaInsets();
      Insets insets = getInsets();
      int tabPlacement = getTabPlacement();
      boolean horizontal = tabPlacement == TOP || tabPlacement == BOTTOM;
      int areaSize;
      int padding;
      if (horizontal) {
        areaSize = getWidth() - tabAreaInsets.left - tabAreaInsets.right
            - insets.left - insets.right;
        // This 3 is the magic number defined in BasicTabbedPaneUI#calculateTabWidth(...)
        padding = tabInsets.left + tabInsets.right + 3;
      } else {
        // BasicTabbedPaneUI#getTabAreaInsets(...) rotates tabAreaInsets for LEFT/RIGHT,
        // so its left/right are applied to the top/bottom of the tab area
        areaSize = getHeight() - tabAreaInsets.left - tabAreaInsets.right
            - insets.top - insets.bottom;
        // This 2 is the magic number defined in BasicTabbedPaneUI#calculateTabHeight(...)
        padding = tabInsets.top + tabInsets.bottom + 2;
      }
      int tabSize = areaSize / tabCount;
      int gap = areaSize - tabSize * tabCount;
      updateAllTabSizes(tabSize - padding, gap, horizontal);
    }
    super.doLayout();
  }

  private void updateAllTabSizes(int tabSize, int gap, boolean horizontal) {
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        // Distribute the remainder (gap < tabCount) one pixel each to the leading tabs
        int size = i < gap ? tabSize + 1 : tabSize;
        // getPreferredSize() returns a copy, so it is safe to modify and set it back
        Dimension d = tab.getPreferredSize();
        if (horizontal) {
          d.width = size;
        } else {
          d.height = size;
        }
        tab.setPreferredSize(d);
      }
    }
  }
}
