package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.add(new PressAndHoldButton(new ImageIcon(getClass().getResource("ei0021-16.png"))));
        add(toolbar, BorderLayout.NORTH);
        add(new JLabel("press and hold the button for 1000 milliseconds"));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class PressAndHoldButton extends JButton {
    private static final Icon ARROW_ICON = new MenuArrowIcon();
    private PressAndHoldHandler handler;
    protected PressAndHoldButton(Icon icon) {
        super(icon);
        // getAction().putValue(Action.NAME, text);
        getAction().putValue(Action.SMALL_ICON, icon);
    }
    @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        handler = new PressAndHoldHandler();
        SwingUtilities.updateComponentTreeUI(handler.pop);
        setAction(handler);
        addMouseListener(handler);
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4 + ARROW_ICON.getIconWidth()));
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dim = getSize();
        Insets ins = getInsets();
        int x = dim.width - ins.right;
        int y = ins.top + (dim.height - ins.top - ins.bottom - ARROW_ICON.getIconHeight()) / 2;
        ARROW_ICON.paintIcon(this, g, x, y);
    }
}

class PressAndHoldHandler extends AbstractAction implements MouseListener {
    public final JPopupMenu pop = new JPopupMenu();
    public final ButtonGroup bg = new ButtonGroup();
    private AbstractButton arrowButton;
    private final Timer holdTimer = new Timer(1000, null);
    protected PressAndHoldHandler() {
        super();
        holdTimer.setInitialDelay(1000);
        holdTimer.addActionListener(e -> {
            System.out.println("InitialDelay(1000)");
            if (Objects.nonNull(arrowButton) && arrowButton.getModel().isPressed() && holdTimer.isRunning()) {
                holdTimer.stop();
                pop.show(arrowButton, 0, arrowButton.getHeight());
                pop.requestFocusInWindow();
            }
        });
        pop.setLayout(new GridLayout(0, 3, 5, 5));
        makeMenuList().stream()
            .map(PressAndHoldHandler::makeMenuButton)
            .forEach(b -> {
                b.addActionListener(e -> {
                    System.out.println(bg.getSelection().getActionCommand());
                    pop.setVisible(false);
                });
                pop.add(b);
                bg.add(b);
            });
    }
    private static AbstractButton makeMenuButton(MenuContext m) {
        AbstractButton b = new JRadioButton(m.command);
        b.setActionCommand(m.command);
        b.setForeground(m.color);
        b.setBorder(BorderFactory.createEmptyBorder());
        // b.setIcon(m.small);
        // b.setRolloverIcon(m.rollover);
        // b.setSelectedIcon(m.rollover);
        // b.setFocusable(false);
        // b.setPreferredSize(new Dimension(m.small.getIconWidth(), m.small.getIconHeight()));
        return b;
    }
    private List<MenuContext> makeMenuList() {
        return Arrays.asList(
            new MenuContext("BLACK", Color.BLACK),
            new MenuContext("BLUE", Color.BLUE),
            new MenuContext("CYAN", Color.CYAN),
            new MenuContext("GREEN", Color.GREEN),
            new MenuContext("MAGENTA", Color.MAGENTA),
            new MenuContext("ORANGE", Color.ORANGE),
            new MenuContext("PINK", Color.PINK),
            new MenuContext("RED", Color.RED),
            new MenuContext("YELLOW", Color.YELLOW));
    }
    @Override public void actionPerformed(ActionEvent e) {
        System.out.println("actionPerformed");
        if (holdTimer.isRunning()) {
            ButtonModel model = bg.getSelection();
            if (Objects.nonNull(model)) {
                System.out.println(model.getActionCommand());
            }
            holdTimer.stop();
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        System.out.println("mousePressed");
        Component c = e.getComponent();
        if (SwingUtilities.isLeftMouseButton(e) && c.isEnabled()) {
            arrowButton = (AbstractButton) c;
            holdTimer.start();
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        holdTimer.stop();
    }
    @Override public void mouseExited(MouseEvent e) {
        if (holdTimer.isRunning()) {
            holdTimer.stop();
        }
    }
    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) { /* not needed */ }
}

class MenuContext {
    public final String command;
    public final Color color;
    // public final Icon small;
    // public final Icon rollover;
    protected MenuContext(String cmd, Color c) {
        command = cmd;
        color = c;
        // small = new ColorIcon(c);
        // rollover = new ColorIcon2(c);
    }
}

class MenuArrowIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Color.BLACK);
        g2.drawLine(2, 3, 6, 3);
        g2.drawLine(3, 4, 5, 4);
        g2.drawLine(4, 5, 4, 5);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 9;
    }
    @Override public int getIconHeight() {
        return 9;
    }
}

// class ColorIcon implements Icon {
//     private final Color color;
//     protected ColorIcon(Color color) {
//         this.color = color;
//     }
//     @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.translate(x, y);
//         g2.setPaint(color);
//         g2.fillOval(4, 4, 16, 16);
//         g2.dispose();
//     }
//     @Override public int getIconWidth() {
//         return 24;
//     }
//     @Override public int getIconHeight() {
//         return 24;
//     }
// }
//
// class ColorIcon2 extends ColorIcon {
//     protected ColorIcon2(Color color) {
//         super(color);
//     }
//     @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//         super.paintIcon(c, g, x, y);
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.translate(x, y);
//         g2.setPaint(Color.BLACK);
//         g2.drawOval(4, 4, 16, 16);
//         g2.dispose();
//     }
// }
