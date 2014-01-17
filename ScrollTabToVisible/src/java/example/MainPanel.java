package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("setSelectedIndex");
    public MainPanel() {
        super(new BorderLayout());
        final JTabbedPane jtp = new JTabbedPane();
        jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        for(int i=0;i<100;i++) {
            jtp.addTab("title"+i, new JLabel("label"+i));
        }
        JSlider slider = new JSlider(0,jtp.getTabCount()-1,50);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int i = ((JSlider)e.getSource()).getValue();
                if(check.isSelected()) {
                    jtp.setSelectedIndex(i);
                }
                scrollTabAt(jtp, i);
            }
        });
        check.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Scroll Slider"));
        p.add(check, BorderLayout.SOUTH);
        p.add(slider, BorderLayout.NORTH);
        add(p, BorderLayout.NORTH);
        add(jtp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void scrollTabAt(JTabbedPane tp, int index) {
        JViewport vp = null;
        for(Component c:tp.getComponents()) {
            if("TabbedPane.scrollableViewport".equals(c.getName())) {
                vp = (JViewport)c; break;
            }
        }
        if(vp==null) {
            return;
        }
        final JViewport viewport = vp;
        for(int i=0;i<tp.getTabCount();i++) {
            tp.setForegroundAt(i, i==index?Color.RED:Color.BLACK);
        }
        Dimension d = tp.getSize();
        Rectangle r = tp.getBoundsAt(index);
        int gw = (d.width-r.width)/2;
        r.grow(gw, 0);
        viewport.scrollRectToVisible(r);
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
