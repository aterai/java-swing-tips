package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.jnlp.*;
import javax.swing.*;

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
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        final JFrame frame = new JFrame("@title@");
        try {
            SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
            singleInstanceService.addSingleInstanceListener(new SingleInstanceListener() {
                private int count;
                @Override public void newActivation(String... args) {
                    //System.out.println(EventQueue.isDispatchThread());
                    EventQueue.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame, "already running: " + count);
                        frame.setTitle("title:" + count);
                        count++;
                    });
                }
            });
        } catch (UnavailableServiceException use) {
            use.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
