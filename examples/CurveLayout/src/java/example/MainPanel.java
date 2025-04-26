// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(initPanel("FlowLayout(LEFT)", new JPanel(new FlowLayout(FlowLayout.LEFT))));
    p.add(initPanel("y=Math.pow(x/4.0,2.0)", new CurvePanel()));
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component initPanel(String title, JComponent p) {
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(new JCheckBox("111111111111111"));
    p.add(new JCheckBox("2222222222"));
    p.add(new JCheckBox("33333333"));
    p.add(new JCheckBox("4444444444"));
    p.add(new JCheckBox("555555555555"));
    return p;
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

class CurvePanel extends JPanel {
  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    g2.translate(r.x, r.y);
    g2.setPaint(Color.RED);
    int px = 0;
    int py = 0;
    for (int x = 0; x < r.width; x++) {
      int y = (int) Math.pow(x / CurveLayout.A2, 2d);
      g2.drawLine(px, py, x, y);
      px = x;
      py = y;
    }
    g2.dispose();
  }

  @Override public void updateUI() {
    super.updateUI();
    setLayout(new CurveLayout());
  }
}

class CurveLayout extends FlowLayout {
  public static final double A2 = 4d;

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      int members = target.getComponentCount();
      if (members <= 0 || !(target instanceof JComponent)) {
        return;
      }
      Rectangle r = SwingUtilities.calculateInnerArea((JComponent) target, null);
      int vg = getVgap();
      int hg = getHgap();
      int rh = (r.height - vg * 2) / members;
      int x = r.x + hg;
      int y = r.y + vg;
      for (Component m : target.getComponents()) {
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          m.setSize(d.width, d.height);
          m.setLocation(x, y);
          y += vg + Math.min(rh, d.height);
          x = (int) (A2 * Math.sqrt(y));
        }
      }
    }
  }
}
