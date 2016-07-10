package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JInternalFrame iframe = new JInternalFrame("title", true, true, true, true);
    private final JTextArea textArea = new JTextArea();
    private final transient InternalFrameListener handler = new InternalFrameListener() {
        @Override public void internalFrameClosing(InternalFrameEvent e) {
            displayMessage("Internal frame closing", e);
        }
        @Override public void internalFrameClosed(InternalFrameEvent e) {
            displayMessage("Internal frame closed", e);
        }
        @Override public void internalFrameOpened(InternalFrameEvent e) {
            displayMessage("Internal frame opened", e);
        }
        @Override public void internalFrameIconified(InternalFrameEvent e) {
            displayMessage("Internal frame iconified", e);
        }
        @Override public void internalFrameDeiconified(InternalFrameEvent e) {
            displayMessage("Internal frame deiconified", e);
        }
        @Override public void internalFrameActivated(InternalFrameEvent e) {
            displayMessage("Internal frame activated", e);
        }
        @Override public void internalFrameDeactivated(InternalFrameEvent e) {
            displayMessage("Internal frame deactivated", e);
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        iframe.addPropertyChangeListener(e -> {
            String prop = e.getPropertyName();
            if (JInternalFrame.IS_MAXIMUM_PROPERTY == prop) {
                if (e.getNewValue() == Boolean.TRUE) {
                    displayMessage("* Internal frame maximized", e);
                } else {
                    displayMessage("* Internal frame minimized", e);
                }
            }
        });
        iframe.addInternalFrameListener(handler);
        iframe.setBounds(10, 10, 160, 100);
        desktop.add(iframe);
        iframe.setVisible(true);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(desktop);
        sp.setBottomComponent(new JScrollPane(textArea));
        sp.setResizeWeight(.8);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private void displayMessage(String prefix, EventObject e) {
        String s = prefix + ": " + e.getSource();
        textArea.append(s + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

//     private JMenuBar createMenuBar() {
//         JMenuBar menuBar = new JMenuBar();
//         JMenu menu = new JMenu("Window");
//         menu.setMnemonic(KeyEvent.VK_W);
//         menuBar.add(menu);
//         JMenuItem menuItem = new JMenuItem("New");
//         menuItem.setMnemonic(KeyEvent.VK_N);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
//         menuItem.setActionCommand("new");
//         menuItem.addActionListener(new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 JInternalFrame frame = createInternalFrame();
//                 desktop.add(frame);
//                 frame.setVisible(true);
//                 //desktop.getDesktopManager().activateFrame(frame);
//             }
//         });
//         menu.add(menuItem);
//         return menuBar;
//     }
//
//     private static JInternalFrame createInternalFrame() {
//         JInternalFrame f = new JInternalFrame(String.format("Document #%s", openFrameCount.getAndIncrement()), true, true, true, true);
//         f.setSize(160, 100);
//         f.setLocation(XOFFSET * openFrameCount.intValue(), YOFFSET * openFrameCount.intValue());
//         return f;
//     }

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
