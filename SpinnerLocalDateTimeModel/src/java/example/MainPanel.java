package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
// import java.awt.*;
// import java.awt.event.*;
// import javax.swing.*;

import java.awt.*;
import java.io.Serializable;
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
        super(new GridLayout(2, 1));

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

        //JSpinner spinner1 = new JSpinner(new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH));
        JSpinner spinner1 = new JSpinner(new SpinnerDateModel(date, start, end, Calendar.DAY_OF_MONTH));
        spinner1.setEditor(new JSpinner.DateEditor(spinner1, DATE_FORMAT_PATTERN));

        LocalDateTime d = LocalDateTime.now();
        LocalDateTime s = d.minus(2, ChronoUnit.DAYS);
        //LocalDateTime e = LocalDateTime.MAX;
        LocalDateTime e = d.plus(7, ChronoUnit.DAYS);

        System.out.println(d);
        System.out.println(s);
        System.out.println(e);

        JSpinner spinner2 = new JSpinner(new SpinnerLocalDateTimeModel(d, s, e, ChronoUnit.DAYS));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner2.getEditor();
        DefaultFormatter formatter = new InternationalFormatter(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN).toFormat());
        DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
        JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
        ftf.setHorizontalAlignment(JTextField.LEFT);
        ftf.setColumns(10);
        ftf.setEditable(true);
        ftf.setFormatterFactory(factory);

        add(makeTitlePanel(spinner1, "SpinnerDateModel"));
        add(makeTitlePanel(spinner2, "SpinnerLocalDateTimeModel"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
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

class SpinnerLocalDateTimeModel extends AbstractSpinnerModel implements Serializable {
    private Comparable<ChronoLocalDateTime<?>> start, end;
    private LocalDateTime value;
    private TemporalUnit temporalUnit;

    public SpinnerLocalDateTimeModel(LocalDateTime value, Comparable<ChronoLocalDateTime<?>> start, Comparable<ChronoLocalDateTime<?>> end, TemporalUnit temporalUnit) {
        super();
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        if (!(start == null || start.compareTo(value) <= 0) && (end == null || end.compareTo(value) >= 0)) {
            throw new IllegalArgumentException("(start <= value <= end) is false");
        }
        this.value = value;
        this.start = start;
        this.end = end;
        this.temporalUnit = temporalUnit;
    }

    public void setStart(Comparable<ChronoLocalDateTime<?>> start) {
        if (start == null ? this.start != null : !start.equals(this.start)) {
            this.start = start;
            fireStateChanged();
        }
    }

    public Comparable<ChronoLocalDateTime<?>> getStart() {
        return start;
    }

    public void setEnd(Comparable<ChronoLocalDateTime<?>> end) {
        if (end == null ? this.end != null : !end.equals(this.end)) {
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
        LocalDateTime next = value.plus(1, temporalUnit);
        return end == null || end.compareTo(next) >= 0 ? next : null;
    }

    @Override public Object getPreviousValue() {
        //Calendar cal = Calendar.getInstance();
        //cal.setTime(value.getTime());
        //cal.add(calendarField, -1);
        //Date prev = cal.getTime();
        LocalDateTime prev = value.minus(1, temporalUnit);
        return start == null || start.compareTo(prev) <= 0 ? prev : null;
    }

    public LocalDateTime getLocalDateTime() {
        return value;
    }

    @Override public Object getValue() {
        return value;
    }

    @Override public void setValue(Object value) {
        if (!(value instanceof LocalDateTime)) {
            throw new IllegalArgumentException("illegal value");
        }
        if (!value.equals(this.value)) {
            this.value = (LocalDateTime) value;
            fireStateChanged();
        }
    }
}
