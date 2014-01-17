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

public class MainPanel extends JPanel {
    private static BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
    public JProgressBar makeProgressBar(final int halign) {
        return new JProgressBar(model) {
            private final JLabel label = new JLabel(getString(), halign);
            private ChangeListener changeListener = null;
            @Override public void updateUI() {
                removeAll();
                if(changeListener!=null) { removeChangeListener(changeListener); }
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
        };
    }

    private final JProgressBar progressBar1 = makeProgressBar(SwingConstants.RIGHT);
    private final JProgressBar progressBar2 = makeProgressBar(SwingConstants.LEFT);
    private final List<JProgressBar> list = Arrays.<JProgressBar>asList(progressBar1, progressBar2);
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
            SwingWorker<String, Void> worker;
            @Override public void actionPerformed(ActionEvent e) {
                if(worker!=null && !worker.isDone()) { worker.cancel(true); }
                worker = new SwingWorker<String, Void>() {
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
//                     @Override public void done() {
//                         String text = null;
//                         if(isCancelled()) {
//                             text = "Cancelled";
//                         }else{
//                             try{
//                                 text = get();
//                             }catch(Exception ex) {
//                                 ex.printStackTrace();
//                                 text = "Exception";
//                             }
//                         }
//                         //appendLine(text);
//                     }
                };
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.execute();
            }
        }));
        box.add(Box.createHorizontalStrut(5));

        add(p);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
