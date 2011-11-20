package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1, 5, 5));
        final JTree tree = new JTree();
        final JCheckBox c = new JCheckBox("setEnabled", true);
        c.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tree.setEnabled(c.isSelected());
            }
        });
        c.setFocusPainted(false);
        JScrollPane l1 = new JScrollPane(tree);
        //l1.setBorder(new ComponentTitledBorder(c, l1, BorderFactory.createEtchedBorder()));

        JLabel icon = new JLabel(new ImageIcon(getClass().getResource("16x16.png")));
        icon.setOpaque(true);
        JLabel l2 = new JLabel("<html>aaaaaaaaaaaaaaaa<br>bbbbbbbbbbbbbbbbb");
        //l2.setBorder(new ComponentTitledBorder(icon, l2, BorderFactory.createEtchedBorder()));

        final JTextArea l3 = new JTextArea("aaaaaaaaaa"); //JSplitPane(); //Label("ccccccccccccccc");
        JButton b = new JButton(new AbstractAction("Clear") {
            @Override public void actionPerformed(ActionEvent e) {
                l3.setText("");
            }
        });
        b.setFocusPainted(false);
        b.setFocusable(false);
        //l3.setBorder(new ComponentTitledBorder(b, l3, BorderFactory.createEtchedBorder()));

        add(makePanel(l1, c));
        //add(makePanel(l2, icon));
        add(makePanel(l3, b));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    public JComponent makePanel(JComponent l1, JComponent c) {
        Border ib = BorderFactory.createEmptyBorder(0,0,10,0);
        Border eb = BorderFactory.createEtchedBorder();
        Border ob = BorderFactory.createEmptyBorder(0,0,10,0);

        Border bo = BorderFactory.createCompoundBorder(eb, ib);
        l1.setBorder(BorderFactory.createCompoundBorder(ob, bo));

        SpringLayout layout = new SpringLayout();
        JLayeredPane p = new JLayeredPane();
        p.setLayout(layout);

        Spring x     = layout.getConstraint(SpringLayout.WIDTH, p);
        Spring y     = layout.getConstraint(SpringLayout.HEIGHT, p);
        Spring g     = Spring.minus(Spring.constant(20));
        Spring width = Spring.constant(c.getPreferredSize().width);

        SpringLayout.Constraints constraints = layout.getConstraints(c);
        constraints.setConstraint(SpringLayout.EAST,  Spring.sum(x, g));
        constraints.setConstraint(SpringLayout.SOUTH, y);
        p.setLayer(c, 1);
        p.add(c);

        constraints = layout.getConstraints(l1);
        constraints.setConstraint(SpringLayout.WEST,  Spring.constant(0));
        constraints.setConstraint(SpringLayout.NORTH, Spring.constant(0));
        constraints.setConstraint(SpringLayout.EAST,  x);
        constraints.setConstraint(SpringLayout.SOUTH, y);
        p.setLayer(l1, 0);
        p.add(l1);

        return p;
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

// class ComponentTitledBorder implements Border, MouseListener, MouseMotionListener, SwingConstants {
//     private static final int offset = 5;
//     private final Component comp;
//     private final JComponent container;
//     private final Border border;
//
//     public ComponentTitledBorder(Component comp, JComponent container, Border border) {
//         this.comp      = comp;
//         this.container = container;
//         this.border    = border;
//         if(comp instanceof JComponent) {
//             ((JComponent)comp).setOpaque(true);
//         }
//         container.addMouseListener(this);
//         container.addMouseMotionListener(this);
//     }
//
//     @Override public boolean isBorderOpaque() {
//         return true;
//     }
//
//     @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//         Insets borderInsets = border.getBorderInsets(c);
//         Insets insets = getBorderInsets(c);
//         int temp = (insets.top-borderInsets.top)/2;
//         border.paintBorder(c, g, x, y+temp, width, height-temp);
//         Dimension size = comp.getPreferredSize();
//         Rectangle rect = new Rectangle(offset, 0, size.width, size.height);
//         SwingUtilities.paintComponent(g, comp, (Container)c, rect);
//         comp.setBounds(rect);
//     }
//
//     @Override public Insets getBorderInsets(Component c) {
//         Dimension size = comp.getPreferredSize();
//         Insets insets = border.getBorderInsets(c);
//         insets.top = Math.max(insets.top, size.height);
//         return insets;
//     }
//
//     private void dispatchEvent(MouseEvent me) {
//         Component src = me.getComponent();
//         comp.dispatchEvent(SwingUtilities.convertMouseEvent(src, me, comp));
//         src.repaint();
//     }
//     @Override public void mouseClicked(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mouseEntered(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mouseExited(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mousePressed(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mouseReleased(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mouseMoved(MouseEvent me) {
//         dispatchEvent(me);
//     }
//     @Override public void mouseDragged(MouseEvent me) {
//         dispatchEvent(me);
//     }
// }
