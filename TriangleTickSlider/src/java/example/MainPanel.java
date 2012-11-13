package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout(5,5));
        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("Default", makeSlider(false)));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("Triangle Tick", makeSlider(true)));
        box.add(Box.createVerticalGlue());
        add(box);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
    }

    private JSlider makeSlider(boolean icon) {
        JSlider slider = new JSlider(0,100);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        if(icon) {
            Dictionary dictionary = slider.getLabelTable();
            if(dictionary != null) {
                Enumeration elements = dictionary.elements();
                Icon tick = new TickIcon();
                while(elements.hasMoreElements()) {
                    JLabel label = (JLabel) elements.nextElement();
                    label.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
                    label.setIcon(tick);
                    label.setIconTextGap(0);
                    label.setVerticalAlignment(SwingConstants.TOP);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    label.setForeground(Color.RED);
                }
            }
        }else{
            slider.setPaintTicks(true);
            slider.setForeground(Color.BLUE);
        }
        return slider;
    }

    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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

class TickIcon implements Icon {
    @Override public void paintIcon(Component c,Graphics g,int x,int y) {
        g.setColor(Color.BLUE);
        g.drawLine( x+2, y+0, x+2, y+2 );
        g.drawLine( x+1, y+1, x+3, y+1 );
        g.drawLine( x+0, y+2, x+4, y+2 );
    }
    @Override public int getIconWidth()  { return 5; }
    @Override public int getIconHeight() { return 3; }
}
