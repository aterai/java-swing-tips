package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected boolean isPressed;
    protected final JLabel label = new JLabel();
    protected final JScrollPane scroll = new JScrollPane(label);
    protected final JScrollBar vBar = scroll.getVerticalScrollBar();
    protected final JScrollBar hBar = scroll.getHorizontalScrollBar();
    protected final JScrollBar vsb = new JScrollBar(Adjustable.VERTICAL) {
        @Override public boolean isVisible() {
            if (isPressed) {
                return false;
            } else {
                return super.isVisible();
            }
        }
        @Override public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            return new Dimension(0, dim.height);
        }
    };
    protected final JScrollBar hsb = new JScrollBar(Adjustable.HORIZONTAL) {
        @Override public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            return new Dimension(dim.width, 0);
        }
    };
    protected final JRadioButton r0 = new JRadioButton("PreferredSize: 0, shift pressed: Horizontal WheelScrolling", true);
    protected final JRadioButton r1 = new JRadioButton("SCROLLBAR_ALWAYS");
    protected final JRadioButton r2 = new JRadioButton("SCROLLBAR_NEVER");

    public MainPanel() {
        super(new BorderLayout());

        label.setIcon(new ImageIcon(MainPanel.class.getResource("CRW_3857_JFR.jpg"))); //http://sozai-free.com/
        MouseAdapter ml = new DragScrollListener();
        label.addMouseMotionListener(ml);
        label.addMouseListener(ml);
        for (JScrollBar sb: Arrays.asList(vsb, hsb, vBar, hBar)) {
            sb.setUnitIncrement(25);
        }

        InputMap im  = scroll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = scroll.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK, false), "pressed");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "released");
        am.put("pressed", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                isPressed = true;
            }
        });
        am.put("released", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                isPressed = false;
            }
        });

        ActionListener al = e -> {
            if (r2.isSelected()) {
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            } else {
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                scroll.setVerticalScrollBar(r0.isSelected() ? vsb : vBar);
                scroll.setHorizontalScrollBar(r0.isSelected() ? hsb : hBar);
            }
        };
        ButtonGroup bg = new ButtonGroup();
        for (AbstractButton b: Arrays.asList(r0, r1, r2)) {
            b.addActionListener(al);
            bg.add(b);
        }

        Box b = Box.createHorizontalBox();
        JPanel p = new JPanel(new GridLayout(2, 1));
        b.add(r1);
        b.add(r2);
        p.add(r0);
        p.add(b);

        scroll.setVerticalScrollBar(vsb);
        scroll.setHorizontalScrollBar(hsb);

        //JScrollBar vsb = scroll.getVerticalScrollBar();
        //vsb.setPreferredSize(new Dimension(0, vsb.getPreferredSize().height));
        //vsb.putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
        //JScrollBar hsb = scroll.getHorizontalScrollBar();
        //hsb.setPreferredSize(new Dimension(hsb.getPreferredSize().width, 0));

        add(p, BorderLayout.NORTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
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

class DragScrollListener extends MouseAdapter {
    protected final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    protected final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    protected final Point pp = new Point();
    @Override public void mouseDragged(MouseEvent e) {
        Component c = e.getComponent();
        Container p = SwingUtilities.getUnwrappedParent(c);
        if (p instanceof JViewport) {
            JViewport vport = (JViewport) p;
            Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
            Point vp = vport.getViewPosition();
            vp.translate(pp.x - cp.x, pp.y - cp.y);
            ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(hndCursor);
        Container p = SwingUtilities.getUnwrappedParent(c);
        if (p instanceof JViewport) {
            JViewport vport = (JViewport) p;
            Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
            pp.setLocation(cp);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        e.getComponent().setCursor(defCursor);
    }
}
