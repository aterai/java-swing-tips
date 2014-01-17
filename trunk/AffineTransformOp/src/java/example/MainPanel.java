package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.imageio.*;

class MainPanel extends JPanel {
    public enum Flip {
        NONE,
        VERTICAL,
        HORIZONTAL,
    }
    private Flip mode = Flip.NONE;
    private BufferedImage bi = null;
    public MainPanel() {
        super(new BorderLayout());
        try{
            bi = ImageIO.read(getClass().getResource("test.jpg"));
        }catch(Exception ioe) {
            ioe.printStackTrace();
        }
        JPanel p = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                int w = bi.getWidth(this);
                int h = bi.getHeight(this);
                if(mode==Flip.NONE) {
                    g.drawImage(bi, 0, 0, w, h, this);
                }else if(mode==Flip.VERTICAL) {
                    AffineTransform at = AffineTransform.getScaleInstance(1.0, -1.0);
                    at.translate(0, -h);
                    g2.drawImage(bi, at, this);
                }else if(mode==Flip.HORIZONTAL) {
                    AffineTransform at = AffineTransform.getScaleInstance(-1.0, 1.0);
                    at.translate(-w, 0);
                    AffineTransformOp atOp = new AffineTransformOp(at, null);
                    g.drawImage(atOp.filter(bi, null), 0, 0, w, h, this);
                }
            }
        };
        List<AbstractAction> list = Arrays.asList(
            new AbstractAction("NONE") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.NONE;
                    repaint();
                }
            },
            new AbstractAction("VERTICAL") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.VERTICAL;
                    repaint();
                }
            },
            new AbstractAction("HORIZONTAL") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.HORIZONTAL;
                    repaint();
                }
            }
        );
        Box box = Box.createHorizontalBox();
        ButtonGroup bg = new ButtonGroup();
        box.add(Box.createHorizontalGlue());
        box.add(new JLabel("Flip: "));
        for(AbstractAction a:list) {
            JRadioButton rb = new JRadioButton(a);
            if(bg.getButtonCount()==0) { rb.setSelected(true); }
            box.add(rb); bg.add(rb);
            box.add(Box.createHorizontalStrut(5));
        }
        add(p);
        add(box, BorderLayout.SOUTH);
        setOpaque(false);
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
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
