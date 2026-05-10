// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Random RANDOM = new Random();
  private int score;

  private MainPanel() {
    super(new BorderLayout());

    // Create a 5-digit odometer-style counter
    Odometer odometer = new Odometer(5);
    add(odometer);

    JButton addButton = new JButton("Add Score");
    addButton.addActionListener(e -> {
      score += RANDOM.nextInt(500) + 100;
      odometer.updateValue(score);
    });

    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(e -> {
      score = 0;
      odometer.updateValue(0);
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(false);
    buttonPanel.add(addButton);
    buttonPanel.add(resetButton);
    add(buttonPanel, BorderLayout.SOUTH);

    setBackground(Color.BLACK);
    setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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

// A counter component that holds multiple DigitWheels to display a value
class Odometer extends JPanel {
  private final List<DigitWheel> wheels = new ArrayList<>();

  protected Odometer(int digitCount) {
    super(new FlowLayout(FlowLayout.CENTER, 4, 0));
    IntStream.range(0, digitCount).forEach(i -> {
      DigitWheel wheel = new DigitWheel();
      wheels.add(wheel);
      add(wheel);
    });
  }

  // Updates the displayed value by extracting each digit mathematically
  public void updateValue(int value) {
    boolean isReset = value == 0;
    int remainingValue = value;
    // Process from right to left (ones place first)
    for (int i = wheels.size() - 1; i >= 0; i--) {
      int digit = remainingValue % 10;
      wheels.get(i).setTargetDigit(digit, isReset);
      remainingValue /= 10;
    }
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public boolean isOpaque() {
    return false;
  }
}

// A vertical rotating wheel component representing a single digit (0-9)
class DigitWheel extends JComponent {
  private static final int DIGIT_HEIGHT = 80;
  private static final int WHEEL_HEIGHT = DIGIT_HEIGHT * 10;
  private final Timer animationTimer = new Timer(16, e -> animateScroll());

  private double currentY;
  private double targetY;

  @Override public Dimension getPreferredSize() {
    return new Dimension(55, DIGIT_HEIGHT);
  }

  private void animateScroll() {
    double delta = targetY - currentY;
    boolean threshold = Math.abs(delta) < .01;
    if (threshold) {
      currentY = targetY;
      animationTimer.stop();
    } else {
      // Smoothly decelerate as it approaches the target
      double speed = Math.min(.25, .5 + Math.abs(delta) / 2000d);
      currentY += delta * speed;
    }
    repaint();
  }

  public void setTargetDigit(int digit, boolean isReset) {
    if (isReset) {
      // For reset, normalize coordinates to return to zero via the shortest path
      targetY = digit * DIGIT_HEIGHT;
      currentY %= WHEEL_HEIGHT;
      if (currentY < 0) {
        currentY += WHEEL_HEIGHT;
      }
    } else {
      double nextTargetY = digit * DIGIT_HEIGHT;
      double normalizedY = currentY % WHEEL_HEIGHT;
      if (normalizedY < 0) {
        normalizedY += WHEEL_HEIGHT;
      }

      double distance = nextTargetY - normalizedY;
      if (distance < 0) {
        // Ensure the wheel always rotates forward (slot machine style)
        distance += WHEEL_HEIGHT;
      }
      targetY = currentY + distance;
    }

    if (!animationTimer.isRunning()) {
      animationTimer.start();
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setClip(0, 0, getWidth(), getHeight());

    // Draw background
    g2.setColor(Color.DARK_GRAY);
    g2.fillRect(0, 0, getWidth(), getHeight());

    g2.setFont(getFont().deriveFont(Font.BOLD, 55f));
    FontMetrics fm = g2.getFontMetrics();

    for (int i = 0; i < 10; i++) {
      double posY = (i * DIGIT_HEIGHT) - (currentY % WHEEL_HEIGHT);
      // Coordinate correction for looping display
      if (posY < -DIGIT_HEIGHT) {
        posY += WHEEL_HEIGHT;
      } else if (posY > WHEEL_HEIGHT - DIGIT_HEIGHT) {
        posY -= WHEEL_HEIGHT;
      }

      // Highlight the number when it's near the center
      double distFromCenter = Math.abs(posY);
      g2.setColor(distFromCenter < 10.0 ? Color.WHITE : Color.GRAY);
      int drawX = (getWidth() - fm.stringWidth(String.valueOf(i))) / 2;
      int drawY = (int) (posY + (DIGIT_HEIGHT / 2d) + (fm.getAscent() / 2d) - 5d);
      g2.drawString(String.valueOf(i), drawX, drawY);
    }
    g2.dispose();
  }
}
