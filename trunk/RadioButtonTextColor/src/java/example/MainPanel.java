package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 3));
        ButtonGroup bg = new ButtonGroup();
        Box box0 = Box.createVerticalBox();
        box0.setBorder(BorderFactory.createTitledBorder("Default"));
        int i = 0;
        while(i < 4) {
            JRadioButton b = new JRadioButton("Default: " + i);
            bg.add(b);
            box0.add(b);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            box0.add(Box.createVerticalStrut(5));
            i++;
        }
        Box box1 = Box.createVerticalBox();
        box1.setBorder(BorderFactory.createTitledBorder("Text Color"));
        while(i < 8) {
            JRadioButton b = new ColorRadioButton("Text: " + i);
            bg.add(b);
            box1.add(b);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            box1.add(Box.createVerticalStrut(5));
            b.setIcon(null);
            b.setPressedIcon(null);
            b.setSelectedIcon(null);
            i++;
        }
        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createTitledBorder("Icon Color"));
        while(i < 12) {
            JRadioButton b = new ColorRadioButton("Icon: " + i);
            bg.add(b);
            box2.add(b);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            box2.add(Box.createVerticalStrut(5));
            i++;
        }
        add(box0);
        add(box1);
        add(box2);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
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

class ColorRadioButton extends JRadioButton {
    private static final Color defaultColor  = Color.BLACK;
    private static final Color pressedColor  = Color.GREEN;
    private static final Color selectedColor = Color.RED;
    private static final Color rolloverColor = Color.BLUE;
    private static final int iconSize = 16;

    public ColorRadioButton(String text) {
        super(text);
        setForeground(defaultColor);
        setIcon(new DefaultIcon());
        setPressedIcon(new PressedIcon());
        setSelectedIcon(new SelectedIcon());
        setRolloverIcon(new RolloverIcon());
//         addChangeListener(new ChangeListener() {
//             @Override public void stateChanged(ChangeEvent e) {
//                 ButtonModel model = getModel();
//                 if(!model.isEnabled()) {
//                     setForeground(Color.GRAY);
//                 }else if(model.isPressed() && model.isArmed()) {
//                     setForeground(pressedColor);
//                 }else if(model.isSelected()) {
//                     setForeground(selectedColor);
//                 }else if(isRolloverEnabled() && model.isRollover()) {
//                     setForeground(rolloverColor);
//                 }else{
//                     setForeground(defaultColor);
//                 }
//             }
//         });
    }
    @Override protected void fireStateChanged() {
        ButtonModel model = getModel();
        if(!model.isEnabled()) {
            setForeground(Color.GRAY);
        }else if(model.isPressed() && model.isArmed()) {
            setForeground(pressedColor);
        }else if(model.isSelected()) {
            setForeground(selectedColor);
        }else if(isRolloverEnabled() && model.isRollover()) {
            setForeground(rolloverColor);
        }else{
            setForeground(defaultColor);
        }
        super.fireStateChanged();
    };
    private static class DefaultIcon implements Icon {
        @Override public int getIconWidth() {
            return iconSize*2;
        }
        @Override public int getIconHeight() {
            return iconSize;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(defaultColor);
            g.drawRect(0, 0, getIconWidth()-1,   getIconHeight()-1);
            g.drawRect(1, 1, getIconWidth()-2-1, getIconHeight()-2-1);
            g.translate(-x, -y);
        }
    }
    private static class PressedIcon extends DefaultIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(pressedColor);
            g.drawRect(0, 0, getIconWidth()-1,   getIconHeight()-1);
            g.drawRect(1, 1, getIconWidth()-2-1, getIconHeight()-2-1);

            g.setColor(selectedColor);
            g.fillRect(4, 4, getIconWidth()-8, getIconHeight()-8);
            g.translate(-x, -y);
        }
    }
    private static class SelectedIcon extends DefaultIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(selectedColor);
            g.drawRect(0, 0, getIconWidth()-1,   getIconHeight()-1);
            g.drawRect(1, 1, getIconWidth()-2-1, getIconHeight()-2-1);

            g.setColor(pressedColor);
            g.fillRect(6, 6, getIconWidth()-12, getIconHeight()-12);
            g.translate(-x, -y);
        }
    }
    private static class RolloverIcon extends DefaultIcon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            g.setColor(rolloverColor);
            g.drawRect(0, 0, getIconWidth()-1,   getIconHeight()-1);
            g.drawRect(1, 1, getIconWidth()-2-1, getIconHeight()-2-1);
            g.translate(-x, -y);
        }
    }
}
