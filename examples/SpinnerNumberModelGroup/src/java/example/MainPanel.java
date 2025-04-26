// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    SpinnerNumberModel model0 = new SpinnerNumberModel(100, 0, 100, 1);
    SpinnerNumberModel model1 = new SpinnerNumberModel(0, 0, 100, 1);
    SpinnerNumberModel model2 = new SpinnerNumberModel(0, 0, 100, 1);

    List<SpinnerNumberModel> list = Arrays.asList(model0, model1, model2);
    int expectedSum = 100;
    JTextArea log = new JTextArea();
    ChangeListener handler = e -> {
      String str = list.stream()
          .map(SpinnerNumberModel::getNumber)
          .map(Object::toString)
          .collect(Collectors.joining(" + "));
      log.append(String.format("%s = %d%n", str, expectedSum));
    };
    SpinnerNumberModelGroup group = new SpinnerNumberModelGroup(expectedSum);
    list.forEach(m -> {
      m.addChangeListener(handler);
      group.add(m);
      p.add(new JSpinner(m));
    });

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
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

// https://stackoverflow.com/questions/21388255/multiple-jsliders-reacting-to-each-other-to-always-equal-100-percent
class SpinnerNumberModelGroup {
  private final int expectedSum;
  private final Deque<SpinnerNumberModel> candidates = new ArrayDeque<>();
  private final ChangeListener changeListener = e -> {
    SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
    update(source);
  };
  private boolean updating;

  protected SpinnerNumberModelGroup(int expectedSum) {
    this.expectedSum = expectedSum;
  }

  private void update(SpinnerNumberModel source) {
    if (updating) {
      return;
    }
    updating = true;
    if (candidates.size() - 1 > 0) {
      int delta = computeSum() - expectedSum;
      if (delta > 0) {
        distributeRemove(delta, source);
      } else {
        distributeAdd(delta, source);
      }
    }
    updating = false;
  }

  private void distributeRemove(int delta, SpinnerNumberModel source) {
    int counter = 0;
    int remaining = delta;
    while (remaining > 0) {
      SpinnerNumberModel model = candidates.removeFirst();
      counter++;
      if (Objects.equals(model, source)) {
        candidates.addLast(model);
      } else {
        Object prev = model.getPreviousValue();
        if (prev instanceof Integer) {
          model.setValue(prev);
          remaining--;
          counter = 0;
        }
        candidates.addLast(model);
        if (remaining == 0) {
          break;
        }
      }
      if (counter > candidates.size()) {
        String msg = "Can not distribute " + delta + " among " + candidates;
        throw new IllegalArgumentException(msg);
      }
    }
  }

  private void distributeAdd(int delta, SpinnerNumberModel source) {
    int counter = 0;
    int remaining = -delta;
    while (remaining > 0) {
      SpinnerNumberModel model = candidates.removeLast();
      counter++;
      if (Objects.equals(model, source)) {
        candidates.addFirst(model);
      } else {
        // if (model.getNumber().intValue() < (int) model.getMaximum()) {
        Object next = model.getNextValue();
        if (next instanceof Integer) {
          model.setValue(next);
          remaining--;
          counter = 0;
        }
        candidates.addFirst(model);
        if (remaining == 0) {
          break;
        }
      }
      if (counter > candidates.size()) {
        String msg = "Can not distribute " + delta + " among " + candidates;
        throw new IllegalArgumentException(msg);
      }
    }
  }

  private int computeSum() {
    return candidates.stream()
        .map(SpinnerNumberModel::getNumber)
        .mapToInt(Number::intValue)
        .sum();
  }

  public void add(SpinnerNumberModel spinner) {
    candidates.add(spinner);
    spinner.addChangeListener(changeListener);
  }

  // public void remove(SpinnerNumberModel spinner) {
  //   candidates.remove(spinner);
  //   spinner.removeChangeListener(changeListener);
  // }
}
