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
    SpringLayout layout = new SpringLayout();

    JPanel panel = new JPanel(layout);
    panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));

    JLabel l1 = new JLabel("label: 5%, 5%, 90%, 55%", SwingConstants.CENTER);
    l1.setOpaque(true);
    l1.setBackground(Color.ORANGE);
    l1.setBorder(BorderFactory.createLineBorder(Color.RED));

    JButton l2 = new JButton("button: 50%, 65%, 40%, 30%");
    // JLabel l2 = new JLabel("label: 50%, 65%, 40%, 30%", SwingConstants.CENTER);
    // l2.setBorder(BorderFactory.createLineBorder(Color.GREEN));

    setScaleAndAdd(panel, layout, l1, new Rectangle2D.Float(.05f, .05f, .90f, .55f));
    setScaleAndAdd(panel, layout, l2, new Rectangle2D.Float(.50f, .65f, .40f, .30f));

    // addComponentListener(new ComponentAdapter() {
    //   @Override public void componentResized(ComponentEvent e) {
    //     initLayout();
    //   }
    // });
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void setScaleAndAdd(Container p, SpringLayout layout, Component c, Rectangle2D r) {
    Spring pw = layout.getConstraint(SpringLayout.WIDTH, p);
    Spring ph = layout.getConstraint(SpringLayout.HEIGHT, p);

    SpringLayout.Constraints sc = layout.getConstraints(c);
    sc.setX(Spring.scale(pw, (float) r.getX()));
    sc.setY(Spring.scale(ph, (float) r.getY()));
    sc.setWidth(Spring.scale(pw, (float) r.getWidth()));
    sc.setHeight(Spring.scale(ph, (float) r.getHeight()));

    p.add(c);
  }

  // public void initLayout() {
  //   SpringLayout layout = new SpringLayout();
  //   Insets i = panel.getInsets();
  //   int w = panel.getWidth() - i.left - i.right;
  //   int h = panel.getHeight() - i.top - i.bottom;
  //
  //   l1.setPreferredSize(new Dimension(w * 90 / 100, h * 55 / 100));
  //   l2.setPreferredSize(new Dimension(w * 40 / 100, h * 30 / 100));
  //
  //   layout.putConstraint(SpringLayout.WEST, l1, w * 5 / 100, SpringLayout.WEST, panel);
  //   layout.putConstraint(SpringLayout.NORTH, l1, h * 5 / 100, SpringLayout.NORTH, panel);
  //   layout.putConstraint(SpringLayout.WEST, l2, w * 50 / 100, SpringLayout.WEST, panel);
  //   layout.putConstraint(SpringLayout.SOUTH, l2, -h * 5 / 100, SpringLayout.SOUTH, panel);
  //
  //   panel.setLayout(layout);
  //   panel.revalidate();
  // }

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
