package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    try {
      Thread.sleep(3000); // dummy task
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    System.out.println("main start / EDT: " + EventQueue.isDispatchThread());
    createAndShowGui();
    System.out.println("main end");
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    JDialog splashScreen = new JDialog(frame, Dialog.ModalityType.DOCUMENT_MODAL);
    JProgressBar progress = new JProgressBar();

    System.out.println(splashScreen.getModalityType());

    EventQueue.invokeLater(() -> {
      splashScreen.setUndecorated(true);
      splashScreen.getContentPane().add(new JLabel(new ImageIcon(MainPanel.class.getResource("splash.png"))));
      splashScreen.getContentPane().add(progress, BorderLayout.SOUTH);
      splashScreen.pack();
      splashScreen.setLocationRelativeTo(null);
      splashScreen.setVisible(true);
    });
    SwingWorker<Void, Void> worker = new BackgroundTask() {
      @Override public void done() {
        splashScreen.dispose();
      }
    };
    worker.addPropertyChangeListener(e -> {
      if ("progress".equals(e.getPropertyName())) {
        progress.setValue((Integer) e.getNewValue());
      }
    });
    worker.execute();

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    EventQueue.invokeLater(() -> frame.setVisible(true));
  }
}

class BackgroundTask extends SwingWorker<Void, Void> {
  @Override public Void doInBackground() {
    int current = 0;
    int lengthOfTask = 120;
    while (current < lengthOfTask && !isCancelled()) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        return null;
      }
      setProgress(100 * current++ / lengthOfTask);
    }
    return null;
  }
}
