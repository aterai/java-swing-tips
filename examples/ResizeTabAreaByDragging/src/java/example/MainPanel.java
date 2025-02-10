// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ClippedTitleTabbedPane tabs = new ClippedTitleTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("JTree", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    tabs.addTab("JTable", new ColorIcon(Color.GREEN), new JScrollPane(new JTable(5, 3)));
    tabs.addTab("JLabel", new ColorIcon(Color.BLUE), new JLabel("text"));
    tabs.addTab("JSplitPane", new ColorIcon(Color.ORANGE), new JSplitPane());
    add(new JLayer<>(tabs, new TabAreaResizeLayer()));
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
    frame.setMinimumSize(new Dimension(256, 200));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  private static final int MIN_WIDTH = 24;
  private int tabAreaWidth = 32;

  protected ClippedTitleTabbedPane() {
    super(LEFT);
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

  public int getTabAreaWidth() {
    return tabAreaWidth;
  }

  public void setTabAreaWidth(int width) {
    int w = Math.max(MIN_WIDTH, Math.min(width, getWidth() - MIN_WIDTH));
    if (tabAreaWidth != w) {
      tabAreaWidth = w;
      revalidate(); // doLayout();
    }
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount == 0 || !isVisible()) {
      super.doLayout();
      return;
    }
    Insets tabAreaInsets = getTabAreaInsets();
    Insets i = getInsets();
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - i.left - i.right;
    int tabWidth; // = tabInsets.left + tabInsets.right + 3;
    int gap;

    int tabPlacement = getTabPlacement();
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = getTabAreaWidth();
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    Insets tabInsets = getTabInsets();
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, icon, LEADING));
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

class TabAreaResizeLayer extends LayerUI<ClippedTitleTabbedPane> {
  private int offset;
  private boolean resizing;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends ClippedTitleTabbedPane> l) {
    ClippedTitleTabbedPane tabbedPane = l.getView();
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      Rectangle rect = getDividerBounds(tabbedPane);
      Point pt = e.getPoint();
      SwingUtilities.convertPoint(e.getComponent(), pt, tabbedPane);
      if (rect.contains(pt)) {
        offset = pt.x - tabbedPane.getTabAreaWidth();
        tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        resizing = true;
        e.consume();
      }
    } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
      tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      resizing = false;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends ClippedTitleTabbedPane> l) {
    ClippedTitleTabbedPane tabbedPane = l.getView();
    Point pt = e.getPoint();
    SwingUtilities.convertPoint(e.getComponent(), pt, tabbedPane);
    if (e.getID() == MouseEvent.MOUSE_MOVED) {
      Rectangle r = getDividerBounds(tabbedPane);
      tabbedPane.setCursor(Cursor.getPredefinedCursor(
          r.contains(pt) ? Cursor.W_RESIZE_CURSOR : Cursor.DEFAULT_CURSOR));
    } else if (e.getID() == MouseEvent.MOUSE_DRAGGED && resizing) {
      tabbedPane.setTabAreaWidth(pt.x - offset);
      e.consume();
    }
  }

  private static Rectangle getDividerBounds(ClippedTitleTabbedPane tabbedPane) {
    Dimension dividerSize = new Dimension(4, 4);
    Rectangle bounds = tabbedPane.getBounds();
    Rectangle compRect = Optional.ofNullable(tabbedPane.getSelectedComponent())
        .map(Component::getBounds).orElseGet(Rectangle::new);
    switch (tabbedPane.getTabPlacement()) {
      case SwingConstants.LEFT:
        bounds.x = compRect.x - dividerSize.width;
        bounds.width = dividerSize.width * 2;
        break;
      case SwingConstants.RIGHT:
        bounds.x += compRect.x + compRect.width - dividerSize.width;
        bounds.width = dividerSize.width * 2;
        break;
      case SwingConstants.BOTTOM:
        bounds.y += compRect.y + compRect.height - dividerSize.height;
        bounds.height = dividerSize.height * 2;
        break;
      default: // case SwingConstants.TOP:
        bounds.y = compRect.y - dividerSize.height;
        bounds.height = dividerSize.height * 2;
        break;
    }
    return bounds;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 3, getIconHeight() - 3);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
