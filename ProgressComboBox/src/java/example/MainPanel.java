// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected final JComboBox<String> combo = new JComboBox<String>() {
    @Override public void updateUI() {
      super.updateUI();
      setRenderer(new ProgressCellRenderer<>());
    }
  };
  protected final JButton button = new JButton("load");
  protected transient SwingWorker<String[], Integer> worker;
  protected int counter;

  public MainPanel() {
    super(new BorderLayout(5, 5));
    button.addActionListener(e -> {
      button.setEnabled(false);
      combo.setEnabled(false);
      // combo.removeAllItems();
      worker = new ComboTask();
      worker.execute();
    });
    add(makeTitledPanel("ProgressComboBox: ", combo, button), BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private class ComboTask extends BackgroundTask {
    @Override protected void process(List<Integer> chunks) {
      if (isCancelled()) {
        return;
      }
      if (!isDisplayable()) {
        System.out.println("process: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      for (Integer i: chunks) {
        counter = i;
      }
      combo.setSelectedIndex(-1);
      combo.repaint();
    }

    @Override public void done() {
      if (!isDisplayable()) {
        System.out.println("done: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      try {
        if (!isCancelled()) {
          String[] array = get();
          combo.setModel(new DefaultComboBoxModel<>(array));
          combo.setSelectedIndex(0);
        }
      } catch (InterruptedException | ExecutionException ex) {
        System.out.println("Interrupted");
      }
      combo.setEnabled(true);
      button.setEnabled(true);
      counter = 0;
    }
  }

  private class ProgressCellRenderer<E> implements ListCellRenderer<E> {
    private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
    private final JProgressBar bar = new JProgressBar();

    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
      if (index < 0 && Objects.nonNull(worker) && !worker.isDone()) {
        bar.setFont(list.getFont());
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setValue(counter);
        return bar;
      }
      return renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  }

  private static Component makeTitledPanel(String title, Component cmp, Component btn) {
    GridBagConstraints c = new GridBagConstraints();
    JPanel p = new JPanel(new GridBagLayout());

    c.insets = new Insets(5, 5, 5, 0);
    p.add(new JLabel(title), c);

    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(cmp, c);

    c.weightx = 0d;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(btn, c);

    return p;
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundTask extends SwingWorker<String[], Integer> {
  private static final int MAX = 30;

  @Override public String[] doInBackground() {
    int current = 0;
    List<String> list = new ArrayList<>();
    while (current <= MAX && !isCancelled()) {
      try {
        Thread.sleep(50);
        int iv = 100 * current / MAX;
        publish(iv);
        // setProgress(iv);
        list.add("Test: " + current);
      } catch (InterruptedException ex) {
        break;
      }
      current++;
    }
    return list.toArray(new String[0]);
  }
}
