// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Fade mode = Fade.IN;

  private MainPanel() {
    super(new BorderLayout());

    Timer animator = new Timer(25, null);
    AtomicInteger alpha = new AtomicInteger(10);

    ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
    Component fade = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha.get() * .1f));
        icon.paintIcon(this, g2, 0, 0);
        g2.dispose();
      }
    };

    animator.addActionListener(e -> {
      if (mode == Fade.IN && alpha.get() < 10) {
        alpha.incrementAndGet(); // alpha += 1;
      } else if (mode == Fade.OUT && alpha.get() > 0) {
        alpha.decrementAndGet(); // alpha -= 1;
      } else {
        animator.stop();
      }
      fade.repaint();
    });

    JButton button = new JButton("Fade In/Out");
    button.addActionListener(e -> {
      mode = mode.toggle();
      animator.start();
    });

    add(fade);
    add(button, BorderLayout.SOUTH);
    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

enum Fade {
  IN, OUT;
  public Fade toggle() {
    return this.equals(IN) ? OUT : IN;
  }
}
