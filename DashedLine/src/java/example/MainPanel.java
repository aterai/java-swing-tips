package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField field = new JTextField("1.0f, 1.0f, 5.0f, 1.0f");
    private final JButton button;
    private final JLabel label;
    private float[] getDashList() {
        StringTokenizer st = new StringTokenizer(field.getText(), ",");
        float[] list = new float[st.countTokens()];
        int i = 0;
//         try{
        while(st.hasMoreTokens()) {
            list[i] = Float.valueOf(st.nextToken());
            i++;
        }
        if(i==0) {
            list = new float[] { 1f };
        }
//         }catch(NumberFormatException nfe) {
//             Toolkit.getDefaultToolkit().beep();
//             JOptionPane.showMessageDialog(label, "Invalid input.\n"+nfe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//             float[] fd = {1.0f};
//             return fd;
//         }
        return list;
    }

    private BasicStroke dashedStroke;
    public MainPanel() {
        super(new BorderLayout());
        button = new JButton(new AbstractAction("Change") {
            @Override public void actionPerformed(ActionEvent ae) {
                dashedStroke = null;
                label.repaint();
            }
        });
        label = new JLabel() {
            @Override public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                super.paintComponent(g2);
                if(dashedStroke==null) {
                    dashedStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, getDashList(), 0.0f);
                }
                g2.setStroke(dashedStroke);
                g2.drawLine(5, label.getHeight()/2, label.getWidth()-10, label.getHeight()/2);
            }
        };
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel p = new JPanel(new BorderLayout(2,2));
        p.add(field); p.add(button, BorderLayout.EAST);
        p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));

        add(p, BorderLayout.NORTH);
        add(label);
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
