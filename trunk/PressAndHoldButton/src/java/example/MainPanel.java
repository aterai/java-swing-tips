package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

class MainPanel extends JPanel {
    private final JToolBar toolbar = new JToolBar("toolbar");
    private final URL url = getClass().getResource("ei0021-16.png");
    public MainPanel() {
        super(new BorderLayout());
        toolbar.add(new PressAndHoldButton(new ImageIcon(url), makeIconList()));
        add(toolbar, BorderLayout.NORTH);
        add(new JLabel("press and hold the button for 1000 milliseconds"));
        setPreferredSize(new Dimension(320, 240));
    }
    private List<MenuContext> makeIconList() {
        return Arrays.asList(
            new MenuContext("BLUE",    Color.BLUE),
            new MenuContext("CYAN",    Color.CYAN),
            new MenuContext("GREEN",   Color.GREEN),
            new MenuContext("MAGENTA", Color.MAGENTA),
            new MenuContext("ORANGE",  Color.ORANGE),
            new MenuContext("PINK",    Color.PINK),
            new MenuContext("RED",     Color.RED),
            new MenuContext("YELLOW",  Color.YELLOW));
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

class PressAndHoldButton extends JToggleButton {
    private static final Icon i = new MenuArrowIcon();
    private final ButtonGroup bg = new ButtonGroup();
    private final JPopupMenu pop = new JPopupMenu();
//     public PressAndHoldButton() {
//         this("", null);
//     }
//     public PressAndHoldButton(Icon icon) {
//         this("", icon);
//     }
//     public PressAndHoldButton(String text) {
//         this(text, null);
//     }
//     public PressAndHoldButton(String text, Icon icon) {
    //private final List<MenuContext> iconList;
    public PressAndHoldButton(Icon icon, List<MenuContext> iconList) {
        super();
        //this.iconList = iconList;
        pop.setLayout(new GridLayout(0,3));
        for(MenuContext m: iconList) {
            AbstractButton b = new JRadioButton();
            b.setBorder(BorderFactory.createEmptyBorder());
            //b.setAction(m.action);
            b.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    System.out.println(bg.getSelection().getActionCommand());
                    pop.setVisible(false);
                }
            });
            b.setIcon(m.small);
            b.setRolloverIcon(m.rollover);
            b.setSelectedIcon(m.rollover);
            b.setActionCommand(m.command);
            b.setFocusable(false);
            b.setPreferredSize(new Dimension(m.small.getIconWidth(), m.small.getIconHeight()));
            pop.add(b);
            bg.add(b);
            b.setSelected(true);
        }
        ArrowButtonHandler handler = new ArrowButtonHandler(pop);
        //handler.putValue(Action.NAME, text);
        handler.putValue(Action.SMALL_ICON, icon);
        setAction(handler);
        addMouseListener(handler);
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4+i.getIconWidth()));
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dim = getSize();
        Insets ins = getInsets();
        int x = dim.width-ins.right;
        int y = ins.top+(dim.height-ins.top-ins.bottom-i.getIconHeight())/2;
        i.paintIcon(this, g, x, y);
    }
}

class MenuContext {
    public final String command;
    public final Icon small;
    public final Icon rollover;
    public MenuContext(String cmd, Color c) {
        command = cmd;
        small = new DummyIcon(c);
        rollover = new DummyIcon2(c);
    }
}

class ArrowButtonHandler extends AbstractAction implements MouseListener {
    private final Timer autoRepeatTimer;
    private final JPopupMenu pop;
    private AbstractButton arrowButton = null;
    public ArrowButtonHandler(JPopupMenu popup) {
        super();
        this.pop = popup;
        autoRepeatTimer = new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("InitialDelay(1000)");
                if(arrowButton!=null && arrowButton.getModel().isPressed() && autoRepeatTimer.isRunning()) {
                    autoRepeatTimer.stop();
                    pop.show(arrowButton, 0, arrowButton.getHeight());
                    pop.requestFocusInWindow();
                }
            }
        });
        autoRepeatTimer.setInitialDelay(1000);
        pop.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if(arrowButton!=null) {
                    arrowButton.setSelected(false);
                }
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        if(autoRepeatTimer.isRunning()) {
            System.out.println("actionPerformed");
            //System.out.println("    "+bg.getSelection().getActionCommand());
            if(arrowButton!=null) {
                arrowButton.setSelected(false);
            }
            autoRepeatTimer.stop();
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        System.out.println("mousePressed");
        if(SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
            arrowButton = (AbstractButton)e.getSource();
            autoRepeatTimer.start();
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        autoRepeatTimer.stop();
    }
    @Override public void mouseExited(MouseEvent e) {
        if(autoRepeatTimer.isRunning()) {
            autoRepeatTimer.stop();
        }
    }
    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) { /* not needed */ }
}

class MenuArrowIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(Color.BLACK);
        g2.translate(x,y);
        g2.drawLine( 2, 3, 6, 3 );
        g2.drawLine( 3, 4, 5, 4 );
        g2.drawLine( 4, 5, 4, 5 );
        g2.translate(-x,-y);
    }
    @Override public int getIconWidth()  { return 9; }
    @Override public int getIconHeight() { return 9; }
}

class DummyIcon implements Icon {
    private final Color color;
    public DummyIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(color);
        g2.translate(x,y);
        g2.fillOval( 4, 4, 16, 16 );
        g2.translate(-x,-y);
    }
    @Override public int getIconWidth()  {
        return 24;
    }
    @Override public int getIconHeight() {
        return 24;
    }
}

class DummyIcon2 extends DummyIcon {
    public DummyIcon2(Color color) {
        super(color);
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        super.paintIcon(c,g,x,y);
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(Color.BLACK);
        g2.translate(x,y);
        g2.drawOval( 4, 4, 16, 16 );
        g2.translate(-x,-y);
    }
}
