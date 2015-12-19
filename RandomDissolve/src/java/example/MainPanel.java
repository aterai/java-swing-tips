package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import javax.imageio.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        BufferedImage i1 = makeImage(getClass().getResource("test.png"));
        BufferedImage i2 = makeImage(getClass().getResource("test.jpg"));
        final RandomDissolve randomDissolve = new RandomDissolve(i1, i2);
        JButton button = new JButton(new AbstractAction("change") {
            @Override public void actionPerformed(ActionEvent ae) {
                randomDissolve.animationStart();
            }
        });
        add(randomDissolve);
        add(button, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private BufferedImage makeImage(URL url) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return img;
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class RandomDissolve extends JComponent implements ActionListener {
    private static final int STAGES = 16;
    private final Timer animator;
    private final transient BufferedImage image1;
    private final transient BufferedImage image2;
    private transient BufferedImage srcimg;
    private boolean mode = true;
    private int currentStage;
    private int[] src, dst, step;

    protected RandomDissolve(BufferedImage i1, BufferedImage i2) {
        super();
        this.image1 = i1;
        this.image2 = i2;
        this.srcimg = copyImage(mode ? image2 : image1);
        animator = new Timer(10, this);
    }
    public boolean nextStage() {
        if (currentStage > 0) {
            currentStage = currentStage - 1;
            for (int i = 0; i < step.length; i++) {
                if (step[i] == currentStage) {
                    src[i] = dst[i];
                }
            }
            return true;
        } else {
            return false;
        }
    }
    private BufferedImage copyImage(final BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }
    private int[] getData(BufferedImage image) {
        WritableRaster wr = image.getRaster();
        DataBufferInt dbi = (DataBufferInt) wr.getDataBuffer();
        return dbi.getData();
        //return ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
    }
    public void animationStart() {
        currentStage = STAGES;
        srcimg = copyImage(mode ? image2 : image1);
        src    = getData(srcimg);
        dst    = getData(copyImage(mode ? image1 : image2));
        step   = new int[src.length];
        mode  ^= true;
        Random rnd = new Random();
        for (int i = 0; i < step.length; i++) {
            step[i] = rnd.nextInt(currentStage);
        }
        animator.start();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.drawImage(srcimg, 0, 0, srcimg.getWidth(), srcimg.getHeight(), this);
        g2.dispose();
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (nextStage()) {
            repaint();
        } else {
            animator.stop();
        }
    }
}
