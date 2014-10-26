package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
//import javax.swing.SwingWorker;
//import org.jdesktop.swingworker.SwingWorker;

public final class MainPanel extends JPanel {
    private final JTextArea area     = new JTextArea();
    private final JProgressBar bar   = new JProgressBar();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton  = new JButton(new RunAction());
    private final JButton canButton  = new JButton(new CancelAction());
    private final AnimatedLabel anil = new AnimatedLabel();
    private transient Task worker;

    public MainPanel() {
        super(new BorderLayout());
        area.setEditable(false);
        area.setLineWrap(true);
        Box box = Box.createHorizontalBox();
        box.add(anil);
        box.add(Box.createHorizontalGlue());
        box.add(runButton);
        box.add(canButton);
        add(box, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        add(new JScrollPane(area));
        setPreferredSize(new Dimension(320, 240));
    }

    class RunAction extends AbstractAction {
        public RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            anil.startAnimation();
            statusPanel.removeAll();
            statusPanel.add(bar);
            statusPanel.revalidate();
            bar.setIndeterminate(true);
            worker = new Task() {
                @Override protected void process(List<String> chunks) {
                    System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
                    if (!isDisplayable()) {
                        cancel(true);
                        return;
                    }
                    for (String message: chunks) {
                        appendLine(message);
                    }
                }
                @Override public void done() {
                    //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
                    if (!isDisplayable()) {
                        cancel(true);
                        return;
                    }
                    anil.stopAnimation();
                    runButton.setEnabled(true);
                    canButton.setEnabled(false);
                    statusPanel.remove(bar);
                    statusPanel.revalidate();
                    try {
                        if (isCancelled()) {
                            appendLine("Cancelled");
                        } else {
                            appendLine(get());
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        appendLine("Exception");
                    }
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(bar));
            worker.execute();
        }
    }

    class CancelAction extends AbstractAction {
        public CancelAction() {
            super("cancel");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
    }

//     private boolean isCancelled() {
//         return (worker != null) ? worker.isCancelled() : true;
//     }

    private void appendLine(String str) {
        area.append(str + "\n");
        area.setCaretPosition(area.getDocument().getLength());
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
        frame.pack();
        //frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, String> {
    @Override public String doInBackground() {
        //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            if (isCancelled()) {
                cancel(true);
            }
            return "Interrupted";
        }
        int current = 0;
        int lengthOfTask = 120; //list.size();
        publish("Length Of Task: " + lengthOfTask);
        publish("------------------------------");
        while (current < lengthOfTask && !isCancelled()) {
            try {
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
    ProgressListener(JProgressBar progressBar) {
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

class AnimatedLabel extends JLabel implements ActionListener {
    private final Timer animator;
    private final AnimeIcon icon = new AnimeIcon();
    //private final AnimeIcon2 icon = new AnimeIcon2();
    //private final AnimeIcon3 icon = new AnimeIcon3();
    //private final AnimeIcon4 icon = new AnimeIcon4();
    public AnimatedLabel() {
        super();
        animator = new Timer(100, this);
        setIcon(icon);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !isDisplayable()) {
                    stopAnimation();
                }
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        icon.next();
        repaint();
    }
    public void startAnimation() {
        icon.setRunning(true);
        animator.start();
    }
    public void stopAnimation() {
        icon.setRunning(false);
        animator.stop();
    }
}

class AnimeIcon implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Color ELLIPSE_COLOR = new Color(.5f, .5f, .5f);
    private static final double R  = 2d;
    private static final double SX = 1d;
    private static final double SY = 1d;
    private static final int WIDTH  = (int) (R * 8 + SX * 2);
    private static final int HEIGHT = (int) (R * 8 + SY * 2);
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
        new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

    private boolean isRunning;
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(c == null ? Color.WHITE : c.getBackground());
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ELLIPSE_COLOR);
        g2.translate(x, y);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        //g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return WIDTH;
    }
    @Override public int getIconHeight() {
        return HEIGHT;
    }
}

class AnimeIcon2 implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Color ELLIPSE_COLOR = new Color(.5f, .8f, .5f);
    private final List<Shape> list = new ArrayList<>();
    private final Dimension dim;
    private boolean isRunning;
    public AnimeIcon2() {
        super();
        int r = 4;
        Shape s = new Ellipse2D.Float(0, 0, 2 * r, 2 * r);
        for (int i = 0; i < 8; i++) {
            AffineTransform at = AffineTransform.getRotateInstance(i * 2 * Math.PI / 8);
            at.concatenate(AffineTransform.getTranslateInstance(r, r));
            list.add(at.createTransformedShape(s));
        }
        //int d = (int) (r * 2*(1 + 2 * Math.sqrt(2)));
        int d = (int) r * 2 * (1 + 3); // 2 * Math.sqrt(2) is nearly equal to 3.
        dim = new Dimension(d, d);
    }
    @Override public int getIconWidth() {
        return dim.width;
    }
    @Override public int getIconHeight() {
        return dim.height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(c == null ? Color.WHITE : c.getBackground());
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ELLIPSE_COLOR);
        int xx = x + dim.width / 2;
        int yy = y + dim.height / 2;
        g2.translate(xx, yy);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        //g2.translate(-xx, -yy);
        g2.dispose();
    }
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}

class AnimeIcon3 implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Color ELLIPSE_COLOR = new Color(.9f, .7f, .7f);
    private final List<Shape> list = new ArrayList<>();
    private final Dimension dim;
    private boolean isRunning;
    private int rotate = 45;
    public AnimeIcon3() {
        super();
        int r = 4;
        Shape s = new Ellipse2D.Float(0, 0, 2 * r, 2 * r);
        for (int i = 0; i < 8; i++) {
            AffineTransform at = AffineTransform.getRotateInstance(i * 2 * Math.PI / 8);
            at.concatenate(AffineTransform.getTranslateInstance(r, r));
            list.add(at.createTransformedShape(s));
        }
        int d = (int) r * 2 * (1 + 3);
        dim = new Dimension(d, d);
    }
    @Override public int getIconWidth() {
        return dim.width;
    }
    @Override public int getIconHeight() {
        return dim.height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(c == null ? Color.WHITE : c.getBackground());
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ELLIPSE_COLOR);
        int xx = x + dim.width / 2;
        int yy = y + dim.height / 2;
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), xx, yy);
        at.concatenate(AffineTransform.getTranslateInstance(xx, yy));
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(at.createTransformedShape(list.get(i)));
        }
        g2.dispose();
    }
    public void next() {
        if (isRunning) {
            rotate = rotate < 360 ? rotate + 45 : 45;
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}

class AnimeIcon4 implements Icon, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int R = 4;
    private static final Color ELLIPSE_COLOR = new Color(.5f, .8f, .5f);
    private final Dimension dim;
    private boolean isRunning;
    private final List<Shape> list = new ArrayList<>();
    public AnimeIcon4() {
        super();
        int d = (int) R * 2 * (1 + 3);
        dim = new Dimension(d, d);

        Ellipse2D.Float cricle = new Ellipse2D.Float(R, R, d - 2 * R, d - 2 * R);
        PathIterator i = new FlatteningPathIterator(cricle.getPathIterator(null), R);
        float[] coords = new float[6];
        int idx = 0;
        while (!i.isDone()) {
            i.currentSegment(coords);
            if (idx < 8) { // XXX
                list.add(new Ellipse2D.Float(coords[0] - R, coords[1] - R, 2 * R, 2 * R));
                idx++;
            }
            i.next();
        }
    }
    @Override public int getIconWidth() {
        return dim.width;
    }
    @Override public int getIconHeight() {
        return dim.height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(c == null ? Color.WHITE : c.getBackground());
        g2.fillRect(x, y, getIconWidth(), getIconHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ELLIPSE_COLOR);
        int size = list.size();
        for (int i = 0; i < size; i++) {
            float alpha = isRunning ? (i + 1) / (float) size : .5f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.fill(list.get(i));
        }
        g2.dispose();
    }
    public void next() {
        if (isRunning) {
            list.add(list.remove(0));
        }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
