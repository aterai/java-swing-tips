package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        Dictionary<Integer, Component> labelTable = new Hashtable<>();
        int c = 0;
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        for(String s:Arrays.asList("wi0009-16.png", "wi0054-16.png", "wi0062-16.png",
                                   "wi0063-16.png", "wi0064-16.png", "wi0096-16.png",
                                   "wi0111-16.png", "wi0122-16.png", "wi0124-16.png",
                                   "wi0126-16.png")) {
            labelTable.put(c++, new JLabel(s, new ImageIcon(getClass().getResource(s)), SwingConstants.RIGHT));
        }
        labelTable.put(c, new JButton("aaa"));
        JSlider slider1 = new JSlider(JSlider.VERTICAL,0,10,0);
        slider1.setSnapToTicks(true);
        //slider1.setMajorTickSpacing(1);
        //slider1.setMinorTickSpacing(5);
        slider1.setPaintTicks(true);
        slider1.setLabelTable(labelTable);
        slider1.setPaintLabels(true);

        Dictionary<Integer, Component> labelTable2 = new Hashtable<Integer, Component>();
        int i=0;
        for(String s:Arrays.asList("零","壱","弐","参","肆","伍","陸","漆","捌","玖","拾")) {
            JLabel l = new JLabel(s);
            l.setForeground(new Color(250,100-i*10,10));
            labelTable2.put(i++, l);
        }
        JSlider slider2 = new JSlider(0,10,0);
        //slider2.setForeground(Color.BLUE);
        slider2.setSnapToTicks(true);
        slider2.setLabelTable(labelTable2);
        slider2.setPaintTicks(true);
        slider2.setPaintLabels(true);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(20,20,20,0));
        box.add(new JSlider(0,100,100));
        box.add(Box.createVerticalStrut(20));
        box.add(new JSlider());
        box.add(Box.createVerticalStrut(20));
        box.add(slider2);
        box.add(Box.createHorizontalGlue());

        add(slider1, BorderLayout.WEST);
        add(box);
        setBorder(BorderFactory.createEmptyBorder(5,20,5,10));
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
