package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;

public class MainPanel extends JPanel {
    // private static final String DRAWS_FOCUS_BORDER_AROUND_ICON = "Tree.drawsFocusBorderAroundIcon";
    // private static final String DRAW_DASHED_FOCUS_INDICATOR = "Tree.drawDashedFocusIndicator";
    private enum TreeDraws {
        DRAWS_FOCUS_BORDER_AROUND_ICON("Tree.drawsFocusBorderAroundIcon"),
        DRAW_DASHED_FOCUS_INDICATOR("Tree.drawDashedFocusIndicator");
        private final String key;
        /* default */ TreeDraws(String key) {
            this.key = key;
        }
        @Override public String toString() {
            return key;
        }
    }
    private final JCheckBox dfbaiCheck = new ActionCommandCheckBox(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON);
    private final JCheckBox ddfiCheck = new ActionCommandCheckBox(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR);
    private final JTextArea textArea = new JTextArea();
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());

        append("MainPanel: init");
        // updateCheckBox("MainPanel: init");

        UIManager.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("lookAndFeel")) {
                // String lnf = e.getNewValue().toString();
                updateCheckBox("UIManager: propertyChange");
            }
        });

        EventQueue.invokeLater(() -> {
            ActionListener al = e -> {
                append("JMenuItem: actionPerformed");
                Object o = e.getSource();
                if (o instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) o).isSelected()) {
                    updateCheckBox("JMenuItem: actionPerformed: invokeLater");
                }
            };
            // List<JRadioButtonMenuItem> list = new ArrayList<>();
            // JMenuBar menuBar = getRootPane().getJMenuBar();
            // searchAllMenuElements(menuBar, list);
            // for (JRadioButtonMenuItem mi: list) {
            //     mi.addActionListener(al);
            // }
            stream(getRootPane().getJMenuBar())
                .filter(JRadioButtonMenuItem.class::isInstance)
                .map(JRadioButtonMenuItem.class::cast)
                .forEach(mi -> mi.addActionListener(al));
        });

        JPanel np = new JPanel(new GridLayout(2, 1));
        np.add(dfbaiCheck);
        np.add(ddfiCheck);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(new JScrollPane(tree));
        p.add(new JScrollPane(textArea));

        add(np, BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }

    @Override public void updateUI() {
        super.updateUI();
        append("JPanel: updateUI");
        updateCheckBox("JPanel: updateUI: invokeLater");
    }

    // public static void searchAllMenuElements(MenuElement me, List<JRadioButtonMenuItem> list) {
    //     if (me instanceof JRadioButtonMenuItem) {
    //         list.add((JRadioButtonMenuItem) me);
    //     }
    //     MenuElement[] sub = me.getSubElements();
    //     if (sub.length != 0) {
    //         for (MenuElement e: sub) {
    //             searchAllMenuElements(e, list);
    //         }
    //     }
    // }

    public static Stream<MenuElement> stream(MenuElement me) {
        return Stream.of(me.getSubElements())
            .map(MainPanel::stream)
            .reduce(Stream.of(me), Stream::concat);
    }

    public static Stream<MenuElement> stream2(MenuElement me) {
        return Stream.of(me.getSubElements())
            .flatMap(m -> Stream.concat(Stream.of(m), stream2(m)));
    }

    private void updateCheckBox(String str) {
        EventQueue.invokeLater(() -> {
            append("--------\n" + str);

            append(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON + ": " + UIManager.getBoolean(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString()));
            dfbaiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString()));

            append(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR + ": " + UIManager.getBoolean(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString()));
            ddfiCheck.setSelected(UIManager.getBoolean(TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString()));
        });
    }

    private void append(String str) {
        if (Objects.nonNull(textArea)) {
            textArea.append(str + "\n");
        } else {
            System.out.println(str);
        }
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
    private static class ActionCommandCheckBox extends JCheckBox {
        protected ActionCommandCheckBox(TreeDraws key) {
            super(key.toString());
            setAction(new AbstractAction(key.toString()) {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBox c = (JCheckBox) e.getSource();
                    UIManager.put(key.toString(), c.isSelected());
                    SwingUtilities.updateComponentTreeUI(c.getRootPane());
                }
            });
        }
    }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lafRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
        lafItem.setActionCommand(lafClassName);
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lafRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
