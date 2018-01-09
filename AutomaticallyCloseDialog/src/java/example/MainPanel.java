package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextArea textArea = new JTextArea();

        JLabel label = new JLabel();
        label.addHierarchyListener(new AutomaticallyCloseListener());

        JButton button = new JButton("show");
        button.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(button), label, "Automatically close dialog", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            switch (r) {
                case JOptionPane.OK_OPTION:
                    textArea.append("OK\n");
                    break;
                case JOptionPane.CANCEL_OPTION:
                    textArea.append("Cancel\n");
                    break;
                case JOptionPane.CLOSED_OPTION:
                    textArea.append("Closed(automatically)\n");
                    break;
                default:
                    textArea.append("----\n");
                    break;
            }
            textArea.append("\n");
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("HierarchyListener"));
        p.add(button);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class AutomaticallyCloseListener implements HierarchyListener {
    // private static final Logger LOGGER = Logger.getLogger(AutomaticallyCloseListener.class.getName());
    private static final int SECONDS = 5;
    private final AtomicInteger atomicDown = new AtomicInteger(SECONDS);
    private final Timer timer = new Timer(1000, null);
    private transient ActionListener listener;

    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            JLabel l = (JLabel) e.getComponent();
            if (l.isShowing()) {
                // LOGGER.info("isShowing=true\n");
                atomicDown.set(SECONDS);
                l.setText(String.format("Closing in %d seconds", SECONDS));
                timer.removeActionListener(listener);
                listener = event -> {
                    int i = atomicDown.decrementAndGet();
                    l.setText(String.format("Closing in %d seconds", i));
                    if (i <= 0 && timer.isRunning()) {
                        // LOGGER.info("Timer: timer.stop()\n");
                        timer.stop();
                        Optional.ofNullable(l.getTopLevelAncestor())
                            .filter(Window.class::isInstance).map(Window.class::cast)
                            .ifPresent(Window::dispose);
//                         Container c = l.getTopLevelAncestor();
//                         if (c instanceof Window) {
//                             // LOGGER.info("window.dispose()\n");
//                             ((Window) c).dispose();
//                         }
                    }
                };
                timer.addActionListener(listener);
                timer.start();
            } else {
                // LOGGER.info("isShowing=false\n");
                if (timer.isRunning()) {
                    // LOGGER.info("timer.stop()\n");
                    timer.stop();
                }
            }
        }
    }
}
