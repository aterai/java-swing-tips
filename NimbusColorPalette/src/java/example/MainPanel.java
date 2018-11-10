package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.*;

public class MainPanel extends JPanel implements HierarchyListener {
    protected transient SwingWorker<String, Void> worker;

    public MainPanel() {
        super(new BorderLayout());

        UIDefaults def = UIManager.getLookAndFeelDefaults(); // new UIDefaults();
        def.put("nimbusOrange", new Color(255, 220, 35, 200));

        UIDefaults d = new UIDefaults();
        Painter<JComponent> painter = new Painter<JComponent>() {
            @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setColor(new Color(100, 250, 120, 50));
                g.fillRect(0, 0, w, h);
                g.setColor(new Color(100, 250, 120, 150));
                g.fillRect(3, h / 2, w - 6, h / 2 - 2);
            }
        };
        d.put("ProgressBar[Enabled].foregroundPainter", painter);
        d.put("ProgressBar[Enabled+Finished].foregroundPainter", painter);

        BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
        JProgressBar progressBar1 = new JProgressBar(model);
        JProgressBar progressBar2 = new JProgressBar(model);

        progressBar2.putClientProperty("Nimbus.Overrides", d);

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(progressBar1);
        p.add(progressBar2);

        JButton button = new JButton("Test start");
        button.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = new BackgroundTask();
            worker.addPropertyChangeListener(new ProgressListener(progressBar1));
            worker.execute();
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button);
        box.add(Box.createHorizontalStrut(5));

        addHierarchyListener(this);
        add(p);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
            System.out.println("DISPOSE_ON_CLOSE");
            worker.cancel(true);
            worker = null;
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
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
