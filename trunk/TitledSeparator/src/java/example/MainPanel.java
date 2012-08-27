package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        Color pbc = UIManager.getColor("Panel.background");
        Box box1 = Box.createVerticalBox();
        box1.setBorder(makeTitledSeparator("TitledBorder", pbc, pbc.darker(), pbc.brighter(), 2, TitledBorder.DEFAULT_POSITION));
        box1.add(new JCheckBox("JCheckBox 0"));
        box1.add(new JCheckBox("JCheckBox 1"));

        Color c = new Color(100,180,200);
        Box box2 = Box.createVerticalBox();
        box2.setBorder(makeTitledSeparator("TitledBorder ABOVE TOP", pbc, c.darker(), c.brighter(), 2, TitledBorder.ABOVE_TOP));
        box2.add(new JCheckBox("JCheckBox 2"));
        box2.add(new JCheckBox("JCheckBox 3"));

        Box box3 = Box.createVerticalBox();
        box3.add(new JSeparator());
        box3.add(new JCheckBox("JCheckBox 4"));
        box3.add(new JCheckBox("JCheckBox 5"));

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(box1);
        p.add(box2);
        p.add(box3);

        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Border makeTitledSeparator(String title, final Color color, final Color darker, final Color brighter, final int height, int titlePosition) {
        return BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(height, 0, 0, 0, new Icon() {
            private int width = -1;
            private Paint painter1, painter2;
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                int w = c.getWidth();
                if(w!=width || painter1==null || painter2==null) {
                    width = w;
                    Point2D start  = new Point2D.Float(0f, 0f);
                    Point2D end    = new Point2D.Float((float)width, 0f);
                    float[] dist   = {0.0f, 1.0f};
                    painter1 = new LinearGradientPaint(start, end, dist, new Color[] {darker,   color});
                    painter2 = new LinearGradientPaint(start, end, dist, new Color[] {brighter, color});
                }
                int h = getIconHeight()/2;
                Graphics2D g2  = (Graphics2D)g.create();
                g2.setPaint(painter1);
                g2.fillRect(x, y,   width, getIconHeight());
                g2.setPaint(painter2);
                g2.fillRect(x, y+h, width, getIconHeight()-h);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return 200; } //dummy width
            @Override public int getIconHeight() { return height; }
        }), title, TitledBorder.DEFAULT_JUSTIFICATION, titlePosition);
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
