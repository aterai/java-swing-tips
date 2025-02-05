// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color DRAW_COLOR = Color.BLACK;
  private static final Color BACK_COLOR = Color.WHITE;
  private static final int MINX = 5;
  private static final int MAXX = 315;
  private static final int MINY = 5;
  private static final int MAXY = 175;
  private static final int MINN = 50;
  private static final int MAXN = 500;
  private final List<Double> array = new ArrayList<>(MAXN);
  private int number = 150;
  private double factorx;
  private double factory;
  private transient SwingWorker<String, Rectangle> worker;

  private final JComboBox<GenerateInputs> distCmb = new JComboBox<>(GenerateInputs.values());
  private final JComboBox<SortAlgorithms> algoCmb = new JComboBox<>(SortAlgorithms.values());
  private final SpinnerNumberModel model = new SpinnerNumberModel(number, MINN, MAXN, 10);
  private final JSpinner spinner = new JSpinner(model);
  private final JButton startButton = new JButton("Start");
  private final JButton cancelButton = new JButton("Cancel");
  private final JPanel panel = new JPanel() {
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      drawAllOval(g);
    }
  };

  private MainPanel() {
    super(new BorderLayout());
    genArray(number);

    startButton.addActionListener(e -> {
      setComponentEnabled(false);
      panel.setToolTipText(null);
      workerExecute();
    });

    cancelButton.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
    });

    ItemListener il = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        genArray(number);
        panel.repaint();
        panel.setToolTipText(null);
      }
    };
    distCmb.addItemListener(il);
    algoCmb.addItemListener(il);
    panel.setBackground(BACK_COLOR);
    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box1.add(new JLabel(" Number:"));
    box1.add(spinner);
    box1.add(new JLabel(" Input:"));
    box1.add(distCmb);

    Box box2 = Box.createHorizontalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box2.add(new JLabel(" Algorithm:"));
    box2.add(algoCmb);
    box2.add(startButton);
    box2.add(cancelButton);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.add(box1);
    p.add(box2);
    add(p, BorderLayout.NORTH);
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  public void drawAllOval(Graphics g) {
    // g.setColor(DRAW_COLOR);
    for (int i = 0; i < number; i++) {
      int px = (int) (MINX + factorx * i);
      int py = MAXY - (int) (factory * array.get(i));
      g.setColor(i % 5 == 0 ? Color.RED : DRAW_COLOR);
      g.drawOval(px, py, 4, 4);
    }
  }

  public void setComponentEnabled(boolean flag) {
    cancelButton.setEnabled(!flag);
    startButton.setEnabled(flag);
    spinner.setEnabled(flag);
    distCmb.setEnabled(flag);
    algoCmb.setEnabled(flag);
  }

  public void genArray(int n) {
    array.clear();
    factorx = (MAXX - MINX) / (double) n;
    factory = (double) MAXY - MINY;
    distCmb.getItemAt(distCmb.getSelectedIndex()).generate(array, n);
  }

  public void workerExecute() {
    int tmp = model.getNumber().intValue();
    if (tmp != number) {
      number = tmp;
      genArray(number);
    }
    SortAlgorithms sa = algoCmb.getItemAt(algoCmb.getSelectedIndex());
    Rectangle paintArea = new Rectangle(MINX, MINY, MAXX - MINX, MAXY - MINY);
    worker = new SortingTask(sa, number, array, paintArea, factorx, factory) {
      @Override protected void process(List<Rectangle> chunks) {
        if (isDisplayable() && !isCancelled()) {
          chunks.forEach(panel::repaint);
        } else {
          // System.out.println("process: DISPOSE_ON_CLOSE");
          cancel(true);
        }
      }

      @Override protected void done() {
        if (!isDisplayable()) {
          // System.out.println("done: DISPOSE_ON_CLOSE");
          cancel(true);
          return;
        }
        setComponentEnabled(true);
        String text;
        try {
          text = isCancelled() ? "Cancelled" : get();
        } catch (InterruptedException ex) {
          text = "Interrupted";
          Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
          ex.printStackTrace();
          text = "Error: " + ex.getMessage();
        }
        panel.setToolTipText(text);
        repaint();
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
      ex.printStackTrace();
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

enum SortAlgorithms {
  ISORT("Insertion Sort"),
  SELSORT("Selection Sort"),
  SHELLSORT("Shell Sort"),
  HSORT("Heap Sort"),
  QSORT("Quicksort"),
  QSORT2("2-way Quicksort");
  private final String description;

  SortAlgorithms(String description) {
    this.description = description;
  }

  @Override public String toString() {
    return description;
  }
}

enum GenerateInputs {
  RANDOM() {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(Math.random());
      }
    }
  },
  ASCENDING() {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(i / (double) n);
      }
    }
  },
  DESCENDING() {
    @Override public void generate(List<Double> array, int n) {
      for (int i = 0; i < n; i++) {
        array.add(1d - i / (double) n);
      }
    }
  };
  public abstract void generate(List<Double> array, int n);
}
