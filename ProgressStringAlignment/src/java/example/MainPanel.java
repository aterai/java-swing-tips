package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel implements HierarchyListener {
    private static BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
    private final JProgressBar progressBar1 = new StringAlignmentProgressBar(model, SwingConstants.RIGHT);
    private final JProgressBar progressBar2 = new StringAlignmentProgressBar(model, SwingConstants.LEFT);
    private final List<JProgressBar> list = Arrays.<JProgressBar>asList(progressBar1, progressBar2);
    private SwingWorker<String, Void> worker;

    public MainPanel() {
        super(new BorderLayout());

        progressBar2.setBorder(BorderFactory.createTitledBorder("TitledBorder"));

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(progressBar1);
        p.add(progressBar2);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JCheckBox(new AbstractAction("setStringPainted") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                for(JProgressBar bar: list) {
                    bar.setStringPainted(cb.isSelected());
                }
            }
        }));
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new AbstractAction("Test") {
            @Override public void actionPerformed(ActionEvent e) {
                if(worker!=null && !worker.isDone()) {
                    worker.cancel(true);
                }
                worker = new Task();
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.execute();
            }
        }));
        box.add(Box.createHorizontalStrut(5));

        addHierarchyListener(this);
        add(p);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void hierarchyChanged(HierarchyEvent he) {
        JComponent c = (JComponent)he.getComponent();
        if((he.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !c.isDisplayable() && worker!=null) {
            System.out.println("DISPOSE_ON_CLOSE");
            worker.cancel(true);
            worker = null;
        }
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
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for(UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
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

class StringAlignmentProgressBar extends JProgressBar {
    private final JLabel label;
    private ChangeListener changeListener;

    public StringAlignmentProgressBar(BoundedRangeModel model, int halign) {
        super(model);
        label = new JLabel(getString(), halign);
    }
    @Override public void updateUI() {
        removeAll();
        if(changeListener!=null) {
            removeChangeListener(changeListener);
        }
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setLayout(new BorderLayout());
                changeListener = new ChangeListener() {
                    @Override public void stateChanged(ChangeEvent e) {
                        //BoundedRangeModel m = (BoundedRangeModel)e.getSource(); //label.setText(m.getValue()+"%");
                        label.setText(getString());
                    }
                };
                addChangeListener(changeListener);
                add(label);
                label.setBorder(BorderFactory.createEmptyBorder(0,4,0,4));
            }
        });
    }
}

class Task extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        int current = 0;
        int lengthOfTask = 100;
        while(current<=lengthOfTask && !isCancelled()) {
            try { // dummy task
                Thread.sleep(50);
            }catch(InterruptedException ie) {
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
    ProgressListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setValue(0);
    }
    @Override public void propertyChange(PropertyChangeEvent evt) {
        String strPropertyName = evt.getPropertyName();
        if("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer)evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
}
