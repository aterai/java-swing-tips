// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JButton button0 = new JButton("Default");
    button0.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile().getAbsolutePath() + "\n");
      }
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
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile().getAbsolutePath() + "\n");
      }
    });

    JButton button2 = new JButton("ShellFolder.get(\"fileChooserShortcutPanelFolders\")");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      FileSystemView fsv = chooser.getFileSystemView();
      File[] files = (File[]) sun.awt.shell.ShellFolder.get("fileChooserShortcutPanelFolders");
      for (File f : files) {
        log.append(f.getAbsolutePath() + "\n");
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
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile().getAbsolutePath() + "\n");
      }
    });

    Box box1 = Box.createHorizontalBox();
    box1.add(button0);
    box1.add(Box.createHorizontalStrut(5));
    box1.add(button1);

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(box1);
    p.add(button2);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
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
