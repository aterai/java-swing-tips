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
        new RoundToHalfSpinnerModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        null
    );
    JSpinner downFmtSpinner = createSpinner(
        new SpinnerNumberModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        createHalfFormatter(RoundingMode.DOWN)
    );
    JSpinner halfUpFmtSpinner = createSpinner(
        new SpinnerNumberModel(INITIAL_VALUE, MIN_VALUE, MAX_VALUE, STEP_SIZE),
        createHalfFormatter(RoundingMode.HALF_UP)
    );

    JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
    p.add(createTitledPanel("Default, stepSize: 0.5", defaultSpinner));
    p.add(createTitledPanel("Override SpinnerNumberModel", downModelSpinner));
    p.add(createTitledPanel("Round down to half Formatter", downFmtSpinner));
    p.add(createTitledPanel("Round to half Formatter", halfUpFmtSpinner));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private JSpinner createSpinner(SpinnerNumberModel model, DefaultFormatter formatter) {
    JSpinner spinner = new JSpinner(model);
    if (formatter != null) {
      JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
      editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
      info(formatter, model);
    }
    return spinner;
  }

  private void info(DefaultFormatter formatter, SpinnerNumberModel model) {
    try {
      String valueText = model.getNumber().toString();
      Object roundedValue = formatter.stringToValue(valueText);
      textArea.append(String.format("%s -> %s%n", valueText, roundedValue));
    } catch (ParseException ex) {
      textArea.append(String.format("Parse error: %s%n", ex.getMessage()));
    }
  }

  private static DefaultFormatter createHalfFormatter(RoundingMode roundingMode) {
    return new DefaultFormatter() {
      @Override public Object stringToValue(String text) {
        return roundToHalf(new BigDecimal(text), roundingMode).doubleValue();
      }

      @Override public String valueToString(Object value) throws ParseException {
        if (!(value instanceof Number)) {
          throw new ParseException("value is not a Number: " + value, 0);
        }
        double doubleValue = ((Number) value).doubleValue();
        return roundToHalf(BigDecimal.valueOf(doubleValue), roundingMode).toString();
      }
    };
  }

  private static BigDecimal roundToHalf(BigDecimal value, RoundingMode roundingMode) {
    return value.multiply(BigDecimal.valueOf(2))
        .setScale(0, roundingMode)
        .multiply(BigDecimal.valueOf(0.5));
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

class RoundToHalfSpinnerModel extends SpinnerNumberModel {
  protected RoundToHalfSpinnerModel(double value, double min, double max, double step) {
    super(roundDownToHalf(value), min, max, step);
  }

  @Override public void setValue(Object value) {
    Number number = requireNumber(value);
    Double roundedValue = roundDownToHalf(number.doubleValue());
    if (!roundedValue.equals(getValue())) {
      super.setValue(roundedValue);
      fireStateChanged();
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

  public static BigDecimal roundToHalf(BigDecimal value, RoundingMode roundingMode) {
    return value.multiply(BigDecimal.valueOf(2))
        .setScale(0, roundingMode)
        .multiply(BigDecimal.valueOf(0.5));
  }
}
