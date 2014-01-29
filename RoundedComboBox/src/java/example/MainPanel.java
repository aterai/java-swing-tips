package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class MainPanel extends JPanel {
    private final static Color BACKGROUND = Color.BLACK; //RED;
    private final static Color FOREGROUND = Color.WHITE; //YELLOW;
    private final static Color SELECTIONFOREGROUND = Color.CYAN;
    private MainPanel() {
        super(new BorderLayout());

        JComboBox<String> combo0 = new JComboBox<>(makeModel());
        JComboBox<String> combo1 = new JComboBox<>(makeModel());
        JComboBox<String> combo2 = new JComboBox<>(makeModel());

        combo0.setBorder(new RoundedCornerBorder());
        combo1.setBorder(new KamabokoBorder());
        combo2.setBorder(new KamabokoBorder());
        if(combo2.getUI() instanceof WindowsComboBoxUI) {
            combo2.setUI(new WindowsComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton b = new JButton(new ArrowIcon(Color.BLACK, Color.BLUE)); //.createArrowButton();
                    b.setContentAreaFilled(false);
                    b.setFocusPainted(false);
                    b.setBorder(BorderFactory.createEmptyBorder());
                    return b;
                }
            });
        }

        Box box0 = Box.createVerticalBox();
        box0.add(createPanel(combo0, "RoundRectangle2D:", null));
        box0.add(Box.createVerticalStrut(5));
        box0.add(createPanel(combo1, "Path2D:", null));
        box0.add(Box.createVerticalStrut(5));
        box0.add(createPanel(combo2, "WindowsComboBoxUI#createArrowButton():", null));
        box0.add(Box.createVerticalStrut(5));
        box0.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        //UIManager.put("TitledBorder.titleColor", FOREGROUND);
        //UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

        UIManager.put("ComboBox.foreground", FOREGROUND);
        UIManager.put("ComboBox.background", BACKGROUND);
        UIManager.put("ComboBox.selectionForeground", SELECTIONFOREGROUND);
        UIManager.put("ComboBox.selectionBackground", BACKGROUND);

        UIManager.put("ComboBox.buttonDarkShadow", BACKGROUND);
        UIManager.put("ComboBox.buttonBackground", FOREGROUND);
        UIManager.put("ComboBox.buttonHighlight", FOREGROUND);
        UIManager.put("ComboBox.buttonShadow", FOREGROUND);

        //UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
        //UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));
        UIManager.put("ComboBox.border", new KamabokoBorder());

        JComboBox<String> combo00 = new JComboBox<>(makeModel());
        JComboBox<String> combo01 = new JComboBox<>(makeModel());

        UIManager.put("ComboBox.border", new KamabokoBorder());
        JComboBox<String> combo02 = new JComboBox<>(makeModel());

        combo00.setUI(new MetalComboBoxUI());
        combo01.setUI(new BasicComboBoxUI());
        combo02.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton(new ArrowIcon(BACKGROUND, FOREGROUND));
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
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,FOREGROUND));
        o = combo01.getAccessibleContext().getAccessibleChild(0);
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,FOREGROUND));
        o = combo02.getAccessibleContext().getAccessibleChild(0);
        ((JComponent)o).setBorder(BorderFactory.createMatteBorder(0,1,1,1,FOREGROUND));

        Box box1 = Box.createVerticalBox();
        box1.add(createPanel(combo00, "MetalComboBoxUI:", BACKGROUND));
        box1.add(Box.createVerticalStrut(10));
        box1.add(createPanel(combo01, "BasicComboBoxUI:", BACKGROUND));
        box1.add(Box.createVerticalStrut(10));
        box1.add(createPanel(combo02, "BasicComboBoxUI#createArrowButton():", BACKGROUND));
        box1.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Basic, Metal", createPanel(box1, null, BACKGROUND));
        tabbedPane.add("Windows", createPanel(box0, null, null));

        add(tabbedPane);
        final List<JComboBox<String>> list = Arrays.asList(combo00, combo01, combo02, combo0, combo1, combo2);
        add(new JCheckBox(new AbstractAction("editable") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean flag = ((JCheckBox)e.getSource()).isSelected();
                for(JComboBox c:list) {
                    c.setEditable(flag);
                }
                repaint();
            }
        }), BorderLayout.SOUTH);
        //setOpaque(true);
        //setBackground(BACKGROUND);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp, String str, Color bgc) {
        JPanel panel = new JPanel(new BorderLayout());
        if(cmp.getLayout() instanceof BoxLayout) {
            panel.add(cmp, BorderLayout.NORTH);
        }else{
            panel.add(cmp);
        }
        if(str!=null) {
            TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), str);
            if(bgc!=null) {
                b.setTitleColor(new Color(~bgc.getRGB()));
            }
            panel.setBorder(b);
        }
        if(bgc!=null) {
            panel.setOpaque(true);
            panel.setBackground(bgc);
        }
        return panel;
    }
    private static DefaultComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("1234");
        model.addElement("5555555555555555555555");
        model.addElement("6789000000000");
        return model;
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

class ArrowIcon implements Icon {
    private final Color color, rollover;
    public ArrowIcon(Color color, Color rollover) {
        this.color = color;
        this.rollover = rollover;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(color);
        int shift = 0;
        if(c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton)c).getModel();
            if(m.isPressed()) {
                shift = 1;
            }else{
                if(m.isRollover()) {
                    g2.setPaint(rollover);
                }
            }
        }
        g2.translate(x, y+shift);
        g2.drawLine(2, 3, 6, 3);
        g2.drawLine(3, 4, 5, 4);
        g2.drawLine(4, 5, 4, 5);
        g2.translate(-x, -y-shift);
    }
    @Override public int getIconWidth()  {
        return 9;
    }
    @Override public int getIconHeight() {
        return 9;
    }
}

class RoundedCornerBorder extends AbstractBorder {
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = 12;
        int w = width  - 1;
        int h = height - 1;

        Area round = new Area(new RoundRectangle2D.Float(x, y, w, h, r, r));

        Container parent = c.getParent();
        if(parent!=null) {
            g2.setColor(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
            corner.subtract(round);
            g2.fill(corner);
        }
        g2.setPaint(c.getForeground());
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

class KamabokoBorder extends RoundedCornerBorder {
//     private static TexturePaint makeCheckerTexture() {
//         int cs = 6;
//         int sz = cs*cs;
//         BufferedImage bi = new BufferedImage(sz,sz,BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2 = bi.createGraphics();
//         g2.setPaint(new Color(200,150,100,50));
//         g2.fillRect(0,0,sz,sz);
//         for(int i=0;i*cs<sz;i++) {
//             for(int j=0;j*cs<sz;j++) {
//                 if((i+j)%2==0) { g2.fillRect(i*cs, j*cs, cs, cs); }
//             }
//         }
//         g2.dispose();
//         return new TexturePaint(bi, new Rectangle(0,0,sz,sz));
//     }
//     private static TexturePaint tp = makeCheckerTexture();
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = 12;
        int w = width  - 1;
        int h = height - 1;
//*/
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x, y + h);
        p.lineTo(x, y + r);
        p.quadTo(x, y, x + r, y);
        p.lineTo(x + w - r, y);
        p.quadTo(x + w, y, x + w, y + r);
        p.lineTo(x + w, y + h);
        p.closePath();
        Area round = new Area(p);
/*/
        Area round = new Area(new RoundRectangle2D.Float(x, y, w, h, r, r));
        Rectangle b = round.getBounds();
        b.setBounds(b.x, b.y + r, b.width, b.height - r);
        round.add(new Area(b));
//*/
        Container parent = c.getParent();
        if(parent!=null) {
            g2.setColor(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
            corner.subtract(round);
            g2.fill(corner);
        }
//         g2.setPaint(tp);
//         g2.fill(round);
        g2.setPaint(c.getForeground());
        g2.draw(round);
        g2.dispose();
    }
}
