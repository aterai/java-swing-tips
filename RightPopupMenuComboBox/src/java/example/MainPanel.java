package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class MainPanel extends JPanel {
    private final JComboBox combo00 = makeComboBox();
    private final JComboBox combo01 = makeComboBox();
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
    private void initComboBox(JComboBox combo) {
        if(combo.getUI() instanceof WindowsComboBoxUI) {
            combo.setUI(new WindowsComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton button = new JButton(icon) {
                        @Override public Dimension getPreferredSize() {
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
            combo.setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton button = super.createArrowButton();
                    ((BasicArrowButton)button).setDirection(SwingConstants.EAST);
                    return button;
                }
            });
        }
        combo.addPopupMenuListener(new RightPopupMenuListener());
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
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("asdfasdfas");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return new JComboBox(model);
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
class RightPopupMenuListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                JComboBox combo = (JComboBox)e.getSource();
                Accessible a = combo.getAccessibleContext().getAccessibleChild(0);
                //Or Accessible a = combo.getUI().getAccessibleChild(combo, 0);
                if(a instanceof BasicComboPopup) {
                    BasicComboPopup pop = (BasicComboPopup)a;
                    Point p = new Point(combo.getSize().width, 0);
                    SwingUtilities.convertPointToScreen(p, combo);
                    pop.setLocation(p);
                }
            }
        });
    }
    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
    @Override public void popupMenuCanceled(PopupMenuEvent e) {}
}
