// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel panel = new JPanel(new SpringLayout());
    panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));

    JLabel l1 = new JLabel("label: 5%, 5%, 90%, 55%", SwingConstants.CENTER);
    l1.setOpaque(true);
    l1.setBackground(Color.ORANGE);
    l1.setBorder(BorderFactory.createLineBorder(Color.RED));
    setScaleAndAdd(panel, l1, new Rectangle2D.Float(.05f, .05f, .90f, .55f));

    JButton l2 = new JButton("button: 50%, 65%, 40%, 30%");
    setScaleAndAdd(panel, l2, new Rectangle2D.Float(.50f, .65f, .40f, .30f));

    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void setScaleAndAdd(Container p, Component c, Rectangle2D r) {
    LayoutManager lm = p.getLayout();
    if (lm instanceof SpringLayout) {
      SpringLayout layout = (SpringLayout) lm;
      Spring pw = layout.getConstraint(SpringLayout.WIDTH, p);
      Spring ph = layout.getConstraint(SpringLayout.HEIGHT, p);
      SpringLayout.Constraints sc = layout.getConstraints(c);
      sc.setX(Spring.scale(pw, (float) r.getX()));
      sc.setY(Spring.scale(ph, (float) r.getY()));
      sc.setWidth(Spring.scale(pw, (float) r.getWidth()));
      sc.setHeight(Spring.scale(ph, (float) r.getHeight()));
      p.add(c);
    }
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
