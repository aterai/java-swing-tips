// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    add(new JScrollPane(createSampleTable()));
    add(new OverlappedScrollPane(createSampleTable()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable createSampleTable() {
    JTable table = new JTable(new DefaultTableModel(30, 5));
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    return table;
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

class OverlappedScrollPane extends JScrollPane {
  protected OverlappedScrollPane(Component view) {
    super(view);
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false; // JScrollBar is overlap
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      getVerticalScrollBar().setUI(new OverlappedScrollBarUI());
      getHorizontalScrollBar().setUI(new OverlappedScrollBarUI());
      setComponentZOrder(getVerticalScrollBar(), 0);
      setComponentZOrder(getHorizontalScrollBar(), 1);
      setComponentZOrder(getViewport(), 2);
      getVerticalScrollBar().setOpaque(false);
      getHorizontalScrollBar().setOpaque(false);
    });
    setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
    setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
    setLayout(new OverlappedScrollPaneLayout());
  }
}

class OverlappedScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 12;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);
      if (Objects.nonNull(colHead)) {
        int h = colHead.isVisible()
            ? Math.min(availR.height, colHead.getPreferredSize().height)
            : 0;
        colHead.setBounds(availR.x, availR.y, availR.width, h);
        availR.y += h;
        availR.height -= h;
      }
      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        int x = availR.x + availR.width - BAR_SIZE;
        vsb.setBounds(x, availR.y, BAR_SIZE, availR.height - BAR_SIZE);
      }
      if (Objects.nonNull(hsb)) {
        int y = availR.y + availR.height - BAR_SIZE;
        hsb.setBounds(availR.x, y, availR.width - BAR_SIZE, BAR_SIZE);
      }
    }
  }
}

class InvisibleButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(0x64_64_B4_FF, true);
  private static final Color DRAGGING_COLOR = new Color(0x64_64_B4_C8, true);
  private static final Color ROLLOVER_COLOR = new Color(0x64_64_B4_DC, true);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new InvisibleButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new InvisibleButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // g.fillRect(r.x, r.y, r.width - 1, r.height - 1);
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    if (!r.isEmpty() && c.isEnabled()) {
      Rectangle thumb = new Rectangle(r.x, r.y, r.width - 1, r.height - 1);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getCurrentThumbColor());
      g2.fill(thumb);
      g2.setPaint(Color.WHITE);
      g2.draw(thumb);
      g2.dispose();
    }
  }

  private Color getCurrentThumbColor() {
    Color color;
    if (isDragging) {
      color = DRAGGING_COLOR;
    } else if (isThumbRollover()) {
      color = ROLLOVER_COLOR;
    } else {
      color = DEFAULT_COLOR;
    }
    return color;
  }
}
