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
        super(new GridLayout(2,1));
        add(new TextLayoutPanel());
        add(new GlyphVectorPanel());
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

class TextLayoutPanel extends JComponent {
    String text = "abcdefthijklmnopqrstuvwxyz";
    Font font = new Font("serif", Font.ITALIC, 64);
    FontRenderContext frc = new FontRenderContext(null,true,true);
    TextLayout tl = new TextLayout(text, font, frc);
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int w = getWidth();
        float baseline = getHeight()/2f;

        g2.setPaint(Color.RED);
        g2.draw(new Line2D.Float(0, baseline, w, baseline));

        g2.setPaint(Color.GREEN);
        float ascent = baseline - tl.getAscent();
        g2.draw(new Line2D.Float(0, ascent, w, ascent));

        g2.setPaint(Color.BLUE);
        float descent = baseline + tl.getDescent();
        g2.draw(new Line2D.Float(0, descent, w, descent));

        g2.setPaint(Color.ORANGE);
        float leading = baseline + tl.getDescent() + tl.getLeading();
        g2.draw(new Line2D.Float(0, leading, w, leading));

        g2.setPaint(Color.CYAN);
        float xheight = baseline - (float)tl.getBlackBoxBounds(23, 24).getBounds().getHeight();
        g2.draw(new Line2D.Float(0, xheight, w, xheight));

        g2.setPaint(Color.BLACK);
        tl.draw(g2, 0f, baseline);
    }
}

class GlyphVectorPanel extends JComponent {
    String text = "abcdefthijklmnopqrstuvwxyz";
    Font font = new Font("serif", Font.ITALIC, 64);
    FontRenderContext frc = new FontRenderContext(null,true,true);
    GlyphVector gv = font.createGlyphVector(frc, text);
    LineMetrics lm = font.getLineMetrics(text, frc);

    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int w = getWidth();
        float baseline = getHeight()/2f;

        g2.setPaint(Color.RED);
        g2.draw(new Line2D.Float(0, baseline, w, baseline));

        g2.setPaint(Color.GREEN);
        float ascent = baseline - lm.getAscent();
        g2.draw(new Line2D.Float(0, ascent, w, ascent));

        g2.setPaint(Color.BLUE);
        float descent = baseline + lm.getDescent();
        g2.draw(new Line2D.Float(0, descent, w, descent));

        g2.setPaint(Color.ORANGE);
        float leading = baseline + lm.getDescent() + lm.getLeading();
        g2.draw(new Line2D.Float(0, leading, w, leading));

        g2.setPaint(Color.CYAN);
        float xheight = baseline - (float)gv.getGlyphMetrics(23).getBounds2D().getHeight();
        g2.draw(new Line2D.Float(0, xheight, w, xheight));

        g2.setPaint(Color.BLACK);
        g2.drawGlyphVector(gv, 0f, baseline);
    }
}
