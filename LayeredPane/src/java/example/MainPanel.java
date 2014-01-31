package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JFrame frame;
    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        frame.setJMenuBar(createMenuBar());
        //title, resizable, closable, maximizable, iconifiable
        JInternalFrame iframe = new JInternalFrame("AlwaysOnTop", true, false, true, true);
        iframe.setSize(180, 180);
        desktop.add(iframe, Integer.valueOf(JLayeredPane.MODAL_LAYER+1));
        iframe.setVisible(true);
        //desktop.getDesktopManager().activateFrame(iframe);
        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Document");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("new");
        menuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame iframe = new MyInternalFrame();
                desktop.add(iframe);
                iframe.setVisible(true);
                //desktop.getDesktopManager().activateFrame(iframe);
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("quit");
        menuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        menu.add(menuItem);
        return menuBar;
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class MyInternalFrame extends JInternalFrame {
    private static final int XOFFSET = 30;
    private static final int YOFFSET = 30;
    private static int openFrameCount;
    public MyInternalFrame() {
        //title, resizable, closable, maximizable, iconifiable
        super(String.format("Document #%s", ++openFrameCount), true, true, true, true);
        setSize(180, 100);
        setLocation(XOFFSET*openFrameCount, YOFFSET*openFrameCount);
    }
}
