// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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
  private final JMenuItem closeTab;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;
  private int counter;

  /* default */ TabbedPanePopupMenu() {
    super();
    add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      addTab(tabs);
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    add("New tab Opens in Background").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      addTab(tabs);
    });
    addSeparator();
    closeTab = add("Close");
    closeTab.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.remove(tabs.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.removeAll();
    });
    closeAllButActive = add("Close all but active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      int selected = tabs.getSelectedIndex();
      // Keep the active tab in place: re-adding it would start the highlight animation.
      for (int i = tabs.getTabCount() - 1; i >= 0; i--) {
        if (i != selected) {
          tabs.removeTabAt(i);
        }
      }
    });
  }

  private void addTab(JTabbedPane tabs) {
    tabs.addTab("Title: " + counter, new JLabel("Tab: " + counter));
    counter++;
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabs = (JTabbedPane) c;
      closeTab.setEnabled(tabs.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabs.getTabCount() > 0);
      closeAllButActive.setEnabled(tabs.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}

class TabHighlightLayerUI extends LayerUI<JTabbedPane> {
  private static final int MAX_ALPHA = 32;
  private final Rectangle highlightRect = new Rectangle();
  private final Timer animator = new Timer(10, null);
  private transient ActionListener listener;
  private int alpha;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      layer.setLayerEventMask(AWTEvent.HIERARCHY_EVENT_MASK);
      listener = e -> updateAlpha(layer);
      animator.addActionListener(listener);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
      animator.stop();
      animator.removeActionListener(listener);
      alpha = 0;
    }
    super.uninstallUI(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (alpha > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 100f));
      g2.setPaint(Color.RED);
      g2.fill(highlightRect);
      g2.dispose();
    }
  }

  @Override protected void processHierarchyEvent(
        HierarchyEvent e, JLayer<? extends JTabbedPane> l) {
    super.processHierarchyEvent(e, l);
    // The same event is also dispatched to all descendants of the added component,
    // such as the JViewport of a JScrollPane, so ignore everything but the component itself.
    Component c = e.getComponent();
    boolean isAdded = Objects.equals(c, e.getChanged())
        && Objects.equals(e.getChangedParent(), l.getView());
    if (isAdded && e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED) {
      EventQueue.invokeLater(() -> startAnimation(l, c));
    }
  }

  private void startAnimation(JLayer<? extends JTabbedPane> l, Component c) {
    JTabbedPane tabs = l.getView();
    // The JTabbedPane also adds its own children, such as the scroll buttons,
    // and the tab may already have been removed.
    int idx = tabs.indexOfComponent(c);
    if (idx >= 0) {
      getHighlightBounds(tabs, idx).ifPresent(r -> {
        l.repaint(highlightRect); // Erase the highlight of the animation in progress
        highlightRect.setBounds(r);
        alpha = 0;
        animator.restart();
      });
    }
  }

  private void updateAlpha(JLayer<?> l) {
    if (alpha < MAX_ALPHA) {
      alpha += 1;
    } else {
      alpha = 0;
      animator.stop();
    }
    l.paintImmediately(highlightRect);
  }

  private static Optional<Rectangle> getHighlightBounds(JTabbedPane tabs, int idx) {
    Rectangle tabRect = tabs.getBoundsAt(idx);
    // JTabbedPane#getBoundsAt(...) returns the bounds in the JTabbedPane coordinate system
    return new Rectangle(tabs.getSize()).contains(tabRect)
        ? Optional.of(tabRect)
        : getScrollForwardButton(tabs).map(Component::getBounds);
  }

  // The scrollTabsForward button is placed to the right of the scrollTabsBackward button.
  private static Optional<JButton> getScrollForwardButton(JTabbedPane tabs) {
    return Arrays.stream(tabs.getComponents())
        .filter(JButton.class::isInstance)
        .map(JButton.class::cast)
        .max(Comparator.comparingInt(Component::getX));
  }
}
