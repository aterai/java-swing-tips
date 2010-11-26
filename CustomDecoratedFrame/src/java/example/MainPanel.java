package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
//import javax.swing.event.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static final int W = 4;
    private static final Color borderColor = new Color(100,100,100);
    public enum Side {
        NW_SIDE, N_SIDE, NE_SIDE, L_SIDE, R_SIDE, SW_SIDE, S_SIDE, SE_SIDE;
    }
    private JLabel left, right, top, bottom, topleft, topright, bottomleft, bottomright;
    private JPanel resizePanel  = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            //super.paintComponent(g);
            //Graphics2D g2 = (Graphics2D)g;
            Graphics2D g2 = (Graphics2D)g.create();
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(Color.ORANGE);
            g2.fillRect(0,0,w,h);
            g2.setPaint(borderColor); //g2.setPaint(Color.RED);
            g2.drawRect(0,0,w-1,h-1);

            //g2.setPaint(Color.WHITE);
            //g2.setPaint(new Color(0,0,0,0));
//             g2.drawLine(0,0,0,0);
//             g2.drawLine(w-1,0,w-1,0);
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
    //private int state = 0;
    public JFrame makeFrame(String str) {
        final JFrame frame = new JFrame(str) {
            @Override public Container getContentPane() {
                return contentPanel;
            }
        };
        frame.setUndecorated(true);
        frame.setBackground(new Color(255,255,255,0));

        JButton button = new JButton(new CloseIcon());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(true);
        button.setBackground(Color.ORANGE);
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.getToolkit().getSystemEventQueue().postEvent(
                    new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
//         //javax/swing/plaf/metal/MetalTitlePane.java
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

        JPanel title = new JPanel(new BorderLayout());
        DragWindowListener dwl = new DragWindowListener();
        title.addMouseListener(dwl);
        title.addMouseMotionListener(dwl);
        title.setOpaque(false);
        //title.setBackground(Color.ORANGE);
        title.setBorder(BorderFactory.createEmptyBorder(W,W,W,W));

        title.add(new JLabel(str, JLabel.CENTER));
        title.add(button, BorderLayout.EAST);
        //title.add(iconify, BorderLayout.WEST);

        ResizeWindowListener rwl = new ResizeWindowListener(frame);
        for(JLabel l:java.util.Arrays.asList(
            left         = new JLabel(), right        = new JLabel(),
            top          = new JLabel(), bottom       = new JLabel(),
            topleft      = new JLabel(), topright     = new JLabel(),
            bottomleft   = new JLabel(), bottomright  = new JLabel())) {
            l.addMouseListener(rwl);
            l.addMouseMotionListener(rwl);
            //l.setOpaque(true);
            //l.setBackground(Color.RED);
        }

//         //top.setBorder(BorderFactory.createMatteBorder(1,0,0,0,borderColor));
//         left.setBorder(BorderFactory.createMatteBorder(0,1,0,0,borderColor));
//         //bottom.setBorder(BorderFactory.createMatteBorder(0,0,1,0,borderColor));
//         right.setBorder(BorderFactory.createMatteBorder(0,0,0,1,borderColor));

        //topleft.setBorder(BorderFactory.createMatteBorder(1,1,0,0,borderColor));
        //bottomleft.setBorder(BorderFactory.createMatteBorder(0,1,1,0,borderColor));
        //bottomright.setBorder(BorderFactory.createMatteBorder(0,0,1,1,borderColor));
        //topright.setBorder(BorderFactory.createMatteBorder(1,0,0,1,borderColor));

//         topleft.setBackground(Color.GREEN);
//         topright.setBackground(Color.GREEN);
//         bottomleft.setBackground(Color.GREEN);
//         bottomright.setBackground(Color.GREEN);

        Dimension d = new Dimension(W, 0);
        left.setPreferredSize(d);
        left.setMinimumSize(d);
        right.setPreferredSize(d);
        right.setMinimumSize(d);

        d = new Dimension(0, W);
        top.setPreferredSize(d);
        top.setMinimumSize(d);
        bottom.setPreferredSize(d);
        bottom.setMinimumSize(d);

        d = new Dimension(W, W);
        topleft.setPreferredSize(d);
        topleft.setMinimumSize(d);
        topright.setPreferredSize(d);
        topright.setMinimumSize(d);
        bottomleft.setPreferredSize(d);
        bottomleft.setMinimumSize(d);
        bottomright.setPreferredSize(d);
        bottomright.setMinimumSize(d);

        left.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        right.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        top.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        bottom.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        topleft.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        topright.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        bottomleft.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        bottomright.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));

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
    class ResizeWindowListener extends MouseAdapter {
        private Rectangle startSide = null;
        private final JFrame frame;
        public ResizeWindowListener(JFrame frame) {
            this.frame = frame;
        }
        @Override public void mousePressed(MouseEvent e) {
            startSide = frame.getBounds();
        }
        @Override public void mouseDragged(MouseEvent e) {
            if(startSide==null) return;
            Component c = e.getComponent();
            if(c==topleft) {
                startSide.y += e.getY();
                startSide.height -= e.getY();
                startSide.x += e.getX();
                startSide.width -= e.getX();
            }else if(c==top) {
                startSide.y += e.getY();
                startSide.height -= e.getY();
            }else if(c==topright) {
                startSide.y += e.getY();
                startSide.height -= e.getY();
                startSide.width += e.getX();
            }else if(c==left) {
                startSide.x += e.getX();
                startSide.width -= e.getX();
            }else if(c==right) {
                startSide.width += e.getX();
            }else if(c==bottomleft) {
                startSide.height += e.getY();
                startSide.x += e.getX();
                startSide.width -= e.getX();
            }else if(c==bottom) {
                startSide.height += e.getY();
            }else if(c==bottomright) {
                startSide.height += e.getY();
                startSide.width += e.getX();
            }
            frame.setBounds(startSide);
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
class DragWindowListener extends MouseAdapter {
    private MouseEvent start;
    //private Point  loc;
    private Window window;
    @Override public void mousePressed(MouseEvent me) {
        start = me;
    }
    @Override public void mouseDragged(MouseEvent me) {
        if(window==null) {
            window = SwingUtilities.windowForComponent(me.getComponent());
        }
        Point eventLocationOnScreen = me.getLocationOnScreen();
        window.setLocation(eventLocationOnScreen.x - start.getX(),
                           eventLocationOnScreen.y - start.getY());
        //loc = window.getLocation(loc);
        //int x = loc.x - start.getX() + me.getX();
        //int y = loc.y - start.getY() + me.getY();
        //window.setLocation(x, y);
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
