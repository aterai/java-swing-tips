package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final int DELAY = 10 * 1000; // 10s
    private final JLabel label = new JLabel("Not connected");
    private final JComboBox<String> combo = makeComboBox();
    private final JTextField textField = new JTextField(20);
    private final JButton button = new JButton("Connect");

    public MainPanel() {
        super(new BorderLayout());
        // EventQueue eventQueue = new EventQueue() {
        //     @Override protected void dispatchEvent(AWTEvent e) {
        //         super.dispatchEvent(e);
        //         if (e instanceof InputEvent && Objects.nonNull(timer) && timer.isRunning()) {
        //             timer.restart();
        //         }
        //     }
        // };
        Timer timer = new Timer(DELAY, null);
        AWTEventListener awtEvent = e -> {
            if (timer.isRunning()) {
                System.out.println("timer.restart()");
                timer.restart();
            }
        };
        timer.addActionListener(e -> {
            System.out.println("timeout");
            setTestConnected(false);
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEvent);
            ((Timer) e.getSource()).stop();
        });

        button.addActionListener(e -> {
            setTestConnected(true);
            Toolkit.getDefaultToolkit().addAWTEventListener(awtEvent, AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
            // Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
            timer.setRepeats(false);
            timer.start();
        });
        setTestConnected(false);

        JPanel p = new JPanel(new BorderLayout());
        p.add(label);
        p.add(button, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Dummy"));
        JPanel box = new JPanel(new BorderLayout(5, 5));
        box.add(textField);
        box.add(combo, BorderLayout.EAST);
        panel.add(box, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTextArea()));

        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel);
        setPreferredSize(new Dimension(320, 240));
    }
    protected final void setTestConnected(boolean flag) {
        String str = flag ? "<font color='blue'>Connected" : "<font color='red'>Not connected";
        label.setText("<html>Status: " + str);
        combo.setEnabled(flag);
        textField.setEnabled(flag);
        button.setEnabled(!flag);
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("dummy model");
        model.addElement("qerqwerew");
        model.addElement("zcxvzxcv");
        model.addElement("41234123");
        return new JComboBox<>(model);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
