package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());

        JComponent box1 = Box.createHorizontalBox();
        box1.add(Box.createHorizontalGlue());
        box1.add(new JButton("default"));
        box1.add(Box.createHorizontalStrut(5));
        box1.add(new JButton("a"));
        //box1.add(Box.createHorizontalStrut(5));
        box1.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));

        JButton button1 = new JButton("getPreferredSize");
        JButton button2 = new JButton("xxx");
        Dimension dim = button1.getPreferredSize();
        button1.setPreferredSize(new Dimension(120, dim.height));
        button2.setPreferredSize(new Dimension(120, dim.height));

        JComponent box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalGlue());
        box2.add(button1);
        box2.add(Box.createHorizontalStrut(5));
        box2.add(button2);
        //box2.add(Box.createHorizontalStrut(5));
        box2.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));
        //box2.add(Box.createRigidArea(new Dimension(0, dim.height+10)));

        JComponent box3 = createRightAlignButtonBox(Arrays.asList(new JButton("Spring+Box"), new JButton("Layout")), 100, dim.height, 5);

        JComponent box4 = createRightAlignButtonBox2(Arrays.asList(new JButton("SpringLayout"), new JButton("gap:2")), 120, dim.height, 2);

        Box box = Box.createVerticalBox();
        //box.add(Box.createVerticalGlue());
        box.add(new JSeparator()); box.add(box4);
        box.add(new JSeparator()); box.add(box3);
        box.add(new JSeparator()); box.add(box2);
        box.add(new JSeparator()); box.add(box1);

        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JComponent createRightAlignButtonBox2(List<JButton>list, int buttonWidth, int buttonHeight, int gap) {
        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout);
        SpringLayout.Constraints pCons = layout.getConstraints(p);
        pCons.setConstraint(SpringLayout.SOUTH, Spring.constant(buttonHeight+gap+gap));

        Spring x     = layout.getConstraint(SpringLayout.WIDTH, p);
        Spring y     = Spring.constant(gap);
        Spring g     = Spring.minus(Spring.constant(gap));
        Spring width = Spring.constant(buttonWidth);
        for(JButton b: list) {
            SpringLayout.Constraints constraints = layout.getConstraints(b);
            constraints.setConstraint(SpringLayout.EAST, x = Spring.sum(x, g));
            constraints.setY(y);
            constraints.setWidth(width);
            p.add(b);
            x = Spring.sum(x, Spring.minus(width));
        }
        return p;
    }

    private static JComponent createRightAlignButtonBox(List<JButton>list, int buttonWidth, int buttonHeight, int gap) {
        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout);
        SpringLayout.Constraints pCons = layout.getConstraints(p);
        pCons.setConstraint(SpringLayout.SOUTH, Spring.constant(buttonHeight+gap+gap));
        pCons.setConstraint(SpringLayout.EAST,  Spring.constant((buttonWidth+gap)*list.size()));

        Spring x     = Spring.constant(0);
        Spring y     = Spring.constant(gap);
        Spring g     = Spring.constant(gap);
        Spring width = Spring.constant(buttonWidth);
        for(JButton b: list) {
            SpringLayout.Constraints constraints = layout.getConstraints(b);
            constraints.setX(x);
            constraints.setY(y);
            constraints.setWidth(width);
            p.add(b);
            x = Spring.sum(x, width);
            x = Spring.sum(x, g);
        }

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(p);
        return box;
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
