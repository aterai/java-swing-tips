// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    // http://www.famfamfam.com/lab/icons/mini/
    JButton button = new JButton(new ImageIcon(getClass().getResource("page_new.gif"))) {
      private transient MouseListener handler;
      @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setFocusable(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        // setAlignmentX(Component.LEFT_ALIGNMENT);
        // setAlignmentY(Component.TOP_ALIGNMENT); // ???
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
        return Component.TOP_ALIGNMENT;
      }
    };

    ClippedTitleTabbedPane tabs = new ClippedTitleTabbedPane() {
      @Override public void updateUI() {
        UIManager.put("TabbedPane.tabAreaInsets", null); // uninstall
        super.updateUI();
        // setAlignmentX(Component.LEFT_ALIGNMENT);
        // setAlignmentY(Component.TOP_ALIGNMENT);
        // System.out.println(button.getAlignmentY());
        // button.setAlignmentY(Component.TOP_ALIGNMENT);
        // System.out.println(button.getAlignmentY());
        UIManager.put("TabbedPane.tabAreaInsets", getButtonPaddingTabAreaInsets());
        super.updateUI(); // reinstall
      }

      @Override public float getAlignmentX() {
        return Component.LEFT_ALIGNMENT;
      }

      @Override public float getAlignmentY() {
        return Component.TOP_ALIGNMENT;
      }

      private Insets getButtonPaddingTabAreaInsets() {
        Insets ti = getTabInsets();
        Insets ai = getTabAreaInsets();
        Dimension d = button.getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        int tih = d.height - fm.getHeight() - ti.top - ti.bottom - ai.bottom;
        return new Insets(Math.max(ai.top, tih), d.width + ai.left, ai.bottom, ai.right);
      }
    };
    tabs.addTab("title1", new JLabel("12345"));
    tabs.addTab("title2", new JScrollPane(new JTree()));
    tabs.addTab("title3", new JLabel("67890"));

    button.addActionListener(e -> tabs.addTab("title", new JLabel("JLabel")));

    JPanel p = new JPanel();
    p.setLayout(new OverlayLayout(p));
    p.add(button);
    p.add(tabs);

    JMenuBar menubar = new JMenuBar();
    JMenu m1 = new JMenu("Tab");
    m1.add("removeAll").addActionListener(e -> tabs.removeAll());
    menubar.add(m1);

    add(menubar, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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

class ClippedTitleTabbedPane extends JTabbedPane {
  private static final int MAX_TAB_WIDTH = 80;

  protected ClippedTitleTabbedPane() {
    super();
  }

  protected ClippedTitleTabbedPane(int tabPlacement) {
    super(tabPlacement);
  }

  protected Insets getTabInsets() {
    Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
    if (Objects.nonNull(insets)) {
      return insets;
    } else {
      SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
      SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
      return style.getInsets(context, null);
    }
  }

  protected Insets getTabAreaInsets() {
    Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
    if (Objects.nonNull(insets)) {
      return insets;
    } else {
      SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
      SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
      return style.getInsets(context, null);
    }
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
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
    int tabWidth = 0; // = tabInsets.left + tabInsets.right + 3;
    int gap = 0;

    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = areaWidth / 4;
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }
    if (tabWidth > MAX_TAB_WIDTH) {
      tabWidth = MAX_TAB_WIDTH;
      gap = 0;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, SwingConstants.CENTER));
  }

  protected void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      JComponent tab = (JComponent) getTabComponentAt(i);
      if (Objects.nonNull(tab)) {
        int a = (i == getTabCount() - 1) ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}
