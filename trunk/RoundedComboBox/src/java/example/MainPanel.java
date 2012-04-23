package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;


import java.awt.image.*;
// import javax.imageio.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("TitledBorder.titleColor", Color.WHITE);
        UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.background", Color.BLACK);
        UIManager.put("ComboBox.selectionForeground", Color.CYAN);
        UIManager.put("ComboBox.selectionBackground", Color.BLACK);

        UIManager.put("ComboBox.buttonDarkShadow", Color.BLACK);
        UIManager.put("ComboBox.buttonBackground", Color.WHITE);
        UIManager.put("ComboBox.buttonHighlight", Color.WHITE);
        UIManager.put("ComboBox.buttonShadow", Color.WHITE);

        //UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
        //UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));
        UIManager.put("ComboBox.border", new RoundedCornerBorder1());

        JComboBox combo00 = makeComboBox();
        JComboBox combo01 = makeComboBox();

        UIManager.put("ComboBox.border", new RoundedCornerBorder2());
        JComboBox combo02 = makeComboBox();

        combo01.setUI(new BasicComboBoxUI());
        combo02.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton(new ArrowIcon()); //.createArrowButton();
                b.setBackground(Color.BLACK);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder());
                return b;
            }
        });

        combo02.addMouseListener(new MouseAdapter() {
            private ButtonModel getButtonModel(MouseEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                JButton b = (JButton)cb.getComponent(0);
                return b.getModel();
            }
            @Override public void mouseEntered(MouseEvent e) {
                getButtonModel(e).setRollover(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                getButtonModel(e).setRollover(false);
            }
            @Override public void mousePressed(MouseEvent e) {
                getButtonModel(e).setPressed(true);
            }
            @Override public void mouseReleased(MouseEvent e) {
                getButtonModel(e).setPressed(false);
            }
        });

        Object o = combo00.getAccessibleContext().getAccessibleChild(0);
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.WHITE));
        o = combo01.getAccessibleContext().getAccessibleChild(0);
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.WHITE));
        o = combo02.getAccessibleContext().getAccessibleChild(0);
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,Color.WHITE));

        Box box = Box.createVerticalBox();
        box.add(createPanel(combo00, "MetalComboBoxUI:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo01, "BasicComboBoxUI:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo02, "BasicComboBoxUI#createArrowButton():"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(box, BorderLayout.NORTH);
        setOpaque(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        panel.setOpaque(true);
        panel.setBackground(Color.BLACK);
        return panel;
    }
//     //JDK 1.7.0
//     private static DefaultComboBoxModel<String> makeModel() {
//         DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("1234");
        model.addElement("5555555555555555555555");
        model.addElement("6789000000000");
        return new JComboBox(model);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class ArrowIcon implements Icon{
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(Color.WHITE);
        if(c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton)c).getModel();
            if(m.isPressed()) {
                y++;
            }else{
                if(m.isRollover()) {
                    g2.setPaint(Color.WHITE);
                }else{
                    g2.setPaint(Color.BLACK);
                }
            }
        }
        g2.translate(x,y);
        g2.drawLine( 2, 3, 6, 3 );
        g2.drawLine( 3, 4, 5, 4 );
        g2.drawLine( 4, 5, 4, 5 );
        g2.translate(-x,-y);
    }
    @Override public int getIconWidth()  {
        return 9;
    }
    @Override public int getIconHeight() {
        return 9;
    }
}
class RoundedCornerBorder1 extends AbstractBorder {
    @Override public void paintBorder(
        Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = 12;

        Area round = new Area(new RoundRectangle2D.Float(x, y, width-1, height-1, r, r));
        Rectangle b = round.getBounds();
        b.setBounds(b.x, b.y + r, b.width, b.height - r);
        round.add(new Area(b));

        Container parent = c.getParent();
        if(parent!=null) {
            g2.setColor(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
            corner.subtract(round);
            g2.fill(corner);
        }
        g2.setPaint(Color.WHITE);
        g2.draw(round);
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }
    @Override public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = 8;
        insets.top = insets.bottom = 4;
        return insets;
    }
}
class RoundedCornerBorder2 extends AbstractBorder {
//     private static TexturePaint makeCheckerTexture() {
//         int cs = 6;
//         int sz = cs*cs;
//         BufferedImage bi = new BufferedImage(sz,sz,BufferedImage.TYPE_4BYTE_ABGR);
//         Graphics2D g2 = bi.createGraphics();
//         g2.setPaint(new Color(200,150,100,50));
//         g2.fillRect(0,0,sz,sz);
//         for(int i=0;i*cs<sz;i++) {
//             for(int j=0;j*cs<sz;j++) {
//                 if((i+j)%2==0) g2.fillRect(i*cs, j*cs, cs, cs);
//             }
//         }
//         g2.dispose();
//         return new TexturePaint(bi, new Rectangle(0,0,sz,sz));
//     }
//     private static TexturePaint tp = makeCheckerTexture();
    @Override public void paintBorder(
        Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = 12;

        int w = width  - 1;
        int h = height - 1;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x, y + h);
        p.lineTo(x, y + r);
        p.quadTo(x, y, x + r, y);
        p.lineTo(x + w - r, y);
        p.quadTo(x + w, y, x + w, y + r);
        p.lineTo(x + w, y + h);
        p.closePath();
        Area round = new Area(p);

        Container parent = c.getParent();
        if(parent!=null) {
            g2.setColor(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
            corner.subtract(round);
            g2.fill(corner);
        }
//         g2.setPaint(tp);
//         g2.fill(round);
        g2.setPaint(Color.WHITE);
        g2.draw(round);
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }
    @Override public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.right = 8;
        insets.top = insets.bottom = 4;
        return insets;
    }
}
