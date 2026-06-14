// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    ExpandableSplitButton btn1 = new ExpandableSplitButton("Project");
    btn1.setAlignmentX(LEFT_ALIGNMENT);
    btn1.setAction(createAction("Project", e -> JOptionPane.showMessageDialog(
        getRootPane(),
        "'Project' title area was clicked.",
        "Title Click",
        JOptionPane.INFORMATION_MESSAGE)));
    btn1.setComponentPopupMenu(buildDefaultPopupMenu());
    box.add(createLayer(btn1));
    box.add(Box.createVerticalStrut(16));

    ExpandableSplitButton btn2 = new ExpandableSplitButton("Settings");
    btn2.setAlignmentX(LEFT_ALIGNMENT);
    btn2.setAction(createAction("Settings", e -> JOptionPane.showMessageDialog(
        getRootPane(),
        "Opening 'Settings' panel.",
        "Settings",
        JOptionPane.INFORMATION_MESSAGE)));
    btn2.setComponentPopupMenu(createPopupMenu());
    box.add(createLayer(btn2));
    box.add(Box.createVerticalStrut(16));

    ExpandableSplitButton btn3 = new ExpandableSplitButton("Dashboard");
    btn3.setAlignmentX(LEFT_ALIGNMENT);
    btn3.setAction(createAction("Dashboard", e -> JOptionPane.showMessageDialog(
        getRootPane(),
        "Navigating to Dashboard.",
        "Navigate",
        JOptionPane.INFORMATION_MESSAGE)));
    btn3.setIcon(new CharIcon("⏰", 10));
    btn3.setComponentPopupMenu(createPopupMenu());
    box.add(createLayer(btn3));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLayer<?> createLayer(ExpandableSplitButton button) {
    JLayer<?> layer = new JLayer<>(button, new SplitButtonLayerUI());
    layer.setAlignmentX(LEFT_ALIGNMENT);
    return layer;
  }

  private static Action createAction(String name, ActionListener al) {
    AbstractAction action = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        al.actionPerformed(e);
      }
    };
    action.putValue(Action.NAME, name);
    return action;
  }

  private JPopupMenu buildDefaultPopupMenu() {
    JPopupMenu popupMenu = new JPopupMenu();
    for (String label : Arrays.asList("Edit", "Copy", "Share", "Delete")) {
      popupMenu.add(createMenuItem(label));
    }
    return popupMenu;
  }

  private JPopupMenu createPopupMenu() {
    JPopupMenu popupMenu = new JPopupMenu();
    for (String opt : Arrays.asList("General", "Security", "Network")) {
      popupMenu.add(createMenuItem(opt));
    }
    return popupMenu;
  }

  private JMenuItem createMenuItem(String label) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(e -> JOptionPane.showMessageDialog(
        this, label + "' was selected.",
        "Action", JOptionPane.INFORMATION_MESSAGE));
    return item;
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

class SplitButtonLayerUI extends LayerUI<ExpandableSplitButton> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    ((JLayer<?>) c).setLayerEventMask(
        AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }

  @Override public void uninstallUI(JComponent c) {
    ((JLayer<?>) c).setLayerEventMask(0);
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(
      MouseEvent e, JLayer<? extends ExpandableSplitButton> layer) {
    ExpandableSplitButton button = layer.getView();
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        button.getAnim().expand();
        button.setForeground(new Color(
            UIManager.getColor("List.selectionForeground").getRGB()));
        break;

      case MouseEvent.MOUSE_EXITED:
        button.setMouseOnArrow(false);
        if (!button.isPopupOpen()) {
          button.getAnim().collapse();
        }
        button.setForeground(UIManager.getColor("List.foreground"));
        break;

      case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_CLICKED:
      case MouseEvent.MOUSE_RELEASED:
        if (SwingUtilities.isRightMouseButton(e)) {
          // Right click is consumed to suppress automatic display of JPopupMenu
          e.consume();
        } else {
          // Left click: Popup for arrow area, Action for title area
          if (button.isOnArrowArea(e.getPoint())) {
            button.showPopup();
            e.consume();
          }
        }
        // Clicking on the title area does not consume
        //     → JButton executes the Action
        break;

      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(
      MouseEvent e, JLayer<? extends ExpandableSplitButton> layer) {
    if (e.getID() == MouseEvent.MOUSE_MOVED) {
      ExpandableSplitButton btn = layer.getView();
      boolean onArrow = btn.isOnArrowArea(e.getPoint());
      if (onArrow != btn.isMouseOnArrow()) {
        btn.setMouseOnArrow(onArrow);
        btn.repaint();
      }
    }
  }
}

class ExpandableSplitButton extends JButton {
  private static final int ARC_RADIUS = 8;
  private static final int ANIM_INTERVAL_MS = 6;
  private static final int ANIM_STEPS = 10;
  private static final int ARROW_AREA_WIDTH = 20;
  private static final int EXTRA_WIDTH = ARROW_AREA_WIDTH + 8;

  private int collapsedWidth;
  private boolean mouseOnArrow;

  private final transient AnimationController anim = new AnimationController();
  private final transient PopupController popupCtrl = new PopupController();

  protected ExpandableSplitButton(String title) {
    super(title);
    EventQueue.invokeLater(() -> {
      revalidate();
      repaint();
    });
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setHorizontalAlignment(LEFT);
    setMargin(new Insets(6, 12, 6, 12));
  }

  public AnimationController getAnim() {
    return anim;
  }

  public boolean isPopupOpen() {
    return popupCtrl.isOpen();
  }

  public boolean isOnArrowArea(Point p) {
    float progress = easeInOut(anim.getProgress());
    return progress > .5f && p.x >= collapsedWidth && p.x <= getWidth();
  }

  public boolean isMouseOnArrow() {
    return mouseOnArrow;
  }

  public void setMouseOnArrow(boolean v) {
    mouseOnArrow = v;
  }

  /* default */ void showPopup() {
    JPopupMenu menu = getComponentPopupMenu();
    if (menu != null) {
      popupCtrl.show(menu, this);
    }
  }

  @Override public void doLayout() {
    updateCollapsedWidth();
    super.doLayout();
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = collapsedWidth + Math.round(EXTRA_WIDTH * easeInOut(anim.getProgress()));
    return d;
  }

  @Override public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    d.width = collapsedWidth;
    return d;
  }

  @Override public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = getWidth();
    int h = getHeight();

    // Background
    Color bg = getModel().isRollover()
        ? UIManager.getColor("List.selectionBackground")
        : UIManager.getColor("List.background");
    g2.setColor(bg);
    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, ARC_RADIUS, ARC_RADIUS));

    // Separator and Border
    g2.setColor(UIManager.getColor("List.dropLineColor"));
    float progress = easeInOut(anim.getProgress());
    boolean isProgress = progress > .5f;
    if (isProgress) {
      g2.setStroke(new BasicStroke(1f));
      g2.drawLine(collapsedWidth, 6, collapsedWidth, h - 6);
    }
    g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, ARC_RADIUS, ARC_RADIUS));
    g2.dispose();

    // Text and Icon
    super.paintComponent(g);

    // Arrow
    if (isProgress) {
      paintArrow(g, progress, h);
    }
  }

  private void paintArrow(Graphics g, float progress, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color hover = UIManager.getColor("List.selectionForeground");
    g2.setColor(mouseOnArrow ? getForeground() : hover.darker());
    int arrowX = collapsedWidth + Math.round(EXTRA_WIDTH * progress) / 2;
    int arrowY = h / 2;
    int aw = 10;
    int ah = 6;
    int[] xp = { arrowX - aw / 2, arrowX + aw / 2, arrowX };
    int[] yp = { arrowY - ah / 2, arrowY - ah / 2, arrowY + ah / 2 };
    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.drawPolyline(
        new int[] { xp[0], xp[2], xp[1] },
        new int[] { yp[0], yp[2], yp[1] },
        3);
    g2.dispose();
  }

  // Recalculates collapsed width from title text width.
  private void updateCollapsedWidth() {
    FontMetrics fm = getFontMetrics(getFont());
    Dimension d = getPreferredSize();
    Rectangle viewR = new Rectangle(0, 0, Integer.MAX_VALUE, d.height);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    SwingUtilities.layoutCompoundLabel(
        this,
        fm,
        getText(),
        getIcon(),
        getVerticalAlignment(),
        getHorizontalAlignment(),
        getVerticalTextPosition(),
        getHorizontalTextPosition(),
        viewR,
        iconR,
        textR,
        getIconTextGap()
    );
    Insets ins = getInsets();
    collapsedWidth = iconR.union(textR).width + ins.left + ins.right;
  }

  private static float easeInOut(float t) {
    return t * t * (3f - 2f * t);
  }

  /* default */ final class AnimationController {
    private float progress;
    private int direction;
    private final Timer timer = new Timer(ANIM_INTERVAL_MS, e -> onTick());

    /* default */ void expand() {
      direction = 1;
      if (!timer.isRunning()) {
        timer.start();
      }
    }

    /* default */ void collapse() {
      direction = -1;
      if (!timer.isRunning()) {
        timer.start();
      }
    }

    private float getProgress() {
      return progress;
    }

    private void onTick() {
      float step = 1f / ANIM_STEPS;
      progress = Math.min(Math.max(progress + direction * step, 0f), 1f);
      // Java 21: progress = Math.clamp(progress + direction * step, 0f, 1f);
      if (progress <= 0f || progress >= 1f) {
        timer.stop();
      }
      revalidate();
      repaint();
      Container parent = getParent();
      if (parent != null) {
        parent.revalidate();
      }
    }
  }

  private final class PopupController implements PopupMenuListener {
    private boolean open;

    private boolean isOpen() {
      return open;
    }

    private void show(JPopupMenu menu, Component invoker) {
      open = true;
      menu.addPopupMenuListener(this);
      menu.show(invoker, 0, invoker.getHeight() + 4);
    }

    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // not needed
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
      // not needed
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      open = false;
      if (!getModel().isRollover()) {
        anim.collapse();
      }
      ((JPopupMenu) e.getSource()).removePopupMenuListener(this);
    }
  }
}

class CharIcon implements Icon {
  private final String name;
  private final int size;

  protected CharIcon(String name, int size) {
    this.name = name;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(c.getForeground());
    FontMetrics fontMetrics = g2.getFontMetrics();
    g2.translate(x, y);
    int tx = (size - fontMetrics.stringWidth(name)) / 2;
    int ty = (size - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
    g2.drawString(name, tx, ty);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}
