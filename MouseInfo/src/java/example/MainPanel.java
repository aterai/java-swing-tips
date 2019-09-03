// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.Serializable;
import javax.swing.*;

public final class MainPanel extends JPanel implements ActionListener, HierarchyListener {
  private final Dimension panelDim = new Dimension(320, 240);
  private final Racket racket = new Racket(panelDim);
  private final JLabel absolute = new JLabel("absolute:");
  private final JLabel relative = new JLabel("relative:");
  private final Timer timer;

  public MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(absolute);
    box.add(relative);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box);
    timer = new Timer(10, this);
    addHierarchyListener(this);
  }

  @Override public Dimension getPreferredSize() {
    return panelDim;
  }

  @Override public void hierarchyChanged(HierarchyEvent e) {
    if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
      if (e.getComponent().isDisplayable()) {
        timer.start();
      } else {
        timer.stop();
      }
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    racket.draw(g);
  }

  @Override public void actionPerformed(ActionEvent e) {
    PointerInfo pi = MouseInfo.getPointerInfo();
    Point pt = pi.getLocation();
    absolute.setText("absolute:" + pt.toString());
    SwingUtilities.convertPointFromScreen(pt, this);
    relative.setText("relative:" + pt.toString());
    racket.move(pt.x);
    repaint();
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// http://d.hatena.ne.jp/aidiary/20070601/1251545490
class Racket implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final int WIDTH = 80;
  private static final int HEIGHT = 5;
  private int centerPos;
  private final Dimension parentSize;

  protected Racket(Dimension parentSize) {
    this.parentSize = parentSize;
    centerPos = parentSize.width / 2;
  }

  public void draw(Graphics g) {
    g.setColor(Color.RED);
    g.fillRect(centerPos - WIDTH / 2, parentSize.height - HEIGHT, WIDTH, HEIGHT);
  }

  public void move(int pos) {
    centerPos = pos;
    if (centerPos < WIDTH / 2) {
      centerPos = WIDTH / 2;
    } else if (centerPos > parentSize.width - WIDTH / 2) {
      centerPos = parentSize.width - WIDTH / 2;
    }
  }
}
