package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalCheckBoxIcon;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        //System.setProperty("swing.noxp", "true");
        //UIManager.put("CheckBox.interiorBackground", new ColorUIResource(Color.GREEN));
        //UIManager.put("CheckBox.darkShadow", new ColorUIResource(Color.RED));
        //UIManager.put("CheckBox.icon", new IconUIResource(new MyCheckBoxIcon()));

        JCheckBox cb1 = new JCheckBox("bbbbbbbbbb");
        cb1.setIcon(new MyCheckBoxIcon());

        JCheckBox cb2 = new JCheckBox("ccccccccccccccc");
        cb2.setIcon(new MyCheckBoxIcon2());

        JCheckBox cb3 = new JCheckBox("dddddddd");
        cb3.setIcon(new MyCheckBoxIcon3());

        Box box = Box.createVerticalBox();
        box.add(makePanel("Default", new JCheckBox("aaaaaaaaaaaaa")));
        box.add(makePanel("WindowsIconFactory",     cb1));
        box.add(makePanel("CheckBox.icon+RED",      cb2));
        box.add(makePanel("MetalCheckBoxIcon+GRAY", cb3));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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

class MyCheckBoxIcon3 implements Icon {
    private final Icon orgIcon = new MetalCheckBoxIcon();
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        orgIcon.paintIcon(c, g, x, y);
        g.setColor(new Color(255, 155, 155, 100));
        g.fillRect(x+2,y+2,getIconWidth()-4,getIconHeight()-4);
    }
    @Override public int getIconWidth() {
        return orgIcon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return orgIcon.getIconHeight();
    }
}

class MyCheckBoxIcon2 implements Icon {
    private final Icon orgIcon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        orgIcon.paintIcon(c, g, x, y);
        if(c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton)c;
            ButtonModel model = b.getModel();
            g.setColor(new Color(255, 155, 155, 100));
            g.fillRect(x+2,y+2,getIconWidth()-4,getIconHeight()-4);
            if(model.isSelected()) {
                g.setColor(Color.RED);
                g.drawLine(x+9, y+3, x+9, y+3);
                g.drawLine(x+8, y+4, x+9, y+4);
                g.drawLine(x+7, y+5, x+9, y+5);
                g.drawLine(x+6, y+6, x+8, y+6);
                g.drawLine(x+3, y+7, x+7, y+7);
                g.drawLine(x+4, y+8, x+6, y+8);
                g.drawLine(x+5, y+9, x+5, y+9);
                g.drawLine(x+3, y+5, x+3, y+5);
                g.drawLine(x+3, y+6, x+4, y+6);
            }
        }
    }
    @Override public int getIconWidth() {
        return orgIcon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return orgIcon.getIconHeight();
    }
}

class MyCheckBoxIcon implements Icon {
    //com/sun/java/swing/plaf/windows/WindowsIconFactory.java
    private static final int CSIZE = 13;
    private static JCheckBox getCheckBox(Component c) {
        if(c instanceof JCheckBox) {
            return (JCheckBox)c;
        }else{
            return null;
        }
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        JCheckBox cb = getCheckBox(c);
        if(cb == null) {
            return;
        }
        ButtonModel model = cb.getModel();

        // outer bevel
        if(cb.isBorderPaintedFlat()) {
            g.setColor(UIManager.getColor("CheckBox.shadow"));
            g.drawRect(x+1, y+1, CSIZE-3, CSIZE-3);

            if(model.isPressed() && model.isArmed()) {
                g.setColor(UIManager.getColor("CheckBox.background"));
            }else{
                g.setColor(UIManager.getColor("CheckBox.interiorBackground"));
            }
            g.fillRect(x+2, y+2, CSIZE-4, CSIZE-4);
        }else{
            // Outer top/left
            g.setColor(UIManager.getColor("CheckBox.shadow"));
            g.drawLine(x, y, x+11, y);
            g.drawLine(x, y+1, x, y+11);

            // Outer bottom/right
            g.setColor(UIManager.getColor("CheckBox.highlight"));
            g.drawLine(x+12, y, x+12, y+12);
            g.drawLine(x, y+12, x+11, y+12);

            // Inner top.left
            g.setColor(UIManager.getColor("CheckBox.darkShadow"));
            g.drawLine(x+1, y+1, x+10, y+1);
            g.drawLine(x+1, y+2, x+1, y+10);

            // Inner bottom/right
            g.setColor(UIManager.getColor("CheckBox.light"));
            g.drawLine(x+1, y+11, x+11, y+11);
            g.drawLine(x+11, y+1, x+11, y+10);

            // inside box
            Color color = new Color(255, 155, 155);
            if(model.isPressed() && model.isArmed()) {
                //g.setColor(UIManager.getColor("CheckBox.background"));
                g.setColor(color.brighter());
            }else{
                //g.setColor(UIManager.getColor("CheckBox.interiorBackground"));
                g.setColor(color);
            }
            g.fillRect(x+2, y+2, CSIZE-4, CSIZE-4);
        }

//         if(model.isEnabled()) {
//             g.setColor(UIManager.getColor("CheckBox.foreground"));
//         }else{
//             g.setColor(UIManager.getColor("CheckBox.shadow"));
//         }

        // paint check
        if(model.isSelected()) {
            g.setColor(Color.BLUE);
            g.drawLine(x+9, y+3, x+9, y+3);
            g.drawLine(x+8, y+4, x+9, y+4);
            g.drawLine(x+7, y+5, x+9, y+5);
            g.drawLine(x+6, y+6, x+8, y+6);
            g.drawLine(x+3, y+7, x+7, y+7);
            g.drawLine(x+4, y+8, x+6, y+8);
            g.drawLine(x+5, y+9, x+5, y+9);
            g.drawLine(x+3, y+5, x+3, y+5);
            g.drawLine(x+3, y+6, x+4, y+6);
        }

        if(model.isRollover()) {
            g.setColor(Color.ORANGE);
            g.drawLine(x+1, y+1, x+1+CSIZE-3, y+1);
            g.drawLine(x+1, y+1, x+1, y+1+CSIZE-3);
        }
    }
    @Override public int getIconWidth() {
        return CSIZE;
    }
    @Override public int getIconHeight() {
        return CSIZE;
    }
}

