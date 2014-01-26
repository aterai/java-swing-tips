package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.*;
import javax.swing.*;

//import java.nio.file.*;
//import java.nio.file.attribute.*;
//import static java.nio.file.FileVisitResult.*;

public class MainPanel extends JPanel {
    private final JComboBox<String> dirCombo = new JComboBox<>();
    private final JFileChooser fileChooser = new JFileChooser();
    private final JTextArea textArea = new JTextArea();
    private final JProgressBar pBar  = new JProgressBar();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton  = new JButton(new RunAction());
    private final JButton canButton  = new JButton(new CancelAction());
    private final JButton openButton = new JButton(new OpenAction());
    private SwingWorker<String, Message>  worker;

    public MainPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(System.getProperty("user.dir"));
        dirCombo.setModel(model);
        dirCombo.setFocusable(false);
        textArea.setEditable(false);

        JPanel box1 = new JPanel(new BorderLayout(5, 5));
        box1.add(new JLabel("Search folder:"), BorderLayout.WEST);
        box1.add(dirCombo);
        box1.add(openButton, BorderLayout.EAST);

        Box box2 = Box.createHorizontalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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
    //@SuppressWarnings("unchecked")
    public static void addItem(JComboBox<String> dirCombo, String str, int max) {
        if(str==null || str.trim().isEmpty()) {
            return;
        }
        dirCombo.setVisible(false);
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)dirCombo.getModel();
        model.removeElement(str);
        model.insertElementAt(str, 0);
        if(model.getSize()>max) {
            model.removeElementAt(max);
        }
        dirCombo.setSelectedIndex(0);
        dirCombo.setVisible(true);
    }
    class RunAction extends AbstractAction {
        public RunAction() {
            super("Run");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            addItem(dirCombo, (String)dirCombo.getEditor().getItem(), 4);
            statusPanel.removeAll();
            statusPanel.add(pBar);
            statusPanel.revalidate();
            dirCombo.setEnabled(false);
            openButton.setEnabled(false);
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            pBar.setIndeterminate(true);
            textArea.setText("");
            File dir = new File((String)dirCombo.getSelectedItem());
            worker = new RecursiveFileSearchTask(dir) {
                @Override protected void process(List<Message> chunks) {
                    //System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
                    if(!isDisplayable()) {
                        System.out.println("process: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    for(Message c: chunks) {
                        if(c.append) {
                            appendLine(c.message);
                        }else{
                            textArea.setText(c.message+"\n");
                        }
                    }
                }
                @Override public void done() {
                    //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
                    if(!isDisplayable()) {
                        System.out.println("done: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    dirCombo.setEnabled(true);
                    openButton.setEnabled(true);
                    runButton.setEnabled(true);
                    canButton.setEnabled(false);
                    statusPanel.remove(pBar);
                    statusPanel.revalidate();

                    String text = null;
                    if(isCancelled()) {
                        text = "Cancelled";
                    }else{
                        try{
                            text = get();
                        }catch(InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            text = "Exception";
                        }
                    }
                    appendLine("----------------");
                    appendLine(text);
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(pBar));
            worker.execute();
        }
    }
    class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            if(worker!=null && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
    }
    private boolean isCancelled() {
        return (worker!=null)?worker.isCancelled():true;
    }
    class OpenAction extends AbstractAction {
        public OpenAction() {
            super("Choose...");
        }
        @Override public void actionPerformed(ActionEvent e) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //fileChooser.setDialogTitle("...");
            fileChooser.setSelectedFile(new File((String) dirCombo.getEditor().getItem()));
            int fcSelected = fileChooser.showOpenDialog(MainPanel.this);
            String title = "title";
            if(fcSelected==JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if(file==null || !file.isDirectory()) {
                    Object[] obj = {"Please select directory."};
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(MainPanel.this, obj, title, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addItem(dirCombo, file.getAbsolutePath(), 4);
                repaint();
            }else if(fcSelected==JFileChooser.CANCEL_OPTION) {
                return;
            }else{
                Object[] obj = {"Error."};
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(MainPanel.this, obj, title, JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
    private void appendLine(String str) {
        System.out.println(str);
        textArea.append(str+"\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class RecursiveFileSearchTask extends SwingWorker<String, Message> {
    private int scount = 0;
    private final File dir;
    public RecursiveFileSearchTask(File dir) {
        super();
        this.dir = dir;
    }
    @Override public String doInBackground() {
        if(dir==null || !dir.exists()) {
            publish(new Message("The directory does not exist.",true));
            return "Error";
        }
        ArrayList<File> list = new ArrayList<File>();
        //ArrayList<Path> list = new ArrayList<>();
        try{
            scount = 0;
            recursiveSearch(dir, list);
        }catch(InterruptedException ie) {
            //recursiveSearch(dir.toPath(), list);
            //}catch(Exception ie) {
            publish(new Message("The search was canceled",true));
            return "Interrupted1";
        }
        firePropertyChange("clear-textarea", "", "");

        final int lengthOfTask = list.size();
        publish(new Message("Length Of Task: "+lengthOfTask,false));
        publish(new Message("----------------",true));

        try{
            int current = 0;
            while(current<lengthOfTask && !isCancelled()) {
                //if(!pBar.isDisplayable()) {
                //    return "Disposed";
                //}
                File file = list.get(current);
                //Path path = list.get(current);
                Thread.sleep(50); //dummy
                setProgress(100 * current / lengthOfTask);
                publish(new Message(current+"/"+lengthOfTask + ", "+file.getAbsolutePath(),true));
                current++;
            }
        }catch(InterruptedException ie) {
            return "Interrupted";
        }
        return "Done";
    }
//*
    private void recursiveSearch(File dir, final List<File> list) throws InterruptedException {
        //System.out.println("recursiveSearch() is EDT?: " + EventQueue.isDispatchThread());
        for(String fname: dir.list()) {
            if(Thread.interrupted()) {
                throw new InterruptedException();
            }
            File sdir = new File(dir, fname);
            if(sdir.isDirectory()) {
                recursiveSearch(sdir, list);
            }else{
                scount++;
                if(scount%100==0) {
                    publish(new Message("Results:"+scount+"\n",false));
                }
                list.add(sdir);
            }
        }
    }
/*/             //http://docs.oracle.com/javase/tutorial/essential/io/walk.html
                private void recursiveSearch(Path dir, final ArrayList<Path> list) throws IOException {
                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if(Thread.interrupted()) {
                                throw new IOException();
                            }
                            if(attrs.isRegularFile()) {
                                list.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
//*/
}

class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent e) {
        String strPropertyName = e.getPropertyName();
        if("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer)e.getNewValue();
            progressBar.setValue(progress);
        }
    }
}

class Message {
    public final String message;
    public final boolean append;
    public Message(String message, boolean append) {
        this.message = message;
        this.append  = append;
    }
}
