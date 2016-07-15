package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final ImageIcon image = new ImageIcon(getClass().getResource("16x16.png"));

        //JComboBox<String> combo01 = new JComboBox<>(makeModel());

        JComboBox<String> combo02 = new JComboBox<>(makeModel());
        ComboBoxUtil.initComboBoxRenderer(combo02, image);

        JComboBox<String> combo03 = new JComboBox<>(makeModel());
        combo03.setEditable(true);
        ComboBoxUtil.initComboBoxRenderer(combo03, image);

        //JComboBox<String> combo04 = new JComboBox<String>(makeModel()) {
        //    @Override public void updateUI() {
        //        setBorder(null);
        //        super.updateUI();
        //        setEditable(true);
        //        ComboBoxUtil.initComboBoxRenderer(this, image);
        //        setBorder(ComboBoxUtil.makeIconComboBorder(this, image));
        //    }
        //};

        JComboBox<String> combo05 = new JComboBox<String>(makeModel()) {
            @Override public void updateUI() {
                setBorder(null);
                super.updateUI();
                setEditable(true);
                ComboBoxUtil.initComboBoxRenderer(this, image);
                ComboBoxUtil.initIconComboBorder1(this, image);
            }
        };

        JComboBox<String> combo06 = new JComboBox<String>(makeModel()) {
            @Override public void updateUI() {
                setBorder(null);
                super.updateUI();
                setEditable(true);
                ComboBoxUtil.initComboBoxRenderer(this, image);
                ComboBoxUtil.initIconComboBorder2(this, image);
            }
        };

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel("setEditable(false)", Arrays.asList(combo02)));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitlePanel("setEditable(true)",  Arrays.asList(combo03, combo05, combo06)));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    public static ComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("ccccccccccccccc");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return model;
    }
    private static JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1d;
        c.gridx   = GridBagConstraints.REMAINDER;
        for (JComponent cmp: list) {
            p.add(cmp, c);
        }
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

final class ComboBoxUtil {
    private ComboBoxUtil() { /* Singleton */ }
//     public static Border makeIconComboBorder(final JComponent comp, final ImageIcon icon) {
//         Icon wrappedIcon = new ImageIcon() {
//             @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//                 g.translate(x, y);
//                 int ih = icon.getIconHeight();
//                 int ch = getIconHeight();
//                 int yy = Math.max((ch - ih) / 2, 0); //ch - ih > 0 ? (ch - ih) / 2 : 0;
//                 g.drawImage(icon.getImage(), 0, yy, c);
//                 g.translate(-x, -y);
//             }
//             @Override public int getIconWidth() {
//                 return icon.getIconWidth();
//             }
//             @Override public int getIconHeight() {
//                 Insets is = comp.getInsets();
//                 return comp.getPreferredSize().height - is.top - is.bottom;
//             }
//         };
//         Border b1 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, wrappedIcon);
//         Border b2 = BorderFactory.createEmptyBorder(0, 5, 0, 0);
//         Border b3 = BorderFactory.createCompoundBorder(b1, b2);
//         return BorderFactory.createCompoundBorder(comp.getBorder(), b3);
//     }
    public static void initIconComboBorder1(JComboBox comboBox, final ImageIcon icon) {
        final JTextField comp = (JTextField) comboBox.getEditor().getEditorComponent();
        Icon wrappedIcon = new ImageIcon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                int ih = icon.getIconHeight();
                int ch = getIconHeight();
                int yy = Math.max((int) (.5 + (ch - ih) * .5), 0); //ch - ih > 0 ? (int) (.5 + (ch - ih) * .5) : 0;
                g2.drawImage(icon.getImage(), 0, yy, c);
                g2.dispose();
            }
            @Override public int getIconWidth() {
                return icon.getIconWidth();
            }
            @Override public int getIconHeight() {
                Insets is = comp.getInsets();
                return comp.getPreferredSize().height - is.top - is.bottom;
            }
        };
        Border b1 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, wrappedIcon);
        Border b2 = BorderFactory.createEmptyBorder(0, 5, 0, 0);
        Border b3 = BorderFactory.createCompoundBorder(b1, b2);
        comp.setBorder(BorderFactory.createCompoundBorder(comp.getBorder(), b3));
    }
    public static void initIconComboBorder2(final JComboBox comboBox, final ImageIcon icon) {
        EventQueue.invokeLater(() -> {
            JTextField c = (JTextField) comboBox.getEditor().getEditorComponent();
            Insets is = c.getInsets();

            int ih = icon.getIconHeight();
            int ch = comboBox.getPreferredSize().height;
            //int ch = c.getPreferredSize().height; //Nimbus???
            int yy = Math.max((int) (.5 + (ch - ih) * .5), 0); //ch - ih > 0 ? (int) (.5 + (ch - ih) * .5) : 0;

            Border margin = BorderFactory.createEmptyBorder(0, icon.getIconWidth() + 2, 0, 2);
            c.setBorder(BorderFactory.createCompoundBorder(c.getBorder(), margin));

            JLabel label = new JLabel(icon);
            label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            label.setBorder(BorderFactory.createEmptyBorder());
            c.add(label);

            label.setBounds(is.left, yy, icon.getIconWidth(), icon.getIconHeight());
        });
    }
    public static void initComboBoxRenderer(JComboBox<String> combo, final ImageIcon icon) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setIcon(icon);
                return l;
            }
        });
    }
}
