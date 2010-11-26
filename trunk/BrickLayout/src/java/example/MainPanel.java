package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        add(createBrickLayoutPanel());
        setPreferredSize(new Dimension(320, 200));
    }
    private static int SIZE = 6;
    public JComponent createBrickLayoutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Brick Layout"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.weightx = 1.0; c.weighty = 0.0;
        for(int i=0;i<SIZE;i++) {
            int x = i & 1; //= (i%2==0)?0:1;
            for(int j=0;j<SIZE;j++) {
                c.gridy = i;
                c.gridx = 2*j+x;
                c.gridwidth = 2;
                panel.add(new JButton(" "),c);
            }
        }
        //<blockquote cite="http://forums.sun.com/thread.jspa?threadID=5364641">
        //<dummy-row>
        c.gridwidth = 1;
        c.gridy = 10;
        for(c.gridx=0; c.gridx<=2*SIZE; c.gridx++)
          panel.add(Box.createHorizontalStrut(24), c);
        //</dummy-row>
        //</blockquote>
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
        }catch(Exception e) {
            e.printStackTrace();
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
