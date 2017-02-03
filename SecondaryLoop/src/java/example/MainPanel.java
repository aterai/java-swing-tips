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
    private final DisableInputLayerUI<JComponent> layerUI = new DisableInputLayerUI<>();
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel();
        p.add(new JCheckBox());
        p.add(new JTextField(10));
        p.add(new JButton(new AbstractAction("Stop 5sec") {
            @Override public void actionPerformed(ActionEvent e) {
                layerUI.setInputBlock(true);
                final SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
                Thread work = new Thread() {
                    @Override public void run() {
                        doInBackground();
                        layerUI.setInputBlock(false);
                        loop.exit();
                    }
                };
                work.start();
                loop.enter();
            }
        }));
        add(new JLayer<>(p, layerUI), BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("dummy")));
        setPreferredSize(new Dimension(320, 240));
    }
    private void doInBackground() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
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
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DisableInputLayerUI<V extends JComponent> extends LayerUI<V> {
    private static final String CMD_REPAINT = "repaint";
    private boolean isRunning;
    public void setInputBlock(boolean block) {
        firePropertyChange(CMD_REPAINT, isRunning, block);
        isRunning = block;
    }
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (!isRunning) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
        g2.setPaint(Color.GRAY.brighter());
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
    @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
        if (isRunning && e instanceof InputEvent) {
            ((InputEvent) e).consume();
        }
    }
    @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer<? extends V> l) {
        String cmd = pce.getPropertyName();
        if (CMD_REPAINT.equals(cmd)) {
            l.getGlassPane().setVisible((Boolean) pce.getNewValue());
            l.repaint();
        }
    }
}
