package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private final JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
        @Override public void updateUI() {
            super.updateUI();
            setUI(new BasicSpinnerUI() {
                @Override protected LayoutManager createLayout() {
                    return new SpinnerLayout();
                }
            });
        }
    };
    private final JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
        @Override public void setLayout(LayoutManager mgr) {
            super.setLayout(new SpinnerLayout());
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        spinner1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        Box box = Box.createVerticalBox();
        box.add(makePanel("Default",          spinner0));
        box.add(makePanel("RIGHT_TO_LEFT",    spinner1));
        box.add(makePanel("L(Prev), R(Next)", spinner2));
        box.add(makePanel("L(Prev), R(Next)", spinner3));

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
class SpinnerLayout extends BorderLayout {
    @Override public void addLayoutComponent(Component comp, Object constraints) {
        if("Editor".equals(constraints)) {
            constraints = "Center";
        }else if("Next".equals(constraints)) {
            constraints = "East";
        }else if("Previous".equals(constraints)) {
            constraints = "West";
        }
        super.addLayoutComponent(comp, constraints);
    }
}
