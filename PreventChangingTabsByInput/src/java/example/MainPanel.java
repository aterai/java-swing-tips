package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        // No effect: UIManager.put("TabbedPane.disabledAreNavigable", Boolean.TRUE);

        JTabbedPane tabbedPane0 = makeTabbedPane();
        tabbedPane0.setEnabled(false);
        tabbedPane0.setBorder(BorderFactory.createTitledBorder("setEnabled(false)"));

        // JTabbedPane tabbedPane1 = makeTabbedPane();
        // for (int i = 0; i < tabbedPane1.getTabCount(); i++) {
        //   tabbedPane1.setEnabledAt(i, false);
        // }
        // tabbedPane1.setBorder(BorderFactory.createTitledBorder("setEnabledAt(idx, false)"));

        JTabbedPane tabbedPane2 = makeTabbedPane();
        tabbedPane2.setEnabled(false);
        for (int i = 0; i < tabbedPane2.getTabCount(); i++) {
            tabbedPane2.setTabComponentAt(i, new JLabel(tabbedPane2.getTitleAt(i)));
        }
        tabbedPane2.setBorder(BorderFactory.createTitledBorder("setTabComponentAt(...)"));

        JTabbedPane tabbedPane3 = makeTabbedPane();
        tabbedPane3.setBorder(BorderFactory.createTitledBorder("DisableInputLayerUI()"));

        JPanel p = new JPanel(new GridLayout(0, 1, 0, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Arrays.asList(tabbedPane0, tabbedPane2).forEach(p::add);
        p.add(new JLayer<Component>(tabbedPane3, new DisableInputLayerUI()));

        JButton button = new JButton("next");
        button.addActionListener(e -> {
            int i = tabbedPane0.getSelectedIndex() + 1;
            int next = i >= tabbedPane0.getTabCount() ? 0 : i;
            Arrays.asList(tabbedPane0, tabbedPane2, tabbedPane3).forEach(t -> t.setSelectedIndex(next));
        });

        add(p, BorderLayout.NORTH);
        add(button, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTabbedPane makeTabbedPane() {
        JTabbedPane tabs = new JTabbedPane() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 70;
                return d;
            }
        };
        for (int i = 0; i < 4; i++) {
            String title = "Step " + i;
            tabs.addTab(title, new JTextField(title));
        }
        tabs.setFocusable(false);
        return tabs;
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

class DisableInputLayerUI extends LayerUI<Component> {
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer<?>) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    @Override public void eventDispatched(AWTEvent e, JLayer<? extends Component> l) {
        if (e instanceof InputEvent && Objects.equals(l.getView(), e.getSource())) {
            ((InputEvent) e).consume();
        }
    }
}
