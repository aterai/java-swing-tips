package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField field = new JTextField();
    private final JButton nb = new JButton("NORTH");
    private final JButton sb = new JButton("SOUTH");
    private final JButton wb = new JButton("WEST");
    private final JButton eb = new JButton("EAST");
    private final JTextArea ta = new JTextArea("aaaaaaaaaa");
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(nb, BorderLayout.NORTH);
        p.add(sb, BorderLayout.SOUTH);
        p.add(wb, BorderLayout.WEST);
        p.add(eb, BorderLayout.EAST);
        p.add(field);
        ta.setEditable(false);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(ta));
        setPreferredSize(new Dimension(320, 180));

        frame.getRootPane().setDefaultButton(eb);

//         frame.addWindowListener(new WindowAdapter() {
//             @Override public void windowOpened(WindowEvent e) {
//                 System.out.println("windowOpened");
//                 field.requestFocus();
//             }
//         });

//         frame.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
//             @Override public Component getInitialComponent(Window w) {
//                 System.out.println("getInitialComponent");
//                 return field;
//             }
//         });

//         frame.addComponentListener(new ComponentAdapter() {
//             @Override public void componentShown(ComponentEvent e) {
//                 System.out.println("componentShown");
//                 field.requestFocusInWindow();
//             }
//         });

//         KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//         focusManager.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
//             @Override public void propertyChange(java.beans.PropertyChangeEvent e) {
//                 String prop = e.getPropertyName();
//                 if("activeWindow".equals(prop) && e.getNewValue()!=null) {
//                     System.out.println("activeWindow");
//                     field.requestFocusInWindow();
//                 }
//             }
//         });

        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                System.out.println("invokeLater");
                field.requestFocusInWindow();
            }
        });

        System.out.println("this");
        //field.requestFocusInWindow();
    }

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
        System.out.println("frame.pack();");
        frame.pack();
        frame.setLocationRelativeTo(null);
        System.out.println("frame.setVisible(true);");
        frame.setVisible(true);
    }
}
