package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import static javax.swing.GroupLayout.Alignment;

public final class MainPanel extends JPanel {
    private final JTextField tf1 = new JTextField();
    private final JTextField tf2 = new JTextField();
    private final JTextField tf3 = new JTextField();
    private final JTextField tf4 = new JTextField();
    private final JLabel label1  = new JLabel("0123456789_0123456789abc:");
    private final JLabel label2  = new JLabel("GroupLayout:");
    private final JLabel label3  = new JLabel("0123456789_0123456789abc:");
    private final JLabel label4  = new JLabel("GridBagLayout:");

    public MainPanel() {
        super(new GridLayout(2, 1));

        //GroupLayout
        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("GroupLayout"));
        GroupLayout layout = new GroupLayout(p1);
        p1.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup().addComponent(label1).addComponent(label2));
        hGroup.addGroup(layout.createParallelGroup().addComponent(tf1).addComponent(tf2));
        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label1).addComponent(tf1));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label2).addComponent(tf2));
        layout.setVerticalGroup(vGroup);

        //GridBagLayout
        JPanel p2 = new JPanel(new GridBagLayout());
        Border inside  = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
        Border outside = BorderFactory.createTitledBorder("GridBagLayout");
        p2.setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p2.add(label3, c);
        c.gridy   = 1; p2.add(label4, c);

        c.gridx   = 1;
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p2.add(tf3, c);
        c.gridy   = 1; p2.add(tf4, c);

        add(p1);
        add(p2);
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
