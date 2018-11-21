package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] model = {"123456", "7890", "a"};

        JComboBox<String> comboBox0 = new JComboBox<>(model);
        comboBox0.setEditable(true);

        JComboBox<String> comboBox1 = new JComboBox<String>(model) {
            private transient PopupMenuListener handler;
            @Override public void updateUI() {
                removePopupMenuListener(handler);
                super.updateUI();
                setEditable(true);
                handler = new SelectItemMenuListener();
                addPopupMenuListener(handler);
            }
        };

        JComboBox<String> comboBox2 = new JComboBox<String>(model) {
            private static final int MAX_HISTORY = 10;
            private static final String ENTER_PRESSED = "enterPressed";
            private transient PopupMenuListener handler;
            @Override public void updateUI() {
                removePopupMenuListener(handler);
                getActionMap().put(ENTER_PRESSED, null);
                super.updateUI();
                Action defaultAction = getActionMap().get(ENTER_PRESSED);
                Action a = new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) {
                        boolean isPopupVisible = isPopupVisible();
                        setPopupVisible(false);
                        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) getModel();
                        String str = Objects.toString(getEditor().getItem(), "");
                        if (m.getIndexOf(str) < 0) {
                            m.removeElement(str);
                            m.insertElementAt(str, 0);
                            if (m.getSize() > MAX_HISTORY) {
                                m.removeElementAt(MAX_HISTORY);
                            }
                            setSelectedIndex(0);
                            setPopupVisible(isPopupVisible);
                        } else {
                            defaultAction.actionPerformed(e);
                        }
                    }
                };
                getActionMap().put(ENTER_PRESSED, a);
                setEditable(true);
                handler = new SelectItemMenuListener();
                addPopupMenuListener(handler);
            }
        };

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Default:", SwingConstants.LEFT));
        p.add(comboBox0);
        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("popupMenuWillBecomeVisible:", SwingConstants.LEFT));
        p.add(comboBox1);
        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("+enterPressed Action:", SwingConstants.LEFT));
        p.add(comboBox2);
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 240));
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class SelectItemMenuListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JComboBox<?> c = (JComboBox<?>) e.getSource();
        c.setSelectedItem(c.getEditor().getItem());
    }
    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* not needed */ }
    @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
}
