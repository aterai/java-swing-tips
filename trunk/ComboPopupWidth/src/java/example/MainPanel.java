package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel{
    private final JComboBox combo00 = makeComboBox();
    private final JComboBox combo01 = makeComboBox();
    private final JComboBox combo02 = makeComboBox();
    private final JComboBox combo03 = makeComboBox();
    public MainPanel() {
        super(new BorderLayout());
        combo00.setEditable(false);
        combo01.setEditable(true);
        combo02.setEditable(false);
        combo03.setEditable(true);
        combo02.addPopupMenuListener(new WidePopupMenuListener());
        combo03.addPopupMenuListener(new WidePopupMenuListener());

        int g = 5;
        JPanel p = new JPanel(new GridLayout(4,2,g,g));
        p.add(combo00); p.add(new JLabel("<- nomal"));
        p.add(combo01); p.add(new JLabel("<- nomal, editable"));
        p.add(combo02); p.add(new JLabel("<- wide"));
        p.add(combo03); p.add(new JLabel("<- wide, editable"));
        setBorder(BorderFactory.createEmptyBorder(g,g,g,g));
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasd");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return new JComboBox(model);
    }
    //Swing - How to widen the drop-down list in a JComboBox
    //https://forums.oracle.com/forums/thread.jspa?threadID=1366300
    private static class WidePopupMenuListener implements PopupMenuListener{
        private static final int POPUP_MIN_WIDTH = 300;
        private boolean adjusting = false;
        @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox combo = (JComboBox)e.getSource();
            Dimension size  = combo.getSize();
            if(size.width>=POPUP_MIN_WIDTH) return;
            if(!adjusting) {
                adjusting = true;
                combo.setSize(POPUP_MIN_WIDTH, size.height);
                combo.showPopup();
            }
            combo.setSize(size);
            adjusting = false;
        }
        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
        @Override public void popupMenuCanceled(PopupMenuEvent e) {}
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
