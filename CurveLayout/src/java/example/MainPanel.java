// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel panel2 = new JPanel() {
      protected static final double A2 = 4d;
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Rectangle r = SwingUtilities.calculateInnerArea(this, null);
        g2.translate(r.x, r.y);
        g2.setPaint(Color.RED);
        int px = 0;
        int py = 0;
        for (int x = 0; x < r.width; x++) {
          int y = (int) Math.pow(x / A2, 2d);
          g2.drawLine(px, py, x, y);
          px = x;
          py = y;
        }
        g2.dispose();
      }

      @Override public void updateUI() {
        super.updateUI();
        setLayout(new FlowLayout() {
          @Override public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
              int nmembers = target.getComponentCount();
              if (nmembers <= 0 || !(target instanceof JComponent)) {
                return;
              }
              Rectangle r = SwingUtilities.calculateInnerArea((JComponent) target, null);
              int vgap = getVgap();
              int hgap = getHgap();
              int rh = (r.height - vgap * 2) / nmembers;
              int x = r.x + hgap;
              int y = r.y + vgap;
              for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                  Dimension d = m.getPreferredSize();
                  m.setSize(d.width, d.height);
                  m.setLocation(x, y);
                  y += vgap + Math.min(rh, d.height);
                  x = (int) (A2 * Math.sqrt(y));
                }
              }
            }
          }
        });
      }
    };

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(initPanel("FlowLayout(LEFT)", panel1));
    p.add(initPanel("y=Math.pow(x/4.0,2.0)", panel2));
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
