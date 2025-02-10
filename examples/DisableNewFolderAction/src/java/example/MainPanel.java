// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.util.Optional;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JFileChooser fc1 = new JFileChooser();
    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      int retValue = fc1.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", fc1.getSelectedFile()));
      }
    });

    JFileChooser fc2 = new JFileChooser();
    fc2.setFileSystemView(new FileSystemView() {
      @Override public File createNewFolder(File containingDir) {
        return null;
      }
    });
    // https://ateraimemo.com/Swing/DetailsViewFileChooser.html
    String cmd = "New Folder"; // = sun.swing.FilePane.ACTION_NEW_FOLDER
    Optional.ofNullable(fc2.getActionMap().get(cmd)).ifPresent(a -> a.setEnabled(false));
    fc2.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, e -> {
      if (e.getNewValue() instanceof File) {
        Optional.ofNullable(fc2.getActionMap().get(cmd)).ifPresent(a -> a.setEnabled(false));
      }
    });

    JButton button2 = new JButton("disable New Folder");
    button2.addActionListener(e -> {
      int retValue = fc2.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", fc2.getSelectedFile()));
      }
    });

    JPanel p = new JPanel(new GridLayout(1, 0, 5, 5));
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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
