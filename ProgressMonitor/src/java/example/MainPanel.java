package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea area     = new JTextArea();
    private final JButton runButton  = new JButton(new RunAction());
    private transient SwingWorker<String, String> worker;
    private final transient ProgressMonitor monitor; // = new ProgressMonitor(p, "message", "note", 0, 100);

    public MainPanel() {
        super(new BorderLayout(5, 5));
        monitor = new ProgressMonitor(this, "message", "note", 0, 100);
        area.setEditable(false);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(runButton);
        add(new JScrollPane(area));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    class RunAction extends AbstractAction {
        public RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            //System.out.println("actionPerformed() is EDT?: " + EventQueue.isDispatchThread());
            runButton.setEnabled(false);
            monitor.setProgress(0);
            worker = new Task() {
                @Override protected void process(List<String> chunks) {
                    //System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
                    for (String message: chunks) {
                        monitor.setNote(message);
                    }
                }
                @Override public void done() {
                    //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
                    runButton.setEnabled(true);
                    monitor.close();
                    try {
                        if (isCancelled()) {
                            area.append("Cancelled\n");
                        } else {
                            area.append(get() + "\n");
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        area.append("Exception\n");
                    }
                    area.setCaretPosition(area.getDocument().getLength());
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(monitor));
            worker.execute();
        }
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, String> {
    @Override public String doInBackground() {
        //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
        int current = 0;
        int lengthOfTask = 120; //list.size();
        while (current < lengthOfTask && !isCancelled()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                return "Interrupted";
            }
            setProgress(100 * current / lengthOfTask);
            publish(current + "/" + lengthOfTask);
            current++;
        }
        return "Done";
    }
}

class ProgressListener implements PropertyChangeListener {
    private final ProgressMonitor monitor;
    public ProgressListener(ProgressMonitor monitor) {
        this.monitor = monitor;
        this.monitor.setProgress(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        String strPropertyName = e.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            monitor.setProgress((Integer) e.getNewValue());
            if (monitor.isCanceled()) {
                ((SwingWorker) e.getSource()).cancel(true);
            }
        }
    }
}
