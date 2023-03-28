// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    add(new JScrollPane(makeList()));
    add(makeTranslucentScrollBar(makeList()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeList() {
    JTable table = new JTable(new DefaultTableModel(30, 5));
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    return table;
  }

  private static JScrollPane makeTranslucentScrollBar(JTable c) {
    return new JScrollPane(c) {
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
        setLayout(new OverlapScrollPaneLayout());
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
      ex.printStackTrace();
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

class OverlapScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 12;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);

      Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
      if (Objects.nonNull(colHead) && colHead.isVisible()) {
        int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
        colHeadR.height = colHeadHeight;
        availR.y += colHeadHeight;
        availR.height -= colHeadHeight;
      }
      colHeadR.width = availR.width;
      colHeadR.x = availR.x;
      if (Objects.nonNull(colHead)) {
        colHead.setBounds(colHeadR);
      }
      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        vsb.setLocation(availR.x + availR.width - BAR_SIZE, availR.y);
        vsb.setSize(BAR_SIZE, availR.height - BAR_SIZE);
        // vsb.setVisible(true);
      }
      if (Objects.nonNull(hsb)) {
        hsb.setLocation(availR.x, availR.y + availR.height - BAR_SIZE);
        hsb.setSize(availR.width - BAR_SIZE, BAR_SIZE);
        // hsb.setVisible(true);
      }
    }
  }
}

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(100, 180, 255, 100);
  private static final Color DRAGGING_COLOR = new Color(100, 180, 200, 100);
  private static final Color ROLLOVER_COLOR = new Color(100, 180, 220, 100);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // g.fillRect(r.x, r.y, r.width - 1, r.height - 1);
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    Color color;
    if (r.isEmpty() || !sb.isEnabled()) {
      return;
    } else if (isDragging) {
      color = DRAGGING_COLOR;
    } else if (isThumbRollover()) {
      color = ROLLOVER_COLOR;
    } else {
      color = DEFAULT_COLOR;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(color);
    g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
    g2.setPaint(Color.WHITE);
    g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
    g2.dispose();
  }
}
