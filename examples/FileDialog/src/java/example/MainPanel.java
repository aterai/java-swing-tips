// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("FileDialog"));
    p.add(makeButton1());
    p.add(makeButton2());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeButton1() {
    JButton button = new JButton("FileDialog(Frame)");
    button.addActionListener(e -> {
      File file = openFileDialog1(this);
      if (file != null) {
        append(file.getAbsolutePath());
      }
    });
    return button;
  }

  private File openFileDialog1(Component parent) {
    Frame frame = JOptionPane.getFrameForComponent(parent);
    FileDialog fd = new FileDialog(frame, "title");
    // fd.setLocation(500, 500);
    fd.setTitle("FileDialog(Frame frame, String title)");
    fd.setDirectory(System.getProperty("user.home"));
    fd.addWindowListener(new WindowAdapter() {
      @Override public void windowOpened(WindowEvent e) {
        info(e, fd);
      }
    });
    fd.setVisible(true);
    return Optional.ofNullable(fd.getFile())
        .map(name -> new File(fd.getDirectory(), name))
        .orElse(null);
  }

  private void info(WindowEvent e, FileDialog fd) {
    append("windowOpened");
    Window w = e.getWindow();
    append("FileDialog: " + fd.getLocation());
    append("Window: " + w.getLocation());
    fd.setTitle("windowOpened");
    // fd.setLocation(500, 500); append("FileDialog: " + fd.getLocation());
    Dialog d = (Dialog) SwingUtilities.getRoot(fd);
    append("fd == SwingUtilities.getRoot(fd): " + Objects.equals(d, fd));
    append("fd == w: " + Objects.equals(w, fd));
  }

  private JButton makeButton2() {
    JButton button = new JButton("FileDialog(Dialog)");
    button.addActionListener(e -> {
      Dialog dialog = new Dialog(SwingUtilities.getWindowAncestor(this));
      FileDialog fd = new FileDialog(dialog, "FileDialog(Dialog dialog, String title)");
      // fd.setDirectory(System.getProperty("user.home"));
      fd.setVisible(true);
      if (fd.getFile() != null) {
        File file = new File(fd.getDirectory(), fd.getFile());
        append(file.getAbsolutePath());
      }
    });
    return button;
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
