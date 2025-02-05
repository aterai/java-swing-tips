// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public final class MainPanel extends JPanel {
  private final JCheckBox check = new JCheckBox("on EDT", true);
  private final JButton start = new JButton("Start");
  private final JButton stop = new JButton("Stop");
  private final JTextArea textArea0 = new JTextArea();
  private final JTextArea textArea1 = new JTextArea();
  private final JTextArea textArea2 = new JTextArea();
  // TEST: Timer timer = new Timer(500, e -> test(LocalDateTime.now().toString()));
  // TEST: Thread thread;
  private transient SwingWorker<String, String> worker;

  private MainPanel() {
    super(new BorderLayout());
    ((DefaultCaret) textArea0.getCaret()).setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
    ((DefaultCaret) textArea1.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    ((DefaultCaret) textArea2.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

    JPanel p = new JPanel(new GridLayout(1, 0));
    p.add(makeTitledPanel("UPDATE_WHEN_ON_EDT", new JScrollPane(textArea0)));
    p.add(makeTitledPanel("ALWAYS_UPDATE", new JScrollPane(textArea1)));
    p.add(makeTitledPanel("NEVER_UPDATE", new JScrollPane(textArea2)));

    IntStream.range(0, 10).mapToObj(Integer::toString).forEach(this::test);

    start.addActionListener(e -> startTest());
    stop.setEnabled(false);
    stop.addActionListener(e -> {
      // TEST: timer.stop();
      // TEST: thread = null;
      if (Objects.nonNull(worker)) {
        worker.cancel(true);
        // worker = null;
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(Box.createHorizontalStrut(5));
    box.add(start);
    box.add(Box.createHorizontalStrut(5));
    box.add(stop);

    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  // private class BackgroundTask extends SwingWorker<String, String> {
  //   @Override protected String doInBackground() throws InterruptedException {
  //     while (!isCancelled()) {
  //       doSomething();
  //       if (check.isSelected()) {
  //         publish(LocalDateTime.now(ZoneId.systemDefault()).toString()); // On EDT
  //       } else {
  //         test(LocalDateTime.now(ZoneId.systemDefault()).toString()); // Not on EDT
  //       }
  //     }
  //     return "Cancelled";
  //   }
  //
  //   protected void doSomething() throws InterruptedException {
  //     Thread.sleep(500);
  //   }
  // }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static void insertText(JTextArea textArea, String s) {
    textArea.append(s + "\n");
  }

  public void test(String s) {
    insertText(textArea0, s);
    insertText(textArea1, s);
    insertText(textArea2, s);
  }

  private void startTest() {
    // // TEST:
    // if (!timer.isRunning()) {
    //   timer.start();
    // }
    // // TEST:
    // if (Objects.isNull(thread)) {
    //   thread = new Thread(() -> {
    //     while (thread != null) {
    //       test(LocalDateTime.now(ZoneId.systemDefault()).toString());
    //       try {
    //         Thread.sleep(1000);
    //       } catch (InterruptedException ex) {
    //         test("Interrupted");
    //       }
    //     }
    //   });
    //   thread.start();
    // }
    if (Objects.isNull(worker) || worker.isDone()) {
      worker = new SwingWorker<String, String>() {
        @Override protected String doInBackground() throws InterruptedException {
          while (!isCancelled()) {
            doSomething();
            if (check.isSelected()) {
              publish(LocalDateTime.now(ZoneId.systemDefault()).toString()); // On EDT
            } else {
              test(LocalDateTime.now(ZoneId.systemDefault()).toString()); // Not on EDT
            }
          }
          return "Cancelled";
        }

        private void doSomething() throws InterruptedException {
          Thread.sleep(500);
        }

        @Override protected void process(List<String> chunks) {
          chunks.forEach(MainPanel.this::test);
        }

        @Override protected void done() {
          check.setEnabled(true);
          start.setEnabled(true);
          stop.setEnabled(false);
        }
      };
      check.setEnabled(false);
      start.setEnabled(false);
      stop.setEnabled(true);
      worker.execute();
    }
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
