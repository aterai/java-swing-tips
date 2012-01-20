package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingworker.SwingWorker;
//import javax.swing.SwingWorker;

public class MainPanel extends JPanel {
    private final JTextArea area     = new JTextArea();
    private final JProgressBar bar   = new JProgressBar();
    private final JPanel statusPanel = new JPanel(new BorderLayout());
    private final JButton runButton  = new JButton(new RunAction());
    private final JButton canButton  = new JButton(new CancelAction());
    private final AnimatedLabel anil = new AnimatedLabel();
    private SwingWorker<String, String> worker;

    public MainPanel() {
        super(new BorderLayout());
        area.setEditable(false);
        Box box = Box.createHorizontalBox();
        box.add(anil);
        box.add(Box.createHorizontalGlue());
        box.add(runButton);
        box.add(canButton);
        add(box, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
        add(new JScrollPane(area));
        setPreferredSize(new Dimension(320, 200));
    }

    class RunAction extends AbstractAction{
        public RunAction() {
            super("run");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            final JProgressBar bar = new JProgressBar(0, 100);
            runButton.setEnabled(false);
            canButton.setEnabled(true);
            anil.startAnimation();
            statusPanel.removeAll();
            statusPanel.add(bar);
            statusPanel.revalidate();
            bar.setIndeterminate(true);
            worker = new SwingWorker<String, String>() {
                @Override public String doInBackground() {
                    //System.out.println("doInBackground() is EDT?: " + EventQueue.isDispatchThread());
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException ie) {
                        if(isCancelled()) {
                            worker.cancel(true);
                        }
                        return "Interrupted";
                    }
                    int current = 0;
                    int lengthOfTask = 120; //list.size();
                    publish("Length Of Task: " + lengthOfTask);
                    publish("------------------------------");
                    while(current<lengthOfTask && !isCancelled()) {
                        try{
                            Thread.sleep(50);
                        }catch(InterruptedException ie) {
                            return "Interrupted";
                        }
                        setProgress(100 * current / lengthOfTask);
                        current++;
                    }
                    return "Done";
                }
                @Override protected void process(List<String> chunks) {
                    System.out.println("process() is EDT?: " + EventQueue.isDispatchThread());
                    for(String message : chunks) {
                        appendLine(message);
                    }
                }
                @Override public void done() {
                    //System.out.println("done() is EDT?: " + EventQueue.isDispatchThread());
                    anil.stopAnimation();
                    runButton.setEnabled(true);
                    canButton.setEnabled(false);
                    statusPanel.remove(bar);
                    statusPanel.revalidate();
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
                    appendLine(text);
                }
            };
            worker.addPropertyChangeListener(new ProgressListener(bar));
            worker.execute();
        }
    }
    class CancelAction extends AbstractAction{
        public CancelAction() {
            super("cancel");
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
    private void appendLine(String str) {
        area.append(str+"\n");
        area.setCaretPosition(area.getDocument().getLength());
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setResizable(false);
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
    @Override public void propertyChange(PropertyChangeEvent e) {
        String strPropertyName = e.getPropertyName();
        if("progress".equals(strPropertyName)) {
            progressBar.setIndeterminate(false);
            int progress = (Integer)e.getNewValue();
            progressBar.setValue(progress);
        }
    }
}

class AnimatedLabel extends JLabel implements ActionListener {
    private final Timer animator;
    private final AnimeIcon icon = new AnimeIcon();
    public AnimatedLabel() {
        super();
        animator = new Timer(100, this);
        setIcon(icon);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !isDisplayable()) {
                    stopAnimation();
                }
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        icon.next();
        repaint();
    }
    public void startAnimation() {
        icon.setRunning(true);
        animator.start();
    }
    public void stopAnimation() {
        icon.setRunning(false);
        animator.stop();
    }
}

class AnimeIcon implements Icon {
    private static final Color cColor = new Color(0.5f,0.5f,0.5f);
    private static final double r  = 2.0d;
    private static final double sx = 1.0d;
    private static final double sy = 1.0d;
    private static final Dimension dim = new Dimension((int)(r*8+sx*2), (int)(r*8+sy*2));
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(sx+3*r, sy+0*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+1*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+6*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+3*r, sy+6*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+0*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+1*r, 2*r, 2*r)));

    private boolean isRunning = false;
    public void next() {
        if(isRunning) list.add(list.remove(0));
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public int getIconWidth()  { return dim.width;  }
    @Override public int getIconHeight() { return dim.height; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint((c!=null)?c.getBackground():Color.WHITE);
        g2d.fillRect(x, y, getIconWidth(), getIconHeight());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(cColor);
        float alpha = 0.0f;
        g2d.translate(x, y);
        for(Shape s: list) {
            alpha = isRunning?alpha+0.1f:0.5f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fill(s);
        }
        g2d.translate(-x, -y);
    }
}
