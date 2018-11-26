package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JDesktopPane desktop = new JDesktopPane();

        InputMap im = desktop.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        int modifiers = InputEvent.CTRL_DOWN_MASK;
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, modifiers), "shrinkUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, modifiers), "shrinkDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, modifiers), "shrinkLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, modifiers), "shrinkRight");

        addFrame(desktop, 0, true);
        addFrame(desktop, 1, false);
        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void addFrame(JDesktopPane desktop, int idx, boolean resizable) {
        JInternalFrame frame = new JInternalFrame("resizable: " + resizable, resizable, true, true, true);
        frame.add(makePanel());
        frame.setSize(240, 100);
        frame.setVisible(true);
        frame.setLocation(10 + 60 * idx, 10 + 120 * idx);
        desktop.add(frame);
    }
    private static Component makePanel() {
        JPanel p = new JPanel();
        p.add(new JLabel("label"));
        p.add(new JButton("button"));
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
        lafItem.setActionCommand(lafClassName);
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
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}

//
// // class NavigationAction extends AbstractAction implements ActionListener {
// //     private int deltaX;
// //     private int deltaY;
// //     private KeyStroke pressedKeyStroke;
// //     private boolean listeningForKeyPressed;
// //
// //     protected NavigationAction(int keyCode, String description, int deltaX, int deltaY) {
// //         super(description);
// //
// //         this.deltaX = deltaX;
// //         this.deltaY = deltaY;
// //
// //         pressedKeyStroke = KeyStroke.getKeyStroke(keyCode, 0, false);
// //         KeyStroke releasedKeyStroke = KeyStroke.getKeyStroke(keyCode, 0, true);
// //
// //         inputMap.put(pressedKeyStroke, getValue(Action.NAME));
// //         inputMap.put(releasedKeyStroke, getValue(Action.NAME));
// //         component.getActionMap().put(getValue(Action.NAME), this);
// //         listeningForKeyPressed = true;
// //     }
// //     @Override public void actionPerformed(ActionEvent e) {
// //         if (listeningForKeyPressed) {
// //             updateDeltaX(deltaX);
// //             updateDeltaY(deltaY);
// //
// //             inputMap.remove(pressedKeyStroke);
// //             listeningForKeyPressed = false;
// //
// //             if (keysPressed == 0) {
// //                 timer.start();
// //             }
// //
// //             keysPressed++;
// //         } else { // listening for key released
// //             updateDeltaX(-deltaX);
// //             updateDeltaY(-deltaY);
// //
// //             inputMap.put(pressedKeyStroke, getValue(Action.NAME));
// //             listeningForKeyPressed = true;
// //             keysPressed--;
// //
// //             if (keysPressed == 0) {
// //                 timer.stop();
// //             }
// //         }
// //     }
// // }
//
// class KeyboardAnimation implements ActionListener {
//     private final static String PRESSED = "pressed ";
//     private final static String RELEASED = "released ";
//     private final JComponent component;
//     private final Timer timer;
//     private final Map<String, Point> pressedKeys = new HashMap<String, Point>();
//
//     public KeyboardAnimation(JComponent component, int delay) {
//         this.component = component;
//
//         timer = new Timer(delay, this);
//         timer.setInitialDelay(0);
//     }
//     public void addAction(String keyStroke, int deltaX, int deltaY) {
//         int offset = keyStroke.lastIndexOf(" ");
//         String key = offset == -1 ? keyStroke :  keyStroke.substring(offset + 1);
//         String modifiers = keyStroke.replace(key, "");
//
//         System.out.println("keyStroke: " + keyStroke);
//         System.out.println("Key: " + key);
//
//
//         InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//         ActionMap actionMap = component.getActionMap();
//
//         //  Create Action and add binding for the pressed key
//
//         Action pressedAction = new AnimationAction(key, new Point(deltaX, deltaY));
//         String pressedKey = modifiers + PRESSED + key;
//         KeyStroke pressedKeyStroke = KeyStroke.getKeyStroke(pressedKey);
//         inputMap.put(pressedKeyStroke, pressedKey);
//         actionMap.put(pressedKey, pressedAction);
//
//         System.out.println("pressedKey: " + pressedKey);
//         System.out.println("pressedKeyStroke: " + pressedKeyStroke);
//
//         //  Create Action and add binding for the released key
//
//         Action releasedAction = new AnimationAction(key, null);
//         String releasedKey = modifiers + RELEASED + key;
//         KeyStroke releasedKeyStroke = KeyStroke.getKeyStroke(releasedKey);
//         inputMap.put(releasedKeyStroke, releasedKey);
//         actionMap.put(releasedKey, releasedAction);
//     }
//
//     //  Invoked whenever a key is pressed or released
//
//     private void handleKeyEvent(String key, Point moveDelta) {
//         System.out.println("handleKeyEvent: " + moveDelta);
//         //  Keep track of which keys are pressed
//         if (moveDelta == null) {
//             pressedKeys.remove(key);
//         } else {
//             pressedKeys.put(key, moveDelta);
//         }
//
//         if (pressedKeys.size() == 1) {
//             System.out.println("start");
//             timer.start();
//         }
//         if (pressedKeys.size() == 0) {
//             System.out.println("stop");
//             timer.stop();
//         }
//     }
//
//     //  Invoked when the Timer fires
//
//     @Override public void actionPerformed(ActionEvent e) {
//         System.out.println("aa");
//         moveComponent();
//     }
//
//     //  Move the component to its new location
//
//     private void moveComponent() {
//         int componentWidth = component.getSize().width;
//         int componentHeight = component.getSize().height;
//
//         Dimension parentSize = component.getParent().getSize();
//         int parentWidth  = parentSize.width;
//         int parentHeight = parentSize.height;
//
//         int deltaX = 0;
//         int deltaY = 0;
//
//         for (Point delta : pressedKeys.values()) {
//             deltaX += delta.x;
//             deltaY += delta.y;
//         }
//
//         int nextX = Math.max(component.getLocation().x + deltaX, 0);
//         if (nextX + componentWidth > parentWidth) {
//             nextX = parentWidth - componentWidth;
//         }
//         int nextY = Math.max(component.getLocation().y + deltaY, 0);
//         if (nextY + componentHeight > parentHeight) {
//             nextY = parentHeight - componentHeight;
//         }
//         System.out.format("moveComponent: %d, %d%n", nextX, nextY);
//         if (component instanceof JInternalFrame) {
//             JInternalFrame frame = (JInternalFrame) component;
//             DesktopManager dm = frame.getDesktopPane().getDesktopManager();
//             dm.dragFrame(frame, nextX, nextY);
//         } else {
//             component.setLocation(nextX, nextY);
//         }
//     }
//     private class AnimationAction extends AbstractAction implements ActionListener {
//         private Point moveDelta;
//         public AnimationAction(String key, Point moveDelta) {
//             super(key);
//             this.moveDelta = moveDelta;
//         }
//         @Override public void actionPerformed(ActionEvent e) {
//             handleKeyEvent((String) getValue(NAME), moveDelta);
//         }
//     }
// }
