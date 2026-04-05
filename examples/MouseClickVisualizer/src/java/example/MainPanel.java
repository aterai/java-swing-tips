// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color COLOR_BODY = Color.WHITE;
  private static final Color COLOR_CLICK = new Color(173, 216, 230);
  private static final Color COLOR_WHEEL_IDLE = Color.LIGHT_GRAY;
  private static final Color COLOR_WHEEL_CLICK = new Color(255, 50, 50);
  private static final Color COLOR_WHEEL_MOVE = new Color(100, 150, 255);
  private static final Color COLOR_LINE = new Color(60, 60, 60);
  private static final int ICON_W = 100;
  private static final int ICON_H = 120;
  private static final float ICON_ARC = 60f;
  private static final int WHEEL_W = 20;
  private static final int WHEEL_H = 30;
  private static final float WHEEL_ARC = 10f;
  private static final float STROKE_W = 4f;
  private final Set<MouseButton> pressedButtons = EnumSet.noneOf(MouseButton.class);
  private final JTextArea logArea = new JTextArea();
  private final JLabel visualizerLabel = new JLabel(new MouseVisualizer());
  private final Timer wheelResetTimer = new Timer(100, null);
  private int wheelOffset;

  private MainPanel() {
    super(new BorderLayout());
    visualizerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    visualizerLabel.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        updateButton(e, true);
      }

      @Override public void mouseReleased(MouseEvent e) {
        updateButton(e, false);
      }
    });
    visualizerLabel.addMouseWheelListener(this::handleWheel);

    wheelResetTimer.addActionListener(e -> {
      wheelOffset = 0;
      visualizerLabel.repaint();
    });
    wheelResetTimer.setRepeats(false);

    logArea.setEditable(false);
    add(new JScrollPane(logArea));
    add(visualizerLabel, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void updateButton(MouseEvent e, boolean isPressed) {
    MouseButton btn = null;
    if (SwingUtilities.isLeftMouseButton(e)) {
      btn = MouseButton.LEFT;
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      btn = MouseButton.MIDDLE;
    } else if (SwingUtilities.isRightMouseButton(e)) {
      btn = MouseButton.RIGHT;
    }
    // boolean isPressed = e.getID() == MouseEvent.MOUSE_PRESSED;
    if (btn != null) {
      if (isPressed) {
        pressedButtons.add(btn);
      } else {
        pressedButtons.remove(btn);
      }
      logEvent((isPressed ? "Pressed: " : "Released: ") + btn);
      visualizerLabel.repaint();
    }
  }

  private void handleWheel(MouseWheelEvent e) {
    int rotation = e.getWheelRotation();
    String direction = rotation < 0 ? "UP" : "DOWN";
    wheelOffset = rotation < 0 ? -5 : 5;
    logEvent("Wheel Moved: " + direction + " (Amount: " + rotation + ")");
    visualizerLabel.repaint();
    wheelResetTimer.restart();
  }

  private void logEvent(String msg) {
    logArea.append(msg + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }

  private final class MouseVisualizer implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      float xx = x + STROKE_W / 2f;
      float yy = y + STROKE_W / 2f;
      float w1 = getIconWidth() - STROKE_W;
      float h1 = getIconHeight() - STROKE_W;
      Area body = new Area(new RoundRectangle2D.Float(xx, yy, w1, h1, ICON_ARC, ICON_ARC));
      g2.setColor(COLOR_BODY);
      g2.fill(body);
      g2.setColor(COLOR_LINE);
      g2.setStroke(new BasicStroke(STROKE_W));
      g2.draw(body);

      float w2 = w1 / 2f;
      float h2 = h1 / 2f;
      Area left = new Area(new Rectangle2D.Float(xx, yy, w2, h2));
      left.intersect(body);
      drawPart(g2, left, MouseButton.LEFT);

      Area right = new Area(new Rectangle2D.Float(xx + w2, yy, w2, h2));
      right.intersect(body);
      drawPart(g2, right, MouseButton.RIGHT);

      float wa = WHEEL_ARC;
      float wx = (float) body.getBounds().getCenterX() - WHEEL_W / 2f;
      float wy = y + WHEEL_H / 2f + wheelOffset;
      Shape wheel = new RoundRectangle2D.Float(wx, wy, WHEEL_W, WHEEL_H, wa, wa);
      g2.setColor(getWheelColor());
      g2.fill(wheel);
      g2.setColor(COLOR_LINE);
      g2.draw(wheel);
      g2.dispose();
    }

    @Override public int getIconWidth() {
      return ICON_W;
    }

    @Override public int getIconHeight() {
      return ICON_H;
    }

    private Color getWheelColor() {
      Color color = COLOR_WHEEL_IDLE;
      if (pressedButtons.contains(MouseButton.MIDDLE)) {
        color = COLOR_WHEEL_CLICK;
      } else if (wheelOffset != 0) {
        color = COLOR_WHEEL_MOVE;
      }
      return color;
    }

    private void drawPart(Graphics2D g2, Shape s, MouseButton target) {
      if (pressedButtons.contains(target)) {
        g2.setColor(COLOR_CLICK);
        g2.fill(s);
      }
      g2.setColor(COLOR_LINE);
      g2.draw(s);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("TabbedPane.tabInsets", new Insets(8, 2, 2, 2));
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

enum MouseButton {
  LEFT, MIDDLE, RIGHT
}
