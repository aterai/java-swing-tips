// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private final JPanel root = new JPanel(new BorderLayout(5, 5));
  private final StepperPanel stepperPanel;
  private final JButton prevButton;
  private final JButton nextButton;
  private final JLabel statusLabel;
  private final JComboBox<StepperPanel.Orientation> orientationCombo;

  private MainPanel() {
    super(new GridBagLayout());
    String[] steps = {"Order", "Your info", "Payment", "Confirmation"};
    stepperPanel = new StepperPanel(steps);

    prevButton = new JButton("Previous Step");
    nextButton = new JButton("Next Step");
    statusLabel = new JLabel("", SwingConstants.CENTER);

    orientationCombo = new JComboBox<>(StepperPanel.Orientation.values());
    orientationCombo.setSelectedItem(StepperPanel.Orientation.HORIZONTAL);

    initUi();
    updateControls();
    setPreferredSize(new Dimension(320, 240));
  }

  private void initUi() {
    statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));

    // Control Panel
    JPanel controlPanel = new JPanel();
    controlPanel.add(orientationCombo);
    controlPanel.add(prevButton);
    controlPanel.add(nextButton);

    prevButton.addActionListener(e -> {
      stepperPanel.previousStep();
      updateControls();
    });

    nextButton.addActionListener(e -> {
      stepperPanel.nextStep();
      updateControls();
    });

    orientationCombo.addActionListener(e -> {
      int index = orientationCombo.getSelectedIndex();
      updateOrientation(orientationCombo.getItemAt(index));
    });

    JPanel contents = new JPanel(new BorderLayout());
    contents.add(statusLabel);
    contents.add(controlPanel, BorderLayout.SOUTH);

    root.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
    root.add(contents);
    updateOrientation(StepperPanel.Orientation.HORIZONTAL);

    add(root);
  }

  private void updateOrientation(StepperPanel.Orientation orientation) {
    stepperPanel.setOrientation(orientation);
    // Container#add(...) removes the component from its old BorderLayout position
    String side = orientation == StepperPanel.Orientation.HORIZONTAL
        ? BorderLayout.NORTH
        : BorderLayout.WEST;
    root.add(stepperPanel, side);
    root.revalidate();
    root.repaint();
  }

  private void updateControls() {
    int current = stepperPanel.getCurrentStepIndex();
    int total = stepperPanel.getStepCount();

    prevButton.setEnabled(current > 0);
    nextButton.setEnabled(current < total - 1);

    String label = stepperPanel.getStepLabel(current);
    statusLabel.setText(
        String.format("Current Step: %d / %d (%s)", current + 1, total, label));
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

// Stepper Panel with Animation & Dual Orientation Support
enum StepStatus {
  DONE(Color.DARK_GRAY, "completed"),
  CURRENT(StepIcon.COLOR_PRIMARY, "current step"),
  UPCOMING(Color.GRAY, "not completed");

  private final Color foreground;
  private final String description; // Spoken form of this status

  StepStatus(Color foreground, String description) {
    this.foreground = foreground;
    this.description = description;
  }

  public Color getForeground() {
    return foreground;
  }

  public String getDescription() {
    return description;
  }
}

final class StepperPanel extends JPanel {
  private static final int STEP_ICON_SIZE = 24;
  private static final int ANIM_DELAY_MS = 16;
  private static final long ANIM_DURATION_NS = TimeUnit.MILLISECONDS.toNanos(250);
  private static final Stroke SOLID_CONNECTOR = new BasicStroke(3f);
  private static final Stroke DASHED_CONNECTOR = new BasicStroke(
      2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {6f, 4f}, 0f);

  public enum Orientation {
    HORIZONTAL,
    VERTICAL
  }

  private final List<JLabel> stepLabels = new ArrayList<>();
  private final transient List<StepIcon> stepIcons = new ArrayList<>();
  private final String[] stepTexts;

  // Scratch rectangles reused by #getIconCenterPoint(...)
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  private Orientation orientation = Orientation.HORIZONTAL;
  private int currentStepIndex;

  // Animation Fields
  private float animatedProgress; // Floating point continuous index (e.g., 0.0 -> 1.0)
  private float startProgress;
  private float targetProgress;
  private long animStartTime;
  private final Timer animTimer = new Timer(ANIM_DELAY_MS, e -> updateAnimation());

  /* default */ StepperPanel(String... labels) {
    super();
    // The layout manager depends on the orientation: see #buildComponents()
    this.stepTexts = labels.clone();
    buildComponents();
  }

  @Override public boolean isOpaque() {
    return false;
  }

  public void setOrientation(Orientation orientation) {
    if (this.orientation != orientation) {
      this.orientation = orientation;
      buildComponents();
    }
  }

  // One StepIcon and one JLabel per step are required, so they cannot be hoisted
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void buildComponents() {
    removeAll();
    stepLabels.clear();
    stepIcons.clear();

    boolean horizontal = orientation == Orientation.HORIZONTAL;
    int count = stepTexts.length;
    // GridLayout gives every step the same cell size regardless of the text length
    setLayout(horizontal ? new GridLayout(1, count) : new GridLayout(count, 1));

    // A Border is stateless and can be shared by every label
    Border padding = horizontal
        ? BorderFactory.createEmptyBorder(5, 5, 5, 5)
        : BorderFactory.createEmptyBorder(6, 10, 6, 10);
    int alignment = horizontal ? SwingConstants.CENTER : SwingConstants.LEFT;
    int textPosition = horizontal ? SwingConstants.CENTER : SwingConstants.RIGHT;
    int verticalPosition = horizontal ? SwingConstants.BOTTOM : SwingConstants.CENTER;

    for (int i = 0; i < count; i++) {
      StepIcon icon = new StepIcon(i + 1, STEP_ICON_SIZE);
      // Display only: a JLabel has no model or input handling to disable
      JLabel label = new JLabel(stepTexts[i], icon, alignment);
      label.setBorder(padding);
      label.setHorizontalTextPosition(textPosition);
      label.setVerticalTextPosition(verticalPosition);

      stepLabels.add(label);
      stepIcons.add(icon);
      add(label);
    }
    getAccessibleContext().setAccessibleName("Stepper");
    updateStepStates();
    revalidate();
    repaint();
  }

  public int getCurrentStepIndex() {
    return currentStepIndex;
  }

  public int getStepCount() {
    return stepTexts.length;
  }

  public String getStepLabel(int index) {
    return stepTexts[index];
  }

  public void nextStep() {
    if (currentStepIndex < stepTexts.length - 1) {
      animateToStep(currentStepIndex + 1);
    }
  }

  public void previousStep() {
    if (currentStepIndex > 0) {
      animateToStep(currentStepIndex - 1);
    }
  }

  private void animateToStep(int targetIndex) {
    this.currentStepIndex = targetIndex;
    this.startProgress = animatedProgress;
    this.targetProgress = targetIndex;
    // nanoTime is monotonic and fine grained, unlike currentTimeMillis
    this.animStartTime = System.nanoTime();

    // The step states are discrete: they change here, not on every frame
    updateStepStates();

    if (!animTimer.isRunning()) {
      animTimer.start();
    }
  }

  private void updateAnimation() {
    long elapsed = System.nanoTime() - animStartTime;
    if (elapsed < ANIM_DURATION_NS) {
      float timeFraction = elapsed / (float) ANIM_DURATION_NS;
      // Ease-out cubic formula
      float easedFraction = 1f - (float) Math.pow(1d - timeFraction, 3d);
      animatedProgress = startProgress + (targetProgress - startProgress) * easedFraction;
    } else {
      animatedProgress = targetProgress;
      animTimer.stop();
    }
    repaint();
  }

  private void updateStepStates() {
    int count = stepLabels.size();
    for (int i = 0; i < count; i++) {
      StepStatus status;
      if (i < currentStepIndex) {
        status = StepStatus.DONE;
      } else if (i == currentStepIndex) {
        status = StepStatus.CURRENT;
      } else {
        status = StepStatus.UPCOMING;
      }
      JLabel label = stepLabels.get(i);
      stepIcons.get(i).setStatus(status);
      // Use color only to prevent 1px text metrics jump
      label.setForeground(status.getForeground());
      // The icon and the color are invisible to assistive technology
      label.getAccessibleContext().setAccessibleDescription(
          String.format("Step %d of %d, %s", i + 1, count, status.getDescription()));
    }
    if (count > 0) {
      getAccessibleContext().setAccessibleDescription(String.format(
          "Step %d of %d: %s", currentStepIndex + 1, count, stepTexts[currentStepIndex]));
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    for (int i = 0; i < stepLabels.size() - 1; i++) {
      Point p1 = getIconCenterPoint(stepLabels.get(i));
      Point p2 = getIconCenterPoint(stepLabels.get(i + 1));

      // How much of this segment is completed: 0f(all dashed) - 1f(all solid)
      float fraction = Math.max(0f, Math.min(1f, animatedProgress - i));
      int mx = Math.round(p1.x + (p2.x - p1.x) * fraction);
      int my = Math.round(p1.y + (p2.y - p1.y) * fraction);

      // Solid line for the filled portion
      g2.setColor(StepIcon.COLOR_PRIMARY);
      g2.setStroke(SOLID_CONNECTOR);
      g2.drawLine(p1.x, p1.y, mx, my);

      // Dashed line for the remaining portion
      g2.setColor(StepIcon.COLOR_INACTIVE);
      g2.setStroke(DASHED_CONNECTOR);
      g2.drawLine(mx, my, p2.x, p2.y);
    }
    g2.dispose();
  }

  // Ask Swing where the icon really is instead of guessing it from the insets
  private Point getIconCenterPoint(JLabel label) {
    Insets i = label.getInsets();
    viewRect.setBounds(
        i.left,
        i.top,
        label.getWidth() - i.left - i.right,
        label.getHeight() - i.top - i.bottom);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);
    SwingUtilities.layoutCompoundLabel(
        label,
        label.getFontMetrics(label.getFont()),
        label.getText(),
        label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        label.getIconTextGap());
    return new Point(
        label.getX() + iconRect.x + iconRect.width / 2,
        label.getY() + iconRect.y + iconRect.height / 2);
  }
}

// Java2D Step Icon Implementation with Visually Centered Glyphs
class StepIcon implements Icon {
  public static final Color COLOR_PRIMARY = new Colorr(0x21_96_F3);
  public static final Color COLOR_INACTIVE = new Color(0xD2_D7_DC);
  public static final Color COLOR_CURRENT_BG = Color.WHITE;
  private final int stepNumber;
  private final int size;
  private StepStatus status = StepStatus.UPCOMING;

  protected StepIcon(int stepNumber, int size) {
    this.stepNumber = stepNumber;
    this.size = size;
  }

  public void setStatus(StepStatus status) {
    this.status = status;
  }

  @SuppressWarnings("MissingSwitchDefault")
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    int padding = 2;
    int diameter = size - padding * 2;
    int circleX = x + padding;
    int circleY = y + padding;

    switch (status) {
      case DONE:
        g2.setColor(COLOR_PRIMARY);
        g2.fillOval(circleX, circleY, diameter, diameter);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(
            diameter * .125f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(circleX + diameter / 2d, circleY + diameter / 2d);
        g2.draw(createCheckMark(diameter));
        break;

      case CURRENT:
        g2.setColor(COLOR_CURRENT_BG);
        g2.fillOval(circleX, circleY, diameter, diameter);

        g2.setColor(COLOR_PRIMARY);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(circleX, circleY, diameter, diameter);

        drawCenteredText(g2, circleX, circleY, diameter, COLOR_PRIMARY, true);
        break;

      case UPCOMING:
        g2.setColor(COLOR_CURRENT_BG);
        g2.fillOval(circleX, circleY, diameter, diameter);

        g2.setColor(COLOR_INACTIVE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(circleX, circleY, diameter, diameter);

        drawCenteredText(g2, circleX, circleY, diameter, COLOR_INACTIVE, false);
        break;
    }

    g2.dispose();
  }

  // Check mark centered on (0, 0) and scaled to the circle diameter
  private static Shape createCheckMark(int diameter) {
    Path2D path = new Path2D.Double();
    path.moveTo(-.25 * diameter, 0d);
    path.lineTo(-.05 * diameter, .2 * diameter);
    path.lineTo(.25 * diameter, -.2 * diameter);
    return path;
  }

  private void drawCenteredText(
      Graphics2D g2, int x, int y, int diameter, Color color, boolean bold) {
    g2.setColor(color);
    float fontSize = size * .48f;
    Font font = g2.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, fontSize);
    g2.setFont(font);

    // Use GlyphVector visual bounds to ensure strict optical centering
    String text = Integer.toString(stepNumber);
    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = font.createGlyphVector(frc, text);
    Rectangle2D visualBounds = gv.getVisualBounds();

    double textWidth = visualBounds.getWidth();
    double textHeight = visualBounds.getHeight();

    float textX = (float) (x + (diameter - textWidth) / 2d - visualBounds.getX());
    float textY = (float) (y + (diameter - textHeight) / 2d - visualBounds.getY());
    g2.drawString(text, textX, textY);
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}
