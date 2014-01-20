package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    private final JTextArea textArea = new JTextArea("aaaaaaaaaa");
    public MainPanel() {
        super(new GridLayout(2, 1, 5, 5));
        //textArea.setBorder(new ComponentTitledBorder(b, textArea, BorderFactory.createEtchedBorder()));

        JCheckBox c = new JCheckBox("setEnabled", true);
        c.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tree.setEnabled(((JCheckBox)e.getSource()).isSelected());
            }
        });

        JButton b = new JButton(new AbstractAction("Clear") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
        b.setFocusable(false);

        add(makePanel(new JScrollPane(tree), c));
        add(makePanel(new JScrollPane(textArea), b));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    public JComponent makePanel(JComponent m, JComponent c) {
        int ir = 20; //inset.right
        int ch = c.getPreferredSize().height/2;

//         Border ib = BorderFactory.createMatteBorder(0,0,ch,0,Color.WHITE);
//         Border eb = BorderFactory.createEtchedBorder();
//         Border ob = BorderFactory.createEmptyBorder(0,0,ch,0);
//         Border bo = BorderFactory.createCompoundBorder(eb, ib);
//         m.setBorder(BorderFactory.createCompoundBorder(ob, bo));

        Border ib = BorderFactory.createEmptyBorder(0, 0, ch, 0);
        Border eb = BorderFactory.createEtchedBorder();
        Border bo = BorderFactory.createCompoundBorder(eb, ib);
        m.setBorder(BorderFactory.createCompoundBorder(ib, bo));

        SpringLayout layout = new SpringLayout();
        JLayeredPane p = new JLayeredPane();
        p.setLayout(layout);

        Spring x     = layout.getConstraint(SpringLayout.WIDTH, p);
        Spring y     = layout.getConstraint(SpringLayout.HEIGHT, p);
        Spring g     = Spring.minus(Spring.constant(ir));

        SpringLayout.Constraints constraints = layout.getConstraints(c);
        constraints.setConstraint(SpringLayout.EAST,  Spring.sum(x, g));
        constraints.setConstraint(SpringLayout.SOUTH, y);
        p.setLayer(c, JLayeredPane.DEFAULT_LAYER+1);
        p.add(c);

        constraints = layout.getConstraints(m);
        constraints.setConstraint(SpringLayout.WEST,  Spring.constant(0));
        constraints.setConstraint(SpringLayout.NORTH, Spring.constant(0));
        constraints.setConstraint(SpringLayout.EAST,  x);
        constraints.setConstraint(SpringLayout.SOUTH, y);
        p.setLayer(m, JLayeredPane.DEFAULT_LAYER);
        p.add(m);

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
