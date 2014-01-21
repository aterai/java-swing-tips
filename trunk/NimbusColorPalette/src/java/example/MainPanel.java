package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
// JDK 1.6.0 import com.sun.java.swing.Painter;

public class MainPanel extends JPanel implements HierarchyListener {
    private static BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
    private SwingWorker<String, Void> worker;
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
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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
            for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
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

class Task extends SwingWorker<String, Void> {
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
