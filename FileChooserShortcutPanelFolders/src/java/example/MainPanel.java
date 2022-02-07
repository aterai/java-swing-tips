// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import sun.awt.shell.ShellFolder;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JButton button0 = new JButton("Default");
    button0.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.showOpenDialog(getRootPane());
    });

    JButton button1 = new JButton("System.getenv(\"SystemDrive\")");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      // https://stackoverflow.com/questions/10524376/how-to-make-jfilechooser-default-to-computer-view-instead-of-my-documents
      // File systemDrive = new File("C:\\");
      // String separator = System.getProperty("file.separator");
      File systemDrive = new File(System.getenv("SystemDrive") + File.separatorChar);
      File pcDir = chooser.getFileSystemView().getParentDirectory(systemDrive);
      chooser.setCurrentDirectory(pcDir);
      chooser.showOpenDialog(getRootPane());
    });

    JButton button2 = new JButton("ShellFolder.get(\"fileChooserShortcutPanelFolders\")");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      FileSystemView fsv = chooser.getFileSystemView();
      File[] files = (File[]) ShellFolder.get("fileChooserShortcutPanelFolders");
      for (File f : files) {
        System.out.println(f.getAbsolutePath());
      }
      chooser.addHierarchyListener(ev -> {
        Component c = ev.getComponent();
        if ((ev.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
          Class<JToggleButton> clz = JToggleButton.class;
          descendants(chooser)
              .filter(clz::isInstance).map(clz::cast)
              .filter(rb -> fsv.getSystemDisplayName(files[3]).equals(rb.getText()))
              .findFirst().ifPresent(AbstractButton::doClick);
        }
      });
      chooser.showOpenDialog(getRootPane());
    });

    add(button0);
    add(button1);
    add(button2);
    setPreferredSize(new Dimension(320, 240));
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
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
