package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.util.Objects;
import javax.swing.*;
// import javax.swing.plaf.ButtonUI;
// import javax.swing.plaf.basic.BasicRadioButtonUI;
// import javax.swing.plaf.synth.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane() {
        @Override public void addTab(String title, Component content) {
            super.addTab(title, content);
            /** //TEST:
            JCheckBox check = new JCheckBox(title) {
                private final Rectangle viewRect = new Rectangle();
                private final Rectangle textRect = new Rectangle();
                private final Rectangle iconRect = new Rectangle();
                @Override public boolean contains(int x, int y) {
                    Icon icon;
                    ButtonUI ui = getUI();
                    if (ui instanceof BasicRadioButtonUI) {
                        icon = ((BasicRadioButtonUI) ui).getDefaultIcon();
                    } else if (ui instanceof SynthButtonUI) {
                        //icon = ((SynthButtonUI) ui).getDefaultIcon(this);
                        SynthContext context = ((SynthButtonUI) ui).getContext(this);
                        icon = context.getStyle().getIcon(context, "CheckBox.icon");
                        //context.dispose();
                    } else {
                        icon = getIcon();
                    }
                    if (Objects.nonNull(icon)) {
                        // layout the text and icon
                        int width = getWidth();
                        int height = getHeight();
                        FontMetrics fm = getFontMetrics(getFont());
                        Insets i = getInsets();
                        viewRect.setBounds(i.left, i.top, width - i.right - i.left, height - i.bottom - i.top);
                        textRect.setBounds(0, 0, 0, 0);
                        iconRect.setBounds(0, 0, 0, 0);
                        SwingUtilities.layoutCompoundLabel(
                            this, fm, getText(), icon,
                            getVerticalAlignment(), getHorizontalAlignment(),
                            getVerticalTextPosition(), getHorizontalTextPosition(),
                            viewRect, iconRect, textRect,
                            getIconTextGap());
                        return iconRect.contains(x, y);
                    } else {
                        return super.contains(x, y);
                    }
                }
                @Override public void updateUI() {
                    super.updateUI();
                    setOpaque(false);
                    setFocusable(false);
                }
            };
            setTabComponentAt(getTabCount() - 1, check);
            /*/
            JCheckBox check = new JCheckBox();
            check.setOpaque(false);
            check.setFocusable(false);
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            p.setOpaque(false);
            p.add(check, BorderLayout.WEST);
            p.add(new JLabel(title));
            setTabComponentAt(getTabCount() - 1, p);
            //*/
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        tabs.addTab("JTree", new JScrollPane(new JTree()));
        tabs.addTab("JLabel", new JLabel("aaaaaaaaaaaaa"));
        add(tabs);
        add(new JButton(new AbstractAction("Add") {
            private int count;
            @Override public void actionPerformed(ActionEvent e) {
                JComponent c = count % 2 == 0 ? new JTree() : new JLabel("Tab" + count);
                tabs.addTab("Title" + count, c);
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
                count++;
            }
        }), BorderLayout.SOUTH);
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
