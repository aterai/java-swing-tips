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
        Box box = Box.createVerticalBox();
        box.add(makeTitledSeparator("TitledBorder", pbc, pbc.darker(), pbc.brighter(), 2, TitledBorder.DEFAULT_POSITION));
        box.add(new JCheckBox("JCheckBox 0"));
        box.add(new JCheckBox("JCheckBox 1"));
        box.add(Box.createVerticalStrut(10));

        Color c = new Color(100,180,200);
        box.add(makeTitledSeparator("TitledBorder ABOVE TOP", pbc, c.darker(), c.brighter(), 2, TitledBorder.ABOVE_TOP));
        box.add(new JCheckBox("JCheckBox 2"));
        box.add(new JCheckBox("JCheckBox 3"));
        box.add(Box.createVerticalStrut(10));

        box.add(new JSeparator());
        box.add(new JCheckBox("JCheckBox 4"));
        box.add(new JCheckBox("JCheckBox 5"));
        //box.add(Box.createVerticalStrut(8));

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledSeparator(String title, final Color color, final Color darker, final Color brighter, final int height, int titlePosition) {
        JLabel l = new JLabel() {
            @Override public Dimension getMaximumSize() {
                Dimension d = super.getPreferredSize();
                d.width = Short.MAX_VALUE;
                return d;
            }
        };
        l.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(height, 0, 0, 0, new Icon() {
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
        }), title, TitledBorder.DEFAULT_JUSTIFICATION, titlePosition));
        return l;
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
