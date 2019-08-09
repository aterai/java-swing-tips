// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));

    // List<String> weeks = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Sat");
    Locale locale = Locale.ENGLISH; // Locale.getDefault();
    DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();
    List<String> weeks = IntStream.range(0, DayOfWeek.values().length)
        .mapToObj(firstDayOfWeek::plus)
        .map(dow -> dow.getDisplayName(TextStyle.SHORT_STANDALONE, locale))
        .collect(Collectors.toList());

    SpinnerModel model1 = new SpinnerNumberModel(20, 0, 59, 1);

    SpinnerModel model2 = new SpinnerListModel(weeks);

    SpinnerModel model3 = new SpinnerNumberModel(20, 0, 59, 1) {
      @Override public Object getNextValue() {
        // Object n = super.getNextValue();
        // return n != null ? n : getMinimum();
        return Optional.ofNullable(super.getNextValue()).orElseGet(this::getMinimum);
      }

      @Override public Object getPreviousValue() {
        // Object n = super.getPreviousValue();
        // return n != null ? n : getMaximum();
        return Optional.ofNullable(super.getPreviousValue()).orElseGet(this::getMaximum);
      }
    };

    SpinnerModel model4 = new SpinnerListModel(weeks) {
      @Override public Object getNextValue() {
        // Object o = super.getNextValue();
        // return o != null ? o : getList().get(0);
        return Optional.ofNullable(super.getNextValue()).orElseGet(() -> getList().get(0));
      }

      @Override public Object getPreviousValue() {
        // List<?> l = getList();
        // Object o = super.getPreviousValue();
        // return o != null ? o : l.get(l.size() - 1);
        return Optional.ofNullable(super.getPreviousValue()).orElseGet(() -> {
          List<?> l = getList();
          return l.get(l.size() - 1);
        });
      }
    };

    add(makeTitledPanel("default model", new JSpinner(model1), new JSpinner(model2)));
    add(makeTitledPanel("cycling model", new JSpinner(model3), new JSpinner(model4)));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component... list) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;
    c.gridx = GridBagConstraints.REMAINDER;
    Stream.of(list).forEach(cmp -> p.add(cmp, c));
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
