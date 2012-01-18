package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class MainPanel extends JPanel{
    private final JComboBox combo01 = new JComboBox(makeModel());
    private final JComboBox combo02 = new JComboBox(makeModel());
    public MainPanel() {
        super(new BorderLayout());

        if(combo02.getUI() instanceof WindowsComboBoxUI) {
            combo02.setUI(new WindowsComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup2( comboBox );
                    //return new BasicComboPopup3( comboBox );
                }
            });
        }else{
            combo02.setUI(new BasicComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup2( comboBox );
                    //return new BasicComboPopup3( comboBox );
                }
            });
        }

        Box box = Box.createVerticalBox();
        box.add(createPanel(combo01, "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo02, "disable right click in drop-down list:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1354123451234513512");
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

class BasicComboPopup2 extends BasicComboPopup {
    private Handler2 handler2;
    @Override public void uninstallingUI() {
        super.uninstallingUI();
        handler2 = null;
    }
    public BasicComboPopup2(JComboBox combo) {
        super(combo);
    }
    @Override protected MouseListener createListMouseListener() {
        if(handler2==null) handler2 = new Handler2();
        return handler2;
    }
    private class Handler2 implements MouseListener{
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e)  {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {
            if(e.getSource() == list) {
                if(list.getModel().getSize() > 0) {
                    // <ins>
                    if(!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) return;
                    // </ins>
                    // JList mouse listener
                    if(comboBox.getSelectedIndex() == list.getSelectedIndex()) {
                        comboBox.getEditor().setItem(list.getSelectedValue());
                    }
                    comboBox.setSelectedIndex(list.getSelectedIndex());
                }
                comboBox.setPopupVisible(false);
                // workaround for cancelling an edited item (bug 4530953)
                if(comboBox.isEditable() && comboBox.getEditor() != null) {
                    comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
                }
            }
        }
    }
}

class BasicComboPopup3 extends BasicComboPopup {
    public BasicComboPopup3(JComboBox combo) {
        super(combo);
    }
    @Override protected JList createList() {
        return new JList( comboBox.getModel() ) {
            @Override public void processMouseEvent(MouseEvent e)  {
                if(SwingUtilities.isRightMouseButton(e)) return;
                if (e.isControlDown())  {
                    // Fix for 4234053. Filter out the Control Key from the list.
                    // ie., don't allow CTRL key deselection.
                    e = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(),
                                       e.getModifiers() ^ InputEvent.CTRL_MASK,
                                       e.getX(), e.getY(),
                                       e.getXOnScreen(), e.getYOnScreen(),
                                       e.getClickCount(),
                                       e.isPopupTrigger(),
                                       MouseEvent.NOBUTTON);
                }
                super.processMouseEvent(e);
            }
        };
    }
}
