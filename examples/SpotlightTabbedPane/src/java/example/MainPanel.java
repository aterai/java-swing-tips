// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new SpotlightTabbedPaneUI());
        setOpaque(true);
        setBackground(SpotlightTabbedPaneUI.BACKGROUND);
        setFont(getFont().deriveFont(Font.PLAIN, 15f));
      }
    };
    String[] titles = {"File", "Home", "Data", "Help"};
    for (String title : titles) {
      tabs.addTab(title, createPanel(title));
    }
    tabs.setEnabledAt(2, false);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createPanel(String title) {
    JLabel label = new JLabel(title, SwingConstants.CENTER);
    label.setForeground(SpotlightTabbedPaneUI.INACTIVE_TEXT);
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(SpotlightTabbedPaneUI.BACKGROUND);
    p.add(label);
    return p;
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

@SuppressWarnings("PMD.TooManyMethods")
class SpotlightTabbedPaneUI extends BasicTabbedPaneUI {
  public static final Color BACKGROUND = new Color(0x11_11_11);
  public static final Color INACTIVE_TEXT = new Color(0x8A_8A_8A);
  private static final Color SELECTED_FILL = new Color(0x1E_1E_1E);
  private static final Color SPOTLIGHT = new Color(0x2A_2A_2A);
  private static final Color ACTIVE_FOREGROUND = Color.WHITE;
  private static final Color DISABLED_TEXT = new Color(0x55_55_55);
  private static final int MARGIN = 2;
  private static final int ARC = 12;
  private static final double SPEED = .3;
  private static final double EPSILON = .5;
  private final Timer timer = new Timer(16, e -> stepSpotlight());
  private Rectangle2D.Double spotlight;
  private int hoverIndex = -1;
  private MouseAdapter hoverHandler;
  private ChangeListener selectionHandler;

  @Override protected void installListeners() {
    super.installListeners();
    hoverHandler = new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        int i = tabForCoordinate(tabPane, e.getX(), e.getY());
        setHoverIndex(i);
      }

      @Override public void mouseExited(MouseEvent e) {
        setHoverIndex(-1);
      }
    };
    tabPane.addMouseListener(hoverHandler);
    tabPane.addMouseMotionListener(hoverHandler);
    selectionHandler = e -> timer.start();
    tabPane.addChangeListener(selectionHandler);
  }

  @SuppressWarnings("PMD.NullAssignment")
  @Override protected void uninstallListeners() {
    timer.stop();
    tabPane.removeMouseListener(hoverHandler);
    tabPane.removeMouseMotionListener(hoverHandler);
    tabPane.removeChangeListener(selectionHandler);
    hoverHandler = null;
    selectionHandler = null;
    super.uninstallListeners();
  }

  private void setHoverIndex(int index) {
    if (hoverIndex != index) {
      hoverIndex = index;
      int type = index >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR;
      tabPane.setCursor(Cursor.getPredefinedCursor(type));
      timer.start();
      tabPane.repaint();
    }
  }

  private Rectangle2D.Double getTabBounds(int index) {
    Rectangle2D.Double r = null;
    if (index >= 0 && index < tabPane.getTabCount() && index < rects.length) {
      Rectangle tab = rects[index];
      r = new Rectangle2D.Double(
          tab.x + MARGIN, tab.y + MARGIN,
          tab.width - 2d * MARGIN, tab.height - 2d * MARGIN);
    }
    return r;
  }

  private Rectangle2D.Double getTargetBounds() {
    return getTabBounds(hoverIndex >= 0 ? hoverIndex : tabPane.getSelectedIndex());
  }

  private void stepSpotlight() {
    Rectangle2D.Double t = getTargetBounds();
    if (t == null || spotlight == null) {
      spotlight = t;
      timer.stop();
    } else {
      double dx = t.x - spotlight.x;
      double dy = t.y - spotlight.y;
      double dw = t.width - spotlight.width;
      double dh = t.height - spotlight.height;
      double maxX = Math.max(Math.abs(dx), Math.abs(dw));
      double maxY = Math.max(Math.abs(dy), Math.abs(dh));
      double max = Math.max(maxX, maxY);
      if (max < EPSILON) {
        spotlight.setRect(t);
        timer.stop();
      } else {
        double sx = spotlight.x + dx * SPEED;
        double sy = spotlight.y + dy * SPEED;
        double sw = spotlight.width + dw * SPEED;
        double sh = spotlight.height + dh * SPEED;
        spotlight.setRect(sx, sy, sw, sh);
      }
    }
    tabPane.repaint();
  }

  @Override protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
    if (!timer.isRunning()) {
      spotlight = getTargetBounds();
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    fillRoundRect(g2, getTabBounds(selectedIndex), SELECTED_FILL);
    fillRoundRect(g2, spotlight, SPOTLIGHT);
    g2.dispose();
    super.paintTabArea(g, tabPlacement, selectedIndex);
  }

  private static void fillRoundRect(Graphics2D g2, Rectangle2D r, Color color) {
    if (r != null) {
      g2.setColor(color);
      g2.fill(new RoundRectangle2D.Double(
          r.getX(), r.getY(), r.getWidth(), r.getHeight(), ARC, ARC));
    }
  }

  @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(font);
    if (tabPane.isEnabledAt(tabIndex)) {
      boolean active = isSelected || tabIndex == hoverIndex;
      g2.setColor(active ? ACTIVE_FOREGROUND : INACTIVE_TEXT);
    } else {
      g2.setColor(DISABLED_TEXT);
    }
    g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    g2.dispose();
  }

  @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // Do not paint anything
  }

  @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // Do not paint anything
  }

  @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    // Do not paint anything
  }

  @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
    // Do not paint anything
  }

  @Override protected Insets getTabInsets(int tabPlacement, int tabIndex) {
    return new Insets(6, 16, 6, 16);
  }

  @Override protected Insets getTabAreaInsets(int tabPlacement) {
    return new Insets(8, 8, 4, 8);
  }

  @Override protected Insets getSelectedTabPadInsets(int tabPlacement) {
    return new Insets(0, 0, 0, 0);
  }

  @Override protected Insets getContentBorderInsets(int tabPlacement) {
    return new Insets(0, 0, 0, 0);
  }

  @Override protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  @Override protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }
}
