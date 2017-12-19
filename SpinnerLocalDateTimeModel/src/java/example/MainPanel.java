package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.text.*;
import java.time.*;
import java.time.chrono.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    public MainPanel() {
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

        JSpinner spinner0 = new JSpinner(new SpinnerDateModel(date, start, end, Calendar.DAY_OF_MONTH));
        spinner0.setEditor(new JSpinner.DateEditor(spinner0, DATE_FORMAT_PATTERN));

        LocalDateTime d = LocalDateTime.now();
        LocalDateTime s = d.minus(2, ChronoUnit.DAYS);
        LocalDateTime e = d.plus(7, ChronoUnit.DAYS);

        System.out.println(d);
        System.out.println(s);
        System.out.println(e);

        JSpinner spinner1 = new JSpinner(new SpinnerDateModel(toDate(d), toDate(s), toDate(e), Calendar.DAY_OF_MONTH));
        spinner1.setEditor(new JSpinner.DateEditor(spinner1, DATE_FORMAT_PATTERN));

        JSpinner spinner2 = new JSpinner(new SpinnerLocalDateTimeModel(d, s, e, ChronoUnit.DAYS));
        spinner2.setEditor(new LocalDateTimeEditor(spinner2, DATE_FORMAT_PATTERN));

//         JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner2.getEditor();
//         DefaultFormatter formatter = new InternationalFormatter(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN).toFormat());
//         DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
//         JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
//         ftf.setHorizontalAlignment(SwingConstants.LEFT);
//         ftf.setColumns(10);
//         ftf.setEditable(true);
//         ftf.setFormatterFactory(factory);

        add(makeTitlePanel(spinner0, "SpinnerDateModel"));
        add(makeTitlePanel(spinner1, "SpinnerDateModel / toInstant"));
        add(makeTitlePanel(spinner2, "SpinnerLocalDateTimeModel"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
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

    protected SpinnerLocalDateTimeModel(ChronoLocalDateTime<?> value, Comparable<ChronoLocalDateTime<?>> start, Comparable<ChronoLocalDateTime<?>> end, TemporalUnit temporalUnit) {
        super();
//         if (Objects.nonNull(start) && start.compareTo(value) >= 0 || Objects.nonNull(end) && end.compareTo(value) <= 0) {
//             throw new IllegalArgumentException("(start <= value <= end) is false");
//         }
        this.value = Optional.ofNullable(value).orElseThrow(() -> new IllegalArgumentException("value is null"));
        this.start = start;
        this.end   = end;
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
        //Calendar cal = Calendar.getInstance();
        //cal.setTime(value.getTime());
        //cal.add(calendarField, 1);
        //Date next = cal.getTime();
        ChronoLocalDateTime<?> next = value.plus(1, temporalUnit);
        return Objects.isNull(end) || end.compareTo(next) >= 0 ? next : null;
    }

    @Override public Object getPreviousValue() {
        //Calendar cal = Calendar.getInstance();
        //cal.setTime(value.getTime());
        //cal.add(calendarField, -1);
        //Date prev = cal.getTime();
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

    protected LocalDateTimeEditor(final JSpinner spinner, String dateFormatPattern) {
        super(spinner);
        if (!(spinner.getModel() instanceof SpinnerLocalDateTimeModel)) {
            throw new IllegalArgumentException("model not a SpinnerLocalDateTimeModel");
        }
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
        model = (SpinnerLocalDateTimeModel) spinner.getModel();
        final DefaultFormatter formatter = new LocalDateTimeFormatter();

        EventQueue.invokeLater(() -> {
            formatter.setValueClass(LocalDateTime.class);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
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
            ftf.setFormatterFactory(factory);
        });
    }

    public SpinnerLocalDateTimeModel getModel() {
        return model; //(SpinnerLocalDateTimeModel) getSpinner().getModel();
    }

    protected class LocalDateTimeFormatter extends InternationalFormatter {
        protected LocalDateTimeFormatter() {
            super(dateTimeFormatter.toFormat());
        }
        @Override public String valueToString(Object value) throws ParseException {
            //System.out.println(value.getClass().getName());
            if (value instanceof TemporalAccessor) {
                //return ((LocalDateTime) value).format(dateTimeFormatter);
                return dateTimeFormatter.format((TemporalAccessor) value);
            } else {
                return "";
            }
        }
        @Override public Object stringToValue(String text) throws ParseException {
            //System.out.println("stringToValue:" + text);
            SpinnerLocalDateTimeModel model = getModel();
            try {
                //LocalDateTime value = LocalDate.parse(text, dateTimeFormatter).atStartOfDay();
                TemporalAccessor ta = dateTimeFormatter.parse(text);
                ChronoLocalDateTime<?> value = model.getLocalDateTime();
                //@see https://tips4java.wordpress.com/2015/04/09/temporal-spinners/
                for (ChronoField field: ChronoField.values()) {
                    if (field.isSupportedBy(value) && ta.isSupported(field)) {
                        value = field.adjustInto(value, ta.getLong(field));
                    }
                }
                Comparable<ChronoLocalDateTime<?>> min = model.getStart();
                Comparable<ChronoLocalDateTime<?>> max = model.getEnd();
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
