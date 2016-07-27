package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final Action closeSelectedFrameAction1 = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getSelectedFrame().ifPresent(desktop.getDesktopManager()::closeFrame);
        }
    };
    private final Action closeSelectedFrameAction2 = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getSelectedFrame().ifPresent(JInternalFrame::doDefaultCloseAction);
        }
    };
    private final Action closeSelectedFrameAction3 = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getSelectedFrame().ifPresent(f -> {
                try {
                    f.setClosed(true);
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            });
        }
    };
    private final Action disposeSelectedFrameAction = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getSelectedFrame().ifPresent(JInternalFrame::dispose);
        }
    };
    private final Action createNewFrameAction = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            JInternalFrame frame = new MyInternalFrame();
            //frame.setVisible(true);
            desktop.add(frame);
            try {
                frame.setSelected(true);
                if (openFrameCount % 2 == 0) {
                    frame.setIcon(true);
                }
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    };
    private int openFrameCount;
    private int row;
    private int col;

    public MainPanel() {
        super(new BorderLayout());

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        desktop.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, "escape");
        desktop.getActionMap().put("escape", closeSelectedFrameAction1);

        add(desktop);
        add(createToolBar(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private Optional<? extends JInternalFrame> getSelectedFrame() {
        return Optional.ofNullable(desktop.getSelectedFrame());
    }
    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        JButton b = new ToolBarButton(createNewFrameAction);
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

    class MyInternalFrame extends JInternalFrame {
        protected MyInternalFrame() {
            super(String.format("Document #%s", ++openFrameCount), true, true, true, true);
            row += 1;
            setSize(240, 120);
            setLocation(20 * row + 20 * col, 20 * row);
            setVisible(true);
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    Rectangle drect = desktop.getBounds();
                    drect.setLocation(0, 0);
                    if (!drect.contains(getBounds())) {
                        row = 0;
                        col += 1;
                    }
                }
            });
            addInternalFrameListener(new MyInternalFrameListener());
            //JComponent c = (JComponent) frame.getContentPane();
            //ActionMap am = frame.getActionMap();
            //Action a = new AbstractAction() {
            //    @Override public void actionPerformed(ActionEvent e) {
            //        try {
            //            frame.setClosed(true);
            //        } catch (PropertyVetoException ex) {
            //            ex.printStackTrace();
            //        }
            //    }
            //};
            //am.put("myTest", a);
            //InputMap im = frame.getInputMap();
            //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "myTest");
        }
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

class MyInternalFrameListener implements InternalFrameListener {
    @Override public void internalFrameClosing(InternalFrameEvent e) {
        System.out.println("internalFrameClosing: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameClosed(InternalFrameEvent e) {
        System.out.println("internalFrameClosed: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameOpened(InternalFrameEvent e) {
        System.out.println("internalFrameOpened: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameIconified(InternalFrameEvent e) {
        System.out.println("internalFrameIconified: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameDeiconified(InternalFrameEvent e) {
        System.out.println("internalFrameDeiconified: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameActivated(InternalFrameEvent e) {
        //System.out.println("internalFrameActivated: " + e.getInternalFrame().getTitle());
    }
    @Override public void internalFrameDeactivated(InternalFrameEvent e) {
        System.out.println("internalFrameDeactivated: " + e.getInternalFrame().getTitle());
    }
}

class ToolBarButton extends JButton {
    private transient MouseAdapter handler;
    protected ToolBarButton(Action a) {
        super(a);
    }
    @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        handler = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setContentAreaFilled(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                setContentAreaFilled(false);
            }
        };
        addMouseListener(handler);
    }
}

class CloseIcon implements Icon {
    private final Color color;
    protected CloseIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.drawLine(4,  4, 11, 11);
        g2.drawLine(4,  5, 10, 11);
        g2.drawLine(5,  4, 11, 10);
        g2.drawLine(11, 4,  4, 11);
        g2.drawLine(11, 5,  5, 11);
        g2.drawLine(10, 4,  4, 10);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}
