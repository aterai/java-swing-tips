package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static final int W = 4;
    private static final Color borderColor = new Color(100,100,100);
    private SideLabel left, right, top, bottom, topleft, topright, bottomleft, bottomright;
    private JPanel resizePanel = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(Color.ORANGE);
            g2.fillRect(0,0,w,h);
            g2.setPaint(borderColor);
            g2.drawRect(0,0,w-1,h-1);

            g2.drawLine(0,2,2,0);
            g2.drawLine(w-3,0,w-1,2);

            g2.clearRect(0,0,2,1);
            g2.clearRect(0,0,1,2);
            g2.clearRect(w-2,0,2,1);
            g2.clearRect(w-1,0,1,2);

            g2.dispose();
        }
    };
    private JPanel contentPanel = new JPanel(new BorderLayout());

    private static JButton makeCloseButton() {
        JButton button = new JButton(new CloseIcon());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(true);
        button.setBackground(Color.ORANGE);
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JComponent b = (JComponent)e.getSource();
                Window w = SwingUtilities.getWindowAncestor(b);
                if(w!=null) {
                    w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
                }
            }
        });
        return button;
    }

//     private static JButton makeIconifyButton() {
//         JButton iconify = new JButton("_");
//         iconify.setContentAreaFilled(false);
//         iconify.setFocusPainted(false);
//         iconify.setBorder(BorderFactory.createEmptyBorder());
//         iconify.setOpaque(true);
//         iconify.setBackground(Color.ORANGE);
//         iconify.addActionListener(new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 frame.setExtendedState(state | Frame.ICONIFIED);
//             }
//         });
//     }

    public JFrame makeFrame(String str) {
        final JFrame frame = new JFrame(str) {
            @Override public Container getContentPane() {
                return contentPanel;
            }
        };
        frame.setUndecorated(true);
        frame.setBackground(new Color(255,255,255,0));

        JPanel title = new JPanel(new BorderLayout());
        DragWindowListener dwl = new DragWindowListener();
        title.addMouseListener(dwl);
        title.addMouseMotionListener(dwl);
        title.setOpaque(false);
        //title.setBackground(Color.ORANGE);
        title.setBorder(BorderFactory.createEmptyBorder(W,W,W,W));

        title.add(new JLabel(str, JLabel.CENTER));
        title.add(makeCloseButton(), BorderLayout.EAST);
        //title.add(iconify, BorderLayout.WEST);

        ResizeWindowListener rwl = new ResizeWindowListener(frame);
        for(SideLabel l:Arrays.asList(
            left       = new SideLabel(Side.W),  right       = new SideLabel(Side.E),
            top        = new SideLabel(Side.N),  bottom      = new SideLabel(Side.S),
            topleft    = new SideLabel(Side.NW), topright    = new SideLabel(Side.NE),
            bottomleft = new SideLabel(Side.SW), bottomright = new SideLabel(Side.SE))) {
            l.addMouseListener(rwl);
            l.addMouseMotionListener(rwl);
        }

        JPanel titlePanel = new JPanel(new BorderLayout(0,0));
        titlePanel.add(top,           BorderLayout.NORTH);
        titlePanel.add(title,         BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout(0,0));
        northPanel.add(topleft,       BorderLayout.WEST);
        northPanel.add(titlePanel,    BorderLayout.CENTER);
        northPanel.add(topright,      BorderLayout.EAST);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(bottomleft,    BorderLayout.WEST);
        southPanel.add(bottom,        BorderLayout.CENTER);
        southPanel.add(bottomright,   BorderLayout.EAST);

        resizePanel.add(left,         BorderLayout.WEST);
        resizePanel.add(right,        BorderLayout.EAST);
        resizePanel.add(northPanel,   BorderLayout.NORTH);
        resizePanel.add(southPanel,   BorderLayout.SOUTH);
        resizePanel.add(contentPanel, BorderLayout.CENTER);

        titlePanel.setOpaque(false);
        northPanel.setOpaque(false);
        southPanel.setOpaque(false);

        contentPanel.setOpaque(false);
        resizePanel.setOpaque(false);
        frame.setContentPane(resizePanel);
        return frame;
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
        MainPanel p = new MainPanel();
        JFrame frame = p.makeFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p);
        frame.setMinimumSize(new Dimension(100, 100));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum Side {
    N (Cursor.N_RESIZE_CURSOR,  new Dimension(0, 4)),
    W (Cursor.W_RESIZE_CURSOR,  new Dimension(4, 0)),
    E (Cursor.E_RESIZE_CURSOR,  new Dimension(4, 0)),
    S (Cursor.S_RESIZE_CURSOR,  new Dimension(0, 4)),
    NW(Cursor.NW_RESIZE_CURSOR, new Dimension(4, 4)),
    NE(Cursor.NE_RESIZE_CURSOR, new Dimension(4, 4)),
    SW(Cursor.SW_RESIZE_CURSOR, new Dimension(4, 4)),
    SE(Cursor.SE_RESIZE_CURSOR, new Dimension(4, 4));
    public final Dimension dim;
    public final int cursor;
    private Side(int cursor, Dimension dim) {
        this.cursor = cursor;
        this.dim = dim;
    }
}

class SideLabel extends JLabel {
    public final Side side;
    public SideLabel(Side side) {
        super();
        this.side = side;
        setCursor(Cursor.getPredefinedCursor(side.cursor));
    }
    @Override public Dimension getPreferredSize() {
        return side.dim;
    }
    @Override public Dimension getMinimumSize() {
        return side.dim;
    }
    @Override public Dimension getMaximumSize() {
        return side.dim;
    }
}

class ResizeWindowListener extends MouseAdapter {
    private final JFrame frame;
    private Rectangle rect;
    public ResizeWindowListener(JFrame frame) {
        this.frame = frame;
        this.rect  = frame.getBounds();
    }
    @Override public void mousePressed(MouseEvent e) {
        rect = frame.getBounds();
    }
    @Override public void mouseDragged(MouseEvent e) {
        if(rect==null) { return; }
        int dx = e.getX(), dy = e.getY();
        switch(((SideLabel)e.getComponent()).side) {
          case NW:
            rect.y      += dy;
            rect.height -= dy;
            rect.x      += dx;
            rect.width  -= dx;
            break;
          case N:
            rect.y      += dy;
            rect.height -= dy;
            break;
          case NE:
            rect.y      += dy;
            rect.height -= dy;
            rect.width  += dx;
            break;
          case W:
            rect.x      += dx;
            rect.width  -= dx;
            break;
          case E:
            rect.width  += dx;
            break;
          case SW:
            rect.height += dy;
            rect.x      += dx;
            rect.width  -= dx;
            break;
          case S:
            rect.height += dy;
            break;
          case SE:
            rect.height += dy;
            rect.width  += dx;
            break;
        }
        frame.setBounds(rect);
    }
}

class DragWindowListener extends MouseAdapter {
    private MouseEvent start;
    private Window window;
    @Override public void mousePressed(MouseEvent me) {
        if(window==null) {
            Object o = me.getSource();
            if(o instanceof Window) {
                window = (Window)o;
            }else if(o instanceof JComponent) {
                window = SwingUtilities.windowForComponent(me.getComponent());
            }
        }
        start = me;
    }
    @Override public void mouseDragged(MouseEvent me) {
        if(window!=null) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            window.setLocation(eventLocationOnScreen.x - start.getX(),
                               eventLocationOnScreen.y - start.getY());
        }
    }
}

class CloseIcon implements Icon {
    private int width;
    private int height;
    public CloseIcon() {
        width  = 16;
        height = 16;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(Color.BLACK);
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
