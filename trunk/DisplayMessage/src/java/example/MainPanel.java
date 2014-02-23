package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final PopupMenu popup = new PopupMenu();
    private final transient SystemTray tray = SystemTray.getSystemTray();
    private final transient Image image     = new ImageIcon(getClass().getResource("16x16.png")).getImage();
    private final transient TrayIcon icon   = new TrayIcon(image, "TRAY", popup);
    private final JTextArea log   = new JTextArea();
    private final JComboBox<TrayIcon.MessageType> messageType = new JComboBox<>(TrayIcon.MessageType.values()); //ERROR, WARNING, INFO, NONE
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());

        MenuItem item1 = new MenuItem("OPEN");
        item1.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
        MenuItem item2 = new MenuItem("EXIT");
        item2.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tray.remove(icon);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.dispose();
                //System.exit(0);
            }
        });
        popup.add(item1);
        popup.add(item2);

        JPanel p = new JPanel();
        p.add(messageType);
        p.add(new JButton(new AbstractAction("TrayIcon#displayMessage()") {
            @Override public void actionPerformed(ActionEvent e) {
                icon.displayMessage("caption", "text text text text", (TrayIcon.MessageType) messageType.getSelectedItem());
            }
        }));

        log.setEditable(false);
        icon.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //System.out.println(e);
                log.append(e.toString() + "\n");
            }
        });

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 240));
        if (!SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            return;
        }
        frame.addWindowStateListener(new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) {
                frame.dispose();
            }
        });
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//             for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels())
//               if ("Nimbus".equals(laf.getName()))
//                 UIManager.setLookAndFeel(laf.getClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
