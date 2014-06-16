package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
//import java.util.stream.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JCheckBox dfbaiCheck = new JCheckBox("Tree.drawsFocusBorderAroundIcon");
    private final JCheckBox ddfiCheck  = new JCheckBox("Tree.drawDashedFocusIndicator");
    private final JTree tree = new JTree();
    public MainPanel(JMenuBar menuBar) {
        super(new BorderLayout());

        dfbaiCheck.setSelected(UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon"));
        ddfiCheck.setSelected(UIManager.getBoolean("Tree.drawDashedFocusIndicator"));

//         ActionListener al = new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 Object o = e.getSource();
//                 if (o instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) o).isSelected()) {
//                     updateCheckBox();
//                 }
//             }
//         };
//
//         List<JRadioButtonMenuItem> list = new ArrayList<>();
//         searchAllMenuElements(menuBar, list);
//         for (JRadioButtonMenuItem mi: list) {
//             mi.addActionListener(al);
//         }
//
// //         listMenuItems(menuBar)
// //           .filter(mi -> mi instanceof JRadioButtonMenuItem)
// //           .forEach(mi -> ((JRadioButtonMenuItem) mi).addActionListener(al));

        dfbaiCheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                boolean b = ((JCheckBox) e.getSource()).isSelected();
                UIManager.put("Tree.drawsFocusBorderAroundIcon", b);
                SwingUtilities.updateComponentTreeUI(tree);
            }
        });
        ddfiCheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                boolean b = ((JCheckBox) e.getSource()).isSelected();
                UIManager.put("Tree.drawDashedFocusIndicator", b);
                SwingUtilities.updateComponentTreeUI(tree);
            }
        });

        JPanel np = new JPanel(new GridLayout(2, 1));
        np.add(dfbaiCheck);
        np.add(ddfiCheck);

        add(np, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }

    @Override public void updateUI() {
        super.updateUI();
        if (dfbaiCheck != null) {
            dfbaiCheck.setSelected(UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon"));
        }
        if (ddfiCheck != null) {
            ddfiCheck.setSelected(UIManager.getBoolean("Tree.drawDashedFocusIndicator"));
        }
    }

//     private static void searchAllMenuElements(MenuElement me, List<JRadioButtonMenuItem> list) {
//         if (me instanceof JRadioButtonMenuItem) {
//             list.add((JRadioButtonMenuItem) me);
//         }
//         MenuElement[] sub = me.getSubElements();
//         if (sub.length != 0) {
//             for (MenuElement e: sub) {
//                 searchAllMenuElements(e, list);
//             }
//         }
//     }

//     static Stream<MenuElement> listMenuItems(MenuElement me) {
//         MenuElement[] sub = me.getSubElements();
//         if (sub.length != 0) {
//             return Arrays.stream(sub).flatMap(MainPanel::listMenuItems);
//         } else {
//             return Stream.of(me);
//         }
//     }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(mb));
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
                } catch (ClassNotFoundException | InstantiationException |
                         IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
