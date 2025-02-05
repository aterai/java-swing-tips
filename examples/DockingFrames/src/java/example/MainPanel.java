// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    setPreferredSize(new Dimension(320, 100));
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
    JFrame frame1 = new JFrame("@title@");
    frame1.getContentPane().add(new MainPanel());
    frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    // frame1.setResizable(false);
    frame1.pack();
    frame1.setLocationRelativeTo(null);

    JFrame frame2 = new JFrame("sub frame");
    frame2.getContentPane().add(new MainPanel());
    // frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    // frame2.setResizable(false);
    frame2.pack();

    new DockingListener(frame1, frame2);

    frame1.setVisible(true);
    frame2.setVisible(true);
  }
}

class DockingListener implements ComponentListener {
  private final JFrame frame1;
  private final JFrame frame2;

  protected DockingListener(JFrame f1, JFrame f2) {
    frame1 = f1;
    frame1.addComponentListener(this);
    frame2 = f2;
    frame2.addComponentListener(this);
  }

  @Override public void componentResized(ComponentEvent e) {
    positionFrames(e);
  }

  @Override public void componentMoved(ComponentEvent e) {
    positionFrames(e);
  }

  @Override public void componentShown(ComponentEvent e) {
    positionFrames(e);
  }

  @Override public void componentHidden(ComponentEvent e) {
    positionFrames(e);
  }

  private void positionFrames(ComponentEvent e) {
    if (e.getComponent().equals(frame1)) {
      int x = frame1.getBounds().x; // + frame1.getBounds().width;
      int y = frame1.getBounds().y + frame1.getBounds().height;
      frame2.removeComponentListener(this);
      frame2.setLocation(x, y);
      frame2.addComponentListener(this);
    } else {
      int x = frame2.getBounds().x; // - frame1.getBounds().width;
      int y = frame2.getBounds().y - frame1.getBounds().height;
      frame1.removeComponentListener(this);
      frame1.setLocation(x, y);
      frame1.addComponentListener(this);
    }
  }
}
