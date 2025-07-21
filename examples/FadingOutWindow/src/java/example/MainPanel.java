// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("open JWindow");
    button.addActionListener(e -> makeWindow(button));
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void makeWindow(JComponent c) {
    JWindow window = new JWindow();
    GraphicsConfiguration gc = window.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      window.setBackground(new Color(0x0, true));
    }
    AtomicInteger alpha = new AtomicInteger(100);
    Timer animator = new Timer(50, null);
    animator.addActionListener(e -> {
      int a = alpha.addAndGet(-10);
      if (a >= 0) {
        updateWindowOpacity(window, a / 100f);
      } else {
        window.dispose();
        animator.stop();
        log.append("JWindow.dispose()\n");
      }
    });
    Shape shape = new RoundRectangle2D.Float(0f, 0f, 240f, 64f, 32f, 32f);
    window.getContentPane().add(makePanel(shape, animator));
    window.pack();
    window.setLocationRelativeTo(c.getRootPane());
    window.setVisible(true);
  }

  private void updateWindowOpacity(JWindow window, float opacity) {
    window.setOpacity(opacity);
    log.append(String.format("JWindow.setOpacity(%f)%n", opacity));
  }

  private Component makePanel(Shape shape, Timer animator) {
    JPanel panel = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return shape.getBounds().getSize();
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new Color(0xAE_3D_9B_CE, true));
        g2.fill(shape);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    DragWindowListener dwl = new DragWindowListener();
    panel.addMouseListener(dwl);
    panel.addMouseMotionListener(dwl);
    panel.add(makeIconBox());
    panel.add(makeCloseButton(animator), BorderLayout.EAST);
    return panel;
  }

  private static Container makeIconBox() {
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")));
    return p;
  }

  private static JButton makeCloseButton(Timer animator) {
    JButton close = new JButton("<html><b>X");
    close.setContentAreaFilled(false);
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setForeground(Color.WHITE);
    close.addActionListener(e -> animator.start());
    return close;
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

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}
