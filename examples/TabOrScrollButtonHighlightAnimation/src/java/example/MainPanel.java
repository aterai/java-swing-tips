// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JLabel", new JLabel("Test"));
    tabs.addTab("JTable", new JScrollPane(new JTable(10, 3)));
    tabs.addTab("JTextArea", new JScrollPane(new JTextArea()));
    tabs.addTab("JSplitPane", new JSplitPane());
    add(new JLayer<>(tabs, new TabHighlightLayerUI()));
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

final class TabbedPanePopupMenu extends JPopupMenu {
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  /* default */ TabbedPanePopupMenu() {
    super();
    AtomicInteger counter = new AtomicInteger();
    add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      addTab(counter, tabs);
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    add("New tab Opens in Background").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      addTab(counter, tabs);
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.remove(tabs.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      int idx = tabs.getSelectedIndex();
      String title = tabs.getTitleAt(idx);
      Component cmp = tabs.getComponentAt(idx);
      tabs.removeAll();
      tabs.addTab(title, cmp);
    });
  }

  private static void addTab(AtomicInteger counter, JTabbedPane tabs) {
    int iv = counter.getAndIncrement();
    tabs.addTab("Title: " + iv, new JLabel("Tab: " + iv));
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabs = (JTabbedPane) c;
      closePage.setEnabled(tabs.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabs.getTabCount() > 0);
      closeAllButActive.setEnabled(tabs.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}

class TabHighlightLayerUI extends LayerUI<JTabbedPane> {
  private static final int MAX = 32;
  private final Rectangle rect = new Rectangle();
  private final Timer animator = new Timer(10, null);
  private transient ActionListener listener;
  private int alpha;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.HIERARCHY_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      float a = alpha / 100f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
      g2.setPaint(Color.RED);
      g2.fill(rect);
      g2.dispose();
    }
  }

  @Override protected void processHierarchyEvent(HierarchyEvent e, JLayer<? extends JTabbedPane> l) {
    super.processHierarchyEvent(e, l);
    Container parent = e.getChangedParent();
    JTabbedPane tabs = l.getView();
    long flags = e.getChangeFlags();
    if (Objects.equals(parent, tabs) && flags == HierarchyEvent.PARENT_CHANGED) {
      EventQueue.invokeLater(() -> startAnime(l, e.getComponent()));
    }
  }

  private void startAnime(JLayer<? extends JTabbedPane> l, Component c) {
    JTabbedPane tabs = l.getView();
    int idx = tabs.indexOfComponent(c);
    Rectangle tabRect = tabs.getBoundsAt(idx);
    if (tabs.getBounds().contains(tabRect)) {
      rect.setBounds(tabRect);
    } else {
      JButton b = getScrollForwardButton(tabs);
      if (b != null) {
        rect.setBounds(b.getBounds());
      }
    }
    animator.start();
    animator.removeActionListener(listener);
    listener = ae -> {
      if (alpha < MAX) {
        alpha += 1;
      } else {
        alpha = 0;
        animator.stop();
      }
      l.paintImmediately(rect);
    };
    animator.addActionListener(listener);
    animator.start();
  }

  private static JButton getScrollForwardButton(JTabbedPane tabs) {
    JButton button1 = null;
    JButton button2 = null;
    for (Component c : tabs.getComponents()) {
      if (c instanceof JButton) {
        if (Objects.isNull(button1)) {
          button1 = (JButton) c;
        } else if (Objects.isNull(button2)) {
          button2 = (JButton) c;
        }
      }
    }
    int x1 = Objects.nonNull(button1) ? button1.getX() : -1;
    int x2 = Objects.nonNull(button2) ? button2.getX() : -1;
    return x1 > x2 ? button1 : button2;
  }
}
