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
      // Container w = ((JComponent) e.getSource()).getTopLevelAncestor();
      Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
      // Frame frame = JOptionPane.getFrameForComponent((Component) e.getSource());
      if (w instanceof Frame) {
        Frame frame = (Frame) w;
        String msg = "parentFrame.getTitle(): " + frame.getTitle();
        JOptionPane.showMessageDialog(frame, msg, "title", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    add(button);
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
