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
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String PAUSE  = "pause";
    private static final String RESUME = "resume";
    private final JTextArea area       = new JTextArea();
    private final JPanel statusPanel   = new JPanel(new BorderLayout());
    private final JButton runButton    = new JButton("run");
    private final JButton cancelButton = new JButton("cancel");
    private final JButton pauseButton  = new JButton(PAUSE);
    private final JProgressBar bar1    = new JProgressBar();
    private final JProgressBar bar2    = new JProgressBar();
    private transient Task worker;

    public MainPanel() {
        super(new BorderLayout(5, 5));
        area.setEditable(false);

        runButton.addActionListener(e -> {
            //System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
            runButton.setEnabled(false);
            cancelButton.setEnabled(true);
            pauseButton.setEnabled(true);
            bar1.setValue(0);
            bar2.setValue(0);
            statusPanel.add(bar1, BorderLayout.NORTH);
            statusPanel.add(bar2, BorderLayout.SOUTH);
            statusPanel.revalidate();
            //bar1.setIndeterminate(true);
            worker = new ProgressTask();
            worker.execute();
        });

        pauseButton.setEnabled(false);
        pauseButton.addActionListener(e -> {
            JButton b = (JButton) e.getSource();
            //String pause = (String) getValue(Action.NAME);
            if (Objects.nonNull(worker)) {
                if (worker.isCancelled() || worker.isPaused) {
                    b.setText(PAUSE);
                } else {
                    b.setText(RESUME);
                }
                worker.isPaused ^= true;
            } else {
                b.setText(PAUSE);
            }
        });

        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
            pauseButton.setText("pause");
            pauseButton.setEnabled(false);
        });

        JComponent box = createRightAlignButtonBox4(Arrays.asList(pauseButton, cancelButton, runButton), 80, 5);
        add(new JScrollPane(area));
        add(box, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private class ProgressTask extends Task {
        @Override protected void process(List<Progress> chunks) {
            //System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
            if (isCancelled()) {
                return;
            }
            if (!isDisplayable()) {
                System.out.println("process: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            processChunks(chunks);
        }
        @Override public void done() {
            if (!isDisplayable()) {
                System.out.println("done: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
            updateComponentDone();
            String message;
            try {
                message = String.format("%n%s%n", isCancelled() ? "Cancelled" : get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                message = String.format("%n%s%n", "Exception");
            }
            appendLine(message);
        }
    }
    protected void updateComponentDone() {
        runButton.requestFocusInWindow();
        runButton.setEnabled(true);
        cancelButton.setEnabled(false);
        pauseButton.setEnabled(false);
        statusPanel.removeAll();
        statusPanel.revalidate();
    }
    protected void processChunks(List<Progress> chunks) {
        for (Progress s: chunks) {
            switch (s.component) {
              case TOTAL:
                bar1.setValue((Integer) s.value);
                break;
              case FILE:
                bar2.setValue((Integer) s.value);
                break;
              case LOG:
                area.append((String) s.value);
                break;
              case PAUSE:
                textProgress((Boolean) s.value);
                break;
              default:
                throw new AssertionError("Unknown Progress");
            }
        }
    }
    protected void textProgress(boolean append) {
        if (append) {
            area.append("*");
        } else {
            try {
                Document doc = area.getDocument();
                doc.remove(doc.getLength() - 1, 1);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
    //@see http://ateraimemo.com/Swing/ButtonWidth.html
    private static JComponent createRightAlignButtonBox4(List<JComponent> list, int buttonWidth, int gap) {
        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout) {
            @Override public Dimension getPreferredSize() {
                int maxHeight = list.stream().map(c -> c.getPreferredSize().height).max(Integer::compare).get();
                return new Dimension(buttonWidth * list.size() + gap + gap, maxHeight + gap + gap);
            }
        };
        Spring x = layout.getConstraint(SpringLayout.WIDTH, p);
        Spring y = Spring.constant(gap);
        Spring g = Spring.minus(Spring.constant(gap));
        Spring w = Spring.constant(buttonWidth);
        for (JComponent b: list) {
            SpringLayout.Constraints constraints = layout.getConstraints(b);
            x = Spring.sum(x, g);
            constraints.setConstraint(SpringLayout.EAST, x);
            constraints.setY(y);
            constraints.setWidth(w);
            p.add(b);
            x = Spring.sum(x, Spring.minus(w));
        }
        return p;
    }
    protected void appendLine(String str) {
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

enum Component { TOTAL, FILE, LOG, PAUSE }

class Progress {
    public final Object value;
    public final Component component;
    protected Progress(Component component, Object value) {
        this.component = component;
        this.value = value;
    }
}

class Task extends SwingWorker<String, Progress> {
    private final Random r = new Random();
    public boolean isPaused;

    @Override public String doInBackground() {
        //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
        int current = 0;
        int lengthOfTask = 12; //filelist.size();
        publish(new Progress(Component.LOG, "Length Of Task: " + lengthOfTask));
        publish(new Progress(Component.LOG, "\n------------------------------\n"));
        while (current < lengthOfTask && !isCancelled()) {
            publish(new Progress(Component.TOTAL, 100 * current / lengthOfTask));
            publish(new Progress(Component.LOG, "*"));
            try {
                convertFileToSomething();
            } catch (InterruptedException ex) {
                return "Interrupted";
            }
            current++;
        }
        publish(new Progress(Component.LOG, "\n"));
        return "Done";
    }
    private void convertFileToSomething() throws InterruptedException {
        boolean blinking = false;
        int current = 0;
        int lengthOfTask = 10 + r.nextInt(50); //long lengthOfTask = file.length();
        while (current <= lengthOfTask && !isCancelled()) {
            if (isPaused) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    return;
                }
                publish(new Progress(Component.PAUSE, blinking));
                blinking ^= true;
                continue;
            }
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
//     private final JButton cancelButton  = new JButton(new CancelAction());
//     private SwingWorker<String, String> worker;
//
//     public MainPanel() {
//         super(new BorderLayout(5, 5));
//         area.setEditable(false);
//         Box box = Box.createHorizontalBox();
//         box.add(Box.createHorizontalGlue());
//         box.add(runButton);
//         box.add(Box.createHorizontalStrut(2));
//         box.add(cancelButton);
//         add(new JScrollPane(area));
//         add(box, BorderLayout.NORTH);
//         add(statusPanel, BorderLayout.SOUTH);
//         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         setPreferredSize(new Dimension(320, 240));
//     }
//
//     class RunAction extends AbstractAction {
//         protected RunAction() {
//             super("run");
//         }
//         @Override public void actionPerformed(ActionEvent e) {
//             //System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
//             final JProgressBar bar1 = new JProgressBar(0, 100);
//             final JProgressBar bar2 = new JProgressBar(0, 100);
//             runButton.setEnabled(false);
//             cancelButton.setEnabled(true);
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
//                         } catch (InterruptedException ex) {
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
//                     cancelButton.setEnabled(false);
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
//         protected CancelAction() {
//             super("cancel");
//         }
//         @Override public void actionPerformed(ActionEvent e) {
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
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
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
