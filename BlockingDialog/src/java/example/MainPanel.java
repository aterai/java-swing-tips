// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JCheckBox check = new JCheckBox("0x01FF0000");
    JButton button = new JButton("Stop 5sec");
    button.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(getRootPane());
      JDialog dialog = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
      dialog.setUndecorated(true);
      dialog.setBounds(w.getBounds());
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      int color = check.isSelected() ? 0x22FF0000 : 0x01FF0000;
      dialog.setBackground(new Color(color, true));
      new BackgroundTask() {
        @Override public void done() {
          if (!isDisplayable()) {
            cancel(true);
            return;
          }
          dialog.setVisible(false);
        }
      }.execute();
      dialog.setVisible(true);
    });

    JPanel p = new JPanel();
    p.add(check);
    p.add(new JTextField(10));
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea(100, 80)));
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

class BackgroundTask extends SwingWorker<String, Void> {
  @Override public String doInBackground() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      // ex.printStackTrace();
      System.out.println("Interrupted");
    }
    return "Done";
  }
}
