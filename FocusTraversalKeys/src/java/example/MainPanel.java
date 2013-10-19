package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
// import javax.swing.event.*;

public class MainPanel extends JPanel {
    private final JTextArea textarea = new JTextArea();
    private final JFrame frame;
    private final JButton b;
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        Box box = Box.createHorizontalBox();
        box.add(box.createHorizontalGlue());
        box.add(new JButton("111"));
        box.add(new JButton("222"));
        box.add(b = new JButton("showOptionDialog"));
        box.add(new JButton("333"));
        b.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent ae) {
                String info = "<html>FORWARD_TRAVERSAL_KEYS : TAB, RIGHT, DOWN"+
                                "<br>BACKWARD_TRAVERSAL_KEYS: SHIFT+TAB, LEFT, UP</html>";
                int retValue = JOptionPane.showConfirmDialog(frame, info);
//                 int retValue = showOptionDialog(frame, info, "Test Options",
//                                                 JOptionPane.YES_NO_CANCEL_OPTION,
//                                                 JOptionPane.INFORMATION_MESSAGE,
//                                                 null, null, null);
                if(retValue==JOptionPane.YES_OPTION) {
                    System.out.println("YES_OPTION");
                }else if(retValue==JOptionPane.NO_OPTION) {
                    System.out.println("NO_OPTION");
                }else if(retValue==JOptionPane.CANCEL_OPTION) {
                    System.out.println("CANCEL_OPTION");
                }
            }
        });
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        //Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(frame.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,  0));
        //frame.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
        focusManager.setDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

        //Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(frame.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
        backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP,   0));
        //frame.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
        focusManager.setDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);

        frame.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            @Override protected boolean accept(Component c) {
                return (c==textarea)?false:super.accept(c);
            }
            @Override public Component getDefaultComponent(Container aContainer) {
                return b;
            }
        });
        textarea.setText("FORWARD_TRAVERSAL_KEYS: TAB, RIGHT, DOWN\nBACKWARD_TRAVERSAL_KEYS: SHIFT+TAB, LEFT, UP");
        add(box, BorderLayout.SOUTH);
        add(new JScrollPane(textarea));
        setPreferredSize(new Dimension(320, 160));
    }

//     // %JAVA_HOME%/src_b23/javax/swing/JOptionPane.java
//     public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) throws HeadlessException {
//         JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);
//         pane.setInitialValue(initialValue);
//         pane.setComponentOrientation(((parentComponent==null)?JOptionPane.getRootFrame():parentComponent).getComponentOrientation());
//
//         //int style = JOptionPane.styleFromMessageType(messageType);
//         //JDialog dialog = pane.createDialog(parentComponent, title, style);
//         JDialog dialog = pane.createDialog(parentComponent, title);
//
//         Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(dialog.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
//         forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
//         forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,  0));
//         dialog.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
//
//         Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(dialog.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
//         backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
//         backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP,   0));
//         dialog.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
//
//         pane.selectInitialValue();
//         //dialog.show();
//         dialog.setVisible(true);
//         dialog.dispose();
//
//         Object selectedValue = pane.getValue();
//         if(selectedValue == null)
//             return JOptionPane.CLOSED_OPTION;
//         if(options == null) {
//             if(selectedValue instanceof Integer)
//               return ((Integer)selectedValue).intValue();
//             return JOptionPane.CLOSED_OPTION;
//         }
//         for(int counter = 0, maxCounter = options.length;
//             counter < maxCounter; counter++) {
//             if(options[counter].equals(selectedValue))
//               return counter;
//         }
//         return JOptionPane.CLOSED_OPTION;
//     }

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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
