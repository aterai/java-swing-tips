// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new ProgressTabbedPane();
    Action addAction = new AbstractAction("Add") {
      private int count;
      @Override public void actionPerformed(ActionEvent e) {
        Component c = count % 2 == 0 ? new JTree() : new JLabel("Tab" + count);
        tabbedPane.addTab("Title" + count, c);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        count++;
      }
    };
    JPopupMenu popup = new JPopupMenu();
    popup.add(addAction);
    popup.addSeparator();
    popup.add("Close All").addActionListener(e -> tabbedPane.removeAll());
    tabbedPane.setComponentPopupMenu(popup);

    tabbedPane.addTab("PopupMenu+addTab", new JScrollPane(new JTree()));
    add(tabbedPane);
    add(new JButton(addAction), BorderLayout.SOUTH);
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

class ProgressTabbedPane extends JTabbedPane {
  // private final Executor executor = Executors.newCachedThreadPool();
  @Override public void addTab(String title, Component content) {
    super.addTab(title, new JLabel("Loading..."));
    JProgressBar bar = new JProgressBar();
    int currentIndex = getTabCount() - 1;
    JLabel label = new JLabel(title);
    Dimension dim = label.getPreferredSize();
    int w = Math.max(80, dim.width);
    label.setPreferredSize(new Dimension(w, dim.height));
    Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    bar.setPreferredSize(new Dimension(w, dim.height - tabInsets.top - 1));
    // bar.setString(title);
    // bar.setUI(new BasicProgressBarUI());
    setTabComponentAt(currentIndex, bar);
    SwingWorker<String, Integer> worker = new BackgroundTask() {
      @Override protected void process(List<Integer> chunks) {
        if (!isDisplayable()) {
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
        setTabComponentAt(currentIndex, label);
        setComponentAt(currentIndex, content);
        String txt;
        try {
          txt = get();
        } catch (InterruptedException ex) {
          txt = "Interrupted";
          Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
          txt = "Exception";
        }
        label.setToolTipText(txt);
      }
    };
    worker.addPropertyChangeListener(new ProgressListener(bar));
    // executor.execute(worker);
    worker.execute();
  }
}

class BackgroundTask extends SwingWorker<String, Integer> {
  private final Random rnd = new Random();

  @Override protected String doInBackground() throws InterruptedException {
    int lengthOfTask = 120;
    int total = 0;
    int current = 0;
    while (current < lengthOfTask) {
      total += doSomething();
      int v = 100 * current++ / lengthOfTask;
      setProgress(v);
      publish(v);
    }
    return String.format("Done(%dms)", total);
  }

  protected int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(20) + 1;
    Thread.sleep(iv);
    return iv;
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
