// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Calendar cal = Calendar.getInstance();
    cal.clear(Calendar.MILLISECOND);
    cal.clear(Calendar.SECOND);
    cal.clear(Calendar.MINUTE);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    Date date = cal.getTime();
    cal.add(Calendar.DATE, -2);
    Date start = cal.getTime();
    cal.add(Calendar.DATE, 9);
    Date end = cal.getTime();

    JTextArea info = new JTextArea();
    info.append(date + "\n");
    info.append(start + "\n");
    info.append(end + "\n");

    String dateFormat = "yyyy/MM/dd";

    JSpinner spinner0 = new JSpinner(makeSpinnerDateModel(date, start, end));
    spinner0.setEditor(new JSpinner.DateEditor(spinner0, dateFormat));

    LocalDateTime d = LocalDateTime.now(ZoneId.systemDefault());
    // LocalDateTime s = d.minus(2, ChronoUnit.DAYS);
    LocalDateTime s = d.minusDays(2);
    // LocalDateTime e = d.plus(7, ChronoUnit.DAYS);
    LocalDateTime e = d.plusDays(7);

    info.append(d + "\n");
    info.append(s + "\n");
    info.append(e + "\n");

    JSpinner spinner1 = new JSpinner(makeSpinnerDateModel(toDate(d), toDate(s), toDate(e)));
    spinner1.setEditor(new JSpinner.DateEditor(spinner1, dateFormat));

    JSpinner spinner2 = new JSpinner(new SpinnerLocalDateTimeModel(d, s, e, ChronoUnit.DAYS));
    spinner2.setEditor(new LocalDateTimeEditor(spinner2, dateFormat));

    // JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner2.getEditor();
    // Format format = DateTimeFormatter.ofPattern(dateFormat).toFormat();
    // DefaultFormatter formatter = new InternationalFormatter(format);
    // DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
    // JFormattedTextField ftf = editor.getTextField();
    // ftf.setHorizontalAlignment(SwingConstants.LEFT);
    // ftf.setColumns(10);
    // ftf.setEditable(true);
    // ftf.setFormatterFactory(factory);

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(makeTitledPanel("SpinnerDateModel", spinner0));
    p.add(makeTitledPanel("SpinnerDateModel / toInstant", spinner1));
    p.add(makeTitledPanel("SpinnerLocalDateTimeModel", spinner2));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(info));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static SpinnerDateModel makeSpinnerDateModel(Date date, Date start, Date end) {
    return new SpinnerDateModel(date, start, end, Calendar.DAY_OF_MONTH);
  }

  private static Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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

class SpinnerLocalDateTimeModel extends AbstractSpinnerModel {
  private transient Comparable<ChronoLocalDateTime<?>> start;
  private transient Comparable<ChronoLocalDateTime<?>> end;
  private transient ChronoLocalDateTime<?> value;
  private transient TemporalUnit temporalUnit;

  protected SpinnerLocalDateTimeModel(
      ChronoLocalDateTime<?> value,
      Comparable<ChronoLocalDateTime<?>> start,
      Comparable<ChronoLocalDateTime<?>> end,
      TemporalUnit temporalUnit) {
    super();
    // if (Objects.nonNull(start) && start.compareTo(value) >= 0 ||
    //     Objects.nonNull(end) && end.compareTo(value) <= 0) {
    //   throw new IllegalArgumentException("(start <= value <= end) is false");
    // }
    this.value = Optional.ofNullable(value)
        .orElseThrow(() -> new IllegalArgumentException("value is null"));
    this.start = start;
    this.end = end;
    this.temporalUnit = temporalUnit;
  }

  public void setStart(Comparable<ChronoLocalDateTime<?>> startDate) {
    boolean b = Optional.ofNullable(startDate)
        .map(s -> !Objects.equals(s, start))
        .orElse(Objects.nonNull(start));
    if (b) {
      start = startDate;
      fireStateChanged();
    }
  }

  public Comparable<ChronoLocalDateTime<?>> getStart() {
    return start;
  }

  public void setEnd(Comparable<ChronoLocalDateTime<?>> endDate) {
    boolean b = Optional.ofNullable(endDate)
        .map(e -> !Objects.equals(e, end))
        .orElse(Objects.nonNull(end));
    if (b) {
      end = endDate;
      fireStateChanged();
    }
  }

  public Comparable<ChronoLocalDateTime<?>> getEnd() {
    return end;
  }

  public void setTemporalUnit(TemporalUnit unit) {
    if (Objects.equals(unit, temporalUnit)) {
      temporalUnit = unit;
      fireStateChanged();
    }
  }

  public TemporalUnit getTemporalUnit() {
    return temporalUnit;
  }

  @Override public Object getNextValue() {
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(value.getTime());
    // cal.add(calendarField, 1);
    // Date next = cal.getTime();
    ChronoLocalDateTime<?> next = value.plus(1, temporalUnit);
    return Objects.isNull(end) || end.compareTo(next) >= 0 ? next : null;
  }

  @Override public Object getPreviousValue() {
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(value.getTime());
    // cal.add(calendarField, -1);
    // Date prev = cal.getTime();
    ChronoLocalDateTime<?> prev = value.minus(1, temporalUnit);
    return Objects.isNull(start) || start.compareTo(prev) <= 0 ? prev : null;
  }

  public ChronoLocalDateTime<?> getLocalDateTime() {
    return value;
  }

  @Override public Object getValue() {
    return getLocalDateTime();
  }

  @Override public void setValue(Object o) {
    if (!(o instanceof ChronoLocalDateTime<?>)) {
      throw new IllegalArgumentException("illegal o");
    }
    if (!o.equals(value)) {
      value = (ChronoLocalDateTime<?>) o;
      fireStateChanged();
    }
  }

  @Override public String toString() {
    return "LocalDateTime SpinnerModel";
  }
}

class LocalDateTimeEditor extends JSpinner.DefaultEditor {
  private final transient DateTimeFormatter dateTimeFormatter;
  private final SpinnerLocalDateTimeModel model;

  protected LocalDateTimeEditor(JSpinner spinner, String dateFormatPattern) {
    super(spinner);
    if (!(spinner.getModel() instanceof SpinnerLocalDateTimeModel)) {
      throw new IllegalArgumentException("model not a SpinnerLocalDateTimeModel");
    }
    dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
    model = (SpinnerLocalDateTimeModel) spinner.getModel();
    DefaultFormatter formatter = new LocalDateTimeFormatter();

    EventQueue.invokeLater(() -> {
      formatter.setValueClass(LocalDateTime.class);
      JFormattedTextField ftf = getTextField();
      try {
        String maxString = formatter.valueToString(model.getStart());
        String minString = formatter.valueToString(model.getEnd());
        ftf.setColumns(Math.max(maxString.length(), minString.length()));
      } catch (ParseException ex) {
        // PENDING: hmuller
        UIManager.getLookAndFeel().provideErrorFeedback(ftf);
      }
      ftf.setHorizontalAlignment(SwingConstants.LEFT);
      ftf.setEditable(true);
      ftf.setFormatterFactory(new DefaultFormatterFactory(formatter));
    });
  }

  public SpinnerLocalDateTimeModel getModel() {
    return model; // (SpinnerLocalDateTimeModel) getSpinner().getModel();
  }

  protected class LocalDateTimeFormatter extends InternationalFormatter {
    protected LocalDateTimeFormatter() {
      super(dateTimeFormatter.toFormat());
    }

    @Override public String valueToString(Object value) {
      // System.out.println(value.getClass().getName());
      String str = "";
      if (value instanceof TemporalAccessor) {
        // str = ((LocalDateTime) value).format(dateTimeFormatter);
        str = dateTimeFormatter.format((TemporalAccessor) value);
      }
      return str;
    }

    @Override public Object stringToValue(String text) throws ParseException {
      // System.out.println("stringToValue:" + text);
      SpinnerLocalDateTimeModel m = getModel();
      try {
        // LocalDateTime value = LocalDate.parse(text, dateTimeFormatter).atStartOfDay();
        TemporalAccessor ta = dateTimeFormatter.parse(text);
        ChronoLocalDateTime<?> value = m.getLocalDateTime();
        // @see https://tips4java.wordpress.com/2015/04/09/temporal-spinners/
        for (ChronoField field : ChronoField.values()) {
          if (field.isSupportedBy(value) && ta.isSupported(field)) {
            value = field.adjustInto(value, ta.getLong(field));
          }
        }
        if (checkMinMax(value, m)) {
          throw new ParseException(text + " is out of range", 0);
        }
        return value;
      } catch (DateTimeParseException ex) {
        String msg = ex.getMessage();
        int idx = ex.getErrorIndex();
        throw (ParseException) new ParseException(msg, idx).initCause(ex);
      }
    }

    private boolean checkMinMax(ChronoLocalDateTime<?> value, SpinnerLocalDateTimeModel m) {
      Comparable<ChronoLocalDateTime<?>> min = m.getStart();
      Comparable<ChronoLocalDateTime<?>> max = m.getEnd();
      boolean a = Objects.nonNull(min) && min.compareTo(value) > 0;
      boolean b = Objects.nonNull(max) && max.compareTo(value) < 0;
      return a || b;
    }
  }
}
