package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("0x01FF0000");
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel();
        p.add(check);
        p.add(new JTextField(10));
        p.add(new JButton(new AbstractAction("Stop 5sec") {
            @Override public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor(getRootPane());
                JDialog dialog = new JDialog(w, Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setUndecorated(true);
                dialog.setBounds(w.getBounds());
                dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                int color = check.isSelected() ? 0x22FF0000 : 0x01FF0000;
                dialog.setBackground(new Color(color, true));
                (new Task() {
                    @Override public void done() {
                        if (!isDisplayable()) {
                            cancel(true);
                            return;
                        }
                        dialog.setVisible(false);
                    }
                }).execute();
                //TEST:
                //JPanel p = new JPanel(new GridBagLayout());
                //p.setOpaque(false);
                //p.add(new JButton(new AbstractAction("cancel") {
                //    @Override public void actionPerformed(ActionEvent e) {
                //        task.cancel(true);
                //    }
                //}));
                //dialog.add(p);
                dialog.setVisible(true);
            }
        }));

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea(100, 80)));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, Void> {
    @Override public String doInBackground() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
            System.out.println("Interrupted");
        }
        return "Done";
    }
}
