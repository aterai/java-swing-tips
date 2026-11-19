// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        UIManager.put("TabbedPane.highlight", Color.GRAY);
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        setUI(new IsoscelesTrapezoidTabbedPaneUI());
      }
    };
    tabs.addTab("JTextArea", new JScrollPane(new JTextArea()));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JButton", new JButton("button"));
    tabs.addTab("JSplitPane", new JSplitPane());

    // Switch the tab placement between TOP and BOTTOM
    JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT));
    box.add(new JLabel("TabPlacement:"));
    JRadioButton top = createRadioButton("TOP", SwingConstants.TOP, tabs);
    JRadioButton bottom = createRadioButton("BOTTOM", SwingConstants.BOTTOM, tabs);
    ButtonGroup group = new ButtonGroup();
    group.add(top);
    group.add(bottom);
    box.add(top);
    box.add(bottom);

    add(box, BorderLayout.NORTH);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JRadioButton createRadioButton(String text, int placement, JTabbedPane tabs) {
    JRadioButton radio = new JRadioButton(text, tabs.getTabPlacement() == placement);
    radio.addActionListener(e -> tabs.setTabPlacement(placement));
    return radio;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (UnsupportedLookAndFeelException ignored) {
    //   Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //   ex.printStackTrace();
    //   return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class IsoscelesTrapezoidTabbedPaneUI extends BasicTabbedPaneUI {
  // horizontal width of the slanted sides of the trapezoid
  private static final int SLANT_WIDTH = 3;
  private static final Color TAB_BACKGROUND = Color.LIGHT_GRAY;
  private static final Color TAB_BORDER = Color.GRAY;
  private final Color selectedTabColor = UIManager.getColor("TabbedPane.selected");

  @Override protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
    int tabCount = tabPane.getTabCount();

    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();
    Rectangle clipRect = g.getClipBounds();

    // copied from BasicTabbedPaneUI#paintTabArea(...)
    for (int i = runCount - 1; i >= 0; i--) {
      int start = tabRuns[i];
      int next = tabRuns[(i + 1) % runCount];
      int end = next == 0 ? tabCount - 1 : next - 1;
      // Paint the tabs from right to left so that the left tab overlaps the right one
      // https://stackoverflow.com/questions/41566659/tabs-rendering-order-in-custom-jtabbedpane
      for (int j = end; j >= start; j--) {
        if (j != selectedIndex && intersectsTrapezoid(rects[j], clipRect)) {
          paintTab(g, tabPlacement, rects, j, iconRect, textRect);
        }
      }
    }
    if (selectedIndex >= 0 && intersectsTrapezoid(rects[selectedIndex], clipRect)) {
      paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
    }
  }

  // The trapezoid sticks out of the tab bounds by SLANT_WIDTH on both sides,
  // so a tab must also be repainted when only its protruding part is in the clip
  // (e.g. when only the adjacent tab is repainted by a focus change).
  private static boolean intersectsTrapezoid(Rectangle tabRect, Rectangle clipRect) {
    Rectangle r = new Rectangle(tabRect);
    r.grow(SLANT_WIDTH, 0);
    return r.intersects(clipRect);
  }

  @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    // Do nothing
  }

  @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    // Do nothing
  }

  @Override protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
    super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    if (tabPlacement == TOP) {
      Graphics g2 = g.create();
      paintSelectedTabEdge(g2, selectedIndex, x, w, y);
      g2.dispose();
    }
  }

  @Override protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
    if (tabPlacement == BOTTOM) {
      // Mirror of the TOP edge: a single 1px line in the same color as the top edge
      // instead of the BasicTabbedPaneUI 3D shadow lines
      Graphics g2 = g.create();
      int lineY = y + h - 1;
      g2.setColor(lightHighlight);
      g2.drawLine(x, lineY, x + w - 2, lineY);
      paintSelectedTabEdge(g2, selectedIndex, x, w, lineY);
      g2.dispose();
    } else {
      super.paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    }
  }

  // Paint over the content border line covered by the long side of the selected trapezoidal tab
  private void paintSelectedTabEdge(Graphics g, int selectedIndex, int x, int w, int lineY) {
    if (selectedIndex >= 0) {
      Rectangle selRect = getTabBounds(selectedIndex, calcRect);
      // Same condition as BasicTabbedPaneUI: skip if the selected tab is
      // scrolled out of view (SCROLL_TAB_LAYOUT)
      if (selRect.x >= x && selRect.x <= x + w) {
        g.setColor(selectedTabColor);
        int x1 = selRect.x - SLANT_WIDTH + 1;
        int x2 = selRect.x + selRect.width + SLANT_WIDTH - 1;
        g.drawLine(x1, lineY, x2, lineY);
      }
    }
  }

  @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // The trapezoid sticks out of the tab bounds, so expand the clipping area
    Rectangle clipRect = g2.getClipBounds();
    clipRect.grow(SLANT_WIDTH + 1, 0);
    g2.setClip(clipRect);

    Path2D trapezoid = createTrapezoid(tabPlacement, w, h, isSelected);
    trapezoid.transform(AffineTransform.getTranslateInstance(x, y));

    g2.setColor(isSelected ? selectedTabColor : TAB_BACKGROUND);
    g2.fill(trapezoid);

    g2.setColor(TAB_BORDER);
    g2.draw(trapezoid);

    g2.dispose();
  }

  // TOP: the long side is at the bottom, adjoining the content area.
  // BOTTOM: vertically flipped, the long side is at the top.
  // Unselected tabs are 1px shorter than the selected tab on the side away from the content area.
  // A normalized 1px stroke at an integer y lights the pixel row y (just below the coordinate),
  // so the mirror of y = h is y = -1: the BOTTOM trapezoid is shifted up by 1px so that
  // its long side falls outside the tab area (clipped) just like the TOP one, instead of
  // drawing an extra line right below the content border edge.
  private static Path2D createTrapezoid(int tabPlacement, int w, int h, boolean isSelected) {
    float inset = isSelected ? 0f : 1f;
    boolean bottom = tabPlacement == BOTTOM;
    float longSideY = bottom ? -1f : h;
    float shortSideY = bottom ? h - 1f - inset : inset;
    Path2D trapezoid = new Path2D.Float();
    trapezoid.moveTo(-SLANT_WIDTH, longSideY);
    trapezoid.lineTo(SLANT_WIDTH, shortSideY);
    trapezoid.lineTo(w - SLANT_WIDTH, shortSideY);
    trapezoid.lineTo(w + SLANT_WIDTH, longSideY);
    trapezoid.closePath();
    return trapezoid;
  }
}
