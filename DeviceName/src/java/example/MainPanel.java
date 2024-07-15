// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(10, 10));
    JTextArea log = new JTextArea();
    String deviceName = "con.txt";

    JButton b1 = new JButton("c:/" + deviceName);
    b1.addActionListener(e -> {
      File file = new File(deviceName);
      try {
        if (file.createNewFile()) {
          log.append("the named file does not exist and was successfully created.\n");
        } else {
          log.append("the named file already exists.\n");
        }
      } catch (IOException ex) {
        // ex.printStackTrace();
        Object[] msg = {ex.getMessage()};
        showMessageDialog(msg);
      }
    });
    Component p1 = makeTitledPanel("IOException: before 1.5", b1);

    JButton b2 = new JButton("c:/" + deviceName + ":getCanonicalPath");
    b2.addActionListener(e -> {
      File file = new File(deviceName);
      if (!isCanonicalPath(file)) {
        Object[] msg = {file.getAbsolutePath() + " is not a canonical path."};
        showMessageDialog(msg);
      }
    });
    Component p2 = makeTitledPanel("getCanonicalPath: before 1.5", b2);

    JButton b3 = new JButton("c:/" + deviceName + ":isFile");
    b3.addActionListener(e -> {
      File file = new File(deviceName);
      if (!file.isFile()) {
        Object[] msg = {file.getAbsolutePath() + " is not a file."};
        showMessageDialog(msg);
      }
    });
    Component p3 = makeTitledPanel("isFile: JDK 1.5+", b3);

    JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
    p.add(p1);
    p.add(p2);
    p.add(p3);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private void showMessageDialog(Object... obj) {
    JRootPane root = getRootPane();
    JOptionPane.showMessageDialog(root, obj, "Error", JOptionPane.INFORMATION_MESSAGE);
  }

  // Before 1.5
  @SuppressWarnings("PMD.OnlyOneReturn")
  public static boolean isCanonicalPath(File file) {
    if (file == null) {
      return false;
    }
    try {
      if (file.getCanonicalPath() == null || !file.isFile()) {
        return false;
      }
    } catch (IOException ex) {
      return false;
    }
    return true;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
