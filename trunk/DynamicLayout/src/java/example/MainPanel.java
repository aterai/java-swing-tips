package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JLabel label = new JLabel("", JLabel.CENTER);
    public MainPanel() {
        super(new BorderLayout());
        label.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                label.setText(label.getSize().toString());
            }
        });
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        final JCheckBox cbox = new JCheckBox("DynamicLayout", true);
        cbox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent ie) {
                Toolkit.getDefaultToolkit().setDynamicLayout(cbox.isSelected());
            }
        });
        add(label);
        add(cbox,  BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
        setMinimumSize(new Dimension(300, 150));
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
        final int mw = 256;
        final int mh = 100;
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int fw = frame.getSize().width;
                int fh = frame.getSize().height;
                frame.setSize(mw>fw?mw:fw, mh>fh?mh:fh);
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
