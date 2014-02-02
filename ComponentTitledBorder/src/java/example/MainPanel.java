package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(3, 1, 5, 5));
        final JTree tree = new JTree();
        final JCheckBox c = new JCheckBox("CheckBox", true);
        c.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tree.setEnabled(c.isSelected());
            }
        });
        c.setFocusPainted(false);
        JScrollPane l1 = new JScrollPane(tree);
        l1.setBorder(new ComponentTitledBorder(c, l1, BorderFactory.createEtchedBorder()));

        JLabel icon = new JLabel(new ImageIcon(getClass().getResource("16x16.png")));
        JLabel l2 = new JLabel("<html>aaaaaaaaaaaaaaaa<br>bbbbbbbbbbbbbbbbb");
        l2.setBorder(new ComponentTitledBorder(icon, l2, BorderFactory.createEtchedBorder()));

        JButton b = new JButton("Button");
        b.setFocusPainted(false);
        JLabel l3 = new JLabel("ccccccccccccccc");
        l3.setBorder(new ComponentTitledBorder(b, l3, BorderFactory.createEtchedBorder()));

        add(l1); add(l2); add(l3);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
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

class ComponentTitledBorder implements Border, MouseListener, MouseMotionListener, SwingConstants {
    private static final int OFFSET = 5;
    private final Component comp;
    private final Border border;

    public ComponentTitledBorder(Component comp, JComponent container, Border border) {
        this.comp      = comp;
        this.border    = border;
        if(comp instanceof JComponent) {
            ((JComponent)comp).setOpaque(true);
        }
        container.addMouseListener(this);
        container.addMouseMotionListener(this);
    }

    @Override public boolean isBorderOpaque() {
        return true;
    }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Insets borderInsets = border.getBorderInsets(c);
        Insets insets = getBorderInsets(c);
        int temp = (insets.top-borderInsets.top)/2;
        border.paintBorder(c, g, x, y+temp, width, height-temp);
        Dimension size = comp.getPreferredSize();
        Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
        SwingUtilities.paintComponent(g, comp, (Container)c, rect);
        comp.setBounds(rect);
    }

    @Override public Insets getBorderInsets(Component c) {
        Dimension size = comp.getPreferredSize();
        Insets insets = border.getBorderInsets(c);
        insets.top = Math.max(insets.top, size.height);
        return insets;
    }

    private void dispatchEvent(MouseEvent me) {
        Component src = me.getComponent();
        comp.dispatchEvent(SwingUtilities.convertMouseEvent(src, me, comp));
        src.repaint();
    }
    @Override public void mouseClicked(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mouseEntered(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mouseExited(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mousePressed(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mouseReleased(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mouseMoved(MouseEvent me) {
        dispatchEvent(me);
    }
    @Override public void mouseDragged(MouseEvent me) {
        dispatchEvent(me);
    }
}
