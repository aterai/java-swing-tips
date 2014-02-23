package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.beans.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        try {
            Thread.sleep(3000); //dummy task
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        System.out.println("main start / EDT: " + EventQueue.isDispatchThread());
        createAndShowGUI();
        System.out.println("main end");
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        final JFrame frame = new JFrame("@title@");
        final JDialog splashScreen  = new JDialog(frame, Dialog.ModalityType.DOCUMENT_MODAL);
        final JProgressBar progress = new JProgressBar();

        System.out.println(splashScreen.getModalityType());

        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                splashScreen.setUndecorated(true);
                splashScreen.getContentPane().add(new JLabel(new ImageIcon(getClass().getResource("splash.png"))));
                splashScreen.getContentPane().add(progress, BorderLayout.SOUTH);
                splashScreen.pack();
                splashScreen.setLocationRelativeTo(null);
                splashScreen.setVisible(true);
            }
        });
        SwingWorker<Void, Void> worker = new Task() {
            @Override public void done() {
                splashScreen.dispose();
            }
        };
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                if ("progress".equals(e.getPropertyName())) {
                    progress.setValue((Integer) e.getNewValue());
                }
            }
        });
        worker.execute();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                frame.setVisible(true);
            }
        });
    }
}

class Task extends SwingWorker<Void, Void> {
    @Override public Void doInBackground() {
        int current = 0;
        int lengthOfTask = 120;
        while (current < lengthOfTask && !isCancelled()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                return null;
            }
            setProgress(100 * current++ / lengthOfTask);
        }
        return null;
    }
}
