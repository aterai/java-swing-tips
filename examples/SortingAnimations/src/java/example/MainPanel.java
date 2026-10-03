// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color DOT_COLOR = Color.BLACK;
  private static final Color MARKER_COLOR = Color.RED;
  private static final int MIN_NUMBER = 50;
  private static final int MAX_NUMBER = 500;
  private final Rectangle plotArea = new Rectangle(5, 5, 310, 170);
  // The list is modified by the SwingWorker thread and read by the EDT
  private final List<Double> array = Collections.synchronizedList(new ArrayList<>(MAX_NUMBER));
  private final JComboBox<InputDistribution> distributionCombo =
      new JComboBox<>(InputDistribution.values());
  private final JComboBox<SortAlgorithm> algorithmCombo = new JComboBox<>(SortAlgorithm.values());
  private final SpinnerNumberModel numberModel =
      new SpinnerNumberModel(150, MIN_NUMBER, MAX_NUMBER, 10);
  private final JSpinner numberSpinner = new JSpinner(numberModel);
  private final JButton startButton = new JButton("Start");
  private final JButton cancelButton = new JButton("Cancel");
  private final JPanel canvas = new JPanel() {
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      drawDots(g);
    }
  };
  private transient SwingWorker<String, Rectangle> worker;
  private boolean needsRegeneration;

  private MainPanel() {
    super(new BorderLayout());
    generateArray();
    setComponentsEnabled(true);

    startButton.addActionListener(e -> startSorting());

    cancelButton.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
    });

    ItemListener listener = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        resetArray();
      }
    };
    distributionCombo.addItemListener(listener);
    algorithmCombo.addItemListener(listener);
    numberSpinner.addChangeListener(e -> resetArray());
    canvas.setBackground(Color.WHITE);

    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box1.add(new JLabel(" Number:"));
    box1.add(numberSpinner);
    box1.add(new JLabel(" Input:"));
    box1.add(distributionCombo);

    Box box2 = Box.createHorizontalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box2.add(new JLabel(" Algorithm:"));
    box2.add(algorithmCombo);
    box2.add(startButton);
    box2.add(cancelButton);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(box1);
    p.add(box2);
    add(p, BorderLayout.NORTH);
    add(canvas);
    setPreferredSize(new Dimension(320, 240));
  }

  private void drawDots(Graphics g) {
    int size = array.size();
    for (int i = 0; i < size; i++) {
      Rectangle r = SortingTask.getDotBounds(plotArea, size, i, array.get(i));
      g.setColor(i % 5 == 0 ? MARKER_COLOR : DOT_COLOR);
      g.drawOval(r.x, r.y, r.width, r.height);
    }
  }

  private void setComponentsEnabled(boolean enabled) {
    cancelButton.setEnabled(!enabled);
    startButton.setEnabled(enabled);
    numberSpinner.setEnabled(enabled);
    distributionCombo.setEnabled(enabled);
    algorithmCombo.setEnabled(enabled);
  }

  private void generateArray() {
    int idx = distributionCombo.getSelectedIndex();
    InputDistribution distribution = distributionCombo.getItemAt(idx);
    array.clear();
    distribution.generate(array, numberModel.getNumber().intValue());
    needsRegeneration = false;
  }

  private void resetArray() {
    generateArray();
    canvas.setToolTipText(null);
    canvas.repaint();
  }

  private void startSorting() {
    // The previous run has already sorted (or partially sorted) the array
    if (needsRegeneration) {
      generateArray();
      canvas.repaint();
    }
    needsRegeneration = true;
    setComponentsEnabled(false);
    canvas.setToolTipText(null);
    SortAlgorithm algorithm = algorithmCombo.getItemAt(algorithmCombo.getSelectedIndex());
    worker = new SortingTask(algorithm, array, plotArea) {
      @Override protected void process(List<Rectangle> chunks) {
        if (isDisplayable() && !isCancelled()) {
          chunks.forEach(canvas::repaint);
        } else {
          cancel(true);
        }
      }

      @Override protected void done() {
        if (isDisplayable()) {
          setComponentsEnabled(true);
          canvas.setToolTipText(getDoneMessage());
          canvas.repaint();
        }
      }
    };
    worker.execute();
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum SortAlgorithm {
  INSERTION("Insertion Sort"),
  SELECTION("Selection Sort"),
  SHELL("Shell Sort"),
  HEAP("Heap Sort"),
  QUICK("Quicksort"),
  TWO_WAY_QUICK("2-way Quicksort");
  private final String description;

  SortAlgorithm(String description) {
    this.description = description;
  }

  @Override public String toString() {
    return description;
  }
}

enum InputDistribution {
  RANDOM {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(Math.random());
      }
    }
  },
  ASCENDING {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(i / (double) n);
      }
    }
  },
  DESCENDING {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(1d - i / (double) n);
      }
    }
  };

  public abstract void generate(List<Double> array, int n);
}
