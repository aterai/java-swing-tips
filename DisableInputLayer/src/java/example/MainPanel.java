package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
    private final JPanel p = new JPanel();
    private final DisableInputLayerUI layerUI = new DisableInputLayerUI();
    private final JLayer<JPanel> jlayer = new JLayer<>(p, layerUI);
    private final Timer stopper = new Timer(5000, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            layerUI.stop();
        }
    });

    public MainPanel() {
        super(new BorderLayout());

        p.add(new JCheckBox());
        p.add(new JTextField(10));
        p.add(new JButton(new AbstractAction("Stop 5sec") {
            @Override public void actionPerformed(ActionEvent e) {
                layerUI.start();
                if (!stopper.isRunning()) {
                    stopper.start();
                }
            }
        }));
        stopper.setRepeats(false);

        add(jlayer, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("dummy")));
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

class DisableInputLayerUI extends LayerUI<JPanel> {
    private boolean isRunning;
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (!isRunning) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
        g2.setPaint(Color.GRAY);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        g2.dispose();
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            jlayer.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                                   | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
                                   | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    @Override public void eventDispatched(AWTEvent e, JLayer l) {
        if (isRunning && e instanceof InputEvent) {
            ((InputEvent) e).consume();
        }
    }
    private static final String CMD_REPAINT = "repaint";
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        firePropertyChange(CMD_REPAINT, false, true);
    }
    public void stop() {
        isRunning = false;
        firePropertyChange(CMD_REPAINT, true, false);
    }
    @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
        String cmd = pce.getPropertyName();
        if (CMD_REPAINT.equals(cmd)) {
            l.getGlassPane().setVisible((Boolean) pce.getNewValue());
            l.repaint();
        }
    }
}
