// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(new PngFileFilter());
    fileChooser.addChoosableFileFilter(new JpgFileFilter());

    FileFilter filter = new FileNameExtensionFilter("*.jpg, *.jpeg", "jpg", "jpeg");
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    // [JDK-4776197] JFileChooser has an easy-to-fix but serious performance bug - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-4776197
    // fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());

    JButton button = new JButton("showOpenDialog");
    button.addActionListener(e -> {
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      System.out.println(retvalue);
      // if (retvalue == JFileChooser.APPROVE_OPTION) {
      //   File file = fileChooser.getSelectedFile();
      //   ((DefaultComboBoxModel) combo1.getModel()).insertElementAt(file.getAbsolutePath(), 0);
      //   combo1.setSelectedIndex(0);
      // }
    });

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showOpenDialog(...)"));
    p.add(button);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
      UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class PngFileFilter extends FileFilter {
  @Override public boolean accept(File file) {
    return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
  }

  @Override public String getDescription() {
    return "PNG(*.png)";
  }
}

class JpgFileFilter extends FileFilter {
  @Override public boolean accept(File file) {
    return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(".jpg");
  }

  @Override public String getDescription() {
    return "JPEG(*.jpg)";
  }
}
