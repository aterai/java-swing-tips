package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
//import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box p = Box.createVerticalBox();
        String[] items = {"JComboBox 11111:", "JComboBox 222:", "JComboBox 33:"};

        JComboBox<String> combo1 = new JComboBox<String>(items) {
            @Override public void updateUI() {
                super.updateUI();
                JLabel r = (JLabel) getRenderer();
                r.setPreferredSize(new Dimension(0, 32));
            }
        };
        p.add(makeTitledPanel("setPreferredSize", combo1));
        p.add(Box.createVerticalStrut(5));

        JComboBox<String> combo2 = new JComboBox<>(items);
        combo2.setRenderer(new DefaultListCellRenderer() {
            private int cheight;
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Dimension d = super.getPreferredSize();
                cheight = index < 0 ? d.height : 32;
                return c;
            }
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = cheight;
                return d;
            }
        });
        p.add(makeTitledPanel("getListCellRendererComponent", combo2));
        p.add(Box.createVerticalStrut(5));

        JComboBox<String> combo3 = new JComboBox<>(items);
        combo3.setRenderer(new DefaultListCellRenderer() {
            private int cheight;
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String title = Objects.toString(value, "");
                if (index >= 0) {
                    title = String.format("<html><table><td height='32'>%s", value);
                }
                return super.getListCellRendererComponent(list, title, index, isSelected, cellHasFocus);
            }
        });
        p.add(makeTitledPanel("html", combo3));
        p.add(Box.createVerticalStrut(5));

//         JComboBox<String> combo4 = new JComboBox<>(items);
//         combo4.setUI(new BasicComboBoxUI() {
//             @Override protected ComboPopup createPopup() {
//                 return new BasicComboPopup(combo) {
//                     @Override protected void configureList() {
//                         super.configureList();
//                         list.setFixedCellHeight(60);
//                     }
//                 };
//             }
//         });
//         p.add(makeTitledPanel("BasicComboBoxUI", combo4));
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
