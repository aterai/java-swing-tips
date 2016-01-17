package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(new JTextField("Another focusable component"));
        box.add(Box.createVerticalStrut(5));
        ButtonGroup bg1 = new ButtonGroup();
        box.add(makeButtonGroupPanel("Default", bg1));
        box.add(Box.createVerticalStrut(5));

        ButtonGroup bg2 = new ButtonGroup();
        JPanel buttons = makeButtonGroupPanel("FocusTraversalPolicy", bg2);
        buttons.setFocusTraversalPolicyProvider(true);
        buttons.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            @Override public Component getDefaultComponent(Container focusCycleRoot) {
                ButtonModel selection = bg2.getSelection();
                for (Component c: focusCycleRoot.getComponents()) {
                    JRadioButton r = (JRadioButton) c;
                    if (r.getModel().equals(selection)) {
                        return r;
                    }
                }
                return super.getDefaultComponent(focusCycleRoot);
            }
        });
        box.add(buttons);
        box.add(Box.createVerticalStrut(5));

        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JButton(new AbstractAction("clear selection") {
            @Override public void actionPerformed(ActionEvent e) {
                bg1.clearSelection();
                bg2.clearSelection();
            }
        }));
        box.add(b);

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makeButtonGroupPanel(String title, ButtonGroup bg) {
        JPanel p = new JPanel();
        for (String s: Arrays.asList("aaa", "bbb", "ccc", "ddd", "eee")) {
            JRadioButton rb = new JRadioButton(s);
            bg.add(rb);
            p.add(rb);
            if ("ccc".equals(s)) {
                rb.setSelected(true);
            }
        }
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
