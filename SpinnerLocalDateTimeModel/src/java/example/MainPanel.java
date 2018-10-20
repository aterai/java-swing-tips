package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
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
import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(0, 1));
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

        System.out.println(date);
        System.out.println(start);
        System.out.println(end);

        String dateFormat = "yyyy/MM/dd";

        JSpinner spinner0 = new JSpinner(new SpinnerDateModel(date, start, end, Calendar.DAY_OF_MONTH));
        spinner0.setEditor(new JSpinner.DateEditor(spinner0, dateFormat));

        LocalDateTime d = LocalDateTime.now();
        LocalDateTime s = d.minus(2, ChronoUnit.DAYS);
        LocalDateTime e = d.plus(7, ChronoUnit.DAYS);

        System.out.println(d);
        System.out.println(s);
        System.out.println(e);

        JSpinner spinner1 = new JSpinner(new SpinnerDateModel(toDate(d), toDate(s), toDate(e), Calendar.DAY_OF_MONTH));
        spinner1.setEditor(new JSpinner.DateEditor(spinner1, dateFormat));

        JSpinner spinner2 = new JSpinner(new SpinnerLocalDateTimeModel(d, s, e, ChronoUnit.DAYS));
        spinner2.setEditor(new LocalDateTimeEditor(spinner2, dateFormat));

        // JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner2.getEditor();
        // DefaultFormatter formatter = new InternationalFormatter(DateTimeFormatter.ofPattern(dateFormat).toFormat());
        // DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
        // JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
        // ftf.setHorizontalAlignment(SwingConstants.LEFT);
        // ftf.setColumns(10);
        // ftf.setEditable(true);
        // ftf.setFormatterFactory(factory);

        add(makeTitledPanel("SpinnerDateModel", spinner0));
        add(makeTitledPanel("SpinnerDateModel / toInstant", spinner1));
        add(makeTitledPanel("SpinnerLocalDateTimeModel", spinner2));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
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
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    private Comparable<ChronoLocalDateTime<?>> start;
    private Comparable<ChronoLocalDateTime<?>> end;
    private ChronoLocalDateTime<?> value;
    private TemporalUnit temporalUnit;

    @SuppressWarnings("checkstyle:linelength")
    protected SpinnerLocalDateTimeModel(ChronoLocalDateTime<?> value, Comparable<ChronoLocalDateTime<?>> start, Comparable<ChronoLocalDateTime<?>> end, TemporalUnit temporalUnit) {
        super();
        // if (Objects.nonNull(start) && start.compareTo(value) >= 0 || Objects.nonNull(end) && end.compareTo(value) <= 0) {
        //     throw new IllegalArgumentException("(start <= value <= end) is false");
        // }
        this.value = Optional.ofNullable(value).orElseThrow(() -> new IllegalArgumentException("value is null"));
        this.start = start;
        this.end = end;
        this.temporalUnit = temporalUnit;
    }

    public void setStart(Comparable<ChronoLocalDateTime<?>> start) {
        if (Objects.isNull(start) ? Objects.nonNull(this.start) : !Objects.equals(start, this.start)) {
            this.start = start;
            fireStateChanged();
        }
    }

    public Comparable<ChronoLocalDateTime<?>> getStart() {
        return start;
    }

    public void setEnd(Comparable<ChronoLocalDateTime<?>> end) {
        if (Objects.isNull(end) ? Objects.nonNull(this.end) : !Objects.equals(end, this.end)) {
            this.end = end;
            fireStateChanged();
        }
    }

    public Comparable<ChronoLocalDateTime<?>> getEnd() {
        return end;
    }

    public void setTemporalUnit(TemporalUnit temporalUnit) {
        if (temporalUnit != this.temporalUnit) {
            this.temporalUnit = temporalUnit;
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
        return value;
    }

    @Override public void setValue(Object value) {
        if (!(value instanceof ChronoLocalDateTime<?>)) {
            throw new IllegalArgumentException("illegal value");
        }
        if (!value.equals(this.value)) {
            this.value = (ChronoLocalDateTime<?>) value;
            fireStateChanged();
        }
    }
}

class LocalDateTimeEditor extends JSpinner.DefaultEditor {
    protected final transient DateTimeFormatter dateTimeFormatter;
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
            JFormattedTextField ftf = (JFormattedTextField) getTextField();
            try {
                String maxString = formatter.valueToString(model.getStart());
                String minString = formatter.valueToString(model.getEnd());
                ftf.setColumns(Math.max(maxString.length(), minString.length()));
            } catch (ParseException ex) {
                // PENDING: hmuller
                ex.printStackTrace();
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
        @Override public String valueToString(Object value) throws ParseException {
            // System.out.println(value.getClass().getName());
            if (value instanceof TemporalAccessor) {
                // return ((LocalDateTime) value).format(dateTimeFormatter);
                return dateTimeFormatter.format((TemporalAccessor) value);
            } else {
                return "";
            }
        }
        @SuppressWarnings("PMD.CyclomaticComplexity")
        @Override public Object stringToValue(String text) throws ParseException {
            // System.out.println("stringToValue:" + text);
            SpinnerLocalDateTimeModel m = getModel();
            try {
                // LocalDateTime value = LocalDate.parse(text, dateTimeFormatter).atStartOfDay();
                TemporalAccessor ta = dateTimeFormatter.parse(text);
                ChronoLocalDateTime<?> value = m.getLocalDateTime();
                // @see https://tips4java.wordpress.com/2015/04/09/temporal-spinners/
                for (ChronoField field: ChronoField.values()) {
                    if (field.isSupportedBy(value) && ta.isSupported(field)) {
                        value = field.adjustInto(value, ta.getLong(field));
                    }
                }
                Comparable<ChronoLocalDateTime<?>> min = m.getStart();
                Comparable<ChronoLocalDateTime<?>> max = m.getEnd();
                if (Objects.nonNull(min) && min.compareTo(value) > 0) {
                    throw new ParseException(text + " is out of range", 0);
                } else if (Objects.nonNull(max) && max.compareTo(value) < 0) {
                    throw new ParseException(text + " is out of range", 0);
                }
                return value;
            } catch (DateTimeParseException ex) {
                throw (ParseException) new ParseException(ex.getMessage(), ex.getErrorIndex()).initCause(ex);
            }
        }
    }
}
