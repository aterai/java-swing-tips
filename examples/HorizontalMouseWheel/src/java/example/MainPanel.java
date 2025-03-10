// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0f, 0f, Color.ORANGE, w, h, Color.WHITE, true));
        g2.fillRect(0, 0, w, h);
        g2.dispose();
      }

      @Override public Dimension getPreferredSize() {
        return new Dimension(640, 640);
      }
    };
    label.setBorder(BorderFactory.createTitledBorder("Horizontal scroll: CTRL + Wheel"));
    label.addMouseWheelListener(e -> {
      Component c = e.getComponent();
      Container o = SwingUtilities.getAncestorOfClass(JScrollPane.class, c);
      if (o instanceof JScrollPane) {
        JScrollPane s = (JScrollPane) o;
        Component bar = e.isControlDown()
            ? s.getHorizontalScrollBar() : s.getVerticalScrollBar();
        bar.dispatchEvent(SwingUtilities.convertMouseEvent(c, e, bar));
      }
    });

    JScrollPane scroll = new JScrollPane(label);
    scroll.getVerticalScrollBar().setUnitIncrement(10);

    JScrollBar hsb = scroll.getHorizontalScrollBar();
    hsb.setUnitIncrement(10);
    hsb.addMouseWheelListener(e -> {
      Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, e.getComponent());
      if (c instanceof JScrollPane) {
        JViewport viewport = ((JScrollPane) c).getViewport();
        Point pt = viewport.getViewPosition();
        pt.translate(hsb.getUnitIncrement() * e.getWheelRotation(), 0);
        JComponent view = (JComponent) SwingUtilities.getUnwrappedView(viewport);
        view.scrollRectToVisible(new Rectangle(pt, viewport.getSize()));
      }
    });

    add(scroll);
    setPreferredSize(new Dimension(320, 240));
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
