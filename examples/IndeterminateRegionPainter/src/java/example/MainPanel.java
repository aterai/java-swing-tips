// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

public final class MainPanel extends JPanel {
  private transient SwingWorker<String, Void> worker;

  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel model = new DefaultBoundedRangeModel();
    JProgressBar progressBar0 = new JProgressBar(model);

    JProgressBar progressBar1 = new JProgressBar(model);
    UIDefaults d = new UIDefaults();
    d.put(
        "ProgressBar[Enabled+Indeterminate].foregroundPainter",
        new IndeterminateRegionPainter()
    );
    progressBar1.putClientProperty("Nimbus.Overrides", d);

    // UIManager.put("ProgressBar.cycleTime", 1000);
    // UIManager.put("ProgressBar.repaintInterval", 10);
    // progressBar1.setUI(new BasicProgressBarUI() {
    //   @Override protected int getBoxLength(int availableLength, int otherDimension) {
    //     return availableLength; // (int) Math.round(availableLength / 6d);
    //   }
    //
    //   @Override public void paintIndeterminate(Graphics g, JComponent c) {
    //     if (!(g instanceof Graphics2D)) {
    //       return;
    //     }
    //
    //     Insets b = progressBar.getInsets(); // area for border
    //     int barRectWidth = progressBar.getWidth() - b.right - b.left;
    //     int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
    //
    //     if (barRectWidth <= 0 || barRectHeight <= 0) {
    //       return;
    //     }
    //
    //     // Paint the bouncing box.
    //     boxRect = getBox(boxRect);
    //     if (Objects.nonNull(boxRect)) {
    //       int w = 10;
    //       int x = getAnimationIndex();
    //       System.out.println(x);
    //       GeneralPath p = new GeneralPath();
    //       p.moveTo(boxRect.x + w * .5f, boxRect.y);
    //       p.lineTo(boxRect.x + w, boxRect.y + boxRect.height);
    //       p.lineTo(boxRect.x + w * .5f, boxRect.y + boxRect.height);
    //       p.lineTo(boxRect.x, boxRect.y);
    //       p.closePath();
    //       Graphics2D g2 = (Graphics2D) g.create();
    //       g2.setRenderingHint(
    //           RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //       g2.setPaint(progressBar.getForeground());
    //       AffineTransform at = AffineTransform.getTranslateInstance(x, 0);
    //       for (int i = -x; i < boxRect.width; i += w) {
    //         g2.fill(AffineTransform.getTranslateInstance(i, 0).createTransformedShape(p));
    //       }
    //       g2.dispose();
    //     }
    //   }
    // });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeTitledPanel("Default", progressBar0));
    p.add(makeTitledPanel("ProgressBar[Indeterminate].foregroundPainter", progressBar1));

    JButton button = new JButton("Test start");
    button.addActionListener(e -> {
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
      progressBar0.setIndeterminate(true);
      progressBar1.setIndeterminate(true);
      worker = new BackgroundTask();
      worker.addPropertyChangeListener(new ProgressListener(progressBar0));
      worker.addPropertyChangeListener(new ProgressListener(progressBar1));
      worker.execute();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(5));

    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable() && Objects.nonNull(worker)) {
        // System.out.println("DISPOSE_ON_CLOSE");
        worker.cancel(true);
        // worker = null;
      }
    });
    add(p);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;
    p.add(cmp, c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
  @Override protected String doInBackground() throws InterruptedException {
    Thread.sleep(5000);
    int current = 0;
    int lengthOfTask = 100;
    while (current <= lengthOfTask && !isCancelled()) {
      doSomething();
      setProgress(100 * current / lengthOfTask);
      current++;
    }
    return "Done";
  }

  protected void doSomething() throws InterruptedException {
    Thread.sleep(50);
  }
}

class ProgressListener implements PropertyChangeListener {
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      int progress = (Integer) e.getNewValue();
      progressBar.setValue(progress);
    }
  }
}

@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
class IndeterminateRegionPainter extends AbstractRegionPainter {
  // Copied from javax.swing.plaf.nimbus.ProgressBarPainter.java
  private static final String KEY = "nimbusOrange";
  private final Color color17 = decodeColor(KEY, .0000000000f, .00000000f, .0000000000f, -156);
  private final Color color18 = decodeColor(KEY, -.0157965120f, .02094239f, -.1529411700f, 0);
  private final Color color19 = decodeColor(KEY, -.0043216050f, .02094239f, -.0745098000f, 0);
  private final Color color20 = decodeColor(KEY, -.0080213990f, .02094239f, -.1019607800f, 0);
  private final Color color21 = decodeColor(KEY, -.0117069040f, -.17905760f, -.0235294100f, 0);
  private final Color color22 = decodeColor(KEY, -.0486912540f, .02094239f, -.3019608000f, 0);
  private final Color color23 = decodeColor(KEY, .0039403290f, -.73753220f, .1764705800f, 0);
  private final Color color24 = decodeColor(KEY, .0055067390f, -.46764207f, .1098039150f, 0);
  private final Color color25 = decodeColor(KEY, .0042127445f, -.18595415f, .0470588200f, 0);
  private final Color color26 = decodeColor(KEY, .0047626942f, .02094239f, .0039215684f, 0);
  private final Color color27 = decodeColor(KEY, .0047626942f, -.15147138f, .1607843000f, 0);
  private final Color color28 = decodeColor(KEY, .0106654760f, -.27317524f, .2509803800f, 0);
  private final Insets ins = new Insets(5, 5, 5, 5);
  private final PaintContext ctx = new PaintContext(ins, new Dimension(29, 19), false);
  private Rectangle2D rect = new Rectangle2D.Float();
  // private Path2D path = new Path2D.Float();

  @Override public void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
    // path = decodePath1();
    g.setPaint(color17);
    g.fill(decodePath1());
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
    // path.reset();
    Path2D path = new Path2D.Float();
    path.moveTo(decodeX(1f), decodeY(.21111111f));
    path.curveTo(decodeAnchorX(1f, -2f), decodeAnchorY(.21111111f, 0f),
        decodeAnchorX(.21111111f, 0f), decodeAnchorY(1f, -2f),
        decodeX(.21111111f), decodeY(1f));
    path.curveTo(decodeAnchorX(.21111111f, 0f), decodeAnchorY(1f, 2f),
        decodeAnchorX(.21111111f, 0f), decodeAnchorY(2f, -2f),
        decodeX(.21111111f), decodeY(2f));
    path.curveTo(decodeAnchorX(.21111111f, 0f), decodeAnchorY(2f, 2f),
        decodeAnchorX(1f, -2f), decodeAnchorY(2.8222225f, 0f),
        decodeX(1f), decodeY(2.8222225f));
    path.curveTo(decodeAnchorX(1f, 2f), decodeAnchorY(2.8222225f, 0f),
        decodeAnchorX(3f, 0f), decodeAnchorY(2.8222225f, 0f),
        decodeX(3f), decodeY(2.8222225f));
    path.lineTo(decodeX(3f), decodeY(2.3333333f));
    path.lineTo(decodeX(.6666667f), decodeY(2.3333333f));
    path.lineTo(decodeX(.6666667f), decodeY(.6666667f));
    path.lineTo(decodeX(3f), decodeY(.6666667f));
    path.lineTo(decodeX(3f), decodeY(.2f));
    path.curveTo(decodeAnchorX(3f, 0f), decodeAnchorY(.2f, 0f),
        decodeAnchorX(1f, 2f), decodeAnchorY(.21111111f, 0f),
        decodeX(1f), decodeY(.21111111f));
    path.closePath();
    return path;
  }

  private Rectangle2D decodeRect3() {
    rect.setRect(decodeX(.4f), // x
        decodeY(.4f), // y
        decodeX(3f) - decodeX(.4f), // width
        decodeY(2.6f) - decodeY(.4f)); // height
    return rect;
  }

  private Rectangle2D decodeRect4() {
    rect.setRect(decodeX(.6f), // x
        decodeY(.6f), // y
        decodeX(2.8f) - decodeX(.6f), // width
        decodeY(2.4f) - decodeY(.6f)); // height
    return rect;
  }

  private Paint decodeGradient5(Shape s) {
    Rectangle2D bounds = s.getBounds2D();
    float x = (float) bounds.getX();
    float y = (float) bounds.getY();
    float w = (float) bounds.getWidth();
    float h = (float) bounds.getHeight();
    return decodeGradient(
        .5f * w + x, y, .5f * w + x, h + y,
        new float[] {
            .038709678f, .05483871f, .07096774f, .28064516f,
            .4903226f, .6967742f, .9032258f, .9241935f, .9451613f
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
        .5f * w + x, y, .5f * w + x, h + y,
        new float[] {
            .038709678f, .061290324f, .08387097f, .27258065f, .46129033f, .4903226f,
            .5193548f, .71774197f, .91612905f, .92419356f, .93225807f
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
