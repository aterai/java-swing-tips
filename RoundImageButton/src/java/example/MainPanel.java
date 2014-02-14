package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
//import javax.swing.plaf.basic.*;

class MainPanel extends JPanel {
    private final JComboBox<? extends Enum> alignmentsChoices = new JComboBox<>(ButtonAlignments.values());
    private final List<? extends JButton> buttons;
    private final Box box = Box.createHorizontalBox();
      // JDK 5
      //new Box(BoxLayout.X_AXIS) {
      //    @Override protected void paintComponent(Graphics g) {
      //        if(ui != null) {
      //            super.paintComponent(g);
      //        }else if(isOpaque()) {
      //            g.setColor(getBackground());
      //            g.fillRect(0, 0, getWidth(), getHeight());
      //        }
      //    }
      //};
    public MainPanel() {
        super(new BorderLayout());
        buttons = Arrays.asList(
            new RoundButton(new ImageIcon(getClass().getResource("005.png")), "005d.png", "005g.png"),
            new RoundButton(new ImageIcon(getClass().getResource("003.png")), "003d.png", "003g.png"),
            new RoundButton(new ImageIcon(getClass().getResource("001.png")), "001d.png", "001g.png"),
            new RoundButton(new ImageIcon(getClass().getResource("002.png")), "002d.png", "002g.png"),
            new RoundButton(new ImageIcon(getClass().getResource("004.png")), "004d.png", "004g.png"));
        //TEST: buttons = makeButtonArray2(getClass()); //Set ButtonUI

        box.setOpaque(true);
        box.setBackground(new Color(120,120,160));
        box.add(Box.createHorizontalGlue());
        box.setBorder(BorderFactory.createEmptyBorder(60,10,60,10));
        for(JButton b: buttons) {
            box.add(b);
            box.add(Box.createHorizontalStrut(5));
        }
        box.add(Box.createHorizontalGlue());
        add(box, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.add(new JCheckBox(new AbstractAction("ButtonBorder Color") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                Color bgc = cb.isSelected() ? Color.WHITE : Color.BLACK;
                for(JButton b: buttons) {
                    b.setBackground(bgc);
                }
                box.repaint();
            }
        }));
        alignmentsChoices.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    ButtonAlignments ba = (ButtonAlignments)alignmentsChoices.getSelectedItem();
                    for(JButton b: buttons) {
                        b.setAlignmentY(ba.alingment);
                    }
                    box.revalidate();
                }
            }
        });
        alignmentsChoices.setSelectedIndex(1);
        p.add(alignmentsChoices);
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

//     //TEST:
//     public static List<JButton> makeButtonArray2(final Class clazz) {
//         return Arrays.asList(
//             new JButton(new ImageIcon(clazz.getResource("005.png"))) {{
//                 setPressedIcon(new ImageIcon(clazz.getResource("005d.png")));
//                 setRolloverIcon(new ImageIcon(clazz.getResource("005g.png")));
//                 setUI(new RoundImageButtonUI());
//             }},
//             new JButton(new ImageIcon(clazz.getResource("003.png"))) {{
//                 setPressedIcon(new ImageIcon(clazz.getResource("003d.png")));
//                 setRolloverIcon(new ImageIcon(clazz.getResource("003g.png")));
//                 setUI(new RoundImageButtonUI());
//             }},
//             new JButton(new ImageIcon(clazz.getResource("001.png"))) {{
//                 setPressedIcon(new ImageIcon(clazz.getResource("001d.png")));
//                 setRolloverIcon(new ImageIcon(clazz.getResource("001g.png")));
//                 setUI(new RoundImageButtonUI());
//             }},
//             new JButton(new ImageIcon(clazz.getResource("002.png"))) {{
//                 setPressedIcon(new ImageIcon(clazz.getResource("002d.png")));
//                 setRolloverIcon(new ImageIcon(clazz.getResource("002g.png")));
//                 setUI(new RoundImageButtonUI());
//             }},
//             new JButton(new ImageIcon(clazz.getResource("004.png"))) {{
//                 setPressedIcon(new ImageIcon(clazz.getResource("004d.png")));
//                 setRolloverIcon(new ImageIcon(clazz.getResource("004g.png")));
//                 setUI(new RoundImageButtonUI());
//             }});
//     }

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

class RoundButton extends JButton {
    protected Shape shape, base;
//     public RoundButton() {
//         super();
//     }
//     public RoundButton(Icon icon) {
//         super(icon);
//     }
//     public RoundButton(String text) {
//         super(text);
//     }
//     public RoundButton(Action a) {
//         super(a);
//         //setAction(a);
//     }
//     public RoundButton(String text, Icon icon) {
//         super(text, icon);
//         //setModel(new DefaultButtonModel());
//         //init(text, icon);
//     }
    public RoundButton(Icon icon, String i2, String i3) {
        super(icon);
        setPressedIcon(new ImageIcon(getClass().getResource(i2)));
        setRolloverIcon(new ImageIcon(getClass().getResource(i3)));
    }
    @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        setBackground(Color.BLACK);
        setContentAreaFilled(false);
        setFocusPainted(false);
        //setVerticalAlignment(SwingConstants.TOP);
        setAlignmentY(Component.TOP_ALIGNMENT);
        initShape();
    }
    @Override public Dimension getPreferredSize() {
        Icon icon = getIcon();
        Insets i = getInsets();
        int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
        return new Dimension(iw+i.right+i.left, iw+i.top+i.bottom);
    }
    protected void initShape() {
        if(!getBounds().equals(base)) {
            Dimension s = getPreferredSize();
            base = getBounds();
            shape = new Ellipse2D.Float(0, 0, s.width-1, s.height-1);
        }
    }
    @Override protected void paintBorder(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        //g2.setStroke(new BasicStroke(1.0f));
        g2.draw(shape);
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.dispose();
    }
    @Override public boolean contains(int x, int y) {
        initShape();
        return shape==null ? false : shape.contains(x, y);
    }
}

// class RoundImageButtonUI extends BasicButtonUI{
//     protected Shape shape, base;
//     @Override protected void installDefaults(AbstractButton b) {
//         super.installDefaults(b);
//         clearTextShiftOffset();
//         defaultTextShiftOffset = 0;
//         Icon icon = b.getIcon();
//         if(icon==null) { return; }
//         b.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
//         b.setContentAreaFilled(false);
//         b.setFocusPainted(false);
//         b.setOpaque(false);
//         b.setBackground(Color.BLACK);
//         //b.setVerticalAlignment(SwingConstants.TOP);
//         b.setAlignmentY(Component.TOP_ALIGNMENT);
//         initShape(b);
//     }
//     @Override protected void installListeners(AbstractButton b) {
//         BasicButtonListener listener = new BasicButtonListener(b) {
//             @Override public void mousePressed(MouseEvent e) {
//                 AbstractButton b = (AbstractButton) e.getSource();
//                 initShape(b);
//                 if(shape.contains(e.getX(), e.getY())) {
//                     super.mousePressed(e);
//                 }
//             }
//             @Override public void mouseEntered(MouseEvent e) {
//                 if(shape.contains(e.getX(), e.getY())) {
//                     super.mouseEntered(e);
//                 }
//             }
//             @Override public void mouseMoved(MouseEvent e) {
//                 if(shape.contains(e.getX(), e.getY())) {
//                     super.mouseEntered(e);
//                 }else{
//                     super.mouseExited(e);
//                 }
//             }
//         };
//         if(listener != null) {
//             b.addMouseListener(listener);
//             b.addMouseMotionListener(listener);
//             b.addFocusListener(listener);
//             b.addPropertyChangeListener(listener);
//             b.addChangeListener(listener);
//         }
//     }
//     @Override public void paint(Graphics g, JComponent c) {
//         super.paint(g, c);
//         Graphics2D g2 = (Graphics2D)g.create();
//         initShape(c);
//         //Border
//         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g2.setColor(c.getBackground());
//         //g2.setStroke(new BasicStroke(1.0f));
//         g2.draw(shape);
//         //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//         g2.dispose();
//     }
//     @Override public Dimension getPreferredSize(JComponent c) {
//         JButton b = (JButton)c;
//         Icon icon = b.getIcon();
//         Insets i = b.getInsets();
//         int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
//         return new Dimension(iw+i.right+i.left, iw+i.top+i.bottom);
//     }
//     private void initShape(JComponent c) {
//         if(!c.getBounds().equals(base)) {
//             Dimension s = c.getPreferredSize();
//             base = c.getBounds();
//             shape = new Ellipse2D.Float(0, 0, s.width-1, s.height-1);
//         }
//     }
// }

enum ButtonAlignments {
    Top    ("Top Alignment", Component.TOP_ALIGNMENT),
    Center ("Center Alignment", Component.CENTER_ALIGNMENT),
    Bottom ("Bottom Alignment", Component.BOTTOM_ALIGNMENT);
    private final String description;
    public final float alingment;
    private ButtonAlignments(String description, float alingment) {
        this.description = description;
        this.alingment   = alingment;
    }
    @Override public String toString() {
        return description;
    }
}
