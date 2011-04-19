package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.jnlp.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 200));
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
        final JFrame frame = new JFrame("@title@");
        try{
            SingleInstanceService singleInstanceService = (SingleInstanceService)ServiceManager.lookup("javax.jnlp.SingleInstanceService");
            singleInstanceService.addSingleInstanceListener(new SingleInstanceListener() {
                private int count = 0;
                @Override public void newActivation(String[] args) {
                    //System.out.println(EventQueue.isDispatchThread());
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            JOptionPane.showMessageDialog(frame, "already running: "+count);
                            frame.setTitle("title:"+count);
                            count++;
                        }
                    });
                }
            });
        } catch(UnavailableServiceException use) {
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
