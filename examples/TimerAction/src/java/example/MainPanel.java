// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Random;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel c1 = new JPanel(new GridLayout(10, 10));
    JPanel c2 = new JPanel(new GridLayout(10, 10));
    Random random = new Random();
    Timer timer = new Timer(16, null);
    IntStream.range(0, 100).forEach(i -> {
      c1.add(new Tile1(random));
      c2.add(new Tile2(random, timer));
    });
    c2.addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
          timer.stop();
        }
      }
    });

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Timer: 100", c1);
    tabs.addTab("Timer: 1, ActionListener: 100", c2);
    tabs.addTab("Timer: 1, ActionListener: 1", makeTilePanel(random));
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeTilePanel(Random rnd) {
    JPanel p = new JPanel(new GridLayout(10, 10));
    IntStream.range(0, 100).forEach(i -> {
      JLabel l = new JLabel();
      l.setOpaque(true);
      p.add(l);
    });
    Timer timer = new Timer(16, e -> IntStream.range(0, 100).forEach(i -> {
      Component c = p.getComponent(i);
      int red = rnd.nextInt(256);
      c.setBackground(new Color(red, 255 - red, 0));
    }));
    p.addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
          timer.stop();
        }
      }
    });
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

class Tile1 extends JComponent {
  // java - javax.swing.Timer slowdown in Java7u40 - Stack Overflow
  // https://stackoverflow.com/questions/18933986/javax-swing-timer-slowdown-in-java7u40
  private int red;
  private final Timer timer = new Timer(16, null);
  private transient HierarchyListener listener;

  protected Tile1(Random rnd) {
    super();
    timer.addActionListener(e -> {
      red = rnd.nextInt(255);
      repaint();
    });
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
          timer.stop();
        }
      }
    };
    addHierarchyListener(listener);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(10, 10);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (isVisible()) {
      if (!timer.isRunning()) {
        timer.start();
      }
    } else {
      timer.stop();
    }
    g.setColor(new Color(red, 255 - red, 0));
    g.fillRect(0, 0, getWidth(), getHeight());
  }
}

class Tile2 extends JComponent {
  private int red;

  protected Tile2(Random rnd, Timer timer) {
    super();
    timer.addActionListener(e -> {
      red = rnd.nextInt(255);
      repaint();
    });
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(10, 10);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(new Color(red, 255 - red, 0));
    g.fillRect(0, 0, getWidth(), getHeight());
  }
}
