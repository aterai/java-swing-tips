package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
    private final JPanel panel = new JPanel();
    private final JCheckBox check = new JCheckBox("color");

    public MainPanel() {
        super(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        check.addActionListener(e -> {
            layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));
            panel.revalidate();
        });
        layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));

        Box box = Box.createHorizontalBox();
        box.add(check);
        box.add(Box.createHorizontalGlue());

        add(panel);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private void layoutComboBoxPanel(JPanel p2, List<JComboBox<?>> list) {
        p2.removeAll();
        p2.setLayout(new GridBagLayout());
        Border inside = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
        Border outside = BorderFactory.createTitledBorder("JComboBox Padding Test");
        p2.setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.LINE_END;
        for (int i = 0; i < list.size(); i++) {
            c.gridx = 0;
            c.weightx = 0d;
            c.fill = GridBagConstraints.NONE;
            p2.add(new JLabel(String.format("%d:", i)), c);
            c.gridx = 1;
            c.weightx = 1d;
            c.fill = GridBagConstraints.HORIZONTAL;
            p2.add(list.get(i), c);
        }
        p2.revalidate(); // ??? JDK 1.7.0 Nimbus ???
    }

    private List<JComboBox<?>> initComboBoxes(boolean isColor) {
        // if (uiCheck.isSelected()) {
        //     // [JDK-7158712] Synth Property "ComboBox.popupInsets" is ignored - Java Bug System
        //     // https://bugs.openjdk.java.net/browse/JDK-7158712
        //     UIManager.put("ComboBox.padding", new InsetsUIResource(1, 15, 1, 1));
        // }
        List<JComboBox<?>> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(makeComboBox());
        }

        // ---- 00 ----
        JComboBox<?> combo00 = list.get(0);
        combo00.setEditable(false);
        combo00.setToolTipText("combo.setEditable(false);");

        // ---- 01 ----
        JComboBox<?> combo01 = list.get(1);
        combo01.setEditable(true);
        JTextField editor01 = (JTextField) combo01.getEditor().getEditorComponent();
        editor01.setBorder(BorderFactory.createCompoundBorder(editor01.getBorder(), getPaddingBorder(isColor)));
        combo01.setToolTipText("editor.setBorder(BorderFactory.createCompoundBorder(editor.getBorder(), padding));");

        // ---- 02 ----
        JComboBox<?> combo02 = list.get(2);
        combo02.setEditable(true);
        JTextField editor02 = (JTextField) combo02.getEditor().getEditorComponent();
        editor02.setBorder(getPaddingBorder(isColor));
        combo02.setToolTipText("editor.setBorder(padding);");

        // ---- 03 ----
        JComboBox<?> combo03 = list.get(3);
        combo03.setEditable(true);
        JTextField editor03 = (JTextField) combo03.getEditor().getEditorComponent();
        Insets i = editor03.getInsets();
        editor03.setMargin(new Insets(i.top, i.left + 5, i.bottom, i.right));
        combo03.setToolTipText("Insets i = editor.getInsets(); editor.setMargin(new Insets(i.top, i.left + 5, i.bottom, i.right));");

        // ---- 04 ----
        JComboBox<?> combo04 = list.get(4);
        combo04.setEditable(true);
        JTextField editor04 = (JTextField) combo04.getEditor().getEditorComponent();
        Insets m = editor04.getMargin();
        editor04.setMargin(new Insets(m.top, m.left + 5, m.bottom, m.right));
        combo04.setToolTipText("Insets m = editor.getMargin(); editor.setMargin(new Insets(m.top, m.left + 5, m.bottom, m.right));");

        // ---- 05 ----
        JComboBox<?> combo05 = list.get(5);
        combo05.setEditable(true);
        combo05.setBorder(BorderFactory.createCompoundBorder(combo05.getBorder(), getPaddingBorder(isColor)));
        combo05.setToolTipText("combo.setBorder(BorderFactory.createCompoundBorder(combo.getBorder(), padding));");

        // ---- 06 ----
        JComboBox<?> combo06 = list.get(6);
        combo06.setEditable(true);
        combo06.setBorder(BorderFactory.createCompoundBorder(getPaddingBorder(isColor), combo06.getBorder()));
        combo06.setToolTipText("combo.setBorder(BorderFactory.createCompoundBorder(padding, combo.getBorder()));");

        if (isColor) {
            Color c = new Color(.8f, 1f, .8f);
            for (JComboBox<?> cb: list) {
                cb.setOpaque(true);
                cb.setBackground(c);
                JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
                editor.setOpaque(true);
                editor.setBackground(c);
            }
        }
        return list;
    }

    protected static Border getPaddingBorder(boolean isColor) {
        return isColor ? BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(1f, .8f, .8f, .5f))
                       : BorderFactory.createEmptyBorder(0, 5, 0, 0);
    }

    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaaaaaaaaaaaaaaaaaaaaaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("bbb1");
        model.addElement("bbb12");

        JComboBox<String> combo = new JComboBox<String>(model) {
            @Override public void updateUI() {
                setRenderer(null);
                super.updateUI();
                ListCellRenderer<? super String> lcr = getRenderer();
                setRenderer(new ListCellRenderer<String>() {
                    @Override public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel l = (JLabel) lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        l.setBorder(getPaddingBorder(false));
                        return l;
                    }
                });
                // ???: UIManager.put("ComboBox.editorBorder", BorderFactory.createEmptyBorder(0, 5, 0, 0));
                // ???: ((JLabel) lcr).setBorder(getPaddingBorder(false));
            }
        };
        return combo;
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
