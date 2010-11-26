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
    private static final String TEXT = "あいうえお かきくけこ さしすせそ たちつてと なにぬねの はひふへほ まみむめも";
    private final JLabel    lbl1 = new JLabel(TEXT);
    private final JLabel    lbl2 = new WrappedLabel(TEXT);
    private final JTextArea lbl3 = new JTextArea(TEXT);
    private GlyphVector gvtext;
    private boolean flg = true;
    public MainPanel() {
        super(new GridLayout(3,1));
        lbl1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.YELLOW, 5), "JLabel"));
        lbl2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN,  5), "GlyphVector"));
        lbl3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 5), "JTextArea"));

        //lbl2.setFont(new Font("serif", Font.TRUETYPE_FONT, 20));
        lbl3.setFont(lbl1.getFont());
        lbl3.setEditable(false);
        lbl3.setLineWrap(true);
        lbl3.setBackground(lbl1.getBackground());

        add(lbl1);
        add(lbl2);
        add(lbl3);
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

class WrappedLabel extends JLabel {
    private GlyphVector gvtext;
    private boolean flg = true;
    public WrappedLabel() {
        this(null);
    }
    public WrappedLabel(String str) {
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
            g2.setPaint(Color.RED);
            g2.drawGlyphVector(gvtext, getInsets().left, getFont().getSize()+getInsets().top);
        }else{
            super.paintComponent(g);
        }
    }
    private GlyphVector getWrappedGlyphVector(String str, float wrapping, Font font, FontRenderContext frc) {
        Point2D gmPos    = new Point2D.Double(0.0d, 0.0d);
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float lineheight = (float) (gv.getLogicalBounds().getHeight());
        float xpos       = 0.0f;
        float advance    = 0.0f;
        int   lineCount  = 0;
        GlyphMetrics gm;
        for(int i=0;i<gv.getNumGlyphs();i++) {
            gm = gv.getGlyphMetrics(i);
            advance = gm.getAdvance();
            if(xpos<wrapping && wrapping<=xpos+advance) {
                lineCount++;
                xpos = 0.0f;
            }
            gmPos.setLocation(xpos, lineheight*lineCount);
            gv.setGlyphPosition(i, gmPos);
            xpos = xpos + advance;
        }
        return gv;
    }
}
