package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private final Timer animator;
    private final JButton button;
    private final Toolkit tk    = Toolkit.getDefaultToolkit();
    private final Cursor[] list = new Cursor[3];
    private final URL url00     = getClass().getResource("00.png");
    private final URL url01     = getClass().getResource("01.png");
    private final URL url02     = getClass().getResource("02.png");
    public MainPanel() {
        super(new BorderLayout());
        Point pt = new Point(0, 0);
        list[0] = tk.createCustomCursor(tk.createImage(url00), pt, "00");
        list[1] = tk.createCustomCursor(tk.createImage(url01), pt, "01");
        list[2] = tk.createCustomCursor(tk.createImage(url02), pt, "02");
        animator = new Timer(100, new ActionListener() {
            private int counter;
            @Override public void actionPerformed(ActionEvent e) {
                button.setCursor(list[counter]);
                counter = (counter + 1 + list.length) % list.length;
            }
        });
        button = new JButton(new AbstractAction("Start") {
            @Override public void actionPerformed(ActionEvent e) {
                JButton b = (JButton)e.getSource();
                if(animator.isRunning()) {
                    b.setText("Start");
                    animator.stop();
                }else{
                    b.setText("Stop");
                    animator.start();
                }
            }
        });
        button.setCursor(list[0]);
        button.setPreferredSize(new Dimension(0, 100));
        button.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                JButton button = (JButton)e.getSource();
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && animator!=null && !button.isDisplayable()) {
                    animator.stop();
                }
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("delay=100ms"));
        p.add(button);
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 180));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
