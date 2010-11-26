package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JLabel l0 = new JLabel("打率");
    private final JLabel l1 = new JLabel("打率", JLabel.RIGHT);
    private final JLabel l2 = new JustifiedLabel("打率");
    private final JLabel l3 = new JLabel("出塁率", JLabel.CENTER);
    private final JLabel l4 = new JustifiedLabel("出塁率");
    private final JLabel l5 = new JustifiedLabel("チーム出塁率");
    public MainPanel() {
        super(new BorderLayout());
        //l2.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
//         l0.setPreferredSize(new Dimension(80, 18));
//         l1.setPreferredSize(new Dimension(80, 18));
//         l2.setPreferredSize(new Dimension(80, 18));
//         l3.setPreferredSize(new Dimension(80, 18));
//         l4.setPreferredSize(new Dimension(80, 18));
//         l5.setPreferredSize(new Dimension(80, 18));

        JPanel p = new JPanel(new GridBagLayout());
        Border inside  = BorderFactory.createEmptyBorder(10,5+2,10,10+2);
        Border outside = BorderFactory.createTitledBorder("JLabel text-align:justify");
        p.setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p.add(l0, c);
        c.gridy   = 1; p.add(l1, c);
        c.gridy   = 2; p.add(l2, c);
        c.gridy   = 3; p.add(l3, c);
        c.gridy   = 4; p.add(l4, c);
        c.gridy   = 5; p.add(l5, c);

        c.gridx   = 1;
        c.weightx = 1.0;
        c.gridy   = 0; p.add(new JTextField(), c);
        c.gridy   = 1; p.add(new JTextField(), c);
        c.gridy   = 2; p.add(new JTextField(), c);
        c.gridy   = 3; p.add(new JTextField(), c);
        c.gridy   = 4; p.add(new JTextField(), c);
        c.gridy   = 5; p.add(new JTextField(), c);

        add(p);
        add(new JustifiedLabel("ちとしはちとしはちとし"), BorderLayout.SOUTH);
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

class JustifiedLabel extends JLabel {
    private GlyphVector gvtext;
    private boolean flg = true;
    public JustifiedLabel() {
        this(null);
    }
    public JustifiedLabel(String str) {
        super(str);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                flg = true;
                repaint();
            }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        if(flg) {
            int WRAPPING_WIDTH = getWidth()-getInsets().left-getInsets().right;
            FontRenderContext frc = g2.getFontRenderContext();
            gvtext = getWrappedGlyphVector(getText(), WRAPPING_WIDTH, getFont(), frc);
            flg    = false;
        }
        if(gvtext!=null) {
            //g2.setPaint(Color.RED);
            g2.drawGlyphVector(gvtext,
                               getInsets().left,
                               getHeight()/2 + getFont().getSize()/2);
        }else{
            super.paintComponent(g);
        }
    }
    private GlyphVector getWrappedGlyphVector(String str, float wrapping, Font font, FontRenderContext frc) {
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float ga = 0.0f;
        for(int i=0;i<gv.getNumGlyphs();i++) {
            ga = ga + gv.getGlyphMetrics(i).getAdvance();
        }
        if(wrapping<ga) return null;

        float xx = (wrapping-ga) / (float)(gv.getNumGlyphs()-1);
        float xpos = 0.0f;
        Point2D gmPos = new Point2D.Double(0.0d, 0.0d);
        for(int i=0;i<gv.getNumGlyphs();i++) {
            GlyphMetrics gm = gv.getGlyphMetrics(i);
            gmPos.setLocation(xpos, 0);
            gv.setGlyphPosition(i, gmPos);
            xpos = xpos + gm.getAdvance() + xx;
        }
        return gv;
    }
}
