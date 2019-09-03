// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());

    JButton button1 = new JButton("FileDialog(Frame)");
    button1.addActionListener(e -> {
      // Window w = SwingUtilities.getWindowAncestor(this);
      // Frame frame = new Frame(w.getGraphicsConfiguration());
      Frame frame = JOptionPane.getFrameForComponent(this);
      // Frame frame = null;
      FileDialog fd = new FileDialog(frame, "title");
      // fd.setLocation(500, 500);
      fd.setTitle("FileDialog(Frame frame, String title)");
      fd.setDirectory(System.getProperty("user.home"));
      // frame.addWindowListener(new WindowAdapter() {
      fd.addWindowListener(new WindowAdapter() {
        @Override public void windowOpened(WindowEvent e) {
          append("windowOpened");
          Window w = e.getWindow();
          append("FileDialog: " + fd.getLocation());
          append("Window: " + w.getLocation());
          fd.setTitle("windowOpened");
          // fd.setLocation(500, 500);
          // append("FileDialog: " + fd.getLocation());
          Dialog d = (Dialog) SwingUtilities.getRoot(fd);
          append("fd == SwingUtilities.getRoot(fd): " + Objects.equals(d, fd));
          append("fd == w: " + Objects.equals(w, fd));
        }
      });
      // fd.addWindowStateListener(ev -> {
      //   System.out.println(ev);
      // });
      fd.setVisible(true);
      if (fd.getFile() != null) {
        // append(fd.getDirectory() + fd.getFile());
        File file = new File(fd.getDirectory(), fd.getFile());
        append(file.getAbsolutePath());
      }
    });

    JButton button2 = new JButton("FileDialog(Dialog)");
    button2.addActionListener(e -> {
      Dialog dialog = new Dialog(SwingUtilities.getWindowAncestor(this));
      FileDialog fd = new FileDialog(dialog, "FileDialog(Dialog dialog, String title)");
      // fd.setDirectory(System.getProperty("user.home"));
      fd.setVisible(true);
      if (fd.getFile() != null) {
        File file = new File(fd.getDirectory(), fd.getFile());
        append(file.getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("FileDialog"));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  public void append(String str) {
    log.append(str + "\n");
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
