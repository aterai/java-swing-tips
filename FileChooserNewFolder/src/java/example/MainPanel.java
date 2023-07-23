// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    info(log, "FileChooser.newFolderActionLabelText");
    info(log, "FileChooser.newFolderToolTipText");
    info(log, "FileChooser.newFolderAccessibleName");

    info(log, "FileChooser.other.newFolder");
    info(log, "FileChooser.other.newFolder.subsequent");

    String newFolderKey = "FileChooser.win32.newFolder";
    String subsequentKey = "FileChooser.win32.newFolder.subsequent";
    info(log, newFolderKey);
    info(log, subsequentKey);
    UIManager.put(newFolderKey, "新しいフォルダー");
    UIManager.put(subsequentKey, "新しいフォルダー ({0})");
    info(log, newFolderKey);
    info(log, subsequentKey);

    JButton button = new JButton("show JFileChooser");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int retValue = fileChooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(fileChooser.getSelectedFile().getAbsolutePath() + "\n");
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalGlue());

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void info(JTextArea log, String key) {
    log.append(String.format("%s:%n  %s%n", key, UIManager.getString(key)));
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
