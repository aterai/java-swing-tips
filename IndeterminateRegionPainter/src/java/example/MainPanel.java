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
import javax.swing.plaf.nimbus.*;

public class MainPanel extends JPanel {
    protected final BoundedRangeModel model = new DefaultBoundedRangeModel();
    protected final JProgressBar progressBar0 = new JProgressBar(model);
    protected final JProgressBar progressBar1;
    protected transient SwingWorker<String, Void> worker;

    public MainPanel() {
        super(new BorderLayout());

        UIDefaults d = new UIDefaults();
        d.put("ProgressBar[Enabled+Indeterminate].foregroundPainter", new IndeterminateRegionPainter());
        progressBar1 = new JProgressBar(model);
        progressBar1.putClientProperty("Nimbus.Overrides", d);

//         UIManager.put("ProgressBar.cycleTime", 1000);
//         UIManager.put("ProgressBar.repaintInterval", 10);
//         progressBar1.setUI(new BasicProgressBarUI() {
//             @Override protected int getBoxLength(int availableLength, int otherDimension) {
//                 return availableLength; //(int) Math.round(availableLength / 6d);
//             }
//             @Override public void paintIndeterminate(Graphics g, JComponent c) {
//                 if (!(g instanceof Graphics2D)) {
//                     return;
//                 }
//
//                 Insets b = progressBar.getInsets(); // area for border
//                 int barRectWidth  = progressBar.getWidth() - b.right - b.left;
//                 int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
//
//                 if (barRectWidth <= 0 || barRectHeight <= 0) {
//                     return;
//                 }
//
//                 // Paint the bouncing box.
//                 boxRect = getBox(boxRect);
//                 if (Objects.nonNull(boxRect)) {
//                     int w = 10;
//                     int x = getAnimationIndex();
//                     System.out.println(x);
//                     GeneralPath p = new GeneralPath();
//                     p.moveTo(boxRect.x + w * .5f, boxRect.y);
//                     p.lineTo(boxRect.x + w,       boxRect.y + boxRect.height);
//                     p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
//                     p.lineTo(boxRect.x,           boxRect.y);
//                     p.closePath();
//                     Graphics2D g2 = (Graphics2D) g.create();
//                     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                     g2.setPaint(progressBar.getForeground());
//                     AffineTransform at = AffineTransform.getTranslateInstance(x, 0);
//                     for (int i = -x; i < boxRect.width; i += w) {
//                         g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
//                     }
//                     g2.dispose();
//                 }
//             }
//         });

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(makeTitlePanel("Default", progressBar0));
        p.add(makeTitlePanel("ProgressBar[Indeterminate].foregroundPainter", progressBar1));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Test start") {
            @Override public void actionPerformed(ActionEvent e) {
                if (Objects.nonNull(worker) && !worker.isDone()) {
                    worker.cancel(true);
                }
                progressBar0.setIndeterminate(true);
                progressBar1.setIndeterminate(true);
                worker = new BackgroundTask();
                worker.addPropertyChangeListener(new ProgressListener(progressBar0));
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.execute();
            }
        }));
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
    private static JComponent makeTitlePanel(String title, JComponent cmp) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
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
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class BackgroundTask extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        try { // dummy task
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            return "Interrupted";
        }
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

class IndeterminateRegionPainter extends AbstractRegionPainter {
    // Copied from javax.swing.plaf.nimbus.ProgressBarPainter.java
    private static final String NIMBUS_ORANGE = "nimbusOrange";
    private final Color color17 = decodeColor(NIMBUS_ORANGE,   0f,            0f,          0f,       -156);
    private final Color color18 = decodeColor(NIMBUS_ORANGE, -.015796512f,   .02094239f, -.15294117f,   0);
    private final Color color19 = decodeColor(NIMBUS_ORANGE, -.004321605f,   .02094239f, -.0745098f,    0);
    private final Color color20 = decodeColor(NIMBUS_ORANGE, -.008021399f,   .02094239f, -.10196078f,   0);
    private final Color color21 = decodeColor(NIMBUS_ORANGE, -.011706904f,  -.1790576f,  -.02352941f,   0);
    private final Color color22 = decodeColor(NIMBUS_ORANGE, -.048691254f,   .02094239f, -.3019608f,    0);
    private final Color color23 = decodeColor(NIMBUS_ORANGE,  .003940329f,  -.7375322f,   .17647058f,   0);
    private final Color color24 = decodeColor(NIMBUS_ORANGE,  .005506739f,  -.46764207f,  .109803915f,  0);
    private final Color color25 = decodeColor(NIMBUS_ORANGE,  .0042127445f, -.18595415f,  .04705882f,   0);
    private final Color color26 = decodeColor(NIMBUS_ORANGE,  .0047626942f,  .02094239f,  .0039215684f, 0);
    private final Color color27 = decodeColor(NIMBUS_ORANGE,  .0047626942f, -.15147138f,  .1607843f,    0);
    private final Color color28 = decodeColor(NIMBUS_ORANGE,  .010665476f,  -.27317524f,  .25098038f,   0);
    private final PaintContext ctx = new PaintContext(new Insets(5, 5, 5, 5), new Dimension(29, 19), false);
    private Rectangle2D rect = new Rectangle2D.Float();
    private Path2D path = new Path2D.Float();
    @Override public void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        path = decodePath1();
        g.setPaint(color17);
        g.fill(path);
        rect = decodeRect3();
        g.setPaint(decodeGradient5(rect));
        g.fill(rect);
        rect = decodeRect4();
        g.setPaint(decodeGradient6(rect));
        g.fill(rect);
    }
    @Override public PaintContext getPaintContext() {
        return ctx;
    }
    private Path2D decodePath1() {
        path.reset();
        path.moveTo(decodeX(.6f), decodeY(.12666667f));
        path.curveTo(decodeAnchorX(.6000000238418579f, -2f), decodeAnchorY(.12666666507720947f, 0f), decodeAnchorX(.12666666507720947f, 0f), decodeAnchorY(.6000000238418579f,  -2f), decodeX(.12666667f), decodeY(.6f));
        path.curveTo(decodeAnchorX(.12666666507720947f, 0f), decodeAnchorY(.6000000238418579f,  2f), decodeAnchorX(.12666666507720947f, 0f), decodeAnchorY(2.4000000953674316f, -2f), decodeX(.12666667f), decodeY(2.4f));
        path.curveTo(decodeAnchorX(.12666666507720947f, 0f), decodeAnchorY(2.4000000953674316f, 2f), decodeAnchorX(.6000000238418579f, -2f), decodeAnchorY(2.8933334350585938f,  0f), decodeX(.6f),        decodeY(2.8933334f));
        path.curveTo(decodeAnchorX(.6000000238418579f,  2f), decodeAnchorY(2.8933334350585938f, 0f), decodeAnchorX(3f,                  0f), decodeAnchorY(2.8933334350585938f,  0f), decodeX(3f),         decodeY(2.8933334f));
        path.lineTo(decodeX(3f),  decodeY(2.6f));
        path.lineTo(decodeX(.4f), decodeY(2.6f));
        path.lineTo(decodeX(.4f), decodeY(.4f));
        path.lineTo(decodeX(3f),  decodeY(.4f));
        path.lineTo(decodeX(3f),  decodeY(.120000005f));
        path.curveTo(decodeAnchorX(3f, 0f), decodeAnchorY(.12000000476837158f, 0f), decodeAnchorX(.6000000238418579f, 2f), decodeAnchorY(.12666666507720947f, 0f), decodeX(.6f), decodeY(.12666667f));
        path.closePath();
        return path;
    }
    private Rectangle2D decodeRect3() {
        rect.setRect(decodeX(.4f), //x
                     decodeY(.4f), //y
                     decodeX(3f)   - decodeX(.4f), //width
                     decodeY(2.6f) - decodeY(.4f)); //height
        return rect;
    }
    private Rectangle2D decodeRect4() {
        rect.setRect(decodeX(.6f), //x
                     decodeY(.6f), //y
                     decodeX(2.8f) - decodeX(.6f), //width
                     decodeY(2.4f) - decodeY(.6f)); //height
        return rect;
    }
    private Paint decodeGradient5(Shape s) {
        Rectangle2D bounds = s.getBounds2D();
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        float w = (float) bounds.getWidth();
        float h = (float) bounds.getHeight();
        return decodeGradient(
            .5f * w + x, 0f * h + y, .5f * w + x, 1f * h + y,
            new float[] {
                .038709678f, .05483871f, .07096774f, .28064516f, .4903226f, .6967742f, .9032258f, .9241935f, .9451613f
              },
            new Color[] {
                color18,
                decodeColor(color18, color19, .5f),
                color19,
                decodeColor(color19, color20, .5f),
                color20,
                decodeColor(color20, color21, .5f),
                color21,
                decodeColor(color21, color22, .5f),
                color22
              });
    }
    private Paint decodeGradient6(Shape s) {
        Rectangle2D bounds = s.getBounds2D();
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        float w = (float) bounds.getWidth();
        float h = (float) bounds.getHeight();
        return decodeGradient(
            .5f * w + x, 0f * h + y, .5f * w + x, 1f * h + y,
            new float[] {
                .038709678f, .061290324f, .08387097f, .27258065f, .46129033f, .4903226f, .5193548f, .71774197f, .91612905f, .92419356f, .93225807f
              },
            new Color[] {
                color23,
                decodeColor(color23, color24, .5f),
                color24,
                decodeColor(color24, color25, .5f),
                color25,
                decodeColor(color25, color26, .5f),
                color26,
                decodeColor(color26, color27, .5f),
                color27,
                decodeColor(color27, color28, .5f),
                color28
              });
    }
}
