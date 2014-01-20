package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        try{
            Thread.sleep(5000); //dummy task
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        System.out.println("main start / EDT: "+EventQueue.isDispatchThread());
        createAndShowGUI();
        System.out.println("main end");
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        final JWindow splashScreen = new JWindow();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                System.out.println("splashScreen show start / EDT: "+EventQueue.isDispatchThread());
                ImageIcon img = new ImageIcon(MainPanel.class.getResource("splash.png"));
                splashScreen.getContentPane().add(new JLabel(img));
                splashScreen.pack();
                splashScreen.setLocationRelativeTo(null);
                splashScreen.setVisible(true);
                System.out.println("splashScreen show end");
            }
        });

        System.out.println("createGUI start / EDT: "+EventQueue.isDispatchThread());
        final JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel()); //new MainPanel() take long time
        frame.pack();
        frame.setLocationRelativeTo(null);
        System.out.println("createGUI end");

        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                System.out.println("    splashScreen dispose start / EDT: "+EventQueue.isDispatchThread());
                //splashScreen.setVisible(false);
                splashScreen.dispose();
                System.out.println("    splashScreen dispose end");

                System.out.println("  frame show start / EDT: "+EventQueue.isDispatchThread());
                frame.setVisible(true);
                System.out.println("  frame show end");
            }
        });
    }
}

//     public static void main(String[] args) {
//         System.out.println("main start / EDT: "+EventQueue.isDispatchThread());
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 createAndShowGUI();
//             }
//         });
//         System.out.println("main end");
//     }
//     public static void createAndShowGUI() {
//         try{
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         }catch(Exception e) {
//             e.printStackTrace();
//         }
//         System.out.println("splashScreen show start / EDT: "+EventQueue.isDispatchThread());
//         final JWindow splashScreen = new JWindow();
//         ImageIcon img = new ImageIcon(MainPanel.class.getResource("splash.png"));
//         splashScreen.getContentPane().add(new JLabel(img));
//         splashScreen.pack();
//         splashScreen.setLocationRelativeTo(null);
//         splashScreen.setVisible(true);
//         System.out.println("splashScreen show end");
//
//         final JFrame frame = new JFrame("@title@");
//         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//         new SwingWorker() {
//             @Override public Object doInBackground() {
//                 System.out.println("frame make start / EDT: "+EventQueue.isDispatchThread());
//                 frame.getContentPane().add(new MainPanel()); //new MainPanel() take long time
//                 System.out.println("frame make end");
//                 return "Done";
//             }
//             @Override public void done() {
//                 System.out.println("splashScreen dispose start / EDT: "+EventQueue.isDispatchThread());
//                 splashScreen.dispose();
//                 System.out.println("splashScreen dispose end");
//                 System.out.println("frame show start / EDT: "+EventQueue.isDispatchThread());
//                 frame.pack();
//                 frame.setLocationRelativeTo(null);
//                 frame.setVisible(true);
//                 System.out.println("frame show end");
//             }
//         }.execute();
//     }
// }
