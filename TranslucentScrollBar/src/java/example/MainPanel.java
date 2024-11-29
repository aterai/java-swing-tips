// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(12, 20));
    add(new JScrollPane(makeList()));
    add(makeTranslucentScrollBar(makeList()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeList() {
    DefaultListModel<String> m = new DefaultListModel<>();
    IntStream.range(0, 500)
        .mapToObj(i -> String.format("%03d: %s", i, LocalDateTime.now(ZoneId.systemDefault())))
        .forEach(m::addElement);
    return new JList<>(m);
  }

  private static Component makeTranslucentScrollBar(Component c) {
    return new JScrollPane(c) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false; // JScrollBar is overlap
      }

      @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
          getVerticalScrollBar().setUI(new TranslucentScrollBarUI());
          setComponentZOrder(getVerticalScrollBar(), 0);
          setComponentZOrder(getViewport(), 1);
          getVerticalScrollBar().setOpaque(false);
        });
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setLayout(new TranslucentScrollPaneLayout());
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

class TranslucentScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 12;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);
      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        vsb.setLocation(availR.x + availR.width - BAR_SIZE, availR.y);
        vsb.setSize(BAR_SIZE, availR.height);
        // vsb.setVisible(true);
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

class TranslucentScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(220, 100, 100, 100);
  private static final Color DRAGGING_COLOR = new Color(200, 100, 100, 100);
  private static final Color ROLLOVER_COLOR = new Color(255, 120, 100, 100);

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
    Dimension min = UIManager.getDimension("ScrollBar.minimumThumbSize");
    r.height = Math.max(r.height, min.height);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getThumbColor(c));
    g2.fill(r);
    g2.setPaint(Color.LIGHT_GRAY);
    g2.draw(r);
    g2.dispose();
  }

  private Color getThumbColor(JComponent c) {
    Color color;
    if (c.isEnabled()) {
      if (isDragging) {
        color = DRAGGING_COLOR;
      } else if (isThumbRollover()) {
        color = ROLLOVER_COLOR;
      } else {
        color = DEFAULT_COLOR;
      }
    } else {
      color = Color.DARK_GRAY;
    }
    return color;
  }
}
