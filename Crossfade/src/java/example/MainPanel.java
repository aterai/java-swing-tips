// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected int alpha = 10;
  protected Crossfade mode = Crossfade.IN;

  public MainPanel() {
    super(new BorderLayout());

    Class<?> clz = MainPanel.class;
    JCheckBox check = new JCheckBox("Crossfade Type?", true);
    ImageIcon icon1 = new ImageIcon(clz.getResource("test.png"));
    ImageIcon icon2 = new ImageIcon(clz.getResource("test.jpg"));
    JButton button = new JButton("change");
    Component crossfade = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (check.isSelected()) {
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha * .1f));
        }
        g2.drawImage(icon1.getImage(), 0, 0, icon1.getIconWidth(), icon1.getIconHeight(), this);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * .1f));
        g2.drawImage(icon2.getImage(), 0, 0, icon2.getIconWidth(), icon2.getIconHeight(), this);
        g2.dispose();
      }
    };

    Timer animator = new Timer(50, null);
    animator.addActionListener(e -> {
      if (mode == Crossfade.IN && alpha < 10) {
        alpha += 1;
      } else if (mode == Crossfade.OUT && alpha > 0) {
        alpha -= 1;
      } else {
        animator.stop();
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

enum Crossfade {
  IN, OUT;
  public Crossfade toggle() {
    return this.equals(IN) ? OUT : IN;
  }
}
