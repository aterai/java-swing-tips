package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final Action closeSelectedFrameAction1;
    private final Action closeSelectedFrameAction2;
    private final Action closeSelectedFrameAction3;
    private final Action disposeSelectedFrameAction;
    private final Action createNewFrameAction;
    public MainPanel() {
        super(new BorderLayout());
        closeSelectedFrameAction1 = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame f = desktop.getSelectedFrame();
                if(f!=null) {
                    desktop.getDesktopManager().closeFrame(f);
                }
            }
        };
        closeSelectedFrameAction2 = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame f = desktop.getSelectedFrame();
                if(f!=null) {
                    f.doDefaultCloseAction();
                }
            }
        };
        closeSelectedFrameAction3 = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                try{
                    JInternalFrame f = desktop.getSelectedFrame();
                    if(f!=null) {
                        f.setClosed(true);
                    }
                }catch(java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        };
        disposeSelectedFrameAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame f = desktop.getSelectedFrame();
                if(f!=null) {
                    f.dispose();
                }
            }
        };
        createNewFrameAction = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                MyInternalFrame frame = new MyInternalFrame();
                //frame.setVisible(true);
                desktop.add(frame);
                try{
                    frame.setSelected(true);
                    if(openFrameCount%2==0) {
                        frame.setIcon(true);
                    }
                }catch(java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        };
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        desktop.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, "escape");
        desktop.getActionMap().put("escape", closeSelectedFrameAction1);

        add(desktop);
        add(createToolBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    protected JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        ToolBarButton b = new ToolBarButton(createNewFrameAction);
        b.setIcon(new ImageIcon(getClass().getResource("icon_new-file.png")));
        b.setToolTipText("create new InternalFrame");
        toolbar.add(b);
        toolbar.add(Box.createGlue());
        b = new ToolBarButton(disposeSelectedFrameAction);
        b.setIcon(new CloseIcon(Color.RED));
        b.setToolTipText("f.dispose();");
        toolbar.add(b);
        b = new ToolBarButton(closeSelectedFrameAction1);
        b.setIcon(new CloseIcon(Color.GREEN));
        b.setToolTipText("desktop.getDesktopManager().closeFrame(f);");
        toolbar.add(b);
        b = new ToolBarButton(closeSelectedFrameAction2);
        b.setIcon(new CloseIcon(Color.BLUE));
        b.setToolTipText("f.doDefaultCloseAction();");
        toolbar.add(b);
        b = new ToolBarButton(closeSelectedFrameAction3);
        b.setIcon(new CloseIcon(Color.YELLOW));
        b.setToolTipText("f.setClosed(true);");
        toolbar.add(b);
        return toolbar;
    }

    private static int openFrameCount = 0;
    private static int row = 0;
    private static int col = 0;
    class MyInternalFrame extends JInternalFrame implements InternalFrameListener {
        public MyInternalFrame() {
            super(String.format("Document #%s", ++openFrameCount), true, true, true, true);
            row = row + 1;
            setSize(240, 120);
            setLocation(20*row+20*col, 20*row);
            setVisible(true);
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    Rectangle drect = desktop.getBounds();
                    drect.setLocation(0,0);
                    if(!drect.contains(getBounds())) {
                        row = 0;
                        col = col + 1;
                    }
                }
            });
            addInternalFrameListener(this);
            //JComponent c = (JComponent)frame.getContentPane();
            //ActionMap am = frame.getActionMap();
            //Action a = new AbstractAction() {
            //    @Override public void actionPerformed(ActionEvent e) {
            //        try{
            //            frame.setClosed(true);
            //        }catch(java.beans.PropertyVetoException ex) {
            //            ex.printStackTrace();
            //        }
            //    }
            //};
            //am.put("myTest", a);
            //InputMap im  = frame.getInputMap();
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "myTest");
        }
        @Override public void internalFrameClosing(InternalFrameEvent e) {
            System.out.println("internalFrameClosing: " + getTitle());
        }
        @Override public void internalFrameClosed(InternalFrameEvent e) {
            System.out.println("internalFrameClosed: " + getTitle());
        }
        @Override public void internalFrameOpened(InternalFrameEvent e) {
            System.out.println("internalFrameOpened: " + getTitle());
        }
        @Override public void internalFrameIconified(InternalFrameEvent e) {
            System.out.println("internalFrameIconified: " + getTitle());
        }
        @Override public void internalFrameDeiconified(InternalFrameEvent e) {
            System.out.println("internalFrameDeiconified: " + getTitle());
        }
        @Override public void internalFrameActivated(InternalFrameEvent e) {
            //System.out.println("internalFrameActivated: " + getTitle());
        }
        @Override public void internalFrameDeactivated(InternalFrameEvent e) {
            System.out.println("internalFrameDeactivated: " + getTitle());
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class ToolBarButton extends JButton {
    public ToolBarButton(Action a) {
        super(a);
        setContentAreaFilled(false);
        setFocusPainted(false);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent me) {
                setContentAreaFilled(true);
            }
            @Override public void mouseExited(MouseEvent me) {
                setContentAreaFilled(false);
            }
        });
    }
}

class CloseIcon implements Icon {
    private int width;
    private int height;
    private final Color color;
    public CloseIcon(Color color) {
        this.color = color;
        width  = 16;
        height = 16;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(color);
        g.drawLine(4,  4, 11, 11);
        g.drawLine(4,  5, 10, 11);
        g.drawLine(5,  4, 11, 10);
        g.drawLine(11, 4,  4, 11);
        g.drawLine(11, 5,  5, 11);
        g.drawLine(10, 4,  4, 10);
        g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return width;
    }
    @Override public int getIconHeight() {
        return height;
    }
}
