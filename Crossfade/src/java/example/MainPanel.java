// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Crossfade mode = Crossfade.IN;

  private MainPanel() {
    super(new BorderLayout());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    JCheckBox check = new JCheckBox("Crossfade Type?", true);
    ImageIcon icon1 = new ImageIcon(Objects.requireNonNull(cl.getResource("example/test.png")));
    ImageIcon icon2 = new ImageIcon(Objects.requireNonNull(cl.getResource("example/test.jpg")));
    JButton button = new JButton("change");

    AtomicInteger alpha = new AtomicInteger(10);
    Component crossfade = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (check.isSelected()) {
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha.get() * .1f));
        }
        icon1.paintIcon(this, g2, 0, 0);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.get() * .1f));
        icon2.paintIcon(this, g2, 0, 0);
        g2.dispose();
      }
    };

    Timer animator = new Timer(50, e -> {
      if (mode == Crossfade.IN && alpha.get() < 10) {
        alpha.incrementAndGet(); // alpha += 1;
      } else if (mode == Crossfade.OUT && alpha.get() > 0) {
        alpha.decrementAndGet(); // alpha -= 1;
      } else {
        ((Timer) e.getSource()).stop();
      }
      crossfade.repaint();
    });

    button.addActionListener(e -> {
      mode = mode.toggle();
      animator.start();
    });

    add(crossfade);
    add(button, BorderLayout.NORTH);
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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

enum Crossfade {
  IN, OUT;
  public Crossfade toggle() {
    return this.equals(IN) ? OUT : IN;
  }
}
