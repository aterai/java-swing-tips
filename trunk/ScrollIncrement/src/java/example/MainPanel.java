package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String LF = "\n";
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<100;i++) {
            String s = i + LF;
            buf.append(s);
        }

        final JScrollPane scrollPane = new JScrollPane(new JTextArea(buf.toString()));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(scrollPane.getVerticalScrollBar().getUnitIncrement(1), 1, 100000, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#####0"));
        spinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner)e.getSource();
                scrollPane.getVerticalScrollBar().setUnitIncrement((Integer)s.getValue());
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Unit Increment:"));
        box.add(Box.createHorizontalStrut(2));
        box.add(spinner);
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        add(scrollPane);
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
