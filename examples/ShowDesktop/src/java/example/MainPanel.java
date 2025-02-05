// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();

    JToggleButton button1 = new JToggleButton("Iconify Frames");
    button1.addActionListener(e -> {
      DesktopManager m = desktop.getDesktopManager();
      List<JInternalFrame> frames = Arrays.asList(desktop.getAllFrames());
      if (((AbstractButton) e.getSource()).isSelected()) {
        reverseList(frames).forEach(m::iconifyFrame);
      } else {
        // reverseList(frames).forEach(m::openFrame);
        frames.forEach(m::deiconifyFrame);
      }
    });

    JToggleButton button2 = new JToggleButton("Show Desktop");
    button2.addActionListener(e -> {
      boolean show = ((AbstractButton) e.getSource()).isSelected();
      JInternalFrame[] frames = desktop.getAllFrames();
      // TEST: Arrays.asList(frames).forEach(f -> f.setVisible(!show));
      reverseList(Arrays.asList(frames)).forEach(f -> f.setVisible(!show));
      // for (int i = frames.length - 1; i >= 0; i--) {
      //   frames[i].setVisible(!show);
      // }
    });

    AtomicInteger idx = new AtomicInteger();
    desktop.add(createFrame(idx.getAndIncrement()));
    JButton button3 = new JButton("add");
    button3.addActionListener(e -> {
      JInternalFrame f = createFrame(idx.getAndIncrement());
      desktop.add(f);
    });

    JMenuBar mb = new JMenuBar();
    mb.add(button1);
    mb.add(Box.createHorizontalStrut(2));
    mb.add(button2);
    mb.add(Box.createHorizontalGlue());
    mb.add(button3);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  public static <T> List<T> reverseList(List<T> list) {
    // List<T> reverse = new ArrayList<>(list);
    Collections.reverse(list);
    return list;
    // return reverse;
  }

  private static JInternalFrame createFrame(int i) {
    JInternalFrame f = new JInternalFrame("#" + i, true, true, true, true);
    // f.add(new JScrollPane(new JTextArea()));
    f.setSize(200, 100);
    f.setLocation(i * 16, i * 24);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
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
