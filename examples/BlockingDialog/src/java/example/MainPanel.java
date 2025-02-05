// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("0x22_FF_00_00");
    JButton button = new JButton("Stop 5sec");
    button.addActionListener(e -> {
      int color = check.isSelected() ? 0x22_FF_00_00 : 0x01_00_00_00;
      blocking(new Color(color, true));
    });
    JPanel p = new JPanel();
    p.add(check);
    p.add(new JTextField(10));
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea(100, 80)));
    setPreferredSize(new Dimension(320, 240));
  }

  private void blocking(Color color) {
    Window w = SwingUtilities.getWindowAncestor(getRootPane());
    JDialog dialog = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setUndecorated(true);
    dialog.setBounds(w.getBounds());
    dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    dialog.setBackground(color);
    new BackgroundTask() {
      @Override protected void done() {
        if (!isDisplayable()) {
          cancel(true);
          return;
        }
        dialog.setVisible(false);
      }
    }.execute();
    dialog.setVisible(true);
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

class BackgroundTask extends SwingWorker<String, Void> {
  @Override protected String doInBackground() throws InterruptedException {
    Thread.sleep(5_000);
    return "Done";
  }
}
