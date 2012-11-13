package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private static final String LF = "\n";
    public MainPanel() {
        super(new BorderLayout());
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<1000;i++) buf.append(i+LF);

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(new JScrollPane(new JTextArea(buf.toString())));

        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));
        sp.setRightComponent(new JScrollPane(new JTextArea(buf.toString())));

        sp.setResizeWeight(.5);
        add(sp);
    }
    private static final Dimension preferredSize = new Dimension(320, 240);
    @Override public Dimension getPreferredSize() {
        return preferredSize;
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
