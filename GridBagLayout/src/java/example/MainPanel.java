package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final int GAP = 5;
    private final JComboBox<String> combo1 = new JComboBox<>(new String[] {"aaaaaa", "bbbbb"});
    private final JComboBox<String> combo2 = new JComboBox<>(new String[] {"cccccccc", "ddd"});
    private final JButton button1 = new JButton("Open");
    private final JButton button2 = new JButton("Open");

    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(20));
        box.add(createCompButtonPanel1(combo1, button1, " BorderLayout:"));
        box.add(Box.createVerticalStrut(20));
        box.add(createCompButtonPanel2(combo2, button2, "GridBagLayout:"));
        box.add(Box.createVerticalStrut(20));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JPanel createCompButtonPanel1(JComponent cmp, JButton btn, String str) {
        JPanel panel = new JPanel(new BorderLayout(GAP, GAP));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP,GAP,GAP,GAP));
        panel.add(new JLabel(str), BorderLayout.WEST);
        panel.add(cmp);
        panel.add(btn, BorderLayout.EAST);
        Dimension d = panel.getPreferredSize();
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        return panel;
    }

    public static JPanel createCompButtonPanel2(JComponent cmp, JButton btn, String str) {
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(GAP, GAP, GAP, 0);
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(str), c);

        c.gridx = 1;
        c.weightx = 1.0;
        //c.insets = new Insets(GAP, GAP, GAP, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmp, c);

        c.gridx = 2;
        c.weightx = 0.0;
        c.insets = new Insets(GAP, GAP, GAP, GAP);
        c.anchor = GridBagConstraints.WEST;
        panel.add(btn, c);

        return panel;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setMinimumSize(new Dimension(300, 120));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
