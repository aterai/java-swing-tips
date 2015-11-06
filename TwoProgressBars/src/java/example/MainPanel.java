package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea area     = new JTextArea();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton  = new JButton(new RunAction());
    private final JButton canButton  = new JButton(new CancelAction());
    private final JProgressBar bar1  = new JProgressBar(0, 100);
    private final JProgressBar bar2  = new JProgressBar(0, 100);
    private SwingWorker<String, Progress> worker;

    public MainPanel() {
        super(new BorderLayout(5, 5));
        area.setEditable(false);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(runButton);
        box.add(Box.createHorizontalStrut(2));
        box.add(canButton);
        add(new JScrollPane(area));
        add(box, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void initStatusPanel(boolean start) {
        if (start) {
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            statusPanel.add(bar1, BorderLayout.NORTH);
            statusPanel.add(bar2, BorderLayout.SOUTH);
        } else {
            runButton.setEnabled(true);
            canButton.setEnabled(false);
            statusPanel.removeAll();
        }
        statusPanel.revalidate();
    }
    class RunAction extends AbstractAction {
        public RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            initStatusPanel(true);
            worker = new ProgressTask();
            worker.execute();
        }
    }
    class ProgressTask extends Task {
        @Override protected void process(List<Progress> chunks) {
            if (isCancelled()) {
                return;
            }
            if (!isDisplayable()) {
                cancel(true);
                return;
            }
            for (Progress s: chunks) {
                switch (s.component) {
                  case TOTAL: bar1.setValue((Integer) s.value); break;
                  case FILE:  bar2.setValue((Integer) s.value); break;
                  case LOG:   area.append((String) s.value);    break;
                  default:    throw new AssertionError("Unknown Progress");
                }
            }
        }
        @Override public void done() {
            if (!isDisplayable()) {
                cancel(true);
                return;
            }
            initStatusPanel(false);
            try {
                appendLine(isCancelled() ? "\nCancelled\n" : get() + "\n");
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                appendLine("\nException\n");
            }
        }
    }
    class CancelAction extends AbstractAction {
        public CancelAction() {
            super("cancel");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
    }

    private void appendLine(String str) {
        area.append(str);
        area.setCaretPosition(area.getDocument().getLength());
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum Component { TOTAL, FILE, LOG }

class Progress {
    public final Object value;
    public final Component component;
    public Progress(Component component, Object value) {
        this.component = component;
        this.value = value;
    }
}

class Task extends SwingWorker<String, Progress> {
    private final Random r = new Random();
    @Override public String doInBackground() {
        //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
        int current = 0;
        int lengthOfTask = 12; //filelist.size();
        publish(new Progress(Component.LOG, "Length Of Task: " + lengthOfTask));
        publish(new Progress(Component.LOG, "\n------------------------------\n"));
        while (current < lengthOfTask && !isCancelled()) {
            try {
                convertFileToSomething();
            } catch (InterruptedException ie) {
                return "Interrupted";
            }
            publish(new Progress(Component.LOG, "*"));
            publish(new Progress(Component.TOTAL, 100 * current / lengthOfTask));
            current++;
        }
        publish(new Progress(Component.LOG, "\n"));
        return "Done";
    }
    private void convertFileToSomething() throws InterruptedException {
        int current = 0;
        int lengthOfTask = 10 + r.nextInt(50); //long lengthOfTask = file.length();
        while (current <= lengthOfTask && !isCancelled()) {
            int iv = 100 * current / lengthOfTask;
            Thread.sleep(20); // dummy
            publish(new Progress(Component.FILE, iv + 1));
            current++;
        }
    }
}

// public final class MainPanel extends JPanel {
//     private final JTextArea area     = new JTextArea();
//     private final JPanel statusPanel = new JPanel(new BorderLayout());
//     private final JButton runButton  = new JButton(new RunAction());
//     private final JButton canButton  = new JButton(new CancelAction());
//     private SwingWorker<String, String> worker;
//
//     public MainPanel() {
//         super(new BorderLayout(5, 5));
//         area.setEditable(false);
//         Box box = Box.createHorizontalBox();
//         box.add(Box.createHorizontalGlue());
//         box.add(runButton);
//         box.add(Box.createHorizontalStrut(2));
//         box.add(canButton);
//         add(new JScrollPane(area));
//         add(box, BorderLayout.NORTH);
//         add(statusPanel, BorderLayout.SOUTH);
//         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         setPreferredSize(new Dimension(320, 240));
//     }
//
//     class RunAction extends AbstractAction {
//         public RunAction() {
//             super("run");
//         }
//         @Override public void actionPerformed(ActionEvent evt) {
//             //System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
//             final JProgressBar bar1 = new JProgressBar(0, 100);
//             final JProgressBar bar2 = new JProgressBar(0, 100);
//             runButton.setEnabled(false);
//             canButton.setEnabled(true);
//             statusPanel.removeAll();
//             statusPanel.add(bar1, BorderLayout.NORTH);
//             statusPanel.add(bar2, BorderLayout.SOUTH);
//             statusPanel.revalidate();
//             //bar1.setIndeterminate(true);
//
//             worker = new SwingWorker<String, String>() {
//                 @Override public String doInBackground() {
//                     //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
//                     int current = 0;
//                     int lengthOfTask = 12; //filelist.size();
//                     publish("Length Of Task: " + lengthOfTask);
//                     publish("\n------------------------------\n");
//                     setProgress(0);
//                     while (current < lengthOfTask && !isCancelled()) {
//                         if (!bar1.isDisplayable()) {
//                             return "Disposed";
//                         }
//                         try {
//                             convertFileToSomething();
//                         } catch (InterruptedException ie) {
//                             return "Interrupted";
//                         }
//                         publish("*");
//                         setProgress(100 * current / lengthOfTask);
//                         current++;
//                     }
//                     publish("\n");
//                     return "Done";
//                 }
//                 private final Random r = new Random();
//                 private void convertFileToSomething() throws InterruptedException {
//                     int current = 0;
//                     int lengthOfTask = 10 + r.nextInt(50); //long lengthOfTask = file.length();
//                     while (current <= lengthOfTask && !isCancelled()) {
//                         int iv = 100 * current / lengthOfTask;
//                         Thread.sleep(20); // dummy
//                         firePropertyChange("progress2", iv, iv + 1);
//                         current++;
//                     }
//                 }
//                 @Override protected void process(List<String> chunks) {
//                     //System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
//                     for (String message: chunks) {
//                         appendLine(message);
//                     }
//                 }
//                 @Override public void done() {
//                     //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
//                     runButton.setEnabled(true);
//                     canButton.setEnabled(false);
//                     statusPanel.remove(bar1);
//                     statusPanel.remove(bar2);
//                     statusPanel.revalidate();
//                     String text = null;
//                     if (isCancelled()) {
//                         text = "Cancelled";
//                     } else {
//                         try {
//                             text = get();
//                         } catch (Exception ex) {
//                             ex.printStackTrace();
//                             text = "Exception";
//                         }
//                     }
//                     //System.out.println(text);
//                     appendLine(text);
//                 }
//             };
//             worker.addPropertyChangeListener(new MainProgressListener(bar1));
//             worker.addPropertyChangeListener(new SubProgressListener(bar2));
//             worker.execute();
//         }
//     }
//     class CancelAction extends AbstractAction {
//         public CancelAction() {
//             super("cancel");
//         }
//         @Override public void actionPerformed(ActionEvent evt) {
//             if (Objects.nonNull(worker) && !worker.isDone()) {
//                 worker.cancel(true);
//             }
//             worker = null;
//         }
//     }
//     private void appendLine(String str) {
//         area.append(str);
//         area.setCaretPosition(area.getDocument().getLength());
//     }
//
//     public static void main(String... args) {
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 createAndShowGUI();
//             }
//         });
//     }
//     public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         JFrame frame = new JFrame("@title@");
//         //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//         frame.getContentPane().add(new MainPanel());
//         frame.pack();
//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//     }
// }
//
// class MainProgressListener implements PropertyChangeListener {
//     protected final JProgressBar progressBar;
//     protected MainProgressListener(JProgressBar progressBar) {
//         this.progressBar = progressBar;
//         this.progressBar.setValue(0);
//     }
//     @Override public void propertyChange(PropertyChangeEvent e) {
//         String strPropertyName = e.getPropertyName();
//         if ("progress".equals(strPropertyName)) {
//             progressBar.setIndeterminate(false);
//             int progress = (Integer) e.getNewValue();
//             progressBar.setValue(progress);
//         }
//     }
// }
// class SubProgressListener implements PropertyChangeListener {
//     private final JProgressBar progressBar;
//     protected SubProgressListener(JProgressBar progressBar) {
//         this.progressBar = progressBar;
//         this.progressBar.setValue(0);
//     }
//     @Override public void propertyChange(PropertyChangeEvent e) {
//         String strPropertyName = e.getPropertyName();
//         if ("progress2".equals(strPropertyName)) {
//             int progress = (Integer) e.getNewValue();
//             progressBar.setValue(progress);
//         }
//     }
// }
