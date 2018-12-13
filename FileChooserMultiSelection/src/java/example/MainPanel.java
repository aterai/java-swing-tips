package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.io.File;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("setMultiSelectionEnabled(true)");
    button2.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      // fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      // fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setMultiSelectionEnabled(true);
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText("");
        for (File file: fileChooser.getSelectedFiles()) {
          log.append(file.getAbsolutePath() + "\n");
        }
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
