package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        BoundedRangeModel model = new DefaultBoundedRangeModel(50, 0, 0, 100);
        JSlider slider0 = new JSlider(SwingConstants.VERTICAL);
        JSlider slider1 = new JSlider(SwingConstants.VERTICAL);
        JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL);
        JSlider slider3 = new JSlider(SwingConstants.HORIZONTAL);
        List<JSlider> list = Arrays.asList(slider0, slider1, slider2, slider3);

        JCheckBox check = new JCheckBox("ComponentOrientation.RIGHT_TO_LEFT");
        check.addActionListener(e -> {
            ComponentOrientation orientation = ((JCheckBox) e.getSource()).isSelected()
              ? ComponentOrientation.RIGHT_TO_LEFT
              : ComponentOrientation.LEFT_TO_RIGHT;
            list.forEach(s -> s.setComponentOrientation(orientation));
        });
        list.forEach(s -> {
            s.setModel(model);
            s.setMajorTickSpacing(20);
            s.setMinorTickSpacing(10);
            s.setPaintTicks(true);
            s.setPaintLabels(true);
        });
        slider1.setInverted(true);
        slider3.setInverted(true);

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        box1.add(slider0);
        box1.add(Box.createHorizontalStrut(20));
        box1.add(slider1);
        box1.add(Box.createHorizontalGlue());

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
        box2.add(makeTitledPanel("Default", slider2));
        box2.add(Box.createVerticalStrut(20));
        box2.add(makeTitledPanel("setInverted(true)", slider3));
        box2.add(Box.createVerticalGlue());

        add(box1, BorderLayout.WEST);
        add(box2);
        add(check, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        c.setBorder(BorderFactory.createTitledBorder(title));
        return c;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lookAndFeelRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
