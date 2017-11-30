package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private transient SwingWorker<String, Void> worker;

    private MainPanel() {
        super(new BorderLayout());

        BoundedRangeModel model = new DefaultBoundedRangeModel();
        JProgressBar progressBar0 = new JProgressBar(model);

        UIManager.put("ProgressBar.foreground", Color.RED);
        UIManager.put("ProgressBar.selectionForeground", Color.ORANGE);
        UIManager.put("ProgressBar.background", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Color.RED);
        JProgressBar progressBar1 = new JProgressBar(model);

        JProgressBar progressBar2 = new JProgressBar(model);
        progressBar2.setForeground(Color.BLUE);
        progressBar2.setBackground(Color.CYAN.brighter());
        progressBar2.setUI(new BasicProgressBarUI() {
            @Override protected Color getSelectionForeground() {
                return Color.PINK;
            }
            @Override protected Color getSelectionBackground() {
                return Color.BLUE;
            }
        });

        progressBar0.setStringPainted(true);
        progressBar1.setStringPainted(true);
        progressBar2.setStringPainted(true);

        JPanel p = new JPanel(new GridLayout(5, 1));
        p.add(makePanel(progressBar0));
        p.add(makePanel(progressBar1));
        p.add(makePanel(progressBar2));

        JButton button = new JButton("Test start");
        button.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = new BackgroundTask();
            worker.addPropertyChangeListener(new ProgressListener(progressBar0));
            worker.addPropertyChangeListener(new ProgressListener(progressBar1));
            worker.addPropertyChangeListener(new ProgressListener(progressBar2));
            worker.execute();
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button);
        box.add(Box.createHorizontalStrut(5));

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
                System.out.println("DISPOSE_ON_CLOSE");
                worker.cancel(true);
                worker = null;
            }
        });
        add(p);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makePanel(Component cmp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1d;
        p.add(cmp, c);
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class BackgroundTask extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        int current = 0;
        int lengthOfTask = 100;
        while (current <= lengthOfTask && !isCancelled()) {
            try { // dummy task
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                return "Interrupted";
            }
            setProgress(100 * current / lengthOfTask);
            current++;
        }
        return "Done";
    }
}

class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    protected ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        String strPropertyName = e.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) e.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
