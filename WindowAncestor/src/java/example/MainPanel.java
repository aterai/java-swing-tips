// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JButton button = new JButton("show frame title");
    button.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
      // Container c = ((JComponent) e.getSource()).getTopLevelAncestor();
      // Frame f = JOptionPane.getFrameForComponent((Component) e.getSource());
      if (w instanceof JFrame) {
        JFrame frame = (JFrame) w;
        String msg = "parentFrame.getTitle(): " + frame.getTitle();
        JOptionPane.showMessageDialog(frame, msg, "title", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    add(button);
    setPreferredSize(new Dimension(320, 100));
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
    JFrame frame1 = new JFrame("@title@");
    frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame1.getContentPane().add(new MainPanel());
    frame1.pack();
    frame1.setLocationRelativeTo(null);

    JFrame frame2 = new JFrame("frame2");
    frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame2.getContentPane().add(new MainPanel());
    frame2.pack();
    Point pt = frame1.getLocation();
    frame2.setLocation(pt.x, pt.y + frame1.getSize().height);

    frame1.setVisible(true);
    frame2.setVisible(true);
  }
}
