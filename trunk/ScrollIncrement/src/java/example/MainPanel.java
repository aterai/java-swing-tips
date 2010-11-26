package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JTextArea textArea = new JTextArea();
    private final JScrollPane scrollPane = new JScrollPane();
    private final JLabel label = new JLabel("Unit Increment:");
    private final JSpinner spinner;
    private final SpinnerNumberModel model;
    private final JSpinner.NumberEditor editor;
    private final JButton button;
    private static final String LF = "\n";
    public MainPanel() {
        super(new BorderLayout());
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<100;i++) buf.append(i+LF);
        textArea.setText(buf.toString());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().add(textArea);
        model = new SpinnerNumberModel(scrollPane.getVerticalScrollBar().getUnitIncrement(1), 1, 100000, 1);
        spinner = new JSpinner(model);
        editor = new JSpinner.NumberEditor(spinner, "#####0");
        spinner.setEditor(editor);
        button = new JButton(new AbstractAction("init") {
            @Override public void actionPerformed(ActionEvent ae) {
                int value = ((Integer) spinner.getValue()).intValue();
                scrollPane.getVerticalScrollBar().setUnitIncrement(value);
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalStrut(2));
        box.add(spinner);
        box.add(Box.createHorizontalStrut(2));
        box.add(button);
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
