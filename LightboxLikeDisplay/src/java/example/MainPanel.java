package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public MainPanel() {
    super(new GridLayout(1, 2));
    EventQueue.invokeLater(() -> {
      getRootPane().setGlassPane(new LightboxGlassPane());
      getRootPane().getGlassPane().setVisible(false);
    });
    JButton button = new JButton("Open");
    button.addActionListener(e -> getRootPane().getGlassPane().setVisible(true));
    add(makeDummyPanel());
    add(button);
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel makeDummyPanel() {
    JButton b = new JButton("Button & Mnemonic");
    b.setMnemonic(KeyEvent.VK_B);
    JTextField t = new JTextField("TextField & ToolTip");
    t.setToolTipText("ToolTip");
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(b, BorderLayout.NORTH);
    p.add(t, BorderLayout.SOUTH);
    p.add(new JScrollPane(new JTree()));
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LightboxGlassPane extends JPanel {
  private static final int BW = 5;
  private final ImageIcon image = new ImageIcon(LightboxGlassPane.class.getResource("test.png"));
  private final transient AnimeIcon animatedIcon = new AnimeIcon();
  private float alpha;
  private int curimgw;
  private int curimgh;
  private final Rectangle rect = new Rectangle();
  protected Timer animator;
  protected transient Handler handler;

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeHierarchyListener(handler);
    super.updateUI();
    setOpaque(false);
    super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    handler = new Handler();
    addMouseListener(handler);
    addHierarchyListener(handler);
  }

  private class Handler extends MouseAdapter implements HierarchyListener {
    @Override public void mouseClicked(MouseEvent e) {
      e.getComponent().setVisible(false);
    }

    @Override public void hierarchyChanged(HierarchyEvent e) {
      boolean isDisplayableChanged = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (isDisplayableChanged && !e.getComponent().isDisplayable() && Objects.nonNull(animator)) {
        animator.stop();
      }
    }
  }

  @Override public void setVisible(boolean isVisible) {
    boolean oldVisible = isVisible();
    super.setVisible(isVisible);
    JRootPane rootPane = getRootPane();
    if (Objects.nonNull(rootPane) && isVisible() != oldVisible) {
      rootPane.getLayeredPane().setVisible(!isVisible);
    }
    boolean b = Objects.isNull(animator) || !animator.isRunning();
    if (isVisible && b) {
      curimgw = 40;
      curimgh = 40;
      alpha = 0f;
      animator = new Timer(10, e -> {
        animatedIcon.next();
        repaint();
      });
      animator.start();
    } else {
      if (Objects.nonNull(animator)) {
        animator.stop();
      }
    }
    animatedIcon.setRunning(isVisible);
  }

  @Override protected void paintComponent(Graphics g) {
    Optional.ofNullable(getRootPane()).ifPresent(r -> r.getLayeredPane().print(g));
    super.paintComponent(g);

    if (curimgh < image.getIconHeight() + BW + BW) {
      curimgh += image.getIconHeight() / 16;
    } else if (curimgw < image.getIconWidth() + BW + BW) {
      curimgh = image.getIconHeight() + BW + BW;
      curimgw += image.getIconWidth() / 16;
    } else if (1f - alpha > 0) {
      curimgw = image.getIconWidth() + BW + BW;
      alpha = alpha + .1f;
    } else {
      animatedIcon.setRunning(false);
      animator.stop();
    }
    rect.setSize(curimgw, curimgh);
    Rectangle screen = getBounds();
    Point centerPt = new Point(screen.x + screen.width / 2, screen.y + screen.height / 2);
    rect.setLocation(centerPt.x - rect.width / 2, centerPt.y - rect.height / 2);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new Color(0x64646464, true));
    g2.fill(screen);
    g2.setPaint(new Color(0xC8FFFFFF, true));
    g2.fill(rect);

    if (alpha > 0) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1f)));
      g2.drawImage(image.getImage(), rect.x + BW, rect.y + BW, image.getIconWidth(), image.getIconHeight(), this);
    } else {
      animatedIcon.paintIcon(this, g2, centerPt.x - animatedIcon.getIconWidth() / 2, centerPt.y - animatedIcon.getIconHeight() / 2);
    }
    g2.dispose();
  }
}

class AnimeIcon implements Icon {
  private static final Color ELLIPSE_COLOR = new Color(.5f, .5f, .5f);
  private static final double R = 2d;
  private static final double SX = 0d;
  private static final double SY = 0d;
  private static final int WIDTH = (int) (R * 8 + SX * 2);
  private static final int HEIGHT = (int) (R * 8 + SY * 2);
  private final List<Shape> list = new ArrayList<>(Arrays.asList(
      new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));
  private boolean running;

  public void next() {
    if (running) {
      // list.add(list.remove(0));
      Collections.rotate(list, 1);
    }
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(new Color(0x0, true));
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    int size = list.size();
    for (int i = 0; i < size; i++) {
      float alpha = running ? (i + 1) / (float) size : .5f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(list.get(i));
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return WIDTH;
  }

  @Override public int getIconHeight() {
    return HEIGHT;
  }
}
