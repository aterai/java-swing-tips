package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel implements HierarchyListener {
    private transient SwingWorker<String, Void> worker;
    private MainPanel() {
        super(new BorderLayout());

        BoundedRangeModel m = new DefaultBoundedRangeModel();
        JProgressBar progress0 = new JProgressBar(m);
        progress0.setStringPainted(false);

        JProgressBar progress1 = new JProgressBar(m);
        progress1.setStringPainted(true);

        JProgressBar progress2 = new JProgressBar(m);
        progress2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JProgressBar progress3 = new JProgressBar(m);
        progress3.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        progress3.setStringPainted(true);

        JPanel p1 = new JPanel(new GridLayout(2, 2, 10, 10));
        p1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Stream.of(progress0, progress1, progress2, progress3).forEach(p1::add);

        JProgressBar progress4 = new JProgressBar(m);
        progress4.setOrientation(SwingConstants.VERTICAL);

        JProgressBar progress5 = new JProgressBar(m);
        progress5.setOrientation(SwingConstants.VERTICAL);
        progress5.setStringPainted(true);

        JProgressBar progress6 = new JProgressBar(m);
        progress6.setOrientation(SwingConstants.VERTICAL);
        progress6.setStringPainted(true);
        progress6.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JProgressBar progress7 = new JProgressBar(m) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.scale(1, -1);
                g2.translate(0, -getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        progress7.setOrientation(SwingConstants.VERTICAL);
        progress7.setStringPainted(true);

        JProgressBar progress8 = new JProgressBar(m);
        progress8.setOrientation(SwingConstants.VERTICAL);
        JLayer<JProgressBar> layer = new JLayer<>(progress8, new VerticalFlipLayerUI<>());

        Box p2 = Box.createHorizontalBox();
        p2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p2.add(Box.createHorizontalGlue());
        Stream.of(progress4, progress5, progress6, progress7, layer).forEach(c -> {
            p2.add(c);
            p2.add(Box.createHorizontalStrut(25));
        });

        JButton button = new JButton("Test");
        button.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = new BackgroundTask();
            worker.addPropertyChangeListener(new ProgressListener(progress0));
            worker.execute();
        });
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        box.add(Box.createVerticalGlue());
        box.add(button);

        addHierarchyListener(this);
        add(p1, BorderLayout.NORTH);
        add(p2, BorderLayout.WEST);
        add(box, BorderLayout.EAST);
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
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
        int lengthOfTask = 200;
        while (current <= lengthOfTask && !isCancelled()) {
            try { // dummy task
                Thread.sleep(5);
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

class VerticalFlipLayerUI<V extends Component> extends LayerUI<V> {
    private transient BufferedImage buf;

    @Override public void paint(Graphics g, JComponent c) {
        if (c instanceof JLayer) {
            Dimension d = ((JLayer<?>) c).getView().getSize();
            buf = Optional.ofNullable(buf)
                .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
                .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));

            Graphics2D g2 = buf.createGraphics();
            g2.scale(1, -1);
            g2.translate(0, -d.height);
            super.paint(g2, c);
            g2.dispose();

            Image image = c.createImage(buf.getSource());
            g.drawImage(image, 0, 0, null);
        } else {
            super.paint(g, c);
        }
    }
}
