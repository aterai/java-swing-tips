// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    desktop.setBackground(Color.GRAY.brighter());
    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    desktop.setDesktopManager(new MagneticDesktopManager());

    JInternalFrame magneticFrame1 = createFrame("Frame1");
    desktop.add(magneticFrame1);
    magneticFrame1.setLocation(30, 10);
    magneticFrame1.setVisible(true);

    JInternalFrame magneticFrame2 = createFrame("Frame2");
    desktop.add(magneticFrame2);
    magneticFrame2.setLocation(50, 30);
    magneticFrame2.setVisible(true);

    // BasicInternalFrameUI ui = (BasicInternalFrameUI) magneticFrame.getUI();
    // Component north = ui.getNorthPane();
    // MouseInputListener mml = new MagneticListener(magneticFrame);
    // north.addMouseMotionListener(mml);
    // north.addMouseListener(mml);

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createFrame(String title) {
    // title, resizable, closable, maximizable, iconifiable
    JInternalFrame frame = new JInternalFrame(title, false, false, true, true);
    frame.setSize(200, 100);
    return frame;
  }

  // private class MagneticListener extends MouseAdapter {
  //   private final JInternalFrame frame;
  //   private final Point loc = new Point();
  //   protected MagneticListener(JInternalFrame frame) {
  //     super();
  //     this.frame = frame;
  //   }
  //
  //   @Override public void mousePressed(MouseEvent e) {
  //     Point p1 = frame.getLocation();
  //     Point p2 = SwingUtilities.convertPoint(frame, e.getPoint(), frame.getDesktopPane());
  //     loc.setLocation(p2.x - p1.x, p2.y - p1.y);
  //   }
  //
  //   @Override public void mouseDragged(MouseEvent e) {
  //     JDesktopPane desktop = frame.getDesktopPane();
  //     Point ep = e.getPoint();
  //     Point pt = SwingUtilities.convertPoint(frame, ep.x - loc.x, ep.y - loc.y, desktop);
  //     int e = pt.x;
  //     int n = pt.y;
  //     int w = desktop.getSize().width - frame.getSize().width - e;
  //     int s = desktop.getSize().height - frame.getSize().height - n;
  //     if (isNear(e) || isNear(n) || isNear(w) || isNear(s)) {
  //       int x = (e < w) ? (isNear(e) ? 0 : e) : (isNear(w) ? w + e : e);
  //       int y = (n < s) ? (isNear(n) ? 0 : n) : (isNear(s) ? s + n : n);
  //       desktop.getDesktopManager().dragFrame(frame, x, y);
  //     }
  //   }
  //
  //   private boolean isNear(int c) {
  //     return (Math.abs(c) < 10);
  //   }
  // }

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
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class MagneticDesktopManager extends DefaultDesktopManager {
  @Override public void dragFrame(JComponent frame, int x, int y) {
    Container c = SwingUtilities.getAncestorOfClass(JDesktopPane.class, frame);
    if (c instanceof JDesktopPane) {
      JDesktopPane desktop = (JDesktopPane) c;
      // int east = x;
      // int north = y;
      int w = desktop.getSize().width - frame.getSize().width - x;
      int s = desktop.getSize().height - frame.getSize().height - y;
      if (isNear(x) || isNear(y) || isNear(w) || isNear(s)) {
        super.dragFrame(frame, getX(x, w), getY(y, s));
      } else {
        super.dragFrame(frame, x, y);
      }
    }
  }

  private static int getX(int east, int west) {
    // return e < w ? isNear(e) ? 0 : e : isNear(w) ? w + e : e;
    int v;
    if (east < west) {
      v = isNear(east) ? 0 : east;
    } else {
      v = isNear(west) ? west + east : east;
    }
    return v;
  }

  private static int getY(int north, int south) {
    // return n < s ? isNear(n) ? 0 : n : isNear(s) ? s + n : n;
    int v;
    if (north < south) {
      v = isNear(north) ? 0 : north;
    } else {
      v = isNear(south) ? south + north : north;
    }
    return v;
  }

  private static boolean isNear(int c) {
    return Math.abs(c) < 10;
  }
}
