// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    add(new TimePickerSingleField().createMainPanel());
    add(new TimePickerSplitField().createMainPanel());
    setBorder(BorderFactory.createEmptyBorder(20, 2, 20, 2));
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

// A time picker made of two separate hour/minute fields,
// each with its own up/down spinner buttons.
class TimePickerSplitField {
  // Background color of the rounded panel that wraps the hour/minute fields.
  private static final Color PANEL_COLOR = new Color(0xDE_DE_DE);

  public JPanel createMainPanel() {
    JFormattedTextField hourField = createNumberField(12, 1, 0, 23);
    JFormattedTextField minuteField = createNumberField(30, 1, 0, 59);

    JPanel upButtonPanel = new JPanel(new GridLayout(1, 2));
    upButtonPanel.add(createCenteredBox(createArrowButton(hourField, 1, 0, 23)));
    upButtonPanel.add(createCenteredBox(createArrowButton(minuteField, 1, 0, 59)));

    JPanel downButtonPanel = new JPanel(new GridLayout(1, 2));
    downButtonPanel.add(createCenteredBox(createArrowButton(hourField, -1, 0, 23)));
    downButtonPanel.add(createCenteredBox(createArrowButton(minuteField, -1, 0, 59)));

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setOpaque(false);
    panel.add(upButtonPanel, BorderLayout.NORTH);
    panel.add(createTimeFieldPanel(hourField, minuteField));
    panel.add(downButtonPanel, BorderLayout.SOUTH);
    return panel;
  }

  public static JButton createArrowButton(JTextField field, int delta, int min, int max) {
    String arrowLabel = delta > 0 ? "⏶" : "⏷";
    JButton button = new JButton(arrowLabel);
    button.setFocusable(false);
    AutoRepeatHandler handler = new AutoRepeatHandler(field, delta, min, max);
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
  }

  private static Box createCenteredBox(JButton button) {
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalGlue());
    return box;
  }

  private static JPanel createTimeFieldPanel(JTextField hourField, JTextField minuteField) {
    JPanel panel = new RoundPanel(8);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    panel.setOpaque(false);
    panel.setBackground(PANEL_COLOR);
    panel.add(Box.createHorizontalGlue());
    panel.add(hourField);
    JLabel colon = new JLabel(":");
    colon.setFont(colon.getFont().deriveFont(Font.BOLD, 42f));
    colon.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
    panel.add(colon);
    panel.add(minuteField);
    panel.add(Box.createHorizontalGlue());
    return panel;
  }

  public static JFormattedTextField createNumberField(int value, int step, int min, int max) {
    JFormattedTextField field = new RoundFormattedTextField(value, step, min, max);
    try {
      // "##" restricts input to exactly two digits (e.g. "07", "23").
      MaskFormatter mask = new MaskFormatter("##");
      mask.setPlaceholderCharacter('0');
      field.setFormatterFactory(new DefaultFormatterFactory(mask));
    } catch (ParseException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
    }
    field.setFont(field.getFont().deriveFont(Font.BOLD, 42f));
    field.setHorizontalAlignment(JTextField.CENTER);
    field.setColumns(2);
    return field;
  }
}

// A JPanel that paints itself as a filled rounded rectangle using its background color.
class RoundPanel extends JPanel {
  private final int radius;

  protected RoundPanel(int radius) {
    super();
    this.radius = radius;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    int w = getWidth();
    int h = getHeight();
    g2.fill(new RoundRectangle2D.Double(0, 0, w, h, radius, radius));
    g2.setColor(getBackground().darker());
    g2.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, radius, radius));
    g2.dispose();
    super.paintComponent(g);
  }
}

// A two-digit numeric field with a rounded, focus-highlighted background
// and mouse-wheel support.
class RoundFormattedTextField extends JFormattedTextField {
  // Background color used while the field is not focused.
  private static final Color FIELD_COLOR = new Color(0xCE_CE_CE);
  // Fully transparent so the selection itself is invisible;
  // the focus highlight is drawn instead.
  private static final Color NO_SELECTION = new Color(0x0, true);

  private transient Handler handler;
  private final int step;
  private final int min;
  private final int max;

  protected RoundFormattedTextField(int value, int step, int min, int max) {
    super(String.format("%02d", value));
    this.step = step;
    this.min = min;
    this.max = max;
  }

  @Override public void updateUI() {
    // Listeners must be re-created after updateUI() because the UI delegate is replaced.
    removeFocusListener(handler);
    removeMouseWheelListener(handler);
    super.updateUI();
    setFocusable(true);
    setOpaque(false);
    setBackground(FIELD_COLOR);
    setSelectionColor(NO_SELECTION);
    setSelectedTextColor(getForeground());
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    setCaret(new DefaultCaret() {
      @Override public boolean isVisible() {
        return false;
      }
    });
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    handler = new Handler();
    addFocusListener(handler);
    addMouseWheelListener(handler);
  }

  @Override protected void paintComponent(Graphics g) {
    if (hasFocus()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(getBackground());
      g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
      g2.setColor(getBackground().darker());
      g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
      g2.dispose();
    }
    super.paintComponent(g);
  }

  private final class Handler implements FocusListener, MouseWheelListener {
    @Override public void focusGained(FocusEvent e) {
      Component c = e.getComponent();
      c.setForeground(UIManager.getColor("TextField.foreground"));
    }

    @Override public void focusLost(FocusEvent e) {
      e.getComponent().setForeground(Color.DARK_GRAY);
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      int delta = e.getWheelRotation() < 0 ? 1 : -1;
      Component c = e.getComponent();
      if (c instanceof JTextComponent) {
        AutoRepeatHandler.adjust((JTextComponent) c, delta * step, min, max);
      }
    }
  }
}

// Shared button handler that adjusts a numeric field's value,
// repeating while the button is held.
class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final JTextComponent targetField;
  private final int delta;
  private final int min;
  private final int max;
  private JButton pressedButton;

  protected AutoRepeatHandler(JTextComponent targetField, int delta, int min, int max) {
    super();
    this.targetField = targetField;
    this.delta = delta;
    this.min = min;
    this.max = max;
    autoRepeatTimer = new Timer(60, this);
    autoRepeatTimer.setInitialDelay(300);
  }

  public static void adjust(JTextComponent field, int delta, int min, int max) {
    field.requestFocusInWindow();
    int range = max - min + 1;
    int value = Integer.parseInt(field.getText());
    // Wrap around within [min, max] instead of clamping (e.g. 23 + 1 -> 0).
    value = (value - min + delta) % range;
    if (value < 0) {
      value += range;
    }
    value += min;
    field.setText(String.format("%02d", value));
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source instanceof Timer) {
      // The auto-repeat timer keeps firing until the button is released.
      boolean released = pressedButton != null && !pressedButton.getModel().isPressed();
      if (released && autoRepeatTimer.isRunning()) {
        autoRepeatTimer.stop();
      }
    } else if (source instanceof JButton) {
      pressedButton = (JButton) source;
    }
    adjust(targetField, delta, min, max);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
      autoRepeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    autoRepeatTimer.stop();
  }

  @Override public void mouseExited(MouseEvent e) {
    if (autoRepeatTimer.isRunning()) {
      autoRepeatTimer.stop();
    }
  }
}

// A time picker backed by a single "HH:mm" masked field; mouse wheel over the hour
// or minute half adjusts that part independently.
class TimePickerSingleField {
  // Index of the colon in the "HH:mm" mask: caret positions 0-2 are over the hour digits.
  private static final int HOUR_END_INDEX = 2;

  private JFormattedTextField timeField;
  private LocalTime currentTime = LocalTime.of(12, 30);
  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

  public Component createMainPanel() {
    try {
      MaskFormatter mask = new MaskFormatter("##:##");
      mask.setPlaceholderCharacter('0');
      timeField = new JFormattedTextField(mask);
    } catch (ParseException ex) {
      timeField = new JFormattedTextField();
    }

    timeField.setFont(timeField.getFont().deriveFont(Font.BOLD, 42f));
    timeField.setHorizontalAlignment(JTextField.CENTER);
    timeField.setEditable(false);
    timeField.setFocusable(true);
    updateDisplay();

    timeField.addMouseWheelListener(e -> {
      boolean isUp = e.getWheelRotation() < 0;
      // Java 9: isHourSide = timeField.viewToModel2D(e.getPoint()) <= HOUR_END_INDEX;
      boolean isHourSide = timeField.viewToModel(e.getPoint()) <= HOUR_END_INDEX;
      adjustTime(isHourSide, isUp);
    });
    return timeField;
  }

  private void adjustTime(boolean isHour, boolean isUp) {
    if (isHour) {
      currentTime = isUp ? currentTime.plusHours(1) : currentTime.minusHours(1);
    } else {
      currentTime = isUp ? currentTime.plusMinutes(1) : currentTime.minusMinutes(1);
    }
    updateDisplay();
  }

  private void updateDisplay() {
    timeField.setText(currentTime.format(timeFormatter));
  }
}
