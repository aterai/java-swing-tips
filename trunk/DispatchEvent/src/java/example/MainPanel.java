package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final int DELAY = 10*1000; //10s
    private final JLabel label = new JLabel("Not connected");
    private final JComboBox combo = makeComboBox();
    private final JTextField textField  = new JTextField(20);
    private final JButton button;
    private final Timer timer;

    public MainPanel() {
        super(new BorderLayout());
//         final EventQueue eventQueue = new EventQueue() {
//             @Override protected void dispatchEvent(AWTEvent e) {
//                 super.dispatchEvent(e);
//                 if(e instanceof InputEvent) {
//                     if(timer!=null && timer.isRunning()) timer.restart();
//                 }
//             }
//         };
        final AWTEventListener awtEvent = new AWTEventListener() {
            @Override public void eventDispatched(AWTEvent e) {
                if(timer.isRunning()) {
                    System.out.println("timer.restart()");
                    timer.restart();
                }
            }
        };
        timer = new Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("timeout");
                setTestConnected(false);
                Toolkit.getDefaultToolkit().removeAWTEventListener(awtEvent);
                timer.stop();
            }
        });
        button = new JButton(new AbstractAction("Connect") {
            @Override public void actionPerformed(ActionEvent e) {
                setTestConnected(true);
                Toolkit.getDefaultToolkit().addAWTEventListener(awtEvent, AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
                //Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
                timer.setRepeats(false);
                timer.start();
            }
        });
        setTestConnected(false);

        JPanel p = new JPanel(new BorderLayout());
        p.add(label);
        p.add(button, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBorder(BorderFactory.createTitledBorder("Dummy"));
        JPanel box = new JPanel(new BorderLayout(5,5));
        box.add(textField);
        box.add(combo, BorderLayout.EAST);
        panel.add(box, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTextArea()));

        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(panel);
        setPreferredSize(new Dimension(320, 180));
    }
    private void setTestConnected(boolean flag) {
        String str = flag ? "<font color='blue'>Connected" : "<font color='red'>Not connected";
        label.setText("<html>Status: "+str);
        combo.setEnabled(flag);
        textField.setEnabled(flag);
        button.setEnabled(!flag);
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("dummy model");
        model.addElement("qerqwerew");
        model.addElement("zcxvzxcv");
        model.addElement("41234123");
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
