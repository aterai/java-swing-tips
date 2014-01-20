package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 2));
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));

        JTextArea textArea = new JTextArea();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<2000;i++) {
            sb.append(String.format("%04d%n", i));
        }
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        add(makePanel(textArea));
        add(makePanel(new JTable(100, 3)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(JComponent c) {
        final JComponent check = new JCheckBox("JCheckBox");
        check.setEnabled(false);
        JPanel p = new JPanel(new BorderLayout());
        JScrollPane scroll = new JScrollPane(c);
        scroll.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = (BoundedRangeModel)e.getSource();
                int extent  = m.getExtent();
                int maximum = m.getMaximum();
                int value   = m.getValue();
                //http://stackoverflow.com/questions/12916192/how-to-know-if-a-jscrollbar-has-reached-the-bottom-of-the-jscrollpane
                //System.out.println("2. Value: " + (value + extent) + " Max: " + maximum);
                //http://terai.xrea.jp/Swing/ScrollBarAsSlider.html
                if(value + extent >= maximum) {
                    check.setEnabled(true);
                }
            }
        });
        p.add(scroll);
        p.add(check, BorderLayout.SOUTH);
        return p;
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
