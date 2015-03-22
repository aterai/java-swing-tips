package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final int SIZE = 6;
    private MainPanel() {
        super(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Brick Layout"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.weightx = 1d; c.weighty = 0d;
        for (int i = 0; i < SIZE; i++) {
            int x = i & 1; //= (i % 2 == 0) ? 0 : 1;
            for (int j = 0; j < SIZE; j++) {
                c.gridy = i;
                c.gridx = 2 * j + x;
                c.gridwidth = 2;
                panel.add(new JButton(" "), c);
            }
        }
        //<blockquote cite="https://community.oracle.com/thread/1357310"
        //           title="GridBagLayout to create a board">
        //<dummy-row>
        c.gridwidth = 1;
        c.gridy = 10;
        for (c.gridx = 0; c.gridx <= 2 * SIZE; c.gridx++) {
            panel.add(Box.createHorizontalStrut(24), c);
        }
        //</dummy-row>
        //</blockquote>

        add(panel);
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
        frame.setMinimumSize(new Dimension(300, 120));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
