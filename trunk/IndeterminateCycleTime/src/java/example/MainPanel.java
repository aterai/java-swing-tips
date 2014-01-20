package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel p = new JPanel(new GridBagLayout());
        p.add(progressBar);

        int cycleTime = UIManager.getInt("ProgressBar.cycleTime");
        final JSpinner cycleTimeSpinner = new JSpinner(new SpinnerNumberModel(cycleTime, 1000, 10000, 100));

        int repaintInterval = UIManager.getInt("ProgressBar.repaintInterval");
        final JSpinner repaintIntervalSpinner = new JSpinner(new SpinnerNumberModel(repaintInterval, 10, 100, 10));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("UIManager.put") {
            @Override public void actionPerformed(ActionEvent e) {
                progressBar.setIndeterminate(false);
                UIManager.put("ProgressBar.repaintInterval", (Integer)repaintIntervalSpinner.getValue());
                UIManager.put("ProgressBar.cycleTime", (Integer)cycleTimeSpinner.getValue());
                progressBar.setIndeterminate(true);
            }
        }));

        JPanel sp = new JPanel(new GridLayout(3,2,5,5));
        sp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        sp.add(new JLabel("ProgressBar.cycleTime:", JLabel.RIGHT));
        sp.add(cycleTimeSpinner);
        sp.add(new JLabel("ProgressBar.repaintInterval:", JLabel.RIGHT));
        sp.add(repaintIntervalSpinner);
        sp.add(Box.createHorizontalStrut(5));
        sp.add(box);



        add(sp, BorderLayout.NORTH);
        add(p);
        //add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
