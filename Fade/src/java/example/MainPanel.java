// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Fade mode = Fade.IN;
  private int alpha = 10;

  private MainPanel() {
    super(new BorderLayout());

    Timer animator = new Timer(25, null);

    ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
    Component fade = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * .1f));
        icon.paintIcon(this, g2, 0, 0);
        g2.dispose();
      }
    };

    animator.addActionListener(e -> {
      if (mode == Fade.IN && alpha < 10) {
        alpha += 1;
      } else if (mode == Fade.OUT && alpha > 0) {
        alpha -= 1;
      } else {
        animator.stop();
      }
      fade.repaint();
    });

    JButton button1 = new JButton("Fade In");
    button1.addActionListener(e -> {
      mode = Fade.IN;
      animator.start();
    });

    JButton button2 = new JButton("Fade Out");
    button2.addActionListener(e -> {
      mode = Fade.OUT;
      animator.start();
    });

    add(fade);
    add(button1, BorderLayout.SOUTH);
    add(button2, BorderLayout.NORTH);
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum Fade { IN, OUT }
