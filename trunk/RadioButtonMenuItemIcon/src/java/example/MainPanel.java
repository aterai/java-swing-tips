package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.plaf.UIResource;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JTextArea());
        setPreferredSize(new Dimension(320, 200));
    }
    public JMenuBar createMenuBar() {
        JMenu menu = new JMenu("RadioButtonMenuItem-Test");

        JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem("default");
        rbmi.setSelected(true);
        menu.add(rbmi);

        UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon1());
        rbmi = new JRadioButtonMenuItem("ANTIALIASING");
        rbmi.setSelected(true);
        menu.add(rbmi);

        UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon2());
        rbmi = new JRadioButtonMenuItem("fillOval");
        rbmi.setSelected(true);
        menu.add(rbmi);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
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
        MainPanel p;
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p = new MainPanel());
        frame.setJMenuBar(p.createMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//com.sun.java.swing.plaf.windows.WindowsIconFactory.java
class RadioButtonMenuItemIcon1 implements Icon, UIResource, Serializable {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        if(model.isSelected()) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillRoundRect(x+3,y+3, getIconWidth()-6, getIconHeight()-6, 4, 4);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }
    @Override public int getIconWidth()  { return 12; }
    @Override public int getIconHeight() { return 12; }
}

class RadioButtonMenuItemIcon2 implements Icon, UIResource, Serializable {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        if(model.isSelected()) {
            //g.fillRoundRect(x+3,y+3, getIconWidth()-6, getIconHeight()-6, 4, 4);
            g.fillOval(x+2,y+2, getIconWidth()-5, getIconHeight()-5);
            //g.fillArc(x+2,y+2,getIconWidth()-5, getIconHeight()-5, 0, 360);
        }
    }
    @Override public int getIconWidth()  { return 12; }
    @Override public int getIconHeight() { return 12; }
}
