package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> dirCombo = new JComboBox<>();
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextArea textArea = new JTextArea();
    private final JProgressBar progress = new JProgressBar();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton = new JButton("Run");
    private final JButton canButton = new JButton("Cancel");
    private final JButton openButton = new JButton("Choose...");
    private transient SwingWorker<String, Message> worker;

    public MainPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(System.getProperty("user.dir"));
        dirCombo.setModel(model);
        dirCombo.setFocusable(false);
        textArea.setEditable(false);
        statusPanel.add(progress);
        statusPanel.setVisible(false);

        runButton.addActionListener(e -> {
            updateComponentStatus(true);
            executeWorker();
        });

        canButton.addActionListener(e -> {
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        });

        openButton.addActionListener(e -> {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            // fileChooser.setDialogTitle("...");
            fileChooser.setSelectedFile(new File(Objects.toString(dirCombo.getEditor().getItem())));
            int fcSelected = fileChooser.showOpenDialog(getRootPane());
            String title = "title";
            if (fcSelected == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (Objects.isNull(file) || !file.isDirectory()) {
                    Object[] obj = {"Please select directory."};
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(getRootPane(), obj, title, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addItem(dirCombo, file.getAbsolutePath(), 4);
                repaint();
            } else if (fcSelected == JFileChooser.CANCEL_OPTION) {
                return;
            } else {
                Object[] obj = {"Error."};
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(getRootPane(), obj, title, JOptionPane.ERROR_MESSAGE);
                return;
            }
        });

        JPanel box1 = new JPanel(new BorderLayout(5, 5));
        box1.add(new JLabel("Search folder:"), BorderLayout.WEST);
        box1.add(dirCombo);
        box1.add(openButton, BorderLayout.EAST);

        Box box2 = Box.createHorizontalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box2.add(Box.createHorizontalGlue());
        box2.add(runButton);
        box2.add(Box.createHorizontalStrut(2));
        box2.add(canButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(box1, BorderLayout.NORTH);
        panel.add(box2, BorderLayout.SOUTH);

        add(new JScrollPane(textArea));
        add(panel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    protected class UITask extends RecursiveFileSearchTask {
        protected UITask(File dir) {
            super(dir);
        }
        @Override protected void process(List<Message> chunks) {
            // System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
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
            // System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
            if (!isDisplayable()) {
                System.out.println("done: DISPOSE_ON_CLOSE");
                cancel(true);
                return;
            }
            updateComponentStatus(false);
            String text;
            if (isCancelled()) {
                text = "Cancelled";
            } else {
                try {
                    text = get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    text = "Exception";
                }
            }
            appendLine("----------------");
            appendLine(text);
        }
    }

    protected void updateComponentStatus(boolean start) {
        if (start) {
            addItem(dirCombo, Objects.toString(dirCombo.getEditor().getItem()), 4);
            statusPanel.setVisible(true);
            dirCombo.setEnabled(false);
            openButton.setEnabled(false);
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            progress.setIndeterminate(true);
            textArea.setText("");
        } else {
            dirCombo.setEnabled(true);
            openButton.setEnabled(true);
            runButton.setEnabled(true);
            canButton.setEnabled(false);
            statusPanel.setVisible(false);
        }
    }

    protected static void addItem(JComboBox<String> dirCombo, String str, int max) {
        if (Objects.isNull(str) || str.isEmpty()) {
            return;
        }
        dirCombo.setVisible(false);
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dirCombo.getModel();
        model.removeElement(str);
        model.insertElementAt(str, 0);
        if (model.getSize() > max) {
            model.removeElementAt(max);
        }
        dirCombo.setSelectedIndex(0);
        dirCombo.setVisible(true);
    }

    protected void executeWorker() {
        File dir = new File(dirCombo.getItemAt(dirCombo.getSelectedIndex()));
        worker = new UITask(dir);
        worker.addPropertyChangeListener(new ProgressListener(progress));
        worker.execute();
    }

    protected void processChunks(List<Message> chunks) {
        chunks.forEach(m -> {
            if (m.append) {
                appendLine(m.text);
            } else {
                textArea.setText(m.text + "\n");
            }
        });
    }

    protected void appendLine(String str) {
        // System.out.println(str);
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
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

// class RecursiveFileSearchTask extends SwingWorker<String, Message> {
//     private int scount;
//     private final File dir;
//     protected RecursiveFileSearchTask(File dir) {
//         super();
//         this.dir = dir;
//     }
//     @Override public String doInBackground() {
//         if (Objects.isNull(dir) || !dir.exists()) {
//             publish(new Message("The directory does not exist.", true));
//             return "Error";
//         }
//         List<File> list = new ArrayList<>();
//         // ArrayList<Path> list = new ArrayList<>();
//         try {
//             scount = 0;
//             recursiveSearch(dir, list);
//         } catch (InterruptedException ex) {
//             // recursiveSearch(dir.toPath(), list);
//             // } catch (Exception ex) {
//             publish(new Message("The search was canceled", true));
//             return "Interrupted1";
//         }
//         firePropertyChange("clear-textarea", "", "");
//
//         int lengthOfTask = list.size();
//         publish(new Message("Length Of Task: " + lengthOfTask, false));
//         publish(new Message("----------------", true));
//
//         try {
//             int current = 0;
//             while (current < lengthOfTask && !isCancelled()) {
//                 // if (!progress.isDisplayable()) {
//                 //     return "Disposed";
//                 // }
//                 File file = list.get(current);
//                 // Path path = list.get(current);
//                 Thread.sleep(50); // dummy
//                 setProgress(100 * current / lengthOfTask);
//                 current++;
//                 publish(new Message(current + "/" + lengthOfTask + ", " + file.getAbsolutePath(), true));
//             }
//         } catch (InterruptedException ex) {
//             return "Interrupted";
//         }
//         return "Done";
//     }
//     private void recursiveSearch(File dir, List<File> list) throws InterruptedException {
//         // System.out.println("recursiveSearch() is EDT?: " + EventQueue.isDispatchThread());
//         for (String fname: dir.list()) {
//             if (Thread.interrupted()) {
//                 throw new InterruptedException();
//             }
//             File sdir = new File(dir, fname);
//             if (sdir.isDirectory()) {
//                 recursiveSearch(sdir, list);
//             } else {
//                 scount++;
//                 if (scount % 100 == 0) {
//                     publish(new Message("Results:" + scount + "\n", false));
//                 }
//                 list.add(sdir);
//             }
//         }
//     }
// }

class RecursiveFileSearchTask extends SwingWorker<String, Message> {
    protected int scount;
    protected final File dir;
    protected RecursiveFileSearchTask(File dir) {
        super();
        this.dir = dir;
    }
    @Override public String doInBackground() {
        if (Objects.isNull(dir) || !dir.exists()) {
            publish(new Message("The directory does not exist.", true));
            return "Error";
        }

        List<Path> list = new ArrayList<>();
        try {
            scount = 0;
            recursiveSearch(dir.toPath(), list);
        } catch (IOException ex) {
            publish(new Message("The search was canceled", true));
            return "Interrupted1";
        }
        firePropertyChange("clear-textarea", "", "");

        int lengthOfTask = list.size();
        publish(new Message("Length Of Task: " + lengthOfTask, false));
        publish(new Message("----------------", true));

        try {
            int current = 0;
            while (current < lengthOfTask && !isCancelled()) {
                Thread.sleep(10); // dummy
                setProgress(100 * current / lengthOfTask);
                Path path = list.get(current);
                current++;
                publish(new Message(current + "/" + lengthOfTask + ", " + path, true));
            }
        } catch (InterruptedException ex) {
            return "Interrupted";
        }
        return "Done";
    }
    // Walking the File Tree (The Java™ Tutorials > Essential Classes > Basic I/O)
    // https://docs.oracle.com/javase/tutorial/essential/io/walk.html
    private void recursiveSearch(Path dirPath, List<Path> list) throws IOException {
        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Thread.interrupted()) {
                    throw new IOException();
                }
                if (attrs.isRegularFile()) {
                    scount++;
                    if (scount % 100 == 0) {
                        publish(new Message("Results:" + scount + "\n", false));
                    }
                    list.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    protected ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        String strPropertyName = e.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) e.getNewValue();
            progressBar.setValue(progress);
        }
    }
}

class Message {
    public final String text;
    public final boolean append;
    protected Message(String text, boolean append) {
        this.text = text;
        this.append = append;
    }
}
