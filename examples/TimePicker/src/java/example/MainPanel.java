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
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    add(new TimePickerSingleField().createComponent());
    add(new TimePickerSplitField().createComponent());
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
  private static final Color PANEL_BACKGROUND = new Color(0xDE_DE_DE);

  public JPanel createComponent() {
    RoundFormattedTextField hourField = createNumberField(12, 1, 0, 23);
    RoundFormattedTextField minuteField = createNumberField(30, 1, 0, 59);

    JPanel upButtonPanel = new JPanel(new GridLayout(1, 2));
    upButtonPanel.add(createCenteredBox(createArrowButton(hourField, 1)));
    upButtonPanel.add(createCenteredBox(createArrowButton(minuteField, 1)));

    JPanel downButtonPanel = new JPanel(new GridLayout(1, 2));
    downButtonPanel.add(createCenteredBox(createArrowButton(hourField, -1)));
    downButtonPanel.add(createCenteredBox(createArrowButton(minuteField, -1)));

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setOpaque(false);
    panel.add(upButtonPanel, BorderLayout.NORTH);
    panel.add(createTimeFieldPanel(hourField, minuteField));
    panel.add(downButtonPanel, BorderLayout.SOUTH);
    return panel;
  }

  // Creates an up/down button that moves the field by one step,
  // repeating while the button is held down.
  private static JButton createArrowButton(RoundFormattedTextField field, int direction) {
    String arrowLabel = direction > 0 ? "⏶" : "⏷";
    JButton button = new JButton(arrowLabel);
    button.setFocusable(false);
    AutoRepeatHandler handler = new AutoRepeatHandler(() -> field.adjustValue(direction));
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
    panel.setBackground(PANEL_BACKGROUND);
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

  private static RoundFormattedTextField createNumberField(
      int value, int step, int min, int max) {
    RoundFormattedTextField field = new RoundFormattedTextField(value, step, min, max);
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
  private final int arc;

  protected RoundPanel(int arc) {
    super();
    this.arc = arc;
  }

  @Override protected void paintComponent(Graphics g) {
    paintRoundRect(g, this, arc);
    super.paintComponent(g);
  }

  // Fills the component bounds with its background color
  // and outlines it with a darker shade of the same color.
  public static void paintRoundRect(Graphics g, Component c, int arc) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = c.getWidth();
    int h = c.getHeight();
    g2.setColor(c.getBackground());
    g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));
    g2.setColor(c.getBackground().darker());
    g2.draw(new RoundRectangle2D.Double(0, 0, w - 1d, h - 1d, arc, arc));
    g2.dispose();
  }
}

// A two-digit numeric field with a rounded, focus-highlighted background
// and mouse-wheel support. The value wraps around within [min, max].
class RoundFormattedTextField extends JFormattedTextField {
  // Background color used while the field is focused.
  private static final Color FIELD_BACKGROUND = new Color(0xCE_CE_CE);
  // Fully transparent so the selection itself is invisible;
  // the focus highlight is drawn instead.
  private static final Color TRANSPARENT = new Color(0x0, true);
  private static final int ARC = 8;

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
    setOpaque(false);
    setBackground(FIELD_BACKGROUND);
    // Dimmed until the field gains the focus; see Handler#focusGained
    setForeground(Color.DARK_GRAY);
    setSelectionColor(TRANSPARENT);
    setSelectedTextColor(UIManager.getColor("TextField.foreground"));
    setBorder(BorderFactory.createEmptyBorder());
    setCaret(new DefaultCaret() {
      @Override public boolean isVisible() {
        return false;
      }
    });
    setCursor(Cursor.getDefaultCursor());
    handler = new Handler();
    addFocusListener(handler);
    addMouseWheelListener(handler);
  }

  // Moves the value by the given number of steps, wrapping around
  // within [min, max] instead of clamping (e.g. 23 + 1 -> 0).
  public void adjustValue(int steps) {
    requestFocusInWindow();
    int range = max - min + 1;
    int value = Integer.parseInt(getText());
    int next = Math.floorMod(value - min + steps * step, range) + min;
    setText(String.format("%02d", next));
  }

  @Override protected void paintComponent(Graphics g) {
    if (hasFocus()) {
      RoundPanel.paintRoundRect(g, this, ARC);
    }
    super.paintComponent(g);
  }

  private final class Handler implements FocusListener, MouseWheelListener {
    @Override public void focusGained(FocusEvent e) {
      setForeground(UIManager.getColor("TextField.foreground"));
    }

    @Override public void focusLost(FocusEvent e) {
      setForeground(Color.DARK_GRAY);
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      // Rotating the wheel away from the user (negative) increases the value
      adjustValue(-e.getWheelRotation());
    }
  }
}

// Runs an action when a button is clicked and keeps repeating it
// while the button is held down, like the arrow buttons of a JSpinner.
class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final Runnable action;
  private AbstractButton arrowButton;

  protected AutoRepeatHandler(Runnable action) {
    super();
    this.action = action;
    autoRepeatTimer = new Timer(60, this);
    autoRepeatTimer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    // The button itself fires once on release; the timer fires while held.
    boolean released = e.getSource() instanceof Timer
        && !arrowButton.getModel().isPressed();
    if (released) {
      // Safety net: stop repeating if the button was released
      // without this handler receiving mouseReleased.
      autoRepeatTimer.stop();
    } else {
      action.run();
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    if (SwingUtilities.isLeftMouseButton(e) && c.isEnabled() && c instanceof AbstractButton) {
      arrowButton = (AbstractButton) c;
      autoRepeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    autoRepeatTimer.stop();
  }

  @Override public void mouseExited(MouseEvent e) {
    autoRepeatTimer.stop();
  }
}

// A time picker backed by a single "HH:mm" masked field; mouse wheel over the hour
// or minute half adjusts that part independently.
class TimePickerSingleField {
  // Index of the colon in the "HH:mm" mask: caret positions 0-2 are over the hour digits.
  private static final int HOUR_END_INDEX = 2;
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private LocalTime currentTime = LocalTime.of(12, 30);

  public JFormattedTextField createComponent() {
    JFormattedTextField field = createMaskedField("##:##");
    field.setFont(field.getFont().deriveFont(Font.BOLD, 42f));
    field.setHorizontalAlignment(JTextField.CENTER);
    field.setEditable(false);
    field.setText(currentTime.format(TIME_FORMATTER));
    field.addMouseWheelListener(e -> {
      // Rotating the wheel away from the user (negative) increases the value
      int steps = -e.getWheelRotation();
      // Java 9: boolean isHourSide = field.viewToModel2D(e.getPoint()) <= HOUR_END_INDEX;
      boolean isHourSide = field.viewToModel(e.getPoint()) <= HOUR_END_INDEX;
      // Unlike TimePickerSplitField, the minutes carry over into the hours (12:59 -> 13:00)
      currentTime = isHourSide ? currentTime.plusHours(steps) : currentTime.plusMinutes(steps);
      field.setText(currentTime.format(TIME_FORMATTER));
    });
    return field;
  }

  private static JFormattedTextField createMaskedField(String pattern) {
    JFormattedTextField field;
    try {
      MaskFormatter mask = new MaskFormatter(pattern);
      mask.setPlaceholderCharacter('0');
      field = new JFormattedTextField(mask);
    } catch (ParseException ex) {
      field = new JFormattedTextField();
    }
    return field;
  }
}
