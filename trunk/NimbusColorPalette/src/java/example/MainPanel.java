package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
// JDK 1.6.0 import com.sun.java.swing.Painter;

public class MainPanel extends JPanel {
    private static BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
    public MainPanel() {
        super(new BorderLayout());

        UIDefaults def = UIManager.getLookAndFeelDefaults(); //new UIDefaults();
        def.put("nimbusOrange", new Color(255,220,35,200));

        UIDefaults d = new UIDefaults();
        d.put("ProgressBar[Enabled].foregroundPainter", new Painter() {
            @Override public void paint(Graphics2D g, Object o, int w, int h) {
                g.setColor(new Color(100,250,120,50));
                g.fillRect(0,0,w-1,h-1);
                g.setColor(new Color(100,250,120,150));
                g.fillRect(3,h/2,w-5,h/2-2);
            }
        });
        d.put("ProgressBar[Enabled+Finished].foregroundPainter", new Painter() {
            @Override public void paint(Graphics2D g, Object o, int w, int h) {
                g.setColor(new Color(100,250,120,50));
                g.fillRect(0,0,w-1,h-1);
                g.setColor(new Color(100,250,120,150));
                g.fillRect(3,h/2,w-5,h/2-2);
            }
        });

        final JProgressBar progressBar1 = new JProgressBar(model);
        final JProgressBar progressBar2 = new JProgressBar(model);

        progressBar2.putClientProperty("Nimbus.Overrides", d);

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(progressBar1);
        p.add(progressBar2);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Test start") {
            SwingWorker<String, Void> worker;
            @Override public void actionPerformed(ActionEvent e) {
                if(worker!=null && !worker.isDone()) worker.cancel(true);
                worker = new SwingWorker<String, Void>() {
                    @Override public String doInBackground() {
                        int current = 0;
                        int lengthOfTask = 100;
                        while(current<=lengthOfTask && !isCancelled()) {
                            try{ // dummy task
                                Thread.sleep(50);
                            }catch(InterruptedException ie) {
                                return "Interrupted";
                            }
                            setProgress(100 * current / lengthOfTask);
                            current++;
                        }
                        return "Done";
                    }
                    @Override public void done() {
                        String text = null;
                        if(isCancelled()) {
                            text = "Cancelled";
                        }else{
                            try{
                                text = get();
                            }catch(Exception ex) {
                                ex.printStackTrace();
                                text = "Exception";
                            }
                        }
                    }
                };
                worker.addPropertyChangeListener(new ProgressListener(progressBar1));
                worker.execute();
            }
        }));
        box.add(Box.createHorizontalStrut(5));

        add(p);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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
            for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels())
              if("Nimbus".equals(laf.getName())) UIManager.setLookAndFeel(laf.getClassName());
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
