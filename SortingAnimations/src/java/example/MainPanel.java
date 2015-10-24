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
            worker = new UITask(sa, number, array, paintArea, factorx, factory);
            worker.execute();
        }
    });
    private final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
        @Override public void actionPerformed(ActionEvent e) {
            if (Objects.nonNull(worker) && !worker.isDone()) {
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
                g.setColor(i % 5 == 0 ? Color.RED : DRAW_COLOR);
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
    private class UITask extends SortingTask {
        public UITask(SortAlgorithms sortAlgorithm, int number, List<Double> array, Rectangle rect, double factorx, double factory) {
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
    SortAlgorithms(String description) {
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}

enum GenerateInputs {
    Random, Ascending, Descending;
}
