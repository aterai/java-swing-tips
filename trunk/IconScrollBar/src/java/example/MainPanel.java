package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JLabel l = new JLabel("aaaaaaaaaaaaaaaaaaaaaaaaaa");
        l.setPreferredSize(new Dimension(1000, 1000));
        JScrollPane scrollPane = new JScrollPane(l);
        if(scrollPane.getVerticalScrollBar().getUI() instanceof WindowsScrollBarUI) {
            scrollPane.getVerticalScrollBar().setUI(new WindowsScrollBarUI() {
                @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                    super.paintThumb(g,c,thumbBounds);
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
                    Color oc = null;
                    Color ic = null;
                    JScrollBar sb = (JScrollBar)c;
                    if(!sb.isEnabled() || thumbBounds.width>thumbBounds.height) {
                        return;
                    }else if(isDragging) {
                        oc = SystemColor.activeCaption.darker();
                        ic = SystemColor.inactiveCaptionText.darker();
                    }else if(isThumbRollover()) {
                        oc = SystemColor.activeCaption.brighter();
                        ic = SystemColor.inactiveCaptionText.brighter();
                    }else{
                        oc = SystemColor.activeCaption;
                        ic = SystemColor.inactiveCaptionText;
                    }
                    paintCircle(g2,thumbBounds,6,oc);
                    paintCircle(g2,thumbBounds,10,ic);
                }
                private void paintCircle(Graphics2D g2, Rectangle thumbBounds, int w, Color color) {
                    g2.setPaint(color);
                    int ww = thumbBounds.width-w;
                    g2.fillOval(thumbBounds.x+w/2,thumbBounds.y+(thumbBounds.height-ww)/2,ww,ww);
                }
            });
        }else{
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                    super.paintThumb(g,c,thumbBounds);
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
                    Color oc = null;
                    Color ic = null;
                    JScrollBar sb = (JScrollBar)c;
                    if(!sb.isEnabled() || thumbBounds.width>thumbBounds.height) {
                        return;
                    }else if(isDragging) {
                        oc = SystemColor.activeCaption.darker();
                        ic = SystemColor.inactiveCaptionText.darker();
                    }else if(isThumbRollover()) {
                        oc = SystemColor.activeCaption.brighter();
                        ic = SystemColor.inactiveCaptionText.brighter();
                    }else{
                        oc = SystemColor.activeCaption;
                        ic = SystemColor.inactiveCaptionText;
                    }
                    paintCircle(g2,thumbBounds,6,oc);
                    paintCircle(g2,thumbBounds,10,ic);
                }
                private void paintCircle(Graphics2D g2, Rectangle thumbBounds, int w, Color color) {
                    g2.setPaint(color);
                    int ww = thumbBounds.width-w;
                    g2.fillOval(thumbBounds.x+w/2,thumbBounds.y+(thumbBounds.height-ww)/2,ww,ww);
                }
            });
        }
        add(scrollPane);
        setPreferredSize(new Dimension(320, 200));
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
