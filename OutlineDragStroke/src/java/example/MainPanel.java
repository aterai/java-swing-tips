// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Rectangle rubberBand = new Rectangle();
    JDesktopPane desktop = makeDesktopPane(rubberBand);
    add(new JLayer<>(desktop, new LayerUI<JDesktopPane>() {
      @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
          JDesktopPane desktop = (JDesktopPane) ((JLayer<?>) c).getView();
          if (desktop.getDragMode() == JDesktopPane.OUTLINE_DRAG_MODE) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Paint color = UIManager.getColor("InternalFrame.borderColor");
            g2.setPaint(Color.GRAY);
            g2.setStroke(makeDotStroke());
            g2.draw(rubberBand);
            g2.dispose();
          }
        }
      }
    }));

    JInternalFrame frame1 = createFrame("Frame1");
    desktop.add(frame1);
    frame1.setLocation(30, 10);

    JInternalFrame frame2 = createFrame("Frame2");
    desktop.add(frame2);
    frame2.setLocation(50, 30);

    EventQueue.invokeLater(() -> {
      frame1.setVisible(true);
      frame2.setVisible(true);
    });
    setPreferredSize(new Dimension(320, 240));
  }

  private static JDesktopPane makeDesktopPane(Rectangle rubberBand) {
    JDesktopPane desktop = new JDesktopPane();
    desktop.setDesktopManager(new DefaultDesktopManager() {
      @Override public void beginResizingFrame(JComponent f, int direction) {
        // System.out.println("beginResizingFrame");
        // JDesktopPane dp = ((JInternalFrame) f).getDesktopPane();
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        super.beginResizingFrame(f, direction);
      }

      @Override public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
        // System.out.println("resizeFrame");
        // JDesktopPane dp = ((JInternalFrame) f).getDesktopPane();
        if (desktop.getDragMode() == JDesktopPane.OUTLINE_DRAG_MODE) {
          super.resizeFrame(f, newX, newY, 0, 0);
          rubberBand.setBounds(newX, newY, newWidth, newHeight);
          desktop.repaint();
        } else {
          super.resizeFrame(f, newX, newY, newWidth, newHeight);
        }
      }

      @Override public void endResizingFrame(JComponent f) {
        // System.out.println("endResizingFrame");
        // JDesktopPane dp = ((JInternalFrame) f).getDesktopPane();
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        if (!rubberBand.isEmpty()) {
          super.resizeFrame(f, rubberBand.x, rubberBand.y, rubberBand.width, rubberBand.height);
          rubberBand.setBounds(0, 0, 0, 0);
        }
        super.endResizingFrame(f);
      }
    });
    return desktop;
  }

  public static Stroke makeDotStroke() {
    float[] dist = {1f, 1f};
    return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dist, 0f);
  }

  private JInternalFrame createFrame(String title) {
    JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
    frame.setSize(200, 100);
    return frame;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
