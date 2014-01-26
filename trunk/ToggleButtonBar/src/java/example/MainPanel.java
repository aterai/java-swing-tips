package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super();

        add(makeToggleButtonBar(0xff7400, true));
        add(makeToggleButtonBar(0x555555, false));
        add(makeToggleButtonBar(0x006400, true));
        add(makeToggleButtonBar(0x8b0000, false));
        add(makeToggleButtonBar(0x001e43, true));

        setPreferredSize(new Dimension(320, 240));
    }
    private static JRadioButton makeRadioButton(String title) {
        JRadioButton radio = new JRadioButton(title);
        radio.setVerticalAlignment(SwingConstants.CENTER);
        radio.setVerticalTextPosition(SwingConstants.CENTER);
        radio.setHorizontalAlignment(SwingConstants.CENTER);
        radio.setHorizontalTextPosition(SwingConstants.CENTER);
        radio.setBorder(BorderFactory.createEmptyBorder());
        radio.setContentAreaFilled(false);
        radio.setFocusPainted(false);
        //radio.setBackground(new Color(cc));
        radio.setForeground(Color.WHITE);
        return radio;
    }
    private static JPanel makeToggleButtonBar(int cc, boolean round) {
        List<JRadioButton> list = Arrays.asList(
            makeRadioButton("left"),
            makeRadioButton("center"),
            makeRadioButton("right"));
        int size = list.size();
        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel(new GridLayout(1, size, 0, 0));
        Color color = new Color(cc);
        for(int i=0; i<size;i++) {
            JRadioButton r = list.get(i);
            r.setBackground(color);
            if(round) {
                r.setIcon(new ToggleButtonBarCellIcon());
            }else{
                r.setIcon(new CellIcon());
            }
            bg.add(r);
            p.add(r);
        }
        p.setBorder(BorderFactory.createTitledBorder(String.format("Color: #%06x", cc)));
        return p;
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class CellIcon implements Icon {
    //http://weboook.blog22.fc2.com/blog-entry-342.html
    //Webpark 2012.11.15
    private static final Color TL = new Color(1f,1f,1f,.2f);
    private static final Color BR = new Color(0f,0f,0f,.2f);
    private static final Color ST = new Color(1f,1f,1f,.4f);
    private static final Color SB = new Color(1f,1f,1f,.1f);

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int w = c.getWidth();
        int h = c.getHeight();
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(c.getBackground());
        g2.fillRect(x, y, w, h);

        Color ssc = TL;
        Color bgc = BR;
        if(c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton)c).getModel();
            if(m.isSelected() || m.isRollover()) {
                ssc = ST;
                bgc = SB;
            }
        }
        g2.setPaint(new GradientPaint(x, y, ssc, x, y+h, bgc, true));
        g2.fillRect(x, y, w, h);

        g2.setPaint(TL);
        g2.fillRect(x, y, 1, h);
        g2.setPaint(BR);
        g2.fillRect(x+w, y, 1, h);

        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return 80;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}

class ToggleButtonBarCellIcon implements Icon {
    private static final Color TL = new Color(1f,1f,1f,.2f);
    private static final Color BR = new Color(0f,0f,0f,.2f);
    private static final Color ST = new Color(1f,1f,1f,.4f);
    private static final Color SB = new Color(1f,1f,1f,.1f);

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int r = 8;
        int w = c.getWidth();
        int h = c.getHeight();
        Container parent = c.getParent();
        if(parent==null) {
            return;
        }

        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Float p = new Path2D.Float();

        if(c==parent.getComponent(0)) {
            //:first-child
            p.moveTo(x, y + r);
            p.quadTo(x, y, x + r, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x + r, y + h);
            p.quadTo(x, y + h, x, y + h - r);
        }else if(c==parent.getComponent(parent.getComponentCount()-1)) {
            //:last-child
            p.moveTo(x, y);
            p.lineTo(x + w - r, y);
            p.quadTo(x + w, y, x + w, y + r);
            p.lineTo(x + w, y + h - r);
            p.quadTo(x + w, y + h, x + w -r, y + h);
            p.lineTo(x, y + h);
        }else{
            p.moveTo(x, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x, y + h);
        }
        p.closePath();
        Area area = new Area(p);

        g2.setPaint(c.getBackground());
        g2.fill(area);

        Color ssc = TL;
        Color bgc = BR;
        if(c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton)c).getModel();
            if(m.isSelected() || m.isRollover()) {
                ssc = ST;
                bgc = SB;
            }
        }
        g2.setPaint(new GradientPaint(x, y, ssc, x, y+h, bgc, true));
        g2.fill(area);

        g2.setPaint(BR);
        g2.draw(area);

        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return 80;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}
