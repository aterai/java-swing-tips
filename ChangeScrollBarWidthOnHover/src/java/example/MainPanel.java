// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private final JPanel scrollBar = new JPanel();
  private final Timer expand = new Timer(10, e -> scrollBar.revalidate());
  private final Timer collapse = new Timer(10, e -> scrollBar.revalidate());

  private MainPanel() {
    super(new GridLayout(1, 2));
    add(makeScrollBarOnHoverScrollPane());
    add(new JLayer<>(makeTranslucentScrollBar(makeList()), new ScrollBarOnHoverLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makeScrollBarOnHoverScrollPane() {
    JScrollPane scroll = new JScrollPane(makeList());
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    scrollBar.setLayout(new ScrollBarLayout());
    scrollBar.add(scroll.getVerticalScrollBar());

    JPanel wrap = new JPanel(new BorderLayout());
    wrap.add(scrollBar, BorderLayout.EAST);
    wrap.add(scroll);
    return new JLayer<>(wrap, new TimerScrollBarLayerUI());
  }

  private final class ScrollBarLayout extends BorderLayout {
    public static final int MIN_WIDTH = 6;
    private int controlsWidth = MIN_WIDTH;

    @Override public Dimension preferredLayoutSize(Container target) {
      Dimension ps = super.preferredLayoutSize(target);
      int barInitWidth = ps.width;
      if (expand.isRunning() && scrollBar.getWidth() < barInitWidth) {
        controlsWidth += 1;
        if (controlsWidth >= barInitWidth) {
          controlsWidth = barInitWidth;
          expand.stop();
        }
      } else if (collapse.isRunning() && scrollBar.getWidth() > MIN_WIDTH) {
        controlsWidth -= 1;
        if (controlsWidth <= MIN_WIDTH) {
          controlsWidth = MIN_WIDTH;
          collapse.stop();
        }
      }
      ps.width = controlsWidth;
      return ps;
    }
  }

  private final class TimerScrollBarLayerUI extends ScrollBarLayerUI {
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
      if (!(e.getComponent() instanceof JScrollBar)) {
        return;
      }
      switch (e.getID()) {
        case MouseEvent.MOUSE_ENTERED:
          expandStart(isDragging());
          break;
        case MouseEvent.MOUSE_EXITED:
          collapseStart(isDragging());
          break;
        case MouseEvent.MOUSE_RELEASED:
          setDragging(false);
          collapseStart(!e.getComponent().getBounds().contains(e.getPoint()));
          break;
        default:
          break;
      }
      l.getView().repaint();
    }

    private void expandStart(boolean dragging) {
      if (!expand.isRunning() && !dragging) {
        expand.setInitialDelay(0);
        expand.start();
      }
    }

    private void collapseStart(boolean dragging) {
      if (!collapse.isRunning() && !dragging) {
        collapse.setInitialDelay(500);
        collapse.start();
      }
    }
  }

  private static Component makeList() {
    DefaultListModel<String> m = new DefaultListModel<>();
    IntStream.range(0, 50)
        .mapToObj(i -> String.format("%05d: %s", i, LocalDateTime.now(ZoneId.systemDefault())))
        .forEach(m::addElement);
    return new JList<>(m);
  }

  private static JScrollPane makeTranslucentScrollBar(Component c) {
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
          getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
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

class ScrollBarLayerUI extends LayerUI<JPanel> {
  private boolean dragging;

  public void setDragging(boolean isDragging) {
    dragging = isDragging;
  }

  public boolean isDragging() {
    return dragging;
  }

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

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
    int id = e.getID();
    Component c = e.getComponent();
    if (c instanceof JScrollBar && id == MouseEvent.MOUSE_DRAGGED) {
      dragging = true;
    }
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
        vsb.setVisible(true);
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
  protected static final int MAX_WIDTH = 12;
  protected static final int MIN_WIDTH = 6;
  private static final Color DEFAULT_COLOR = new Color(100, 100, 100, 190);
  private static final Color DRAGGING_COLOR = new Color(100, 100, 100, 220);
  private static final Color ROLLOVER_COLOR = new Color(100, 100, 100, 220);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // Graphics2D g2 = (Graphics2D) g.create();
    // g2.setPaint(new Color(100, 100, 100, 100));
    // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
    // g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    Color color;
    if (!sb.isEnabled() || r.width > r.height) {
      return;
    } else if (isDragging) {
      color = DRAGGING_COLOR;
    } else if (isThumbRollover()) {
      color = ROLLOVER_COLOR;
    } else {
      color = DEFAULT_COLOR;
      int dw = r.width - sb.getPreferredSize().width;
      r.x += dw;
      r.width -= dw;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(color);
    g2.fillRect(r.x, r.y, r.width - 2, r.height - 1);
    g2.dispose();
  }
}

class ScrollBarOnHoverLayerUI extends LayerUI<JScrollPane> {
  private final Timer timer = new Timer(2000, null);
  private transient ActionListener listener;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    int id = e.getID();
    Component c = e.getComponent();
    if (c instanceof JScrollBar) {
      if (id == MouseEvent.MOUSE_ENTERED) {
        c.setPreferredSize(new Dimension(TranslucentScrollBarUI.MAX_WIDTH, 0));
      } else if (id == MouseEvent.MOUSE_EXITED) {
        timer.removeActionListener(listener);
        listener = ev -> {
          c.setPreferredSize(new Dimension(TranslucentScrollBarUI.MIN_WIDTH, 0));
          l.getView().revalidate();
          l.getView().repaint();
        };
        timer.addActionListener(listener);
        timer.setRepeats(false);
        timer.start();
      }
      l.getView().repaint();
    }
  }
}
