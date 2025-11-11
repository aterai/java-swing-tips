// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JFileChooser fileChooser = new JFileChooser() {
      @Override public void approveSelection() {
        File file = getSelectedFile();
        if (file.exists() && getDialogType() == SAVE_DIALOG) {
          if (showConfirmDialog(file) == JOptionPane.YES_OPTION) {
            super.approveSelection();
          }
        } else {
          super.approveSelection();
        }
      }

      // @see
      // https://community.oracle.com/thread/1391852
      // https://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
      private int showConfirmDialog(File file) {
        String format = "<html>%s already exists.<br>Do you want to replace it?";
        String m = String.format(format, file.getAbsolutePath());
        // String m = "Replace file: " + file.getAbsolutePath() + "?";
        // String m = "The file exists, overwrite?";
        String title = "Save As";
        return JOptionPane.showConfirmDialog(this, m, title, JOptionPane.YES_NO_OPTION);
      }
    };

    JTextArea log = new JTextArea();
    JButton button = new JButton("Override JFileChooser#approveSelection()");
    button.addActionListener(e -> {
      int ret = fileChooser.showSaveDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        log.append(file + "\n");
      }
    });

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showSaveDialog(...)"));
    p.add(button);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
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
