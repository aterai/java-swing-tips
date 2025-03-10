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
  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(8.85, 8.0, 72.0, .5));
    JSpinner spinner1 = new JSpinner(new RoundToHalfSpinnerModel(8.85, 8.0, 72.0, .5));
    JSpinner spinner2 = makeSpinner(makeDownFormatter());
    JSpinner spinner3 = makeSpinner(makeUpFormatter());
    JPanel p = new JPanel(new GridLayout(0, 2));
    p.add(makeTitledPanel("Default, stepSize: 0.5", spinner0));
    p.add(makeTitledPanel("Override SpinnerNumberModel", spinner1));
    p.add(makeTitledPanel("Round down to half Formatter", spinner2));
    p.add(makeTitledPanel("Round up to half Formatter", spinner3));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private JSpinner makeSpinner(DefaultFormatter formatter) {
    SpinnerNumberModel model = new SpinnerNumberModel(8.85, 8.0, 72.0, .5);
    JSpinner spinner = new JSpinner(model);
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
    JFormattedTextField ftf = editor.getTextField();
    ftf.setFormatterFactory(new DefaultFormatterFactory(formatter));
    info(formatter, model);
    return spinner;
  }

  private void info(DefaultFormatter formatter, SpinnerNumberModel model) {
    try {
      String v1 = model.getNumber().toString();
      Object v2 = formatter.stringToValue(v1);
      textArea.append(String.format("%s -> %s%n", v1, v2));
    } catch (ParseException ex) {
      textArea.append(String.format("%s%n", ex.getMessage()));
    }
  }

  private static DefaultFormatter makeDownFormatter() {
    return new DefaultFormatter() {
      @Override public Object stringToValue(String text) {
        return roundToDown(new BigDecimal(text)).doubleValue();
      }

      @Override public String valueToString(Object value) {
        return roundToDown(BigDecimal.valueOf((Double) value)).toString();
      }
    };
  }

  private static BigDecimal roundToDown(BigDecimal value) {
    return value.multiply(BigDecimal.valueOf(2))
        .setScale(0, RoundingMode.DOWN)
        .multiply(BigDecimal.valueOf(.5));
  }

  private static DefaultFormatter makeUpFormatter() {
    return new DefaultFormatter() {
      @Override public Object stringToValue(String text) {
        return roundToUp(new BigDecimal(text)).doubleValue();
      }

      @Override public String valueToString(Object value) {
        return roundToUp(BigDecimal.valueOf((Double) value)).toString();
      }
    };
  }

  private static BigDecimal roundToUp(BigDecimal value) {
    return value.multiply(BigDecimal.valueOf(2))
        .setScale(0, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(.5));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
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
    if (value instanceof Double) {
      Double v = roundDownToHalf((Double) value);
      if (!v.equals(getValue())) {
        super.setValue(v);
        fireStateChanged();
      }
    } else {
      throw new IllegalArgumentException("illegal value");
    }
  }

  private static double roundDownToHalf(Double value) {
    return BigDecimal.valueOf(value)
        .multiply(BigDecimal.valueOf(2))
        .setScale(0, RoundingMode.DOWN)
        .multiply(BigDecimal.valueOf(.5))
        .doubleValue();
  }
}
