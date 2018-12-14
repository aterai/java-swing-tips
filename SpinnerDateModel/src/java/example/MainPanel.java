// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1));
    String dateFormat = "yyyy/MM/dd";

    Date date = new Date();
    JSpinner spinner1 = new JSpinner(new SpinnerDateModel(date, date, null, Calendar.DAY_OF_MONTH));
    spinner1.setEditor(new JSpinner.DateEditor(spinner1, dateFormat));

    Calendar today = Calendar.getInstance();
    today.clear(Calendar.MILLISECOND);
    today.clear(Calendar.SECOND);
    today.clear(Calendar.MINUTE);
    today.set(Calendar.HOUR_OF_DAY, 0);
    Date start = today.getTime();

    System.out.println(date);
    System.out.println(start);

    JSpinner spinner2 = new JSpinner(new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH));
    spinner2.setEditor(new JSpinner.DateEditor(spinner2, dateFormat));

    JSpinner spinner3 = new JSpinner(new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH));
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner3, dateFormat);
    spinner3.setEditor(editor);
    editor.getTextField().addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        EventQueue.invokeLater(() -> {
          int i = dateFormat.lastIndexOf("dd");
          editor.getTextField().select(i, i + 2);
        });
      }
    });

    add(makeTitledPanel("Calendar.DAY_OF_MONTH", spinner1));
    add(makeTitledPanel("min: set(Calendar.HOUR_OF_DAY, 0)", spinner2));
    add(makeTitledPanel("JSpinner.DateEditor + FocusListener", spinner3));
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
