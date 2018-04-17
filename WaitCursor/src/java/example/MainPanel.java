package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        EventQueue.invokeLater(() -> {
            Component gp = new LockingGlassPane();
            gp.setVisible(false);
            getRootPane().setGlassPane(gp);
        });

        JButton button = new JButton("Stop 5sec");
        button.addActionListener(e -> {
            // System.out.println("actionPerformed: " + EventQueue.isDispatchThread());
            getRootPane().getGlassPane().setVisible(true);
            Component c = (Component) e.getSource();
            c.setEnabled(false);
            (new BackgroundTask() {
                @Override public void done() {
                    if (!isDisplayable()) {
                        System.out.println("done: DISPOSE_ON_CLOSE");
                        cancel(true);
                        return;
                    }
                    getRootPane().getGlassPane().setVisible(false);
                    c.setEnabled(true);
                }
            }).execute();
        });
        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton b = new JButton("Button&Mnemonic");
        b.setMnemonic(KeyEvent.VK_B);

        JTextField t = new JTextField("TextField&ToolTip");
        t.setToolTipText("ToolTip");

        box.add(b);
        box.add(Box.createHorizontalStrut(5));
        box.add(t);
        box.add(Box.createHorizontalStrut(5));

        add(box, BorderLayout.NORTH);
        add(button, BorderLayout.SOUTH);
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
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
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class BackgroundTask extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return "Done";
    }
}

class LockingGlassPane extends JPanel {
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    @Override public void setVisible(boolean isVisible) {
        boolean oldVisible = isVisible();
        super.setVisible(isVisible);
        Optional.ofNullable(getRootPane())
            .filter(rootPane -> isVisible() != oldVisible)
            .ifPresent(rootPane -> rootPane.getLayeredPane().setVisible(!isVisible));
//         JRootPane rootPane = getRootPane();
//         if (Objects.nonNull(rootPane) && isVisible() != oldVisible) {
//             rootPane.getLayeredPane().setVisible(!isVisible);
//         }
    }
    @Override protected void paintComponent(Graphics g) {
        Optional.ofNullable(getRootPane()).ifPresent(rootPane -> rootPane.getLayeredPane().print(g));
//         JRootPane rootPane = getRootPane();
//         if (Objects.nonNull(rootPane)) {
//             // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/disabling_swing.html
//             // it is important to call print() instead of paint() here
//             // because print() doesn't affect the frame's double buffer
//             rootPane.getLayeredPane().print(g);
//         }
        super.paintComponent(g);
    }
}

//     class LockingGlassPane_ extends JComponent {
//         public LockingGlassPane_() {
//             setOpaque(false);
//             setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
//                 @Override public boolean accept(Component c) {
//                     return false;
//                 }
//             });
// //             Set<AWTKeyStroke> s = Collections.emptySet();
// //             setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, s);
// //             setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, s);
//             // addKeyListener(new KeyAdapter() {});
//             addMouseListener(new MouseAdapter() {});
//             requestFocusInWindow();
//             super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//         }
//         @Override public void setVisible(boolean flag) {
//             super.setVisible(flag);
//             setFocusTraversalPolicyProvider(flag);
//         }
//     }
