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
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private static final int MIN_WIDTH = 6;
  public boolean willExpand;

  @SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane scroll = new JScrollPane(makeList());
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JPanel controls = new JPanel();
    Timer animator = new Timer(10, e -> controls.revalidate());
    controls.setLayout(new BorderLayout(0, 0) {
      private int controlsWidth = MIN_WIDTH;

      @Override public Dimension preferredLayoutSize(Container target) {
        Dimension ps = super.preferredLayoutSize(target);
        int controlsPreferredWidth = ps.width;
        if (animator.isRunning()) {
          if (willExpand) {
            if (controls.getWidth() < controlsPreferredWidth) {
              controlsWidth += 1;
            }
          } else {
            if (controls.getWidth() > MIN_WIDTH) {
              controlsWidth -= 1;
            }
          }
          if (controlsWidth <= MIN_WIDTH) {
            controlsWidth = MIN_WIDTH;
            animator.stop();
          } else if (controlsWidth >= controlsPreferredWidth) {
            controlsWidth = controlsPreferredWidth;
            animator.stop();
          }
        }
        ps.width = controlsWidth;
        return ps;
      }
    });
    controls.add(scroll.getVerticalScrollBar());

    JPanel p = new JPanel(new BorderLayout());
    p.add(controls, BorderLayout.EAST);
    p.add(scroll);

    JPanel pp = new JPanel(new GridLayout(1, 2));
    pp.add(new JLayer<>(p, new LayerUI<JPanel>() {
      private boolean isDragging;

      @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
          ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
          isDragging = true;
        }
      }

      @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        if (e.getComponent() instanceof JScrollBar) {
          switch (e.getID()) {
            case MouseEvent.MOUSE_ENTERED:
              if (!animator.isRunning() && !isDragging) {
                willExpand = true;
                animator.setInitialDelay(0);
                animator.start();
              }
              break;
            case MouseEvent.MOUSE_EXITED:
              if (!animator.isRunning() && !isDragging) {
                willExpand = false;
                animator.setInitialDelay(500);
                animator.start();
              }
              break;
            case MouseEvent.MOUSE_RELEASED:
              isDragging = false;
              if (!animator.isRunning() && !e.getComponent().getBounds().contains(e.getPoint())) {
                willExpand = false;
                animator.setInitialDelay(500);
                animator.start();
              }
              break;
            default:
              break;
          }
          l.getView().repaint();
        }
      }
    }));
    pp.add(new JLayer<>(makeTranslucentScrollBar(makeList()), new ScrollBarOnHoverLayerUI()));

    add(pp);
    setPreferredSize(new Dimension(320, 240));
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
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

class TranslucentScrollPaneLayout extends ScrollPaneLayout {
  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;

      Rectangle availR = scrollPane.getBounds();
      availR.setLocation(0, 0); // availR.x = availR.y = 0;

      Insets insets = parent.getInsets();
      availR.x = insets.left;
      availR.y = insets.top;
      availR.width -= insets.left + insets.right;
      availR.height -= insets.top + insets.bottom;

      Rectangle vsbR = new Rectangle();
      vsbR.width = 12;
      vsbR.height = availR.height;
      vsbR.x = availR.x + availR.width - vsbR.width;
      vsbR.y = availR.y;

      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        vsb.setVisible(true);
        vsb.setBounds(vsbR);
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
