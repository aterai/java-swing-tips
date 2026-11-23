// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Rectangle rubberBand = new Rectangle();
    JDesktopPane desktop = new JDesktopPane();
    desktop.setDesktopManager(new OutlineResizeDesktopManager(rubberBand));
    add(new JLayer<>(desktop, new LayerUI<JDesktopPane>() {
      @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (!rubberBand.isEmpty()) {
          Graphics2D g2 = (Graphics2D) g.create();
          // Paint color = UIManager.getColor("InternalFrame.borderColor");
          g2.setPaint(Color.GRAY);
          g2.setStroke(createDotStroke());
          g2.draw(rubberBand);
          g2.dispose();
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

  public static Stroke createDotStroke() {
    float[] dash = {1f, 1f};
    return new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash, 0f);
  }

  private static JInternalFrame createFrame(String title) {
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

class OutlineResizeDesktopManager extends DefaultDesktopManager {
  private final Rectangle rubberBand;

  protected OutlineResizeDesktopManager(Rectangle rubberBand) {
    super();
    this.rubberBand = rubberBand;
  }

  @Override public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
    // Do not call super.resizeFrame(...): the frame keeps its bounds while resizing,
    // and only the rubber band is updated and painted by the LayerUI.
    repaintRubberBand(f);
    rubberBand.setBounds(newX, newY, newWidth, newHeight);
    repaintRubberBand(f);
  }

  @Override public void endResizingFrame(JComponent f) {
    if (!rubberBand.isEmpty()) {
      repaintRubberBand(f);
      setBoundsForFrame(f, rubberBand.x, rubberBand.y, rubberBand.width, rubberBand.height);
      rubberBand.setBounds(0, 0, 0, 0);
    }
    super.endResizingFrame(f);
  }

  private void repaintRubberBand(JComponent f) {
    Container parent = f.getParent();
    if (parent != null && !rubberBand.isEmpty()) {
      // Graphics2D#draw(Rectangle) paints one pixel beyond the width and height
      parent.repaint(rubberBand.x, rubberBand.y, rubberBand.width + 1, rubberBand.height + 1);
    }
  }
}
