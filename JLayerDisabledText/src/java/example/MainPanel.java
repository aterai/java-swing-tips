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
    private MainPanel() {
        super();

        UIManager.put("Button.disabledText", Color.RED);
        final JButton button1 = makeButton("Default");
        final JButton button2 = makeButton("setForeground");
        final JButton button3 = makeButton("JLayer");

        DisableInputLayerUI<AbstractButton> layerUI = new DisableInputLayerUI<>();
        JCheckBox check = new JCheckBox(new AbstractAction("setEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
                button1.setEnabled(isSelected);

                button2.setEnabled(isSelected);
                button2.setForeground(isSelected ? Color.BLACK : Color.RED);

                layerUI.setLocked(!isSelected);
            }
        });
        check.setSelected(true);

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("setEnabled"));
        p1.add(button1);
        p1.add(button2);
        p1.add(new JLayer<>(button3, layerUI));

        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("Focus dummy"));
        p2.add(new JTextField(16));
        p2.add(new JButton("dummy"));

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(p1);
        panel.add(p2);

        add(panel);
        add(check);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JButton makeButton(String title) {
        JPopupMenu pop = new JPopupMenu();
        pop.add(new JMenuItem(title));
        JButton button = new JButton(title);
        if (title.length() > 0) {
            button.setMnemonic(title.codePointAt(0));
        }
        button.setToolTipText(title);
        button.setComponentPopupMenu(pop);
        return button;
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
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DisableInputLayerUI<V extends AbstractButton> extends LayerUI<V> {
    private static final String CMD_BLOCKING = "lock";
    private static final boolean DEBUG_POPUP_BLOCK = false;
    private final transient MouseListener dummyMouseListener = new MouseAdapter() { /* Dummy listener */ };
    private final transient KeyListener dummyKeyListener = new KeyAdapter() { /* Dummy listener */ };
    private boolean isBlocking;

    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            if (DEBUG_POPUP_BLOCK) {
                jlayer.getGlassPane().addMouseListener(dummyMouseListener);
                jlayer.getGlassPane().addKeyListener(dummyKeyListener);
            }
            jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                                   | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
                                   | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            jlayer.setLayerEventMask(0);
            if (DEBUG_POPUP_BLOCK) {
                jlayer.getGlassPane().removeMouseListener(dummyMouseListener);
                jlayer.getGlassPane().removeKeyListener(dummyKeyListener);
            }
        }
        super.uninstallUI(c);
    }
    @Override protected void processComponentEvent(ComponentEvent e, JLayer<? extends V> l) {
        System.out.println("processComponentEvent");
    }
    @Override protected void processKeyEvent(KeyEvent e, JLayer<? extends V> l) {
        System.out.println("processKeyEvent");
    }
    @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends V> l) {
        System.out.println("processFocusEvent");
    }
    @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
        if (isBlocking && e instanceof InputEvent) {
            ((InputEvent) e).consume();
        }
    }
    public void setLocked(boolean flag) {
        boolean oldv = isBlocking;
        isBlocking = flag;
        firePropertyChange(CMD_BLOCKING, oldv, isBlocking);
    }
    @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer<? extends V> l) {
        String cmd = pce.getPropertyName();
        if (CMD_BLOCKING.equals(cmd)) {
            AbstractButton b = l.getView();
            b.setFocusable(!isBlocking);
            b.setMnemonic(isBlocking ? 0 : b.getText().codePointAt(0));
            b.setForeground(isBlocking ? Color.RED : Color.BLACK);
            l.getGlassPane().setVisible((Boolean) pce.getNewValue());
        }
    }
}

// class LockingGlassPane extends JPanel {
//     protected LockingGlassPane() {
//         super();
//         setOpaque(false);
//         setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
//             @Override public boolean accept(Component c) {
//                 return false;
//             }
//         });
//         addKeyListener(new KeyAdapter() {});
//         addMouseListener(new MouseAdapter() {});
//         requestFocusInWindow();
//         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//     }
//     @Override public void setVisible(boolean flag) {
//         super.setVisible(flag);
//         setFocusTraversalPolicyProvider(flag);
//     }
// }

// class LockingGlassPane extends JPanel {
//     protected LockingGlassPane() {
//         super();
//         setOpaque(false);
//         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//     }
//     @Override public void setVisible(boolean isVisible) {
//         boolean oldVisible = isVisible();
//         super.setVisible(isVisible);
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if (rootPane != null && isVisible() != oldVisible) {
//             rootPane.getLayeredPane().setVisible(!isVisible);
//         }
//     }
//     @Override protected void paintComponent(Graphics g) {
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if (rootPane != null) {
//             // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//             // it is important to call print() instead of paint() here
//             // because print() doesn't affect the frame's double buffer
//             rootPane.getLayeredPane().print(g);
//         }
//         super.paintComponent(g);
//     }
// }

// class PrintGlassPane extends JPanel {
//     //TexturePaint texture = TextureFactory.createCheckerTexture(4);
//     protected PrintGlassPane() {
//         super((LayoutManager) null);
//         setOpaque(false);
//     }
//     @Override public void setVisible(boolean isVisible) {
//         boolean oldVisible = isVisible();
//         super.setVisible(isVisible);
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if (rootPane != null && isVisible() != oldVisible) {
//             rootPane.getLayeredPane().setVisible(!isVisible);
//         }
//     }
//     @Override protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if (rootPane != null) {
//             //http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//             // it is important to call print() instead of paint() here
//             // because print() doesn't affect the frame's double buffer
//             rootPane.getLayeredPane().print(g);
//         }
//         //Graphics2D g2 = (Graphics2D) g.create();
//         //g2.setPaint(texture);
//         //g2.fillRect(0, 0, getWidth(), getHeight());
//         //g2.dispose();
//     }
// }

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lafRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lafRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
