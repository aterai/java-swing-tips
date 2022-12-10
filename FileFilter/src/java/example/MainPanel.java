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
    super(new BorderLayout(5, 5));
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(new PngFileFilter());
    fileChooser.addChoosableFileFilter(new JpgFileFilter());

    FileFilter filter = new FileNameExtensionFilter("*.jpg, *.jpeg", "jpg", "jpeg");
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    // [JDK-4776197] JFileChooser has an easy-to-fix but serious performance bug - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-4776197
    // fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());

    JTextArea log = new JTextArea();
    JButton button = new JButton("showOpenDialog");
    button.addActionListener(e -> {
      int retValue = fileChooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", fileChooser.getSelectedFile()));
        // File file = fileChooser.getSelectedFile();
        // ((DefaultComboBoxModel) combo1.getModel()).insertElementAt(file.getAbsolutePath(), 0);
        // combo1.setSelectedIndex(0);
      }
    });

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showOpenDialog(...)"));
    p.add(button);

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
      UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
