package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public class MainPanel extends JPanel {
    protected transient SwingWorker<String, Void> worker;

    public MainPanel() {
        super(new BorderLayout());
        BoundedRangeModel model = new DefaultBoundedRangeModel();

        JProgressBar progress01 = new JProgressBar(model);
        progress01.setStringPainted(true);

        JProgressBar progress02 = new JProgressBar(model);
        progress02.setStringPainted(true);

        JProgressBar progress03 = new JProgressBar(model);
        progress03.setOpaque(false);

        JProgressBar progress04 = new JProgressBar(model);
        progress04.setOpaque(true); // for NimbusLookAndFeel

        BlockedColorLayerUI<Component> layerUI = new BlockedColorLayerUI<>();
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(makeTitledPanel("setStringPainted(true)", progress01, progress02));
        p.add(makeTitledPanel("setStringPainted(false)", progress03, new JLayer<>(progress04, layerUI)));

        JCheckBox check = new JCheckBox("Turn the progress bar red");
        check.addActionListener(e -> {
            boolean b = ((JCheckBox) e.getSource()).isSelected();
            progress02.setForeground(b ? new Color(255, 0, 0, 100) : progress01.getForeground());
            layerUI.isPreventing = b;
            p.repaint();
        });

        JButton button = new JButton("Start");
        button.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = new BackgroundTask();
            worker.addPropertyChangeListener(new ProgressListener(progress01));
            worker.execute();
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(check);
        box.add(Box.createHorizontalStrut(2));
        box.add(button);
        box.add(Box.createHorizontalStrut(2));

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

class BlockedColorLayerUI<V extends Component> extends LayerUI<V> {
    protected boolean isPreventing;
    private transient BufferedImage buf;

    @Override public void paint(Graphics g, JComponent c) {
        if (isPreventing && c instanceof JLayer) {
            Dimension d = ((JLayer<?>) c).getView().getSize();
            buf = Optional.ofNullable(buf)
                .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
                .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));

            Graphics2D g2 = buf.createGraphics();
            super.paint(g2, c);
            g2.dispose();

            Image image = c.createImage(new FilteredImageSource(buf.getSource(), new RedGreenChannelSwapFilter()));
            // BUG: cause an infinite repaint loop: g.drawImage(image, 0, 0, c);
            g.drawImage(image, 0, 0, null);
        } else {
            super.paint(g, c);
        }
    }
}

class RedGreenChannelSwapFilter extends RGBImageFilter {
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (int) ((argb >> 16) & 0xFF);
        int g = (int) ((argb >> 8) & 0xFF);
        int b = (int) (argb & 0xFF);
        return (argb & 0xFF000000) | (g << 16) | (r << 8) | b;
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
