package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel implements HierarchyListener {
    private final BoundedRangeModel model = new DefaultBoundedRangeModel();
    private SwingWorker<String, Void> worker;
    public MainPanel() {
        super(new BorderLayout());
        final JProgressBar progressBar0 = new JProgressBar(model);

        UIManager.put("ProgressBar.foreground", Color.RED);
        UIManager.put("ProgressBar.selectionForeground", Color.ORANGE);
        UIManager.put("ProgressBar.background", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Color.RED);
        final JProgressBar progressBar1 = new JProgressBar(model);

        final JProgressBar progressBar2 = new JProgressBar(model);
        progressBar2.setForeground(Color.BLUE);
        progressBar2.setBackground(Color.CYAN.brighter());
        progressBar2.setUI(new BasicProgressBarUI() {
            @Override protected Color getSelectionForeground() {
                return Color.PINK;
            }
            @Override protected Color getSelectionBackground() {
                return Color.BLUE;
            }
        });

        progressBar0.setStringPainted(true);
        progressBar1.setStringPainted(true);
        progressBar2.setStringPainted(true);

        JPanel p = new JPanel(new GridLayout(5, 1));
        p.add(makePanel(progressBar0));
        p.add(makePanel(progressBar1));
        p.add(makePanel(progressBar2));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Test start") {
            @Override public void actionPerformed(ActionEvent e) {
                if (worker != null && !worker.isDone()) {
                    worker.cancel(true);
                }
                worker = new Task();
                worker.addPropertyChangeListener(new ProgressListener(progressBar0));
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.addPropertyChangeListener(new ProgressListener(progressBar2));
                worker.execute();
            }
        }));
        box.add(Box.createHorizontalStrut(5));

        addHierarchyListener(this);
        add(p);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void hierarchyChanged(HierarchyEvent he) {
        JComponent c = (JComponent) he.getComponent();
        if ((he.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !c.isDisplayable() && worker != null) {
            System.out.println("DISPOSE_ON_CLOSE");
            worker.cancel(true);
            worker = null;
        }
    }

    private static JComponent makePanel(JComponent cmp) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.gridy   = 0;
        p.add(cmp, c);
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        int current = 0;
        int lengthOfTask = 100;
        while (current <= lengthOfTask && !isCancelled()) {
            try { // dummy task
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                return "Interrupted";
            }
            setProgress(100 * current / lengthOfTask);
            current++;
        }
        return "Done";
    }
}

class ProgressListener implements PropertyChangeListener {
    private final JProgressBar progressBar;
    public ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if ("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
