package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(3,1));
        p.add(makeUI());
        p.add(makeUI());
        p.add(makeUI());
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    public JComponent makeUI() {
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setOpaque(false);
        progressBar.setUI(new GradientPalletProgressBarUI());

        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(32,8,0,8));

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;

        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 4);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(progressBar, c);

        c.gridx = 1;
        c.weightx = 0.0;
        p.add(new JButton(new AbstractAction("Start") {
            @Override public void actionPerformed(ActionEvent e) {
                final JButton b = (JButton)e.getSource();
                b.setEnabled(false);
                SwingWorker<Void,Void> worker = new Task() {
                    @Override public void done() {
                        if(b.isDisplayable()) {
                            b.setEnabled(true);
                        }
                    }
                };
                worker.addPropertyChangeListener(new ProgressListener(progressBar));
                worker.execute();
            }
        }), c);
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<Void,Void> {
    @Override public Void doInBackground() {
        int current = 0, lengthOfTask = 100;
        while(current<=lengthOfTask && !isCancelled()) {
            try{ // dummy task
                Thread.sleep(50);
            }catch(InterruptedException ie) {
                return null;
            }
            setProgress(100 * current / lengthOfTask);
            current++;
        }
        return null;
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

class GradientPalletProgressBarUI extends BasicProgressBarUI{
    private final int[] pallet;
    public GradientPalletProgressBarUI() {
        super();
        this.pallet = makeGradientPallet();
    }
    private static int[] makeGradientPallet() {
        BufferedImage image = new BufferedImage(100, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2  = image.createGraphics();
        Point2D start  = new Point2D.Float(0f, 0f);
        Point2D end    = new Point2D.Float(99f, 0f);
        float[] dist   = {0.0f, 0.5f, 1.0f};
        Color[] colors = { Color.RED, Color.YELLOW, Color.GREEN };
        g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
        g2.fillRect(0, 0, 100, 1);
        g2.dispose();

        int width  = image.getWidth(null);
        int[] pallet = new int[width];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, 1, pallet, 0, width);
        try{
            pg.grabPixels();
        }catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        return pallet;
    }
    private static Color getColorFromPallet(int[] pallet, float x) {
        if(x < 0.0 || x > 1.0) {
            throw new IllegalArgumentException("Parameter outside of expected range");
        }
        int i = (int)(pallet.length * x);
        int max = pallet.length-1;
        int index = i<0?0:i>max?max:i;
        return new Color(pallet[index] & 0x00ffffff);
        //translucent
        //int pix = pallet[index] & 0x00ffffff | (0x64 << 24);
        //return new Color(pix), true);
    }
    @Override public void paintDeterminate(Graphics g, JComponent c) {
        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth  = progressBar.getWidth()  - b.right - b.left;
        int barRectHeight = progressBar.getHeight() - b.top   - b.bottom;
        if(barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }
        //int cellLength = getCellLength();
        //int cellSpacing = getCellSpacing();
        // amount of progress to draw
        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        if(progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            // draw the cells
            float x = amountFull / (float)barRectWidth;
            g.setColor(getColorFromPallet(pallet, x));
            g.fillRect(b.left, b.top, amountFull, barRectHeight);

        }else{ // VERTICAL
            //XXX
            super.paintDeterminate(g, c);
            return;
        }
        // Deal with possible text painting
        if(progressBar.isStringPainted()) {
            paintString(g, b.left, b.top, barRectWidth, barRectHeight, amountFull, b);
        }
    }
}
