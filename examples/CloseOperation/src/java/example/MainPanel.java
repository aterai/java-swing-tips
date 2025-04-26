// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final AtomicInteger COUNTER = new AtomicInteger(0);

  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("New Frame");
    button.addActionListener(e -> {
      JFrame frame = createFrame(null);
      frame.getContentPane().add(new MainPanel());
      frame.pack();
      Container c = ((JComponent) e.getSource()).getTopLevelAncestor();
      if (c instanceof Window) {
        frame.setLocation(c.getX(), c.getY() + frame.getSize().height);
      }
      frame.setVisible(true);
    });
    add(button);
    setPreferredSize(new Dimension(320, 100));
  }

  public static JFrame createFrame(String title) {
    JFrame frame = new JFrame(Objects.toString(title, "Frame #" + COUNTER));
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    COUNTER.getAndIncrement();
    frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        if (COUNTER.getAndDecrement() == 0) {
          Window w = e.getWindow();
          if (w instanceof JFrame) {
            ((JFrame) w).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          }
        }
      }
    });
    return frame;
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
    JFrame frame = createFrame("@title@"); // new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
