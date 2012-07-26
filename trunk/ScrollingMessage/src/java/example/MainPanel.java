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
    public final Timer animator;
    private final GlyphVector gv;
    private final LineMetrics lm;
    private float xx, baseline, xheight;

    public MarqueePanel() {
        super();
        animator = new Timer(10, this);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                JComponent c = (JComponent)e.getSource();
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 &&
                   animator!=null && !c.isDisplayable()) {
                    animator.stop();
                }
            }
        });

        String text = "abcdefthijklmnopqrstuvwxyz";
        Font font = new Font("serif", Font.PLAIN, 100);
        FontRenderContext frc = new FontRenderContext(null,true,true);

        gv = font.createGlyphVector(frc, text);
        lm = font.getLineMetrics(text, frc);

        GlyphMetrics xgm = gv.getGlyphMetrics(23);
        xheight = (float)xgm.getBounds2D().getHeight();
        animator.start();
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();

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
        float xh = baseline - xheight;
        g2.draw(new Line2D.Float(0, xh, w, xh));

        g2.setPaint(Color.BLACK);
        g2.drawGlyphVector(gv, w - xx, baseline);
    }
    @Override public void actionPerformed(ActionEvent e) {
        xx = getWidth()+gv.getVisualBounds().getWidth()-xx > 0 ? xx+2f : 0f;
        baseline = getHeight()/2f;
        repaint();
    }
}
