package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
//import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        // Insets
        // UIManager.put("TabbedPane.tabInsets",            new Insets(8, 8, 8, 8));
        // UIManager.put("TabbedPane.tabAreaInsets",        new Insets(8, 8, 8, 8));
        // UIManager.put("TabbedPane.contentBorderInsets",  new Insets(8, 8, 8, 8));
        // UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(8, 8, 8, 8));

        // Color
        // UIManager.put("TabbedPane.shadow",                Color.GRAY);
        // UIManager.put("TabbedPane.darkShadow",            Color.GRAY);
        // UIManager.put("TabbedPane.light",                 Color.GRAY);
        // UIManager.put("TabbedPane.highlight",             Color.GRAY);
        // UIManager.put("TabbedPane.tabAreaBackground",     Color.GRAY);
        // UIManager.put("TabbedPane.unselectedBackground",  Color.GRAY);
        // UIManager.put("TabbedPane.background",            Color.GRAY);
        // UIManager.put("TabbedPane.foreground",            Color.WHITE);
        // UIManager.put("TabbedPane.focus",                 Color.WHITE);
        // UIManager.put("TabbedPane.contentAreaColor",      Color.WHITE);
        // UIManager.put("TabbedPane.selected",              Color.WHITE);
        // UIManager.put("TabbedPane.selectHighlight",       Color.WHITE);
        // UIManager.put("TabbedPane.borderHightlightColor", Color.WHITE);

        // Opaque
        // UIManager.put("TabbedPane.tabsOpaque",    Boolean.FALSE);
        // UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);

        // ???
        // UIManager.put("TabbedPane.tabRunOverlay", Boolean.FALSE);
        // UIManager.put("TabbedPane.tabsOverlapBorder", Boolean.FALSE);
        // // UIManager.put("TabbedPane.selectionFollowsFocus", Boolean.FALSE);
        HashMap<String, Color> map = new HashMap<>();
        map.put("TabbedPane.darkShadow",            Color.GRAY);
        map.put("TabbedPane.light",                 Color.GRAY);
        map.put("TabbedPane.tabAreaBackground",     Color.GRAY);
        map.put("TabbedPane.unselectedBackground",  Color.GRAY);
        map.put("TabbedPane.shadow",                Color.GRAY);
        map.put("TabbedPane.highlight",             Color.GRAY);
        // map.put("TabbedPane.background",            Color.RED);
        // map.put("TabbedPane.foreground",            Color.BLUE);
        map.put("TabbedPane.focus",                 Color.WHITE);
        map.put("TabbedPane.contentAreaColor",      Color.WHITE);
        map.put("TabbedPane.selected",              Color.WHITE);
        map.put("TabbedPane.selectHighlight",       Color.WHITE);
        map.put("TabbedPane.borderHightlightColor", Color.WHITE);
        // for (Map.Entry<String, Color> entry: map.entrySet()) {
        //     UIManager.put(entry.getKey(), entry.getValue());
        // }
        map.forEach(UIManager::put);

        JTabbedPane tabs = makeTabbedPane();
        JComboBox<String> combo = makeComboBox(map);
        JCheckBox opaque = new JCheckBox("JTabbedPane#setOpaque", true);

        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = GridBagConstraints.REMAINDER;
        p.add(opaque, c);
        p.add(combo, c);

        opaque.addActionListener(e -> {
            tabs.setOpaque(((JCheckBox) e.getSource()).isSelected());
            tabs.repaint();
        });
        combo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            map.forEach(UIManager::put);
            if (combo.getSelectedIndex() > 0) {
                UIManager.put(combo.getSelectedItem(), Color.GREEN);
            }
            // XXX: JComboBox: by UP/DOWN keys
            // XXX: NullPointerException at javax.swing.plaf.basic.BasicComboBoxUI.selectNextPossibleValue(BasicComboBoxUI.java:1128)
            // SwingUtilities.updateComponentTreeUI(tabs);
            tabs.updateUI();
        });

        tabs.addTab("JTree", new JScrollPane(new JTree()));
        tabs.addTab("JTextArea", new JScrollPane(new JTextArea()));
        tabs.addTab("JButton", new JButton("button"));
        tabs.addTab("JPanel", p);

        tabs.setMnemonicAt(0, KeyEvent.VK_T);
        tabs.setMnemonicAt(1, KeyEvent.VK_A);
        tabs.setMnemonicAt(2, KeyEvent.VK_B);
        tabs.setMnemonicAt(3, KeyEvent.VK_P);

        add(tabs);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JTabbedPane makeTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        // tabs.setBackground(Color.GREEN);
        tabs.setOpaque(true);
        tabs.addChangeListener(e -> {
            JTabbedPane t = (JTabbedPane) e.getSource();
            int si = t.getSelectedIndex();
            for (int i = 0; i < t.getTabCount(); i++) {
                t.setForegroundAt(i, (i == si) ? Color.BLACK : Color.WHITE);
            }
        });
        tabs.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                JTabbedPane t = (JTabbedPane) e.getComponent();
                int si = t.getSelectedIndex();
                int tgt = t.indexAtLocation(e.getX(), e.getY());
                for (int i = 0; i < t.getTabCount(); i++) {
                    if (i != si) {
                        t.setForegroundAt(i, (i == tgt) ? Color.ORANGE : Color.WHITE);
                    }
                }
            }
        });
        return tabs;
    }

    private static JComboBox<String> makeComboBox(Map<String, Color> map) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("gray-white");
        map.forEach((key, value) -> model.addElement(key));
        return new JComboBox<>(model);
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
