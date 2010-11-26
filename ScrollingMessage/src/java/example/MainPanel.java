package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new MarqueePanel());
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

class MarqueePanel extends JComponent implements ActionListener {
    public final javax.swing.Timer animator;
    private final GlyphVector gv;
    private float xx, yy;
    private int cw, ch;

    public MarqueePanel() {
        super();
        animator = new javax.swing.Timer(10, this);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                JComponent c = (JComponent)e.getSource();
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 &&
                   animator!=null && !c.isDisplayable()) {
                    animator.stop();
                }
            }
        });

        String text = "asffdfaaAAASFDsfasdfsdfasdfasd";
        Font font = new Font("serif", Font.PLAIN, 100);
        FontRenderContext frc = new FontRenderContext(null,true,true);

        gv = font.createGlyphVector(frc, text);
        LineMetrics lm = font.getLineMetrics(text, frc);
        yy = lm.getAscent()/2f + (float)gv.getVisualBounds().getY();
        animator.start();
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.WHITE);
        g2.draw(new Line2D.Float(0,ch/2f,cw,ch/2f));
        g2.setPaint(Color.BLACK);
        g2.drawGlyphVector(gv, cw-xx, ch/2f-yy);
    }
    @Override public void actionPerformed(ActionEvent e) {
        cw = getWidth();
        ch = getHeight();
        xx = (cw+gv.getVisualBounds().getWidth()-xx > 0) ? xx+2f : 0f;
        repaint();
    }
}
