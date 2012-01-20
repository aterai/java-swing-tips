package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class MainPanel extends JPanel{
    private final JComboBox combo00 = new JComboBox(makeModel());
    private final JComboBox combo01 = new JComboBox(makeModel());
    public MainPanel() {
        super(new BorderLayout());
        initComboBox(combo01);
        int g = 5;
        JPanel p = new JPanel(new GridLayout(2,2,g,g));
        p.add(combo00); p.add(new JLabel("<- default"));
        p.add(combo01); p.add(new JLabel("<- RightPopupMenuListener"));
        setBorder(BorderFactory.createEmptyBorder(g,g,g,g));
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private final ImageIcon icon = new ImageIcon(getClass().getResource("14x14.png"));
    private void initComboBox(JComboBox combo2) {
        if(combo2.getUI() instanceof WindowsComboBoxUI) {
            combo2.setUI(new WindowsComboBoxUI() {
                protected JButton createArrowButton() {
                    JButton button = new JButton(icon) {
                        public Dimension getPreferredSize() {
                            return new Dimension(14, 14);
                        }
                    };
                    button.setRolloverIcon(makeRolloverIcon(icon));
                    button.setFocusPainted(false);
                    button.setContentAreaFilled(false);
                    return button;
                }
            });
        }else{
            combo2.setUI(new BasicComboBoxUI() {
                protected JButton createArrowButton() {
                    JButton button = super.createArrowButton();
                    ((BasicArrowButton)button).setDirection(SwingConstants.EAST);
                    return button;
                }
            });
        }
        combo2.addPopupMenuListener(new RightPopupMenuListener());
    }
    private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] { 1.2f,1.2f,1.2f,1.0f },
            new float[] { 0f,0f,0f,0f }, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        //g.drawImage(srcIcon.getImage(), 0, 0, null);
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("asdfasdfas");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return model;
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
class RightPopupMenuListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JComboBox combo = (JComboBox)e.getSource();
        Accessible a = combo.getUI().getAccessibleChild(combo, 0);
        if(a instanceof BasicComboPopup) {
            BasicComboPopup pop = (BasicComboPopup)a;
            Point p = new Point(combo.getSize().width, 0);
            SwingUtilities.convertPointToScreen(p, combo);
            pop.setLocation(p);
        }
    }
    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
    @Override public void popupMenuCanceled(PopupMenuEvent e) {}
}
