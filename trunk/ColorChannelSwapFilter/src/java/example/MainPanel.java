package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

class MainPanel extends JPanel implements HierarchyListener {
    private final BoundedRangeModel model = new DefaultBoundedRangeModel();
    private final JProgressBar progress01 = new JProgressBar(model);
    private final JProgressBar progress02 = new JProgressBar(model);
    private final JProgressBar progress03 = new JProgressBar(model);
    private final JProgressBar progress04 = new JProgressBar(model);
    private final BlockedColorLayerUI layerUI = new BlockedColorLayerUI();
    private final JPanel p = new JPanel(new GridLayout(2,1));
    private SwingWorker<String, Void> worker;

    public MainPanel() {
        super(new BorderLayout());
        progress01.setStringPainted(true);
        progress02.setStringPainted(true);

        progress04.setOpaque(true); //for NimbusLookAndFeel

        p.add(makeTitlePanel("setStringPainted(true)",  Arrays.asList(progress01, progress02)));
        p.add(makeTitlePanel("setStringPainted(false)", Arrays.asList(progress03, new JLayer<JProgressBar>(progress04, layerUI))));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JCheckBox(new AbstractAction("Turn the progress bar red") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean b = ((JCheckBox)e.getSource()).isSelected();
                progress02.setForeground(b? new Color(255,0,0,100) : progress01.getForeground());
                layerUI.isPreventing = b;
                p.repaint();
            }
        }));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("Start") {
            @Override public void actionPerformed(ActionEvent e) {
                if(worker!=null && !worker.isDone()) {
                    worker.cancel(true);
                }
                worker = new Task();
                worker.addPropertyChangeListener(new ProgressListener(progress01));
                worker.execute();
            }
        }));
        box.add(Box.createHorizontalStrut(2));

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
    private JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.gridy   = 0;
        for(JComponent cmp:list) {
            p.add(cmp, c);
            c.gridy++;
        }
        return p;
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

class BlockedColorLayerUI extends LayerUI<JProgressBar>{
    public boolean isPreventing;
    private transient BufferedImage bi;
    private int prevw = -1;
    private int prevh = -1;

    @Override public void paint(Graphics g, JComponent c) {
        if(isPreventing && c instanceof JLayer) {
            JLayer jlayer = (JLayer)c;
            JProgressBar progress = (JProgressBar)jlayer.getView();
            int w = progress.getSize().width;
            int h = progress.getSize().height;

            if(bi==null || w!=prevw || h!=prevh) {
                bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            prevw = w;
            prevh = h;

            Graphics2D g2 = bi.createGraphics();
            super.paint(g2, c);
            g2.dispose();

            Image image = c.createImage(new FilteredImageSource(bi.getSource(), new RedGreenChannelSwapFilter()));
            g.drawImage(image, 0, 0, c);
        }else{
            super.paint(g, c);
        }
    }
}

class RedGreenChannelSwapFilter extends RGBImageFilter {
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (int)((argb >> 16) & 0xff);
        int g = (int)((argb >>  8) & 0xff);
        int b = (int)((argb      ) & 0xff);
        return (argb & 0xff000000) | (g<<16) | (r<<8) | (b);
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
