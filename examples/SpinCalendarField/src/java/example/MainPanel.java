// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    Calendar c = Calendar.getInstance();
    // c.clear(Calendar.HOUR_OF_DAY);
    // c.clear(Calendar.AM_PM);
    // c.clear(Calendar.HOUR);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.clear(Calendar.MINUTE);
    c.clear(Calendar.SECOND);
    c.clear(Calendar.MILLISECOND);
    Date d = c.getTime();

    SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
    DefaultFormatterFactory factory = new DefaultFormatterFactory(new DateFormatter(format));

    JSpinner spinner1 = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.SECOND));
    ((JSpinner.DefaultEditor) spinner1.getEditor()).getTextField().setFormatterFactory(factory);

    JSpinner spinner2 = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.SECOND) {
      @Override public void setCalendarField(int calendarField) {
        // https://docs.oracle.com/javase/8/docs/api/javax/swing/SpinnerDateModel.html#setCalendarField-int-
        // If you only want one field to spin you can subclass
        // and ignore the setCalendarField calls.
      }
    });
    ((JSpinner.DefaultEditor) spinner2.getEditor()).getTextField().setFormatterFactory(factory);

    add(makeTitledPanel("Default SpinnerDateModel", spinner1));
    add(makeTitledPanel("Override SpinnerDateModel#setCalendarField(...)", spinner2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
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
