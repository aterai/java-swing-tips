// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JProgressBar progress1 = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new ProgressCircleUI());
      }
    };
    progress1.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
    progress1.setForeground(new Color(0xAA_FF_AA_AA, true));

    JProgressBar progress2 = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new ProgressCircleUI());
      }
    };
    progress2.setStringPainted(true);
    progress2.setFont(progress2.getFont().deriveFont(24f));
    progress2.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

    JSlider slider = new JSlider();
    slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    progress1.setModel(slider.getModel());

    JButton button = new JButton("start");
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      b.setEnabled(false);
      SwingWorker<String, Void> worker = new BackgroundTask() {
        @Override protected void done() {
          b.setEnabled(true);
        }
      };
      worker.addPropertyChangeListener(new ProgressListener(progress2));
      worker.execute();
    });

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(progress1);
    p.add(progress2);

    add(slider, BorderLayout.NORTH);
    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ProgressCircleUI extends BasicProgressBarUI {
  @Override public Dimension getPreferredSize(JComponent c) {
    Dimension d = super.getPreferredSize(c);
    int v = Math.max(d.width, d.height);
    d.setSize(v, v);
    return d;
  }

  @Override public void paint(Graphics g, JComponent c) {
    // Insets b = progressBar.getInsets(); // area for border
    // int barRectWidth = progressBar.getWidth() - b.right - b.left;
    // int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
    // if (barRectWidth <= 0 || barRectHeight <= 0) {
    //   return;
    // }
    Rectangle rect = SwingUtilities.calculateInnerArea(progressBar, null);
    if (rect.isEmpty()) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double start = 90d;
    double degree = 360d * progressBar.getPercentComplete();
    double sz = Math.min(rect.width, rect.height);
    double cx = rect.getCenterX();
    double cy = rect.getCenterY();
    double or = sz * .5;
    double ir = or * .5; // .8;
    Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2d, ir * 2d);
    Shape outer = new Ellipse2D.Double(cx - or, cy - or, sz, sz);
    Shape sector = new Arc2D.Double(cx - or, cy - or, sz, sz, start, degree, Arc2D.PIE);

    Area foreground = new Area(sector);
    Area background = new Area(outer);
    Area hole = new Area(inner);

    foreground.subtract(hole);
    background.subtract(hole);

    // Draw the track
    g2.setPaint(new Color(0xDD_DD_DD));
    g2.fill(background);

    // Draw the circular sector
    // AffineTransform at = AffineTransform.getScaleInstance(-1.0, 1.0);
    // at.translate(-(barRectWidth + b.left * 2), 0);
    // AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(degree), cx, cy);
    // g2.fill(at.createTransformedShape(area));
    g2.setPaint(progressBar.getForeground());
    g2.fill(foreground);
    g2.dispose();

    // Deal with possible text painting
    if (progressBar.isStringPainted()) {
      Insets ins = progressBar.getInsets();
      paintString(g, rect.x, rect.y, rect.width, rect.height, 0, ins);
    }
  }

  // https://ateraimemo.com/Swing/ProgressBarSelectionColor.html
  // @Override protected Color getSelectionForeground() {
  //   return Color.GREEN; // Not used in ProgressCircleUI
  // }

  // @Override protected Color getSelectionBackground() {
  //   return Color.RED; // a progress string color
  // }
}

class BackgroundTask extends SwingWorker<String, Void> {
  @Override protected String doInBackground() throws InterruptedException {
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
