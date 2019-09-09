// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(makePanel(new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10))));
    add(makePanel(new ScrollableWrapPanel(new ScrollableWrapLayout(FlowLayout.LEFT, 10, 10))));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel(JPanel box) {
    List<ListItem> model = Arrays.asList(
        new ListItem("red", new ColorIcon(Color.RED)),
        new ListItem("green", new ColorIcon(Color.GREEN)),
        new ListItem("blue", new ColorIcon(Color.BLUE)),
        new ListItem("cyan", new ColorIcon(Color.CYAN)),
        new ListItem("darkGray", new ColorIcon(Color.DARK_GRAY)),
        new ListItem("gray", new ColorIcon(Color.GRAY)),
        new ListItem("lightGray", new ColorIcon(Color.LIGHT_GRAY)),
        new ListItem("magenta", new ColorIcon(Color.MAGENTA)),
        new ListItem("orange", new ColorIcon(Color.ORANGE)),
        new ListItem("pink", new ColorIcon(Color.PINK)),
        new ListItem("yellow", new ColorIcon(Color.YELLOW)),
        new ListItem("black", new ColorIcon(Color.BLACK)),
        new ListItem("white", new ColorIcon(Color.WHITE)));

    model.forEach(item -> {
      JButton button = new JButton(item.icon);
      JLabel label = new JLabel(item.title, SwingConstants.CENTER);
      JPanel p = new JPanel(new BorderLayout());
      p.add(button);
      p.add(label, BorderLayout.SOUTH);
      box.add(p);
    });
    return new JScrollPane(box);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ScrollableWrapPanel extends JPanel implements Scrollable {
  protected ScrollableWrapPanel(LayoutManager lmg) {
    super(lmg);
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    Container c = SwingUtilities.getUnwrappedParent(this);
    if (c instanceof JViewport) {
      return c.getSize();
    }
    return super.getPreferredSize();
  }

  @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 32;
  }

  @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return 32;
  }

  @Override public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return false;
  }
}

class ScrollableWrapLayout extends FlowLayout {
  private final int fixedHorizontalGap;

  protected ScrollableWrapLayout(int align, int hgap, int vgap) {
    super(align, hgap, vgap);
    fixedHorizontalGap = hgap;
  }

  private int getPreferredHorizontalGap(Container target) {
    Insets insets = target.getInsets();
    int columns = 0;
    int width = target.getWidth();
    if (target.getParent() instanceof JViewport) {
      width = target.getParent().getBounds().width;
    }
    width -= insets.left + insets.right + fixedHorizontalGap * 2;
    for (int i = 0; i < target.getComponentCount(); i++) {
      Component m = target.getComponent(i);
      if (m.isVisible()) {
        Dimension d = m.getPreferredSize();
        if (width - d.width - fixedHorizontalGap < 0) {
          columns = i;
          break;
        }
        width -= d.width + fixedHorizontalGap;
      }
    }
    return fixedHorizontalGap + width / columns;
  }

  @Override public void layoutContainer(Container target) {
    setHgap(getPreferredHorizontalGap(target));
    super.layoutContainer(target);
  }

  @Override public Dimension preferredLayoutSize(Container target) {
    Dimension dim = super.preferredLayoutSize(target);
    synchronized (target.getTreeLock()) {
      if (target.getParent() instanceof JViewport) {
        dim.width = target.getParent().getBounds().width;
        for (int i = 0; i < target.getComponentCount(); i++) {
          Component m = target.getComponent(i);
          if (m.isVisible()) {
            Dimension d = m.getPreferredSize();
            dim.height = Math.max(dim.height, d.height + m.getY());
          }
        }
      }
      return dim;
    }
  }
}

class ListItem {
  public final Icon icon;
  public final String title;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
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
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
