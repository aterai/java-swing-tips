// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JFileChooser fileChooser = new JFileChooser() {
      @Override public void approveSelection() {
        File f = getSelectedFile();
        if (f.exists() && getDialogType() == SAVE_DIALOG) {
          // @see https://community.oracle.com/thread/1391852 How to react on events fired by a JFileChooser?
          // @see https://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
          // String m = "Replace file: " + f.getAbsolutePath() + "?";
          // String m = "The file exists, overwrite?";
          String m = String.format("<html>%s already exists.<br>Do you want to replace it?", f.getAbsolutePath());
          int rv = JOptionPane.showConfirmDialog(this, m, "Save As", JOptionPane.YES_NO_OPTION);
          if (rv != JOptionPane.YES_OPTION) {
            return;
          }
        }
        super.approveSelection();
      }
    };

    JButton button = new JButton("Override JFileChooser#approveSelection()");
    button.addActionListener(e -> {
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        System.out.println(file);
      }
    });

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showSaveDialog(...)"));
    p.add(button);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
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
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
