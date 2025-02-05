// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JProgressBar bar = new JProgressBar();
  private final JComboBox<String> combo = new JComboBox<String>() {
    @Override public void updateUI() {
      setRenderer(null);
      super.updateUI();
      bar.setBorder(BorderFactory.createEmptyBorder());
      ListCellRenderer<? super String> renderer = new DefaultListCellRenderer();
      setRenderer((list, value, index, isSelected, cellHasFocus) -> {
        if (index < 0 && isWorking()) {
          return bar;
        }
        return renderer.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
      });
    }
  };
  private final JButton button = new JButton("load");
  private transient SwingWorker<String[], Integer> worker;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    button.addActionListener(e -> {
      button.setEnabled(false);
      combo.setEnabled(false);
      // combo.removeAllItems();
      worker = new ComboTask();
      worker.addPropertyChangeListener(new ProgressListener(bar));
      worker.execute();
    });
    add(makeTitledPanel("ProgressComboBox: ", combo, button), BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public boolean isWorking() {
    return Objects.nonNull(worker) && !worker.isDone();
  }

  private final class ComboTask extends BackgroundTask {
    @Override protected void process(List<Integer> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(this::setProgress);
        combo.setSelectedIndex(-1);
        combo.repaint();
      } else {
        // System.out.println("process: DISPOSE_ON_CLOSE");
        cancel(true);
      }
    }

    @Override protected void done() {
      if (!isDisplayable()) {
        // System.out.println("done: DISPOSE_ON_CLOSE");
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
        // System.out.println("Interrupted");
        Thread.currentThread().interrupt();
      }
      combo.setEnabled(true);
      button.setEnabled(true);
    }
  }

  // private class ProgressCellRenderer<E> implements ListCellRenderer<E> {
  //   private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
  //   private final JProgressBar bar = new JProgressBar();
  //
  //   @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
  //     if (index < 0 && Objects.nonNull(worker) && !worker.isDone()) {
  //       bar.setFont(list.getFont());
  //       bar.setBorder(BorderFactory.createEmptyBorder());
  //       bar.setValue(counter);
  //       return bar;
  //     }
  //     return renderer.getListCellRendererComponent(
  //         list, value, index, isSelected, cellHasFocus);
  //   }
  // }

  public static Component makeTitledPanel(String title, Component cmp, Component btn) {
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

  @Override protected String[] doInBackground() throws InterruptedException {
    int current = 0;
    List<String> list = new ArrayList<>();
    while (current <= MAX && !isCancelled()) {
      list.add(getComboItem(current));
      current++;
    }
    return list.toArray(new String[0]);
  }

  protected String getComboItem(int current) throws InterruptedException {
    Thread.sleep(50);
    publish(100 * current / MAX);
    return "Test: " + current;
  }
}

class ProgressListener implements PropertyChangeListener {
  private final JProgressBar progressBar;

  protected ProgressListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean isProgress = "progress".equals(e.getPropertyName());
    if (isProgress) {
      progressBar.setIndeterminate(false);
      int progress = (Integer) e.getNewValue();
      progressBar.setValue(progress);
    }
  }
}
