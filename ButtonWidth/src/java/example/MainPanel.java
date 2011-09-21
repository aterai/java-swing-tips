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
    private final JButton button1   = new JButton("showErrorDialog");
    private final JButton button2   = new JButton("exit");
    private final JButton button3   = new JButton("showErrorDialog");
    private final JButton button4   = new JButton("exit");
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());

        Dimension dim = button1.getPreferredSize();
        button1.setPreferredSize(new Dimension(120, dim.height));
        button2.setPreferredSize(new Dimension(120, dim.height));

        Box box1 = Box.createHorizontalBox();
        box1.add(Box.createHorizontalGlue());
        box1.add(button1);
        box1.add(Box.createHorizontalStrut(5));
        box1.add(button2);
        box1.add(Box.createHorizontalStrut(5));
        box1.add(Box.createRigidArea(new Dimension(0, dim.height+10)));

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalGlue());
        box2.add(button3);
        box2.add(Box.createHorizontalStrut(5));
        box2.add(button4);
        box2.add(Box.createHorizontalStrut(5));
        box2.add(Box.createRigidArea(new Dimension(0, dim.height+10)));

        Box box3 = createLeftAlignButtonBox(Arrays.asList(new JButton("aaa"), new JButton("bbb")), 100, dim.height, 5);

        Box box = Box.createVerticalBox();
        box.add(new JSeparator());
        box.add(box3);
        box.add(Box.createVerticalGlue());
        box.add(new JSeparator());
        box.add(box2);
        box.add(Box.createVerticalGlue());
        box.add(new JSeparator());
        box.add(box1);

        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static Box createLeftAlignButtonBox(List<JButton>list, int buttonWidth, int buttonHeight, int gap) {
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
