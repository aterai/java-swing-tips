// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final Color DRAWER_BG = new Color(33, 37, 41);
  public static final Color HOVER_COLOR = new Color(52, 58, 64);
  public static final int DRAWER_WIDTH = 200;
  public static final int DURATION = 400;
  private final JPanel overlay = makeOverlay();
  private final JPanel drawer = makeDrawer();
  private final Timer timer = new Timer(10, e -> animate());
  private DrawerPosition position = DrawerPosition.LEFT;
  private long startTime;
  private int startX;
  private int targetX;
  private boolean isOpen;

  private MainPanel() {
    super();
    EventQueue.invokeLater(() -> {
      initLayeredPane();
      updateLayoutSizes();
    });
    add(makeControlBox());
    setBackground(Color.GRAY);
    setPreferredSize(new Dimension(320, 240));
  }

  private void initLayeredPane() {
    JLayeredPane layeredPane = getRootPane().getLayeredPane();
    // layeredPane.add(this, JLayeredPane.DEFAULT_LAYER);
    layeredPane.add(overlay, JLayeredPane.PALETTE_LAYER);
    layeredPane.add(drawer, JLayeredPane.MODAL_LAYER);
    layeredPane.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        updateLayoutSizes();
      }
    });
  }

  private JPanel makeControlBox() {
    JButton btnOpen = new JButton("Open Menu");
    btnOpen.addActionListener(e -> toggleDrawer());

    JRadioButton rbLeft = new JRadioButton("Left", true);
    JRadioButton rbRight = new JRadioButton("Right", false);
    ButtonGroup group = new ButtonGroup();
    group.add(rbLeft);
    group.add(rbRight);
    ActionListener positionSwitcher = e -> {
      position = rbLeft.isSelected() ? DrawerPosition.LEFT : DrawerPosition.RIGHT;
      updateLayoutSizes();
    };
    rbLeft.addActionListener(positionSwitcher);
    rbRight.addActionListener(positionSwitcher);

    JPanel controls = new JPanel();
    controls.setOpaque(false);
    controls.add(btnOpen);
    controls.add(rbLeft);
    controls.add(rbRight);
    return controls;
  }

  private JPanel makeOverlay() {
    JPanel ov = new OverlayPanel();
    ov.setVisible(false);
    ov.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (isOpen) {
          toggleDrawer();
        }
      }
    });
    return ov;
  }

  private JPanel makeDrawer() {
    Box menuContainer = Box.createVerticalBox();
    menuContainer.setBackground(DRAWER_BG);
    menuContainer.add(new NavButton("🏠  Dashboard"));
    menuContainer.add(new NavButton("📩  Messages"));
    menuContainer.add(new NavButton("📊  Analytics"));
    menuContainer.add(new NavButton("⚙️  Settings"));
    menuContainer.add(Box.createVerticalGlue());
    menuContainer.add(new NavButton("🚪  Logout"));

    JScrollPane scroll = new JScrollPane(menuContainer);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getViewport().setBackground(DRAWER_BG);

    JPanel dr = new JPanel(new BorderLayout());
    dr.setBackground(DRAWER_BG);
    dr.add(scroll);
    // KeyStroke escape = KeyStroke.getKeyStroke("ESCAPE");
    // dr.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(escape, "closeDrawer");
    // dr.getActionMap().put("closeDrawer", new AbstractAction() {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     if (isOpen) {
    //       toggleDrawer();
    //     }
    //   }
    // });
    return dr;
  }

  private void updateLayoutSizes() {
    Container contentPane = getRootPane().getContentPane();
    Dimension d = contentPane.getSize(); // = this.getSize();
    int w = d.width;
    int h = d.height;
    contentPane.setBounds(0, 0, w, h); // this.setBounds(0, 0, w, h);
    overlay.setBounds(0, 0, w, h);
    drawer.setBounds(isOpen ? getOpenX(w) : getClosedX(w), 0, DRAWER_WIDTH, h);
    revalidate();
    repaint();
  }

  private int getOpenX(int w) {
    return position == DrawerPosition.LEFT ? 0 : w - DRAWER_WIDTH;
  }

  private int getClosedX(int w) {
    return position == DrawerPosition.LEFT ? -DRAWER_WIDTH : w;
  }

  private void toggleDrawer() {
    if (!timer.isRunning()) {
      isOpen = !isOpen;
      int w = getRootPane().getContentPane().getWidth();
      startX = drawer.getX();
      targetX = isOpen ? getOpenX(w) : getClosedX(w);
      overlay.setVisible(isOpen);
      startTime = System.currentTimeMillis();
      timer.start();
    }
  }

  private void animate() {
    long elapsed = System.currentTimeMillis() - startTime;
    double progress = Math.min(1d, (double) elapsed / DURATION);
    double easedProgress = 1 - Math.pow(1 - progress, 3);
    int currentX = (int) (startX + (targetX - startX) * easedProgress);
    drawer.setLocation(currentX, 0);
    boolean stop = progress >= 1d;
    if (stop) {
      timer.stop();
      if (!isOpen) {
        overlay.setVisible(false);
      }
    }
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
    JFrame frame = new JFrame("Drawer Demo");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum DrawerPosition {
  LEFT, RIGHT
}

class OverlayPanel extends JPanel {
  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(new Color(0, 0, 0, 140));
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}

class NavButton extends JButton {
  public static final Color TEXT_COLOR = new Color(248, 249, 250);
  private transient MouseListener mouseListener;

  protected NavButton(String text) {
    super(text);
  }

  @Override public void updateUI() {
    removeMouseListener(mouseListener);
    super.updateUI();
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setForeground(TEXT_COLOR);
    setHorizontalAlignment(LEFT);
    setFont(getFont().deriveFont(Font.PLAIN, 14f));
    setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    mouseListener = new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        setOpaque(true);
        setBackground(MainPanel.HOVER_COLOR);
      }

      @Override public void mouseExited(MouseEvent e) {
        setOpaque(false);
      }

      @Override public void mousePressed(MouseEvent e) {
        setBackground(MainPanel.HOVER_COLOR.darker());
      }

      @Override public void mouseReleased(MouseEvent e) {
        setBackground(MainPanel.HOVER_COLOR);
      }
    };
    addMouseListener(mouseListener);
  }

  @Override public Dimension getMaximumSize() {
    return new Dimension(MainPanel.DRAWER_WIDTH, 50);
  }

  @Override protected void paintComponent(Graphics g) {
    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    super.paintComponent(g);
  }
}
