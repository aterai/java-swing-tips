// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(3, 1));

    @SuppressWarnings("JavaUtilDate")
    Date date = new Date();
    String dateFormat = "yyyy/MM/dd";
    SpinnerDateModel model1 = new SpinnerDateModel(date, date, null, Calendar.DAY_OF_MONTH);
    JSpinner spinner1 = makeSpinner(model1, dateFormat);
    p.add(makeTitledPanel("Calendar.DAY_OF_MONTH", spinner1));

    Calendar today = Calendar.getInstance();
    today.clear(Calendar.MILLISECOND);
    today.clear(Calendar.SECOND);
    today.clear(Calendar.MINUTE);
    today.set(Calendar.HOUR_OF_DAY, 0);
    Date start = today.getTime();

    JTextArea log = new JTextArea();
    log.append(date + "\n");
    log.append(start + "\n");

    SpinnerDateModel model2 = new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH);
    JSpinner spinner2 = makeSpinner(model2, dateFormat);
    p.add(makeTitledPanel("min: set(Calendar.HOUR_OF_DAY, 0)", spinner2));

    SpinnerDateModel model3 = new SpinnerDateModel(date, start, null, Calendar.DAY_OF_MONTH);
    JSpinner spinner3 = makeSpinner3(model3, dateFormat);
    p.add(makeTitledPanel("JSpinner.DateEditor + FocusListener", spinner3));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSpinner makeSpinner(SpinnerDateModel model, String dateFormat) {
    JSpinner spinner = new JSpinner(model);
    spinner.setEditor(new JSpinner.DateEditor(spinner, dateFormat));
    return spinner;
  }

  private static JSpinner makeSpinner3(SpinnerDateModel model, String dateFormat) {
    JSpinner spinner = new JSpinner(model);
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, dateFormat);
    spinner.setEditor(editor);
    editor.getTextField().addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        EventQueue.invokeLater(() -> {
          int i = dateFormat.lastIndexOf("dd");
          editor.getTextField().select(i, i + 2);
        });
      }
    });
    return spinner;
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
