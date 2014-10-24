package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
//import com.sun.tools.attach.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
//     private static final IAppInstanceCounter counter = new JVMDescriptorInstanceCounter();
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
//         if (counter.getInstanceCount() > 1) {
//             JOptionPane.showMessageDialog(null, "An instance of the application is already running...");
//             return;
//         }

        // Java Swing Hacks #68
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(38765);
        } catch (IOException e) {
            socket = null;
        }
        if (socket == null) {
            //String stag = "<html><center><br /><br /><br /><br /><br />";
            //String etag = "<br /><br /><br /><br /><br /><br /></center>";
            //JOptionPane.showMessageDialog(null, stag + "An instance of the application is already running..." + etag);
            //System.exit(0);
            JOptionPane.showMessageDialog(null, "An instance of the application is already running...");
            return;
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//------------------------------------------------------
//Attach API
//> http://d.hatena.ne.jp/Kazzz/20071221/p1
// interface IAppInstanceCounter {
//     int getInstanceCount();
// }
//
// class JVMDescriptorInstanceCounter implements IAppInstanceCounter {
//     private final String mainclassName;
//     public JVMDescriptorInstanceCounter() {
//         StackTraceElement[] traces = Thread.currentThread().getStackTrace();
//         mainclassName = traces[traces.length-1].getClassName();
//         //System.out.println(mainclassName);
//     }
//     public int getInstanceCount() {
//         return FinderUtil.findAll(
//             VirtualMachine.list(),
//             new IPredicate<VirtualMachineDescriptor>() {
//                 @Override public boolean evaluate(VirtualMachineDescriptor input) {
//                     return input.displayName().equals(mainclassName);
//                 }
//             }).size();
//     }
// }
//
// interface IPredicate<T> {
//    boolean evaluate(T input);
// }
//
// final class FinderUtil {
//     public static final <T> List<T> findAll(List<T> list, IPredicate<T> match) {
//         List<T> temp = new ArrayList<T>();
//         for (T t: list) {
//             if (match.evaluate(t)) {
//                 temp.add(t);
//             }
//         }
//         return temp;
//     }
// }

//------------------------------------------------------
// class PseudoFileSemaphoreCounter implements IAppInstanceCounter {
//     private PseudoFileSemaphore semaphore;
//     private int launchLimit;
//     public PseudoFileSemaphoreCounter(String appName, int launchLimit) {
//         this.semaphore = new PseudoFileSemaphore(appName, launchLimit);
//         this.launchLimit = launchLimit;
//         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//             @Override public void run() {
//                 semaphore.release();
//             }
//         }));
//     }
//     @Override public int getInstanceCount() {
//         int result = this.semaphore.tryAcquire();
//         return (result != 0) ? result : this.launchLimit + 1;
//     }
// }
