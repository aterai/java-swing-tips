// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

public final class MainPanel extends JPanel {
  private static final double INITIAL_VALUE = 8.85;
  private static final double MIN_VALUE = 8.0;
  private static final double MAX_VALUE = 72.0;
  private static final double STEP_SIZE = 0.5;

  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JSpinner defaultSpinner = createSpinner(
        new SpinnerNumberModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        null
    );
    JSpinner downModelSpinner = createSpinner(
        new RoundDownToHalfSpinnerModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        null
    );
    JSpinner downFormatSpinner = createSpinner(
        new SpinnerNumberModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        new HalfFormatter(RoundingMode.DOWN, MIN_VALUE, MAX_VALUE)
    );
    JSpinner halfUpSpinner = createSpinner(
        new SpinnerNumberModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        new HalfFormatter(RoundingMode.HALF_UP, MIN_VALUE, MAX_VALUE)
    );

    JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
    p.add(createTitledPanel("Default, stepSize: 0.5", defaultSpinner));
    p.add(createTitledPanel("Override SpinnerNumberModel", downModelSpinner));
    p.add(createTitledPanel("Round down to half Formatter", downFormatSpinner));
    p.add(createTitledPanel("Round to half Formatter", halfUpSpinner));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private JSpinner createSpinner(SpinnerNumberModel model, DefaultFormatter formatter) {
    JSpinner spinner = new JSpinner(model);
    if (formatter != null) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
      appendRoundedValue(formatter, model);
    }
    return spinner;
  }

  private void appendRoundedValue(DefaultFormatter formatter, SpinnerNumberModel model) {
    try {
      String valueText = model.getNumber().toString();
      Object roundedValue = formatter.stringToValue(valueText);
      textArea.append(String.format("%s -> %s%n", valueText, roundedValue));
    } catch (ParseException ex) {
      textArea.append(String.format("Parse error: %s%n", ex.getMessage()));
    }
  }

  private static Component createTitledPanel(String title, Component component) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    panel.add(component, c);
    return panel;
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

final class HalfFormatter extends DefaultFormatter {
  private final RoundingMode roundingMode;
  private final double minimum;
  private final double maximum;

  /* default */ HalfFormatter(RoundingMode roundingMode, double minimum, double maximum) {
    super();
    this.roundingMode = roundingMode;
    this.minimum = minimum;
    this.maximum = maximum;
    // DefaultFormatter overwrites typed characters by default, unlike NumberFormatter
    setOverwriteMode(false);
  }

  @Override public Object stringToValue(String text) throws ParseException {
    BigDecimal value;
    try {
      value = new BigDecimal(text.trim());
    } catch (NumberFormatException ex) {
      // DefaultFormatter must report invalid text as a ParseException
      // so that JFormattedTextField can revert the edit
      ParseException pe = new ParseException("Invalid number: " + text, 0);
      pe.initCause(ex);
      throw pe;
    }
    double rounded = RoundDownToHalfSpinnerModel.roundToHalf(value, roundingMode).doubleValue();
    // This formatter replaces the NumberEditor's one, which checks the bounds of the model
    if (rounded < minimum || rounded > maximum) {
      throw new ParseException("Out of range: " + text, 0);
    }
    return rounded;
  }

  @Override public String valueToString(Object value) throws ParseException {
    if (!(value instanceof Number)) {
      throw new ParseException("value is not a Number: " + value, 0);
    }
    double doubleValue = ((Number) value).doubleValue();
    return RoundDownToHalfSpinnerModel.roundToHalf(
        BigDecimal.valueOf(doubleValue), roundingMode).toString();
  }
}

class RoundDownToHalfSpinnerModel extends SpinnerNumberModel {
  public RoundDownToHalfSpinnerModel(double value, double min, double max, double step) {
    super(roundDownToHalf(value), min, max, step);
  }

  @Override public void setValue(Object value) {
    Double roundedValue = roundDownToHalf(requireNumber(value).doubleValue());
    if (roundedValue.equals(getValue())) {
      if (!roundedValue.equals(value)) {
        // The value is unchanged, but the editor still displays the unrounded text
        // (e.g. 8.85 when the value is 8.5), so notify it to redisplay the current value
        fireStateChanged();
      }
    } else {
      // SpinnerNumberModel#setValue(...) fires a ChangeEvent by itself
      super.setValue(roundedValue);
    }
  }

  private static Number requireNumber(Object value) {
    if (value instanceof Number) {
      return (Number) value;
    }
    throw new IllegalArgumentException("Value must be a Number: " + value);
  }

  private static double roundDownToHalf(double value) {
    return roundToHalf(BigDecimal.valueOf(value), RoundingMode.DOWN).doubleValue();
  }

  // Round to a multiple of 0.5: double the value, round it to an integer
  // with the given RoundingMode, and then halve it.
  public static BigDecimal roundToHalf(BigDecimal value, RoundingMode roundingMode) {
    return value.multiply(BigDecimal.valueOf(2))
        .setScale(0, roundingMode)
        .multiply(BigDecimal.valueOf(0.5));
  }
}
