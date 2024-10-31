// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(new JLabel("SpinnerNumberModel(double, ...)"));
    box.add(Box.createVerticalStrut(2));

    JPanel p1 = new JPanel(new GridLayout(1, 2, 5, 5));
    p1.add(new JSpinner(new SpinnerNumberModel(2.01, 2.00, 3.02, .01)));
    p1.add(new JSpinner(new SpinnerNumberModel(29.7, 29.6, 30.2, .1)));
    box.add(p1);
    box.add(Box.createVerticalStrut(5));
    box.add(new JLabel("BigDecimalSpinnerModel"));
    box.add(Box.createVerticalStrut(2));

    JPanel p2 = new JPanel(new GridLayout(1, 2, 5, 5));
    p2.add(new JSpinner(new BigDecimalSpinnerModel(2.01, 2.00, 3.02, .01)));
    p2.add(new JSpinner(new BigDecimalSpinnerModel(29.7, 29.6, 30.2, .1)));
    box.add(p2);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // TEST:
    double d = 29.7 - 29.6 - .1;
    String s1 = String.format("29.7-29.6-0.1>=0:%b%n", d >= 0.0);
    String s2 = String.format("abs(29.7-29.6-0.1)<1.0e-14:%b%n", Math.abs(d) < 1.0e-14);
    String s3 = String.format("abs(29.7-29.6-0.1)<1.0e-15:%b%n", Math.abs(d) < 1.0e-15);

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea(s1 + s2 + s3)));
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
      ex.printStackTrace();
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

class BigDecimalSpinnerModel extends SpinnerNumberModel {
  protected BigDecimalSpinnerModel(double value, double min, double max, double stepSize) {
    super(value, min, max, stepSize);
  }

  @Override public Object getPreviousValue() {
    return incrValue2(-1);
  }

  @Override public Object getNextValue() {
    return incrValue2(+1);
  }

  // @see SpinnerNumberModel#incrValue(int dir)
  private Number incrValue2(int dir) {
    BigDecimal value = BigDecimal.valueOf((Double) getNumber());
    BigDecimal stepSize = BigDecimal.valueOf((Double) getStepSize());
    BigDecimal newVal = dir > 0 ? value.add(stepSize) : value.subtract(stepSize);
    BigDecimal max = BigDecimal.valueOf((Double) getMaximum());
    BigDecimal min = BigDecimal.valueOf((Double) getMinimum());
    return max.compareTo(newVal) < 0 || min.compareTo(newVal) > 0 ? null : newVal;
  }
}
