// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
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
    String path = "example/page_new.gif"; // http://www.famfamfam.com/lab/icons/mini/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      Icon icn;
      try (InputStream s = url.openStream()) {
        icn = new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        icn = UIManager.getIcon("html.missingImage");
      }
      return icn;
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));

    JButton button = new TopAlignmentButton(icon);
    JTabbedPane tabs = makeTabbedPane(button);
    tabs.addTab("title1", new JLabel("12345"));
    tabs.addTab("title2", new JScrollPane(new JTree()));
    tabs.addTab("title3", new JLabel("67890"));
    button.addActionListener(e -> tabs.addTab("title", new JLabel("JLabel")));

    JPanel p = new JPanel();
    p.setLayout(new OverlayLayout(p));
    p.add(button);
    p.add(tabs);
    add(p);
    JMenuBar menuBar = new JMenuBar();
    JMenu m1 = new JMenu("Tab");
    m1.add("removeAll").addActionListener(e -> tabs.removeAll());
    menuBar.add(m1);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    setPreferredSize(new Dimension(320, 240));
  }

  private JTabbedPane makeTabbedPane(JButton button) {
    String key = "TabbedPane.tabAreaInsets";
    return new ClippedTitleTabbedPane() {
      @Override public void updateUI() {
        UIManager.put(key, null); // uninstall
        super.updateUI();
        UIManager.put(key, getButtonPaddingTabAreaInsets(button.getPreferredSize()));
        super.updateUI(); // reinstall
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);
      }

      private Insets getButtonPaddingTabAreaInsets(Dimension d) {
        Insets ti = getTabInsets();
        Insets ai = getTabAreaInsets();
        FontMetrics fm = getFontMetrics(getFont());
        int tih = d.height - fm.getHeight() - ti.top - ti.bottom - ai.bottom;
        return new Insets(Math.max(ai.top, tih), d.width + ai.left, ai.bottom, ai.right);
      }
    };
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
    frame.setMinimumSize(new Dimension(256, 200));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TopAlignmentButton extends JButton {
  private transient MouseListener handler;

  protected TopAlignmentButton(Icon icon) {
    super(icon);
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    setFocusable(false);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    // setAlignmentX(Component.LEFT_ALIGNMENT);
    // setAlignmentY(Component.TOP_ALIGNMENT); // not work???
    handler = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        setContentAreaFilled(true);
      }

      @Override public void mouseExited(MouseEvent e) {
        setContentAreaFilled(false);
      }
    };
    addMouseListener(handler);
  }

  @Override public float getAlignmentY() {
    return TOP_ALIGNMENT;
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  private static final int MAX_TAB_WIDTH = 80;

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

  protected Insets getTabInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB));
  }

  protected Insets getTabAreaInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabAreaInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB_AREA));
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount > 0 && isVisible()) {
      Insets tabIns = getTabInsets();
      Insets tabAreaIns = getTabAreaInsets();
      Insets i = getInsets();
      int tabPlacement = getTabPlacement();
      int areaWidth = getWidth() - tabAreaIns.left - tabAreaIns.right - i.left - i.right;
      boolean isTopBottom = tabPlacement == TOP || tabPlacement == BOTTOM;
      int tabWidth = isTopBottom ? areaWidth / tabCount : areaWidth / 4;
      int gap = isTopBottom ? areaWidth - tabWidth * tabCount : 0;
      // This 3 is the magic number defined in BasicTabbedPaneUI#calculateTabWidth(...)
      tabWidth -= tabIns.left + tabIns.right + 3;
      updateAllTabWidth(tabWidth, gap);
    }
    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, icon, CENTER));
  }

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
