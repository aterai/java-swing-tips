// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.*;
// import com.sun.tools.attach.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  // private static final IAppInstanceCounter counter = new JVMDescriptorInstanceCounter();
  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    // if (counter.getInstanceCount() > 1) {
    //   JOptionPane.showMessageDialog(null, "An instance of the application is already running...");
    //   return;
    // }

    SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
    // Java Swing Hacks #68
    try (ServerSocket socket = new ServerSocket(38_765)) {
      JFrame frame = new JFrame("@title@");
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.getContentPane().add(new MainPanel());
      frame.pack();
      frame.setResizable(false);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
      frame.addWindowListener(new WindowAdapter() {
        @Override public void windowClosing(WindowEvent e) {
          loop.exit();
        }
      });
      loop.enter();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(null, "An instance of the application is already running...");
    }
  }
}

// // Attach API
// // http://d.hatena.ne.jp/Kazzz/20071221/p1
// interface IAppInstanceCounter {
//   int getInstanceCount();
// }
//
// class JVMDescriptorInstanceCounter implements IAppInstanceCounter {
//   private final String mainclassName;
//   protected JVMDescriptorInstanceCounter() {
//     StackTraceElement[] traces = Thread.currentThread().getStackTrace();
//     mainclassName = traces[traces.length-1].getClassName();
//     // System.out.println(mainclassName);
//   }
//   public int getInstanceCount() {
//     return FinderUtil.findAll(
//       VirtualMachine.list(),
//       new IPredicate<VirtualMachineDescriptor>() {
//         @Override public boolean evaluate(VirtualMachineDescriptor input) {
//           return input.displayName().equals(mainclassName);
//         }
//       }).size();
//   }
// }
//
// interface IPredicate<T> {
//  boolean evaluate(T input);
// }
//
// final class FinderUtil {
//   public static final <T> List<T> findAll(List<T> list, IPredicate<T> match) {
//     List<T> temp = new ArrayList<>();
//     for (T t: list) {
//       if (match.evaluate(t)) {
//         temp.add(t);
//       }
//     }
//     return temp;
//   }
// }

// class PseudoFileSemaphoreCounter implements IAppInstanceCounter {
//   private PseudoFileSemaphore semaphore;
//   private int launchLimit;
//   protected PseudoFileSemaphoreCounter(String appName, int launchLimit) {
//     this.semaphore = new PseudoFileSemaphore(appName, launchLimit);
//     this.launchLimit = launchLimit;
//     Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//       @Override public void run() {
//         semaphore.release();
//       }
//     }));
//   }
//   @Override public int getInstanceCount() {
//     int result = this.semaphore.tryAcquire();
//     return result != 0 ? result : this.launchLimit + 1;
//   }
// }
