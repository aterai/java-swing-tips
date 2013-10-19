package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public final JMenuBar menuBar = new JMenuBar();
    public MainPanel() {
        super();
        menuBar.add(createLookAndFeelMenu());

        UIManager.put("Button.disabledText", Color.RED);
        final JButton button1 = makeButton("Default");
        final JButton button2 = makeButton("setForeground");
        final JButton button3 = makeButton("JLayer");

        final DisableInputLayerUI layerUI = new DisableInputLayerUI();
        JCheckBox check = new JCheckBox(new AbstractAction("setEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean isSelected = ((JCheckBox)e.getSource()).isSelected();
                button1.setEnabled(isSelected);

                button2.setEnabled(isSelected);
                button2.setForeground(isSelected?Color.BLACK:Color.RED);

                layerUI.setLocked(!isSelected);
            }
        });
        check.setSelected(true);

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("setEnabled"));
        p1.add(button1);
        p1.add(button2);
        p1.add(new JLayer<JComponent>(button3, layerUI));

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
        button.setMnemonic(title.charAt(0));
        button.setToolTipText(title);
        button.setComponentPopupMenu(pop);
        return button;
    }

    //<blockquote cite="http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java">
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;
    protected JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }
        return menu;
    }
    protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = this.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>
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
        MainPanel p = new MainPanel();
        frame.getContentPane().add(p);
        frame.setJMenuBar(p.menuBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class DisableInputLayerUI extends LayerUI<JComponent> {
    private static final boolean DEBUG_POPUP_BLOCK = false;
    private static final MouseAdapter dummyMouseListener = new MouseAdapter() {};
    private static final KeyAdapter dummyKeyListener = new KeyAdapter() {};
    private boolean isBlocking = false;
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        JLayer jlayer = (JLayer)c;
        if(DEBUG_POPUP_BLOCK) {
            jlayer.getGlassPane().addMouseListener(dummyMouseListener);
            jlayer.getGlassPane().addKeyListener(dummyKeyListener);
        }
        jlayer.setLayerEventMask(
            AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
            AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
            AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
    }
    @Override public void uninstallUI(JComponent c) {
        JLayer jlayer = (JLayer)c;
        jlayer.setLayerEventMask(0);
        if(DEBUG_POPUP_BLOCK) {
            jlayer.getGlassPane().removeMouseListener(dummyMouseListener);
            jlayer.getGlassPane().removeKeyListener(dummyKeyListener);
        }
        super.uninstallUI(c);
    }
    @Override protected void processComponentEvent(ComponentEvent e, JLayer<? extends JComponent> l) {
        System.out.println("processComponentEvent");
    }
    @Override protected void processKeyEvent(KeyEvent e, JLayer<? extends JComponent> l) {
        System.out.println("processKeyEvent");
    }
    @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends JComponent> l) {
        System.out.println("processFocusEvent");
    }
    @Override public void eventDispatched(AWTEvent e, JLayer<? extends JComponent> l) {
        if(isBlocking && e instanceof InputEvent) {
            ((InputEvent)e).consume();
        }
    }
    private static final String CMD_BLOCKING = "lock";
    public void setLocked(boolean flag) {
        boolean oldv = isBlocking;
        isBlocking = flag;
        firePropertyChange(CMD_BLOCKING,oldv,isBlocking);
    }
    @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer<? extends JComponent> l) {
        String cmd = pce.getPropertyName();
        if(CMD_BLOCKING.equals(cmd)) {
            JButton b = (JButton)l.getView();
            b.setFocusable(!isBlocking);
            b.setMnemonic(isBlocking?0:b.getText().charAt(0));
            b.setForeground(isBlocking?Color.RED:Color.BLACK);
            l.getGlassPane().setVisible((Boolean)pce.getNewValue());
        }
    }
}

// class LockingGlassPane extends JPanel {
//   public LockingGlassPane() {
//     setOpaque(false);
//     setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
//       @Override public boolean accept(Component c) { return false; }
//     });
//     addKeyListener(new KeyAdapter() {});
//     addMouseListener(new MouseAdapter() {});
//     requestFocusInWindow();
//     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//   }
//   @Override public void setVisible(boolean flag) {
//     super.setVisible(flag);
//     setFocusTraversalPolicyProvider(flag);
//   }
// }
//
// class LockingGlassPane extends JPanel {
//   public LockingGlassPane() {
//     setOpaque(false);
//     super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//   }
//   @Override public void setVisible(boolean isVisible) {
//     boolean oldVisible = isVisible();
//     super.setVisible(isVisible);
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if(rootPane!=null && isVisible()!=oldVisible) {
//       rootPane.getLayeredPane().setVisible(!isVisible);
//     }
//   }
//   @Override public void paintComponent(Graphics g) {
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if(rootPane!=null) {
//       // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//       // it is important to call print() instead of paint() here
//       // because print() doesn't affect the frame's double buffer
//       rootPane.getLayeredPane().print(g);
//     }
//     super.paintComponent(g);
//   }
// }

// class PrintGlassPane extends JPanel {
//     //TexturePaint texture = TextureFactory.createCheckerTexture(4);
//     public PrintGlassPane() {
//         super((LayoutManager)null);
//         setOpaque(false);
//     }
//     @Override public void setVisible(boolean isVisible) {
//         boolean oldVisible = isVisible();
//         super.setVisible(isVisible);
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if(rootPane!=null && isVisible()!=oldVisible) {
//             rootPane.getLayeredPane().setVisible(!isVisible);
//         }
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         JRootPane rootPane = SwingUtilities.getRootPane(this);
//         if(rootPane!=null) {
//             //http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//             // it is important to call print() instead of paint() here
//             // because print() doesn't affect the frame's double buffer
//             rootPane.getLayeredPane().print(g);
//         }
//         //     Graphics2D g2 = (Graphics2D) g;
//         //     g2.setPaint(texture);
//         //     g2.fillRect(0,0,getWidth(),getHeight());
//     }
// }
