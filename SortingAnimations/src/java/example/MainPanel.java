package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

// SortAnim.java -- Animate sorting algorithms
// Copyright (C) 1999 Lucent Technologies
// From 'Programming Pearls' by Jon Bentley
//Sorting Algorithm Animations from Programming Pearls
//http://www.cs.bell-labs.com/cm/cs/pearls/sortanim.html
// modified by aterai at.terai@gmail.com

public final class MainPanel extends JPanel {
    private final JComboBox<? extends Enum> distributionsChoices = new JComboBox<>(GenerateInputs.values());
    private final JComboBox<? extends Enum> algorithmsChoices    = new JComboBox<>(SortAlgorithms.values());
    //private final JComboBox<Enum> algorithmsChoices = new JComboBox<Enum>(SortAlgorithms.values());
    //private final JComboBox<SortAlgorithms> algorithmsChoices = new JComboBox<>(SortAlgorithms.values());
    private final SpinnerNumberModel model;
    private final JSpinner spinner;
    private SwingWorker<String, Rectangle> worker;
    private final JButton startButton = new JButton(new AbstractAction("Start") {
        @Override public void actionPerformed(ActionEvent e) {
            setComponentEnabled(false);
            int tmp = model.getNumber().intValue();
            if (tmp != number) {
                number = tmp;
                genArray(number);
            }
            SortAlgorithms sa = (SortAlgorithms) algorithmsChoices.getSelectedItem();
            Rectangle paintArea = new Rectangle(MINX, MINY, MAXX - MINX, MAXY - MINY);
            worker = new SortingTask(sa, number, array, paintArea, factorx, factory) {
                @Override protected void process(List<Rectangle> chunks) {
                    if (!isDisplayable()) {
                        System.out.println("process: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    for (Rectangle r: chunks) {
                        panel.repaint(r);
                    }
                }
                @Override public void done() {
                    if (!isDisplayable()) {
                        System.out.println("done: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    setComponentEnabled(true);
                    try {
                        String text = isCancelled() ? "Cancelled" : get();
                        System.out.println(text);
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                    }
                    repaint();
                }
            };
            worker.execute();
        }
    });
    private final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
        @Override public void actionPerformed(ActionEvent e) {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
        }
    });
    private final JPanel panel = new JPanel() {
        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //g.setColor(DRAW_COLOR);
            for (int i = 0; i < number; i++) {
                int px = (int) (MINX + factorx * i);
                int py = MAXY - (int) (factory * array.get(i));
                g.setColor((i % 5 == 0) ? Color.RED : DRAW_COLOR);
                g.drawOval(px, py, 4, 4);
            }
        }
    };

    private static final Color DRAW_COLOR = Color.BLACK;
    private static final Color BACK_COLOR = Color.WHITE;
    private static final int MINX = 5,  MAXX = 315;
    private static final int MINY = 5,  MAXY = 175;
    private static final int MINN = 50, MAXN = 500;;
    private final List<Double> array = new ArrayList<>(MAXN);
    private int number = 150;
    private double factorx, factory;

    public MainPanel() {
        super(new BorderLayout());
        genArray(number);
        model = new SpinnerNumberModel(number, MINN, MAXN, 10);
        spinner = new JSpinner(model);
        ItemListener il = new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    genArray(number);
                    panel.repaint();
                }
            }
        };
        distributionsChoices.addItemListener(il);
        algorithmsChoices.addItemListener(il);
        panel.setBackground(BACK_COLOR);
        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box1.add(new JLabel(" Number:"));    box1.add(spinner);
        box1.add(new JLabel(" Input:"));     box1.add(distributionsChoices);

        Box box2 = Box.createHorizontalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box2.add(new JLabel(" Algorithm:")); box2.add(algorithmsChoices);
        box2.add(startButton); box2.add(cancelButton);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(box1); p.add(box2);
        add(p, BorderLayout.NORTH); add(panel);
        setPreferredSize(new Dimension(320, 240));
    }
    private void setComponentEnabled(boolean flag) {
        cancelButton.setEnabled(!flag);
        startButton.setEnabled(flag);
        spinner.setEnabled(flag);
        distributionsChoices.setEnabled(flag);
        algorithmsChoices.setEnabled(flag);
    }
    private void genArray(int n) {
        array.clear();
        factorx = (MAXX - MINX) / (double) n;
        factory = MAXY - MINY;
        GenerateInputs gi = (GenerateInputs) distributionsChoices.getSelectedItem();
        for (int i = 0; i < n; i++) {
            switch (gi) {
              case Random:     array.add((double) Math.random()); break;
              case Ascending:  array.add(i / (double) n);         break;
              case Descending: array.add(1d - i / (double) n);    break;
              default:         throw new AssertionError("Unknown GenerateInputs");
            }
        }
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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum SortAlgorithms {
    Isort    ("Insertion Sort"),
    Selsort  ("Selection Sort"),
    Shellsort("Shell Sort"),
    Hsort    ("Heap Sort"),
    Qsort    ("Quicksort"),
    Qsort2   ("2-way Quicksort");
    private final String description;
    private SortAlgorithms(String description) {
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}

enum GenerateInputs {
    Random, Ascending, Descending;
}

class SortingTask extends SwingWorker<String, Rectangle> {
    private final List<Double> array;
    private final int number;
    private final double factorx;
    private final double factory;
    private final Rectangle rect;
    private final Rectangle repaintArea;
    private final SortAlgorithms sortAlgorithm;

    public SortingTask(SortAlgorithms sortAlgorithm, int number, List<Double> array, Rectangle rect, double factorx, double factory) {
        super();
        this.sortAlgorithm = sortAlgorithm;
        this.number  = number;
        this.array   = array;
        this.rect    = rect;
        this.factorx = factorx;
        this.factory = factory;
        this.repaintArea = new Rectangle(rect);
        this.repaintArea.grow(5, 5);
    }

    @Override public String doInBackground() {
        try {
            switch (sortAlgorithm) {
              case Isort:     isort(number);         break;
              case Selsort:   ssort(number);         break;
              case Shellsort: shellsort(number);     break;
              case Hsort:     heapsort(number);      break;
              case Qsort:     qsort(0, number - 1);  break;
              case Qsort2:    qsort2(0, number - 1); break;
              default:        throw new AssertionError("Unknown SortAlgorithms");
            }
        } catch (InterruptedException ie) {
            return "Interrupted";
        }
        return "Done";
    }

    private void swap(int i, int j) throws InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException();
        }
        int px = (int) (rect.x + factorx * i);
        int py = rect.y + rect.height - (int) (factory * array.get(i));
        publish(new Rectangle(px, py, 4, 4));

//         double t = array.get(i);
//         array.set(i, array.get(j));
//         array.set(j, t);
        Collections.swap(array, i, j);

        px = (int) (rect.x + factorx * i);
        py = rect.y + rect.height - (int) (factory * array.get(i));
        publish(new Rectangle(px, py, 4, 4));

        publish(repaintArea);
        Thread.sleep(5);
    }

    // Sorting Algs
    private void isort(int n) throws InterruptedException {
        for (int i = 1; i < n; i++) {
            for (int j = i; j > 0 && array.get(j - 1) > array.get(j); j--) {
                swap(j - 1, j);
            }
        }
    }

    private void ssort(int n) throws InterruptedException {
        for (int i = 0; i < n - 1; i++) {
            for (int j = i; j < n; j++) {
                if (array.get(j) < array.get(i)) {
                    swap(i, j);
                }
            }
        }
    }

    private void shellsort(int n) throws InterruptedException {
        int i;
        int j;
        int h = 1;
        while (h < n) {
            h = 3 * h + 1;
        }
        for (;;) {
            h /= 3;
            if (h < 1) {
                break;
            }
            for (i = h; i < n; i++) {
                for (j = i; j >= h; j -= h) {
                    if (array.get(j - h) < array.get(j)) {
                        break;
                    }
                    swap(j - h, j);
                }
            }
        }
    }

    private void siftdown(int l, int u) throws InterruptedException {
        int i = l;
        int c;
        for (;;) {
            c = 2 * i;
            if (c > u) {
                break;
            }
            if (c + 1 <= u && array.get(c + 1) > array.get(c)) {
                c++;
            }
            if (array.get(i) >= array.get(c)) {
                break;
            }
            swap(i, c);
            i = c;
        }
    }

    private void heapsort(int n) throws InterruptedException { // BEWARE!!! Sorts x[1..n-1]
        int i;
        for (i = n / 2; i > 0; i--) {
            siftdown(i, n - 1);
        }
        for (i = n - 1; i >= 2; i--) {
            swap(1, i);
            siftdown(1, i - 1);
        }
    }

    private void qsort(int l, int u) throws InterruptedException {
        if (l >= u) {
            return;
        }
        int m = l;
        for (int i = l + 1; i <= u; i++) {
            if (array.get(i) < array.get(l)) {
                swap(++m, i);
            }
        }
        swap(l, m);
        qsort(l, m - 1);
        qsort(m + 1, u);
    }

    private void qsort2(int l, int u) throws InterruptedException {
        if (l >= u) {
            return;
        }
        int i = l;
        int j = u + 1;
        for (;;) {
            do {
                i++;
            } while (i <= u && array.get(i) < array.get(l));
            do {
                j--;
            } while (array.get(j) > array.get(l));
            if (i > j) {
                break;
            }
            swap(i, j);
        }
        swap(l, j);
        qsort2(l, j - 1);
        qsort2(j + 1, u);
    }
}
