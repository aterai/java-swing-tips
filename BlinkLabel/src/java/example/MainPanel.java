package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected final JLabel label1 = new JLabel("\u25CF", SwingConstants.CENTER);
    protected final JLabel label2 = new JLabel("", SwingConstants.CENTER);
    protected final Timer timer1 = new Timer(600, new ActionListener() {
        protected boolean flg = true;
        @Override public void actionPerformed(ActionEvent e) {
            flg ^= true;
            label1.setText(flg ? "\u25CB" : "\u25CF");
        }
    });
    protected final Timer timer2 = new Timer(300, new ActionListener() {
        protected boolean flg = true;
        @Override public void actionPerformed(ActionEvent e) {
            flg ^= true;
            label2.setText(flg ? "!!!Warning!!!" : "");
        }
    });

    public MainPanel() {
        super(new BorderLayout());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (e.getComponent().isDisplayable()) {
                    timer1.start();
                    timer2.start();
                } else {
                    timer1.stop();
                    timer2.stop();
                }
            }
        });
        JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
        p.add(makePanel("\u25CB<->\u25CF", label1));
        p.add(makePanel("!!!Warning!!!<->Empty", label2));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
