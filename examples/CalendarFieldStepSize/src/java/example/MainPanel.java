// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.clear(Calendar.MINUTE);
    calendar.clear(Calendar.SECOND);
    calendar.clear(Calendar.MILLISECOND);
    Date d = calendar.getTime();

    SimpleDateFormat format = new SimpleDateFormat("mm:ss, SSS", Locale.getDefault());
    DefaultFormatterFactory factory = new DefaultFormatterFactory(new DateFormatter(format));

    SpinnerDateModel model1 = new SpinnerDateModel(d, null, null, Calendar.SECOND);
    JSpinner spinner1 = new JSpinner(model1);
    ((JSpinner.DefaultEditor) spinner1.getEditor()).getTextField().setFormatterFactory(factory);

    SpinnerDateModel model2 = makeSpinnerDateModel(d);
    JSpinner spinner2 = new JSpinner(model2);
    ((JSpinner.DefaultEditor) spinner2.getEditor()).getTextField().setFormatterFactory(factory);

    add(makeTitledPanel("Default SpinnerDateModel", spinner1));
    add(makeTitledPanel("Override SpinnerDateModel#getNextValue(...)", spinner2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static SpinnerDateModel makeSpinnerDateModel(Date d) {
    Map<Integer, Integer> stepSizeMap = new ConcurrentHashMap<>();
    stepSizeMap.put(Calendar.HOUR_OF_DAY, 1);
    stepSizeMap.put(Calendar.MINUTE, 1);
    stepSizeMap.put(Calendar.SECOND, 30);
    stepSizeMap.put(Calendar.MILLISECOND, 500);

    return new SpinnerDateModel(d, null, null, Calendar.SECOND) {
      @Override public Object getPreviousValue() {
        return getDateValue(-1);
      }

      @Override public Object getNextValue() {
        return getDateValue(1);
      }

      private Date getDateValue(int dir) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate());
        int calendarField = getCalendarField();
        int stepSize = Optional.ofNullable(stepSizeMap.get(calendarField)).orElse(1);
        cal.add(calendarField, dir * stepSize);
        return cal.getTime();
      }
    };
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
