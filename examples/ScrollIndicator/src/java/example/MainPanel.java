// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public final class MainPanel extends JPanel {
  private static final String LF = "\n";

  private MainPanel() {
    super(new BorderLayout());
    StringBuilder buf = new StringBuilder();
    IntStream.range(0, 100).forEach(i -> buf.append(i).append(LF));
    JScrollPane scroll = new JScrollPane(new JTextArea(buf.toString()));
    BoundedRangeModel model = scroll.getVerticalScrollBar().getModel();
    JProgressBar progress = new ScrollIndicator(model);
    scroll.setColumnHeaderView(progress);
    add(scroll);
    // add(progress, BorderLayout.SOUTH);
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
      Logger.getGlobal().severe(ex::getMessage);
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

class ScrollIndicator extends JProgressBar {
  protected ScrollIndicator(BoundedRangeModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    setUI(new ScrollIndicatorUI());
    setBorder(BorderFactory.createEmptyBorder());
  }

  @Override public double getPercentComplete() {
    long span = model.getMaximum() - model.getMinimum();
    double currentValue = model.getValue() + model.getExtent();
    return (currentValue - model.getMinimum()) / span;
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (getOrientation() == HORIZONTAL) {
      d.height = 4;
    } else {
      d.width = 4;
    }
    return d;
  }
}

class ScrollIndicatorUI extends BasicProgressBarUI {
  @Override public void paintDeterminate(Graphics g, JComponent c) {
    Insets b = progressBar.getInsets();
    Rectangle r = SwingUtilities.calculateInnerArea(progressBar, null);
    // BoundedRangeModel m = progressBar.getModel();
    if (!r.isEmpty()) {
      // int range = m.getMaximum() - m.getMinimum();
      // int extent = (int) Math.floor(r.width *  m.getExtent() / (float) range);
      int amountFull = getAmountFull(b, r.width, r.height); // + extent;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(UIManager.getColor("ProgressBar.foreground"));
      if (progressBar.getOrientation() == SwingConstants.HORIZONTAL) {
        g2.fillRect(r.x, r.y, amountFull, r.height);
      } else { // VERTICAL
        g2.fillRect(r.x, r.y + r.height - amountFull, r.width, amountFull);
      }
      // Deal with possible text painting
      if (progressBar.isStringPainted()) {
        paintString(g2, r.x, r.y, r.width, r.height, amountFull, b);
      }
      g2.dispose();
    }
  }
}
