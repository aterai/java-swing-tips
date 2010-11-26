package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

//How to Use Internal Frames
//http://java.sun.com/docs/books/tutorial/uiswing/components/internalframe.html
//Swing - Lock JInternalPane
//http://forums.sun.com/thread.jspa?threadID=609043
public class MainPanel extends JPanel {
    private final JDesktopPane desktop;
    private final JInternalFrame immovableFrame;
    public MainPanel() {
        super(new BorderLayout());
        desktop = new JDesktopPane();
        //title, resizable, closable, maximizable, iconifiable
        immovableFrame = new JInternalFrame("immovable", false, false, true, true);
        BasicInternalFrameUI ui = (BasicInternalFrameUI)immovableFrame.getUI();
        Component north = ui.getNorthPane();
        MouseMotionListener[] actions = (MouseMotionListener[])north.getListeners(MouseMotionListener.class);
        for(int i=0;i<actions.length;i++) {
            north.removeMouseMotionListener(actions[i]);
        }
        //immovableFrame.setLocation(0, 0);
        immovableFrame.setSize(160, 0);
        desktop.add(immovableFrame);
        immovableFrame.setVisible(true);

        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                //System.out.println(e.toString());
                immovableFrame.setSize(immovableFrame.getSize().width, desktop.getSize().height);
            }
        });

        add(desktop);
        add(createMenuBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("new");
        menuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                createFrame();
            }
        });
        menu.add(menuItem);
        return menuBar;
    }

    protected void createFrame() {
        MyInternalFrame frame = new MyInternalFrame();
        desktop.add(frame);
        frame.setVisible(true);
        //desktop.getDesktopManager().activateFrame(frame);
    }

    static int openFrameCount = 0;
    static class MyInternalFrame extends JInternalFrame{
        public MyInternalFrame() {
            super("Document #" + (++openFrameCount), true, true, true, true);
            setSize(160, 100);
            setLocation(30*openFrameCount, 30*openFrameCount);
        }
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
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
