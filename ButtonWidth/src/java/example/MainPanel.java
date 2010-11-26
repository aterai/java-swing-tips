package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JButton button1   = new JButton("showErrorDialog");
    private final JButton button2   = new JButton("exit");
    private final JButton button3   = new JButton("showErrorDialog");
    private final JButton button4   = new JButton("exit");
    private final JTextField field = new JTextField(15);
    private final JComboBox combo  = new JComboBox();
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        button1.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        button3.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
        button4.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

//         System.out.println("combo  getPreferredSize"+combo.getPreferredSize().toString());
//         System.out.println("text   getPreferredSize"+field.getPreferredSize().toString());
//         System.out.println("button getPreferredSize"+button.getPreferredSize().toString());
//         System.out.println("----------------------------------------------------------");
//         System.out.println("combo  getSize"+combo.getSize().toString());
//         System.out.println("text   getSize"+field.getSize().toString());
//         System.out.println("button getSize"+button.getSize().toString());
//         System.out.println("----------------------------------------------------------");

        Dimension dim = button1.getPreferredSize();
        button1.setPreferredSize(new Dimension(120, dim.height));
        button2.setPreferredSize(new Dimension(120, dim.height));

        Box box1 = Box.createHorizontalBox();
        box1.add(Box.createHorizontalGlue());
        box1.add(button1);
        box1.add(Box.createHorizontalStrut(5));
        box1.add(button2);
        box1.add(Box.createHorizontalStrut(5));
        box1.add(Box.createRigidArea(new Dimension(0, dim.height+10)));

        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalGlue());
        box2.add(button3);
        box2.add(Box.createHorizontalStrut(5));
        box2.add(button4);
        box2.add(Box.createHorizontalStrut(5));
        box2.add(Box.createRigidArea(new Dimension(0, dim.height+10)));

        Box box = Box.createVerticalBox();
        box.add(new JSeparator());
        box.add(box2);
        box.add(Box.createVerticalGlue());
        box.add(new JSeparator());
        box.add(box1);

        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 180));
    }

    private void buttonActionPerformed(ActionEvent e) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, "error message", "title", JOptionPane.ERROR_MESSAGE);
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
