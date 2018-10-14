package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MainPanel extends JPanel {
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

    private final JComboBox<GenerateInputs> distributionsChoices = new JComboBox<>(GenerateInputs.values());
    private final JComboBox<SortAlgorithms> algorithmsChoices = new JComboBox<>(SortAlgorithms.values());
    private final SpinnerNumberModel model = new SpinnerNumberModel(number, MINN, MAXN, 10);
    private final JSpinner spinner = new JSpinner(model);
    private final JButton startButton = new JButton("Start");
    private final JButton cancelButton = new JButton("Cancel");
    protected final JPanel panel = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawAllOval(g);
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        genArray(number);

        startButton.addActionListener(e -> {
            setComponentEnabled(false);
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
            }
        };
        distributionsChoices.addItemListener(il);
        algorithmsChoices.addItemListener(il);
        panel.setBackground(BACK_COLOR);
        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box1.add(new JLabel(" Number:"));
        box1.add(spinner);
        box1.add(new JLabel(" Input:"));
        box1.add(distributionsChoices);

        Box box2 = Box.createHorizontalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        box2.add(new JLabel(" Algorithm:"));
        box2.add(algorithmsChoices);
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
    protected final void drawAllOval(Graphics g) {
        // g.setColor(DRAW_COLOR);
        for (int i = 0; i < number; i++) {
            int px = (int) (MINX + factorx * i);
            int py = MAXY - (int) (factory * array.get(i));
            g.setColor(i % 5 == 0 ? Color.RED : DRAW_COLOR);
            g.drawOval(px, py, 4, 4);
        }
    }
    protected final void setComponentEnabled(boolean flag) {
        cancelButton.setEnabled(!flag);
        startButton.setEnabled(flag);
        spinner.setEnabled(flag);
        distributionsChoices.setEnabled(flag);
        algorithmsChoices.setEnabled(flag);
    }
    protected final void genArray(int n) {
        array.clear();
        factorx = (MAXX - MINX) / (double) n;
        factory = MAXY - MINY;
        GenerateInputs gi = distributionsChoices.getItemAt(distributionsChoices.getSelectedIndex());
        for (int i = 0; i < n; i++) {
            switch (gi) {
                case RANDOM:
                    array.add((double) Math.random());
                    break;
                case ASCENDING:
                    array.add(i / (double) n);
                    break;
                case DESCENDING:
                    array.add(1d - i / (double) n);
                    break;
                default:
                    throw new AssertionError("Unknown GenerateInputs");
            }
        }
    }
    protected final void workerExecute() {
        int tmp = model.getNumber().intValue();
        if (tmp != number) {
            number = tmp;
            genArray(number);
        }
        SortAlgorithms sa = algorithmsChoices.getItemAt(algorithmsChoices.getSelectedIndex());
        Rectangle paintArea = new Rectangle(MINX, MINY, MAXX - MINX, MAXY - MINY);
        worker = new UITask(sa, number, array, paintArea, factorx, factory);
        worker.execute();
    }
    private class UITask extends SortingTask {
        protected UITask(SortAlgorithms sortAlgorithm, int number, List<Double> array, Rectangle rect, double factorx, double factory) {
            super(sortAlgorithm, number, array, rect, factorx, factory);
        }
        @Override protected void process(List<Rectangle> chunks) {
            if (isCancelled()) {
                return;
            }
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    RANDOM, ASCENDING, DESCENDING;
}
