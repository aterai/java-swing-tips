// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Container makeTitleBox(String title) {
    JCheckBox check = new JCheckBox("check");
    check.setOpaque(false);
    check.setFocusable(false);
    String[] items = {title, title + " (1)", title + " (2)", title + " (3)"};
    JComboBox<String> combo = new NoButtonComboBox<>(new DefaultComboBoxModel<>(items));
    Box titleBox = Box.createHorizontalBox();
    titleBox.add(combo);
    titleBox.add(Box.createHorizontalGlue());
    titleBox.add(check);
    return titleBox;
  }

  public static JFrame makeFrame(String title) {
    JFrame frame = new JFrame(title);
    frame.setUndecorated(true);
    GraphicsConfiguration gc = frame.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      frame.setBackground(new Color(0x0, true));
    }
    Container titleBox = makeTitleBox(title);
    JPanel resizePanel = new ResizePanel(titleBox);
    resizePanel.setOpaque(false);
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setOpaque(false);
    resizePanel.add(contentPane);
    frame.setContentPane(resizePanel);
    return frame;
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
    JFrame frame = makeFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setMinimumSize(new Dimension(100, 100));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum Side {
  N(Cursor.N_RESIZE_CURSOR, 0, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.y += d.y;
      r.height -= d.y;
      return r;
    }
  },
  W(Cursor.W_RESIZE_CURSOR, 4, 0) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.x += d.x;
      r.width -= d.x;
      return r;
    }
  },
  E(Cursor.E_RESIZE_CURSOR, 4, 0) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.width += d.x;
      return r;
    }
  },
  S(Cursor.S_RESIZE_CURSOR, 0, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.height += d.y;
      return r;
    }
  },
  NW(Cursor.NW_RESIZE_CURSOR, 4, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.y += d.y;
      r.height -= d.y;
      r.x += d.x;
      r.width -= d.x;
      return r;
    }
  },
  NE(Cursor.NE_RESIZE_CURSOR, 4, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.y += d.y;
      r.height -= d.y;
      r.width += d.x;
      return r;
    }
  },
  SW(Cursor.SW_RESIZE_CURSOR, 4, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.height += d.y;
      r.x += d.x;
      r.width -= d.x;
      return r;
    }
  },
  SE(Cursor.SE_RESIZE_CURSOR, 4, 4) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      r.height += d.y;
      r.width += d.x;
      return r;
    }
  };

  /* default */ final int cursor;
  /* default */ final int width;
  /* default */ final int height;

  Side(int cursor, int width, int height) {
    this.cursor = cursor;
    this.width = width;
    this.height = height;
  }

  public Cursor getCursor() {
    return Cursor.getPredefinedCursor(cursor);
  }

  public Dimension getSize() {
    return new Dimension(width, height);
  }

  /* default */ abstract Rectangle getBounds(Rectangle rect, Point delta);
}

class SideLabel extends JLabel {
  public final Side side;

  protected SideLabel(Side side) {
    super();
    this.side = side;
    EventQueue.invokeLater(() -> setCursor(side.getCursor()));
  }

  @Override public final void setCursor(Cursor cursor) {
    super.setCursor(cursor);
  }

  @Override public Dimension getPreferredSize() {
    return side.getSize();
  }

  @Override public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override public Dimension getMaximumSize() {
    return getPreferredSize();
  }
}

class ResizePanel extends JPanel {
  private static final int W = 4;
  private final Color borderColor = new Color(0x64_64_64);

  protected ResizePanel(Container titleBox) {
    super(new BorderLayout());
    MouseAdapter rwl = new ResizeWindowListener();
    SideLabel left = new SideLabel(Side.W);
    SideLabel right = new SideLabel(Side.E);
    SideLabel top = new SideLabel(Side.N);
    SideLabel bottom = new SideLabel(Side.S);
    SideLabel topLeft = new SideLabel(Side.NW);
    SideLabel topRight = new SideLabel(Side.NE);
    SideLabel bottomLeft = new SideLabel(Side.SW);
    SideLabel bottomRight = new SideLabel(Side.SE);
    Stream.of(left, right, top, bottom, topLeft, topRight, bottomLeft, bottomRight)
        .forEach(c -> {
          c.addMouseListener(rwl);
          c.addMouseMotionListener(rwl);
        });

    JPanel titleBar = makeTitleBar(titleBox);
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.add(top, BorderLayout.NORTH);
    titlePanel.add(new JLayer<>(titleBar, new TitleBarDragLayerUI()), BorderLayout.CENTER);

    JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.add(topLeft, BorderLayout.WEST);
    northPanel.add(titlePanel, BorderLayout.CENTER);
    northPanel.add(topRight, BorderLayout.EAST);

    JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.add(bottomLeft, BorderLayout.WEST);
    southPanel.add(bottom, BorderLayout.CENTER);
    southPanel.add(bottomRight, BorderLayout.EAST);

    add(left, BorderLayout.WEST);
    add(right, BorderLayout.EAST);
    add(northPanel, BorderLayout.NORTH);
    add(southPanel, BorderLayout.SOUTH);

    titlePanel.setOpaque(false);
    northPanel.setOpaque(false);
    southPanel.setOpaque(false);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    int h = getHeight();
    g2.setPaint(Color.ORANGE);
    g2.fillRect(0, 0, w, h);
    g2.setPaint(borderColor);
    g2.drawRect(0, 0, w - 1, h - 1);
    g2.drawLine(0, 2, 2, 0);
    g2.drawLine(w - 3, 0, w - 1, 2);
    g2.clearRect(0, 0, 2, 1);
    g2.clearRect(0, 0, 1, 2);
    g2.clearRect(w - 2, 0, 2, 1);
    g2.clearRect(w - 1, 0, 1, 2);
    g2.dispose();
  }

  private static JPanel makeTitleBar(Container titleBox) {
    JPanel titleBar = new JPanel(new BorderLayout(W, W));
    titleBar.setOpaque(false);
    titleBar.setBackground(Color.ORANGE);
    titleBar.setBorder(BorderFactory.createEmptyBorder(W, W, W, W));

    JButton button = makeTitleButton(new ApplicationIcon());
    button.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    titleBar.add(button, BorderLayout.WEST);
    titleBar.add(titleBox);

    JButton close = makeTitleButton(new CloseIcon());
    close.addActionListener(e -> {
      JComponent b = (JComponent) e.getSource();
      Container c = b.getTopLevelAncestor();
      if (c instanceof Window) {
        Window w = (Window) c;
        w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
      }
    });
    titleBar.add(close, BorderLayout.EAST);
    return titleBar;
  }

  private static JButton makeTitleButton(Icon icon) {
    JButton button = new JButton(icon);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setOpaque(true);
    button.setBackground(Color.ORANGE);
    return button;
  }
}

class ResizeWindowListener extends MouseAdapter {
  private final Rectangle rect = new Rectangle();

  @Override public void mousePressed(MouseEvent e) {
    Component p = SwingUtilities.getRoot(e.getComponent());
    if (p instanceof Window) {
      rect.setBounds(p.getBounds());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Component p = SwingUtilities.getRoot(c);
    if (!rect.isEmpty() && c instanceof SideLabel && p instanceof Window) {
      Side side = ((SideLabel) c).side;
      p.setBounds(side.getBounds(rect, e.getPoint()));
    }
  }
}

class TitleBarDragLayerUI extends LayerUI<JComponent> {
  private final Point startPt = new Point();

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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
    if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    boolean isDrag = e.getID() == MouseEvent.MOUSE_DRAGGED;
    if (c instanceof Window && isDrag && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}

class ApplicationIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(Color.BLUE);
    g2.fillOval(4, 4, 11, 11);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class CloseIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.drawLine(4, 4, 11, 11);
    g2.drawLine(4, 5, 10, 11);
    g2.drawLine(5, 4, 11, 10);
    g2.drawLine(11, 4, 4, 11);
    g2.drawLine(11, 5, 5, 11);
    g2.drawLine(10, 4, 4, 10);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class NoButtonComboBox<E> extends JComboBox<E> {
  private transient MouseAdapter listener;

  protected NoButtonComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    super.updateUI();
    setUI(new BasicComboBoxUI() {
      @Override protected JButton createArrowButton() {
        JButton button = new JButton();
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setVisible(false);
        return button;
      }
    });
    setOpaque(true);
    setForeground(Color.BLACK);
    setBackground(Color.ORANGE);
    setBorder(BorderFactory.createEmptyBorder());
    setFocusable(false);
    // setFont(getFont().deriveFont(16f));
    listener = new MouseHandler();
    addMouseListener(listener);
  }

  protected class MouseHandler extends MouseAdapter {
    private ButtonModel getButtonModel(MouseEvent e) {
      JComboBox<?> cb = (JComboBox<?>) e.getComponent();
      JButton b = (JButton) cb.getComponent(0);
      return b.getModel();
    }

    @Override public void mouseEntered(MouseEvent e) {
      getButtonModel(e).setRollover(true);
      e.getComponent().setBackground(Color.ORANGE.darker());
    }

    @Override public void mouseExited(MouseEvent e) {
      getButtonModel(e).setRollover(false);
      e.getComponent().setBackground(Color.ORANGE);
    }

    @Override public void mousePressed(MouseEvent e) {
      getButtonModel(e).setPressed(true);
      setPopupVisible(false);
    }

    @Override public void mouseReleased(MouseEvent e) {
      getButtonModel(e).setPressed(false);
    }

    @Override public void mouseClicked(MouseEvent e) {
      setPopupVisible(true);
    }
  }
}
