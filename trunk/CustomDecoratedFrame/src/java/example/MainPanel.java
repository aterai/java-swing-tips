package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final int W = 4;
    private static final Color BORDER_COLOR = new Color(100, 100, 100);
    private final SideLabel left        = new SideLabel(Side.W);
    private final SideLabel right       = new SideLabel(Side.E);
    private final SideLabel top         = new SideLabel(Side.N);
    private final SideLabel bottom      = new SideLabel(Side.S);
    private final SideLabel topleft     = new SideLabel(Side.NW);
    private final SideLabel topright    = new SideLabel(Side.NE);
    private final SideLabel bottomleft  = new SideLabel(Side.SW);
    private final SideLabel bottomright = new SideLabel(Side.SE);
    private final JPanel contentPanel   = new JPanel(new BorderLayout());
    private final JPanel resizePanel    = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(Color.ORANGE);
            g2.fillRect(0, 0, w, h);
            g2.setPaint(BORDER_COLOR);
            g2.drawRect(0, 0, w - 1, h - 1);

            g2.drawLine(0, 2, 2, 0);
            g2.drawLine(w - 3, 0, w - 1, 2);

            g2.clearRect(0, 0, 2, 1);
            g2.clearRect(0, 0, 1, 2);
            g2.clearRect(w - 2, 0, 2, 1);
            g2.clearRect(w - 1, 0, 1, 2);

            g2.dispose();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JButton makeCloseButton() {
        JButton button = new JButton(new CloseIcon());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(true);
        button.setBackground(Color.ORANGE);
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JComponent b = (JComponent) e.getSource();
                Window w = SwingUtilities.getWindowAncestor(b);
                if (w != null) {
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
        frame.setBackground(new Color(255, 255, 255, 0));

        JPanel title = new JPanel(new BorderLayout());
        DragWindowListener dwl = new DragWindowListener();
        title.addMouseListener(dwl);
        title.addMouseMotionListener(dwl);
        title.setOpaque(false);
        //title.setBackground(Color.ORANGE);
        title.setBorder(BorderFactory.createEmptyBorder(W, W, W, W));

        title.add(new JLabel(str, JLabel.CENTER));
        title.add(makeCloseButton(), BorderLayout.EAST);
        //title.add(iconify, BorderLayout.WEST);

        ResizeWindowListener rwl = new ResizeWindowListener(frame);
        for (SideLabel l:Arrays.asList(left, right, top, bottom, topleft, topright, bottomleft, bottomright)) {
            l.addMouseListener(rwl);
            l.addMouseMotionListener(rwl);
        }

        JPanel titlePanel = new JPanel(new BorderLayout(0, 0));
        titlePanel.add(top,           BorderLayout.NORTH);
        titlePanel.add(title,         BorderLayout.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout(0, 0));
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
        super();
        this.frame = frame;
        this.rect  = frame.getBounds();
    }
    @Override public void mousePressed(MouseEvent e) {
        rect = frame.getBounds();
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (rect == null) {
            return;
        }
        Side side = ((SideLabel) e.getComponent()).side;
        frame.setBounds(getResizedRect(rect, side, e.getX(), e.getY()));
    }
    private static Rectangle getResizedRect(Rectangle r, Side side, int dx, int dy) {
        switch(side) {
          case NW:
            r.y      += dy;
            r.height -= dy;
            r.x      += dx;
            r.width  -= dx;
            break;
          case N:
            r.y      += dy;
            r.height -= dy;
            break;
          case NE:
            r.y      += dy;
            r.height -= dy;
            r.width  += dx;
            break;
          case W:
            r.x      += dx;
            r.width  -= dx;
            break;
          case E:
            r.width  += dx;
            break;
          case SW:
            r.height += dy;
            r.x      += dx;
            r.width  -= dx;
            break;
          case S:
            r.height += dy;
            break;
          case SE:
            r.height += dy;
            r.width  += dx;
            break;
          default: throw new AssertionError("Unknown SideLabel");
        }
        return r;
    }
}

class DragWindowListener extends MouseAdapter {
    private final transient Point startPt = new Point();
    private transient Window window;
    @Override public void mousePressed(MouseEvent me) {
        if (window == null) {
            Object o = me.getSource();
            if (o instanceof Window) {
                window = (Window) o;
            } else if (o instanceof JComponent) {
                window = SwingUtilities.windowForComponent(me.getComponent());
            }
        }
        startPt.setLocation(me.getPoint());
    }
    @Override public void mouseDragged(MouseEvent me) {
        if (window != null) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            window.setLocation(eventLocationOnScreen.x - startPt.x,
                               eventLocationOnScreen.y - startPt.y);
        }
    }
}

class CloseIcon implements Icon {
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
        return 16;
    }
    @Override public int getIconHeight() {
        return 16;
    }
}
