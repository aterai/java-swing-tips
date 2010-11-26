package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);
        JLabel  l1 = new JLabel("label: 05%,05%,90%,55%", SwingConstants.CENTER);
        JButton l2 = new JButton("button: 50%,65%,40%,30%");
        //JLabel l2 = new JLabel("label: 50%,65%,40%,30%", SwingConstants.CENTER);

        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));
        l1.setOpaque(true);
        l1.setBackground(Color.ORANGE);
        l1.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        //l2.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));

        setScaleAndAdd(panel, layout, l1, 0.05f, 0.05f, 0.90f, 0.55f);
        setScaleAndAdd(panel, layout, l2, 0.50f, 0.65f, 0.40f, 0.30f);

        //addComponentListener(new ComponentAdapter() {
        //    @Override public void componentResized(ComponentEvent e) {
        //        initLayout();
        //    }
        //});
        add(panel);
        setPreferredSize(new Dimension(320, 200));
    }

    private static void setScaleAndAdd(JComponent parent, SpringLayout layout, JComponent child, float sx, float sy, float sw, float sh) {
        Spring panelw = layout.getConstraint(SpringLayout.WIDTH,  parent);
        Spring panelh = layout.getConstraint(SpringLayout.HEIGHT, parent);

        SpringLayout.Constraints c = layout.getConstraints(child);
        c.setX(Spring.scale(panelw, sx));
        c.setY(Spring.scale(panelh, sy));
        c.setWidth(Spring.scale(panelw,  sw));
        c.setHeight(Spring.scale(panelh, sh));

        parent.add(child);
    }

//     public void initLayout() {
//         SpringLayout layout = new SpringLayout();
//         Insets i = panel.getInsets();
//         int w = panel.getWidth()  - i.left - i.right;
//         int h = panel.getHeight() - i.top  - i.bottom;
//
//         l1.setPreferredSize(new Dimension( w*90/100, h*55/100 ) );
//         l2.setPreferredSize(new Dimension( w*40/100, h*30/100 ) );
//
//         layout.putConstraint(SpringLayout.WEST,  l1, w*5/100,  SpringLayout.WEST,  panel);
//         layout.putConstraint(SpringLayout.NORTH, l1, h*5/100,  SpringLayout.NORTH, panel);
//         layout.putConstraint(SpringLayout.WEST,  l2, w*50/100, SpringLayout.WEST,  panel);
//         layout.putConstraint(SpringLayout.SOUTH, l2, -h*5/100, SpringLayout.SOUTH, panel);
//
//         panel.setLayout(layout);
//         panel.revalidate();
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
