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
  private static final int WHEEL_SHIFT = 5;
  private static final float STROKE_W = 4f;
  private final Set<MouseButton> pressedButtons = EnumSet.noneOf(MouseButton.class);
  private final JTextArea logArea = new JTextArea();
  private final JLabel visualizerLabel = new JLabel(new MouseIcon());
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
    MouseButton button = null;
    if (SwingUtilities.isLeftMouseButton(e)) {
      button = MouseButton.LEFT;
    } else if (SwingUtilities.isMiddleMouseButton(e)) {
      button = MouseButton.MIDDLE;
    } else if (SwingUtilities.isRightMouseButton(e)) {
      button = MouseButton.RIGHT;
    }
    if (button != null) {
      if (isPressed) {
        pressedButtons.add(button);
      } else {
        pressedButtons.remove(button);
      }
      logEvent((isPressed ? "Pressed: " : "Released: ") + button);
      visualizerLabel.repaint();
    }
  }

  private void handleWheel(MouseWheelEvent e) {
    int rotation = e.getWheelRotation();
    // A high-resolution wheel reports 0 until the rotation reaches a full notch.
    if (rotation != 0) {
      String direction = rotation < 0 ? "UP" : "DOWN";
      wheelOffset = rotation < 0 ? -WHEEL_SHIFT : WHEEL_SHIFT;
      logEvent("Wheel Moved: " + direction + " (Amount: " + rotation + ")");
      visualizerLabel.repaint();
      wheelResetTimer.restart();
    }
  }

  private void logEvent(String msg) {
    logArea.append(msg + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }

  private final class MouseIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      float bodyX = x + STROKE_W / 2f;
      float bodyY = y + STROKE_W / 2f;
      float bodyWidth = getIconWidth() - STROKE_W;
      float bodyHeight = getIconHeight() - STROKE_W;
      Area body = new Area(new RoundRectangle2D.Float(
          bodyX, bodyY, bodyWidth, bodyHeight, ICON_ARC, ICON_ARC));
      g2.setColor(COLOR_BODY);
      g2.fill(body);
      g2.setColor(COLOR_LINE);
      g2.setStroke(new BasicStroke(STROKE_W));
      g2.draw(body);

      float halfWidth = bodyWidth / 2f;
      float halfHeight = bodyHeight / 2f;
      Area left = new Area(new Rectangle2D.Float(bodyX, bodyY, halfWidth, halfHeight));
      left.intersect(body);
      drawButton(g2, left, MouseButton.LEFT);

      float centerX = bodyX + halfWidth;
      Area right = new Area(new Rectangle2D.Float(centerX, bodyY, halfWidth, halfHeight));
      right.intersect(body);
      drawButton(g2, right, MouseButton.RIGHT);

      float wheelX = centerX - WHEEL_W / 2f;
      float wheelY = y + WHEEL_H / 2f + wheelOffset;
      Shape wheel = new RoundRectangle2D.Float(
          wheelX, wheelY, WHEEL_W, WHEEL_H, WHEEL_ARC, WHEEL_ARC);
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

    private void drawButton(Graphics2D g2, Shape shape, MouseButton button) {
      if (pressedButtons.contains(button)) {
        g2.setColor(COLOR_CLICK);
        g2.fill(shape);
      }
      g2.setColor(COLOR_LINE);
      g2.draw(shape);
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
