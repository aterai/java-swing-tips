package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox(new AbstractAction("Slider.onlyLeftMouseButtonDrag") {
        @Override public void actionPerformed(ActionEvent e) {
            boolean f = ((JCheckBox) e.getSource()).isSelected();
            UIManager.put("Slider.onlyLeftMouseButtonDrag", f);
        }
    }) {
        @Override public void updateUI() {
            super.updateUI();
            setSelected(UIManager.getLookAndFeelDefaults().getBoolean("Slider.onlyLeftMouseButtonDrag"));
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        BoundedRangeModel model = new DefaultBoundedRangeModel(50, 0, 0, 100);
        JSlider slider1 = new JSlider(SwingConstants.VERTICAL);
        JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL);

        for (JSlider s: Arrays.asList(slider1, slider2)) {
            s.setModel(model);
            s.setMajorTickSpacing(20);
            s.setMinorTickSpacing(10);
            s.setPaintTicks(true);
            s.setPaintLabels(true);
        }

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        box1.add(slider1);
        box1.add(Box.createHorizontalGlue());

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
        box2.add(slider2);
        box2.add(Box.createVerticalGlue());

        add(box1, BorderLayout.WEST);
        add(box2);
        add(check, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
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
