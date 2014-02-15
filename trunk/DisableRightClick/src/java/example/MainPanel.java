package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
//         Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
//             @Override public void eventDispatched(AWTEvent event) {
//                 if(event instanceof MouseWheelEvent) {
//                     Object source = event.getSource();
//                     if(source instanceof JScrollPane) {
//                         System.out.println("JScrollPane");
//                         return;
//                     }
//                     ((MouseWheelEvent)event).consume();
//                 }
//             }
//         }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);

        JComboBox<String> combo1 = makeComboBox(5);
        if(combo1.getUI() instanceof WindowsComboBoxUI) {
            combo1.setUI(new WindowsComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup2(comboBox);
                }
            });
        }else{
            combo1.setUI(new BasicComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup2(comboBox);
                }
            });
        }

        JComboBox<String> combo2 = makeComboBox(5);
        if(combo2.getUI() instanceof WindowsComboBoxUI) {
            combo2.setUI(new WindowsComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup3(comboBox);
                }
            });
        }else{
            combo2.setUI(new BasicComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new BasicComboPopup3(comboBox);
                }
            });
        }

        Box box = Box.createVerticalBox();
        box.add(createPanel(makeComboBox(5), "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(makeComboBox(20), "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo1, "disable right click in drop-down list:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo2, "disable right click and scroll in drop-down list:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
    }
    private static JComboBox<String> makeComboBox(int size) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for(int i=0;i<size;i++) {
            model.addElement("No."+i);
        }
        return new JComboBox<String>(model);
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

class BasicComboPopup2 extends BasicComboPopup {
    private transient Handler2 handler2;
    @Override public void uninstallingUI() {
        super.uninstallingUI();
        handler2 = null;
    }
    public BasicComboPopup2(JComboBox combo) {
        super(combo);
    }
    @Override protected MouseListener createListMouseListener() {
        if(handler2==null) {
            handler2 = new Handler2();
        }
        return handler2;
    }
    private class Handler2 extends MouseAdapter {
        @Override public void mouseReleased(MouseEvent e) {
            if(e.getSource().equals(list)) {
                if(list.getModel().getSize() > 0) {
                    // <ins>
                    if(!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
                        return;
                    }
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
    @Override protected JScrollPane createScroller() {
        JScrollPane sp = new JScrollPane(list,
                                         ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
            @Override protected void processEvent(AWTEvent e) {
                if(e instanceof MouseWheelEvent) {
                    JScrollBar toScroll = getVerticalScrollBar();
                    if(toScroll == null || !toScroll.isVisible()) {
                        ((MouseWheelEvent)e).consume();
                        return;
                    }
                }
                super.processEvent(e);
            }
        };
        sp.setHorizontalScrollBar(null);
        return sp;
    }
    @SuppressWarnings("unchecked")
    @Override protected JList createList() {
        return new JList(comboBox.getModel()) {
            @Override public void processMouseEvent(MouseEvent e)  {
                if(SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                MouseEvent ev = e;
                if(e.isControlDown())  {
                    // Fix for 4234053. Filter out the Control Key from the list.
                    // ie., don't allow CTRL key deselection.
                    ev = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(),
                                        e.getModifiers() ^ InputEvent.CTRL_MASK,
                                        e.getX(), e.getY(),
                                        e.getXOnScreen(), e.getYOnScreen(),
                                        e.getClickCount(),
                                        e.isPopupTrigger(),
                                        MouseEvent.NOBUTTON);
                }
                super.processMouseEvent(ev);
            }
        };
    }
}
