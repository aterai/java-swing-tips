package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.nimbus.*;

public class MainPanel extends JPanel implements HierarchyListener {
    private final BoundedRangeModel model = new DefaultBoundedRangeModel();
    private final JProgressBar progressBar0 = new JProgressBar(model);
    private final JProgressBar progressBar1 = new JProgressBar(model);
    private final JProgressBar progressBar2 = new JProgressBar(model);
    private final JProgressBar progressBar3 = new JProgressBar(model);
    private final JProgressBar progressBar4 = new JProgressBar(model);
    private SwingWorker<String, Void> worker;
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("ProgressBar.cycleTime", 1000);
        UIManager.put("ProgressBar.repaintInterval", 10);

        progressBar1.setUI(new StripedProgressBarUI(true,  true));
        progressBar2.setUI(new StripedProgressBarUI(true,  false));
        progressBar3.setUI(new StripedProgressBarUI(false, true));
        progressBar4.setUI(new StripedProgressBarUI(false, false));

        JPanel p = new JPanel(new GridLayout(5, 1));
        p.add(makePanel(progressBar0));
        p.add(makePanel(progressBar1));
        p.add(makePanel(progressBar2));
        p.add(makePanel(progressBar3));
        p.add(makePanel(progressBar4));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Test start") {
            @Override public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(worker) && !worker.isDone()) {
                    worker.cancel(true);
                }
                progressBar0.setIndeterminate(true);
                progressBar1.setIndeterminate(true);
                progressBar2.setIndeterminate(true);
                progressBar3.setIndeterminate(true);
                progressBar4.setIndeterminate(true);
                worker = new Task();
                worker.addPropertyChangeListener(new ProgressListener(progressBar0));
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.addPropertyChangeListener(new ProgressListener(progressBar2));
                worker.addPropertyChangeListener(new ProgressListener(progressBar3));
                worker.addPropertyChangeListener(new ProgressListener(progressBar4));
                worker.execute();
            }
        }));
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

    private static JComponent makePanel(JComponent cmp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1d;
        c.gridy   = 0;
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class StripedProgressBarUI extends BasicProgressBarUI {
    private final boolean dir;
    private final boolean slope;
    public StripedProgressBarUI(boolean dir, boolean slope) {
        super();
        this.dir = dir;
        this.slope = slope;
    }
    @Override protected int getBoxLength(int availableLength, int otherDimension) {
        return availableLength; //(int)Math.round(availableLength/6.0);
    }
    @Override public void paintIndeterminate(Graphics g, JComponent c) {
        //if (!(g instanceof Graphics2D)) {
        //    return;
        //}

        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth  = progressBar.getWidth() - b.right - b.left;
        int barRectHeight = progressBar.getHeight() - b.top - b.bottom;

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        // Paint the striped box.
        boxRect = getBox(boxRect);
        if (Objects.nonNull(boxRect)) {
            int w = 10;
            int x = getAnimationIndex();
            GeneralPath p = new GeneralPath();
            if (dir) {
                p.moveTo(boxRect.x,           boxRect.y);
                p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
                p.lineTo(boxRect.x + w,       boxRect.y + boxRect.height);
                p.lineTo(boxRect.x + w * .5f, boxRect.y);
            } else {
                p.moveTo(boxRect.x,           boxRect.y + boxRect.height);
                p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
                p.lineTo(boxRect.x + w,       boxRect.y);
                p.lineTo(boxRect.x + w * .5f, boxRect.y);
            }
            p.closePath();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(progressBar.getForeground());
            if (slope) {
                for (int i = boxRect.width + x; i > -w; i -= w) {
                    g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
                }
            } else {
                for (int i = -x; i < boxRect.width; i += w) {
                    g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
                }
            }
            g2.dispose();
        }
    }
}

class Task extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        try { // dummy task
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            return "Interrupted";
        }
        int current = 0;
        int lengthOfTask = 100;
        while (current <= lengthOfTask && !isCancelled()) {
            try { // dummy task
                Thread.sleep(50);
            } catch (InterruptedException ie) {
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
    public ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
